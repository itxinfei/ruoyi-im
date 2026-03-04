<template>
  <div class="bubble-box" :class="{ 'is-own': message.isOwn }">
    <div v-if="normalizedType === 'TEXT'" class="text-bubble" :class="{ 'is-own': message.isOwn }">
      <div v-if="message.replyTo" class="reply-ref" @click.stop="$emit('scroll-to', message.replyTo.id)">
        <span class="ref-user">{{ message.replyTo.senderName }}:</span>
        <span class="ref-text">{{ message.replyTo.content }}</span>
      </div>
      <div class="msg-text" v-html="parsedText"></div>
      <span v-if="message.isEdited" class="edited-flag">(已编辑)</span>
    </div>

    <div v-else-if="normalizedType === 'IMAGE'" class="image-bubble" @click="$emit('preview', parsedContent.imageUrl || parsedContent.url)">
      <img :src="parsedContent.imageUrl || parsedContent.url" class="msg-img" />
    </div>

    <div v-else-if="normalizedType === 'FILE'" class="file-bubble" @click="$emit('download', parsedContent)">
      <el-icon class="file-icon"><Document /></el-icon>
      <div class="file-info">
        <div class="file-name">{{ parsedContent.fileName || parsedContent.name }}</div>
        <div class="file-meta">{{ formatSize(parsedContent.size) }}</div>
      </div>
    </div>

    <div v-else-if="normalizedType === 'SYSTEM'" class="system-msg"><span>{{ message.content }}</span></div>

    <div v-else-if="normalizedType === 'RECALLED'" class="recalled-msg">
      <span class="material-icons-outlined">block</span>
      <span>{{ message.isOwn ? '你撤回了一条消息' : `${message.senderName}撤回了一条消息` }}</span>
    </div>

    <div v-else-if="normalizedType === 'LINK'" class="link-bubble">
      <LinkCardMessage :message="message" />
    </div>

    <!-- 兜底显示未知类型 -->
    <div v-else class="text-bubble" :class="{ 'is-own': message.isOwn }">
      <div class="msg-text" style="color: #ff4d4f;">[不支持的消息类型: {{ message.type }}]</div>
      <div class="msg-text">{{ message.content }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Document } from '@element-plus/icons-vue'
import LinkCardMessage from './LinkCardMessage.vue'

const props = defineProps({ message: Object })

const normalizedType = computed(() => {
  return props.message?.type ? props.message.type.toUpperCase() : 'TEXT'
})

const parsedContent = computed(() => {
  if (!props.message?.content) return {}
  try { return typeof props.message.content === 'string' ? JSON.parse(props.message.content) : props.message.content }
  catch { return { content: props.message.content } }
})

const parsedText = computed(() => {
  if (normalizedType.value !== 'TEXT') return ''
  if (!props.message?.content) return ''
  return String(props.message.content).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank" class="msg-link">$1</a>')
})

const formatSize = (s) => {
  if (!s) return '0B'; const k = 1024, sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(s) / Math.log(k))
  return parseFloat((s / Math.pow(k, i)).toFixed(2)) + sizes[i]
}
</script>

<style scoped lang="scss">
.bubble-box {
  display: flex;
  flex-direction: column;
  max-width: 100%;
  min-width: 44px;
  position: relative;
  align-items: flex-start;

  &.is-own {
    align-items: flex-end;
  }
}

.text-bubble {
  background: #ffffff;
  padding: 10px 14px;
  border-radius: 4px 12px 12px 12px;
  border: 1px solid rgba(31, 35, 41, 0.12);
  color: #1f2329;
  font-size: 15px;
  line-height: 1.5;
  position: relative;
  word-wrap: break-word;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

  &.is-own {
    background: #e8f4ff;
    border-color: rgba(0, 126, 255, 0.2);
    border-radius: 12px 4px 12px 12px;
    box-shadow: 0 1px 2px rgba(0, 126, 255, 0.06);
  }

  .reply-ref {
    background: rgba(0, 0, 0, 0.05);
    border-left: 3px solid #1677ff;
    padding: 6px 10px;
    margin-bottom: 8px;
    border-radius: 4px;
    font-size: 13px;
    color: #646a73;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: rgba(0, 0, 0, 0.08);
    }

    .ref-user {
      font-weight: 500;
      margin-right: 4px;
      color: #1f2329;
    }

    .ref-text {
      opacity: 0.8;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .msg-text {
    white-space: pre-wrap;
    :deep(.msg-link) {
      color: #1677ff;
      text-decoration: none;
      &:hover {
        text-decoration: underline;
      }
    }
  }

  .edited-flag {
    font-size: 11px;
    color: #8f959e;
    margin-top: 4px;
    display: block;
    text-align: right;
  }
}

.image-bubble {
  cursor: zoom-in;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(31, 35, 41, 0.1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.01);
  }

  .msg-img {
    max-width: 320px;
    max-height: 320px;
    display: block;
    object-fit: contain;
  }
}

.file-bubble {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #ffffff;
  border: 1px solid rgba(31, 35, 41, 0.12);
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  min-width: 240px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;

  &:hover {
    border-color: #1677ff;
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.1);
    background: #f8fbff;
  }

  .file-icon {
    font-size: 36px;
    color: #1677ff;
  }

  .file-info {
    flex: 1;
    min-width: 0;
    .file-name {
      font-size: 14px;
      font-weight: 500;
      color: #1f2329;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .file-meta {
      font-size: 12px;
      color: #8f959e;
      margin-top: 4px;
    }
  }
}

.system-msg {
  width: 100%;
  text-align: center;
  margin: 12px 0;
  span {
    background: rgba(0, 0, 0, 0.06);
    padding: 3px 12px;
    border-radius: 12px;
    font-size: 12px;
    color: #646a73;
  }
}

.recalled-msg {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #8f959e;
  padding: 4px 0;
  .material-icons-outlined {
    font-size: 16px;
  }
}

.link-bubble {
  max-width: 320px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>
