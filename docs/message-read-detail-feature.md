# 消息已读回执详情功能文档

## 功能概述

用户可查看自己发送的消息的已读状态，包括：
- 已读人数和未读人数
- 已读用户列表（含阅读时间）
- 未读用户列表

---

## 技术实现

### 后端实现（已完成）

#### 1. 核心接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/im/message/read/detail/{messageId}` | GET | 获取消息已读详情（已读/未读人员列表） |
| `/api/im/message/read/status/{messageId}` | GET | 获取消息已读状态（已读人数和百分比） |

#### 2. 数据结构

**ImMessageReadDetailVO** 响应结构：
```json
{
  "messageId": 123,
  "conversationId": 456,
  "messagePreview": "消息内容预览",
  "sendTime": "2026-03-04 10:30:00",
  "totalCount": 5,
  "readCount": 3,
  "unreadCount": 2,
  "readUsers": [
    {
      "userId": 1,
      "userName": "张三",
      "avatar": "http://...",
      "readTime": "2026-03-04 10:31:00"
    }
  ],
  "unreadUsers": [
    {
      "userId": 2,
      "userName": "李四",
      "avatar": "http://..."
    }
  ]
}
```

#### 3. 已有文件

| 文件 | 说明 |
|------|------|
| `ImMessageReadController.java` | 已读回执控制器 |
| `ImMessageReadService.java` | 业务逻辑接口 |
| `ImMessageReadServiceImpl.java` | 业务逻辑实现 |
| `ImMessageReadDetail.java` | 已读详情实体 |
| `ImMessageReadDetailMapper.java` | 数据访问层 |
| `ImMessageReadDetailVO.java` | 已读详情 VO |

---

### 前端实现（新增）

#### 1. 新增文件

| 文件 | 说明 |
|------|------|
| `src/components/Chat/MessageReadDetailDialog.vue` | 已读详情弹窗组件 |

#### 2. 修改文件

| 文件 | 修改内容 |
|------|----------|
| `src/api/im/message.js` | 新增 `getMessageReadDetail`、`getMessageReadStatus` API |
| `src/components/Chat/MessageItem.vue` | 添加"查看已读"菜单项 |
| `src/components/Chat/MessageList.vue` | 集成已读详情对话框 |
| `src/views/ChatPanel.vue` | 传递 `conversationId` 属性 |

#### 3. 组件功能

**MessageReadDetailDialog.vue**
- 消息预览（内容、发送时间）
- 已读/未读 Tab 切换
- 已读人数统计
- 已读时间格式化（今天/昨天/7 天内）
- 头像加载错误处理

---

## 使用场景

### 场景 1：查看消息已读详情

```
1. 用户发送消息
2. 鼠标悬停消息 → 显示快捷按钮
3. 点击"更多"按钮
4. 选择"查看已读"
5. 弹窗显示已读/未读人员列表
```

### 场景 2：群聊已读状态

```
群主发送 @所有人 消息
  ↓
接收方可查看谁已读
  ↓
显示：已读 (3) / 未读 (7)
```

---

## UI 设计

### 弹窗布局

```
┌────────────────────────────────────┐
│         消息已读详情             × │
├────────────────────────────────────┤
│ 消息内容                             │
│ 这是一条测试消息内容...              │
│ 发送时间：2026-03-04 10:30    已读：3/10  30%│
├────────────────────────────────────┤
│ [已读 (3)]  [未读 (7)]               │
├────────────────────────────────────┤
│ 👤 张三                      发送者 │
│    今天 10:31                       │
├────────────────────────────────────┤
│ 👤 李四                             │
│    今天 10:32                       │
├────────────────────────────────────┤
│ 👤 王五                             │
│    昨天 14:20                       │
└────────────────────────────────────┘
```

---

## 交互细节

### 已读时间格式化

| 时间范围 | 显示格式 |
|----------|----------|
| 今天 | `今天 HH:mm` |
| 昨天 | `昨天 HH:mm` |
| 7 天内 | `周几 HH:mm` |
| 更早 | `YYYY-MM-DD HH:mm` |

### 未读状态

- 显示"尚未阅读"
- 无时间信息

### 空状态

- 已读列表为空：显示"暂无已读"
- 未读列表为空：显示"全部已读"

---

## 代码示例

### 前端调用

```javascript
// 获取已读详情
const { data } = await getMessageReadDetail(messageId)

// data 结构
{
  code: 200,
  data: {
    readCount: 3,
    readUsers: [{ userId, userName, avatar, readTime }],
    unreadUsers: [{ userId, userName, avatar }]
  }
}
```

### 后端 Service

```java
// 获取已读详情
ImMessageReadDetailVO detail = messageReadService.getMessageReadDetail(messageId, userId);

// 返回列表包含：
// - readUsers: 已读用户列表（带头像、昵称、阅读时间）
// - unreadUsers: 未读用户列表（带头像、昵称）
```

---

## 测试用例

### 测试 1：查看已读详情

```
前置条件：群聊中有 5 人
步骤：
1. 用户 A 发送消息
2. 用户 B、C、D 已读
3. 用户 A 点击"查看已读"

期望结果：
- 显示已读 (3) / 未读 (1)
- 已读列表：B、C、D（带阅读时间）
- 未读列表：E
```

### 测试 2：私聊已读

```
前置条件：私聊会话
步骤：
1. 发送消息
2. 对方已读

期望结果：
- 已读人数：1
- 未读人数：0
```

### 测试 3：空状态

```
前置条件：新发送消息，无人已读
步骤：点击查看已读

期望结果：
- 已读列表显示"暂无已读"
- 未读列表显示所有接收人
```

---

## 后续优化

1. **批量查看**：支持一次查看多条消息的已读状态
2. **导出已读**：支持导出已读人员列表
3. **已读提醒**：对未读人员发送提醒
4. **已读统计**：管理员可查看群消息的平均阅读率
