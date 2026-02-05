<template>
  <div class="dingtalk-new-friends">
    <div
      v-if="loading"
      class="dt-loading"
    >
      <div class="dt-spinner" />
    </div>

    <div
      v-else-if="requests.length === 0"
      class="dt-empty"
    >
      <svg
        viewBox="0 0 48 48"
        fill="none"
        stroke="currentColor"
        stroke-width="1.5"
      >
        <circle
          cx="19"
          cy="19"
          r="9"
        />
        <path
          d="M34 10l-6 6m0 0l-6-6m6 6l-6 6m6-6h.01"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
      <p>暂无新的好友申请</p>
    </div>

    <div
      v-else
      class="dt-request-list"
    >
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
          <div class="dt-request-name">
            {{ request.nickname || request.username }}
          </div>
          <div class="dt-request-msg">
            {{ request.message || '请求添加你为好友' }}
          </div>
        </div>
        <div class="dt-request-actions">
          <template v-if="request.status === 'PENDING'">
            <button
              class="dt-btn-deny"
              @click="handleReject(request)"
            >
              拒绝
            </button>
            <button
              class="dt-btn-accept"
              @click="handleAccept(request)"
            >
              同意
            </button>
          </template>
          <span
            v-else
            class="dt-status"
            :class="request.status.toLowerCase()"
          >
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
          if (a.status === 'PENDING' && b.status !== 'PENDING') {return -1}
          if (a.status !== 'PENDING' && b.status === 'PENDING') {return 1}
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

const handleAccept = async request => {
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

const handleReject = async request => {
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
  width: 28px;
  height: 28px;
  border: 3px solid var(--dt-border-light);
  border-top-color: var(--dt-brand-color);
  border-radius: var(--dt-radius-full);
  animation: spin 0.8s linear infinite;

  .dark & {
    border-color: var(--dt-border-dark);
    border-top-color: var(--dt-brand-color);
  }
}

.dt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-quaternary);

  svg {
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
    opacity: 0.2;
  }

  p {
    font-size: var(--dt-font-size-base);
    margin: 0;
    color: var(--dt-text-tertiary);
  }
}

.dt-request-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px;
}

.dt-request-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #ffffff;
  border-radius: var(--dt-radius-md);
  border: 1px solid var(--dt-border-light);
  transition: all var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-border-color);
    box-shadow: var(--dt-shadow-1);
  }

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      border-color: var(--dt-border-color);
    }
  }
}

.dt-request-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.dt-request-name {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin-bottom: 4px;

  @include text-ellipsis;
}

.dt-request-msg {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);

  @include text-ellipsis;
}

.dt-request-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.dt-btn-deny,
.dt-btn-accept {
  padding: 8px 16px;
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-medium);
  border-radius: var(--dt-radius-md);
  border: none;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:active {
    transform: scale(0.95);
  }
}

.dt-btn-deny {
  background: #ffffff;
  color: var(--dt-text-secondary);
  border: 1px solid var(--dt-border-color);

  &:hover {
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);
  }

  .dark & {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);
    color: var(--dt-text-secondary-dark);

    &:hover {
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }
  }
}

.dt-btn-accept {
  background: var(--dt-brand-color);
  color: #ffffff;

  &:hover {
    background: var(--dt-brand-hover);
  }
}

.dt-status {
  font-size: var(--dt-font-size-sm);
  padding: 4px 12px;
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-quaternary);
  background: var(--dt-bg-card-hover);

  &.accepted {
    color: var(--dt-success-color);
    background: var(--dt-success-bg);
  }

  &.rejected {
    color: var(--dt-text-quaternary);
    background: var(--dt-bg-card-hover);
  }
}

// ============================================================================
// 响应式适配
// ============================================================================

@media (max-width: 768px) {
  .dt-request-list {
    padding: 4px;
    gap: 4px;
  }

  .dt-request-item {
    padding: 10px;
  }

  .dt-btn-deny,
  .dt-btn-accept {
    padding: 6px 12px;
    font-size: var(--dt-font-size-xs);
  }
}
</style>
