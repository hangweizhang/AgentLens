package io.agentlens.springai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AgentLens Spring AI 集成相关配置项。
 * <p>
 * 前缀：agentlens.springai，可在 application.yml 中配置是否追踪各类组件、是否截获内容与成本等。
 * </p>
 */
@ConfigurationProperties(prefix = "agentlens.springai")
public class AgentLensSpringAiProperties {

    /** 是否对 ChatModel 调用打点 */
    private boolean traceChatModel = true;

    /** 是否对 EmbeddingModel 调用打点 */
    private boolean traceEmbeddingModel = true;

    /** 是否对 VectorStore 操作打点 */
    private boolean traceVectorStore = true;

    /** 是否截获输入/输出内容（可能含敏感信息，生产可关闭） */
    private boolean captureContent = true;

    /** 截获内容的最大长度，超出部分截断 */
    private int maxContentLength = 10000;

    /** 是否计算并记录 LLM/Embedding 成本 */
    private boolean enableCostTracking = true;

    public boolean isTraceChatModel() {
        return traceChatModel;
    }

    public void setTraceChatModel(boolean traceChatModel) {
        this.traceChatModel = traceChatModel;
    }

    public boolean isTraceEmbeddingModel() {
        return traceEmbeddingModel;
    }

    public void setTraceEmbeddingModel(boolean traceEmbeddingModel) {
        this.traceEmbeddingModel = traceEmbeddingModel;
    }

    public boolean isTraceVectorStore() {
        return traceVectorStore;
    }

    public void setTraceVectorStore(boolean traceVectorStore) {
        this.traceVectorStore = traceVectorStore;
    }

    public boolean isCaptureContent() {
        return captureContent;
    }

    public void setCaptureContent(boolean captureContent) {
        this.captureContent = captureContent;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public boolean isEnableCostTracking() {
        return enableCostTracking;
    }

    public void setEnableCostTracking(boolean enableCostTracking) {
        this.enableCostTracking = enableCostTracking;
    }
}
