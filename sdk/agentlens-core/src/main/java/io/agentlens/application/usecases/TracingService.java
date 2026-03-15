package io.agentlens.application.usecases;

import io.agentlens.application.ports.driven.ExporterPort;
import io.agentlens.application.ports.driver.TracingPort;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.SpanId;
import io.agentlens.domain.trace.SpanStatus;
import io.agentlens.domain.trace.SpanType;
import io.agentlens.domain.trace.Trace;
import io.agentlens.domain.trace.TraceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 追踪用例的应用服务实现。
 * <p>
 * 负责 Trace/Span 的生命周期管理：创建、结束、维护当前 Trace 与 Span 栈（ThreadLocal），
 * 并在 Span 结束或 Trace 完成时调用 Exporter 导出数据。
 * </p>
 */
public class TracingService implements TracingPort {

    private static final Logger log = LoggerFactory.getLogger(TracingService.class);

    /** 默认项目 ID（未显式指定时使用） */
    private final String defaultProjectId;
    /** 导出器（将 Span/Trace 发送到 Collector 等） */
    private final ExporterPort exporter;

    /** 当前线程的活跃 Trace */
    private final ThreadLocal<Trace> currentTrace = new ThreadLocal<>();
    /** 当前线程的 Span 栈（用于确定父子关系与当前 Span） */
    private final ThreadLocal<Deque<Span>> spanStack = ThreadLocal.withInitial(LinkedList::new);
    /** 所有活跃 Trace（按 TraceId 索引） */
    private final Map<TraceId, Trace> activeTraces = new ConcurrentHashMap<>();

    public TracingService(String defaultProjectId, ExporterPort exporter) {
        this.defaultProjectId = Objects.requireNonNull(defaultProjectId, "defaultProjectId is required");
        this.exporter = Objects.requireNonNull(exporter, "exporter is required");
    }

    @Override
    public Trace startTrace() {
        return startTrace(defaultProjectId);
    }

    @Override
    public Trace startTrace(String projectId) {
        Trace trace = Trace.create(projectId);
        currentTrace.set(trace);
        activeTraces.put(trace.getId(), trace);
        log.debug("Started trace: {}", trace.getId());
        return trace;
    }

    @Override
    public Optional<Trace> getCurrentTrace() {
        return Optional.ofNullable(currentTrace.get());
    }

    @Override
    public Optional<Trace> getTrace(TraceId traceId) {
        return Optional.ofNullable(activeTraces.get(traceId));
    }

    @Override
    public Span startSpan(String name, SpanType type) {
        SpanId parentSpanId = getCurrentSpan().map(Span::getId).orElse(null);
        return startSpan(name, type, parentSpanId);
    }

    @Override
    public Span startSpan(String name, SpanType type, SpanId parentSpanId) {
        Trace trace = currentTrace.get();
        if (trace == null) {
            trace = startTrace();
        }

        Span span = trace.startSpan(name, type, parentSpanId);
        spanStack.get().push(span);
        log.debug("Started span: {} ({})", name, span.getId());
        return span;
    }

    @Override
    public void endSpan(Span span) {
        endSpanInternal(span, SpanStatus.OK, null);
    }

    @Override
    public void endSpanWithError(Span span, Throwable error) {
        endSpanInternal(span, SpanStatus.ERROR, error);
    }

    private void endSpanInternal(Span span, SpanStatus status, Throwable error) {
        Trace trace = currentTrace.get();
        if (trace == null) {
            log.warn("Attempted to end span {} but no active trace", span.getId());
            return;
        }

        if (!span.isCompleted()) {
            if (error != null) {
                span.endWithError(error);
            } else {
                span.end(status);
            }
        }

        trace.endSpan(span, span.getStatus());

        Deque<Span> stack = spanStack.get();
        if (!stack.isEmpty() && stack.peek().getId().equals(span.getId())) {
            stack.pop();
        }

        exporter.exportSpan(span);
        log.debug("Ended span: {} ({}) - {}ms", span.getName(), span.getId(), span.getDurationMs());
    }

    @Override
    public Optional<Span> getCurrentSpan() {
        Deque<Span> stack = spanStack.get();
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack.peek());
    }

    @Override
    public void completeTrace() {
        Trace trace = currentTrace.get();
        if (trace != null) {
            completeTrace(trace);
        }
    }

    @Override
    public void completeTrace(Trace trace) {
        if (trace == null) {
            return;
        }

        trace.complete();
        exporter.exportTrace(trace);
        activeTraces.remove(trace.getId());

        if (currentTrace.get() != null && currentTrace.get().getId().equals(trace.getId())) {
            currentTrace.remove();
            spanStack.remove();
        }

        log.debug("Completed trace: {} - {}ms, {} spans", 
            trace.getId(), trace.getDurationMs(), trace.getSpanCount());
    }

    @Override
    public <T> T trace(String name, SpanType type, Supplier<T> operation) {
        Span span = startSpan(name, type);
        try {
            T result = operation.get();
            span.setOutput(result);
            endSpan(span);
            return result;
        } catch (Exception e) {
            endSpanWithError(span, e);
            throw e;
        }
    }

    @Override
    public void trace(String name, SpanType type, Runnable operation) {
        trace(name, type, () -> {
            operation.run();
            return null;
        });
    }
}
