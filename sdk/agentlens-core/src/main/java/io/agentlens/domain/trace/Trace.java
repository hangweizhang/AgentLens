package io.agentlens.domain.trace;

import io.agentlens.domain.cost.TokenCost;
import io.agentlens.domain.shared.AggregateRoot;
import io.agentlens.domain.trace.events.SpanEndedEvent;
import io.agentlens.domain.trace.events.SpanStartedEvent;
import io.agentlens.domain.trace.events.TraceCompletedEvent;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 表示一次完整追踪的聚合根。
 * <p>
 * 一个 Trace 对应一次请求/响应周期，包含该周期内所有 Span（LLM、工具、向量库等）。
 * 负责创建/结束 Span、维护父子关系、汇总耗时与成本，并在完成时发布领域事件。
 * </p>
 */
public class Trace extends AggregateRoot<TraceId> {

    private final String projectId;
    private final Instant startTime;
    private Instant endTime;
    private SpanStatus status;
    private final List<Span> spans;
    private SpanId rootSpanId;

    private Trace(TraceId traceId, String projectId) {
        super(traceId);
        this.projectId = Objects.requireNonNull(projectId, "projectId is required");
        this.startTime = Instant.now();
        this.status = SpanStatus.RUNNING;
        this.spans = new ArrayList<>();
    }

    public static Trace create(String projectId) {
        return new Trace(TraceId.generate(), projectId);
    }

    public static Trace create(TraceId traceId, String projectId) {
        return new Trace(traceId, projectId);
    }

    public Span startSpan(String name, SpanType type) {
        return startSpan(name, type, null);
    }

    public Span startSpan(String name, SpanType type, SpanId parentSpanId) {
        Span span = Span.builder(getId(), name, type)
            .parentSpanId(parentSpanId)
            .build();

        if (rootSpanId == null) {
            rootSpanId = span.getId();
        }

        spans.add(span);
        addDomainEvent(new SpanStartedEvent(getId(), span.getId(), name, type));
        return span;
    }

    public void endSpan(Span span) {
        endSpan(span, SpanStatus.OK);
    }

    public void endSpan(Span span, SpanStatus status) {
        if (!spans.contains(span)) {
            throw new IllegalArgumentException("Span does not belong to this trace");
        }

        if (!span.isCompleted()) {
            span.end(status);
        }

        addDomainEvent(new SpanEndedEvent(getId(), span.getId(), status, span.getDurationMs()));

        checkAndCompleteTrace();
    }

    private void checkAndCompleteTrace() {
        boolean allCompleted = spans.stream().allMatch(Span::isCompleted);
        if (allCompleted && !spans.isEmpty()) {
            complete();
        }
    }

    public void complete() {
        if (this.endTime != null) {
            return;
        }

        this.endTime = Instant.now();
        this.status = determineStatus();

        addDomainEvent(new TraceCompletedEvent(
            getId(),
            this.status,
            getDurationMs(),
            getTotalCost()
        ));
    }

    private SpanStatus determineStatus() {
        boolean hasError = spans.stream().anyMatch(Span::isError);
        return hasError ? SpanStatus.ERROR : SpanStatus.OK;
    }

    public Optional<Span> findSpan(SpanId spanId) {
        return spans.stream()
            .filter(s -> s.getId().equals(spanId))
            .findFirst();
    }

    public Optional<Span> getRootSpan() {
        return rootSpanId != null ? findSpan(rootSpanId) : Optional.empty();
    }

    public List<Span> getChildSpans(SpanId parentId) {
        return spans.stream()
            .filter(s -> parentId.equals(s.getParentSpanId()))
            .toList();
    }

    public Duration getDuration() {
        Instant end = endTime != null ? endTime : Instant.now();
        return Duration.between(startTime, end);
    }

    public long getDurationMs() {
        return getDuration().toMillis();
    }

    public int getTotalTokens() {
        return spans.stream()
            .map(Span::getCost)
            .filter(Objects::nonNull)
            .mapToInt(TokenCost::getTotalTokens)
            .sum();
    }

    public BigDecimal getTotalCost() {
        return spans.stream()
            .map(Span::getCost)
            .filter(Objects::nonNull)
            .map(TokenCost::getCostUsd)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getSpanCount() {
        return spans.size();
    }

    public long getSpanCountByType(SpanType type) {
        return spans.stream()
            .filter(s -> s.getType() == type)
            .count();
    }

    public boolean isCompleted() {
        return endTime != null;
    }

    public boolean isError() {
        return status == SpanStatus.ERROR;
    }

    // Getters
    public String getProjectId() {
        return projectId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public SpanStatus getStatus() {
        return status;
    }

    public List<Span> getSpans() {
        return Collections.unmodifiableList(spans);
    }

    public SpanId getRootSpanId() {
        return rootSpanId;
    }
}
