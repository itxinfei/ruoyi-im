/**
 * 用户在线状态管理
 * 提供统一的用户在线状态获取方法
 */

import { computed } from 'vue'
import { useStore } from 'vuex'

/**
 * 在线状态常量
 */
export const UserStatus = {
  ONLINE: 'online',
  OFFLINE: 'offline',
  AWAY: 'away',
  BUSY: 'busy'
}

/**
 * Composable：用户在线状态
 * @param {Object} [options] - 配置选项
 * @param {Function} [options.store] - Vuex store 实例，默认使用 useStore()
 * @returns {Object} 用户状态相关方法
 */
export function useUserStatus(options = {}) {
  const store = options.store || useStore()

  /**
   * 获取用户在线状态
   * @param {number|string} userId - 用户ID
   * @returns {boolean} 用户是否在线
   */
  const isUserOnline = userId => {
    if (!userId) {return false}
    const userStatus = store.state.im?.contact?.userStatus || {}
    return userStatus[userId] === UserStatus.ONLINE
  }

  /**
   * 获取用户状态文本
   * @param {number|string} userId - 用户ID
   * @returns {string} 状态文本
   */
  const getUserStatusText = userId => {
    if (!userId) {return '离线'}
    const userStatus = store.state.im?.contact?.userStatus || {}
    const status = userStatus[userId]
    switch (status) {
      case UserStatus.ONLINE:
        return '在线'
      case UserStatus.AWAY:
        return '离开'
      case UserStatus.BUSY:
        return '忙碌'
      default:
        return '离线'
    }
  }

  /**
   * 获取所有在线用户ID列表
   * @returns {Array} 在线用户ID数组
   */
  const getOnlineUsers = () => {
    const userStatus = store.state.im?.contact?.userStatus || {}
    return Object.keys(userStatus).filter(userId => userStatus[userId] === UserStatus.ONLINE)
  }

  /**
   * 根据会话判断用户是否在线
   * @param {Object} session - 会话对象
   * @returns {boolean} 用户是否在线
   */
  const isSessionUserOnline = session => {
    if (!session || session.type === 'GROUP') {return false}

    const userId = session.targetId
    if (userId && store.state.im?.contact?.userStatus?.[userId]) {
      return store.state.im.contact.userStatus[userId] === UserStatus.ONLINE
    }

    return session.peerOnline ?? false
  }

  /**
   * 创建计算属性：会话用户在线状态
   * @param {Object} session - 会话对象
   * @returns {import('vue').ComputedRef<boolean>}
   */
  const createSessionOnlineComputed = session => {
    return computed(() => isSessionUserOnline(session))
  }

  return {
    UserStatus,
    isUserOnline,
    getUserStatusText,
    getOnlineUsers,
    isSessionUserOnline,
    createSessionOnlineComputed
  }
}

/**
 * 全局状态映射（用于批量查询）
 * @param {Array} userIds - 用户ID数组
 * @returns {Object} 用户ID到在线状态的映射
 */
export function mapUserOnlineStatus(userIds) {
  const store = useStore()
  const userStatus = store.state.im?.contact?.userStatus || {}
  const result = {}

  userIds.forEach(userId => {
    result[userId] = userStatus[userId] === UserStatus.ONLINE
  })

  return result
}
