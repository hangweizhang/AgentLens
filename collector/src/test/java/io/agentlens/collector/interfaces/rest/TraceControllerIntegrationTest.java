package io.agentlens.collector.interfaces.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MVP 测试方案 - Collector 集成测试 I-01～I-06
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TraceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("I-06 GET /api/v1/health 返回 200，body 含 status=ok")
    void health_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    @Order(2)
    @DisplayName("I-01 POST /api/v1/traces 上报 trace，返回 200，后续 GET 可查到")
    void ingestTrace_thenListTraces_containsTrace() throws Exception {
        List<Map<String, Object>> body = List.of(
            Map.<String, Object>of(
                "type", "trace",
                "traceId", "t1",
                "projectId", "p1",
                "status", "ok",
                "durationMs", 100L,
                "spanCount", 1
            )
        );

        mockMvc.perform(post("/api/v1/traces")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    [{"type":"trace","traceId":"t1","projectId":"p1","status":"ok","durationMs":100,"spanCount":1}]
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/projects/p1/traces").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.content[?(@.traceId == 't1')]").exists());
    }

    @Test
    @Order(3)
    @DisplayName("I-02 POST /api/v1/traces 上报 span，GET trace 详情含该 Span、cost 正确")
    void ingestSpan_thenGetTraceDetail_containsSpanAndCost() throws Exception {
        mockMvc.perform(post("/api/v1/traces")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    [{"type":"span","spanId":"s1","traceId":"t1","name":"LLM.call","spanType":"llm","status":"ok",\
                    "startTime":"2026-03-15T00:00:00Z","endTime":"2026-03-15T00:00:01Z","durationMs":1000,\
                    "cost":{"inputTokens":10,"outputTokens":20,"totalTokens":30,"costUsd":0.001,"model":"gpt-4o","provider":"openai"}}]
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/traces/t1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.traceId").value("t1"))
            .andExpect(jsonPath("$.spans").isArray())
            .andExpect(jsonPath("$.spans[?(@.spanId == 's1')]").exists())
            .andExpect(jsonPath("$.spans[?(@.spanId == 's1')].name").value("LLM.call"))
            .andExpect(jsonPath("$.spans[?(@.spanId == 's1')].costUsd").value(0.001))
            .andExpect(jsonPath("$.spans[?(@.spanId == 's1')].inputTokens").value(10))
            .andExpect(jsonPath("$.spans[?(@.spanId == 's1')].outputTokens").value(20));
    }

    @Test
    @Order(4)
    @DisplayName("I-03 GET /api/v1/projects/p1/traces 返回 200，content 含 traceId t1")
    void listTraces_returnsContentWithTraceId() throws Exception {
        mockMvc.perform(get("/api/v1/projects/p1/traces").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[?(@.traceId == 't1')]").exists());
    }

    @Test
    @Order(5)
    @DisplayName("I-04 GET /api/v1/traces/t1 返回 200，body 含 traceId、spans 数组含 s1")
    void getTraceDetail_returnsTraceWithSpans() throws Exception {
        mockMvc.perform(get("/api/v1/traces/t1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.traceId").value("t1"))
            .andExpect(jsonPath("$.projectId").value("p1"))
            .andExpect(jsonPath("$.spans").isArray())
            .andExpect(jsonPath("$.spans[?(@.spanId == 's1')]").exists());
    }

    @Test
    @Order(6)
    @DisplayName("I-05 GET /api/v1/projects/p1/stats?days=7 返回 200，traceCount≥1，结构正确")
    void getProjectStats_returnsValidStructure() throws Exception {
        mockMvc.perform(get("/api/v1/projects/p1/stats").param("days", "7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectId").value("p1"))
            .andExpect(jsonPath("$.days").value(7))
            .andExpect(jsonPath("$.traceCount").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.totalCostUsd").exists())
            .andExpect(jsonPath("$.totalTokens").exists())
            .andExpect(jsonPath("$.errorCount").exists())
            .andExpect(jsonPath("$.errorRate").exists())
            .andExpect(jsonPath("$.spanTypeStats").isMap())
            .andExpect(jsonPath("$.modelUsage").isArray());
    }
}
