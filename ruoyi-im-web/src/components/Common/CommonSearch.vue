<template>
  <div
    class="common-search"
    :class="{ focused: isFocused, empty: !query }"
  >
    <div class="search-input-container">
      <div class="search-icon">
        <i class="el-icon-search" />
      </div>
      <input
        ref="inputRef"
        v-model="query"
        class="search-input"
        :placeholder="placeholder"
        :autofocus="autofocus"
        @focus="handleFocus"
        @blur="handleBlur"
        @keyup.enter="handleSearch"
        @keyup.esc="handleClear"
      >
      <div
        v-if="query"
        class="clear-icon"
        @click="handleClear"
      >
        <i class="el-icon-circle-close" />
      </div>
    </div>

    <div
      v-if="showFilter && filters.length > 0"
      class="search-filters"
    >
      <select
        v-model="selectedFilter"
        class="filter-select"
      >
        <option
          v-for="filter in filters"
          :key="filter.value"
          :value="filter.value"
        >
          {{ filter.label }}
        </option>
      </select>
    </div>

    <div
      v-if="showButton"
      class="search-button"
    >
      <button
        :disabled="!query"
        @click="handleSearch"
      >
        {{ buttonText }}
      </button>
    </div>

    <div
      v-if="showSuggestions && suggestions.length > 0"
      class="search-suggestions"
    >
      <div
        v-for="(suggestion, index) in suggestions"
        :key="index"
        class="suggestion-item"
        :class="{ highlighted: index === highlightedIndex }"
        @click="selectSuggestion(suggestion)"
        @mouseenter="highlightIndex = index"
      >
        <span
          v-html="highlightMatch(suggestion.text, query)"
        />
        <span
          v-if="suggestion.type"
          class="suggestion-type"
        >
          {{ suggestion.type }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '搜索...'
  },
  autofocus: {
    type: Boolean,
    default: false
  },
  showFilter: {
    type: Boolean,
    default: false
  },
  filters: {
    type: Array,
    default: () => []
  },
  showButton: {
    type: Boolean,
    default: false
  },
  buttonText: {
    type: String,
    default: '搜索'
  },
  showSuggestions: {
    type: Boolean,
    default: false
  },
  suggestions: {
    type: Array,
    default: () => []
  },
  debounce: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'clear', 'select-suggestion', 'filter-change'])

const query = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const inputRef = ref(null)
const isFocused = ref(false)
const selectedFilter = ref('')
const highlightedIndex = ref(-1)
let debounceTimer = null

// 监听筛选器变化
watch(selectedFilter, newVal => {
  emit('filter-change', newVal)
})

// 方法
const handleFocus = () => {
  isFocused.value = true
  highlightedIndex.value = -1
}

const handleBlur = () => {
  isFocused.value = false
  // 延迟隐藏建议，以便点击建议项时不会消失
  setTimeout(() => {
    highlightedIndex.value = -1
  }, 200)
}

const handleSearch = () => {
  if (query.value.trim()) {
    emit('search', { query: query.value, filter: selectedFilter.value })
  }
}

const handleClear = () => {
  query.value = ''
  emit('clear')
  highlightedIndex.value = -1
  inputRef.value?.focus()
}

const selectSuggestion = suggestion => {
  query.value = suggestion.text
  emit('select-suggestion', suggestion)
  emit('search', { query: query.value, filter: selectedFilter.value })
  highlightedIndex.value = -1
}

const highlightMatch = (text, searchTerm) => {
  if (!searchTerm) {return text}
  const regex = new RegExp(`(${searchTerm})`, 'gi')
  return text.replace(regex, '<strong>$1</strong>')
}

// 键盘导航
const handleKeyDown = e => {
  if (!props.showSuggestions || props.suggestions.length === 0) {return}

  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      highlightedIndex.value = Math.min(highlightedIndex.value + 1, props.suggestions.length - 1)
      break
    case 'ArrowUp':
      e.preventDefault()
      highlightedIndex.value = Math.max(highlightedIndex.value - 1, -1)
      break
    case 'Enter':
      e.preventDefault()
      if (highlightedIndex.value >= 0) {
        selectSuggestion(props.suggestions[highlightedIndex.value])
      } else {
        handleSearch()
      }
      break
    case 'Escape':
      e.preventDefault()
      handleClear()
      break
  }
}

// 监听键盘事件
watch(query, () => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  debounceTimer = setTimeout(() => {
    if (query.value) {
      emit('update:modelValue', query.value)
    }
  }, props.debounce)
})

// 为组件暴露焦点方法
defineExpose({
  focus: () => {
    inputRef.value?.focus()
  },
  blur: () => {
    inputRef.value?.blur()
  }
})
</script>

<style scoped>
.common-search {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 500px;
}

.search-input-container {
  display: flex;
  align-items: center;
  flex: 1;
  position: relative;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 6px;
  transition: all 0.2s ease;
}

.common-search.focused .search-input-container {
  border-color: var(--dt-brand-color);
  box-shadow: 0 0 0 2px var(--dt-brand-lighter);
}

.search-input-container:hover:not(.focused) {
  border-color: var(--dt-border-secondary);
}

.search-icon {
  padding: 0 12px;
  color: var(--dt-text-tertiary);
  font-size: 16px;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 10px 4px;
  background: transparent;
  color: var(--dt-text-primary);
  font-size: 14px;
}

.search-input::placeholder {
  color: var(--dt-text-quaternary);
}

.clear-icon {
  padding: 0 12px;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  border-radius: 4px;
  transition: color 0.2s;
}

.clear-icon:hover {
  color: var(--dt-text-secondary);
  background: var(--dt-bg-hover);
}

.search-filters {
  margin-left: 8px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid var(--dt-border-light);
  border-radius: 4px;
  background: var(--dt-bg-card);
  color: var(--dt-text-primary);
  font-size: 14px;
}

.search-button {
  margin-left: 8px;
}

.search-button button {
  padding: 8px 16px;
  background: var(--dt-brand-color);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.search-button button:hover:not(:disabled) {
  background: var(--dt-brand-hover);
}

.search-button button:disabled {
  background: var(--dt-border-light);
  cursor: not-allowed;
}

.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: 240px;
  overflow-y: auto;
  z-index: 1000;
  margin-top: 4px;
}

.suggestion-item {
  padding: 10px 16px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.2s;
}

.suggestion-item:hover,
.suggestion-item.highlighted {
  background: var(--dt-brand-bg);
}

.suggestion-item strong {
  color: var(--dt-brand-color);
  font-weight: 600;
}

.suggestion-type {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  background: var(--dt-bg-subtle);
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 8px;
}

/* 滚动条样式 */
.search-suggestions::-webkit-scrollbar {
  width: 6px;
}

.search-suggestions::-webkit-scrollbar-track {
  background: transparent;
}

.search-suggestions::-webkit-scrollbar-thumb {
  background: var(--dt-scrollbar-thumb);
  border-radius: 3px;
}

.search-suggestions::-webkit-scrollbar-thumb:hover {
  background: var(--dt-scrollbar-thumb-hover);
}

/* 暗色模式 */
:global(.dark) {
  .search-input-container {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .search-input {
    color: var(--dt-text-primary-dark);
  }

  .search-input::placeholder {
    color: var(--dt-text-quaternary-dark);
  }

  .search-icon,
  .clear-icon {
    color: var(--dt-text-tertiary-dark);
  }

  .clear-icon:hover {
    background: var(--dt-bg-hover-dark);
  }

  .filter-select {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    color: var(--dt-text-primary-dark);
  }

  .search-suggestions {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }

  .suggestion-type {
    background: var(--dt-bg-subtle-dark);
    color: var(--dt-text-tertiary-dark);
  }

  .suggestion-item:hover,
  .suggestion-item.highlighted {
    background: var(--dt-brand-bg);
  }

  .search-suggestions::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb-dark);
  }

  .search-suggestions::-webkit-scrollbar-thumb:hover {
    background: var(--dt-scrollbar-thumb-dark-hover);
  }
}
</style>