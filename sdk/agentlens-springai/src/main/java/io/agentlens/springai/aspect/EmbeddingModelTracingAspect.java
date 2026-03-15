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
 * Spring AI EmbeddingModel 调用追踪切面。
 * <p>
 * 拦截 embed/embedAll 与 call，创建 EMBEDDING 类型 Span，记录模型、Token、维度与成本。
 * </p>
 */
@Aspect
public class EmbeddingModelTracingAspect {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingModelTracingAspect.class);

    private final AgentLensSpringAiProperties properties;

    public EmbeddingModelTracingAspect(AgentLensSpringAiProperties properties) {
        this.properties = properties;
    }

    @Around("execution(* org.springframework.ai.embedding.EmbeddingModel.embed(..))")
    public Object traceEmbedding(ProceedingJoinPoint pjp) throws Throwable {
        if (!AgentLens.isInitialized()) {
            return pjp.proceed();
        }

        String modelName = extractModelName(pjp);
        String provider = extractProvider(pjp);

        SpanAttributes attributes = SpanAttributes.builder()
            .put(GenAiAttributes.OPERATION_NAME, GenAiAttributes.Operations.EMBEDDINGS)
            .put(GenAiAttributes.REQUEST_MODEL, modelName)
            .put(GenAiAttributes.PROVIDER_NAME, provider)
            .build();

        Span span = AgentLens.startSpan("EmbeddingModel.embed", SpanType.EMBEDDING);
        span.addAttributes(attributes);

        Object[] args = pjp.getArgs();
        if (args.length > 0) {
            if (args[0] instanceof java.util.List<?> list) {
                span.addAttribute("input_count", list.size());
            } else {
                span.addAttribute("input_count", 1);
            }
        }

        try {
            Object result = pjp.proceed();

            if (result != null) {
                extractAndSetEmbeddingInfo(span, result, modelName, provider);
            }

            AgentLens.endSpan(span);
            return result;

        } catch (Throwable e) {
            AgentLens.endSpanWithError(span, e);
            throw e;
        }
    }

    @Around("execution(* org.springframework.ai.embedding.EmbeddingModel.call(..))")
    public Object traceEmbeddingCall(ProceedingJoinPoint pjp) throws Throwable {
        if (!AgentLens.isInitialized()) {
            return pjp.proceed();
        }

        String modelName = extractModelName(pjp);
        String provider = extractProvider(pjp);

        SpanAttributes attributes = SpanAttributes.builder()
            .put(GenAiAttributes.OPERATION_NAME, GenAiAttributes.Operations.EMBEDDINGS)
            .put(GenAiAttributes.REQUEST_MODEL, modelName)
            .put(GenAiAttributes.PROVIDER_NAME, provider)
            .build();

        Span span = AgentLens.startSpan("EmbeddingModel.call", SpanType.EMBEDDING);
        span.addAttributes(attributes);

        try {
            Object result = pjp.proceed();

            if (result != null) {
                extractAndSetEmbeddingInfo(span, result, modelName, provider);
            }

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

        if (className.contains("OpenAi")) return "text-embedding-3-small";
        if (className.contains("Ollama")) return "nomic-embed-text";
        if (className.contains("Qwen") || className.contains("DashScope")) return "text-embedding-v3";

        return "unknown";
    }

    private String extractProvider(ProceedingJoinPoint pjp) {
        String className = pjp.getTarget().getClass().getSimpleName().toLowerCase();

        if (className.contains("openai")) return GenAiAttributes.Providers.OPENAI;
        if (className.contains("ollama")) return GenAiAttributes.Providers.OLLAMA;
        if (className.contains("qwen") || className.contains("dashscope")) return GenAiAttributes.Providers.DASHSCOPE;

        return "unknown";
    }

    private void extractAndSetEmbeddingInfo(Span span, Object result, String modelName, String provider) {
        try {
            java.lang.reflect.Method getMetadata = result.getClass().getMethod("getMetadata");
            Object metadata = getMetadata.invoke(result);

            if (metadata != null) {
                java.lang.reflect.Method getUsage = metadata.getClass().getMethod("getUsage");
                Object usage = getUsage.invoke(metadata);

                if (usage != null) {
                    java.lang.reflect.Method getPromptTokens = usage.getClass().getMethod("getPromptTokens");
                    Long promptTokens = (Long) getPromptTokens.invoke(usage);

                    int inputTokens = promptTokens != null ? promptTokens.intValue() : 0;

                    span.addAttribute(GenAiAttributes.USAGE_INPUT_TOKENS, inputTokens);

                    if (properties.isEnableCostTracking()) {
                        TokenCost cost = PricingRegistry.getInstance()
                            .calculateCost(modelName, provider, inputTokens, 0);
                        span.setCost(cost);
                        span.addAttribute(GenAiAttributes.COST_USD, cost.getCostUsd().doubleValue());
                    }
                }
            }

            java.lang.reflect.Method getOutput = result.getClass().getMethod("getOutput");
            Object output = getOutput.invoke(result);
            if (output instanceof java.util.List<?> embeddings && !embeddings.isEmpty()) {
                Object firstEmbedding = embeddings.get(0);
                java.lang.reflect.Method getEmbedding = firstEmbedding.getClass().getMethod("getOutput");
                Object vector = getEmbedding.invoke(firstEmbedding);
                if (vector instanceof float[] arr) {
                    span.addAttribute("vector.dimension", arr.length);
                }
            }

        } catch (Exception e) {
            log.trace("Could not extract embedding info", e);
        }
    }
}
