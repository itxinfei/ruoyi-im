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

    <!-- 引用/编辑 提示条 -->
    <div v-if="replyingMessage" class="reply-preview-bar">
      <div class="reply-content">
        <span class="reply-author">{{ replyingMessage.senderName }}: </span>
        <span class="reply-text">{{ replyingMessage.content }}</span>
      </div>
      <el-icon class="icon-close" @click="clearReply"><Close /></el-icon>
    </div>

    <div v-if="isEditMode" class="edit-mode-bar">
      <span class="edit-mode-text">编辑消息</span>
      <span class="edit-mode-hint">Enter 保存 · ESC 取消</span>
      <el-icon class="icon-close" @click="cancelEdit"><Close /></el-icon>
    </div>

    <!-- 工具栏 (钉钉 8.2 风格) -->
    <div class="toolbar">
      <el-popover v-model:visible="emojiPickerVisible" placement="top" :width="320" trigger="click">
        <template #reference>
          <el-icon class="tool-icon" title="表情"><Star /></el-icon>
        </template>
        <div class="emoji-picker">
          <div class="emoji-grid">
            <span v-for="emoji in emojiList" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">{{ emoji }}</span>
          </div>
        </div>
      </el-popover>

      <el-upload action="#" :show-file-list="false" :auto-upload="false" :on-change="processImageSelect" accept="image/*">
        <el-icon class="tool-icon" title="图片"><Picture /></el-icon>
      </el-upload>

      <el-upload action="#" :show-file-list="false" :auto-upload="false" :on-change="processFileSelect">
        <el-icon class="tool-icon" title="文件"><Folder /></el-icon>
      </el-upload>

      <el-upload action="#" :show-file-list="false" :auto-upload="false" :on-change="processVideoSelect" accept="video/*">
        <el-icon class="tool-icon" title="视频"><VideoCamera /></el-icon>
      </el-upload>

      <el-icon class="tool-icon" title="名片" @click="openCardPicker"><User /></el-icon>
      
      <el-icon 
        class="tool-icon" 
        :class="{ 'is-recording': isRecording }" 
        title="语音" 
        @click="toggleRecording"
      ><Microphone /></el-icon>
    </div>

    <!-- 预览区 (图片/视频) -->
    <div v-if="pendingImages.length > 0" class="image-preview-bar">
      <div v-for="(img, index) in pendingImages" :key="`img-${index}`" class="preview-item">
        <el-image :src="img.url" fit="cover" class="preview-img" />
        <el-icon class="preview-remove" @click="removeImage(index)"><Close /></el-icon>
      </div>
    </div>

    <!-- 输入区 -->
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
      />
    </div>

    <!-- 底部发送栏 -->
    <div class="input-footer">
      <span class="tip">{{ isEditMode ? 'Enter 保存，ESC 取消' : 'Enter 发送，Ctrl+Enter 换行' }}</span>
      <button :class="['send-btn', { 'is-active': canSend }]" @click="executeSendMessage">发送</button>
    </div>

    <!-- 名片选择对话框 (对齐钉钉 8.2 多维选择) -->
    <el-dialog v-model="cardPickerVisible" title="选择名片" width="440px" append-to-body class="dt-card-picker">
      <div class="card-picker-search">
        <el-input v-model="cardSearchKeyword" placeholder="搜索联系人或群组" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      
      <el-tabs v-model="activeCardTab" class="card-tabs">
        <el-tab-pane label="联系人" name="user">
          <div class="card-picker-list">
            <div v-for="c in filteredContacts" :key="c.userId" class="card-picker-item" @click="selectCard('user', c)">
              <img :src="c.avatar || '/avatars/default.png'" class="card-avatar">
              <div class="card-info">
                <div class="card-name">{{ c.nickname || c.userName }}</div>
                <div class="card-dept">{{ c.deptName || '普通成员' }}</div>
              </div>
            </div>
            <div v-if="filteredContacts.length === 0" class="card-empty">未找到匹配的联系人</div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="群组" name="group">
          <div class="card-picker-list">
            <div v-for="g in filteredGroups" :key="g.id" class="card-picker-item" @click="selectCard('group', g)">
              <img :src="g.avatar || '/avatars/group.png'" class="card-avatar">
              <div class="card-info">
                <div class="card-name">{{ g.name }}</div>
                <div class="card-dept">群成员 {{ g.memberCount || 0 }} 人</div>
              </div>
            </div>
            <div v-if="filteredGroups.length === 0" class="card-empty">未找到匹配的群组</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Star, Picture, Folder, Upload, Microphone, VideoCamera, Clock, User, Search } from '@element-plus/icons-vue'
import { getContacts } from '@/api/im/contact'
import { getConversations } from '@/api/im' // 用于获取群组列表（模拟）

const props = defineProps({
  replyingMessage: Object,
  editingMessage: Object,
  session: Object,
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['send', 'clear-reply', 'update:modelValue', 'edit-save', 'edit-cancel'])

const editorRef = ref(null)
const hasContent = ref(false)
const emojiPickerVisible = ref(false)
const pendingImages = ref([])
const pendingVideos = ref([])
const isDragover = ref(false)
const isEditMode = ref(false)
let isInternalSet = false

// 名片选择相关
const cardPickerVisible = ref(false)
const cardSearchKeyword = ref('')
const activeCardTab = ref('user')
const contactList = ref([])
const groupList = ref([])

// 录音相关
const isRecording = ref(false)
let mediaRecorder = null
let recordedChunks = []

// 监听外部草稿
watch(() => props.modelValue, (newVal) => {
  if (!editorRef.value || isInternalSet) return
  if (editorRef.value.innerText !== newVal) {
    editorRef.value.innerText = newVal || ''
    hasContent.value = !!newVal
  }
})

const emojiList = ['😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂', '😉', '😌', '😍', '🥰', '😘', '😋', '👍', '👎', '👏', '🙏']

const canSend = computed(() => hasContent.value || pendingImages.value.length > 0 || pendingVideos.value.length > 0)

const handleInput = () => {
  const text = editorRef.value.innerText
  hasContent.value = !!text.trim()
  emit('update:modelValue', text)
}

const insertEmoji = (emoji) => {
  if (!editorRef.value) return
  editorRef.value.innerText += emoji
  handleInput()
  emojiPickerVisible.value = false
}

const insertNewLine = () => document.execCommand('insertHTML', false, '<br>')

const executeSendMessage = () => {
  const content = editorRef.value.innerText.trim()
  if (isEditMode.value) {
    if (content) emit('edit-save', { content })
    return
  }
  
  if (pendingImages.value.length > 0) {
    pendingImages.value.forEach(img => emit('send', { type: 'IMAGE', file: img.file }))
    pendingImages.value = []
  }

  if (content) {
    emit('send', { type: 'TEXT', content })
    editorRef.value.innerHTML = ''
    hasContent.value = false
    emit('update:modelValue', '')
  }
}

const openCardPicker = async () => {
  cardSearchKeyword.value = ''
  cardPickerVisible.value = true
  try {
    const [cRes, gRes] = await Promise.all([getContacts(), getConversations('GROUP')])
    if (cRes.code === 200) contactList.value = cRes.data || []
    if (gRes.code === 200) groupList.value = gRes.data || []
  } catch (e) {
    ElMessage.error('获取列表失败')
  }
}

const filteredContacts = computed(() => {
  const kw = cardSearchKeyword.value.toLowerCase()
  return contactList.value.filter(c => (c.nickname || c.userName || '').toLowerCase().includes(kw))
})

const filteredGroups = computed(() => {
  const kw = cardSearchKeyword.value.toLowerCase()
  return groupList.value.filter(g => (g.name || '').toLowerCase().includes(kw))
})

const selectCard = (type, data) => {
  cardPickerVisible.value = false
  emit('send', {
    type: 'CARD',
    card: {
      cardType: type,
      userId: type === 'user' ? data.userId : data.id,
      userName: type === 'user' ? (data.nickname || data.userName) : data.name,
      userAvatar: data.avatar,
      department: type === 'user' ? (data.deptName || '') : `群成员 ${data.memberCount || 0} 人`
    }
  })
}

// 图片选择
const processImageSelect = (file) => {
  const url = URL.createObjectURL(file.raw)
  pendingImages.value.push({ file: file.raw, url })
}
const removeImage = (index) => {
  URL.revokeObjectURL(pendingImages.value[index].url)
  pendingImages.value.splice(index, 1)
}

const toggleRecording = () => ElMessage.info('语音功能联调中')
const processPaste = (e) => {
  const items = (e.clipboardData || e.originalEvent.clipboardData).items
  for (const item of items) {
    if (item.type.indexOf('image') !== -1) processImageSelect({ raw: item.getAsFile() })
  }
}

onMounted(() => editorRef.value?.focus())
</script>

<style scoped lang="scss">
.chat-input-wrapper {
  display: flex; flex-direction: column; background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light); position: relative;
}

.toolbar {
  height: 40px; padding: 0 12px; display: flex; align-items: center; gap: 4px;
  .tool-icon {
    font-size: 18px; color: var(--dt-text-tertiary); padding: 6px; cursor: pointer;
    border-radius: 4px; transition: all 0.2s;
    &:hover { background: var(--dt-bg-hover); color: var(--dt-text-primary); }
  }
}

.input-main {
  padding: 8px 16px; min-height: 80px; max-height: 200px; overflow-y: auto;
}
.rich-editor {
  width: 100%; outline: none; font-size: 14px; line-height: 1.6; color: var(--dt-text-primary);
  &:empty:before { content: attr(placeholder); color: var(--dt-text-quaternary); }
}

.input-footer {
  height: 40px; padding: 0 16px 8px; display: flex; align-items: center; justify-content: space-between;
  .tip { font-size: 11px; color: var(--dt-text-quaternary); }
}

.send-btn {
  padding: 4px 16px; border: none; border-radius: 4px; background: var(--dt-bg-hover);
  color: var(--dt-text-quaternary); font-weight: 600; cursor: not-allowed;
  &.is-active { background: var(--dt-brand-color); color: #fff; cursor: pointer; }
}

// 名片选择器样式
.dt-card-picker {
  .card-picker-search { padding: 0 20px 12px; }
  .card-tabs { :deep(.el-tabs__nav-wrap) { padding: 0 20px; } }
  .card-picker-list { max-height: 320px; overflow-y: auto; padding: 4px 12px; }
  .card-picker-item {
    display: flex; align-items: center; gap: 12px; padding: 10px 8px; border-radius: 6px; cursor: pointer;
    &:hover { background: var(--dt-bg-hover); }
    .card-avatar { width: 36px; height: 36px; border-radius: 4px; }
    .card-name { font-size: 14px; font-weight: 500; color: var(--dt-text-primary); }
    .card-dept { font-size: 12px; color: var(--dt-text-tertiary); }
  }
  .card-empty { text-align: center; padding: 40px; color: var(--dt-text-quaternary); font-size: 13px; }
}

.image-preview-bar {
  display: flex; gap: 8px; padding: 12px 16px; background: var(--dt-bg-body);
  .preview-item {
    position: relative; width: 60px; height: 60px; border-radius: 4px; overflow: hidden;
    .preview-remove {
      position: absolute; top: 2px; right: 2px; background: rgba(0,0,0,0.5); color: #fff;
      border-radius: 50%; padding: 2px; cursor: pointer;
    }
  }
}
</style>
