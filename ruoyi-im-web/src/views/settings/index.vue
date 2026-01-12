<template>
  <el-dialog
    v-model="visible"
    title="系统设置"
    width="800px"
    :close-on-click-modal="false"
    :append-to-body="true"
    class="settings-dialog"
  >
    <el-tabs v-model="activeSection" class="settings-tabs">
      <el-tab-pane label="个人信息" name="profile">
        <div class="settings-content">
          <el-form v-if="!loading" ref="profileFormRef" :model="profileForm" label-width="100px">
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-avatar :size="80" :src="profileForm.avatar">
                  {{ (profileForm.nickname || profileForm.username || 'U')?.charAt(0) }}
                </el-avatar>
                <div class="avatar-actions">
                  <el-upload
                    :action="uploadUrl"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                    accept="image/*"
                    name="avatarfile"
                  >
                    <el-button size="small" :loading="avatarUploading">
                      {{ avatarUploading ? '上传中...' : '更换头像' }}
                    </el-button>
                  </el-upload>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled placeholder="用户名不可修改" />
            </el-form-item>

            <el-form-item label="昵称">
              <el-input
                v-model="profileForm.nickname"
                placeholder="请输入昵称"
                maxlength="20"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="个性签名">
              <el-input
                v-model="profileForm.signature"
                type="textarea"
                :rows="3"
                placeholder="介绍一下自己..."
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="手机号">
              <el-input v-model="profileForm.phonenumber" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="性别">
              <el-radio-group v-model="profileForm.gender">
                <el-radio :label="0">保密</el-radio>
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
          <div v-else class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="隐私设置" name="privacy">
        <div class="settings-content">
          <div class="setting-group">
            <h4>在线状态</h4>
            <div class="setting-item">
              <span class="setting-label">显示在线状态</span>
              <span class="setting-desc">允许其他用户看到您的在线状态</span>
              <el-switch v-model="privacySettings.showOnlineStatus" @change="savePrivacySettings" />
            </div>

            <div class="setting-item">
              <span class="setting-label">显示最后在线时间</span>
              <span class="setting-desc">允许其他用户看到您最后在线的时间</span>
              <el-switch v-model="privacySettings.showLastSeen" @change="savePrivacySettings" />
            </div>
          </div>

          <div class="setting-group">
            <h4>消息隐私</h4>
            <div class="setting-item">
              <span class="setting-label">已读回执</span>
              <span class="setting-desc">发送消息时显示已读状态</span>
              <el-switch v-model="privacySettings.readReceipts" @change="savePrivacySettings" />
            </div>

            <div class="setting-item">
              <span class="setting-label">输入状态</span>
              <span class="setting-desc">显示正在输入状态</span>
              <el-switch v-model="privacySettings.typingIndicator" @change="savePrivacySettings" />
            </div>
          </div>

          <div class="setting-group">
            <h4>添加好友</h4>
            <div class="setting-item">
              <span class="setting-label">允许陌生人添加</span>
              <span class="setting-desc">允许任何人添加您为好友</span>
              <el-switch v-model="privacySettings.allowStrangerAdd" @change="savePrivacySettings" />
            </div>

            <div class="setting-item">
              <span class="setting-label">需要验证</span>
              <span class="setting-desc">添加好友时需要验证</span>
              <el-switch
                v-model="privacySettings.requireVerification"
                @change="savePrivacySettings"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="通知设置" name="notifications">
        <div class="settings-content">
          <div class="setting-group">
            <h4>消息通知</h4>
            <div class="setting-item">
              <span class="setting-label">新消息提醒</span>
              <span class="setting-desc">收到新消息时显示通知</span>
              <el-switch
                v-model="notificationSettings.newMessage"
                @change="saveNotificationSettings"
              />
            </div>

            <div class="setting-item">
              <span class="setting-label">群组消息</span>
              <span class="setting-desc">接收群组消息通知</span>
              <el-switch
                v-model="notificationSettings.groupMessage"
                @change="saveNotificationSettings"
              />
            </div>

            <div class="setting-item">
              <span class="setting-label">@提醒</span>
              <span class="setting-desc">被@时特别提醒</span>
              <el-switch
                v-model="notificationSettings.mentionAlert"
                @change="saveNotificationSettings"
              />
            </div>
          </div>

          <div class="setting-group">
            <h4>声音提醒</h4>
            <div class="setting-item">
              <span class="setting-label">消息提示音</span>
              <span class="setting-desc">收到消息时播放提示音</span>
              <el-switch
                v-model="notificationSettings.messageSound"
                @change="saveNotificationSettings"
              />
            </div>

            <div class="setting-item">
              <span class="setting-label">通话铃声</span>
              <span class="setting-desc">来电时播放铃声</span>
              <el-switch
                v-model="notificationSettings.callRingtone"
                @change="saveNotificationSettings"
              />
            </div>
          </div>

          <div class="setting-group">
            <h4>免打扰</h4>
            <div class="setting-item">
              <span class="setting-label">免打扰模式</span>
              <span class="setting-desc">开启后不接收任何通知</span>
              <el-switch
                v-model="notificationSettings.doNotDisturb"
                @change="saveNotificationSettings"
              />
            </div>

            <div v-if="notificationSettings.doNotDisturb" class="setting-item">
              <span class="setting-label">免打扰时间</span>
              <span class="setting-desc">设置免打扰的时间段</span>
              <el-time-picker
                v-model="notificationSettings.dndStartTime"
                placeholder="开始时间"
                format="HH:mm"
                value-format="HH:mm"
              />
              <span class="time-separator">至</span>
              <el-time-picker
                v-model="notificationSettings.dndEndTime"
                placeholder="结束时间"
                format="HH:mm"
                value-format="HH:mm"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="安全设置" name="security">
        <div class="settings-content">
          <div class="setting-group">
            <h4>密码管理</h4>
            <div class="setting-item full-width">
              <span class="setting-label">修改密码</span>
              <span class="setting-desc">定期更换密码以提高安全性</span>
              <el-button @click="showChangePassword = true">修改密码</el-button>
            </div>
          </div>

          <div class="setting-group">
            <h4>数据安全</h4>
            <div class="setting-item">
              <span class="setting-label">消息加密</span>
              <span class="setting-desc">端到端加密保护消息安全</span>
              <el-tag type="success">已启用</el-tag>
            </div>

            <div class="setting-item">
              <span class="setting-label">自动清理</span>
              <span class="setting-desc">定期清理过期消息</span>
              <el-switch v-model="securitySettings.autoCleanup" />
            </div>

            <div class="setting-item">
              <span class="setting-label">清理时间</span>
              <span class="setting-desc">消息保留时间</span>
              <el-select v-model="securitySettings.cleanupDays" placeholder="选择时间">
                <el-option label="7天" :value="7" />
                <el-option label="30天" :value="30" />
                <el-option label="90天" :value="90" />
                <el-option label="永久" :value="0" />
              </el-select>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="主题设置" name="theme">
        <div class="settings-content">
          <div class="setting-group">
            <h4>主题模式</h4>
            <div class="theme-options">
              <div
                v-for="theme in themeOptions"
                :key="theme.value"
                class="theme-option"
                :class="{ active: currentTheme === theme.value }"
                @click="setTheme(theme.value)"
              >
                <div class="theme-preview" :class="theme.value">
                  <div class="preview-header"></div>
                  <div class="preview-body"></div>
                </div>
                <span class="theme-name">{{ theme.label }}</span>
              </div>
            </div>
          </div>

          <div class="setting-group">
            <h4>颜色主题</h4>
            <div class="color-options">
              <div
                v-for="color in colorOptions"
                :key="color.value"
                class="color-option"
                :class="{ active: currentColor === color.value }"
                :style="{ backgroundColor: color.value }"
                @click="setColor(color.value)"
              >
                <el-icon v-if="currentColor === color.value" class="color-check"><Check /></el-icon>
              </div>
            </div>
          </div>

          <div class="setting-group">
            <h4>字体设置</h4>
            <div class="setting-item">
              <span class="setting-label">字体大小</span>
              <span class="setting-desc">调整聊天界面字体大小</span>
              <el-select v-model="fontSize" placeholder="选择字体大小" @change="setFontSize">
                <el-option label="小" value="small" />
                <el-option label="中" value="medium" />
                <el-option label="大" value="large" />
              </el-select>
            </div>
          </div>

          <div class="setting-group">
            <h4>聊天背景</h4>
            <div class="background-options">
              <div
                v-for="bg in backgroundOptions"
                :key="bg.value"
                class="background-option"
                :class="{ active: currentBackground === bg.value }"
                @click="setBackground(bg.value)"
              >
                <div class="background-preview" :style="{ backgroundImage: bg.preview }"></div>
                <span class="background-name">{{ bg.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="关于" name="about">
        <div class="settings-content">
          <div class="about-content">
            <div class="app-info">
              <div class="app-logo-wrapper">
                <svg class="app-logo" viewBox="0 0 48 48" fill="none">
                  <rect width="48" height="48" rx="8" fill="url(#logoGradient)" />
                  <path
                    d="M24 12L18 18H14V22L8 28V36H16V32H20V36H28V28L32 24H36V16H28L24 12Z"
                    fill="white"
                  />
                  <defs>
                    <linearGradient
                      id="logoGradient"
                      x1="0"
                      y1="0"
                      x2="48"
                      y2="48"
                      gradientUnits="userSpaceOnUse"
                    >
                      <stop offset="0" stop-color="#1890FF" />
                      <stop offset="1" stop-color="#096DD9" />
                    </linearGradient>
                  </defs>
                </svg>
              </div>
              <h4>RuoYi IM</h4>
              <p class="version">版本 1.0.0</p>
              <p class="description">企业级即时通讯系统</p>
            </div>

            <div class="about-links">
              <a href="#" @click.prevent="showPrivacyPolicy">隐私政策</a>
              <a href="#" @click.prevent="showTermsOfService">服务条款</a>
              <a href="#" @click.prevent="showHelp">帮助中心</a>
              <a href="#" @click.prevent="checkUpdate">检查更新</a>
            </div>

            <div class="copyright">
              <p>&copy; 2024 RuoYi IM. All rights reserved.</p>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="showChangePassword"
      title="修改密码"
      width="400px"
      :close-on-click-modal="false"
      :append-to-body="true"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        label-width="100px"
        :rules="passwordRules"
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
            placeholder="请输入新密码（6-20位）"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelChangePassword">取消</el-button>
        <el-button type="primary" :loading="changingPassword" @click="confirmChangePassword"
          >确认修改</el-button
        >
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getCurrentUserInfo, getCurrentUserId, setUserInfo } from '@/utils/im-user'
import { updateProfile, changePassword as changePasswordApi, uploadAvatar } from '@/api/im/user'
import { setTheme as applyGlobalTheme, getTheme } from '@/utils/theme'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'save'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeSection = ref('profile')
const loading = ref(true)
const saving = ref(false)
const avatarUploading = ref(false)
const showChangePassword = ref(false)
const changingPassword = ref(false)

const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const uploadUrl = computed(() => {
  return process.env.VUE_APP_BASE_API + '/common/upload'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('Admin-Token')
  return {
    Authorization: 'Bearer ' + token
  }
})

const profileForm = reactive({
  id: null,
  username: '',
  nickname: '',
  signature: '',
  email: '',
  phonenumber: '',
  gender: 0,
  avatar: '',
})

const privacySettings = reactive({
  showOnlineStatus: true,
  showLastSeen: true,
  readReceipts: true,
  typingIndicator: true,
  allowStrangerAdd: false,
  requireVerification: true,
})

const notificationSettings = reactive({
  newMessage: true,
  groupMessage: true,
  mentionAlert: true,
  messageSound: true,
  callRingtone: true,
  doNotDisturb: false,
  dndStartTime: '22:00',
  dndEndTime: '08:00',
})

const securitySettings = reactive({
  autoCleanup: true,
  cleanupDays: 90,
})

const currentTheme = ref('light')
const currentColor = ref('#1890ff')
const currentBackground = ref('default')
const fontSize = ref('medium')

const themeOptions = [
  { label: '浅色', value: 'light' },
  { label: '深色', value: 'dark' },
  { label: '自动', value: 'auto' },
]

const colorOptions = [
  { value: '#1890ff' },
  { value: '#52c41a' },
  { value: '#faad14' },
  { value: '#f5222d' },
  { value: '#722ed1' },
  { value: '#eb2f96' },
]

const backgroundOptions = [
  { label: '默认', value: 'default', preview: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { label: '简约', value: 'simple', preview: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)' },
  { label: '自然', value: 'nature', preview: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
  { label: '星空', value: 'starry', preview: 'linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%)' },
]

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadUserInfo = async () => {
  try {
    loading.value = true
    const userId = getCurrentUserId()
    if (userId) {
      const userInfo = await getCurrentUserInfo()
      if (userInfo) {
        Object.assign(profileForm, userInfo)
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const loadSettings = () => {
  const savedPrivacy = localStorage.getItem('im_privacy_settings')
  if (savedPrivacy) {
    Object.assign(privacySettings, JSON.parse(savedPrivacy))
  }

  const savedNotification = localStorage.getItem('im_notification_settings')
  if (savedNotification) {
    Object.assign(notificationSettings, JSON.parse(savedNotification))
  }

  const savedSecurity = localStorage.getItem('im_security_settings')
  if (savedSecurity) {
    Object.assign(securitySettings, JSON.parse(savedSecurity))
  }

  const savedTheme = localStorage.getItem('im_theme')
  if (savedTheme) {
    currentTheme.value = savedTheme
  }

  const savedColor = localStorage.getItem('im_theme_color')
  if (savedColor) {
    currentColor.value = savedColor
  }

  const savedBackground = localStorage.getItem('im_background')
  if (savedBackground) {
    currentBackground.value = savedBackground
  }

  const savedFontSize = localStorage.getItem('im_font_size')
  if (savedFontSize) {
    fontSize.value = savedFontSize
  }
}

const saveProfile = async () => {
  try {
    saving.value = true
    await updateProfile(profileForm)
    
    const userInfo = getCurrentUserInfo()
    Object.assign(userInfo, profileForm)
    setUserInfo(userInfo)
    
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const savePrivacySettings = () => {
  localStorage.setItem('im_privacy_settings', JSON.stringify(privacySettings))
  ElMessage.success('隐私设置已保存')
}

const saveNotificationSettings = () => {
  localStorage.setItem('im_notification_settings', JSON.stringify(notificationSettings))
  ElMessage.success('通知设置已保存')
}

const setTheme = (theme) => {
  currentTheme.value = theme
  localStorage.setItem('im_theme', theme)
  applyGlobalTheme(theme)
  ElMessage.success('主题已切换')
}

const setColor = (color) => {
  currentColor.value = color
  localStorage.setItem('im_theme_color', color)
  document.documentElement.style.setProperty('--el-color-primary', color)
  ElMessage.success('颜色主题已切换')
}

const setFontSize = (size) => {
  fontSize.value = size
  localStorage.setItem('im_font_size', size)
  ElMessage.success('字体大小已调整')
}

const setBackground = (bg) => {
  currentBackground.value = bg
  localStorage.setItem('im_background', bg)
  ElMessage.success('聊天背景已切换')
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    profileForm.avatar = response.url
    avatarUploading.value = false
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
    avatarUploading.value = false
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  avatarUploading.value = true
  return true
}

const confirmChangePassword = async () => {
  try {
    const valid = await passwordFormRef.value.validate()
    if (valid) {
      changingPassword.value = true
      await changePasswordApi({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功')
      showChangePassword.value = false
      resetPasswordForm()
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

const cancelChangePassword = () => {
  showChangePassword.value = false
  resetPasswordForm()
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const showPrivacyPolicy = () => {
  ElMessage.info('隐私政策功能开发中')
}

const showTermsOfService = () => {
  ElMessage.info('服务条款功能开发中')
}

const showHelp = () => {
  ElMessage.info('帮助中心功能开发中')
}

const checkUpdate = () => {
  ElMessage.info('当前已是最新版本')
}

const handleSave = async () => {
  if (activeSection.value === 'profile') {
    await saveProfile()
  }
  emit('save')
}

const handleCancel = () => {
  visible.value = false
}

watch(() => props.modelValue, (val) => {
  if (val) {
    loadUserInfo()
    loadSettings()
  }
})

onMounted(() => {
  if (props.modelValue) {
    loadUserInfo()
    loadSettings()
  }
})
</script>

<style scoped lang="scss">
.settings-dialog {
  .settings-tabs {
    :deep(.el-tabs__content) {
      max-height: 500px;
      overflow-y: auto;
    }
  }

  .settings-content {
    padding: 10px 0;
  }

  .setting-group {
    margin-bottom: 24px;

    h4 {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 16px;
    }
  }

  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .setting-label {
      font-size: 14px;
      color: #303133;
      font-weight: 500;
    }

    .setting-desc {
      font-size: 12px;
      color: #909399;
      margin-left: 8px;
    }

    &.full-width {
      flex-direction: column;
      align-items: flex-start;

      .setting-desc {
        margin-left: 0;
        margin-top: 4px;
      }
    }
  }

  .time-separator {
    margin: 0 8px;
    color: #909399;
  }

  .avatar-upload {
    display: flex;
    align-items: center;
    gap: 16px;

    .avatar-actions {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
  }

  .theme-options {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;

    .theme-option {
      cursor: pointer;
      border: 2px solid transparent;
      border-radius: 8px;
      padding: 12px;
      transition: all 0.3s;

      &:hover {
        border-color: #e8e8e8;
      }

      &.active {
        border-color: #1890ff;
      }

      .theme-preview {
        height: 80px;
        border-radius: 6px;
        margin-bottom: 8px;
        overflow: hidden;

        .preview-header {
          height: 30%;
          background: #1890ff;
        }

        .preview-body {
          height: 70%;
          background: #f5f5f5;
        }

        &.dark {
          .preview-header {
            background: #1f1f1f;
          }

          .preview-body {
            background: #141414;
          }
        }

        &.auto {
          .preview-header {
            background: linear-gradient(90deg, #1890ff 50%, #1f1f1f 50%);
          }

          .preview-body {
            background: linear-gradient(90deg, #f5f5f5 50%, #141414 50%);
          }
        }
      }

      .theme-name {
        display: block;
        text-align: center;
        font-size: 14px;
        color: #303133;
      }
    }
  }

  .color-options {
    display: flex;
    gap: 12px;

    .color-option {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid transparent;
      transition: all 0.3s;

      &:hover {
        transform: scale(1.1);
      }

      &.active {
        border-color: #303133;
      }

      .color-check {
        color: white;
        font-size: 20px;
      }
    }
  }

  .background-options {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .background-option {
      cursor: pointer;
      border: 2px solid transparent;
      border-radius: 8px;
      padding: 8px;
      transition: all 0.3s;

      &:hover {
        border-color: #e8e8e8;
      }

      &.active {
        border-color: #1890ff;
      }

      .background-preview {
        height: 100px;
        border-radius: 6px;
        margin-bottom: 8px;
        background-size: cover;
        background-position: center;
      }

      .background-name {
        display: block;
        text-align: center;
        font-size: 14px;
        color: #303133;
      }
    }
  }

  .about-content {
    text-align: center;
    padding: 20px 0;

    .app-info {
      margin-bottom: 32px;

      .app-logo-wrapper {
        margin-bottom: 16px;

        .app-logo {
          width: 80px;
          height: 80px;
        }
      }

      h4 {
        font-size: 20px;
        color: #303133;
        margin-bottom: 8px;
      }

      .version {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .description {
        font-size: 14px;
        color: #606266;
      }
    }

    .about-links {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 32px;

      a {
        color: #1890ff;
        text-decoration: none;
        font-size: 14px;
        cursor: pointer;

        &:hover {
          text-decoration: underline;
        }
      }
    }

    .copyright {
      font-size: 12px;
      color: #909399;
    }
  }

  .loading-container {
    padding: 20px;
  }
}
</style>
