package io.agentlens.domain.attributes;

/**
 * OpenTelemetry 生成式 AI 语义约定常量。
 * <p>
 * 基于官方 GenAI 语义规范，用于 Span 属性的键名统一，便于与 OTLP/Langfuse 等生态对接。
 * 参考：<a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/">GenAI Semantic Conventions</a>
 * </p>
 */
public final class GenAiAttributes {

    private GenAiAttributes() {}

    // ---------- 操作类型 ----------
    /** 操作名称：chat / text_completion / embeddings 等 */
    public static final String OPERATION_NAME = "gen_ai.operation.name";

    // ---------- 系统/厂商 ----------
    public static final String SYSTEM = "gen_ai.system";
    /** 厂商名：openai、anthropic、dashscope 等 */
    public static final String PROVIDER_NAME = "gen_ai.provider.name";

    // ---------- 请求属性 ----------
    /** 请求使用的模型 ID */
    public static final String REQUEST_MODEL = "gen_ai.request.model";
    public static final String REQUEST_MAX_TOKENS = "gen_ai.request.max_tokens";
    public static final String REQUEST_TEMPERATURE = "gen_ai.request.temperature";
    public static final String REQUEST_TOP_P = "gen_ai.request.top_p";
    public static final String REQUEST_TOP_K = "gen_ai.request.top_k";
    public static final String REQUEST_STOP_SEQUENCES = "gen_ai.request.stop_sequences";
    public static final String REQUEST_FREQUENCY_PENALTY = "gen_ai.request.frequency_penalty";
    public static final String REQUEST_PRESENCE_PENALTY = "gen_ai.request.presence_penalty";

    // Response attributes
    public static final String RESPONSE_ID = "gen_ai.response.id";
    public static final String RESPONSE_MODEL = "gen_ai.response.model";
    public static final String RESPONSE_FINISH_REASONS = "gen_ai.response.finish_reasons";

    // ---------- 用量 ----------
    /** 输入 Token 数 */
    public static final String USAGE_INPUT_TOKENS = "gen_ai.usage.input_tokens";
    /** 输出 Token 数 */
    public static final String USAGE_OUTPUT_TOKENS = "gen_ai.usage.output_tokens";
    /** 总 Token 数 */
    public static final String USAGE_TOTAL_TOKENS = "gen_ai.usage.total_tokens";

    // ---------- AgentLens 扩展（成本） ----------
    /** 本次调用成本（美元） */
    public static final String COST_USD = "agentlens.cost.usd";
    public static final String COST_INPUT_USD = "agentlens.cost.input_usd";
    public static final String COST_OUTPUT_USD = "agentlens.cost.output_usd";

    // Operation names
    public static final class Operations {
        public static final String CHAT = "chat";
        public static final String TEXT_COMPLETION = "text_completion";
        public static final String EMBEDDINGS = "embeddings";
        public static final String IMAGE_GENERATION = "image_generation";
        public static final String AUDIO_TRANSCRIPTION = "audio_transcription";

        private Operations() {}
    }

    // Provider names
    public static final class Providers {
        public static final String OPENAI = "openai";
        public static final String ANTHROPIC = "anthropic";
        public static final String GOOGLE = "google";
        public static final String DASHSCOPE = "dashscope";
        public static final String DEEPSEEK = "deepseek";
        public static final String OLLAMA = "ollama";
        public static final String AZURE_OPENAI = "azure_openai";

        private Providers() {}
    }
}
