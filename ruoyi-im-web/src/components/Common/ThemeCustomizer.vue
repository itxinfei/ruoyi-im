<template>
  <el-dialog
    v-model="visible"
    title="主题定制"
    :width="680"
    :modal="true"
    :close-on-click-modal="true"
    class="theme-customizer-dialog"
    @close="handleClose"
  >
    <div class="theme-customizer">
      <!-- 预设主题 -->
      <div class="customizer-section">
        <div class="section-header">
          <span class="material-icons-outlined section-icon">palette</span>
          <h3 class="section-title">预设主题</h3>
        </div>
        <div class="theme-presets">
          <button
            v-for="preset in themePresets"
            :key="preset.id"
            class="theme-preset"
            :class="{ active: currentTheme.id === preset.id }"
            @click="applyTheme(preset)"
          >
            <div class="preset-preview" :style="getPresetStyle(preset)">
              <div class="preview-header"></div>
              <div class="preview-body">
                <div class="preview-bubble left"></div>
                <div class="preview-bubble right"></div>
              </div>
            </div>
            <span class="preset-name">{{ preset.name }}</span>
          </button>
        </div>
      </div>

      <!-- 模糊效果设置 -->
      <div class="customizer-section">
        <div class="section-header">
          <span class="material-icons-outlined section-icon">blur_on</span>
          <h3 class="section-title">模糊效果</h3>
        </div>
        <div class="blur-settings">
          <div class="setting-item">
            <div class="setting-info">
              <span class="setting-label">背景模糊</span>
              <span class="setting-desc">聊天气泡和面板的毛玻璃效果</span>
            </div>
            <el-switch
              v-model="blurSettings.backgroundBlur"
              :active-color="brandColor"
              @change="handleBlurChange"
            />
          </div>
          <div class="setting-item">
            <div class="setting-info">
              <span class="setting-label">模糊强度</span>
              <span class="setting-desc">毛玻璃效果的强度</span>
            </div>
            <div class="blur-slider">
              <el-slider
                v-model="blurSettings.intensity"
                :min="0"
                :max="20"
                :step="1"
                :show-tooltip="false"
                @change="handleBlurChange"
              />
              <span class="slider-value">{{ blurSettings.intensity }}px</span>
            </div>
          </div>
          <div class="setting-item">
            <div class="setting-info">
              <span class="setting-label">透明度</span>
              <span class="setting-desc">背景透明度</span>
            </div>
            <div class="blur-slider">
              <el-slider
                v-model="blurSettings.opacity"
                :min="70"
                :max="100"
                :step="5"
                :show-tooltip="false"
                @change="handleBlurChange"
              />
              <span class="slider-value">{{ blurSettings.opacity }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 颜色自定义 -->
      <div class="customizer-section">
        <div class="section-header">
          <span class="material-icons-outlined section-icon">tune</span>
          <h3 class="section-title">颜色自定义</h3>
        </div>
        <div class="color-settings">
          <div class="color-item">
            <span class="color-label">品牌色</span>
            <div class="color-picker-wrapper">
              <input
                type="color"
                v-model="customColors.brand"
                class="color-input"
                @change="handleColorChange"
              />
              <span class="color-value">{{ customColors.brand }}</span>
            </div>
          </div>
          <div class="color-item">
            <span class="color-label">背景色</span>
            <div class="color-picker-wrapper">
              <input
                type="color"
                v-model="customColors.background"
                class="color-input"
                @change="handleColorChange"
              />
              <span class="color-value">{{ customColors.background }}</span>
            </div>
          </div>
          <div class="color-item">
            <span class="color-label">文字色</span>
            <div class="color-picker-wrapper">
              <input
                type="color"
                v-model="customColors.text"
                class="color-input"
                @change="handleColorChange"
              />
              <span class="color-value">{{ customColors.text }}</span>
            </div>
          </div>
          <div class="color-item">
            <span class="color-label">强调色</span>
            <div class="color-picker-wrapper">
              <input
                type="color"
                v-model="customColors.accent"
                class="color-input"
                @change="handleColorChange"
              />
              <span class="color-value">{{ customColors.accent }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 字体设置 -->
      <div class="customizer-section">
        <div class="section-header">
          <span class="material-icons-outlined section-icon">text_fields</span>
          <h3 class="section-title">字体设置</h3>
        </div>
        <div class="font-settings">
          <div class="font-item">
            <span class="font-label">字体大小</span>
            <div class="font-size-options">
              <button
                v-for="size in fontSizes"
                :key="size.value"
                class="size-btn"
                :class="{ active: fontSettings.size === size.value }"
                @click="handleFontSizeChange(size.value)"
              >
                {{ size.label }}
              </button>
            </div>
          </div>
          <div class="font-item">
            <span class="font-label">字体样式</span>
            <select v-model="fontSettings.family" @change="handleFontChange" class="font-select">
              <option value="system">系统默认</option>
              <option value="sans-serif">无衬线</option>
              <option value="serif">衬线</option>
              <option value="monospace">等宽</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 预览区域 -->
      <div class="customizer-section">
        <div class="section-header">
          <span class="material-icons-outlined section-icon">visibility</span>
          <h3 class="section-title">效果预览</h3>
        </div>
        <div class="preview-area" :style="getPreviewStyle()">
          <div class="preview-chat">
            <div class="preview-bubble received">
              <span class="bubble-text">这是一条收到的消息示例</span>
            </div>
            <div class="preview-bubble sent">
              <span class="bubble-text">这是发送的消息预览</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <button class="footer-btn secondary" @click="handleReset">
          <span class="material-icons-outlined">restart_alt</span>
          重置默认
        </button>
        <div class="footer-right">
          <button class="footer-btn secondary" @click="handleClose">取消</button>
          <button class="footer-btn primary" @click="handleSave">
            <span class="material-icons-outlined">check</span>
            应用主题
          </button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'change'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 预设主题
const themePresets = [
  {
    id: 'default',
    name: '默认蓝',
    brand: '#1677ff',
    background: '#f5f5f5',
    text: '#333333',
    accent: '#52c41a'
  },
  {
    id: 'purple',
    name: '优雅紫',
    brand: '#722ed1',
    background: '#f5f3ff',
    text: '#333333',
    accent: '#9333ea'
  },
  {
    id: 'green',
    name: '清新绿',
    brand: '#52c41a',
    background: '#f6ffed',
    text: '#333333',
    accent: '#1677ff'
  },
  {
    id: 'orange',
    name: '活力橙',
    brand: '#fa8c16',
    background: '#fff7e6',
    text: '#333333',
    accent: '#eb2f96'
  },
  {
    id: 'dark',
    name: '暗夜黑',
    brand: '#1677ff',
    background: '#1a1a1a',
    text: '#e5e5e5',
    accent: '#52c41a'
  },
  {
    id: 'pink',
    name: '少女粉',
    brand: '#eb2f96',
    background: '#fff0f6',
    text: '#333333',
    accent: '#722ed1'
  }
]

// 当前主题
const currentTheme = ref(themePresets[0])

// 模糊效果设置
const blurSettings = ref({
  backgroundBlur: true,
  intensity: 10,
  opacity: 95
})

// 自定义颜色
const customColors = ref({
  brand: '#1677ff',
  background: '#f5f5f5',
  text: '#333333',
  accent: '#52c41a'
})

// 字体设置
const fontSettings = ref({
  size: 14,
  family: 'system'
})

const fontSizes = [
  { label: '小', value: 12 },
  { label: '默认', value: 14 },
  { label: '中', value: 16 },
  { label: '大', value: 18 },
  { label: '特大', value: 20 }
]

const brandColor = computed(() => customColors.value.brand)

// 获取预设样式
const getPresetStyle = (preset) => {
  return {
    '--preview-brand': preset.brand,
    '--preview-bg': preset.background,
    '--preview-text': preset.text
  }
}

// 获取预览样式
const getPreviewStyle = () => {
  return {
    '--blur-amount': `${blurSettings.value.intensity}px`,
    '--bg-opacity': blurSettings.value.opacity / 100,
    '--preview-brand': customColors.value.brand,
    '--preview-bg': customColors.value.background,
    '--preview-text': customColors.value.text,
    '--preview-accent': customColors.value.accent,
    '--font-size': `${fontSettings.value.size}px`,
    '--font-family': getFontFamily(fontSettings.value.family),
    backdropFilter: blurSettings.value.backgroundBlur ? `blur(${blurSettings.value.intensity}px)` : 'none'
  }
}

const getFontFamily = (family) => {
  const families = {
    system: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial',
    'sans-serif': 'ui-sans-serif, system-ui, sans-serif',
    serif: 'ui-serif, Georgia, Cambria, "Times New Roman", Times, serif',
    monospace: 'ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace'
  }
  return families[family] || families.system
}

// 应用主题
const applyTheme = (preset) => {
  currentTheme.value = preset
  customColors.value = {
    brand: preset.brand,
    background: preset.background,
    text: preset.text,
    accent: preset.accent
  }

  // 暗色主题时自动调整模糊设置
  if (preset.id === 'dark') {
    blurSettings.value = {
      backgroundBlur: true,
      intensity: 15,
      opacity: 90
    }
  } else {
    blurSettings.value = {
      backgroundBlur: true,
      intensity: 10,
      opacity: 95
    }
  }
}

// 处理模糊变化
const handleBlurChange = () => {
  applyThemeToDocument()
}

// 处理颜色变化
const handleColorChange = () => {
  // 切换到自定义主题
  currentTheme.value = {
    id: 'custom',
    name: '自定义',
    brand: customColors.value.brand,
    background: customColors.value.background,
    text: customColors.value.text,
    accent: customColors.value.accent
  }
}

// 处理字体大小变化
const handleFontSizeChange = (size) => {
  fontSettings.value.size = size
}

// 处理字体变化
const handleFontChange = () => {
  // 字体变化处理
}

// 应用主题到文档
const applyThemeToDocument = () => {
  const root = document.documentElement

  // 应用颜色
  root.style.setProperty('--dt-brand-color', customColors.value.brand)
  root.style.setProperty('--dt-bg-body', customColors.value.background)
  root.style.setProperty('--dt-text-primary', customColors.value.text)

  // 应用模糊效果
  if (blurSettings.value.backgroundBlur) {
    root.style.setProperty('--dt-backdrop-blur', `${blurSettings.value.intensity}px`)
    root.style.setProperty('--dt-bg-opacity', blurSettings.value.opacity / 100)
  } else {
    root.style.setProperty('--dt-backdrop-blur', 'none')
  }

  // 应用字体
  root.style.setProperty('--dt-font-size-base', `${fontSettings.value.size}px`)
}

// 保存主题
const handleSave = () => {
  applyThemeToDocument()

  // 保存到本地存储
  const themeData = {
    current: currentTheme.value,
    blur: blurSettings.value,
    colors: customColors.value,
    font: fontSettings.value
  }
  localStorage.setItem('im-custom-theme', JSON.stringify(themeData))

  ElMessage.success('主题已应用')
  emit('change', themeData)
  handleClose()
}

// 重置主题
const handleReset = () => {
  applyTheme(themePresets[0])
  blurSettings.value = {
    backgroundBlur: true,
    intensity: 10,
    opacity: 95
  }
  fontSettings.value = {
    size: 14,
    family: 'system'
  }

  // 清除本地存储
  localStorage.removeItem('im-custom-theme')

  ElMessage.info('已重置为默认主题')
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 初始化
onMounted(() => {
  // 加载保存的主题
  const saved = localStorage.getItem('im-custom-theme')
  if (saved) {
    try {
      const themeData = JSON.parse(saved)
      if (themeData.current) currentTheme.value = themeData.current
      if (themeData.blur) blurSettings.value = themeData.blur
      if (themeData.colors) customColors.value = themeData.colors
      if (themeData.font) fontSettings.value = themeData.font
    } catch (e) {
      console.warn('加载主题失败', e)
    }
  }
})
</script>

<style scoped lang="scss">
.theme-customizer {
  max-height: 600px;
  overflow-y: auto;
  padding: 8px 0;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;
  }
}

.customizer-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--dt-border-light);
}

.section-icon {
  font-size: 20px;
  color: var(--dt-brand-color);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

// 预设主题
.theme-presets {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.theme-preset {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: var(--dt-bg-input);
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    border-color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }
}

.preset-preview {
  width: 80px;
  height: 60px;
  background: var(--preview-bg, #f5f5f5);
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .preview-header {
    height: 16px;
    background: var(--preview-brand, #1677ff);
  }

  .preview-body {
    padding: 6px;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .preview-bubble {
    height: 10px;
    border-radius: 4px;

    &.left {
      width: 30px;
      background: rgba(0, 0, 0, 0.1);
    }

    &.right {
      width: 40px;
      align-self: flex-end;
      background: var(--preview-brand, #1677ff);
      opacity: 0.8;
    }
  }
}

.preset-name {
  font-size: 12px;
  color: var(--dt-text-secondary);
}

// 模糊效果设置
.blur-settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: var(--dt-bg-input);
  border-radius: 8px;
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.setting-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
}

.setting-desc {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.blur-slider {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 200px;

  .el-slider {
    flex: 1;
  }
}

.slider-value {
  min-width: 45px;
  text-align: right;
  font-size: 12px;
  color: var(--dt-text-secondary);
  font-variant-numeric: tabular-nums;
}

// 颜色设置
.color-settings {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.color-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: var(--dt-bg-input);
  border-radius: 8px;
}

.color-label {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-input {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  background: none;
}

.color-value {
  font-size: 11px;
  color: var(--dt-text-tertiary);
  font-family: monospace;
}

// 字体设置
.font-settings {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.font-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: var(--dt-bg-input);
  border-radius: 8px;
}

.font-label {
  font-size: 14px;
  color: var(--dt-text-secondary);
}

.font-size-options {
  display: flex;
  gap: 6px;
}

.size-btn {
  padding: 6px 12px;
  border: 1px solid var(--dt-border-light);
  background: transparent;
  border-radius: 6px;
  font-size: 12px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-brand-color);
  }

  &.active {
    background: var(--dt-brand-color);
    border-color: var(--dt-brand-color);
    color: #fff;
  }
}

.font-select {
  padding: 6px 12px;
  border: 1px solid var(--dt-border-light);
  border-radius: 6px;
  background: var(--dt-bg-card);
  color: var(--dt-text-primary);
  font-size: 13px;
  cursor: pointer;

  &:focus {
    outline: none;
    border-color: var(--dt-brand-color);
  }
}

// 预览区域
.preview-area {
  padding: 16px;
  background: var(--preview-bg, #f5f5f5);
  border-radius: 12px;
  min-height: 100px;
  transition: all var(--dt-transition-base);

  &.has-blur {
    backdrop-filter: blur(var(--blur-amount, 10px));
    background: rgba(255, 255, 255, var(--bg-opacity, 0.95));
  }
}

.preview-chat {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: var(--font-size, 14px);
  font-family: var(--font-family, system-ui);

  &.received {
    align-self: flex-start;
    background: rgba(0, 0, 0, 0.05);
    color: var(--preview-text, #333);
  }

  &.sent {
    align-self: flex-end;
    background: var(--preview-brand, #1677ff);
    color: #fff;
  }
}

.bubble-text {
  line-height: 1.4;
}

// 对话框底部
.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.footer-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &.secondary {
    background: var(--dt-bg-input);
    color: var(--dt-text-secondary);

    &:hover {
      background: var(--dt-bg-hover);
    }
  }

  &.primary {
    background: var(--dt-brand-color);
    color: #fff;

    &:hover {
      opacity: 0.9;
      transform: translateY(-1px);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

.footer-right {
  display: flex;
  gap: 10px;
}

// 对话框样式调整
:deep(.theme-customizer-dialog) {
  .el-dialog__header {
    padding: 20px 24px 16px;
    border-bottom: 1px solid var(--dt-border-light);
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }

  .el-dialog__body {
    padding: 20px 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px 20px;
    border-top: 1px solid var(--dt-border-light);
  }
}
</style>
