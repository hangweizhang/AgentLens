package io.agentlens.langchain4j;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
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
 * 为 LangChain4j EmbeddingModel 增加追踪能力的装饰器。
 * <p>
 * 在 embedAll 调用前后创建/结束 EMBEDDING Span，记录 Token、维度与成本。
 * </p>
 */
public class TracedEmbeddingModel implements EmbeddingModel {

    private final EmbeddingModel delegate;
    private final String modelName;
    private final String provider;
    private final boolean enableCostTracking;

    private TracedEmbeddingModel(Builder builder) {
        this.delegate = Objects.requireNonNull(builder.delegate, "delegate is required");
        this.modelName = builder.modelName != null ? builder.modelName : extractModelName(delegate);
        this.provider = builder.provider != null ? builder.provider : extractProvider(delegate);
        this.enableCostTracking = builder.enableCostTracking;
    }

    public static Builder wrap(EmbeddingModel model) {
        return new Builder(model);
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        if (!AgentLens.isInitialized()) {
            return delegate.embedAll(textSegments);
        }

        SpanAttributes attributes = SpanAttributes.builder()
            .put(GenAiAttributes.OPERATION_NAME, GenAiAttributes.Operations.EMBEDDINGS)
            .put(GenAiAttributes.REQUEST_MODEL, modelName)
            .put(GenAiAttributes.PROVIDER_NAME, provider)
            .put("input_count", textSegments.size())
            .build();

        Span span = AgentLens.startSpan("EmbeddingModel.embedAll", SpanType.EMBEDDING);
        span.addAttributes(attributes);

        try {
            Response<List<Embedding>> response = delegate.embedAll(textSegments);

            if (response != null) {
                TokenUsage usage = response.tokenUsage();
                if (usage != null) {
                    int inputTokens = usage.inputTokenCount() != null ? usage.inputTokenCount() : 0;

                    span.addAttribute(GenAiAttributes.USAGE_INPUT_TOKENS, inputTokens);

                    if (enableCostTracking) {
                        TokenCost cost = PricingRegistry.getInstance()
                            .calculateCost(modelName, provider, inputTokens, 0);
                        span.setCost(cost);
                        span.addAttribute(GenAiAttributes.COST_USD, cost.getCostUsd().doubleValue());
                    }
                }

                List<Embedding> embeddings = response.content();
                if (embeddings != null && !embeddings.isEmpty()) {
                    span.addAttribute("output_count", embeddings.size());
                    span.addAttribute("vector.dimension", embeddings.get(0).dimension());
                }
            }

            AgentLens.endSpan(span);
            return response;

        } catch (Exception e) {
            AgentLens.endSpanWithError(span, e);
            throw e;
        }
    }

    private static String extractModelName(EmbeddingModel model) {
        String className = model.getClass().getSimpleName();
        if (className.contains("OpenAi")) return "text-embedding-3-small";
        if (className.contains("Ollama")) return "nomic-embed-text";
        if (className.contains("Qwen")) return "text-embedding-v3";
        return "unknown";
    }

    private static String extractProvider(EmbeddingModel model) {
        String className = model.getClass().getSimpleName().toLowerCase();
        if (className.contains("openai")) return GenAiAttributes.Providers.OPENAI;
        if (className.contains("ollama")) return GenAiAttributes.Providers.OLLAMA;
        if (className.contains("qwen")) return GenAiAttributes.Providers.DASHSCOPE;
        return "unknown";
    }

    public static class Builder {
        private final EmbeddingModel delegate;
        private String modelName;
        private String provider;
        private boolean enableCostTracking = true;

        private Builder(EmbeddingModel delegate) {
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

        public Builder enableCostTracking(boolean enableCostTracking) {
            this.enableCostTracking = enableCostTracking;
            return this;
        }

        public TracedEmbeddingModel build() {
            return new TracedEmbeddingModel(this);
        }
    }
}
