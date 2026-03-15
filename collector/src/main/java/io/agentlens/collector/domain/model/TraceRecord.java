package io.agentlens.collector.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 追踪记录实体（持久化用）。
 * <p>
 * 对应一次完整 Trace，存 traceId、项目、起止时间、状态、Span 数、总 Token、总成本等；
 * 与 SpanRecord 为一对多关系。
 * </p>
 */
@Entity
@Table(name = "traces", indexes = {
    @Index(name = "idx_traces_project_id", columnList = "projectId"),
    @Index(name = "idx_traces_start_time", columnList = "startTime"),
    @Index(name = "idx_traces_status", columnList = "status")
})
public class TraceRecord {

    @Id
    @Column(length = 64)
    private String traceId;

    @Column(nullable = false, length = 128)
    private String projectId;

    @Column(length = 64)
    private String rootSpanId;

    @Column(length = 255)
    private String rootSpanName;

    @Column(nullable = false)
    private Instant startTime;

    private Instant endTime;

    private Long durationMs;

    @Column(length = 32)
    private String status;

    private Integer totalTokens;

    @Column(precision = 12, scale = 6)
    private BigDecimal totalCostUsd;

    private Integer spanCount;

    @OneToMany(mappedBy = "trace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpanRecord> spans = new ArrayList<>();

    public TraceRecord() {}

    public TraceRecord(String traceId, String projectId) {
        this.traceId = traceId;
        this.projectId = projectId;
        this.startTime = Instant.now();
    }

    // Getters and Setters
    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getRootSpanId() {
        return rootSpanId;
    }

    public void setRootSpanId(String rootSpanId) {
        this.rootSpanId = rootSpanId;
    }

    public String getRootSpanName() {
        return rootSpanName;
    }

    public void setRootSpanName(String rootSpanName) {
        this.rootSpanName = rootSpanName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public BigDecimal getTotalCostUsd() {
        return totalCostUsd;
    }

    public void setTotalCostUsd(BigDecimal totalCostUsd) {
        this.totalCostUsd = totalCostUsd;
    }

    public Integer getSpanCount() {
        return spanCount;
    }

    public void setSpanCount(Integer spanCount) {
        this.spanCount = spanCount;
    }

    public List<SpanRecord> getSpans() {
        return spans;
    }

    public void setSpans(List<SpanRecord> spans) {
        this.spans = spans;
    }

    public void addSpan(SpanRecord span) {
        spans.add(span);
        span.setTrace(this);
    }
}
