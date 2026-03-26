<template>
  <div class="chat-window">
    <!-- 1. 聊天头部 -->
    <header class="chat-header">
      <div class="header-left">
        <h3 class="title">{{ currentSession?.name || '聊天' }}</h3>
        <span v-if="currentSession?.memberCount" class="member-count">({{ currentSession.memberCount }})</span>
      </div>
      <div class="header-right">
        <i class="el-icon-phone" title="语音通话"></i>
        <i class="el-icon-more" title="详情" @click="detailDrawerVisible = true"></i>
      </div>
    </header>

    <!-- 2. 消息列表区 -->
    <div class="message-list-viewport" ref="listRef" @scroll="handleScroll">
      <div class="message-list-content">
        <ChatMessageBubble
          v-for="(msg, index) in messages"
          :key="msg.clientMsgId || msg.id"
          :message="msg"
          :is-me="msg.senderId === currentUserId"
          :is-grouped="checkIsGrouped(msg, index)"
          :show-time="checkShowTime(msg, index)"
          :quoted-message="msg.quotedMessage || getQuotedMessage(msg)"
          @reply="processReply"
          @forward="processForward"
          @recall="processRecall"
          @delete="processDelete"
          @favorite="processFavorite"
        />
      </div>
    </div>

    <!-- 3. 输入区 -->
    <ChatInputArea
      v-model="currentDraft"
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
 * ChatWindow.vue (Vuex 路径修复版 + WebSocket 实时消息 + 触顶加载历史)
 */
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue';
import { useStore } from 'vuex';
import { ElMessage } from 'element-plus';
import { useImWebSocket } from '@/composables/useImWebSocket';
import { uploadImage, uploadFile } from '@/api/im/file';
import ChatMessageBubble from './ChatMessageBubble.vue';
import GroupDetailDrawer from './GroupDetailDrawer.vue';
import ChatInputArea from './ChatInputArea.vue';

const store = useStore();

// 1. WebSocket 集成
const { onMessage, onRead, connect, cleanup } = useImWebSocket();

// 2. 基础状态
const listRef = ref(null);
const detailDrawerVisible = ref(false);
const replyingMessage = ref(null);
const isLoadingMore = ref(false); // 是否正在加载更多
const currentDraft = ref(''); // 当前会话草稿

// 3. 数据联动
const currentUserId = computed(() => store.state.im?.currentUser?.id || 1);
const currentSession = computed(() => store.state.im?.session?.currentSession);
const messages = computed(() => store.state.im?.message?.messages || []);

// 4. WebSocket 消息处理
const handleNewMessage = (payload) => {
  store.dispatch('im/message/receiveMessage', payload);
  if (payload.conversationId === currentSession.value?.id) {
    nextTick(() => scrollToBottom());
  }
};

// 5. 已读回执处理
const handleReadReceipt = (payload) => {
  const { messageId, conversationId } = payload;
  if (conversationId === currentSession.value?.id) {
    store.commit('im/message/UPDATE_MESSAGE', {
      sessionId: conversationId,
      message: { messageId, isRead: true }
    });
  }
};

// 6. 触顶加载历史消息
const handleScroll = async () => {
  if (!listRef.value || isLoadingMore.value) return;
  // 滚动到顶部附近（100px 内）
  if (listRef.value.scrollTop < 100) {
    const sessionId = currentSession.value?.id;
    if (!sessionId) return;

    // 记录当前滚动高度
    const oldScrollHeight = listRef.value.scrollHeight;

    // 获取当前消息列表的第一条消息 ID
    const firstMessage = messages.value[0];
    const lastId = firstMessage?.id;

    if (!lastId) return;

    isLoadingMore.value = true;
    try {
      await store.dispatch('im/message/loadMessages', {
        sessionId,
        lastMessageId: lastId,
        pageSize: 20,
        isLoadMore: true
      });
      // 加载完成后，保持滚动条位置不变
      nextTick(() => {
        if (listRef.value) {
          const newScrollHeight = listRef.value.scrollHeight;
          listRef.value.scrollTop = newScrollHeight - oldScrollHeight;
        }
      });
    } finally {
      isLoadingMore.value = false;
    }
  }
};

// 7. 初始化
onMounted(() => {
  connect();
  onMessage(handleNewMessage);
  onRead(handleReadReceipt);
  scrollToBottom();
});

// 8. 监听当前会话变化（保存旧草稿 + 恢复新草稿）
watch(() => currentSession.value?.id, async (newSessionId, oldSessionId) => {
  // 保存旧会话草稿（仅在切换到新会话时保存）
  if (oldSessionId && newSessionId && oldSessionId !== newSessionId && currentDraft.value) {
    await store.dispatch('im/session/saveDraft', {
      sessionId: oldSessionId,
      content: currentDraft.value
    });
  }

  if (newSessionId) {
    store.dispatch('im/message/loadMessages', { sessionId: newSessionId });
    // 恢复新会话草稿
    const session = store.state.im?.session?.sessions?.find(s => s.id === newSessionId);
    currentDraft.value = session?.draftContent || '';
    nextTick(() => scrollToBottom());
  }
}, { immediate: true });

// 9. 组件卸载时清理
onUnmounted(() => {
  cleanup();
});

const checkIsGrouped = (msg, index) => {
  if (index === 0 || !msg) return false;
  const prevMsg = messages.value[index - 1];
  if (!prevMsg || !msg.createTime || !prevMsg.createTime) return false;
  
  const timeDiff = (new Date(msg.createTime).getTime() - new Date(prevMsg.createTime).getTime()) / 1000;
  return msg.senderId === prevMsg.senderId && timeDiff < 60; // 钉钉通常为 1 分钟内收纳
};

const checkShowTime = (msg, index) => {
  if (index === 0 || !msg) return true;
  const prevMsg = messages.value[index - 1];
  if (!prevMsg || !msg.createTime || !prevMsg.createTime) return true;
  
  const timeDiff = (new Date(msg.createTime).getTime() - new Date(prevMsg.createTime).getTime()) / 1000;
  return timeDiff > 300; // 5 分钟显示一次时间线
};

const processReply = (message) => {
  replyingMessage.value = message;
};

// 获取被引用的消息
const getQuotedMessage = (msg) => {
  if (!msg.replyToMessageId) return null;
  return messages.value.find(m => (m.messageId || m.id) === msg.replyToMessageId);
};

// 处理转发
const processForward = (message) => {
  // TODO: 打开转发选择对话框
  console.log('转发消息:', message);
};

// 处理撤回
const processRecall = async (message) => {
  try {
    await store.dispatch('im/message/recallMessage', message.messageId || message.id);
  } catch (error) {
    console.error('撤回消息失败:', error);
  }
};

// 处理删除
const processDelete = async (message) => {
  try {
    await store.dispatch('im/message/deleteMessage', message.messageId || message.id);
  } catch (error) {
    console.error('删除消息失败:', error);
  }
};

// 处理收藏
const processFavorite = async (message) => {
  const messageId = message.messageId || message.id;
  const conversationId = currentSession.value?.id;

  if (!messageId) {
    console.error('消息ID不存在');
    return;
  }

  try {
    if (message.isFavorited) {
      // 取消收藏
      await store.dispatch('im-message/removeFavorite', { messageId, conversationId });
      ElMessage.success('已取消收藏');
    } else {
      // 添加收藏
      await store.dispatch('im-message/addFavorite', { messageId, conversationId });
      ElMessage.success('已添加收藏');
    }
  } catch (error) {
    console.error('收藏操作失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

const processSendMessage = async (payload) => {
  if (!currentSession.value) return;

  let content = payload.content;
  let messageType = payload.type || 'TEXT';

  // 如果是图片或文件，先上传获取URL
  if (payload.type === 'IMAGE' || payload.type === 'FILE') {
    const formData = new FormData();
    formData.append('file', payload.file);
    if (payload.fileName) {
      formData.append('fileName', payload.fileName);
    }

    try {
      const api = payload.type === 'IMAGE' ? uploadImage : uploadFile;
      const res = await api(formData);
      if (res.code === 200 && res.data) {
        // content 应该是 JSON 字符串，包含 fileUrl, fileName, fileSize 等
        content = JSON.stringify({
          fileUrl: res.data.url,
          fileName: payload.fileName || res.data.fileName || payload.file?.name,
          fileSize: payload.file?.size
        });
      } else {
        throw new Error('上传失败');
      }
    } catch (error) {
      console.error('文件上传失败:', error);
      ElMessage.error('文件上传失败');
      return;
    }
  }

  const messageData = {
    sessionId: currentSession.value.id,
    content: content,
    type: messageType,
    replyToMessageId: replyingMessage.value?.id || null
  };

  replyingMessage.value = null;
  try {
    await store.dispatch('im/message/sendMessage', messageData);
    // 发送成功后清除草稿
    currentDraft.value = '';
    await store.dispatch('im/session/saveDraft', {
      sessionId: currentSession.value.id,
      content: ''
    });
    scrollToBottom();
  } catch (error) {
    console.error('消息发送失败:', error);
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight;
    }
  });
};
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
.header-right i {
  cursor: pointer;
  font-size: 18px;
  transition: all var(--dt-transition-fast);
}
.header-right i:hover {
  color: var(--dt-brand-color);
}
.header-right i:active {
  transform: scale(0.95);
}
.message-list-viewport { flex: 1; overflow-y: auto; background-color: var(--dt-bg-chat); padding: 20px 0; }

/* 对齐钉钉输入区高度约束: 最低 130px，最高不超过聊天区的 40% */
:deep(.chat-input-wrapper) {
  min-height: 130px;
  max-height: 40%;
}
</style>
