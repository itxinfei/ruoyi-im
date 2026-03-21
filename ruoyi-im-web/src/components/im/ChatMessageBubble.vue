<template>
  <div 
    :class="[
      'message-item', 
      isMe ? 'is-me' : 'is-other',
      { 'is-grouped': isGrouped }
    ]"
  >
    <!-- 时间分割线 (Doc-21 §8.2) -->
    <div v-if="showTime" class="system-notice time-divider">
      {{ formattedTime }}
    </div>

    <div class="message-container">
      <!-- 头像 (Doc-21 §8.4): 36x36, 圆角4px -->
      <div v-if="!isGrouped" class="avatar-wrapper">
        <img :src="message.senderAvatar" class="avatar" alt="avatar" />
      </div>
      <!-- 聚合占位 (Doc-30 §1.8.1): 隐藏头像时保留 36px 宽度占位 -->
      <div v-else class="avatar-placeholder"></div>

      <div class="message-content-wrapper">
        <!-- 昵称: 仅非聚合且非自己时显示 -->
        <div v-if="!isMe && !isGrouped" class="sender-name">
          {{ message.senderNickname }}
        </div>

        <div class="bubble-and-actions">
          <!-- 消息气泡 (Doc-21 §8.4): 钉钉标准色与圆角 -->
          <div 
            class="message-bubble" 
            @mouseenter="isHovered = true" 
            @mouseleave="isHovered = false"
          >
            <div class="text-content">{{ message.content }}</div>
            
            <!-- 已读状态 (Doc-13 §4.10): 钉钉水滴标识 -->
            <div v-if="isMe" class="read-status">
              <span :class="message.isRead ? 'read' : 'unread'">
                {{ message.isRead ? '已读' : '未读' }}
              </span>
            </div>
          </div>

          <!-- 消息操作悬浮条 (Doc-21 §8.1): Hover 触发 -->
          <transition name="fade">
            <div v-if="isHovered" class="message-action-bar">
              <button class="action-btn" title="回复" @click="processReply">
                <i class="icon-reply"></i>
              </button>
              <button class="action-btn" title="转发" @click="processForward">
                <i class="icon-forward"></i>
              </button>
              <button class="action-btn" title="更多" @click="processMore">
                <i class="icon-more"></i>
              </button>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * ChatMessageBubble.vue
 * 测试结论：严格对齐 Doc-21/30/35，纯 JS 开发
 */
import { ref, computed } from 'vue';

const props = defineProps({
  // 核心消息对象 (Doc-31)
  message: {
    type: Object,
    required: true
  },
  // 是否为自己发送
  isMe: {
    type: Boolean,
    default: false
  },
  // 是否被聚合 (Doc-30 §1.8.1 算法判定结果)
  isGrouped: {
    type: Boolean,
    default: false
  },
  // 是否显示时间分割线
  showTime: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['reply', 'forward', 'more']);

// 响应式状态 (Doc-02: 变量名严禁 data/info)
const isHovered = ref(false);

// 格式化时间 (Doc-30 §1.6: 接收 Long 时间戳)
const formattedTime = computed(() => {
  const date = new Date(props.message.createTime);
  return date.getHours().toString().padStart(2, '0') + ':' + 
         date.getMinutes().toString().padStart(2, '0');
});

// 业务动词命名 (Doc-02: 禁止 handle 前缀)
const processReply = () => {
  emit('reply', props.message);
};

const processForward = () => {
  emit('forward', props.message);
};

const processMore = () => {
  emit('more', props.message);
};
</script>

<style scoped>
/* 引用 Doc-35 钉钉视觉契约变量 */
.message-item {
  display: flex;
  flex-direction: column;
  padding: 0 var(--dt-spacing-m);
  margin-bottom: 16px; /* 默认间距 (Doc-21 §8.3) */
}

/* 连续消息间距压缩 (Doc-30 §1.8.1) */
.message-item.is-grouped {
  margin-top: -12px; /* 抵消掉部分 margin，使气泡间距变为 4px */
}

.message-container {
  display: flex;
  align-items: flex-start;
}

.is-me .message-container {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-s); /* 4px (Doc-35 §2) */
  object-fit: cover;
}

.avatar-placeholder {
  width: 36px;
}

.message-content-wrapper {
  max-width: 70%; /* Doc-21 §8.4 */
  margin: 0 var(--dt-spacing-s);
}

.sender-name {
  font-size: 12px;
  color: var(--dt-text-desc);
  margin-bottom: 4px;
}

.bubble-and-actions {
  position: relative;
  display: flex;
  align-items: center;
}

.is-me .bubble-and-actions {
  flex-direction: row-reverse;
}

.message-bubble {
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
  border-radius: var(--dt-radius-m); /* 8px (Doc-35 §2) */
  position: relative;
  word-break: break-all;
}

/* 钉钉蓝气泡 (Doc-35) */
.is-me .message-bubble {
  background-color: var(--dt-bubble-me);
  color: var(--dt-text-white);
}

/* 钉钉冷灰气泡 (Doc-35) */
.is-other .message-bubble {
  background-color: var(--dt-bubble-other);
  color: var(--dt-text-main);
}

/* 消息操作悬浮条 (Doc-21 §8.1) */
.message-action-bar {
  display: flex;
  background: #fff;
  border: 1px solid var(--dt-border-color);
  border-radius: 4px;
  padding: 4px 8px;
  margin: 0 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.action-btn {
  border: none;
  background: transparent;
  padding: 4px;
  cursor: pointer;
  color: var(--dt-text-desc);
}

.action-btn:hover {
  color: var(--dt-brand-color);
}

.read-status {
  font-size: 10px;
  margin-top: 2px;
  text-align: right;
}

.read-status .unread { color: var(--dt-brand-color); }
.read-status .read { color: var(--dt-text-desc); }

.time-divider {
  align-self: center;
  margin: 16px 0;
  color: var(--dt-text-desc);
  font-size: 12px;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
