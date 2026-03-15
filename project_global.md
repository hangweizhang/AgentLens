# AgentLens - 项目全局状态

> 最后更新: 2026-03-15

## 项目概述

**AgentLens** 是一个 Java 生态的通用 Agent 可观测性平台，提供：
- 全链路 Trace 追踪（LLM、向量数据库、工具调用、HTTP 请求）
- 成本归因（自动计算每次调用的 Token 成本）
- 开箱即用的框架支持（AgentScope-Java、Spring AI、LangChain4j）
- 可视化 Dashboard

## 当前开发状态

| 模块 | 状态 | 进度 | 备注 |
|------|------|------|------|
| **项目结构** | 🟢 已完成 | 100% | Monorepo 骨架搭建完成 |
| **agentlens-core** | 🟢 已完成 | 100% | SDK 核心模块: Tracer, Span, Cost, Exporter |
| **agentlens-springai** | 🟢 已完成 | 100% | Spring AOP 自动拦截 |
| **agentlens-langchain4j** | 🟢 已完成 | 100% | 装饰器模式包装 |
| **agentlens-agentscope** | 🟢 已完成 | 100% | 基础集成框架 |
| **agentlens-collector** | 🟢 已完成 | 100% | REST API + JPA + SQLite/PostgreSQL |
| **agentlens-dashboard** | 🟢 已完成 | 100% | React + Tailwind + Recharts |
| **Docker 部署** | 🟢 已完成 | 100% | Docker Compose 一键部署 |

状态图例: 🟢 已完成 | 🟡 进行中 | ⚪ 待开始 | 🔴 阻塞

## MVP 已完成功能

- [x] SDK Core: Tracer, Span, SpanType, 属性定义
- [x] 成本计算: 内置 OpenAI/Claude/Qwen/Gemini/DeepSeek 定价
- [x] Spring AI 自动追踪: ChatModel, EmbeddingModel, VectorStore
- [x] LangChain4j 装饰器: ChatLanguageModel, EmbeddingModel
- [x] Collector REST API: Trace 接收, 查询, 统计
- [x] Dashboard: Traces 列表, Trace 详情, 成本分析
- [x] Docker 部署: PostgreSQL + Collector + Dashboard

---

## 技术栈

### 后端 (SDK + Collector)
- **语言**: Java 17+
- **构建**: Maven (多模块)
- **框架**: Spring Boot 3.x
- **协议**: OpenTelemetry (OTLP)
- **架构**: Clean Architecture + DDD + Hexagonal

### 前端 (Dashboard)
- **框架**: React 18 + TypeScript
- **构建**: Vite
- **样式**: Tailwind CSS
- **图表**: Recharts

### 存储 (可插拔)
- PostgreSQL (默认)
- ClickHouse (大数据量)
- SQLite (单机演示)

---

## 目录结构

```
AgentLens/
├── project_global.md              # 本文件 - 项目全局状态
├── docs/                          # 文档
│   └── agentscope-studio-research.md
├── sdk/                           # Java SDK (Maven 多模块)
│   ├── pom.xml                    # 父 POM
│   ├── agentlens-core/            # 核心模块
│   ├── agentlens-springai/        # Spring AI 适配器
│   ├── agentlens-langchain4j/     # LangChain4j 适配器
│   └── agentlens-agentscope/      # AgentScope 适配器
├── collector/                     # Collector 服务
│   └── pom.xml
├── dashboard/                     # React Dashboard
│   └── package.json
└── docker/                        # Docker 部署
    └── docker-compose.yml
```

---

## 架构设计

### 整体架构

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
│              Storage Adapter (PostgreSQL/ClickHouse/SQLite)     │
└───────────────────────────┬─────────────────────────────────────┘
                            │
              ┌─────────────┼─────────────┬─────────────┐
              ▼             ▼             ▼             ▼
        AgentLens      AgentLens      AgentLens      AgentLens
        SDK for        SDK for        SDK for        SDK for
        AgentScope     Spring AI      LangChain4j    (通用)
```

### SDK 分层架构 (Clean Architecture + Hexagonal)

```
sdk/agentlens-core/
└── src/main/java/io/agentlens/
    ├── domain/                    # 领域层 (无外部依赖)
    │   ├── trace/                 # Trace 聚合根
    │   │   ├── Trace.java
    │   │   ├── Span.java
    │   │   ├── SpanType.java
    │   │   └── SpanStatus.java
    │   ├── cost/                  # 成本领域
    │   │   ├── TokenCost.java
    │   │   └── PricingModel.java
    │   └── shared/                # 共享内核
    │       ├── Entity.java
    │       ├── ValueObject.java
    │       └── DomainEvent.java
    ├── application/               # 应用层
    │   ├── ports/
    │   │   ├── driver/            # 入站端口 (用例接口)
    │   │   │   └── TracingPort.java
    │   │   └── driven/            # 出站端口 (依赖接口)
    │   │       └── ExporterPort.java
    │   └── usecases/
    │       └── StartSpanUseCase.java
    └── infrastructure/            # 基础设施层
        ├── adapters/
        │   ├── driver/            # 入站适配器
        │   │   └── AgentLensTracer.java
        │   └── driven/            # 出站适配器
        │       ├── OtlpExporter.java
        │       └── HttpExporter.java
        └── config/
            └── AgentLensConfig.java
```

---

## 开发路线图

### Phase 1: MVP (已完成)
- [x] 项目规划与调研
- [x] Monorepo 项目结构搭建
- [x] SDK Core 实现 (Tracer, Span, Cost, Exporter)
- [x] Spring AI 适配器 (AOP 自动拦截)
- [x] LangChain4j 适配器 (装饰器模式)
- [x] AgentScope-Java 适配器 (基础框架)
- [x] Collector 基础版 (SQLite/PostgreSQL)
- [x] Dashboard 基础版 (Traces, Analytics)
- [x] Docker 部署方案

### Phase 2: 扩展 (待开发)
- [ ] OTLP gRPC Receiver
- [ ] ClickHouse 存储适配器
- [ ] 流式 Span 处理
- [ ] Trace 重放/Debug 功能
- [ ] 更多 LLM 模型定价

### Phase 3: 生产就绪 (规划中)
- [ ] 告警规则引擎
- [ ] Slack/钉钉通知集成
- [ ] 云托管版本
- [ ] 用户认证与多租户
- [ ] 性能优化
- [ ] 文档完善

---

## 关键决策记录

| 日期 | 决策 | 原因 |
|------|------|------|
| 2026-03-15 | 采用 Monorepo 结构 | 便于统一管理 SDK、Collector、Dashboard |
| 2026-03-15 | 存储层可插拔设计 | 支持不同场景：SQLite(演示)、PostgreSQL(生产)、ClickHouse(大数据) |
| 2026-03-15 | 后端采用 Clean Architecture | 保持代码整洁、可测试、易扩展 |
| 2026-03-15 | Dashboard 采用 React + Vite | 现代技术栈，生态成熟 |
| 2026-03-15 | 补充详细中文注释 | 类/接口/方法级 Javadoc 与 TS 文件头注释，便于团队阅读与维护 |

---

## 待解决问题

1. **OTLP gRPC Receiver**: 当前使用 HTTP，需要添加 gRPC 支持
2. **Spring AI 版本兼容性**: 当前基于 1.0.0-M5，需要跟进正式版
3. **AgentScope-Java 集成**: 等待 AgentScope-Java 稳定版发布后完善
4. **ClickHouse 适配器**: 大数据量场景的存储方案

---

## 参考资源

- [AgentScope-Studio 调研报告](./docs/agentscope-studio-research.md)
- [OpenTelemetry GenAI Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/gen-ai/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
