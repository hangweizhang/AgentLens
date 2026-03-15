package io.agentlens.domain.cost;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MVP 测试方案 - 领域层 U-D-03：TokenCost 总 Token 与成本
 */
class TokenCostTest {

    @Test
    @DisplayName("U-D-03 of(100, 200, 0.01) 总 Token=300，成本 0.01")
    void of_totalTokensAndCost() {
        TokenCost cost = TokenCost.of(100, 200, new BigDecimal("0.01"));
        assertThat(cost.getInputTokens()).isEqualTo(100);
        assertThat(cost.getOutputTokens()).isEqualTo(200);
        assertThat(cost.getTotalTokens()).isEqualTo(300);
        assertThat(cost.getCostUsd()).isEqualByComparingTo("0.01");
    }

    @Test
    void zero_hasZeros() {
        TokenCost cost = TokenCost.zero();
        assertThat(cost.getTotalTokens()).isZero();
        assertThat(cost.getCostUsd()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void negativeInputTokens_throws() {
        assertThatThrownBy(() -> TokenCost.of(-1, 0, BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Input tokens");
    }

    @Test
    void negativeCost_throws() {
        assertThatThrownBy(() -> TokenCost.of(1, 1, new BigDecimal("-0.01")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cost");
    }
}
