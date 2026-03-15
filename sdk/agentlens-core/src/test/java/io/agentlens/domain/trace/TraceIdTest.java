package io.agentlens.domain.trace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MVP 测试方案 - 领域层 U-D-01：TraceId
 */
class TraceIdTest {

    @Nested
    @DisplayName("U-D-01 TraceId.of 空或 null 抛异常")
    class OfValidation {

        @Test
        void of_null_throws() {
            assertThatThrownBy(() -> TraceId.of(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void of_blank_throws() {
            assertThatThrownBy(() -> TraceId.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
        }

        @Test
        void of_whitespaceOnly_throws() {
            assertThatThrownBy(() -> TraceId.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void of_validValue_succeeds() {
            TraceId id = TraceId.of("abc123");
            assertThat(id.getValue()).isEqualTo("abc123");
        }
    }

    @Test
    @DisplayName("generate 返回非空字符串")
    void generate_returnsNonEmpty() {
        TraceId id = TraceId.generate();
        assertThat(id.getValue()).isNotBlank().hasSize(32);
    }
}
