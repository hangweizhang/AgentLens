# AgentLens MVP 测试方案

> 版本: v0.1 | 更新: 2026-03-15

## 1. 测试目标与范围

### 1.1 目标

- 验证 **SDK 打点 → Collector 接收 → 持久化 → Dashboard 展示** 全链路可用。
- 验证 **成本计算、多 Span 类型、错误记录** 等核心能力。
- 为后续迭代建立可回归的测试基线。

### 1.2 MVP 范围

| 模块 | 验证重点 |
|------|----------|
| **agentlens-core** | Trace/Span 生命周期、成本计算、Console/Http 导出 |
| **agentlens-springai** | ChatModel/EmbeddingModel/VectorStore 自动拦截（可选 Demo） |
| **agentlens-langchain4j** | 装饰器包装后 generate/embed 打点（可选 Demo） |
| **collector** | HTTP 接入、存储、列表/详情/统计 API |
| **dashboard** | Traces 列表、Trace 详情、Analytics 页展示 |
| **docker** | 一键启动、服务互通、端口正确 |

---

## 2. 测试环境与前置条件

### 2.1 环境

- **JDK**: 17+
- **Maven**: 3.8+
- **Node**: 18+（仅 Dashboard 开发时）
- **Docker & Docker Compose**（E2E 与 Docker 验证时）

### 2.2 前置

- 仓库已 clone，根目录执行 `mvn clean install -DskipTests` 通过。
- 如需 E2E：Collector 默认端口 **4317** 未被占用；Dashboard 开发时端口 **3000** 可用。

---

## 3. 单元测试（SDK Core）

### 3.1 领域层

| 用例 ID | 描述 | 预期 |
|---------|------|------|
| U-D-01 | `TraceId.of(value)` 空或 null 抛异常 | `IllegalArgumentException` |
| U-D-02 | `SpanId.generate()` 返回 16 位非空字符串 | 长度 16，非空 |
| U-D-03 | `TokenCost.of(100, 200, 0.01)` 总 Token=300，成本 0.01 | `getTotalTokens()==300`, `getCostUsd()==0.01` |
| U-D-04 | `PricingModel.calculateCost(1000, 500)` 按 input/output 单价计算 | 结果与定价表一致 |
| U-D-05 | `PricingRegistry.calculateCost("gpt-4o", "openai", 1000, 500)` | 返回非零成本，且 input+output 对应 |
| U-D-06 | 未知模型 `PricingRegistry.calculateCost("unknown", "x", 1, 1)` | 返回 `TokenCost` 且 cost=0（不抛异常） |
| U-D-07 | `Span` 未 end 时 `getDurationMs()` 为当前耗时 | ≥0 |
| U-D-08 | `Trace.getChildSpans(parentId)` 仅返回 parentSpanId=parentId 的 Span | 数量与内容正确 |

### 3.2 应用层（TracingService）

| 用例 ID | 描述 | 预期 |
|---------|------|------|
| U-A-01 | 未 init 时 `startTrace()` 抛异常 | `IllegalStateException`（通过 `AgentLens.startTrace()` 间接测） |
| U-A-02 | init 后 `startTrace(projectId)` 返回非 null Trace，且 getCurrentTrace() 一致 | 同一条 Trace |
| U-A-03 | `startSpan("x", LLM)` 后 getCurrentSpan() 为刚创建的 Span | name=x, type=LLM |
| U-A-04 | `trace("op", LLM, () -> "ok")` 返回 "ok"，且 Span 状态 OK、有 output | 返回值 "ok"，Span.endTime 非 null，status=OK |
| U-A-05 | `trace("op", LLM, () -> { throw new RuntimeException("e"); })` 后 Span 状态 ERROR、带 errorMessage | status=ERROR，errorMessage 含 "e" |
| U-A-06 | 使用 NoopExporter 时 exportSpan/exportTrace 被调用不抛异常 | 无异常 |

### 3.3 执行方式

在项目根目录执行：

```bash
mvn test -pl sdk/agentlens-core
```

**通过标准**: 上述用例对应测试类/方法全部通过，无失败。

---

## 4. 集成测试（Collector）

### 4.1 API 行为

| 用例 ID | 接口 | 操作 | 预期 |
|---------|------|------|------|
| I-01 | `POST /api/v1/traces` | Body: `[{ "type": "trace", "traceId": "t1", "projectId": "p1", "status": "ok", "durationMs": 100, "spanCount": 1 }]` | 200，且后续 GET 可查到 |
| I-02 | `POST /api/v1/traces` | Body: `[{ "type": "span", "spanId": "s1", "traceId": "t1", "name": "LLM.call", "spanType": "llm", "status": "ok", "startTime": "2026-03-15T00:00:00Z", "endTime": "2026-03-15T00:00:01Z", "durationMs": 1000, "cost": { "inputTokens": 10, "outputTokens": 20, "costUsd": 0.001 } }]` | 200，且 GET trace 详情含该 Span、cost 正确 |
| I-03 | `GET /api/v1/projects/p1/traces` | - | 200，content 含 traceId t1 |
| I-04 | `GET /api/v1/traces/t1` | - | 200，body 含 traceId、spans 数组含 s1 |
| I-05 | `GET /api/v1/projects/p1/stats?days=7` | - | 200，traceCount≥1，totalCostUsd、modelUsage 等结构正确 |
| I-06 | `GET /api/v1/health` | - | 200，body 含 status=ok |

### 4.2 执行方式

1. 启动 Collector（SQLite 即可）:
   ```bash
   mvn -pl collector spring-boot:run
   ```
2. 使用 curl / Postman / 单元测试里的 `RestTemplate` 或 `TestRestTemplate` 按上表调用。
3. 建议在 `collector` 下增加 `src/test/java` 的 `@SpringBootTest` 集成测试，用 `TestRestTemplate` 或 `MockMvc` 自动化 I-01～I-06。

**通过标准**: 上述接口返回与预期一致，且数据库中存在对应 Trace/Span 记录。

---

## 5. 端到端验证（E2E）

### 5.1 链路说明

```
Demo 应用（带 SDK）→ HTTP 上报 → Collector（4317）→ 持久化
                                                         ↓
Dashboard（3000）← 代理 /api → Collector REST API
```

### 5.2 方式一：SDK 直连 Collector（推荐先做）

1. **启动 Collector**
   ```bash
   cd collector && mvn spring-boot:run
   ```
   默认 SQLite，端口 4317。

2. **运行一段「纯 SDK」脚本**（不依赖 Spring AI/LangChain4j 真实调用）：
   - 初始化 AgentLens：`AgentLens.init(AgentLensConfig.builder("mvp-test").collectorUrl("http://localhost:4317").exporterType(HTTP).build())`
   - `startTrace()` → `startSpan("Demo.LLM", LLM)` → 设置 attributes（如 model、inputTokens、outputTokens）→ 设置 cost → `endSpan()` → `completeTrace()`
   - 可选：再发 1～2 条带不同 projectId 或 spanType 的 Trace。

3. **验证**
   - `GET http://localhost:4317/api/v1/projects/mvp-test/traces` 能看到刚发的 Trace。
   - `GET http://localhost:4317/api/v1/traces/{traceId}` 能看到 Span 及 cost。
   - 启动 Dashboard（见 5.4），在 Traces 页和详情页、Analytics 页能看到数据。

### 5.3 方式二：Docker 一键环境

1. **启动**
   ```bash
   cd docker && docker-compose up -d
   ```
2. **等待** postgres、collector、dashboard 就绪（可 `docker-compose logs -f collector` 看日志）。
3. **发送数据**：用方式一中的「纯 SDK」脚本，将 `collectorUrl` 改为 `http://localhost:4317`（Docker 下 Collector 映射在 4317）。
4. **验证**：浏览器打开 `http://localhost:3000`，切换/确认项目为 `mvp-test`（或你使用的 projectId），查看 Traces、详情、Analytics。

### 5.4 启动 Dashboard（开发模式）

```bash
cd dashboard
npm install
npm run dev
```

浏览器访问 `http://localhost:3000`。若 Collector 未通过同一域名代理，需在 `vite.config.ts` 中配置 proxy 将 `/api` 指到 `http://localhost:4317`（当前配置应已包含）。

### 5.5 E2E 检查清单

- [ ] Collector 启动无报错，health 返回 200。
- [ ] SDK 上报后，Traces 列表出现对应 Trace（项目、时间、状态、耗时、成本）。
- [ ] 点击某条 Trace 进入详情，Span 列表与预期一致（名称、类型、耗时、Token、成本）。
- [ ] Analytics 页在选定时间范围内展示统计（Trace 数、总成本、Token、错误率、Span 类型分布、模型用量）。
- [ ] Docker 模式下，Dashboard 能通过 proxy 访问 Collector，无 CORS 问题。

---

## 6. 可选：Spring AI / LangChain4j Demo 验证

若需验证「真实框架 + 真实 LLM 调用」的自动打点（依赖网络与 API Key）：

### 6.1 Spring AI Demo 思路

- 新建独立 Maven 模块或工程，依赖 `agentlens-springai` 与 `agentlens-core`，以及 Spring AI + 某厂商（如 OpenAI/Ollama）。
- `AgentLens.init(...)` 指向本地 Collector；`agentlens.enabled=true`。
- 调用 `ChatModel.call()` / `EmbeddingModel.embed(...)` / `VectorStore.similaritySearch(...)` 若干次。
- 在 Collector 与 Dashboard 中查看是否出现 LLM/EMBEDDING/VECTOR_DB 类型 Span 及成本。

### 6.2 LangChain4j Demo 思路

- 独立工程依赖 `agentlens-langchain4j`、`agentlens-core`，以及 LangChain4j + 某模型。
- 使用 `AgentLensLangChain4j.trace(chatModel)` 包装后调用 `generate(...)`。
- 同上，在 Collector/Dashboard 验证 Span 与成本。

### 6.3 注意

- 需配置真实 API Key 或本地模型；MVP 验证可仅用「纯 SDK 模拟 Span」完成主链路验收，Demo 为增强项。

---

## 7. MVP 验收标准汇总

| 类别 | 通过标准 |
|------|----------|
| **单元测试** | `mvn test -pl sdk/agentlens-core` 全部通过，且覆盖 3.1、3.2 中关键用例 |
| **Collector API** | 4.1 中 I-01～I-06 均符合预期（可自动化或手工） |
| **E2E** | 至少完成 5.2（SDK → Collector → 查询），并在 Dashboard 看到 Traces 与详情、Analytics 有数据 |
| **Docker** | 5.3 流程可跑通，无端口或依赖错误 |
| **回归** | 上述步骤在干净环境（新 clone + 新 DB）可重复执行通过 |

---

## 8. 附录：快速脚本示例（纯 SDK 上报）

可在任意能依赖 `agentlens-core` 的 Java 类中执行（如一个 `main` 或单测里），用于 E2E 快速打数据：

```java
// 1. 初始化（指向本地 Collector）
AgentLens.init(AgentLensConfig.builder("mvp-test")
    .collectorUrl("http://localhost:4317")
    .exporterType(AgentLensConfig.ExporterType.HTTP)
    .enableCostTracking(true)
    .build());

// 2. 打一条 Trace + 一个 LLM Span
Trace trace = AgentLens.startTrace();
Span span = AgentLens.startSpan("DemoLLM.call", SpanType.LLM);
span.addAttribute("gen_ai.request.model", "gpt-4o");
span.addAttribute("gen_ai.provider.name", "openai");
span.addAttribute("gen_ai.usage.input_tokens", 100);
span.addAttribute("gen_ai.usage.output_tokens", 50);
span.setCost(TokenCost.of(100, 50, new BigDecimal("0.00125")));
AgentLens.endSpan(span);
AgentLens.completeTrace();

// 3. 若使用 HttpExporter，建议稍等或主动 flush 后再查 API
Thread.sleep(2000);
```

执行后调用 `GET /api/v1/projects/mvp-test/traces` 与 `GET /api/v1/traces/{traceId}` 验证。

---

## 9. 用例与测试类对应关系

自动化用例与代码中的测试类/方法对应如下，便于回归时定位。

### 9.1 SDK Core 单元测试（`sdk/agentlens-core`）

| 用例 ID | 测试类 | 说明 |
|---------|--------|------|
| U-D-01 | `TraceIdTest` | TraceId 校验 |
| U-D-02 | `SpanIdTest` | SpanId 生成 |
| U-D-03 | `TokenCostTest` | TokenCost 计算 |
| U-D-04 | `PricingModelTest` | PricingModel 单价计算 |
| U-D-05, U-D-06 | `PricingRegistryTest` | PricingRegistry 已知/未知模型 |
| U-D-07 | `SpanTest` | Span 未 end 时 duration |
| U-D-08 | `TraceTest` | Trace.getChildSpans |
| U-A-01 | `AgentLensTest` | 未 init 时 startTrace 抛异常 |
| U-A-02～U-A-06 | `TracingServiceTest` | TracingService/AgentLens 打点与导出 |

执行：`mvn test -pl sdk/agentlens-core`

### 9.2 Collector 集成与 E2E（`collector`）

| 用例 ID | 测试类 | 说明 |
|---------|--------|------|
| I-01～I-06 | `TraceControllerIntegrationTest` | MockMvc 调用 POST/GET 接口，@Order 保证先 ingest 再查询 |
| E2E | `TraceE2ETest` | SDK 初始化 HttpExporter → 打 Trace+Span → GET 列表/详情断言 |

执行：先 `mvn install -pl sdk/agentlens-core -DskipTests`，再 `mvn test -pl collector`。

---

## 10. 建议执行顺序

1. **单元测试**：`mvn test -pl sdk/agentlens-core`，确认 Core 无回归。
2. **Collector 测试**：`mvn install -pl sdk/agentlens-core -DskipTests` 后执行 `mvn test -pl collector`，覆盖 I-01～I-06 与 E2E。
3. **启动 Collector（可选）**：`mvn -pl collector spring-boot:run`，确认 4317 健康检查通过。
4. **集成验证**：用 curl 或已自动化集成测试按 4.1 发 1 条 trace + 1 条 span，再查列表/详情/统计。
5. **E2E**：用附录脚本或等价代码上报 1～2 条 Trace，启动 Dashboard，在页面上逐项勾选 5.5 检查清单。
6. **Docker**：`docker-compose up -d` 后重复步骤 5，确认通过代理访问正常。
7. （可选）Spring AI / LangChain4j Demo 工程跑真实调用，抽查 Dashboard 上的 LLM/Embedding Span 与成本。
