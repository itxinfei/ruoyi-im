<template>
  <el-dialog
    v-model="visible"
    title="转发消息"
    width="600px"
    @close="handleClose"
  >
    <div class="forward-dialog">
      <div class="forward-message">
        <div class="forward-message-label">转发消息：</div>
        <div class="forward-message-content">
          <div v-if="message.type === 'IMAGE'">
            <img :src="JSON.parse(message.content).imageUrl" class="msg-img" />
          </div>
          <div v-if="message.type === 'VIDEO'">
            <video :src="JSON.parse(message.content).videoUrl" class="msg-video" controls />
          </div>
          <div v-else-if="message.type === 'FILE'">
            <div class="file-preview">
              <el-icon><Document /></el-icon>
              <span>{{ message.fileData?.fileName || message.content }}</span>
            </div>
          </div>
          <div v-else>
            {{ message.content }}
          </div>
        </div>
      </div>

      <el-divider />

      <div class="forward-sessions">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索会话..."
            :prefix-icon="Search"
            clearable
          />
        </div>

        <div class="session-list">
          <div
            v-for="session in filteredSessions"
            :key="session.id"
            class="session-item"
            :class="{ active: selectedSessionId === session.id }"
            @click="selectSession(session)"
          >
            <el-avatar :size="40" :src="addTokenToUrl(session.avatar)">
              {{ session.name?.charAt(0) }}
            </el-avatar>
            <div class="session-info">
              <div class="session-name">{{ session.name }}</div>
              <div class="session-preview">{{ session.lastMessage || '暂无消息' }}</div>
            </div>
          </div>

          <div v-if="filteredSessions.length === 0" class="empty-sessions">
            <el-empty description="暂无匹配会话" />
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :disabled="!selectedSessionId" @click="handleForward">
        转发
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Document } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { addTokenToUrl } from '@/utils/file'

const store = useStore()

const visible = ref(false)
const message = ref(null)
const searchKeyword = ref('')
const selectedSessionId = ref(null)

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

// 打开对话框
const open = (msg) => {
  message.value = msg
  selectedSessionId.value = null
  searchKeyword.value = ''
  visible.value = true
}

// 选择会话
const selectSession = (session) => {
  selectedSessionId.value = session.id
}

// 转发消息
const handleForward = () => {
  if (!selectedSessionId.value) {
    ElMessage.warning('请选择要转发的会话')
    return
  }

  visible.value = false

  // 触发转发事件，由父组件处理实际的转发逻辑
  emit('forward', {
    message: message.value,
    targetSessionId: selectedSessionId.value
  })
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  message.value = null
  selectedSessionId.value = null
  searchKeyword.value = ''
}

const emit = defineEmits(['forward'])

defineExpose({
  open
})
</script>

<style scoped lang="scss">
.forward-dialog {
  .forward-message {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .forward-message-label {
      font-size: 14px;
      color: #595959;
      flex-shrink: 0;
    }

    .forward-message-content {
      flex: 1;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 8px;
      font-size: 14px;
      color: #262626;
      word-break: break-all;

      .file-preview {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #0089ff;

        .el-icon {
          font-size: 24px;
        }
      }
    }
  }

  .forward-sessions {
    height: 400px;
    display: flex;
    flex-direction: column;

    .search-box {
      margin-bottom: 16px;
    }

    .session-list {
      flex: 1;
      overflow-y: auto;
      border: 1px solid #f0f0f0;
      border-radius: 8px;

      .session-item {
        display: flex;
        align-items: center;
        padding: 12px;
        cursor: pointer;
        border-bottom: 1px solid #f5f5f5;
        transition: all 0.2s ease;

        &:hover {
          background: #f5f5f5;
        }

        &.active {
          background: #e6f7ff;
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
            color: #262626;
            margin-bottom: 4px;
          }

          .session-preview {
            font-size: 12px;
            color: #8c8c8c;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
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
</style>
