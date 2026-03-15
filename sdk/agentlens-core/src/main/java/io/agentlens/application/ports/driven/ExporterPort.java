package io.agentlens.application.ports.driven;

import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.Trace;

/**
 * 追踪数据导出驱动端口（出站端口）。
 * <p>
 * 将已完成的 Span/Trace 发送到外部系统（如 AgentLens Collector、OTLP、控制台）。
 * 实现类：{@link io.agentlens.infrastructure.adapters.driven.HttpExporter}、
 * {@link io.agentlens.infrastructure.adapters.driven.ConsoleExporter} 等。
 * </p>
 */
public interface ExporterPort {

    /** 导出已结束的 Span */
    void exportSpan(Span span);

    /** 导出已完成的 Trace（含其下所有 Span 的汇总信息） */
    void exportTrace(Trace trace);

    /** 刷新缓冲区（如有） */
    void flush();

    /** 关闭导出器并释放资源 */
    void shutdown();

    /** 是否可接收数据 */
    boolean isReady();
}
