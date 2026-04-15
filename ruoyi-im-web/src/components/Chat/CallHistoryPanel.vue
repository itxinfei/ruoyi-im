<template>
  <div class="call-history-panel">
    <!-- 头部 -->
    <div class="panel-header">
      <h2>通话记录</h2>
      <div class="header-actions">
        <el-button size="small" @click="loadHistory">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 通话记录列表 -->
    <div v-loading="loading" class="history-list scrollbar-thin">
      <el-empty v-if="!loading && history.length === 0" description="暂无通话记录" />

      <div
        v-for="record in history"
        :key="record.id"
        class="call-record"
        @click="handleCallClick(record)"
      >
        <div class="call-avatar">
          <el-icon :class="record.callType === 'VIDEO' ? 'video-icon' : 'voice-icon'">
            <VideoCamera v-if="record.callType === 'VIDEO'" />
            <Phone v-else />
          </el-icon>
        </div>

        <div class="call-info">
          <div class="call-name">{{ record.targetName || '未知' }}</div>
          <div class="call-meta">
            <span class="call-type">
              <el-icon v-if="record.callType === 'VIDEO'" class="meta-icon"><VideoCamera /></el-icon>
              <el-icon v-else class="meta-icon"><Phone /></el-icon>
              {{ record.callType === 'VIDEO' ? '视频' : '语音' }}
            </span>
            <span class="call-duration">{{ formatDuration(record.duration) }}</span>
          </div>
        </div>

        <div class="call-status">
          <el-tag :type="getStatusType(record.status)" size="small">
            {{ getStatusText(record.status) }}
          </el-tag>
          <span class="call-time">{{ formatTime(record.startTime) }}</span>
        </div>

        <div class="call-actions">
          <el-tooltip content="重新呼叫">
            <el-button text @click.stop="handleRecall(record)">
              <el-icon><Phone /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Phone, VideoCamera } from '@element-plus/icons-vue'
import { getCallHistory } from '@/api/im/videoCall'

const emit = defineEmits(['call'])

const loading = ref(false)
const history = ref([])

// 加载通话历史
const loadHistory = async () => {
  loading.value = true
  try {
    const res = await getCallHistory(50)
    if (res.code === 200) {
      history.value = res.data || []
    }
  } catch (e) {
    console.error('加载通话记录失败', e)
    ElMessage.error('加载通话记录失败')
  } finally {
    loading.value = false
  }
}

// 格式化通话时长
const formatDuration = (seconds) => {
  if (!seconds) return '0秒'
  if (seconds < 60) return `${seconds}秒`
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (mins < 60) return `${mins}分${secs}秒`
  const hours = Math.floor(mins / 60)
  const remainMins = mins % 60
  return `${hours}小时${remainMins}分`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString()
}

// 获取状态标签类型
const getStatusType = (status) => {
  const types = {
    'COMPLETED': 'success',
    'MISSED': 'danger',
    'REJECTED': 'warning',
    'CANCELLED': 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'COMPLETED': '已接通',
    'MISSED': '未接',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return texts[status] || status
}

// 点击通话记录
const handleCallClick = (record) => {
  // 可以打开详情或直接呼叫
}

// 重新呼叫
const handleRecall = (record) => {
  emit('call', {
    targetId: record.targetId,
    targetName: record.targetName,
    callType: record.callType
  })
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped lang="scss">
.call-history-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-body);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--dt-spacing-lg);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
    color: var(--dt-text-primary);
  }

  .header-actions {
    display: flex;
    gap: var(--dt-spacing-sm);
  }
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-spacing-sm);
}

.call-record {
  display: flex;
  align-items: center;
  padding: var(--dt-spacing-md);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  margin-bottom: var(--dt-spacing-sm);
  cursor: pointer;
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);

    .call-actions {
      opacity: 1;
    }
  }
}

.call-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--dt-spacing-md);
  flex-shrink: 0;

  .el-icon {
    font-size: 20px;
  }

  .video-icon {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  .voice-icon {
    background: var(--dt-success-bg);
    color: var(--dt-success-color);
  }
}

.call-info {
  flex: 1;
  min-width: 0;

  .call-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin-bottom: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .call-meta {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-md);
    font-size: 12px;
    color: var(--dt-text-tertiary);

    .call-type {
      display: flex;
      align-items: center;
      gap: 4px;

      .meta-icon {
        font-size: 12px;
      }
    }

    .call-duration {
      color: var(--dt-text-secondary);
    }
  }
}

.call-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  margin-left: var(--dt-spacing-md);

  .call-time {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}

.call-actions {
  opacity: 0;
  transition: opacity var(--dt-transition-fast);
  margin-left: var(--dt-spacing-sm);

  .el-icon {
    font-size: 18px;
    color: var(--dt-text-secondary);
  }
}
</style>
