package io.agentlens.infrastructure.adapters.driven;

import io.agentlens.application.ports.driven.ExporterPort;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.Trace;

/**
 * 空操作导出器：不发送任何数据。
 * <p>
 * 用于单元测试或需要关闭追踪又不改代码的场景。
 * </p>
 */
public class NoopExporter implements ExporterPort {

    @Override
    public void exportSpan(Span span) {
        // No-op
    }

    @Override
    public void exportTrace(Trace trace) {
        // No-op
    }

    @Override
    public void flush() {
        // No-op
    }

    @Override
    public void shutdown() {
        // No-op
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
