# RuoYi-IM 个人全栈开发规范

> **版本**: v5.1  
> **定位**: 个人全栈开发约束（Java 1.8 + Vue 3）  
> **适用**: AI辅助开发  
> **更新**: 2026-02-05

---

## 一、绝对红线（必须遵守）

### 1.1 JDK 1.8 约束
**禁止使用的语法**（会导致编译失败）：
- `var` 关键字、 `record` 类型、 `sealed` 关键字
- `switch →` 箭头语法、`"""` 文本块
- `Optional.isEmpty()`、`Collection.toList()`

### 1.2 架构约束
- **分层**: Controller → Service → Mapper，禁止跨层调用
- **数据**: Entity 不能直接作为 API 参数/返回值，用 DTO/VO
- **查询**: MyBatis-Plus 必须用 Lambda，禁止 Magic String

### 1.3 代码质量红线
- 禁止 `System.out.println`（用 log）
- 禁止硬编码密码/密钥（用环境变量）
- 禁止无意义命名：`data`、`info`、`temp`、`obj`

---

## 二、开发流程（简化版）

```
搜索现有代码 → 编写新代码 → 验证无错误
```

**大改动**（改2+文件）需要列出改动计划，小改动直接做。

---

## 三、Java 规范

### 3.1 分层职责
```
Controller: 接收请求、参数校验、调用 Service
Service:    业务逻辑、@Transactional 事务
Mapper:     数据库访问、Lambda 查询
```

### 3.2 命名规范
| 类型 | 格式 | 示例 |
|------|------|------|
| Controller | XxxController | `UserController` |
| Service | XxxService / XxxServiceImpl | `UserService` |
| DTO | XxxDTO | `UserDTO` |
| VO | XxxVO | `UserVO` |
| 方法 | 动词开头 | `getUser`、`createOrder` |

**禁止**: `handle`、`doXxx`、`process` 等模糊命名

### 3.3 MyBatis-Plus 示例
```java
// ✅ 正确
wrapper.eq(ImUser::getId, userId)

// ❌ 错误
wrapper.eq("id", userId)
```

### 3.4 日志规范
```java
// ✅
log.info("用户登录成功, userId={}", userId);
log.error("操作失败, error={}", e.getMessage(), e);

// ❌
System.out.println("处理完成");
e.printStackTrace();
```

---

## 四、Vue 3 规范

### 4.1 基础要求
- 使用 `<script setup>` 语法
- 文件名 PascalCase（如 `UserProfile.vue`）
- CSS 必须加 `scoped`
- `v-for` 必须指定 `:key`

### 4.2 响应式数据
```javascript
// ✅
const count = ref(0)           // 简单类型
const user = reactive({})      // 对象
const { name } = toRefs(user)  // 解构保持响应性

// ❌
const { name } = user          // 失去响应性
```

### 4.3 API 封装
```javascript
// ✅ 封装在 api/ 目录
export function getUserList(params) {
  return request({ url: '/api/user/list', method: 'get', params })
}

// ❌ 组件内直接用 axios
```

---

## 五、项目信息

### 5.1 模块结构
| 模块 | 端口 | 技术栈 |
|------|------|--------|
| ruoyi-im-api | 9999 | Spring Boot 2.7、WebSocket |
| ruoyi-im-web | 5173 | Vue 3、Element Plus |

### 5.2 核心 API
| Controller | 路径 | 功能 |
|------------|------|------|
| ImAuthController | /api/im/auth | 登录/注册 |
| ImMessageController | /api/im/message | 消息 |
| ImConversationController | /api/im/conversation | 会话 |
| ImContactController | /api/im/contact | 联系人 |
| ImUserController | /api/im/user | 用户 |
| ImGroupController | /api/im/group | 群组 |

### 5.3 常用命令
```bash
# 后端启动
cd ruoyi-im-api && mvn clean package

# 前端启动
cd ruoyi-im-web && npm run dev

# 数据库
mysql -h 172.168.20.222 -u im -p123456 im
```

### 5.4 前端目录
```
src/
├── api/           # API接口
├── components/    # 组件
├── views/         # 页面
├── store/         # Vuex
├── router/        # 路由
└── utils/         # 工具
```

---

## 六、AI 助手约束（关键）

### 6.1 禁止行为
- ❌ **不要重复造轮子**：写代码前先搜索现有实现
- ❌ **不要假设API存在**：不确定的方法先 Read 文件确认
- ❌ **不要过度设计**：简单问题用简单方案
- ❌ **不要只写正常流程**：必须处理异常和边界情况
- ❌ **不要编造代码**：只用确定存在的类和方法

### 6.2 垃圾代码禁止项（严格执行）
- ❌ **禁止创建重复组件**：不允许创建 `XxxOptimized`、`XxxRefactored`、`XxxV2` 等后缀组件
- ❌ **禁止在 src 下创建文档**：所有 `.md` 文件必须放在项目根目录或 `docs/` 目录
- ❌ **禁止新建不用的代码**：新建文件前必须确认会被实际引用
- ❌ **禁止删除前不检查**：删除文件前必须用 grep 检查引用关系
- ❌ **禁止空目录**：不要创建空的组件目录或测试目录
- ❌ **禁止废弃代码残留**：标记 `@deprecated` 的代码必须在迁移完成后删除

### 6.3 必须做的
- ✅ 搜索现有代码后再写新代码
- ✅ 使用确定的 API 和方法
- ✅ 处理异常情况（try-catch）
- ✅ 方法不超过 50 行，嵌套不超过 3 层
- ✅ 中文回复 + 中文注释
- ✅ 删除文件前用 grep 检查引用
- ✅ 新建组件前确认功能是否已存在
- ✅ 复用现有组件而非创建新版本

---

## 七、检查清单（提交前）

### 7.1 基础检查
- [ ] 无 Java 9+ 语法（var、record 等）
- [ ] 无 `System.out.println`
- [ ] 无硬编码密码
- [ ] 方法 ≤ 50 行

### 7.2 架构检查
- [ ] Controller 不直接调 Mapper
- [ ] Service 有 `@Transactional`
- [ ] 使用 DTO/VO，不用 Entity 直接传
- [ ] MyBatis-Plus 用 Lambda 查询

### 7.3 前端检查
- [ ] 使用 `<script setup>`
- [ ] CSS 有 `scoped`
- [ ] API 封装在 `api/` 目录
- [ ] 异步操作有 loading 处理

---

## 八、FAQ

**Q: 小改动需要列计划吗？**  
A: 不需要，搜索 → 修改 → 验证即可。

**Q: 必须用 DTO/VO 吗？**  
A: 个人开发可以用 Entity + VO，只要不暴露敏感字段。

**Q: 不确定某个方法是否存在怎么办？**  
A: 先用 Read 读取文件确认，不确定就问我。

**Q: 发现现有代码有重复怎么办？**  
A: 优先复用现有代码，不要重新写。

---

*本文档用于规范 AI 辅助个人全栈开发，保持简单实用*
