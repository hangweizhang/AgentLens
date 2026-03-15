package io.agentlens.domain.trace.events;

import io.agentlens.domain.shared.DomainEvent;
import io.agentlens.domain.trace.SpanStatus;
import io.agentlens.domain.trace.TraceId;

import java.math.BigDecimal;

/**
 * Trace 完成时发布的领域事件（含状态、总耗时、总成本）。
 */
public class TraceCompletedEvent extends DomainEvent {

    private final TraceId traceId;
    private final SpanStatus status;
    private final long durationMs;
    private final BigDecimal totalCostUsd;

    public TraceCompletedEvent(TraceId traceId, SpanStatus status, long durationMs, BigDecimal totalCostUsd) {
        super();
        this.traceId = traceId;
        this.status = status;
        this.durationMs = durationMs;
        this.totalCostUsd = totalCostUsd;
    }

    @Override
    public String getEventType() {
        return "trace.completed";
    }

    public TraceId getTraceId() {
        return traceId;
    }

    public SpanStatus getStatus() {
        return status;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public BigDecimal getTotalCostUsd() {
        return totalCostUsd;
    }
}
