package io.agentlens.application.ports.driver;

import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.SpanId;
import io.agentlens.domain.trace.SpanType;
import io.agentlens.domain.trace.Trace;
import io.agentlens.domain.trace.TraceId;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 追踪能力驱动端口（入站端口）。
 * <p>
 * 客户端通过此接口与 AgentLens 交互：创建 Trace/Span、结束 Span、在 Span 内执行代码等。
 * 由 {@link io.agentlens.application.usecases.TracingService} 实现。
 * </p>
 */
public interface TracingPort {

    /**
     * Start a new trace with the configured project ID.
     */
    Trace startTrace();

    /**
     * Start a new trace with a specific project ID.
     */
    Trace startTrace(String projectId);

    /**
     * Get the current active trace, if any.
     */
    Optional<Trace> getCurrentTrace();

    /**
     * Get a trace by its ID.
     */
    Optional<Trace> getTrace(TraceId traceId);

    /**
     * Start a new span within the current trace.
     */
    Span startSpan(String name, SpanType type);

    /**
     * Start a new span with a specific parent.
     */
    Span startSpan(String name, SpanType type, SpanId parentSpanId);

    /**
     * End a span with OK status.
     */
    void endSpan(Span span);

    /**
     * End a span with an error.
     */
    void endSpanWithError(Span span, Throwable error);

    /**
     * Get the current active span, if any.
     */
    Optional<Span> getCurrentSpan();

    /**
     * Complete the current trace.
     */
    void completeTrace();

    /**
     * Complete a specific trace.
     */
    void completeTrace(Trace trace);

    /**
     * Execute an operation within a traced span.
     * Automatically handles span lifecycle and error recording.
     */
    <T> T trace(String name, SpanType type, Supplier<T> operation);

    /**
     * Execute an operation within a traced span (void version).
     */
    void trace(String name, SpanType type, Runnable operation);
}
