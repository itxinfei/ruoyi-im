# API接口规范化方案

## 一、现状分析

### 1.1 导出方式不一致

| 文件 | 导出方式 | 问题 |
|------|----------|------|
| `message.js` | 具名导出 `export function` | ✅ 推荐 |
| `conversation.js` | 默认导出 `export default {}` | ❌ 不推荐 |
| `login.js` | 具名导出 `export function` | ✅ 推荐 |
| `group.js` | 混合导出 | ❌ 不推荐 |

**问题**：不同导出方式导致使用方式不统一，增加记忆成本。

### 1.2 API路径不一致

```javascript
// 消息相关
'/api/im/message/send'         // 正常IM接口
'/api/admin/im/message/unread/count'  // 管理员接口
```

**问题**：普通IM接口和管理员接口混在一起，不利于权限控制。

### 1.3 参数命名不一致

```javascript
// 会话ID参数混用
sessionId vs conversationId

// 分页参数混用
pageSize vs limit

// 消息ID参数混用
lastId vs lastMessageId
```

### 1.4 请求方式不规范

```javascript
// 有些用 data 传参数
{ url: '/api/xxx', method: 'post', data: params }

// 有些用 params 传参数
{ url: '/api/xxx', method: 'post', params: data }
```

---

## 二、统一规范

### 2.1 导出方式统一

**规则**：全部使用具名导出 + 默认导出对象

```javascript
// ✅ 推荐写法
import request from '@/utils/request'

// 具名导出 - 方便按需引入
export function sendMessage(data) { ... }
export function getMessageList(params) { ... }

// 默认导出 - 方便批量引入
export default {
  sendMessage,
  getMessageList,
}
```

### 2.2 API路径规范

```
/api/im/{module}/{action}          # IM普通接口
/api/admin/im/{module}/{action}    # IM管理员接口
/api/system/{module}/{action}      # 系统管理接口
/api/auth/{action}                 # 认证相关接口
```

### 2.3 参数命名统一

| 场景 | 统一命名 | 说明 |
|------|----------|------|
| 会话ID | `conversationId` | 全局统一 |
| 用户ID | `userId` | 全局统一 |
| 群组ID | `groupId` | 全局统一 |
| 消息ID | `messageId` | 全局统一 |
| 分页大小 | `pageSize` | 全局统一 |
| 当前页 | `pageNum` | 全局统一 |
| 上一页ID | `lastId` | 全局统一 |

### 2.4 请求方式规范

```javascript
// GET 请求 - 使用 params
{
  url: '/api/im/message/list',
  method: 'get',
  params: { conversationId, pageSize, lastId }
}

// POST 请求 - 使用 data
{
  url: '/api/im/message/send',
  method: 'post',
  data: { conversationId, content, type }
}
```

### 2.5 函数命名规范

| 操作 | 命名 | 示例 |
|------|------|------|
| 列表 | `list{Module}` | `listMessage` |
| 详情 | `get{Module}` | `getMessageDetail` |
| 创建 | `create{Module}` | `createConversation` |
| 更新 | `update{Module}` | `updateUserInfo` |
| 删除 | `delete{Module}` | `deleteMessage` |
| 发送 | `send{Module}` | `sendMessage` |
| 搜索 | `search{Module}` | `searchMessages` |

---

## 三、重构计划

### 3.1 优先级 P0（立即执行）

1. **统一 `conversation.js` 导出方式**
   - 将默认导出改为具名导出 + 默认导出对象

2. **统一参数命名**
   - `sessionId` → `conversationId`
   - `limit` → `pageSize`

### 3.2 优先级 P1（近期执行）

1. **分离管理员接口**
   - 创建 `api/admin/` 目录
   - 移动管理员接口到独立文件

2. **统一函数命名**
   - 按规范重命名不一致的函数

### 3.3 优先级 P2（中期执行）

1. **添加TypeScript类型定义**
   - 创建 `api/types/` 目录
   - 定义接口参数和返回值类型

2. **添加接口注释**
   - 统一使用JSDoc注释
   - 标注参数类型和返回值

---

## 四、示例代码

### 4.1 标准API模块模板

```javascript
/**
 * 消息模块API
 * @module api/im/message
 */
import request from '@/utils/request'

/**
 * 发送消息
 * @param {Object} data - 消息数据
 * @param {string} data.conversationId - 会话ID
 * @param {string} data.type - 消息类型
 * @param {string} data.content - 消息内容
 * @param {string} [data.replyToMessageId] - 回复消息ID
 * @returns {Promise}
 */
export function sendMessage(data) {
  return request({
    url: '/api/im/message/send',
    method: 'post',
    data,
  })
}

/**
 * 获取消息列表
 * @param {Object} params - 查询参数
 * @param {string} params.conversationId - 会话ID
 * @param {number} [params.pageSize=20] - 每页数量
 * @param {string} [params.lastId] - 上一页最后一条消息ID
 * @returns {Promise}
 */
export function listMessage(params) {
  return request({
    url: `/api/im/message/list/${params.conversationId}`,
    method: 'get',
    params: {
      pageSize: params.pageSize || 20,
      lastId: params.lastId,
    },
  })
}

// 默认导出 - 方便批量引入
export default {
  sendMessage,
  listMessage,
}
```

### 4.2 使用方式

```javascript
// 方式1：按需引入（推荐）
import { sendMessage, listMessage } from '@/api/im/message'

sendMessage({ conversationId: 'xxx', content: 'hello' })

// 方式2：整体引入
import messageApi from '@/api/im/message'

messageApi.sendMessage({ conversationId: 'xxx', content: 'hello' })

// 方式3：从统一入口引入
import { messageApi } from '@/api/im'

messageApi.sendMessage({ conversationId: 'xxx', content: 'hello' })
```

---

## 五、检查清单

### 5.1 API文件检查

- [ ] 所有API文件使用统一的导出方式
- [ ] 函数命名符合规范
- [ ] API路径符合规范
- [ ] 参数命名统一
- [ ] 添加JSDoc注释

### 5.2 使用检查

- [ ] 组件中使用正确的导入方式
- [ ] 调用时参数命名正确
- [ ] 错误处理统一

---

## 六、注意事项

1. **向后兼容**：重构时保持旧函数名作为别名
2. **渐进式迁移**：先新建规范文件，再逐步迁移使用
3. **充分测试**：每次修改后进行接口测试
4. **文档更新**：同步更新API文档
