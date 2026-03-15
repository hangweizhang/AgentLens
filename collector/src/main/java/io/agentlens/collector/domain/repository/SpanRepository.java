package io.agentlens.collector.domain.repository;

import io.agentlens.collector.domain.model.SpanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Span 记录仓储接口。
 * <p>
 * 支持按 traceId 查询 Span 列表、按类型统计、按模型统计用量与成本、按项目与时间查询错误 Span。
 * </p>
 */
@Repository
public interface SpanRepository extends JpaRepository<SpanRecord, String> {

    List<SpanRecord> findByTraceIdOrderByStartTimeAsc(String traceId);

    List<SpanRecord> findByTraceIdAndSpanType(String traceId, String spanType);

    @Query("SELECT s.spanType, COUNT(s), SUM(s.durationMs), SUM(s.costUsd) " +
           "FROM SpanRecord s WHERE s.traceId IN " +
           "(SELECT t.traceId FROM TraceRecord t WHERE t.projectId = :projectId AND t.startTime >= :since) " +
           "GROUP BY s.spanType")
    List<Object[]> getSpanTypeStatsByProjectIdSince(@Param("projectId") String projectId, @Param("since") Instant since);

    @Query("SELECT s.model, s.provider, COUNT(s), SUM(s.inputTokens), SUM(s.outputTokens), SUM(s.costUsd) " +
           "FROM SpanRecord s WHERE s.spanType = 'llm' AND s.traceId IN " +
           "(SELECT t.traceId FROM TraceRecord t WHERE t.projectId = :projectId AND t.startTime >= :since) " +
           "GROUP BY s.model, s.provider ORDER BY SUM(s.costUsd) DESC")
    List<Object[]> getModelUsageStatsByProjectIdSince(@Param("projectId") String projectId, @Param("since") Instant since);

    @Query("SELECT s FROM SpanRecord s WHERE s.status = 'error' AND s.traceId IN " +
           "(SELECT t.traceId FROM TraceRecord t WHERE t.projectId = :projectId AND t.startTime >= :since) " +
           "ORDER BY s.startTime DESC")
    List<SpanRecord> findErrorSpansByProjectIdSince(@Param("projectId") String projectId, @Param("since") Instant since);
}
