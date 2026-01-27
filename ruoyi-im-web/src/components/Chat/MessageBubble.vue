<template>
  <el-dropdown
    trigger="contextmenu"
    @command="handleCommand"
    popper-class="message-context-menu"
  >
    <div
      class="bubble"
      :class="[message.type, { 'is-own': message.isOwn, 'is-selected': isSelected }]"
      @click="handleClick"
    >
      <!-- 引用消息区块 (如果该消息是回复某人的) -->
      <div v-if="message.replyTo" class="bubble-reply-ref" @click.stop="$emit('scroll-to', message.replyTo.id)">
        <span class="ref-user">{{ message.replyTo.senderName }}:</span>
        <span class="ref-content">{{ message.replyTo.content }}</span>
      </div>

      <!-- 文本消息 -->
      <div v-if="message.type === 'TEXT'" class="text-content-wrapper">
        <span class="main-text">{{ message.content }}</span>
        <span v-if="message.isEdited" class="edited-tag">(已编辑)</span>
      </div>

      <!-- 图片消息 -->
      <viewer v-else-if="message.type === 'IMAGE' && parsedContent.imageUrl"
              :images="[parsedContent.imageUrl]"
              :options="{ toolbar: true, url: 'data-source', title: false }"
              class="image-viewer">
        <img :src="parsedContent.imageUrl"
             class="msg-image"
             data-source="click" />
      </viewer>

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

      <!-- 消息状态图标 -->
      <div v-if="message.isOwn" class="message-status">
        <el-icon v-if="message.status === 'sending'" class="is-loading" color="#909399">
          <Loading />
        </el-icon>
        <el-icon v-else-if="message.status === 'sent'" color="#909399">
          <Check />
        </el-icon>
        <el-icon v-else-if="message.status === 'read'" color="#909399">
          <Check />
          <Check />
        </el-icon>
        <el-icon v-else-if="message.status === 'failed'" color="#f56c6c" @click="handleRetry">
          <WarningFilled />
        </el-icon>
      </div>
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
import { useStore } from 'vuex'
import { Document, ChatLineSquare, CopyDocument, Share, RefreshLeft, Delete, Edit, InfoFilled, Checked, Loading, WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

const emit = defineEmits(['command', 'preview', 'download', 'at', 'scroll-to', 'retry'])

const store = useStore()
const selectedMessages = computed(() => store.state.im.message.selectedMessages)

const isSelected = computed(() => selectedMessages.value.has(props.message.id))

const handleClick = (event) => {
  if (event.ctrlKey || event.metaKey) {
    // Ctrl + 点击：不连续多选
    toggleSelection()
    event.stopPropagation()
  } else if (event.shiftKey) {
    // Shift + 点击：连续多选
    rangeSelection()
    event.stopPropagation()
  } else {
    // 普通点击：不做处理，让父组件处理
    emit('command', 'click', props.message)
  }
}

const toggleSelection = () => {
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', props.message.id)
}

const rangeSelection = () => {
  // TODO: 实现连续选择逻辑
  // 需要获取当前会话的所有消息，然后找到当前消息和最后选中的消息之间的所有消息
  toggleSelection()
}

const handleCommand = (cmd) => {
  if (!cmd) return
  if (cmd === 'at') emit('at', props.message)
  else emit('command', cmd, props.message)
}

const handleRetry = () => {
  emit('retry', props.message)
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
.bubble {
  background: var(--dt-bubble-left-bg);
  padding: 10px 14px;
  border-radius: 4px 16px 16px 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
  font-size: 14px;
  word-break: break-word;
  line-height: 1.6;
  color: #1f2329;
  position: relative;
  max-width: 520px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;

  &.is-selected {
    border: 2px solid #1890ff;
    background-color: #e6f7ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
  }

  &.is-own {
    background: var(--dt-bubble-right-bg);
    color: #1f2329;
    border-radius: 16px 4px 16px 16px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    border: none;

    &.is-selected {
      border: 2px solid #1890ff;
      background-color: #e6f7ff;
    }
  }

  /* 引用回复展示 */
  .bubble-reply-ref {
    display: block;
    background: rgba(0, 0, 0, 0.05);
    border-left: 2px solid #0089ff;
    padding: 6px 10px;
    margin-bottom: 8px;
    border-radius: 4px;
    font-size: 12px;
    color: #64748b;
    cursor: pointer;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    user-select: none;
    .ref-user { font-weight: 600; margin-right: 4px; color: #1f2329; }
    &:hover { background: rgba(0, 0, 0, 0.08); }
  }

  .text-content-wrapper {
    display: flex; flex-direction: column;
    .main-text { white-space: pre-wrap; }
    .edited-tag { font-size: 11px; opacity: 0.5; margin-top: 2px; align-self: flex-end; }
  }

  &:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.08); }

  &.IMAGE { padding: 2px; border-radius: 12px; background: #fff !important; border: 1px solid #f0f1f2; }
  &.VIDEO { padding: 0; border-radius: 12px; background: #000 !important; }
}

.msg-image { max-width: 320px; max-height: 400px; border-radius: 10px; display: block; cursor: zoom-in; transition: transform 0.3s; &:hover { transform: scale(1.01); } }

.message-status {
  display: flex;
  align-items: center;
  margin-left: 5px;
  font-size: 14px;
  cursor: pointer;

  .el-icon {
    margin: 0 1px;
  }
}

.msg-file {
  display: flex; align-items: center; gap: 14px; cursor: pointer;
  background: #f8fafc; padding: 12px; border-radius: 10px; border: 1px solid #eef2f6;
  .el-icon { font-size: 32px; color: #1677ff; }
  .file-info { 
    display: flex; flex-direction: column; overflow: hidden; 
    .file-name { font-weight: 600; font-size: 14px; color: #1f2329; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .file-size { font-size: 11px; color: #8f959e; margin-top: 2px; }
  }
}

.msg-recalled { display: flex; align-items: center; gap: 6px; color: #8f959e; font-size: 13px; font-style: italic; }
.msg-system { font-size: 12px; color: #8f959e; text-align: center; width: 100%; margin: 8px 0; }

:global(.dark) {
  .bubble {
    background: #1e293b; color: #f1f5f9; border-color: #334155;
    &.is-own { background: #1d4ed8; color: #fff; }
    .bubble-reply-ref { background: rgba(255, 255, 255, 0.05); color: #94a3b8; .ref-user { color: #f1f5f9; } }
  }
  .msg-file { background: #0f172a; border-color: #334155; .file-name { color: #f1f5f9; } }
}
</style>
