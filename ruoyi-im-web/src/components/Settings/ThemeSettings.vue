<!--
  主题设置组件
-->
<template>
  <div class="theme-settings">
    <div class="setting-section">
      <div class="section-title">显示设置</div>
      <div class="setting-item">
        <span class="setting-label">主题模式</span>
        <el-radio-group v-model="localSettings.theme" @change="handleThemeChange">
          <el-radio-button label="light">浅色</el-radio-button>
          <el-radio-button label="dark">深色</el-radio-button>
          <el-radio-button label="auto">跟随系统</el-radio-button>
        </el-radio-group>
      </div>
      <div class="setting-item">
        <span class="setting-label">主题色</span>
        <div class="color-options">
          <div
            v-for="color in themeColors"
            :key="color.value"
            class="color-option"
            :class="{ active: localSettings.primaryColor === color.value }"
            @click="handleColorChange(color.value)"
            :style="{ '--color': color.value }"
          >
            <el-icon v-if="localSettings.primaryColor === color.value"><Check /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue'])

const { setThemeMode } = useTheme()

// 本地设置
const localSettings = reactive({
  theme: props.modelValue?.theme || 'light',
  primaryColor: props.modelValue?.primaryColor || '#0089FF'
})

// 主题色选项
const themeColors = [
  { value: '#0089FF', name: '蓝色' },
  { value: '#52C41A', name: '绿色' },
  { value: '#722ED1', name: '紫色' },
  { value: '#FA8C16', name: '橙色' },
  { value: '#F5222D', name: '红色' }
]

const handleThemeChange = () => {
  setThemeMode(localSettings.theme)
  emitChanges()
}

const handleColorChange = (color) => {
  localSettings.primaryColor = color
  emitChanges()
}

const emitChanges = () => {
  emit('update:modelValue', {
    theme: localSettings.theme,
    primaryColor: localSettings.primaryColor
  })
}

// 监听外部变化
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    localSettings.theme = newVal.theme || 'light'
    localSettings.primaryColor = newVal.primaryColor || '#0089FF'
  }
}, { deep: true })
</script>

<style scoped lang="scss">
.theme-settings {
  padding: 20px 0;
}

.setting-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 16px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 12px 0;
  border-bottom: 1px solid var(--dt-border-light);

  &:last-child {
    border-bottom: none;
  }
}

.setting-label {
  font-size: 14px;
  color: var(--dt-text-secondary);
  margin-right: 20px;
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
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--dt-border-color);
  transition: all 0.2s;
  position: relative;

  &.active {
    border-color: var(--dt-brand-color);
    transform: scale(1.1);
  }

  &:hover {
    transform: scale(1.05);
  }
}
</style>
