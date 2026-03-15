package io.agentlens.infrastructure.adapters.driven;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.agentlens.application.ports.driven.ExporterPort;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.Trace;
import io.agentlens.infrastructure.config.AgentLensConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于 HTTP 的导出器：将 Span/Trace 上报到 AgentLens Collector。
 * <p>
 * 使用内存队列缓冲，达到批量大小或定时触发时向 Collector 的 /api/v1/traces 发送 JSON 数组。
 * 支持配置批量大小、刷新间隔与超时。
 * </p>
 */
public class HttpExporter implements ExporterPort {

    private static final Logger log = LoggerFactory.getLogger(HttpExporter.class);

    private final String collectorUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final BlockingQueue<Object> buffer;
    private final int batchSize;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService exportExecutor;
    private final AtomicBoolean ready = new AtomicBoolean(true);

    public HttpExporter(AgentLensConfig config) {
        this.collectorUrl = config.getCollectorUrl();
        this.batchSize = config.getBatchSize();

        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(config.getExportTimeout())
            .build();

        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.buffer = new LinkedBlockingQueue<>(10000);
        this.exportExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "agentlens-exporter");
            t.setDaemon(true);
            return t;
        });

        if (config.isEnableAutoFlush()) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "agentlens-flush-scheduler");
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleAtFixedRate(
                this::flush,
                config.getFlushInterval().toMillis(),
                config.getFlushInterval().toMillis(),
                TimeUnit.MILLISECONDS
            );
        } else {
            this.scheduler = null;
        }
    }

    @Override
    public void exportSpan(Span span) {
        if (!ready.get()) {
            return;
        }

        Map<String, Object> spanData = convertSpanToMap(span);
        if (!buffer.offer(spanData)) {
            log.warn("Export buffer full, dropping span: {}", span.getId());
        }

        if (buffer.size() >= batchSize) {
            exportExecutor.submit(this::flush);
        }
    }

    @Override
    public void exportTrace(Trace trace) {
        if (!ready.get()) {
            return;
        }

        Map<String, Object> traceData = convertTraceToMap(trace);
        if (!buffer.offer(traceData)) {
            log.warn("Export buffer full, dropping trace: {}", trace.getId());
        }
    }

    @Override
    public void flush() {
        if (buffer.isEmpty()) {
            return;
        }

        List<Object> batch = new ArrayList<>();
        buffer.drainTo(batch, batchSize);

        if (batch.isEmpty()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(batch);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(collectorUrl + "/api/v1/traces"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.debug("Exported {} items successfully", batch.size());
            } else {
                log.warn("Export failed with status {}: {}", response.statusCode(), response.body());
                buffer.addAll(batch);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Export failed", e);
            buffer.addAll(batch);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void shutdown() {
        ready.set(false);

        if (scheduler != null) {
            scheduler.shutdown();
        }

        flush();

        exportExecutor.shutdown();
        try {
            if (!exportExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                exportExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            exportExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("HttpExporter shutdown complete");
    }

    @Override
    public boolean isReady() {
        return ready.get();
    }

    private Map<String, Object> convertSpanToMap(Span span) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "span");
        map.put("spanId", span.getId().getValue());
        map.put("traceId", span.getTraceId().getValue());
        map.put("parentSpanId", span.getParentSpanId() != null ? span.getParentSpanId().getValue() : null);
        map.put("name", span.getName());
        map.put("spanType", span.getType().getValue());
        map.put("startTime", span.getStartTime().toString());
        map.put("endTime", span.getEndTime() != null ? span.getEndTime().toString() : null);
        map.put("durationMs", span.getDurationMs());
        map.put("status", span.getStatus().getValue());
        map.put("attributes", span.getAttributes().toMap());
        map.put("errorMessage", span.getErrorMessage());

        if (span.getCost() != null) {
            Map<String, Object> cost = new HashMap<>();
            cost.put("inputTokens", span.getCost().getInputTokens());
            cost.put("outputTokens", span.getCost().getOutputTokens());
            cost.put("totalTokens", span.getCost().getTotalTokens());
            cost.put("costUsd", span.getCost().getCostUsd().doubleValue());
            cost.put("model", span.getCost().getModel());
            cost.put("provider", span.getCost().getProvider());
            map.put("cost", cost);
        }

        return map;
    }

    private Map<String, Object> convertTraceToMap(Trace trace) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "trace");
        map.put("traceId", trace.getId().getValue());
        map.put("projectId", trace.getProjectId());
        map.put("startTime", trace.getStartTime().toString());
        map.put("endTime", trace.getEndTime() != null ? trace.getEndTime().toString() : null);
        map.put("durationMs", trace.getDurationMs());
        map.put("status", trace.getStatus().getValue());
        map.put("spanCount", trace.getSpanCount());
        map.put("totalTokens", trace.getTotalTokens());
        map.put("totalCostUsd", trace.getTotalCost().doubleValue());
        map.put("rootSpanId", trace.getRootSpanId() != null ? trace.getRootSpanId().getValue() : null);
        return map;
    }
}
