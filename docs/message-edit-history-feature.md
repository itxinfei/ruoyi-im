# 消息编辑历史查看功能文档

## 功能概述

用户可以查看已编辑消息的历史记录，包括：
- 每次编辑的时间和编辑人
- 编辑前的内容（删除线红色显示）
- 编辑后的内容（蓝色显示）
- 编辑次数统计

---

## 技术实现

### 后端实现

#### 1. 数据库表：`im_message_edit_history`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) | 主键 |
| message_id | bigint(20) | 消息 ID（外键） |
| editor_id | bigint(20) | 编辑人 ID |
| old_content | text | 编辑前的内容 |
| new_content | text | 编辑后的内容 |
| edit_time | datetime | 编辑时间 |
| create_time | datetime | 创建时间 |

索引：
- `idx_message_id`: 消息 ID 索引
- `idx_editor_id`: 编辑人 ID 索引
- `idx_edit_time`: 编辑时间索引

#### 2. API 接口

**获取消息编辑历史**
```
GET /api/im/message/edit-history/{messageId}

响应:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "messageId": 123,
      "editorId": 1,
      "editorName": "张三",
      "editorAvatar": "http://...",
      "oldContent": "原始内容",
      "newContent": "修改后的内容",
      "editTime": "2026-03-04 10:30:00"
    }
  ]
}
```

**获取消息编辑统计**
```
GET /api/im/message/edit-history/stats/{messageId}

响应:
{
  "code": 200,
  "data": {
    "messageId": 123,
    "editTimes": 3
  }
}
```

#### 3. 已有文件

| 文件 | 说明 |
|------|------|
| `ImMessageEditHistory.java` | 编辑历史实体 |
| `ImMessageEditHistoryMapper.java` | Mapper 接口 |
| `MessageEditHistoryVO.java` | 编辑历史 VO |

#### 4. 新增文件

| 文件 | 说明 |
|------|------|
| `ImMessageEditHistoryService.java` | Service 接口 |
| `ImMessageEditHistoryServiceImpl.java` | Service 实现 |
| `ImMessageEditHistoryController.java` | Controller |
| `sql/migrations/20260304_message_edit_history.sql` | 数据库迁移脚本 |

---

### 前端实现

#### 1. 新增文件

| 文件 | 说明 |
|------|------|
| `components/Chat/MessageEditHistoryDialog.vue` | 编辑历史弹窗组件 |

#### 2. 修改文件

| 文件 | 修改内容 |
|------|----------|
| `api/im/message.js` | +getMessageEditHistory API |
| `components/Chat/MessageItem.vue` | + 编辑历史菜单项 |
| `components/Chat/MessageList.vue` | + 集成弹窗组件 |

#### 3. 组件功能

**MessageEditHistoryDialog.vue**
- 消息当前内容预览
- 编辑次数显示
- 编辑历史记录列表
- 编辑前后内容对比（红色删除线 vs 蓝色新增）
- 编辑人头像和昵称
- 编辑时间格式化

---

## 使用场景

### 场景 1：查看消息编辑历史

```
1. 消息显示"(已编辑)" 标记
2. 鼠标悬停消息 → 显示快捷按钮
3. 点击"更多"按钮
4. 选择"编辑历史"
5. 弹窗显示所有编辑记录
```

### 场景 2：对比内容变化

```
编辑历史弹窗中：
┌─────────────────────────────────┐
│ 编辑前（红色删除线）            │
│ 这是原始内容                    │
│          ↓                      │
│ 编辑后（蓝色）                  │
│ 这是修改后的内容                │
└─────────────────────────────────┘
```

---

## UI 设计

### 弹窗布局

```
┌───────────────────────────────────────────┐
│         消息编辑历史                   ×  │
├───────────────────────────────────────────┤
│ 当前内容                                  │
│ 这是消息的最新内容...                     │
│ 编辑次数：3 次                            │
├───────────────────────────────────────────┤
│ 📋 编辑记录                               │
├───────────────────────────────────────────┤
│ 👤 张三                              最新 │
│    2026-03-04 10:35:00                    │
│ ┌─────────────────────────────────────┐   │
│ │ 编辑前                              │   │
│ │ 这是第二次修改的内容                │   │
│ │          ↓                          │   │
│ │ 编辑后                              │   │
│ │ 这是消息的最新内容                  │   │
│ └─────────────────────────────────────┘   │
├───────────────────────────────────────────┤
│ 👤 李四                                   │
│    2026-03-04 10:32:00                    │
│ ┌─────────────────────────────────────┐   │
│ │ 编辑前                              │   │
│ │ 这是原始内容                        │   │
│ │          ↓                          │   │
│ │ 编辑后                              │   │
│ │ 这是第二次修改的内容                │   │
│ └─────────────────────────────────────┘   │
└───────────────────────────────────────────┘
```

---

## 编辑规则

### 编辑限制

| 限制项 | 说明 |
|--------|------|
| 消息类型 | 仅支持 TEXT 文本消息 |
| 编辑时限 | 发送后 5 分钟内可编辑 |
| 编辑权限 | 仅消息发送者可编辑 |
| 撤回消息 | 已撤回的消息不能编辑 |

### 编辑历史保存

每次编辑时：
1. 保存编辑前的内容到 `old_content`
2. 保存编辑后的内容到 `new_content`
3. 记录编辑人 ID 和编辑时间
4. 更新消息的 `edited_content`、`is_edited`、`edit_count`、`edit_time`

---

## 代码示例

### 前端调用

```javascript
// 获取编辑历史
const { data } = await getMessageEditHistory(messageId)

// data 结构
[
  {
    id: 1,
    editorName: '张三',
    editorAvatar: 'http://...',
    oldContent: '原始内容',
    newContent: '修改后的内容',
    editTime: '2026-03-04 10:30:00'
  }
]
```

### 后端保存编辑历史

```java
// 在 ImMessageServiceImpl.editMessage() 中
String oldContent = message.getIsEdited() == 1
    ? message.getEditedContent()
    : message.getContent();

ImMessageEditHistory history = new ImMessageEditHistory();
history.setMessageId(messageId);
history.setOldContent(oldContent);
history.setNewContent(newContent);
history.setEditorId(userId);
history.setEditTime(LocalDateTime.now());
editHistoryMapper.insert(history);
```

---

## 测试用例

### 测试 1：查看编辑历史

```
前置条件：消息已被编辑 3 次
步骤：
1. 鼠标悬停消息
2. 点击"更多" → "编辑历史"

期望结果：
- 弹窗显示 3 条编辑记录
- 每条记录显示编辑前后内容对比
- 显示编辑人和编辑时间
```

### 测试 2：首次编辑

```
前置条件：消息未编辑过
步骤：
1. 编辑消息
2. 查看编辑历史

期望结果：
- 显示 1 条编辑记录
- oldContent 为原始内容
- newContent 为编辑后内容
```

### 测试 3：无权查看

```
前置条件：群聊中其他人编辑的消息
步骤：点击查看编辑历史

期望结果：
- 可以查看编辑历史（编辑历史对所有人可见）
```

---

## 样式设计

### 编辑前后对比样式

| 元素 | 样式 |
|------|------|
| 编辑前背景 | 浅红色 `#fef0f0` |
| 编辑前边框 | 红色 `#fde2e2` |
| 编辑前文字 | 红色 `#f56c6c` + 删除线 |
| 编辑后背景 | 浅蓝色 `#f0f9ff` |
| 编辑后边框 | 蓝色 `#e1f3fd` |
| 编辑后文字 | 蓝色 `#1677ff` |

---

## 后续优化

1. **差异高亮**：使用 diff 算法高亮具体变化的文字
2. **折叠展开**：支持折叠/展开编辑记录
3. **还原功能**：支持还原到历史版本
4. **编辑通知**：编辑后通知会话成员
