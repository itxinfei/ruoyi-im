<template>
  <el-dropdown 
    trigger="contextmenu" 
    @command="handleCommand" 
    popper-class="message-context-menu"
  >
    <div class="bubble" :class="[message.type, { 'is-own': message.isOwn }]">
      <!-- 文本消息 -->
      <span v-if="message.type === 'TEXT'">{{ message.content }}</span>

      <!-- 图片消息 -->
      <img v-else-if="message.type === 'IMAGE' && parsedContent.imageUrl" 
           :src="parsedContent.imageUrl" 
           class="msg-image" 
           @click="$emit('preview', parsedContent.imageUrl)" />

      <!-- 文件消息 -->
      <div v-else-if="message.type === 'FILE'" class="msg-file" @click="$emit('download', parsedContent)">
        <el-icon><Document /></el-icon>
        <div class="file-info">
          <span class="file-name">{{ parsedContent.fileName || '未知文件' }}</span>
          <span class="file-size">{{ formatSize(parsedContent.size) }}</span>
        </div>
      </div>

      <!-- 视频消息 -->
      <div v-else-if="message.type === 'VIDEO'" class="msg-video">
        <video v-if="parsedContent.videoUrl" :src="parsedContent.videoUrl" controls class="video-preview"></video>
      </div>

      <!-- 系统消息 -->
      <div v-else-if="message.type === 'SYSTEM'" class="msg-system">
        {{ message.content }}
      </div>

      <!-- 撤回消息 -->
      <div v-else-if="message.type === 'RECALLED'" class="msg-recalled">
        <span class="material-icons-outlined">block</span>
        <span>{{ message.isOwn ? '你撤回了一条消息' : `${message.senderName}撤回了一条消息` }}</span>
      </div>

      <span v-else>[{{ message.type }}]</span>
    </div>

    <!-- 右键菜单：回归 Slot + el-icon 标准写法 -->
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item v-if="!message.isOwn && sessionType === 'GROUP'" command="at">
          <el-icon><InfoFilled /></el-icon> <span>@ 提及</span>
        </el-dropdown-item>
        <el-dropdown-item command="reply">
          <el-icon><ChatLineSquare /></el-icon> <span>回复</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="message.type === 'TEXT'" command="copy">
          <el-icon><CopyDocument /></el-icon> <span>复制</span>
        </el-dropdown-item>
        <el-dropdown-item command="forward">
          <el-icon><Share /></el-icon> <span>转发</span>
        </el-dropdown-item>
        
        <el-dropdown-item v-if="message.isOwn && canRecall" command="recall" divided>
          <el-icon><RefreshLeft /></el-icon> <span>撤回</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="message.isOwn" command="delete" class="danger" :divided="!(message.isOwn && canRecall)">
          <el-icon><Delete /></el-icon> <span>删除</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="message.isOwn && message.type === 'TEXT'" command="edit">
          <el-icon><Edit /></el-icon> <span>编辑</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { Document, ChatLineSquare, CopyDocument, Share, RefreshLeft, Delete, Edit, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

const emit = defineEmits(['command', 'preview', 'download', 'at'])

const handleCommand = (cmd) => {
  if (!cmd) return
  if (cmd === 'at') emit('at', props.message)
  else emit('command', cmd)
}

const parsedContent = computed(() => {
  try {
    if (!props.message || !props.message.content) return {}
    const isMedia = ['IMAGE', 'FILE', 'VIDEO', 'VOICE', 'AUDIO'].includes(props.message.type)
    return (typeof props.message.content === 'string' && isMedia)
      ? JSON.parse(props.message.content)
      : (props.message.content || {})
  } catch (e) { return {} }
})

const canRecall = computed(() => {
  if (!props.message?.timestamp) return false
  return (Date.now() - new Date(props.message.timestamp).getTime()) < 2 * 60 * 1000
})

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped lang="scss">
.bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  font-size: 14px;
  word-break: break-word;
  line-height: 1.6;
  color: #1f2329;
  position: relative;
  max-width: 460px;
  
  &.is-own {
    background: linear-gradient(135deg, #1677ff 0%, #1890ff 100%);
    color: #ffffff;
  }
}

.msg-image { max-width: 100%; border-radius: 4px; cursor: pointer; }
.msg-file {
  display: flex; align-items: center; gap: 12px; cursor: pointer;
  background: rgba(0,0,0,0.02); padding: 8px; border-radius: 4px;
  .file-info { display: flex; flex-direction: column; overflow: hidden; .file-name { font-weight: 500; } }
}
.msg-video { max-width: 300px; .video-preview { width: 100%; border-radius: 4px; } }
.msg-recalled { display: flex; align-items: center; gap: 6px; color: #8c8c8c; font-size: 13px; }
</style>
