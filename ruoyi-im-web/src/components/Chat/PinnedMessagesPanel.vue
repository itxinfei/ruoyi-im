<template>
  <div class="pinned-messages-panel">
    <!-- 头部 -->
    <div class="panel-header">
      <div class="header-left">
        <span class="material-icons-outlined header-icon">push_pin</span>
        <span class="header-title">置顶消息</span>
        <span
          v-if="pinnedMessages.length > 0"
          class="header-count"
        >{{ pinnedMessages.length }}</span>
      </div>
      <div class="header-actions">
        <el-button
          v-if="pinnedMessages.length > 0"
          type="danger"
          size="small"
          text
          @click="handleClearAll"
        >
          <el-icon><Delete /></el-icon>
          全部取消
        </el-button>
        <el-button
          type="default"
          size="small"
          text
          @click="$emit('close')"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="panel-content">
      <!-- 空状态 -->
      <div
        v-if="pinnedMessages.length === 0"
        class="empty-state"
      >
        <span class="material-icons-outlined empty-icon">push_pin</span>
        <span class="empty-text">暂无置顶消息</span>
        <span class="empty-hint">长按消息，选择「置顶」可将重要消息固定</span>
      </div>

      <!-- 置顶消息列表 -->
      <div
        v-else
        class="pinned-list"
      >
        <div
          v-for="msg in pinnedMessages"
          :key="msg.id"
          class="pinned-item"
          @click="handleMessageClick(msg)"
        >
          <!-- 消息内容预览 -->
          <div class="item-content">
            <div class="item-header">
              <DingtalkAvatar
                :src="msg.senderAvatar"
                :name="msg.senderName"
                :user-id="msg.senderId"
                :size="28"
                shape="square"
              />
              <span class="sender-name">{{ msg.senderName }}</span>
              <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
            </div>
            <div class="item-body">
              <div
                v-if="msg.type === 'TEXT'"
                class="content-preview text-preview"
              >
                {{ getContentPreview(msg) }}
              </div>
              <div
                v-else-if="msg.type === 'IMAGE'"
                class="content-preview image-preview"
              >
                <span class="material-icons-outlined">image</span>
                <span>[图片]</span>
              </div>
              <div
                v-else-if="msg.type === 'FILE'"
                class="content-preview file-preview"
              >
                <span class="material-icons-outlined">description</span>
                <span>[文件] {{ getFileName(msg) }}</span>
              </div>
              <div
                v-else-if="msg.type === 'VOICE'"
                class="content-preview voice-preview"
              >
                <span class="material-icons-outlined">mic</span>
                <span>[语音]</span>
              </div>
              <div
                v-else
                class="content-preview"
              >
                <span>[{{ msg.type }}]</span>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div
            class="item-actions"
            @click.stop
          >
            <el-button
              type="danger"
              size="small"
              text
              title="取消置顶"
              @click="handleUnpin(msg)"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Close, Delete } from '@element-plus/icons-vue'
import { pinMessage, unpinMessage } from '@/api/im/message'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { formatChatTime } from '@/utils/format'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'scroll-to-message', 'update'])

// 置顶消息列表
const pinnedMessages = computed(() => {
  return props.messages.filter(msg => msg.isPinned).sort((a, b) => {
    // 按置顶时间倒序（这里使用消息时间作为替代）
    return (b.pinTime || b.timestamp) - (a.pinTime || a.timestamp)
  })
})

// 获取内容预览
const getContentPreview = msg => {
  if (!msg.content) {return ''}
  if (typeof msg.content === 'string') {
    return msg.content.length > 50 ? msg.content.substring(0, 50) + '...' : msg.content
  }
  if (typeof msg.content === 'object') {
    return msg.content.text || msg.content.content || ''
  }
  return ''
}

// 获取文件名
const getFileName = msg => {
  if (!msg.content) {return ''}
  if (typeof msg.content === 'object') {
    return msg.content.fileName || msg.content.name || ''
  }
  return ''
}

// 格式化时间
const formatTime = formatChatTime

// 点击消息 - 滚动到对应位置
const handleMessageClick = msg => {
  emit('scroll-to-message', msg.id)
  emit('close')
}

// 取消单个置顶
const handleUnpin = async msg => {
  try {
    await unpinMessage(msg.id)
    ElMessage.success('已取消置顶')
    emit('update', { messageId: msg.id, isPinned: false })
  } catch (error) {
    console.error('取消置顶失败:', error)
    ElMessage.error('取消置顶失败')
  }
}

// 全部取消置顶
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要取消 ${pinnedMessages.value.length} 条置顶消息吗？`,
      '取消全部置顶',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 批量取消置顶
    const promises = pinnedMessages.value.map(msg => unpinMessage(msg.id))
    await Promise.all(promises)

    ElMessage.success('已取消全部置顶')
    // 批量更新状态
    pinnedMessages.value.forEach(msg => {
      emit('update', { messageId: msg.id, isPinned: false })
    })
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.pinned-messages-panel {
  width: var(--dt-contact-panel-width);
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
  border-left: 1px solid var(--dt-border-light);
}

// 头部
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--dt-border-lighter);
  background: var(--dt-bg-hover);

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .header-icon {
      font-size: 18px;
      color: var(--dt-brand-color);
    }

    .header-title {
      font-size: 15px;
      font-weight: 600;
      color: var(--dt-text-primary);
    }

    .header-count {
      font-size: 12px;
      color: #fff;
      background: var(--dt-brand-color);
      padding: 2px 6px;
      border-radius: var(--dt-radius-lg);
      min-width: 18px;
      text-align: center;
    }
  }

  .header-actions {
    display: flex;
    gap: 4px;
  }
}

// 内容区域
.panel-content {
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border);
    border-radius: var(--dt-radius-sm);

    &:hover {
      background: var(--dt-border-dark);
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  color: var(--dt-text-tertiary);
  text-align: center;

  .empty-icon {
    font-size: 48px;
    opacity: 0.3;
    margin-bottom: 12px;
  }

  .empty-text {
    font-size: 14px;
    color: var(--dt-text-secondary);
    margin-bottom: 6px;
  }

  .empty-hint {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}

// 置顶消息列表
.pinned-list {
  padding: 8px;
}

.pinned-item {
  display: flex;
  align-items: stretch;
  gap: 8px;
  padding: 10px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  background: var(--dt-bg-body);
  margin-bottom: 8px;

  &:hover {
    background: var(--dt-bg-hover);
    .item-actions {
      opacity: 1;
    }
  }

  .item-content {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;

    .item-header {
      display: flex;
      align-items: center;
      gap: 6px;

      .sender-name {
        font-size: 12px;
        color: var(--dt-text-secondary);
        font-weight: 500;
      }

      .message-time {
        font-size: 11px;
        color: var(--dt-text-tertiary);
        margin-left: auto;
      }
    }

    .item-body {
      .content-preview {
        font-size: 13px;
        color: var(--dt-text-primary);
        display: flex;
        align-items: center;
        gap: 4px;
        line-height: 1.5;

        &.text-preview {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        &:not(.text-preview) {
          color: var(--dt-text-secondary);
        }

        .material-icons-outlined {
          font-size: 16px;
          opacity: 0.7;
        }
      }
    }
  }

  .item-actions {
    display: flex;
    align-items: center;
    opacity: 0;
    transition: opacity var(--dt-transition-fast);
  }
}
</style>
