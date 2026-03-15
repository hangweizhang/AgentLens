package io.agentlens.langchain4j;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

/**
 * LangChain4j 模型与 AgentLens 追踪的包装工厂。
 * <p>
 * 提供 wrap(model) 返回 Builder，或 trace(model) 直接返回包装后的模型；
 * 支持 ChatLanguageModel 与 EmbeddingModel。
 * </p>
 *
 * <p>示例：
 * <pre>{@code
 * ChatLanguageModel model = OpenAiChatModel.builder().apiKey(apiKey).build();
 * ChatLanguageModel tracedModel = AgentLensLangChain4j.wrap(model).modelName("gpt-4o").build();
 * // 或简写：AgentLensLangChain4j.trace(model);
 * }</pre>
 */
public final class AgentLensLangChain4j {

    private AgentLensLangChain4j() {}

    /**
     * Wrap a ChatLanguageModel with tracing capabilities.
     * Returns a builder for further configuration.
     */
    public static TracedChatLanguageModel.Builder wrap(ChatLanguageModel model) {
        return TracedChatLanguageModel.wrap(model);
    }

    /**
     * Wrap a ChatLanguageModel with tracing using default settings.
     */
    public static ChatLanguageModel trace(ChatLanguageModel model) {
        return TracedChatLanguageModel.wrap(model).build();
    }

    /**
     * Wrap a ChatLanguageModel with tracing, specifying the model name.
     */
    public static ChatLanguageModel trace(ChatLanguageModel model, String modelName) {
        return TracedChatLanguageModel.wrap(model)
            .modelName(modelName)
            .build();
    }

    /**
     * Wrap an EmbeddingModel with tracing capabilities.
     * Returns a builder for further configuration.
     */
    public static TracedEmbeddingModel.Builder wrap(EmbeddingModel model) {
        return TracedEmbeddingModel.wrap(model);
    }

    /**
     * Wrap an EmbeddingModel with tracing using default settings.
     */
    public static EmbeddingModel trace(EmbeddingModel model) {
        return TracedEmbeddingModel.wrap(model).build();
    }

    /**
     * Wrap an EmbeddingModel with tracing, specifying the model name.
     */
    public static EmbeddingModel trace(EmbeddingModel model, String modelName) {
        return TracedEmbeddingModel.wrap(model)
            .modelName(modelName)
            .build();
    }
}
