package io.agentlens.domain.attributes;

/**
 * 工具/函数调用类 Span 的语义属性常量。
 * <p>
 * 用于 Function Calling、MCP 等工具调用的 name、input、output、status、error 等。
 * </p>
 */
public final class ToolAttributes {

    private ToolAttributes() {}

    // Tool identification
    public static final String TOOL_NAME = "tool.name";
    public static final String TOOL_DESCRIPTION = "tool.description";
    public static final String TOOL_TYPE = "tool.type";

    // Tool execution
    public static final String TOOL_INPUT = "tool.input";
    public static final String TOOL_OUTPUT = "tool.output";
    public static final String TOOL_STATUS = "tool.status";
    public static final String TOOL_ERROR_MESSAGE = "tool.error.message";
    public static final String TOOL_ERROR_TYPE = "tool.error.type";

    // Tool metadata
    public static final String TOOL_PARAMETERS_SCHEMA = "tool.parameters.schema";
    public static final String TOOL_RETURN_TYPE = "tool.return_type";
    public static final String TOOL_IS_ASYNC = "tool.is_async";

    // MCP specific
    public static final String MCP_SERVER_NAME = "mcp.server.name";
    public static final String MCP_TOOL_URI = "mcp.tool.uri";

    // Status values
    public static final class Statuses {
        public static final String SUCCESS = "success";
        public static final String ERROR = "error";
        public static final String TIMEOUT = "timeout";
        public static final String CANCELLED = "cancelled";

        private Statuses() {}
    }

    // Tool types
    public static final class Types {
        public static final String FUNCTION = "function";
        public static final String MCP = "mcp";
        public static final String HTTP = "http";
        public static final String CODE_INTERPRETER = "code_interpreter";
        public static final String RETRIEVAL = "retrieval";
        public static final String CUSTOM = "custom";

        private Types() {}
    }
}
