<template>
  <div class="general-settings">
    <!-- 外观设置 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-orange">
          <span class="material-icons-outlined">palette</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            外观设置
          </h3>
          <p class="section-desc">
            自定义界面主题和显示效果
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <!-- 主题模式 -->
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">brightness_6</span>
              <span class="item-label">主题模式</span>
            </div>
            <span class="item-desc">选择您喜欢的界面主题风格</span>
          </div>
          <div class="item-action">
            <div class="theme-options">
              <div 
                class="theme-option"
                :class="{ active: localSettings.general.theme === 'light' }"
                @click="handleThemeChange('light')"
              >
                <div class="theme-preview light-theme">
                  <span class="material-icons-outlined">light_mode</span>
                </div>
                <span class="theme-label">浅色</span>
              </div>
              <div 
                class="theme-option"
                :class="{ active: localSettings.general.theme === 'dark' }"
                @click="handleThemeChange('dark')"
              >
                <div class="theme-preview dark-theme">
                  <span class="material-icons-outlined">dark_mode</span>
                </div>
                <span class="theme-label">深色</span>
              </div>
              <div 
                class="theme-option"
                :class="{ active: localSettings.general.theme === 'auto' }"
                @click="handleThemeChange('auto')"
              >
                <div class="theme-preview auto-theme">
                  <span class="material-icons-outlined">brightness_auto</span>
                </div>
                <span class="theme-label">跟随系统</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 语言与地区 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-blue">
          <span class="material-icons-outlined">language</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            语言与地区
          </h3>
          <p class="section-desc">
            设置界面语言和格式
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">translate</span>
              <span class="item-label">界面语言</span>
            </div>
          </div>
          <div class="item-action">
            <el-select 
              v-model="localSettings.general.language" 
              size="default" 
              style="width: 140px" 
              @change="handleSettingChange('language', $event)"
            >
              <el-option
                label="简体中文"
                value="zh-CN"
              >
                <span style="display: flex; align-items: center; gap: 8px;">
                  <span class="flag">🇨🇳</span> 简体中文
                </span>
              </el-option>
              <el-option
                label="English"
                value="en-US"
              >
                <span style="display: flex; align-items: center; gap: 8px;">
                  <span class="flag">🇺🇸</span> English
                </span>
              </el-option>
            </el-select>
          </div>
        </div>

        <el-divider class="item-divider" />
        
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">schedule</span>
              <span class="item-label">时间格式</span>
            </div>
          </div>
          <div class="item-action">
            <el-radio-group 
              v-model="localSettings.general.timeFormat" 
              size="small"
              @change="handleSettingChange('timeFormat', $event)"
            >
              <el-radio-button label="24h">
                24小时制
              </el-radio-button>
              <el-radio-button label="12h">
                12小时制
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>
    </section>

    <!-- 启动与行为 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-green">
          <span class="material-icons-outlined">power_settings_new</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            启动与行为
          </h3>
          <p class="section-desc">
            配置应用程序的启动和关闭行为
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">refresh</span>
              <span class="item-label">开机自启动</span>
            </div>
            <span class="item-desc">系统启动时自动运行应用程序</span>
          </div>
          <div class="item-action">
            <el-switch 
              v-model="localSettings.general.autoStart" 
              @change="handleSettingChange('autoStart', $event)" 
            />
          </div>
        </div>

        <el-divider class="item-divider" />
        
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">minimize</span>
              <span class="item-label">关闭时最小化到托盘</span>
            </div>
            <span class="item-desc">点击关闭按钮时隐藏到系统托盘而不是退出</span>
          </div>
          <div class="item-action">
            <el-switch 
              v-model="localSettings.general.minimizeToTray" 
              @change="handleSettingChange('minimizeToTray', $event)" 
            />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, watch, ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import { getSettingsByType, updateSetting } from '@/api/im/userSettings'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const store = useStore()
const { setTheme: switchTheme } = useTheme()

const localSettings = reactive({
  general: {
    theme: 'light',
    language: 'zh-CN',
    timeFormat: '24h',
    autoStart: false,
    minimizeToTray: true
  }
})

const saving = ref(false)
const saveTimer = ref(null)

onMounted(async () => {
  await loadSettings()
})

const loadSettings = async () => {
  try {
    const res = await getSettingsByType('general')
    if (res.code === 200 && res.data) {
      const backendSettings = {}
      res.data.forEach(item => {
        backendSettings[item.settingKey] = item.settingValue
      })
      
      Object.assign(localSettings.general, {
        theme: backendSettings.theme || 'light',
        language: backendSettings.language || 'zh-CN',
        timeFormat: backendSettings.timeFormat || '24h',
        autoStart: backendSettings.autoStart === 'true',
        minimizeToTray: backendSettings.minimizeToTray === 'true'
      })
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

const handleThemeChange = theme => {
  localSettings.general.theme = theme
  switchTheme(theme)
  handleDebounceSave('theme', theme)
}

const handleSettingChange = (key, value) => {
  localSettings.general[key] = value
  handleDebounceSave(key, value)
}

const handleDebounceSave = (key, value) => {
  if (saveTimer.value) {clearTimeout(saveTimer.value)}
  saveTimer.value = setTimeout(async () => {
    await saveSingleSetting(key, value)
  }, 500)
}

const saveSingleSetting = async (key, value) => {
  try {
    saving.value = true
    const res = await updateSetting({ settingKey: key, settingValue: String(value) })
    if (res.code === 200) {
      ElMessage.success('设置已保存')
      emit('update:modelValue', JSON.parse(JSON.stringify(localSettings)))
      emit('change')
    }
  } catch (error) {
    console.error('保存设置失败:', error)
  } finally {
    saving.value = false
  }
}

watch(() => props.modelValue, newVal => {
  if (JSON.stringify(newVal) !== JSON.stringify(localSettings)) {
    Object.assign(localSettings, JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })
</script>

<style scoped lang="scss">
.general-settings {
  max-width: 720px;
}

.setting-section {
  margin-bottom: 32px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.section-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  
  span {
    font-size: 24px;
    color: white;
  }
  
  &.bg-gradient-orange {
    background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
  }
  
  &.bg-gradient-blue {
    background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
  }
  
  &.bg-gradient-green {
    background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
  }
}

.section-title-group {
  flex: 1;
}

.section-title {
  font-size: 16px;
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 0;
}

.setting-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
}

.item-divider {
  margin: 0;
  border-color: var(--dt-border-light);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: var(--dt-bg-hover);
  }
}

.item-main {
  flex: 1;
  margin-right: 20px;
}

.item-label-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.item-label {
  font-size: 14px;
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.item-icon {
  font-size: 20px;
  color: var(--dt-text-secondary);
}

.item-desc {
  font-size: 12px;
  color: var(--dt-text-secondary);
  padding-left: 28px;
}

.item-action {
  display: flex;
  align-items: center;
}

// 主题选项
.theme-options {
  display: flex;
  gap: 16px;
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    .theme-preview {
      transform: scale(1.05);
    }
  }
  
  &.active {
    .theme-preview {
      border-color: var(--dt-brand-color);
      box-shadow: 0 0 0 2px var(--dt-brand-bg);
    }
    
    .theme-label {
      color: var(--dt-brand-color);
      font-weight: var(--dt-font-weight-medium);
    }
  }
}

.theme-preview {
  width: 64px;
  height: 64px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--dt-border-color);
  transition: all 0.2s ease;
  
  span {
    font-size: 28px;
  }
  
  &.light-theme {
    background: linear-gradient(135deg, #fafafa 0%, #f0f0f0 100%);
    color: #faad14;
  }
  
  &.dark-theme {
    background: linear-gradient(135deg, #262626 0%, #1f1f1f 100%);
    color: #d9d9d9;
  }
  
  &.auto-theme {
    background: linear-gradient(135deg, #fafafa 0%, #262626 100%);
    color: var(--dt-brand-color);
  }
}

.theme-label {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

.flag {
  font-size: 16px;
}

// 暗黑模式适配
.dark {
  .setting-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
  
  .item-divider {
    border-color: var(--dt-border-dark);
  }
  
  .setting-item:hover {
    background-color: var(--dt-bg-hover-dark);
  }
}
</style>
