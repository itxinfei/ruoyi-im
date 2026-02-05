/**
 * 消息状态管理
 * 处理发送状态、已读回执、撤回倒计时等
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'

export function useMessageStatus(props) {
  const store = useStore()

  // ==================== 状态相关 ====================

  // 撤回倒计时
  const recallRemainingSeconds = ref(0)
  let recallTimer = null

  // 更新撤回倒计时
  const updateRecallCountdown = () => {
    if (!props.message?.timestamp) {
      recallRemainingSeconds.value = 0
      return
    }

    const recallTimeLimit = store.state.im.settings.chat?.recallTimeLimit || 2
    const messageTime = new Date(props.message.timestamp).getTime()
    const elapsed = Date.now() - messageTime
    const timeLimit = recallTimeLimit * 60 * 1000
    const remaining = Math.max(0, timeLimit - elapsed)
    recallRemainingSeconds.value = Math.ceil(remaining / 1000)
  }

  // 格式化撤回剩余时间
  const recallTimeDisplay = computed(() => {
    const seconds = recallRemainingSeconds.value
    if (seconds <= 0) {return ''}
    if (seconds < 60) {return `${seconds}秒`}
    const minutes = Math.floor(seconds / 60)
    const secs = seconds % 60
    return secs > 0 ? `${minutes}分${secs}秒` : `${minutes}分钟`
  })

  // ========== 发送状态判断 ==========

  /**
   * 获取消息的发送状态
   * 优先使用 sendStatus 字段，其次使用 status 字段（兼容旧代码）
   */
  const messageStatus = computed(() => {
    // 优先使用 sendStatus（后端 SendStatus 枚举）
    if (props.message?.sendStatus !== undefined) {
      return props.message.sendStatus
    }
    // 兼容 status 字段（字符串）
    if (props.message?.status !== undefined) {
      const statusMap = {
        'sending': 1,  // SENDING
        'sent': 2,      // DELIVERED
        'read': 3,      // READ
        'failed': 4     // FAILED
      }
      return statusMap[props.message.status] || 2
    }
    // 默认为已送达
    return 2
  })

  // 是否显示发送中状态
  const isSending = computed(() => {
    const status = messageStatus.value
    return status === 0 || status === 1 // PENDING or SENDING
  })

  // 是否显示发送失败状态
  const isFailed = computed(() => {
    return messageStatus.value === 4 // FAILED
  })

  // 是否已送达/已读
  const isDelivered = computed(() => {
    const status = messageStatus.value
    return status === 2 || status === 3 // DELIVERED or READ
  })

  // 是否已读
  const isRead = computed(() => {
    return messageStatus.value === 3 || props.message?.readCount > 0
  })

  // 状态文本
  const statusText = computed(() => {
    if (isSending.value) {return '发送中'}
    if (isFailed.value) {return '发送失败'}
    if (isRead.value) {return '已读'}
    if (isDelivered.value) {return '已送达'}
    return '未读'
  })

  // ==================== 生命周期 ====================

  onMounted(() => {
    updateRecallCountdown()
    recallTimer = setInterval(updateRecallCountdown, 1000)
  })

  onUnmounted(() => {
    if (recallTimer) {
      clearInterval(recallTimer)
      recallTimer = null
    }
  })

  return {
    // State
    recallRemainingSeconds,

    // Computed
    recallTimeDisplay,
    isSending,
    isFailed,
    isRead,
    isDelivered,
    statusText,
    messageStatus  // 导出发送状态值
  }
}
