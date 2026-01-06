<template>
  <div class="settings-container">
    <el-tabs v-model="activeTab" tab-position="left" class="settings-tabs">
      <el-tab-pane label="基本设置" name="basic">
        <div class="setting-section">
          <el-card>
            <template #header>
              <span>个人资料</span>
            </template>
            <div class="profile-header">
              <div class="avatar-container">
                <AvatarUpload
                  :avatar="userInfo.avatar"
                  :default-text="userInfo.nickName?.charAt(0) || 'U'"
                  :size="80"
                  @success="handleAvatarSuccess"
                  @error="handleAvatarError"
                />
              </div>
              <div class="user-basic">
                <h3>{{ userInfo.nickName || '用户' }}</h3>
                <p class="user-id">ID: {{ userInfo.userId || '-' }}</p>
              </div>
            </div>

            <el-form :model="profileForm" label-width="100px" class="profile-form">
              <el-form-item label="昵称">
                <el-input v-model="profileForm.nickName" placeholder="请输入昵称" maxlength="20" />
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio :value="0">男</el-radio>
                  <el-radio :value="1">女</el-radio>
                  <el-radio :value="2">保密</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="个性签名">
                <el-input
                  v-model="profileForm.signature"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入个性签名"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="profileLoading" @click="handleSaveProfile">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <span>账号安全</span>
            </template>
            <div class="security-item">
              <div class="security-info">
                <h4>登录密码</h4>
                <p>定期更换密码可以提高账号安全性</p>
              </div>
              <el-button type="primary" link @click="showPasswordDialog = true">修改密码</el-button>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="主题设置" name="theme">
        <div class="setting-section">
          <el-card>
            <template #header>
              <span>外观设置</span>
            </template>
            <div class="theme-setting">
              <div class="setting-row">
                <span class="setting-label">主题模式</span>
                <el-radio-group v-model="themeForm.mode" @change="handleThemeChange">
                  <el-radio-button value="light">浅色</el-radio-button>
                  <el-radio-button value="dark">深色</el-radio-button>
                  <el-radio-button value="auto">跟随系统</el-radio-button>
                </el-radio-group>
              </div>
              <div class="setting-row">
                <span class="setting-label">主题颜色</span>
                <div class="color-options">
                  <div
                    v-for="color in themeColors"
                    :key="color.value"
                    class="color-item"
                    :class="{ active: themeForm.primaryColor === color.value }"
                    :style="{ backgroundColor: color.value }"
                    @click="handleColorChange(color.value)"
                  >
                    <el-icon v-if="themeForm.primaryColor === color.value"><Check /></el-icon>
                  </div>
                </div>
              </div>
              <div class="setting-row">
                <span class="setting-label">字体大小</span>
                <el-slider
                  v-model="themeForm.fontSize"
                  :min="12"
                  :max="18"
                  :step="1"
                  :marks="fontSizeMarks"
                  style="width: 300px"
                  @change="handleFontSizeChange"
                />
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="通知设置" name="notification">
        <div class="setting-section">
          <el-card>
            <template #header>
              <span>消息通知</span>
            </template>
            <div class="setting-item">
              <div class="setting-info">
                <h4>启用通知</h4>
                <p>接收新消息的通知提醒</p>
              </div>
              <el-switch v-model="notificationForm.enabled" @change="handleNotificationChange" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>桌面通知</h4>
                <p>在桌面显示通知弹窗</p>
              </div>
              <el-switch
                v-model="notificationForm.desktop"
                :disabled="!notificationForm.enabled"
                @change="handleNotificationChange"
              />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>声音提醒</h4>
                <p>收到新消息时播放提示音</p>
              </div>
              <el-switch
                v-model="notificationForm.sound"
                :disabled="!notificationForm.enabled"
                @change="handleNotificationChange"
              />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>消息预览</h4>
                <p>在通知中显示消息内容预览</p>
              </div>
              <el-switch
                v-model="notificationForm.preview"
                :disabled="!notificationForm.enabled"
                @change="handleNotificationChange"
              />
            </div>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <span>免打扰设置</span>
            </template>
            <div class="setting-item">
              <div class="setting-info">
                <h4>免打扰模式</h4>
                <p>开启后将不会收到任何通知</p>
              </div>
              <el-switch
                v-model="notificationForm.doNotDisturb"
                @change="handleNotificationChange"
              />
            </div>
            <div v-if="notificationForm.doNotDisturb" class="setting-item">
              <div class="setting-info">
                <h4>免打扰时段</h4>
                <p>设置免打扰的时间段</p>
              </div>
              <div class="time-range">
                <el-time-picker
                  v-model="notificationForm.dndStart"
                  placeholder="开始时间"
                  format="HH:mm"
                />
                <span style="margin: 0 8px">至</span>
                <el-time-picker
                  v-model="notificationForm.dndEnd"
                  placeholder="结束时间"
                  format="HH:mm"
                />
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="隐私设置" name="privacy">
        <div class="setting-section">
          <el-card>
            <template #header>
              <span>隐私设置</span>
            </template>
            <div class="setting-item">
              <div class="setting-info">
                <h4>在线状态</h4>
                <p>谁可以看到我的在线状态</p>
              </div>
              <el-select v-model="privacyForm.onlineStatus" @change="handlePrivacyChange">
                <el-option label="所有人" value="all" />
                <el-option label="仅好友" value="friends" />
                <el-option label="不公开" value="none" />
              </el-select>
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>添加好友验证</h4>
                <p>别人添加我为好友时需要验证</p>
              </div>
              <el-switch v-model="privacyForm.friendVerification" @change="handlePrivacyChange" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>已读回执</h4>
                <p>对方可以看到我是否已读消息</p>
              </div>
              <el-switch v-model="privacyForm.readReceipt" @change="handlePrivacyChange" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>正在输入</h4>
                <p>对方可以看到我正在输入</p>
              </div>
              <el-switch v-model="privacyForm.typingStatus" @change="handlePrivacyChange" />
            </div>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <div class="card-header-with-action">
                <span>黑名单</span>
                <el-button type="primary" link @click="showBlockDialog = true">添加</el-button>
              </div>
            </template>
            <div v-if="blockedUsers.length === 0" class="empty-state">
              <el-empty description="暂无黑名单用户" />
            </div>
            <div v-else class="blocked-list">
              <div v-for="user in blockedUsers" :key="user.id" class="blocked-item">
                <div class="user-info">
                  <el-avatar :size="40" :src="user.avatar">{{ user.name?.charAt(0) }}</el-avatar>
                  <span class="user-name">{{ user.name }}</span>
                </div>
                <el-button type="danger" link @click="handleUnblock(user)">移除</el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px" destroy-on-close>
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入当前密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请确认新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * @file 设置页面
 * @description IM系统用户设置功能
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import AvatarUpload from '@/components/AvatarUpload/index.vue'

const router = useRouter()

// ==================== 响应式状态 ====================

/** 当前激活的标签 */
const activeTab = ref('basic')

/** 加载状态 */
const profileLoading = ref(false)
const passwordLoading = ref(false)

/** 表单引用 */
const passwordFormRef = ref(null)

/** 对话框显示控制 */
const showPasswordDialog = ref(false)
const showBlockDialog = ref(false)

/** 用户信息 */
const userInfo = ref({
  userId: '10001',
  nickName: '测试用户',
  avatar: '',
  phone: '138****8888',
  email: 'test@example.com',
  gender: 0,
  signature: '',
})

/** 个人资料表单 */
const profileForm = reactive({
  nickName: '',
  gender: 0,
  phone: '',
  email: '',
  signature: '',
})

/** 密码表单 */
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

/** 密码验证规则 */
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

/** 主题设置表单 */
const themeForm = reactive({
  mode: 'light',
  primaryColor: '#409eff',
  fontSize: 14,
})

/** 主题颜色选项 */
const themeColors = [
  { label: '默认蓝', value: '#409eff' },
  { label: '清新绿', value: '#67c23a' },
  { label: '活力橙', value: '#e6a23c' },
  { label: '热情红', value: '#f56c6c' },
  { label: '典雅紫', value: '#9b59b6' },
  { label: '稳重灰', value: '#606266' },
]

/** 字体大小标记 */
const fontSizeMarks = {
  12: '小',
  14: '中',
  16: '大',
  18: '特大',
}

/** 通知设置表单 */
const notificationForm = reactive({
  enabled: true,
  desktop: true,
  sound: true,
  preview: true,
  doNotDisturb: false,
  dndStart: null,
  dndEnd: null,
})

/** 隐私设置表单 */
const privacyForm = reactive({
  onlineStatus: 'all',
  friendVerification: true,
  readReceipt: true,
  typingStatus: true,
})

/** 黑名单用户列表 */
const blockedUsers = ref([])

// ==================== 方法定义 ====================

/**
 * 初始化表单数据
 */
const initFormData = () => {
  Object.assign(profileForm, {
    nickName: userInfo.value.nickName,
    gender: userInfo.value.gender,
    phone: userInfo.value.phone,
    email: userInfo.value.email,
    signature: userInfo.value.signature,
  })
}

/**
 * 头像上传成功
 */
const handleAvatarSuccess = avatarUrl => {
  userInfo.value.avatar = avatarUrl
  ElMessage.success('头像更新成功')
}

/**
 * 头像上传失败
 */
const handleAvatarError = error => {
  ElMessage.error(error || '头像上传失败')
}

/**
 * 保存个人资料
 */
const handleSaveProfile = async () => {
  profileLoading.value = true
  try {
    // 模拟保存
    await new Promise(resolve => setTimeout(resolve, 500))
    Object.assign(userInfo.value, profileForm)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    profileLoading.value = false
  }
}

/**
 * 修改密码
 */
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate()
  passwordLoading.value = true

  try {
    // 模拟修改密码
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('密码修改成功，请重新登录')
    showPasswordDialog.value = false
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    // 跳转登录页
    router.push('/login')
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

/**
 * 主题模式变化
 */
const handleThemeChange = mode => {
  // 应用主题
  if (mode === 'dark') {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
  localStorage.setItem('theme-mode', mode)
  ElMessage.success('主题已切换')
}

/**
 * 主题颜色变化
 */
const handleColorChange = color => {
  themeForm.primaryColor = color
  document.documentElement.style.setProperty('--el-color-primary', color)
  localStorage.setItem('theme-color', color)
}

/**
 * 字体大小变化
 */
const handleFontSizeChange = size => {
  document.documentElement.style.setProperty('--base-font-size', `${size}px`)
  localStorage.setItem('font-size', size)
}

/**
 * 通知设置变化
 */
const handleNotificationChange = () => {
  // 如果启用桌面通知，请求权限
  if (notificationForm.desktop && 'Notification' in window) {
    if (Notification.permission !== 'granted') {
      Notification.requestPermission()
    }
  }
  localStorage.setItem('notification-settings', JSON.stringify(notificationForm))
  ElMessage.success('设置已保存')
}

/**
 * 隐私设置变化
 */
const handlePrivacyChange = () => {
  localStorage.setItem('privacy-settings', JSON.stringify(privacyForm))
  ElMessage.success('设置已保存')
}

/**
 * 解除黑名单
 */
const handleUnblock = async user => {
  try {
    await ElMessageBox.confirm(`确定要将"${user.name}"移出黑名单吗？`, '确认', {
      type: 'warning',
    })
    const index = blockedUsers.value.findIndex(u => u.id === user.id)
    if (index > -1) {
      blockedUsers.value.splice(index, 1)
      ElMessage.success('已移出黑名单')
    }
  } catch {
    // 取消操作
  }
}

/**
 * 加载本地设置
 */
const loadLocalSettings = () => {
  // 加载主题设置
  const themeMode = localStorage.getItem('theme-mode')
  if (themeMode) {
    themeForm.mode = themeMode
    if (themeMode === 'dark') {
      document.documentElement.classList.add('dark')
    }
  }

  const themeColor = localStorage.getItem('theme-color')
  if (themeColor) {
    themeForm.primaryColor = themeColor
    document.documentElement.style.setProperty('--el-color-primary', themeColor)
  }

  const fontSize = localStorage.getItem('font-size')
  if (fontSize) {
    themeForm.fontSize = parseInt(fontSize)
  }

  // 加载通知设置
  const notificationSettings = localStorage.getItem('notification-settings')
  if (notificationSettings) {
    Object.assign(notificationForm, JSON.parse(notificationSettings))
  }

  // 加载隐私设置
  const privacySettings = localStorage.getItem('privacy-settings')
  if (privacySettings) {
    Object.assign(privacyForm, JSON.parse(privacySettings))
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  initFormData()
  loadLocalSettings()
})
</script>

<style lang="scss" scoped>
.settings-container {
  height: 100%;
  padding: 20px;
  background-color: #f5f5f5;

  .settings-tabs {
    height: 100%;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    :deep(.el-tabs__header) {
      margin-right: 0;
      border-right: 1px solid #e4e7ed;
      width: 150px;

      .el-tabs__item {
        height: 50px;
        line-height: 50px;
        text-align: left;
        padding-left: 20px;

        &.is-active {
          background-color: #ecf5ff;
        }
      }
    }

    :deep(.el-tabs__content) {
      padding: 20px;
      height: 100%;
      overflow-y: auto;
    }
  }
}

.setting-section {
  max-width: 800px;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;

  .avatar-container {
    margin-right: 24px;
    text-align: center;

    .avatar-upload {
      margin-top: 12px;
    }
  }

  .user-basic {
    h3 {
      margin: 0 0 8px;
      font-size: 20px;
    }

    .user-id {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }
}

.profile-form {
  max-width: 500px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;

  .security-info {
    h4 {
      margin: 0 0 4px;
      font-size: 14px;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 12px;
    }
  }
}

.theme-setting {
  .setting-row {
    display: flex;
    align-items: center;
    margin-bottom: 24px;

    .setting-label {
      width: 100px;
      font-size: 14px;
      color: #606266;
    }

    .color-options {
      display: flex;
      gap: 12px;

      .color-item {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: transform 0.2s;

        &:hover {
          transform: scale(1.1);
        }

        &.active {
          box-shadow:
            0 0 0 2px #fff,
            0 0 0 4px currentColor;
        }

        .el-icon {
          color: #fff;
        }
      }
    }
  }
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;

  &:last-child {
    border-bottom: none;
  }

  .setting-info {
    flex: 1;
    margin-right: 16px;

    h4 {
      margin: 0 0 4px;
      font-size: 14px;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 12px;
    }
  }

  .time-range {
    display: flex;
    align-items: center;
  }
}

.card-header-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  padding: 40px 0;
}

.blocked-list {
  .blocked-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .user-name {
        font-size: 14px;
      }
    }
  }
}
</style>
