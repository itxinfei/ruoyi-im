<template>
  <div class="dingtalk-new-friends">
    <div v-if="loading" class="dt-loading">
      <div class="dt-spinner"></div>
    </div>

    <div v-else-if="requests.length === 0" class="dt-empty">
      <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
        <circle cx="19" cy="19" r="9"/>
        <path d="M34 10l-6 6m0 0l-6-6m6 6l-6 6m6-6h.01" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <p>暂无新的好友申请</p>
    </div>

    <div v-else class="dt-request-list">
      <div
        v-for="request in requests"
        :key="request.id"
        class="dt-request-item"
      >
        <DingtalkAvatar
          :name="request.nickname || request.username"
          :size="44"
          :src="request.avatar"
        />
        <div class="dt-request-info">
          <div class="dt-request-name">{{ request.nickname || request.username }}</div>
          <div class="dt-request-msg">{{ request.message || '请求添加你为好友' }}</div>
        </div>
        <div class="dt-request-actions">
          <template v-if="request.status === 'PENDING'">
            <button class="dt-btn-deny" @click="handleReject(request)">拒绝</button>
            <button class="dt-btn-accept" @click="handleAccept(request)">同意</button>
          </template>
          <span v-else class="dt-status" :class="request.status.toLowerCase()">
            {{ request.status === 'ACCEPTED' ? '已添加' : '已拒绝' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getFriendRequests, handleFriendRequest } from '@/api/im/contact'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const emit = defineEmits(['update-count'])

const loading = ref(false)
const requests = ref([])

const loadRequests = async () => {
  loading.value = true
  try {
    const res = await getFriendRequests()
    if (res.code === 200) {
      requests.value = (res.data || [])
        .filter(r => r.direction === 'RECEIVED')
        .sort((a, b) => {
          // 待处理的排前面
          if (a.status === 'PENDING' && b.status !== 'PENDING') return -1
          if (a.status !== 'PENDING' && b.status === 'PENDING') return 1
          return 0
        })

      emit('update-count', requests.value.filter(r => r.status === 'PENDING').length)
    }
  } catch (e) {
    console.error('加载好友请求失败', e)
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
      emit('update-count', requests.value.filter(r => r.status === 'PENDING').length)
    } else {
      ElMessage.error(res.msg || '操作失败')
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
      emit('update-count', requests.value.filter(r => r.status === 'PENDING').length)
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadRequests()
})

defineExpose({ refresh: loadRequests })
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.dingtalk-new-friends {
  height: 100%;
}

.dt-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.dt-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #e5e6eb;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;

  .dark & {
    border-color: #3e3f42;
    border-top-color: #1890ff;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.dt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-quaternary);

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
    opacity: 0.3;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

.dt-request-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dt-request-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;

  .dark & {
    background: #2a2b2c;
    border-color: #3e3f42;
  }
}

.dt-request-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.dt-request-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
  @include text-ellipsis;
}

.dt-request-msg {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  @include text-ellipsis;
}

.dt-request-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.dt-btn-deny,
.dt-btn-accept {
  padding: 6px 16px;
  font-size: 13px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}

.dt-btn-deny {
  background: #fff;
  color: var(--dt-text-secondary);
  border: 1px solid #d9d9d9;

  &:hover {
    border-color: #1890ff;
    color: #1890ff;
  }

  .dark & {
    background: #3a3a3a;
    border-color: #5c5c5c;
  }
}

.dt-btn-accept {
  background: #1890ff;
  color: #fff;

  &:hover {
    background: #40a9ff;
  }

  .dark & {
    background: #1890ff;
  }
}

.dt-status {
  font-size: 12px;
  color: var(--dt-text-quaternary);

  &.accepted {
    color: var(--dt-success-color);
  }

  &.rejected {
    color: var(--dt-text-quaternary);
  }
}
</style>
