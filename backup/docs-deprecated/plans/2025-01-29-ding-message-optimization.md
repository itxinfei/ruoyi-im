# DING 消息功能优化设计文档

**日期**: 2025-01-29
**优化范围**: 后端性能 + 前端 UI/UX 全链路优化

---

## 一、问题诊断

### 1.1 后端代码问题

| 类/文件 | 问题 | 严重程度 | 状态 |
|--------|------|---------|------|
| `DingSendRequest.java:69` | 字段重复定义：`isUrgent` 出现两次 | 🔴 高 | ✅ 已修复 |
| `DingSendRequest.java` | 字段不一致：`priority` 与 `isUrgent` 混用 | 🟠 中 | ✅ 已修复 |
| `ImDingServiceImpl.java:362` | N+1 查询：循环中查询发送者信息 | 🔴 高 | ✅ 已修复 |
| `ImDingMessageServiceImpl.java:101` | N+1 查询：循环中验证接收者 | 🟠 中 | ℹ️ 旧实现，新实现已优化 |
| `ImDingServiceImpl.java` | 批量查询缺失 | 🟠 中 | ✅ 已修复 |

### 1.2 前端代码问题

| 组件/文件 | 问题 | 严重程度 | 状态 |
|---------|------|---------|------|
| `SendDingDialog.vue:301` | 字段不匹配：发送 `priority`，后端期望 `isUrgent` | 🔴 高 | ✅ 已修复 |
| `SendDingDialog.vue` | 使用 emoji 图标：违反 UI 规范 | 🟠 中 | ✅ 已修复 |
| `DingMessageBubble.vue:46` | 使用 emoji 图标：违反 UI 规范 | 🟠 中 | ✅ 已修复 |
| `ding.js` | 缺少获取已读用户列表的 API | 🟠 中 | ✅ 已修复 |
| `ding.js` | JSDoc 注释字段名不一致 | 🟡 低 | ✅ 已修复 |

---

## 二、后端优化

### 2.1 DingSendRequest.java 修复

**修复前**:
```java
private Integer isUrgent = 0;  // 第42行
// ... 其他字段
private Integer isUrgent = 0;  // 第69行 - 重复定义
```

**修复后**:
```java
private Integer isUrgent = 0;  // 统一字段

@Min(value = 1, message = "过期时间至少1小时")
@Max(value = 72, message = "过期时间最多72小时")
private Integer expireHours = 24;  // 添加校验
```

### 2.2 N+1 查询优化

**修复前** - `ImDingServiceImpl.java:199-201`:
```java
return dings.stream()
    .map(ding -> convertToVO(ding, userId))  // 每次都查询发送者信息
    .collect(Collectors.toList());
```

**修复后**:
```java
// 批量查询所有发送者信息
Set<Long> senderIds = dings.stream()
    .map(ImDingMessage::getSenderId)
    .filter(Objects::nonNull)
    .collect(Collectors.toSet());
Map<Long, ImUser> senderMap = new HashMap<>();
if (!senderIds.isEmpty()) {
    List<ImUser> senders = userMapper.selectImUserListByIds(new ArrayList<>(senderIds));
    senderMap = senders.stream().collect(Collectors.toMap(ImUser::getId, u -> u));
}

// 批量查询已读状态
Map<Long, Boolean> readStatusMap = new HashMap<>();
if (userId != null) {
    List<Long> dingIds = dings.stream().map(ImDingMessage::getId).collect(Collectors.toList());
    List<Long> readDingIds = dingReadMapper.selectReadDingIdsByUserId(dingIds, userId);
    for (Long dingId : dingIds) {
        readStatusMap.put(dingId, readDingIds.contains(dingId));
    }
}
```

**性能提升**: 从 O(n) 个查询降低到 O(1)（3个批量查询）

---

## 三、前端优化

### 3.1 字段统一

**修复前**:
```javascript
const form = reactive({
  priority: 'NORMAL',  // 字段与后端不匹配
})
```

**修复后**:
```javascript
const form = reactive({
  isUrgent: 0,  // 0: 普通, 1: 紧急
})
```

### 3.2 Emoji 图标替换为 SVG

**修复前**:
```vue
<span class="priority-icon">⚡</span>
<span class="priority-icon">📢</span>
```

**修复后**:
```vue
<svg class="priority-icon" viewBox="0 0 24 24" fill="none">
  <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" fill="currentColor"/>
</svg>
```

**替换范围**:
- `SendDingDialog.vue`: 9 处 emoji 替换为 SVG
- `DingMessageBubble.vue`: 5 处 emoji 替换为 SVG

### 3.3 新增 API 接口

```javascript
/**
 * 获取DING消息已读用户列表
 */
export function getDingReadUsers(dingId)

/**
 * 确认DING消息（需要回执时使用）
 */
export function confirmDing(dingId, remark)
```

---

## 四、验证清单

### 后端
- [x] 无 `var` 关键字
- [x] 无 `record` 类型
- [x] 无重复字段定义
- [x] 字段命名统一（isUrgent）
- [x] 批量查询优化完成

### 前端
- [x] 无 emoji 作为图标
- [x] SVG 图标大小统一
- [x] 字段与后端一致
- [x] API 接口完整

---

## 五、性能指标

| 指标 | 优化前 | 优化后 | 改善 |
|------|-------|--------|------|
| 查询 20 条 DING 消息 | ~40 次数据库查询 | ~3 次数据库查询 | ~92% ↓ |
| 前端首屏渲染 | emoji 字符 | SVG 矢量 | 视觉更专业 |

---

## 六、后续建议

1. **WebSocket 实时更新**: 当用户标记已读时，通过 WebSocket 推送给发送者实时更新已读状态
2. **缓存优化**: 对频繁查询的用户信息使用 Redis 缓存
3. **分页加载**: 大量 DING 消息时使用虚拟滚动或分页
