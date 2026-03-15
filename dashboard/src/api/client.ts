/**
 * AgentLens Collector REST API 客户端
 * 通过 Vite 代理 /api -> Collector，调用 /api/v1 下的接口
 */
import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
})

/** 列表项中的 Trace 摘要 */
export interface Trace {
  traceId: string
  projectId: string
  rootSpanName: string | null
  startTime: string
  endTime: string | null
  durationMs: number | null
  status: string
  spanCount: number | null
  totalTokens: number | null
  totalCostUsd: number | null
}

/** 单条 Span（一次 LLM/工具/向量库等操作） */
export interface Span {
  spanId: string
  traceId: string
  parentSpanId: string | null
  name: string
  spanType: string
  startTime: string
  endTime: string | null
  durationMs: number | null
  status: string
  attributes: string | null
  input: string | null
  output: string | null
  errorMessage: string | null
  inputTokens: number | null
  outputTokens: number | null
  totalTokens: number | null
  costUsd: number | null
  model: string | null
  provider: string | null
}

/** Trace 详情（含其下所有 Span） */
export interface TraceDetail extends Trace {
  rootSpanId: string | null
  spans: Span[]
}

/** 项目在指定天数内的统计 */
export interface ProjectStats {
  projectId: string
  days: number
  traceCount: number
  totalCostUsd: number
  totalTokens: number
  errorCount: number
  errorRate: number
  spanTypeStats: Record<string, SpanTypeStats>
  modelUsage: ModelUsage[]
}

/** 按 Span 类型汇总的统计 */
export interface SpanTypeStats {
  spanType: string
  count: number
  totalDurationMs: number
  totalCostUsd: number
}

/** 按模型汇总的调用与成本 */
export interface ModelUsage {
  model: string
  provider: string
  callCount: number
  inputTokens: number
  outputTokens: number
  totalCostUsd: number
}

/** 分页响应 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

/** 封装对 Collector API 的调用 */
export const api = {
  /** 分页获取某项目的 Trace 列表 */
  async listTraces(projectId: string, page = 0, size = 20): Promise<PageResponse<Trace>> {
    const response = await apiClient.get(`/projects/${projectId}/traces`, {
      params: { page, size },
    })
    return response.data
  },

  /** 按 traceId 获取 Trace 详情（含所有 Span） */
  async getTraceDetail(traceId: string): Promise<TraceDetail> {
    const response = await apiClient.get(`/traces/${traceId}`)
    return response.data
  },

  /** 获取项目在最近 days 天内的统计（Trace 数、成本、Token、错误率、Span 类型与模型分布） */
  async getProjectStats(projectId: string, days = 7): Promise<ProjectStats> {
    const response = await apiClient.get(`/projects/${projectId}/stats`, {
      params: { days },
    })
    return response.data
  },
}

export default apiClient
