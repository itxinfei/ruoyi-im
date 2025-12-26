/**
 * Mock数据文件
 * 提供模拟数据用于开发和测试
 */

// 用户数据
export const mockUsers = [
  {
    id: 1,
    username: 'admin',
    nickname: '管理员',
    avatar: '/static/images/avatar/avatar1.jpg',
    status: 'online',
    role: 'admin',
  },
  {
    id: 2,
    username: 'user1',
    nickname: '测试用户1',
    avatar: '/static/images/avatar/avatar2.jpg',
    status: 'offline',
    role: 'user',
  },
  {
    id: 3,
    username: 'user2',
    nickname: '测试用户2',
    avatar: '/static/images/avatar/avatar3.jpg',
    status: 'busy',
    role: 'user',
  },
]

// 会话数据
export const mockSessions = [
  {
    id: 1,
    type: 'private',
    name: '测试会话1',
    avatar: '/static/images/avatar/avatar1.jpg',
    lastMessage: '你好，这是一条测试消息',
    lastTime: '2024-01-01 12:00:00',
    unreadCount: 3,
    members: [1, 2],
  },
  {
    id: 2,
    type: 'group',
    name: '测试群组',
    avatar: '/static/images/avatar/group.jpg',
    lastMessage: '大家好',
    lastTime: '2024-01-01 11:30:00',
    unreadCount: 0,
    members: [1, 2, 3],
  },
]

// 消息数据
export const mockMessages = [
  {
    id: 1,
    sessionId: 1,
    senderId: 1,
    content: '你好，这是一条测试消息',
    type: 'text',
    timestamp: '2024-01-01 12:00:00',
    status: 'sent',
  },
  {
    id: 2,
    sessionId: 1,
    senderId: 2,
    content: '收到，谢谢！',
    type: 'text',
    timestamp: '2024-01-01 12:01:00',
    status: 'sent',
  },
  {
    id: 3,
    sessionId: 1,
    senderId: 1,
    content: '这是一个图片消息',
    type: 'image',
    url: '/static/images/test.jpg',
    timestamp: '2024-01-01 12:02:00',
    status: 'sent',
  },
]

// 群组数据
export const mockGroups = [
  {
    id: 1,
    name: '测试群组',
    avatar: '/static/images/avatar/group.jpg',
    description: '这是一个测试群组',
    members: 3,
    creatorId: 1,
    createTime: '2024-01-01 10:00:00',
  },
]

// 系统配置
export const mockSystemConfig = {
  appName: 'RuoYi IM',
  version: '1.0.0',
  maxFileSize: 10485760, // 10MB
  allowedFileTypes: ['jpg', 'jpeg', 'png', 'gif', 'mp4', 'mp3', 'pdf', 'doc', 'docx'],
  enableVoice: true,
  enableVideo: true,
  enableFile: true,
}

// 获取模拟用户
export function getMockUsers() {
  return mockUsers
}

// 获取模拟会话
export function getMockSessions() {
  return mockSessions
}

// 获取模拟消息
export function getMockMessages(sessionId) {
  return mockMessages.filter(msg => msg.sessionId === sessionId)
}

// 获取模拟群组
export function getMockGroups() {
  return mockGroups
}

// 获取系统配置
export function getMockSystemConfig() {
  return mockSystemConfig
}

export default {
  mockUsers,
  mockSessions,
  mockMessages,
  mockGroups,
  mockSystemConfig,
  getMockUsers,
  getMockSessions,
  getMockMessages,
  getMockGroups,
  getMockSystemConfig,
}
