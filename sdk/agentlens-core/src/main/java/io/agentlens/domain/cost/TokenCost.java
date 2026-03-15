package io.agentlens.domain.cost;

import io.agentlens.domain.shared.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 基于 Token 的调用成本值对象。
 * <p>
 * 不可变，自校验（Token 数与非负成本）。用于记录单次 LLM/Embedding 调用的输入/输出 Token 及折算的美元成本。
 * </p>
 */
public final class TokenCost extends ValueObject {

    private final int inputTokens;
    private final int outputTokens;
    private final BigDecimal costUsd;
    private final String model;
    private final String provider;

    private TokenCost(int inputTokens, int outputTokens, BigDecimal costUsd, String model, String provider) {
        if (inputTokens < 0) {
            throw new IllegalArgumentException("Input tokens cannot be negative");
        }
        if (outputTokens < 0) {
            throw new IllegalArgumentException("Output tokens cannot be negative");
        }
        if (costUsd != null && costUsd.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost cannot be negative");
        }

        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd = costUsd != null ? costUsd.setScale(6, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.model = model;
        this.provider = provider;
    }

    public static TokenCost of(int inputTokens, int outputTokens, BigDecimal costUsd) {
        return new TokenCost(inputTokens, outputTokens, costUsd, null, null);
    }

    public static TokenCost of(int inputTokens, int outputTokens, BigDecimal costUsd, String model, String provider) {
        return new TokenCost(inputTokens, outputTokens, costUsd, model, provider);
    }

    public static TokenCost zero() {
        return new TokenCost(0, 0, BigDecimal.ZERO, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getInputTokens() {
        return inputTokens;
    }

    public int getOutputTokens() {
        return outputTokens;
    }

    public int getTotalTokens() {
        return inputTokens + outputTokens;
    }

    public BigDecimal getCostUsd() {
        return costUsd;
    }

    public String getModel() {
        return model;
    }

    public String getProvider() {
        return provider;
    }

    public TokenCost add(TokenCost other) {
        return new TokenCost(
            this.inputTokens + other.inputTokens,
            this.outputTokens + other.outputTokens,
            this.costUsd.add(other.costUsd),
            this.model,
            this.provider
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenCost tokenCost = (TokenCost) o;
        return inputTokens == tokenCost.inputTokens &&
               outputTokens == tokenCost.outputTokens &&
               Objects.equals(costUsd, tokenCost.costUsd) &&
               Objects.equals(model, tokenCost.model) &&
               Objects.equals(provider, tokenCost.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputTokens, outputTokens, costUsd, model, provider);
    }

    @Override
    public String toString() {
        return String.format("TokenCost{in=%d, out=%d, total=%d, cost=$%.6f, model=%s}",
            inputTokens, outputTokens, getTotalTokens(), costUsd, model);
    }

    public static class Builder {
        private int inputTokens;
        private int outputTokens;
        private BigDecimal costUsd;
        private String model;
        private String provider;

        public Builder inputTokens(int inputTokens) {
            this.inputTokens = inputTokens;
            return this;
        }

        public Builder outputTokens(int outputTokens) {
            this.outputTokens = outputTokens;
            return this;
        }

        public Builder costUsd(BigDecimal costUsd) {
            this.costUsd = costUsd;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public TokenCost build() {
            return new TokenCost(inputTokens, outputTokens, costUsd, model, provider);
        }
    }
}
