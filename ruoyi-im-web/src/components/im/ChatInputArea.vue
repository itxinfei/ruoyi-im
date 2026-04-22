<template>
  <div
    class="slack-input-wrapper"
    :class="{ 'is-dragover': isDragover }"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
  >
    <div class="slack-input-box" :class="{ 'is-focused': isFocused }">
      <!-- 1. 富文本编辑区 -->
      <div class="editor-zone">
        <div
          ref="editorRef"
          class="slack-rich-editor"
          contenteditable="true"
          placeholder="给同事发送消息..."
          @focus="isFocused = true"
          @blur="isFocused = false"
          @input="handleInput"
          @keydown.enter.exact.prevent="executeSendMessage"
        />
      </div>

      <!-- 2. 底部工具栏与发送区 -->
      <div class="toolbar-zone">
        <div class="tools-left">
          <button class="tool-btn" title="添加附件"><el-icon><Plus /></el-icon></button>
          <div class="sep"></div>
          <button class="tool-btn" title="表情"><el-icon><CircleCheck /></el-icon></button>
          <button class="tool-btn" title="提及"><el-icon><User /></el-icon></button>
          <button class="tool-btn" title="文字加粗"><b style="font-family: serif;">B</b></button>
          <button class="tool-btn" title="斜体"><i style="font-family: serif;">I</i></button>
          <button class="tool-btn" title="链接"><el-icon><Link /></el-icon></button>
          <button class="tool-btn" title="代码块"><el-icon><Document /></el-icon></button>
        </div>

        <div class="tools-right">
          <button 
            class="slack-send-btn" 
            :class="{ 'can-send': canSend }"
            @click="executeSendMessage"
          >
            <el-icon><Promotion /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <div class="slack-footer-tip">
      <span v-if="!canSend"><strong>Shift + Enter</strong> 换行</span>
      <span v-else><strong>Enter</strong> 发送，<strong>Shift + Enter</strong> 换行</span>
    </div>

    <!-- 拖拽浮层 -->
    <transition name="fade">
      <div v-if="isDragover" class="drag-overlay">
        <div class="overlay-inner">
          <el-icon><UploadFilled /></el-icon>
          <p>释放文件以发送</p>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="js">
import { ref, computed, onMounted } from 'vue'
import { Plus, CircleCheck, User, Link, Document, Promotion, UploadFilled } from '@element-plus/icons-vue'

const editorRef = ref(null)
const isFocused = ref(false)
const hasContent = ref(false)
const isDragover = ref(false)

const canSend = computed(() => hasContent.value)

const handleInput = () => {
  hasContent.value = !!editorRef.value?.innerText.trim()
}

const executeSendMessage = () => {
  if (!canSend.value) return
  editorRef.value.innerHTML = ''
  hasContent.value = false
}

const handleDragOver = () => { isDragover.value = true }
const handleDragLeave = () => { isDragover.value = false }
const handleDrop = (e) => {
  isDragover.value = false
  const files = e.dataTransfer?.files
  if (files?.length) {
    // 文件上传逻辑将通过 emit 通知父组件
  }
}

onMounted(() => editorRef.value?.focus())
</script>

<style scoped lang="scss">
.slack-input-wrapper {
  padding: 0 var(--dt-spacing-xl) var(--dt-spacing-xl);
  background: var(--dt-bg-card);
  position: relative;
  display: flex;
  flex-direction: column;
}

.slack-input-box {
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s, border-color 0.2s;
  overflow: hidden;

  &.is-focused {
    border-color: transparent;
    box-shadow: 0 0 0 1px var(--dt-brand-color), 0 0 0 4px var(--dt-brand-lighter);
  }
}

/* 1. 编辑区 */
.editor-zone {
  padding: 10px 14px;
  min-height: 48px;
  max-height: 40vh;
  overflow-y: auto;

  .slack-rich-editor {
    width: 100%;
    height: 100%;
    outline: none;
    font-size: 15px;
    line-height: 1.5;
    color: var(--dt-text-primary);
    word-break: break-all;

    &:empty::before {
      content: attr(placeholder);
      color: var(--dt-text-tertiary);
      pointer-events: none;
    }
  }
}

/* 2. 工具栏 */
.toolbar-zone {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px var(--dt-spacing-sm);
  background: var(--dt-bg-input);
  border-top: 1px solid var(--dt-border-lighter);

  .tools-left {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .sep {
    width: 1px;
    height: 16px;
    background: var(--dt-border-color);
    margin: 0 6px;
  }

  .tool-btn {
    width: 28px;
    height: 28px;
    border: none;
    background: transparent;
    color: var(--dt-text-secondary);
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    @include flex-center;
    font-size: 16px;
    transition: var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-text-primary);
    }
  }
}

/* 发送按钮 */
.slack-send-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-sm);
  border: none;
  background: transparent;
  color: var(--dt-text-quaternary);
  @include flex-center;
  font-size: 16px;
  transition: all 0.2s;

  &.can-send {
    background: var(--dt-brand-color);
    color: var(--dt-text-white);
    cursor: pointer;

    &:hover { background: var(--dt-brand-hover); }
  }
}

.slack-footer-tip {
  text-align: right;
  margin-top: var(--dt-spacing-sm);
  font-size: 11px;
  color: var(--dt-text-tertiary);
  padding-right: var(--dt-spacing-xs);

  strong { font-weight: 600; color: var(--dt-text-secondary); }
}

.drag-overlay {
  position: absolute; inset: 0; background: rgba(255,255,255,0.9);
  z-index: 100; @include flex-center;
  .overlay-inner {
    border: 2px dashed var(--dt-brand-color); border-radius: var(--dt-radius-lg); padding: 32px;
    color: var(--dt-brand-color); text-align: center; font-weight: 700;
    .el-icon { font-size: 48px; margin-bottom: var(--dt-spacing-md); }
  }
}
</style>
