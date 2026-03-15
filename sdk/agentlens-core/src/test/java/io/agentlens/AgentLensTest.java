package io.agentlens;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MVP 测试方案 - 应用层 U-A-01：未 init 时 startTrace() 抛异常
 */
class AgentLensTest {

    @BeforeEach
    void ensureNotInitialized() {
        if (AgentLens.isInitialized()) {
            AgentLens.shutdown();
        }
    }

    @Test
    @DisplayName("U-A-01 未 init 时 startTrace() 抛 IllegalStateException")
    void startTrace_withoutInit_throws() {
        assertThatThrownBy(AgentLens::startTrace)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not initialized");
    }

    @Test
    @DisplayName("未 init 时 getCurrentTrace 抛异常")
    void getCurrentTrace_withoutInit_throws() {
        assertThatThrownBy(AgentLens::getCurrentTrace)
            .isInstanceOf(IllegalStateException.class);
    }
}
