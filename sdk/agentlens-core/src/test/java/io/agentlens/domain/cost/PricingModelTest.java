package io.agentlens.domain.cost;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MVP 测试方案 - 领域层 U-D-04：PricingModel.calculateCost 按 input/output 单价计算
 */
class PricingModelTest {

    @Test
    @DisplayName("U-D-04 calculateCost(1000, 500) 与定价一致")
    void calculateCost_matchesPricing() {
        // 每 1K input $0.005, 每 1K output $0.015
        PricingModel model = PricingModel.of("test", "test-provider",
            new BigDecimal("0.005"),
            new BigDecimal("0.015"));

        TokenCost cost = model.calculateCost(1000, 500);

        assertThat(cost.getInputTokens()).isEqualTo(1000);
        assertThat(cost.getOutputTokens()).isEqualTo(500);
        // 1000/1000 * 0.005 + 500/1000 * 0.015 = 0.005 + 0.0075 = 0.0125
        assertThat(cost.getCostUsd()).isEqualByComparingTo("0.0125");
    }

    @Test
    void fromPerMillionPricing_calculatesCorrectly() {
        PricingModel model = PricingModel.fromPerMillionPricing("m", "p",
            new BigDecimal("5.00"),   // $5 per 1M input
            new BigDecimal("15.00")); // $15 per 1M output
        TokenCost cost = model.calculateCost(1_000_000, 1_000_000);
        assertThat(cost.getCostUsd()).isEqualByComparingTo("20.00");
    }
}
