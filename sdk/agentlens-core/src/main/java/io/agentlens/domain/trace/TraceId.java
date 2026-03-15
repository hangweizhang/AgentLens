package io.agentlens.domain.trace;

import io.agentlens.domain.shared.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * 追踪 ID 值对象。
 * <p>
 * 一个 Trace 表示一次完整的请求/响应周期，TraceId 为其唯一标识。
 * </p>
 */
public final class TraceId extends ValueObject {

    private final String value;

    private TraceId(String value) {
        this.value = Objects.requireNonNull(value, "TraceId value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TraceId value cannot be blank");
        }
    }

    public static TraceId generate() {
        return new TraceId(UUID.randomUUID().toString().replace("-", ""));
    }

    public static TraceId of(String value) {
        return new TraceId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraceId traceId = (TraceId) o;
        return Objects.equals(value, traceId.value);
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
