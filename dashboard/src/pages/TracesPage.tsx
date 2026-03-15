/**
 * Trace 列表页：按项目分页展示 Trace，表格含状态、耗时、Token、成本、时间
 */
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { formatDistanceToNow } from 'date-fns'
import { api, Trace } from '../api/client'
import clsx from 'clsx'
import { Clock, DollarSign, Layers } from 'lucide-react'

export default function TracesPage() {
  const projectId = 'default'
  
  const { data, isLoading, error } = useQuery({
    queryKey: ['traces', projectId],
    queryFn: () => api.listTraces(projectId),
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600">Failed to load traces</p>
      </div>
    )
  }

  const traces = data?.content || []

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Traces</h1>
        <p className="text-gray-600">Monitor your AI agent executions</p>
      </div>

      {traces.length === 0 ? (
        <div className="card p-12 text-center">
          <Layers className="h-12 w-12 mx-auto text-gray-400 mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No traces yet</h3>
          <p className="text-gray-600">
            Start sending traces from your application using the AgentLens SDK.
          </p>
        </div>
      ) : (
        <div className="card overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Trace
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Duration
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Tokens
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Cost
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Time
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {traces.map((trace: Trace) => (
                <tr key={trace.traceId} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <Link
                      to={`/traces/${trace.traceId}`}
                      className="text-primary-600 hover:text-primary-800 font-medium"
                    >
                      {trace.rootSpanName || trace.traceId.substring(0, 8)}
                    </Link>
                    <p className="text-xs text-gray-500 mt-1">
                      {trace.spanCount} spans
                    </p>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={clsx('badge', {
                        'badge-success': trace.status === 'ok',
                        'badge-error': trace.status === 'error',
                        'badge-warning': trace.status === 'running',
                      })}
                    >
                      {trace.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <div className="flex items-center">
                      <Clock className="h-4 w-4 mr-1" />
                      {trace.durationMs ? `${trace.durationMs}ms` : '-'}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {trace.totalTokens?.toLocaleString() || '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <div className="flex items-center">
                      <DollarSign className="h-4 w-4 mr-1" />
                      {trace.totalCostUsd ? `$${trace.totalCostUsd.toFixed(4)}` : '-'}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatDistanceToNow(new Date(trace.startTime), { addSuffix: true })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
