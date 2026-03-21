<template>
  <div class="chat-window">
    <!-- 1. 聊天头部 -->
    <header class="chat-header">
      <div class="header-left">
        <h3 class="title">{{ currentSession?.name || '聊天' }}</h3>
        <span v-if="currentSession?.memberCount" class="member-count">({{ currentSession.memberCount }})</span>
      </div>
      <div class="header-right">
        <i class="el-icon-search" title="搜索会话内容"></i>
        <i class="el-icon-phone" title="语音通话"></i>
        <i class="el-icon-more" title="详情" @click="detailDrawerVisible = true"></i>
      </div>
    </header>

    <!-- 2. 消息列表区 -->
    <div class="message-list-viewport" ref="listRef">
      <div class="message-list-content">
        <ChatMessageBubble 
          v-for="(msg, index) in messages" 
          :key="msg.clientMsgId || msg.id"
          :message="msg"
          :is-me="msg.senderId === currentUserId"
          :is-grouped="checkIsGrouped(msg, index)"
          :show-time="checkShowTime(msg, index)"
          @reply="processReply"
        />
      </div>
    </div>

    <!-- 3. 输入区 -->
    <ChatInputArea 
      :replying-message="replyingMessage"
      @send="processSendMessage"
      @clear-reply="replyingMessage = null"
    />

    <!-- 业务侧边栏池 -->
    <GroupDetailDrawer 
      v-if="currentSession"
      v-model="detailDrawerVisible" 
      :group-id="currentSession.id" 
    />
  </div>
</template>

<script setup lang="js">
/**
 * ChatWindow.vue (Vuex 路径修复版)
 */
import { ref, computed, nextTick, onMounted } from 'vue';
import { useStore } from 'vuex';
import ChatMessageBubble from './ChatMessageBubble.vue';
import GroupDetailDrawer from './GroupDetailDrawer.vue';
import ChatInputArea from './ChatInputArea.vue';

const store = useStore();

// 1. 基础状态
const listRef = ref(null);
const detailDrawerVisible = ref(false);
const replyingMessage = ref(null);

// 2. 数据联动 (修正：嵌套路径 im -> message)
const currentUserId = computed(() => store.state.im?.currentUser?.id || 1);
const currentSession = computed(() => store.state.im?.session?.currentSession);
const messages = computed(() => store.state.im?.message?.messages || []);

const checkIsGrouped = (msg, index) => {
  if (index === 0) return false;
  const prevMsg = messages.value[index - 1];
  const timeDiff = (new Date(msg.createTime) - new Date(prevMsg.createTime)) / 1000;
  return msg.senderId === prevMsg.senderId && timeDiff < 120;
};

const checkShowTime = (msg, index) => {
  if (index === 0) return true;
  const prevMsg = messages.value[index - 1];
  return (new Date(msg.createTime) - new Date(prevMsg.createTime)) / 1000 > 300;
};

const processReply = (message) => {
  replyingMessage.value = message;
};

const processSendMessage = async (payload) => {
  if (!currentSession.value) return;

  const messageData = {
    ...payload,
    conversationId: currentSession.value.id,
    clientMsgId: crypto.randomUUID(),
    senderId: currentUserId.value,
    replyToMessageId: replyingMessage.value?.id || null
  };

  replyingMessage.value = null;
  // 修正：调用嵌套 Action 路径
  await store.dispatch('im/message/sendMessage', messageData);
  scrollToBottom();
};

const scrollToBottom = () => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight;
    }
  });
};

onMounted(() => scrollToBottom());
</script>

<style scoped>
.chat-window { display: flex; flex-direction: column; height: 100%; background-color: var(--dt-bg-chat); position: relative; }
.chat-header {
  height: 60px; padding: 0 20px; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-color); flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 8px; }
.title { font-size: 16px; font-weight: 600; color: var(--dt-text-main); }
.member-count { font-size: 14px; color: var(--dt-text-desc); }
.header-right { display: flex; gap: 20px; color: var(--dt-text-desc); }
.header-right i { cursor: pointer; font-size: 18px; }
.message-list-viewport { flex: 1; overflow-y: auto; background-color: var(--dt-bg-chat); padding: 20px 0; }
</style>
