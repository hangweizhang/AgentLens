package io.agentlens;

import io.agentlens.application.ports.driven.ExporterPort;
import io.agentlens.application.ports.driver.TracingPort;
import io.agentlens.application.usecases.TracingService;
import io.agentlens.domain.cost.PricingRegistry;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.SpanType;
import io.agentlens.domain.trace.Trace;
import io.agentlens.infrastructure.adapters.driven.ConsoleExporter;
import io.agentlens.infrastructure.adapters.driven.HttpExporter;
import io.agentlens.infrastructure.adapters.driven.NoopExporter;
import io.agentlens.infrastructure.config.AgentLensConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * AgentLens SDK 统一入口。
 * <p>
 * 提供静态方法：初始化、创建 Trace/Span、在 Span 内执行代码、结束 Span/Trace。
 * 使用前必须调用 {@link #init(AgentLensConfig)} 或 {@link #init(String)}。
 * </p>
 *
 * <p>示例：
 * <pre>{@code
 * // 初始化
 * AgentLens.init(AgentLensConfig.builder("my-project")
 *     .collectorUrl("http://localhost:4317")
 *     .enableCostTracking(true)
 *     .build());
 *
 * // 开始一次追踪
 * Trace trace = AgentLens.startTrace();
 *
 * // 在 Span 内执行并自动记录耗时/异常
 * String result = AgentLens.trace("ChatModel.call", SpanType.LLM, () -> {
 *     return chatModel.call(prompt);
 * });
 *
 * // 完成当前 Trace
 * AgentLens.completeTrace();
 * }</pre>
 */
public final class AgentLens {

    private static final Logger log = LoggerFactory.getLogger(AgentLens.class);

    /** 追踪服务（用例实现） */
    private static volatile TracingPort tracer;
    /** 当前配置 */
    private static volatile AgentLensConfig config;
    /** 是否已初始化 */
    private static volatile boolean initialized = false;

    private AgentLens() {}

    /**
     * 使用给定配置初始化 AgentLens（仅首次有效，重复调用会忽略）。
     */
    public static synchronized void init(AgentLensConfig config) {
        if (initialized) {
            log.warn("AgentLens already initialized, ignoring re-initialization");
            return;
        }

        AgentLens.config = config;
        ExporterPort exporter = createExporter(config);
        AgentLens.tracer = new TracingService(config.getProjectId(), exporter);
        initialized = true;

        log.info("AgentLens initialized: project={}, exporter={}, costTracking={}",
            config.getProjectId(),
            config.getExporterType(),
            config.isEnableCostTracking());

        Runtime.getRuntime().addShutdownHook(new Thread(AgentLens::shutdown));
    }

    /**
     * 使用最小配置初始化（仅项目 ID，导出到控制台，便于快速试用）。
     */
    public static void init(String projectId) {
        init(AgentLensConfig.builder(projectId)
            .exporterType(AgentLensConfig.ExporterType.CONSOLE)
            .build());
    }

    private static ExporterPort createExporter(AgentLensConfig config) {
        return switch (config.getExporterType()) {
            case CONSOLE -> new ConsoleExporter();
            case HTTP, OTLP_HTTP -> new HttpExporter(config);
            case NOOP -> new NoopExporter();
            default -> {
                log.warn("Unsupported exporter type: {}, falling back to console", config.getExporterType());
                yield new ConsoleExporter();
            }
        };
    }

    /** 获取当前 Tracer 实例 */
    public static TracingPort getTracer() {
        ensureInitialized();
        return tracer;
    }

    /** 获取当前配置 */
    public static AgentLensConfig getConfig() {
        return config;
    }

    /** 获取定价注册表（可注册自定义模型价格） */
    public static PricingRegistry getPricingRegistry() {
        return PricingRegistry.getInstance();
    }

    /** 使用默认项目 ID 开始一次新 Trace */
    public static Trace startTrace() {
        ensureInitialized();
        return tracer.startTrace();
    }

    /** 使用指定项目 ID 开始一次新 Trace */
    public static Trace startTrace(String projectId) {
        ensureInitialized();
        return tracer.startTrace(projectId);
    }

    /** 获取当前线程的活跃 Trace（如有） */
    public static Optional<Trace> getCurrentTrace() {
        ensureInitialized();
        return tracer.getCurrentTrace();
    }

    /** 在当前 Trace 下开始一个新 Span（父 Span 为当前 Span） */
    public static Span startSpan(String name, SpanType type) {
        ensureInitialized();
        return tracer.startSpan(name, type);
    }

    /** 正常结束指定 Span */
    public static void endSpan(Span span) {
        ensureInitialized();
        tracer.endSpan(span);
    }

    /** 以异常结束指定 Span（会记录错误信息） */
    public static void endSpanWithError(Span span, Throwable error) {
        ensureInitialized();
        tracer.endSpanWithError(span, error);
    }

    /** 获取当前线程的栈顶 Span（如有） */
    public static Optional<Span> getCurrentSpan() {
        ensureInitialized();
        return tracer.getCurrentSpan();
    }

    /** 完成当前 Trace 并触发导出 */
    public static void completeTrace() {
        ensureInitialized();
        tracer.completeTrace();
    }

    /** 在名为 name、类型为 type 的 Span 内执行 operation，自动记录耗时与异常，并返回结果 */
    public static <T> T trace(String name, SpanType type, Supplier<T> operation) {
        ensureInitialized();
        return tracer.trace(name, type, operation);
    }

    /** 在 Span 内执行无返回值的 operation（void 版本） */
    public static void trace(String name, SpanType type, Runnable operation) {
        ensureInitialized();
        tracer.trace(name, type, operation);
    }

    /** 关闭 AgentLens 并刷新未发送数据（通常由 JVM 关闭钩子调用） */
    public static synchronized void shutdown() {
        if (!initialized) {
            return;
        }

        log.info("Shutting down AgentLens...");
        tracer.completeTrace();
        initialized = false;
        tracer = null;
        config = null;
    }

    /** 是否已完成初始化 */
    public static boolean isInitialized() {
        return initialized;
    }

    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("AgentLens not initialized. Call AgentLens.init() first.");
        }
    }
}
