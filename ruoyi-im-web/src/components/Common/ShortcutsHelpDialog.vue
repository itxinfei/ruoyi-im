<template>
  <el-dialog
    v-model="visible"
    title="键盘快捷键"
    :width="520"
    :modal="true"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    class="shortcuts-help-dialog"
  >
    <div class="shortcuts-container">
      <div
        v-for="(group, index) in shortcutGroups"
        :key="index"
        class="shortcut-group"
      >
        <h4 class="group-title">
          {{ group.title }}
        </h4>
        <div class="shortcuts-list">
          <div
            v-for="item in group.shortcuts"
            :key="item.key"
            class="shortcut-item"
          >
            <span class="shortcut-description">{{ item.description }}</span>
            <div class="shortcut-keys">
              <kbd
                v-for="(key, keyIndex) in item.keys"
                :key="keyIndex"
                class="key"
              >{{ key }}</kbd>
            </div>
          </div>
        </div>
      </div>

      <div class="tip-section">
        <span class="material-icons-outlined tip-icon">info</span>
        <span class="tip-text">快捷键仅在非输入状态下生效（部分除外）</span>
      </div>
    </div>

    <template #footer>
      <el-button
        type="primary"
        @click="visible = false"
      >
        知道了
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ShortcutKeys, ShortcutDescriptions, getShortcutLabel, useKeyboardShortcuts } from '@/composables/useKeyboardShortcuts'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

/**
 * 快捷键分组
 */
const shortcutGroups = ref([
  {
    title: '搜索与导航',
    shortcuts: [
      { key: ShortcutKeys.SEARCH, description: ShortcutDescriptions[ShortcutKeys.SEARCH] },
      { key: ShortcutKeys.FIND_IN_CHAT, description: ShortcutDescriptions[ShortcutKeys.FIND_IN_CHAT] },
      { key: ShortcutKeys.NEXT_SESSION, description: ShortcutDescriptions[ShortcutKeys.NEXT_SESSION] },
      { key: ShortcutKeys.PREV_SESSION, description: ShortcutDescriptions[ShortcutKeys.PREV_SESSION] }
    ]
  },
  {
    title: '消息操作',
    shortcuts: [
      { key: ShortcutKeys.SEND_MESSAGE, description: ShortcutDescriptions[ShortcutKeys.SEND_MESSAGE] },
      { key: ShortcutKeys.SEND_MESSAGE_ALT, description: ShortcutDescriptions[ShortcutKeys.SEND_MESSAGE_ALT], allowInInput: true },
      { key: ShortcutKeys.SELECT_ALL, description: ShortcutDescriptions[ShortcutKeys.SELECT_ALL] },
      { key: ShortcutKeys.DELETE, description: ShortcutDescriptions[ShortcutKeys.DELETE] },
      { key: ShortcutKeys.FORWARD, description: ShortcutDescriptions[ShortcutKeys.FORWARD] }
    ]
  },
  {
    title: '创建与操作',
    shortcuts: [
      { key: ShortcutKeys.NEW_CHAT, description: ShortcutDescriptions[ShortcutKeys.NEW_CHAT] },
      { key: ShortcutKeys.NEW_GROUP, description: ShortcutDescriptions[ShortcutKeys.NEW_GROUP] },
      { key: ShortcutKeys.SCREENSHOT, description: ShortcutDescriptions[ShortcutKeys.SCREENSHOT] }
    ]
  },
  {
    title: '其他',
    shortcuts: [
      { key: ShortcutKeys.CLOSE, description: ShortcutDescriptions[ShortcutKeys.CLOSE] },
      { key: ShortcutKeys.SHOW_SHORTCUTS, description: ShortcutDescriptions[ShortcutKeys.SHOW_SHORTCUTS] }
    ]
  }
])

// 处理快捷键显示
shortcutGroups.value.forEach(group => {
  group.shortcuts.forEach(item => {
    item.keys = formatKeys(item.key)
    item.allowInInput = item.allowInInput || false
  })
})

/**
 * 格式化快捷键按键显示
 * @param {string} shortcut - 快捷键字符串
 * @returns {Array} 按键数组
 */
function formatKeys(shortcut) {
  const label = getShortcutLabel(shortcut)
  // 将组合键拆分成单独的按键
  if (label.includes('+')) {
    return label.split('+')
  }
  // 处理 macOS 风格的组合键
  if (/[⌘⌥⇧]/.test(label)) {
    const keys = []
    let remaining = label
    const symbols = { '⌘': 'Cmd', '⌥': 'Alt', '⇧': 'Shift' }
    for (const [symbol, name] of Object.entries(symbols)) {
      if (remaining.includes(symbol)) {
        keys.push(name)
        remaining = remaining.replace(symbol, '')
      }
    }
    if (remaining) {
      keys.push(remaining)
    }
    return keys
  }
  return [label]
}

// 注册快捷键打开帮助对话框
const { registerShortcut } = useKeyboardShortcuts()

registerShortcut(ShortcutKeys.SHOW_SHORTCUTS, () => {
  visible.value = true
}, {
  description: '显示快捷键帮助',
  allowInInput: true
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.shortcuts-container {
  padding: 8px 0;
}

.shortcut-group {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.group-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--dt-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.shortcuts-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.shortcut-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-tertiary);
  }
}

.shortcut-description {
  font-size: 14px;
  color: var(--dt-text-primary);
}

.shortcut-keys {
  display: flex;
  align-items: center;
  gap: 4px;
}

.key {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  background: linear-gradient(180deg, #ffffff 0%, #f5f5f5 100%);
  border: 1px solid #d4d4d4;
  border-radius: var(--dt-radius-md);
  box-shadow: 0 2px 0 #a0a0a0, 0 3px 1px rgba(0, 0, 0, 0.1);
  font-size: 12px;
  font-weight: 600;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  color: var(--dt-text-primary);
  user-select: none;
}

.tip-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  margin-top: 16px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-md);
  color: var(--dt-brand-color);
  font-size: 13px;

  .tip-icon {
    font-size: 18px;
    flex-shrink: 0;
  }

  .tip-text {
    flex: 1;
  }
}

// 暗色模式
.dark {
  .key {
    background: linear-gradient(180deg, #3a3a3a 0%, #2a2a2a 100%);
    border-color: #555;
    box-shadow: 0 2px 0 #000, 0 3px 1px rgba(0, 0, 0, 0.3);
    color: #e0e0e0;
  }

  .tip-section {
    background: rgba(0, 137, 255, 0.15);
  }
}

// 对话框样式调整
:deep(.shortcuts-help-dialog) {
  .el-dialog__header {
    padding: 20px 20px 16px;
  }

  .el-dialog__body {
    padding: 0 20px 20px;
    max-height: 60vh;
    overflow-y: auto;
  }

  .el-dialog__footer {
    padding: 16px 20px;
    border-top: 1px solid var(--dt-border-lighter);
  }
}

// 响应式
@media (max-width: 600px) {
  .shortcut-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .shortcut-keys {
    align-self: flex-end;
  }
}
</style>
