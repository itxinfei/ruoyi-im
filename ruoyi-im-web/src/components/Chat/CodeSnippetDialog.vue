<template>
  <el-dialog
    v-model="visible"
    title="发送代码片段"
    width="700px"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    class="code-snippet-dialog"
    @close="handleClose"
  >
    <!-- 代码编辑区域 -->
    <div class="code-editor-container">
      <!-- 工具栏 -->
      <div class="editor-toolbar">
        <!-- 语言选择 -->
        <div class="toolbar-left">
          <el-select
            v-model="selectedLanguage"
            placeholder="选择语言"
            size="small"
            class="language-select"
            filterable
          >
            <el-option
              v-for="lang in languageOptions"
              :key="lang.value"
              :label="lang.label"
              :value="lang.value"
            >
              <span class="lang-option">
                <span class="lang-icon">{{ lang.icon }}</span>
                <span class="lang-name">{{ lang.label }}</span>
              </span>
            </el-option>
          </el-select>

          <!-- 文件名输入 -->
          <el-input
            v-model="fileName"
            placeholder="文件名（可选）"
            size="small"
            class="filename-input"
            clearable
          >
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="toolbar-right">
          <!-- 格式化按钮 -->
          <el-tooltip content="格式化代码" placement="top">
            <el-button :icon="MagicStick" size="small" circle @click="formatCode" />
          </el-tooltip>

          <!-- 清空按钮 -->
          <el-tooltip content="清空代码" placement="top">
            <el-button :icon="Delete" size="small" circle @click="clearCode" />
          </el-tooltip>

          <!-- 复制按钮 -->
          <el-tooltip content="复制代码" placement="top">
            <el-button :icon="DocumentCopy" size="small" circle @click="copyCode" />
          </el-tooltip>
        </div>
      </div>

      <!-- 代码输入区 -->
      <div class="code-input-wrapper">
        <!-- 行号 -->
        <div ref="lineNumbersRef" class="line-numbers">
          <div
            v-for="line in lineCount"
            :key="line"
            class="line-number"
            :class="{ current: currentLine === line }"
          >
            {{ line }}
          </div>
        </div>

        <!-- 代码文本框 -->
        <textarea
          ref="codeTextarea"
          v-model="codeContent"
          class="code-textarea"
          :placeholder="codePlaceholder"
          spellcheck="false"
          @input="handleCodeInput"
          @scroll="syncScroll"
          @keydown="handleKeyDown"
          @click="updateCurrentLine"
          @keyup="updateCurrentLine"
        ></textarea>
      </div>

      <!-- 代码统计信息 -->
      <div class="code-stats">
        <span class="stat-item">
          <el-icon><Document /></el-icon>
          <span>{{ lineCount }} 行</span>
        </span>
        <span class="stat-item">
          <el-icon><EditPen /></el-icon>
          <span>{{ charCount }} 字符</span>
        </span>
        <span v-if="selectedLanguage" class="stat-item language-tag">
          <span class="tag-dot"></span>
          <span>{{ getLanguageLabel(selectedLanguage) }}</span>
        </span>
      </div>
    </div>

    <!-- 预览区域 -->
    <div v-if="showPreview && codeContent" class="code-preview">
      <div class="preview-header">
        <div class="preview-title">
          <el-icon><View /></el-icon>
          <span>代码预览</span>
        </div>
        <el-button type="primary" link size="small" @click="showPreview = false">
          隐藏预览
        </el-button>
      </div>
      <div class="preview-content">
        <pre
          class="code-block"
          :class="`language-${selectedLanguage}`"
        ><code>{{ codeContent }}</code></pre>
      </div>
    </div>

    <!-- 描述说明 -->
    <div class="description-section">
      <el-input
        v-model="description"
        type="textarea"
        :rows="2"
        placeholder="添加代码说明（可选）..."
        maxlength="200"
        show-word-limit
      />
    </div>

    <!-- 底部操作 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="!showPreview && codeContent" link @click="showPreview = true">
          显示预览
        </el-button>
        <div class="footer-actions">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" :disabled="!canSend" @click="handleSend"> 发送代码 </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * @file CodeSnippetDialog.vue
 * @description 代码片段发送对话框组件 - 用于编辑和发送代码片段消息
 * @author IM System
 * @version 1.0.0
 */

import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, DocumentCopy, Delete, MagicStick, View, EditPen } from '@element-plus/icons-vue'

// ==================== Props 定义 ====================
const props = defineProps({
  /**
   * 控制对话框显示状态
   */
  modelValue: {
    type: Boolean,
    default: false,
  },
  /**
   * 会话ID
   */
  sessionId: {
    type: String,
    default: '',
  },
  /**
   * 初始代码内容
   */
  initialCode: {
    type: String,
    default: '',
  },
  /**
   * 初始语言
   */
  initialLanguage: {
    type: String,
    default: 'javascript',
  },
})

// ==================== Emits 定义 ====================
const emit = defineEmits([
  'update:modelValue',
  'send', // 发送代码片段时触发
])

// ==================== 常量定义 ====================

/**
 * 支持的编程语言列表
 */
const languageOptions = [
  { value: 'javascript', label: 'JavaScript', icon: '📜' },
  { value: 'typescript', label: 'TypeScript', icon: '📘' },
  { value: 'python', label: 'Python', icon: '🐍' },
  { value: 'java', label: 'Java', icon: '☕' },
  { value: 'csharp', label: 'C#', icon: '🎯' },
  { value: 'cpp', label: 'C++', icon: '⚙️' },
  { value: 'c', label: 'C', icon: '🔧' },
  { value: 'go', label: 'Go', icon: '🐹' },
  { value: 'rust', label: 'Rust', icon: '🦀' },
  { value: 'php', label: 'PHP', icon: '🐘' },
  { value: 'ruby', label: 'Ruby', icon: '💎' },
  { value: 'swift', label: 'Swift', icon: '🍎' },
  { value: 'kotlin', label: 'Kotlin', icon: '🎨' },
  { value: 'scala', label: 'Scala', icon: '🔴' },
  { value: 'html', label: 'HTML', icon: '🌐' },
  { value: 'css', label: 'CSS', icon: '🎨' },
  { value: 'scss', label: 'SCSS', icon: '💅' },
  { value: 'less', label: 'Less', icon: '💄' },
  { value: 'vue', label: 'Vue', icon: '💚' },
  { value: 'react', label: 'React/JSX', icon: '⚛️' },
  { value: 'sql', label: 'SQL', icon: '🗃️' },
  { value: 'shell', label: 'Shell/Bash', icon: '🖥️' },
  { value: 'powershell', label: 'PowerShell', icon: '💻' },
  { value: 'yaml', label: 'YAML', icon: '📄' },
  { value: 'json', label: 'JSON', icon: '📋' },
  { value: 'xml', label: 'XML', icon: '📰' },
  { value: 'markdown', label: 'Markdown', icon: '📝' },
  { value: 'dockerfile', label: 'Dockerfile', icon: '🐳' },
  { value: 'nginx', label: 'Nginx', icon: '🌿' },
  { value: 'plaintext', label: '纯文本', icon: '📃' },
]

/**
 * 不同语言的占位符示例
 */
const placeholderExamples = {
  javascript:
    '// 在这里输入 JavaScript 代码\nfunction hello() {\n  console.log("Hello World!");\n}',
  typescript:
    '// 在这里输入 TypeScript 代码\nfunction greet(name: string): void {\n  console.log(`Hello, ${name}!`);\n}',
  python: '# 在这里输入 Python 代码\ndef hello():\n    print("Hello World!")',
  java: '// 在这里输入 Java 代码\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello World!");\n    }\n}',
  sql: '-- 在这里输入 SQL 语句\nSELECT * FROM users WHERE status = 1;',
  shell: '#!/bin/bash\n# 在这里输入 Shell 脚本\necho "Hello World!"',
  default: '// 在这里输入代码...',
}

// ==================== 响应式状态 ====================

/** 对话框显示状态 */
const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val),
})

/** 选中的编程语言 */
const selectedLanguage = ref(props.initialLanguage)

/** 文件名 */
const fileName = ref('')

/** 代码内容 */
const codeContent = ref(props.initialCode)

/** 代码描述 */
const description = ref('')

/** 是否显示预览 */
const showPreview = ref(false)

/** 当前光标所在行 */
const currentLine = ref(1)

/** DOM 引用 */
const codeTextarea = ref(null)
const lineNumbersRef = ref(null)

// ==================== 计算属性 ====================

/**
 * 代码行数
 */
const lineCount = computed(() => {
  if (!codeContent.value) return 1
  return codeContent.value.split('\n').length
})

/**
 * 字符数
 */
const charCount = computed(() => {
  return codeContent.value.length
})

/**
 * 是否可以发送
 */
const canSend = computed(() => {
  return codeContent.value.trim().length > 0
})

/**
 * 代码占位符
 */
const codePlaceholder = computed(() => {
  return placeholderExamples[selectedLanguage.value] || placeholderExamples.default
})

// ==================== 方法定义 ====================

/**
 * 获取语言显示名称
 * @param {string} value - 语言值
 * @returns {string} 语言名称
 */
const getLanguageLabel = value => {
  const lang = languageOptions.find(l => l.value === value)
  return lang ? lang.label : value
}

/**
 * 处理代码输入
 */
const handleCodeInput = () => {
  updateCurrentLine()
}

/**
 * 更新当前行号
 */
const updateCurrentLine = () => {
  if (!codeTextarea.value) return

  const textarea = codeTextarea.value
  const text = textarea.value.substring(0, textarea.selectionStart)
  currentLine.value = text.split('\n').length
}

/**
 * 同步滚动行号和代码区域
 */
const syncScroll = () => {
  if (!lineNumbersRef.value || !codeTextarea.value) return
  lineNumbersRef.value.scrollTop = codeTextarea.value.scrollTop
}

/**
 * 处理键盘事件
 * @param {KeyboardEvent} event - 键盘事件
 */
const handleKeyDown = event => {
  // Tab 键插入空格
  if (event.key === 'Tab') {
    event.preventDefault()
    const textarea = codeTextarea.value
    const start = textarea.selectionStart
    const end = textarea.selectionEnd

    const spaces = '  ' // 2个空格
    codeContent.value =
      codeContent.value.substring(0, start) + spaces + codeContent.value.substring(end)

    // 恢复光标位置
    nextTick(() => {
      textarea.selectionStart = textarea.selectionEnd = start + spaces.length
    })
  }

  // Ctrl+A 全选
  if (event.ctrlKey && event.key === 'a') {
    // 默认行为
  }

  // Ctrl+D 复制当前行
  if (event.ctrlKey && event.key === 'd') {
    event.preventDefault()
    duplicateLine()
  }
}

/**
 * 复制当前行
 */
const duplicateLine = () => {
  const textarea = codeTextarea.value
  if (!textarea) return

  const lines = codeContent.value.split('\n')
  const currentLineIndex = currentLine.value - 1

  if (currentLineIndex >= 0 && currentLineIndex < lines.length) {
    const lineToDuplicate = lines[currentLineIndex]
    lines.splice(currentLineIndex + 1, 0, lineToDuplicate)
    codeContent.value = lines.join('\n')
  }
}

/**
 * 格式化代码（简单的缩进调整）
 */
const formatCode = () => {
  if (!codeContent.value.trim()) {
    ElMessage.warning('没有代码需要格式化')
    return
  }

  try {
    // 对于 JSON，尝试格式化
    if (selectedLanguage.value === 'json') {
      const parsed = JSON.parse(codeContent.value)
      codeContent.value = JSON.stringify(parsed, null, 2)
      ElMessage.success('JSON 格式化成功')
      return
    }

    // 其他语言的简单格式化（去除多余空行）
    const lines = codeContent.value.split('\n')
    const formatted = lines
      .map(line => line.trimEnd()) // 去除行尾空格
      .join('\n')
      .replace(/\n{3,}/g, '\n\n') // 最多保留一个空行

    codeContent.value = formatted
    ElMessage.success('代码已整理')
  } catch (error) {
    ElMessage.warning('格式化失败，请检查代码语法')
  }
}

/**
 * 清空代码
 */
const clearCode = () => {
  if (!codeContent.value) return

  codeContent.value = ''
  fileName.value = ''
  description.value = ''
  currentLine.value = 1
  ElMessage.info('代码已清空')
}

/**
 * 复制代码到剪贴板
 */
const copyCode = async () => {
  if (!codeContent.value) {
    ElMessage.warning('没有代码可复制')
    return
  }

  try {
    await navigator.clipboard.writeText(codeContent.value)
    ElMessage.success('代码已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = codeContent.value
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('代码已复制到剪贴板')
  }
}

/**
 * 发送代码片段
 */
const handleSend = () => {
  if (!canSend.value) {
    ElMessage.warning('请输入代码内容')
    return
  }

  // 构建代码片段消息数据
  const codeData = {
    type: 'code',
    language: selectedLanguage.value,
    languageLabel: getLanguageLabel(selectedLanguage.value),
    content: codeContent.value,
    fileName: fileName.value.trim() || null,
    description: description.value.trim() || null,
    lineCount: lineCount.value,
    charCount: charCount.value,
    createdAt: new Date().toISOString(),
  }

  emit('send', codeData)
  ElMessage.success('代码片段发送成功')
  handleClose()
}

/**
 * 关闭对话框
 */
const handleClose = () => {
  visible.value = false
  resetForm()
}

/**
 * 重置表单
 */
const resetForm = () => {
  codeContent.value = ''
  fileName.value = ''
  description.value = ''
  selectedLanguage.value = 'javascript'
  showPreview.value = false
  currentLine.value = 1
}

// ==================== 监听器 ====================

/**
 * 监听对话框打开，聚焦到代码输入框
 */
watch(visible, newVal => {
  if (newVal) {
    nextTick(() => {
      codeTextarea.value?.focus()
    })
  } else {
    resetForm()
  }
})

/**
 * 监听初始代码变化
 */
watch(
  () => props.initialCode,
  newVal => {
    if (newVal) {
      codeContent.value = newVal
    }
  }
)

/**
 * 监听初始语言变化
 */
watch(
  () => props.initialLanguage,
  newVal => {
    if (newVal) {
      selectedLanguage.value = newVal
    }
  }
)
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.code-snippet-dialog {
  :deep(.el-dialog__body) {
    padding: $spacing-lg $spacing-xl;
    max-height: 70vh;
    overflow-y: auto;
  }
}

// 代码编辑器容器
.code-editor-container {
  border: 1px solid $border-base;
  border-radius: $border-radius-base;
  overflow: hidden;
  background-color: #1e1e1e; // VS Code 深色背景

  // 工具栏
  .editor-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-sm $spacing-md;
    background-color: #252526;
    border-bottom: 1px solid #3c3c3c;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .language-select {
        width: 140px;

        :deep(.el-input__wrapper) {
          background-color: #3c3c3c;
          border: none;
          box-shadow: none;

          .el-input__inner {
            color: #d4d4d4;
          }

          .el-input__suffix {
            color: #d4d4d4;
          }
        }
      }

      .filename-input {
        width: 180px;

        :deep(.el-input__wrapper) {
          background-color: #3c3c3c;
          border: none;
          box-shadow: none;

          .el-input__inner {
            color: #d4d4d4;

            &::placeholder {
              color: #808080;
            }
          }

          .el-input__prefix {
            color: #808080;
          }
        }
      }

      .lang-option {
        display: flex;
        align-items: center;
        gap: $spacing-sm;

        .lang-icon {
          font-size: 14px;
        }

        .lang-name {
          font-size: 13px;
        }
      }
    }

    .toolbar-right {
      display: flex;
      align-items: center;
      gap: $spacing-xs;

      :deep(.el-button) {
        background-color: transparent;
        border-color: transparent;
        color: #808080;

        &:hover {
          background-color: #3c3c3c;
          color: #d4d4d4;
        }
      }
    }
  }

  // 代码输入区域
  .code-input-wrapper {
    display: flex;
    height: 300px;
    overflow: hidden;

    // 行号
    .line-numbers {
      width: 50px;
      padding: $spacing-md 0;
      background-color: #1e1e1e;
      border-right: 1px solid #3c3c3c;
      overflow-y: hidden;
      text-align: right;
      user-select: none;

      .line-number {
        height: 21px;
        line-height: 21px;
        padding-right: $spacing-sm;
        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
        font-size: 13px;
        color: #858585;
        transition: color $transition-fast $ease-base;

        &.current {
          color: #c6c6c6;
          background-color: rgba(255, 255, 255, 0.04);
        }
      }
    }

    // 代码文本框
    .code-textarea {
      flex: 1;
      padding: $spacing-md;
      background-color: #1e1e1e;
      border: none;
      outline: none;
      resize: none;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 13px;
      line-height: 21px;
      color: #d4d4d4;
      tab-size: 2;
      overflow-y: auto;
      @include custom-scrollbar(8px, #4a4a4a);

      &::placeholder {
        color: #5a5a5a;
      }

      &:focus {
        background-color: #1e1e1e;
      }
    }
  }

  // 代码统计信息
  .code-stats {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    padding: $spacing-sm $spacing-md;
    background-color: #007acc;
    font-size: 12px;
    color: white;

    .stat-item {
      display: flex;
      align-items: center;
      gap: $spacing-xs;

      .el-icon {
        font-size: 14px;
      }
    }

    .language-tag {
      margin-left: auto;

      .tag-dot {
        width: 8px;
        height: 8px;
        border-radius: $border-radius-round;
        background-color: #f0db4f; // JavaScript 黄色示例
      }
    }
  }
}

// 代码预览区域
.code-preview {
  margin-top: $spacing-lg;
  border: 1px solid $border-light;
  border-radius: $border-radius-base;
  overflow: hidden;

  .preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-sm $spacing-md;
    background-color: $bg-base;
    border-bottom: 1px solid $border-light;

    .preview-title {
      display: flex;
      align-items: center;
      gap: $spacing-xs;
      font-size: 13px;
      color: $text-secondary;
    }
  }

  .preview-content {
    max-height: 200px;
    overflow: auto;
    @include custom-scrollbar(6px, $border-dark);

    .code-block {
      margin: 0;
      padding: $spacing-md;
      background-color: #f6f8fa;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 13px;
      line-height: 1.5;
      overflow-x: auto;
      white-space: pre;

      code {
        color: #24292e;
      }
    }
  }
}

// 描述说明区域
.description-section {
  margin-top: $spacing-lg;

  :deep(.el-textarea__inner) {
    border-radius: $border-radius-base;
    border-color: $border-base;

    &:focus {
      border-color: $primary-color;
    }
  }
}

// 底部操作
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .footer-actions {
    display: flex;
    gap: $spacing-sm;
  }
}

// 响应式适配
@media (max-width: $breakpoint-md) {
  .code-snippet-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 2vh auto !important;
    }
  }

  .code-editor-container {
    .editor-toolbar {
      flex-direction: column;
      gap: $spacing-sm;

      .toolbar-left {
        width: 100%;
        flex-wrap: wrap;

        .language-select,
        .filename-input {
          flex: 1;
          min-width: 120px;
        }
      }

      .toolbar-right {
        width: 100%;
        justify-content: flex-end;
      }
    }

    .code-input-wrapper {
      height: 200px;

      .line-numbers {
        width: 40px;

        .line-number {
          font-size: 12px;
        }
      }

      .code-textarea {
        font-size: 12px;
      }
    }
  }
}

// 减少动画偏好
@media (prefers-reduced-motion: reduce) {
  .code-editor-container,
  .code-preview {
    transition: none;
  }
}
</style>
