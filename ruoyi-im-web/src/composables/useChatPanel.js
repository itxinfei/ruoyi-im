/**
 * ChatPanel 状态管理 Composable
 *
 * 职责：
 * - 管理聊天面板的核心状态
 * - 处理消息发送、加载更多
 * - 管理各种弹窗的显示状态
 *
 * 使用方式：
 * ```js
 * const {
 *   messages, loading, noMore,
 *   loadHistory, loadMore,
 *   dialogStates,
 *   handleSend
 * } = useChatPanel({ session, store })
 * ```
 */
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { useMessageTransformation } from './useMessageTransformation'

export function useChatPanel(options = {}) {
  const {
    session = ref(null),
    storeValue = null,
    msgListRef = ref(null)
  } = options

  const store = storeValue || useStore()
  const currentUser = computed(() => store.getters['user/currentUser'])

  // 使用消息转换工具
  const { transformMsg, createTempMessage, tempMessageFactories } = useMessageTransformation({
    currentUser
  })

  // ==================== 核心状态 ====================

  const messages = ref([])
  const loading = ref(false)
  const sending = ref(false)
  const noMore = ref(false)

  // ==================== 弹窗状态 ====================

  const dialogStates = ref({
    showGroupDetail: false,
    showVoiceCall: false,
    showVideoCall: false,
    showChatHistory: false,
    showAnnouncementDialog: false,
    showSearchPanel: false,
    showChatSearch: false,
    showFilesPanel: false,
    showExportDialog: false,
    showGroupFilesPanel: false,
    showCombineDetail: false
  })

  // ==================== 多选状态 ====================

  const isMultiSelectModeActive = ref(false)

  // ==================== 置顶面板 ====================

  const showPinnedPanel = ref(false)
  const pinnedCount = computed(() => messages.value.filter(m => m.isPinned).length)

  // ==================== 图片预览 ====================

  const showImagePreview = ref(false)
  const imagePreviewIndex = ref(0)

  const conversationImages = computed(() => {
    return messages.value
      .filter(m => {
        if (m.type !== 'IMAGE') {return false}
        try {
          const content = typeof m.content === 'string' ? JSON.parse(m.content) : m.content
          return content && (content.url || content.imageUrl)
        } catch {
          return false
        }
      })
      .map(m => {
        try {
          const content = typeof m.content === 'string' ? JSON.parse(m.content) : m.content
          return content.url || content.imageUrl
        } catch {
          return ''
        }
      })
      .filter(url => url)
  })

  // ==================== 消息操作 ====================

  /**
   * 加载历史消息
   */
  const loadHistory = async () => {
    if (!session.value?.id) {return}

    loading.value = true
    noMore.value = false

    try {
      const res = await store.dispatch('im/message/loadMessages', {
        sessionId: session.value.id,
        pageSize: 50
      })

      messages.value = (res || []).map(m => transformMsg(m))
    } finally {
      loading.value = false
      msgListRef.value?.scrollToBottom()
    }
  }

  /**
   * 加载更多消息
   */
  const loadMore = async () => {
    if (loading.value || noMore.value) {return}

    const firstMsg = messages.value[0]
    if (!firstMsg) {return}

    loading.value = true
    const oldHeight = msgListRef.value?.$refs.listRef.scrollHeight

    try {
      const newMsgs = await store.dispatch('im/message/loadMessages', {
        sessionId: session.value.id,
        lastMessageId: firstMsg.id,
        pageSize: 20,
        isLoadMore: true
      })

      if (newMsgs && newMsgs.length > 0) {
        messages.value = [...newMsgs, ...messages.value]
        msgListRef.value?.maintainScroll(oldHeight)
      } else {
        noMore.value = true
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 发送消息（核心方法）
   * @param {string} content - 消息内容
   * @param {Object} options - 选项 { replyToMessageId, type }
   */
  const send = async (content, sendOptions = {}) => {
    const { type = 'TEXT', replyToMessageId = null } = sendOptions

    // 创建临时消息
    const tempMsg = tempMessageFactories.text(content)
    if (replyToMessageId) {
      tempMsg.replyToMessageId = replyToMessageId
    }

    messages.value.push(tempMsg)
    msgListRef.value?.scrollToBottom()

    try {
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: session.value.id,
        type,
        content,
        replyToMessageId
      })

      // 更新消息状态
      const index = messages.value.findIndex(m => m.id === tempMsg.id)
      if (index !== -1) {
        messages.value.splice(index, 1, { ...transformMsg(msg), status: null })
      }
    } catch (error) {
      const index = messages.value.findIndex(m => m.id === tempMsg.id)
      if (index !== -1) {
        messages.value[index].status = 'failed'
      }
      console.error('发送失败', error)
      throw error
    }
  }

  /**
   * 重试发送失败的消息
   */
  const retry = async message => {
    if (message.status !== 'failed') {return}

    message.status = 'sending'

    try {
      const res = await store.dispatch('im/message/sendMessage', {
        sessionId: session.value.id,
        type: message.type,
        content: typeof message.content === 'object' ? JSON.stringify(message.content) : message.content
      })

      const realMsg = transformMsg(res)
      Object.assign(message, { ...realMsg, status: null })
    } catch (error) {
      message.status = 'failed'
      throw error
    }
  }

  /**
   * 删除消息
   */
  const remove = async messageId => {
    try {
      await store.dispatch('im/message/deleteMessage', messageId)
      const index = messages.value.findIndex(m => m.id === messageId)
      if (index !== -1) {
        messages.value.splice(index, 1)
      }
    } catch (error) {
      console.error('删除失败', error)
      throw error
    }
  }

  /**
   * 撤回消息
   */
  const recall = async messageId => {
    try {
      await store.dispatch('im/message/recallMessage', messageId)
      const index = messages.value.findIndex(m => m.id === messageId)
      if (index !== -1) {
        messages.value[index].type = 'RECALLED'
        messages.value[index].content = '消息已撤回'
      }
    } catch (error) {
      console.error('撤回失败', error)
      throw error
    }
  }

  /**
   * 编辑消息
   */
  const edit = async (messageId, newContent) => {
    try {
      await store.dispatch('im/message/editMessage', {
        messageId,
        content: newContent
      })

      const index = messages.value.findIndex(m => m.id === messageId)
      if (index !== -1) {
        messages.value[index].content = newContent
        messages.value[index].isEdited = true
      }
    } catch (error) {
      console.error('编辑失败', error)
      throw error
    }
  }

  // ==================== 弹窗控制 ====================

  /**
   * 切换弹窗状态
   */
  const toggleDialog = (dialogName, forceState) => {
    if (Object.hasOwn(dialogStates.value, dialogName)) {
      dialogStates.value[dialogName] = forceState ?? !dialogStates.value[dialogName]
    }
  }

  /**
   * 关闭所有弹窗
   */
  const closeAllDialogs = () => {
    Object.keys(dialogStates.value).forEach(key => {
      dialogStates.value[key] = false
    })
  }

  // ==================== 多选操作 ====================

  const selectedMessages = computed(() => store.getters['im/message/selectedMessageList'])

  const toggleMultiSelect = active => {
    isMultiSelectModeActive.value = active
    if (!active) {
      store.commit('im/message/CLEAR_MESSAGE_SELECTION')
    }
  }

  const clearSelection = () => {
    store.commit('im/message/CLEAR_MESSAGE_SELECTION')
    isMultiSelectModeActive.value = false
  }

  // ==================== 监听会话变化 ====================

  watch(() => session.value, () => {
    messages.value = []
    loadHistory()
  })

  // ==================== WebSocket 集成 ====================

  /**
   * 处理新消息
   */
  const handleNewMessage = msg => {
    if (msg.conversationId === session.value?.id) {
      const transformedMsg = transformMsg(msg)
      messages.value.push(transformedMsg)
      msgListRef.value?.scrollToBottom()
      return transformedMsg
    }
    return null
  }

  /**
   * 处理消息状态更新
   */
  const handleMessageStatus = data => {
    if (data.conversationId !== session.value?.id) {return}

    const index = messages.value.findIndex(m => m.id === data.messageId)
    if (index !== -1) {
      const statusMap = {
        0: 'sending',
        1: 'sending',
        2: null,
        3: 'read',
        4: 'failed'
      }
      const sendStatus = parseInt(data.sendStatus)
      messages.value[index].status = statusMap[sendStatus] ?? null
    }
  }

  return {
    // 状态
    messages,
    loading,
    sending,
    noMore,
    dialogStates,
    isMultiSelectModeActive,
    showPinnedPanel,
    pinnedCount,
    showImagePreview,
    imagePreviewIndex,
    conversationImages,
    selectedMessages,

    // 消息操作
    loadHistory,
    loadMore,
    send,
    retry,
    remove,
    recall,
    edit,

    // 弹窗控制
    toggleDialog,
    closeAllDialogs,

    // 多选操作
    toggleMultiSelect,
    clearSelection,

    // WebSocket 处理
    handleNewMessage,
    handleMessageStatus,

    // 工具方法
    transformMsg,
    createTempMessage
  }
}
