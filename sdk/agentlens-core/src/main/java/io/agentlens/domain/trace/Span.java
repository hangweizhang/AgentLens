package io.agentlens.domain.trace;

import io.agentlens.domain.cost.TokenCost;
import io.agentlens.domain.shared.Entity;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * 表示一次追踪内的单次操作的实体。
 * <p>
 * Span 记录某次具体操作的执行（如一次 LLM 调用、工具调用、向量检索等），
 * 包含开始/结束时间、状态、属性、输入输出及可选的 Token 成本。
 * </p>
 */
public class Span extends Entity<SpanId> {

    private final TraceId traceId;
    private final SpanId parentSpanId;
    private final String name;
    private final SpanType type;
    private final Instant startTime;
    private Instant endTime;
    private SpanStatus status;
    private SpanAttributes attributes;
    private Object input;
    private Object output;
    private String errorMessage;
    private TokenCost cost;

    private Span(Builder builder) {
        super(builder.spanId);
        this.traceId = Objects.requireNonNull(builder.traceId, "traceId is required");
        this.parentSpanId = builder.parentSpanId;
        this.name = Objects.requireNonNull(builder.name, "name is required");
        this.type = Objects.requireNonNull(builder.type, "type is required");
        this.startTime = builder.startTime != null ? builder.startTime : Instant.now();
        this.status = SpanStatus.RUNNING;
        this.attributes = builder.attributes != null ? builder.attributes : SpanAttributes.empty();
    }

    public static Builder builder(TraceId traceId, String name, SpanType type) {
        return new Builder(traceId, name, type);
    }

    public void end() {
        end(SpanStatus.OK);
    }

    public void end(SpanStatus status) {
        if (this.endTime != null) {
            throw new IllegalStateException("Span already ended");
        }
        this.endTime = Instant.now();
        this.status = status;
    }

    public void endWithError(String errorMessage) {
        this.errorMessage = errorMessage;
        end(SpanStatus.ERROR);
    }

    public void endWithError(Throwable exception) {
        endWithError(exception.getMessage());
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public void setInput(Object input) {
        this.input = input;
    }

    public void setCost(TokenCost cost) {
        this.cost = cost;
    }

    public void addAttribute(String key, Object value) {
        this.attributes = this.attributes.merge(
            SpanAttributes.builder().put(key, value).build()
        );
    }

    public void addAttributes(SpanAttributes additionalAttributes) {
        this.attributes = this.attributes.merge(additionalAttributes);
    }

    public Duration getDuration() {
        if (endTime == null) {
            return Duration.between(startTime, Instant.now());
        }
        return Duration.between(startTime, endTime);
    }

    public long getDurationMs() {
        return getDuration().toMillis();
    }

    public boolean isRunning() {
        return status == SpanStatus.RUNNING;
    }

    public boolean isCompleted() {
        return endTime != null;
    }

    public boolean isError() {
        return status == SpanStatus.ERROR;
    }

    // Getters
    public TraceId getTraceId() {
        return traceId;
    }

    public SpanId getParentSpanId() {
        return parentSpanId;
    }

    public String getName() {
        return name;
    }

    public SpanType getType() {
        return type;
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

    public SpanAttributes getAttributes() {
        return attributes;
    }

    public Object getInput() {
        return input;
    }

    public Object getOutput() {
        return output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public TokenCost getCost() {
        return cost;
    }

    public static class Builder {
        private SpanId spanId;
        private final TraceId traceId;
        private SpanId parentSpanId;
        private final String name;
        private final SpanType type;
        private Instant startTime;
        private SpanAttributes attributes;

        private Builder(TraceId traceId, String name, SpanType type) {
            this.traceId = traceId;
            this.name = name;
            this.type = type;
            this.spanId = SpanId.generate();
        }

        public Builder spanId(SpanId spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder parentSpanId(SpanId parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder attributes(SpanAttributes attributes) {
            this.attributes = attributes;
            return this;
        }

        public Span build() {
            return new Span(this);
        }
    }
}
