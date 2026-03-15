package io.agentlens.collector.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Span 记录实体（持久化用）。
 * <p>
 * 对应一次操作（LLM/工具/向量库等），存 spanId、traceId、父 span、名称、类型、
 * 起止时间、状态、属性 JSON、输入输出及 Token/成本等。
 * </p>
 */
@Entity
@Table(name = "spans", indexes = {
    @Index(name = "idx_spans_trace_id", columnList = "traceId"),
    @Index(name = "idx_spans_type", columnList = "spanType"),
    @Index(name = "idx_spans_start_time", columnList = "startTime")
})
public class SpanRecord {

    @Id
    @Column(length = 64)
    private String spanId;

    @Column(length = 64, nullable = false)
    private String traceId;

    @Column(length = 64)
    private String parentSpanId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 32)
    private String spanType;

    @Column(nullable = false)
    private Instant startTime;

    private Instant endTime;

    private Long durationMs;

    @Column(length = 32)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String attributes;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // Cost fields
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;

    @Column(precision = 12, scale = 6)
    private BigDecimal costUsd;

    @Column(length = 128)
    private String model;

    @Column(length = 64)
    private String provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traceId", insertable = false, updatable = false)
    private TraceRecord trace;

    public SpanRecord() {}

    public SpanRecord(String spanId, String traceId, String name, String spanType) {
        this.spanId = spanId;
        this.traceId = traceId;
        this.name = name;
        this.spanType = spanType;
        this.startTime = Instant.now();
    }

    // Getters and Setters
    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpanType() {
        return spanType;
    }

    public void setSpanType(String spanType) {
        this.spanType = spanType;
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

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(Integer inputTokens) {
        this.inputTokens = inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(Integer outputTokens) {
        this.outputTokens = outputTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public BigDecimal getCostUsd() {
        return costUsd;
    }

    public void setCostUsd(BigDecimal costUsd) {
        this.costUsd = costUsd;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public TraceRecord getTrace() {
        return trace;
    }

    public void setTrace(TraceRecord trace) {
        this.trace = trace;
    }
}
