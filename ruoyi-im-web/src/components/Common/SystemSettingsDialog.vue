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
        <template v-if="activeMenu === 'notification'">
          <div class="section-title">消息通知</div>
          <div class="setting-group">
            <div class="setting-row">
              <div class="info">
                <div class="label">新消息提醒</div>
                <div class="desc">在桌面显示新消息通知</div>
              </div>
              <el-switch v-model="settings.notifications.enabled" />
            </div>
            <div class="setting-row">
              <div class="info">
                <div class="label">声音提醒</div>
                <div class="desc">播放新消息提示音</div>
              </div>
              <el-switch v-model="settings.notifications.sound" />
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'privacy'">
          <div class="section-title">隐私与安全</div>
          <div class="setting-group">
            <div class="setting-row">
              <div class="info">
                <div class="label">在线状态</div>
                <div class="desc">允许他人查看我的在线/离线状态</div>
              </div>
              <el-switch v-model="settings.privacy.showStatus" />
            </div>
            <div class="setting-row">
              <div class="info">
                <div class="label">已读回执</div>
                <div class="desc">发送消息已读回执给对方</div>
              </div>
              <el-switch v-model="settings.privacy.readReceipt" />
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'general'">
          <div class="section-title">通用设置</div>
          <div class="setting-group">
            <div class="setting-row">
              <div class="info">
                <div class="label">外观主题</div>
                <div class="desc">切换系统的视觉配色</div>
              </div>
              <div class="theme-picker">
                <el-radio-group v-model="isDark" size="small" @change="toggleDark">
                  <el-radio-button :value="false">浅色</el-radio-button>
                  <el-radio-button :value="true">深色</el-radio-button>
                </el-radio-group>
              </div>
            </div>
            <div class="setting-row">
              <div class="info">
                <div class="label">多语言</div>
                <div class="desc">切换系统显示语言</div>
              </div>
              <el-select v-model="settings.general.language" style="width: 120px" size="small">
                <el-option label="简体中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
              </el-select>
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
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const activeMenu = ref('notification')
const { isDark, toggleDark } = useTheme()

const menuItems = [
  { id: 'notification', label: '通知设置', icon: 'notifications' },
  { id: 'privacy', label: '隐私安全', icon: 'security' },
  { id: 'general', label: '通用设置', icon: 'settings' },
  { id: 'about', label: '关于应用', icon: 'info' }
]

const settings = reactive({
  notifications: {
    enabled: true,
    sound: false
  },
  privacy: {
    showStatus: true,
    readReceipt: true
  },
  general: {
    language: 'zh-CN'
  }
})

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
  padding: 12px 0;
  
  .dark & { background: #0f172a; border-right-color: #334155; }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 20px;
    font-size: 14px;
    color: #1f2329;
    cursor: pointer;
    transition: all 0.2s;
    
    .dark & { color: #cbd5e1; }

    .menu-icon { font-size: 20px; color: #646a73; }
    
    &:hover { background: #f2f3f5; .dark & { background: #1e293b; } }
    
    &.active {
      background: #e1f0ff;
      color: #0089ff;
      font-weight: 500;
      .menu-icon { color: #0089ff; }
      .dark & { background: rgba(0, 137, 255, 0.15); }
    }
  }
}

.settings-main {
  flex: 1;
  padding: 24px;
  background: #fff;
  overflow-y: auto;
  
  .dark & { background: #1e293b; color: #f1f5f9; }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 20px;
  }
}

.setting-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .info {
    .label { font-size: 14px; font-weight: 500; color: #1f2329; margin-bottom: 2px; .dark & { color: #f1f5f9; } }
    .desc { font-size: 12px; color: #8f959e; }
  }
}

.about-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40px;
  
  .logo {
    width: 80px; height: 80px; background: #0089ff; color: #fff;
    border-radius: 20px; display: flex; align-items: center; justify-content: center;
    font-size: 32px; font-weight: bold; margin-bottom: 16px;
    box-shadow: 0 4px 12px rgba(0, 137, 255, 0.3);
  }
  .version { font-size: 15px; font-weight: 600; margin-bottom: 8px; }
  .copyright { font-size: 12px; color: #8f959e; }
}

.scrollbar-thin::-webkit-scrollbar { width: 4px; }
.scrollbar-thin::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 4px; }
</style>
