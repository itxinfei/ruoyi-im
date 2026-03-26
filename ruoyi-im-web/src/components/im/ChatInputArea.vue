<template>
  <div
    class="chat-input-wrapper"
    :class="{ 'is-dragover': isDragover }"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
  >
    <!-- 拖拽上传遮罩 -->
    <div v-if="isDragover" class="dragover-mask">
      <el-icon class="drag-icon"><Upload /></el-icon>
      <span>拖拽文件到此处上传</span>
    </div>

    <!-- 引用消息预览区 -->
    <div v-if="replyingMessage" class="reply-preview-bar">
      <div class="reply-content">
        <span class="reply-author">{{ replyingMessage.senderName }}: </span>
        <span class="reply-text">{{ replyingMessage.content }}</span>
      </div>
      <el-icon class="icon-close" @click="clearReply"><Close /></el-icon>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <!-- 表情选择器 -->
      <el-popover
        placement="top"
        :width="320"
        trigger="click"
        v-model:visible="emojiPickerVisible"
      >
        <template #reference>
          <el-icon class="tool-icon" title="表情"><Star /></el-icon>
        </template>
        <div class="emoji-picker">
          <div class="emoji-grid">
            <span
              v-for="emoji in emojiList"
              :key="emoji"
              class="emoji-item"
              @click="insertEmoji(emoji)"
            >{{ emoji }}</span>
          </div>
        </div>
      </el-popover>

      <!-- 图片上传 -->
      <el-upload
        action="#"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="processImageSelect"
        accept="image/*"
        class="upload-wrapper"
      >
        <el-icon class="tool-icon" title="图片"><Picture /></el-icon>
      </el-upload>

      <!-- 文件上传 -->
      <el-upload
        action="#"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="processFileSelect"
        class="upload-wrapper"
      >
        <el-icon class="tool-icon" title="文件"><Folder /></el-icon>
      </el-upload>
    </div>

    <!-- 图片预览区 -->
    <div v-if="pendingImages.length > 0" class="image-preview-bar">
      <div v-for="(img, index) in pendingImages" :key="index" class="preview-item">
        <el-image :src="img.url" fit="cover" class="preview-img" />
        <el-icon class="preview-remove" @click="removeImage(index)"><Close /></el-icon>
      </div>
    </div>

    <!-- 高级输入区 -->
    <div class="input-main">
      <div
        ref="editorRef"
        class="rich-editor"
        contenteditable="true"
        placeholder="请输入消息..."
        @keydown.enter.exact.prevent="executeSendMessage"
        @keydown.ctrl.enter.stop="insertNewLine"
        @paste="processPaste"
        @input="handleInput"
      ></div>
    </div>

    <!-- 底部发送栏 -->
    <div class="input-footer">
      <span class="tip">Enter 发送，Ctrl+Enter 换行</span>
      <button
        :class="['send-btn', { 'is-active': canSend }]"
        @click="executeSendMessage"
      >
        发送
      </button>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * ChatInputArea.vue (对齐钉钉无边框沉浸式输入 & 状态驱动发送按钮 + 表情选择器 + 图片预览)
 */
import { ref, computed, watch, onMounted } from 'vue';
import { Close, Star, Picture, Folder, Upload } from '@element-plus/icons-vue';

const props = defineProps({
  replyingMessage: Object,
  modelValue: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['send', 'clear-reply', 'update:modelValue']);

const editorRef = ref(null);
const hasContent = ref(false);
const emojiPickerVisible = ref(false);
const pendingImages = ref([]);
const isDragover = ref(false);
let isInternalSet = false; // 防止 watch 设置时触发 handleInput 冒泡

// 监听外部 draft 变化（会话切换时恢复草稿）
watch(() => props.modelValue, (newVal) => {
  if (!editorRef.value) return;
  const currentText = editorRef.value.innerText;
  if (currentText !== newVal) {
    isInternalSet = true;
    editorRef.value.innerText = newVal || '';
    hasContent.value = !!newVal;
  }
});

// 常用表情列表
const emojiList = [
  '😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂', '😉', '😌',
  '😍', '🥰', '😘', '😋', '😛', '🤔', '🤨', '😐', '😑', '😶', '🙄', '😏',
  '😣', '😥', '😮', '🤐', '😯', '😪', '😫', '😴', '😌', '😛', '😜', '😝',
  '🤤', '😒', '😓', '😔', '😕', '🙃', '🤑', '😲', '🙁', '😖', '😞', '😟',
  '😤', '😢', '😭', '😦', '😧', '😨', '😩', '🤯', '😬', '😰', '😱', '👍', '👎'
];

// 是否可以发送（有文字或有图片）
const canSend = computed(() => {
  return hasContent.value || pendingImages.value.length > 0;
});

const handleInput = () => {
  if (isInternalSet) {
    isInternalSet = false;
    return;
  }
  const text = editorRef.value.innerText;
  hasContent.value = !!text.trim();
  emit('update:modelValue', text);
};

// 插入表情
const insertEmoji = (emoji) => {
  if (!editorRef.value) return;
  editorRef.value.innerText += emoji;
  editorRef.value.focus();
  hasContent.value = true;
  emojiPickerVisible.value = false;
  emit('update:modelValue', editorRef.value.innerText);
};

// 插入换行符
const insertNewLine = () => {
  if (!editorRef.value) return;
  document.execCommand('insertHTML', false, '<br>');
};

const processPaste = async (e) => {
  const items = (e.clipboardData || e.originalEvent.clipboardData).items;
  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      const file = item.getAsFile();
      addPendingImage(file);
      e.preventDefault();
    }
  }
};

const executeSendMessage = () => {
  // 发送图片
  if (pendingImages.value.length > 0) {
    pendingImages.value.forEach(img => {
      emit('send', { type: 'IMAGE', file: img.file });
    });
    pendingImages.value = [];
  }

  // 发送文字
  const content = editorRef.value.innerText.trim();
  if (content) {
    emit('send', { type: 'TEXT', content: content });
    editorRef.value.innerHTML = '';
  }

  hasContent.value = false;
  emit('update:modelValue', '');
};

// 处理图片选择
const processImageSelect = (file) => {
  addPendingImage(file.raw);
};

// 处理文件选择
const processFileSelect = (file) => {
  emit('send', { type: 'FILE', file: file.raw, fileName: file.name });
};

// 添加待发送图片
const addPendingImage = (file) => {
  const url = URL.createObjectURL(file);
  pendingImages.value.push({ file, url });
};

// 移除待发送图片
const removeImage = (index) => {
  URL.revokeObjectURL(pendingImages.value[index].url);
  pendingImages.value.splice(index, 1);
};

const clearReply = () => emit('clear-reply');

// 拖拽上传
const handleDragOver = (e) => {
  const hasFiles = e.dataTransfer?.types.includes('Files');
  if (hasFiles) {
    isDragover.value = true;
  }
};

const handleDragLeave = (e) => {
  // 仅当离开整个输入区时才隐藏遮罩，防止子元素触发闪烁
  if (!e.currentTarget.contains(e.relatedTarget)) {
    isDragover.value = false;
  }
};

const handleDrop = (e) => {
  isDragover.value = false;
  const files = e.dataTransfer?.files;
  if (!files || files.length === 0) return;

  for (const file of files) {
    if (file.type.startsWith('image/')) {
      addPendingImage(file);
    } else {
      emit('send', { type: 'FILE', file, fileName: file.name });
    }
  }
};

onMounted(() => {
  editorRef.value?.focus();
});
</script>

<style scoped>
.chat-input-wrapper {
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-chat);
  border-top: 1px solid var(--dt-border-light);
  position: relative;
}

/* 拖拽上传遮罩 */
.dragover-mask {
  position: absolute;
  inset: 0;
  background: var(--dt-bg-hover);
  border: 2px dashed var(--dt-brand-color);
  border-radius: var(--dt-radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  z-index: 10;
  color: var(--dt-brand-color);
  font-size: 14px;
  font-weight: 500;
}

.drag-icon {
  font-size: 32px;
}

.chat-input-wrapper.is-dragover {
  border-color: var(--dt-brand-color);
}

/* 引用回复栏 */
.reply-preview-bar {
  height: auto;
  max-height: 60px;
  background: var(--dt-info-bg);
  border-left: 2px solid var(--dt-brand-color);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 8px 12px;
  margin: 8px 20px 0;
  border-radius: 0 var(--dt-radius-sm) var(--dt-radius-sm) 0;
}

.reply-content {
  font-size: var(--dt-font-size-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: var(--dt-text-secondary);
}

.reply-author {
  color: var(--dt-text-tertiary);
}

.icon-close {
  cursor: pointer;
  color: var(--dt-text-tertiary);
  font-size: 14px;
  margin-left: 8px;
  transition: color var(--dt-transition-fast);
}

.icon-close:hover {
  color: var(--dt-text-primary);
}

/* 工具栏 */
.toolbar {
  height: 40px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.tool-icon {
  font-size: 20px;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  outline: none;
  transition: color var(--dt-transition-fast);
}

.tool-icon:hover {
  color: var(--dt-text-primary);
}

.upload-wrapper {
  display: flex;
  align-items: center;
}

/* 表情选择器 */
.emoji-picker {
  padding: 8px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  border-radius: var(--dt-radius-sm);
  transition: background-color var(--dt-transition-fast);
}

.emoji-item:hover {
  background-color: var(--dt-bg-hover);
}

/* 图片预览区 */
.image-preview-bar {
  display: flex;
  gap: 8px;
  padding: 8px 20px;
  background-color: var(--dt-bg-body);
  overflow-x: auto;
}

.preview-item {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: var(--dt-radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 18px;
  height: 18px;
  background-color: var(--dt-overlay-bg);
  color: var(--dt-text-white);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
}

/* 主输入区 */
.input-main {
  flex: 1;
  padding: 0 20px;
  min-height: 60px;
  max-height: 240px;
  overflow-y: auto;
}

/* 钉钉风格富文本编辑器: 无边框，无焦点发光 */
.rich-editor {
  width: 100%;
  height: 100%;
  outline: none;
  border: none;
  font-size: var(--dt-font-size-base);
  line-height: 1.6;
  color: var(--dt-text-primary);
  white-space: pre-wrap;
  word-break: break-all;
}

.rich-editor:empty:before {
  content: attr(placeholder);
  color: var(--dt-text-tertiary);
}

/* 底部发送栏 */
.input-footer {
  height: 48px;
  padding: 0 20px 12px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.tip {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.send-btn {
  background-color: var(--dt-bg-hover);
  color: var(--dt-text-white);
  border: none;
  padding: 0 20px;
  height: 32px;
  border-radius: var(--dt-radius-sm);
  font-size: var(--dt-font-size-base);
  cursor: not-allowed;
  transition: all var(--dt-transition-fast);
}

.send-btn.is-active {
  background-color: var(--dt-brand-color);
  cursor: pointer;
}

.send-btn.is-active:hover {
  background-color: var(--dt-brand-hover);
}

.send-btn.is-active:active {
  transform: scale(0.98);
}

/* 滚动条美化 */
.input-main::-webkit-scrollbar { width: 4px; }
.input-main::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.05); border-radius: 2px; }
</style>
