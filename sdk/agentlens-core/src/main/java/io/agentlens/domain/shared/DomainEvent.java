package io.agentlens.domain.shared;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件基类。
 * <p>
 * 领域事件表示「已经发生」的领域事实，命名使用过去式（如 SpanStarted、TraceCompleted）。
 * 不可变；包含事件 ID 与发生时间，用于审计与跨聚合通信。
 * </p>
 */
public abstract class DomainEvent {

    /** 事件唯一 ID */
    private final String eventId;
    /** 事件发生时间 */
    private final Instant occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    /** 事件类型标识（如 span.started、trace.completed） */
    public abstract String getEventType();
}
