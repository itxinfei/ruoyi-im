---
stepsCompleted: [1, 2, 3, 4, 5]
inputDocuments: []
session_topic: 'RuoYi-IM 聊天功能业务代码深度分析 + 前端UI/UX优化'
session_goals: '深入理解聊天功能的关键业务逻辑、数据流转、WebSocket信令、前端组件交互和数据库设计；基于竞品分析制定页面优化方案'
selected_approach: 'code-analysis'
techniques_used: ['code-review', 'architecture-analysis', 'data-flow-analysis', 'cross-pollination']
ideas_generated: ['设计令牌系统', 'SystemSettingsDialog优化', 'SessionPanel悬停操作', 'MessageList骨架屏']
context_file: ''
session_continued: true
continuation_date: 2026-01-27
extension_date: 2026-01-28
implementation_date: 2026-01-28
p0_tasks_completed: true
p1_tasks_completed: true
p2_in_progress: true
---

# RuoYi-IM 聊天功能业务代码深度分析报告

**Facilitator:** Itxinfei
**Date:** 2026-01-27

---

# 目录

1. [系统架构概览](#一系统架构概览)
2. [核心文件结构](#二核心文件结构)
3. [消息发送完整流程](#三消息发送完整流程)
4. [WebSocket 信令处理机制](#四websocket-信令处理机制)
5. [前端组件交互详解](#五前端组件交互详解)
6. [数据库表结构设计](#六数据库表结构设计)
7. [技术亮点与优化](#七技术亮点与优化)
8. [业务规则总结](#八业务规则总结)
9. [关键代码位置索引](#九关键代码位置索引)

---

## 一、系统架构概览

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           RuoYi-IM 聊天架构                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐               │
│  │   Vue 3 前端  │    │  Spring Boot │    │    MySQL     │               │
│  │              │    │    后端服务   │    │    数据库    │               │
│  │  · MessageInput│   │              │    │              │               │
│  │  · MessageList │   │  · Controller│    │  · im_message│               │
│  │  · Vuex Store │   │  · Service   │    │  · im_conversation│          │
│  │  · WebSocket  │───│  · WebSocket │───│  · im_conversation_member│   │
│  │              │    │  · Redis     │    │              │               │
│  └──────────────┘    └──────────────┘    └──────────────┘               │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 通信协议

| 协议 | 用途 | 说明 |
|------|------|------|
| **REST API** | 消息 CRUD 操作 | HTTP/HTTPS 同步请求 |
| **WebSocket** | 实时消息推送 | 双向异步通信 |
| **Redis Pub/Sub** | 分布式消息广播 | 跨服务器消息同步 |

---

## 二、核心文件结构

### 2.1 后端核心文件

| 文件 | 路径 | 职责 |
|------|------|------|
| `ImMessageServiceImpl.java` | `ruoyi-im-api/.../service/impl/` | 消息核心业务逻辑 |
| `ImMessageController.java` | `ruoyi-im-api/.../controller/` | REST API 入口 |
| `ImMessageMapper.java` | `ruoyi-im-api/.../mapper/` | 数据访问层接口 |
| `ImMessageMapper.xml` | `ruoyi-im-api/.../resources/mapper/` | MyBatis SQL 映射 |
| `ImMessage.java` | `ruoyi-im-api/.../domain/` | 消息实体类 |
| `ImWebSocketEndpoint.java` | `ruoyi-im-api/.../websocket/` | WebSocket 服务端点 |
| `ImWebSocketBroadcastService.java` | `ruoyi-im-api/.../service/` | 消息广播服务 |

### 2.2 前端核心文件

| 文件 | 路径 | 职责 |
|------|------|------|
| `im-message.js` | `ruoyi-im-web/src/store/modules/` | Vuex 消息状态管理 |
| `im.js` | `ruoyi-im-web/src/store/modules/` | Vuex 核心状态管理 |
| `imWebSocket.js` | `ruoyi-im-web/src/utils/websocket/` | WebSocket 客户端实现 |
| `useImWebSocket.js` | `ruoyi-im-web/src/composables/` | WebSocket 组合式函数 |
| `MessageInput.vue` | `ruoyi-im-web/src/components/Chat/` | 消息输入组件 |
| `MessageList.vue` | `ruoyi-im-web/src/components/Chat/` | 消息列表容器组件 |
| `MessageItem.vue` | `ruoyi-im-web/src/components/Chat/` | 单条消息组件 |
| `MessageBubble.vue` | `ruoyi-im-web/src/components/Chat/` | 消息气泡组件 |

---

## 三、消息发送完整流程

### 3.1 流程图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              消息发送完整流程                                  │
└─────────────────────────────────────────────────────────────────────────────┘

【前端 MessageInput.vue】
    │
    ├─ 1. 用户输入内容
    │
    ├─ 2. 生成 clientMsgId (UUID)
    │
    ├─ 3. 调用 Vuex action: sendMessage()
    │
    ├─ 4. 调用 API: POST /api/im/message/send
    │
【后端 ImMessageController】
    │
    ├─ 5. 接收 ImMessageSendRequest DTO
    │
【后端 ImMessageServiceImpl.sendMessage()】
    │
    ├─ 6. 【幂等性检查】clientMsgId + Redis 去重
    │      if (exists) return cached
    │
    ├─ 7. 【权限验证】检查会话成员资格
    │      if (!member) throw BusinessException
    │
    ├─ 8. 【分布式锁】Redis 锁防止并发
    │      lockKey = "im:send:message:{conversationId}"
    │
    ├─ 9. 【XSS 防护】HtmlUtil.escape()
    │
    ├─ 10. 【消息加密】encryptionUtil.encryptMessage()
    │
    ├─ 11. 【保存消息】im_message 表插入
    │       - sendStatus = "SENDING"
    │
    ├─ 12. 【更新会话】lastMessageId, lastMessageTime
    │
    ├─ 13. 【更新未读数】其他成员 unread_count++
    │
    ├─ 14. 【@提及】创建提及记录
    │
    ├─ 15. 【机器人】发布 GroupMessageEvent
    │
    ├─ 16. 【WebSocket 广播】推送会话成员
    │
【前端 WebSocket 接收】
    │
    ├─ 17. imWebSocket.onMessage() 接收
    │
    ├─ 18. Vuex mutation: ADD_MESSAGE
    │
    ├─ 19. MessageList.vue 自动更新
    │
    └─ 【完成】消息显示在所有成员界面
```

### 3.2 关键代码：消息发送

**位置**: `ImMessageServiceImpl.java:95-259`

```java
@Override
@Transactional(rollbackFor = Exception.class)
public ImMessageVO sendMessage(ImMessageSendRequest request, Long userId) {
    // 1. 幂等性检查
    String clientMsgId = request.getClientMsgId();
    if (clientMsgId != null) {
        Long existingMessageId = redisUtil.checkAndRecordClientMsgId(clientMsgId);
        if (existingMessageId != null) {
            return getCachedMessage(existingMessageId);
        }
    }

    // 2. 权限验证
    ImConversationMember member = imConversationMemberMapper
        .selectByConversationIdAndUserId(conversationId, userId);
    if (member == null) {
        throw new BusinessException("您不是该会话成员");
    }

    // 3. 分布式锁
    return distributedLock.executeWithLock(
        LockKeys.sendMessageKey(conversationId),
        10,
        () -> doSendMessage(request, userId, conversationId, sender, clientMsgId)
    );
}

private ImMessageVO doSendMessage(...) {
    // XSS 防护
    if ("TEXT".equalsIgnoreCase(request.getType())) {
        plainContent = HtmlUtil.escape(plainContent);
    }

    // 消息加密
    String contentToSave = encryptionUtil.encryptMessage(plainContent);

    // 保存消息
    imMessageMapper.insertImMessage(message);

    // 更新会话
    conversationUpdate.setLastMessageId(message.getId());
    imConversationMapper.updateById(conversationUpdate);

    // 增加未读数
    for (ImConversationMember member : members) {
        if (!member.getUserId().equals(userId)) {
            imConversationMemberMapper.incrementUnreadCount(
                conversationId, member.getUserId(), 1);
        }
    }

    // 异步广播
    broadcastService.broadcastMessageToConversation(conversationId, message.getId(), sender);

    return messageVO;
}
```

---

## 四、WebSocket 信令处理机制

### 4.1 消息类型定义

**位置**: `ImWebSocketEndpoint.java` / `imWebSocket.js`

```javascript
// 前端消息类型
export const MESSAGE_TYPE = {
    AUTH: 'auth',              // 认证
    MESSAGE: 'message',        // 聊天消息
    MESSAGE_STATUS: 'message_status',  // 消息状态更新
    PING: 'ping',              // 心跳请求
    PONG: 'pong',              // 心跳响应
    READ: 'read',              // 已读回执
    TYPING: 'typing',          // 正在输入
    ONLINE: 'online',          // 用户上线
    OFFLINE: 'offline',        // 用户下线
    CALL: 'call'               // 音视频通话
}
```

### 4.2 连接建立流程

**位置**: `ImWebSocketEndpoint.java:122-230`

```java
@OnOpen
public void onOpen(Session session) {
    // 1. 从 URL 参数提取 token 和 userId
    String queryString = session.getQueryString();
    String tokenValue = extractTokenFromQuery(queryString);

    // 2. 验证 token (生产环境)
    if (staticSecurityEnabled && !staticJwtUtils.validateToken(tokenValue)) {
        session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "token 无效"));
        return;
    }

    // 3. 解析 userId
    Long userId = staticJwtUtils.getUserIdFromToken(tokenValue);

    // 4. 同步块确保原子性
    synchronized (onlineUsers) {
        // 关闭旧连接（单设备策略）
        Session oldSession = onlineUsers.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            oldSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "新连接建立"));
        }

        // 保存新会话
        onlineUsers.put(userId, session);
        sessionUserMap.put(session, userId);
    }

    // 5. 更新 Redis 在线状态
    staticImRedisUtil.addOnlineUser(userId);

    // 6. 广播上线消息
    staticBroadcastService.broadcastOnlineStatus(userId, true);

    // 7. 推送离线消息
    pushOfflineMessages(userId);
}
```

### 4.3 消息路由处理

**位置**: `ImWebSocketEndpoint.java:239-316`

```java
@OnMessage
public void onMessage(String message, Session session) {
    Long userId = sessionUserMap.get(session);

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> messageMap = mapper.readValue(message,
        new TypeReference<Map<String, Object>>() {});

    String type = (String) messageMap.get("type");
    Object payload = messageMap.get("data");

    // 消息路由
    switch (type) {
        case "auth":
            handleAuthMessage(session, (Map) payload);
            break;
        case "message":
            handleChatMessage(userId, payload);
            break;
        case "typing":
            handleTypingStatus(userId, payload);
            break;
        case "read":
            handleReadReceipt(userId, payload);
            break;
        case "ping":
            sendMessage(session, buildStatusMessage("pong", userId, true));
            break;
        default:
            log.warn("未知消息类型: type={}", type);
    }
}
```

### 4.4 聊天消息处理

**位置**: `ImWebSocketEndpoint.java:394-461`

```java
private void handleChatMessage(Long userId, Object payload) {
    Session senderSession = onlineUsers.get(userId);

    try {
        // 1. 解析消息数据
        Map<String, Object> messageData = mapper.convertValue(payload,
            new TypeReference<Map<String, Object>>() {});

        // 2. 兼容字段名
        Long conversationId = getConversationId(messageData);
        String messageType = getMessageType(messageData);
        String content = (String) messageData.get("content");
        String clientMsgId = (String) messageData.get("clientMsgId");

        // 3. 构建请求
        ImMessageSendRequest request = new ImMessageSendRequest();
        request.setConversationId(conversationId);
        request.setType(messageType);
        request.setContent(content);
        request.setClientMsgId(clientMsgId);

        // 4. 保存消息
        ImMessageVO vo = staticImMessageService.sendMessage(request, userId);
        Long messageId = vo != null ? vo.getId() : null;

        // 5. 立即返回 ACK
        if (messageId != null) {
            sendAckMessage(senderSession, clientMsgId, messageId);
        } else {
            sendErrorMessage(senderSession, clientMsgId, "SAVE_FAILED", "消息保存失败");
        }

    } catch (Exception e) {
        log.error("处理聊天消息异常", e);
        sendErrorMessage(senderSession, clientMsgId, "PROCESS_ERROR", e.getMessage());
    }
}
```

### 4.5 心跳保活机制

**前端**: `imWebSocket.js:218-228`

```javascript
startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
        this.send({
            type: MESSAGE_TYPE.PING,
            data: {
                timestamp: Date.now()
            }
        })
    }, this.heartbeatInterval)  // 30秒
}
```

**后端**: `ImWebSocketEndpoint.java:305-308`

```java
case "ping":
    // 处理心跳
    sendMessage(session, buildStatusMessage("pong", userId, true));
    break;
```

### 4.6 在线用户管理

```java
// 在线用户存储
private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();
private static final Map<Session, Long> sessionUserMap = new ConcurrentHashMap<>();

// 检查用户是否在线
public static boolean isUserOnline(Long userId) {
    return onlineUsers.containsKey(userId);
}

// 发送消息给指定用户
public static void sendToUser(Long userId, Object message) {
    Session session = onlineUsers.get(userId);
    if (session != null && session.isOpen()) {
        String messageJson = new ObjectMapper().writeValueAsString(message);
        session.getBasicRemote().sendText(messageJson);
    }
}

// 获取在线用户数
public static int getOnlineUserCount() {
    return onlineUsers.size();
}
```

---

## 五、前端组件交互详解

### 5.1 组件层次结构

```
ChatPanel.vue (聊天面板)
    │
    ├── SessionPanel.vue (会话列表)
    │
    ├── MessageList.vue (消息列表容器)
    │   │
    │   └── MessageItem.vue (单条消息) - 循环渲染
    │       │
    │       ├── DingtalkAvatar.vue (头像)
    │       │
    │       └── MessageBubble.vue (消息气泡)
    │           ├── 文本消息
    │           ├── 图片消息
    │           ├── 文件消息
    │           ├── 语音消息
    │           └── 视频消息
    │
    └── MessageInput.vue (输入框)
        ├── 工具栏 (表情、图片、文件、截图、@成员、语音)
        ├── 引用回复预览
        ├── 编辑消息预览
        └── 输入区域 (文本 / 语音录制)
```

### 5.2 MessageList.vue - 消息列表容器

**位置**: `ruoyi-im-web/src/components/Chat/MessageList.vue`

**核心功能**:

1. **时间分隔线**: 30分钟间隔或每10条消息显示时间
2. **滚动加载**: 滚动到顶部加载历史消息
3. **自动滚动**: 新消息自动滚动到底部
4. **已读上报**: IntersectionObserver 自动标记已读

```javascript
// 计算带时间分割线的消息列表
const messagesWithDividers = computed(() => {
    const res = []
    props.messages.forEach((msg, index) => {
        let showDivider = false
        if (index === 0) {
            showDivider = true
        } else {
            const prevMsg = props.messages[index - 1]
            const timeDiff = msg.timestamp - prevMsg.timestamp
            // 30分钟间隔或每10条消息显示一次时间
            if (timeDiff > 30 * 60 * 1000 || index % 10 === 0) {
                showDivider = true
            }
        }

        if (showDivider) {
            res.push({
                isTimeDivider: true,
                timeText: formatTimeDivider(msg.timestamp)
            })
        }
        res.push(msg)
    })
    return res
})

// 滚动事件处理
const handleScroll = () => {
    const { scrollTop, clientHeight, scrollHeight } = listRef.value

    // 滚动到顶部加载更多
    if (scrollTop === 0) {
        emit('load-more')
    }

    // 检测是否接近底部
    const distanceFromBottom = scrollHeight - scrollTop - clientHeight
    showScrollToBottom.value = distanceFromBottom > 300
}

// 已读上报监听
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const msgId = entry.target.getAttribute('data-id')
            const msg = props.messages.find(m => m.id == msgId)
            // 如果消息未读且不是自己发的
            if (msg && !msg.isOwn && !msg.isRead) {
                emit('command', 'mark-read', msg)
            }
        }
    })
}, { threshold: 0.5 })
```

### 5.3 MessageItem.vue - 单条消息组件

**位置**: `ruoyi-im-web/src/components/Chat/MessageItem.vue`

**核心功能**:

1. **悬停操作栏**: 回复、点赞、更多操作
2. **多选模式**: 复选框选择
3. **发送状态**: 发送中/已读/失败图标
4. **快捷操作**: 右键@提及、左键查看资料

```vue
<template>
  <div class="message-item" :class="{ 'is-own': message.isOwn }">
    <!-- 多选复选框 -->
    <div v-if="multiSelectMode" class="checkbox-container">
      <el-checkbox :model-value="isSelected" @change="handleCheckboxChange" />
    </div>

    <!-- 头像 -->
    <div class="avatar-container" @click="$emit('show-user', message.senderId)">
      <DingtalkAvatar :src="message.senderAvatar" :name="message.senderName" />
    </div>

    <div class="content-wrapper">
      <!-- 发送者姓名 (群聊中非自己发的消息显示) -->
      <div v-if="!message.isOwn" class="sender-name">{{ message.senderName }}</div>

      <div class="message-content-main">
        <!-- 悬停快捷按钮区 -->
        <div class="message-actions-floating">
          <button @click="$emit('reply', message)">回复</button>
          <button @click="$emit('reaction', message, '👍')">👍</button>
          <el-dropdown @command="(c) => $emit('command', c, message)">
            <button>更多</button>
          </el-dropdown>
        </div>

        <!-- 消息气泡内容插槽 -->
        <slot name="bubble"></slot>
      </div>

      <!-- 消息页脚 (状态与时间) -->
      <div class="message-footer">
        <!-- 发送状态图标 -->
        <el-icon v-if="message.status === 'sending'" class="is-loading">
          <Loading />
        </el-icon>
        <el-icon v-else-if="message.status === 'failed'" class="error" @click="$emit('retry')">
          <WarningFilled />
        </el-icon>

        <!-- 已读状态 -->
        <div v-else class="read-status" :class="{ read: message.readCount > 0 }">
          {{ message.readCount > 0 ? '已读' : '未读' }}
        </div>

        <div class="time">{{ formattedTime }}</div>
      </div>
    </div>
  </div>
</template>
```

### 5.4 MessageInput.vue - 输入组件

**位置**: `ruoyi-im-web/src/components/Chat/MessageInput.vue`

**核心功能**:

1. **工具栏**: 表情、图片、文件、截图、@成员、语音、通话
2. **引用回复**: 显示正在回复的消息预览
3. **编辑消息**: 显示正在编辑的消息预览
4. **输入状态**: 防抖发送 typing 状态
5. **快捷键**: Enter / Ctrl+Enter 发送
6. **拖拽上传**: 支持文件拖拽
7. **高度调整**: 支持拖拽调整输入框高度

```javascript
// 输入状态防抖发送
let typingTimer = null
let lastTypingSendTime = 0
const TYPING_DEBOUNCE = 1000 // 1秒内只发送一次
const TYPING_INTERVAL = 3000 // 每3秒重新发送

const sendTypingIndicator = () => {
    const now = Date.now()

    if (typingTimer) {
        clearTimeout(typingTimer)
    }

    const shouldSend = now - lastTypingSendTime > TYPING_DEBOUNCE

    if (shouldSend && props.session?.id && messageContent.value.trim()) {
        lastTypingSendTime = now

        wsSendMessage({
            type: 'typing',
            data: {
                conversationId: props.session.id,
                userId: currentUser.value?.id
            }
        })

        // 设置下次重新发送的定时器
        typingTimer = setTimeout(() => {
            if (messageContent.value.trim()) {
                lastTypingSendTime = Date.now()
                wsSendMessage({ type: 'typing', data: {...} })
            }
        }, TYPING_INTERVAL)
    }
}

// 快捷键处理
const handleKeydown = (e) => {
    const sendShortcut = store.state.im.settings.shortcuts.send || 'enter'

    if (e.key === 'Enter') {
        if (sendShortcut === 'enter') {
            if (!e.shiftKey && !e.ctrlKey) {
                e.preventDefault()
                handleSend()
            }
        } else if (sendShortcut === 'ctrl-enter') {
            if (e.ctrlKey) {
                e.preventDefault()
                handleSend()
            }
        }
    }

    // @键触发成员选择
    if (e.key === '@' && props.session?.type === 'GROUP') {
        setTimeout(() => atMemberPickerRef.value?.open(), 50)
    }
}

// 拖拽上传处理
const handleDrop = (e) => {
    const files = e.dataTransfer?.files
    if (!files || files.length === 0) return
    for (const file of files) {
        const formData = new FormData()
        formData.append('file', file)
        if (file.type.startsWith('image/')) emit('upload-image', formData)
        else emit('upload-file', formData)
    }
}
```

### 5.5 Vuex 状态管理

**位置**: `ruoyi-im-web/src/store/modules/im-message.js`

```javascript
export default {
    namespaced: true,

    state: () => ({
        // 消息列表（按 sessionId 分组）
        messages: {},
        replyingMessage: null,
        loading: false,
        selectedMessages: new Set()
    }),

    mutations: {
        // 预加消息（历史消息加载更多）
        PREPEND_MESSAGES(state, { sessionId, messages }) {
            if (!state.messages[sessionId]) {
                state.messages[sessionId] = []
            }
            state.messages[sessionId] = [...messages, ...state.messages[sessionId]]
        },

        // 添加单条消息（自动去重）
        ADD_MESSAGE(state, { sessionId, message }) {
            if (!state.messages[sessionId]) {
                state.messages[sessionId] = []
            }
            const index = state.messages[sessionId].findIndex(m => m.id === message.id)
            if (index === -1) {
                state.messages[sessionId].push(message)
            } else {
                // 如果已存在，则更新 (比如编辑后)
                state.messages[sessionId][index] = {
                    ...state.messages[sessionId][index],
                    ...message
                }
            }
        },

        // 更新消息发送状态
        UPDATE_MESSAGE_STATUS(state, { sessionId, messageId, sendStatus }) {
            if (!state.messages[sessionId]) return
            const index = state.messages[sessionId].findIndex(m => m.id === messageId)
            if (index !== -1) {
                state.messages[sessionId][index].status = mapSendStatusToUi(sendStatus)
            }
        }
    },

    actions: {
        // 发送消息
        async sendMessage({ commit }, { sessionId, type, content, replyToMessageId }) {
            const clientMsgId = generateUUID()
            const res = await apiSendMessage({
                conversationId: sessionId,
                type, content, replyToMessageId, clientMsgId
            })

            if (res.code === 200 && res.data) {
                commit('ADD_MESSAGE', { sessionId, message: res.data })
                commit('session/UPDATE_SESSION', {
                    id: sessionId,
                    lastMessage: formatMessagePreviewFromObject(res.data),
                    lastMessageTime: res.data.timestamp
                }, { root: true })
                return res.data
            }
            throw new Error('发送消息失败')
        },

        // 接收消息（WebSocket 推送）
        receiveMessage({ commit, rootState }, message) {
            const sessionId = message.conversationId
            commit('ADD_MESSAGE', { sessionId, message })

            // 如果不是当前会话，增加未读数
            const isCurrentSession = rootState.session.currentSession?.id === sessionId
            commit('session/UPDATE_SESSION', {
                id: sessionId,
                unreadCount: isCurrentSession ? 0 : ((session?.unreadCount || 0) + 1)
            }, { root: true })
        }
    }
}
```

---

## 六、数据库表结构设计

### 6.1 im_message - 消息表

**位置**: `ImMessage.java`

```sql
CREATE TABLE im_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',

    -- 幂等性控制
    client_msg_id VARCHAR(64) COMMENT '客户端消息ID（去重用）',

    -- 发送状态
    send_status VARCHAR(20) COMMENT '发送状态：PENDING/SENDING/DELIVERED/FAILED',
    send_retry_count INT DEFAULT 0 COMMENT '发送重试次数',
    send_error_code VARCHAR(50) COMMENT '发送错误码',
    send_error_msg VARCHAR(255) COMMENT '发送错误信息',
    delivered_time DATETIME COMMENT '送达时间',

    -- 关联信息
    conversation_id BIGINT COMMENT '会话ID',
    sender_id BIGINT COMMENT '发送者用户ID',
    message_type VARCHAR(20) COMMENT '消息类型：TEXT/IMAGE/FILE/VOICE/VIDEO',
    content TEXT COMMENT '消息内容（加密）',

    -- 文件信息
    file_url VARCHAR(500) COMMENT '文件URL',
    file_name VARCHAR(255) COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小（字节）',

    -- 敏感词检测
    sensitive_level VARCHAR(20) COMMENT '敏感级别：NONE/WARN/BLOCK',

    -- 撤回相关
    is_revoked TINYINT DEFAULT 0 COMMENT '是否撤回：0否 1是',
    revoked_time DATETIME COMMENT '撤回时间',
    revoker_id BIGINT COMMENT '撤回者ID',

    -- 编辑相关
    is_edited TINYINT DEFAULT 0 COMMENT '是否已编辑：0否 1是',
    edited_content TEXT COMMENT '编辑后的内容',
    edit_count INT DEFAULT 0 COMMENT '编辑次数',
    edit_time DATETIME COMMENT '最后编辑时间',

    -- 回复/转发
    reply_to_message_id BIGINT COMMENT '回复的消息ID',
    forward_from_message_id BIGINT COMMENT '转发来源消息ID',

    -- 删除标记
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否 1是',
    deleted_time DATETIME COMMENT '删除时间',

    -- 时间戳
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_create_time (create_time),
    INDEX idx_client_msg_id (client_msg_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM消息表';
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `client_msg_id` | VARCHAR(64) | 客户端生成的唯一ID，用于幂等性控制 |
| `send_status` | VARCHAR(20) | 发送状态：PENDING/SENDING/DELIVERED/READ/FAILED |
| `message_type` | VARCHAR(20) | TEXT/IMAGE/FILE/VOICE/VIDEO/COMBINE |
| `content` | TEXT | 加密存储的消息内容 |
| `is_revoked` | TINYINT | 是否已撤回 |
| `is_edited` | TINYINT | 是否已编辑 |
| `reply_to_message_id` | BIGINT | 回复的消息ID（支持嵌套） |
| `forward_from_message_id` | BIGINT | 转发来源消息ID |

### 6.2 im_conversation - 会话表

**位置**: `ImConversation.java`

```sql
CREATE TABLE im_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',

    type VARCHAR(20) COMMENT '会话类型：PRIVATE(单聊)/GROUP(群聊)',
    target_id BIGINT COMMENT '目标ID：单聊为对方用户ID，群聊为群组ID',

    name VARCHAR(255) COMMENT '会话名称',
    avatar VARCHAR(500) COMMENT '会话头像',

    last_message_id BIGINT COMMENT '最后消息ID',
    last_message_time DATETIME COMMENT '最后消息时间',

    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否 1是',
    deleted_time DATETIME COMMENT '删除时间',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_type_target (type, target_id),
    INDEX idx_last_message_time (last_message_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM会话表';
```

### 6.3 im_conversation_member - 会话成员表

**位置**: `ImConversationMember.java`

```sql
CREATE TABLE im_conversation_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    conversation_id BIGINT COMMENT '会话ID',
    user_id BIGINT COMMENT '用户ID',

    nickname VARCHAR(100) COMMENT '群内昵称',
    role VARCHAR(20) COMMENT '角色：OWNER/ADMIN/MEMBER',

    unread_count INT DEFAULT 0 COMMENT '未读消息数',
    is_pinned TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是',
    is_muted TINYINT DEFAULT 0 COMMENT '是否免打扰：0否 1是',

    last_read_message_id BIGINT COMMENT '最后已读消息ID',
    last_read_time DATETIME COMMENT '最后已读时间',

    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否 1是',
    deleted_time DATETIME COMMENT '删除时间',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_conversation_user (conversation_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_unread_count (unread_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM会话成员表';
```

### 6.4 数据流转图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              数据流向                                     │
└─────────────────────────────────────────────────────────────────────────┘

【发送消息】
    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
    │  前端   │───▶│Controller│───▶│ Service │───▶│ Mapper  │───▶│  MySQL  │
    └─────────┘    └─────────┘    └────┬────┘    └─────────┘    └────┬────┘
                                            │                          │
                                            ▼                          │
                                      ┌─────────┐                     │
                                      │ WebSocket│                     │
                                      │ Broadcast│                     │
                                      └────┬────┘                     │
                                           │                          │
        ┌──────────────┐                │                          │
        │ 其他客户端   │◀───────────────┘                          │
        └──────────────┘                                           │
                                                                   │
【查询消息】                                                        │
    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    │
    │  前端   │───▶│Controller│───▶│ Service │───▶│ Mapper  │───┘
    │         │    │         │    │         │    │         │
    │  Vuex   │◀───│   VO    │◀───│ 批量查询 │◀───│  MySQL  │
    └─────────┘    └─────────┘    └─────────┘    └─────────┘
```

---

## 七、技术亮点与优化

### 7.1 幂等性保证

**问题**: 网络重试可能导致消息重复发送

**解决方案**:
```java
// 使用 clientMsgId + Redis 去重
String clientMsgId = request.getClientMsgId();
Long existingMessageId = redisUtil.checkAndRecordClientMsgId(clientMsgId);
if (existingMessageId != null) {
    log.info("消息已存在，跳过重复发送: clientMsgId={}, messageId={}",
        clientMsgId, existingMessageId);
    return getCachedMessage(existingMessageId);
}
```

### 7.2 并发安全

**问题**: 同一会话的并发消息可能导致顺序错乱

**解决方案**:
```java
// 使用分布式锁
ImMessageVO messageVO = distributedLock.executeWithLock(
    "im:send:message:" + conversationId,
    10,  // 10秒过期时间
    () -> doSendMessage(request, userId, conversationId, sender, clientMsgId)
);
```

### 7.3 XSS 防护

**问题**: 文本消息可能包含恶意脚本

**解决方案**:
```java
// HTML 转义
if ("TEXT".equalsIgnoreCase(request.getType())) {
    plainContent = HtmlUtil.escape(plainContent);
}
```

### 7.4 消息加密

**问题**: 敏感消息内容需要加密存储

**解决方案**:
```java
// 加密存储
String contentToSave = encryptionUtil.encryptMessage(plainContent);

// 解密读取
String decryptedContent = encryptionUtil.decryptMessage(message.getContent());
```

### 7.5 N+1 查询优化

**问题**: 消息列表查询时，每条消息都查询一次发送者信息

**优化前**:
```java
for (ImMessage message : messageList) {
    ImUser sender = imUserMapper.selectImUserById(message.getSenderId()); // N 次查询
}
```

**优化后**:
```java
// 1. 收集所有发送者ID
Set<Long> senderIds = new HashSet<>();
for (ImMessage message : messageList) {
    senderIds.add(message.getSenderId());
}

// 2. 批量查询
List<ImUser> users = imUserMapper.selectImUserListByIds(new ArrayList<>(senderIds));

// 3. 构建 Map
Map<Long, ImUser> userMap = new HashMap<>();
for (ImUser user : users) {
    userMap.put(user.getId(), user);
}

// 4. 从 Map 获取
for (ImMessage message : messageList) {
    ImUser sender = userMap.get(message.getSenderId()); // O(1)
}
```

**查询复杂度**: O(N) → O(1)

### 7.6 乐观 UI 更新

**前端采用乐观更新策略**:
```javascript
// 发送后立即显示消息
commit('ADD_MESSAGE', { sessionId, message: res.data });

// WebSocket 推送其他成员的消息
receiveMessage({ commit }, message) {
    commit('ADD_MESSAGE', { sessionId, message });
}
```

### 7.7 离线消息推送

**位置**: `ImWebSocketEndpoint.java:991-1009`

```java
private void pushOfflineMessages(Long userId) {
    CompletableFuture.runAsync(() -> {
        try {
            Object offlineService = applicationContext.getBean("offlineMessageServiceImpl");
            if (offlineService != null) {
                Method method = offlineService.getClass()
                    .getMethod("pushAndClearOfflineMessages", Long.class);
                int count = (int) method.invoke(offlineService, userId);
                log.info("推送离线消息完成: userId={}, count={}", userId, count);
            }
        } catch (Exception e) {
            log.error("推送离线消息失败: userId={}", userId, e);
        }
    });
}
```

### 7.8 单设备登录策略

**位置**: `ImWebSocketEndpoint.java:184-202`

```java
synchronized (onlineUsers) {
    // 检查用户是否已存在在线连接
    Session oldSession = onlineUsers.get(userId);
    if (oldSession != null && oldSession.isOpen()) {
        log.info("用户已存在连接，关闭旧连接: userId={}", userId);
        try {
            // 先从映射中移除
            sessionUserMap.remove(oldSession);
            // 关闭旧连接
            oldSession.close(new CloseReason(
                CloseReason.CloseCodes.NORMAL_CLOSURE,
                "新连接建立"
            ));
        } catch (IOException e) {
            log.error("关闭旧连接异常: userId={}", userId, e);
        }
    }

    // 保存新会话
    onlineUsers.put(userId, session);
    sessionUserMap.put(session, userId);
}
```

---

## 八、业务规则总结

| 规则 | 说明 | 实现位置 |
|------|------|----------|
| **消息撤回** | 只能撤回自己发送的消息，有时间限制 | `ImMessageServiceImpl:440` |
| **消息编辑** | 只能编辑文本消息，15分钟内可编辑 | `ImMessageServiceImpl:475` |
| **消息删除** | 只能删除自己的消息（软删除） | `ImMessageServiceImpl:520` |
| **权限控制** | 只有会话成员才能发送/查看消息 | `ImMessageServiceImpl:139` |
| **@提及** | 群组消息支持 @ 成员，创建提醒记录 | `ImMessageServiceImpl:217` |
| **未读计数** | 其他成员收到消息时未读数 +1 | `ImMessageServiceImpl:206` |
| **消息去重** | 使用 clientMsgId 防止重复发送 | `ImMessageServiceImpl:98` |
| **并发控制** | 使用分布式锁保证消息顺序 | `ImMessageServiceImpl:149` |
| **XSS 防护** | 文本消息自动转义 HTML | `ImMessageServiceImpl:173` |
| **消息加密** | 敏感内容加密存储 | `ImMessageServiceImpl:176` |

---

## 九、关键代码位置索引

| 功能 | 后端位置 | 前端位置 |
|------|----------|----------|
| **发送消息** | `ImMessageServiceImpl:95` | `im-message.js:223` |
| **撤回消息** | `ImMessageServiceImpl:440` | `im-message.js:306` |
| **编辑消息** | `ImMessageServiceImpl:475` | `im-message.js:248` |
| **转发消息** | `ImMessageServiceImpl:574` | `im-message.js:272` |
| **查询消息** | `ImMessageServiceImpl:262` | `im-message.js:204` |
| **标记已读** | `ImMessageServiceImpl:542` | `im-message.js:287` |
| **搜索消息** | `ImMessageServiceImpl:716` | - |
| **WebSocket 端点** | `ImWebSocketEndpoint.java` | `imWebSocket.js` |
| **消息广播** | `ImWebSocketBroadcastService.java` | - |
| **消息输入** | - | `MessageInput.vue` |
| **消息列表** | - | `MessageList.vue` |
| **单条消息** | - | `MessageItem.vue` |
| **消息气泡** | - | `MessageBubble.vue` |
| **状态管理** | - | `im-message.js` |
| **WebSocket Hook** | - | `useImWebSocket.js` |

---

## 附录：完整流程时序图

### 消息发送时序图

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  用户   │    │  前端   │    │ 后端API │    │  MySQL  │    │ 其他端  │
└────┬────┘    └────┬────┘    └────┬────┘    └────┬────┘    └────┬────┘
     │              │              │              │              │
     │ 输入消息      │              │              │              │
     │─────────────▶│              │              │              │
     │              │              │              │              │
     │              │ 生成 clientMsgId              │              │
     │              │              │              │              │
     │              │ POST /api/im/message/send    │              │
     │              │─────────────▶│              │              │
     │              │              │              │              │
     │              │              │ 检查幂等性    │              │
     │              │              │─────────────▶│              │
     │              │              │◀─────────────│              │
     │              │              │              │              │
     │              │              │ 获取分布式锁   │              │
     │              │              │─────────────▶│              │
     │              │              │◀─────────────│              │
     │              │              │              │              │
     │              │              │ XSS防护+加密   │              │
     │              │              │              │              │
     │              │              │ INSERT message│              │
     │              │              │─────────────▶│              │
     │              │              │◀─────────────│              │
     │              │              │              │              │
     │              │              │ 更新会话     │              │
     │              │              │─────────────▶│              │
     │              │              │◀─────────────│              │
     │              │              │              │              │
     │              │              │ 增加未读数   │              │
     │              │              │─────────────▶│              │
     │              │              │◀─────────────│              │
     │              │              │              │              │
     │              │◀─────────────│ 返回 messageId              │
     │              │              │              │              │
     │              │ 更新 Vuex 状态 │              │              │
     │              │              │              │              │
     │ 显示消息      │              │              │              │
     │◀─────────────│              │              │              │
     │              │              │              │              │
     │              │              │ WebSocket 广播              │
     │              │              │─────────────────────────────▶│
     │              │              │              │              │
     │              │              │              │    推送消息  │
     │              │              │              │◀─────────────│
     │              │              │              │              │
     │              │              │              │    显示消息  │
     │              │              │              │─────────────▶│
```

### WebSocket 连接建立时序图

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  客户端  │    │WebSocket│    │  后端   │    │  Redis  │
└────┬────┘    └────┬────┘    └────┬────┘    └────┬────┘
     │              │              │              │
     │ ws://host/ws/im?token=xxx&userId=xxx      │
     │─────────────▶│              │              │
     │              │              │              │
     │              │   @OnOpen    │              │
     │              │─────────────▶│              │
     │              │              │              │
     │              │              │ 验证 token  │
     │              │              │─────────────▶│
     │              │              │◀─────────────│
     │              │              │              │
     │              │              │ 解析 userId │
     │              │              │              │
     │              │              │ 关闭旧连接   │
     │              │              │ (单设备)     │
     │              │              │              │
     │              │              │ 添加在线列表 │
     │              │              │─────────────▶│
     │              │              │              │
     │              │              │ 广播上线     │
     │              │              │─────────────▶│ (发布订阅)
     │              │              │              │
     │              │◀─────────────│ connected    │
     │              │              │              │
     │              │ 推送离线消息  │              │
     │              │◀─────────────│              │
     │              │              │              │
     │              │ 心跳开始      │              │
     │              │◀─────────────│ ping         │
     │              │─────────────▶│ pong         │
```

---

## 十、前端 UI/UX 优化方案（跨界借鉴头脑风暴）

**日期**: 2026-01-27
**方法**: 跨界借鉴 (Cross-Pollination)
**竞品分析**: 钉钉 DingDesign、飞书 Universe Design、Slack 2025

### 竞品 UI 分析对比

| 产品 | 侧边导航 | 会话列表 | 聊天区域 | 特点 |
|------|----------|----------|----------|------|
| **钉钉** | 72px 垂直导航 | 280px 三栏（消息/联系人/应用） | 自适应宽度 | 图标为主，紧凑高效 |
| **飞书** | 可折叠侧边 + 顶部Tab | 多栏切换 | 支持多窗口 | 灵活组合，信息密度高 |
| **Slack** | 整合侧边栏（2025新版） | 统一Tabs系统 | 沉浸式 | Focus优先，减少干扰 |

### 设计系统基础优化

#### 设计令牌补充

```scss
// 新增/完善的设计令牌
:root {
  // 间距系统（4px基准）
  --dt-space-1: 4px;
  --dt-space-2: 8px;
  --dt-space-3: 12px;
  --dt-space-4: 16px;
  --dt-space-5: 20px;
  --dt-space-6: 24px;
  --dt-space-8: 32px;

  // 圆角系统（5档）
  --dt-radius-sm: 4px;
  --dt-radius-md: 6px;
  --dt-radius-lg: 8px;
  --dt-radius-xl: 12px;
  --dt-radius-2xl: 16px;

  // 阴影系统（4档）
  --dt-shadow-1: 0 1px 2px rgba(0,0,0,0.04);
  --dt-shadow-2: 0 2px 8px rgba(0,0,0,0.06);
  --dt-shadow-3: 0 4px 12px rgba(0,0,0,0.08);
  --dt-shadow-4: 0 8px 24px rgba(0,0,0,0.12);

  // 字体系统（6档）
  --dt-font-xs: 12px;
  --dt-font-sm: 13px;
  --dt-font-base: 14px;
  --dt-font-md: 15px;
  --dt-font-lg: 16px;
  --dt-font-xl: 18px;

  // 过渡时间
  --dt-duration-fast: 150ms;
  --dt-duration-base: 200ms;
  --dt-duration-slow: 300ms;
}
```

### 页面优化方案

#### 1. SystemSettingsDialog（设置页面）

**优化点**:
- 左侧导航改为56px图标导航（钉钉风格）
- 右侧内容采用卡片式布局（飞书风格）
- 添加平滑切换动画

#### 2. SessionPanel（会话列表）

**优化点**:
- 悬停显示操作栏（置顶、免打扰、更多）
- 未读消息徽标优化
- 草稿状态标识
- 时间格式智能显示

#### 3. MessageList（消息列表）

**优化点**:
- Slack风格时间分组
- 骨架屏加载状态
- 智能消息分组（连续消息合并）
- 已读状态优化

### 实施优先级

**P0（立即执行）**:
1. 补全设计令牌系统
2. SystemSettingsDialog 优化
3. SessionPanel 悬停操作
4. MessageList 骨架屏

**P1（近期执行）**:
1. ChatPanel 细节打磨
2. MessageInput 拖拽体验
3. GroupFilePanel 空状态

**P2（后续迭代）**:
1. WorkbenchPanel Bento Grid布局
2. ContactsPanel 组织架构树
3. CalendarPanel 日/周/月视图
4. AssistantPanel 对话式UI

### 成功指标

| 指标 | 目标 | 测量方式 |
|------|------|----------|
| 视觉一致性 | 100% 组件符合设计规范 | 设计审查 |
| 加载性能 | 骨架屏替代纯Loading | 视觉检查 |
| 交互反馈 | 所有悬停有反馈 | 用户测试 |
| 响应式 | 1920/1440/1366px 适配 | 多分辨率测试 |

---

## 参考资源

- [钉钉2025 B端设计趋势 - 版式](https://page.dingtalk.com/wow/dingtalk/default/dingtalk/EDeT4UWD0205)
- [钉钉2025 B端设计趋势 - 个性化](https://page.dingtalk.com/wow/dingtalk/default/dingtalk/JVNYHDarV0121)
- [钉钉2025 B端设计趋势 - 风格&质感](https://page.dingtalk.com/wow/dingtalk/default/dingtalk/9ijIiD4sqjwY0126)
- [飞书UI设计规范](https://docs.feishu.cn/article/wiki/WkfiwqwgkiDgdpkiLKvcu0XInmd)
- [Slack 2025 重新设计](https://slack.com/blog/productivity/a-redesigned-slack-built-for-focus)


---

## 十一、P0 任务实施记录 (2026-01-28)

### 实施概览

**实施日期**: 2026-01-28
**实施范围**: P0 优先级任务
**状态**: ✅ 全部完成

### 任务详情

#### 1. 设计令牌系统补全 ✅

**文件**: `ruoyi-im-web/src/styles/design-tokens.scss`

**实施内容**:
- 验证现有设计令牌已完善
- 包含完整的间距、圆角、阴影、字体、过渡时间系统
- 提供丰富的 mixins 和动画关键帧

**验证结果**: 无需修改，已符合要求

---

#### 2. SystemSettingsDialog 导航优化 ✅

**文件**: `ruoyi-im-web/src/components/Common/SystemSettingsDialog.vue`

**实施内容**:
- 左侧导航从 180px 改为 56px 宽度
- 移除文字标签，改用 tooltip 提示
- 添加左侧激活指示条
- 顶部添加品牌 Logo
- 底部用户头像作为账号入口

**关键代码**:
```scss
.settings-nav {
  width: 56px;  // 钉钉风格图标导航
  ...
}

.nav-item {
  width: 40px;
  height: 40px;
  .nav-indicator {
    // 左侧激活指示条
  }
}
```

---

#### 3. SessionPanel 悬停操作栏 ✅

**文件**: `ruoyi-im-web/src/views/SessionPanel.vue`

**实施内容**:
- 添加悬停操作栏（置顶、免打扰、更多）
- 150ms 延迟显示避免闪烁
- 激活会话始终显示操作按钮
- 玻璃态背景 + 平滑动画

**关键代码**:
```vue
<div class="session-hover-actions"
     :class="{ visible: hoveredSessionId === session.id || isActiveSession(session) }">
  <button @click.stop="handleQuickTogglePin(session)">...</button>
  <button @click.stop="handleQuickToggleMute(session)">...</button>
  <button @click.stop="handleContextMenu($event, session)">...</button>
</div>
```

---

#### 4. MessageList 骨架屏 ✅

**文件**: `ruoyi-im-web/src/components/Chat/MessageList.vue`

**实施内容**:
- 替换简单 loading 为消息骨架屏
- 5 条消息占位，带交错淡入动画
- 包含头像、名称、消息气泡占位
- 使用 `skeleton-loading` mixin

**关键代码**:
```vue
<div v-if="loading" class="skeleton-wrapper">
  <div v-for="i in 5" :key="i" class="message-skeleton">
    <div class="skeleton-avatar"></div>
    <div class="skeleton-content">
      <div class="skeleton-name"></div>
      <div class="skeleton-bubble">
        <div class="skeleton-line long"></div>
        <div class="skeleton-line"></div>
      </div>
    </div>
  </div>
</div>
```

---

### 修改文件汇总

| 文件 | 修改类型 | 说明 |
|------|----------|------|
| `design-tokens.scss` | 验证 | 无需修改 |
| `SystemSettingsDialog.vue` | 重构 | 导航改为56px图标风格 |
| `SessionPanel.vue` | 新增 | 添加悬停操作栏 |
| `MessageList.vue` | 新增 | 添加骨架屏加载状态 |

---

### 效果预览

**SystemSettingsDialog**:
```
┌─────────────────────────────────────┐
│ [设置] ────┬── 账号                │
│  💼        │                       │
│  🔔        │  内容区域...           │
│  ⚙️        │                       │
│  📁        │                       │
│  ❓        │                       │
│  ℹ️        │                       │
│  👤 ────── │                       │
└─────────────────────────────────────┘
```

**SessionPanel 悬停操作**:
```
┌───────────────────────────────────┐
│ 头像 [名称]            [📌 🔕 ⋮] │
│       预览文字...                  │
└───────────────────────────────────┘
```

**MessageList 骨架屏**:
```
┌───────────────────────────────────┐
│ ▓▓▓  ▓▓▓▓▓▓▓▓                    │
│      ▓▓▓▓▓                       │
│      ▓▓▓▓▓▓▓▓                     │
│                                  │
│ ▓▓▓  ▓▓▓▓▓▓▓▓                    │
│      ▓▓▓▓▓                       │
│      ▓▓▓▓▓▓▓▓                     │
└───────────────────────────────────┘
```

---

### 下一步计划

**P1 任务（近期执行）**:
1. ChatPanel 细节打磨 ✅
2. MessageInput 拖拽体验 ✅
3. GroupFilePanel 空状态 ✅

**P2 任务（后续迭代）**:
1. WorkbenchPanel Bento Grid布局
2. ContactsPanel 组织架构树
3. CalendarPanel 日/周/月视图
4. AssistantPanel 对话式UI

---

## P1 任务实施记录 (2026-01-28)

### 1. ChatPanel 细节打磨 ✅

**文件**: `ruoyi-im-web/src/views/ChatPanel.vue`

**实施内容**:
- 拖拽上传视觉反馈优化
- 新增 `isDragOver` 状态和 `dragEnterCounter` 计数器
- 添加 `handleDragEnter` 和优化后的 `handleDragLeave` 函数
- 新增 `is-drag-over` 样式，带脉冲动画和渐变背景

**关键代码**:
```javascript
const isDragOver = ref(false)
let dragEnterCounter = 0

const handleDragEnter = (event) => {
  dragEnterCounter++
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    const hasImage = Array.from(files).some(file => file.type.startsWith('image/'))
    if (hasImage) {
      isDragOver.value = true
    }
  }
}
```

**CSS 效果**:
- 背景渐变: `rgba(24, 144, 255, 0.08)`
- 内阴影边框: `inset 0 0 0 2px`
- 径向渐变叠加层
- 脉冲动画: `pulse-drag 1.5s ease-in-out infinite`

---

### 2. MessageInput 拖拽体验优化 ✅

**文件**: `ruoyi-im-web/src/components/Chat/MessageInput.vue`

**实施内容**:
- 新增拖拽状态管理 (`isDragOver`, `dragCounter`)
- 将拖拽事件从 textarea 移至 input-area 容器
- 添加拖拽进入/离开/悬停处理函数
- 新增 `is-drag-over` 样式，带脉冲提示

**关键代码**:
```vue
<div
  class="input-area"
  :class="{ 'is-drag-over': isDragOver }"
  @dragenter="handleDragEnter"
  @dragleave="handleDragLeave"
  @dragover="handleDragOver"
  @drop.prevent="handleDrop"
>
```

**CSS 效果**:
- 淡蓝背景: `rgba(24, 144, 255, 0.04)`
- 内嵌边框: `inset 0 0 0 2px var(--dt-brand-color)`
- 中心提示框: "松开即可发送文件"
- 输入框透明度降低至 0.3

---

### 3. GroupFilePanel 空状态设计 ✅

**文件**: `ruoyi-im-web/src/views/GroupFilePanel.vue`

**实施内容**:
- 替换 `el-empty` 为自定义空状态组件
- 新增插画区域，包含文件夹图标和浮动文件图标
- 根据搜索状态显示不同文案
- 添加操作按钮：上传文件、刷新列表

**关键代码**:
```vue
<div v-if="fileList.length === 0 && !loading" class="empty-state">
  <div class="empty-illustration">
    <div class="folder-icon">
      <span class="material-icons-outlined">folder_open</span>
    </div>
    <div class="floating-icons">
      <span class="icon icon-1 material-icons-outlined">image</span>
      <span class="icon icon-2 material-icons-outlined">description</span>
      <span class="icon icon-3 material-icons-outlined">video_file</span>
    </div>
  </div>
  <h3 class="empty-title">群文件为空</h3>
  <p class="empty-description">
    {{ searchKeyword ? '没有找到匹配的文件' : '暂无群文件，上传文件与群成员共享' }}
  </p>
</div>
```

**CSS 效果**:
- 文件夹图标: 渐变背景 `#ff9a56 → #ff6b6b`
- 浮动图标: 错位浮动动画，延迟 0s/0.5s/1s
- 最小高度: 400px
- 按钮图标化设计

---

### P1 修改文件汇总

| 文件 | 修改类型 | 说明 |
|------|----------|------|
| `ChatPanel.vue` | 增强 | 拖拽视觉反馈优化 |
| `MessageInput.vue` | 增强 | 拖拽体验优化 |
| `GroupFilePanel.vue` | 重构 | 空状态设计 |
| `MessageBubble.vue` | 优化 | 气泡阴影效果增强 |

---

### P1 完成效果

**ChatPanel 拖拽状态**:
```
┌─────────────────────────────────────┐
│                                     │
│     ┌─────────────────────┐         │
│     │  拖放图片到此处      │         │
│     └─────────────────────┘         │
│         (脉冲动画 + 蓝色边框)         │
└─────────────────────────────────────┘
```

**MessageInput 拖拽状态**:
```
┌─────────────────────────────────────┐
│     ┌─────────────────────┐         │
│     │  松开即可发送文件    │         │
│     └─────────────────────┘         │
│  [背景淡蓝 + 输入框半透明]           │
└─────────────────────────────────────┘
```

**GroupFilePanel 空状态**:
```
         ┌─────────┐
        ╱  📁     ╱
       ╱ 🔉 📄 📷 ╱  ← 浮动动画
      ╱─────────╱

      群文件为空
   暂无群文件，上传文件与群成员共享

   [📤 上传文件]  [🔄 刷新列表]
```

