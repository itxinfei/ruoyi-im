<template>
  <el-dialog
    v-model="visible"
    title="设置"
    width="600px"
    class="system-settings-dialog"
    destroy-on-close
    append-to-body
  >
    <div class="settings-container scrollbar-thin">
      <!-- 侧边导航 (设置分类) -->
      <div class="settings-aside">
        <div 
          v-for="item in menuItems" 
          :key="item.id"
          class="menu-item"
          :class="{ active: activeMenu === item.id }"
          @click="activeMenu = item.id"
        >
          <span class="material-icons-outlined menu-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </div>
      </div>

      <!-- 右侧设置内容 -->
      <div class="settings-main">
        <template v-if="activeMenu === 'account'">
          <div class="section-title">账号安全</div>
          <div class="setting-group">
            <div class="setting-card">
              <div class="setting-row">
                <div class="info">
                  <div class="label">当前登录账号</div>
                  <div class="desc">{{ currentUser.username }} (UID: {{ currentUser.id }})</div>
                </div>
              </div>
              <div class="setting-row">
                <div class="info">
                  <div class="label">修改登录密码</div>
                  <div class="desc">建议定期修改密码以保障账号安全</div>
                </div>
                <el-button type="primary" plain size="small" @click="showChangePassword = true">修改密码</el-button>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'notification'">
          <div class="section-title">消息通知</div>
          <div class="setting-group">
            <div class="setting-card">
              <div class="setting-row">
                <div class="info">
                  <div class="label">新消息提醒</div>
                  <div class="desc">在桌面显示新消息通知</div>
                </div>
                <el-switch v-model="localSettings.notifications.enabled" />
              </div>
              <div class="setting-row">
                <div class="info">
                  <div class="label">声音提醒</div>
                  <div class="desc">播放新消息提示音</div>
                </div>
                <div class="flex items-center gap-3">
                  <el-button link type="primary" size="small" @click="testSound" v-if="localSettings.notifications.sound">
                    <el-icon class="mr-1"><VideoPlay /></el-icon>测试音效
                  </el-button>
                  <el-switch v-model="localSettings.notifications.sound" />
                </div>
              </div>
            </div>
            <div class="section-title mt-6">快捷键</div>
            <div class="setting-card">
              <div class="setting-row">
                <div class="info">
                  <div class="label">发送消息</div>
                  <div class="desc">设置发送消息的快捷按键</div>
                </div>
                <el-select v-model="localSettings.shortcuts.send" size="small" style="width: 120px">
                  <el-option label="Enter" value="enter" />
                  <el-option label="Ctrl + Enter" value="ctrl-enter" />
                </el-select>
              </div>
              <div class="setting-row">
                <div class="info">
                  <div class="label">截图快捷键</div>
                  <div class="desc">全局唤起截图工具 (模拟)</div>
                </div>
                <code class="shortcut-key">Alt + A</code>
              </div>
              <div class="setting-row">
                <div class="info">
                  <div class="label">快捷唤起</div>
                  <div class="desc">快速显示/隐藏 IM 窗口</div>
                </div>
                <code class="shortcut-key">Alt + Q</code>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'privacy'">
          <div class="section-title">隐私与安全</div>
          <div class="setting-group">
            <div class="setting-card">
              <div class="setting-row">
                <div class="info">
                  <div class="label">在线状态</div>
                  <div class="desc">允许他人查看我的在线/离线状态</div>
                </div>
                <el-switch v-model="localSettings.privacy.showStatus" />
              </div>
              <div class="setting-row">
                <div class="info">
                  <div class="label">已读回执</div>
                  <div class="desc">发送消息已读回执给对方</div>
                </div>
                <el-switch v-model="localSettings.privacy.readReceipt" />
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'general'">
          <div class="section-title">通用设置</div>
          <div class="setting-group">
            <div class="setting-card">
              <div class="setting-row">
                <div class="info">
                  <div class="label">外观主题</div>
                  <div class="desc">切换系统的视觉配色</div>
                </div>
                <div class="theme-picker">
                  <el-radio-group v-model="localSettings.general.theme" size="small">
                    <el-radio-button label="light">浅色</el-radio-button>
                    <el-radio-button label="dark">深色</el-radio-button>
                    <el-radio-button label="auto">跟随系统</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
              <div class="setting-row">
                <div class="info">
                  <div class="label">多语言</div>
                  <div class="desc">切换系统显示语言</div>
                </div>
                <el-select v-model="localSettings.general.language" style="width: 120px" size="small">
                  <el-option label="简体中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                </el-select>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'about'">
          <div class="section-title">关于 IM</div>
          <div class="about-content">
            <div class="logo">IM</div>
            <div class="version">版本: v4.1.0</div>
            <div class="copyright">© 2025 RuoYi-IM Team. All rights reserved.</div>
            <el-button link type="primary" class="mt-4" @click="checkUpdate">检查更新</el-button>
          </div>
        </template>
      </div>
    </div>
    <ChangePasswordDialog v-model="showChangePassword" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, computed } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import { ElMessage } from 'element-plus'
import { VideoPlay } from '@element-plus/icons-vue'
import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const visible = ref(false)
const showChangePassword = ref(false)
const activeMenu = ref('account')
const { isDark, themeMode, setThemeMode } = useTheme()

// 移除这里的提前调用，移动到下方定义后
// ...

const menuItems = [
  { id: 'account', label: '账号安全', icon: 'manage_accounts' },
  { id: 'notification', label: '通知设置', icon: 'notifications' },
  { id: 'privacy', label: '隐私安全', icon: 'security' },
  { id: 'general', label: '通用设置', icon: 'settings' },
  { id: 'about', label: '关于应用', icon: 'info' }
]

const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 使用 Store 中的设置
const settings = computed(() => store.state.im.settings)

// 更新设置的方法
const updateSetting = (key, value) => {
  const newSettings = { ...settings.value }
  newSettings[key] = { ...newSettings[key], ...value }
  store.commit('im/UPDATE_SETTINGS', newSettings)
}

// 代理 reactive 对象以保持模板兼容性 (或者直接修改模板中的绑定)
// 为了不修改大规模模板，我们使用一个监听器同步
const localSettings = reactive(JSON.parse(JSON.stringify(settings.value)))

// 核心同步逻辑：本地修改同步到 Store
watch(localSettings, (newVal) => {
  // 只有当本地值与 Store 值不一致时才提交，防止死循环
  if (JSON.stringify(newVal) !== JSON.stringify(settings.value)) {
    store.commit('im/UPDATE_SETTINGS', JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })

// 远端/Store 变化同步回本地
watch(() => settings.value, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(localSettings)) {
    Object.assign(localSettings, JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })

// 同步主题设置到 Hook
watch(() => localSettings.general.theme, (val) => {
  if (val !== themeMode.value) {
    setThemeMode(val)
  }
}, { immediate: true })

// 移除本地冗余逻辑，由 Vuex 统一管理
// ...

const testSound = () => {
  ElMessage.success('测试音效播放中...')
  // 实际项目中可以播放一段短促的提示音
}

const checkUpdate = () => {
  ElMessage.success('当前已是最新版本')
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.system-settings-dialog :deep(.el-dialog__body) {
  padding: 0;
  height: 480px;
}

.settings-container {
  display: flex;
  height: 100%;
}

.settings-aside {
  width: 160px;
  background: #f8fafc;
  border-right: 1px solid #f2f3f5;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .dark & { background: #0f172a; border-right-color: #334155; }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    font-size: 14px;
    color: #4b5563;
    cursor: pointer;
    transition: all 0.2s ease;
    border-radius: 8px;
    
    .dark & { color: #94a3b8; }

    .menu-icon { 
      font-size: 20px; 
      color: #64748b; 
      transition: all 0.2s;
    }
    
    &:hover { 
      background: rgba(0,0,0,0.04); 
      color: #1f2329;
      .dark & { background: rgba(255,255,255,0.06); color: #f1f5f9; } 
    }
    
    &.active {
      background: #eff6ff;
      color: #0089ff;
      font-weight: 600;
      .menu-icon { color: #0089ff; transform: scale(1.1); }
      .dark & { 
        background: rgba(0, 137, 255, 0.15); 
        color: #38bdf8; 
        .menu-icon { color: #38bdf8; } 
      }
    }
  }
}

.settings-main {
  flex: 1;
  padding: 32px;
  background: #fff;
  overflow-y: auto;
  scroll-behavior: smooth;
  
  .dark & { background: #1e293b; color: #f1f5f9; }

  .section-title {
    font-size: 18px;
    font-weight: 700;
    margin-bottom: 24px;
    color: #1f2329;
    .dark & { color: #f8fafc; }
  }
}

.setting-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.setting-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px 20px;
  border: 1px solid #f1f5f9;
  transition: all 0.2s;
  
  .dark & { background: #0f172a; border-color: #334155; }
  
  &:hover {
    border-color: #e2e8f0;
    .dark & { border-color: #475569; }
  }

  .setting-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    &:not(:last-child) {
      margin-bottom: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f1f5f9;
      .dark & { border-bottom-color: #1e293b; }
    }
    
    .info {
      .label { 
        font-size: 14px; 
        font-weight: 600; 
        color: #1f2329; 
        margin-bottom: 4px; 
        .dark & { color: #f1f5f9; } 
      }
      .desc { font-size: 12px; color: #64748b; line-height: 1.4; }
    }
  }
}

.about-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  background: #f8fafc;
  border-radius: 16px;
  .dark & { background: #0f172a; }
  
  .logo {
    width: 72px; height: 72px; background: linear-gradient(135deg, #0089ff 0%, #00d2ff 100%);
    color: #fff; border-radius: 20px; display: flex; align-items: center; justify-content: center;
    font-size: 28px; font-weight: 800; margin-bottom: 20px;
    box-shadow: 0 8px 20px rgba(0, 137, 255, 0.25);
  }
  .version { font-size: 16px; font-weight: 700; color: #1f2329; margin-bottom: 8px; .dark & { color: #f1f5f9; } }
  .copyright { font-size: 12px; color: #64748b; }
}

.shortcut-key {
  background: #f0f2f5;
  padding: 4px 10px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
  color: #1f2329;
  border: 1px solid #dcdfe6;
  .dark & { background: #1e293b; color: #f1f5f9; border-color: #475569; }
}

.scrollbar-thin::-webkit-scrollbar { width: 4px; }
.scrollbar-thin::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 4px; }
</style>
