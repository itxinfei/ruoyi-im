<template>
  <div 
    class="search-box" 
    :class="{ 
      'is-expanded': isExpanded, 
      'is-focused': isFocused,
      'is-loading': loading,
      [`size-${size}`]: true
    }"
  >
    <!-- 搜索输入框容器 -->
    <div
      class="search-input-container"
      @click="focusInput"
    >
      <!-- 前缀图标 -->
      <div class="prefix-icon">
        <el-icon
          v-if="loading"
          class="is-loading"
        >
          <Loading />
        </el-icon>
        <el-icon v-else>
          <Search />
        </el-icon>
      </div>
      
      <!-- 输入框 -->
      <input
        ref="inputRef"
        v-model="inputValue"
        type="text"
        class="search-input"
        :placeholder="placeholder"
        :disabled="disabled"
        @focus="handleFocus"
        @blur="handleBlur"
        @input="handleInput"
        @keydown="handleKeyDown"
      >
      
      <!-- 清除按钮 -->
      <transition name="fade-scale">
        <button
          v-if="showClear && inputValue"
          type="button"
          class="clear-btn"
          @click.stop="clearInput"
        >
          <el-icon><CircleClose /></el-icon>
        </button>
      </transition>
      
      <!-- 语音搜索按钮 -->
      <button
        v-if="showVoice"
        type="button"
        class="voice-btn"
        :class="{ 'is-recording': isRecording }"
        @click.stop="toggleVoice"
      >
        <el-icon><Microphone /></el-icon>
      </button>
      
      <!-- 搜索按钮 -->
      <button
        v-if="showSearchButton"
        type="button"
        class="search-btn"
        @click.stop="handleSearch"
      >
        <el-icon><ArrowRight /></el-icon>
      </button>
      
      <!-- 后缀插槽 -->
      <div
        v-if="$slots.suffix"
        class="suffix-slot"
      >
        <slot name="suffix" />
      </div>
    </div>
    
    <!-- 搜索建议下拉面板 -->
    <transition name="dropdown-slide">
      <div
        v-if="isExpanded && suggestions.length > 0"
        class="suggestions-dropdown"
        :style="dropdownStyle"
      >
        <!-- 建议列表 -->
        <div class="suggestions-list">
          <div
            v-for="(item, index) in suggestions"
            :key="item.id || index"
            class="suggestion-item"
            :class="{ 
              'is-active': activeIndex === index,
              'is-selected': isSelected(item)
            }"
            @click.stop="selectSuggestion(item)"
            @mouseenter="activeIndex = index"
          >
            <!-- 建议项图标 -->
            <div class="suggestion-icon">
              <el-icon v-if="item.icon">
                <component :is="item.icon" />
              </el-icon>
              <el-icon v-else>
                <Search />
              </el-icon>
            </div>
            
            <!-- 建议内容 -->
            <div class="suggestion-content">
              <span
                class="suggestion-text"
                v-html="highlightText(item.text)"
              />
              <span
                v-if="item.description"
                class="suggestion-desc"
              >{{ item.description }}</span>
            </div>
            
            <!-- 类型标签 -->
            <span
              v-if="item.type"
              class="suggestion-type"
            >{{ item.type }}</span>
            
            <!-- 选中图标 -->
            <el-icon
              v-if="isSelected(item)"
              class="selected-icon"
            >
              <Check />
            </el-icon>
          </div>
        </div>
        
        <!-- 下拉底部 -->
        <div
          v-if="$slots.dropdownFooter"
          class="dropdown-footer"
        >
          <slot name="dropdownFooter" />
        </div>
      </div>
    </transition>
    
    <!-- 快捷操作栏 -->
    <div
      v-if="showQuickActions && quickActions.length > 0"
      class="quick-actions"
    >
      <button
        v-for="action in quickActions"
        :key="action.key"
        type="button"
        class="quick-action-btn"
        :class="{ 'is-active': activeQuickAction === action.key }"
        @click.stop="handleQuickAction(action)"
      >
        <el-icon v-if="action.icon">
          <component :is="action.icon" />
        </el-icon>
        <span>{{ action.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { 
  Search, 
  CircleClose, 
  Microphone, 
  ArrowRight, 
  Loading,
  Check
} from '@element-plus/icons-vue'

const props = defineProps({
  // 输入值
  modelValue: {
    type: String,
    default: ''
  },
  // 占位符
  placeholder: {
    type: String,
    default: '搜索...'
  },
  // 尺寸
  size: {
    type: String,
    default: 'default',
    validator: value => ['small', 'default', 'large'].includes(value)
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否显示清除按钮
  showClear: {
    type: Boolean,
    default: true
  },
  // 是否显示语音按钮
  showVoice: {
    type: Boolean,
    default: false
  },
  // 是否显示搜索按钮
  showSearchButton: {
    type: Boolean,
    default: false
  },
  // 建议列表
  suggestions: {
    type: Array,
    default: () => []
  },
  // 是否加载中
  loading: {
    type: Boolean,
    default: false
  },
  // 防抖延迟
  debounce: {
    type: Number,
    default: 300
  },
  // 最大建议数
  maxSuggestions: {
    type: Number,
    default: 10
  },
  // 是否自动聚焦
  autofocus: {
    type: Boolean,
    default: false
  },
  // 是否远程搜索
  remote: {
    type: Boolean,
    default: false
  },
  // 是否显示快捷操作
  showQuickActions: {
    type: Boolean,
    default: false
  },
  // 快捷操作列表
  quickActions: {
    type: Array,
    default: () => []
  },
  // 下拉面板宽度
  dropdownWidth: {
    type: String,
    default: null
  }
})

const emit = defineEmits([
  'update:modelValue',
  'search',
  'select',
  'focus',
  'blur',
  'clear',
  'input',
  'quickAction'
])

// 状态
const inputRef = ref(null)
const isUnmounted = ref(false) // 标记组件是否已卸载
const inputValue = ref(props.modelValue)
const isFocused = ref(false)
const isExpanded = ref(false)
const activeIndex = ref(-1)
const isRecording = ref(false)
const activeQuickAction = ref('')

let debounceTimer = null
let blurTimer = null

// 计算属性
const dropdownStyle = computed(() => {
  if (props.dropdownWidth) {
    return { width: props.dropdownWidth }
  }
  return {}
})

// 监听输入值
watch(() => props.modelValue, val => {
  inputValue.value = val
})

watch(inputValue, val => {
  emit('update:modelValue', val)
  
  if (props.remote) {
    clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      emit('input', val)
    }, props.debounce)
  } else {
    emit('input', val)
  }
  
  // 有输入值时显示下拉
  if (val && props.suggestions.length > 0) {
    isExpanded.value = true
  }
})

// 监听建议列表
watch(() => props.suggestions, val => {
  if (val.length > 0 && inputValue.value) {
    isExpanded.value = true
    activeIndex.value = -1
  }
}, { deep: true })

// 方法
const focusInput = () => {
  inputRef.value?.focus()
}

const handleFocus = () => {
  isFocused.value = true
  
  // 有建议时展开下拉
  if (props.suggestions.length > 0) {
    isExpanded.value = true
  }
  
  emit('focus', inputValue.value)
}

const handleBlur = () => {
  isFocused.value = false
  
  // 延迟收起，以便点击建议项
  blurTimer = setTimeout(() => {
    isExpanded.value = false
    activeIndex.value = -1
  }, 200)
  
  emit('blur', inputValue.value)
}

const handleInput = () => {
  activeIndex.value = -1
}

const clearInput = () => {
  inputValue.value = ''
  isExpanded.value = false
  activeIndex.value = -1
  emit('clear')
  nextTick(() => {
    if (isUnmounted.value) {return}
    focusInput()
  })
}

const handleSearch = () => {
  isExpanded.value = false
  emit('search', inputValue.value)
}

const selectSuggestion = item => {
  inputValue.value = item.text || item.value || item.label
  isExpanded.value = false
  activeIndex.value = -1
  emit('select', item)
}

const isSelected = item => {
  return item.text === inputValue.value || item.value === inputValue.value
}

// 高亮搜索文本
const highlightText = text => {
  if (!text || !inputValue.value) {return escapeHtml(String(text))}
  const keyword = inputValue.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const reg = new RegExp(`(${keyword})`, 'gi')
  return escapeHtml(String(text)).replace(reg, '<mark>$1</mark>')
}

// 转义 HTML 特殊字符，防止 XSS 攻击
const escapeHtml = str => {
  if (!str) {return ''}
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}

const toggleVoice = () => {
  isRecording.value = !isRecording.value
  // 语音搜索逻辑
  if (isRecording.value) {
    // 开始录音
    console.log('开始语音搜索...')
  } else {
    // 停止录音
    console.log('停止语音搜索')
  }
}

const handleQuickAction = action => {
  activeQuickAction.value = action.key
  emit('quickAction', action)
}

// 键盘导航
const handleKeyDown = e => {
  if (!isExpanded.value || props.suggestions.length === 0) {
    if (e.key === 'Enter') {
      handleSearch()
    }
    return
  }
  
  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      activeIndex.value = (activeIndex.value + 1) % props.suggestions.length
      break
    case 'ArrowUp':
      e.preventDefault()
      activeIndex.value = activeIndex.value <= 0 
        ? props.suggestions.length - 1 
        : activeIndex.value - 1
      break
    case 'Enter':
      e.preventDefault()
      if (activeIndex.value >= 0) {
        selectSuggestion(props.suggestions[activeIndex.value])
      } else {
        handleSearch()
      }
      break
    case 'Escape':
      e.preventDefault()
      isExpanded.value = false
      activeIndex.value = -1
      break
    case 'Tab':
      isExpanded.value = false
      break
  }
}

// 生命周期
onMounted(() => {
  if (props.autofocus) {
    nextTick(() => {
      if (isUnmounted.value) {return}
      focusInput()
    })
  }
})

onUnmounted(() => {
  isUnmounted.value = true // 标记组件已卸载
  clearTimeout(debounceTimer)
  clearTimeout(blurTimer)
})

// 暴露方法
defineExpose({
  focus: focusInput,
  blur: () => inputRef.value?.blur(),
  clear: clearInput,
  search: handleSearch,
  expand: () => { isExpanded.value = true },
  collapse: () => { isExpanded.value = false }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 本地过渡变量（Design Tokens 中未定义）
$transition: all var(--dt-transition-base) var(--dt-ease-out);

// 动画
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all var(--dt-transition-fast) var(--dt-ease-out);
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.dropdown-slide-enter-active,
.dropdown-slide-leave-active {
  transition: all var(--dt-transition-base) var(--dt-ease-in-out);
}

.dropdown-slide-enter-from,
.dropdown-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 搜索框容器
.search-box {
  position: relative;
  width: 100%;
}

// 输入框容器
.search-input-container {
  display: flex;
  align-items: center;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-lg);
  padding: 4px;
  transition: $transition;
  cursor: text;

  &:hover {
    background: var(--dt-border-light);
  }

  // 聚焦状态
  .is-focused & {
    background: var(--dt-bg-card);
    box-shadow: 0 0 0 3px var(--dt-brand-light), var(--dt-shadow-3);
  }

  // 深色模式
  :global(.dark) & {
    background: var(--dt-bg-card-dark);

    &:hover {
      background: var(--dt-bg-hover-dark);
    }

    .is-focused & {
      background: var(--dt-bg-input-dark);
      box-shadow: 0 0 0 3px var(--dt-brand-light), var(--dt-shadow-3);
    }
  }
}

// 尺寸变体
.size-small {
  .search-input-container {
    padding: 2px;
    border-radius: var(--dt-radius-lg);
  }
  
  .prefix-icon,
  .clear-btn,
  .voice-btn,
  .search-btn {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
  
  .search-input {
    height: 28px;
    font-size: 13px;
  }
}

.size-large {
  .search-input-container {
    padding: 6px;
    border-radius: var(--dt-radius-xl);
  }
  
  .prefix-icon,
  .clear-btn,
  .voice-btn,
  .search-btn {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }
  
  .search-input {
    height: 44px;
    font-size: 16px;
  }
}

// 前缀图标
.prefix-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-tertiary);
  font-size: 18px;
  flex-shrink: 0;

  &.is-loading {
    color: var(--dt-brand-color);
  }

  :global(.dark) & {
    color: var(--dt-text-tertiary);
  }
}

// 输入框
.search-input {
  flex: 1;
  border: none;
  background: transparent;
  height: 36px;
  padding: 0 8px;
  font-size: 14px;
  color: var(--dt-text-primary);
  outline: none;

  &::placeholder {
    color: var(--dt-text-quaternary);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }

  :global(.dark) & {
    color: var(--dt-text-primary-dark);

    &::placeholder {
      color: var(--dt-text-tertiary);
    }
  }
}

// 清除按钮
.clear-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  font-size: 16px;
  cursor: pointer;
  border-radius: var(--dt-radius-full);
  flex-shrink: 0;
  transition: $transition;

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-secondary);
  }

  :global(.dark) & {
    color: var(--dt-text-tertiary);

    &:hover {
      background: var(--dt-bg-hover-dark);
      color: var(--dt-text-secondary);
    }
  }
}

// 语音按钮
.voice-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  font-size: 18px;
  cursor: pointer;
  border-radius: var(--dt-radius-full);
  flex-shrink: 0;
  transition: $transition;

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-secondary);
  }

  &.is-recording {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
    animation: pulse 1.5s infinite;
  }

  @keyframes pulse {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.1); }
  }

  :global(.dark) & {
    color: var(--dt-text-tertiary);

    &:hover {
      background: var(--dt-bg-hover-dark);
      color: var(--dt-text-secondary);
    }

    &.is-recording {
      background: rgba(244, 67, 54, 0.2);
      color: var(--dt-error-color);
    }
  }
}

// 搜索按钮
.search-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: var(--dt-brand-color);
  color: var(--dt-bubble-right-text);
  font-size: 18px;
  cursor: pointer;
  border-radius: var(--dt-radius-md);
  flex-shrink: 0;
  transition: $transition;

  &:hover {
    background: var(--dt-brand-hover);
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.95);
  }
}

// 后缀插槽
.suffix-slot {
  display: flex;
  align-items: center;
  padding-left: 8px;
  border-left: 1px solid var(--dt-border-color);
  margin-left: 4px;

  :global(.dark) & {
    border-color: var(--dt-border-dark);
  }
}

// 建议下拉面板
.suggestions-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-2xl);
  z-index: 1000;
  overflow: hidden;

  :global(.dark) & {
    background: var(--dt-bg-card-dark);
    box-shadow: var(--dt-shadow-3xl);
  }
}

.suggestions-list {
  max-height: 320px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: $transition;

  &:hover,
  &.is-active {
    background: var(--dt-bg-hover);

    :global(.dark) & {
      background: var(--dt-bg-hover-dark);
    }
  }

  &.is-selected {
    .suggestion-text {
      color: var(--dt-brand-color);
      font-weight: 500;
    }
  }
}

.suggestion-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  color: var(--dt-text-secondary);
  font-size: 16px;
  flex-shrink: 0;

  :global(.dark) & {
    background: var(--dt-bg-hover-dark);
    color: var(--dt-text-tertiary);
  }
}

.suggestion-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.suggestion-text {
  font-size: 14px;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  :deep(mark) {
    background: var(--dt-brand-light);
    color: var(--dt-brand-color);
    font-weight: 600;
    padding: 0 2px;
    border-radius: var(--dt-radius-sm);
  }

  :global(.dark) & {
    color: var(--dt-text-primary-dark);
  }
}

.suggestion-desc {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  :global(.dark) & {
    color: var(--dt-text-tertiary);
  }
}

.suggestion-type {
  font-size: 11px;
  padding: 2px 8px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-secondary);
  flex-shrink: 0;

  :global(.dark) & {
    background: var(--dt-bg-hover-dark);
    color: var(--dt-text-tertiary);
  }
}

.selected-icon {
  font-size: 16px;
  color: var(--dt-brand-color);
  flex-shrink: 0;
}

// 下拉底部
.dropdown-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--dt-border-color);
  background: var(--dt-bg-hover);

  :global(.dark) & {
    border-color: var(--dt-border-dark);
    background: var(--dt-bg-card-dark);
  }
}

// 快捷操作栏
.quick-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.quick-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: var(--dt-bg-hover);
  border: none;
  border-radius: var(--dt-radius-xl);
  font-size: 13px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: $transition;

  .el-icon {
    font-size: 14px;
  }

  &:hover {
    background: var(--dt-border-light);
    color: var(--dt-text-primary);
  }

  &.is-active {
    background: var(--dt-brand-light);
    color: var(--dt-brand-color);
    font-weight: 500;
  }

  :global(.dark) & {
    background: var(--dt-bg-hover-dark);
    color: var(--dt-text-tertiary);

    &:hover {
      background: var(--dt-bg-hover-dark);
      color: var(--dt-text-secondary);
    }

    &.is-active {
      background: var(--dt-brand-light);
      color: var(--dt-brand-color);
    }
  }
}
</style>
