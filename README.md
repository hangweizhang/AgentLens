# AgentLens

> Java Agent Observability Platform

AgentLens 是一个 Java 生态的通用 Agent 可观测性平台，提供全链路 Trace 追踪、成本归因、开箱即用的框架支持。

## 特性

- **全链路追踪**: LLM 调用、向量数据库、工具调用、HTTP 请求
- **成本归因**: 自动计算每次 LLM 调用的 Token 成本
- **多框架支持**: Spring AI、LangChain4j、AgentScope-Java
- **可视化 Dashboard**: Trace 瀑布图、成本分析、错误监控
- **OpenTelemetry 兼容**: 遵循 GenAI Semantic Conventions

## 快速开始

### 1. 添加依赖

```xml
<!-- Spring AI 用户 -->
<dependency>
    <groupId>io.agentlens</groupId>
    <artifactId>agentlens-springai</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>

<!-- LangChain4j 用户 -->
<dependency>
    <groupId>io.agentlens</groupId>
    <artifactId>agentlens-langchain4j</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 2. 初始化 SDK

```java
// 初始化 AgentLens
AgentLens.init(AgentLensConfig.builder("my-project")
    .collectorUrl("http://localhost:4317")
    .enableCostTracking(true)
    .build());
```

### 3. Spring AI 自动追踪

Spring AI 用户无需额外配置，添加依赖后自动追踪 ChatModel、EmbeddingModel、VectorStore。

```yaml
# application.yml
agentlens:
  enabled: true
  springai:
    trace-chat-model: true
    trace-embedding-model: true
    trace-vector-store: true
```

### 4. LangChain4j 手动包装

```java
ChatLanguageModel model = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .build();

// 包装模型以启用追踪
ChatLanguageModel tracedModel = AgentLensLangChain4j.trace(model, "gpt-4o");
```

### 5. 启动 Collector 和 Dashboard

```bash
cd docker
docker-compose up -d
```

访问 http://localhost:3000 查看 Dashboard。

## 架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      AgentLens Dashboard                        │
│                   (React + Recharts + Tailwind)                 │
├─────────────────────────────────────────────────────────────────┤
│                      REST API (Spring Boot)                     │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      AgentLens Collector                        │
│              (Spring Boot + OTLP Receiver)                      │
├─────────────────────────────────────────────────────────────────┤
│              Storage Adapter (PostgreSQL/SQLite)                │
└───────────────────────────┬─────────────────────────────────────┘
                            │
              ┌─────────────┼─────────────┬─────────────┐
              ▼             ▼             ▼             ▼
        AgentLens      AgentLens      AgentLens      AgentLens
        SDK for        SDK for        SDK for        SDK for
        AgentScope     Spring AI      LangChain4j    (通用)
```

## 项目结构

```
AgentLens/
├── sdk/                    # Java SDK
│   ├── agentlens-core/     # 核心模块
│   ├── agentlens-springai/ # Spring AI 适配器
│   ├── agentlens-langchain4j/  # LangChain4j 适配器
│   └── agentlens-agentscope/   # AgentScope 适配器
├── collector/              # Collector 服务
├── dashboard/              # React Dashboard
└── docker/                 # Docker 部署
```

## 支持的 Span 类型

| 类型 | 说明 |
|------|------|
| AGENT | Agent 整体执行 |
| LLM | 大模型调用 |
| EMBEDDING | 向量嵌入 |
| VECTOR_DB | 向量数据库操作 |
| TOOL | 工具/函数调用 |
| HTTP | HTTP 请求 |
| RETRIEVER | RAG 检索 |
| MEMORY | 记忆读写 |

## 内置成本计算

支持以下模型的自动成本计算：

- OpenAI: GPT-4o, GPT-4o-mini, GPT-4-turbo, GPT-3.5-turbo
- Anthropic: Claude-3.5-sonnet, Claude-3-opus, Claude-3-haiku
- Alibaba: Qwen-max, Qwen-plus, Qwen-turbo
- Google: Gemini-1.5-pro, Gemini-1.5-flash
- DeepSeek: deepseek-chat, deepseek-coder

## 开发

### 构建

```bash
# 构建 SDK 和 Collector
mvn clean install

# 启动 Collector (开发模式，SQLite)
cd collector
mvn spring-boot:run

# 启动 Dashboard
cd dashboard
npm install
npm run dev
```

### 运行测试

```bash
mvn test
```

## License

Apache 2.0

## 后续规划
1. 支持所有java-agent框架的trace集成
2. 支持模型,agent评估
3. etc...
