package io.agentlens.domain.cost;

import io.agentlens.domain.shared.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 某款 LLM 的定价模型值对象。
 * <p>
 * 价格单位为「每 1000 Token 的美元数」；也支持从「每 100 万 Token」换算。
 * 用于根据 input/output Token 数计算单次调用成本。
 * </p>
 */
public final class PricingModel extends ValueObject {

    private final String modelId;
    private final String provider;
    private final BigDecimal inputPricePerThousand;
    private final BigDecimal outputPricePerThousand;

    private PricingModel(String modelId, String provider, 
                         BigDecimal inputPricePerThousand, 
                         BigDecimal outputPricePerThousand) {
        this.modelId = Objects.requireNonNull(modelId, "modelId is required");
        this.provider = Objects.requireNonNull(provider, "provider is required");
        this.inputPricePerThousand = Objects.requireNonNull(inputPricePerThousand, "inputPrice is required");
        this.outputPricePerThousand = Objects.requireNonNull(outputPricePerThousand, "outputPrice is required");
    }

    public static PricingModel of(String modelId, String provider, 
                                   BigDecimal inputPricePerThousand, 
                                   BigDecimal outputPricePerThousand) {
        return new PricingModel(modelId, provider, inputPricePerThousand, outputPricePerThousand);
    }

    public static PricingModel fromPerMillionPricing(String modelId, String provider,
                                                      BigDecimal inputPricePerMillion,
                                                      BigDecimal outputPricePerMillion) {
        return new PricingModel(
            modelId, 
            provider,
            inputPricePerMillion.divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP),
            outputPricePerMillion.divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP)
        );
    }

    public TokenCost calculateCost(int inputTokens, int outputTokens) {
        BigDecimal inputCost = inputPricePerThousand
            .multiply(BigDecimal.valueOf(inputTokens))
            .divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP);

        BigDecimal outputCost = outputPricePerThousand
            .multiply(BigDecimal.valueOf(outputTokens))
            .divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP);

        BigDecimal totalCost = inputCost.add(outputCost);

        return TokenCost.builder()
            .inputTokens(inputTokens)
            .outputTokens(outputTokens)
            .costUsd(totalCost)
            .model(modelId)
            .provider(provider)
            .build();
    }

    public String getModelId() {
        return modelId;
    }

    public String getProvider() {
        return provider;
    }

    public BigDecimal getInputPricePerThousand() {
        return inputPricePerThousand;
    }

    public BigDecimal getOutputPricePerThousand() {
        return outputPricePerThousand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PricingModel that = (PricingModel) o;
        return Objects.equals(modelId, that.modelId) &&
               Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, provider);
    }

    @Override
    public String toString() {
        return String.format("PricingModel{model=%s, provider=%s, input=$%.4f/1K, output=$%.4f/1K}",
            modelId, provider, inputPricePerThousand, outputPricePerThousand);
    }
}
