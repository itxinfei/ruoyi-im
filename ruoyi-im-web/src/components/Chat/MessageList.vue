<template>
  <div class="message-list" ref="listRef">
    <div v-if="loading" class="loading-wrapper"><el-spinner /></div>
    <div v-else-if="messages.length === 0" class="empty">暂无消息</div>
    
    <div v-for="msg in messagesWithDividers" :key="msg.id || msg.timeText" class="message-wrapper">
      <div v-if="msg.isTimeDivider" class="time-divider">{{ msg.timeText }}</div>
      <div v-else class="message-item" :class="{ 'is-own': msg.isOwn }">
        <el-avatar class="avatar" :size="36" :src="msg.senderAvatar">
          {{ (msg.senderName || '?').charAt(0) }}
        </el-avatar>
        <div class="content-wrapper">
          <div v-if="!msg.isOwn" class="sender-name">{{ msg.senderName }}</div>
          
            <el-dropdown trigger="contextmenu" @command="(cmd) => handleCommand(cmd, msg)">
              <div class="bubble" :class="msg.type">
                <span v-if="msg.type === 'TEXT'">{{ msg.content }}</span>
                <img v-else-if="msg.type === 'IMAGE'" 
                     :src="parseContent(msg).imageUrl" 
                     class="msg-image" 
                     @click="previewImage(parseContent(msg).imageUrl)" />
                <div v-else-if="msg.type === 'FILE'" class="msg-file">
                  <el-icon><Document /></el-icon>
                  <span>{{ parseContent(msg).fileName }}</span>
                </div>
                <span v-else>[未知消息类型]</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
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
import { computed, ref, nextTick, watch } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  messages: Array,
  currentUser: Object,
  loading: Boolean
})

const emit = defineEmits(['delete', 'recall'])

const listRef = ref(null)

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

defineExpose({ scrollToBottom })
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
  gap: 8px;
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
</style>
