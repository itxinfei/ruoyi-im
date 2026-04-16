<template>
  <div class="chat-window">
    <!-- 1. 聊天头部 -->
    <ChatWindowHeader
      :session="currentSession"
      @show-detail="detailDrawerVisible = true"
      @toggle-selection="toggleSelectionMode"
      @open-search="showGlobalSearch = true"
      @voice-call="handleVoiceCall"
      @video-call="handleVideoCall"
      @header-command="handleHeaderCommand"
    />

    <!-- 1.5 多选模式顶部栏 -->
    <SelectionHeader
      v-if="isSelectionMode"
      :selected-count="selectedMessages.size"
      @select-all="selectAll"
      @cancel="cancelSelection"
      @forward="batchForward"
      @mark-as-read="batchMarkAsRead"
      @favorite="batchFavorite"
      @delete="batchDelete"
    />

    <!-- 2. 消息列表区 -->
    <ChatMessageList
      ref="messageListRef"
      :messages="messages"
      :is-selection-mode="isSelectionMode"
      :selected-messages="selectedMessages"
      @reply="processReply"
      @forward="processForward"
      @recall="processRecall"
      @delete="processDelete"
      @favorite="processFavorite"
      @reaction="processReaction"
      @read-detail="handleReadDetail"
      @edit="handleEdit"
      @scroll-top="handleListScroll"
      @select-message="handleMessageClick"
    />

    <!-- 3. 输入区 -->
    <ChatInputArea
      v-model="currentDraft"
      :session="currentSession"
      :replying-message="replyingMessage"
      :editing-message="editingMessage"
      @send="processSendMessage"
      @clear-reply="replyingMessage = null"
      @edit-save="handleEditSave"
      @edit-cancel="handleEditCancel"
    />

    <!-- 业务侧边栏池：统一使用 ChatDetailDrawer (支持 GROUP 和 PRIVATE) -->
    <ChatDetailDrawer
      v-if="currentSession"
      v-model="detailDrawerVisible"
      :session="currentSession"
    />

    <!-- 通话弹窗 -->
    <CallDialog ref="callDialogRef" :session="currentSession" />

    <!-- 已读详情 -->
    <ReadStatusDrawer v-model="showReadDrawer" :message-id="readDetailMessageId" />

    <!-- 全局搜索 -->
    <GlobalSearch v-model:visible="showGlobalSearch" @select="handleSearchSelect" />

    <!-- 转发对话框 -->
    <ForwardDialog ref="forwardDialogRef" />
  </div>
</template>

<script setup lang="js">
/**
 * ChatWindow.vue (Vuex 路径修复版 + WebSocket 实时消息 + 触顶加载历史)
 */
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { uploadImage, uploadFile } from '@/api/im/file'
import { initiateCall } from '@/api/im/videoCall'
import ChatDetailDrawer from '@/components/Chat/ChatDetailDrawer.vue'
import ChatInputArea from './ChatInputArea.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'
import ReadStatusDrawer from '@/components/im/ReadStatusDrawer.vue'
import GlobalSearch from '@/components/Chat/GlobalSearch.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import ChatWindowHeader from './ChatWindow/ChatWindowHeader.vue'
import SelectionHeader from './ChatWindow/SelectionHeader.vue'
import ChatMessageList from './ChatWindow/ChatMessageList.vue'

const store = useStore()

// 1. WebSocket 集成
const { onMessage, onRead, onCall, connect, cleanup } = useImWebSocket()

// 2. 基础状态
const messageListRef = ref(null)
const callDialogRef = ref(null)
const forwardDialogRef = ref(null)
const detailDrawerVisible = ref(false)
const showReadDrawer = ref(false)
const readDetailMessageId = ref(null)
const replyingMessage = ref(null)
const showGlobalSearch = ref(false)
const isLoadingMore = ref(false) // 是否正在加载更多
const currentDraft = ref('') // 当前会话草稿
const editingMessage = ref(null) // 正在编辑的消息
const isSelectionMode = ref(false) // 多选模式
const selectedMessages = ref(new Set()) // 已选中的消息ID集合

// 3. 数据联动
const currentSession = computed(() => store.state.im?.session?.currentSession)
const messages = computed(() => {
  const sessionId = currentSession.value?.id
  if (!sessionId) return []
  return store.state.im?.message?.messages?.[sessionId] || []
})

// 4. WebSocket 消息处理
const handleNewMessage = (payload) => {
  store.dispatch('im/message/receiveMessage', payload)
  if (payload.conversationId === currentSession.value?.id) {
    nextTick(() => scrollToBottom())
  }
}

// 5. 已读回执处理
const handleReadReceipt = (payload) => {
  const { messageId, conversationId } = payload
  if (conversationId === currentSession.value?.id) {
    store.commit('im/message/UPDATE_MESSAGE', {
      sessionId: conversationId,
      message: { messageId, isRead: true }
    })
  }
}

// 6. 来电处理
const handleIncomingCall = (payload) => {
  if (!callDialogRef.value) return

  const { action, callId, fromUserId, callType, sdp, fromUserName, fromUserAvatar } = payload

  switch (action) {
    case 'call':
    case 'offer':
      // 收到来电/呼叫邀请
      callDialogRef.value.open(callType === 'VIDEO' ? 'video' : 'voice', {
        status: 'incoming',
        callId,
        peerId: fromUserId,
        peerName: fromUserName || '未知',
        peerAvatar: fromUserAvatar || '',
        pendingOffer: sdp
      })
      break

    case 'cancel':
    case 'hangup':
      // 对方取消或挂断
      if (callDialogRef.value?.callId === callId) {
        callDialogRef.value.end()
      }
      break

    case 'reject':
      // 对方拒绝
      if (callDialogRef.value?.callId === callId) {
        callDialogRef.value.end()
        ElMessage.warning('对方拒绝了通话')
      }
      break

    default:
      // 其他信令（offer/answer/candidate）转给 CallDialog 处理
      if (callDialogRef.value && callId === callDialogRef.value?.callId) {
        callDialogRef.value.handleWebRTCSignal(action, payload)
      }
      break
  }
}

// 6. 触顶加载历史消息
const handleListScroll = async (listEl) => {
  if (!listEl || isLoadingMore.value) return
  // 滚动到顶部附近（100px 内）
  if (listEl.scrollTop < 100) {
    const sessionId = currentSession.value?.id
    if (!sessionId) return

    // 记录当前滚动高度
    const oldScrollHeight = listEl.scrollHeight

    // 获取当前消息列表的第一条消息 ID
    const firstMessage = messages.value[0]
    const lastId = firstMessage?.messageId

    if (!lastId) return

    isLoadingMore.value = true
    try {
      await store.dispatch('im/message/loadMessages', {
        sessionId,
        lastMessageId: lastId,
        pageSize: 20,
        isLoadMore: true
      })
      // 加载完成后，保持滚动条位置不变
      nextTick(() => {
        if (listEl) {
          const newScrollHeight = listEl.scrollHeight
          listEl.scrollTop = newScrollHeight - oldScrollHeight
        }
      })
    } finally {
      isLoadingMore.value = false
    }
  }
}

// 7. 初始化
onMounted(() => {
  connect()
  onMessage(handleNewMessage)
  onRead(handleReadReceipt)
  onCall(handleIncomingCall)
  scrollToBottom()
  // 监听滚动到顶部快捷键
  window.addEventListener('main:scroll-to-top', scrollToTop)
})

// 8. 监听当前会话变化（保存旧草稿 + 恢复新草稿）
watch(() => currentSession.value?.id, async (newSessionId, oldSessionId) => {
  // 保存旧会话草稿（仅在切换到新会话时保存）
  if (oldSessionId && newSessionId && oldSessionId !== newSessionId && currentDraft.value) {
    await store.dispatch('im/session/saveDraft', {
      sessionId: oldSessionId,
      content: currentDraft.value
    })
  }

  if (newSessionId) {
    store.dispatch('im/message/loadMessages', { sessionId: newSessionId })
    // 恢复新会话草稿
    const session = store.state.im?.session?.sessions?.find(s => s.id === newSessionId)
    currentDraft.value = session?.draftContent || ''
    nextTick(() => scrollToBottom())
  }
}, { immediate: true })

// 9. 组件卸载时清理
onUnmounted(() => {
  cleanup()
  window.removeEventListener('main:scroll-to-top', scrollToTop)
})

const processReply = (message) => {
  replyingMessage.value = message
}

// 处理转发
const processForward = (message) => {
  forwardDialogRef.value?.open(message)
}

// 处理撤回
const processRecall = async (message) => {
  try {
    await store.dispatch('im/message/recallMessage', message.messageId)
  } catch (error) {
    console.error('撤回消息失败:', error)
    ElMessage.error('撤回消息失败')
  }
}

// 处理删除
const processDelete = async (message) => {
  try {
    await store.dispatch('im/message/deleteMessage', message.messageId)
  } catch (error) {
    console.error('删除消息失败:', error)
    ElMessage.error('删除消息失败')
  }
}

// 处理收藏
const processFavorite = async (message) => {
  const messageId = message.messageId
  const conversationId = currentSession.value?.id

  if (!messageId) {
    console.error('消息ID不存在')
    return
  }

  try {
    if (message.isFavorited) {
      // 取消收藏
      await store.dispatch('im/message/removeFavorite', { messageId, conversationId })
      ElMessage.success('已取消收藏')
    } else {
      // 添加收藏
      await store.dispatch('im/message/addFavorite', { messageId, conversationId })
      ElMessage.success('已添加收藏')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 处理表情回应
const processReaction = async ({ message, emoji }) => {
  const messageId = message.messageId
  if (!messageId) {
    console.error('消息ID不存在')
    return
  }
  try {
    await store.dispatch('im/message/toggleReaction', { messageId, emoji })
  } catch (error) {
    console.error('表情回应失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 处理编辑消息
const handleEdit = (message) => {
  editingMessage.value = message
  currentDraft.value = message.content || ''
  ElMessage.info('编辑模式：修改内容后 Enter 发送，ESC 取消')
}

// 保存编辑
const handleEditSave = async () => {
  if (!editingMessage.value || !currentDraft.value.trim()) return
  const messageId = editingMessage.value.messageId
  try {
    await store.dispatch('im/message/editMessage', {
      messageId,
      content: currentDraft.value.trim()
    })
    ElMessage.success('消息已更新')
    editingMessage.value = null
    currentDraft.value = ''
  } catch (error) {
    console.error('编辑消息失败:', error)
    ElMessage.error('编辑失败，请重试')
  }
}

// 取消编辑
const handleEditCancel = () => {
  editingMessage.value = null
  currentDraft.value = ''
}

const processSendMessage = async (payload) => {
  if (!currentSession.value) return

  let content = payload.content
  const messageType = payload.type || 'TEXT'

  // 如果是图片或文件，先上传获取URL
  if (payload.type === 'IMAGE' || payload.type === 'FILE' || payload.type === 'VOICE' || payload.type === 'VIDEO') {
    const formData = new FormData()
    formData.append('file', payload.file)
    if (payload.fileName) {
      formData.append('fileName', payload.fileName)
    }

    try {
      const api = payload.type === 'IMAGE' ? uploadImage : uploadFile
      const res = await api(formData)
      if (res.code === 200 && res.data) {
        // content 应该是 JSON 字符串，包含 fileUrl, fileName, fileSize 等
        content = JSON.stringify({
          fileUrl: res.data.url,
          fileName: payload.fileName || res.data.fileName || payload.file?.name,
          fileSize: payload.file?.size,
          duration: payload.duration || res.data.duration || null
        })
      } else {
        throw new Error('上传失败')
      }
    } catch (error) {
      console.error('文件上传失败:', error)
      ElMessage.error('文件上传失败')
      return
    }
  }

  // 如果是名片消息，将 card 对象序列化为 JSON
  if (payload.type === 'CARD' && payload.card) {
    content = JSON.stringify(payload.card)
  }

  // 如果是位置消息，将 location 对象序列化为 JSON
  if (payload.type === 'LOCATION' && payload.location) {
    content = JSON.stringify(payload.location)
  }

  const messageData = {
    sessionId: currentSession.value.id,
    content,
    type: messageType,
    replyToMessageId: replyingMessage.value?.messageId || null
  }

  replyingMessage.value = null
  try {
    await store.dispatch('im/message/sendMessage', messageData)
    // 发送成功后清除草稿
    currentDraft.value = ''
    await store.dispatch('im/session/saveDraft', {
      sessionId: currentSession.value.id,
      content: ''
    })
    scrollToBottom()
  } catch (error) {
    console.error('消息发送失败:', error)
    ElMessage.error('消息发送失败，请重试')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    const el = messageListRef.value?.listRef
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

// 滚动到顶部
const scrollToTop = () => {
  nextTick(() => {
    const el = messageListRef.value?.listRef
    if (el) {
      el.scrollTop = 0
    }
  })
}

// 语音通话
const handleVoiceCall = async () => {
  if (!currentSession.value) return
  const peerId = currentSession.value.peerUserId || currentSession.value.targetId
  if (!peerId) {
    ElMessage.warning('无法获取对方ID')
    return
  }
  try {
    const res = await initiateCall({
      calleeId: peerId,
      conversationId: currentSession.value.id,
      callType: 'VOICE'
    })
    if (res.code === 200) {
      callDialogRef.value?.open('voice', {
        status: 'calling',
        callId: res.data.callId,
        peerId,
        peerName: currentSession.value.name,
        peerAvatar: currentSession.value.avatar
      })
    } else {
      throw new Error(res.message || '发起通话失败')
    }
  } catch (error) {
    console.error('发起语音通话失败:', error)
    ElMessage.error(error.message || '发起语音通话失败')
  }
}

// 视频通话
const handleVideoCall = async () => {
  if (!currentSession.value) return
  const peerId = currentSession.value.peerUserId || currentSession.value.targetId
  if (!peerId) {
    ElMessage.warning('无法获取对方ID')
    return
  }
  try {
    const res = await initiateCall({
      calleeId: peerId,
      conversationId: currentSession.value.id,
      callType: 'VIDEO'
    })
    if (res.code === 200) {
      callDialogRef.value?.open('video', {
        status: 'calling',
        callId: res.data.callId,
        peerId,
        peerName: currentSession.value.name,
        peerAvatar: currentSession.value.avatar
      })
    } else {
      throw new Error(res.message || '发起通话失败')
    }
  } catch (error) {
    console.error('发起视频通话失败:', error)
    ElMessage.error(error.message || '发起视频通话失败')
  }
}

// 已读详情
const handleReadDetail = (messageId) => {
  readDetailMessageId.value = messageId
  showReadDrawer.value = true
}

// ========== 头部更多菜单 ==========
const handleHeaderCommand = async (command) => {
  if (!currentSession.value) return

  switch (command) {
    case 'pin':
      // 置顶会话
      await store.dispatch('im/session/togglePinSession', currentSession.value.id)
      ElMessage.success(currentSession.value.isPinned ? '已取消置顶' : '已置顶')
      break

    case 'mute':
      // 消息免打扰
      await store.dispatch('im/session/toggleMuteSession', currentSession.value.id)
      ElMessage.success(currentSession.value.isMuted ? '已开启免打扰' : '已关闭免打扰')
      break

    case 'clear':
      // 清空聊天记录
      try {
        await ElMessageBox.confirm('确定要清空此会话的所有消息吗？此操作不可恢复。', '清空聊天记录', {
          confirmButtonText: '确定清空',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await store.dispatch('im/message/clearMessages', currentSession.value.id)
        ElMessage.success('聊天记录已清空')
      } catch (e) {
        if (e !== 'cancel') console.error('清空失败', e)
      }
      break

    case 'mention':
      // 艾特我的消息 - 滚动到第一条 @ 我的消息
      scrollToMentionMe()
      break

    case 'detail':
      // 会话详情
      detailDrawerVisible.value = true
      break
  }
}

// 滚动到艾特我的消息
const scrollToMentionMe = () => {
  const listEl = messageListRef.value?.listRef
  if (!listEl) return
  const mentionMsg = messages.value.find(msg =>
    msg.content && msg.content.includes(`@${store.state.im?.currentUser?.name || store.state.im?.currentUser?.nickname || ''}`)
  )
  if (mentionMsg) {
    const el = listEl.querySelector(`[data-message-id="${mentionMsg.messageId}"]`)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' })
      ElMessage.success('已定位到艾特消息')
    }
  } else {
    ElMessage.info('没有找到艾特消息')
  }
}

// ========== 多选模式 ==========
const toggleSelectionMode = () => {
  if (isSelectionMode.value) {
    cancelSelection()
  } else {
    isSelectionMode.value = true
  }
}

const cancelSelection = () => {
  isSelectionMode.value = false
  selectedMessages.value.clear()
}

const handleMessageClick = (msg) => {
  if (!isSelectionMode.value) return
  if (selectedMessages.value.has(msg.messageId)) {
    selectedMessages.value.delete(msg.messageId)
  } else {
    selectedMessages.value.add(msg.messageId)
  }
}

const selectAll = () => {
  messages.value.forEach(msg => {
    selectedMessages.value.add(msg.messageId)
  })
}

const batchForward = () => {
  if (selectedMessages.value.size === 0) {
    ElMessage.warning('请先选择消息')
    return
  }
  const selectedMsgs = messages.value.filter(msg => selectedMessages.value.has(msg.messageId))
  forwardDialogRef.value?.openMultiple(selectedMsgs)
  cancelSelection()
}

const batchDelete = async () => {
  if (selectedMessages.value.size === 0) {
    ElMessage.warning('请先选择消息')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedMessages.value.size} 条消息吗？`, '批量删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = Array.from(selectedMessages.value)
    await store.dispatch('im/message/batchDeleteMessagesAction', ids)
    ElMessage.success('批量删除成功')
    cancelSelection()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 批量标记已读
const batchMarkAsRead = async () => {
  if (selectedMessages.value.size === 0) {
    ElMessage.warning('请先选择消息')
    return
  }
  const conversationId = currentSession.value?.id
  if (!conversationId) return

  try {
    const messageIds = Array.from(selectedMessages.value)
    await store.dispatch('im/message/markMessageAsRead', {
      conversationId,
      messageId: messageIds
    })
    ElMessage.success('批量已读成功')
    cancelSelection()
  } catch (error) {
    console.error('批量已读失败:', error)
    ElMessage.error('批量已读失败')
  }
}

// 批量收藏
const batchFavorite = async () => {
  if (selectedMessages.value.size === 0) {
    ElMessage.warning('请先选择消息')
    return
  }
  const conversationId = currentSession.value?.id
  if (!conversationId) return

  try {
    const messageIds = Array.from(selectedMessages.value)
    // 逐条添加收藏
    for (const messageId of messageIds) {
      await store.dispatch('im/message/addFavorite', { messageId, conversationId })
    }
    ElMessage.success('批量收藏成功')
    cancelSelection()
  } catch (error) {
    console.error('批量收藏失败:', error)
    ElMessage.error('批量收藏失败')
  }
}

// 搜索结果跳转
const handleSearchSelect = (res) => {
  showGlobalSearch.value = false
  if (res?.messageId) {
    nextTick(() => scrollToMessage(res.messageId))
  }
}

// 滚动到指定消息
const scrollToMessage = (messageId) => {
  const listEl = messageListRef.value?.listRef
  if (!listEl) return
  // Try both id field names used by the backend
  const el = listEl.querySelector(`[data-message-id="${messageId}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  } else {
    // Fallback: find by index in messages array
    const index = messages.value.findIndex(m => m.messageId === messageId)
    if (index !== -1) {
      const allItems = listEl.querySelectorAll('[data-message-id]')
      if (allItems[index]) {
        allItems[index].scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    }
  }
}
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  background-color: var(--dt-bg-chat);
}

/* 对齐钉钉输入区高度约束: 最低 180px，最高不超过聊天区的 40% */
:deep(.chat-input-wrapper) {
  min-height: var(--dt-input-min-height);
  max-height: 40%;
  margin: 0 var(--dt-chat-gutter) var(--dt-chat-gutter);
  border-radius: var(--dt-radius-2xl);
  overflow: hidden;
  border: 1px solid rgba(23, 26, 29, 0.06);
}

@media (max-width: 960px) {
  :deep(.chat-input-wrapper) {
    margin-left: var(--dt-chat-gutter-compact);
    margin-right: var(--dt-chat-gutter-compact);
    margin-bottom: var(--dt-chat-gutter-compact);
  }
}

@media (max-width: 640px) {
  :deep(.chat-input-wrapper) {
    margin: 0 10px 10px;
    border-radius: var(--dt-radius-2xl);
  }
}
</style>
