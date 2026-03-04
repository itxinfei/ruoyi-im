<template>
  <div class="message-bubble" :class="{ 'is-own': message.isOwn }">
    <!-- 文本消息 -->
    <div v-if="message.type === 'TEXT'" class="text-content">
      {{ message.content }}
    </div>

    <!-- 图片消息 -->
    <div v-else-if="message.type === 'IMAGE'" class="image-content">
      <el-image 
        :src="message.content" 
        :preview-src-list="[message.content]"
        fit="cover"
        class="img"
      />
    </div>

    <!-- 文件消息 -->
    <div v-else-if="message.type === 'FILE'" class="file-content">
      <el-icon><Document /></el-icon>
      <span class="file-name">{{ message.fileName || '未知文件' }}</span>
    </div>

    <!-- 文档消息 -->
    <div v-else-if="message.type === 'DOCUMENT'" class="document-content" @click="handleDocumentClick">
      <div class="doc-card">
        <div class="doc-icon" :class="getDocumentIconClass(message.documentType)">
          <span class="material-icons-outlined">{{ getDocumentIcon(message.documentType) }}</span>
        </div>
        <div class="doc-info">
          <div class="doc-title">{{ message.documentTitle || '未知文档' }}</div>
          <div class="doc-meta">
            <span>{{ message.documentCreator || '我' }}</span>
            <span>·</span>
            <span>{{ formatTime(message.createTime) }}</span>
          </div>
        </div>
        <div class="doc-action">
          <span class="material-icons-outlined">open_in_new</span>
        </div>
      </div>
    </div>

    <!-- 撤回消息 -->
    <div v-else-if="message.type === 'RECALLED'" class="recalled">
      消息已撤回
    </div>

    <!-- 兜底 -->
    <div v-else class="other-content">
      [不支持的消息类型]
    </div>

    <!-- 文档预览抽屉 -->
    <DocumentPreviewDrawer
      ref="documentPreviewRef"
      @close="handlePreviewClose"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Document } from '@element-plus/icons-vue'
import DocumentPreviewDrawer from './DocumentPreviewDrawer.vue'
import { formatTime } from '@/utils/format'

const props = defineProps({
  message: Object
})

const documentPreviewRef = ref(null)

// 获取文档图标
const getDocumentIcon = (type) => {
  const icons = {
    'TEXT': 'description',
    'FILE': 'description',
    'IMAGE': 'image',
    'VIDEO': 'video_library',
    'VOICE': 'mic'
  }
  return icons[type] || 'description'
}

// 获取文档图标样式
const getDocumentIconClass = (type) => {
  const classes = {
    'TEXT': 'icon-doc',
    'FILE': 'icon-doc',
    'IMAGE': 'icon-image',
    'VIDEO': 'icon-video',
    'VOICE': 'icon-voice'
  }
  return classes[type] || 'icon-doc'
}

// 点击文档消息
const handleDocumentClick = () => {
  if (props.message.documentId) {
    documentPreviewRef.value?.open(props.message.documentId)
  }
}

// 预览关闭回调
const handlePreviewClose = () => {
  // 可以在这里处理预览关闭后的逻辑
}
</script>

<style scoped lang="scss">
.message-bubble {
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  color: #1f1f1f;

  &.is-own {
    background: #e7f3ff;
    border-color: #cce5ff;
  }
}

.image-content {
  border-radius: 4px;
  overflow: hidden;
  .img { max-width: 240px; display: block; }
}

.file-content {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1677ff;
  .file-name { text-decoration: underline; cursor: pointer; }
}

.document-content {
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .doc-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f8fafc;
    border-radius: 8px;
    min-width: 240px;
    
    .doc-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      
      &.icon-doc { background: #e6f4ff; color: #1677ff; }
      &.icon-image { background: #f0f5ff; color: #1890ff; }
      &.icon-video { background: #fff0f6; color: #eb2f96; }
      &.icon-voice { background: #fffbe6; color: #fa8c16; }
    }
    
    .doc-info {
      flex: 1;
      
      .doc-title {
        font-size: 14px;
        font-weight: 500;
        color: #1f2329;
        margin-bottom: 4px;
      }
      
      .doc-meta {
        font-size: 12px;
        color: #8f959e;
      }
    }
    
    .doc-action {
      color: #8f959e;
    }
  }
}

.recalled {
  color: #bfbfbf;
  font-size: 12px;
}
</style>
