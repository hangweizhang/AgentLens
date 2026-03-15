package io.agentlens.collector.interfaces.rest.dto;

import java.math.BigDecimal;

public record SpanTypeStatsDTO(
    String spanType,
    long count,
    long totalDurationMs,
    BigDecimal totalCostUsd
) {}
