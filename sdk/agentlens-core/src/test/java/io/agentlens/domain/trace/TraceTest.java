package io.agentlens.domain.trace;

import io.agentlens.domain.cost.TokenCost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MVP 测试方案 - 领域层 U-D-08：Trace.getChildSpans(parentId) 仅返回 parentSpanId=parentId 的 Span
 */
class TraceTest {

    @Test
    @DisplayName("U-D-08 getChildSpans 仅返回指定父 Span 的子 Span")
    void getChildSpans_returnsOnlyChildrenOfParent() {
        Trace trace = Trace.create("p1");
        Span root = trace.startSpan("root", SpanType.AGENT);
        Span child1 = trace.startSpan("child1", SpanType.LLM, root.getId());
        Span child2 = trace.startSpan("child2", SpanType.TOOL, root.getId());
        Span grandChild = trace.startSpan("grandChild", SpanType.LLM, child1.getId());

        trace.endSpan(root);
        trace.endSpan(child1);
        trace.endSpan(child2);
        trace.endSpan(grandChild);

        assertThat(trace.getChildSpans(root.getId())).hasSize(2)
            .extracting(Span::getName).containsExactlyInAnyOrder("child1", "child2");
        assertThat(trace.getChildSpans(child1.getId())).hasSize(1)
            .element(0).extracting(Span::getName).isEqualTo("grandChild");
        assertThat(trace.getChildSpans(child2.getId())).isEmpty();
    }

    @Test
    void getTotalCost_sumsSpanCosts() {
        Trace trace = Trace.create("p1");
        Span s1 = trace.startSpan("llm1", SpanType.LLM);
        s1.setCost(TokenCost.of(100, 50, new BigDecimal("0.001")));
        trace.endSpan(s1);
        Span s2 = trace.startSpan("llm2", SpanType.LLM);
        s2.setCost(TokenCost.of(200, 100, new BigDecimal("0.002")));
        trace.endSpan(s2);
        trace.complete();
        assertThat(trace.getTotalCost()).isEqualByComparingTo("0.003");
    }
}
