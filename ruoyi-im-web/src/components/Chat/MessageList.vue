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
          <div class="time">{{ formatTime(msg.timestamp) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, watch, onMounted } from 'vue'
import { Document, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  messages: Array,
  currentUser: Object,
  loading: Boolean
})

const emit = defineEmits(['delete', 'recall', 'reply', 'load-more'])

const listRef = ref(null)
const showImageViewer = ref(false)
const previewUrl = ref('')

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

const messagesWithDividers = computed(() => {
  const res = []
  let lastDate = ''
  props.messages.forEach(msg => {
    const d = new Date(msg.timestamp).toDateString()
    if (d !== lastDate) {
      res.push({ isTimeDivider: true, timeText: d, id: 'd-' + d + msg.id })
      lastDate = d
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
  padding: 20px;
  background: #f7f8fa;
}
.time-divider {
  text-align: center;
  color: #ccc;
  font-size: 12px;
  margin: 10px 0;
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
}
.content-wrapper {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}
.sender-name {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.is-own .sender-name {
  text-align: right;
}
.bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  font-size: 14px;
  word-break: break-all;
}
.is-own .bubble {
  background: #95ec69;
}
.msg-image {
  max-width: 100%;
  border-radius: 4px;
  cursor: pointer;
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
.time {
  font-size: 12px;
  color: #ccc;
  margin-top: 4px;
  text-align: right;
}
.is-own .time {
  text-align: left;
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
