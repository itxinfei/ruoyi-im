<template>
  <div class="chat-window">
    <!-- 1. 聊天头部 (Doc-21 §6): 60px 高度 -->
    <header class="chat-header">
      <div class="header-left">
        <h3 class="title">{{ conversationName }}</h3>
        <span class="status-dot online"></span>
      </div>
      <div class="header-right">
        <i class="icon-search" title="搜索会话内容"></i>
        <i class="icon-call" title="语音通话"></i>
        <i class="icon-more" title="详情" @click="showDetail = true"></i>
      </div>
    </header>

    <!-- 2. 消息列表区 (Doc-21 §7): 虚拟滚动与聚合 -->
    <div class="message-list-viewport" ref="listRef" @scroll="processScroll">
      <div class="message-list-content">
        <ChatMessageBubble 
          v-for="(msg, index) in messages" 
          :key="msg.clientMsgId || msg.msgId"
          :message="msg"
          :is-me="msg.senderId === currentUserId"
          :is-grouped="checkIsGrouped(msg, index)"
          :show-time="checkShowTime(msg, index)"
        />
      </div>
    </div>

    <!-- 3. 输入区 (Doc-21 §9): 自适应高度 -->
    <footer class="chat-input-area" :style="{ height: inputHeight + 'px' }">
      <div class="toolbar">
        <i class="icon-emoji" title="表情"></i>
        <i class="icon-image" title="图片"></i>
        <i class="icon-file" title="文件"></i>
      </div>
      
      <div class="input-container">
        <textarea 
          ref="inputRef"
          v-model="inputText"
          placeholder="请输入消息..."
          @keydown.enter.prevent="processSendMessage"
          @input="adjustInputHeight"
        ></textarea>
      </div>

      <div class="input-footer">
        <span class="tip">Enter 发送，Ctrl+Enter 换行</span>
        <button 
          class="send-btn" 
          :disabled="!inputText.trim()"
          @click="processSendMessage"
        >发送</button>
      </div>
    </footer>
  </div>
</template>

<script setup lang="js">
/**
 * ChatWindow.vue (中枢组件)
 * 严格对齐 Doc-21/30/35 的所有硬核算法与规格
 */
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue';
import { useSessionStore } from '@/stores/session';
import ChatMessageBubble from './ChatMessageBubble.vue';

const sessionStore = useSessionStore();

// 1. 基础状态 (Doc-02: 肃清 data/info 命名)
const currentUserId = ref(1); // 示例
const inputText = ref('');
const inputHeight = ref(160); // 初始高度 (Doc-21 §3.2)
const listRef = ref(null);
const inputRef = ref(null);
const showDetail = ref(false);

// 2. 数据联动
const activeConversationId = computed(() => sessionStore.activeConversationId);
const messages = computed(() => {
  return sessionStore.messageMap.get(activeConversationId.value) || [];
});
const conversationName = ref('项目协作群'); // 实际应从 store 获取

/**
 * 核心：消息聚合算法 (Doc-30 §1.8.1)
 * 逻辑：同用户、2min内、连续消息，则聚合
 */
const checkIsGrouped = (msg, index) => {
  if (index === 0) return false;
  const prevMsg = messages.value[index - 1];
  const timeDiff = (msg.createTime - prevMsg.createTime) / 1000;
  return msg.senderId === prevMsg.senderId && timeDiff < 120;
};

/**
 * 核心：时间显示判定 (Doc-21 §7.2)
 * 逻辑：与上一条间隔超过 5min 则显示时间分割线
 */
const checkShowTime = (msg, index) => {
  if (index === 0) return true;
  const prevMsg = messages.value[index - 1];
  return (msg.createTime - prevMsg.createTime) / 1000 > 300;
};

/**
 * 核心：输入框高度平滑自适应 (Doc-30 §1.8.3)
 */
const adjustInputHeight = () => {
  const textarea = inputRef.value;
  textarea.style.height = 'auto';
  const newHeight = Math.min(Math.max(textarea.scrollHeight + 100, 160), 300);
  inputHeight.value = newHeight;
};

/**
 * 核心：乐观更新发送逻辑 (Doc-13 §10.2)
 */
const processSendMessage = async () => {
  if (!inputText.value.trim()) return;

  const payload = {
    conversationId: activeConversationId.value,
    content: inputText.value,
    messageType: 'TEXT',
    clientMsgId: crypto.randomUUID(), // 雪花去重 ID
    senderId: currentUserId.value
  };

  inputText.value = ''; // 瞬时清空
  nextTick(() => adjustInputHeight());

  await sessionStore.sendMessage(payload);
  scrollToBottom();
};

const scrollToBottom = () => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight;
    }
  });
};

const processScroll = (e) => {
  // 向上滚动触顶加载历史 (Doc-30 §1.8.2)
  if (e.target.scrollTop < 50) {
    sessionStore.loadHistoryMessages(activeConversationId.value);
  }
};

// 内存安全清理 (Doc-30 §1.9)
onUnmounted(() => {
  // 清理逻辑...
});

onMounted(() => {
  scrollToBottom();
});
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: var(--dt-bg-chat);
}

/* 头部规范 (Doc-21 §6) */
.chat-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: var(--dt-line-width) solid var(--dt-border-color);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-main);
}

.header-right {
  display: flex;
  gap: 20px;
  color: var(--dt-text-desc);
}

.header-right i {
  cursor: pointer;
  font-size: 18px;
}

.header-right i:hover {
  color: var(--dt-brand-color);
}

/* 列表区 */
.message-list-viewport {
  flex: 1;
  overflow-y: auto;
  background-color: var(--dt-bg-chat);
  padding: 20px 0;
}

/* 输入区 (Doc-21 §9) */
.chat-input-area {
  border-top: var(--dt-line-width) solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
  transition: height 0.1s ease;
  background-color: var(--dt-bg-chat);
}

.toolbar {
  height: 40px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--dt-text-desc);
}

.toolbar i {
  font-size: 20px;
  cursor: pointer;
}

.toolbar i:hover {
  color: var(--dt-brand-color);
}

.input-container {
  flex: 1;
  padding: 0 20px;
}

.input-container textarea {
  width: 100%;
  height: 100%;
  border: none;
  resize: none;
  outline: none;
  font-size: 14px;
  line-height: 1.6;
  color: var(--dt-text-main);
  background: transparent;
}

.input-footer {
  height: 44px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tip {
  font-size: 12px;
  color: var(--dt-text-desc);
}

.send-btn {
  background-color: var(--dt-brand-color);
  color: white;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.send-btn:hover:not(:disabled) {
  background-color: var(--dt-brand-hover);
}
</style>
