<template>
  <el-dialog
    v-model="visible"
    :width="dialogWidth"
    class="user-profile-dialog"
    :show-close="false"
    :close-on-click-modal="true"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" v-loading="loading" class="loading-state"></div>
    <div v-else-if="userDetail" class="profile-container">
      <!-- 关闭按钮 -->
      <button class="close-btn" @click="handleClose">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>

      <!-- 用户信息 -->
      <div class="profile-header">
        <div class="avatar-section">
          <DingtalkAvatar
            :name="userDetail.nickname || userDetail.username"
            :user-id="userDetail.id"
            :size="80"
            :src="userDetail.avatar"
          />
          <div v-if="userDetail.online" class="online-badge" title="在线"></div>
        </div>
        <h2 class="user-name">{{ userDetail.nickname || userDetail.username }}</h2>
        <p class="user-account">{{ userDetail.username }}</p>
      </div>

      <!-- 详细信息 -->
      <div class="profile-details">
        <div class="detail-section">
          <h4 class="section-title">基本信息</h4>
          <div class="detail-list">
            <div class="detail-row">
              <span class="detail-label">职位</span>
              <span class="detail-value">{{ userDetail.position || '成员' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">部门</span>
              <span class="detail-value">{{ userDetail.departmentName || userDetail.department || '未分配' }}</span>
            </div>
          </div>
        </div>

        <div v-if="userDetail.mobile || userDetail.email" class="detail-section">
          <h4 class="section-title">联系方式</h4>
          <div class="detail-list">
            <div v-if="userDetail.mobile" class="detail-row">
              <span class="detail-label">手机</span>
              <span class="detail-value">{{ userDetail.mobile }}</span>
            </div>
            <div v-if="userDetail.email" class="detail-row">
              <span class="detail-label">邮箱</span>
              <span class="detail-value">{{ userDetail.email }}</span>
            </div>
          </div>
        </div>

        <div v-if="userDetail.signature" class="detail-section">
          <h4 class="section-title">个人签名</h4>
          <div class="signature-text">{{ userDetail.signature }}</div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="profile-footer">
        <el-button type="primary" class="chat-btn" @click="handleStartChat">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          发消息
        </el-button>
        <el-button class="call-btn" @click="handleStartCall">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </el-button>
        <el-dropdown trigger="click" @command="handleMoreAction">
          <el-button class="more-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="1"/>
              <circle cx="12" cy="5" r="1"/>
              <circle cx="12" cy="19" r="1"/>
            </svg>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="add-friend">添加为好友</el-dropdown-item>
              <el-dropdown-item command="copy-userid">复制用户ID</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/api/im/user'
import { createConversation } from '@/api/im/conversation'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  modelValue: Boolean,
  userId: [String, Number]
})

const emit = defineEmits(['update:modelValue', 'chat'])

const store = useStore()
const visible = ref(false)
const loading = ref(false)
const userDetail = ref(null)

const dialogWidth = computed(() => '400px')

const handleClose = () => emit('update:modelValue', false)

const loadUserDetail = async () => {
  if (!props.userId) return
  loading.value = true
  try {
    const res = await getUserInfo(props.userId)
    if (res.code === 200) {
      userDetail.value = res.data
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleStartChat = async () => {
  try {
    const res = await createConversation({
      type: 'PRIVATE',
      targetId: props.userId
    })
    if (res.code === 200) {
      store.commit('im/session/SET_CURRENT_SESSION', res.data)
      window.dispatchEvent(new CustomEvent('switch-tab', { detail: 'chat' }))
      handleClose()
    }
  } catch (error) {
    ElMessage.error('无法发起聊天')
  }
}

const handleStartCall = () => {
  ElMessage.info('语音通话功能开发中...')
}

const handleMoreAction = (command) => {
  switch (command) {
    case 'add-friend':
      ElMessage.info('添加好友功能开发中...')
      break
    case 'copy-userid':
      navigator.clipboard.writeText(String(props.userId))
      ElMessage.success('用户ID已复制')
      break
  }
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.userId) loadUserDetail()
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.user-profile-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0, 0, 0, 0.05);
    animation: dialogFadeIn 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
    
    .el-dialog__body {
      padding: 0;
    }
  }
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
  }
  :deep(.el-dialog__header) {
    display: none;
  }
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.loading-state {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-container {
  position: relative;
  background: var(--dt-bg-card);
  max-height: 80vh;
  overflow-y: auto;

  .dark & {
    background: var(--dt-bg-card-dark);
  }

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;
  }
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.03);
  border: none;
  color: var(--dt-text-secondary);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;

  svg {
    width: 18px;
    height: 18px;
    transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    background: rgba(0, 0, 0, 0.08);
    color: var(--dt-text-primary);
    transform: scale(1.05);
    
    svg {
      transform: rotate(90deg);
    }
  }

  &:active {
    transform: scale(0.95);
  }

  .dark & {
    background: rgba(255, 255, 255, 0.05);
    
    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }
  }
}

// ============================================================================
// 头部区域
// ============================================================================
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 24px 20px;
  text-align: center;
}

.avatar-section {
  position: relative;
  margin-bottom: 16px;

  :deep(.dingtalk-avatar) {
    border: 3px solid #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .dark & :deep(.dingtalk-avatar) {
    border-color: var(--dt-bg-card-dark);
  }
}

.online-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #52c41a;
  border: 2px solid #fff;
  border-radius: 50%;

  .dark & {
    border-color: var(--dt-bg-card-dark);
  }
}

.user-name {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.user-account {
  margin: 0;
  font-size: 13px;
  color: var(--dt-text-tertiary);
}

// ============================================================================
// 详细信息
// ============================================================================
.profile-details {
  padding: 0 24px 24px;
}

.detail-section {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-text-quaternary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 8px 0;
}

.detail-list {
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.detail-row {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }
}

.detail-label {
  font-size: 13px;
  color: var(--dt-text-secondary);
  min-width: 60px;
}

.detail-value {
  flex: 1;
  font-size: 14px;
  color: var(--dt-text-primary);
  text-align: right;
}

.signature-text {
  padding: 12px 16px;
  font-size: 14px;
  color: var(--dt-text-secondary);
  line-height: 1.6;
  font-style: italic;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

// ============================================================================
// 底部操作
// ============================================================================
.profile-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  border-top: 1px solid var(--dt-border-light);

  .dark & {
    border-top-color: var(--dt-border-dark);
  }

  .chat-btn {
    flex: 1;
    height: 40px;
    font-size: 14px;
  }

  .call-btn,
  .more-btn {
    width: 40px;
    height: 40px;
    padding: 0;

    svg {
      width: 18px;
      height: 18px;
    }
  }

  .more-btn {
    color: var(--dt-text-secondary);

    &:hover {
      color: var(--dt-text-primary);
    }
  }
}

// ============================================================================
// 移动端响应式
// ============================================================================
@media (max-width: 479px) {
  .profile-header {
    padding: 24px 20px 16px;
  }

  .profile-details {
    padding: 0 20px 20px;
  }

  .profile-footer {
    padding: 12px 20px;
  }
}
</style>
