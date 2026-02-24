<template>
  <div v-if="message.isTimeDivider" class="time-divider">
    <span class="time-text">{{ message.timeText }}</span>
  </div>

  <div
    v-else
    class="message-item"
    :class="[message.isOwn ? 'is-right' : 'is-left']"
    :data-id="message.id"
  >
    <!-- 头像：钉钉风格方圆角 -->
    <div class="avatar-box" @click="$emit('show-user', message.senderId)">
      <DingtalkAvatar
        :src="message.senderAvatar"
        :name="message.senderName"
        :user-id="message.senderId"
        :size="36"
        shape="square"
      />
    </div>

    <!-- 消息内容 -->
    <div class="content-box">
      <!-- 名字 (仅群聊且非己方显示) -->
      <div v-if="sessionType === 'GROUP' && !message.isOwn" class="sender-name">
        {{ message.senderName }}
      </div>

      <div class="bubble-row">
        <slot name="bubble">
          <MessageBubble
            :message="message"
            :session-type="sessionType"
            @command="$emit('command', $event, message)"
            @preview="$emit('preview', $event)"
            @re-edit="$emit('re-edit', $event)"
          />
        </slot>
        
        <div v-if="message.isOwn" class="status-box">
          <slot name="read-status" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import MessageBubble from './MessageBubble.vue'

defineProps({
  message: { type: Object, required: true },
  sessionType: { type: String, default: 'PRIVATE' }
})

defineEmits(['command', 'show-user', 'preview', 're-edit'])
</script>

<style scoped lang="scss">
.message-item {
  display: flex;
  padding: 0 24px; // 增加左右内边距
  margin-bottom: 20px; // 提升至 20px，增加呼吸感
  position: relative;
  
  &.is-right { flex-direction: row-reverse; }
}

.avatar-box {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  cursor: pointer;
  margin-top: 2px; // 对齐气泡首行
  
  :deep(.dingtalk-avatar) {
    border-radius: 4px !important;
  }
}

.content-box {
  display: flex;
  flex-direction: column;
  max-width: calc(100% - 100px);
  margin: 0 10px; // 头像与气泡的间距
  min-width: 0;
}

.sender-name {
  font-size: 12px;
  color: #86909C;
  margin-bottom: 4px;
  margin-left: 2px;
}

.bubble-row {
  display: flex;
  align-items: flex-end;
  gap: 6px;
}

.is-right .bubble-row { flex-direction: row-reverse; }

.status-box { flex-shrink: 0; margin-bottom: 4px; }

.time-divider {
  display: flex; align-items: center; justify-content: center; margin: 24px 0;
  .time-text { font-size: 12px; color: #86909C; background: #F2F3F5; padding: 2px 10px; border-radius: 10px; }
}
</style>
