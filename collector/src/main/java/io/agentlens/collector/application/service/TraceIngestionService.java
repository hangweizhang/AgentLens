package io.agentlens.collector.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentlens.collector.domain.model.SpanRecord;
import io.agentlens.collector.domain.model.TraceRecord;
import io.agentlens.collector.domain.repository.SpanRepository;
import io.agentlens.collector.domain.repository.TraceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Trace/Span 数据接入服务。
 * <p>
 * 接收 SDK 通过 HTTP 上报的批量数据（List&lt;Map&gt;），解析 type=span 或 type=trace，
 * 转换为 TraceRecord/SpanRecord 并落库；若仅有 Span 无 Trace 则自动补一条默认 Trace。
 * </p>
 */
@Service
public class TraceIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TraceIngestionService.class);

    private final TraceRepository traceRepository;
    private final SpanRepository spanRepository;
    private final ObjectMapper objectMapper;

    public TraceIngestionService(TraceRepository traceRepository, 
                                  SpanRepository spanRepository,
                                  ObjectMapper objectMapper) {
        this.traceRepository = traceRepository;
        this.spanRepository = spanRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void ingestBatch(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            String type = (String) item.get("type");
            if ("span".equals(type)) {
                ingestSpan(item);
            } else if ("trace".equals(type)) {
                ingestTrace(item);
            } else {
                log.warn("Unknown item type: {}", type);
            }
        }
    }

    @Transactional
    public void ingestSpan(Map<String, Object> data) {
        String spanId = (String) data.get("spanId");
        String traceId = (String) data.get("traceId");
        String name = (String) data.get("name");
        String spanType = (String) data.get("spanType");

        // 先确保 Trace 存在，避免 Span 插入时外键约束失败（SDK 批量中可能先发 Span）
        ensureTraceExists(traceId);

        SpanRecord span = spanRepository.findById(spanId)
            .orElseGet(() -> new SpanRecord(spanId, traceId, name, spanType));

        span.setParentSpanId((String) data.get("parentSpanId"));
        span.setName(name);
        span.setSpanType(spanType);
        span.setStatus((String) data.get("status"));
        span.setErrorMessage((String) data.get("errorMessage"));

        if (data.get("startTime") != null) {
            span.setStartTime(Instant.parse((String) data.get("startTime")));
        }
        if (data.get("endTime") != null) {
            span.setEndTime(Instant.parse((String) data.get("endTime")));
        }
        if (data.get("durationMs") != null) {
            span.setDurationMs(((Number) data.get("durationMs")).longValue());
        }

        if (data.get("attributes") != null) {
            try {
                span.setAttributes(objectMapper.writeValueAsString(data.get("attributes")));
            } catch (Exception e) {
                log.warn("Failed to serialize attributes", e);
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> cost = (Map<String, Object>) data.get("cost");
        if (cost != null) {
            span.setInputTokens(cost.get("inputTokens") != null ? ((Number) cost.get("inputTokens")).intValue() : null);
            span.setOutputTokens(cost.get("outputTokens") != null ? ((Number) cost.get("outputTokens")).intValue() : null);
            span.setTotalTokens(cost.get("totalTokens") != null ? ((Number) cost.get("totalTokens")).intValue() : null);
            span.setCostUsd(cost.get("costUsd") != null ? BigDecimal.valueOf(((Number) cost.get("costUsd")).doubleValue()) : null);
            span.setModel((String) cost.get("model"));
            span.setProvider((String) cost.get("provider"));
        }

        spanRepository.save(span);

        log.debug("Ingested span: {} ({})", name, spanId);
    }

    @Transactional
    public void ingestTrace(Map<String, Object> data) {
        String traceId = (String) data.get("traceId");
        String projectId = (String) data.get("projectId");

        TraceRecord trace = traceRepository.findById(traceId)
            .orElseGet(() -> new TraceRecord(traceId, projectId != null ? projectId : "default"));

        if (projectId != null) {
            trace.setProjectId(projectId);
        }

        trace.setRootSpanId((String) data.get("rootSpanId"));
        trace.setStatus((String) data.get("status"));

        if (data.get("startTime") != null) {
            trace.setStartTime(Instant.parse((String) data.get("startTime")));
        }
        if (data.get("endTime") != null) {
            trace.setEndTime(Instant.parse((String) data.get("endTime")));
        }
        if (data.get("durationMs") != null) {
            trace.setDurationMs(((Number) data.get("durationMs")).longValue());
        }
        if (data.get("spanCount") != null) {
            trace.setSpanCount(((Number) data.get("spanCount")).intValue());
        }
        if (data.get("totalTokens") != null) {
            trace.setTotalTokens(((Number) data.get("totalTokens")).intValue());
        }
        if (data.get("totalCostUsd") != null) {
            trace.setTotalCostUsd(BigDecimal.valueOf(((Number) data.get("totalCostUsd")).doubleValue()));
        }

        traceRepository.save(trace);

        log.debug("Ingested trace: {}", traceId);
    }

    private void ensureTraceExists(String traceId) {
        if (!traceRepository.existsById(traceId)) {
            TraceRecord trace = new TraceRecord(traceId, "default");
            traceRepository.save(trace);
        }
    }
}
