<template>
  <div class="new-friends-view">
    <header class="view-header">
      <button class="back-btn" @click="$emit('back')">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="view-title">
        新的朋友
      </h1>
    </header>

    <div class="view-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="requests.length === 0" class="empty-state">
        <el-icon class="empty-icon"><Plus /></el-icon>
        <p class="empty-text">
          暂无新的好友申请
        </p>
      </div>

      <div v-else class="requests-list">
        <div
          v-for="request in requests"
          :key="request.id"
          class="request-item"
        >
          <div class="request-avatar">
            <img v-if="request.avatar" :src="addTokenToUrl(request.avatar)" :alt="`${request.nickname || request.username} 的头像`">
            <span v-else>{{ (request.nickname || request.username || '?').charAt(0).toUpperCase() }}</span>
          </div>
          <div class="request-info">
            <div class="request-name">
              {{ request.nickname || request.username }}
            </div>
            <div class="request-message">
              {{ request.message || '请求添加你为好友' }}
            </div>
          </div>
          <div class="request-actions">
            <template v-if="request.status === 'PENDING'">
              <el-button size="small" @click="handleReject(request)">
                拒绝
              </el-button>
              <el-button type="primary" size="small" @click="handleAccept(request)">
                同意
              </el-button>
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
import { Loading, ArrowLeft, Plus } from '@element-plus/icons-vue'
import { getFriendRequests, handleFriendRequest } from '@/api/im/contact'
import { ElMessage } from 'element-plus'
import { addTokenToUrl } from '@/utils/file'

defineEmits(['back'])

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
    ElMessage.error('加载好友请求失败')
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
  background: var(--dt-bg-body);
}

.view-header {
  height: 56px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
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
  color: var(--dt-text-secondary);
  display: flex;
  align-items: center;
}

.back-btn:hover {
  color: var(--dt-brand-color);
}

.view-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-main);
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
  color: var(--dt-text-desc);
  gap: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-secondary);
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
  background: var(--dt-bg-card);
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: var(--dt-shadow-1);
}

.request-avatar {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--dt-brand-color), var(--dt-brand-active));
  color: var(--dt-text-white);
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
  color: var(--dt-text-main);
  margin-bottom: 4px;
}

.request-message {
  font-size: 13px;
  color: var(--dt-text-desc);
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
  color: var(--dt-text-desc);
}

/* 暗色模式 */
:deep(.dark) .view-header,
:deep(.dark) .request-item {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-bg-hover-dark);
}

:deep(.dark) .view-title,
:deep(.dark) .request-name {
  color: var(--dt-text-main-dark);
}

:deep(.dark) .request-message,
:deep(.dark) .status-text {
  color: var(--dt-text-desc-dark);
}
</style>
