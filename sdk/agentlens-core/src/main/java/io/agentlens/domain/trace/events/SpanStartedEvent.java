package io.agentlens.domain.trace.events;

import io.agentlens.domain.shared.DomainEvent;
import io.agentlens.domain.trace.SpanId;
import io.agentlens.domain.trace.SpanType;
import io.agentlens.domain.trace.TraceId;

/**
 * Span 开始时发布的领域事件。
 */
public class SpanStartedEvent extends DomainEvent {

    private final TraceId traceId;
    private final SpanId spanId;
    private final String spanName;
    private final SpanType spanType;

    public SpanStartedEvent(TraceId traceId, SpanId spanId, String spanName, SpanType spanType) {
        super();
        this.traceId = traceId;
        this.spanId = spanId;
        this.spanName = spanName;
        this.spanType = spanType;
    }

    @Override
    public String getEventType() {
        return "span.started";
    }

    public TraceId getTraceId() {
        return traceId;
    }

    public SpanId getSpanId() {
        return spanId;
    }

    public String getSpanName() {
        return spanName;
    }

    public SpanType getSpanType() {
        return spanType;
    }
}
