package io.agentlens.domain.attributes;

/**
 * 向量数据库相关 Span 的语义属性常量。
 * <p>
 * 扩展 OpenTelemetry 数据库约定，用于 Qdrant/Milvus/PgVector 等检索、写入操作的属性键名。
 * </p>
 */
public final class VectorDbAttributes {

    private VectorDbAttributes() {}

    // Standard DB attributes
    public static final String DB_SYSTEM = "db.system";
    public static final String DB_OPERATION_NAME = "db.operation.name";
    public static final String DB_COLLECTION_NAME = "db.collection.name";
    public static final String DB_NAMESPACE = "db.namespace";

    // Vector-specific attributes
    public static final String VECTOR_DIMENSION = "vector.dimension";
    public static final String VECTOR_DISTANCE_METRIC = "vector.distance_metric";

    // Search attributes
    public static final String SEARCH_TOP_K = "vector.search.top_k";
    public static final String SEARCH_RESULTS_COUNT = "vector.search.results_count";
    public static final String SEARCH_SCORE_THRESHOLD = "vector.search.score_threshold";
    public static final String SEARCH_FILTER = "vector.search.filter";
    public static final String SEARCH_MIN_SCORE = "vector.search.min_score";
    public static final String SEARCH_MAX_SCORE = "vector.search.max_score";

    // Insert/Update attributes
    public static final String INSERT_COUNT = "vector.insert.count";
    public static final String UPDATE_COUNT = "vector.update.count";
    public static final String DELETE_COUNT = "vector.delete.count";

    // Operation names
    public static final class Operations {
        public static final String SEARCH = "search";
        public static final String INSERT = "insert";
        public static final String UPDATE = "update";
        public static final String DELETE = "delete";
        public static final String UPSERT = "upsert";
        public static final String GET = "get";

        private Operations() {}
    }

    // Database systems
    public static final class Systems {
        public static final String QDRANT = "qdrant";
        public static final String MILVUS = "milvus";
        public static final String PGVECTOR = "pgvector";
        public static final String CHROMA = "chroma";
        public static final String PINECONE = "pinecone";
        public static final String WEAVIATE = "weaviate";
        public static final String ELASTICSEARCH = "elasticsearch";
        public static final String REDIS = "redis";

        private Systems() {}
    }

    // Distance metrics
    public static final class DistanceMetrics {
        public static final String COSINE = "cosine";
        public static final String EUCLIDEAN = "euclidean";
        public static final String DOT_PRODUCT = "dot_product";
        public static final String MANHATTAN = "manhattan";

        private DistanceMetrics() {}
    }
}
