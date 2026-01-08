# Bug 修复记录

## 2026-01-08

### 1. ImFriendMapper.xml 列名不匹配

**问题描述：**
```
Error querying database. Cause: java.sql.SQLSyntaxErrorException: Unknown column 'friend_user_id' in 'field list'
```

**原因：**
- Mapper XML 使用了 `friend_user_id` 列名
- 实际数据库表 `im_friend` 的列名是 `friend_id`
- 实体类 `ImFriend` 正确使用了 `@TableField("friend_id")`

**修复内容：**
- `ImFriendMapper.xml` 中所有 `friend_user_id` → `friend_id`
- 移除了 `selectImFriendVo` 中的 `status` 字段（status 在实体类中标记为 `@TableField(exist = false)`，非数据库字段）
- 更新了 insert/update 语句中的列名

**修改文件：**
- `ruoyi-im-api/src/main/resources/mapper/ImFriendMapper.xml`

---

### 2. 前端参数名不匹配导致 undefined 错误

**问题描述：**
```
NumberFormatException: For input string: "undefined" - /api/im/message/list/undefined
```

**原因：**
- 后端 API: `GET /api/im/message/list/{conversationId}`
- 前端 store 调用 `listMessage({ sessionId, ... })`
- 前端 API 函数期望 `params.conversationId`
- 导致 `conversationId` 为 `undefined`，字符串 "undefined" 被传到后端

**修复内容：**
- `store/modules/im.js` 的 `loadMessages` action 中：
  ```javascript
  // 修复前
  const response = await listMessage({ sessionId, page, pageSize })

  // 修复后
  const response = await listMessage({ conversationId: sessionId, page, pageSize })
  ```

**修改文件：**
- `ruoyi-im-web/src/store/modules/im.js`

**注意：**
- 前端使用 `sessionId` (会话ID) 概念
- 后端使用 `conversationId` (对话ID) 概念
- 两者指向同一实体，需要在调用 API 时进行参数名映射
