<template>
  <el-dropdown trigger="contextmenu" @command="$emit('command', $event)" popper-class="message-context-menu">
    <div class="bubble" :class="[message.type, { 'is-own': message.isOwn }]">
      <!-- 文本消息 -->
      <span v-if="message.type === 'TEXT'">{{ message.content }}</span>

      <!-- 图片消息 -->
      <img v-else-if="message.type === 'IMAGE'" 
           :src="parsedContent.imageUrl" 
           class="msg-image" 
           @click="$emit('preview', parsedContent.imageUrl)" />

      <!-- 文件消息 -->
      <div v-else-if="message.type === 'FILE'" class="msg-file" @click="$emit('download', parsedContent)">
        <el-icon><Document /></el-icon>
        <div class="file-info">
          <span class="file-name">{{ parsedContent.fileName }}</span>
          <span class="file-size">{{ formatSize(parsedContent.size) }}</span>
        </div>
      </div>

      <!-- 视频消息 -->
      <div v-else-if="message.type === 'VIDEO'" class="msg-video">
        <video :src="parsedContent.videoUrl" controls class="video-preview"></video>
      </div>

      <!-- 语音消息 -->
      <div v-else-if="message.type === 'VOICE' || message.type === 'AUDIO'" class="msg-audio">
        <audio :src="parsedContent.audioUrl || parsedContent.voiceUrl" controls></audio>
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

      <!-- 未知类型 -->
      <span v-else>
        [未知消息类型: {{ message.type || '无type字段' }}]
      </span>
    </div>

    <!-- 右键菜单 -->
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="at" v-if="!message.isOwn && sessionType === 'GROUP'">
          <el-icon><InfoFilled /></el-icon> <span>@ 提及</span>
        </el-dropdown-item>
        <el-dropdown-item command="reply">
          <el-icon><ChatLineSquare /></el-icon> <span>回复</span>
        </el-dropdown-item>
        <el-dropdown-item command="copy" v-if="message.type === 'TEXT'">
          <el-icon><CopyDocument /></el-icon> <span>复制</span>
        </el-dropdown-item>
        <el-dropdown-item command="forward">
          <el-icon><Share /></el-icon> <span>转发</span>
        </el-dropdown-item>
        <div class="menu-divider"></div>
        <el-dropdown-item command="recall" v-if="message.isOwn && canRecall">
          <el-icon><RefreshLeft /></el-icon> <span>撤回</span>
        </el-dropdown-item>
        <el-dropdown-item command="delete" v-if="message.isOwn" class="danger">
          <el-icon><Delete /></el-icon> <span>删除</span>
        </el-dropdown-item>
        <el-dropdown-item command="edit" v-if="message.isOwn && message.type === 'TEXT'">
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
  message: {
    type: Object,
    required: true
  },
  sessionType: {
    type: String,
    default: 'PRIVATE'
  }
})

defineEmits(['command', 'preview', 'download', 'at'])

const parsedContent = computed(() => {
  try {
    return typeof props.message.content === 'string' && (props.message.type === 'IMAGE' || props.message.type === 'FILE' || props.message.type === 'VIDEO' || props.message.type === 'VOICE' || props.message.type === 'AUDIO')
      ? JSON.parse(props.message.content)
      : props.message.content
  } catch (e) {
    return {}
  }
})

const canRecall = computed(() => {
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

  &.is-own {
    background: linear-gradient(135deg, #1677ff 0%, #1890ff 100%);
    color: #ffffff;
    
    &::before {
      left: auto;
      right: -8px;
      border-width: 6px 0 6px 8px;
      border-color: transparent transparent transparent #1677ff;
    }
  }
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

.msg-audio {
  padding: 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.02);
  audio { width: 200px; height: 32px; }
}

.msg-system {
  color: #8c8c8c;
  font-size: 12px;
  text-align: center;
  padding: 4px 12px;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 10px;
}

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
  .material-icons-outlined { font-size: 16px; color: #bfbfbf; }
}

.menu-divider {
  height: 1px;
  background-color: var(--dt-border-light);
  margin: 4px 0;
}
</style>
