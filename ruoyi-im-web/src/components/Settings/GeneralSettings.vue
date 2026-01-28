<template>
  <div class="general-settings">
    <div class="setting-group">
      <h3 class="group-title">外观</h3>
      <div class="theme-selector">
        <div 
          class="theme-option" 
          :class="{ active: localSettings.general.theme === 'light' }"
          @click="selectTheme('light')"
        >
          <div class="theme-preview light-preview">
            <div class="preview-nav"></div>
            <div class="preview-content">
              <div class="preview-line"></div>
              <div class="preview-line short"></div>
            </div>
          </div>
          <span class="theme-label">浅色</span>
          <div class="active-indicator" v-if="localSettings.general.theme === 'light'">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        
        <div 
          class="theme-option" 
          :class="{ active: localSettings.general.theme === 'dark' }"
          @click="selectTheme('dark')"
        >
          <div class="theme-preview dark-preview">
            <div class="preview-nav"></div>
            <div class="preview-content">
              <div class="preview-line"></div>
              <div class="preview-line short"></div>
            </div>
          </div>
          <span class="theme-label">深色</span>
          <div class="active-indicator" v-if="localSettings.general.theme === 'dark'">
            <el-icon><Check /></el-icon>
          </div>
        </div>

        <div 
          class="theme-option" 
          :class="{ active: localSettings.general.theme === 'auto' }"
          @click="selectTheme('auto')"
        >
          <div class="theme-preview auto-preview">
            <div class="preview-half light"></div>
            <div class="preview-half dark"></div>
          </div>
          <span class="theme-label">跟随系统</span>
          <div class="active-indicator" v-if="localSettings.general.theme === 'auto'">
            <el-icon><Check /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <div class="setting-group">
      <h3 class="group-title">语言</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">界面语言</div>
            <div class="item-desc">选择软件显示的语言</div>
          </div>
          <el-select v-model="localSettings.general.language" size="default" style="width: 140px" @change="handleChange">
            <el-option label="简体中文" value="zh-CN" />
            <el-option label="English" value="en-US" />
          </el-select>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { Check } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const localSettings = reactive(JSON.parse(JSON.stringify(props.modelValue)))

watch(() => props.modelValue, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(localSettings)) {
    Object.assign(localSettings, JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })

const selectTheme = (theme) => {
  localSettings.general.theme = theme
  handleChange()
}

const handleChange = () => {
  emit('update:modelValue', JSON.parse(JSON.stringify(localSettings)))
  emit('change')
}
</script>

<style scoped lang="scss">
.general-settings {
  padding-bottom: 20px;
}

.setting-group {
  margin-bottom: 32px;
}

.group-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  margin-bottom: 16px;
  padding-left: 4px;
}

.theme-selector {
  display: flex;
  gap: 20px;
}

.theme-option {
  position: relative;
  cursor: pointer;
  width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  
  &:hover .theme-preview {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &.active .theme-preview {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px var(--dt-brand-color-light);
  }
}

.theme-preview {
  width: 100%;
  height: 80px;
  border-radius: 8px;
  border: 1px solid var(--dt-border-light);
  overflow: hidden;
  position: relative;
  transition: all 0.2s;
  background: #fff;
  
  .dark & {
    border-color: var(--dt-border-dark);
  }
}

.light-preview {
  background: #f5f5f5;
  .preview-nav {
    width: 20px;
    height: 100%;
    background: #fff;
    position: absolute;
    left: 0;
    top: 0;
    border-right: 1px solid #eee;
  }
  .preview-content {
    margin-left: 20px;
    padding: 10px;
  }
  .preview-line {
    height: 4px;
    background: #e0e0e0;
    margin-bottom: 6px;
    border-radius: 2px;
    &.short { width: 60%; }
  }
}

.dark-preview {
  background: #1e1e1e;
  .preview-nav {
    width: 20px;
    height: 100%;
    background: #2d2d2d;
    position: absolute;
    left: 0;
    top: 0;
    border-right: 1px solid #333;
  }
  .preview-content {
    margin-left: 20px;
    padding: 10px;
  }
  .preview-line {
    height: 4px;
    background: #444;
    margin-bottom: 6px;
    border-radius: 2px;
    &.short { width: 60%; }
  }
}

.auto-preview {
  display: flex;
  .preview-half {
    flex: 1;
    height: 100%;
    &.light { background: #f5f5f5; }
    &.dark { background: #1e1e1e; }
  }
}

.theme-label {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

.active-indicator {
  position: absolute;
  top: 4px;
  right: 4px;
  background: var(--dt-brand-color);
  color: #fff;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.setting-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  overflow: hidden;
  
  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  
  .item-title {
    font-size: 15px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin-bottom: 4px;
  }

  .item-desc {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }
}
</style>
