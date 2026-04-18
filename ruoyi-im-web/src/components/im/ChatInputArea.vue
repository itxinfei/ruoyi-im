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

onMounted(() => editorRef.value?.focus())
</script>

<style scoped lang="scss">
.slack-input-wrapper {
  padding: 0 20px 20px;
  background: #fff; // Slack 整个聊天区背景通常都是纯白
  position: relative;
  display: flex;
  flex-direction: column;
}

.slack-input-box {
  border: 1px solid #86868b; // Slack 特色的灰色边框
  border-radius: 8px;
  background: #fff;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s, border-color 0.2s;
  overflow: hidden;

  &.is-focused {
    border-color: transparent;
    box-shadow: 0 0 0 1px var(--dt-brand-color), 0 0 0 4px rgba(39, 126, 251, 0.2); // 典型的 focus ring
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
    color: #1d1d1f;
    word-break: break-all;

    &:empty::before {
      content: attr(placeholder);
      color: #86868b;
      pointer-events: none;
    }
  }
}

/* 2. 工具栏 */
.toolbar-zone {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  background: #f8f9fa; // 极淡的底部栏背景
  border-top: 1px solid #f0f0f0;

  .tools-left {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .sep {
    width: 1px;
    height: 16px;
    background: #dcdfe6;
    margin: 0 6px;
  }

  .tool-btn {
    width: 28px;
    height: 28px;
    border: none;
    background: transparent;
    color: #5f6368;
    border-radius: 4px;
    cursor: pointer;
    @include flex-center;
    font-size: 16px;
    transition: 0.15s;

    &:hover {
      background: #e3e5e8;
      color: #1d1d1f;
    }
  }
}

/* 发送按钮 */
.slack-send-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: #aaa;
  @include flex-center;
  font-size: 16px;
  transition: all 0.2s;

  &.can-send {
    background: #007a5a; // Slack 标志性绿色
    color: #fff;
    cursor: pointer;
    
    &:hover { background: #148567; }
    &:active { transform: scale(0.95); }
  }
}

.slack-footer-tip {
  text-align: right;
  margin-top: 8px;
  font-size: 11px;
  color: #86868b;
  padding-right: 4px;
  
  strong { font-weight: 600; color: #555; }
}

.drag-overlay {
  position: absolute; inset: 0; background: rgba(255,255,255,0.9);
  z-index: 100; @include flex-center;
  .overlay-inner {
    border: 2px dashed #007a5a; border-radius: 12px; padding: 32px;
    color: #007a5a; text-align: center; font-weight: 700;
    .el-icon { font-size: 48px; margin-bottom: 16px; }
  }
}
</style>
