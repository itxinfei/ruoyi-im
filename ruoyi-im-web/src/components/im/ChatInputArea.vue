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

    <!-- 编辑模式提示条 -->
    <div v-if="isEditMode" class="edit-mode-bar">
      <span class="edit-mode-text">编辑消息</span>
      <span class="edit-mode-hint">Enter 保存 · ESC 取消</span>
      <el-icon class="icon-close" @click="cancelEdit"><Close /></el-icon>
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

      <!-- 视频上传 -->
      <el-upload
        action="#"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="processVideoSelect"
        accept="video/*"
        class="upload-wrapper"
      >
        <el-icon class="tool-icon" title="视频"><VideoCamera /></el-icon>
      </el-upload>

      <!-- 名片 -->
      <el-icon class="tool-icon" title="名片" @click="openCardPicker">
        <User />
      </el-icon>

      <!-- 位置 -->
      <el-icon class="tool-icon" title="位置" @click="openLocationPicker">
        <LocationInformation />
      </el-icon>

      <!-- 语音录制 -->
      <el-icon class="tool-icon" :class="{ 'is-recording': isRecording }" title="语音" @click="toggleRecording">
        <Microphone />
      </el-icon>
    </div>

    <!-- 录音状态区 -->
    <div v-if="isRecording" class="recording-bar">
      <div class="recording-info">
        <span class="recording-dot"></span>
        <span class="recording-text">录音中 {{ formatDuration(recordingDuration) }}</span>
      </div>
      <div class="recording-actions">
        <button class="rec-btn cancel" @click="cancelRecording">取消</button>
        <button class="rec-btn send" @click="sendRecording">发送</button>
      </div>
    </div>

    <!-- 图片预览区 -->
    <div v-if="pendingImages.length > 0" class="image-preview-bar">
      <div v-for="(img, index) in pendingImages" :key="index" class="preview-item">
        <el-image :src="img.url" fit="cover" class="preview-img" />
        <el-icon class="preview-remove" @click="removeImage(index)"><Close /></el-icon>
      </div>
    </div>

    <!-- 视频预览区 -->
    <div v-if="pendingVideos.length > 0" class="video-preview-bar">
      <div v-for="(video, index) in pendingVideos" :key="index" class="preview-item video-preview-item">
        <video :src="video.url" class="preview-video" />
        <el-icon class="preview-duration"><Clock /></el-icon>
        <span class="preview-duration-text">{{ formatDuration(video.duration) }}</span>
        <el-icon class="preview-remove" @click="removeVideo(index)"><Close /></el-icon>
      </div>
    </div>

    <!-- 高级输入区 -->
    <div class="input-main">
      <div
        ref="editorRef"
        class="rich-editor"
        contenteditable="true"
        :placeholder="isEditMode ? '修改消息...' : '请输入消息...'"
        @keydown.enter.exact.prevent="executeSendMessage"
        @keydown.esc.stop="cancelEdit"
        @keydown.ctrl.enter.stop="insertNewLine"
        @paste="processPaste"
        @input="handleInput"
      ></div>
    </div>

    <!-- 底部发送栏 -->
    <div class="input-footer">
      <span class="tip">{{ isEditMode ? 'Enter 保存，ESC 取消' : 'Enter 发送，Ctrl+Enter 换行' }}</span>
      <button
        :class="['send-btn', { 'is-active': canSend }]"
        @click="executeSendMessage"
      >
        发送
      </button>
    </div>

    <!-- 名片选择对话框 -->
    <el-dialog
      v-model="cardPickerVisible"
      title="选择联系人"
      width="400px"
      append-to-body
    >
      <div class="card-picker-search">
        <el-input v-model="cardSearchKeyword" placeholder="搜索联系人" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="card-picker-list">
        <div
          v-for="contact in filteredContacts"
          :key="contact.id"
          class="card-picker-item"
          @click="selectCardContact(contact)"
        >
          <img :src="contact.avatar || '/avatars/default.png'" class="card-avatar" />
          <div class="card-info">
            <div class="card-name">{{ contact.name }}</div>
            <div class="card-dept">{{ contact.department || '' }}</div>
          </div>
        </div>
        <div v-if="filteredContacts.length === 0" class="card-empty">
          暂无联系人
        </div>
      </div>
    </el-dialog>

    <!-- 位置选择对话框 -->
    <el-dialog
      v-model="locationPickerVisible"
      title="发送位置"
      width="420px"
      append-to-body
    >
      <el-form :model="locationForm" label-width="80px">
        <el-form-item label="位置名称" required>
          <el-input v-model="locationForm.name" placeholder="如：公司、家里" maxlength="50" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="locationForm.address" placeholder="如：北京市朝阳区xxx路" maxlength="100" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="locationForm.latitude" :precision="6" :step="0.000001" placeholder="纬度" style="width: 100%" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="locationForm.longitude" :precision="6" :step="0.000001" placeholder="经度" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="locationPickerVisible = false">取消</el-button>
        <el-button type="primary" @click="sendLocation">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
/**
 * ChatInputArea.vue (对齐钉钉无边框沉浸式输入 & 状态驱动发送按钮 + 表情选择器 + 图片预览)
 */
import { ref, computed, watch, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, Star, Picture, Folder, Upload, Microphone, VideoCamera, Clock, User, Search, LocationInformation } from '@element-plus/icons-vue';
import { getContacts } from '@/api/im/contact';

const props = defineProps({
  replyingMessage: Object,
  editingMessage: Object,
  modelValue: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['send', 'clear-reply', 'update:modelValue', 'edit-save', 'edit-cancel']);

const editorRef = ref(null);
const hasContent = ref(false);
const emojiPickerVisible = ref(false);
const pendingImages = ref([]);
const pendingVideos = ref([]);
const isDragover = ref(false);
let isInternalSet = false; // 防止 watch 设置时触发 handleInput 冒泡
const isEditMode = ref(false); // 编辑模式标识

// 录音状态
const isRecording = ref(false);
const recordingDuration = ref(0);
let mediaRecorder = null;
let recordedChunks = [];
let recordingTimer = null;

// 名片选择状态
const cardPickerVisible = ref(false);
const cardSearchKeyword = ref('');
const contactList = ref([]);

// 位置选择状态
const locationPickerVisible = ref(false);
const locationForm = ref({
  name: '',
  address: '',
  latitude: 39.9042,
  longitude: 116.4074
});

// 监听外部草稿变化（会话切换时恢复草稿）
watch(() => props.modelValue, (newVal) => {
  if (!editorRef.value) return;
  const currentText = editorRef.value.innerText;
  if (currentText !== newVal) {
    isInternalSet = true;
    editorRef.value.innerText = newVal || '';
    hasContent.value = !!newVal;
  }
});

// 监听编辑消息变化（进入/退出编辑模式）
watch(() => props.editingMessage, (newVal) => {
  if (newVal) {
    isEditMode.value = true;
    isInternalSet = true;
    if (editorRef.value) {
      editorRef.value.innerText = newVal.content || '';
      hasContent.value = !!newVal.content;
    }
    nextTick(() => editorRef.value?.focus());
  } else {
    isEditMode.value = false;
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

// 是否可以发送（有文字或有图片或有视频）
const canSend = computed(() => {
  return hasContent.value || pendingImages.value.length > 0 || pendingVideos.value.length > 0;
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
  // 编辑模式：直接触发保存
  if (isEditMode.value) {
    const content = editorRef.value.innerText.trim();
    if (content) {
      emit('edit-save', { content });
    }
    return;
  }

  // 发送图片
  if (pendingImages.value.length > 0) {
    pendingImages.value.forEach(img => {
      emit('send', { type: 'IMAGE', file: img.file });
    });
    pendingImages.value = [];
  }

  // 发送视频
  if (pendingVideos.value.length > 0) {
    pendingVideos.value.forEach(video => {
      emit('send', { type: 'VIDEO', file: video.file, duration: video.duration });
    });
    pendingVideos.value = [];
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

// 取消编辑
const cancelEdit = () => {
  emit('edit-cancel');
};

// 处理图片选择
const processImageSelect = (file) => {
  addPendingImage(file.raw);
};

// 处理文件选择
const processFileSelect = (file) => {
  emit('send', { type: 'FILE', file: file.raw, fileName: file.name });
};

// 处理视频选择
const processVideoSelect = (file) => {
  addPendingVideo(file.raw);
};

// 添加待发送视频
const addPendingVideo = (file) => {
  const url = URL.createObjectURL(file);
  // 获取视频时长
  const video = document.createElement('video');
  video.preload = 'metadata';
  video.onloadedmetadata = () => {
    const duration = Math.floor(video.duration);
    pendingVideos.value.push({ file, url, duration });
    URL.revokeObjectURL(url);
  };
  video.onerror = () => {
    pendingVideos.value.push({ file, url, duration: 0 });
  };
  video.src = url;
};

// 移除待发送视频
const removeVideo = (index) => {
  URL.revokeObjectURL(pendingVideos.value[index].url);
  pendingVideos.value.splice(index, 1);
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

// 过滤联系人
const filteredContacts = computed(() => {
  if (!cardSearchKeyword.value) return contactList.value;
  const keyword = cardSearchKeyword.value.toLowerCase();
  return contactList.value.filter(c =>
    c.name?.toLowerCase().includes(keyword) ||
    c.department?.toLowerCase().includes(keyword)
  );
});

// 打开名片选择器
const openCardPicker = async () => {
  cardSearchKeyword.value = '';
  cardPickerVisible.value = true;
  try {
    const res = await getContacts()
    if (res.code === 200) {
      contactList.value = res.data || []
    } else {
      throw new Error(res.message)
    }
  } catch (e) {
    ElMessage.error('获取联系人失败')
    contactList.value = []
  }
};

// 选择联系人发送名片
const selectCardContact = (contact) => {
  cardPickerVisible.value = false;
  emit('send', {
    type: 'CARD',
    card: {
      cardType: 'user',
      userId: contact.id,
      userName: contact.name,
      userAvatar: contact.avatar,
      department: contact.department || ''
    }
  });
};

// 打开位置选择器
const openLocationPicker = () => {
  locationForm.value = {
    name: '',
    address: '',
    latitude: 39.9042,
    longitude: 116.4074
  };
  locationPickerVisible.value = true;
};

// 发送位置
const sendLocation = () => {
  if (!locationForm.value.name.trim()) {
    return;
  }
  locationPickerVisible.value = false;
  emit('send', {
    type: 'LOCATION',
    location: {
      name: locationForm.value.name,
      address: locationForm.value.address || '',
      latitude: locationForm.value.latitude,
      longitude: locationForm.value.longitude
    }
  });
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
    } else if (file.type.startsWith('video/')) {
      addPendingVideo(file);
    } else {
      emit('send', { type: 'FILE', file, fileName: file.name });
    }
  }
};

// 录音
const toggleRecording = async () => {
  if (isRecording.value) {
    stopRecording();
  } else {
    await startRecording();
  }
};

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' });
    recordedChunks = [];

    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) recordedChunks.push(e.data);
    };

    mediaRecorder.start();
    isRecording.value = true;
    recordingDuration.value = 0;

    recordingTimer = setInterval(() => {
      recordingDuration.value++;
    }, 1000);
  } catch (e) {
    console.error('无法访问麦克风', e);
    ElMessage.error('无法访问麦克风，请检查权限设置')
  }
};

const stopRecording = () => {
  if (!mediaRecorder || mediaRecorder.state === 'inactive') return;
  mediaRecorder.stop();
  mediaRecorder.stream.getTracks().forEach(t => t.stop());
  clearInterval(recordingTimer);
  isRecording.value = false;
};

const cancelRecording = () => {
  stopRecording();
  recordedChunks = [];
  recordingDuration.value = 0;
};

const sendRecording = () => {
  if (recordedChunks.length === 0) return;
  const blob = new Blob(recordedChunks, { type: 'audio/webm' });
  const file = new File([blob], `录音_${formatDuration(recordingDuration.value)}.webm`, { type: 'audio/webm' });
  emit('send', { type: 'VOICE', file });
  recordedChunks = [];
  recordingDuration.value = 0;
  stopRecording();
};

const formatDuration = (seconds) => {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
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

/* 编辑模式提示条 */
.edit-mode-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  margin: 8px 20px 0;
  background: var(--dt-warning-bg);
  border-left: 2px solid var(--dt-warning-color);
  border-radius: 0 var(--dt-radius-sm) var(--dt-radius-sm) 0;
}

.edit-mode-text {
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  color: var(--dt-warning-color);
}

.edit-mode-hint {
  flex: 1;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
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
  height: var(--dt-toolbar-height, 32px);
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

.tool-icon.is-recording {
  color: var(--dt-error-color);
  animation: pulse-recording 1s ease-in-out infinite;
}

@keyframes pulse-recording {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 录音状态栏 */
.recording-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 20px;
  background: var(--dt-bg-body);
  border-top: 1px solid var(--dt-border-light);
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from { height: 0; opacity: 0; }
  to { height: 44px; opacity: 1; }
}

.recording-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recording-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--dt-error-color);
  animation: blink 0.8s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.2; }
}

.recording-text {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

.recording-actions {
  display: flex;
  gap: 12px;
}

.rec-btn {
  border: none;
  border-radius: var(--dt-radius-sm);
  padding: 4px 16px;
  font-size: var(--dt-font-size-sm);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.rec-btn.cancel {
  background: var(--dt-bg-hover);
  color: var(--dt-text-secondary);
}

.rec-btn.cancel:hover {
  background: var(--dt-border-light);
}

.rec-btn.send {
  background: var(--dt-brand-color);
  color: var(--dt-text-white);
}

.rec-btn.send:hover {
  background: var(--dt-brand-hover);
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

/* 视频预览区 */
.video-preview-bar {
  display: flex;
  gap: 8px;
  padding: 8px 20px;
  background-color: var(--dt-bg-body);
  overflow-x: auto;
}

.video-preview-item {
  position: relative;
  width: 96px;
  height: 64px;
  border-radius: var(--dt-radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  background-color: var(--dt-bg-card);
}

.preview-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-duration {
  position: absolute;
  bottom: 2px;
  left: 4px;
  font-size: 10px;
  color: var(--dt-text-white);
  background-color: var(--dt-overlay-bg);
  border-radius: 2px;
  padding: 1px 3px;
}

.preview-duration-text {
  position: absolute;
  bottom: 2px;
  right: 4px;
  font-size: 10px;
  color: var(--dt-text-white);
  background-color: var(--dt-overlay-bg);
  border-radius: 2px;
  padding: 1px 3px;
}

/* 名片选择器 */
.card-picker-search {
  margin-bottom: 16px;
}

.card-picker-list {
  max-height: 300px;
  overflow-y: auto;
}

.card-picker-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);
}

.card-picker-item:hover {
  background-color: var(--dt-bg-hover);
}

.card-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-sm);
  object-fit: cover;
}

.card-info {
  flex: 1;
  overflow: hidden;
}

.card-name {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-dept {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-empty {
  text-align: center;
  padding: 24px;
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
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
