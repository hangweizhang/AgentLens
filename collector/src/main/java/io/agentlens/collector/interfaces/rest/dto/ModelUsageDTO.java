package io.agentlens.collector.interfaces.rest.dto;

import java.math.BigDecimal;

public record ModelUsageDTO(
    String model,
    String provider,
    long callCount,
    long inputTokens,
    long outputTokens,
    BigDecimal totalCostUsd
) {}
