<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="600px"
    destroy-on-close
    @close="handleClose"
  >
    <div class="forward-dialog">
      <!-- 消息预览区 -->
      <div class="forward-message">
        <div class="forward-message-label">
          {{ isMultiple ? `转发消息（${messages.length}条）` : '转发消息：' }}
        </div>
        <div class="forward-message-content">
          <!-- 多条消息预览 -->
          <template v-if="isMultiple">
            <div class="multi-message-preview">
              <div
                v-for="(msg, index) in previewMessages"
                :key="msg.id || `preview-${index}`"
                class="preview-item"
              >
                <span class="preview-sender">{{ msg.senderName }}:</span>
                <span class="preview-content">{{ getMessagePreview(msg) }}</span>
              </div>
              <div v-if="messages.length > 5" class="preview-more">
                还有 {{ messages.length - 5 }} 条消息...
              </div>
            </div>
          </template>
          <!-- 单条消息预览 -->
          <template v-else-if="singleMessage">
            <div v-if="singleMessage.type === 'IMAGE'" class="msg-image">
              <img :src="getImageUrl(singleMessage)" class="msg-img">
            </div>
            <div v-else-if="singleMessage.type === 'VIDEO'" class="msg-video">
              <video :src="getVideoUrl(singleMessage)" class="msg-video-player" controls />
            </div>
            <div v-else-if="singleMessage.type === 'FILE'" class="file-preview">
              <el-icon><Document /></el-icon>
              <span>{{ getFileName(singleMessage) }}</span>
            </div>
            <div v-else class="msg-text">
              {{ singleMessage.content }}
            </div>
          </template>
        </div>
      </div>

      <el-divider />

      <!-- 会话选择区 -->
      <div class="forward-sessions">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索联系人或群组..."
            :prefix-icon="Search"
            clearable
          />
        </div>

        <div class="session-list">
          <div
            v-for="session in filteredSessions"
            :key="session.id"
            class="session-item"
            :class="{ active: selectedSessionIds.includes(session.id) }"
            @click="toggleSession(session)"
          >
            <DingtalkAvatar
              :src="session.avatar"
              :name="session.name"
              :user-id="session.targetId"
              :size="40"
              shape="square"
            />
            <div class="session-info">
              <div class="session-name">
                {{ session.name }}
              </div>
              <div class="session-meta">
                <span v-if="session.type === 'GROUP'" class="session-type">群聊</span>
                <span v-else class="session-type">私聊</span>
                <span class="session-preview">{{ session.lastMessage || '暂无消息' }}</span>
              </div>
            </div>
            <div v-if="selectedSessionIds.includes(session.id)" class="selected-check">
              <el-icon><Check /></el-icon>
            </div>
          </div>

          <div v-if="filteredSessions.length === 0" class="empty-sessions">
            <el-empty description="暂无匹配会话" :image-size="80" />
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <span v-if="isMultiple && messages.length > 1" class="forward-mode-hint">
          <el-radio-group v-model="forwardMode" size="small">
            <el-radio-button label="oneByOne">逐条转发</el-radio-button>
            <el-radio-button label="combine">合并转发</el-radio-button>
          </el-radio-group>
        </span>
        <div class="footer-actions">
          <el-button @click="handleClose">
            取消
          </el-button>
          <el-button
            type="primary"
            :disabled="selectedSessionIds.length === 0 || forwarding"
            :loading="forwarding"
            @click="handleForward"
          >
            {{ forwarding ? '转发中...' : `转发${selectedSessionIds.length > 1 ? `(${selectedSessionIds.length}个会话)` : ''}` }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Document, Check } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { addTokenToUrl } from '@/utils/file'

const store = useStore()

const visible = ref(false)
const messages = ref([])
const searchKeyword = ref('')
const selectedSessionIds = ref([])
const forwardMode = ref('oneByOne') // oneByOne | combine
const forwarding = ref(false)

// 计算属性
const isMultiple = computed(() => messages.value.length > 1)
const singleMessage = computed(() => messages.value.length === 1 ? messages.value[0] : null)

const dialogTitle = computed(() => {
  if (isMultiple.value) {
    return `转发 ${messages.value.length} 条消息`
  }
  return '转发消息'
})

const previewMessages = computed(() => {
  return messages.value.slice(0, 5)
})

// 获取会话列表
const sessions = computed(() => store.state.im.session?.sessions || [])

// 过滤会话列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value) {
    return sessions.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return sessions.value.filter(session =>
    session.name?.toLowerCase().includes(keyword)
  )
})

// 辅助方法
const getMessagePreview = (msg) => {
  if (msg.type === 'IMAGE') return '[图片]'
  if (msg.type === 'VIDEO') return '[视频]'
  if (msg.type === 'FILE') return '[文件]'
  if (msg.type === 'VOICE') return '[语音]'
  if (msg.type === 'RECALLED') return '[已撤回]'
  return msg.content?.substring(0, 30) || ''
}

const getImageUrl = (msg) => {
  try {
    const content = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content
    return addTokenToUrl(content.imageUrl || content)
  } catch {
    return msg.content
  }
}

const getVideoUrl = (msg) => {
  try {
    const content = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content
    return content.videoUrl || content
  } catch {
    return msg.content
  }
}

const getFileName = (msg) => {
  try {
    const content = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content
    return content.fileName || '文件'
  } catch {
    return '文件'
  }
}

// 打开对话框 - 单条消息
const open = (msg) => {
  messages.value = [msg]
  selectedSessionIds.value = []
  searchKeyword.value = ''
  forwardMode.value = 'oneByOne'
  visible.value = true
}

// 打开对话框 - 多条消息（逐条转发）
const openMultiple = (msgs) => {
  messages.value = msgs
  selectedSessionIds.value = []
  searchKeyword.value = ''
  forwardMode.value = 'oneByOne'
  visible.value = true
}

// 打开对话框 - 合并转发
const openCombine = (msgs) => {
  messages.value = msgs
  selectedSessionIds.value = []
  searchKeyword.value = ''
  forwardMode.value = 'combine'
  visible.value = true
}

// 切换会话选择
const toggleSession = (session) => {
  const index = selectedSessionIds.value.indexOf(session.id)
  if (index === -1) {
    selectedSessionIds.value.push(session.id)
  } else {
    selectedSessionIds.value.splice(index, 1)
  }
}

// 执行转发
const handleForward = async () => {
  if (selectedSessionIds.value.length === 0) {
    ElMessage.warning('请选择要转发的会话')
    return
  }

  forwarding.value = true

  try {
    let successCount = 0

    // 逐条转发模式：每条消息单独转发到每个会话
    if (forwardMode.value === 'oneByOne') {
      for (const sessionId of selectedSessionIds.value) {
        for (const msg of messages.value) {
          try {
            await store.dispatch('im/message/forwardMessage', {
              messageId: msg.id,
              targetConversationId: sessionId
            })
            successCount++
          } catch (e) {
            console.error('转发失败', e)
          }
        }
      }
    }
    // 合并转发模式：所有消息合并成一条转发
    else if (forwardMode.value === 'combine') {
      // 构建合并转发内容
      const combineContent = messages.value.map(msg => ({
        senderId: msg.senderId,
        senderName: msg.senderName,
        type: msg.type,
        content: msg.content,
        timestamp: msg.timestamp
      }))

      for (const sessionId of selectedSessionIds.value) {
        try {
          await store.dispatch('im/message/sendMessage', {
            sessionId,
            type: 'COMBINE',
            content: JSON.stringify({
              title: `聊天记录 (${messages.value.length}条)`,
              messages: combineContent
            })
          })
          successCount++
        } catch (e) {
          console.error('合并转发失败', e)
        }
      }
    }

    const action = forwardMode.value === 'combine' ? '合并转发' : '转发'
    ElMessage.success(`${action}成功！共 ${successCount} 条消息`)
    handleClose()

    emit('forwarded', { count: successCount })
  } catch (e) {
    console.error('转发失败', e)
    ElMessage.error('转发失败，请重试')
  } finally {
    forwarding.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  messages.value = []
  selectedSessionIds.value = []
  searchKeyword.value = ''
}

const emit = defineEmits(['forward', 'forwarded'])

defineExpose({
  open,
  openMultiple,
  openCombine
})
</script>

<style scoped lang="scss">
.forward-dialog {
  .forward-message {
    margin-bottom: 16px;

    .forward-message-label {
      font-size: 13px;
      color: var(--dt-text-secondary);
      margin-bottom: 8px;
    }

    .forward-message-content {
      padding: 12px;
      background: var(--dt-bg-body);
      border-radius: var(--dt-radius-md);
      font-size: 14px;
      color: var(--dt-text-primary);
      word-break: break-all;
      max-height: 200px;
      overflow-y: auto;

      .msg-img {
        max-width: 150px;
        max-height: 150px;
        border-radius: var(--dt-radius-sm);
      }

      .msg-video-player {
        max-width: 200px;
        border-radius: var(--dt-radius-sm);
      }

      .file-preview {
        display: flex;
        align-items: center;
        gap: 8px;
        color: var(--dt-brand-color);

        .el-icon {
          font-size: 24px;
        }
      }

      .msg-text {
        line-height: 1.5;
      }

      .multi-message-preview {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .preview-item {
          display: flex;
          gap: 6px;
          font-size: 13px;

          .preview-sender {
            color: var(--dt-brand-color);
            font-weight: 500;
            flex-shrink: 0;
          }

          .preview-content {
            color: var(--dt-text-secondary);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .preview-more {
          color: var(--dt-text-tertiary);
          font-size: 12px;
          text-align: center;
          padding-top: 4px;
        }
      }
    }
  }

  .forward-sessions {
    height: 350px;
    display: flex;
    flex-direction: column;

    .search-box {
      margin-bottom: 12px;
    }

    .session-list {
      flex: 1;
      overflow-y: auto;
      border: 1px solid var(--dt-border-light);
      border-radius: var(--dt-radius-md);

      .session-item {
        display: flex;
        align-items: center;
        padding: 12px;
        cursor: pointer;
        border-bottom: 1px solid var(--dt-border-lighter);
        transition: background-color 0.15s;

        &:hover {
          background: var(--dt-bg-hover);
        }

        &.active {
          background: var(--dt-brand-bg);
        }

        &:last-child {
          border-bottom: none;
        }

        .session-info {
          flex: 1;
          margin-left: 12px;
          overflow: hidden;

          .session-name {
            font-size: 14px;
            font-weight: 500;
            color: var(--dt-text-primary);
            margin-bottom: 4px;
          }

          .session-meta {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 12px;

            .session-type {
              color: var(--dt-text-tertiary);
              padding: 1px 6px;
              background: var(--dt-bg-body);
              border-radius: var(--dt-radius-sm);
            }

            .session-preview {
              color: var(--dt-text-tertiary);
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              flex: 1;
            }
          }
        }

        .selected-check {
          width: 20px;
          height: 20px;
          border-radius: 50%;
          background: var(--dt-brand-color);
          color: var(--dt-text-white);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
        }
      }

      .empty-sessions {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .forward-mode-hint {
    .el-radio-group {
      :deep(.el-radio-button__inner) {
        padding: 6px 12px;
      }
    }
  }

  .footer-actions {
    display: flex;
    gap: 8px;
  }
}

// 暗色模式
.dark {
  .forward-dialog {
    .forward-message-content {
      background: var(--dt-bg-body-dark);
    }

    .session-item {
      &:hover {
        background: var(--dt-bg-hover-dark);
      }

      &.active {
        background: var(--dt-brand-bg-dark);
      }
    }
  }
}
</style>
