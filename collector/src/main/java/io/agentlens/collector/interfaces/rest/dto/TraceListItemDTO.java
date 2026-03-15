package io.agentlens.collector.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TraceListItemDTO(
    String traceId,
    String projectId,
    String rootSpanName,
    Instant startTime,
    Instant endTime,
    Long durationMs,
    String status,
    Integer spanCount,
    Integer totalTokens,
    BigDecimal totalCostUsd
) {}
