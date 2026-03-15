package io.agentlens.application.ports.driven;

import io.agentlens.domain.trace.Trace;
import io.agentlens.domain.trace.TraceId;

import java.util.Optional;

/**
 * Trace 持久化驱动端口（出站端口）。
 * <p>
 * 用于在本地存储与查询 Trace（如重放、调试功能）。当前 Collector 侧独立存储，SDK 侧可选实现。
 * </p>
 */
public interface TraceRepositoryPort {

    /** 保存 Trace */
    void save(Trace trace);

    /** 按 ID 查询 Trace */
    Optional<Trace> findById(TraceId traceId);

    /** 删除 Trace */
    void delete(TraceId traceId);
}
