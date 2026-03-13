<template>
  <div class="desktop-profile-panel">
    <!-- 1. 精致顶部卡片 -->
    <div class="profile-hero-card">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="avatar-container" @click="triggerAvatarUpload">
          <DingtalkAvatar :src="userInfo.avatar" :name="userInfo.nickname" :size="80" shape="square" />
          <div class="avatar-mask">
            <span class="material-icons-outlined">photo_camera</span>
          </div>
        </div>
        <div class="user-basic">
          <div class="nickname-row">
            <span class="nickname">{{ userInfo.nickname || '未设置昵称' }}</span>
            <el-tag size="small" type="success" effect="dark" round>在线</el-tag>
          </div>
          <div class="username-id">账号: {{ userInfo.username }} | ID: {{ userInfo.id }}</div>
        </div>
      </div>
      <input ref="avatarInput" type="file" hidden accept="image/*" @change="handleAvatarChange" />
    </div>

    <!-- 2. 信息列表 (Desktop Card Style) -->
    <div class="profile-scroll-area custom-scrollbar">
      <div class="info-group">
        <div class="group-header">详细信息</div>
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
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
                <el-radio :label="0">保密</el-radio>
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
        <div class="group-header">联系方式</div>
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
        <div class="group-header">安全与密码</div>
        <div class="info-card">
          <div class="info-row clickable" @click="showPasswordDialog = true">
            <span class="label">修改登录密码</span>
            <span class="material-icons-outlined arrow">lock_reset</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 修改密码弹窗 (Keep it clean) -->
    <el-dialog v-model="showPasswordDialog" title="安全重置密码" width="400px" append-to-body>
      <el-form label-position="top">
        <el-form-item label="当前旧密码">
          <el-input type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordUpdate">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo, updateUser, uploadAvatar } from '@/api/im/user'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage } from 'element-plus'

const userInfo = ref({ id: '', username: '', nickname: '', avatar: '', gender: 0, position: '', department: '', email: '', phone: '' })
const showPasswordDialog = ref(false)
const avatarInput = ref(null)

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
  formData.append('avatar', file)
  const res = await uploadAvatar(formData)
  if (res.code === 200) {
    userInfo.value.avatar = res.data
    ElMessage.success('头像上传成功')
  }
}

const handlePasswordUpdate = () => {
  ElMessage.warning('密码修改服务正在维护中')
  showPasswordDialog.value = false
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
  height: 180px;
  background: var(--dt-bg-card);
  flex-shrink: 0;
  border-bottom: 1px solid var(--dt-border-light);

  .hero-bg {
    height: 100px;
    background: linear-gradient(120deg, var(--dt-brand-color) 0%, var(--dt-info-color) 100%);
  }

  .hero-content {
    position: absolute;
    bottom: 20px;
    left: 24px;
    display: flex;
    align-items: flex-end;
    gap: 16px;

    .avatar-container {
      position: relative;
      border: 4px solid var(--dt-bg-card);
      border-radius: 12px;
      cursor: pointer;
      overflow: hidden;
      box-shadow: var(--dt-shadow-2);

      .avatar-mask {
        position: absolute;
        inset: 0;
        background: var(--dt-bg-mask);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--dt-text-primary);
        opacity: 0;
        transition: 0.2s;
      }
      &:hover .avatar-mask { opacity: 1; }
    }

    .user-basic {
      padding-bottom: 4px;
      .nickname-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;
        .nickname { font-size: 20px; font-weight: 800; color: var(--dt-text-primary); }
      }
      .username-id { font-size: 12px; color: var(--dt-text-secondary); font-weight: 500; }
    }
  }
}

.profile-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.info-group {
  margin-bottom: 24px;
  .group-header { font-size: 13px; font-weight: 700; color: var(--dt-text-secondary); margin-bottom: 8px; padding-left: 4px; }
}

.info-card {
  background: var(--dt-bg-card);
  border-radius: 12px;
  border: 1px solid var(--dt-border-light);
  overflow: hidden;

  .info-row {
    display: flex;
    align-items: center;
    padding: 14px 16px;
    border-bottom: 1px solid var(--dt-border-lighter);
    min-height: 56px;

    &:last-child { border-bottom: none; }

    &.clickable {
      cursor: pointer;
      &:hover { background: var(--dt-bg-body); }
    }

    .label { width: 100px; font-size: 13px; color: var(--dt-text-secondary); font-weight: 500; }

    .value-box {
      flex: 1;
      :deep(.el-input__wrapper) { box-shadow: none !important; padding: 0; background: transparent; }
      :deep(.el-input__inner) { font-weight: 600; color: var(--dt-text-primary); }
    }

    .readonly-value { font-weight: 600; color: var(--dt-text-primary); font-size: 14px; }
    .arrow { color: var(--dt-border-color); margin-left: auto; }
  }
}

// 暗色模式兼容
:global(.dark) .desktop-profile-panel {
  background: var(--dt-bg-body-dark);
  .profile-hero-card {
    background: var(--dt-bg-card-dark); border-color: var(--dt-border-dark);
    .user-basic .nickname-row .nickname { color: var(--dt-text-primary-dark); }
  }
  .info-card {
    background: var(--dt-bg-card-dark); border-color: var(--dt-border-dark);
    .info-row {
      border-color: var(--dt-border-dark);
      .readonly-value { color: var(--dt-text-primary-dark); }
      .value-box :deep(.el-input__inner) { color: var(--dt-text-primary-dark); }
      &.clickable:hover { background: var(--dt-bg-hover-dark); }
    }
  }
}
</style>
