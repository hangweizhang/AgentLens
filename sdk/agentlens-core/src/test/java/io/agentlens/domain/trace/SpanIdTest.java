package io.agentlens.domain.trace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MVP 测试方案 - 领域层 U-D-02：SpanId.generate() 返回 16 位非空字符串
 */
class SpanIdTest {

    @Test
    @DisplayName("U-D-02 generate 返回 16 位非空字符串")
    void generate_returns16CharsNonEmpty() {
        SpanId id = SpanId.generate();
        assertThat(id.getValue()).isNotBlank().hasSize(16);
    }

    @Test
    void of_null_throws() {
        assertThatThrownBy(() -> SpanId.of(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void of_blank_throws() {
        assertThatThrownBy(() -> SpanId.of(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_valid_succeeds() {
        SpanId id = SpanId.of("a1b2c3d4e5f67890");
        assertThat(id.getValue()).hasSize(16);
    }
}
