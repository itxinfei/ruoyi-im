import { defineStore } from 'pinia';
import { reactive, ref } from 'vue';
import { messageApi } from '@/api/message';

/**
 * 会话与消息核心状态仓库 (纯 JS 规范)
 * 严格遵守 Doc-13/Doc-30 的状态机与内存安全规约
 */
export const useSessionStore = defineStore('session', () => {
  // 核心数据模型 (Doc-01 §3.1)
  // key: conversationId, value: MessageVO[]
  const messageMap = reactive(new Map()); 
  const activeConversationId = ref(null);
  const isLoading = ref(false);
  const hasMore = ref(true);

  /**
   * 追加单条消息 (带去重与乐观更新策略)
   * @param {Long} conversationId 
   * @param {Object} messageVO 
   */
  const appendMessage = (conversationId, messageVO) => {
    if (!messageMap.has(conversationId)) {
      messageMap.set(conversationId, []);
    }
    const list = messageMap.get(conversationId);

    // 防重机制 (Doc-13 §10.2): 依据 clientMsgId 或 msgId 去重
    const existsIndex = list.findIndex(m => 
      (m.clientMsgId && m.clientMsgId === messageVO.clientMsgId) || 
      (m.msgId && m.msgId === messageVO.msgId)
    );

    if (existsIndex > -1) {
      // 若已存在，则用服务器返回的新状态(如 SENT) 覆盖本地 PENDING 状态
      list[existsIndex] = { ...list[existsIndex], ...messageVO };
    } else {
      // 否则将新消息 push 到底部
      list.push(messageVO);
    }
  };

  /**
   * 分页拉取历史消息 (断网恢复/向上滚动加载)
   * @param {Long} conversationId 
   * @param {Long} lastMessageId 
   */
  const loadHistoryMessages = async (conversationId, lastMessageId = null) => {
    if (isLoading.value || !hasMore.value) return;
    
    isLoading.value = true;
    try {
      const response = await messageApi.getList({
        conversationId,
        lastMessageId,
        pageSize: 20
      });

      const records = response.responseBody || []; // 遵循 Doc-30 §1.6 空值处理
      
      if (!messageMap.has(conversationId)) {
        messageMap.set(conversationId, []);
      }

      if (records.length < 20) {
        hasMore.value = false; // 触顶
      }

      // 将历史记录 unshift 到数组头部
      const currentList = messageMap.get(conversationId);
      messageMap.set(conversationId, [...records, ...currentList]);

    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 发送消息 (乐观更新)
   * @param {Object} payload 
   */
  const sendMessage = async (payload) => {
    // 1. 构造本地假消息 (PENDING)
    const tempMsg = {
      ...payload,
      msgId: null,
      sendStatus: 'PENDING',
      createTime: new Date().getTime()
    };
    appendMessage(payload.conversationId, tempMsg);

    try {
      // 2. 真实发送
      const response = await messageApi.send(payload);
      
      // 3. 收到成功回执后更新状态 (SENT)
      const serverMsg = response.responseBody;
      appendMessage(payload.conversationId, {
        ...tempMsg,
        ...serverMsg,
        sendStatus: 'SENT'
      });
    } catch (error) {
      // 4. 发送失败降级
      appendMessage(payload.conversationId, {
        ...tempMsg,
        sendStatus: 'FAILED'
      });
    }
  };

  /**
   * 切换当前活跃会话
   */
  const setActiveConversation = (conversationId) => {
    activeConversationId.value = conversationId;
    hasMore.value = true; // 重置分页状态
    if (!messageMap.has(conversationId) || messageMap.get(conversationId).length === 0) {
      loadHistoryMessages(conversationId, null);
    }
  };

  /**
   * 内存释放契约 (Doc-30 §1.9)
   */
  const $reset = () => {
    messageMap.clear();
    activeConversationId.value = null;
    isLoading.value = false;
    hasMore.value = true;
  };

  return {
    messageMap,
    activeConversationId,
    isLoading,
    hasMore,
    appendMessage,
    loadHistoryMessages,
    sendMessage,
    setActiveConversation,
    $reset
  };
});
