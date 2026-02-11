/**
 * 输入状态管理 Composable
 *
 * 职责：
 * - 发送本地用户的 typing/stop_typing 状态（防抖）
 * - 管理远端用户的输入状态列表
 * - 处理输入事件
 * - 清理定时器
 */

import { ref, computed, onUnmounted } from 'vue'
import { useImWebSocket } from '@/composables/useImWebSocket'

const TYPING_DEBOUNCE = 500 // 500ms 防抖
const TYPING_TIMEOUT = 5000 // 5s 后移除远端输入状态

export function useChatTyping(sessionId) {
  const { sendTyping, sendStopTyping, onTyping } = useImWebSocket()

  const currentSessionId = computed(() => sessionId?.value ?? sessionId)

  // 远端用户输入状态列表
  const typingUsers = ref([])
  const typingTimers = {}

  // 本地防抖定时器
  let sendTypingTimer = null

  /**
   * 发送正在输入状态（防抖）
   */
  const sendMyTypingStatus = () => {
    const sid = currentSessionId.value
    if (!sid) { return }

    if (sendTypingTimer) {
      clearTimeout(sendTypingTimer)
    }

    sendTypingTimer = setTimeout(() => {
      sendTyping(sid)
    }, TYPING_DEBOUNCE)
  }

  /**
   * 发送停止输入状态
   */
  const sendMyStopTypingStatus = () => {
    const sid = currentSessionId.value
    if (!sid) { return }

    if (sendTypingTimer) {
      clearTimeout(sendTypingTimer)
      sendTypingTimer = null
    }

    sendStopTyping(sid)
  }

  /**
   * 处理输入事件
   */
  const handleInput = content => {
    if (content && content.trim().length > 0) {
      sendMyTypingStatus()
    } else {
      sendMyStopTypingStatus()
    }
  }

  /**
   * 处理远端用户输入状态
   */
  const handleRemoteTyping = (userId, userName) => {
    // 清除旧定时器
    if (typingTimers[userId]) {
      clearTimeout(typingTimers[userId])
    }

    // 添加到正在输入列表
    if (!typingUsers.value.find(u => u.userId === userId)) {
      typingUsers.value.push({ userId, userName })
    }

    // 设置超时后移除
    typingTimers[userId] = setTimeout(() => {
      const index = typingUsers.value.findIndex(u => u.userId === userId)
      if (index !== -1) {
        typingUsers.value.splice(index, 1)
      }
      delete typingTimers[userId]
    }, TYPING_TIMEOUT)
  }

  /**
   * 初始化 typing 事件监听
   * @param {Object} currentUser - 当前用户（用于忽略自己的 typing）
   */
  const initTypingListener = currentUser => {
    onTyping(data => {
      const sid = currentSessionId.value
      if (data.conversationId !== sid) { return }
      if (data.userId === (currentUser.value?.id || currentUser?.id)) { return }

      handleRemoteTyping(data.userId, data.userName || data.senderName)
    })
  }

  /**
   * 重置输入状态（会话切换时调用）
   */
  const resetTypingState = () => {
    Object.keys(typingTimers).forEach(uid => {
      clearTimeout(typingTimers[uid])
      delete typingTimers[uid]
    })
    typingUsers.value = []
  }

  /**
   * 清理所有定时器
   */
  const cleanup = () => {
    if (sendTypingTimer) {
      clearTimeout(sendTypingTimer)
      sendTypingTimer = null
    }
    resetTypingState()
  }

  return {
    typingUsers,
    sendMyTypingStatus,
    sendMyStopTypingStatus,
    handleInput,
    handleRemoteTyping,
    initTypingListener,
    resetTypingState,
    cleanup
  }
}
