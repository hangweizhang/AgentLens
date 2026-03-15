package io.agentlens.test;

import io.agentlens.application.ports.driven.ExporterPort;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.Trace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 测试用导出器：记录所有 exportSpan/exportTrace 调用，便于断言
 */
public class RecordingExporter implements ExporterPort {

    private final List<Span> exportedSpans = new CopyOnWriteArrayList<>();
    private final List<Trace> exportedTraces = new CopyOnWriteArrayList<>();
    private volatile boolean ready = true;

    @Override
    public void exportSpan(Span span) {
        exportedSpans.add(span);
    }

    @Override
    public void exportTrace(Trace trace) {
        exportedTraces.add(trace);
    }

    @Override
    public void flush() {}

    @Override
    public void shutdown() {
        ready = false;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    public List<Span> getExportedSpans() {
        return new ArrayList<>(exportedSpans);
    }

    public List<Trace> getExportedTraces() {
        return new ArrayList<>(exportedTraces);
    }

    public Span getLastExportedSpan() {
        return exportedSpans.isEmpty() ? null : exportedSpans.get(exportedSpans.size() - 1);
    }

    public void clear() {
        exportedSpans.clear();
        exportedTraces.clear();
    }
}
