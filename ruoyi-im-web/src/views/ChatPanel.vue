<template>
  <div class="chat-panel">
    <div v-if="!session" class="empty-placeholder">
      <el-empty description="选择一个会话开始聊天" />
    </div>
    <template v-else>
      <div class="main-container">
        <!-- 左侧聊天主体 -->
        <div class="chat-viewport">
          <ChatHeader 
            :session="session" 
            @toggle-sidebar="handleToggleSidebar" 
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
          />
        </div>

        <!-- 右侧详情侧边栏 -->
        <Transition name="slide-right">
          <ChatSidebar 
            v-if="showSidebar" 
            :session="session" 
            @close="showSidebar = false"
            @member-click="handleMemberClick"
          />
        </Transition>
      </div>

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
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { Share, Collection, Delete } from '@element-plus/icons-vue'
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInput.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'
import ChatSidebar from '@/components/Chat/ChatSidebar.vue'
import { getMessages } from '@/api/im/message'
import { uploadFile, uploadImage } from '@/api/im/file'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { ElMessage } from 'element-plus'

const props = defineProps({
  session: Object
})

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])
const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const noMore = ref(false)
const replyingMessage = computed(() => store.state.im.replyingMessage)
const editingMessage = ref(null)
const msgListRef = ref(null)
const forwardDialogRef = ref(null)
const callDialogRef = ref(null)
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const messageInputRef = ref(null)

const emit = defineEmits(['show-user'])

const { onMessage } = useImWebSocket()

const loadHistory = async () => {
  if (!props.session?.id) return
  loading.value = true
  noMore.value = false
  try {
    const res = await store.dispatch('im/loadMessages', {
      sessionId: props.session.id,
      pageSize: 50
    })
    console.log('===== ChatPanel loadHistory 开始 =====')
    console.log('ChatPanel - 当前登录用户:', currentUser.value)
    console.log('ChatPanel - 会话ID:', props.session.id)
    console.log('ChatPanel - 原始消息数量:', res?.length)

    messages.value = (res || []).map(m => {
      const transformed = transformMsg(m)
      console.log('消息详情:', {
        id: m.id,
        content: m.content,
        senderId: m.senderId,
        senderName: m.senderName,
        后端isSelf: m.isSelf,
        前端isOwn: transformed.isOwn,
        当前userId: currentUser.value?.id,
        匹配结果: m.senderId === currentUser.value?.id
      })
      return transformed
    })
    console.log('===== ChatPanel loadHistory 结束 =====')
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
    const newMsgs = await store.dispatch('im/loadMessages', {
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
  // 优先使用后端返回的 isSelf 字段，后端根据 userId header 判断
  // 如果后端返回了 isSelf（布尔值），直接使用；否则回退到前端判断
  const isOwn = m.isSelf === true || m.isSelf === false
    ? m.isSelf  // 后端已明确返回 isSelf 值
    : m.senderId === currentUser.value?.id  // 前端回退判断

  // 确保消息类型存在，默认为TEXT
  const messageType = m.type || m.messageType || 'TEXT'
  
  // 调试日志：查看消息原始数据
  if (!m.type) {
    console.warn('[ChatPanel] 消息缺少type字段:', m)
  }

  return {
    ...m,
    type: messageType,  // 确保type字段存在
    isOwn,
    timestamp: m.sendTime || m.createTime || m.timestamp
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
  store.commit('im/SET_REPLYING_MESSAGE', null)
  msgListRef.value?.scrollToBottom()

  try {
    const msg = await store.dispatch('im/sendMessage', {
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

const handleRetry = async (msg) => {
  if (msg.status !== 'failed') return
  
  // 重置为发送中
  msg.status = 'sending'
  // 移动到最新位置 (可选，钉钉一般是不动的，但为了简单我们先不动，或者删掉重发)
  // 这里我们选择：保留原位置重试
  
  try {
    const res = await store.dispatch('im/sendMessage', {
      sessionId: props.session.id,
      type: msg.type, // 支持重试不同类型
      content: msg.content,
      // 注意：重试时不再携带 replyTo，除非我们存下来了。简单起见先忽略回复引用
    })
    
    // 更新
    const realMsg = transformMsg(res)
    // 查找并替换
    const index = messages.value.findIndex(m => m.id === msg.id)
    if (index !== -1) {
      messages.value.splice(index, 1, { ...realMsg, status: 'success' })
    }
  } catch (error) {
    msg.status = 'failed'
    ElMessage.error('重试失败')
  }
}

// Websocket handling
onMessage((msg) => {
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

watch(() => props.session, () => {
  messages.value = []
  loadHistory()
})

const handleDelete = async (messageId) => {
  try {
    await store.dispatch('im/deleteMessage', messageId)
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
    await store.dispatch('im/recallMessage', messageId)
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
  }
}

// 处理设为待办
const handleAddToTodo = async (msg) => {
  try {
    // 这里应调用待办 API，暂时模拟提示
    ElMessage.success('已添加到待办事项')
    console.log('添加到待办:', msg)
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

// 处理已读上报
const handleMarkRead = async (msg) => {
  try {
    await store.dispatch('im/markMessageAsRead', {
      conversationId: props.session.id,
      messageId: msg.id
    })
    // 本地标记已读，避免重复触发
    msg.isRead = true
  } catch (e) {
    console.warn('上报已读状态失败', e)
  }
}

const handleToggleSidebar = () => {
  showSidebar.value = !showSidebar.value
}

const handleShowUser = (userId) => {
  emit('show-user', userId)
}

const handleMemberClick = (member) => {
  handleShowUser(member.id)
}

// 处理转发确认
const handleForwardConfirm = async ({ message, targetSessionId }) => {
  try {
    await store.dispatch('im/forwardMessage', {
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
  store.commit('im/SET_REPLYING_MESSAGE', message)
}

const handleEdit = (message) => {
  editingMessage.value = message
}

// 处理 @ 提及
const handleAt = (message) => {
  if (!message) return
  messageInputRef.value?.insertAt(message.senderName)
}

const handleCancelEdit = () => {
  editingMessage.value = null
}

const handleEditConfirm = async (content) => {
  if (!editingMessage.value) return
  
  try {
    await store.dispatch('im/editMessage', {
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

const handleCancelReply = () => {
  store.commit('im/SET_REPLYING_MESSAGE', null)
}

// 通话功能
const handleStartCall = () => {
  callDialogRef.value?.open('voice')
}

const handleStartVideo = () => {
  callDialogRef.value?.open('video')
}

// 文件上传相关
const triggerFileUpload = () => fileInputRef.value?.click()
const triggerImageUpload = () => imageInputRef.value?.click()

const handleFileUpload = async (payload) => {
  let file, formData
  if (payload instanceof FormData) {
    formData = payload
    file = payload.get('file')
  } else {
    file = payload.target.files[0]
    if (!file) return
    formData = new FormData()
    formData.append('file', file)
    payload.target.value = ''
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
      const msg = await store.dispatch('im/sendMessage', {
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
  let file, formData
  if (payload instanceof FormData) {
    formData = payload
    file = payload.get('file')
  } else {
    file = payload.target.files[0]
    if (!file) return
    formData = new FormData()
    formData.append('file', file)
    payload.target.value = ''
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
      const msg = await store.dispatch('im/sendMessage', {
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
  if (props.session) loadHistory()
  
  // 请求浏览器通知权限
  import('@/utils/messageNotification').then(({ requestNotificationPermission }) => {
    requestNotificationPermission().then(permission => {
      if (permission === 'granted') {
        console.log('[消息提醒] 通知权限已授予')
      } else if (permission === 'denied') {
        console.warn('[消息提醒] 通知权限被拒绝')
      }
    })
  })
})
</script>

<style scoped lang="scss">
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f5f7fa;

  .dark & {
    background: #0f172a;
  }
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
  
  .dark & { background: #1e293b; }
}

/* 侧边栏平滑滑入动画 */
.slide-right-enter-active, .slide-right-leave-active {
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
}
.slide-right-enter-from, .slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  flex: 1;
}

/* 多选工具栏 */
.multi-select-toolbar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  z-index: 100;
  
  .dark & { background: #1e293b; border-color: #334155; }
  
  .selection-info { font-size: 14px; color: #8f959e; }
  .actions { display: flex; align-items: center; gap: 12px; }
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: transform 0.3s ease-out;
}
.slide-up-enter-from, .slide-up-leave-to {
  transform: translateY(100%);
}
</style>
