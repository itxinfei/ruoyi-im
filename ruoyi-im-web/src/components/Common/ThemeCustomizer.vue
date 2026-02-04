<!--
  主题定制器组件
-->
<template>
  <el-dialog
    v-model="visible"
    title="主题定制"
    width="480px"
    :before-close="handleClose"
    append-to-body
  >
    <div class="theme-customizer">
      <div class="customizer-section">
        <h4>配色方案</h4>
        <div class="color-palette">
          <div
            v-for="color in presetColors"
            :key="color.name"
            class="color-preset"
            @click="applyPreset(color)"
          >
            <div class="color-preview" :style="{ background: color.primary }"></div>
            <span class="color-name">{{ color.name }}</span>
          </div>
        </div>
      </div>

      <div class="customizer-section">
        <h4>自定义主题色</h4>
        <div class="custom-colors">
          <div class="color-input">
            <span class="color-label">主色</span>
            <input type="color" v-model="customColors.primary" @input="handleCustomColorChange">
            <span class="color-value">{{ customColors.primary }}</span>
          </div>
          <div class="color-input">
            <span class="color-label">强调色</span>
            <input type="color" v-model="customColors.accent" @input="handleCustomColorChange">
            <span class="color-value">{{ customColors.accent }}</span>
          </div>
        </div>
      </div>

      <div class="customizer-actions">
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" @click="handleApply">应用</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'change'])

const visible = ref(false)

// 预设配色方案
const presetColors = [
  { name: '默认蓝', primary: '#0089FF', accent: '#1890FF' },
  { name: '科技绿', primary: '#52C41A', accent: '#73D13D' },
  { name: '优雅紫', primary: '#722ED1', accent: '#9254DE' },
  { name: '活力橙', primary: '#FA8C16', accent: '#FFA940' }
]

// 自定义颜色
const customColors = reactive({
  primary: '#0089FF',
  accent: '#1890FF'
})

const applyPreset = (color) => {
  customColors.primary = color.primary
  customColors.accent = color.accent
}

const handleCustomColorChange = () => {
  // 颜色变更时自动预览
}

const handleReset = () => {
  customColors.primary = '#0089FF'
  customColors.accent = '#1890FF'
  ElMessage.success('已重置为默认颜色')
}

const handleApply = () => {
  emit('change', {
    primaryColor: customColors.primary,
    accentColor: customColors.accent
  })
  visible.value = false
  ElMessage.success('主题已应用')
}

const handleClose = () => {
  visible.value = false
}

// 监听显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style scoped lang="scss">
.theme-customizer {
  padding: 20px 0;
}

.customizer-section {
  margin-bottom: 24px;

  h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 12px 0;
  }
}

.color-palette {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.color-preset {
  cursor: pointer;
  text-align: center;

  .color-preview {
    width: 100%;
    height: 48px;
    border-radius: var(--dt-radius-md);
    margin-bottom: 8px;
    border: 2px solid var(--dt-border-color);
    transition: all 0.2s;

    &:hover {
      border-color: var(--dt-brand-color);
      transform: translateY(-2px);
    }
  }

  .color-name {
    font-size: 12px;
    color: var(--dt-text-secondary);
  }
}

.custom-colors {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.color-input {
  display: flex;
  align-items: center;
  gap: 12px;

  input[type="color"] {
    width: 40px;
    height: 40px;
    border: none;
    border-radius: var(--dt-radius-md);
    cursor: pointer;
  }

  .color-label {
    flex: 1;
    font-size: 14px;
    color: var(--dt-text-secondary);
  }

  .color-value {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    font-family: monospace;
  }
}

.customizer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--dt-border-light);
}
</style>
