package io.agentlens.springai.aspect;

import io.agentlens.AgentLens;
import io.agentlens.domain.attributes.VectorDbAttributes;
import io.agentlens.domain.trace.Span;
import io.agentlens.domain.trace.SpanAttributes;
import io.agentlens.domain.trace.SpanType;
import io.agentlens.springai.AgentLensSpringAiProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Spring AI VectorStore 操作追踪切面。
 * <p>
 * 拦截 add、delete、similaritySearch 等，创建 VECTOR_DB 类型 Span，
 * 记录库类型（Qdrant/Milvus/PgVector 等）、操作类型与结果数量。
 * </p>
 */
@Aspect
public class VectorStoreTracingAspect {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreTracingAspect.class);

    private final AgentLensSpringAiProperties properties;

    public VectorStoreTracingAspect(AgentLensSpringAiProperties properties) {
        this.properties = properties;
    }

    @Around("execution(* org.springframework.ai.vectorstore.VectorStore.add(..))")
    public Object traceAdd(ProceedingJoinPoint pjp) throws Throwable {
        if (!AgentLens.isInitialized()) {
            return pjp.proceed();
        }

        String dbSystem = extractDbSystem(pjp);

        SpanAttributes attributes = SpanAttributes.builder()
            .put(VectorDbAttributes.DB_SYSTEM, dbSystem)
            .put(VectorDbAttributes.DB_OPERATION_NAME, VectorDbAttributes.Operations.INSERT)
            .build();

        Span span = AgentLens.startSpan("VectorStore.add", SpanType.VECTOR_DB);
        span.addAttributes(attributes);

        Object[] args = pjp.getArgs();
        if (args.length > 0 && args[0] instanceof List<?> documents) {
            span.addAttribute(VectorDbAttributes.INSERT_COUNT, documents.size());
        }

        try {
            Object result = pjp.proceed();
            AgentLens.endSpan(span);
            return result;
        } catch (Throwable e) {
            AgentLens.endSpanWithError(span, e);
            throw e;
        }
    }

    @Around("execution(* org.springframework.ai.vectorstore.VectorStore.delete(..))")
    public Object traceDelete(ProceedingJoinPoint pjp) throws Throwable {
        if (!AgentLens.isInitialized()) {
            return pjp.proceed();
        }

        String dbSystem = extractDbSystem(pjp);

        SpanAttributes attributes = SpanAttributes.builder()
            .put(VectorDbAttributes.DB_SYSTEM, dbSystem)
            .put(VectorDbAttributes.DB_OPERATION_NAME, VectorDbAttributes.Operations.DELETE)
            .build();

        Span span = AgentLens.startSpan("VectorStore.delete", SpanType.VECTOR_DB);
        span.addAttributes(attributes);

        Object[] args = pjp.getArgs();
        if (args.length > 0 && args[0] instanceof List<?> ids) {
            span.addAttribute(VectorDbAttributes.DELETE_COUNT, ids.size());
        }

        try {
            Object result = pjp.proceed();
            AgentLens.endSpan(span);
            return result;
        } catch (Throwable e) {
            AgentLens.endSpanWithError(span, e);
            throw e;
        }
    }

    @Around("execution(* org.springframework.ai.vectorstore.VectorStore.similaritySearch(..))")
    public Object traceSimilaritySearch(ProceedingJoinPoint pjp) throws Throwable {
        if (!AgentLens.isInitialized()) {
            return pjp.proceed();
        }

        String dbSystem = extractDbSystem(pjp);

        SpanAttributes attributes = SpanAttributes.builder()
            .put(VectorDbAttributes.DB_SYSTEM, dbSystem)
            .put(VectorDbAttributes.DB_OPERATION_NAME, VectorDbAttributes.Operations.SEARCH)
            .build();

        Span span = AgentLens.startSpan("VectorStore.similaritySearch", SpanType.VECTOR_DB);
        span.addAttributes(attributes);

        Object[] args = pjp.getArgs();
        extractSearchParams(span, args);

        try {
            Object result = pjp.proceed();

            if (result instanceof List<?> documents) {
                span.addAttribute(VectorDbAttributes.SEARCH_RESULTS_COUNT, documents.size());
            }

            AgentLens.endSpan(span);
            return result;
        } catch (Throwable e) {
            AgentLens.endSpanWithError(span, e);
            throw e;
        }
    }

    private String extractDbSystem(ProceedingJoinPoint pjp) {
        String className = pjp.getTarget().getClass().getSimpleName().toLowerCase();

        if (className.contains("qdrant")) return VectorDbAttributes.Systems.QDRANT;
        if (className.contains("milvus")) return VectorDbAttributes.Systems.MILVUS;
        if (className.contains("pgvector") || className.contains("postgres")) return VectorDbAttributes.Systems.PGVECTOR;
        if (className.contains("chroma")) return VectorDbAttributes.Systems.CHROMA;
        if (className.contains("pinecone")) return VectorDbAttributes.Systems.PINECONE;
        if (className.contains("weaviate")) return VectorDbAttributes.Systems.WEAVIATE;
        if (className.contains("elasticsearch")) return VectorDbAttributes.Systems.ELASTICSEARCH;
        if (className.contains("redis")) return VectorDbAttributes.Systems.REDIS;

        return "unknown";
    }

    private void extractSearchParams(Span span, Object[] args) {
        if (args == null || args.length == 0) return;

        for (Object arg : args) {
            if (arg instanceof String query) {
                if (properties.isCaptureContent()) {
                    String truncated = query.length() > properties.getMaxContentLength()
                        ? query.substring(0, properties.getMaxContentLength()) + "..."
                        : query;
                    span.setInput(truncated);
                }
            }

            try {
                if (arg != null && arg.getClass().getSimpleName().contains("SearchRequest")) {
                    java.lang.reflect.Method getTopK = arg.getClass().getMethod("getTopK");
                    Object topK = getTopK.invoke(arg);
                    if (topK instanceof Number n) {
                        span.addAttribute(VectorDbAttributes.SEARCH_TOP_K, n.intValue());
                    }

                    java.lang.reflect.Method getSimilarityThreshold = arg.getClass().getMethod("getSimilarityThreshold");
                    Object threshold = getSimilarityThreshold.invoke(arg);
                    if (threshold instanceof Number n) {
                        span.addAttribute(VectorDbAttributes.SEARCH_SCORE_THRESHOLD, n.doubleValue());
                    }
                }
            } catch (Exception e) {
                log.trace("Could not extract search params", e);
            }
        }
    }
}
