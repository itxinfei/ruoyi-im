<template>
  <div class="theme-settings">
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-blue">
          <span class="material-icons-outlined">contrast</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            显示模式
          </h3>
          <p class="section-desc">
            选择浅色、深色或跟随系统
          </p>
        </div>
      </div>

      <div class="setting-card mode-grid">
        <button
          v-for="mode in themeModes"
          :key="mode.value"
          class="mode-card"
          :class="{ active: localSettings.themeMode === mode.value }"
          @click="handleThemeModeChange(mode.value)"
        >
          <span class="material-icons-outlined mode-icon">{{ mode.icon }}</span>
          <div class="mode-text">
            <span class="mode-title">{{ mode.label }}</span>
            <span class="mode-desc">{{ mode.desc }}</span>
          </div>
        </button>
      </div>
    </section>

    <!-- 主题色设置 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-pink">
          <span class="material-icons-outlined">color_lens</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            主题色
          </h3>
          <p class="section-desc">
            选择您喜欢的主题颜色
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div class="color-grid">
          <div
            v-for="color in themeColors"
            :key="color.value"
            class="color-option"
            :class="{ active: localSettings.primaryColor === color.value }"
            :style="{ '--color': color.value }"
            @click="handleColorChange(color.value)"
          >
            <div
              class="color-circle"
              :style="{ background: color.value }"
            >
              <span
                v-if="localSettings.primaryColor === color.value"
                class="material-icons-outlined check-icon"
              >check</span>
            </div>
            <span class="color-name">{{ color.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 自定义颜色 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-purple">
          <span class="material-icons-outlined">tune</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            自定义颜色
          </h3>
          <p class="section-desc">
            使用自定义的主题颜色
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div class="custom-color-row">
          <div
            class="color-preview"
            :style="{ background: customColor }"
          />
          <el-color-picker 
            v-model="customColor" 
            show-alpha
            @change="handleColorChange(customColor)"
          />
          <span class="color-value">{{ customColor }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useTheme } from '@/composables/useTheme'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue'])

const { setThemeMode, themeMode } = useTheme()

const resolveThemeMode = value => {
  return value?.general?.theme || value?.theme || themeMode.value || 'auto'
}

const resolvePrimaryColor = value => {
  return value?.theme?.primaryColor || value?.primaryColor || '#1677ff'
}

const localSettings = ref({
  themeMode: resolveThemeMode(props.modelValue),
  primaryColor: resolvePrimaryColor(props.modelValue)
})

const customColor = ref(resolvePrimaryColor(props.modelValue))

const themeModes = [
  { value: 'light', label: '浅色', desc: '明亮界面，阅读更清晰', icon: 'light_mode' },
  { value: 'dark', label: '深色', desc: '夜间使用更舒适', icon: 'dark_mode' },
  { value: 'auto', label: '跟随系统', desc: '自动匹配系统外观', icon: 'settings_suggest' }
]

const themeColors = [
  { value: '#1677ff', name: '科技蓝' },
  { value: '#52c41a', name: '生机绿' },
  { value: '#f5222d', name: '活力红' },
  { value: '#fa8c16', name: '温暖橙' },
  { value: '#722ed1', name: '优雅紫' },
  { value: '#eb2f96', name: '浪漫粉' },
  { value: '#13c2c2', name: '清新青' },
  { value: '#2f4554', name: '商务灰' }
]

const handleColorChange = color => {
  localSettings.value.primaryColor = color
  customColor.value = color
  emitChanges()
}

const handleThemeModeChange = mode => {
  localSettings.value.themeMode = mode
  setThemeMode(mode)
  emitChanges()
}

const emitChanges = () => {
  const incomingSettings = props.modelValue && typeof props.modelValue === 'object' ? props.modelValue : {}
  const incomingGeneral = incomingSettings.general || {}
  const incomingTheme = incomingSettings.theme || {}

  emit('update:modelValue', {
    ...incomingSettings,
    general: {
      ...incomingGeneral,
      theme: localSettings.value.themeMode
    },
    theme: {
      ...incomingTheme,
      primaryColor: localSettings.value.primaryColor
    }
  })
}

watch(() => props.modelValue, newVal => {
  if (newVal) {
    localSettings.value.themeMode = resolveThemeMode(newVal)
    localSettings.value.primaryColor = resolvePrimaryColor(newVal)
    customColor.value = resolvePrimaryColor(newVal)
  }
}, { deep: true })
</script>

<style scoped lang="scss">
.theme-settings {
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
  
  &.bg-gradient-pink {
    background: linear-gradient(135deg, #eb2f96 0%, #ff85c0 100%);
  }

  &.bg-gradient-purple {
    background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);
  }

  &.bg-gradient-blue {
    background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
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
  padding: 24px;
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.mode-card {
  border: 1px solid var(--dt-border-color);
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    border-color: var(--dt-brand-color);
  }

  &.active {
    border-color: var(--dt-brand-color);
    background: color-mix(in srgb, var(--dt-brand-color) 10%, var(--dt-bg-body));
  }
}

.mode-icon {
  font-size: 20px;
  color: var(--dt-brand-color);
}

.mode-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.mode-title {
  font-size: 13px;
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
}

.mode-desc {
  font-size: 12px;
  color: var(--dt-text-secondary);
  line-height: 1.35;
}

// 颜色网格
.color-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.color-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    .color-circle {
      transform: scale(1.1);
    }
  }
  
  &.active {
    .color-circle {
      box-shadow: 0 0 0 3px var(--dt-bg-card), 0 0 0 5px var(--color);
    }
    
    .color-name {
      color: var(--dt-text-primary);
      font-weight: var(--dt-font-weight-medium);
    }
  }
}

.color-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  
  .check-icon {
    color: white;
    font-size: 24px;
    filter: drop-shadow(0 1px 2px var(--dt-shadow-color));
  }
}

.color-name {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

// 自定义颜色
.custom-color-row {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .color-preview {
    width: 48px;
    height: 48px;
    border-radius: var(--dt-radius-md);
    border: 1px solid var(--dt-border-color);
  }
  
  .color-value {
    font-family: monospace;
    font-size: 14px;
    color: var(--dt-text-secondary);
  }
}

@media (max-width: 900px) {
  .mode-grid {
    grid-template-columns: 1fr;
  }
}

// 暗黑模式适配
.dark {
  .setting-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
  
  .color-option.active .color-circle {
    box-shadow: 0 0 0 3px var(--dt-bg-card-dark), 0 0 0 5px var(--color);
  }
}
</style>
