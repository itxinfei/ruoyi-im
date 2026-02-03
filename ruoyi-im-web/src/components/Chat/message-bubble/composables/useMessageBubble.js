/**
 * 消息气泡通用逻辑
 * 处理消息点击、多选、长按等交互
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { parseMessageContent } from '@/utils/message'

export function useMessageBubble(props, emit) {
  const store = useStore()
  const bubbleRef = ref(null)

  // ==================== 计算属性 ====================

  // 是否被选中
  const isSelected = computed(() => {
    return store.state.im.message.selectedMessages.has(props.message.id)
  })

  // 解析消息内容（使用统一的工具函数）
  const parsedContent = computed(() => {
    return parseMessageContent(props.message)
  })

  // 是否有标记
  const hasMarkers = computed(() => {
    return props.message?.markers?.length > 0
  })

  // 是否可以撤回
  const canRecall = computed(() => {
    if (!props.message?.timestamp) return false

    // 检查时间限制
    const recallTimeLimit = store.state.im.settings.chat?.recallTimeLimit || 2
    const messageTime = new Date(props.message.timestamp).getTime()
    const elapsed = Date.now() - messageTime
    const timeLimit = recallTimeLimit * 60 * 1000
    if (elapsed >= timeLimit) return false

    // 单聊：只能撤回自己的消息
    if (props.sessionType === 'PRIVATE') {
      return props.message.isOwn
    }

    // 群聊：群主/管理员可以撤回任何消息，普通成员只能撤回自己的消息
    if (props.sessionType === 'GROUP') {
      const currentUser = store.getters['user/currentUser']
      const memberRole = props.message?.memberRole // 'OWNER', 'ADMIN', 'MEMBER'

      // 如果是自己的消息，可以撤回
      if (props.message.isOwn) return true

      // 如果是群主或管理员，可以撤回任何人的消息
      if (memberRole === 'OWNER' || memberRole === 'ADMIN') {
        return true
      }

      // 普通成员不能撤回别人的消息
      return false
    }

    return false
  })

  // ==================== 交互处理 ====================

  // 处理消息点击
  const handleClick = (event) => {
    if (event.ctrlKey || event.metaKey) {
      // Ctrl + 点击：不连续多选
      toggleSelection()
      event.stopPropagation()
    } else if (event.shiftKey) {
      // Shift + 点击：连续多选
      rangeSelection()
      event.stopPropagation()
    }
  }

  // 切换选中状态
  const toggleSelection = () => {
    store.commit('im/message/TOGGLE_MESSAGE_SELECTION', props.message.id)
    store.commit('im/message/SET_LAST_CLICKED_MESSAGE', props.message.id)
  }

  // 范围选择
  const rangeSelection = () => {
    const currentSession = store.state.im.session?.currentSession
    if (!currentSession) return

    const sessionId = currentSession.id
    const lastClickedId = store.state.im.message?.lastClickedMessageId

    if (!lastClickedId) {
      toggleSelection()
      store.commit('im/message/SET_LAST_CLICKED_MESSAGE', props.message.id)
      return
    }

    if (lastClickedId === props.message.id) {
      toggleSelection()
      return
    }

    const messages = store.state.im.message?.messages?.[sessionId] || []
    if (messages.length === 0) return

    store.commit('im/message/SELECT_MESSAGE_RANGE', {
      sessionId,
      startMessageId: lastClickedId,
      endMessageId: props.message.id
    })

    store.commit('im/message/SET_LAST_CLICKED_MESSAGE', props.message.id)
  }

  // 处理右键菜单命令
  const handleCommand = (cmd) => {
    if (!cmd) return
    if (cmd === 'at') {
      emit('at', props.message)
    } else {
      emit('command', cmd, props.message)
    }
  }

  // 处理重试
  const handleRetry = () => {
    emit('retry', props.message)
  }

  // ==================== 长按处理 ====================

  const LONG_PRESS_DURATION = 500
  let longPressTimer = null
  const isLongPressing = ref(false)

  const handleTouchStart = (e) => {
    longPressTimer = setTimeout(() => {
      isLongPressing.value = true
      emit('long-press', { event: e, message: props.message })
      if (navigator.vibrate) {
        navigator.vibrate(50)
      }
    }, LONG_PRESS_DURATION)
  }

  const handleTouchEnd = () => {
    if (longPressTimer) {
      clearTimeout(longPressTimer)
      longPressTimer = null
    }
    isLongPressing.value = false
  }

  const handleMouseHold = (e) => {
    if (e.button !== 0) return
    longPressTimer = setTimeout(() => {
      isLongPressing.value = true
      emit('long-press', { event: e, message: props.message })
    }, LONG_PRESS_DURATION)
  }

  const handleMouseRelease = () => {
    if (longPressTimer) {
      clearTimeout(longPressTimer)
      longPressTimer = null
    }
    isLongPressing.value = false
  }

  // ==================== 生命周期 ====================

  onUnmounted(() => {
    if (longPressTimer) {
      clearTimeout(longPressTimer)
    }
  })

  return {
    // Refs
    bubbleRef,
    isLongPressing,

    // Computed
    isSelected,
    parsedContent,
    hasMarkers,
    canRecall,

    // Methods
    handleClick,
    toggleSelection,
    rangeSelection,
    handleCommand,
    handleRetry,
    handleTouchStart,
    handleTouchEnd,
    handleMouseHold,
    handleMouseRelease
  }
}
