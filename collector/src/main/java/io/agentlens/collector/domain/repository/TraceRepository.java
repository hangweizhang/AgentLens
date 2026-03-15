package io.agentlens.collector.domain.repository;

import io.agentlens.collector.domain.model.TraceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Trace 记录仓储接口。
 * <p>
 * 支持按项目、状态、时间范围查询，以及按项目与时间汇总数量、成本、Token、错误数。
 * </p>
 */
@Repository
public interface TraceRepository extends JpaRepository<TraceRecord, String> {

    Page<TraceRecord> findByProjectIdOrderByStartTimeDesc(String projectId, Pageable pageable);

    Page<TraceRecord> findByProjectIdAndStatusOrderByStartTimeDesc(String projectId, String status, Pageable pageable);

    @Query("SELECT t FROM TraceRecord t WHERE t.projectId = :projectId " +
           "AND t.startTime BETWEEN :startTime AND :endTime " +
           "ORDER BY t.startTime DESC")
    List<TraceRecord> findByProjectIdAndTimeRange(
        @Param("projectId") String projectId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    @Query("SELECT COUNT(t) FROM TraceRecord t WHERE t.projectId = :projectId " +
           "AND t.startTime >= :since")
    long countByProjectIdSince(@Param("projectId") String projectId, @Param("since") Instant since);

    @Query("SELECT SUM(t.totalCostUsd) FROM TraceRecord t WHERE t.projectId = :projectId " +
           "AND t.startTime >= :since")
    java.math.BigDecimal sumCostByProjectIdSince(@Param("projectId") String projectId, @Param("since") Instant since);

    @Query("SELECT SUM(t.totalTokens) FROM TraceRecord t WHERE t.projectId = :projectId " +
           "AND t.startTime >= :since")
    Long sumTokensByProjectIdSince(@Param("projectId") String projectId, @Param("since") Instant since);

    @Query("SELECT COUNT(t) FROM TraceRecord t WHERE t.projectId = :projectId " +
           "AND t.status = 'error' AND t.startTime >= :since")
    long countErrorsByProjectIdSince(@Param("projectId") String projectId, @Param("since") Instant since);
}
