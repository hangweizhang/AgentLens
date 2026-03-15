package io.agentlens.domain.trace;

/**
 * AgentLens 支持的 Span 类型枚举。
 * <p>
 * 每种类型对应 AI Agent 系统中的一类操作，用于在 Dashboard 中分类展示与统计。
 * </p>
 */
public enum SpanType {

    /** Agent 整体执行（一次请求的根 Span） */
    AGENT("agent"),

    /** 大模型调用（Chat/Completion） */
    LLM("llm"),

    /** 向量嵌入（Embedding） */
    EMBEDDING("embedding"),

    /** 向量数据库操作（检索、写入、删除等） */
    VECTOR_DB("vector_db"),

    /** 工具/函数调用（Function Calling、MCP 等） */
    TOOL("tool"),

    /** 外部 HTTP 请求 */
    HTTP("http"),

    /** 重排序（Reranker） */
    RERANKER("reranker"),

    /** Prompt 格式化/模板渲染 */
    FORMATTER("formatter"),

    /** 记忆读写（Memory） */
    MEMORY("memory"),

    /** RAG 检索（Retriever） */
    RETRIEVER("retriever"),

    /** 自定义 Span */
    CUSTOM("custom");

    /** 用于序列化/存储的字符串值 */
    private final String value;

    SpanType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /** 根据字符串值解析为枚举，未知则返回 CUSTOM */
    public static SpanType fromValue(String value) {
        for (SpanType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return CUSTOM;
    }
}
