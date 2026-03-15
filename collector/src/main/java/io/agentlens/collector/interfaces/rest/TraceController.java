package io.agentlens.collector.interfaces.rest;

import io.agentlens.collector.application.service.TraceIngestionService;
import io.agentlens.collector.application.service.TraceQueryService;
import io.agentlens.collector.interfaces.rest.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Trace 相关 REST 接口。
 * <p>
 * 提供：POST /traces 接收 SDK 上报；GET /projects/{id}/traces 分页列表；
 * GET /traces/{id} 详情；GET /projects/{id}/stats 统计。供 Dashboard 调用。
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class TraceController {

    private final TraceIngestionService ingestionService;
    private final TraceQueryService queryService;

    public TraceController(TraceIngestionService ingestionService, TraceQueryService queryService) {
        this.ingestionService = ingestionService;
        this.queryService = queryService;
    }

    /**
     * Ingest trace/span data from SDK.
     */
    @PostMapping("/traces")
    public ResponseEntity<Void> ingestTraces(@RequestBody List<Map<String, Object>> items) {
        ingestionService.ingestBatch(items);
        return ResponseEntity.ok().build();
    }

    /**
     * List traces for a project.
     */
    @GetMapping("/projects/{projectId}/traces")
    public ResponseEntity<Page<TraceListItemDTO>> listTraces(
        @PathVariable("projectId") String projectId,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Page<TraceListItemDTO> traces = queryService.listTraces(projectId, status, page, size);
        return ResponseEntity.ok(traces);
    }

    /**
     * Get trace detail with all spans.
     */
    @GetMapping("/traces/{traceId}")
    public ResponseEntity<TraceDetailDTO> getTraceDetail(@PathVariable("traceId") String traceId) {
        return queryService.getTraceDetail(traceId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get project statistics.
     */
    @GetMapping("/projects/{projectId}/stats")
    public ResponseEntity<ProjectStatsDTO> getProjectStats(
        @PathVariable("projectId") String projectId,
        @RequestParam(name = "days", defaultValue = "7") int days
    ) {
        ProjectStatsDTO stats = queryService.getProjectStats(projectId, days);
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
