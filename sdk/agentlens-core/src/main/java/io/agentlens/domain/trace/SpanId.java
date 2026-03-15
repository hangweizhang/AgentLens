package io.agentlens.domain.trace;

import io.agentlens.domain.shared.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * Span ID 值对象。
 * <p>
 * 一个 Span 表示 Trace 内的一次单一操作（如一次 LLM 调用、一次工具调用）。
 * </p>
 */
public final class SpanId extends ValueObject {

    private final String value;

    private SpanId(String value) {
        this.value = Objects.requireNonNull(value, "SpanId value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("SpanId value cannot be blank");
        }
    }

    public static SpanId generate() {
        return new SpanId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
    }

    public static SpanId of(String value) {
        return new SpanId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpanId spanId = (SpanId) o;
        return Objects.equals(value, spanId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
