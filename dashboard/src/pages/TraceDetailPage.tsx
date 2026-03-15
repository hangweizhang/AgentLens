/**
 * Trace 详情页：展示单条 Trace 的汇总信息与所有 Span 的瀑布条（类型、耗时、Token、成本、错误）
 */
import { useQuery } from '@tanstack/react-query'
import { useParams, Link } from 'react-router-dom'
import { api, Span } from '../api/client'
import clsx from 'clsx'
import { ArrowLeft, Clock, DollarSign, AlertCircle } from 'lucide-react'

/** Span 类型对应的徽章样式 */
const spanTypeColors: Record<string, string> = {
  agent: 'bg-purple-100 text-purple-800 border-purple-300',
  llm: 'bg-blue-100 text-blue-800 border-blue-300',
  embedding: 'bg-cyan-100 text-cyan-800 border-cyan-300',
  vector_db: 'bg-green-100 text-green-800 border-green-300',
  tool: 'bg-orange-100 text-orange-800 border-orange-300',
  http: 'bg-gray-100 text-gray-800 border-gray-300',
  retriever: 'bg-teal-100 text-teal-800 border-teal-300',
  memory: 'bg-pink-100 text-pink-800 border-pink-300',
}

function SpanRow({ span, maxDuration }: { span: Span; maxDuration: number }) {
  const duration = span.durationMs || 0
  const widthPercent = maxDuration > 0 ? (duration / maxDuration) * 100 : 0
  const typeColor = spanTypeColors[span.spanType] || 'bg-gray-100 text-gray-800 border-gray-300'

  return (
    <div className="border-b border-gray-100 py-3 px-4 hover:bg-gray-50">
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center space-x-3">
          <span className={clsx('px-2 py-0.5 text-xs font-medium rounded border', typeColor)}>
            {span.spanType}
          </span>
          <span className="font-medium text-gray-900">{span.name}</span>
          {span.status === 'error' && (
            <AlertCircle className="h-4 w-4 text-red-500" />
          )}
        </div>
        <div className="flex items-center space-x-4 text-sm text-gray-500">
          {span.model && (
            <span className="text-xs bg-gray-100 px-2 py-0.5 rounded">{span.model}</span>
          )}
          {span.totalTokens && (
            <span>{span.totalTokens.toLocaleString()} tokens</span>
          )}
          {span.costUsd && (
            <span className="flex items-center">
              <DollarSign className="h-3 w-3" />
              {span.costUsd.toFixed(4)}
            </span>
          )}
          <span className="flex items-center">
            <Clock className="h-3 w-3 mr-1" />
            {duration}ms
          </span>
        </div>
      </div>
      
      <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
        <div
          className={clsx('h-full rounded-full', {
            'bg-green-500': span.status === 'ok',
            'bg-red-500': span.status === 'error',
            'bg-yellow-500': span.status === 'running',
            'bg-blue-500': !span.status,
          })}
          style={{ width: `${widthPercent}%` }}
        />
      </div>

      {span.errorMessage && (
        <div className="mt-2 p-2 bg-red-50 border border-red-200 rounded text-sm text-red-700">
          {span.errorMessage}
        </div>
      )}
    </div>
  )
}

export default function TraceDetailPage() {
  const { traceId } = useParams<{ traceId: string }>()

  const { data: trace, isLoading, error } = useQuery({
    queryKey: ['trace', traceId],
    queryFn: () => api.getTraceDetail(traceId!),
    enabled: !!traceId,
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (error || !trace) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600">Failed to load trace</p>
      </div>
    )
  }

  const maxDuration = Math.max(...(trace.spans || []).map((s) => s.durationMs || 0))

  return (
    <div>
      <div className="mb-6">
        <Link
          to="/traces"
          className="inline-flex items-center text-gray-600 hover:text-gray-900 mb-4"
        >
          <ArrowLeft className="h-4 w-4 mr-1" />
          Back to Traces
        </Link>
        
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              {trace.spans?.[0]?.name || 'Trace Detail'}
            </h1>
            <p className="text-gray-500 text-sm mt-1">{trace.traceId}</p>
          </div>
          
          <div className="flex items-center space-x-6">
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-900">{trace.durationMs || 0}ms</p>
              <p className="text-xs text-gray-500">Duration</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-900">
                {trace.totalTokens?.toLocaleString() || 0}
              </p>
              <p className="text-xs text-gray-500">Tokens</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-900">
                ${trace.totalCostUsd?.toFixed(4) || '0.0000'}
              </p>
              <p className="text-xs text-gray-500">Cost</p>
            </div>
            <div className="text-center">
              <span
                className={clsx('badge text-sm', {
                  'badge-success': trace.status === 'ok',
                  'badge-error': trace.status === 'error',
                })}
              >
                {trace.status}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="px-4 py-3 border-b border-gray-200 bg-gray-50">
          <h2 className="font-semibold text-gray-900">
            Spans ({trace.spans?.length || 0})
          </h2>
        </div>
        <div>
          {(trace.spans || []).map((span) => (
            <SpanRow key={span.spanId} span={span} maxDuration={maxDuration} />
          ))}
        </div>
      </div>
    </div>
  )
}
