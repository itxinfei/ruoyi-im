<template>
  <div class="ding-input" :class="{ 'is-disabled': disabled }">
    <textarea
      ref="textareaRef"
      class="ding-textarea"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :maxlength="maxlength"
      @input="handleInput"
      @keydown="handleKeydown"
      @focus="handleFocus"
      @blur="handleBlur"
    ></textarea>

    <!-- 字数统计 -->
    <span v-if="showCount && modelValue.length > 0" class="ding-input-count" :class="{ 'is-near-limit': isNearLimit }">
      {{ currentLength }}/{{ maxlength }}
    </span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

/**
 * DingInput - 钉钉5.6风格输入框组件
 * 使用原生textarea实现，完全可控的样式
 */
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '输入消息...'
  },
  maxlength: {
    type: Number,
    default: 1000
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showCount: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'keydown', 'focus', 'blur'])

const textareaRef = ref(null)

// 当前输入长度
const currentLength = computed(() => props.modelValue?.length || 0)

// 是否接近字数限制（90%）
const isNearLimit = computed(() => {
  return currentLength.value > props.maxlength * 0.9
})

// 处理输入事件
const handleInput = (e) => {
  emit('update:modelValue', e.target.value)
}

// 处理键盘事件
const handleKeydown = (e) => {
  emit('keydown', e)
}

// 处理获得焦点
const handleFocus = (e) => {
  emit('focus', e)
}

// 处理失去焦点
const handleBlur = (e) => {
  emit('blur', e)
}

// 聚焦方法
const focus = () => {
  textareaRef.value?.focus()
}

// 失焦方法
const blur = () => {
  textareaRef.value?.blur()
}

// 暴露方法供父组件调用
defineExpose({ focus, blur })
</script>

<style lang="scss" scoped>
.ding-input {
  position: relative;
  width: 100%;

  &.is-disabled {
    opacity: 0.6;
    cursor: not-allowed;

    .ding-textarea {
      cursor: not-allowed;
    }
  }
}

.ding-textarea {
  display: block;
  width: 100%;
  min-height: 200px;
  max-height: 500px;
  border: none;
  outline: none;
  background: transparent;
  padding: 16px 0;
  margin: 0;
  line-height: 1.8;
  font-size: 14px;
  color: #1A1A1A;
  resize: vertical;
  font-family: inherit;
  box-sizing: border-box;

  &::placeholder {
    color: #B8B8B8;
  }

  &:disabled {
    cursor: not-allowed;
  }

  // 自定义滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #E6E6E6;
    border-radius: 3px;

    &:hover {
      background: #C8C8C8;
    }
  }

  // Firefox 滚动条
  scrollbar-width: thin;
  scrollbar-color: #E6E6E6 transparent;
}

.ding-input-count {
  position: absolute;
  bottom: 12px;
  left: 12px;
  font-size: 12px;
  color: #B8B8B8;
  background: rgba(255, 255, 255, 0.95);
  padding: 4px 8px;
  border-radius: 4px;
  pointer-events: none;
  transition: all 0.2s ease;

  &.is-near-limit {
    color: #F5222D;
    background: rgba(245, 34, 45, 0.08);
    font-weight: 500;
  }
}
</style>
