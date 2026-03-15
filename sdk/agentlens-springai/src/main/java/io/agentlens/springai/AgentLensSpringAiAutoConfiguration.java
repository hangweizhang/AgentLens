package io.agentlens.springai;

import io.agentlens.springai.aspect.ChatModelTracingAspect;
import io.agentlens.springai.aspect.EmbeddingModelTracingAspect;
import io.agentlens.springai.aspect.VectorStoreTracingAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AgentLens 与 Spring AI 的自动配置。
 * <p>
 * 在 classpath 中存在 Spring AI 且 agentlens.enabled 不为 false 时，
 * 注册 AOP 切面，对 ChatModel、EmbeddingModel、VectorStore 的调用自动打点。
 * </p>
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(AgentLensSpringAiProperties.class)
@ConditionalOnProperty(prefix = "agentlens", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AgentLensSpringAiAutoConfiguration {

    @Bean
    @ConditionalOnClass(name = "org.springframework.ai.chat.ChatModel")
    @ConditionalOnProperty(prefix = "agentlens.springai", name = "trace-chat-model", havingValue = "true", matchIfMissing = true)
    public ChatModelTracingAspect chatModelTracingAspect(AgentLensSpringAiProperties properties) {
        return new ChatModelTracingAspect(properties);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.ai.embedding.EmbeddingModel")
    @ConditionalOnProperty(prefix = "agentlens.springai", name = "trace-embedding-model", havingValue = "true", matchIfMissing = true)
    public EmbeddingModelTracingAspect embeddingModelTracingAspect(AgentLensSpringAiProperties properties) {
        return new EmbeddingModelTracingAspect(properties);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.ai.vectorstore.VectorStore")
    @ConditionalOnProperty(prefix = "agentlens.springai", name = "trace-vector-store", havingValue = "true", matchIfMissing = true)
    public VectorStoreTracingAspect vectorStoreTracingAspect(AgentLensSpringAiProperties properties) {
        return new VectorStoreTracingAspect(properties);
    }
}
