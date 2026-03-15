package io.agentlens.domain.trace.events;

import io.agentlens.domain.shared.DomainEvent;
import io.agentlens.domain.trace.SpanId;
import io.agentlens.domain.trace.SpanStatus;
import io.agentlens.domain.trace.TraceId;

/**
 * Span 结束时发布的领域事件。
 */
public class SpanEndedEvent extends DomainEvent {

    private final TraceId traceId;
    private final SpanId spanId;
    private final SpanStatus status;
    private final long durationMs;

    public SpanEndedEvent(TraceId traceId, SpanId spanId, SpanStatus status, long durationMs) {
        super();
        this.traceId = traceId;
        this.spanId = spanId;
        this.status = status;
        this.durationMs = durationMs;
    }

    @Override
    public String getEventType() {
        return "span.ended";
    }

    public TraceId getTraceId() {
        return traceId;
    }

    public SpanId getSpanId() {
        return spanId;
    }

    public SpanStatus getStatus() {
        return status;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
