# RuoYi-IM 代码重构方案

> **版本**: v1.0
> **日期**: 2026-02-07
> **目标**: 按照阿里巴巴Java规范全面重构代码，消除重复，统一规范

---

## 一、问题汇总

### 1.1 高优先级问题（严重）

| 问题 | 影响 | 位置 |
|------|------|------|
| ImGroupController vs ImGroupsController | 功能重复 | controller包 |
| 用户搜索功能重复4处 | 代码冗余 | UserController/ContactController/GlobalSearchController |
| Controller内部类作为DTO | 违反分层架构 | ImContactController等 |
| 直接使用JdbcTemplate | 违反分层原则 | ImContactController:724 |
| 临时调试接口未移除 | 生产代码污染 | ImContactController:718 |
| ImUserServiceImpl依赖过多(15个) | 违反单一职责 | ImMessageServiceImpl |

### 1.2 中优先级问题

| 问题 | 影响 | 位置 |
|------|------|------|
| 消息已读接口重复 | 功能混淆 | ImMessageController |
| Redis工具类重复 | 维护困难 | CacheUtil/ImRedisUtil |
| 加密工具类重复 | 功能重叠 | E2EEncryptionUtil/MessageEncryptionUtil |
| 命名不统一 | 代码可读性 | 多处 |
| 分页参数不统一 | 接口不一致 | 多个Controller |

### 1.3 低优先级问题

| 问题 | 影响 | 位置 |
|------|------|------|
| Swagger注解不完整 | 文档缺失 | 部分DTO |
| 日志级别不统一 | 调试困难 | 多处 |
| 异常处理不规范 | 错误信息不清晰 | 多处 |

---

## 二、重构目标

### 2.1 符合阿里巴巴Java规范

- [ ] 所有工具类为final，有私有构造器
- [ ] 常量全大写下划线分隔
- [ ] 方法不超过50行
- [ ] 类不超过500行
- [ ] 避免魔法值
- [ ] 异常处理规范
- [ ] 日志规范

### 2.2 架构优化

- [ ] Controller职责单一
- [ ] Service不直接调用其他Service
- [ ] DTO/VO统一管理
- [ ] 消除重复代码
- [ ] 统一异常处理
- [ ] 统一分页处理

### 2.3 接口规范

- [ ] RESTful风格统一
- [ ] 路径命名规范
- [ ] 参数传递一致
- [ ] 返回值格式统一
- [ ] 错误码规范

---

## 三、重构方案

### 3.1 Controller层重构

#### 3.1.1 合并重复Controller

**执行操作：**

1. **删除ImGroupsController，合并到ImGroupController**
   - 统一路径：`/api/im/group`
   - 统一方法命名

2. **统一搜索功能到ImGlobalSearchController**
   - 删除UserController.search()和ContactController.search()
   - 统一使用：`GET /api/im/search/{type}`

3. **ImMessageController拆分**
   ```
   当前：ImMessageController (32个接口，603行)

   拆分为：
   ├── ImMessageController       (基础消息操作)
   ├── ImMessageReactionController (表情反应)
   ├── ImMessageMentionController  (@提及)
   └── ImMessageSyncController     (消息同步)
   ```

#### 3.1.2 提取Controller内部类

**ImContactController内部类移动：**

| 当前位置 | 目标位置 |
|----------|----------|
| GroupRenameRequest | dto/contact/GroupRenameRequest.java |
| MoveToGroupRequest | dto/contact/MoveToGroupRequest.java |
| FriendTagsUpdateRequest | dto/contact/FriendTagsUpdateRequest.java |
| BatchAddRequest | dto/contact/BatchAddRequest.java |
| BatchDeleteRequest | dto/contact/BatchDeleteRequest.java |
| BatchMoveRequest | dto/contact/BatchMoveRequest.java |
| AddressBookUploadRequest | dto/contact/AddressBookUploadRequest.java |
| BatchClearCacheRequest | dto/contact/BatchClearCacheRequest.java |

#### 3.1.3 统一接口规范

**RESTful路径规范：**

| 功能 | 当前路径 | 规范路径 |
|------|----------|----------|
| 标记已读 | /message/mark-read | /message/{id}/read |
| 标记会话已读 | /message/read | /conversation/{id}/read |
| 获取未读数 | /message/unread/count/{id} | /conversation/{id}/unread |
| 删除消息 | /message/{id} | DELETE /message/{id} |
| 撤回消息 | /message/{id}/recall | POST /message/{id}/recall |

**统一响应格式：**
```java
public class Result<T> {
    private String code;      // 状态码
    private String message;   // 消息
    private T data;          // 数据
    private Long timestamp;  // 时间戳

    public static <T> Result<T> success(T data) { ... }
    public static <T> Result<T> fail(String message) { ... }
    public static <T> Result<T> error(ErrorCode code) { ... }
}
```

### 3.2 Service层重构

#### 3.2.1 提取通用服务

**创建通用工具服务：**

```java
// 通用分页服务
@Service
public class PageService {
    public <T> PageResult<T> paginate(Supplier<List<T>> loader,
                                      Integer pageNum,
                                      Integer pageSize) {
        // 统一的分页逻辑
    }
}

// 通用批处理服务
@Service
public class BatchOperationService {
    public <T> BatchResult processBatch(List<T> items,
                                        Consumer<T> processor) {
        // 统一的批处理逻辑
    }
}

// 通用缓存服务
@Service
public class CacheService {
    public <T> T getOrLoad(String key, Supplier<T> loader,
                           long expire, TimeUnit unit) {
        // 统一的缓存逻辑
    }
}
```

#### 3.2.2 拆分复杂Service

**ImMessageServiceImpl拆分：**

```
当前：ImMessageServiceImpl (约1500行)

拆分为：
├── MessageQueryService          (查询相关)
├── MessageCommandService        (命令相关)
├── MessageReactionService       (已有)
├── MessageMentionService        (已有)
├── MessageReadService           (已有)
└── MessageNotificationService   (通知相关)
```

#### 3.2.3 统一事务管理

**事务规范：**

```java
// 小事务原则
@Transactional(rollbackFor = Exception.class)
public void doDatabaseOperation() {
    // 只包含数据库操作
    // 不包含RPC调用、消息推送等
}

// 非事务操作在事务外执行
public void sendMessageWithNotification(ImMessage message) {
    // 1. 事务内保存消息
    transactionTemplate.execute(status -> {
        messageMapper.insert(message);
        return null;
    });

    // 2. 事务外推送通知
    notificationService.broadcast(message);
}
```

### 3.3 DTO/VO重构

#### 3.3.1 创建基类

```java
// 基础分页请求
public abstract class BasePageRequest {
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页最少1条")
    @Max(value = 100, message = "每页最多100条")
    private Integer pageSize = 20;
}

// 基础创建请求
public abstract class BaseCreateRequest {
    @NotBlank(message = "创建人不能为空")
    private Long creatorId;
}

// 基础更新请求
public abstract class BaseUpdateRequest {
    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "更新人不能为空")
    private Long updaterId;
}
```

#### 3.3.2 统一校验注解

```java
@Schema(description = "群组创建请求")
public class ImGroupCreateRequest extends BaseCreateRequest {

    @Schema(description = "群组名称")
    @NotBlank(message = "群组名称不能为空")
    @Size(min = 2, max = 50, message = "群组名称长度2-50字符")
    private String name;

    @Schema(description = "群组类型")
    @NotNull(message = "群组类型不能为空")
    private GroupType type;  // 使用枚举而非String

    @Schema(description = "成员数量限制")
    @Min(value = 3, message = "群组成员最少3人")
    @Max(value = 500, message = "群组成员最多500人")
    private Integer memberLimit;

    @Schema(description = "初始成员ID列表")
    @NotEmpty(message = "群组成员不能为空")
    private Set<Long> memberIds;  // 使用Set而非List
}
```

### 3.4 工具类重构

#### 3.4.1 合并重复工具类

```java
// 合并前
CacheUtil + ImRedisUtil

// 合并后
@Component
public final class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    // 通用操作
    public void set(String key, Object value, long expire, TimeUnit unit) { ... }
    public Object get(String key) { ... }
    public void delete(String key) { ... }

    // IM专用操作
    public void cacheUserInfo(Long userId, ImUserVO user) { ... }
    public void cacheConversationList(Long userId, List<ImConversationVO> list) { ... }
    public boolean isOnlineUser(Long userId) { ... }

    // 私有构造器
    private RedisUtil() { throw new UnsupportedOperationException("Utility class"); }
}
```

#### 3.4.2 统一加密工具

```java
// 创建统一加密工厂
public final class EncryptionUtil {

    public enum EncryptionType {
        AES_GCM, RSA, ECDH
    }

    public static Encrypter getEncrypter(EncryptionType type) {
        switch (type) {
            case AES_GCM:
                return new AESGCMEncrypter();
            case RSA:
                return new RSAEncrypter();
            case ECDH:
                return new ECDHEncrypter();
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    // 私有构造器
    private EncryptionUtil() { }
}
```

### 3.5 前端API同步

#### 3.5.1 新增API文件

**e2e.js（端到端加密）：**
```javascript
import request from '@/utils/request'

// 生成密钥对
export function generateKeyPair() {
  return request({
    url: '/api/im/e2e/generate',
    method: 'post'
  })
}

// 注册公钥
export function registerPublicKey(data) {
  return request({
    url: '/api/im/e2e/register',
    method: 'post',
    data
  })
}

// 获取用户公钥
export function getUserPublicKey(userId) {
  return request({
    url: `/api/im/e2e/public-key/${userId}`,
    method: 'get'
  })
}

// 检查E2E状态
export function checkE2EStatus() {
  return request({
    url: '/api/im/e2e/status',
    method: 'get'
  })
}

// 密钥轮换
export function rotateKey() {
  return request({
    url: '/api/im/e2e/rotate',
    method: 'post'
  })
}

// 禁用E2E
export function disableE2E() {
  return request({
    url: '/api/im/e2e/disable',
    method: 'delete'
  })
}
```

**push.js（推送设备）：**
```javascript
import request from '@/utils/request'

// 注册推送设备
export function registerDevice(data) {
  return request({
    url: '/api/im/push-device/register',
    method: 'post',
    data
  })
}

// 取消注册设备
export function unregisterDevice(deviceToken) {
  return request({
    url: '/api/im/push-device/unregister',
    method: 'post',
    data: { deviceToken }
  })
}

// 获取用户设备列表
export function getUserDevices() {
  return request({
    url: '/api/im/push-device/list',
    method: 'get'
  })
}

// 更新设备活跃时间
export function updateDeviceActiveTime(deviceToken) {
  return request({
    url: '/api/im/push-device/heartbeat',
    method: 'post',
    data: { deviceToken }
  })
}
```

#### 3.5.2 修改现有API

**message.js（修改markAsRead）：**
```javascript
// 修改前
export function markAsRead(data) {
  return request({
    url: '/api/im/message/read',
    method: 'put',
    params: {
      conversationId: data.conversationId,
      lastReadMessageId: data.messageId
    }
  })
}

// 修改后
export function markAsRead(data) {
  return request({
    url: '/api/im/message/mark-read',
    method: 'put',
    data: {
      conversationId: data.conversationId,
      messageIds: data.messageIds || [data.messageId]
    }
  })
}

// 新增批量标记已读
export function batchMarkAsRead(conversationId, messageIds) {
  return request({
    url: '/api/im/message/mark-read',
    method: 'put',
    data: { conversationId, messageIds }
  })
}
```

---

## 四、实施计划

### 4.1 第一阶段（1-2周）- 基础重构

| 任务 | 文件 | 预计时间 |
|------|------|----------|
| 合并Redis工具类 | CacheUtil + ImRedisUtil | 1天 |
| 提取Controller内部类 | ImContactController等 | 2天 |
| 创建DTO基类 | dto包 | 1天 |
| 统一校验注解 | dto包 | 2天 |
| 移除调试接口 | ImContactController | 0.5天 |

### 4.2 第二阶段（2-3周）- Controller重构

| 任务 | 文件 | 预计时间 |
|------|------|----------|
| 合并ImGroupsController | Group相关 | 1天 |
| 统一搜索功能 | GlobalSearchController | 2天 |
| 拆分ImMessageController | Message相关 | 3天 |
| 统一接口规范 | 所有Controller | 3天 |
| 添加Swagger注解 | 所有DTO | 2天 |

### 4.3 第三阶段（3-4周）- Service重构

| 任务 | 文件 | 预计时间 |
|------|------|----------|
| 创建通用服务 | PageService等 | 2天 |
| 拆分ImMessageServiceImpl | Message相关 | 4天 |
| 优化事务管理 | 所有Service | 3天 |
| 统一异常处理 | GlobalExceptionHandler | 2天 |

### 4.4 第四阶段（4-5周）- 前端同步

| 任务 | 文件 | 预计时间 |
|------|------|----------|
| 新增e2e.js | api/im/e2e.js | 1天 |
| 新增push.js | api/im/push.js | 0.5天 |
| 修改message.js | api/im/message.js | 1天 |
| 更新组件调用 | views/ | 3天 |
| 测试验证 | 全部 | 2天 |

---

## 五、验收标准

### 5.1 代码质量

- [ ] 无重复代码（Sonar Qube重复率<3%）
- [ ] 所有类符合阿里巴巴规范
- [ ] 单元测试覆盖率>60%
- [ ] 无严重bug

### 5.2 性能指标

- [ ] 接口响应时间<500ms (P95)
- [ ] 并发支持>1000 QPS
- [ ] 内存占用<500MB

### 5.3 功能完整性

- [ ] 所有现有功能正常
- [ ] 新功能(E2E/推送)正常工作
- [ ] 前后端接口一致

---

## 六、风险控制

### 6.1 回滚方案

- 每个阶段独立分支
- 完成后合并到main
- 问题可快速回滚

### 6.2 测试策略

- 单元测试：Service层
- 集成测试：Controller层
- E2E测试：关键流程
- 回归测试：全部功能

### 6.3 上线策略

- 灰度发布：10% -> 50% -> 100%
- 监控告警：日志、性能、错误率
- 应急预案：快速回滚、热修复

---

*本文档将随着重构进展持续更新*
