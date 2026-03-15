package io.agentlens.collector.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TraceDetailDTO(
    String traceId,
    String projectId,
    String rootSpanId,
    Instant startTime,
    Instant endTime,
    Long durationMs,
    String status,
    Integer spanCount,
    Integer totalTokens,
    BigDecimal totalCostUsd,
    List<SpanDTO> spans
) {}
