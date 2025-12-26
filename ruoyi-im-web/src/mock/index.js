/**
 * Mock服务入口文件
 * 统一导出所有mock数据和配置
 */

import { mockUsers, mockSessions, mockMessages, mockGroups, mockSystemConfig } from './mockData'

// 导出所有mock数据
export { mockUsers, mockSessions, mockMessages, mockGroups, mockSystemConfig }

// 导出获取mock数据的函数
export {
  getMockUsers,
  getMockSessions,
  getMockMessages,
  getMockGroups,
  getMockSystemConfig,
} from './mockData'

// 默认导出
export default {
  mockUsers,
  mockSessions,
  mockMessages,
  mockGroups,
  mockSystemConfig,
}

// 如果是开发环境，可以在这里添加mock请求拦截
if (process.env.NODE_ENV === 'development') {
  console.log('Mock服务已启用')
}
