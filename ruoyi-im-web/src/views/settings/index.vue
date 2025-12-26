<template>
  <div class="settings-container">
    <div class="settings-header">
      <h2>设置</h2>
      <p>管理您的账户和应用程序设置</p>
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
          <i :class="section.icon"></i>
          <span>{{ section.name }}</span>
        </div>
      </div>

      <!-- 设置内容 -->
      <div class="settings-panel">
        <!-- 个人信息设置 -->
        <div v-if="activeSection === 'profile'" class="settings-section">
          <h3>个人信息</h3>
          <el-form :model="profileForm" label-width="100px">
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-avatar :size="80" :src="profileForm.avatar">
                  {{ profileForm.name?.charAt(0) }}
                </el-avatar>
                <el-button size="small" @click="uploadAvatar">更换头像</el-button>
              </div>
            </el-form-item>

            <el-form-item label="昵称">
              <el-input v-model="profileForm.name" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="个性签名">
              <el-input
                v-model="profileForm.signature"
                type="textarea"
                :rows="3"
                placeholder="请输入个性签名"
              />
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
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
              <el-switch v-model="privacySettings.showOnlineStatus" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">显示最后在线时间</span>
                <span class="setting-desc">允许其他用户看到您最后在线的时间</span>
              </div>
              <el-switch v-model="privacySettings.showLastSeen" />
            </div>
          </div>

          <div class="setting-group">
            <h4>消息隐私</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">已读回执</span>
                <span class="setting-desc">发送消息时显示已读状态</span>
              </div>
              <el-switch v-model="privacySettings.readReceipts" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">输入状态</span>
                <span class="setting-desc">显示正在输入状态</span>
              </div>
              <el-switch v-model="privacySettings.typingIndicator" />
            </div>
          </div>

          <div class="setting-group">
            <h4>添加好友</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">允许陌生人添加</span>
                <span class="setting-desc">允许任何人添加您为好友</span>
              </div>
              <el-switch v-model="privacySettings.allowStrangerAdd" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">需要验证</span>
                <span class="setting-desc">添加好友时需要验证</span>
              </div>
              <el-switch v-model="privacySettings.requireVerification" />
            </div>
          </div>

          <el-button type="primary" @click="savePrivacySettings">保存设置</el-button>
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
              <el-switch v-model="notificationSettings.newMessage" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">群组消息</span>
                <span class="setting-desc">接收群组消息通知</span>
              </div>
              <el-switch v-model="notificationSettings.groupMessage" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">@提醒</span>
                <span class="setting-desc">被@时特别提醒</span>
              </div>
              <el-switch v-model="notificationSettings.mentionAlert" />
            </div>
          </div>

          <div class="setting-group">
            <h4>声音提醒</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">消息提示音</span>
                <span class="setting-desc">收到消息时播放提示音</span>
              </div>
              <el-switch v-model="notificationSettings.messageSound" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">通话铃声</span>
                <span class="setting-desc">来电时播放铃声</span>
              </div>
              <el-switch v-model="notificationSettings.callRingtone" />
            </div>
          </div>

          <div class="setting-group">
            <h4>免打扰</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">免打扰模式</span>
                <span class="setting-desc">开启后不接收任何通知</span>
              </div>
              <el-switch v-model="notificationSettings.doNotDisturb" />
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
              />
              <span class="time-separator">至</span>
              <el-time-picker
                v-model="notificationSettings.dndEndTime"
                placeholder="结束时间"
                format="HH:mm"
              />
            </div>
          </div>

          <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
        </div>

        <!-- 安全设置 -->
        <div v-if="activeSection === 'security'" class="settings-section">
          <h3>安全设置</h3>

          <div class="setting-group">
            <h4>密码管理</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">修改密码</span>
                <span class="setting-desc">定期更换密码以提高安全性</span>
              </div>
              <el-button @click="showChangePassword = true">修改密码</el-button>
            </div>
          </div>

          <div class="setting-group">
            <h4>登录安全</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">两步验证</span>
                <span class="setting-desc">启用两步验证提高账户安全性</span>
              </div>
              <el-switch v-model="securitySettings.twoFactorAuth" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">登录设备管理</span>
                <span class="setting-desc">查看和管理已登录的设备</span>
              </div>
              <el-button @click="showDeviceManagement = true">管理设备</el-button>
            </div>
          </div>

          <div class="setting-group">
            <h4>数据安全</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">消息加密</span>
                <span class="setting-desc">端到端加密保护消息安全</span>
              </div>
              <el-switch v-model="securitySettings.messageEncryption" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">自动清理</span>
                <span class="setting-desc">定期清理过期消息</span>
              </div>
              <el-switch v-model="securitySettings.autoCleanup" />
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
                @click="currentTheme = theme.value"
              >
                <div class="theme-preview" :class="theme.value"></div>
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
                @click="currentColor = color.value"
              ></div>
            </div>
          </div>

          <div class="setting-group">
            <h4>字体设置</h4>
            <div class="setting-item">
              <div class="setting-info">
                <span class="setting-label">字体大小</span>
                <span class="setting-desc">调整界面字体大小</span>
              </div>
              <el-select v-model="fontSize" placeholder="选择字体大小">
                <el-option label="小" value="small" />
                <el-option label="中" value="medium" />
                <el-option label="大" value="large" />
              </el-select>
            </div>
          </div>

          <el-button type="primary" @click="saveThemeSettings">保存设置</el-button>
        </div>

        <!-- 关于 -->
        <div v-if="activeSection === 'about'" class="settings-section">
          <h3>关于</h3>

          <div class="about-content">
            <div class="app-info">
              <img src="/logo.png" alt="Logo" class="app-logo" />
              <h4>RuoYi IM</h4>
              <p class="version">版本 1.0.0</p>
            </div>

            <div class="about-links">
              <a href="#" @click.prevent="showPrivacyPolicy">隐私政策</a>
              <a href="#" @click.prevent="showTermsOfService">服务条款</a>
              <a href="#" @click.prevent="showHelp">帮助中心</a>
              <a href="#" @click.prevent="contactSupport">联系支持</a>
            </div>

            <div class="copyright">
              <p>&copy; 2024 RuoYi IM. All rights reserved.</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showChangePassword" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.currentPassword" type="password" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 设备管理弹窗 -->
    <el-dialog v-model="showDeviceManagement" title="登录设备管理" width="600px">
      <div class="device-list">
        <div v-for="device in devices" :key="device.id" class="device-item">
          <div class="device-info">
            <div class="device-name">{{ device.name }}</div>
            <div class="device-details">
              <span>{{ device.browser }}</span>
              <span>{{ device.location }}</span>
              <span>{{ device.lastLogin }}</span>
            </div>
          </div>
          <div class="device-actions">
            <el-button size="small" @click="logoutDevice(device.id)">退出登录</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const activeSection = ref('profile')
const showChangePassword = ref(false)
const showDeviceManagement = ref(false)

// 设置分类
const settingsSections = [
  { key: 'profile', name: '个人信息', icon: 'el-icon-user' },
  { key: 'privacy', name: '隐私设置', icon: 'el-icon-lock' },
  { key: 'notifications', name: '通知设置', icon: 'el-icon-bell' },
  { key: 'security', name: '安全设置', icon: 'el-icon-shield' },
  { key: 'theme', name: '主题设置', icon: 'el-icon-palette' },
  { key: 'about', name: '关于', icon: 'el-icon-info' },
]

// 个人信息表单
const profileForm = reactive({
  name: '张三',
  signature: '今天天气真不错！',
  email: 'zhangsan@example.com',
  phone: '13800138001',
  avatar: '',
})

// 隐私设置
const privacySettings = reactive({
  showOnlineStatus: true,
  showLastSeen: true,
  readReceipts: true,
  typingIndicator: true,
  allowStrangerAdd: false,
  requireVerification: true,
})

// 通知设置
const notificationSettings = reactive({
  newMessage: true,
  groupMessage: true,
  mentionAlert: true,
  messageSound: true,
  callRingtone: true,
  doNotDisturb: false,
  dndStartTime: null,
  dndEndTime: null,
})

// 安全设置
const securitySettings = reactive({
  twoFactorAuth: false,
  messageEncryption: true,
  autoCleanup: true,
})

// 主题设置
const currentTheme = ref('light')
const currentColor = ref('#1890ff')
const fontSize = ref('medium')

const themeOptions = [
  { label: '浅色', value: 'light' },
  { label: '深色', value: 'dark' },
  { label: '自动', value: 'auto' },
]

const colorOptions = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2']

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 设备列表
const devices = ref([
  {
    id: '1',
    name: 'Chrome on Windows',
    browser: 'Chrome 120.0',
    location: '北京',
    lastLogin: '2024-01-15 14:30',
  },
  {
    id: '2',
    name: 'Safari on iPhone',
    browser: 'Safari 17.0',
    location: '上海',
    lastLogin: '2024-01-14 09:15',
  },
])

// 方法
const uploadAvatar = () => {
  ElMessage.info('头像上传功能开发中...')
}

const saveProfile = () => {
  ElMessage.success('个人信息保存成功')
}

const savePrivacySettings = () => {
  ElMessage.success('隐私设置保存成功')
}

const saveNotificationSettings = () => {
  ElMessage.success('通知设置保存成功')
}

const saveThemeSettings = () => {
  ElMessage.success('主题设置保存成功')
}

const changePassword = () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  ElMessage.success('密码修改成功')
  showChangePassword.value = false
}

const logoutDevice = () => {
  ElMessage.success('设备已退出登录')
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

const contactSupport = () => {
  ElMessage.info('联系支持功能开发中...')
}
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
  text-align: center;
  margin-bottom: 32px;

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

.settings-content {
  display: flex;
  gap: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.settings-nav {
  width: 240px;
  background: #fafafa;
  border-right: 1px solid #e5e5e5;
  padding: 24px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #666;

  &:hover {
    background: #f0f0f0;
    color: #1890ff;
  }

  &.active {
    background: #e6f7ff;
    color: #1890ff;
    border-right: 3px solid #1890ff;
  }

  i {
    margin-right: 12px;
    font-size: 16px;
  }

  span {
    font-size: 14px;
  }
}

.settings-panel {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  max-height: 600px;
}

.settings-section {
  h3 {
    margin: 0 0 24px 0;
    font-size: 20px;
    color: #333;
  }
}

.setting-group {
  margin-bottom: 32px;

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
}

.setting-info {
  flex: 1;

  .setting-label {
    display: block;
    font-size: 14px;
    color: #333;
    margin-bottom: 4px;
  }

  .setting-desc {
    font-size: 12px;
    color: #999;
  }
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
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
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    border-color: #e0e0e0;
  }

  &.active {
    border-color: #1890ff;
    background: #f0f8ff;
  }
}

.theme-preview {
  width: 60px;
  height: 40px;
  border-radius: 4px;
  margin: 0 auto 8px;
  border: 1px solid #e0e0e0;

  &.light {
    background: linear-gradient(to bottom, #ffffff 50%, #f0f0f0 50%);
  }

  &.dark {
    background: linear-gradient(to bottom, #1f1f1f 50%, #2f2f2f 50%);
  }

  &.auto {
    background: linear-gradient(to bottom, #ffffff 50%, #1f1f1f 50%);
  }
}

.theme-name {
  font-size: 12px;
  color: #666;
}

.color-options {
  display: flex;
  gap: 12px;
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;

  &:hover {
    transform: scale(1.1);
  }

  &.active {
    border-color: #333;
    transform: scale(1.1);
  }
}

.time-separator {
  margin: 0 8px;
  color: #666;
}

.about-content {
  text-align: center;
}

.app-info {
  margin-bottom: 32px;

  .app-logo {
    width: 80px;
    height: 80px;
    margin-bottom: 16px;
  }

  h4 {
    margin: 0 0 8px 0;
    font-size: 20px;
    color: #333;
  }

  .version {
    color: #666;
    font-size: 14px;
  }
}

.about-links {
  margin-bottom: 24px;

  a {
    display: inline-block;
    margin: 0 12px;
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

.device-list {
  max-height: 400px;
  overflow-y: auto;
}

.device-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.device-info {
  flex: 1;

  .device-name {
    font-size: 14px;
    color: #333;
    margin-bottom: 4px;
  }

  .device-details {
    font-size: 12px;
    color: #666;

    span {
      margin-right: 12px;
    }
  }
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
  }

  .nav-item {
    padding: 16px 24px;
  }

  .settings-panel {
    padding: 24px;
  }

  .theme-options {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
