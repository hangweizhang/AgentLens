package io.agentlens.domain.trace;

import io.agentlens.domain.cost.TokenCost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MVP 测试方案 - 领域层 U-D-07：Span 未 end 时 getDurationMs() 为当前耗时
 */
class SpanTest {

    @Test
    @DisplayName("U-D-07 Span 未 end 时 getDurationMs() >= 0")
    void getDurationMs_whenNotEnded_returnsCurrentElapsed() throws InterruptedException {
        Trace trace = Trace.create("p1");
        Span span = trace.startSpan("test", SpanType.LLM);
        Thread.sleep(10);
        long ms = span.getDurationMs();
        assertThat(ms).isGreaterThanOrEqualTo(10);
        assertThat(span.isRunning()).isTrue();
        assertThat(span.isCompleted()).isFalse();
    }

    @Test
    void end_setsStatusAndEndTime() {
        Trace trace = Trace.create("p1");
        Span span = trace.startSpan("test", SpanType.LLM);
        span.end();
        assertThat(span.getStatus()).isEqualTo(SpanStatus.OK);
        assertThat(span.getEndTime()).isNotNull();
        assertThat(span.isCompleted()).isTrue();
    }

    @Test
    void setCost_getCostReturnsValue() {
        Trace trace = Trace.create("p1");
        Span span = trace.startSpan("llm", SpanType.LLM);
        TokenCost cost = TokenCost.of(10, 20, new BigDecimal("0.001"));
        span.setCost(cost);
        assertThat(span.getCost()).isSameAs(cost);
    }
}
