<template>
  <div
    class="message-item message-enter"
    :class="{ 'is-own': message.isOwn, 'is-multi-select': multiSelectMode, 'is-merged': message.isMerged }"
  >
    <!-- 时间分割线 (如果是时间消息) -->
    <div v-if="message.isTimeDivider" class="time-divider">
      <span class="time-text">{{ message.timeText }}</span>
    </div>

    <template v-else>
      <!-- 多选复选框 -->
      <div v-if="multiSelectMode" class="checkbox-container" :class="{ 'is-merged': message.isMerged }">
        <el-checkbox
          :model-value="isSelected"
          @change="handleCheckboxChange"
          @click.stop
        />
      </div>

      <!-- 头像 - 合并状态下隐藏 -->
      <div
        v-show="!message.isMerged"
        class="avatar-container"
        :class="{ 'is-merged': message.isMerged }"
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

      <div class="content-wrapper" :class="{ 'is-merged': message.isMerged }">
        <div class="message-content-main">
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
import { useStore } from 'vuex'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const store = useStore()

const props = defineProps({
  message: { type: Object, required: true },
  multiSelectMode: { type: Boolean, default: false }
})

const emit = defineEmits(['reply', 'reaction', 'command', 'scroll-to', 'at', 'show-user', 'retry', 'toggle-select'])

// 消息是否被选中
const isSelected = computed(() => {
  return store.state.im.message.selectedMessages.has(props.message.id)
})

// 处理复选框变化
const handleCheckboxChange = () => {
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', props.message.id)
}

const formattedTime = computed(() => {
  if (!props.message.timestamp) return ''
  const date = new Date(props.message.timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-item {
  display: flex;
  margin-bottom: 16px;
  position: relative;
  padding: 0 16px;
  animation: fadeInUp 0.3s var(--dt-ease-out) both;

  &.is-own {
    flex-direction: row-reverse;
  }

  // 对方的消息：头像和气泡顶部对齐（钉钉风格）
  &:not(.is-own) {
    align-items: flex-start;
  }

  // 自己的消息：头像和气泡顶部对齐
  &.is-own {
    align-items: flex-start;
  }
}

.time-divider {
  width: 100%;
  text-align: center;
  margin: 12px 0;

  .time-text {
    background: var(--dt-bg-body);
    color: var(--dt-text-tertiary);
    font-size: 11px;
    padding: 3px 10px;
    border-radius: 4px;
  }
}

// 多选复选框容器
.checkbox-container {
  display: flex;
  align-items: flex-start;
  padding-top: 8px;
  margin: 0 8px;
  flex-shrink: 0;

  // 合并状态：隐藏但保持布局
  &.is-merged {
    visibility: hidden;
    margin: 0;
    padding: 0;
    width: 0;
  }

  :deep(.el-checkbox) {
    .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
    }
  }
}

// 多选模式下的消息项样式
.message-item.is-multi-select {
  padding: 0 8px;
  cursor: pointer;
  border-radius: var(--dt-radius-md);
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.is-own {
    flex-direction: row-reverse;
  }
}

.avatar-container {
  margin: 0 10px;
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity var(--dt-transition-base);

  &:hover {
    opacity: 0.85;
  }

  .message-avatar {
    border-radius: 4px;
  }

  // 合并状态：隐藏但仍占据空间以保持对齐
  &.is-merged {
    visibility: hidden;
    margin: 0;
    width: 0;
  }
}

.content-wrapper {
  max-width: 85%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;

  // 合并状态：减少上边距让气泡更紧凑
  &.is-merged {
    margin-top: -8px;
  }
}

// 对方的消息：气泡顶部与头像顶部对齐（钉钉风格）
.message-item:not(.is-own) .content-wrapper {
  align-items: flex-start;
}

// 自己的消息：气泡右对齐，紧靠头像（钉钉风格）
.is-own .content-wrapper {
  align-items: flex-end;
}

.sender-name {
  font-size: 12px;
  color: var(--dt-text-secondary);
  margin-bottom: 4px;
  padding: 0 4px;
}

.is-own .sender-name {
  text-align: right;
}

.message-content-main {
  position: relative;
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 11px;
}

.is-own .message-footer {
  flex-direction: row-reverse;
}

.read-status {
  &.unread {
    color: var(--dt-brand-color);
  }
  &.read {
    color: var(--dt-text-quaternary);
  }
}

.status-icon {
  font-size: 14px;
  color: var(--dt-text-secondary);
  transition: color var(--dt-transition-fast);

  &.error {
    color: var(--dt-error-color);
    cursor: pointer;

    &:hover {
      color: #d9363e;
    }
  }
}

.time {
  color: var(--dt-text-quaternary);
  font-variant-numeric: tabular-nums;
}
</style>
