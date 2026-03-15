package io.agentlens.domain.cost;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型定价注册表。
 * <p>
 * 内置常见模型（OpenAI、Claude、Qwen、Gemini、DeepSeek 等）的定价，并支持注册自定义定价。
 * 单例，线程安全。
 * </p>
 */
public final class PricingRegistry {

    private static final PricingRegistry INSTANCE = new PricingRegistry();

    private final Map<String, PricingModel> pricingModels = new ConcurrentHashMap<>();

    private PricingRegistry() {
        registerBuiltInPricing();
    }

    public static PricingRegistry getInstance() {
        return INSTANCE;
    }

    private void registerBuiltInPricing() {
        // OpenAI Models (prices per 1M tokens, converted to per 1K)
        register(PricingModel.fromPerMillionPricing("gpt-4o", "openai",
            new BigDecimal("5.00"), new BigDecimal("15.00")));
        register(PricingModel.fromPerMillionPricing("gpt-4o-mini", "openai",
            new BigDecimal("0.15"), new BigDecimal("0.60")));
        register(PricingModel.fromPerMillionPricing("gpt-4-turbo", "openai",
            new BigDecimal("10.00"), new BigDecimal("30.00")));
        register(PricingModel.fromPerMillionPricing("gpt-3.5-turbo", "openai",
            new BigDecimal("0.50"), new BigDecimal("1.50")));

        // Anthropic Claude Models
        register(PricingModel.fromPerMillionPricing("claude-3-5-sonnet-20241022", "anthropic",
            new BigDecimal("3.00"), new BigDecimal("15.00")));
        register(PricingModel.fromPerMillionPricing("claude-3-opus-20240229", "anthropic",
            new BigDecimal("15.00"), new BigDecimal("75.00")));
        register(PricingModel.fromPerMillionPricing("claude-3-haiku-20240307", "anthropic",
            new BigDecimal("0.25"), new BigDecimal("1.25")));

        // Alibaba Qwen Models (DashScope)
        register(PricingModel.fromPerMillionPricing("qwen-max", "dashscope",
            new BigDecimal("1.50"), new BigDecimal("6.00")));
        register(PricingModel.fromPerMillionPricing("qwen-plus", "dashscope",
            new BigDecimal("0.50"), new BigDecimal("2.00")));
        register(PricingModel.fromPerMillionPricing("qwen-turbo", "dashscope",
            new BigDecimal("0.30"), new BigDecimal("0.60")));

        // Google Gemini Models
        register(PricingModel.fromPerMillionPricing("gemini-1.5-pro", "google",
            new BigDecimal("3.50"), new BigDecimal("10.50")));
        register(PricingModel.fromPerMillionPricing("gemini-1.5-flash", "google",
            new BigDecimal("0.075"), new BigDecimal("0.30")));

        // DeepSeek Models
        register(PricingModel.fromPerMillionPricing("deepseek-chat", "deepseek",
            new BigDecimal("0.14"), new BigDecimal("0.28")));
        register(PricingModel.fromPerMillionPricing("deepseek-coder", "deepseek",
            new BigDecimal("0.14"), new BigDecimal("0.28")));

        // Embedding Models
        register(PricingModel.fromPerMillionPricing("text-embedding-3-small", "openai",
            new BigDecimal("0.02"), BigDecimal.ZERO));
        register(PricingModel.fromPerMillionPricing("text-embedding-3-large", "openai",
            new BigDecimal("0.13"), BigDecimal.ZERO));
        register(PricingModel.fromPerMillionPricing("text-embedding-v3", "dashscope",
            new BigDecimal("0.07"), BigDecimal.ZERO));
    }

    public void register(PricingModel pricingModel) {
        String key = buildKey(pricingModel.getModelId(), pricingModel.getProvider());
        pricingModels.put(key, pricingModel);
    }

    public Optional<PricingModel> findPricing(String modelId, String provider) {
        String key = buildKey(modelId, provider);
        PricingModel pricing = pricingModels.get(key);

        if (pricing == null) {
            pricing = pricingModels.get(buildKey(modelId, "*"));
        }

        if (pricing == null) {
            pricing = findByModelIdOnly(modelId);
        }

        return Optional.ofNullable(pricing);
    }

    private PricingModel findByModelIdOnly(String modelId) {
        return pricingModels.values().stream()
            .filter(p -> p.getModelId().equalsIgnoreCase(modelId))
            .findFirst()
            .orElse(null);
    }

    public TokenCost calculateCost(String modelId, String provider, int inputTokens, int outputTokens) {
        return findPricing(modelId, provider)
            .map(p -> p.calculateCost(inputTokens, outputTokens))
            .orElse(TokenCost.builder()
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .costUsd(BigDecimal.ZERO)
                .model(modelId)
                .provider(provider)
                .build());
    }

    private String buildKey(String modelId, String provider) {
        return (provider + ":" + modelId).toLowerCase();
    }

    public void clear() {
        pricingModels.clear();
    }

    public void reset() {
        clear();
        registerBuiltInPricing();
    }
}
