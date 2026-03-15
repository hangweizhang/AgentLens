package io.agentlens.domain.trace;

import io.agentlens.domain.shared.ValueObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Span 属性值对象。
 * <p>
 * 不可变的键值对集合，描述 Span 的元数据（如模型名、Token 数、成本等）。
 * 键名建议遵循 OpenTelemetry GenAI 语义约定（如 gen_ai.request.model）。
 * </p>
 */
public final class SpanAttributes extends ValueObject {

    private final Map<String, Object> attributes;

    private SpanAttributes(Map<String, Object> attributes) {
        this.attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    public static SpanAttributes empty() {
        return new SpanAttributes(Collections.emptyMap());
    }

    public static SpanAttributes of(Map<String, Object> attributes) {
        return new SpanAttributes(attributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public String getString(String key) {
        Object value = attributes.get(key);
        return value != null ? value.toString() : null;
    }

    public Integer getInt(String key) {
        Object value = attributes.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    public Long getLong(String key) {
        Object value = attributes.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    public Double getDouble(String key) {
        Object value = attributes.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    public Boolean getBoolean(String key) {
        Object value = attributes.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

    public boolean containsKey(String key) {
        return attributes.containsKey(key);
    }

    public Map<String, Object> toMap() {
        return new HashMap<>(attributes);
    }

    public SpanAttributes merge(SpanAttributes other) {
        Map<String, Object> merged = new HashMap<>(this.attributes);
        merged.putAll(other.attributes);
        return new SpanAttributes(merged);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpanAttributes that = (SpanAttributes) o;
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }

    @Override
    public String toString() {
        return attributes.toString();
    }

    public static class Builder {
        private final Map<String, Object> attributes = new HashMap<>();

        public Builder put(String key, Object value) {
            if (key != null && value != null) {
                attributes.put(key, value);
            }
            return this;
        }

        public Builder putAll(Map<String, Object> attrs) {
            if (attrs != null) {
                attrs.forEach(this::put);
            }
            return this;
        }

        public SpanAttributes build() {
            return new SpanAttributes(attributes);
        }
    }
}
