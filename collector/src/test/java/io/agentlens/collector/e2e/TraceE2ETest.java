package io.agentlens.collector.e2e;

import io.agentlens.AgentLens;
import io.agentlens.domain.cost.TokenCost;
import io.agentlens.domain.trace.SpanType;
import io.agentlens.infrastructure.config.AgentLensConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MVP E2E：SDK 上报 → Collector 接收 → API 可查
 * 需先启动 Spring 容器（随机端口），再用 AgentLens 打点并请求本地 Collector 验证
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TraceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String E2E_PROJECT = "mvp-e2e-test";

    @BeforeEach
    void setUp() {
        if (AgentLens.isInitialized()) {
            AgentLens.shutdown();
        }
    }

    @AfterEach
    void tearDown() {
        if (AgentLens.isInitialized()) {
            AgentLens.shutdown();
        }
    }

    @Test
    @DisplayName("E2E: SDK 上报 Trace+Span 后，GET 列表与详情可查到")
    void sdkReportedTrace_appearsInCollectorApi() throws InterruptedException {
        String collectorUrl = "http://localhost:" + port;

        AgentLens.init(AgentLensConfig.builder(E2E_PROJECT)
            .collectorUrl(collectorUrl)
            .exporterType(AgentLensConfig.ExporterType.HTTP)
            .enableCostTracking(true)
            .enableAutoFlush(true)
            .flushInterval(java.time.Duration.ofMillis(500))
            .build());

        var trace = AgentLens.startTrace();
        var span = AgentLens.startSpan("DemoLLM.call", SpanType.LLM);
        span.addAttribute("gen_ai.request.model", "gpt-4o");
        span.addAttribute("gen_ai.provider.name", "openai");
        span.addAttribute("gen_ai.usage.input_tokens", 100);
        span.addAttribute("gen_ai.usage.output_tokens", 50);
        span.setCost(TokenCost.of(100, 50, new BigDecimal("0.00125")));
        AgentLens.endSpan(span);
        AgentLens.completeTrace();

        // 等待 HttpExporter 批量或定时发送
        Thread.sleep(2500);

        // GET /api/v1/projects/mvp-e2e-test/traces
        var listResponse = restTemplate.exchange(
            collectorUrl + "/api/v1/projects/" + E2E_PROJECT + "/traces?page=0&size=20",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        assertThat(listResponse.getStatusCode().is2xxSuccessful()).isTrue();

        @SuppressWarnings("unchecked")
        var content = (java.util.List<Map<String, Object>>) listResponse.getBody().get("content");
        assertThat(content).isNotEmpty();
        assertThat(content.get(0).get("projectId")).isEqualTo(E2E_PROJECT);

        String traceId = (String) content.get(0).get("traceId");

        // GET /api/v1/traces/{traceId}
        var detailResponse = restTemplate.getForEntity(
            collectorUrl + "/api/v1/traces/" + traceId,
            Map.class
        );
        assertThat(detailResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> detail = detailResponse.getBody();
        assertThat(detail.get("traceId")).isEqualTo(traceId);
        assertThat(detail.get("spans")).asList().isNotEmpty();
    }
}
