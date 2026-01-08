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

---

### 3. ImTodoItemMapper.xml 字段不匹配

**问题描述：**
```
GET /api/im/workbench/overview 返回 500 错误
GET /api/im/workbench/todos 返回 500 错误
```

**原因：**
- Mapper XML 使用了 `is_completed` 字段
- Mapper XML 引用了不存在的 `type` 和 `related_id` 字段
- 实际数据库表使用 `status` 字段 (PENDING/IN_PROGRESS/COMPLETED/CANCELLED)

**修复内容：**
- resultMap 中 `is_completed` → `status`
- selectImTodoItemVo 中移除 `type` 和 `related_id`
- 查询条件 `is_completed = false` → `status = 'PENDING'`
- 排序条件移除 `is_completed`
- insert/update 语句移除 `type` 和 `related_id`
- markAsCompleted 语句更新为 `status = 'COMPLETED'`

**修改文件：**
- `ruoyi-im-api/src/main/resources/mapper/ImTodoItemMapper.xml`

---

### 4. ImGroupServiceImpl 使用非数据库字段

**问题描述：**
```
GET /api/im/group/list 返回 500 错误
```

**原因：**
- `ImGroup` 实体的 `status` 字段标记为 `@TableField(exist = false)`
- 服务代码尝试检查 `group.getStatus() == "NORMAL"`，该字段始终为 null
- 实际的状态字段是 `is_deleted` (0=正常, 1=已删除)

**修复内容：**
- `getUserGroups()` 方法：`"NORMAL".equals(group.getStatus())` → `group.getIsDeleted() == 0`
- `dismissGroup()` 方法：
  - `group.setStatus("DISMISSED")` → `group.setIsDeleted(1)`
  - 添加 `group.setDeletedTime(LocalDateTime.now())`

**修改文件：**
- `ruoyi-im-api/src/main/java/com/ruoyi/im/service/impl/ImGroupServiceImpl.java`

---

## 待修复问题

### 1. 登录接口返回 500 (用户不存在)
- **状态**: 非Bug，正常业务逻辑
- **说明**: 当用户不存在时，服务正确返回 500 状态码，错误信息为 "用户不存在 [USER_NOT_EXIST]"
- **建议**: 可优化为返回 400 或 404 状态码，但当前实现符合业务逻辑

### 2. 注册接口返回 500
- **状态**: 待排查
- **说明**: 注册用户时返回 500 错误，需要进一步排查具体原因

---

## 需要重新编译的项目

修改了 Mapper XML 文件和 Java 源文件，需要重新编译：

```bash
cd D:\MyCode\im\ruoyi-im-api
mvn clean compile
```

然后重启应用服务器。
