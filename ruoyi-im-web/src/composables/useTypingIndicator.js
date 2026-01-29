/**
 * 输入状态指示器 Composable
 *
 * 职责：
 * - 管理正在输入的用户列表
 * - 发送和停止自己的输入状态
 * - 防抖处理避免频繁发送
 *
 * 使用方式：
 * ```js
 * const {
 *   typingUsers,
 *   sendMyTypingStatus,
 *   sendMyStopTypingStatus,
 *   handleTypingEvent
 * } = useTypingIndicator({ sessionId, currentUser })
 * ```
 */
import { ref } from 'vue'

// 防抖配置
const TYPING_DEBOUNCE = 500 // 500ms 防抖
const TYPING_DURATION = 5000 // 输入状态显示时长

export function useTypingIndicator(options = {}) {
  const {
    sessionId = ref(null),
    currentUser = ref(null),
    sendTyping = null,
    sendStopTyping = null
  } = options

  // 正在输入的用户列表
  const typingUsers = ref([])

  // 内部计时器存储
  const typingTimers = {} // userId -> timerId
  let sendTypingTimer = null
  let lastTypingSendTime = 0

  /**
   * 添加正在输入的用户
   * @param {string} userId - 用户ID
   * @param {string} userName - 用户名
   */
  const addTypingUser = (userId, userName) => {
    // 清除该用户的旧定时器
    if (typingTimers[userId]) {
      clearTimeout(typingTimers[userId])
    }

    // 添加到列表（去重）
    if (!typingUsers.value.find(u => u.userId === userId)) {
      typingUsers.value.push({ userId, userName })
    }

    // 设置过期定时器
    typingTimers[userId] = setTimeout(() => {
      removeTypingUser(userId)
      delete typingTimers[userId]
    }, TYPING_DURATION)
  }

  /**
   * 移除正在输入的用户
   * @param {string} userId - 用户ID
   */
  const removeTypingUser = (userId) => {
    const index = typingUsers.value.findIndex(u => u.userId === userId)
    if (index !== -1) {
      typingUsers.value.splice(index, 1)
    }
  }

  /**
   * 清空输入用户列表
   */
  const clearTypingUsers = () => {
    // 清除所有定时器
    Object.values(typingTimers).forEach(timer => clearTimeout(timer))
    typingUsers.value = []
  }

  /**
   * 发送自己的输入状态（防抖）
   */
  const sendMyTypingStatus = () => {
    // 只在单聊时发送输入状态
    if (!sessionId.value) return

    // 清除之前的定时器
    if (sendTypingTimer) {
      clearTimeout(sendTypingTimer)
    }

    // 防抖：延迟发送
    sendTypingTimer = setTimeout(() => {
      if (sendTyping) {
        sendTyping(sessionId.value)
      }
    }, TYPING_DEBOUNCE)
  }

  /**
   * 发送停止输入状态
   */
  const sendMyStopTypingStatus = () => {
    if (!sessionId.value) return

    if (sendTypingTimer) {
      clearTimeout(sendTypingTimer)
      sendTypingTimer = null
    }

    if (sendStopTyping) {
      sendStopTyping(sessionId.value)
    }
  }

  /**
   * 处理输入事件（带输入内容判断）
   * @param {string} content - 输入内容
   */
  const handleInput = (content) => {
    if (content && content.trim().length > 0) {
      sendMyTypingStatus()
    } else {
      sendMyStopTypingStatus()
    }
  }

  /**
   * 处理来自 WebSocket 的输入事件
   * @param {Object} data - 事件数据 { conversationId, userId, userName }
   */
  const handleTypingEvent = (data) => {
    // 过滤：不是当前会话或自己发出的
    if (data.conversationId !== sessionId.value) return
    if (data.userId === currentUser.value?.id) return

    addTypingUser(data.userId, data.userName || data.senderName)
  }

  /**
   * 切换会话时清理
   */
  const cleanup = () => {
    clearTypingUsers()
    if (sendTypingTimer) {
      clearTimeout(sendTypingTimer)
      sendTypingTimer = null
    }
  }

  return {
    // 状态
    typingUsers,

    // 发送状态
    sendMyTypingStatus,
    sendMyStopTypingStatus,

    // 事件处理
    handleInput,
    handleTypingEvent,

    // 工具方法
    addTypingUser,
    removeTypingUser,
    clearTypingUsers,
    cleanup
  }
}
