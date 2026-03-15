package io.agentlens.langchain4j;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import io.agentlens.AgentLens;
import io.agentlens.domain.attributes.GenAiAttributes;
import io.agentlens.domain.cost.PricingRegistry;
import io.agentlens.domain.cost.TokenCost;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.SpanAttributes;
import io.agentlens.domain.trace.SpanType;

import java.util.List;
import java.util.Objects;

/**
 * 为 LangChain4j ChatLanguageModel 增加追踪能力的装饰器。
 * <p>
 * 在调用 delegate.generate 前后创建/结束 LLM Span，记录 Token 用量与成本，
 * 并可选截获消息内容（由 Builder 配置）。
 * </p>
 */
public class TracedChatLanguageModel implements ChatLanguageModel {

    private final ChatLanguageModel delegate;
    private final String modelName;
    private final String provider;
    private final boolean captureContent;
    private final boolean enableCostTracking;

    private TracedChatLanguageModel(Builder builder) {
        this.delegate = Objects.requireNonNull(builder.delegate, "delegate is required");
        this.modelName = builder.modelName != null ? builder.modelName : extractModelName(delegate);
        this.provider = builder.provider != null ? builder.provider : extractProvider(delegate);
        this.captureContent = builder.captureContent;
        this.enableCostTracking = builder.enableCostTracking;
    }

    public static Builder wrap(ChatLanguageModel model) {
        return new Builder(model);
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        if (!AgentLens.isInitialized()) {
            return delegate.generate(messages);
        }

        SpanAttributes attributes = SpanAttributes.builder()
            .put(GenAiAttributes.OPERATION_NAME, GenAiAttributes.Operations.CHAT)
            .put(GenAiAttributes.REQUEST_MODEL, modelName)
            .put(GenAiAttributes.PROVIDER_NAME, provider)
            .build();

        Span span = AgentLens.startSpan("ChatLanguageModel.generate", SpanType.LLM);
        span.addAttributes(attributes);

        if (captureContent) {
            span.setInput(formatMessages(messages));
        }

        try {
            Response<AiMessage> response = delegate.generate(messages);

            if (response != null) {
                TokenUsage usage = response.tokenUsage();
                if (usage != null) {
                    int inputTokens = usage.inputTokenCount() != null ? usage.inputTokenCount() : 0;
                    int outputTokens = usage.outputTokenCount() != null ? usage.outputTokenCount() : 0;

                    span.addAttribute(GenAiAttributes.USAGE_INPUT_TOKENS, inputTokens);
                    span.addAttribute(GenAiAttributes.USAGE_OUTPUT_TOKENS, outputTokens);
                    span.addAttribute(GenAiAttributes.USAGE_TOTAL_TOKENS, inputTokens + outputTokens);

                    if (enableCostTracking) {
                        TokenCost cost = PricingRegistry.getInstance()
                            .calculateCost(modelName, provider, inputTokens, outputTokens);
                        span.setCost(cost);
                        span.addAttribute(GenAiAttributes.COST_USD, cost.getCostUsd().doubleValue());
                    }
                }

                if (response.finishReason() != null) {
                    span.addAttribute(GenAiAttributes.RESPONSE_FINISH_REASONS, response.finishReason().toString());
                }

                if (captureContent && response.content() != null) {
                    span.setOutput(response.content().text());
                }
            }

            AgentLens.endSpan(span);
            return response;

        } catch (Exception e) {
            AgentLens.endSpanWithError(span, e);
            throw e;
        }
    }

    @Override
    public String generate(String userMessage) {
        return ChatLanguageModel.super.generate(userMessage);
    }

    private String formatMessages(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : messages) {
            sb.append("[").append(msg.type()).append("] ");
            sb.append(msg.text()).append("\n");
        }
        return sb.toString();
    }

    private static String extractModelName(ChatLanguageModel model) {
        String className = model.getClass().getSimpleName();
        if (className.contains("OpenAi")) return "gpt-4o";
        if (className.contains("Anthropic")) return "claude-3-5-sonnet";
        if (className.contains("Ollama")) return "ollama";
        if (className.contains("Qwen")) return "qwen-max";
        return "unknown";
    }

    private static String extractProvider(ChatLanguageModel model) {
        String className = model.getClass().getSimpleName().toLowerCase();
        if (className.contains("openai")) return GenAiAttributes.Providers.OPENAI;
        if (className.contains("anthropic")) return GenAiAttributes.Providers.ANTHROPIC;
        if (className.contains("ollama")) return GenAiAttributes.Providers.OLLAMA;
        if (className.contains("qwen")) return GenAiAttributes.Providers.DASHSCOPE;
        return "unknown";
    }

    public static class Builder {
        private final ChatLanguageModel delegate;
        private String modelName;
        private String provider;
        private boolean captureContent = true;
        private boolean enableCostTracking = true;

        private Builder(ChatLanguageModel delegate) {
            this.delegate = delegate;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder captureContent(boolean captureContent) {
            this.captureContent = captureContent;
            return this;
        }

        public Builder enableCostTracking(boolean enableCostTracking) {
            this.enableCostTracking = enableCostTracking;
            return this;
        }

        public TracedChatLanguageModel build() {
            return new TracedChatLanguageModel(this);
        }
    }
}
