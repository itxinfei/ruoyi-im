<template>
  <el-dialog
    v-model="visible"
    width="400px"
    class="personal-profile-dialog"
    destroy-on-close
    append-to-body
    :show-close="false"
    :close-on-click-modal="true"
  >
    <div class="user-profile-container">
      <!-- 头部：头像 + 基本信息 -->
      <div class="header">
        <div class="desc">
          <h2 class="user-name">{{ currentUser.nickname || currentUser.username }}</h2>
          <label class="user-id">{{ currentUser.id || '' }}</label>
        </div>
        <div class="avatar-wrapper">
          <DingtalkAvatar
            :src="currentUser.avatar"
            :name="currentUser.nickname || currentUser.username"
            :size="64"
            class="avatar"
          />
        </div>
      </div>

      <!-- 内容：信息列表 -->
      <div class="content">
        <ul class="info-list">
          <li v-if="currentUser.mobile">
            <label>手机</label>
            <div class="value-wrapper">
              <span>{{ currentUser.mobile }}</span>
              <span class="icon-btn" @click="copyText(currentUser.mobile)">
                <span class="material-icons-outlined">content_copy</span>
              </span>
            </div>
          </li>
          <li v-if="currentUser.email">
            <label>邮箱</label>
            <div class="value-wrapper">
              <span class="email-text">{{ currentUser.email }}</span>
              <span class="icon-btn" @click="copyText(currentUser.email)">
                <span class="material-icons-outlined">content_copy</span>
              </span>
            </div>
          </li>
          <li>
            <label>部门</label>
            <div>{{ currentUser.dept?.deptName || currentUser.department || '-' }}</div>
          </li>
          <li>
            <label>职位</label>
            <div>{{ currentUser.position || '-' }}</div>
          </li>
          <li>
            <label>性别</label>
            <div>{{ genderText }}</div>
          </li>
          <li v-if="currentUser.birthday">
            <label>生日</label>
            <div>{{ currentUser.birthday }}</div>
          </li>
        </ul>
      </div>

      <!-- 底部操作按钮 -->
      <div class="footer">
        <a href="#" class="action-btn" @click.prevent="handleEdit">
          <span class="material-icons-outlined">edit</span>
          <span>编辑资料</span>
        </a>
        <a href="#" class="action-btn" @click.prevent="showChangePassword = true">
          <span class="material-icons-outlined">lock</span>
          <span>修改密码</span>
        </a>
        <a href="#" class="action-btn close-btn" @click.prevent="handleClose">
          <span class="material-icons-outlined">close</span>
        </a>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <EditProfileDialog
      v-model="showEditDialog"
      @save="handleSaveProfile"
    />

    <!-- 修改密码弹窗 -->
    <ChangePasswordDialog v-model="showChangePassword" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import EditProfileDialog from './EditProfileDialog.vue'
import ChangePasswordDialog from './ChangePasswordDialog.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()

const visible = ref(false)
const showEditDialog = ref(false)
const showChangePassword = ref(false)

// 数据源
const currentUser = computed(() => store.getters['user/currentUser'] || {})

const genderText = computed(() => {
  const gender = currentUser.value?.gender
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '保密'
})

// 复制文本
const copyText = (text) => {
  if (!text) return
  navigator.clipboard.writeText(String(text)).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 处理方法
const handleClose = () => emit('update:modelValue', false)

const handleEdit = () => {
  showEditDialog.value = true
}

const handleSaveProfile = async (formData) => {
  try {
    await store.dispatch('user/updateProfile', formData)
    ElMessage.success('资料已更新')
    showEditDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

// 监听
watch(() => props.modelValue, (val) => visible.value = val)
watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 对话框样式
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
  background: #fcfcfc;
}

:deep(.el-dialog__header) {
  display: none;
}

:deep(.el-dialog__body) {
  padding: 0;
  background: #fcfcfc;
}

.user-profile-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  background: #fcfcfc;
  color: #292a2c;
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e6e6e6;
}

.desc {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  flex: 1;
  padding-right: 16px;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: #292a2c;
  margin: 0 0 4px 0;
}

.user-id {
  font-size: 12px;
  color: #7f7f7f;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.avatar-wrapper .avatar {
  border-radius: 4px;
  flex-shrink: 0;
}

// 内容区
.content {
  width: 100%;
  text-align: left;
}

.info-list {
  list-style: none;
  margin: 16px 20px;
  padding: 0;
}

.info-list li {
  display: flex;
  align-items: center;
  height: 36px;
  line-height: 36px;
  font-size: 13px;
  border-bottom: 1px solid transparent;

  &:last-child {
    border-bottom: none;
  }
}

.info-list li label {
  min-width: 40px;
  margin-right: 16px;
  color: #7f7f7f;
  font-size: 13px;
  text-align: justify;
  text-align-last: justify;
}

.info-list li > div {
  flex: 1;
  color: #292a2c;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.value-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;

  .email-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 200px;
  }
}

.icon-btn {
  margin-left: 8px;
  cursor: pointer;
  color: #7f7f7f;
  transition: color 0.2s;

  &:hover {
    color: #4168e0;
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

// 底部操作区
.footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px 20px;
  padding-top: 20px;
  border-top: 1px solid #e6e6e6;
  gap: 8px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: #5d7ce8;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: background 0.2s;

  &:hover {
    background: rgba(93, 124, 232, 0.1);
  }

  .material-icons-outlined {
    font-size: 18px;
  }

  &.close-btn {
    color: #7f7f7f;

    &:hover {
      background: rgba(0, 0, 0, 0.05);
      color: #292a2c;
    }
  }
}

// 暗黑模式适配
.dark {
  .user-profile-container,
  :deep(.el-dialog) {
    background: #1a1a1a;
  }

  .header {
    border-bottom-color: #333;
  }

  .user-name {
    color: #e5e5e5;
  }

  .user-id,
  .info-list li label {
    color: #999;
  }

  .info-list li > div {
    color: #e5e5e5;
  }

  .footer {
    border-top-color: #333;
  }

  .action-btn {
    &.close-btn {
      color: #999;

      &:hover {
        background: rgba(255, 255, 255, 0.05);
        color: #e5e5e5;
      }
    }
  }
}
</style>
