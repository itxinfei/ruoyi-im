<template>
  <el-dialog
    :model-value="visible"
    width="420px"
    @update:model-value="$emit('update:visible', $event)"
    class="dt-add-contact-dialog"
    :show-close="false"
    :close-on-click-modal="true"
    destroy-on-close
    append-to-body
  >
    <!-- 头部 -->
    <div class="dt-dialog-header">
      <h3 class="dt-dialog-title">添加联系人</h3>
      <button class="dt-close-btn" @click="$emit('update:visible', false)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>

    <!-- 搜索区域 -->
    <div class="dt-search-section">
      <div class="dt-search-input">
        <svg class="dt-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/>
          <path d="M21 21l-4.35-4.35" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <input
          v-model="keyword"
          type="text"
          placeholder="输入用户名或昵称搜索"
          @keyup.enter="handleSearch"
        />
        <button
          v-if="keyword"
          class="dt-clear-btn"
          @click="keyword = ''"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <button class="dt-search-btn" :class="{ loading: searching }" @click="handleSearch">
        <svg v-if="!searching" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span v-else class="dt-search-spinner"></span>
        搜索
      </button>
    </div>

    <!-- 搜索结果 -->
    <div class="dt-results" v-loading="searching && !hasSearched">
      <div v-if="hasSearched && results.length === 0" class="dt-empty">
        <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="19" cy="19" r="9"/>
          <path d="M39 39l-6-6" stroke-linecap="round"/>
        </svg>
        <p>未找到相关用户</p>
      </div>
      <div
        v-for="user in results"
        :key="user.id"
        class="dt-user-item"
      >
        <DingtalkAvatar
          :name="user.nickname || user.username"
          :size="40"
          :src="user.avatar"
        />
        <div class="dt-user-info">
          <div class="dt-user-name">{{ user.nickname || user.username }}</div>
          <div class="dt-user-meta">
            {{ user.department || '未知部门' }} · {{ user.position || '未知职位' }}
          </div>
        </div>
        <button
          v-if="!user.isFriend"
          class="dt-add-btn"
          @click="handleAdd(user)"
        >
          添加
        </button>
        <span v-else class="dt-added-badge">已添加</span>
      </div>
    </div>

    <!-- 发送申请弹窗 -->
    <el-dialog
      v-model="showRequestDialog"
      width="380px"
      title="发送好友申请"
      :show-close="false"
      class="dt-request-dialog"
      append-to-body
      destroy-on-close
    >
      <div class="dt-request-form">
        <div class="dt-form-group">
          <label>验证信息</label>
          <textarea
            v-model="requestMessage"
            placeholder="我是..."
            rows="3"
            maxlength="100"
          ></textarea>
          <span class="dt-char-count">{{ requestMessage.length }}/100</span>
        </div>
        <div class="dt-form-group">
          <label>分组</label>
          <select v-model="selectedGroup">
            <option value="默认分组">默认分组</option>
            <option value="同事">同事</option>
            <option value="朋友">朋友</option>
          </select>
        </div>
      </div>
      <template #footer>
        <span class="dt-dialog-footer">
          <button class="dt-btn dt-btn-secondary" @click="showRequestDialog = false">取消</button>
          <button class="dt-btn dt-btn-primary" :class="{ loading: sending }" @click="confirmAdd">
            {{ sending ? '' : '发送' }}
            <span v-if="sending" class="dt-btn-spinner"></span>
          </button>
        </span>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { searchUsers } from '@/api/im/user'
import { sendFriendRequest } from '@/api/im/contact'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['update:visible'])

const keyword = ref('')
const results = ref([])
const searching = ref(false)
const hasSearched = ref(false)

const showRequestDialog = ref(false)
const requestMessage = ref('')
const selectedGroup = ref('默认分组')
const targetUser = ref(null)
const sending = ref(false)

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  searching.value = true
  hasSearched.value = true
  try {
    const res = await searchUsers(keyword.value)
    if (res.code === 200) {
      results.value = res.data || []
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    searching.value = false
  }
}

const handleAdd = (user) => {
  targetUser.value = user
  requestMessage.value = `我是${user.nickname || ''}`
  showRequestDialog.value = true
}

const confirmAdd = async () => {
  if (!targetUser.value) return
  sending.value = true
  try {
    await sendFriendRequest({
      targetUserId: targetUser.value.id,
      message: requestMessage.value,
      groupName: selectedGroup.value
    })
    ElMessage.success('申请已发送')
    showRequestDialog.value = false
    results.value = results.value.map(u => {
      if (u.id === targetUser.value.id) {
        return { ...u, isFriend: true }
      }
      return u
    })
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    sending.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.dt-add-contact-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }
  :deep(.el-dialog__body) {
    padding: 0;
    border-radius: var(--dt-radius-xl);
    overflow: hidden;
  }
}

.dt-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-lighter);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }
}

.dt-dialog-title {
  margin: 0;
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
}

.dt-close-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--dt-text-secondary);
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: var(--dt-bg-session-hover);
  }
}

.dt-search-section {
  padding: 16px 20px;
  background: var(--dt-bg-body);

  .dark & {
    background: var(--dt-bg-body-dark);
  }
}

.dt-search-input {
  display: flex;
  align-items: center;
  background: #ffffff;
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  padding: 8px 12px;
  margin-bottom: 12px;
  transition: all var(--dt-transition-fast);

  .dark & {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);
  }

  &:focus-within {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px var(--dt-brand-lighter);
  }
}

.dt-search-icon {
  width: 16px;
  height: 16px;
  color: var(--dt-text-quaternary);
  margin-right: 8px;
  flex-shrink: 0;
}

.dt-search-input input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);

  &::placeholder {
    color: var(--dt-text-quaternary);
  }
}

.dt-clear-btn {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--dt-text-quaternary);
  cursor: pointer;

  svg {
    width: 12px;
    height: 12px;
  }

  &:hover {
    color: var(--dt-text-secondary);
  }
}

.dt-search-btn {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-md);
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-brand-hover);
  }

  svg {
    width: 16px;
    height: 16px;
  }

  &.loading {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.dt-search-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.dt-results {
  max-height: 320px;
  overflow-y: auto;

  @extend .scrollbar-sm;
}

.dt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
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

.dt-user-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);

    .dark & {
      background: var(--dt-bg-hover-dark);
    }
  }
}

.dt-user-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.dt-user-name {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.dt-user-meta {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.dt-add-btn {
  padding: 6px 16px;
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-md);
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-medium);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-brand-hover);
  }
}

.dt-added-badge {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-quaternary);
  padding: 6px 12px;
  background: var(--dt-bg-card-hover);
  border-radius: var(--dt-radius-md);

  .dark & {
    background: var(--dt-bg-hover-dark);
  }
}

.dt-request-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.dt-request-form {
  padding: 20px;
}

.dt-form-group {
  margin-bottom: 16px;
  position: relative;

  label {
    display: block;
    font-size: var(--dt-font-size-sm);
    font-weight: var(--dt-font-weight-medium);
    color: var(--dt-text-secondary);
    margin-bottom: 8px;
  }

  textarea,
  select {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid var(--dt-border-color);
    border-radius: var(--dt-radius-md);
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
    background: #ffffff;
    outline: none;
    resize: none;
    transition: all var(--dt-transition-fast);

    .dark & {
      background: var(--dt-bg-input-dark);
      border-color: var(--dt-border-dark);
      color: var(--dt-text-primary-dark);
    }

    &:focus {
      border-color: var(--dt-brand-color);
      box-shadow: 0 0 0 2px var(--dt-brand-lighter);
    }
  }
}

.dt-char-count {
  position: absolute;
  bottom: -20px;
  right: 0;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-quaternary);
}

.dt-dialog-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--dt-border-lighter);

  .dark & {
    border-top-color: var(--dt-border-dark);
  }
}

.dt-btn {
  padding: 8px 20px;
  border-radius: var(--dt-radius-md);
  font-size: var(--dt-font-size-base);
  cursor: pointer;
  border: none;
  transition: all var(--dt-transition-fast);

  &.dt-btn-secondary {
    background: var(--dt-bg-card-hover);
    color: var(--dt-text-secondary);

    &:hover {
      background: var(--dt-border-color);
    }
  }

  &.dt-btn-primary {
    background: var(--dt-brand-color);
    color: #fff;

    &:hover {
      background: var(--dt-brand-hover);
    }

    &.loading {
      opacity: 0.7;
      cursor: not-allowed;
      padding-right: 36px;
    }
  }
}

.dt-btn-spinner {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
