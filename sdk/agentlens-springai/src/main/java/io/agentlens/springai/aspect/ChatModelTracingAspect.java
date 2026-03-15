package io.agentlens.springai.aspect;

import io.agentlens.AgentLens;
import io.agentlens.domain.attributes.GenAiAttributes;
import io.agentlens.domain.cost.PricingRegistry;
import io.agentlens.domain.cost.TokenCost;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.SpanAttributes;
import io.agentlens.domain.trace.SpanType;
import io.agentlens.springai.AgentLensSpringAiProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring AI ChatModel 调用追踪切面。
 * <p>
 * 拦截 ChatModel.call 与 ChatModel.stream，自动创建 LLM 类型 Span，
 * 记录模型/厂商、Token 用量与成本（若启用），并在异常时结束为 ERROR。
 * </p>
 */
@Aspect
public class ChatModelTracingAspect {

    private static final Logger log = LoggerFactory.getLogger(ChatModelTracingAspect.class);

    private final AgentLensSpringAiProperties properties;

    public ChatModelTracingAspect(AgentLensSpringAiProperties properties) {
        this.properties = properties;
    }

    @Around("execution(* org.springframework.ai.chat.model.ChatModel.call(..))")
    public Object traceChatModelCall(ProceedingJoinPoint pjp) throws Throwable {
        if (!AgentLens.isInitialized()) {
            return pjp.proceed();
        }

        String modelName = extractModelName(pjp);
        String provider = extractProvider(pjp);

        SpanAttributes attributes = SpanAttributes.builder()
            .put(GenAiAttributes.OPERATION_NAME, GenAiAttributes.Operations.CHAT)
            .put(GenAiAttributes.REQUEST_MODEL, modelName)
            .put(GenAiAttributes.PROVIDER_NAME, provider)
            .build();

        Span span = AgentLens.startSpan("ChatModel.call", SpanType.LLM);
        span.addAttributes(attributes);

        if (properties.isCaptureContent()) {
            Object[] args = pjp.getArgs();
            if (args.length > 0) {
                span.setInput(truncateContent(args[0]));
            }
        }

        try {
            Object result = pjp.proceed();

            if (result != null) {
                extractAndSetTokenUsage(span, result, modelName, provider);

                if (properties.isCaptureContent()) {
                    span.setOutput(truncateContent(result));
                }
            }

            AgentLens.endSpan(span);
            return result;

        } catch (Throwable e) {
            AgentLens.endSpanWithError(span, e);
            throw e;
        }
    }

    @Around("execution(* org.springframework.ai.chat.model.ChatModel.stream(..))")
    public Object traceChatModelStream(ProceedingJoinPoint pjp) throws Throwable {
        if (!AgentLens.isInitialized()) {
            return pjp.proceed();
        }

        String modelName = extractModelName(pjp);
        String provider = extractProvider(pjp);

        SpanAttributes attributes = SpanAttributes.builder()
            .put(GenAiAttributes.OPERATION_NAME, GenAiAttributes.Operations.CHAT)
            .put(GenAiAttributes.REQUEST_MODEL, modelName)
            .put(GenAiAttributes.PROVIDER_NAME, provider)
            .put("streaming", true)
            .build();

        Span span = AgentLens.startSpan("ChatModel.stream", SpanType.LLM);
        span.addAttributes(attributes);

        try {
            Object result = pjp.proceed();
            AgentLens.endSpan(span);
            return result;
        } catch (Throwable e) {
            AgentLens.endSpanWithError(span, e);
            throw e;
        }
    }

    private String extractModelName(ProceedingJoinPoint pjp) {
        Object target = pjp.getTarget();
        String className = target.getClass().getSimpleName();

        try {
            java.lang.reflect.Method getModelMethod = target.getClass().getMethod("getDefaultOptions");
            Object options = getModelMethod.invoke(target);
            if (options != null) {
                java.lang.reflect.Method getModel = options.getClass().getMethod("getModel");
                Object model = getModel.invoke(options);
                if (model != null) {
                    return model.toString();
                }
            }
        } catch (Exception e) {
            log.trace("Could not extract model name via reflection", e);
        }

        if (className.contains("OpenAi")) return "gpt-4o";
        if (className.contains("Anthropic")) return "claude-3-5-sonnet";
        if (className.contains("Ollama")) return "ollama";
        if (className.contains("Qwen") || className.contains("DashScope")) return "qwen-max";

        return "unknown";
    }

    private String extractProvider(ProceedingJoinPoint pjp) {
        String className = pjp.getTarget().getClass().getSimpleName().toLowerCase();

        if (className.contains("openai")) return GenAiAttributes.Providers.OPENAI;
        if (className.contains("anthropic")) return GenAiAttributes.Providers.ANTHROPIC;
        if (className.contains("ollama")) return GenAiAttributes.Providers.OLLAMA;
        if (className.contains("qwen") || className.contains("dashscope")) return GenAiAttributes.Providers.DASHSCOPE;
        if (className.contains("azure")) return GenAiAttributes.Providers.AZURE_OPENAI;
        if (className.contains("google") || className.contains("gemini")) return GenAiAttributes.Providers.GOOGLE;

        return "unknown";
    }

    private void extractAndSetTokenUsage(Span span, Object result, String modelName, String provider) {
        try {
            java.lang.reflect.Method getMetadata = result.getClass().getMethod("getMetadata");
            Object metadata = getMetadata.invoke(result);

            if (metadata != null) {
                java.lang.reflect.Method getUsage = metadata.getClass().getMethod("getUsage");
                Object usage = getUsage.invoke(metadata);

                if (usage != null) {
                    java.lang.reflect.Method getPromptTokens = usage.getClass().getMethod("getPromptTokens");
                    java.lang.reflect.Method getGenerationTokens = usage.getClass().getMethod("getGenerationTokens");

                    Long promptTokens = (Long) getPromptTokens.invoke(usage);
                    Long generationTokens = (Long) getGenerationTokens.invoke(usage);

                    int inputTokens = promptTokens != null ? promptTokens.intValue() : 0;
                    int outputTokens = generationTokens != null ? generationTokens.intValue() : 0;

                    span.addAttribute(GenAiAttributes.USAGE_INPUT_TOKENS, inputTokens);
                    span.addAttribute(GenAiAttributes.USAGE_OUTPUT_TOKENS, outputTokens);
                    span.addAttribute(GenAiAttributes.USAGE_TOTAL_TOKENS, inputTokens + outputTokens);

                    if (properties.isEnableCostTracking()) {
                        TokenCost cost = PricingRegistry.getInstance()
                            .calculateCost(modelName, provider, inputTokens, outputTokens);
                        span.setCost(cost);
                        span.addAttribute(GenAiAttributes.COST_USD, cost.getCostUsd().doubleValue());
                    }
                }
            }
        } catch (Exception e) {
            log.trace("Could not extract token usage", e);
        }
    }

    private Object truncateContent(Object content) {
        if (content == null) return null;

        String str = content.toString();
        if (str.length() > properties.getMaxContentLength()) {
            return str.substring(0, properties.getMaxContentLength()) + "... [truncated]";
        }
        return str;
    }
}
