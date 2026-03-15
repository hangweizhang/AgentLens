package io.agentlens.infrastructure.config;

import java.time.Duration;
import java.util.Objects;

/**
 * AgentLens SDK 配置。
 * <p>
 * 包含项目 ID、Collector 地址、是否启用成本统计、导出器类型及批量/超时等参数。
 * 通过 {@link Builder} 链式构建。
 * </p>
 */
public class AgentLensConfig {

    /** 项目标识，用于在 Dashboard 中按项目筛选 */
    private final String projectId;
    /** Collector 或 OTLP 端点 URL */
    private final String collectorUrl;
    /** 是否自动计算并记录 LLM/Embedding 成本 */
    private final boolean enableCostTracking;
    /** 是否定时自动刷新导出缓冲区 */
    private final boolean enableAutoFlush;
    /** 自动刷新间隔 */
    private final Duration flushInterval;
    /** 批量导出时每批条数 */
    private final int batchSize;
    /** 单次导出请求超时 */
    private final Duration exportTimeout;
    /** 导出器类型：控制台 / HTTP / OTLP / 空操作 */
    private final ExporterType exporterType;

    private AgentLensConfig(Builder builder) {
        this.projectId = Objects.requireNonNull(builder.projectId, "projectId is required");
        this.collectorUrl = builder.collectorUrl;
        this.enableCostTracking = builder.enableCostTracking;
        this.enableAutoFlush = builder.enableAutoFlush;
        this.flushInterval = builder.flushInterval;
        this.batchSize = builder.batchSize;
        this.exportTimeout = builder.exportTimeout;
        this.exporterType = builder.exporterType;
    }

    public static Builder builder(String projectId) {
        return new Builder(projectId);
    }

    public String getProjectId() {
        return projectId;
    }

    public String getCollectorUrl() {
        return collectorUrl;
    }

    public boolean isEnableCostTracking() {
        return enableCostTracking;
    }

    public boolean isEnableAutoFlush() {
        return enableAutoFlush;
    }

    public Duration getFlushInterval() {
        return flushInterval;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public Duration getExportTimeout() {
        return exportTimeout;
    }

    public ExporterType getExporterType() {
        return exporterType;
    }

    /** 导出器类型 */
    public enum ExporterType {
        /** OTLP gRPC（规划中） */
        OTLP_GRPC,
        /** OTLP HTTP */
        OTLP_HTTP,
        /** 自定义 HTTP 上报到 AgentLens Collector */
        HTTP,
        /** 仅输出到日志/控制台，便于调试 */
        CONSOLE,
        /** 不导出，用于测试或关闭追踪 */
        NOOP
    }

    public static class Builder {
        private final String projectId;
        private String collectorUrl = "http://localhost:4317";
        private boolean enableCostTracking = true;
        private boolean enableAutoFlush = true;
        private Duration flushInterval = Duration.ofSeconds(5);
        private int batchSize = 100;
        private Duration exportTimeout = Duration.ofSeconds(10);
        private ExporterType exporterType = ExporterType.OTLP_HTTP;

        private Builder(String projectId) {
            this.projectId = projectId;
        }

        public Builder collectorUrl(String collectorUrl) {
            this.collectorUrl = collectorUrl;
            return this;
        }

        public Builder enableCostTracking(boolean enableCostTracking) {
            this.enableCostTracking = enableCostTracking;
            return this;
        }

        public Builder enableAutoFlush(boolean enableAutoFlush) {
            this.enableAutoFlush = enableAutoFlush;
            return this;
        }

        public Builder flushInterval(Duration flushInterval) {
            this.flushInterval = flushInterval;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder exportTimeout(Duration exportTimeout) {
            this.exportTimeout = exportTimeout;
            return this;
        }

        public Builder exporterType(ExporterType exporterType) {
            this.exporterType = exporterType;
            return this;
        }

        public AgentLensConfig build() {
            return new AgentLensConfig(this);
        }
    }
}
