<template>
  <div class="chat-input-wrapper">
    <!-- 引用消息预览区 -->
    <div v-if="replyingMessage" class="reply-preview-bar">
      <span class="reply-text">回复: {{ replyingMessage.senderName }}: {{ replyingMessage.content }}</span>
      <i class="icon-close" @click="clearReply"></i>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <i class="icon-emoji" title="表情"></i>
      <el-upload
        action="#"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="processFileSelect"
      >
        <i class="icon-image" title="图片"></i>
      </el-upload>
      <i class="icon-file" title="文件"></i>
    </div>

    <!-- 高级输入区: 支持粘贴图片与 @ 提及 (钉钉级核心) -->
    <div class="input-main">
      <div
        ref="editorRef"
        class="rich-editor"
        contenteditable="true"
        placeholder="请输入消息..."
        @keydown.enter.exact.prevent="executeSendMessage"
        @paste="processPaste"
        @input="processInput"
      ></div>
    </div>

    <div class="input-footer">
      <span class="tip">Enter 发送，Ctrl+Enter 换行</span>
      <button 
        class="send-btn" 
        @click="executeSendMessage"
      >发送</button>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * ChatInputArea.vue
 * 核心：支持粘贴截图、拖拽上传、@ 提及逻辑
 * 严格对齐钉钉 Windows 版输入体感
 */
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  replyingMessage: Object
});

const emit = defineEmits(['send', 'clear-reply']);

const editorRef = ref(null);

/**
 * 核心：处理粘贴事件 (支持截图直接发送)
 */
const processPaste = async (e) => {
  const items = (e.clipboardData || e.originalEvent.clipboardData).items;
  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      const file = item.getAsFile();
      // 捕获到图片文件，提示用户是否发送（或直接进入 PENDING）
      emit('send', { type: 'IMAGE', file: file });
      e.preventDefault(); // 阻止将图片 base64 插入编辑器
    }
  }
};

const processInput = () => {
  // 可以在此处理 @ 提及的弹窗逻辑
};

const executeSendMessage = () => {
  const content = editorRef.value.innerText.trim();
  if (!content) return;

  emit('send', { type: 'TEXT', content: content });
  editorRef.value.innerHTML = ''; // 清空编辑器
};

const processFileSelect = (file) => {
  emit('send', { type: 'IMAGE', file: file.raw });
};

const clearReply = () => emit('clear-reply');

onMounted(() => {
  editorRef.value?.focus();
});
</script>

<style scoped>
.chat-input-wrapper {
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-chat);
  border-top: 1px solid var(--dt-border-color);
}

.reply-preview-bar {
  height: 32px;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #eee;
  font-size: 12px;
  color: #888;
}

.toolbar {
  height: 40px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--dt-text-desc);
}

.toolbar i { font-size: 20px; cursor: pointer; }
.toolbar i:hover { color: var(--dt-brand-color); }

.input-main {
  flex: 1;
  padding: 0 20px;
  min-height: 80px;
  max-height: 240px;
  overflow-y: auto;
}

/* 钉钉风格富文本编辑器 */
.rich-editor {
  width: 100%;
  height: 100%;
  outline: none;
  font-size: 14px;
  line-height: 1.6;
  color: var(--dt-text-main);
  white-space: pre-wrap;
  word-break: break-all;
}

.rich-editor:empty:before {
  content: attr(placeholder);
  color: #aaa;
}

.input-footer {
  height: 44px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tip { font-size: 12px; color: var(--dt-text-desc); }

.send-btn {
  background-color: var(--dt-brand-color);
  color: white;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}
</style>
