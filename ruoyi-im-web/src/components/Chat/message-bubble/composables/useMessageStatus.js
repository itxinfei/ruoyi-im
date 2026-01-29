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
    if (seconds <= 0) return ''
    if (seconds < 60) return `${seconds}秒`
    const minutes = Math.floor(seconds / 60)
    const secs = seconds % 60
    return secs > 0 ? `${minutes}分${secs}秒` : `${minutes}分钟`
  })

  // 是否显示发送中状态
  const isSending = computed(() => {
    return ['sending', 'uploading'].includes(props.message?.status)
  })

  // 是否显示发送失败状态
  const isFailed = computed(() => {
    return props.message?.status === 'failed'
  })

  // 是否已读
  const isRead = computed(() => {
    return props.message?.readCount > 0
  })

  // 状态文本
  const statusText = computed(() => {
    if (isSending.value) return '发送中'
    if (isFailed.value) return '发送失败'
    if (isRead.value) return '已读'
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
    statusText
  }
}
