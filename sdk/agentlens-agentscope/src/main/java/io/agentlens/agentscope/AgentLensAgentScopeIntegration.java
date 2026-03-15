package io.agentlens.agentscope;

import io.agentlens.AgentLens;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.SpanType;
import io.agentlens.infrastructure.config.AgentLensConfig;

/**
 * AgentScope-Java 与 AgentLens 的集成入口。
 * <p>
 * 在 AgentScope 内置 TelemetryTracer 基础上，增加成本统计、扩展属性及上报到 AgentLens Collector。
 * 使用前先 init(AgentLensConfig)，再按需调用 startAgentReplySpan/startLlmSpan/startToolSpan 等打点。
 * </p>
 *
 * <p>使用示例见类文档；仓库：<a href="https://github.com/agentscope-ai/agentscope-java">AgentScope-Java</a></p>
 */
public final class AgentLensAgentScopeIntegration {

    private static volatile boolean initialized = false;

    private AgentLensAgentScopeIntegration() {}

    /**
     * Initialize AgentLens with AgentScope integration.
     * This sets up hooks to capture AgentScope telemetry data.
     */
    public static synchronized void init(AgentLensConfig config) {
        if (initialized) {
            return;
        }

        AgentLens.init(config);

        initialized = true;
    }

    /**
     * Check if the integration is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Start a span for an AgentScope agent reply.
     */
    public static Span startAgentReplySpan(String agentName) {
        if (!AgentLens.isInitialized()) {
            return null;
        }

        Span span = AgentLens.startSpan(agentName + ".reply", SpanType.AGENT);
        span.addAttribute("agent.name", agentName);
        return span;
    }

    /**
     * Start a span for an AgentScope LLM call.
     */
    public static Span startLlmSpan(String modelName, String provider) {
        if (!AgentLens.isInitialized()) {
            return null;
        }

        Span span = AgentLens.startSpan("LLM.call", SpanType.LLM);
        span.addAttribute("gen_ai.request.model", modelName);
        span.addAttribute("gen_ai.provider.name", provider);
        return span;
    }

    /**
     * Start a span for an AgentScope tool call.
     */
    public static Span startToolSpan(String toolName) {
        if (!AgentLens.isInitialized()) {
            return null;
        }

        Span span = AgentLens.startSpan("Tool." + toolName, SpanType.TOOL);
        span.addAttribute("tool.name", toolName);
        return span;
    }

    /**
     * End a span.
     */
    public static void endSpan(Span span) {
        if (span != null && AgentLens.isInitialized()) {
            AgentLens.endSpan(span);
        }
    }

    /**
     * End a span with error.
     */
    public static void endSpanWithError(Span span, Throwable error) {
        if (span != null && AgentLens.isInitialized()) {
            AgentLens.endSpanWithError(span, error);
        }
    }
}
