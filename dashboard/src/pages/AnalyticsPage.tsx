/**
 * 分析页：最近 7 天统计卡片（Trace 数、总成本、总 Token、错误率）+ Span 类型饼图 + 模型用量柱状图 + 模型成本表格
 */
import { useQuery } from '@tanstack/react-query'
import { api } from '../api/client'
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts'
import { Activity, DollarSign, AlertTriangle, Layers } from 'lucide-react'

const COLORS = ['#0ea5e9', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#ec4899']

/** 统计卡片：图标 + 标题 + 数值 */
function StatCard({
  title,
  value,
  icon: Icon,
  color,
}: {
  title: string
  value: string | number
  icon: React.ElementType
  color: string
}) {
  return (
    <div className="card p-6">
      <div className="flex items-center">
        <div className={`p-3 rounded-lg ${color}`}>
          <Icon className="h-6 w-6 text-white" />
        </div>
        <div className="ml-4">
          <p className="text-sm text-gray-500">{title}</p>
          <p className="text-2xl font-bold text-gray-900">{value}</p>
        </div>
      </div>
    </div>
  )
}

export default function AnalyticsPage() {
  const projectId = 'default'

  const { data: stats, isLoading } = useQuery({
    queryKey: ['stats', projectId],
    queryFn: () => api.getProjectStats(projectId, 7),
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  const spanTypeData = stats?.spanTypeStats
    ? Object.entries(stats.spanTypeStats).map(([type, data]) => ({
        name: type,
        count: data.count,
        cost: data.totalCostUsd,
      }))
    : []

  const modelData = stats?.modelUsage || []

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Analytics</h1>
        <p className="text-gray-600">Last 7 days overview</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <StatCard
          title="Total Traces"
          value={stats?.traceCount?.toLocaleString() || 0}
          icon={Layers}
          color="bg-blue-500"
        />
        <StatCard
          title="Total Cost"
          value={`$${stats?.totalCostUsd?.toFixed(2) || '0.00'}`}
          icon={DollarSign}
          color="bg-green-500"
        />
        <StatCard
          title="Total Tokens"
          value={stats?.totalTokens?.toLocaleString() || 0}
          icon={Activity}
          color="bg-purple-500"
        />
        <StatCard
          title="Error Rate"
          value={`${stats?.errorRate?.toFixed(1) || 0}%`}
          icon={AlertTriangle}
          color="bg-red-500"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Span Types</h3>
          {spanTypeData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={spanTypeData}
                  dataKey="count"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  label={({ name, percent }) =>
                    `${name} ${(percent * 100).toFixed(0)}%`
                  }
                >
                  {spanTypeData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-[300px] flex items-center justify-center text-gray-500">
              No data available
            </div>
          )}
        </div>

        <div className="card p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Model Usage</h3>
          {modelData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={modelData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="model" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="callCount" fill="#0ea5e9" name="Calls" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-[300px] flex items-center justify-center text-gray-500">
              No data available
            </div>
          )}
        </div>
      </div>

      {modelData.length > 0 && (
        <div className="card mt-6 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
            <h3 className="font-semibold text-gray-900">Model Cost Breakdown</h3>
          </div>
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Model
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Provider
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Calls
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Input Tokens
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Output Tokens
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Cost
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {modelData.map((model, i) => (
                <tr key={i} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">
                    {model.model}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-gray-500">
                    {model.provider}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-gray-500">
                    {model.callCount.toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-gray-500">
                    {model.inputTokens.toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-gray-500">
                    {model.outputTokens.toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">
                    ${model.totalCostUsd.toFixed(4)}
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
