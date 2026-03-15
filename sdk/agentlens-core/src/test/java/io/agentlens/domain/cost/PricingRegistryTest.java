package io.agentlens.domain.cost;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MVP 测试方案 - 领域层 U-D-05、U-D-06：PricingRegistry 已知模型与未知模型
 */
class PricingRegistryTest {

    private final PricingRegistry registry = PricingRegistry.getInstance();

    @Test
    @DisplayName("U-D-05 gpt-4o openai 返回非零成本且 input+output 对应")
    void calculateCost_knownModel_returnsNonZeroCost() {
        TokenCost cost = registry.calculateCost("gpt-4o", "openai", 1000, 500);
        assertThat(cost.getInputTokens()).isEqualTo(1000);
        assertThat(cost.getOutputTokens()).isEqualTo(500);
        assertThat(cost.getTotalTokens()).isEqualTo(1500);
        assertThat(cost.getCostUsd()).isGreaterThan(java.math.BigDecimal.ZERO);
        assertThat(cost.getModel()).isEqualTo("gpt-4o");
        assertThat(cost.getProvider()).isEqualTo("openai");
    }

    @Test
    @DisplayName("U-D-06 未知模型返回 TokenCost 且 cost=0 不抛异常")
    void calculateCost_unknownModel_returnsZeroCostNoException() {
        TokenCost cost = registry.calculateCost("unknown-model", "x", 1, 1);
        assertThat(cost).isNotNull();
        assertThat(cost.getInputTokens()).isEqualTo(1);
        assertThat(cost.getOutputTokens()).isEqualTo(1);
        assertThat(cost.getCostUsd()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
    }
}
