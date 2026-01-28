<template>
  <div class="search-box" :class="{ 'is-expanded': expanded }">
    <el-input
      ref="searchInput"
      v-model="searchQuery"
      :placeholder="placeholder"
      :prefix-icon="Search"
      clearable
      :size="size"
      @input="handleInput"
      @focus="handleFocus"
      @blur="handleBlur"
    >
      <template v-if="$slots.prefix" #prefix>
        <slot name="prefix" />
      </template>
      <template v-if="$slots.suffix" #suffix>
        <slot name="suffix" />
      </template>
    </el-input>
    
    <!-- 搜索建议 -->
    <div v-if="showSuggestions && suggestions.length > 0" class="search-suggestions">
      <div
        v-for="(item, index) in suggestions"
        :key="index"
        class="suggestion-item"
        :class="{ active: selectedIndex === index }"
        @click="selectSuggestion(item)"
        @mouseenter="selectedIndex = index"
      >
        <el-icon v-if="item.icon" class="suggestion-icon">
          <component :is="item.icon" />
        </el-icon>
        <span class="suggestion-text">{{ item.text }}</span>
        <span v-if="item.type" class="suggestion-type">{{ item.type }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  // 占位符
  placeholder: {
    type: String,
    default: '搜索...'
  },
  // 尺寸
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  },
  // 是否显示搜索建议
  showSuggestions: {
    type: Boolean,
    default: false
  },
  // 建议列表
  suggestions: {
    type: Array,
    default: () => []
  },
  // 防抖延迟
  debounce: {
    type: Number,
    default: 300
  },
  // 是否获得焦点
  autofocus: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['search', 'select', 'focus', 'blur'])

const searchInput = ref(null)
const searchQuery = ref('')
const expanded = ref(false)
const selectedIndex = ref(-1)
let debounceTimer = null

// 处理输入
const handleInput = () => {
  selectedIndex.value = -1
  emit('search', searchQuery.value)
}

// 防抖搜索
const handleDebounceSearch = () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    emit('search', searchQuery.value)
  }, props.debounce)
}

// 处理焦点
const handleFocus = () => {
  expanded.value = true
  emit('focus', searchQuery.value)
}

const handleBlur = () => {
  // 延迟收起，以便点击建议项
  setTimeout(() => {
    expanded.value = false
    selectedIndex.value = -1
  }, 200)
  emit('blur')
}

// 选择建议项
const selectSuggestion = (item) => {
  searchQuery.value = item.text
  expanded.value = false
  selectedIndex.value = -1
  emit('select', item)
}

// 键盘导航
const handleKeydown = (event) => {
  if (!props.showSuggestions || props.suggestions.length === 0) return
  
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      selectedIndex.value = (selectedIndex.value + 1) % props.suggestions.length
      break
    case 'ArrowUp':
      event.preventDefault()
      selectedIndex.value = selectedIndex.value <= 0 
        ? props.suggestions.length - 1 
        : selectedIndex.value - 1
      break
    case 'Enter':
      if (selectedIndex.value >= 0) {
        event.preventDefault()
        selectSuggestion(props.suggestions[selectedIndex.value])
      }
      break
    case 'Escape':
      expanded.value = false
      selectedIndex.value = -1
      searchInput.value?.blur()
      break
  }
}

// 监听键盘事件
onMounted(() => {
  if (props.autofocus) {
    nextTick(() => {
      searchInput.value?.focus()
    })
  }
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  clearTimeout(debounceTimer)
})

// 暴露方法
defineExpose({
  focus: () => searchInput.value?.focus(),
  blur: () => searchInput.value?.blur(),
  clear: () => {
    searchQuery.value = ''
    expanded.value = false
    selectedIndex.value = -1
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/design-tokens' as *;

.search-box {
  position: relative;
  width: 100%;
  
  .el-input {
    :deep(.el-input__wrapper) {
      border-radius: $border-radius-lg;
      box-shadow: $shadow-sm;
      transition: all $transition-duration-normal $ease-in-out;
      background: var(--el-fill-color-blank);
      
      &:hover {
        box-shadow: $shadow-md;
        border-color: var(--el-border-color);
      }
      
      &.is-focus {
        box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
        border-color: var(--el-color-primary);
      }
    }
    
    :deep(.el-input__inner) {
      font-size: 14px;
      padding: 0 12px;
      
      &::placeholder {
        color: var(--el-text-color-placeholder);
      }
    }
    
    &.is-large {
      :deep(.el-input__inner) {
        font-size: 16px;
        padding: 0 16px;
      }
    }
    
    &.is-small {
      :deep(.el-input__inner) {
        font-size: 12px;
        padding: 0 8px;
      }
    }
  }
  
  // 搜索建议
  .search-suggestions {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-light);
    border-radius: $border-radius-lg;
    box-shadow: $shadow-lg;
    z-index: 1000;
    max-height: 300px;
    overflow-y: auto;
    margin-top: 4px;
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: var(--el-border-color);
      border-radius: 3px;
      
      &:hover {
        background: var(--el-border-color-darker);
      }
    }
  }
  
  .suggestion-item {
    display: flex;
    align-items: center;
    padding: $spacing-sm $spacing-md;
    cursor: pointer;
    transition: all $transition-duration-fast $ease-in-out;
    
    &:hover,
    &.active {
      background: var(--el-color-primary-light-9);
    }
    
    .suggestion-icon {
      margin-right: $spacing-sm;
      color: var(--el-text-color-secondary);
      font-size: 16px;
    }
    
    .suggestion-text {
      flex: 1;
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .suggestion-type {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      background: var(--el-fill-color-light);
      padding: 2px 8px;
      border-radius: $border-radius-sm;
      margin-left: $spacing-sm;
    }
  }
}

// 响应式适配
@include mobile {
  .search-box {
    .el-input {
      :deep(.el-input__inner) {
        font-size: 16px; // 防止iOS缩放
      }
    }
    
    .search-suggestions {
      max-height: 250px;
    }
  }
}
</style>