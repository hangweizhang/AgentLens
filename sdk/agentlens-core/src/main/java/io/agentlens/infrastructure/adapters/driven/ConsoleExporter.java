package io.agentlens.infrastructure.adapters.driven;

import io.agentlens.application.ports.driven.ExporterPort;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 控制台导出器（驱动端适配器）。
 * <p>
 * 将 Span/Trace 以日志形式输出到控制台，适用于开发调试；生产环境建议使用 HttpExporter。
 * </p>
 */
public class ConsoleExporter implements ExporterPort {

    private static final Logger log = LoggerFactory.getLogger(ConsoleExporter.class);

    private volatile boolean ready = true;

    @Override
    public void exportSpan(Span span) {
        log.info("[SPAN] {} | type={} | status={} | duration={}ms | trace={}",
            span.getName(),
            span.getType().getValue(),
            span.getStatus().getValue(),
            span.getDurationMs(),
            span.getTraceId()
        );

        if (span.getCost() != null) {
            log.info("  └─ cost: ${} | tokens: in={}, out={}",
                span.getCost().getCostUsd(),
                span.getCost().getInputTokens(),
                span.getCost().getOutputTokens()
            );
        }

        if (span.getErrorMessage() != null) {
            log.info("  └─ error: {}", span.getErrorMessage());
        }
    }

    @Override
    public void exportTrace(Trace trace) {
        log.info("[TRACE] {} | project={} | status={} | duration={}ms | spans={} | cost=${}",
            trace.getId(),
            trace.getProjectId(),
            trace.getStatus().getValue(),
            trace.getDurationMs(),
            trace.getSpanCount(),
            trace.getTotalCost()
        );
    }

    @Override
    public void flush() {
        // Console exporter writes immediately, nothing to flush
    }

    @Override
    public void shutdown() {
        ready = false;
        log.info("ConsoleExporter shutdown");
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
