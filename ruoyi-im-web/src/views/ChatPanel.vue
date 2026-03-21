<template>
  <div class="chat-panel-container">
    <!-- 1. 精致欢迎页 -->
    <ImEmpty
      v-if="!session"
      title="高效办公，即时协同"
      description="对标钉钉 8.2 的企业级通讯体验"
      action-text="发起群聊"
      @action="handleOpenCreateGroup"
    >
      <template #icon>
        <div class="welcome-logo-box">
          IM
        </div>
      </template>
    </ImEmpty>

    <!-- 2. 聊天主体 -->
    <template v-else>
      <div class="chat-main-canvas">
        <div class="chat-main-canvas__body">
          <!-- 头部 (强制 Key 刷新，解决重构失效) -->
          <ChatHeader
            :key="session.id"
            :session="session"
            :is-typing="isTyping"
            @toggle-sidebar="handleHeaderClick"
            @show-profile="handleShowProfile"
            @voice-call="handleVoiceCall"
            @video-call="handleVideoCall"
            @search="handleOpenSearch"
            @files="handleOpenFiles"
            @pin="handlePinSession"
            @clear="handleClearMessages"
          />

          <!-- 消息流 -->
          <MessageList
            ref="msgListRef"
            :messages="messages"
            :loading="loading"
            :session-type="session?.type"
            :conversation-id="session?.id"
            :jumping="jumpLoading"
            :highlighted-id="highlightedId"
            @command="handleMessageCommand"
            @load-more="handleLoadMore"
            @show-user="handleShowUserProfile"
            @retry="handleRetryMessage"
            @jump="handleJumpToMessage"
          />

          <!-- 输入框 -->
          <footer class="chat-input-area">
            <MessageInput
              ref="messageInputRef"
              :session="session"
              :sending="sending"
              :editing-message="editingMessage"
              @send="handleSend"
              @upload-file="handleUploadFile"
              @upload-image="handleUploadImage"
              @upload-batch="handleBatchUpload"
              @typing="handleInputTyping"
              @edit-save="handleEditSave"
              @edit-cancel="handleEditCancel"
            />
          </footer>
        </div>
      </div>
    </template>

    <!-- 3. 弹窗群 (严格身份校验) -->
    <UserProfileDialog
      v-model="showUserDetail"
      :user-id="activeUserId"
      @start-call="handleStartCallFromProfile"
    />

    <GroupProfileDialog
      v-model="showGroupDetail"
      :group-id="session?.type === 'GROUP' ? session?.targetId : null"
      @show-user="handleShowUserProfile"
    />

    <CallDialog ref="callDialogRef" :session="session" />
    <GlobalSearch v-model:visible="showGlobalSearch" @select="handleSearchSelect" />
    <ForwardDialog ref="forwardDialogRef" />
    <ReadStatusDrawer v-model="showReadDrawer" :message-id="readDetailMessageId" />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInput.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import GroupProfileDialog from '@/components/Contacts/GroupProfileDialog.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'
import ImEmpty from '@/components/Common/ImEmpty.vue'
import GlobalSearch from '@/components/Chat/GlobalSearch.vue'
import ReadStatusDrawer from '@/components/im/ReadStatusDrawer.vue'
import { uploadImage, uploadFile, batchUploadFiles } from '@/api/im/file'
import { initiateCall } from '@/api/im/videoCall'
import { toggleMessageReaction, editMessage } from '@/api/im/message'
import { useImWebSocket } from '@/composables/useImWebSocket'

const props = defineProps({ session: Object })
const emit = defineEmits(['show-user', 'create-group', 'toggle-detail'])
const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])
const messages = ref([])
const loading = ref(false)
const sending = ref(false)

const showUserDetail = ref(false)
const showGroupDetail = ref(false)
const showReadDrawer = ref(false)
const readDetailMessageId = ref(null)
const activeUserId = ref(null)
const showGlobalSearch = ref(false)
const editingMessage = ref(null)
const jumpLoading = ref(false)
const highlightedId = ref(null)

const msgListRef = ref(null)
const messageInputRef = ref(null)
const callDialogRef = ref(null)
const forwardDialogRef = ref(null)
const isTyping = ref(false) // 对方正在输入状态
let typingTimeout = null

const { sendMessage, onCall, onTyping } = useImWebSocket()

const loadHistory = async () => {
  if (!props.session?.id) return
  loading.value = true
  try {
    const res = await store.dispatch('im/message/loadMessages', { sessionId: props.session.id, pageSize: 50 })
    messages.value = (res || []).map(m => ({ ...m, isOwn: m.senderId === currentUser.value?.id || m.isSelf }))
  } catch (error) {
    console.error('加载历史消息失败:', error)
    ElMessage.error('加载历史消息失败，请重试')
  } finally {
    loading.value = false
    nextTick(() => {
      const jumpId = getPendingJump(props.session.id)
      if (jumpId) {
        msgListRef.value?.scrollToMessage(jumpId)
        highlightedId.value = jumpId
        setTimeout(() => { highlightedId.value = null }, 2000)
      } else {
        msgListRef.value?.scrollToBottom()
      }
    })
  }
}

/**
 * 处理正在输入事件
 */
const handleTypingEvent = (data) => {
  // 检查是否是当前会话
  if (data.conversationId === props.session?.id) {
    isTyping.value = true

    // 清除之前的超时
    if (typingTimeout) {
      clearTimeout(typingTimeout)
    }

    // 设置 3 秒后自动消退
    typingTimeout = setTimeout(() => {
      isTyping.value = false
    }, 3000)
  }
}

/**
 * 发送正在输入信号
 */
const handleInputTyping = () => {
  if (!props.session?.id) return

  // 通过 WebSocket 发送正在输入信号
  sendMessage({
    type: 'typing',
    data: {
      conversationId: props.session.id,
      targetId: props.session.targetId
    }
  })
}

/**
 * 处理通话事件
 */
const handleCallEvent = (data) => {
  if (!callDialogRef.value) return

  // 幂等检查：避免重复处理相同的 callId
  const currentCallId = callDialogRef.value?.callId
  if (currentCallId && currentCallId !== data.callId) {
    // 如果已有通话在进行，且不是当前callId，先结束当前通话
    callDialogRef.value?.end()
  }

  switch (data.action) {
    case 'offer':
      // 收到通话邀请
      ElMessage.info(`${data.callerName} 邀请你进行${data.type === 'video' ? '视频' : '语音'}通话`)
      callDialogRef.value?.open(data.type === 'video' ? 'video' : 'voice', {
        status: 'incoming',
        callId: data.callId,
        peerId: data.callerId,
        peerName: data.callerName,
        peerAvatar: data.callerAvatar,
        pendingOffer: data.sdp
      })
      break

    case 'answer':
      // 对方接听了通话
      ElMessage.success('对方已接听')
      callDialogRef.value?.handleWebRTCSignal('answer', data)
      break

    case 'candidate':
      // 收到 ICE 候选者
      callDialogRef.value?.handleWebRTCSignal('candidate', data)
      break

    case 'reject':
      // 对方拒绝通话
      ElMessage.info('对方拒绝了通话')
      callDialogRef.value?.end()
      break

    case 'cancel':
      // 对方取消通话
      ElMessage.info('对方取消了通话')
      callDialogRef.value?.end()
      break

    case 'hangup':
      // 对方挂断
      ElMessage.info('对方已挂断')
      callDialogRef.value?.end()
      break
  }
}

const handleHeaderClick = () => {
  if (!props.session) return
  emit('toggle-detail')
}

const handleShowProfile = () => {
  emit('toggle-detail')
}

const handleShowUserProfile = (userId) => {
  activeUserId.value = userId
  showUserDetail.value = true
}

const handleVoiceCall = async (s) => {
  const peerId = s.peerUserId || s.targetId
  if (!peerId) {
    ElMessage.warning('无法获取对方ID')
    return
  }

  try {
    // 先通过后端 API 发起通话
    const res = await initiateCall({
      calleeId: peerId,
      conversationId: props.session?.id,
      callType: 'VOICE'
    })

    if (res.code === 200) {
      // 打开通话对话框
      callDialogRef.value?.open('voice', {
        status: 'calling',
        callId: res.data.callId,
        peerId,
        peerName: s.name,
        peerAvatar: s.avatar
      })
    } else {
      throw new Error(res.message || '发起通话失败')
    }
  } catch (error) {
    console.error('发起语音通话失败:', error)
    ElMessage.error(error.message || '发起语音通话失败')
  }
}

const handleVideoCall = async (s) => {
  const peerId = s.peerUserId || s.targetId
  if (!peerId) {
    ElMessage.warning('无法获取对方ID')
    return
  }

  try {
    // 先通过后端 API 发起通话
    const res = await initiateCall({
      calleeId: peerId,
      conversationId: props.session?.id,
      callType: 'VIDEO'
    })

    if (res.code === 200) {
      // 打开通话对话框
      callDialogRef.value?.open('video', {
        status: 'calling',
        callId: res.data.callId,
        peerId,
        peerName: s.name,
        peerAvatar: s.avatar
      })
    } else {
      throw new Error(res.message || '发起通话失败')
    }
  } catch (error) {
    console.error('发起视频通话失败:', error)
    ElMessage.error(error.message || '发起视频通话失败')
  }
}

const handleStartCallFromProfile = async (p) => {
  try {
    // 先通过后端 API 发起通话
    const res = await initiateCall({
      calleeId: p.user.userId,
      callType: p.type === 'video' ? 'VIDEO' : 'VOICE'
    })

    if (res.code === 200) {
      // 打开通话对话框
      callDialogRef.value?.open(p.type === 'video' ? 'video' : 'voice', {
        status: 'calling',
        callId: res.data.callId,
        peerId: p.user.userId,
        peerName: p.user.nickname,
        peerAvatar: p.user.avatar
      })
    } else {
      throw new Error(res.message || '发起通话失败')
    }
  } catch (error) {
    console.error('发起通话失败:', error)
    ElMessage.error(error.message || '发起通话失败')
  }
}

const handleOpenSearch = () => { showGlobalSearch.value = true }
const handleSearchSelect = (res) => { if (res.messageId) msgListRef.value?.scrollToMessage(res.messageId) }

const findMessageIndex = (messageId) => {
  const idStr = String(messageId)
  return messages.value.findIndex(m => String(m.messageId || m.id) === idStr)
}

const loadMoreForJump = async () => {
  if (loading.value || !props.session?.id) return []
  const lastMessage = messages.value.length > 0 ? messages.value[0] : null
  const lastMessageId = lastMessage?.id || lastMessage?.messageId
  if (!lastMessageId) return []

  loading.value = true
  try {
    const res = await store.dispatch('im/message/loadMessages', {
      sessionId: props.session.id,
      lastMessageId,
      pageSize: 50,
      isLoadMore: true
    })

    if (res && res.length > 0) {
      const newMessages = (res || []).map(m => ({
        ...m,
        isOwn: m.senderId === currentUser.value?.id || m.isSelf
      }))
      messages.value = [...newMessages, ...messages.value]
    }
    return res || []
  } finally {
    loading.value = false
  }
}

const handleJumpToMessage = async (messageId) => {
  if (!messageId || jumpLoading.value) return
  jumpLoading.value = true

  try {
    let idx = findMessageIndex(messageId)
    if (idx !== -1) {
      msgListRef.value?.scrollToMessage(messageId)
      highlightedId.value = messageId
      setTimeout(() => { highlightedId.value = null }, 2000)
      return
    }

    // 逐页加载历史消息直到找到或无更多
    const maxTries = 8
    for (let i = 0; i < maxTries; i++) {
      const batch = await loadMoreForJump()
      if (!batch || batch.length === 0) break
      idx = findMessageIndex(messageId)
      if (idx !== -1) {
        msgListRef.value?.scrollToMessage(messageId)
        highlightedId.value = messageId
        setTimeout(() => { highlightedId.value = null }, 2000)
        return
      }
    }

    ElMessage.info('未找到引用消息，可能已被清理')
  } finally {
    jumpLoading.value = false
  }
}

const getPendingJump = (sessionId) => {
  try {
    const raw = localStorage.getItem('im_jump_message')
    if (!raw) return null
    const data = JSON.parse(raw)
    if (String(data.sessionId) === String(sessionId)) {
      localStorage.removeItem('im_jump_message')
      return data.messageId
    }
  } catch {}
  return null
}

const handleSend = async (payload) => {
  const msg = await store.dispatch('im/message/sendMessage', { sessionId: props.session.id, ...payload })
  if (msg) {
    msg.isOwn = true
    messages.value.push(msg)
    store.dispatch('im/session/clearDraft', props.session.id)
    nextTick(() => msgListRef.value?.scrollToBottom())
  }
}

const handleMessageCommand = (c, m) => {
  if (c === 'reply') {
    store.dispatch('im/message/setReplyingMessage', m)
  } else if (c === 'copy') {
    let text = ''
    if (m?.type === 'TEXT') {
      text = m?.content || ''
    } else if (m?.type === 'LINK') {
      try {
        const data = JSON.parse(m?.content || '{}')
        text = data.url || ''
      } catch (e) {
        text = ''
      }
    } else {
      ElMessage.info('仅支持复制文本/链接')
      return
    }
    if (!text) {
      ElMessage.info('无可复制内容')
      return
    }
    navigator.clipboard?.writeText(text).then(() => {
      ElMessage.success('已复制')
    }).catch(() => {
      ElMessage.error('复制失败')
    })
  } else if (c === 'forward') {
    forwardDialogRef.value?.open(m)
  } else if (c === 'recall') {
    store.dispatch('im/message/recallMessage', { sessionId: props.session.id, messageId: m.id })
  } else if (c === 'reaction') {
    // 表情回应
    toggleMessageReaction(m.id || m.messageId, m.emoji)
  } else if (c === 'readDetail') {
    // 查看已读详情
    showReadDetail(m.id || m.messageId)
  } else if (c === 'edit') {
    // 编辑消息
    startEditMessage(m)
  }
}

const handleClearMessages = () => {
  ElMessageBox.confirm('清空聊天记录？', '提示', { type: 'warning' }).then(async () => {
    await store.dispatch('im/message/clearMessages', props.session.id)
    messages.value = []; ElMessage.success('已清空')
  })
}

const handleOpenFiles = () => {
  showGlobalSearch.value = true
}

const handlePinSession = () => {
  const newPinnedState = !props.session?.isPinned
  store.dispatch('im/session/pinSession', {
    sessionId: props.session.id,
    pinned: newPinnedState
  }).then(() => {
    ElMessage.success(newPinnedState ? '已置顶会话' : '已取消置顶')
  })
}

const handleOpenCreateGroup = () => {
  emit('create-group')
}

// 表情回应
const handleToggleReaction = async (messageId, emoji) => {
  try {
    const res = await toggleMessageReaction(messageId, emoji)
    if (res.code === 200) {
      ElMessage.success('表情已添加')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 查看已读详情
const showReadDetail = (messageId) => {
  readDetailMessageId.value = messageId
  showReadDrawer.value = true
}

// 开始编辑消息
const startEditMessage = (message) => {
  if (!message || message.type !== 'TEXT') {
    ElMessage.warning('只能编辑文本消息')
    return
  }
  editingMessage.value = message
}

// 保存编辑的消息
const handleEditSave = async (data) => {
  if (!data.messageId || !data.content?.trim()) {
    ElMessage.warning('消息内容不能为空')
    return
  }

  try {
    const res = await editMessage(data.messageId, { newContent: data.content })
    if (res.code === 200) {
      // 更新本地消息列表
      const msgIndex = messages.value.findIndex(m => (m.id || m.messageId) === data.messageId)
      if (msgIndex !== -1) {
        messages.value[msgIndex].content = data.content
        messages.value[msgIndex].edited = true
      }
      ElMessage.success('消息已编辑')
    } else {
      throw new Error(res.msg || '编辑失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '编辑失败')
  } finally {
    editingMessage.value = null
  }
}

// 取消编辑
const handleEditCancel = () => {
  editingMessage.value = null
}

/**
 * 加载更多历史消息（分页）
 */
const handleLoadMore = async () => {
  if (loading.value || !props.session?.id) return

  // 获取当前消息列表中最早的消息ID
  const lastMessage = messages.value.length > 0 ? messages.value[0] : null
  const lastMessageId = lastMessage?.id || lastMessage?.messageId

  // 如果没有消息ID，说明没有更多可加载
  if (!lastMessageId) {
    ElMessage.info('没有更多消息了')
    return
  }

  loading.value = true
  try {
    // 保存当前滚动高度，以便加载后保持位置
    const scrollHeight = msgListRef.value?.listRef?.scrollHeight || 0

    const res = await store.dispatch('im/message/loadMessages', {
      sessionId: props.session.id,
      lastMessageId,
      pageSize: 20,
      isLoadMore: true
    })

    if (res && res.length > 0) {
      // 更新本地消息列表（保持原有的 isOwn 标记）
      const newMessages = (res || []).map(m => ({
        ...m,
        isOwn: m.senderId === currentUser.value?.id || m.isSelf
      }))

      // 将新消息添加到列表开头
      messages.value = [...newMessages, ...messages.value]

      // 恢复滚动位置
      nextTick(() => {
        if (msgListRef.value?.listRef) {
          const newScrollHeight = msgListRef.value.listRef.scrollHeight
          msgListRef.value.listRef.scrollTop = newScrollHeight - scrollHeight
        }
      })
    } else {
      ElMessage.info('没有更多消息了')
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
    ElMessage.error('加载历史消息失败')
  } finally {
    loading.value = false
  }
}

/**
 * 上传文件
 */
const handleUploadFile = async (file) => {
  if (!props.session?.id) {
    ElMessage.warning('请先选择会话')
    return
  }

  // 创建临时消息显示上传状态
  const tempId = `upload_${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'FILE',
    status: 'uploading',
    fileName: file.name,
    fileSize: formatFileSize(file.size),
    progress: 0
  }
  messages.value.push(tempMsg)
  nextTick(() => msgListRef.value?.scrollToBottom())

  // 创建 FormData
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', 'file')
  formData.append('sessionId', props.session.id)

  try {
    const res = await uploadFile(formData)
    if (res.code === 200) {
      // 上传成功，发送文件消息
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: props.session.id,
        content: JSON.stringify({
          fileId: res.data.fileId,
          fileName: file.name,
          fileSize: file.size,
          fileUrl: res.data.fileUrl || res.data.url
        }),
        type: 'FILE'
      })

      // 移除临时消息，添加实际消息
      const index = messages.value.findIndex(m => m.id === tempId)
      if (index !== -1) {
        messages.value.splice(index, 1)
      }

      if (msg) {
        msg.isOwn = true
        messages.value.push(msg)
        nextTick(() => msgListRef.value?.scrollToBottom())
      }

      ElMessage.success('文件上传成功')
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    // 更新临时消息状态为失败
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error(error.message || '文件上传失败')
  }
}

/**
 * 上传图片
 */
const handleUploadImage = async (file) => {
  if (!props.session?.id) {
    ElMessage.warning('请先选择会话')
    return
  }

  // 创建临时消息显示上传状态
  const tempId = `upload_${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'IMAGE',
    status: 'uploading',
    fileName: file.name,
    fileSize: formatFileSize(file.size),
    progress: 0
  }
  messages.value.push(tempMsg)
  nextTick(() => msgListRef.value?.scrollToBottom())

  // 创建 FormData
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', 'image')
  formData.append('sessionId', props.session.id)

  try {
    const res = await uploadImage(formData)
    if (res.code === 200) {
      // 上传成功，发送图片消息
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: props.session.id,
        content: JSON.stringify({
          fileId: res.data.fileId,
          fileName: file.name,
          fileSize: file.size,
          fileUrl: res.data.fileUrl || res.data.url,
          thumbnailUrl: res.data.thumbnailUrl || res.data.url
        }),
        type: 'IMAGE'
      })

      // 移除临时消息，添加实际消息
      const index = messages.value.findIndex(m => m.id === tempId)
      if (index !== -1) {
        messages.value.splice(index, 1)
      }

      if (msg) {
        msg.isOwn = true
        messages.value.push(msg)
        nextTick(() => msgListRef.value?.scrollToBottom())
      }

      ElMessage.success('图片上传成功')
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    // 更新临时消息状态为失败
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error(error.message || '图片上传失败')
  }
}

/**
 * 批量上传文件
 */
const handleBatchUpload = async (formData) => {
  if (!props.session?.id) {
    ElMessage.warning('请先选择会话')
    return
  }

  try {
    const res = await batchUploadFiles(formData)
    if (res.code === 200) {
      // 批量上传成功，为每个文件创建消息
      for (const fileData of res.data || []) {
        const msg = await store.dispatch('im/message/sendMessage', {
          sessionId: props.session.id,
          content: JSON.stringify({
            fileId: fileData.fileId,
            fileName: fileData.fileName,
            fileSize: fileData.fileSize,
            fileUrl: fileData.fileUrl
          }),
          type: 'FILE'
        })

        if (msg) {
          msg.isOwn = true
          messages.value.push(msg)
        }
      }
      nextTick(() => msgListRef.value?.scrollToBottom())
      ElMessage.success(`批量上传成功，共${res.data?.length || 0}个文件`)
    } else {
      throw new Error(res.message || '批量上传失败')
    }
  } catch (error) {
    console.error('批量上传失败:', error)
    ElMessage.error(error.message || '批量上传失败')
  }
}

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

const handleRetryMessage = async (msg) => {
  if (!msg) return
  // 更新消息状态为发送中
  const index = messages.value.findIndex(m => m.id === msg.id)
  if (index !== -1) {
    messages.value[index].status = 'sending'
  }
  try {
    const result = await store.dispatch('im/message/sendMessage', { sessionId: props.session.id, content: msg.content, type: msg.type })
    if (result && index !== -1) {
      messages.value[index].status = 'sent'
      messages.value[index].id = result.id
    }
  } catch (error) {
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error('消息发送失败，请重试')
  }
}

watch(() => props.session, (newSession) => {
  messages.value = []
  if (newSession) {
    loadHistory()
    // 切换会话时重置详情状态，防止影子状态残留
    showUserDetail.value = false
    showGroupDetail.value = false
  }
}, { immediate: true })

// 注册通话事件监听
onMounted(() => {
  onCall(handleCallEvent)
  onTyping(handleTypingEvent)
})

// 清理通话事件监听
onUnmounted(() => {
  // 清理逻辑由 useImWebSocket 自动处理
  if (typingTimeout) {
    clearTimeout(typingTimeout)
  }
})
</script>

<style scoped lang="scss">
.chat-panel-container { height: 100%; flex: 1; min-width: var(--dt-chat-min-width, 400px); background: var(--dt-bg-card); display: flex; flex-direction: column; overflow: hidden; position: relative; }
.welcome-logo-box { width: var(--dt-logo-size); height: var(--dt-logo-size); background: var(--dt-brand-color); border-radius: var(--dt-radius-xl); color: var(--dt-text-primary); font-size: var(--dt-font-size-2xl); font-weight: 800; display: flex; align-items: center; justify-content: center; margin: 0 auto var(--dt-spacing-xl); box-shadow: var(--dt-shadow-3); }
.chat-main-canvas { height: 100%; width: 100%; display: flex; flex-direction: column; overflow: hidden; &__body { flex: 1; display: flex; flex-direction: column; min-width: 0; position: relative; background: var(--dt-bg-chat); overflow: hidden; } }
.chat-input-area { flex-shrink: 0; background: var(--dt-bg-card); border-top: 1px solid var(--dt-border-light); min-height: var(--dt-chat-input-height); z-index: 10; display: flex; flex-direction: column; }
</style>
