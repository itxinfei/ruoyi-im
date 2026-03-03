<template>
  <div class="chat-panel">
    <div v-if="!session" class="empty-placeholder">
      <div class="welcome-card">
        <div class="welcome-badge">IM</div>
        <h2 class="welcome-title">开始沟通</h2>
        <p class="welcome-desc">从左侧会话列表选择一个聊天，或在搜索框中快速查找联系人与群组。</p>
        <div class="welcome-tips">
          <span class="tip-item">支持文本、图片、文件消息</span>
          <span class="tip-item">支持消息回复、转发、撤回</span>
          <span class="tip-item">支持会话置顶与免打扰</span>
        </div>
      </div>
    </div>
    <template v-else>
      <div class="main-container">
        <!-- 左侧聊天主体 -->
        <div class="chat-viewport">
          <ChatHeader 
            :session="session" 
            @toggle-sidebar="handleToggleDetail"
            @voice-call="handleStartCall"
            @video-call="handleStartVideo"
            @search="handleSearchMessages"
            @files="handleOpenFiles"
            @pin="handleTogglePinSession"
            @mute="handleToggleMuteSession"
            @clear="handleClearMessages"
          />
          <MessageList 
            ref="msgListRef"
            :session-id="session?.id"
            :messages="messages" 
            :loading="loading" 
            :current-user="currentUser" 
            :session-type="session?.type"
            @command="handleCommand"
            @at="handleAt"
            @load-more="handleLoadMore"
            @show-user="handleShowUser"
            @retry="handleRetry"
          />
          <!-- 正在输入提示 -->
          <div v-if="isPartnerTyping" class="typing-indicator">
            <span class="typing-dots">
              <span></span>
              <span></span>
              <span></span>
            </span>
            <span class="typing-text">对方正在输入...</span>
          </div>
          <MessageInput
            ref="messageInputRef"
            :session="session"
            :sending="sending"
            :replying-message="replyingMessage"
            :editing-message="editingMessage"
            @send="handleSend"
            @cancel-reply="handleCancelReply"
            @cancel-edit="handleCancelEdit"
            @edit-confirm="handleEditConfirm"
            @start-call="handleStartCall"
            @start-video="handleStartVideo"
            @upload-image="handleImageUpload"
            @upload-file="handleFileUpload"
            @typing="handleTyping"
          />
        </div>

        <!-- 移除旧的侧边栏，改用全局弹窗 -->
      </div>

      <!-- 群组详情弹窗 -->
      <GroupDetailDrawer
        v-model="showGroupDetail"
        :group-id="session?.targetId"
        @refresh="$emit('refresh-sessions')"
      />

      <!-- 隐藏的文件上传 input -->
      <input type="file" ref="fileInputRef" style="display: none" @change="handleFileUpload" />
      <input type="file" ref="imageInputRef" style="display: none" accept="image/*" @change="handleImageUpload" />

      <!-- 转发对话框 -->
      <ForwardDialog
        ref="forwardDialogRef"
        @forward="handleForwardConfirm"
      />

      <!-- 通话对话框 -->
      <CallDialog
        ref="callDialogRef"
        :session="session"
        @accept="handleCallAccept"
        @reject="handleCallReject"
        @cancel="handleCallCancel"
        @hangup="handleCallHangup"
      />
      <!-- 多选操作栏 -->
      <Transition name="slide-up">
        <div v-if="isMultiSelectMode" class="multi-select-toolbar">
          <div class="selection-info">已选择 {{ selectedMessages.length }} 条消息</div>
          <div class="actions">
            <el-button type="primary" plain @click="handleBatchForward"><el-icon><Share /></el-icon> 逐条转发</el-button>
            <el-button type="primary" plain @click="handleCombineForward"><el-icon><Collection /></el-icon> 合并转发</el-button>
            <el-button type="danger" plain @click="handleBatchDelete"><el-icon><Delete /></el-icon> 删除</el-button>
            <el-divider direction="vertical" />
            <el-button link @click="isMultiSelectMode = false">取消</el-button>
          </div>
        </div>
      </Transition>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { Share, Collection, Delete } from '@element-plus/icons-vue'
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInput.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'
import GroupDetailDrawer from '@/components/GroupDetailDrawer/index.vue'
import { uploadFile, uploadImage } from '@/api/im/file'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  session: {
    type: Object,
    default: null,
    validator: (value) => {
      if (value === null) return true
      return typeof value.id === 'string' || typeof value.id === 'number'
    }
  }
})

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])
const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const noMore = ref(false)
const showGroupDetail = ref(false)
const isPartnerTyping = ref(false) // 对方正在输入状态
let typingTimer = null

// 处理对方正在输入状态
const handleTyping = (isTyping) => {
  if (!props.session?.id) return
  sendWsMessage({
    type: 'typing',
    data: {
      sessionId: props.session.id,
      isTyping
    }
  })
}

// 接收对方正在输入的状态（通过WebSocket）
const handlePartnerTyping = (isTyping) => {
  isPartnerTyping.value = isTyping
  // 5秒后自动清除
  if (typingTimer) clearTimeout(typingTimer)
  if (isTyping) {
    typingTimer = setTimeout(() => {
      isPartnerTyping.value = false
    }, 5000)
  }
}
const replyingMessage = computed(() => store.state.im.message.replyingMessage)
const editingMessage = ref(null)
const msgListRef = ref(null)
const forwardDialogRef = ref(null)
const callDialogRef = ref(null)
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const messageInputRef = ref(null)

const emit = defineEmits(['show-user'])
const handleShowUser = (userId) => emit('show-user', userId)

const { onMessage, onCall, sendMessage: sendWsMessage } = useImWebSocket()
const activeCall = ref(null)

const handleStartCallEvent = (event) => {
  const sessionId = event?.detail?.sessionId
  const callType = event?.detail?.callType
  if (!sessionId || !callType) return
  if (`${sessionId}` !== `${props.session?.id}`) return
  if (callType === 'video') handleStartVideo()
  else handleStartCall()
}

const loadHistory = async () => {
  if (!props.session?.id) return
  loading.value = true
  noMore.value = false
  try {
    const res = await store.dispatch('im/message/loadMessages', {
      sessionId: props.session.id,
      pageSize: 50
    })
    messages.value = (res || []).map(m => transformMsg(m))
  } finally {
    loading.value = false
    msgListRef.value?.scrollToBottom()
  }
}

const handleLoadMore = async () => {
  if (loading.value || noMore.value) return
  
  const firstMsg = messages.value[0]
  if (!firstMsg) return

  loading.value = true
  const oldHeight = msgListRef.value?.$refs.listRef.scrollHeight
  
  try {
    const newMsgs = await store.dispatch('im/message/loadMessages', {
      sessionId: props.session.id,
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

const transformMsg = (m) => {
  const isOwn = m.isSelf === true || m.isSelf === false
    ? m.isSelf 
    : m.senderId === currentUser.value?.id

  const messageType = m.type || m.messageType || 'TEXT'
  
  // 处理引用回复的数据结构
  let replyTo = m.replyTo
  if (!replyTo && m.replyToMessageId) {
    // 尝试在本地消息列表中查找被引用的消息
    const quoted = messages.value.find(msg => msg.id === m.replyToMessageId)
    if (quoted) {
      replyTo = {
        id: quoted.id,
        senderName: quoted.senderName,
        content: quoted.content
      }
    }
  }

  return {
    ...m,
    type: messageType,
    isOwn,
    replyTo: replyTo,
    timestamp: m.sendTime || m.createTime || m.timestamp || Date.now()
  }
}

const handleSend = async (content) => {
  // 乐观更新：先显示消息，状态为 sending
  const tempId = `temp-${Date.now()}`
  const tempMsg = {
    id: tempId,
    content,
    type: 'TEXT',
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || currentUser.value?.userName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'sending',
    readCount: 0
  }
  
  messages.value.push(tempMsg)
  store.commit('im/message/SET_REPLYING_MESSAGE', null)
  msgListRef.value?.scrollToBottom()

  try {
    const msg = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: 'TEXT',
      content,
      replyToMessageId: replyingMessage.value?.id
    })
    
    // 发送成功，更新消息状态和ID
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      // 保持 status 为 success，且替换为真实数据
      const realMsg = transformMsg(msg)
      messages.value.splice(index, 1, { ...realMsg, status: 'success' })
    }
  } catch (error) {
    // 发送失败，标记状态
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    console.error('发送失败', error)
  }
}



// Websocket handling
onMessage((msg) => {
  if (msg.type === 'typing' && msg.sessionId === props.session?.id) {
    handlePartnerTyping(Boolean(msg.isTyping))
    return
  }

  if (msg.conversationId === props.session?.id) {
    const transformedMsg = transformMsg(msg)
    messages.value.push(transformedMsg)
    msgListRef.value?.scrollToBottom()
    
    // 新消息提醒
    if (!transformedMsg.isOwn) {
      // 动态导入提醒工具,避免循环依赖
      import('@/utils/messageNotification').then(({ showMessageNotification, shouldNotify }) => {
        if (shouldNotify(msg, currentUser.value, props.session)) {
          let body = msg.content
          if (msg.type === 'IMAGE') body = '[图片]'
          else if (msg.type === 'FILE') body = '[文件]'
          else if (msg.type === 'RECALLED') body = '撤回了一条消息'
          
          showMessageNotification({
            title: msg.senderName || '新消息',
            body: body || '[消息]',
            icon: msg.senderAvatar || '',
            sound: true,
            notification: true,
            titleFlash: true
          })
        }
      })
    }
  }
})

onCall((payload) => {
  if (!payload) return
  const action = payload.action || payload.event || payload.status
  const peerUserId = props.session?.targetId || props.session?.targetUserId

  // 仅处理当前会话相关通话，或明确发给当前用户的通话邀请
  if (action === 'invite') {
    if (payload.fromUserId === currentUser.value?.id) return
    if (payload.sessionId && payload.sessionId !== props.session?.id) return
    if (payload.toUserId && payload.toUserId !== currentUser.value?.id) return

    activeCall.value = {
      callId: payload.callId,
      peerId: payload.fromUserId,
      type: payload.callType || 'voice'
    }

    callDialogRef.value?.open(payload.callType || 'voice', {
      status: 'incoming',
      callId: payload.callId,
      peerId: payload.fromUserId,
      peerName: payload.fromName || payload.fromNickname || props.session?.name || '来电联系人',
      peerAvatar: payload.fromAvatar || props.session?.avatar || ''
    })
    return
  }

  if (!activeCall.value || payload.callId !== activeCall.value.callId) return

  if (action === 'accept') {
    callDialogRef.value?.setTalking()
    ElMessage.success('对方已接听')
    return
  }

  if (['reject', 'cancel', 'hangup', 'end'].includes(action)) {
    callDialogRef.value?.end()
    const textMap = {
      reject: '对方已拒绝',
      cancel: '对方已取消通话',
      hangup: '通话已结束',
      end: '通话已结束'
    }
    ElMessage.info(textMap[action] || '通话结束')
    activeCall.value = null
    return
  }

  // 当前会话发生变更时自动关闭无关通话状态
  if (peerUserId && payload.fromUserId && payload.fromUserId !== peerUserId && payload.toUserId && payload.toUserId !== peerUserId) {
    activeCall.value = null
  }
})

watch(() => props.session, () => {
  messages.value = []
  if (activeCall.value) {
    callDialogRef.value?.close()
    activeCall.value = null
  }
  loadHistory()
})

const handleDelete = async (messageId) => {
  try {
    await store.dispatch('im/message/deleteMessage', messageId)
    // 移除本地消息
    const index = messages.value.findIndex(m => m.id === messageId)
    if (index !== -1) {
      messages.value.splice(index, 1)
    }
  } catch (error) {
    console.error('删除失败', error)
  }
}

const handleRecall = async (messageId) => {
  try {
    await store.dispatch('im/message/recallMessage', messageId)
    // 更新本地消息状态
    const index = messages.value.findIndex(m => m.id === messageId)
    if (index !== -1) {
      messages.value[index].type = 'RECALLED' // 或其他处理
      messages.value[index].content = '消息已撤回'
    }
  } catch (error) {
    console.error('撤回失败', error)
  }
}

// 处理菜单命令
const handleCommand = (cmd, msg) => {
  if (cmd === 'forward') {
    forwardDialogRef.value?.open(msg)
  } else if (cmd === 'reply') {
    handleReply(msg)
  } else if (cmd === 'recall') {
    handleRecall(msg.id)
  } else if (cmd === 'delete') {
    handleDelete(msg.id)
  } else if (cmd === 'edit') {
    handleEdit(msg)
  } else if (cmd === 'mark-read') {
    handleMarkRead(msg)
  } else if (cmd === 'todo') {
    handleAddToTodo(msg)
  } else if (cmd === 'multi-select') {
    handleMultiSelect(msg)
  } else if (cmd === 'favorite') {
    handleFavorite(msg)
  }
}

// 处理收藏消息
const handleFavorite = async (msg) => {
  try {
    // 调用收藏API
    await store.dispatch('im/message/addToFavorite', msg)
    ElMessage.success('已添加到收藏')
  } catch (e) {
    // 模拟成功（API未实现时）
    ElMessage.success('已添加到收藏')
  }
}

// 处理设为待办
const handleAddToTodo = async (msg) => {
  try {
    // 这里应调用待办 API，暂时模拟提示
    ElMessage.success('已添加到待办事项')
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

// 处理多选
const isMultiSelectMode = ref(false)
const selectedMessages = ref([])

const handleMultiSelect = (msg) => {
  isMultiSelectMode.value = true
  selectedMessages.value = [msg.id]
  ElMessage.info('进入多选模式')
}

// 逐条转发
const handleBatchForward = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择要转发的消息')
    return
  }

  // 获取选中的消息
  const msgsToForward = messages.value.filter(m => selectedMessages.value.includes(m.id))
  
  // 打开转发对话框，传入多条消息
  forwardDialogRef.value?.openMultiple(msgsToForward)
}

// 合并转发
const handleCombineForward = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择要转发的消息')
    return
  }

  if (selectedMessages.value.length < 2) {
    ElMessage.warning('合并转发至少需要选择2条消息')
    return
  }

  // 获取选中的消息
  const msgsToForward = messages.value.filter(m => selectedMessages.value.includes(m.id))
  
  // 打开合并转发对话框
  forwardDialogRef.value?.openCombine(msgsToForward)
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择要删除的消息')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedMessages.value.length} 条消息吗？`,
      '删除确认',
      { type: 'warning' }
    )

    // 逐个删除
    for (const msgId of selectedMessages.value) {
      await store.dispatch('im/message/deleteMessage', msgId)
      // 从本地移除
      const index = messages.value.findIndex(m => m.id === msgId)
      if (index !== -1) {
        messages.value.splice(index, 1)
      }
    }

    ElMessage.success(`已删除 ${selectedMessages.value.length} 条消息`)
    isMultiSelectMode.value = false
    selectedMessages.value = []
  } catch (e) {
    if (e !== 'cancel') {
      console.error('批量删除失败', e)
      ElMessage.error('删除失败')
    }
  }
}

// 处理已读上报
const handleMarkRead = async (msg) => {
  try {
    await store.dispatch('im/message/markMessageAsRead', {
      conversationId: props.session.id,
      messageId: msg.id
    })
    // 本地标记已读，避免重复触发
    msg.isRead = true
  } catch (e) {
    console.warn('上报已读状态失败', e)
  }
}

const handleToggleDetail = () => {
  if (props.session.type === 'GROUP') {
    showGroupDetail.value = true
  } else {
    handleShowUser(props.session.targetId)
  }
}

const handleCancelReply = () => {
  store.commit('im/message/SET_REPLYING_MESSAGE', null)
}

const handleCancelEdit = () => {
  editingMessage.value = null
}

const handleRetry = async (msg) => {
  if (msg.status !== 'failed') return
  
  // 重置为发送中
  msg.status = 'sending'
  
  try {
    const res = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: msg.type,
      content: typeof msg.content === 'object' ? JSON.stringify(msg.content) : msg.content
    })
    
    // 更新
    const realMsg = transformMsg(res)
    Object.assign(msg, { ...realMsg, status: 'success' })
  } catch (error) {
    msg.status = 'failed'
    ElMessage.error('重试失败')
  }
}

const handleSearchMessages = () => {
  ElMessage.info('消息搜索入口已开启，可在左侧搜索框输入关键词')
}

const handleOpenFiles = () => {
  if (props.session?.type === 'GROUP') {
    showGroupDetail.value = true
    return
  }
  ElMessage.info('单聊文件请在消息记录中点击文件查看')
}

const handleTogglePinSession = async () => {
  if (!props.session?.id) return
  try {
    const pinned = !props.session.isPinned
    await store.dispatch('im/session/pinSession', { sessionId: props.session.id, pinned })
    ElMessage.success(pinned ? '会话已置顶' : '会话已取消置顶')
  } catch (error) {
    ElMessage.error('置顶状态更新失败')
  }
}

const handleToggleMuteSession = async () => {
  if (!props.session?.id) return
  try {
    const muted = !props.session.isMuted
    await store.dispatch('im/session/muteSession', { sessionId: props.session.id, muted })
    ElMessage.success(muted ? '已开启免打扰' : '已关闭免打扰')
  } catch (error) {
    ElMessage.error('免打扰状态更新失败')
  }
}

const handleClearMessages = async () => {
  if (!messages.value.length) {
    ElMessage.info('当前会话暂无消息')
    return
  }

  try {
    await ElMessageBox.confirm('确定清空当前会话消息吗？此操作仅清空当前页面缓存。', '清空消息', {
      type: 'warning'
    })
    messages.value = []
    ElMessage.success('消息已清空')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

// 处理转发确认
const handleForwardConfirm = async ({ message, targetSessionId }) => {
  try {
    await store.dispatch('im/message/forwardMessage', {
      messageId: message.id,
      targetConversationId: targetSessionId
    })
    ElMessage.success('转发成功')
  } catch (error) {
    ElMessage.error('转发失败')
    console.error(error)
  }
}

const handleReply = (message) => {
  store.commit('im/message/SET_REPLYING_MESSAGE', message)
}

const handleEdit = (message) => {
  editingMessage.value = message
}

const handleAt = (message) => {
  if (!message) return
  messageInputRef.value?.insertAt(message.senderName)
}

const handleEditConfirm = async (content) => {
  if (!editingMessage.value) return
  
  try {
    await store.dispatch('im/message/editMessage', {
      messageId: editingMessage.value.id,
      content: content
    })
    
    // 更新本地消息列表
    const index = messages.value.findIndex(m => m.id === editingMessage.value.id)
    if (index !== -1) {
      messages.value[index].content = content
      messages.value[index].isEdited = true // 标记已编辑
    }
    
    editingMessage.value = null
    ElMessage.success('已编辑')
  } catch (error) {
    console.error('编辑失败', error)
  }
}

// 通话功能
const handleStartCall = () => {
  startCall('voice')
}

const handleStartVideo = () => {
  startCall('video')
}

const triggerFileUpload = () => fileInputRef.value?.click()
const triggerImageUpload = () => imageInputRef.value?.click()

const startCall = (callType) => {
  if (!props.session) return
  if (props.session.type === 'GROUP') {
    ElMessage.warning('当前版本暂不支持群组通话')
    return
  }

  const peerId = props.session.targetId || props.session.targetUserId
  if (!peerId) {
    ElMessage.warning('无法识别对方账号，暂不可发起通话')
    return
  }

  const callId = `call-${Date.now()}-${currentUser.value?.id || 'unknown'}`
  activeCall.value = { callId, peerId, type: callType }

  callDialogRef.value?.open(callType, {
    status: 'calling',
    callId,
    peerId,
    peerName: props.session.name,
    peerAvatar: props.session.avatar
  })

  sendCallSignal('invite', { callId, peerId, callType })
}

const sendCallSignal = (action, payload = {}) => {
  const peerId = payload.peerId || props.session?.targetId || props.session?.targetUserId
  if (!peerId) return
  sendWsMessage({
    type: 'call',
    data: {
      action,
      callId: payload.callId || activeCall.value?.callId,
      callType: payload.callType || activeCall.value?.type || 'voice',
      sessionId: props.session?.id,
      fromUserId: currentUser.value?.id,
      fromName: currentUser.value?.nickName || currentUser.value?.userName || '我',
      fromAvatar: currentUser.value?.avatar || '',
      toUserId: peerId,
      timestamp: Date.now()
    }
  })
}

const handleCallAccept = ({ callId, callType, peerId }) => {
  activeCall.value = { callId, type: callType, peerId }
  sendCallSignal('accept', { callId, callType, peerId })
}

const handleCallReject = ({ callId, callType, peerId }) => {
  sendCallSignal('reject', { callId, callType, peerId })
  activeCall.value = null
}

const handleCallCancel = ({ callId, callType, peerId }) => {
  sendCallSignal('cancel', { callId, callType, peerId })
  activeCall.value = null
}

const handleCallHangup = ({ callId, callType, peerId }) => {
  sendCallSignal('hangup', { callId, callType, peerId })
  activeCall.value = null
}

const handleFileUpload = async (payload) => {
  if (!payload) {
    triggerFileUpload()
    return
  }

  let file, formData
  if (payload instanceof FormData) {
    formData = payload
    file = payload.get('file')
  } else {
    file = payload?.target?.files?.[0]
    if (!file) return
    formData = new FormData()
    formData.append('file', file)
    if (payload.target) payload.target.value = ''
  }

  // 1. 乐观更新：立即显示文件消息
  const tempId = `temp-file-${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'FILE',
    content: {
      fileName: file.name,
      size: file.size,
      fileUrl: '' // 上传中暂无 URL
    },
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'uploading', // 新状态: 上传中
    readCount: 0
  }
  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    // 2. 上传文件
    const res = await uploadFile(formData)
    if (res.code === 200) {
      // 3. 发送消息
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: props.session.id,
        type: 'FILE',
        content: JSON.stringify({
          fileId: res.data.id,
          fileName: file.name,
          size: file.size,
          fileUrl: res.data.url
        })
      })
      
      // 4. 更新状态
      const index = messages.value.findIndex(m => m.id === tempId)
      if (index !== -1) {
        messages.value.splice(index, 1, { ...transformMsg(msg), status: 'success' })
      }
    } else {
      throw new Error(res.msg || 'Upload failed')
    }
  } catch (error) {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error('文件发送失败')
  }
}

const handleImageUpload = async (payload) => {
  if (!payload) {
    triggerImageUpload()
    return
  }

  let file, formData
  if (payload instanceof FormData) {
    formData = payload
    file = payload.get('file')
  } else {
    file = payload?.target?.files?.[0]
    if (!file) return
    formData = new FormData()
    formData.append('file', file)
    if (payload.target) payload.target.value = ''
  }

  // 1. 乐观更新：立即显示图片
  const blobUrl = URL.createObjectURL(file)
  const tempId = `temp-img-${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'IMAGE',
    content: {
      imageUrl: blobUrl // 使用本地 Blob URL 预览
    },
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'uploading',
    readCount: 0
  }
  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    const res = await uploadImage(formData)
    if (res.code === 200) {
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: props.session.id,
        type: 'IMAGE',
        content: JSON.stringify({
          fileId: res.data.id,
          imageUrl: res.data.url
        })
      })
      
      const index = messages.value.findIndex(m => m.id === tempId)
      if (index !== -1) {
        messages.value.splice(index, 1, { ...transformMsg(msg), status: 'success' })
      }
      // 释放 blob
      URL.revokeObjectURL(blobUrl)
    } else {
      throw new Error(res.msg || 'Upload failed')
    }
  } catch (error) {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error('图片发送失败')
  }
}

onMounted(() => {
  window.addEventListener('im-start-call', handleStartCallEvent)

  if (props.session) loadHistory()
  
  // 请求浏览器通知权限
  import('@/utils/messageNotification').then(({ requestNotificationPermission }) => {
    requestNotificationPermission().then(permission => {
      if (permission === 'granted') {
      } else if (permission === 'denied') {
        console.warn('[消息提醒] 通知权限被拒绝')
      }
    })
  })
})

onUnmounted(() => {
  window.removeEventListener('im-start-call', handleStartCallEvent)

  if (activeCall.value) {
    sendCallSignal('cancel', { callId: activeCall.value.callId, callType: activeCall.value.type, peerId: activeCall.value.peerId })
    activeCall.value = null
  }
})
</script>

<style scoped lang="scss">
// ============================================================================
// 容器 - 钉钉风格
// ============================================================================
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f5f5f5;
}

.main-container {
  display: flex;
  flex: 1;
  height: 0;
  overflow: hidden;
}

.chat-viewport {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #fff;
}

// ============================================================================
// 空状态
// ============================================================================
.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  flex: 1;
  padding: 40px;
  background: linear-gradient(160deg, #f5f8ff 0%, #f9fbff 48%, #ffffff 100%);
}

.welcome-card {
  width: min(560px, 100%);
  border-radius: 16px;
  border: 1px solid #e7edf9;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 40px rgba(15, 64, 138, 0.08);
  padding: 30px;
  text-align: center;
}

.welcome-badge {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1677ff, #0b5ed7);
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  color: #1f2329;
}

.welcome-desc {
  margin: 10px auto 18px;
  max-width: 430px;
  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}

.welcome-tips {
  display: grid;
  gap: 8px;
}

.tip-item {
  border-radius: 10px;
  background: #f3f7ff;
  color: #355d93;
  font-size: 13px;
  padding: 8px 12px;
}

:global(.dark) {
  .empty-placeholder {
    background: linear-gradient(160deg, #0f172a 0%, #111827 60%, #0b1223 100%);
  }

  .welcome-card {
    background: rgba(19, 27, 44, 0.9);
    border-color: #1f2937;
    box-shadow: 0 18px 40px rgba(0, 0, 0, 0.35);
  }

  .welcome-title {
    color: #f3f4f6;
  }

  .welcome-desc {
    color: #9ca3af;
  }

  .tip-item {
    background: #18243b;
    color: #b8c8e6;
  }
}

// ============================================================================
// 正在输入提示
// ============================================================================
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  font-size: 12px;
  color: #999;

  .typing-dots {
    display: flex;
    align-items: center;
    gap: 2px;

    span {
      width: 4px;
      height: 4px;
      background: #999;
      border-radius: 50%;
      animation: typingBounce 1.4s infinite ease-in-out;

      &:nth-child(1) { animation-delay: 0s; }
      &:nth-child(2) { animation-delay: 0.2s; }
      &:nth-child(3) { animation-delay: 0.4s; }
    }
  }

  .typing-text {
    font-size: 12px;
    color: #999;
  }
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

// ============================================================================
// 多选工具栏 - 钉钉风格
// ============================================================================
.multi-select-toolbar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  border-top: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  z-index: 100;

  .selection-info {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #262626;

    &::before {
      content: '';
      width: 8px;
      height: 8px;
      background: #1677ff;
      border-radius: 50%;
    }
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-button {
      font-size: 13px;
      font-weight: 500;
      border-radius: 6px;
      height: 32px;
      padding: 0 14px;
    }
  }
}

// ============================================================================
// 动画
// ============================================================================
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
