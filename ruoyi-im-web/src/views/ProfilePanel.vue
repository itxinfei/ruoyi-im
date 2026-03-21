<template>
  <div class="desktop-profile-panel">
    <!-- 1. 精致顶部卡片 -->
    <div class="profile-hero-card">
      <div class="hero-bg" />
      <div class="hero-content">
        <div class="avatar-container" @click="triggerAvatarUpload">
          <DingtalkAvatar
            :src="userInfo.avatar"
            :name="userInfo.nickname"
            :size="80"
            shape="square"
          />
          <div class="avatar-mask">
            <span class="material-icons-outlined">photo_camera</span>
          </div>
        </div>
        <div class="user-basic">
          <div class="nickname-row">
            <span class="nickname">{{ userInfo.nickname || '未设置昵称' }}</span>
            <el-tag
              size="small"
              type="success"
              effect="dark"
              round
            >
              在线
            </el-tag>
          </div>
          <div class="username-id">
            账号: {{ userInfo.username }} | ID: {{ userInfo.id }}
          </div>
        </div>
      </div>
      <input
        ref="avatarInput"
        type="file"
        hidden
        accept="image/*"
        @change="handleAvatarChange"
      >
    </div>

    <!-- 2. 信息列表 (Desktop Card Style) -->
    <div class="profile-scroll-area custom-scrollbar">
      <div class="info-group">
        <div class="group-header">
          详细信息
        </div>
        <div class="info-card">
          <!-- 昵称 -->
          <div class="info-row">
            <span class="label">展示昵称</span>
            <div class="value-box">
              <el-input v-model="userInfo.nickname" size="default" @blur="updateField('nickname')" />
            </div>
          </div>
          <!-- 性别 -->
          <div class="info-row">
            <span class="label">性别</span>
            <div class="value-box">
              <el-radio-group v-model="userInfo.gender" @change="updateField('gender')">
                <el-radio :label="1">
                  男
                </el-radio>
                <el-radio :label="2">
                  女
                </el-radio>
                <el-radio :label="0">
                  保密
                </el-radio>
              </el-radio-group>
            </div>
          </div>
          <!-- 职位 -->
          <div class="info-row">
            <span class="label">当前职位</span>
            <div class="value-box">
              <el-input v-model="userInfo.position" placeholder="如：高级产品经理" @blur="updateField('position')" />
            </div>
          </div>
          <!-- 部门 -->
          <div class="info-row">
            <span class="label">所属部门</span>
            <span class="readonly-value">{{ userInfo.department || '研发中心' }}</span>
          </div>
        </div>
      </div>

      <div class="info-group">
        <div class="group-header">
          联系方式
        </div>
        <div class="info-card">
          <div class="info-row">
            <span class="label">手机号码</span>
            <span class="readonly-value">{{ userInfo.phone || '未绑定' }}</span>
          </div>
          <div class="info-row">
            <span class="label">电子邮箱</span>
            <div class="value-box">
              <el-input v-model="userInfo.email" placeholder="输入邮箱" @blur="updateField('email')" />
            </div>
          </div>
        </div>
      </div>

      <div class="info-group">
        <div class="group-header">
          安全与密码
        </div>
        <div class="info-card">
          <div class="info-row clickable" @click="showPasswordDialog = true">
            <span class="label">修改登录密码</span>
            <span class="material-icons-outlined arrow">lock_reset</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 修改密码弹窗 (Keep it clean) -->
    <el-dialog
      v-model="showPasswordDialog"
      title="安全重置密码"
      width="400px"
      append-to-body
    >
      <el-form label-position="top">
        <el-form-item label="当前旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">
          取消
        </el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handlePasswordUpdate">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo, updateUser, uploadAvatar, changePassword } from '@/api/im/user'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage } from 'element-plus'

const userInfo = ref({ id: '', username: '', nickname: '', avatar: '', gender: 0, position: '', department: '', email: '', phone: '' })
const showPasswordDialog = ref(false)
const avatarInput = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordLoading = ref(false)

const loadUser = async () => {
  const res = await getUserInfo()
  if (res.code === 200) userInfo.value = res.data
}

const updateField = async (field) => {
  try {
    await updateUser(userInfo.value.id, { [field]: userInfo.value[field] })
    ElMessage.success('个人资料已同步')
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

const triggerAvatarUpload = () => avatarInput.value?.click()
const handleAvatarChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('avatarfile', file)
  const res = await uploadAvatar(formData)
  if (res.code === 200) {
    userInfo.value.avatar = res.data
    ElMessage.success('头像上传成功')
  }
}

const handlePasswordUpdate = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  
  try {
    passwordLoading.value = true
    const res = await changePassword(
      userInfo.value.id,
      passwordForm.value.oldPassword,
      passwordForm.value.newPassword
    )
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      showPasswordDialog.value = false
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    } else {
      ElMessage.error(res.msg || '密码修改失败')
    }
  } catch (error) {
    console.error('密码修改失败:', error)
    ElMessage.error('密码修改失败，请重试')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(loadUser)
</script>

<style scoped lang="scss">
.desktop-profile-panel {
  height: 100%;
  background: var(--dt-bg-body);
  display: flex;
  flex-direction: column;
}

.profile-hero-card {
  position: relative;
  height: var(--dt-profile-hero-height, 180px);
  background: var(--dt-bg-card);
  flex-shrink: 0;
  border-bottom: 1px solid var(--dt-border-light);
  overflow: hidden;

  .hero-bg {
    height: var(--dt-profile-bg-height, 120px);
    background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 50%, var(--dt-brand-light) 100%);
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: var(--dt-gradient-overlay-height, 40px);
      background: linear-gradient(to bottom, transparent, var(--dt-bg-card));
    }
  }

  .hero-content {
    position: absolute;
    bottom: var(--dt-spacing-md, 16px);
    left: var(--dt-spacing-lg, 24px);
    display: flex;
    align-items: flex-end;
    gap: var(--dt-spacing-md, 16px);

    .avatar-container {
      position: relative;
      border: var(--dt-avatar-border, 4px) solid var(--dt-bg-card);
      border-radius: var(--dt-radius-lg);
      cursor: pointer;
      overflow: hidden;
      box-shadow: var(--dt-shadow-3);
      transition: all var(--dt-transition-base);

      &:hover {
        transform: scale(1.05);
        box-shadow: var(--dt-shadow-4);
      }

      .avatar-mask {
        position: absolute;
        inset: 0;
        background: var(--dt-bg-mask);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--dt-text-white);
        opacity: 0;
        transition: all var(--dt-transition-base);

        .material-icons-outlined {
          font-size: var(--dt-icon-size-xl, 24px);
        }
      }
      &:hover .avatar-mask { opacity: 1; }
    }

    .user-basic {
      padding-bottom: var(--dt-spacing-xs, 2px);
      .nickname-row {
        display: flex;
        align-items: center;
        gap: var(--dt-spacing-sm, 8px);
        margin-bottom: var(--dt-spacing-xs, 2px);

        .nickname {
          font-size: var(--dt-font-size-xl);
          font-weight: var(--dt-font-weight-bold);
          color: var(--dt-text-primary);
        }

        :deep(.el-tag) {
          background: var(--dt-success-color);
          border: none;
          color: var(--dt-text-white);
          font-weight: var(--dt-font-weight-medium);
        }
      }
      .username-id {
        font-size: var(--dt-font-size-sm);
        color: var(--dt-text-secondary);
        font-weight: var(--dt-font-weight-medium);
        opacity: 0.8;
      }
    }
  }
}

.profile-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-spacing-lg);

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-light);
    border-radius: var(--dt-radius-sm, 4px);

    &:hover {
      background: var(--dt-border-color);
    }
  }
}

.info-group {
  margin-bottom: var(--dt-spacing-lg);

  .group-header {
    font-size: var(--dt-font-size-sm);
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-tertiary);
    margin-bottom: var(--dt-spacing-sm);
    padding-left: var(--dt-spacing-xs);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

.info-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  border: 1px solid var(--dt-border-light);
  overflow: hidden;
  box-shadow: var(--dt-shadow-1);
  transition: all var(--dt-transition-base);

  &:hover {
    box-shadow: var(--dt-shadow-2);
  }

  .info-row {
    display: flex;
    align-items: center;
    padding: var(--dt-spacing-md) var(--dt-spacing-lg);
    border-bottom: 1px solid var(--dt-border-lighter);
    min-height: 56px;
    transition: all var(--dt-transition-fast);

    &:last-child {
      border-bottom: none;
    }

    &.clickable {
      cursor: pointer;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: var(--dt-indicator-width, 3px);
        background: var(--dt-brand-color);
        transform: scaleY(0);
        transition: transform var(--dt-transition-fast);
      }

      &:hover {
        background: var(--dt-brand-bg);

        &::after {
          transform: scaleY(1);
        }

        .arrow {
          color: var(--dt-brand-color);
          transform: translateX(var(--dt-transform-x, 2px));
        }
      }
    }

    .label {
      width: 100px;
      font-size: var(--dt-font-size-sm);
      color: var(--dt-text-secondary);
      font-weight: var(--dt-font-weight-medium);
    }

    .value-box {
      flex: 1;
      :deep(.el-input__wrapper) {
        box-shadow: none !important;
        padding: 0;
        background: transparent;
      }
      :deep(.el-input__inner) {
        font-weight: var(--dt-font-weight-semibold);
        color: var(--dt-text-primary);
      }
    }

    .readonly-value {
      font-weight: var(--dt-font-weight-semibold);
      color: var(--dt-text-primary);
      font-size: var(--dt-font-size-base);
    }

    .arrow {
      color: var(--dt-text-quaternary);
      margin-left: auto;
      transition: all var(--dt-transition-fast);
      font-size: var(--dt-icon-size-lg, 20px);
    }
  }
}

// 暗色模式兼容
:global(.dark) .desktop-profile-panel {
  background: var(--dt-bg-body-dark);

  .profile-hero-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    .hero-bg {
      background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 50%, var(--dt-brand-light) 100%);
    }

    .user-basic .nickname-row .nickname {
      color: var(--dt-text-primary-dark);
    }
  }

  .info-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    .info-row {
      border-color: var(--dt-border-dark);

      .readonly-value {
        color: var(--dt-text-primary-dark);
      }

      .value-box :deep(.el-input__inner) {
        color: var(--dt-text-primary-dark);
      }

      &.clickable:hover {
        background: var(--dt-brand-bg-dark);

        &::after {
          background: var(--dt-brand-color);
        }
      }
    }
  }
}
</style>
