package io.agentlens.domain.attributes;

/**
 * Semantic attributes for RAG/retriever operations.
 */
public final class RetrieverAttributes {

    private RetrieverAttributes() {}

    // Retriever identification
    public static final String RETRIEVER_NAME = "retriever.name";
    public static final String RETRIEVER_TYPE = "retriever.type";

    // Query attributes
    public static final String RETRIEVER_QUERY = "retriever.query";
    public static final String RETRIEVER_TOP_K = "retriever.top_k";

    // Results attributes
    public static final String RETRIEVER_RESULTS_COUNT = "retriever.results_count";
    public static final String RETRIEVER_DOCUMENTS = "retriever.documents";
    public static final String RETRIEVER_SCORES = "retriever.scores";

    // Filter attributes
    public static final String RETRIEVER_FILTER = "retriever.filter";
    public static final String RETRIEVER_MIN_SCORE = "retriever.min_score";

    // Retriever types
    public static final class Types {
        public static final String VECTOR = "vector";
        public static final String KEYWORD = "keyword";
        public static final String HYBRID = "hybrid";
        public static final String RERANKER = "reranker";

        private Types() {}
    }
}
