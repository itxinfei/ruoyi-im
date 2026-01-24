<template>
  <div class="new-friends-view">
    <header class="view-header">
      <button class="back-btn" @click="$emit('back')">
        <span class="material-icons-outlined">arrow_back</span>
      </button>
      <h1 class="view-title">新的朋友</h1>
    </header>

    <div class="view-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="requests.length === 0" class="empty-state">
        <span class="material-icons-outlined empty-icon">person_add</span>
        <p class="empty-text">暂无新的好友申请</p>
      </div>

      <div v-else class="requests-list">
        <div
          v-for="request in requests"
          :key="request.id"
          class="request-item"
        >
          <div class="request-avatar">
            <img v-if="request.avatar" :src="addTokenToUrl(request.avatar)" :alt="`${request.nickname || request.username} 的头像`" />
            <span v-else>{{ (request.nickname || request.username || '?').charAt(0).toUpperCase() }}</span>
          </div>
          <div class="request-info">
            <div class="request-name">{{ request.nickname || request.username }}</div>
            <div class="request-message">{{ request.message || '请求添加你为好友' }}</div>
          </div>
          <div class="request-actions">
            <template v-if="request.status === 'PENDING'">
              <el-button size="small" @click="handleReject(request)">拒绝</el-button>
              <el-button type="primary" size="small" @click="handleAccept(request)">同意</el-button>
            </template>
            <span v-else class="status-text">
              {{ request.status === 'ACCEPTED' ? '已添加' : '已拒绝' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getFriendRequests, handleFriendRequest } from '@/api/im/contact'
import { ElMessage } from 'element-plus'
import { addTokenToUrl } from '@/utils/file'

const emit = defineEmits(['back'])

const loading = ref(false)
const requests = ref([])

const loadRequests = async () => {
  loading.value = true
  try {
    const res = await getFriendRequests()
    if (res.code === 200) {
      requests.value = (res.data || []).filter(r => r.direction === 'RECEIVED')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAccept = async (request) => {
  try {
    const res = await handleFriendRequest(request.id, true)
    if (res.code === 200) {
      ElMessage.success('已同意好友请求')
      request.status = 'ACCEPTED'
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (request) => {
  try {
    const res = await handleFriendRequest(request.id, false)
    if (res.code === 200) {
      ElMessage.info('已拒绝好友请求')
      request.status = 'REJECTED'
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadRequests()
})
</script>

<style scoped>
.new-friends-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f4f7f9;
}

.view-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 16px;
  flex-shrink: 0;
}

.back-btn {
  background: none;
  border: none;
  padding: 4px;
  margin-right: 8px;
  cursor: pointer;
  color: #595959;
  display: flex;
  align-items: center;
}

.back-btn:hover {
  color: #1677ff;
}

.view-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.view-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #8c8c8c;
  gap: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #bfbfbf;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.empty-text {
  font-size: 14px;
  margin: 0;
}

.requests-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.request-item {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.request-avatar {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  flex-shrink: 0;
  overflow: hidden;
}

.request-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.request-info {
  flex: 1;
  min-width: 0;
}

.request-name {
  font-size: 15px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.request-message {
  font-size: 13px;
  color: #8c8c8c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.request-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.status-text {
  font-size: 13px;
  color: #8c8c8c;
}

/* 暗色模式 */
:deep(.dark) .view-header,
:deep(.dark) .request-item {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .view-title,
:deep(.dark) .request-name {
  color: #f1f5f9;
}

:deep(.dark) .request-message,
:deep(.dark) .status-text {
  color: #94a3b8;
}
</style>
