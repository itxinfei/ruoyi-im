<template>
  <div
    class="message-item message-enter"
    :class="{ 'is-own': message.isOwn }"
  >
    <!-- 时间分割线 (如果是时间消息) -->
    <div v-if="message.isTimeDivider" class="time-divider">
      <span class="time-text">{{ message.timeText }}</span>
    </div>

    <template v-else>
      <!-- 头像 -->
      <div
        class="avatar-container"
        @contextmenu.prevent="$emit('at', message)"
        @click="$emit('show-user', message.senderId)"
        title="右键 @提及，左键查看资料"
      >
        <DingtalkAvatar
          :src="message.senderAvatar"
          :name="message.senderName"
          :user-id="message.senderId"
          :size="36"
          shape="square"
          custom-class="message-avatar"
        />
      </div>

      <div class="content-wrapper">
        <!-- 发送者姓名 -->
        <div v-if="!message.isOwn" class="sender-name">{{ message.senderName }}</div>

        <div class="message-content-main">
          <!-- 悬停快捷按钮区 (还原钉钉微交互) -->
          <div class="message-actions-floating" v-if="message.type !== 'RECALLED'">
            <div class="action-bar-min">
               <el-tooltip content="回复" placement="top" :show-after="400">
                  <button class="mini-btn" @click="$emit('reply', message)"><el-icon><ChatLineSquare /></el-icon></button>
               </el-tooltip>
               <el-tooltip content="点赞" placement="top" :show-after="400">
                  <button class="mini-btn" @click="$emit('reaction', message, '👍')">👍</button>
               </el-tooltip>
               <el-dropdown @command="(c) => $emit('command', c, message)" trigger="click">
                  <button class="mini-btn"><el-icon><MoreFilled /></el-icon></button>
                  <template #dropdown>
                    <el-dropdown-menu>
                       <el-dropdown-item command="forward">转发</el-dropdown-item>
                       <el-dropdown-item command="copy" v-if="message.type === 'TEXT'">复制</el-dropdown-item>
                       <el-dropdown-item command="todo">设为待办</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
               </el-dropdown>
            </div>
          </div>

          <!-- 消息气泡内容插槽 -->
          <slot name="bubble"></slot>
        </div>

        <!-- 消息页脚 (状态与时间) -->
        <div class="message-footer">
          <div v-if="message.isOwn" class="status-container">
            <!-- 发送中 / 上传中 -->
            <el-icon v-if="['sending', 'uploading'].includes(message.status)" class="is-loading status-icon"><Loading /></el-icon>
            
            <!-- 发送失败 -->
            <el-icon v-else-if="message.status === 'failed'" class="status-icon error" title="发送失败，点击重试" @click="$emit('retry', message)">
              <WarningFilled />
            </el-icon>

            <!-- 已读状态 (钉钉风格：不分人数显示，仅显示已读/未读) -->
            <div v-else class="read-status" :class="{ 'read': message.readCount > 0, 'unread': message.readCount === 0 }">
              {{ message.readCount > 0 ? '已读' : '未读' }}
            </div>
          </div>
          <div class="time">{{ formattedTime }}</div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ChatLineSquare, MoreFilled, Loading, WarningFilled } from '@/utils/icons'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  message: { type: Object, required: true }
})

defineEmits(['reply', 'reaction', 'command', 'scroll-to', 'at', 'show-user', 'retry'])

const formattedTime = computed(() => {
  if (!props.message.timestamp) return ''
  const date = new Date(props.message.timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })
})
</script>

<style scoped lang="scss">
.message-item {
  display: flex; margin-bottom: 20px; position: relative; padding: 0 16px;
  &.is-own { flex-direction: row-reverse; }
}

.time-divider {
  width: 100%; text-align: center; margin: 12px 0;
  .time-text { background: rgba(0, 0, 0, 0.04); color: #8f959e; font-size: 11px; padding: 2px 10px; border-radius: 10px; }
}

.avatar-container {
  margin: 0 10px; flex-shrink: 0; cursor: pointer; transition: opacity 0.2s;
  &:hover { opacity: 0.85; }
}

.content-wrapper { max-width: 85%; display: flex; flex-direction: column; }

.sender-name { font-size: 12px; color: #8c8c8c; margin-bottom: 4px; padding: 0 4px; }
.is-own .sender-name { text-align: right; }

.message-content-main {
  position: relative;
  &:hover .message-actions-floating { opacity: 1; transform: translateY(0); }
}

.message-actions-floating {
  position: absolute; top: -28px; left: 0; 
  opacity: 0; transform: translateY(4px); transition: all 0.2s; z-index: 10;
  
  .action-bar-min {
    display: flex; gap: 2px; padding: 2px 6px; background: #fff; border-radius: 6px;
    border: 1px solid #f0f1f2; box-shadow: 0 2px 8px rgba(0,0,0,0.08);

    .mini-btn {
      background: none; border: none; padding: 4px; color: #646a73; cursor: pointer;
      display: flex; align-items: center; border-radius: 4px; font-size: 14px;
      &:hover { background: #f2f3f5; color: #1677ff; }
    }
  }
}
.is-own .message-actions-floating { left: auto; right: 0; }

.message-footer {
  display: flex; align-items: center; gap: 8px; margin-top: 4px; font-size: 11px;
}
.is-own .message-footer { flex-direction: row-reverse; }

.read-status {
  &.unread { color: var(--dt-unread-color); }
  &.read { color: var(--dt-read-color); }
}

.status-icon {
  font-size: 14px; color: #8c8c8c;
  &.error { color: #ff4d4f; cursor: pointer; }
}

.time { color: #bfbfbf; }

:global(.dark) {
  .message-actions-floating .action-bar-min { background: #1e293b; border-color: #334155; }
  .mini-btn:hover { background: rgba(255,255,255,0.1) !important; }
}
</style>
