<template>
  <div class="code-block" :class="`language-${language}`">
    <div class="code-header">
      <span class="code-language">{{ languageLabel }}</span>
      <button class="code-copy-btn" @click="handleCopy" title="复制代码">
        <el-icon><Document /></el-icon>
      </button>
    </div>
    <pre class="code-content"><code>{{ code }}</code></pre>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { copyToClipboard } from '@/utils/format'

const props = defineProps({
  language: { type: String, default: 'plaintext' },
  code: { type: String, required: true }
})

const languageLabel = computed(() => {
  const langMap = {
    javascript: 'JavaScript',
    typescript: 'TypeScript',
    python: 'Python',
    java: 'Java',
    cpp: 'C++',
    c: 'C',
    go: 'Go',
    rust: 'Rust',
    ruby: 'Ruby',
    php: 'PHP',
    swift: 'Swift',
    kotlin: 'Kotlin',
    html: 'HTML',
    css: 'CSS',
    scss: 'SCSS',
    json: 'JSON',
    sql: 'SQL',
    bash: 'Bash',
    shell: 'Shell',
    plaintext: 'Text'
  }
  return langMap[props.language] || props.language || 'Text'
})

// 使用共享复制函数
const handleCopy = () => {
  copyToClipboard(props.code, { successMsg: '代码已复制' })
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.code-block {
  margin: 8px 0;
  background: #1e1e1e;
  border-radius: var(--dt-radius-sm);
  overflow: hidden;
  font-size: 13px;
  max-width: 500px;

  .code-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 12px;
    background: #2d2d2d;
    border-bottom: 1px solid #3e3e3e;
  }

  .code-language {
    font-size: var(--dt-font-size-xs);
    color: #8b949e;
    text-transform: uppercase;
    font-weight: 500;
  }

  .code-copy-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border: none;
    background: transparent;
    color: #8b949e;
    cursor: pointer;
    border-radius: var(--dt-radius-sm);
    transition: all var(--dt-transition-fast);

    &:hover {
      background: rgba(255, 255, 255, 0.1);
      color: #fff;
    }

    .el-icon {
      font-size: 14px;
    }
  }

  .code-content {
    margin: 0;
    padding: 12px;
    overflow-x: auto;
    background: transparent;

    code {
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      line-height: 1.6;
      color: #e6edf3;
      white-space: pre;
    }
  }

  // 语言标识颜色
  &.language-javascript .code-language { color: #f1e05a; }
  &.language-typescript .code-language { color: #3178c6; }
  &.language-python .code-language { color: #3572A5; }
  &.language-java .code-language { color: #b07219; }
  &.language-cpp .code-language { color: #f34b7d; }
  &.language-c .code-language { color: #555555; }
  &.language-go .code-language { color: #00ADD8; }
  &.language-rust .code-language { color: #dea584; }
  &.language-ruby .code-language { color: #701516; }
  &.language-php .code-language { color: #4F5D95; }
  &.language-swift .code-language { color: #F05138; }
  &.language-kotlin .code-language { color: #A97BFF; }
  &.language-html .code-language { color: #e34c26; }
  &.language-css .code-language { color: #563d7c; }
  &.language-scss .code-language { color: #c6538c; }
  &.language-json .code-language { color: #cbcb41; }
  &.language-sql .code-language { color: #cc3e44; }
  &.language-bash .code-language { color: #89e051; }
  &.language-shell .code-language { color: #89e051; }
}

// 暗色模式不需要额外适配
</style>
