<template>
  <el-dropdown 
    trigger="contextmenu" 
    @command="handleCommand" 
    popper-class="message-context-menu"
  >
    <div class="bubble-wrapper" :class="{ 'is-own': message.isOwn }">
      <!-- 悬hover操作按钮 (钉钉风格) -->
      <div class="bubble-actions" @click.stop>
        <button class="action-btn" @click="$emit('reply', message)" title="回复">
          <span class="material-icons-outlined">reply</span>
        </button>
        <button class="action-btn" @click="$emit('command', 'forward', message)" title="转发">
          <span class="material-icons-outlined">forward</span>
        </button>
        <button class="action-btn" @click="$emit('command', 'favorite', message)" title="收藏">
          <span class="material-icons-outlined">star</span>
        </button>
        <button class="action-btn" @click="$emit('command', 'multi-select', message)" title="多选">
          <span class="material-icons-outlined">checklist</span>
        </button>
        <button class="action-btn" v-if="message.isOwn" @click="$emit('command', 'recall', message)" title="撤回">
          <span class="material-icons-outlined">undo</span>
        </button>
        <button class="action-btn" v-if="message.isOwn" @click="$emit('command', 'delete', message)" title="删除">
          <span class="material-icons-outlined">delete</span>
        </button>
      </div>

      <div v-if="!['VOICE', 'LOCATION', 'LINK'].includes(message.type)" class="bubble" :class="[message.isOwn ? 'is-own' : '', message.type]">
        <!-- 引用消息区块 (如果该消息是回复某人的) -->
        <div v-if="message.replyTo" class="bubble-reply-ref" @click.stop="$emit('scroll-to', message.replyTo.id)">
          <span class="ref-user">{{ message.replyTo.senderName }}:</span>
          <span class="ref-content">{{ message.replyTo.content }}</span>
        </div>

        <!-- 文本消息 (支持链接识别) -->
        <div v-if="message.type === 'TEXT'" class="text-content-wrapper">
          <div class="main-text" v-html="parsedTextWithLinks"></div>
          <span v-if="message.isEdited" class="edited-tag">(已编辑)</span>
        </div>

        <!-- 图片消息 -->
        <img
          v-else-if="message.type === 'IMAGE' && parsedContent.imageUrl"
          :src="parsedContent.imageUrl"
          class="msg-image"
          @click="$emit('preview', parsedContent.imageUrl)"
        />

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

      <!-- 语音消息 -->
      <VoiceMessageBubble v-else-if="message.type === 'VOICE'" :message="message" @command="(cmd, msg) => emit('command', cmd, msg)" />

      <!-- 位置消息 -->
      <LocationMessageBubble v-else-if="message.type === 'LOCATION'" :message="message" @command="(cmd, msg) => emit('command', cmd, msg)" />

      <!-- 链接卡片消息 -->
      <LinkCardMessage v-else-if="message.type === 'LINK'" :message="message" @command="(cmd, msg) => emit('command', cmd, msg)" />
    </div>

    <!-- 右键菜单：精品化菜单项 -->
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="copy" v-if="message.type === 'TEXT'">
          <el-icon><CopyDocument /></el-icon> <span>复制</span>
        </el-dropdown-item>
        <el-dropdown-item command="reply">
          <el-icon><ChatLineSquare /></el-icon> <span>回复</span>
        </el-dropdown-item>
        <el-dropdown-item command="at" v-if="!message.isOwn && sessionType === 'GROUP'">
          <el-icon><InfoFilled /></el-icon> <span>@ 提及</span>
        </el-dropdown-item>
        <el-dropdown-item command="forward" divided>
          <el-icon><Share /></el-icon> <span>转发</span>
        </el-dropdown-item>
        <el-dropdown-item command="favorite">
          <el-icon><Star /></el-icon> <span>收藏</span>
        </el-dropdown-item>
        <el-dropdown-item command="todo">
          <el-icon><Checked /></el-icon> <span>设为待办</span>
        </el-dropdown-item>
        
        <el-dropdown-item v-if="message.isOwn && canRecall" command="recall" divided class="danger">
          <el-icon><RefreshLeft /></el-icon> <span>撤回</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="message.isOwn" command="delete" class="danger">
          <el-icon><Delete /></el-icon> <span>删除</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="message.isOwn && message.type === 'TEXT'" command="edit" divided>
          <el-icon><Edit /></el-icon> <span>编辑</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { Document, ChatLineSquare, CopyDocument, Share, RefreshLeft, Delete, Edit, InfoFilled, Checked, Star } from '@element-plus/icons-vue'
import VoiceMessageBubble from './VoiceMessageBubble.vue'
import LocationMessageBubble from './LocationMessageBubble.vue'
import LinkCardMessage from './LinkCardMessage.vue'

const props = defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

const emit = defineEmits(['command', 'preview', 'download', 'at', 'scroll-to', 'reply'])

const handleCommand = (cmd) => {
  if (!cmd) return
  if (cmd === 'at') emit('at', props.message)
  else emit('command', cmd, props.message)
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

// 解析文本中的URL并转换为可点击链接
const parsedTextWithLinks = computed(() => {
  if (!props.message?.content || props.message.type !== 'TEXT') return ''
  
  const text = props.message.content
  // URL正则表达式
  const urlRegex = /(https?:\/\/[^\s<>"{}|\\^`\[\]]+)/g
  
  return text.replace(urlRegex, (url) => {
    // 确保URL以http/https开头
    const displayUrl = url.length > 40 ? url.substring(0, 40) + '...' : url
    return `<a href="${url}" target="_blank" class="message-link" onclick="event.stopPropagation()">${displayUrl}</a>`
  })
})

const canRecall = computed(() => {
  if (!props.message?.timestamp) return false
  return (Date.now() - new Date(props.message.timestamp).getTime()) < 5 * 60 * 1000 // 增加到5分钟
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
// ============================================================================
// 气泡容器 (用于包裹气泡和操作按钮)
// ============================================================================
.bubble-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  position: relative;
  
  // 默认隐藏操作按钮
  .bubble-actions {
    opacity: 0;
    transform: translateX(8px);
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    display: flex;
    align-items: center;
    gap: 2px;
    padding: 2px 4px;
    border-radius: 6px;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    .action-btn {
      width: 28px;
      height: 28px;
      border: none;
      background: transparent;
      border-radius: 4px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #5c5c5c;
      transition: all 0.15s;
      
      .material-icons-outlined {
        font-size: 18px;
      }
      
      &:hover {
        background: #f2f3f5;
        color: #1677ff;
      }
      
      &:active {
        background: #e8f4ff;
      }
    }
  }
  
  // 悬停时显示操作按钮
  &:hover .bubble-actions {
    opacity: 1;
    transform: translateX(0);
  }
  
  // 自己的消息，操作按钮位置调整
  &.is-own {
    flex-direction: row-reverse;
    
    .bubble-actions {
      flex-direction: row-reverse;
    }
  }
}

.bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 2px 12px 12px 2px;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.04);
  font-size: 14px;
  word-break: break-word;
  line-height: 1.6;
  color: #262626;
  position: relative;
  max-width: 520px;
  transition: box-shadow 0.2s;
  border: none;

  // 对方消息圆角: 2px 12px 12px 2px
  &.is-own {
    background: #e8f4ff;
    color: #1f2329;
    border-radius: 12px 12px 2px 12px;
    box-shadow: 0 1px 1px rgba(22, 119, 255, 0.1);
  }

  &:hover { 
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08); 
  }

  .bubble-reply-ref {
    display: block;
    background: rgba(0, 0, 0, 0.06);
    border-left: 3px solid #1677ff;
    padding: 8px 12px;
    margin-bottom: 8px;
    border-radius: 6px;
    font-size: 13px;
    color: #5c5c5c;
    cursor: pointer;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    user-select: none;
    .ref-user { font-weight: 600; margin-right: 6px; color: #1677ff; }
    &:hover { background: rgba(0, 0, 0, 0.1); }
  }

  .text-content-wrapper {
    display: flex; flex-direction: column;
    .main-text { 
      white-space: pre-wrap; 
      // 链接样式
      :deep(.message-link) {
        color: #1677ff;
        text-decoration: underline;
        cursor: pointer;
        transition: opacity 0.2s;
        
        &:hover {
          opacity: 0.8;
        }
      }
    }
    .edited-tag { font-size: 11px; opacity: 0.7; margin-top: 4px; align-self: flex-end; font-style: italic; }
  }

  &.IMAGE { padding: 3px; border-radius: 12px; background: transparent !important; box-shadow: none; }
  &.VIDEO { padding: 0; border-radius: 12px; background: #000 !important; overflow: hidden; }
}

.msg-image { 
  max-width: 300px; 
  max-height: 300px; 
  border-radius: 10px; 
  display: block; 
  cursor: zoom-in; 
  transition: transform 0.2s, box-shadow 0.2s; 
  object-fit: cover;
  &:hover { transform: scale(1.02); box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15); } 
}

.msg-file {
  display: flex; align-items: center; gap: 12px; cursor: pointer;
  background: rgba(255, 255, 255, 0.9); padding: 12px 14px; border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.06); min-width: 200px; transition: all 0.15s;
  .el-icon { font-size: 36px; color: #1677ff; flex-shrink: 0; }
  .file-info { 
    display: flex; flex-direction: column; overflow: hidden; flex: 1;
    .file-name { font-weight: 500; font-size: 14px; color: #262626; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .file-size { font-size: 12px; color: #999; margin-top: 2px; }
  }
  &:hover { background: #fff; border-color: #1677ff; box-shadow: 0 2px 8px rgba(22, 119, 255, 0.1); }
}

.is-own .msg-file {
  background: rgba(255, 255, 255, 0.15); border-color: rgba(255, 255, 255, 0.2);
  .file-name { color: #fff; }
  .file-size { color: rgba(255, 255, 255, 0.7); }
  .el-icon { color: #fff; }
  &:hover { background: rgba(255, 255, 255, 0.25); }
}

.msg-recalled { display: flex; align-items: center; gap: 6px; color: #999; font-size: 13px; font-style: italic; padding: 4px 0;
  .material-icons-outlined { font-size: 16px; }
}
.msg-system { font-size: 12px; color: #999; text-align: center; width: 100%; padding: 8px 0; }

:global(.dark) {
  .bubble {
    background: #1e293b; color: #e2e8f0;
    &.is-own { background: #1677ff; color: #fff; }
    .bubble-reply-ref { background: rgba(255, 255, 255, 0.08); &:hover { background: rgba(255, 255, 255, 0.12); } }
  }
  .msg-file { background: rgba(30, 41, 59, 0.8); border-color: rgba(255, 255, 255, 0.1);
    .file-name { color: #e2e8f0; }
    &:hover { background: rgba(30, 41, 59, 1); border-color: #1677ff; }
  }
  .is-own .msg-file { background: rgba(255, 255, 255, 0.15); border-color: rgba(255, 255, 255, 0.2);
    &:hover { background: rgba(255, 255, 255, 0.25); }
  }
}
</style>
