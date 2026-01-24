<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <div v-if="loading" class="loading-wrapper"><el-icon class="is-loading"><Loading /></el-icon>数据加载中...</div>
    <div v-else-if="messages.length === 0" class="empty">暂无消息</div>
    
    <div v-for="msg in messagesWithDividers" :key="msg.id || msg.timeText" :data-id="msg.id" class="message-wrapper">
      <div v-if="msg.isTimeDivider" class="time-divider">
        <span class="time-text">{{ msg.timeText }}</span>
      </div>
      <div v-else class="message-item" :class="{ 'is-own': msg.isOwn }">
        <el-avatar class="avatar" :size="36" :src="addTokenToUrl(msg.senderAvatar)" shape="square" :class="getAvatarBgClass(msg)">
          {{ (msg.senderName || '?').charAt(0).toUpperCase() }}
        </el-avatar>
        <div class="content-wrapper">
          <div v-if="!msg.isOwn" class="sender-name">{{ msg.senderName }}</div>
          
          <div v-if="msg.replyTo" class="reply-wrapper" @click="scrollToMsg(msg.replyTo.id)">
            <span class="reply-sender">{{ msg.replyTo.senderName }}: </span>
            <span class="reply-content">{{ msg.replyTo.content }}</span>
          </div>

          <div class="message-content-main relative">
            <!-- 悬停快捷按钮 -->
            <div class="message-actions">
              <button class="action-btn" @click="$emit('reply', msg)" title="回复">
                <el-icon><ChatLineSquare /></el-icon>
              </button>
              <button class="action-btn" @click="handleReaction(msg, '👍')" title="点赞">
                <span>👍</span>
              </button>
              <button class="action-btn" title="更多">
                <el-icon><MoreFilled /></el-icon>
              </button>
            </div>

            <el-dropdown trigger="contextmenu" @command="(cmd) => handleCommand(cmd, msg)" popper-class="message-context-menu">
              <div class="bubble" :class="msg.type">
                <span v-if="msg.type === 'TEXT'">{{ msg.content }}</span>
                <img v-else-if="msg.type === 'IMAGE'" 
                     :src="parseContent(msg).imageUrl" 
                     class="msg-image" 
                     @click="previewImage(parseContent(msg).imageUrl)" />
                <div v-else-if="msg.type === 'FILE'" class="msg-file" @click="downloadFile(parseContent(msg))">
                  <el-icon><Document /></el-icon>
                  <div class="file-info">
                    <span class="file-name">{{ parseContent(msg).fileName }}</span>
                    <span class="file-size">{{ formatSize(parseContent(msg).size) }}</span>
                  </div>
                </div>
                <div v-else-if="msg.type === 'VIDEO'" class="msg-video">
                  <video :src="parseContent(msg).videoUrl" controls class="video-preview"></video>
                </div>
                <div v-else-if="msg.type === 'VOICE' || msg.type === 'AUDIO'" class="msg-audio">
                  <audio :src="parseContent(msg).audioUrl || parseContent(msg).voiceUrl" controls></audio>
                </div>
                <div v-else-if="msg.type === 'SYSTEM'" class="msg-system">
                  {{ msg.content }}
                </div>
                <div v-else-if="msg.type === 'RECALLED'" class="msg-recalled">
                  <span class="material-icons-outlined">block</span>
                  <span>{{ msg.isOwn ? '你撤回了一条消息' : `${msg.senderName}撤回了一条消息` }}</span>
                </div>
                <span v-else>
                  [未知消息类型: {{ msg.type || '无type字段' }}]
                  <span v-if="!msg.type" class="debug-info">请查看控制台日志</span>
                </span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="reply">
                    <el-icon><ChatLineSquare /></el-icon> <span>回复</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="copy" v-if="msg.type === 'TEXT'">
                    <el-icon><CopyDocument /></el-icon> <span>复制</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="forward">
                    <el-icon><Share /></el-icon> <span>转发</span>
                  </el-dropdown-item>
                  <div class="menu-divider"></div>
                  <el-dropdown-item command="recall" v-if="msg.isOwn && canRecall(msg)">
                    <el-icon><RefreshLeft /></el-icon> <span>撤回</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" v-if="msg.isOwn" class="danger">
                    <el-icon><Delete /></el-icon> <span>删除</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="edit" v-if="msg.isOwn && msg.type === 'TEXT'">
                    <el-icon><Edit /></el-icon> <span>编辑</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <div class="message-footer">
            <div v-if="msg.isOwn" class="read-status">
              <span v-if="msg.isRead" class="read">已读</span>
              <el-popover
                v-else-if="msg.readCount > 0"
                placement="top"
                :width="200"
                trigger="hover"
                @before-enter="fetchReadUsers(msg)"
              >
                <template #reference>
                  <span class="read-count">{{ msg.readCount }}人已读</span>
                </template>
                <div v-loading="loadingReadUsers[msg.id]" class="read-users-list">
                  <div v-for="user in readUsersMap[msg.id]" :key="user.id" class="read-user-item">
                    <el-avatar :size="24" :src="user.avatar">{{ (user.name?.charAt(0) || '?').toUpperCase() }}</el-avatar>
                    <span>{{ user.name }}</span>
                  </div>
                  <div v-if="!loadingReadUsers[msg.id] && (!readUsersMap[msg.id] || readUsersMap[msg.id].length === 0)" class="empty">
                    加载中...
                  </div>
                </div>
              </el-popover>
              <span v-else class="unread">未读</span>
            </div>
            <div class="time">{{ formatTime(msg.timestamp) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 回到底部按钮 -->
    <Transition name="fade">
      <div v-if="showScrollToBottom" class="scroll-to-bottom-btn" @click="scrollToBottom()">
        <el-icon><ArrowDown /></el-icon>
        <span>回到最新</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, watch,  onMounted } from 'vue'
import { Document, Loading, ChatLineSquare, CopyDocument, Share, RefreshLeft, Delete, MoreFilled, ArrowDown, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessageReadUsers } from '@/api/im/message'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  messages: Array,
  currentUser: Object,
  loading: Boolean,
  sessionId: [String, Number]
})

const emit = defineEmits(['delete', 'recall', 'reply', 'load-more', 'edit'])

const listRef = ref(null)
const showImageViewer = ref(false)
const previewUrl = ref('')

const readUsersMap = ref({})
const loadingReadUsers = ref({})
const showScrollToBottom = ref(false)

const fetchReadUsers = async (msg) => {
  if (readUsersMap.value[msg.id] || loadingReadUsers.value[msg.id]) return
  
  loadingReadUsers.value[msg.id] = true
  try {
    const res = await getMessageReadUsers(props.sessionId, msg.id)
    if (res.code === 200) {
      readUsersMap.value[msg.id] = res.data
    }
  } catch (error) {
    console.error('获取已读用户失败', error)
  } finally {
    loadingReadUsers.value[msg.id] = false
  }
}

const getAvatarBgClass = (msg) => {
  if (msg.isOwn) return 'bg-blue-600'
  const colors = ['bg-blue-500', 'bg-orange-500', 'bg-emerald-500', 'bg-purple-500']
  return colors[(msg.senderId || 0) % colors.length]
}

const parseContent = (msg) => {
  try {
    return typeof msg.content === 'string' && (msg.type === 'IMAGE' || msg.type === 'FILE')
      ? JSON.parse(msg.content)
      : msg.content
  } catch (e) {
    return {}
  }
}

const formatTime = (ts) => {
  return new Date(ts).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const previewImage = (url) => {
  previewUrl.value = url
  showImageViewer.value = true
}

const downloadFile = (fileInfo) => {
  if (!fileInfo.fileUrl) return
  window.open(fileInfo.fileUrl, '_blank')
}

const formatTimeDivider = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  const diffDays = Math.floor((today - msgDate) / (1000 * 60 * 60 * 24))

  const timeStr = date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })

  if (diffDays === 0) return `今天 ${timeStr}`
  if (diffDays === 1) return `昨天 ${timeStr}`
  if (diffDays < 7) {
    const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    return `${weekDays[date.getDay()]} ${timeStr}`
  }

  return `${date.getMonth() + 1}月${date.getDate()}日 ${timeStr}`
}

const messagesWithDividers = computed(() => {
  const res = []
  props.messages.forEach((msg, index) => {
    let showDivider = false
    if (index === 0) {
      showDivider = true
    } else {
      const prevMsg = props.messages[index - 1]
      const timeDiff = msg.timestamp - prevMsg.timestamp
      // 30分钟间隔或每10条消息
      if (timeDiff > 30 * 60 * 1000 || index % 10 === 0) {
        showDivider = true
      }
    }

    if (showDivider) {
      res.push({ 
        isTimeDivider: true, 
        timeText: formatTimeDivider(msg.timestamp), 
        id: 'divider-' + msg.timestamp + '-' + index 
      })
    }
    res.push(msg)
  })
  return res
})

const scrollToBottom = (smooth = true) => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTo({
        top: listRef.value.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto'
      })
    }
  })
}

// Watch messages length to scroll
watch(() => props.messages.length, () => scrollToBottom())


// Can recall within 2 minutes
const canRecall = (msg) => {
  return (Date.now() - new Date(msg.timestamp).getTime()) < 2 * 60 * 1000
}

const handleCommand = (cmd, msg) => {
   if (cmd === 'copy') {
     navigator.clipboard.writeText(msg.content)
     ElMessage.success('已复制')
   } else if (cmd === 'reply') {
     emit('reply', msg)
   } else if (cmd === 'recall') {
     emit('recall', msg.id)
   } else if (cmd === 'delete') {
     ElMessageBox.confirm('确定删除这条消息吗？', '提示', {
       type: 'warning'
     }).then(() => {
       emit('delete', msg.id)
     })
   } else if (cmd === 'edit') {
     emit('edit', msg)
   }
 }

const handleReaction = (msg, reaction) => {
  ElMessage.success(`你对消息做出了 ${reaction} 反应`)
}

const scrollToMsg = (id) => {
  const el = listRef.value?.querySelector(`[data-id="${id}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

const handleScroll = () => {
  if (!listRef.value || props.loading) return
  
  const { scrollTop, clientHeight, scrollHeight } = listRef.value
  
  // 滚动到顶部加载更多
  if (scrollTop === 0) {
    emit('load-more')
  }
  
  // 检测是否接近底部（显示"回到底部"按钮）
  const distanceFromBottom = scrollHeight - scrollTop - clientHeight
  showScrollToBottom.value = distanceFromBottom > 300
}

// 保持滚动距离（用于向上加载时）
const maintainScroll = (oldHeight) => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight - oldHeight
    }
  })
}

defineExpose({ scrollToBottom, maintainScroll })
</script>

<style scoped>
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f7f8fa;
}
.time-divider {
  text-align: center;
  margin: 20px 0;
  color: #b3b3b3;
  font-size: 12px;
  line-height: 1;
  
  .time-text {
    background: rgba(0, 0, 0, 0.05);
    padding: 4px 12px;
    border-radius: 10px;
    display: inline-block;
  }
}
.message-item {
  display: flex;
  margin-bottom: 20px;
}
.message-item.is-own {
  flex-direction: row-reverse;
}
.avatar {
  margin: 0 12px;
  border-radius: 8px !important;
  flex-shrink: 0;
  font-weight: 500;
}
.bg-blue-600 { background-color: #2563eb; }
.bg-blue-500 { background-color: #3b82f6; }
.bg-orange-500 { background-color: #f97316; }
.bg-emerald-500 { background-color: #10b981; }
.bg-purple-500 { background-color: #a855f7; }
.content-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}
.sender-name {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
  padding-left: 4px;
}
.is-own .sender-name {
  text-align: right;
  padding-right: 4px;
}
.message-content-main {
  position: relative;
  
  &:hover {
    .message-actions {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.message-actions {
  position: absolute;
  top: -32px;
  left: 0;
  display: flex;
  background: #fff;
  border: 1px solid var(--dt-border-light);
  border-radius: 20px;
  padding: 2px 8px;
  box-shadow: var(--dt-shadow-md);
  opacity: 0;
  transform: translateY(5px);
  transition: all 0.2s ease-in-out;
  z-index: 10;
  gap: 4px;

  .action-btn {
    background: transparent;
    border: none;
    padding: 2px 6px;
    color: var(--dt-text-secondary);
    cursor: pointer;
    font-size: 16px;
    display: flex;
    align-items: center;
    border-radius: 10px;
    
    &:hover {
      background-color: var(--dt-bg-session-hover);
      color: var(--dt-brand-color);
    }
    
    span { font-size: 14px; }
  }
}

.is-own .message-actions {
  left: auto;
  right: 0;
}
.bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  font-size: 14px;
  word-break: break-word;
  line-height: 1.6;
  color: var(--dt-text-primary);
  position: relative;
  max-width: 460px;
  
  &::before {
    content: '';
    position: absolute;
    left: -8px;
    top: 14px;
    width: 0;
    height: 0;
    border-style: solid;
    border-width: 6px 8px 6px 0;
    border-color: transparent #fff transparent transparent;
  }
}
.is-own .bubble {
  background: linear-gradient(135deg, #1677ff 0%, #1890ff 100%);
  color: #ffffff;
  
  &::before {
    left: auto;
    right: -8px;
    border-width: 6px 0 6px 8px;
    border-color: transparent transparent transparent #1677ff;
  }
}
.msg-image {
  max-width: 100%;
  border-radius: 4px;
  cursor: pointer;
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 11px;
}

.is-own .message-footer {
  flex-direction: row-reverse;
}

.read-status {
  color: #bfbfbf;
  
  .read {
    color: #bfbfbf;
  }
  
  .unread {
    color: #0089ff;
  }
  
  .read-count {
    color: #0089ff;
    cursor: pointer;
    &:hover { text-decoration: underline; }
  }
}

.read-users-list {
  max-height: 200px;
  overflow-y: auto;
  
  .read-user-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 0;
    font-size: 13px;
    
    &:not(:last-child) {
      border-bottom: 1px solid #f0f0f0;
    }
  }
  
  .empty {
    text-align: center;
    color: #999;
    padding: 8px 0;
    font-size: 12px;
  }
}

.time {
  color: #bfbfbf;
}

.msg-file {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  background: rgba(0,0,0,0.02);
  padding: 8px;
  border-radius: 4px;
  &:hover { background: rgba(0,0,0,0.05); }
  
  .file-info {
    display: flex;
    flex-direction: column;
    overflow: hidden;
    
    .file-name {
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .file-size {
      font-size: 11px;
      color: #999;
    }
  }
}

.msg-video {
  max-width: 300px;
  .video-preview {
    width: 100%;
    border-radius: 4px;
  }
}

.reply-wrapper {
  background: #f0f0f0;
  border-left: 3px solid #0089ff;
  padding: 4px 8px;
  border-radius: 4px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #8c8c8c;
  cursor: pointer;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  
  .reply-sender {
    color: #434343;
    font-weight: 500;
  }
}

.is-own .reply-wrapper {
  background: rgba(0, 0, 0, 0.05);
  align-self: flex-end;
}

/* 语音消息样式 */
.msg-audio {
  padding: 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.02);
  
  audio {
    width: 200px;
    height: 32px;
  }
}

/* 系统消息样式 */
.msg-system {
  color: #8c8c8c;
  font-size: 12px;
  text-align: center;
  padding: 4px 12px;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 10px;
}

/* 撤回消息样式 */
.msg-recalled {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8c8c8c;
  font-size: 13px;
  font-style: italic;
  padding: 8px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
  
  .material-icons-outlined {
    font-size: 16px;
    color: #bfbfbf;
  }
}

/* 调试信息样式 */
.debug-info {
  display: inline-block;
  margin-left: 8px;
  font-size: 11px;
  color: #ff4d4f;
  padding: 2px 6px;
  background: #fff1f0;
  border-radius: 4px;
}

/* 回到底部按钮 */
.scroll-to-bottom-btn {
  position: absolute;
  right: 20px;
  bottom: 80px;
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 20px;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  font-size: 13px;
  color: #595959;
  z-index: 10;
  transition: all 0.2s;
  
  &:hover {
    background: #f5f5f5;
    border-color: #1677ff;
    color: #1677ff;
    box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  }
  
  .el-icon {
    font-size: 14px;
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>

<style lang="scss">
.message-context-menu {
  padding: 4px 0 !important;
  border-radius: 8px !important;
  box-shadow: var(--dt-shadow-lg) !important;
  border: 1px solid var(--dt-border-light) !important;

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px !important;
    font-size: 13px !important;
    color: var(--dt-text-secondary) !important;

    .el-icon {
      font-size: 16px;
      color: var(--dt-text-tertiary);
    }

    &:hover {
      background-color: var(--dt-bg-session-hover) !important;
      color: var(--dt-brand-color) !important;
      .el-icon { color: var(--dt-brand-color); }
    }

    &.danger {
      color: var(--dt-error-color) !important;
      &:hover { background-color: #fff1f0 !important; }
      .el-icon { color: var(--dt-error-color); }
    }
  }

  .menu-divider {
    height: 1px;
    background-color: var(--dt-border-light);
    margin: 4px 0;
  }
}

.dark .message-actions {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}
</style>
