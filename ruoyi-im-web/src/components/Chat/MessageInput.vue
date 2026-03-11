<template>
  <div class="message-input">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button :icon="Orange" link @click="insertEmoji('😀')" />
      <el-button :icon="Picture" link @click="triggerUpload('image')" />
      <el-button :icon="Files" link @click="triggerUpload('file')" />
    </div>

    <!-- 输入区 -->
    <div class="text-area">
      <textarea
        v-model="content"
        placeholder="请输入消息..."
        @keydown.enter.prevent="handleSend"
      ></textarea>
    </div>

    <!-- 发送按钮 -->
    <div class="footer">
      <el-button 
        type="primary" 
        size="small" 
        :disabled="!content.trim()"
        @click="handleSend"
      >
        发送
      </el-button>
    </div>

    <input type="file" ref="fileRef" hidden @change="onFileChange" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Orange, Picture, Files } from '@element-plus/icons-vue'

const props = defineProps({ session: Object })
const emit = defineEmits(['send', 'upload'])

const content = ref('')
const fileRef = ref(null)

const handleSend = () => {
  if (!content.value.trim()) return
  emit('send', { content: content.value.trim(), type: 'TEXT' })
  content.value = ''
}

const insertEmoji = (emoji) => {
  content.value += emoji
}

const triggerUpload = (type) => {
  fileRef.value.click()
}

const onFileChange = (e) => {
  const file = e.target.files[0]
  if (file) emit('upload', file)
}
</script>

<style scoped lang="scss">
.message-input {
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
  padding: 8px 16px;
  display: flex;
  flex-direction: column;
}

.toolbar {
  height: 32px;
  display: flex;
  align-items: center;
  gap: 8px;
  .el-button { font-size: 20px; color: var(--dt-text-secondary); &:hover { color: var(--dt-brand-color); } }
}

.text-area {
  flex: 1;
  padding: 8px 0;
  textarea {
    width: 100%;
    height: 80px;
    border: none;
    outline: none;
    resize: none;
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
    background: transparent;
    &::placeholder { color: var(--dt-text-quaternary); }
  }
}

.footer {
  display: flex;
  justify-content: flex-end;
}
</style>
