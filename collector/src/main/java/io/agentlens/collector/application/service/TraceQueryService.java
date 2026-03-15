package io.agentlens.collector.application.service;

import io.agentlens.collector.domain.model.SpanRecord;
import io.agentlens.collector.domain.model.TraceRecord;
import io.agentlens.collector.domain.repository.SpanRepository;
import io.agentlens.collector.domain.repository.TraceRepository;
import io.agentlens.collector.interfaces.rest.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Trace/Span 查询服务。
 * <p>
 * 提供：按项目分页列 Trace、按 traceId 查详情（含所有 Span）、按项目与天数查统计（Trace 数、成本、Token、错误率、Span 类型分布、模型用量）。
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class TraceQueryService {

    private final TraceRepository traceRepository;
    private final SpanRepository spanRepository;

    public TraceQueryService(TraceRepository traceRepository, SpanRepository spanRepository) {
        this.traceRepository = traceRepository;
        this.spanRepository = spanRepository;
    }

    public Page<TraceListItemDTO> listTraces(String projectId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<TraceRecord> traces;
        if (status != null && !status.isEmpty()) {
            traces = traceRepository.findByProjectIdAndStatusOrderByStartTimeDesc(projectId, status, pageable);
        } else {
            traces = traceRepository.findByProjectIdOrderByStartTimeDesc(projectId, pageable);
        }

        return traces.map(this::toTraceListItemDTO);
    }

    public Optional<TraceDetailDTO> getTraceDetail(String traceId) {
        return traceRepository.findById(traceId)
            .map(trace -> {
                List<SpanRecord> spans = spanRepository.findByTraceIdOrderByStartTimeAsc(traceId);
                return toTraceDetailDTO(trace, spans);
            });
    }

    public ProjectStatsDTO getProjectStats(String projectId, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);

        long traceCount = traceRepository.countByProjectIdSince(projectId, since);
        BigDecimal totalCost = traceRepository.sumCostByProjectIdSince(projectId, since);
        Long totalTokens = traceRepository.sumTokensByProjectIdSince(projectId, since);
        long errorCount = traceRepository.countErrorsByProjectIdSince(projectId, since);

        List<Object[]> spanStats = spanRepository.getSpanTypeStatsByProjectIdSince(projectId, since);
        List<Object[]> modelStats = spanRepository.getModelUsageStatsByProjectIdSince(projectId, since);

        Map<String, SpanTypeStatsDTO> spanTypeStats = new HashMap<>();
        for (Object[] row : spanStats) {
            String type = (String) row[0];
            spanTypeStats.put(type, new SpanTypeStatsDTO(
                type,
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO
            ));
        }

        List<ModelUsageDTO> modelUsage = modelStats.stream()
            .map(row -> new ModelUsageDTO(
                (String) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue(),
                row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO
            ))
            .toList();

        return new ProjectStatsDTO(
            projectId,
            days,
            traceCount,
            totalCost != null ? totalCost : BigDecimal.ZERO,
            totalTokens != null ? totalTokens : 0L,
            errorCount,
            traceCount > 0 ? (double) errorCount / traceCount * 100 : 0.0,
            spanTypeStats,
            modelUsage
        );
    }

    private TraceListItemDTO toTraceListItemDTO(TraceRecord trace) {
        return new TraceListItemDTO(
            trace.getTraceId(),
            trace.getProjectId(),
            trace.getRootSpanName(),
            trace.getStartTime(),
            trace.getEndTime(),
            trace.getDurationMs(),
            trace.getStatus(),
            trace.getSpanCount(),
            trace.getTotalTokens(),
            trace.getTotalCostUsd()
        );
    }

    private TraceDetailDTO toTraceDetailDTO(TraceRecord trace, List<SpanRecord> spans) {
        List<SpanDTO> spanDTOs = spans.stream()
            .map(this::toSpanDTO)
            .toList();

        return new TraceDetailDTO(
            trace.getTraceId(),
            trace.getProjectId(),
            trace.getRootSpanId(),
            trace.getStartTime(),
            trace.getEndTime(),
            trace.getDurationMs(),
            trace.getStatus(),
            trace.getSpanCount(),
            trace.getTotalTokens(),
            trace.getTotalCostUsd(),
            spanDTOs
        );
    }

    private SpanDTO toSpanDTO(SpanRecord span) {
        return new SpanDTO(
            span.getSpanId(),
            span.getTraceId(),
            span.getParentSpanId(),
            span.getName(),
            span.getSpanType(),
            span.getStartTime(),
            span.getEndTime(),
            span.getDurationMs(),
            span.getStatus(),
            span.getAttributes(),
            span.getInput(),
            span.getOutput(),
            span.getErrorMessage(),
            span.getInputTokens(),
            span.getOutputTokens(),
            span.getTotalTokens(),
            span.getCostUsd(),
            span.getModel(),
            span.getProvider()
        );
    }
}
