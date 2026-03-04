<template>
  <div class="message-item" :class="{ 'is-own': message.isOwn }">
    <!-- 头像 -->
    <el-avatar 
      shape="square" 
      :size="36" 
      :src="message.senderAvatar" 
      @click="$emit('show-user', message.senderId)"
      class="avatar"
    >
      {{ message.senderName?.charAt(0) }}
    </el-avatar>

    <div class="content">
      <!-- 昵称 (对方才显示) -->
      <div v-if="!message.isOwn" class="nickname">{{ message.senderName }}</div>
      
      <!-- 消息主区 (气泡) -->
      <div class="bubble-wrapper">
        <slot name="bubble"></slot>
        
        <!-- 状态标识 (己方才显示) -->
        <div v-if="message.isOwn" class="status">
          <span v-if="message.status === 'sending'" class="loading">...</span>
          <span v-else :class="{ 'read': message.readCount > 0 }">
            {{ message.readCount > 0 ? '已读' : '未读' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ message: Object })
defineEmits(['show-user'])
</script>

<style scoped lang="scss">
.message-item {
  display: flex;
  padding: 0 16px;
  gap: 12px;
  
  &.is-own {
    flex-direction: row-reverse;
    .content {
      align-items: flex-end;
    }
    .bubble-wrapper {
      flex-direction: row-reverse;
    }
  }
}

.avatar { cursor: pointer; flex-shrink: 0; }

.content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.nickname {
  font-size: 12px;
  color: #8c8c8c;
}

.bubble-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.status {
  font-size: 11px;
  color: #bfbfbf;
  margin-bottom: 4px;
  .read { color: #1677ff; }
}
</style>
