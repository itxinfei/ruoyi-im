<template>
  <el-dialog
    v-model="visible"
    width="580px"
    :show-close="false"
    class="dingtalk-profile-dialog"
    append-to-body
    destroy-on-close
  >
    <div v-if="loading" v-loading="loading" class="loading-state" />
    <div v-else-if="userDetail" class="profile-container">
      <header class="profile-header">
        <div class="profile-main">
          <div class="avatar-wrapper">
            <img
              v-if="userDetail.avatar"
              :src="userDetail.avatar"
              alt="avatar"
              class="avatar-img"
            >
            <div v-else class="avatar-placeholder">
              {{ (userDetail.nickname || userDetail.username || '?').charAt(0) }}
            </div>
          </div>
          <div class="base-info">
            <div class="name-row">
              <span class="user-name">{{ userDetail.nickname || userDetail.username }}</span>
              <span class="status-dot online" />
              <span class="status-text">在线</span>
            </div>
            <div class="sub-row">
              <span>{{ userDetail.departmentName || userDetail.department || '公司组织' }}</span>
              <span class="divider">·</span>
              <span>{{ userDetail.position || '成员' }}</span>
            </div>
          </div>
        </div>
        <div class="profile-actions">
          <el-button class="call-btn" @click="handleStartCall('voice')">
            <el-icon><Phone /></el-icon>
            语音
          </el-button>
          <el-button class="call-btn" @click="handleStartCall('video')">
            <el-icon><VideoCamera /></el-icon>
            视频
          </el-button>
          <el-button type="primary" class="chat-btn" @click="handleStartChat">
            <el-icon><ChatDotRound /></el-icon>
            发消息
          </el-button>
        </div>
      </header>

      <div class="profile-body">
        <div class="detail-list">
          <div class="detail-row">
            <span class="label">工号</span>
            <span class="value">{{ userDetail.username }}</span>
          </div>
          <div class="detail-row">
            <span class="label">手机</span>
            <span class="value">{{ userDetail.mobile || '未公开' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">邮箱</span>
            <span class="value">{{ userDetail.email || '未设置' }}</span>
          </div>
          <div class="detail-row clickable" @click="handleEditRemark">
            <span class="label">备注</span>
            <span class="value">{{ userDetail.remarkName || '未设置' }}</span>
          </div>
          <div v-if="userDetail.signature" class="detail-row">
            <span class="label">签名</span>
            <span class="value signature">{{ userDetail.signature }}</span>
          </div>
        </div>
      </div>

      <footer class="profile-footer">
        <el-dropdown trigger="click" @command="handleMoreCommand">
          <el-button class="more-btn">
            <el-icon><MoreFilled /></el-icon>
            更多
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="star">设为星标</el-dropdown-item>
              <el-dropdown-item command="block">加入黑名单</el-dropdown-item>
              <el-dropdown-item command="remark">设置备注</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </footer>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { MoreFilled, Phone, VideoCamera, ChatDotRound } from '@element-plus/icons-vue'
import { getUserInfo, updateUser } from '@/api/im/user'
import { createConversation } from '@/api/im/conversation'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({ modelValue: Boolean, userId: [String, Number] })
const emit = defineEmits(['update:modelValue', 'chat', 'start-call'])

const store = useStore()
const visible = ref(false)
const loading = ref(false)
const userDetail = ref(null)

const handleClose = () => emit('update:modelValue', false)

const loadUserDetail = async () => {
  if (!props.userId) return
  loading.value = true
  try {
    const res = await getUserInfo(props.userId)
    if (res.code === 200) userDetail.value = res.data
  } finally { loading.value = false }
}

const handleStartChat = async () => {
  try {
    const res = await createConversation({ type: 'PRIVATE', targetId: props.userId })
    if (res.code === 200) {
      store.commit('im/session/SET_CURRENT_SESSION', res.data)
      handleClose()
    }
  } catch { ElMessage.error('无法发起对话') }
}

const handleStartCall = (type) => { emit('start-call', { type, user: userDetail.value }); handleClose() }
const handleMoreCommand = (cmd) => {
  if (cmd === 'star') ElMessage.info('已设为星标（待接入）')
  else if (cmd === 'block') ElMessage.info('已加入黑名单（待接入）')
  else if (cmd === 'remark') ElMessage.info('设置备注（待接入）')
}

const handleEditRemark = () => {
  ElMessageBox.prompt('设置备注', '备注', {
    inputValue: userDetail.value?.remarkName || '',
    confirmButtonText: '保存',
    cancelButtonText: '取消'
  }).then(async ({ value }) => {
    await updateUser(props.userId, { remarkName: value })
    userDetail.value.remarkName = value
    ElMessage.success('备注已更新')
  }).catch(() => {})
}

watch(() => props.modelValue, (val) => { visible.value = val; if (val && props.userId) loadUserDetail() })
watch(visible, (val) => { if (!val) emit('update:modelValue', false) })
</script>

<style scoped lang="scss">
.dingtalk-profile-dialog {
  :deep(.el-dialog) {
    border-radius: 8px;
    overflow: hidden;
    padding: 0;
    box-shadow: var(--dt-shadow-3);
  }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 0; }
}

.profile-container {
  background: var(--dt-bg-card);
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--dt-spacing-lg);
  border-bottom: 1px solid var(--dt-border-light);
}

.profile-main {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  min-width: 0;
}

.avatar-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--dt-bg-body);
  display: flex;
  align-items: center;
  justify-content: center;

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-placeholder {
    font-size: 28px;
    font-weight: 600;
    color: var(--dt-brand-color);
  }
}

.base-info {
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--dt-success-color);
}

.status-text {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.sub-row {
  margin-top: 4px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.profile-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-actions .el-button {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 4px;
}

.profile-actions .call-btn {
  color: var(--dt-brand-color);
  background: var(--dt-brand-bg);
  border-color: transparent;
}

.profile-actions .chat-btn {
  background: var(--dt-brand-color);
  border-color: var(--dt-brand-color);
}

.profile-body {
  padding: var(--dt-spacing-lg);
}

.detail-list {
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  padding: 12px 16px;
  background: var(--dt-bg-body);
}

.detail-row {
  display: flex;
  padding: 8px 0;
  font-size: 13px;

  .label {
    width: 48px;
    color: var(--dt-text-tertiary);
    flex-shrink: 0;
  }

  .value {
    color: var(--dt-text-primary);
    flex: 1;
    word-break: break-all;

    &.signature {
      color: var(--dt-text-secondary);
    }
  }
}

.detail-row.clickable {
  cursor: pointer;
}

.detail-row.clickable:hover {
  color: var(--dt-brand-color);
}

.profile-footer {
  padding: 12px var(--dt-spacing-lg);
  border-top: 1px solid var(--dt-border-light);
  display: flex;
  justify-content: flex-end;
}

.more-btn {
  color: var(--dt-text-secondary);
  font-size: 12px;
}
</style>
