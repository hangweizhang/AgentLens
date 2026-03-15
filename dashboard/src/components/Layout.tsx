/**
 * 全局布局：顶部导航（Traces / Analytics）+ 项目选择 + 子路由出口
 */
import { Outlet, NavLink } from 'react-router-dom'
import { Activity, BarChart3, Layers } from 'lucide-react'
import clsx from 'clsx'

const navigation = [
  { name: 'Traces', href: '/traces', icon: Layers },
  { name: 'Analytics', href: '/analytics', icon: BarChart3 },
]

export default function Layout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex">
              <div className="flex-shrink-0 flex items-center">
                <Activity className="h-8 w-8 text-primary-600" />
                <span className="ml-2 text-xl font-bold text-gray-900">AgentLens</span>
              </div>
              <div className="hidden sm:ml-8 sm:flex sm:space-x-4">
                {navigation.map((item) => (
                  <NavLink
                    key={item.name}
                    to={item.href}
                    className={({ isActive }) =>
                      clsx(
                        'inline-flex items-center px-3 py-2 text-sm font-medium rounded-md',
                        isActive
                          ? 'text-primary-600 bg-primary-50'
                          : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                      )
                    }
                  >
                    <item.icon className="h-4 w-4 mr-2" />
                    {item.name}
                  </NavLink>
                ))}
              </div>
            </div>
            <div className="flex items-center">
              <select className="text-sm border-gray-300 rounded-md">
                <option value="default">default project</option>
              </select>
            </div>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
    </div>
  )
}
