<template>
  <div class="settings-container">
    <div class="settings-header">
      <div class="header-left">
        <el-button class="back-button" :icon="ArrowLeft" @click="goBack">返回</el-button>
      </div>
      <div class="header-center">
        <h2>设置</h2>
        <p>管理您的账户和应用程序设置</p>
      </div>
    </div>

    <div class="settings-content">
      <!-- 设置导航 -->
      <div class="settings-nav">
        <div
          v-for="section in settingsSections"
          :key="section.key"
          class="nav-item"
          :class="{ active: activeSection === section.key }"
          @click="activeSection = section.key"
        >
          <el-icon>
            <component :is="section.icon" />
          </el-icon>
          <span>{{ section.name }}</span>
        </div>
      </div>

      <!-- 设置内容 -->
      <div class="settings-panel">
        <!-- 个人信息设置 -->
        <div v-if="activeSection === 'profile'" class="settings-section">
          <h3>个人信息</h3>
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

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
          <div v-else class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
        </div>

        <!-- 隐私设置 -->
        <div v-if="activeSection === 'privacy'" class="settings-section">
          <h3>隐私设置</h3>

          <div class="setting-group">
            <h4>在线状态</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">显示在线状态</span>
                <span class="setting-desc">允许其他用户看到您的在线状态</span>
              </div>
              <el-switch v-model="privacySettings.showOnlineStatus" @change="savePrivacySettings" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">显示最后在线时间</span>
                <span class="setting-desc">允许其他用户看到您最后在线的时间</span>
              </div>
              <el-switch v-model="privacySettings.showLastSeen" @change="savePrivacySettings" />
            </div>
          </div>

          <div class="setting-group">
            <h4>消息隐私</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">已读回执</span>
                <span class="setting-desc">发送消息时显示已读状态</span>
              </div>
              <el-switch v-model="privacySettings.readReceipts" @change="savePrivacySettings" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">输入状态</span>
                <span class="setting-desc">显示正在输入状态</span>
              </div>
              <el-switch v-model="privacySettings.typingIndicator" @change="savePrivacySettings" />
            </div>
          </div>

          <div class="setting-group">
            <h4>添加好友</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">允许陌生人添加</span>
                <span class="setting-desc">允许任何人添加您为好友</span>
              </div>
              <el-switch v-model="privacySettings.allowStrangerAdd" @change="savePrivacySettings" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">需要验证</span>
                <span class="setting-desc">添加好友时需要验证</span>
              </div>
              <el-switch
                v-model="privacySettings.requireVerification"
                @change="savePrivacySettings"
              />
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-if="activeSection === 'notifications'" class="settings-section">
          <h3>通知设置</h3>

          <div class="setting-group">
            <h4>消息通知</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">新消息提醒</span>
                <span class="setting-desc">收到新消息时显示通知</span>
              </div>
              <el-switch
                v-model="notificationSettings.newMessage"
                @change="saveNotificationSettings"
              />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">群组消息</span>
                <span class="setting-desc">接收群组消息通知</span>
              </div>
              <el-switch
                v-model="notificationSettings.groupMessage"
                @change="saveNotificationSettings"
              />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">@提醒</span>
                <span class="setting-desc">被@时特别提醒</span>
              </div>
              <el-switch
                v-model="notificationSettings.mentionAlert"
                @change="saveNotificationSettings"
              />
            </div>
          </div>

          <div class="setting-group">
            <h4>声音提醒</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">消息提示音</span>
                <span class="setting-desc">收到消息时播放提示音</span>
              </div>
              <el-switch
                v-model="notificationSettings.messageSound"
                @change="saveNotificationSettings"
              />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">通话铃声</span>
                <span class="setting-desc">来电时播放铃声</span>
              </div>
              <el-switch
                v-model="notificationSettings.callRingtone"
                @change="saveNotificationSettings"
              />
            </div>
          </div>

          <div class="setting-group">
            <h4>免打扰</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">免打扰模式</span>
                <span class="setting-desc">开启后不接收任何通知</span>
              </div>
              <el-switch
                v-model="notificationSettings.doNotDisturb"
                @change="saveNotificationSettings"
              />
            </div>

            <div v-if="notificationSettings.doNotDisturb" class="setting-item">
              <div class="setting-info">
                <span class="setting-label">免打扰时间</span>
                <span class="setting-desc">设置免打扰的时间段</span>
              </div>
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

        <!-- 安全设置 -->
        <div v-if="activeSection === 'security'" class="settings-section">
          <h3>安全设置</h3>

          <div class="setting-group">
            <h4>密码管理</h4>
            <div class="setting-item full-width">
              <div class="setting-info">
                <span class="setting-label">修改密码</span>
                <span class="setting-desc">定期更换密码以提高安全性</span>
              </div>
              <el-button @click="showChangePassword = true">修改密码</el-button>
            </div>
          </div>

          <div class="setting-group">
            <h4>数据安全</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">消息加密</span>
                <span class="setting-desc">端到端加密保护消息安全</span>
              </div>
              <el-tag type="success">已启用</el-tag>
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">自动清理</span>
                <span class="setting-desc">定期清理过期消息</span>
              </div>
              <el-switch v-model="securitySettings.autoCleanup" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">清理时间</span>
                <span class="setting-desc">消息保留时间</span>
              </div>
              <el-select v-model="securitySettings.cleanupDays" placeholder="选择时间">
                <el-option label="7天" :value="7" />
                <el-option label="30天" :value="30" />
                <el-option label="90天" :value="90" />
                <el-option label="永久" :value="0" />
              </el-select>
            </div>
          </div>
        </div>

        <!-- 主题设置 -->
        <div v-if="activeSection === 'theme'" class="settings-section">
          <h3>主题设置</h3>

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
              <div class="setting-info">
                <span class="setting-label">字体大小</span>
                <span class="setting-desc">调整聊天界面字体大小</span>
              </div>
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

        <!-- 关于 -->
        <div v-if="activeSection === 'about'" class="settings-section">
          <h3>关于</h3>

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
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="showChangePassword"
      title="修改密码"
      width="400px"
      :close-on-click-modal="false"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Bell,
  ShieldCheck,
  Brush,
  InfoFilled,
  Check,
  Picture,
} from '@element-plus/icons-vue'
import { getCurrentUserInfo, getCurrentUserId, setUserInfo } from '@/utils/im-user'
import { updateProfile, changePassword as changePasswordApi, uploadAvatar } from '@/api/im/user'

// 响应式数据
const activeSection = ref('profile')
const loading = ref(true)
const saving = ref(false)
const avatarUploading = ref(false)
const showChangePassword = ref(false)
const changingPassword = ref(false)

// 设置分类
const settingsSections = [
  { key: 'profile', name: '个人信息', icon: User },
  { key: 'privacy', name: '隐私设置', icon: Lock },
  { key: 'notifications', name: '通知设置', icon: Bell },
  { key: 'security', name: '安全设置', icon: ShieldCheck },
  { key: 'theme', name: '主题设置', icon: Brush },
  { key: 'about', name: '关于', icon: InfoFilled },
]

// 个人信息表单
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

// 隐私设置（从localStorage加载）
const privacySettings = reactive({
  showOnlineStatus: true,
  showLastSeen: true,
  readReceipts: true,
  typingIndicator: true,
  allowStrangerAdd: false,
  requireVerification: true,
})

// 通知设置（从localStorage加载）
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

// 安全设置
const securitySettings = reactive({
  autoCleanup: true,
  cleanupDays: 90,
})

// 主题设置
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
  { label: '蓝色', value: '#1890ff' },
  { label: '绿色', value: '#52c41a' },
  { label: '橙色', value: '#faad14' },
  { label: '红色', value: '#f5222d' },
  { label: '紫色', value: '#722ed1' },
  { label: '青色', value: '#13c2c2' },
]

const backgroundOptions = [
  { label: '默认', value: 'default', preview: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)' },
  { label: '纯白', value: 'white', preview: '#ffffff' },
  { label: '护眼', value: 'eye', preview: '#e8f5e9' },
  {
    label: '星空',
    value: 'star',
    preview: 'linear-gradient(to bottom, #0f2027, #203a43, #2c5364)',
  },
]

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
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

const passwordFormRef = ref(null)

// 上传配置
const uploadUrl = computed(() => {
  return (import.meta.env.VITE_APP_BASE_API || '/api') + '/im/user/avatar'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token') || localStorage.getItem('Admin-Token')
  return {
    Authorization: `Bearer ${token}`,
  }
})

// 方法
const loadUserInfo = async () => {
  loading.value = true
  try {
    const userInfo = getCurrentUserInfo()
    if (userInfo) {
      Object.assign(profileForm, {
        id: userInfo.userId || userInfo.id,
        username: userInfo.username || userInfo.userName || '',
        nickname: userInfo.nickname || userInfo.nickName || '',
        signature: userInfo.signature || '',
        email: userInfo.email || '',
        phonenumber: userInfo.phonenumber || userInfo.phone || '',
        gender: userInfo.gender || 0,
        avatar: userInfo.avatar || '',
      })
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const loadSettings = () => {
  // 从localStorage加载设置
  const savedPrivacy = localStorage.getItem('privacySettings')
  if (savedPrivacy) {
    Object.assign(privacySettings, JSON.parse(savedPrivacy))
  }

  const savedNotification = localStorage.getItem('notificationSettings')
  if (savedNotification) {
    Object.assign(notificationSettings, JSON.parse(savedNotification))
  }

  const savedSecurity = localStorage.getItem('securitySettings')
  if (savedSecurity) {
    Object.assign(securitySettings, JSON.parse(savedSecurity))
  }

  // 加载主题设置
  currentTheme.value = localStorage.getItem('theme') || 'light'
  currentColor.value = localStorage.getItem('primaryColor') || '#1890ff'
  currentBackground.value = localStorage.getItem('chatBackground') || 'default'
  fontSize.value = localStorage.getItem('fontSize') || 'medium'

  // 应用主题
  applyTheme()
}

const saveProfile = async () => {
  if (!profileForm.nickname) {
    ElMessage.warning('请输入昵称')
    return
  }

  saving.value = true
  try {
    await updateProfile(profileForm.id, {
      nickname: profileForm.nickname,
      signature: profileForm.signature,
      email: profileForm.email,
      phonenumber: profileForm.phonenumber,
      gender: profileForm.gender,
    })

    // 更新localStorage中的用户信息
    const userInfo = getCurrentUserInfo()
    if (userInfo) {
      Object.assign(userInfo, {
        nickname: profileForm.nickname,
        nickName: profileForm.nickname,
        name: profileForm.nickname,
        signature: profileForm.signature,
        email: profileForm.email,
        phonenumber: profileForm.phonenumber,
        gender: profileForm.gender,
      })
      setUserInfo(userInfo)
    }

    ElMessage.success('个人信息保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const savePrivacySettings = () => {
  localStorage.setItem('privacySettings', JSON.stringify(privacySettings))
  ElMessage.success('隐私设置已保存')
}

const saveNotificationSettings = () => {
  localStorage.setItem('notificationSettings', JSON.stringify(notificationSettings))
  ElMessage.success('通知设置已保存')
}

const beforeAvatarUpload = file => {
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
  return true
}

const handleAvatarSuccess = async response => {
  avatarUploading.value = false
  if (response.code === 200) {
    const avatarUrl = response.data
    profileForm.avatar = avatarUrl

    // 更新localStorage中的用户信息
    const userInfo = getCurrentUserInfo()
    if (userInfo) {
      Object.assign(userInfo, { avatar: avatarUrl })
      setUserInfo(userInfo)
    }

    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.msg || '头像上传失败')
  }
}

const confirmChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()

    changingPassword.value = true
    try {
      await changePasswordApi(profileForm.id, passwordForm.oldPassword, passwordForm.newPassword)
      ElMessage.success('密码修改成功，请重新登录')
      showChangePassword.value = false

      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''

      // 3秒后自动登出
      setTimeout(() => {
        logout()
      }, 3000)
    } catch (error) {
      ElMessage.error(error.message || '密码修改失败')
    } finally {
      changingPassword.value = false
    }
  } catch (error) {
    // 表单验证失败
  }
}

const cancelChangePassword = () => {
  showChangePassword.value = false
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('Admin-Token')
  localStorage.removeItem('userInfo')
  window.location.href = '/login'
}

const setTheme = theme => {
  currentTheme.value = theme
  localStorage.setItem('theme', theme)
  applyTheme()
}

const setColor = color => {
  currentColor.value = color
  localStorage.setItem('primaryColor', color)
  applyTheme()
}

const setBackground = bg => {
  currentBackground.value = bg
  localStorage.setItem('chatBackground', bg)
  // 发送事件通知聊天组件更新背景
  window.dispatchEvent(new CustomEvent('chatBackgroundChange', { detail: bg }))
}

const setFontSize = size => {
  fontSize.value = size
  localStorage.setItem('fontSize', size)
  document.documentElement.setAttribute('data-font-size', size)
}

const applyTheme = () => {
  const root = document.documentElement

  // 应用主题模式
  let shouldBeDark = false
  if (currentTheme.value === 'dark') {
    shouldBeDark = true
  } else if (currentTheme.value === 'auto') {
    // 根据系统设置自动切换
    shouldBeDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  }
  // 'light' 或其他情况使用浅色模式

  if (shouldBeDark) {
    root.classList.add('dark')
  } else {
    root.classList.remove('dark')
  }

  // 同步darkMode到localStorage，供其他组件使用
  localStorage.setItem('darkMode', String(shouldBeDark))

  // 应用主色调
  root.style.setProperty('--el-color-primary', currentColor.value)
  root.style.setProperty('--primary-color', currentColor.value)

  // 应用字体大小
  document.documentElement.setAttribute('data-font-size', fontSize.value)

  // 发送事件通知其他组件主题已更改
  window.dispatchEvent(new CustomEvent('themeChange', { detail: { theme: currentTheme.value, isDark: shouldBeDark } }))
}

const showPrivacyPolicy = () => {
  ElMessage.info('隐私政策页面开发中...')
}

const showTermsOfService = () => {
  ElMessage.info('服务条款页面开发中...')
}

const showHelp = () => {
  ElMessage.info('帮助中心页面开发中...')
}

const checkUpdate = () => {
  ElMessage.success('当前已是最新版本')
}

// 路由
const router = useRouter()

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadUserInfo()
  loadSettings()
})
</script>

<style lang="scss" scoped>
.settings-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.settings-header {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
  gap: 16px;

  .header-left {
    flex-shrink: 0;
  }

  .header-center {
    flex: 1;
    text-align: center;

    h2 {
      margin: 0 0 8px 0;
      font-size: 28px;
      color: #333;
    }

    p {
      margin: 0;
      color: #666;
      font-size: 14px;
    }
  }
}

.settings-content {
  display: flex;
  gap: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.settings-nav {
  width: 240px;
  background: #fafafa;
  border-right: 1px solid #e5e5e5;
  padding: 16px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #666;
  font-size: 14px;

  &:hover {
    background: #f0f0f0;
    color: #1890ff;
  }

  &.active {
    background: #e6f7ff;
    color: #1890ff;
    border-right: 3px solid #1890ff;
  }

  .el-icon {
    font-size: 18px;
  }
}

.settings-panel {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  max-height: calc(100vh - 200px);
}

.settings-section {
  h3 {
    margin: 0 0 24px 0;
    font-size: 20px;
    color: #333;
    font-weight: 500;
  }
}

.setting-group {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;

  &:last-of-type {
    border-bottom: none;
  }

  h4 {
    margin: 0 0 16px 0;
    font-size: 16px;
    color: #333;
    font-weight: 500;
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &.full-width {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

.setting-info {
  flex: 1;

  .setting-label {
    display: block;
    font-size: 14px;
    color: #333;
    margin-bottom: 4px;
    font-weight: 500;
  }

  .setting-desc {
    font-size: 12px;
    color: #999;
  }
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;

  .avatar-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .el-upload {
      text-align: left;
    }
  }
}

.loading-container {
  padding: 20px 0;
}

.theme-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.theme-option {
  text-align: center;
  cursor: pointer;
  padding: 16px;
  border: 2px solid transparent;
  border-radius: 12px;
  transition: all 0.2s ease;

  &:hover {
    border-color: #e0e0e0;
    background: #fafafa;
  }

  &.active {
    border-color: #1890ff;
    background: #f0f8ff;
  }
}

.theme-preview {
  width: 80px;
  height: 56px;
  border-radius: 8px;
  margin: 0 auto 12px;
  border: 1px solid #e0e0e0;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .preview-header {
    flex: 1;
    background: #fff;
  }

  .preview-body {
    flex: 2;
  }

  &.light {
    .preview-body {
      background: #f0f0f0;
    }
  }

  &.dark {
    .preview-header {
      background: #1f1f1f;
    }
    .preview-body {
      background: #2f2f2f;
    }
  }

  &.auto {
    .preview-header {
      background: #fff;
    }
    .preview-body {
      background: #1f1f1f;
    }
  }
}

.theme-name {
  font-size: 14px;
  color: #666;
}

.color-options {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.color-option {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    transform: scale(1.1);
  }

  &.active {
    border-color: #333;
    transform: scale(1.1);
  }

  .color-check {
    color: white;
    font-size: 18px;
  }
}

.background-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.background-option {
  text-align: center;
  cursor: pointer;
  padding: 12px;
  border: 2px solid transparent;
  border-radius: 12px;
  transition: all 0.2s ease;

  &:hover {
    border-color: #e0e0e0;
  }

  &.active {
    border-color: #1890ff;
  }

  .background-preview {
    width: 100%;
    height: 80px;
    border-radius: 8px;
    margin-bottom: 8px;
    background-size: cover;
    background-position: center;
  }

  .background-name {
    font-size: 12px;
    color: #666;
  }
}

.time-separator {
  margin: 0 8px;
  color: #666;
}

.about-content {
  text-align: center;
  padding: 40px 20px;
}

.app-info {
  margin-bottom: 40px;

  .app-logo-wrapper {
    width: 80px;
    height: 80px;
    margin: 0 auto 20px;
  }

  .app-logo {
    width: 100%;
    height: 100%;
  }

  h4 {
    margin: 0 0 8px 0;
    font-size: 24px;
    color: #333;
  }

  .version {
    margin: 0 0 8px 0;
    color: #666;
    font-size: 14px;
  }

  .description {
    margin: 0;
    color: #999;
    font-size: 12px;
  }
}

.about-links {
  margin-bottom: 24px;

  a {
    display: inline-block;
    margin: 0 16px;
    color: #1890ff;
    text-decoration: none;
    font-size: 14px;

    &:hover {
      text-decoration: underline;
    }
  }
}

.copyright {
  color: #999;
  font-size: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .settings-container {
    padding: 16px;
  }

  .settings-content {
    flex-direction: column;
  }

  .settings-nav {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e5e5e5;
    display: flex;
    overflow-x: auto;
    padding: 8px;

    .nav-item {
      padding: 10px 16px;
      white-space: nowrap;
      border-right: none;
      border-bottom: 3px solid transparent;

      &.active {
        border-right: none;
        border-bottom-color: #1890ff;
      }
    }
  }

  .settings-panel {
    padding: 24px 16px;
  }

  .theme-options,
  .background-options {
    grid-template-columns: repeat(2, 1fr);
  }

  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

// 暗色主题
:deep(.dark) {
  .settings-container {
    background: #1a1a1a;
  }

  .settings-content {
    background: #2a2a2a;
  }

  .settings-nav {
    background: #222;
    border-right-color: #333;
  }

  .nav-item {
    color: #999;

    &:hover {
      background: #333;
      color: #1890ff;
    }

    &.active {
      background: #1a3a1a;
      color: #1890ff;
    }
  }

  .settings-section h3,
  .setting-group h4,
  .setting-info .setting-label {
    color: #e5e5e5;
  }

  .setting-info .setting-desc {
    color: #666;
  }
}
</style>
