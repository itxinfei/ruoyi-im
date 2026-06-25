# Java API 系统性优化报告

**优化日期**: 2026-06-25
**优化范围**: `ruoyi-im-api/src/main/java` 全部 Service 实现层
**优化维度**: 事务安全、Redis 反模式、异常脱敏、资源管理、N+1 查询、缓存策略

---

## 一、优化统计总览

| 优化类别 | 修复数量 | 影响文件数 |
|----------|----------|-----------|
| @Transactional rollbackFor 补全 | 31 处 | 5 文件 |
| 多表写入缺 @Transactional | 3 处 | 3 文件 |
| Redis KEYS → SCAN | 2 处 | 2 文件 |
| Redis 无界递归修复 | 1 处 | 1 文件 |
| Redis 安全风险修复 | 1 处 | 1 文件 |
| Redis TTL 修复 | 1 处 | 1 文件 |
| Redis 在线状态检查修复 | 1 处 | 1 文件 |
| N+1 查询优化 | 3 处 | 2 文件 |
| 缓存失效策略修复 | 1 处 | 1 文件 |
| e.getMessage() 泄漏修复 | 12 处 | 7 文件 |
| 空 catch 块补日志 | 2 处 | 1 文件 |
| ObjectMapper 单例化 | 2 处 | 2 文件 |
| Process 资源泄漏修复 | 2 处 | 1 文件 |
| 测试认证 mock 修复 | 3 处 | 2 文件 |
| **合计** | **65 处** | **18 文件** |
| 空 catch 块补日志 | 2 处 | 1 文件 |
| ObjectMapper 单例化 | 2 处 | 2 文件 |
| Process 资源泄漏修复 | 2 处 | 1 文件 |
| 测试认证 mock 修复 | 3 处 | 2 文件 |
| **合计** | **61 处** | **16 文件** |

---

## 二、Phase 1: @Transactional 事务安全

### 2.1 rollbackFor 补全 (31 处)

Spring `@Transactional` 默认仅回滚 `RuntimeException`，缺少 `rollbackFor = Exception.class` 会导致 checked exception 时不回滚。

**修复文件:**
- `ImMessageMentionServiceImpl.java` — 3 处 (createMentions, markAsRead, batchMarkAsRead)
- `ImFileChunkUploadServiceImpl.java` — 5 处 (initChunkUpload, uploadChunk, mergeChunks, cancelChunkUpload, pauseChunkUpload)
- `ImDocumentServiceImpl.java` — 11 处
- `ImGroupAnnouncementServiceImpl.java` — 6 处
- `ImGroupFileServiceImpl.java` — 6 处

**修复方式**: `@Transactional` → `@Transactional(rollbackFor = Exception.class)`

### 2.2 多表写入补事务 (3 处)

| 文件 | 方法 | 操作 |
|------|------|------|
| `ImUserServiceImpl.java` | `batchImportUsers()` | 循环 createUser + updateRole，补 @Transactional |
| `ImMessageServiceImpl.java` | `replyMessage()` | 写 im_message，补 @Transactional |
| `ImUrlMetadataServiceImpl.java` | `save()` | insert/update，补 @Transactional |

---

## 三、Phase 2: Redis 反模式修复

### 3.1 KEYS 命令替换为 SCAN (P0)

**问题**: `KEYS` 命令阻塞 Redis 单线程，生产环境不可用。

**修复文件:**
- `ImCacheServiceImpl.java:159` — `redisTemplate.keys(pattern)` → `redisTemplate.scan(ScanOptions)`
- `ImRedisUtil.java:391` — 同上

### 3.2 无界递归修复 (P0)

**问题**: `CacheProtectionUtil.getWithMutexLock()` 锁获取失败时无限递归，导致 `StackOverflowError`。

**修复**: 改为循环重试，最多 5 次，耗尽后降级直接查询数据源。

### 3.3 Redis 反序列化安全风险修复 (P0)

**问题**: `ImRedisConfig.java` 使用 `LaissezFaireSubTypeValidator`，接受所有类型信息，存在 RCE 风险。

**修复**: 替换为 `BasicPolymorphicTypeValidator`，仅允许 `com.ruoyi.im.`、`java.util.`、`java.time.`、`java.lang.` 包。

### 3.4 消息 Seq 计数器无 TTL (P1)

**问题**: `im:seq:{conversationId}` 通过 `increment()` 创建但无过期时间，永久累积。

**修复**: 在 `ImMessageServiceImpl.sendMessage()` 中添加 `redisUtil.expire(seqKey, 30, TimeUnit.DAYS)`。

### 3.5 视频通话在线状态检查错误 (P1)

**问题**: `ImVideoCallServiceImpl.initiateCall()` 检查 `im:online:{userId}` (在线时间 key) 而非 Set 成员。

**修复**: 注入 `ImRedisUtil`，使用 `imRedisUtil.isOnlineUser(calleeId)` 替代直接 key 检查。

---

## 四、Phase 4: N+1 查询优化与缓存策略

### 4.1 N+1 查询优化 (3 处)

**ImMessageReadServiceImpl — getMessageReadStatus()**:
- 原始: 循环内调用 `userMapper.selectImUserById()` 查询用户信息（2 处循环，各查 N 次）
- 修复: 批量收集 userIds → `selectImUserListByIds()` 一次查询 → `Map<Long, ImUser>` 查找

**ImMessageReadServiceImpl — getMessageReadDetail()**:
- 原始: 已读用户列表和未读用户列表各循环内调用 `selectImUserById()`
- 修复: 预加载所有会话成员用户信息到 Map，两个循环共用

**ImConversationServiceImpl — searchConversations()**:
- 原始: 每个会话分别调用 `getPeerUserId()` + `selectImUserById()` 或 `selectImGroupById()`
- 修复: 批量加载所有成员 → 收集 peerUserIds/groupIds → 批量查询用户和群组 → Map 查找

### 4.2 缓存失效策略修复 (1 处)

**问题**: `ImMessageServiceImpl.sendMessage()` 发送新消息后未清除会话列表缓存，导致用户看到的会话列表中"最后一条消息"最多滞后 5 分钟。

**修复**: 在消息发送成功后，遍历会话所有成员，清除每人的 `conversation:list:{userId}` 缓存。

---

## 五、Phase 3: 异常脱敏与资源管理

### 4.1 e.getMessage() 泄漏修复 (12 处)

| 文件 | 原始 | 修复后 |
|------|------|--------|
| ImFileChunkUploadServiceImpl | `"分片上传失败: " + e.getMessage()` | `"分片上传失败"` + 日志 |
| ImFileChunkUploadServiceImpl | `"文件合并失败: " + e.getMessage()` | `"文件合并失败"` + 日志 |
| ImFilePreviewServiceImpl | `"生成缩略图失败: " + e.getMessage()` | `"生成缩略图失败"` + 日志 |
| ImTranslationServiceImpl | `"翻译失败: " + e.getMessage()` | `"翻译失败"` |
| ImUserServiceImpl | `"导入失败: " + e.getMessage()` | `"导入失败"` + 日志 |
| ImBackupServiceImpl | `RuntimeException(e.getMessage())` | `BusinessException("导出失败")` |
| ImBackupServiceImpl | `RuntimeException(e.getMessage())` | `BusinessException("恢复失败")` |
| ImBackupServiceImpl | DB 存储 `e.getMessage()` | `"备份执行异常"` |
| ImUrlMetadataServiceImpl | DB 存储 `e.getMessage()` (3处) | `"抓取失败"` / `"抓取异常"` |
| ImVoiceTranscriptServiceImpl | DB 存储 `e.getMessage()` | `"语音处理失败"` |

### 4.2 空 catch 块补日志 (2 处)

`ImFileChunkUploadServiceImpl.cleanupChunks()` — 文件删除失败从静默忽略改为 `log.warn()`。

### 4.3 ObjectMapper 单例化 (2 处)

- `ImMessageMentionServiceImpl` — `new ObjectMapper()` → 静态 `OBJECT_MAPPER` 常量
- `ImMessageReadServiceImpl` — 同上

### 4.4 Process 资源泄漏修复 (2 处)

`ImBackupServiceImpl` — `performBackup()` 和 `performRestore()` 的 `Process` 对象添加 `try-finally` + `process.destroy()`。

---

## 五、测试验证

- `mvn compile` ✅ 通过
- `mvn test` ✅ 500/500 通过，0 失败，0 错误

---

## 六、遗留优化项（后续迭代）

| 优先级 | 项目 | 说明 |
|--------|------|------|
| P1 | N+1 查询优化 | MessageReadServiceImpl 等 3+ 处循环内 DB 查询 |
| P1 | 会话列表缓存失效 | 新消息到达时未清除会话列表缓存 |
| P1 | 群组信息缓存失效 | 群数据变更后未主动清除缓存 |
| P2 | 缓存层合并 | ImRedisUtil 与 ImCacheServiceImpl 功能重叠 |
| P2 | 离线消息实现合并 | ImRedisUtil 和 OfflineMessageServiceImpl 两套实现 |
| P2 | 消息列表缓存 | 激活 ImCacheServiceImpl 的缓存最近消息功能 |
| P2 | 群成员列表缓存 | 高频查询但未缓存 |
| P3 | 在线状态 TTL 缩短 | 30min → 60-90s + 心跳续期 |
