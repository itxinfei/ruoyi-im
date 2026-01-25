<template>
  <el-dropdown 
    trigger="contextmenu" 
    @command="handleCommand" 
    popper-class="message-context-menu"
  >
    <div class="bubble" :class="[message.type, { 'is-own': message.isOwn }]">
      <!-- 文本消息 -->
      <div v-if="message.type === 'TEXT'" class="text-content-wrapper">
        <span>{{ message.content }}</span>
        <span v-if="message.isEdited" class="edited-tag">(已编辑)</span>
      </div>

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
        <el-dropdown-item command="todo">
          <el-icon><Checked /></el-icon> <span>设为待办</span>
        </el-dropdown-item>
        <el-dropdown-item command="multi-select">
          <el-icon><List /></el-icon> <span>多选</span>
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
import { Document, ChatLineSquare, CopyDocument, Share, RefreshLeft, Delete, Edit, InfoFilled, Checked, List } from '@element-plus/icons-vue'

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
  border-radius: 2px 14px 14px 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  font-size: 14px;
  word-break: break-word;
  line-height: 1.6;
  color: #1f2329;
  position: relative;
  max-width: 520px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #f0f1f2;
  
  .is-own {
    background: #0089ff;
    color: #ffffff;
    border-radius: 14px 2px 14px 14px;
    box-shadow: 0 4px 12px rgba(0, 137, 255, 0.2);
    border: none;
  }

  .text-content-wrapper {
    display: flex;
    flex-direction: column;
    
    .edited-tag {
      font-size: 11px;
      opacity: 0.7;
      margin-top: 2px;
      align-self: flex-end;
    }
  }

  &:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    &.is-own { box-shadow: 0 6px 16px rgba(0, 137, 255, 0.3); }
  }

  /* 不同类型消息的特殊样式 */
  &.IMAGE { 
    padding: 0; 
    border-radius: 8px; 
    background: transparent !important; 
    box-shadow: 0 2px 12px rgba(0,0,0,0.12);
    border: none;
    overflow: hidden;
  }
  
  &.VIDEO { 
    padding: 0; 
    border-radius: 8px; 
    background: #000 !important; 
    overflow: hidden;
  }
}

.msg-image { 
  max-width: 320px; 
  max-height: 400px; 
  border-radius: 8px; 
  display: block;
  cursor: zoom-in;
  transition: all 0.3s;
  &:hover { transform: scale(1.02); filter: brightness(0.95); }
}

.msg-file {
  display: flex; align-items: center; gap: 14px; cursor: pointer;
  background: #f8fafc; padding: 12px; border-radius: 10px;
  border: 1px solid #eef2f6;
  transition: all 0.2s;
  &:hover { background: #f1f5f9; border-color: #cbd5e1; }
  
  .el-icon { font-size: 36px; color: #1677ff; }
  .file-info { 
    display: flex; flex-direction: column; overflow: hidden; 
    .file-name { font-weight: 600; font-size: 14px; color: #1f2329; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .file-size { font-size: 12px; color: #64748b; margin-top: 4px; }
  }
}

.is-own .msg-file {
  background: rgba(255,255,255,0.12); border-color: rgba(255,255,255,0.2);
  &:hover { background: rgba(255,255,255,0.2); border-color: rgba(255,255,255,0.3); }
  .el-icon { color: #fff; }
  .file-name { color: #fff; }
  .file-size { color: rgba(255,255,255,0.8); }
}

.msg-video { 
  max-width: 300px; 
  .video-preview { width: 100%; border-radius: 4px; display: block; } 
}

.msg-recalled { 
  display: flex; align-items: center; gap: 6px; color: #8f959e; font-size: 13px; font-style: italic; 
}

.msg-system {
  font-size: 12px;
  color: #8f959e;
  text-align: center;
  width: 100%;
}

:global(.dark) {
  .bubble {
    background: #1e293b;
    color: #f1f5f9;
    border-color: #334155;
    box-shadow: 0 2px 4px rgba(0,0,0,0.3);
    
    &.is-own {
      background: #1d4ed8;
      border-color: transparent;
      box-shadow: 0 4px 16px rgba(29, 78, 216, 0.4);
    }
  }
  
  .msg-file {
    background: #0f172a;
    border-color: #334155;
    .file-name { color: #f1f5f9; }
    .file-size { color: #94a3b8; }
    &:hover { background: #1e293b; border-color: #475569; }
  }
}
</style>
