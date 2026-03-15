package io.agentlens.application.usecases;

import io.agentlens.application.ports.driven.ExporterPort;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.SpanStatus;
import io.agentlens.domain.trace.SpanType;
import io.agentlens.domain.trace.Trace;
import io.agentlens.infrastructure.adapters.driven.NoopExporter;
import io.agentlens.test.RecordingExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MVP 测试方案 - 应用层 U-A-02～U-A-06：TracingService 行为
 */
class TracingServiceTest {

    private static final String PROJECT_ID = "test-project";
    private RecordingExporter recordingExporter;
    private TracingService tracingService;

    @BeforeEach
    void setUp() {
        recordingExporter = new RecordingExporter();
        tracingService = new TracingService(PROJECT_ID, recordingExporter);
    }

    @Nested
    @DisplayName("U-A-02 init 后 startTrace(projectId) 返回非 null，getCurrentTrace 一致")
    class StartTrace {

        @Test
        void startTrace_returnsTraceAndCurrentTraceMatches() {
            Trace trace = tracingService.startTrace(PROJECT_ID);
            assertThat(trace).isNotNull();
            assertThat(trace.getProjectId()).isEqualTo(PROJECT_ID);
            assertThat(tracingService.getCurrentTrace()).isPresent().get().isSameAs(trace);
        }
    }

    @Nested
    @DisplayName("U-A-03 startSpan 后 getCurrentSpan 为刚创建的 Span")
    class StartSpan {

        @Test
        void startSpan_thenGetCurrentSpan_isSameSpan() {
            tracingService.startTrace();
            Span span = tracingService.startSpan("x", SpanType.LLM);
            assertThat(span).isNotNull();
            assertThat(span.getName()).isEqualTo("x");
            assertThat(span.getType()).isEqualTo(SpanType.LLM);
            assertThat(tracingService.getCurrentSpan()).isPresent().get().isSameAs(span);
        }
    }

    @Nested
    @DisplayName("U-A-04 trace(Supplier) 返回 ok，Span 状态 OK、有 output")
    class TraceSupplierSuccess {

        @Test
        void trace_returnsResult_spanOkWithOutput() {
            tracingService.startTrace();
            String result = tracingService.trace("op", SpanType.LLM, () -> "ok");
            assertThat(result).isEqualTo("ok");

            Span exported = recordingExporter.getLastExportedSpan();
            assertThat(exported).isNotNull();
            assertThat(exported.getStatus()).isEqualTo(SpanStatus.OK);
            assertThat(exported.getEndTime()).isNotNull();
            assertThat(exported.getOutput()).isEqualTo("ok");
        }
    }

    @Nested
    @DisplayName("U-A-05 trace(Supplier) 抛异常时 Span 状态 ERROR、带 errorMessage")
    class TraceSupplierException {

        @Test
        void trace_whenSupplierThrows_spanErrorWithMessage() {
            tracingService.startTrace();
            assertThatThrownBy(() ->
                tracingService.trace("op", SpanType.LLM, () -> {
                    throw new RuntimeException("e");
                })
            ).isInstanceOf(RuntimeException.class).hasMessageContaining("e");

            Span exported = recordingExporter.getLastExportedSpan();
            assertThat(exported).isNotNull();
            assertThat(exported.getStatus()).isEqualTo(SpanStatus.ERROR);
            assertThat(exported.getErrorMessage()).contains("e");
        }
    }

    @Nested
    @DisplayName("U-A-06 NoopExporter 时 exportSpan/exportTrace 不抛异常")
    class NoopExporterFlow {

        @Test
        void fullFlow_withNoopExporter_noException() {
            TracingService service = new TracingService(PROJECT_ID, new NoopExporter());
            Trace trace = service.startTrace();
            Span span = service.startSpan("llm", SpanType.LLM);
            span.setOutput("out");
            service.endSpan(span);
            service.completeTrace();
            // 无异常即通过
        }
    }
}
