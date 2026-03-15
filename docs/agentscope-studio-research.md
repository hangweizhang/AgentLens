# AgentScope-Studio 调研报告

> 生成时间: 2026-03-15  
> 目的: 分析 AgentScope-Studio 现有功能、技术架构，寻找「Agent 可观测性」方向的差异化机会

---

## 1. AgentScope-Studio 概览

| 项目 | 信息 |
|------|------|
| 仓库 | [agentscope-ai/agentscope-studio](https://github.com/agentscope-ai/agentscope-studio) |
| Stars | 452+ |
| 语言 | TypeScript 95%（React + Vite + Node.js） |
| 协议 | Apache 2.0 |
| 最新版本 | v1.0.9 |
| 安装 | `npm install -g @agentscope/studio` |
| 定位 | AgentScope 官方开发可视化工具 |

---

## 2. 核心功能

### 2.1 已实现功能

| 功能模块 | 说明 |
|---------|------|
| **项目管理** | 按 Project → Runs 组织；支持多项目、多次运行管理 |
| **运行时可视化** | 聊天式界面，实时与 Agent 交互，观察执行过程 |
| **OpenTelemetry Tracing** | 可视化 LLM 调用、Token 用量、Agent 调用链 |
| **Agent 评估** | 统计视角分析 Agent 系统 |
| **内置 Copilot（Friday）** | 开发助手，快速二次开发、集成高级功能 |
| **语音支持** | 客户端语音识别 + 语音回复（v1.0.9 新增） |

### 2.2 Tracing 实现细节

AgentScope 的 Tracing 基于 **OpenTelemetry**，支持：

| 能力 | 说明 |
|------|------|
| 内置装饰器 | `@trace_llm`（LLM）、`@trace_reply`（Agent）、`@trace_format`（Formatter）、`@trace`（通用函数） |
| 追踪类型 | LLM、TOOL、AGENT_STEP、SEARCH、IMAGE_GENERATION、RAG、INTENTION、PLUGIN_CENTER |
| 第三方对接 | 支持 Langfuse、Arize-Phoenix、阿里云 CloudMonitor（OTLP 协议） |
| Studio 对接 | `agentscope.init(studio_url="http://xxx:port")` 一行代码接入 |

### 2.3 Java 版本（AgentScope-Java）支持情况

| 功能 | 状态 |
|------|------|
| Studio 集成扩展 | `agentscope-extensions-studio` 模块 |
| OpenTelemetry 依赖 | `opentelemetry-api` + `opentelemetry-exporter-otlp` + `opentelemetry-reactor-3.1` |
| OTLP Headers 支持 | **缺失**（Issue #351 提出需求，尚未实现） |
| 核心类 | `TelemetryTracer.java`、`StudioManager.java`、`StudioConfig.java` |

---

## 3. 技术架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      AgentScope Studio                          │
│                   (React + Vite + Node.js)                      │
├─────────────────────────────────────────────────────────────────┤
│  Project Manager │ Runtime Chat │ Trace Viewer │ Evaluator      │
├─────────────────────────────────────────────────────────────────┤
│                      WebSocket / HTTP API                       │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   AgentScope SDK (Python/Java)                  │
├─────────────────────────────────────────────────────────────────┤
│  @trace_llm  │  @trace_reply  │  @trace_format  │  @trace       │
├─────────────────────────────────────────────────────────────────┤
│              OpenTelemetry Exporter (OTLP)                      │
└───────────────────────────┬─────────────────────────────────────┘
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
        AgentScope      Langfuse     阿里云 CloudMonitor
         Studio
```

---

## 4. 当前局限性与差异化机会

### 4.1 AgentScope-Studio 的局限

| 局限 | 说明 | 机会 |
|------|------|------|
| **只支持 AgentScope 生态** | 深度绑定 AgentScope SDK，其他框架（LangChain4j、Spring AI）无法直接用 | 做「通用 Java Agent 可观测性 SDK」 |
| **Java OTLP Headers 缺失** | Issue #351，无法对接 Langfuse 等需要认证的平台 | 贡献 PR 或做增强版 |
| **无成本归因** | 只有 Token 用量，没有「每次调用花了多少钱」 | 结合 API 成本管理，做「成本追踪」 |
| **无重放/Debug** | 只能看 Trace，不能像 AgentTrace 那样「重放失败 Agent」 | 做「确定性重放」功能 |
| **无告警** | 只有可视化，没有「异常/成本超标」告警 | 做「告警规则 + 通知」 |
| **本地部署** | 官方 Studio 是本地工具，没有云托管版 | 做「云托管 SaaS」 |

### 4.2 竞品对比（与通用可观测性工具）

| 工具 | 生态绑定 | 成本追踪 | 重放/Debug | 告警 | 云托管 | Java 支持 |
|------|---------|---------|-----------|------|--------|----------|
| **AgentScope-Studio** | AgentScope | ❌ | ❌ | ❌ | ❌ | 部分 |
| **AgentTrace** | 通用 | ❌ | ✅ 100%重放 | ❌ | ✅ SaaS | Python/TS |
| **Laminar** | 通用 | ✅ | ✅ 本地调试 | ❌ | ✅ | Python/TS |
| **Spanora** | 通用 | ✅ 100+模型 | ❌ | ✅ | ✅ | Python/TS |
| **Traceloop** | 通用 | ❌ | ❌ | ❌ | ❌ 本地 | Python |

---

## 5. 你的差异化方向

基于调研，建议你做「**Java 生态的通用 Agent 可观测性 SDK + Dashboard**」，与 AgentScope-Studio 形成互补而非竞争。

### 5.1 定位

**「AgentLens」——Java Agent 可观测性 SDK**

> 一行代码接入，看清 Agent 每一步调用、耗时、成本、错误；支持 AgentScope-Java、Spring AI、LangChain4j；开源 SDK + 可选云托管。

### 5.2 核心差异点

| 差异点 | 说明 |
|--------|------|
| **框架通用** | 同时支持 AgentScope-Java、Spring AI、LangChain4j（三大 Java Agent 框架） |
| **成本归因** | 自动计算每次 LLM 调用的 Token 成本（内置 OpenAI/Claude/Qwen 定价） |
| **重放/Debug** | 记录完整上下文，支持「重放失败调用」进行本地调试 |
| **告警** | 异常率超标、成本超预算时发送通知（邮件/Slack/钉钉） |
| **云托管可选** | 开源自托管 + 云 SaaS 两种模式 |

### 5.3 与 AgentScope-Studio 的关系

| 方式 | 说明 |
|------|------|
| **互补** | AgentScope-Studio 专注「AgentScope 生态」，你专注「通用 Java 生态」 |
| **可对接** | 你的 SDK 可以同时输出到 AgentScope-Studio（OTLP）和你自己的 Dashboard |
| **贡献** | 可以给 AgentScope-Java 贡献 OTLP Headers 功能（Issue #351），积累社区影响力 |

---

## 6. 技术实现方案

### 6.1 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                      AgentLens Dashboard                        │
│                   (React + Recharts + Tailwind)                 │
├─────────────────────────────────────────────────────────────────┤
│  Trace Viewer │ Cost Analytics │ Error Dashboard │ Alerts       │
├─────────────────────────────────────────────────────────────────┤
│                      REST API (Spring Boot)                     │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      AgentLens Collector                        │
│              (Spring Boot + OTLP Receiver)                      │
├─────────────────────────────────────────────────────────────────┤
│              PostgreSQL / ClickHouse (存储)                     │
└───────────────────────────┬─────────────────────────────────────┘
                            │
              ┌─────────────┼─────────────┬─────────────┐
              ▼             ▼             ▼             ▼
        AgentLens      AgentLens      AgentLens      AgentLens
        SDK for        SDK for        SDK for        SDK for
        AgentScope     Spring AI      LangChain4j    (通用)
```

### 6.2 核心模块

| 模块 | 技术 | 功能 |
|------|------|------|
| **agentlens-sdk-core** | Java | 通用 Trace API、成本计算、上下文捕获 |
| **agentlens-sdk-agentscope** | Java | AgentScope-Java 拦截器 |
| **agentlens-sdk-springai** | Java | Spring AI 拦截器 |
| **agentlens-sdk-langchain4j** | Java | LangChain4j 拦截器 |
| **agentlens-collector** | Spring Boot | OTLP Receiver、存储、API |
| **agentlens-dashboard** | React | 可视化 UI |

### 6.3 SDK 接入示例

```java
// AgentScope-Java 接入
AgentScopeConfig config = AgentScopeConfig.builder()
    .modelConfigName("qwen")
    .agentLensUrl("http://localhost:8080")  // AgentLens Collector
    .build();
AgentScope.init(config);

// Spring AI 接入
@Configuration
public class AgentLensConfig {
    @Bean
    public AgentLensTracer agentLensTracer() {
        return AgentLensTracer.builder()
            .collectorUrl("http://localhost:8080")
            .projectName("my-spring-ai-app")
            .enableCostTracking(true)
            .build();
    }
}

// LangChain4j 接入
ChatModel model = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .build();
model = AgentLens.wrap(model, "my-langchain4j-app");
```

### 6.4 Trace 数据结构

```json
{
  "traceId": "abc123",
  "spanId": "span456",
  "parentSpanId": "span123",
  "name": "ChatModel.call",
  "type": "LLM",
  "startTime": "2026-03-15T10:00:00Z",
  "endTime": "2026-03-15T10:00:02Z",
  "durationMs": 2000,
  "status": "OK",
  "attributes": {
    "model": "qwen-max",
    "provider": "dashscope",
    "inputTokens": 150,
    "outputTokens": 300,
    "totalTokens": 450,
    "costUsd": 0.0045,
    "temperature": 0.7
  },
  "input": {
    "messages": [...]
  },
  "output": {
    "content": "..."
  },
  "error": null
}
```

### 6.5 成本计算逻辑

```java
public class CostCalculator {
    // 内置定价表（可配置）
    private static final Map<String, TokenPricing> PRICING = Map.of(
        "gpt-4o", new TokenPricing(0.005, 0.015),      // $/1K tokens
        "gpt-4o-mini", new TokenPricing(0.00015, 0.0006),
        "claude-3-5-sonnet", new TokenPricing(0.003, 0.015),
        "qwen-max", new TokenPricing(0.0015, 0.006)
        // ...
    );
    
    public double calculateCost(String model, int inputTokens, int outputTokens) {
        TokenPricing pricing = PRICING.get(model);
        if (pricing == null) return 0.0;
        return (inputTokens / 1000.0 * pricing.inputPrice()) +
               (outputTokens / 1000.0 * pricing.outputPrice());
    }
}
```

---

## 7. 12 周执行计划

| 周 | 任务 | 产出 |
|----|------|------|
| **1** | 深入研究 AgentScope-Java 源码（TelemetryTracer 等） | 设计文档 |
| **2** | 设计 SDK API + Trace 数据结构 | API 文档 |
| **3–4** | 实现 `agentlens-sdk-core`：Trace API、成本计算 | SDK v0.1 |
| **5** | 实现 `agentlens-sdk-agentscope`：AgentScope-Java 拦截器 | 可用 SDK |
| **6** | 实现 `agentlens-collector`：OTLP Receiver + PostgreSQL 存储 | Collector v0.1 |
| **7–8** | 实现 `agentlens-dashboard`：Trace Viewer、成本统计 | Dashboard v0.1 |
| **9** | 添加 Spring AI 支持 | SDK 扩展 |
| **10** | 开源发布 + 文档 | GitHub |
| **11** | 云托管版（Railway/Fly.io） | SaaS 上线 |
| **12** | 推广 + 收费 | 5+ 早期用户 |

---

## 8. 变现路径

| 档位 | 价格 | 功能 |
|------|------|------|
| **开源自托管** | $0 | 全功能、无限 Trace、本地存储 |
| **云托管 - 个人** | $19/月 | 7 天数据、1 项目、基础告警 |
| **云托管 - 团队** | $49/月 | 30 天数据、5 项目、高级告警、多成员 |
| **企业私有部署** | $999+/年 | 私有部署、SLA、技术支持 |

---

## 9. 风险与缓解

| 风险 | 缓解 |
|------|------|
| AgentScope-Studio 后续加强 | 做「通用框架支持」差异化；与社区保持合作 |
| 框架 API 变更 | 抽象层隔离；关注 Release Notes |
| 用户不愿付费 | 开源先积累用户；云托管做「省心」溢价 |

---

## 10. 下一步建议

1. **先给 AgentScope-Java 贡献 OTLP Headers PR**（Issue #351）——积累社区影响力 + 深入理解源码。
2. **同步启动 SDK 设计**——先支持 AgentScope-Java，再扩展到 Spring AI。
3. **Landing Page + 邮箱收集**——验证需求。

---

## 附录：关键源码位置（待研究）

| 文件 | 仓库 | 作用 |
|------|------|------|
| `TelemetryTracer.java` | agentscope-java | OpenTelemetry Trace 发送 |
| `StudioManager.java` | agentscope-java | Studio 连接管理 |
| `StudioConfig.java` | agentscope-java | Studio 配置 |
| `agentscope-extensions-studio` | agentscope-java | Studio 集成扩展模块 |

需要我帮你：
- **深入分析 AgentScope-Java 源码**（TelemetryTracer 实现细节）
- **生成 SDK 骨架代码**
- **设计 Dashboard 原型**

随时说。

---

## 11. 全链路追踪设计（补充）

### 11.1 为什么需要全链路？

一个完整的 AI Agent 调用往往包含多个环节：

```
用户请求
    │
    ▼
┌─────────────────┐
│   Agent 入口    │  ← Span: agent.request
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌───────┐  ┌───────────┐
│ RAG   │  │ Tool Call │
│检索增强│  │ 工具调用   │
└───┬───┘  └─────┬─────┘
    │            │
    ▼            ▼
┌───────────┐  ┌───────────┐
│向量数据库  │  │ 外部API   │
│ Query     │  │ HTTP Call │
└───┬───────┘  └─────┬─────┘
    │                │
    └────────┬───────┘
             ▼
      ┌─────────────┐
      │   LLM 调用   │  ← Span: llm.chat
      │ (带上下文)   │
      └──────┬──────┘
             │
             ▼
      ┌─────────────┐
      │  返回结果    │
      └─────────────┘
```

**只追踪 LLM 是不够的**——你需要看到：
- 向量检索耗时多少？召回了哪些文档？
- 工具调用成功了吗？返回了什么？
- 整个链路的瓶颈在哪里？

### 11.2 支持的 Span 类型（全链路）

| Span 类型 | 说明 | 关键属性 |
|-----------|------|---------|
| **AGENT** | Agent 整体执行 | agent.name, agent.type, input, output |
| **LLM** | 大模型调用 | model, provider, input_tokens, output_tokens, cost, temperature |
| **EMBEDDING** | 向量嵌入 | model, provider, input_tokens, dimensions |
| **RETRIEVER** | 检索/RAG | query, top_k, results_count, latency |
| **VECTOR_DB** | 向量数据库操作 | db_type (qdrant/milvus/pgvector), operation (search/insert/delete), collection |
| **TOOL** | 工具/函数调用 | tool.name, tool.input, tool.output, tool.status |
| **HTTP** | 外部 HTTP 请求 | url, method, status_code, latency |
| **RERANKER** | 重排序 | model, input_count, output_count |
| **FORMATTER** | Prompt 格式化 | template, variables |
| **MEMORY** | 记忆读写 | operation (read/write), memory_type |

### 11.3 OpenTelemetry 语义规范对齐

遵循 OpenTelemetry **GenAI Semantic Conventions**（`gen_ai.*` 命名空间）：

```java
// LLM Span 属性（标准化）
public class GenAiAttributes {
    // 必选
    public static final String OPERATION_NAME = "gen_ai.operation.name";      // chat, text_completion
    public static final String PROVIDER_NAME = "gen_ai.provider.name";        // openai, anthropic, dashscope
    public static final String REQUEST_MODEL = "gen_ai.request.model";        // gpt-4o, qwen-max
    
    // Token 使用
    public static final String INPUT_TOKENS = "gen_ai.usage.input_tokens";
    public static final String OUTPUT_TOKENS = "gen_ai.usage.output_tokens";
    
    // 请求参数
    public static final String MAX_TOKENS = "gen_ai.request.max_tokens";
    public static final String TEMPERATURE = "gen_ai.request.temperature";
    public static final String TOP_P = "gen_ai.request.top_p";
    
    // 响应
    public static final String FINISH_REASON = "gen_ai.response.finish_reasons";
}
```

**自定义扩展**（向量数据库、工具调用等）：

```java
// 向量数据库 Span 属性
public class VectorDbAttributes {
    public static final String DB_SYSTEM = "db.system";                // qdrant, milvus, pgvector
    public static final String DB_OPERATION = "db.operation.name";     // search, insert, delete
    public static final String DB_COLLECTION = "db.collection.name";
    public static final String VECTOR_DIMENSION = "vector.dimension";
    public static final String SEARCH_TOP_K = "vector.search.top_k";
    public static final String SEARCH_RESULTS_COUNT = "vector.search.results_count";
    public static final String SEARCH_SCORE_THRESHOLD = "vector.search.score_threshold";
}

// 工具调用 Span 属性
public class ToolAttributes {
    public static final String TOOL_NAME = "tool.name";
    public static final String TOOL_DESCRIPTION = "tool.description";
    public static final String TOOL_INPUT = "tool.input";
    public static final String TOOL_OUTPUT = "tool.output";
    public static final String TOOL_STATUS = "tool.status";            // success, error, timeout
    public static final String TOOL_ERROR_MESSAGE = "tool.error.message";
}

// RAG/Retriever Span 属性
public class RetrieverAttributes {
    public static final String RETRIEVER_NAME = "retriever.name";
    public static final String RETRIEVER_QUERY = "retriever.query";
    public static final String RETRIEVER_TOP_K = "retriever.top_k";
    public static final String RETRIEVER_RESULTS_COUNT = "retriever.results_count";
    public static final String RETRIEVER_DOCUMENTS = "retriever.documents";  // JSON array
}
```

### 11.4 全链路 Trace 数据结构

```json
{
  "traceId": "abc123",
  "projectId": "my-rag-agent",
  "rootSpan": {
    "spanId": "span-001",
    "name": "RAGAgent.reply",
    "type": "AGENT",
    "startTime": "2026-03-15T10:00:00.000Z",
    "endTime": "2026-03-15T10:00:03.500Z",
    "durationMs": 3500,
    "status": "OK",
    "input": {"query": "什么是 AgentScope？"},
    "output": {"content": "AgentScope 是一个..."},
    "children": [
      {
        "spanId": "span-002",
        "name": "Embedding.embed",
        "type": "EMBEDDING",
        "durationMs": 150,
        "attributes": {
          "gen_ai.provider.name": "dashscope",
          "gen_ai.request.model": "text-embedding-v3",
          "gen_ai.usage.input_tokens": 12
        }
      },
      {
        "spanId": "span-003",
        "name": "Qdrant.search",
        "type": "VECTOR_DB",
        "durationMs": 45,
        "attributes": {
          "db.system": "qdrant",
          "db.operation.name": "search",
          "db.collection.name": "docs",
          "vector.search.top_k": 5,
          "vector.search.results_count": 5
        }
      },
      {
        "spanId": "span-004",
        "name": "ChatModel.call",
        "type": "LLM",
        "durationMs": 2800,
        "attributes": {
          "gen_ai.operation.name": "chat",
          "gen_ai.provider.name": "dashscope",
          "gen_ai.request.model": "qwen-max",
          "gen_ai.usage.input_tokens": 1500,
          "gen_ai.usage.output_tokens": 300,
          "gen_ai.request.temperature": 0.7,
          "cost.usd": 0.012
        }
      },
      {
        "spanId": "span-005",
        "name": "WebSearch.call",
        "type": "TOOL",
        "durationMs": 500,
        "attributes": {
          "tool.name": "web_search",
          "tool.input": {"query": "AgentScope latest version"},
          "tool.output": {"results": [...]},
          "tool.status": "success"
        }
      }
    ]
  },
  "summary": {
    "totalDurationMs": 3500,
    "totalTokens": 1812,
    "totalCostUsd": 0.013,
    "spanCounts": {
      "AGENT": 1,
      "LLM": 1,
      "EMBEDDING": 1,
      "VECTOR_DB": 1,
      "TOOL": 1
    }
  }
}
```

### 11.5 SDK 全链路拦截设计

```java
// ============ 核心 Tracer 接口 ============
public interface AgentLensTracer {
    
    // 开始一个 Span
    Span startSpan(String name, SpanType type, Map<String, Object> attributes);
    
    // 结束 Span
    void endSpan(Span span, SpanStatus status, Object output);
    
    // 记录异常
    void recordException(Span span, Throwable exception);
    
    // 便捷方法：自动管理 Span 生命周期
    <T> T trace(String name, SpanType type, Supplier<T> operation);
}

// ============ Span 类型枚举 ============
public enum SpanType {
    AGENT,          // Agent 整体
    LLM,            // 大模型调用
    EMBEDDING,      // 向量嵌入
    RETRIEVER,      // 检索器
    VECTOR_DB,      // 向量数据库
    TOOL,           // 工具调用
    HTTP,           // HTTP 请求
    RERANKER,       // 重排序
    FORMATTER,      // Prompt 格式化
    MEMORY,         // 记忆操作
    CUSTOM          // 自定义
}

// ============ 框架拦截器示例 ============

// 1. LLM 拦截器
public class LlmInterceptor {
    private final AgentLensTracer tracer;
    
    public ChatResponse intercept(ChatRequest request, ChatModel model) {
        Map<String, Object> attrs = Map.of(
            "gen_ai.operation.name", "chat",
            "gen_ai.provider.name", model.getProvider(),
            "gen_ai.request.model", model.getModelName(),
            "gen_ai.request.temperature", request.getTemperature()
        );
        
        Span span = tracer.startSpan("ChatModel.call", SpanType.LLM, attrs);
        try {
            ChatResponse response = model.call(request);
            
            // 补充响应属性
            span.setAttribute("gen_ai.usage.input_tokens", response.getInputTokens());
            span.setAttribute("gen_ai.usage.output_tokens", response.getOutputTokens());
            span.setAttribute("cost.usd", calculateCost(model, response));
            
            tracer.endSpan(span, SpanStatus.OK, response.getContent());
            return response;
        } catch (Exception e) {
            tracer.recordException(span, e);
            tracer.endSpan(span, SpanStatus.ERROR, null);
            throw e;
        }
    }
}

// 2. 向量数据库拦截器
public class VectorDbInterceptor {
    private final AgentLensTracer tracer;
    
    public List<Document> interceptSearch(String collection, float[] vector, int topK, VectorStore store) {
        Map<String, Object> attrs = Map.of(
            "db.system", store.getType(),           // qdrant, milvus, pgvector
            "db.operation.name", "search",
            "db.collection.name", collection,
            "vector.dimension", vector.length,
            "vector.search.top_k", topK
        );
        
        Span span = tracer.startSpan("VectorStore.search", SpanType.VECTOR_DB, attrs);
        try {
            List<Document> results = store.search(collection, vector, topK);
            
            span.setAttribute("vector.search.results_count", results.size());
            tracer.endSpan(span, SpanStatus.OK, results);
            return results;
        } catch (Exception e) {
            tracer.recordException(span, e);
            tracer.endSpan(span, SpanStatus.ERROR, null);
            throw e;
        }
    }
}

// 3. 工具调用拦截器
public class ToolInterceptor {
    private final AgentLensTracer tracer;
    
    public Object interceptToolCall(String toolName, Object input, Tool tool) {
        Map<String, Object> attrs = Map.of(
            "tool.name", toolName,
            "tool.description", tool.getDescription(),
            "tool.input", JsonUtils.toJson(input)
        );
        
        Span span = tracer.startSpan("Tool." + toolName, SpanType.TOOL, attrs);
        try {
            Object result = tool.execute(input);
            
            span.setAttribute("tool.output", JsonUtils.toJson(result));
            span.setAttribute("tool.status", "success");
            tracer.endSpan(span, SpanStatus.OK, result);
            return result;
        } catch (Exception e) {
            span.setAttribute("tool.status", "error");
            span.setAttribute("tool.error.message", e.getMessage());
            tracer.recordException(span, e);
            tracer.endSpan(span, SpanStatus.ERROR, null);
            throw e;
        }
    }
}

// 4. Embedding 拦截器
public class EmbeddingInterceptor {
    private final AgentLensTracer tracer;
    
    public float[] interceptEmbed(String text, EmbeddingModel model) {
        Map<String, Object> attrs = Map.of(
            "gen_ai.operation.name", "embeddings",
            "gen_ai.provider.name", model.getProvider(),
            "gen_ai.request.model", model.getModelName()
        );
        
        Span span = tracer.startSpan("Embedding.embed", SpanType.EMBEDDING, attrs);
        try {
            float[] vector = model.embed(text);
            
            span.setAttribute("gen_ai.usage.input_tokens", model.countTokens(text));
            span.setAttribute("vector.dimension", vector.length);
            tracer.endSpan(span, SpanStatus.OK, null);
            return vector;
        } catch (Exception e) {
            tracer.recordException(span, e);
            tracer.endSpan(span, SpanStatus.ERROR, null);
            throw e;
        }
    }
}
```

### 11.6 使用示例（全链路）

```java
// 用户代码 - 自动追踪整个链路
@Service
public class RAGAgent {
    
    @Autowired
    private ChatModel chatModel;           // 自动拦截
    
    @Autowired
    private EmbeddingModel embeddingModel; // 自动拦截
    
    @Autowired
    private VectorStore vectorStore;       // 自动拦截
    
    @Autowired
    private WebSearchTool webSearchTool;   // 自动拦截
    
    @Trace(name = "RAGAgent.answer", type = SpanType.AGENT)  // 注解方式
    public String answer(String question) {
        // 1. 向量嵌入 → 自动记录 EMBEDDING span
        float[] queryVector = embeddingModel.embed(question);
        
        // 2. 向量检索 → 自动记录 VECTOR_DB span
        List<Document> docs = vectorStore.search("knowledge_base", queryVector, 5);
        
        // 3. 工具调用 → 自动记录 TOOL span
        String webResults = webSearchTool.search(question);
        
        // 4. LLM 生成 → 自动记录 LLM span
        String context = buildContext(docs, webResults);
        String answer = chatModel.chat(buildPrompt(question, context));
        
        return answer;
    }
}
```

### 11.7 Dashboard 全链路视图

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Trace: RAGAgent.answer                           Total: 3.5s  $0.013  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ├─ [AGENT] RAGAgent.answer ─────────────────────────────────── 3500ms │
│  │                                                                      │
│  │   ├─ [EMBEDDING] Embedding.embed ────────────────────────── 150ms  │
│  │   │   model: text-embedding-v3, tokens: 12                          │
│  │   │                                                                  │
│  │   ├─ [VECTOR_DB] Qdrant.search ──────────────────────────── 45ms   │
│  │   │   collection: knowledge_base, top_k: 5, results: 5              │
│  │   │                                                                  │
│  │   ├─ [TOOL] WebSearch.search ────────────────────────────── 500ms  │
│  │   │   status: success                                               │
│  │   │                                                                  │
│  │   └─ [LLM] ChatModel.call ───────────────────────────────── 2800ms │
│  │       model: qwen-max, in: 1500 tokens, out: 300 tokens             │
│  │       cost: $0.012                                                  │
│                                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│  Summary:  LLM: 80%  │  Tool: 14%  │  VectorDB: 1%  │  Embedding: 4%   │
└─────────────────────────────────────────────────────────────────────────┘
```

### 11.8 支持的数据源（开箱即用）

| 类别 | 组件 | 自动拦截 |
|------|------|---------|
| **LLM** | OpenAI, Claude, Qwen (DashScope), Gemini, Ollama | ✅ |
| **Embedding** | OpenAI Embedding, Qwen Embedding, HuggingFace | ✅ |
| **向量数据库** | Qdrant, Milvus, PgVector, Chroma, Elasticsearch | ✅ |
| **工具** | MCP Tools, Function Calling, 自定义 Tool | ✅ |
| **记忆** | Mem0, ReME, 自定义 Memory | ✅ |
| **HTTP** | OkHttp, RestTemplate, WebClient | ✅ |
| **框架** | AgentScope-Java, Spring AI, LangChain4j | ✅ |

### 11.9 成本计算（全链路）

```java
public class FullStackCostCalculator {
    
    public TraceCost calculate(Trace trace) {
        TraceCost cost = new TraceCost();
        
        for (Span span : trace.getAllSpans()) {
            switch (span.getType()) {
                case LLM:
                    cost.addLlmCost(calculateLlmCost(span));
                    break;
                case EMBEDDING:
                    cost.addEmbeddingCost(calculateEmbeddingCost(span));
                    break;
                case VECTOR_DB:
                    // 向量数据库通常按请求次数或存储量计费
                    cost.addVectorDbCost(calculateVectorDbCost(span));
                    break;
                case TOOL:
                    // 某些工具（如付费 API）可能有成本
                    cost.addToolCost(calculateToolCost(span));
                    break;
            }
        }
        
        return cost;
    }
}

// 成本报告
public class TraceCost {
    private double llmCost;
    private double embeddingCost;
    private double vectorDbCost;
    private double toolCost;
    
    public double getTotalCost() {
        return llmCost + embeddingCost + vectorDbCost + toolCost;
    }
    
    public Map<String, Double> getBreakdown() {
        return Map.of(
            "LLM", llmCost,
            "Embedding", embeddingCost,
            "VectorDB", vectorDbCost,
            "Tool", toolCost
        );
    }
}
```

---

## 12. 更新后的 MVP 范围

| 优先级 | 功能 | 支持的 Span 类型 |
|--------|------|-----------------|
| **P0** | LLM 调用追踪 | LLM |
| **P0** | 向量数据库追踪 | VECTOR_DB, EMBEDDING |
| **P0** | Agent 整体追踪 | AGENT |
| **P1** | 工具调用追踪 | TOOL |
| **P1** | 成本计算（LLM + Embedding） | - |
| **P2** | HTTP 请求追踪 | HTTP |
| **P2** | 记忆操作追踪 | MEMORY |
| **P2** | 告警规则 | - |

---

## 13. 总结

**AgentLens 全链路可观测性**能够追踪：

1. ✅ **LLM 调用**：模型、Token、成本、延迟
2. ✅ **向量嵌入**：Embedding 模型、维度、Token
3. ✅ **向量数据库**：Qdrant/Milvus/PgVector 查询、召回数量
4. ✅ **工具调用**：MCP/Function Calling 输入输出
5. ✅ **HTTP 请求**：外部 API 调用
6. ✅ **Agent 整体**：端到端链路、总耗时、总成本

这比 AgentScope-Studio（主要只追踪 LLM）更全面，是明显的差异化优势。
