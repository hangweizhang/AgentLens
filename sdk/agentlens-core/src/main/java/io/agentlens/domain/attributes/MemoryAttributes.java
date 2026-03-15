package io.agentlens.domain.attributes;

/**
 * Semantic attributes for memory operations (agent memory systems).
 */
public final class MemoryAttributes {

    private MemoryAttributes() {}

    // Memory identification
    public static final String MEMORY_TYPE = "memory.type";
    public static final String MEMORY_NAME = "memory.name";

    // Operation attributes
    public static final String MEMORY_OPERATION = "memory.operation";
    public static final String MEMORY_KEY = "memory.key";
    public static final String MEMORY_USER_ID = "memory.user_id";
    public static final String MEMORY_SESSION_ID = "memory.session_id";

    // Content attributes
    public static final String MEMORY_CONTENT = "memory.content";
    public static final String MEMORY_METADATA = "memory.metadata";

    // Query results
    public static final String MEMORY_RESULTS_COUNT = "memory.results_count";

    // Memory types
    public static final class Types {
        public static final String SHORT_TERM = "short_term";
        public static final String LONG_TERM = "long_term";
        public static final String EPISODIC = "episodic";
        public static final String SEMANTIC = "semantic";
        public static final String PROCEDURAL = "procedural";

        private Types() {}
    }

    // Operation types
    public static final class Operations {
        public static final String READ = "read";
        public static final String WRITE = "write";
        public static final String DELETE = "delete";
        public static final String SEARCH = "search";
        public static final String CLEAR = "clear";

        private Operations() {}
    }
}
