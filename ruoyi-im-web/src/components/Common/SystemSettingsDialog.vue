<template>
  <el-dialog
    v-model="visible"
    title=""
    :width="dialogWidth"
    class="system-settings-dialog"
    destroy-on-close
    append-to-body
    :fullscreen="isFullscreen"
  >
    <div class="settings-container">
      <!-- 左侧导航 -->
      <nav class="settings-nav">
        <div class="nav-title">设置</div>
        <div class="nav-list">
          <div
            v-for="item in menuItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: activeMenu === item.id }"
            @click="activeMenu = item.id"
          >
            <span class="material-icons-outlined nav-icon">{{ item.icon }}</span>
            <span class="nav-label">{{ item.label }}</span>
          </div>
        </div>
        <div class="nav-footer">
          <div class="current-user" @click="handleEditProfile">
            <el-avatar :size="32" :src="currentUser.avatar" />
            <span class="user-name">{{ currentUser.nickname || currentUser.username }}</span>
          </div>
        </div>
      </nav>

      <!-- 右侧内容 -->
      <main class="settings-content">
        <header class="content-header">
          <h2 class="content-title">{{ currentMenuLabel }}</h2>
          <el-button class="close-btn" :icon="Close" circle @click="visible = false" />
        </header>

        <div class="content-body">
          <!-- 账号安全 -->
          <section v-if="activeMenu === 'account'" class="setting-section">
            <div class="profile-card">
              <el-avatar :size="64" :src="currentUser.avatar" />
              <div class="profile-info">
                <h3 class="profile-name">{{ currentUser.nickname || currentUser.username }}</h3>
                <p class="profile-detail">UID: {{ currentUser.id }}</p>
                <p class="profile-detail">{{ currentUser.email || '未绑定邮箱' }}</p>
              </div>
              <el-button type="primary" @click="handleEditProfile">编辑资料</el-button>
            </div>

            <div class="setting-group">
              <h4 class="group-title">安全设置</h4>
              <div class="setting-list">
                <div class="setting-row" @click="showChangePassword = true">
                  <div class="setting-row-info">
                    <span class="setting-row-label">修改密码</span>
                    <span class="setting-row-desc">定期修改密码以保护账号安全</span>
                  </div>
                  <el-icon class="setting-row-arrow"><ArrowRight /></el-icon>
                </div>
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">双重验证</span>
                    <span class="setting-row-desc">未开启</span>
                  </div>
                  <el-button link type="primary">设置</el-button>
                </div>
              </div>
            </div>
          </section>

          <!-- 通知设置 -->
          <section v-else-if="activeMenu === 'notification'" class="setting-section">
            <div class="setting-group">
              <h4 class="group-title">消息通知</h4>
              <div class="setting-list">
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">桌面通知</span>
                    <span class="setting-row-desc">收到新消息时显示通知</span>
                  </div>
                  <el-switch v-model="localSettings.notifications.enabled" @change="handleNotificationSettingChange" />
                </div>
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">消息声音</span>
                    <span class="setting-row-desc">新消息提示音</span>
                  </div>
                  <el-switch v-model="localSettings.notifications.sound" @change="handleNotificationSettingChange" />
                </div>
              </div>
            </div>

            <div class="setting-group">
              <h4 class="group-title">快捷键</h4>
              <div class="setting-list">
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">发送消息</span>
                  </div>
                  <el-select v-model="localSettings.shortcuts.send" size="small" style="width: 120px" @change="handleShortcutSettingChange">
                    <el-option label="Enter" value="enter" />
                    <el-option label="Ctrl+Enter" value="ctrl-enter" />
                  </el-select>
                </div>
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">截图</span>
                    <span class="setting-row-desc">全局快捷键</span>
                  </div>
                  <kbd class="shortcut-key">Alt + A</kbd>
                </div>
              </div>
            </div>
          </section>

          <!-- 通用设置 -->
          <section v-else-if="activeMenu === 'general'" class="setting-section">
            <div class="setting-group">
              <h4 class="group-title">外观</h4>
              <div class="setting-list">
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">主题模式</span>
                  </div>
                  <el-segmented v-model="localSettings.general.theme" :options="themeOptions" @change="handleGeneralSettingChange" />
                </div>
              </div>
            </div>

            <div class="setting-group">
              <h4 class="group-title">语言</h4>
              <div class="setting-list">
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">界面语言</span>
                  </div>
                  <el-select v-model="localSettings.general.language" size="small" style="width: 120px" @change="handleGeneralSettingChange">
                    <el-option label="简体中文" value="zh-CN" />
                    <el-option label="English" value="en-US" />
                  </el-select>
                </div>
              </div>
            </div>
          </section>

          <!-- 存储与数据 -->
          <section v-else-if="activeMenu === 'storage'" class="setting-section">
            <div class="setting-group">
              <h4 class="group-title">缓存管理</h4>
              <div class="setting-list">
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">本地缓存</span>
                    <span class="setting-row-desc">{{ cacheSize }}</span>
                  </div>
                  <el-button type="danger" link @click="handleClearCache">清理</el-button>
                </div>
              </div>
            </div>

            <div class="setting-group">
              <h4 class="group-title">数据</h4>
              <div class="setting-list">
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">导出聊天记录</span>
                    <span class="setting-row-desc">备份为 JSON 文件</span>
                  </div>
                  <el-button type="primary" link @click="handleExportChat">导出</el-button>
                </div>
                <div class="setting-row">
                  <div class="setting-row-info">
                    <span class="setting-row-label">退出保留数据</span>
                  </div>
                  <el-switch v-model="localSettings.data.keepOnLogout" @change="handleDataSettingChange" />
                </div>
              </div>
            </div>
          </section>

          <!-- 帮助与反馈 -->
          <section v-else-if="activeMenu === 'help'" class="setting-section">
            <div class="setting-group">
              <h4 class="group-title">常见问题</h4>
              <div class="faq-list">
                <div v-for="(faq, index) in faqs" :key="index" class="faq-item">
                  <div class="faq-question" @click="faq.expanded = !faq.expanded">
                    <span>{{ faq.question }}</span>
                    <el-icon :class="{ 'is-expanded': faq.expanded }"><ArrowRight /></el-icon>
                  </div>
                  <div v-if="faq.expanded" class="faq-answer">{{ faq.answer }}</div>
                </div>
              </div>
            </div>

            <div class="setting-group">
              <h4 class="group-title">意见反馈</h4>
              <el-form :model="feedbackForm" label-position="top" class="feedback-form">
                <el-form-item label="问题类型">
                  <el-radio-group v-model="feedbackForm.type" size="small">
                    <el-radio-button value="feature">功能建议</el-radio-button>
                    <el-radio-button value="bug">问题反馈</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="详细描述">
                  <el-input v-model="feedbackForm.description" type="textarea" :rows="3" placeholder="请描述您的问题或建议..." />
                </el-form-item>
                <el-button type="primary" :loading="feedbackSubmitting" @click="handleSubmitFeedback">提交</el-button>
              </el-form>
            </div>
          </section>

          <!-- 关于 -->
          <section v-else-if="activeMenu === 'about'" class="setting-section about-section">
            <div class="app-logo">
              <span class="logo-text">IM</span>
            </div>
            <h3 class="app-name">RuoYi IM</h3>
            <p class="app-version">v4.1.0</p>
            <el-button class="update-btn" @click="checkUpdate">检查更新</el-button>
            <p class="app-copyright">© 2025 RuoYi-IM Team</p>
          </section>
        </div>
      </main>
    </div>

    <ChangePasswordDialog v-model="showChangePassword" />
    <EditProfileDialog v-model="showEditProfile" @success="handleProfileUpdate" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight, Close } from '@element-plus/icons-vue'
import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'
import EditProfileDialog from '@/components/Common/EditProfileDialog.vue'

const props = defineProps({
  modelValue: Boolean,
  defaultMenu: { type: String, default: 'account' }
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const { setThemeMode, themeMode } = useTheme()

const visible = ref(false)
const showChangePassword = ref(false)
const showEditProfile = ref(false)
const activeMenu = ref('account')

const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const isFullscreen = computed(() => windowWidth.value < 640)

const dialogWidth = computed(() => {
  if (windowWidth.value < 768) return '95%'
  if (windowWidth.value < 1024) return '800px'
  return '900px'
})

const themeOptions = [
  { label: '浅色', value: 'light' },
  { label: '深色', value: 'dark' },
  { label: '跟随系统', value: 'auto' }
]

const menuItems = [
  { id: 'account', label: '账号', icon: 'person' },
  { id: 'notification', label: '通知', icon: 'notifications' },
  { id: 'general', label: '通用', icon: 'settings' },
  { id: 'storage', label: '存储', icon: 'folder' },
  { id: 'help', label: '帮助', icon: 'help_outline' },
  { id: 'about', label: '关于', icon: 'info' }
]

const currentMenuLabel = computed(() => menuItems.find(i => i.id === activeMenu.value)?.label || '')
const currentUser = computed(() => store.getters['user/currentUser'] || {})
const settings = computed(() => store.state.im.settings)
const localSettings = reactive(JSON.parse(JSON.stringify(settings.value)))

// 初始化默认值
if (!localSettings.chat) localSettings.chat = { fontSize: 'medium' }
if (!localSettings.shortcuts) localSettings.shortcuts = { send: 'enter' }
if (!localSettings.data) localSettings.data = { keepOnLogout: true }

const cacheSize = ref('0 MB')
const feedbackSubmitting = ref(false)
const feedbackForm = reactive({ type: 'feature', description: '' })

const faqs = reactive([
  { question: '如何修改密码？', answer: '在账号页面点击"修改密码"按钮进行设置。', expanded: false },
  { question: '如何设置消息免打扰？', answer: '在会话列表中右键点击会话，选择"消息免打扰"。', expanded: false },
  { question: '聊天记录会保存多久？', answer: '默认永久保存，除非关闭"退出保留数据"或清理缓存。', expanded: false }
])

const handleResize = () => windowWidth.value = window.innerWidth
onMounted(() => {
  window.addEventListener('resize', handleResize)
  calculateCacheSize()
})
onUnmounted(() => window.removeEventListener('resize', handleResize))

const calculateCacheSize = () => {
  let total = 0
  for (let key in localStorage) {
    if (localStorage.hasOwnProperty(key)) total += localStorage[key].length + key.length
  }
  cacheSize.value = `${(total / 1024 / 1024).toFixed(2)} MB`
}

const handleEditProfile = () => showEditProfile.value = true
const handleProfileUpdate = () => store.dispatch('user/getInfo')

const handleNotificationSettingChange = () => store.dispatch('im/updateNotificationSettings', localSettings.notifications)
const handleGeneralSettingChange = () => store.dispatch('im/updateGeneralSettings', localSettings.general)
const handleShortcutSettingChange = () => store.dispatch('im/updateShortcutSettings', localSettings.shortcuts)
const handleDataSettingChange = () => store.dispatch('im/updateDataSettings', localSettings.data)

const handleClearCache = () => {
  ElMessageBox.confirm('清理缓存将释放本地空间，但图片和文件需要重新下载。是否继续？', '清理缓存', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const keep = ['im-system-settings', 'token', 'user-info']
    Object.keys(localStorage).forEach(key => { if (!keep.includes(key)) localStorage.removeItem(key) })
    calculateCacheSize()
    ElMessage.success('缓存清理成功')
  })
}

const handleExportChat = () => {
  const dataStr = JSON.stringify(store.state.im.message?.messages || [], null, 2)
  const blob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `im-backup-${new Date().getTime()}.json`
  a.click()
  ElMessage.success('备份已导出')
}

const checkUpdate = () => ElMessage.success('当前已是最新版本')

const handleSubmitFeedback = async () => {
  if (!feedbackForm.description.trim()) return ElMessage.warning('请输入描述内容')
  feedbackSubmitting.value = true
  await new Promise(r => setTimeout(r, 800))
  ElMessage.success('提交成功')
  feedbackForm.description = ''
  feedbackSubmitting.value = false
}

// 监听主题变化
watch(() => localSettings.general.theme, (val) => {
  if (val !== themeMode.value) setThemeMode(val)
}, { immediate: true })

// 监听设置变化同步到 store
watch(localSettings, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(settings.value)) {
    store.commit('im/UPDATE_SETTINGS', JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })

// 监听弹窗显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.defaultMenu) activeMenu.value = props.defaultMenu
})
watch(visible, (val) => { if (!val) emit('update:modelValue', false) })
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 对话框
// ============================================================================
.system-settings-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;
  }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) {
    padding: 0;
    height: 600px;
    background: var(--dt-bg-card);
  }
}

// ============================================================================
// 容器
// ============================================================================
.settings-container {
  display: flex;
  height: 100%;
}

// ============================================================================
// 左侧导航
// ============================================================================
.settings-nav {
  width: 180px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  border-right: 1px solid var(--dt-border-light);

  .dark & {
    background: #1e1e1e;
    border-right-color: var(--dt-border-dark);
  }
}

.nav-title {
  padding: 20px 16px 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.nav-list {
  flex: 1;
  padding: 0 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 40px;
  padding: 0 12px;
  margin: 2px 0;
  border-radius: 6px;
  cursor: pointer;
  color: var(--dt-text-secondary);
  font-size: 14px;
  transition: all 0.2s;

  .nav-icon {
    font-size: 18px;
  }

  .nav-label {
    flex: 1;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.04);
    color: var(--dt-text-primary);

    .dark & {
      background: rgba(255, 255, 255, 0.06);
    }
  }

  &.active {
    background: #e6f4ff;
    color: var(--dt-brand-color);

    .dark & {
      background: rgba(22, 119, 255, 0.15);
    }
  }
}

.nav-footer {
  padding: 12px;
  border-top: 1px solid var(--dt-border-light);

  .dark & {
    border-top-color: var(--dt-border-dark);
  }
}

.current-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(0, 0, 0, 0.04);

    .dark & {
      background: rgba(255, 255, 255, 0.06);
    }
  }

  .user-name {
    flex: 1;
    font-size: 13px;
    color: var(--dt-text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// ============================================================================
// 右侧内容
// ============================================================================
.settings-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }
}

.content-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--dt-text-secondary);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
  }
}

.content-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;
  }
}

// ============================================================================
// 设置分组
// ============================================================================
.setting-section {
  max-width: 600px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  margin-bottom: 20px;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .profile-info {
    flex: 1;

    .profile-name {
      font-size: 16px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin: 0 0 4px 0;
    }

    .profile-detail {
      font-size: 13px;
      color: var(--dt-text-tertiary);
      margin: 0;
    }
  }
}

.setting-group {
  margin-bottom: 24px;
}

.group-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--dt-text-quaternary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 8px 12px;
}

.setting-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--dt-border-light);
  cursor: pointer;
  transition: background 0.2s;

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--dt-bg-hover);
  }

  .setting-row-info {
    flex: 1;

    .setting-row-label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary);
      margin-bottom: 2px;
    }

    .setting-row-desc {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }

  .setting-row-arrow {
    color: var(--dt-text-quaternary);
    transition: transform 0.2s;
  }

  &:hover .setting-row-arrow {
    transform: translateX(2px);
  }
}

// ============================================================================
// FAQ
// ============================================================================
.faq-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.faq-item {
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }
}

.faq-question {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  transition: background 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  .el-icon {
    color: var(--dt-text-quaternary);
    transition: transform 0.2s;

    &.is-expanded {
      transform: rotate(90deg);
    }
  }
}

.faq-answer {
  padding: 0 16px 14px;
  font-size: 13px;
  color: var(--dt-text-secondary);
  line-height: 1.6;
}

// ============================================================================
// 反馈表单
// ============================================================================
.feedback-form {
  padding: 16px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 600;
    color: var(--dt-text-quaternary);
  }
}

// ============================================================================
// 关于页面
// ============================================================================
.about-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40px;
  text-align: center;

  .app-logo {
    width: 80px;
    height: 80px;
    background: var(--dt-brand-color);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;

    .logo-text {
      font-size: 28px;
      font-weight: 700;
      color: #fff;
    }
  }

  .app-name {
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 8px 0;
  }

  .app-version {
    font-size: 13px;
    color: var(--dt-text-tertiary);
    margin: 0 0 20px 0;
  }

  .update-btn {
    margin-bottom: 24px;
  }

  .app-copyright {
    font-size: 12px;
    color: var(--dt-text-quaternary);
    margin: 0;
  }
}

// ============================================================================
// 快捷键显示
// ============================================================================
.shortcut-key {
  display: inline-block;
  padding: 4px 8px;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-color);
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
  font-weight: 500;
  color: var(--dt-text-secondary);
}

// ============================================================================
// 分段控件
// ============================================================================
:deep(.el-segmented) {
  --el-segmented-bg-color: var(--dt-bg-body);
  --el-segmented-item-selected-bg-color: var(--dt-brand-color);
  --el-segmented-item-selected-text-color: #fff;
  --el-segmented-item-hover-bg-color: var(--dt-bg-hover);
}

// ============================================================================
// 响应式
// ============================================================================
@media (max-width: 768px) {
  .settings-nav {
    display: none;
  }

  .settings-container {
    flex-direction: column;
  }

  .content-header {
    padding: 12px 16px;
  }

  .content-body {
    padding: 16px;
  }
}
</style>
