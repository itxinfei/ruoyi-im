<!--
转发对话框组件
用于选择转发目标会话并执行转发操作
-->
<template>
  <el-dialog
    v-model="visible"
    title="转发消息"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="forward-dialog">
      <!-- 转发模式选择 -->
      <div
        v-if="showModeSelect"
        class="mode-select"
      >
        <div class="mode-title">
          选择转发方式
        </div>
        <div class="mode-options">
          <button
            class="mode-btn"
            :class="{ active: forwardMode === 'batch' }"
            @click="forwardMode = 'batch'"
          >
            <span class="material-icons-outlined">share</span>
            <span class="mode-label">
              <strong>逐条转发</strong>
              <small>按原消息顺序逐条发送</small>
            </span>
          </button>
          <button
            class="mode-btn"
            :class="{ active: forwardMode === 'combine' }"
            @click="forwardMode = 'combine'"
          >
            <span class="material-icons-outlined">collections</span>
            <span class="mode-label">
              <strong>合并转发</strong>
              <small>作为聊天记录一次性发送</small>
            </span>
          </button>
        </div>
      </div>

      <!-- 会话选择 -->
      <div class="session-select">
        <div class="section-title">
          <span>选择转发到</span>
          <span
            v-if="selectedSessions.length > 0"
            class="selected-count"
          >
            已选 {{ selectedSessions.length }} 个会话
          </span>
        </div>

        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索联系人或群聊"
          prefix-icon="Search"
          clearable
          class="search-input"
        />

        <!-- 会话列表 -->
        <div class="session-list">
          <div
            v-for="session in filteredSessions"
            :key="session.id"
            class="session-item"
            :class="{ selected: isSelected(session.id) }"
            @click="toggleSession(session)"
          >
            <el-checkbox
              :model-value="isSelected(session.id)"
              @change="toggleSession(session)"
            />
            <DingtalkAvatar
              :user="session.type === 'PRIVATE' ? session.target : null"
              :group="session.type === 'GROUP' ? session.target : null"
              :size="40"
            />
            <div class="session-info">
              <div class="session-name">
                {{ session.target?.name || '未知' }}
              </div>
              <div
                v-if="session.lastMessage"
                class="session-preview"
              >
                {{ getSessionPreview(session) }}
              </div>
            </div>
            <span
              v-if="session.type === 'GROUP'"
              class="session-tag"
            >群</span>
          </div>

          <div
            v-if="filteredSessions.length === 0"
            class="empty-state"
          >
            <span class="material-icons-outlined">search_off</span>
            <p>未找到相关会话</p>
          </div>
        </div>
      </div>

      <!-- 转发预览 -->
      <div
        v-if="selectedSessions.length > 0"
        class="forward-preview"
      >
        <div class="preview-title">
          <span>将转发 {{ messageCount }} 条消息到 {{ selectedSessions.length }} 个会话</span>
        </div>
        <div class="preview-sessions">
          <span
            v-for="session in selectedSessions"
            :key="session.id"
            class="preview-chip"
          >
            {{ session.target?.name }}
          </span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="forwarding"
          :disabled="selectedSessions.length === 0"
          @click="handleForward"
        >
          发送 ({{ selectedSessions.length }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { forwardMessage, batchForwardMessages } from '@/api/im/message'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  // 要转发的消息列表
  messages: {
    type: Array,
    default: () => []
  },
  // 是否显示转发模式选择
  showModeSelect: {
    type: Boolean,
    default: true
  },
  // 可转发的会话列表
  availableSessions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'fail'])

// 状态
const visible = ref(false)
const searchKeyword = ref('')
const selectedSessions = ref([])
const forwardMode = ref('batch') // batch | combine
const forwarding = ref(false)

// 计算属性
const messageCount = computed(() => props.messages?.length || 0)

const filteredSessions = computed(() => {
  const sessions = props.availableSessions || []
  if (!searchKeyword.value) {
    return sessions
  }
  const keyword = searchKeyword.value.toLowerCase()
  return sessions.filter(s =>
    s.target?.name?.toLowerCase().includes(keyword)
  )
})

// 方法
const isSelected = sessionId => {
  return selectedSessions.value.some(s => s.id === sessionId)
}

const toggleSession = session => {
  const index = selectedSessions.value.findIndex(s => s.id === session.id)
  if (index >= 0) {
    selectedSessions.value.splice(index, 1)
  } else {
    selectedSessions.value.push(session)
  }
}

const getSessionPreview = session => {
  const msg = session.lastMessage
  if (!msg) {return '暂无消息'}
  if (msg.type === 'TEXT') {return msg.content}
  if (msg.type === 'IMAGE') {return '[图片]'}
  if (msg.type === 'FILE') {return '[文件]'}
  if (msg.type === 'VOICE' || msg.type === 'AUDIO') {return '[语音]'}
  if (msg.type === 'VIDEO') {return '[视频]'}
  return '[消息]'
}

const handleClose = () => {
  visible.value = false
  searchKeyword.value = ''
  selectedSessions.value = []
}

const handleForward = async () => {
  if (selectedSessions.value.length === 0) {
    ElMessage.warning('请选择转发目标')
    return
  }

  forwarding.value = true

  try {
    const messageIds = props.messages.map(m => m.id).filter(Boolean)
    const sessionIds = selectedSessions.value.map(s => s.id)

    if (forwardMode.value === 'combine' && messageIds.length > 1) {
      // 合并转发
      await batchForwardMessages({
        messageIds,
        conversationIds: sessionIds,
        forwardType: 'combine'
      })
    } else {
      // 逐条转发
      await batchForwardMessages({
        messageIds,
        conversationIds: sessionIds,
        forwardType: 'batch'
      })
    }

    ElMessage.success(`已转发 ${messageCount.value} 条消息`)
    emit('success', {
      mode: forwardMode.value,
      sessionCount: selectedSessions.value.length,
      messageCount: messageCount.value
    })
    handleClose()
  } catch (error) {
    console.error('转发失败:', error)
    const errorMsg = error?.response?.data?.msg || error?.message || '转发失败，请稍后重试'
    ElMessage.error(errorMsg)
    emit('fail', error)
  } finally {
    forwarding.value = false
  }
}

// 监听 modelValue 变化
watch(() => props.modelValue, val => {
  visible.value = val
})

watch(visible, val => {
  emit('update:modelValue', val)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.forward-dialog {
  .mode-select {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid var(--dt-border-lighter);
  }

  .mode-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin-bottom: 12px;
  }

  .mode-options {
    display: flex;
    gap: 12px;
  }

  .mode-btn {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border: 2px solid var(--dt-border-light);
    border-radius: var(--dt-radius-md);
    background: var(--dt-bg-card);
    cursor: pointer;
    transition: all var(--dt-transition-fast);

    &:hover {
      border-color: var(--dt-brand-color);
      background: var(--dt-brand-bg-light);
    }

    &.active {
      border-color: var(--dt-brand-color);
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    .material-icons-outlined {
      font-size: 24px;
    }

    .mode-label {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      gap: 2px;

      strong {
        font-size: 14px;
      }

      small {
        font-size: 11px;
        color: var(--dt-text-secondary);
      }
    }
  }
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);

  .selected-count {
    font-size: 12px;
    color: var(--dt-brand-color);
  }
}

.search-input {
  margin-bottom: 12px;
}

.session-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.selected {
    background: var(--dt-brand-bg-light);
  }

  .session-info {
    flex: 1;
    min-width: 0;

    .session-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .session-preview {
      font-size: 12px;
      color: var(--dt-text-secondary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .session-tag {
    padding: 2px 6px;
    font-size: 11px;
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    border-radius: var(--dt-radius-sm);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--dt-text-tertiary);

  .material-icons-outlined {
    font-size: 48px;
    margin-bottom: 8px;
    opacity: 0.5;
  }

  p {
    font-size: 14px;
  }
}

.forward-preview {
  margin-top: 16px;
  padding: 12px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);

  .preview-title {
    font-size: 13px;
    color: var(--dt-text-secondary);
    margin-bottom: 8px;
  }

  .preview-sessions {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .preview-chip {
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    font-size: 12px;
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    border-radius: var(--dt-radius-sm);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
