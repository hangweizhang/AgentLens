package io.agentlens.domain.trace;

/**
 * Span 执行状态。
 */
public enum SpanStatus {

    /** 正在执行 */
    RUNNING("running"),

    /** 成功结束 */
    OK("ok"),

    /** 以错误结束 */
    ERROR("error"),

    /** 已取消/中止 */
    CANCELLED("cancelled");

    private final String value;

    SpanStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SpanStatus fromValue(String value) {
        for (SpanStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ERROR;
    }
}
