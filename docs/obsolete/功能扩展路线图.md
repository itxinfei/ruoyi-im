# RuoYi-IM 功能扩展路线图

**项目版本**: v1.9
**更新日期**: 2026-01-13
**整体完成度**: 94%

---

## 一、核心功能增强

### 1.1 已读回执完善 ✅ (已完成)

**当前状态**：已完成前端和后端完整实现

**实施方案**：

1. **后端API优化**
```java
/**
 * 批量获取消息已读状态
 */
@GetMapping("/message/read-status")
public Result<Map<Long, ReadStatus>> getReadStatus(@RequestParam List<Long> messageIds) {
    // 返回每条消息的已读人数和详情
}

/**
 * 标记消息已读
 */
@PostMapping("/message/{messageId}/read")
public Result<Void> markAsRead(@PathVariable Long messageId) {
    // 更新已读状态
    // 通过WebSocket推送给发送者
}
```

2. **前端组件实现**
```vue
<!-- 已读回执组件 -->
<template>
  <div class="read-receipt" @click="showReadDialog">
    <span v-if="readCount === 0">未读</span>
    <span v-else-if="isAllRead">全部已读</span>
    <span v-else>{{ readCount }}人已读</span>
  </div>
</template>
```

### 1.2 消息发送失败重试 ✅ (已完成)

**当前状态**：已实现完整的重试机制，包括指数退避策略

**已实现功能**：
- Redis存储失败消息记录
- 指数退避重试策略（1s → 10s max）
- 最大重试3次，24小时过期
- 前端自动重试composable

**实施方案**（已实现）：

```java
/**
 * 消息重试服务
 */
@Service
public class MessageRetryService {

    private static final int MAX_RETRY = 3;

    /**
     * 处理发送失败的消息
     */
    public void handleFailedMessage(Long clientMsgId) {
        int retryCount = getRetryCount(clientMsgId);

        if (retryCount < MAX_RETRY) {
            incrementRetryCount(clientMsgId);
            // 延迟重试（指数退避）
            long delay = (long) Math.pow(2, retryCount) * 1000;
            scheduleRetry(clientMsgId, delay);
        } else {
            // 通知用户发送失败
            notifySendFailed(clientMsgId);
        }
    }
}
```

### 1.3 DING消息功能 ✅ (已完成)

**当前状态**：后端API已实现，支持DING发送、回执、模板管理

**功能描述**：强制提醒消息，必须接收者确认

**实施方案**：

```java
/**
 * DING消息实体
 */
@Entity
@Table(name = "im_ding_message")
public class ImDingMessage {
    private Long id;
    private Long senderId;
    private String title;        // DING标题
    private String content;      // DING内容
    private DingType type;       // DING类型：通知/任务/会议
    private List<Long> targets;  // 接收者列表
    private Integer confirmCount;// 已确认数
    private LocalDateTime deadline; // 截止时间
}

/**
 * DING消息服务
 */
@Service
public class DingMessageService {

    /**
     * 发送DING消息
     */
    public Long sendDing(DingMessageRequest request) {
        // 1. 创建DING消息
        // 2. 强制推送给所有接收者
        // 3. 记录未确认状态
        // 4. 设置定时提醒
    }

    /**
     * 确认DING消息
     */
    public void confirmDing(Long messageId, Long userId) {
        // 1. 更新确认状态
        // 2. 通知发送者
        // 3. 检查是否全部确认
    }
}
```

### 1.4 音视频通话UI完善（中优先级）

**实施方案**：

```vue
<!-- 视频通话组件 -->
<template>
  <div class="video-call-container" v-if="inCall">
    <!-- 本地视频 -->
    <video ref="localVideo" autoplay muted></video>

    <!-- 远程视频 -->
    <video ref="remoteVideo" autoplay></video>

    <!-- 控制栏 -->
    <div class="controls">
      <el-button @click="toggleMute">
        <el-icon><Mute v-if="muted" /></el-icon>
        <el-icon><Mic v-else /></el-icon>
      </el-button>
      <el-button @click="toggleVideo">
        <el-icon><VideoCameraFilled v-if="videoOn" /></el-icon>
        <el-icon><VideoCamera v-else /></el-icon>
      </el-button>
      <el-button type="danger" @click="hangup">挂断</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useWebRTC } from '@/composables/useWebRTC'

const {
  localVideo,
  remoteVideo,
  startCall,
  hangup,
  toggleMute,
  toggleVideo
} = useWebRTC()
</script>
```

### 1.5 群已读未读统计 ✅ (已完成)

**当前状态**：已集成到已读回执功能中

**已实现功能**：
- 支持获取群消息已读/未读成员列表
- 已读详情对话框展示
- ReadReceiptDialog.vue 组件实现

**实施方案**（已实现）：

```java
/**
 * 群消息已读统计
 */
@Data
public class GroupMessageReadStatus {
    private Long messageId;
    private Integer totalCount;    // 群成员总数
    private Integer readCount;      // 已读人数
    private List<UserInfo> unreadUsers; // 未读用户列表
}

/**
 * 获取群消息已读状态
 */
@GetMapping("/message/{messageId}/read-status-group")
public Result<GroupMessageReadStatus> getGroupReadStatus(@PathVariable Long messageId) {
    // 1. 查询群成员总数
    // 2. 查询已读成员列表
    // 3. 计算未读成员
}
```

## 二、高级功能扩展

### 2.1 消息定时发送

**实施方案**：

```java
/**
 * 定时消息任务
 */
@Entity
@Table(name = "im_scheduled_message")
public class ImScheduledMessage {
    private Long id;
    private Long senderId;
    private Long conversationId;
    private String messageType;
    private String content;
    private LocalDateTime scheduledTime; // 计划发送时间
    private MessageStatus status;        // 状态：待发送/已发送/已取消
}

/**
 * 定时任务扫描
 */
@Component
public class ScheduledMessageScanner {

    @Scheduled(fixedRate = 60000) // 每分钟扫描一次
    public void scanAndSend() {
        List<ImScheduledMessage> pending = scheduledMessageMapper
            .selectPendingMessages(LocalDateTime.now());

        pending.forEach(msg -> {
            try {
                messageService.sendMessage(msg.convertToSendRequest(), msg.getSenderId());
                msg.setStatus(MessageStatus.SENT);
                scheduledMessageMapper.updateById(msg);
            } catch (Exception e) {
                log.error("定时消息发送失败: messageId={}", msg.getId(), e);
            }
        });
    }
}
```

### 2.2 消息标记/收藏

**实施方案**：

```java
/**
 * 消息标记
 */
@Entity
@Table(name = "im_message_mark")
public class ImMessageMark {
    private Long id;
    private Long userId;
    private Long messageId;
    private MarkType type;  // 标记类型：重要/待办/收藏
    private LocalDateTime createTime;
}

/**
 * 标记消息
 */
@PostMapping("/message/{messageId}/mark")
public Result<Void> markMessage(
    @PathVariable Long messageId,
    @RequestParam MarkType type
) {
    // 1. 检查是否已标记
    // 2. 添加标记记录
    // 3. 返回成功
}
```

### 2.3 语音转文字

**技术选型**：接入第三方ASR服务（如阿里云、讯飞）

**实施方案**：

```java
/**
 * 语音转文字服务
 */
@Service
public class VoiceToTextService {

    @Autowired
    private AsrClient asrClient; // 第三方ASR客户端

    /**
     * 语音消息转文字
     */
    @Async
    public CompletableFuture<String> convertVoiceToText(String voiceUrl) {
        // 1. 下载语音文件
        // 2. 调用ASR接口
        // 3. 存储识别结果
        // 4. 通过WebSocket推送结果
        return asrClient.recognize(voiceUrl);
    }
}
```

### 2.4 消息全文搜索优化

**技术方案**：Elasticsearch集成

```java
/**
 * 消息索引服务
 */
@Service
public class MessageIndexService {

    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    /**
     * 索引消息（保存时同步）
     */
    public void indexMessage(ImMessage message) {
        MessageDoc doc = MessageDoc.builder()
            .id(message.getId())
            .conversationId(message.getConversationId())
            .senderId(message.getSenderId())
            .content(message.getContent())
            .createTime(message.getCreateTime())
            .build();

        esTemplate.save(doc);
    }

    /**
     * 全文搜索
     */
    public Page<MessageDoc> search(Long userId, String keyword, Pageable page) {
        // 1. 查询用户有权限的会话
        // 2. 构建ES查询（匹配会话+关键词）
        // 3. 返回高亮结果
    }
}
```

## 三、用户体验增强

### 3.1 桌面通知

**实施方案**：

```javascript
/**
 * 桌面通知服务
 */
class DesktopNotification {
  constructor() {
    this.permission = 'default'
    this.init()
  }

  init() {
    if ('Notification' in window) {
      Notification.requestPermission().then(permission => {
        this.permission = permission
      })
    }
  }

  /**
   * 显示通知
   */
  show({ title, body, icon, onClick }) {
    if (this.permission !== 'granted') return

    const notification = new Notification(title, { body, icon })

    notification.onclick = () => {
      window.focus()
      onClick && onClick()
      notification.close()
    }

    // 5秒后自动关闭
    setTimeout(() => notification.close(), 5000)
  }
}
```

### 3.2 消息引用回复优化

**实施方案**：完善引用消息的展示和交互

### 3.3 表情包管理

**实施方案**：

```java
/**
 * 表情包服务
 */
@Service
public class EmojiPackageService {

    /**
     * 上传自定义表情包
     */
    public void uploadCustomEmoji(MultipartFile file, Long userId) {
        // 1. 验证文件格式和大小
        // 2. 存储到文件系统或OSS
        // 3. 保存表情包记录
    }

    /**
     * 获取表情包列表
     */
    public List<EmojiPackage> getUserEmojis(Long userId) {
        // 返回系统表情包+用户自定义表情包
    }
}
```

## 四、功能优先级矩阵

| 功能 | 用户价值 | 实现复杂度 | 推荐优先级 | 状态 | 预估工时 |
|-----|---------|-----------|-----------|------|---------|
| 已读回执UI完善 | 高 | 低 | 🔴 P0 | ✅ 已完成 | 2天 |
| 消息发送重试 | 高 | 中 | 🔴 P0 | ✅ 已完成 | 3天 |
| 离线消息存储 | 高 | 中 | 🔴 P0 | ✅ 已完成 | 2天 |
| 系统健康检查 | 高 | 低 | 🔴 P0 | ✅ 已完成 | 1天 |
| WebSocket断线重连 | 高 | 中 | 🔴 P0 | ✅ 已完成 | 2天 |
| DING消息 | 高 | 中 | 🔴 P0 | ✅ 已完成 | 5天 |
| 群已读未读统计 | 高 | 低 | 🔴 P0 | ✅ 已完成 | 2天 |
| 系统监控指标 | 中 | 低 | 🔴 P0 | ✅ 已完成 | 1天 |
| 音视频通话UI | 中 | 高 | 🟡 P1 | 🚧 进行中 | 8天 |
| 邮件模块完善 | 中 | 中 | 🟡 P1 | 🚧 进行中 | 5天 |
| 消息定时发送 | 中 | 中 | 🟡 P1 | ⏸ 待开始 | 3天 |
| 消息标记/收藏 | 中 | 低 | 🟡 P1 | ✅ 已完成 | 2天 |
| 语音转文字 | 中 | 高 | 🟢 P2 | ⏸ 待开始 | 5天 |
| 全文搜索优化 | 中 | 中 | 🟢 P2 | ⏸ 待开始 | 4天 |
| 表情包管理 | 低 | 低 | 🟢 P2 | ⏸ 待开始 | 3天 |
| 桌面通知 | 中 | 低 | 🟢 P2 | ⏸ 待开始 | 2天 |

**图例说明**：
- ✅ 已完成：功能已实现并测试通过
- 🚧 进行中：正在开发中
- ⏸ 待开始：计划中但尚未开始

## 五、技术实施路线图

### 阶段一：核心功能完善 ✅ (已完成)
- ✅ 已读回执UI完善 (useReadReceipt.js, ReadReceiptBadge.vue, ReadReceiptDialog.vue)
- ✅ 消息发送重试机制 (ImMessageRetryService, useMessageRetry.js)
- ✅ 群已读未读统计 (已读详情对话框)
- ✅ 离线消息存储推送 (OfflineMessageService, Redis存储7天过期)
- ✅ WebSocket断线重连优化 (WebSocketManager.js, 指数退避)
- ✅ 系统健康检查 (ImHealthCheckController, /api/im/health)
- ✅ 系统监控指标 (ImMonitorController, /api/im/monitor)

### 阶段二：钉钉特色功能 ✅ (已完成)
- ✅ DING消息功能 (ImDingMessageController, 发送/回执/模板)
- ✅ 消息标记/收藏 (ImMessageFavoriteController, 标签管理)
- ✅ 任务分配功能 (日程管理、工作报告)

### 阶段三：高级功能 🚧 (进行中)
- 🚧 音视频通话完善 (ImVideoCallController后端已完成, UI待完善)
- 🚧 邮箱模块 (ImEmailController后端已完成, 前端已对接)
- ⏸ 语音转文字 (待接入ASR服务)
- ⏸ 全文搜索优化 (待集成Elasticsearch)

### 阶段四：体验优化 ⏸ (计划中)
- ⏸ 桌面通知 (浏览器Notification API)
- ⏸ 表情包管理 (自定义表情包上传)
- ⏸ 主题定制 (深色模式等)

---

## 六、版本发布记录

### v1.9 (2026-01-13) - 当前版本
**新增功能**：
- 离线消息存储与自动推送
- 消息已读回执自动标记（IntersectionObserver）
- 消息发送失败自动重试
- 系统健康检查接口
- 系统运行监控指标
- WebSocket断线重连优化

**优化改进**：
- WebSocket连接稳定性增强
- 网络状态监听与自动重连
- 心跳超时检测机制

### v1.8 (2026-01-12)
**新增功能**：
- 消息内容加密/解密功能

### v1.7 (2026-01-11)
**新增功能**：
- 邮箱模块前端实现

### v1.6 (2026-01-10)
**新增功能**：
- 视频通话模块基础功能
- WebRTC信令支持
