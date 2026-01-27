<template>
  <el-dialog
    v-model="visible"
    title=""
    :width="dialogWidth"
    :class="['system-settings-dialog', { 'is-mobile': isMobile }]"
    destroy-on-close
    append-to-body
    :fullscreen="isFullscreen"
  >
    <div class="settings-wrapper">
      <!-- 左侧导航栏 -->
      <aside class="settings-sidebar">
        <!-- 设置标题 -->
        <div class="sidebar-header">
          <div class="settings-logo">
            <span class="material-icons-outlined">settings</span>
          </div>
        </div>

        <!-- 导航菜单 -->
        <nav class="sidebar-nav">
          <div
            v-for="item in filteredMenuItems"
            :key="item.id"
            class="nav-item-group"
          >
            <div
              class="nav-item"
              :class="{ active: activeMenu === item.id }"
              @click="activeMenu = item.id"
            >
              <span class="nav-icon material-icons-outlined">{{ item.icon }}</span>
              <span class="nav-label">{{ item.label }}</span>
              <span v-if="activeMenu === item.id" class="nav-indicator"></span>
            </div>
          </div>
        </nav>

        <!-- 底部用户信息 -->
        <div class="sidebar-footer">
          <div class="user-mini-card" @click="handleShowProfile">
            <el-avatar :size="32" :src="currentUser.avatar" />
            <div class="user-mini-info">
              <span class="user-mini-name">{{ currentUser.nickname || currentUser.username }}</span>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧内容区 -->
      <main class="settings-main">
        <header class="main-header">
          <h3 class="header-title">{{ currentMenuLabel }}</h3>
          <div class="header-actions">
            <!-- 搜索框移到这里 -->
            <el-input
              v-if="!isMobile"
              v-model="searchQuery"
              placeholder="搜索设置..."
              prefix-icon="Search"
              clearable
              size="default"
              class="header-search"
            />
            <el-button circle @click="visible = false" class="close-btn">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </header>

        <div class="main-content">
          <transition name="page-fade" mode="out-in">
            <div :key="activeMenu" class="content-container">
              
              <!-- 账号安全 -->
              <section v-if="activeMenu === 'account'" class="space-y-6">
                <div class="bg-gradient-to-br from-primary/5 to-transparent p-6 rounded-2xl border border-primary/10">
                    <div class="flex items-start gap-6">
                      <div class="avatar-gradient-wrapper">
                        <div class="avatar-gradient-border"></div>
                        <el-avatar :size="80" :src="currentUser.avatar" class="avatar-inner" />
                        <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-green-500 border-4 border-white dark:border-slate-800 rounded-full"></div>
                      </div>
                      <div class="flex-1 space-y-2">
                        <h4 class="text-2xl font-bold text-slate-800 dark:text-slate-100">{{ currentUser.nickname || currentUser.username }}</h4>
                        <div class="flex flex-wrap gap-4 text-sm text-slate-500">
                          <span class="flex items-center gap-1"><el-icon><User /></el-icon> UID: {{ currentUser.id }}</span>
                          <span class="flex items-center gap-1"><el-icon><Message /></el-icon> {{ currentUser.email || '未绑定邮箱' }}</span>
                        </div>
                        <div class="pt-2 flex gap-3">
                          <el-button type="primary" size="default" round @click="handleEditProfile">
                            <el-icon class="mr-1"><Edit /></el-icon>编辑资料
                          </el-button>
                          <el-button plain size="default" round @click="showChangePassword = true">
                            <el-icon class="mr-1"><Lock /></el-icon>修改密码
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div class="p-4 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/30 dark:bg-slate-800/30">
                    <p class="text-xs text-slate-400 uppercase tracking-wider font-bold mb-1">登录保护</p>
                    <div class="flex items-center justify-between">
                      <span class="text-sm font-medium">双重身份验证</span>
                      <el-tag size="small" type="info">未开启</el-tag>
                    </div>
                  </div>
                  <div class="p-4 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/30 dark:bg-slate-800/30">
                    <p class="text-xs text-slate-400 uppercase tracking-wider font-bold mb-1">账号注销</p>
                    <div class="flex items-center justify-between">
                      <span class="text-sm font-medium">申请永久注销</span>
                      <el-button link type="danger" size="small">立即申请</el-button>
                    </div>
                  </div>
                </div>
              </section>

              <!-- 通知设置 -->
              <section v-else-if="activeMenu === 'notification'" class="space-y-6">
                <div class="setting-group-card">
                  <h4 class="group-title">消息提醒</h4>
                  <div class="setting-item">
                    <div class="item-info">
                      <p class="item-label">新消息桌面通知</p>
                      <p class="item-desc">当收到新消息时，在屏幕边缘显示横幅通知</p>
                    </div>
                    <el-switch v-model="localSettings.notifications.enabled" @change="handleNotificationSettingChange" />
                  </div>
                  <div class="setting-item border-t border-slate-50 dark:border-slate-800/50">
                    <div class="item-info">
                      <p class="item-label">声音提醒</p>
                      <p class="item-desc">播放新消息提示音，帮助您及时关注消息</p>
                    </div>
                    <div class="flex items-center gap-3">
                      <el-button v-if="localSettings.notifications.sound" link type="primary" size="small" @click="testSound">
                        <el-icon class="mr-1"><VideoPlay /></el-icon>测试
                      </el-button>
                      <el-switch v-model="localSettings.notifications.sound" @change="handleNotificationSettingChange" />
                    </div>
                  </div>
                </div>

                <div class="setting-group-card">
                  <h4 class="group-title">快捷键</h4>
                  <div class="setting-item">
                    <div class="item-info">
                      <p class="item-label">发送消息</p>
                      <p class="item-desc">设置发送消息的组合键</p>
                    </div>
                    <el-select v-model="localSettings.shortcuts.send" size="default" style="width: 140px" @change="handleShortcutSettingChange">
                      <el-option label="Enter" value="enter" />
                      <el-option label="Ctrl + Enter" value="ctrl-enter" />
                    </el-select>
                  </div>
                  <div class="setting-item border-t border-slate-50 dark:border-slate-800/50">
                    <div class="item-info">
                      <p class="item-label">截图工具</p>
                      <p class="item-desc">全局唤起截图功能的快捷键</p>
                    </div>
                    <kbd class="kbd-key">Alt + A</kbd>
                  </div>
                </div>
              </section>

              <!-- 通用设置 -->
              <section v-else-if="activeMenu === 'general'" class="space-y-6">
                <div class="setting-group-card">
                  <h4 class="group-title">外观外观</h4>
                  <div class="setting-item">
                    <div class="item-info">
                      <p class="item-label">深色模式</p>
                      <p class="item-desc">选择您喜欢的外观主题</p>
                    </div>
                    <el-radio-group v-model="localSettings.general.theme" size="default" class="custom-radio-group" @change="handleGeneralSettingChange">
                      <el-radio-button label="light">浅色</el-radio-button>
                      <el-radio-button label="dark">深色</el-radio-button>
                      <el-radio-button label="auto">跟随系统</el-radio-button>
                    </el-radio-group>
                  </div>
                </div>

                <div class="setting-group-card">
                  <h4 class="group-title">语言与区域</h4>
                  <div class="setting-item">
                    <div class="item-info">
                      <p class="item-label">界面语言</p>
                      <p class="item-desc">切换 IM 系统的显示语言</p>
                    </div>
                    <el-select v-model="localSettings.general.language" style="width: 140px" @change="handleGeneralSettingChange">
                      <el-option label="简体中文" value="zh-CN" />
                      <el-option label="English" value="en-US" />
                    </el-select>
                  </div>
                </div>
              </section>

              <!-- 存储与数据 -->
              <section v-else-if="activeMenu === 'storage'" class="space-y-6">
                <div class="p-6 rounded-2xl bg-slate-50 dark:bg-slate-800 border border-slate-100 dark:border-slate-700 flex items-center justify-between">
                  <div class="space-y-1">
                    <h5 class="text-sm font-bold text-slate-800 dark:text-slate-100">本地存储占用</h5>
                    <p class="text-2xl font-black text-primary">{{ cacheSize }}</p>
                    <p class="text-xs text-slate-400">包含聊天图片、文件缓存和系统配置</p>
                  </div>
                  <el-button type="danger" round @click="handleClearCache">
                    <el-icon class="mr-1"><Delete /></el-icon>清理缓存
                  </el-button>
                </div>

                <div class="setting-group-card">
                  <h4 class="group-title">数据管理</h4>
                  <div class="setting-item">
                    <div class="item-info">
                      <p class="item-label">导出聊天记录</p>
                      <p class="item-desc">将所有本地聊天记录备份为 JSON 文件</p>
                    </div>
                    <el-button type="primary" plain round size="small" @click="handleExportChat">
                      <el-icon class="mr-1"><Download /></el-icon>立即导出
                    </el-button>
                  </div>
                  <div class="setting-item border-t border-slate-50 dark:border-slate-800/50">
                    <div class="item-info">
                      <p class="item-label">退出时保留数据</p>
                      <p class="item-desc">关闭此项将在退出登录后彻底清除所有本地记录</p>
                    </div>
                    <el-switch v-model="localSettings.data.keepOnLogout" @change="handleDataSettingChange" />
                  </div>
                </div>
              </section>

              <!-- 帮助与反馈 (简化) -->
              <section v-else-if="activeMenu === 'help'" class="space-y-6">
                <el-tabs v-model="helpActiveTab" class="modern-tabs">
                  <el-tab-pane label="常见问题" name="faq">
                    <div class="space-y-3 mt-4">
                      <div v-for="(faq, index) in filteredFaqs" :key="index" class="faq-card">
                        <div class="p-4 cursor-pointer flex items-center justify-between" @click="faq.expanded = !faq.expanded">
                          <span class="font-medium text-slate-700 dark:text-slate-200">{{ faq.question }}</span>
                          <el-icon :class="{'rotate-90': faq.expanded}" class="transition-transform"><ArrowRight /></el-icon>
                        </div>
                        <transition name="expand">
                          <div v-if="faq.expanded" class="px-4 pb-4 text-sm text-slate-500 leading-relaxed border-t border-slate-50 dark:border-slate-800/50 pt-3">
                            {{ faq.answer }}
                          </div>
                        </transition>
                      </div>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="意见反馈" name="feedback">
                    <div class="mt-4 bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-100 dark:border-slate-800">
                      <el-form :model="feedbackForm" label-position="top">
                        <el-form-item label="问题类型">
                          <el-radio-group v-model="feedbackForm.type" size="small">
                            <el-radio-button label="feature">功能建议</el-radio-button>
                            <el-radio-button label="bug">缺陷报告</el-radio-button>
                            <el-radio-button label="other">其他</el-radio-button>
                          </el-radio-group>
                        </el-form-item>
                        <el-form-item label="详细描述">
                          <el-input v-model="feedbackForm.description" type="textarea" :rows="4" placeholder="请描述您遇到的问题或想法..." />
                        </el-form-item>
                        <el-button type="primary" class="w-full" :loading="feedbackSubmitting" @click="handleSubmitFeedback">提交反馈</el-button>
                      </el-form>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </section>

              <!-- 关于应用 -->
              <section v-else-if="activeMenu === 'about'" class="text-center py-8">
                <div class="mb-6 flex flex-col items-center">
                  <div class="w-20 h-20 bg-primary rounded-2xl flex items-center justify-center text-white text-3xl font-black shadow-2xl shadow-primary/30 mb-4 animate-bounce-slow">
                    IM
                  </div>
                  <h4 class="text-xl font-black text-slate-800 dark:text-slate-100">RuoYi IM Desktop</h4>
                  <p class="text-sm text-slate-400">版本 v4.1.0-stable (Build 20250125)</p>
                </div>
                
                <div class="max-w-xs mx-auto space-y-3">
                  <el-button class="w-full" type="primary" round @click="checkUpdate">检查更新</el-button>
                  <div class="flex justify-center gap-4 text-xs text-primary font-medium">
                    <a href="#" class="hover:underline">服务条款</a>
                    <span class="text-slate-300">|</span>
                    <a href="#" class="hover:underline">隐私政策</a>
                  </div>
                  <p class="text-[10px] text-slate-400 mt-6">© 2025 RuoYi-IM Team. All rights reserved.</p>
                </div>
              </section>

            </div>
          </transition>
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
import { 
  VideoPlay, Lock, Edit, Bell, Camera, Position, View, Document, Sunny, Moon, 
  Monitor, Refresh, Delete, Download, Key, Promotion, Search, ArrowRight, 
  Close, User, Message 
} from '@element-plus/icons-vue'
import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'
import EditProfileDialog from '@/components/Common/EditProfileDialog.vue'

const props = defineProps({
  modelValue: Boolean,
  defaultMenu: {
    type: String,
    default: 'account'
  }
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const visible = ref(false)
const showChangePassword = ref(false)
const showEditProfile = ref(false)
const activeMenu = ref('account')
const searchQuery = ref('')
const { isDark, themeMode, setThemeMode } = useTheme()

const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const isFullscreen = computed(() => windowWidth.value < 640)

const dialogWidth = computed(() => {
  if (windowWidth.value < 768) return '95%'
  if (windowWidth.value < 1024) return '820px'
  return '960px'
})

const handleResize = () => { windowWidth.value = window.innerWidth }
onMounted(() => { window.addEventListener('resize', handleResize); calculateCacheSize() })
onUnmounted(() => { window.removeEventListener('resize', handleResize) })

const menuItems = [
  { id: 'account', label: '账号安全', icon: 'manage_accounts' },
  { id: 'notification', label: '通知提醒', icon: 'notifications' },
  { id: 'general', label: '外观通用', icon: 'settings' },
  { id: 'storage', label: '存储数据', icon: 'storage' },
  { id: 'help', label: '帮助反馈', icon: 'help_outline' },
  { id: 'about', label: '关于应用', icon: 'info' }
]

const filteredMenuItems = computed(() => {
  if (!searchQuery.value) return menuItems
  const q = searchQuery.value.toLowerCase()
  return menuItems.filter(i => i.label.toLowerCase().includes(q))
})

const currentMenuLabel = computed(() => menuItems.find(i => i.id === activeMenu.value)?.label || '')

const currentUser = computed(() => store.getters['user/currentUser'] || { status: 'online' })
const settings = computed(() => store.state.im.settings)
const localSettings = reactive(JSON.parse(JSON.stringify(settings.value)))

// 初始化默认值
if (!localSettings.chat) localSettings.chat = { fontSize: 'medium', background: 'default', bubbleStyle: 'default', sendShortcut: 'enter' }
if (!localSettings.file) localSettings.file = { autoDownloadImage: true, autoDownloadFile: false, sizeWarning: true }
if (!localSettings.data) localSettings.data = { keepOnLogout: true }

watch(localSettings, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(settings.value)) {
    store.commit('im/UPDATE_SETTINGS', JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })

watch(() => localSettings.general.theme, (val) => {
  if (val !== themeMode.value) setThemeMode(val)
}, { immediate: true })

const testSound = () => ElMessage.success('提示音效正常')
const handleEditProfile = () => { showEditProfile.value = true }
const checkUpdate = () => ElMessage.success('当前已是最新版本')
const cacheSize = ref('0 MB')

const calculateCacheSize = () => {
  let total = 0
  for (let key in localStorage) { if (localStorage.hasOwnProperty(key)) total += localStorage[key].length + key.length }
  cacheSize.value = `${(total / 1024 / 1024).toFixed(2)} MB`
}

const handleNotificationSettingChange = () => store.dispatch('im/updateNotificationSettings', localSettings.notifications)
const handleGeneralSettingChange = () => store.dispatch('im/updateGeneralSettings', localSettings.general)
const handleShortcutSettingChange = () => store.dispatch('im/updateShortcutSettings', localSettings.shortcuts)
const handleDataSettingChange = () => store.dispatch('im/updateDataSettings', localSettings.data)

const handleClearCache = () => {
  ElMessageBox.confirm('清理缓存将释放本地空间，但图片和文件需要重新下载。是否继续？', '清理缓存', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning', roundButton: true
  }).then(() => {
    const keep = ['im-system-settings', 'token', 'user-info']
    Object.keys(localStorage).forEach(key => { if (!keep.includes(key)) localStorage.removeItem(key) })
    calculateCacheSize()
    ElMessage.success('缓存清理成功')
  })
}

const handleExportChat = () => {
  const dataStr = JSON.stringify(store.state.im.message.messages || [], null, 2)
  const blob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `im-backup-${new Date().getTime()}.json`
  a.click()
  ElMessage.success('备份已导出')
}

const handleProfileUpdate = () => store.dispatch('user/getInfo')
const handleShowProfile = () => { showEditProfile.value = true }

// 帮助相关
const helpActiveTab = ref('faq')
const feedbackSubmitting = ref(false)
const feedbackForm = reactive({ type: 'feature', description: '', contact: '' })
const faqs = reactive([
  { question: '如何修改登录密码？', answer: '请在"账号安全"板块点击"修改密码"按钮进行设置。', expanded: false },
  { question: '如何设置消息免打扰？', answer: '在主界面会话列表中右键点击对应好友或群组，选择"消息免打扰"即可。', expanded: false },
  { question: '本地记录可以保存多久？', answer: '默认永久保存，除非您在"存储数据"中关闭了"退出保留数据"或手动清理了缓存。', expanded: false }
])

const filteredFaqs = computed(() => faqs) // 简化处理

const handleSubmitFeedback = async () => {
  if (!feedbackForm.description.trim()) return ElMessage.warning('请输入描述内容')
  feedbackSubmitting.value = true
  await new Promise(r => setTimeout(r, 800))
  ElMessage.success('提交成功')
  feedbackForm.description = ''
  feedbackSubmitting.value = false
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.defaultMenu) activeMenu.value = props.defaultMenu
})
watch(visible, (val) => { if (!val) emit('update:modelValue', false) })
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

// ============================================================================
// 对话框容器
// ============================================================================
.system-settings-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 60px -12px rgba(0, 0, 0, 0.2);
    background: transparent;
  }

  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) {
    padding: 0;
    height: 640px;
    background: transparent;
  }

  &.is-mobile :deep(.el-dialog__body) { height: 100vh; }
}

// ============================================================================
// 主容器
// ============================================================================
.settings-wrapper {
  display: flex;
  height: 100%;
  overflow: hidden;
  background: var(--dt-bg-card);
  border-radius: 16px;
}

// ============================================================================
// 侧边导航栏 (200px 宽度)
// ============================================================================
.settings-sidebar {
  width: 200px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-right: 1px solid var(--dt-border-light);

  .dark & {
    background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
    border-right-color: var(--dt-border-dark);
  }
}

// 侧边栏顶部
.sidebar-header {
  padding: 20px 16px 12px;

  .settings-logo {
    width: 48px;
    height: 48px;
    margin: 0 auto;
    background: var(--dt-brand-color);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
    transition: transform 0.2s;

    &:hover {
      transform: scale(1.05);
    }

    .material-icons-outlined {
      font-size: 24px;
    }
  }
}

// 导航菜单
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 2px;
  }

  .nav-item-group {
    margin-bottom: 2px;
  }

  .nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    color: var(--dt-text-secondary);
    position: relative;

    .nav-icon {
      font-size: 20px;
      flex-shrink: 0;
    }

    .nav-label {
      font-size: 14px;
      font-weight: 500;
      flex: 1;
    }

    .nav-indicator {
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background: var(--dt-brand-color);
      border-radius: 0 3px 3px 0;
    }

    &:hover {
      background: rgba(22, 119, 255, 0.08);
      color: var(--dt-text-primary);
    }

    &.active {
      background: var(--dt-brand-color);
      color: #fff;
      box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
    }
  }
}

// 底部用户卡片
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--dt-border-light);

  .dark & {
    border-top-color: var(--dt-border-dark);
  }

  .user-mini-card {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: rgba(22, 119, 255, 0.08);
    }

    .user-mini-info {
      flex: 1;
      min-width: 0;

      .user-mini-name {
        font-size: 13px;
        font-weight: 600;
        color: var(--dt-text-primary);
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

// ============================================================================
// 右侧内容区
// ============================================================================
.settings-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--dt-bg-body);
}

// 顶部标题栏
.main-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);
  flex-shrink: 0;

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;

    .header-search {
      width: 200px;

      :deep(.el-input__wrapper) {
        border-radius: 8px;
        background: var(--dt-bg-card);
        border-color: var(--dt-border-light);
        transition: all 0.2s;

        &:hover {
          border-color: var(--dt-brand-color);
        }
      }
    }

    .close-btn {
      width: 32px;
      height: 32px;
      border: none;
      background: var(--dt-bg-card);
      color: var(--dt-text-secondary);
      transition: all 0.2s;

      &:hover {
        background: var(--dt-bg-hover);
        color: var(--dt-text-primary);
      }
    }
  }
}

// 内容区域
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;

    &:hover {
      background: var(--dt-text-tertiary);
    }
  }
}

.content-container {
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: 40px;
}

// ============================================================================
// 设置卡片样式（飞书风格增强版）
// ============================================================================
.setting-group-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: box-shadow 0.2s;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  }

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06), 0 2px 4px rgba(0, 0, 0, 0.04);
  }

  .group-title {
    padding: 14px 20px;
    background: linear-gradient(to bottom, var(--dt-bg-hover), var(--dt-bg-card));
    font-size: 12px;
    font-weight: 600;
    color: var(--dt-text-tertiary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
    border-bottom: 1px solid var(--dt-border-light);

    .dark & {
      background: linear-gradient(to bottom, var(--dt-bg-active-dark), var(--dt-bg-card-dark));
      border-bottom-color: var(--dt-border-dark);
    }
  }
}

.setting-item {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background 0.2s;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--dt-bg-hover);
  }

  .item-label {
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }

  .item-desc {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin-top: 2px;
  }
}

// 快捷键显示
.kbd-key {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-color);
  font-size: 12px;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-weight: 600;
  color: var(--dt-text-secondary);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

// FAQ 卡片
.faq-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  transition: all 0.2s;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 2px 8px rgba(22, 119, 255, 0.1);
  }
}

// 单选按钮组
.custom-radio-group {
  :deep(.el-radio-button__inner) {
    border: none;
    background: var(--dt-bg-hover);
    color: var(--dt-text-secondary);
    padding: 8px 16px;
    font-size: 13px;
    font-weight: 600;
    border-radius: 8px;
    margin-left: 4px;

    &:hover {
      color: var(--dt-brand-color);
    }
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: var(--dt-brand-color);
    color: #fff;
    box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
  }
}

// 现代化标签页
.modern-tabs {
  :deep(.el-tabs__nav-wrap::after) { display: none; }
  :deep(.el-tabs__active-bar) { height: 3px; border-radius: 2px; }
  :deep(.el-tabs__item) {
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-tertiary);
    transition: color 0.2s;
    &.is-active { color: var(--dt-brand-color); }
  }
}

// 慢弹跳动画
.animate-bounce-slow {
  animation: bounce-slow 3s infinite;
}
@keyframes bounce-slow {
  0%, 100% { transform: translateY(-5%); animation-timing-function: cubic-bezier(0.8, 0, 1, 1); }
  50% { transform: translateY(0); animation-timing-function: cubic-bezier(0, 0, 0.2, 1); }
}

// ============================================================================
// 过渡动画
// ============================================================================
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.2s ease-out;
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}
.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 300px;
}

// ============================================================================
// 头像渐变旋转边框
// ============================================================================
.avatar-gradient-wrapper {
  position: relative;
  width: 88px;
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-gradient-border {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, #1677ff, #4096ff, #52c41a, #1677ff);
  animation: rotate-border 3s linear infinite;
  z-index: 0;
}

.avatar-inner {
  position: relative;
  z-index: 1;
  background: var(--dt-bg-card);
  border-radius: 50%;
  animation: counter-rotate 3s linear infinite;
}

@keyframes rotate-border {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes counter-rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(-360deg);
  }
}

// ============================================================================
// 滚动条样式
// ============================================================================
.scrollbar-thin::-webkit-scrollbar {
  width: 4px;
}
.scrollbar-thin::-webkit-scrollbar-thumb {
  background: var(--dt-border-color);
  border-radius: 2px;
}
.scrollbar-thin::-webkit-scrollbar-thumb:hover {
  background: var(--dt-text-tertiary);
}
</style>