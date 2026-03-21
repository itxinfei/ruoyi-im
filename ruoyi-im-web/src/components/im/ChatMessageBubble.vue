<template>
  <div 
    :class="[
      'message-item', 
      isMe ? 'is-me' : 'is-other',
      { 'is-grouped': isGrouped }
    ]"
  >
    <div v-if="showTime" class="system-notice time-divider">
      {{ formattedTime }}
    </div>

    <div class="message-container">
      <div v-if="!isGrouped" class="avatar-wrapper">
        <img :src="message.senderAvatar || '/avatars/default.png'" class="avatar" alt="avatar" />
      </div>
      <div v-else class="avatar-placeholder"></div>

      <div class="message-content-wrapper">
        <div v-if="!isMe && !isGrouped" class="sender-name">{{ message.senderName }}</div>

        <div class="bubble-and-actions">
          <div 
            :class="['message-bubble', `type-${(message.type || 'text').toLowerCase()}`]"
            @mouseenter="isHovered = true" 
            @mouseleave="isHovered = false"
          >
            <!-- 文本 -->
            <div v-if="!message.type || message.type === 'TEXT'" class="text-content">
              {{ message.content }}
            </div>

            <!-- 图片 -->
            <div v-else-if="message.type === 'IMAGE'" class="image-content">
              <el-image :src="message.fileUrl" :preview-src-list="[message.fileUrl]" fit="cover" class="content-img" />
            </div>

            <!-- 已读状态 -->
            <div v-if="isMe" class="read-status">
              <span :class="message.isRead ? 'read' : 'unread'">
                {{ message.isRead ? '已读' : '未读' }}
              </span>
            </div>
          </div>

          <!-- 表情回应 -->
          <MessageReaction :message-id="message.id" :reactions="message.reactions" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * ChatMessageBubble.vue (Vuex 修复版)
 */
import { ref, computed } from 'vue';
import { useStore } from 'vuex';
import MessageReaction from './MessageReaction.vue';

const props = defineProps({
  message: Object,
  isMe: Boolean,
  isGrouped: Boolean,
  showTime: Boolean
});

const store = useStore();
const isHovered = ref(false);

const formattedTime = computed(() => {
  if (!props.message.createTime) return '';
  const d = new Date(props.message.createTime);
  return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`;
});
</script>

<style scoped>
.message-item { display: flex; flex-direction: column; padding: 0 16px; margin-bottom: 12px; }
.message-item.is-grouped { margin-top: -8px; }
.message-container { display: flex; align-items: flex-start; }
.is-me .message-container { flex-direction: row-reverse; }
.avatar { width: 36px; height: 36px; border-radius: 4px; }
.avatar-placeholder { width: 36px; }
.message-content-wrapper { max-width: 75%; margin: 0 8px; }
.bubble-and-actions { position: relative; display: flex; align-items: center; }
.is-me .bubble-and-actions { flex-direction: row-reverse; }
.message-bubble { padding: 8px 12px; border-radius: 8px; font-size: 14px; line-height: 1.6; }
.is-me .message-bubble { background-color: var(--dt-bubble-me); color: #fff; }
.is-other .message-bubble { background-color: var(--dt-bubble-other); color: var(--dt-text-main); }
.content-img { border-radius: 4px; max-width: 200px; display: block; }
.read-status { font-size: 10px; margin-top: 2px; text-align: right; }
.read-status .unread { color: var(--dt-brand-color); }
.read-status .read { color: var(--dt-text-desc); }
.time-divider { align-self: center; margin: 16px 0; color: var(--dt-text-desc); font-size: 12px; }
</style>
