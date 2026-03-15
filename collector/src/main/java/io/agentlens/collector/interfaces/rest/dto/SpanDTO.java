package io.agentlens.collector.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SpanDTO(
    String spanId,
    String traceId,
    String parentSpanId,
    String name,
    String spanType,
    Instant startTime,
    Instant endTime,
    Long durationMs,
    String status,
    String attributes,
    String input,
    String output,
    String errorMessage,
    Integer inputTokens,
    Integer outputTokens,
    Integer totalTokens,
    BigDecimal costUsd,
    String model,
    String provider
) {}
