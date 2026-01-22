<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <div v-if="loading" class="loading-wrapper"><el-icon class="is-loading"><Loading /></el-icon>数据加载中...</div>
    <div v-else-if="messages.length === 0" class="empty">暂无消息</div>
    
    <div v-for="msg in messagesWithDividers" :key="msg.id || msg.timeText" :data-id="msg.id" class="message-wrapper">
      <div v-if="msg.isTimeDivider" class="time-divider">{{ msg.timeText }}</div>
      <div v-else class="message-item" :class="{ 'is-own': msg.isOwn }">
        <el-avatar class="avatar" :size="36" :src="msg.senderAvatar">
          {{ (msg.senderName || '?').charAt(0) }}
        </el-avatar>
        <div class="content-wrapper">
          <div v-if="!msg.isOwn" class="sender-name">{{ msg.senderName }}</div>
          
          <div v-if="msg.replyTo" class="reply-wrapper" @click="scrollToMsg(msg.replyTo.id)">
            <span class="reply-sender">{{ msg.replyTo.senderName }}: </span>
            <span class="reply-content">{{ msg.replyTo.content }}</span>
          </div>

          <el-dropdown trigger="contextmenu" @command="(cmd) => handleCommand(cmd, msg)">
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
              <span v-else>[未知消息类型]</span>
            </div>
            <el-image-viewer 
              v-if="showImageViewer" 
              :url-list="[previewUrl]" 
              @close="showImageViewer = false" 
            />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="reply">回复</el-dropdown-item>
                <el-dropdown-item command="copy" v-if="msg.type === 'TEXT'">复制</el-dropdown-item>
                <el-dropdown-item command="recall" v-if="msg.isOwn && canRecall(msg)">撤回</el-dropdown-item>
                <el-dropdown-item command="delete" v-if="msg.isOwn">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
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
                    <el-avatar :size="24" :src="user.avatar">{{ user.name?.charAt(0) }}</el-avatar>
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
  </div>
</template>

<script setup>
import { computed, ref, nextTick, watch, onMounted } from 'vue'
import { Document, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessageReadUsers } from '@/api/im/message'

const props = defineProps({
  messages: Array,
  currentUser: Object,
  loading: Boolean,
  sessionId: [String, Number]
})

const emit = defineEmits(['delete', 'recall', 'reply', 'load-more'])

const listRef = ref(null)
const showImageViewer = ref(false)
const previewUrl = ref('')

const readUsersMap = ref({})
const loadingReadUsers = ref({})

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

const scrollToBottom = () => {
  nextTick(() => {
    if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
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
   }
 }

const scrollToMsg = (id) => {
  const el = listRef.value?.querySelector(`[data-id="${id}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

const handleScroll = () => {
  if (listRef.value.scrollTop === 0 && !props.loading) {
    emit('load-more')
  }
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
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 16px 0;
  
  &::before, &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: rgba(0, 0, 0, 0.04);
    margin: 0 12px;
  }
  
  .time-text {
    background: rgba(0, 0, 0, 0.04);
    padding: 2px 10px;
    border-radius: 4px;
    font-size: 11px;
    color: #bfbfbf;
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
  margin: 0 10px;
  background: #409eff;
  flex-shrink: 0;
}
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
.bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  font-size: 14px;
  word-break: break-all;
  line-height: 1.5;
  color: #262626;
}
.is-own .bubble {
  background: #0089ff;
  color: #ffffff;
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
</style>
