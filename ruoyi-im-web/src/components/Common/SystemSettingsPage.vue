<template>
  <Transition name="dt-settings-slide">
    <div
      v-if="visible"
      class="dt-settings-page"
      role="dialog"
      aria-modal="true"
      :aria-label="title"
    >
      <!-- 顶部导航：返回 + 居中标题（对齐 App 全屏 push 页） -->
      <div class="dt-settings-nav">
        <button
          type="button"
          class="dt-settings-back"
          aria-label="返回"
          @click="goBack"
        >
          <el-icon :size="22">
            <ArrowLeft />
          </el-icon>
        </button>
        <span class="dt-settings-title">{{ title }}</span>
      </div>

      <div class="dt-settings-body scrollbar-thin">
        <div class="dt-settings-container">
          <!-- ================= 设置首页 ================= -->
          <template v-if="!activeMenu">
            <div class="dt-set-card">
              <button
                v-for="item in entryItems"
                :key="item.key"
                type="button"
                class="dt-set-row dt-set-row--click"
                @click="activeMenu = item.key"
              >
                <span
                  class="dt-set-ic"
                  :class="`dt-set-ic--${item.tone}`"
                >
                  <el-icon :size="18">
                    <component :is="item.icon" />
                  </el-icon>
                </span>
                <span class="dt-set-main">
                  <span class="dt-set-label">{{ item.label }}</span>
                </span>
                <span
                  v-if="item.value"
                  class="dt-set-value"
                >{{ item.value }}</span>
                <span class="dt-set-arrow">›</span>
              </button>
            </div>

            <div class="dt-set-gap" />

            <button
              type="button"
              class="dt-set-logout"
              @click="handleLogout"
            >
              退出登录
            </button>
            <p class="dt-set-note">
              心飞IM v8.2.0<br>
              © 2026 心飞科技有限公司
            </p>
          </template>

          <!-- ================= 个人资料 ================= -->
          <template v-else-if="activeMenu === 'profile'">
            <div class="dt-set-card dt-set-profile">
              <DingtalkAvatar
                :src="currentUser.avatar"
                :name="displayName"
                :user-id="currentUser.id"
                :size="56"
              />
              <div class="dt-set-profile-info">
                <div class="dt-set-name">
                  {{ displayName }}
                </div>
                <div class="dt-set-sub">
                  {{ departmentName }}
                </div>
              </div>
              <button
                type="button"
                class="dt-set-edit"
                @click="handleEditProfile"
              >
                编辑资料
              </button>
            </div>

            <div class="dt-set-sec">
              账号信息
            </div>
            <div class="dt-set-card">
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">昵称</span>
                </span>
                <span class="dt-set-value">{{ displayName }}</span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">工号</span>
                </span>
                <span class="dt-set-value">{{ currentUser.userNo || currentUser.jobNumber || '未设置' }}</span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">部门</span>
                </span>
                <span class="dt-set-value">{{ departmentName }}</span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">邮箱</span>
                </span>
                <span class="dt-set-value">{{ currentUser.email || '未设置' }}</span>
              </div>
            </div>

            <div class="dt-set-gap" />
            <button
              type="button"
              class="dt-set-logout"
              @click="handleLogout"
            >
              退出登录
            </button>
          </template>

          <!-- ================= 账号与安全 ================= -->
          <template v-else-if="activeMenu === 'account'">
            <div class="dt-set-card">
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">修改密码</span>
                  <span class="dt-set-desc">定期更换密码可提升账号安全性</span>
                </span>
                <span class="dt-set-arrow">›</span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">屏蔽列表</span>
                  <span class="dt-set-desc">已屏蔽的账号不会再收到其消息</span>
                </span>
                <span class="dt-set-value">{{ blockedCount }} 人</span>
                <span class="dt-set-arrow">›</span>
              </div>
            </div>

            <div class="dt-set-sec">
              隐私
            </div>
            <div class="dt-set-card">
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">展示在线状态</span>
                  <span class="dt-set-desc">关闭后他人看不到你的在线状态</span>
                </span>
                <button
                  type="button"
                  class="dt-switch"
                  :class="{ on: local.privacy.showStatus }"
                  role="switch"
                  :aria-checked="local.privacy.showStatus"
                  aria-label="展示在线状态"
                  @click="local.privacy.showStatus = !local.privacy.showStatus"
                />
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">接收已读回执</span>
                  <span class="dt-set-desc">开启后对方可看到你已读消息</span>
                </span>
                <button
                  type="button"
                  class="dt-switch"
                  :class="{ on: local.privacy.readReceipt }"
                  role="switch"
                  :aria-checked="local.privacy.readReceipt"
                  aria-label="接收已读回执"
                  @click="local.privacy.readReceipt = !local.privacy.readReceipt"
                />
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">禁止陌生人私信</span>
                  <span class="dt-set-desc">开启后仅通讯录联系人可发起会话</span>
                </span>
                <button
                  type="button"
                  class="dt-switch"
                  :class="{ on: local.privacy.blockStranger }"
                  role="switch"
                  :aria-checked="local.privacy.blockStranger"
                  aria-label="禁止陌生人私信"
                  @click="local.privacy.blockStranger = !local.privacy.blockStranger"
                />
              </div>
            </div>
          </template>

          <!-- ================= 消息通知 ================= -->
          <template v-else-if="activeMenu === 'notify'">
            <div class="dt-set-card">
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">新消息通知</span>
                  <span class="dt-set-desc">开启后收到系统通知横幅和声音</span>
                </span>
                <button
                  type="button"
                  class="dt-switch"
                  :class="{ on: local.notifications.enabled }"
                  role="switch"
                  :aria-checked="local.notifications.enabled"
                  aria-label="新消息通知"
                  @click="local.notifications.enabled = !local.notifications.enabled"
                />
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">通知显示消息详情</span>
                  <span class="dt-set-desc">在通知横幅中显示消息内容预览</span>
                </span>
                <button
                  type="button"
                  class="dt-switch"
                  :class="{ on: local.notifications.preview }"
                  role="switch"
                  :aria-checked="local.notifications.preview"
                  aria-label="通知显示消息详情"
                  @click="local.notifications.preview = !local.notifications.preview"
                />
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">声音</span>
                  <span class="dt-set-desc">收到消息时播放提示音</span>
                </span>
                <button
                  type="button"
                  class="dt-switch"
                  :class="{ on: local.notifications.sound }"
                  role="switch"
                  :aria-checked="local.notifications.sound"
                  aria-label="声音"
                  @click="local.notifications.sound = !local.notifications.sound"
                />
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">免打扰时段</span>
                  <span class="dt-set-desc">时段内仅静音，未读角标仍然保留</span>
                </span>
                <span class="dt-set-value">{{ local.notifications.dnd }}</span>
                <span class="dt-set-arrow">›</span>
              </div>
            </div>
          </template>

          <!-- ================= 主题与外观 ================= -->
          <template v-else-if="activeMenu === 'appearance'">
            <div class="dt-set-sec">
              主题色
            </div>
            <div class="dt-set-card">
              <div class="dt-theme-dots">
                <button
                  v-for="theme in themeOptions"
                  :key="theme.key"
                  type="button"
                  class="dt-theme-dot"
                  :class="[`dt-theme-dot--${theme.key}`, { active: local.general.themeColor === theme.key }]"
                  :aria-label="theme.label"
                  :title="theme.label"
                  @click="local.general.themeColor = theme.key"
                />
              </div>
            </div>

            <div class="dt-set-sec">
              外观
            </div>
            <div class="dt-set-card">
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">外观模式</span>
                </span>
                <span class="dt-chips">
                  <button
                    v-for="opt in themeModeOptions"
                    :key="opt.value"
                    type="button"
                    class="dt-chip"
                    :class="{ active: local.general.theme === opt.value }"
                    @click="local.general.theme = opt.value"
                  >{{ opt.label }}</button>
                </span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">会话列表密度</span>
                </span>
                <span class="dt-chips">
                  <button
                    v-for="opt in densityOptions"
                    :key="opt.value"
                    type="button"
                    class="dt-chip"
                    :class="{ active: local.general.density === opt.value }"
                    @click="local.general.density = opt.value"
                  >{{ opt.label }}</button>
                </span>
              </div>
            </div>
          </template>

          <!-- ================= 通用设置 ================= -->
          <template v-else-if="activeMenu === 'general'">
            <div class="dt-set-card">
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">多语言</span>
                </span>
                <span class="dt-chips">
                  <button
                    v-for="opt in languageOptions"
                    :key="opt.value"
                    type="button"
                    class="dt-chip"
                    :class="{ active: local.general.language === opt.value }"
                    @click="local.general.language = opt.value"
                  >{{ opt.label }}</button>
                </span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">发送消息</span>
                </span>
                <span class="dt-chips">
                  <button
                    v-for="opt in sendOptions"
                    :key="opt.value"
                    type="button"
                    class="dt-chip"
                    :class="{ active: local.shortcuts.send === opt.value }"
                    @click="local.shortcuts.send = opt.value"
                  >{{ opt.label }}</button>
                </span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">自动检查更新</span>
                </span>
                <button
                  type="button"
                  class="dt-switch"
                  :class="{ on: local.general.autoUpdate }"
                  role="switch"
                  :aria-checked="local.general.autoUpdate"
                  aria-label="自动检查更新"
                  @click="local.general.autoUpdate = !local.general.autoUpdate"
                />
              </div>
              <button
                type="button"
                class="dt-set-row dt-set-row--click"
                @click="handleClearCache"
              >
                <span class="dt-set-main">
                  <span class="dt-set-label">清空本地设置</span>
                  <span class="dt-set-desc">恢复默认设置并刷新页面</span>
                </span>
                <span class="dt-set-arrow">›</span>
              </button>
            </div>
          </template>

          <!-- ================= 帮助与反馈 ================= -->
          <template v-else-if="activeMenu === 'help'">
            <div class="dt-set-card">
              <button
                type="button"
                class="dt-set-row dt-set-row--click"
                @click="handleFaq"
              >
                <span class="dt-set-main">
                  <span class="dt-set-label">常见问题</span>
                </span>
                <span class="dt-set-arrow">›</span>
              </button>
              <button
                type="button"
                class="dt-set-row dt-set-row--click"
                @click="handleFeedback"
              >
                <span class="dt-set-main">
                  <span class="dt-set-label">意见反馈</span>
                </span>
                <span class="dt-set-arrow">›</span>
              </button>
              <button
                type="button"
                class="dt-set-row dt-set-row--click"
                @click="handleCopyEmail"
              >
                <span class="dt-set-main">
                  <span class="dt-set-label">联系客服</span>
                </span>
                <span class="dt-set-value">{{ supportEmail }}</span>
                <span class="dt-set-arrow">›</span>
              </button>
            </div>
            <p class="dt-set-note">
              如有问题也可发送邮件至 {{ supportEmail }}
            </p>
          </template>

          <!-- ================= 关于 ================= -->
          <template v-else-if="activeMenu === 'about'">
            <div class="dt-set-card">
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">产品版本</span>
                </span>
                <span class="dt-set-value">{{ appVersion }}</span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">开发商</span>
                </span>
                <span class="dt-set-value">心飞科技</span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">官方网站</span>
                </span>
                <span class="dt-set-value">xinfei.com</span>
              </div>
              <div class="dt-set-row">
                <span class="dt-set-main">
                  <span class="dt-set-label">用户协议与隐私政策</span>
                </span>
                <span class="dt-set-arrow">›</span>
              </div>
            </div>
            <p class="dt-set-note">
              心飞IM v8.2.0<br>
              © 2026 心飞科技有限公司
            </p>
          </template>
        </div>
      </div>

      <ChangePasswordDialog v-model="showChangePassword" />
    </div>
  </Transition>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Lock,
  Bell,
  Sunny,
  Setting,
  QuestionFilled,
  InfoFilled,
  ArrowLeft
} from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'
import {
  getGeneralSettings,
  updateGeneralSettings,
  getNotificationSettings,
  updateNotificationSettings,
  getPrivacySettings,
  updatePrivacySettings,
  getShortcutSettings,
  updateShortcutSettings,
  getBlockedUsers
} from '@/api/im/config'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'

const APP_NAME = '心飞IM'
const APP_VERSION = 'v8.2.0'
const SUPPORT_EMAIL = 'support@xinfei.com'

const appVersion = APP_VERSION
const supportEmail = SUPPORT_EMAIL

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'open-edit-profile'])

const store = useStore()
const { themeMode, setThemeMode } = useTheme()

const visible = ref(false)
const showChangePassword = ref(false)
const activeMenu = ref('')
const loading = ref(false)
const blockedCount = ref(0)

const currentUser = computed(() => store.getters['user/currentUser'] || {})
const settings = computed(() => store.state.im.settings)
const local = reactive(JSON.parse(JSON.stringify(settings.value)))

/* ---------------- 导航 ---------------- */

const themeOptions = [
  { key: 'blue', label: '品牌蓝' },
  { key: 'orange', label: '活力橙' },
  { key: 'green', label: '安心绿' },
  { key: 'red', label: '绛红' },
  { key: 'purple', label: '烟雨紫' }
]

const themeModeOptions = [
  { value: 'light', label: '浅色' },
  { value: 'dark', label: '深色' },
  { value: 'auto', label: '跟随系统' }
]

const densityOptions = [
  { value: 'compact', label: '紧凑' },
  { value: 'standard', label: '标准' },
  { value: 'loose', label: '宽松' }
]

const languageOptions = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en-US', label: 'English' }
]

const sendOptions = [
  { value: 'enter', label: 'Enter' },
  { value: 'ctrl-enter', label: 'Ctrl + Enter' }
]

const themeName = computed(() => {
  const hit = themeOptions.find(item => item.key === local.general.themeColor)
  return hit ? hit.label : '品牌蓝'
})

const entryItems = computed(() => [
  { key: 'profile', label: '个人资料', icon: User, tone: 'blue' },
  { key: 'account', label: '账号与安全', icon: Lock, tone: 'green' },
  { key: 'notify', label: '消息通知', icon: Bell, tone: 'orange', value: local.notifications.enabled ? '已开启' : '已关闭' },
  { key: 'appearance', label: '主题与外观', icon: Sunny, tone: 'purple', value: themeName.value },
  { key: 'general', label: '通用设置', icon: Setting, tone: 'gray' },
  { key: 'help', label: '帮助与反馈', icon: QuestionFilled, tone: 'blue' },
  { key: 'about', label: '关于 心飞IM', icon: InfoFilled, tone: 'gray', value: APP_VERSION }
])

const title = computed(() => {
  if (!activeMenu.value) return '设置'
  const hit = entryItems.value.find(item => item.key === activeMenu.value)
  return hit ? hit.label : '设置'
})

const goBack = () => {
  if (activeMenu.value) {
    activeMenu.value = ''
    return
  }
  close()
}

/* ---------------- 用户信息 ---------------- */

const displayName = computed(() => {
  const user = currentUser.value
  return user.nickName || user.nickname || user.userName || user.username || user.name || '用户'
})

const departmentName = computed(() => {
  const user = currentUser.value
  const dept = user.dept
  if (dept && typeof dept === 'object') return dept.deptName || dept.name || '未设置'
  return user.deptName || user.department || '未设置'
})

/* ---------------- 主题 ---------------- */

const applyThemeColor = (key) => {
  const root = document.documentElement
  if (themeOptions.some(item => item.key === key)) {
    root.setAttribute('data-dt-theme', key)
  } else {
    root.removeAttribute('data-dt-theme')
  }
}

watch(() => local.general.themeColor, applyThemeColor, { immediate: true })

/* ---------------- 会话列表密度 ---------------- */

const DENSITY_KEYS = ['compact', 'standard', 'loose']

const applyDensity = (val) => {
  const root = document.documentElement
  if (DENSITY_KEYS.includes(val)) {
    root.setAttribute('data-dt-density', val)
  } else {
    root.removeAttribute('data-dt-density')
  }
}

watch(() => local.general.density, applyDensity, { immediate: true })

watch(() => local.general.theme, (val) => {
  if (val !== themeMode.value) setThemeMode(val)
}, { immediate: true })

/* ---------------- 持久化 ---------------- */

let saveTimer = null

const persist = () => {
  if (loading.value) return
  store.commit('im/UPDATE_SETTINGS', JSON.parse(JSON.stringify(local)))
  clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    Promise.allSettled([
      updateGeneralSettings(local.general),
      updateNotificationSettings(local.notifications),
      updatePrivacySettings(local.privacy),
      updateShortcutSettings(local.shortcuts)
    ])
  }, 500)
}

watch(local, persist, { deep: true })

/* ---------------- 远程设置加载 ---------------- */

const applyRemote = (data, target) => {
  if (data && typeof data === 'object') Object.assign(target, data)
}

const loadSettings = async () => {
  loading.value = true
  try {
    const [generalRes, notifyRes, privacyRes, shortcutRes] = await Promise.allSettled([
      getGeneralSettings(),
      getNotificationSettings(),
      getPrivacySettings(),
      getShortcutSettings()
    ])
    if (generalRes.status === 'fulfilled' && generalRes.value?.code === 200) {
      applyRemote(generalRes.value.data, local.general)
    }
    if (notifyRes.status === 'fulfilled' && notifyRes.value?.code === 200) {
      applyRemote(notifyRes.value.data, local.notifications)
    }
    if (privacyRes.status === 'fulfilled' && privacyRes.value?.code === 200) {
      applyRemote(privacyRes.value.data, local.privacy)
    }
    if (shortcutRes.status === 'fulfilled' && shortcutRes.value?.code === 200) {
      applyRemote(shortcutRes.value.data, local.shortcuts)
    }
    store.commit('im/UPDATE_SETTINGS', JSON.parse(JSON.stringify(local)))
  } catch (error) {
    console.warn('加载设置失败', error)
  } finally {
    loading.value = false
  }
}

const loadBlockedCount = async () => {
  try {
    const res = await getBlockedUsers()
    if (res?.code === 200) blockedCount.value = (res.data || []).length
  } catch (error) {
    console.warn('加载屏蔽列表失败', error)
  }
}

onMounted(() => {
  // 组件 setup 可能早于 App.vue 的 LOAD_SETTINGS，这里确保本地配置已就绪
  loading.value = true
  store.commit('im/LOAD_SETTINGS')
  Object.assign(local, JSON.parse(JSON.stringify(settings.value)))
  loadSettings()
  loadBlockedCount()
})

/* ---------------- 操作 ---------------- */

const close = () => emit('update:modelValue', false)

const handleEditProfile = () => emit('open-edit-profile')

const handleFaq = () => ElMessage.info('常见问题（演示）')

const handleCopyEmail = async () => {
  try {
    await navigator.clipboard.writeText(SUPPORT_EMAIL)
    ElMessage.success(`已复制客服邮箱 ${SUPPORT_EMAIL}`)
  } catch (error) {
    ElMessage.info(SUPPORT_EMAIL)
  }
}

const handleFeedback = () => {
  const subject = encodeURIComponent(`${APP_NAME} 意见反馈`)
  window.location.href = `mailto:${SUPPORT_EMAIL}?subject=${subject}`
}

const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm('将恢复默认设置并刷新页面，确定继续吗？', '清空本地设置', {
      confirmButtonText: '清空并刷新',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (error) {
    return
  }
  localStorage.removeItem('im-system-settings')
  localStorage.removeItem('theme-mode')
  ElMessage.success('本地设置已清空，即将刷新')
  setTimeout(() => window.location.reload(), 600)
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('退出登录后需要重新输入账号密码，确定退出吗？', '退出登录', {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (error) {
    return
  }
  await store.dispatch('user/logout')
  close()
  window.location.reload()
}

const onKeydown = (e) => {
  if (e.key === 'Escape' && visible.value) goBack()
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (!val) activeMenu.value = ''
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})

watch(visible, (val) => {
  if (val) {
    window.addEventListener('keydown', onKeydown)
  } else {
    window.removeEventListener('keydown', onKeydown)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped lang="scss">
/* ============================================================================
   App 设置页（全屏 push 页，对齐 prototype/dingtalk-h5.html .h5-settings）
   ============================================================================ */

.dt-settings-page {
  position: fixed;
  inset: 0;
  z-index: var(--dt-z-modal);
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-body);
}

/* ---------- 顶部导航 ---------- */
.dt-settings-nav {
  position: relative;
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 8px;
  flex-shrink: 0;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
}

.dt-settings-back {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--dt-text-primary);
  cursor: pointer;

  &:active {
    background: var(--dt-bg-body);
  }
}

.dt-settings-title {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  line-height: 44px;
  text-align: center;
  color: var(--dt-text-primary);
  pointer-events: none;
}

/* ---------- 内容区 ---------- */
.dt-settings-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.dt-settings-container {
  max-width: 640px;
  margin: 0 auto;
  padding: 12px 12px 24px;
}

/* ---------- 分组标题 ---------- */
.dt-set-sec {
  padding: 12px 16px 8px;
  font-size: 12px;
  font-weight: 500;
  color: var(--dt-text-secondary);
}

/* ---------- 卡片 ---------- */
.dt-set-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
}

/* ---------- 行 ---------- */
.dt-set-row {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 48px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--dt-border-light);
  text-align: left;
  cursor: default;

  &:last-child {
    border-bottom: none;
  }
}

.dt-set-row--click {
  width: 100%;
  padding: 10px 16px;
  border: none;
  border-bottom: 1px solid var(--dt-border-light);
  background: transparent;
  font-family: var(--dt-font-family);
  cursor: pointer;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: var(--dt-bg-hover);
  }
}

.dt-set-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.dt-set-label {
  font-size: 14px;
  color: var(--dt-text-primary);
}

.dt-set-desc {
  margin-top: 2px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.dt-set-value {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--dt-text-tertiary);
}

.dt-set-arrow {
  flex-shrink: 0;
  font-size: 16px;
  color: var(--dt-text-quaternary);
}

/* ---------- 彩色图标块 ---------- */
.dt-set-ic {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-lg);
  flex-shrink: 0;
}

.dt-set-ic--blue {
  background: var(--dt-brand-light);
  color: var(--dt-brand-color);
}

.dt-set-ic--green {
  background: var(--dt-success-bg);
  color: var(--dt-success-color);
}

.dt-set-ic--orange {
  background: var(--dt-warning-bg);
  color: var(--dt-warning-color);
}

.dt-set-ic--purple {
  background: var(--dt-purple-bg);
  color: var(--dt-purple-color);
}

.dt-set-ic--gray {
  background: var(--dt-bg-body);
  color: var(--dt-text-secondary);
}

/* ---------- App 开关（44x26 胶囊） ---------- */
.dt-switch {
  position: relative;
  flex-shrink: 0;
  width: 44px;
  height: 26px;
  padding: 0;
  border: none;
  border-radius: 13px;
  background: var(--dt-border);
  cursor: pointer;
  transition: background 0.15s;

  &::after {
    content: '';
    position: absolute;
    top: 2px;
    left: 2px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: var(--dt-bg-card);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
    transition: transform 0.15s;
  }

  &.on {
    background: var(--dt-brand-color);
  }

  &.on::after {
    transform: translateX(18px);
  }
}

/* ---------- 胶囊选项组 ---------- */
.dt-chips {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.dt-chip {
  padding: 4px 10px;
  border: 1px solid var(--dt-border);
  border-radius: var(--dt-radius-full);
  background: transparent;
  font-family: var(--dt-font-family);
  font-size: 12px;
  color: var(--dt-text-secondary);
  cursor: pointer;

  &.active {
    border-color: var(--dt-brand-color);
    background: var(--dt-brand-light);
    color: var(--dt-brand-color);
    font-weight: 500;
  }
}

/* ---------- 主题色圆点 ---------- */
.dt-theme-dots {
  display: flex;
  gap: 16px;
  padding: 14px 16px;
}

.dt-theme-dot {
  position: relative;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: var(--dt-radius-full);
  cursor: pointer;

  &.active {
    border-color: var(--dt-text-primary);
  }

  &.active::after {
    content: '✓';
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
    line-height: 1;
    color: var(--dt-bg-card);
  }
}

.dt-theme-dot--blue {
  background: var(--dt-theme-blue);
}

.dt-theme-dot--orange {
  background: var(--dt-theme-orange);
}

.dt-theme-dot--green {
  background: var(--dt-theme-green);
}

.dt-theme-dot--red {
  background: var(--dt-theme-red);
}

.dt-theme-dot--purple {
  background: var(--dt-theme-purple);
}

/* ---------- 个人资料卡 ---------- */
.dt-set-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

.dt-set-profile-info {
  flex: 1;
  min-width: 0;
}

.dt-set-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.dt-set-sub {
  margin-top: 2px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.dt-set-edit {
  flex-shrink: 0;
  padding: 5px 12px;
  border: 1px solid var(--dt-border);
  border-radius: var(--dt-radius-full);
  background: transparent;
  font-family: var(--dt-font-family);
  font-size: 12px;
  color: var(--dt-text-secondary);
  cursor: pointer;
}

/* ---------- 底部操作 ---------- */
.dt-set-gap {
  height: 16px;
}

.dt-set-logout {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: var(--dt-radius-xl);
  background: var(--dt-bg-card);
  color: var(--dt-error-color);
  font-family: var(--dt-font-family);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;

  &:active {
    background: var(--dt-error-bg);
  }
}

.dt-set-note {
  padding: 12px 16px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--dt-text-tertiary);
  text-align: center;
}

/* ---------- 进入动效（push 从右滑入） ---------- */
.dt-settings-slide-enter-active,
.dt-settings-slide-leave-active {
  transition: transform 0.22s ease, opacity 0.22s ease;
}

.dt-settings-slide-enter-from {
  transform: translateX(100%);
  opacity: 1;
}

.dt-settings-slide-leave-to {
  transform: translateX(100%);
  opacity: 1;
}
</style>
