package io.agentlens.collector.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProjectStatsDTO(
    String projectId,
    int days,
    long traceCount,
    BigDecimal totalCostUsd,
    long totalTokens,
    long errorCount,
    double errorRate,
    Map<String, SpanTypeStatsDTO> spanTypeStats,
    List<ModelUsageDTO> modelUsage
) {}
