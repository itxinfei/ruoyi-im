<template>
  <div class="theme-settings">
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

const { setThemeMode } = useTheme()

const localSettings = ref({
  theme: props.modelValue?.theme || 'light',
  primaryColor: props.modelValue?.primaryColor || '#1677ff'
})

const customColor = ref(props.modelValue?.primaryColor || '#1677ff')

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

const emitChanges = () => {
  emit('update:modelValue', {
    theme: localSettings.value.theme,
    primaryColor: localSettings.value.primaryColor
  })
}

watch(() => props.modelValue, newVal => {
  if (newVal) {
    localSettings.value.theme = newVal.theme || 'light'
    localSettings.value.primaryColor = newVal.primaryColor || '#1677ff'
    customColor.value = newVal.primaryColor || '#1677ff'
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
