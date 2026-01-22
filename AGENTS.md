# AGENTS.md - RuoYi-IM AI 助手工作指南

本文档帮助 AI 助手在 RuoYi-IM 代码库中有效工作。它补充了现有的 `.agent/rules/im.md` 和 `CLAUDE.md` 文件，提供实用的操作信息。

---

## 项目概述

**RuoYi-IM** 是部署在内网环境的企业级即时通讯系统，由以下三个模块组成：

- **ruoyi-im-api** (端口 8080)：核心 IM 服务 - WebSocket/REST API，Spring Boot 2.7，MyBatis-Plus
- **ruoyi-im-admin** (端口 8081)：后台管理系统 - RuoYi v4.8.0，Apache Shiro，Thymeleaf
- **ruoyi-im-web** (端口 3000/5173)：前端聊天界面 - Vue 3 Composition API，Vite，Element Plus

**核心原则**：API 服务（8080）与管理系统（8081）**独立且解耦**。不要假设它们共享依赖或数据结构。

---

## 常用命令

### 后端开发 (ruoyi-im-api)

```bash
# 进入 API 模块
cd ruoyi-im-api

# 构建 JAR 包
mvn clean package

# 安装到本地 Maven 仓库
mvn clean install

# 运行应用（Spring Boot）
# 主类：com.ruoyi.im.ImApplication
# 默认端口：8080
# 访问 Swagger 文档：http://localhost:8080/swagger-ui.html

# 运行测试（注意：pom.xml 中目前跳过了测试）
mvn test
```

### 后端开发 (ruoyi-im-admin)

```bash
# 进入管理模块
cd ruoyi-im-admin

# 构建所有模块
mvn clean package

# 运行管理系统
# 主类：com.ruoyi.RuoYiApplication
# 默认端口：8081
# 访问管理面板：http://localhost:8081
# 默认账号：admin / 密码：123456
```

### 前端开发 (ruoyi-im-web)

```bash
# 进入前端模块
cd ruoyi-im-web

# 安装依赖（首次运行）
npm install

# 启动开发服务器（Vite）
# 默认端口：3000（可配置）
# 将 /api 代理到 http://localhost:8080
npm run dev

# 生产环境构建
npm run build

# 预览生产构建
npm run preview
```

### 数据库操作

```bash
# 连接 MySQL（根据需要调整凭据）
mysql -h 172.168.20.222 -P 3306 -u im -p123456 im

# 导入数据库结构
mysql -u root -p im < [sql文件路径]
```

---

## 项目结构与组织

### 后端结构 (ruoyi-im-api)

```
ruoyi-im-api/
├── src/main/java/com/ruoyi/im/
│   ├── ImApplication.java              # Spring Boot 入口类
│   ├── config/                         # 配置类
│   │   ├── ImConfig.java               # IM 特定配置
│   │   ├── SecurityConfig.java         # Spring Security 配置
│   │   ├── ImWebSocketConfig.java      # WebSocket 端点配置
│   │   └── MyBatisConfig.java          # MyBatis-Plus 配置
│   ├── controller/                     # REST API 控制器
│   │   └── ImXxxController.java        # 命名模式：Im[资源]Controller
│   ├── service/                        # 业务逻辑层
│   │   ├── IXxxService.java            # Service 接口
│   │   └── impl/XxxServiceImpl.java    # Service 实现
│   ├── mapper/                         # 数据访问层
│   │   └── ImXxxMapper.java            # MyBatis-Plus Mapper
│   ├── domain/                         # 实体类
│   │   └── ImXxx.java                  # 映射到数据库表
│   ├── dto/                            # 请求 DTO
│   │   └── XxxRequest.java             # 命名模式：[动作][资源]Request
│   ├── vo/                             # 响应 VO
│   │   └── XxxVO.java                  # 命名模式：[资源]VO
│   ├── websocket/                      # WebSocket 处理器
│   │   └── ImWebSocketEndpoint.java    # 主要 WebSocket 端点
│   ├── util/                           # 工具类
│   └── exception/                      # 异常处理
├── src/main/resources/
│   ├── application.yml                 # 主配置文件
│   ├── mapper/                         # MyBatis XML 映射文件
│   └── static/                         # 静态资源
└── src/test/                           # 测试代码
```

### 前端结构 (ruoyi-im-web)

```
ruoyi-im-web/
├── src/
│   ├── api/                            # API 客户端模块
│   │   ├── request.js                  # Axios 实例配置
│   │   └── im/                         # IM 特定 API 客户端
│   │       ├── message.js
│   │       ├── conversation.js
│   │       ├── user.js
│   │       └── ...
│   ├── views/                          # 页面组件
│   │   ├── ChatPanel.vue
│   │   ├── ContactsPanel.vue
│   │   └── ...
│   ├── components/                     # 可复用组件
│   │   ├── Chat/
│   │   │   ├── MessageList.vue
│   │   │   ├── MessageInput.vue
│   │   │   └── ...
│   │   └── ...
│   ├── store/                          # Vuex store
│   │   └── modules/
│   │       ├── im.js                  # IM 状态管理
│   │       └── user.js
│   ├── composables/                    # Vue 组合式函数
│   │   └── useImWebSocket.js           # WebSocket 逻辑
│   ├── router/                         # Vue Router 配置
│   ├── utils/                          # 工具函数
│   │   └── websocket/
│   │       └── imWebSocket.js          # WebSocket 单例
│   └── main.js                         # 应用入口
├── vite.config.js                      # Vite 配置
├── package.json                        # Node 依赖
└── index.html                          # HTML 模板
```

---

## 代码模式与约定

### 后端模式

**Controller 模式**：
```java
@Tag(name = "资源名称", description = "API 描述")
@RestController
@RequestMapping("/api/im/resource")
public class ImResourceController {

    private final IResourceService resourceService;

    public ImResourceController(IResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/create")
    public Result<ResponseType> create(@Valid @RequestBody CreateRequest request,
                                       @RequestHeader(value = "userId", required = false) Long userId) {
        // 实现代码
        return Result.success(data);
    }
}
```

**Service 模式**：
```java
@Service
public class ResourceServiceImpl implements IResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createResource(CreateRequest request) {
        // 业务逻辑
        return resourceId;
    }
}
```

**Mapper 模式**（MyBatis-Plus）：
```java
public interface ImMessageMapper extends BaseMapper<ImMessage> {
    // 使用 LambdaQueryWrapper 的自定义查询
    default List<ImMessage> findByConversationId(Long conversationId) {
        return selectList(new LambdaQueryWrapper<ImMessage>()
            .eq(ImMessage::getConversationId, conversationId)
            .orderByDesc(ImMessage::getCreateTime));
    }
}
```

**DTO 模式**：
```java
@Data
public class ImMessageSendRequest {
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000字符")
    private String content;
}
```

### 前端模式

**组件模式**（Composition API）：
```vue
<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'

// Props
const props = defineProps({
  session: Object
})

// Emits
const emit = defineEmits(['update', 'delete'])

// 状态
const loading = ref(false)
const data = ref([])

// 计算属性
const filtered = computed(() => data.value.filter(...))

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const res = await apiCall()
    data.value = res.data
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="component">
    <!-- 模板 -->
  </div>
</template>

<style scoped>
/* 样式 */
</style>
```

**API 客户端模式**：
```javascript
import request from '@/utils/request'

export function createResource(data) {
  return request({
    url: '/api/im/resource/create',
    method: 'post',
    data
  })
}

export function getResourceList(params) {
  return request({
    url: '/api/im/resource/list',
    method: 'get',
    params
  })
}
```

---

## 命名规范

### Java 类命名
- **控制器**：`Im[资源]Controller`（如 `ImMessageController`）
- **服务层**：`I[资源]Service`（接口）和 `[资源]ServiceImpl`（实现）
- **数据层**：`Im[资源]Mapper`
- **实体类**：`Im[资源]`（如 `ImMessage`、`ImUser`）
- **DTO**：`[动作][资源]Request`（如 `ImMessageSendRequest`、`UserCreateRequest`）
- **VO**：`[资源]VO`（如 `ImMessageVO`、`UserVO`）
- **异常类**：`BusinessException`

### 数据库表命名
- 前缀使用 `im_`（如 `im_message`、`im_user`）
- 使用 `snake_case`（如 `conversation_id`、`create_time`）
- 通用字段：`id`、`create_time`、`update_time`、`del_flag`（0=正常，1=删除）

### 前端文件命名
- **组件**：PascalCase（如 `MessageList.vue`、`ChatHeader.vue`）
- **视图**：PascalCase（如 `ChatPanel.vue`、`ContactsPanel.vue`）
- **API 模块**：kebab-case.js（如 `message.js`、`conversation.js`）
- **组合式函数**：`use[功能].js`（如 `useImWebSocket.js`）

---

## 配置文件

### 后端配置

**ruoyi-im-api/src/main/resources/application.yml**：
- 服务器端口：8080
- 数据库：MySQL（172.168.20.222:3306/im）
- Redis：172.168.20.222:6379（密码：123456）
- MyBatis-Plus 映射文件位置：`classpath*:/mapper/**/*Mapper.xml`
- 文件上传：单个文件最大 20MB
- JWT 密钥和消息加密密钥为 Base64 编码

**ruoyi-im-admin/ruoyi-admin/src/main/resources/application.yml**：
- 服务器端口：8081
- 数据库：与 API 相同
- Redis：与 API 相同
- 使用 Apache Shiro 进行身份验证

### 前端配置

**ruoyi-im-web/vite.config.js**：
- 开发服务器端口：3000
- 代理：`/api` → `http://localhost:8080`
- 路径别名：`@` → `src`

**ruoyi-im-web/package.json** 脚本：
- `npm run dev`：启动开发服务器
- `npm run build`：生产环境构建
- `npm run preview`：预览构建

---

## 关键注意事项与非显而易见的模式

### 1. JDK 1.8 严格兼容性
- **关键**：不要使用 Java 9+ 特性（var、record、sealed、switch 表达式、文本块）
- 项目通过 Maven 编译器插件强制 JDK 1.8 兼容性
- 完整的禁止语法列表见 `.agent/rules/im.md`

### 2. 三层数据隔离
- **Entity** ↔ **DTO** ↔ **VO**
- 永远不要将 Entity 直接用作 API 参数或返回值
- Controller 必须转换 DTO → Entity → VO
- 示例：`BeanUtils.copyProperties(dto, entity)` 然后 `BeanUtils.copyProperties(entity, vo)`

### 3. 分层架构约束
- Controller → Service → Mapper（单向依赖）
- Controller 不能直接调用 Mapper
- Service 不能调用 Controller
- 所有业务逻辑必须在 Service 层

### 4. MyBatis-Plus Lambda 查询
- 使用 Lambda 风格：`.eq(ImUser::getId, userId)`
- 永远不要使用魔法字符串：`.eq("id", userId)`
- 大查询必须分页（每页最多 1000 条）
- 永远不要使用 `SELECT *` - 明确指定字段

### 5. WebSocket 通信
- 端点：`/ws/im`（由 `ImWebSocketEndpoint` 处理）
- 消息类型：`auth`、`message`、`ping`、`pong`、`read`、`typing`
- 心跳间隔：30 秒
- 连接需要在查询参数中提供 JWT token

### 6. Redis 使用模式
- 用户在线状态存储在 Redis 中
- Token 缓存带 TTL
- 分布式锁用于并发操作
- 消息队列用于异步广播

### 7. 事务管理
- 修改数据的 Service 方法必须有 `@Transactional(rollbackFor = Exception.class)`
- Controller 中的多个 Service 调用需要在 Service 层包装事务

### 8. 异常处理
- 不要广泛捕获 `Exception` 或 `Throwable`
- 使用特定的异常
- 永远不要吞掉异常（空的 catch 块）
- 在重新抛出业务异常前始终记录日志
- 对已知错误条件使用 `BusinessException`

### 9. 前后端集成
- API 基础 URL：`/api`（在开发环境中代理到 localhost:8080）
- 授权头：`Bearer {token}` 存储在 localStorage 中
- WebSocket URL：`ws://localhost:8080/ws/im?token={token}`
- 响应格式：`{ code: 200, msg: "success", data: {...} }`
- 错误码：200=成功，401=未授权，403=禁止访问，500=服务器错误

### 10. Vue 3 特定要求
- 必须使用 `<script setup>` 语法
- 必须使用 Composition API（不支持 Options API）
- CSS 必须具有 `scoped` 属性
- Props 必须定义类型和默认值
- Emits 必须显式定义
- 通过 `defineExpose` 暴露方法

### 11. 文件上传处理
- 后端支持大文件分片上传
- 最大文件大小：20MB（可配置）
- 上传路径：项目根目录 `uploads/` 文件夹
- 下载链接 60 分钟后过期（可配置）

### 12. 敏感词过滤
- 内置敏感词检测
- 可配置操作：REJECT（拒绝）、MASK（遮蔽）或 WARN（警告）
- 在 `application.yml` 中默认启用

### 13. 消息加密
- 消息可以在静态时加密
- 在 `application.yml` 中可配置（`im.message.encryption.enabled`）
- 密钥为 Base64 编码

### 14. 测试状态
- 测试文件存在于 `ruoyi-im-api/src/test/`
- 测试目前在 Maven 配置中跳过
- 要运行测试：从 pom.xml 中删除 `<skip>true</skip>`

### 15. API 与管理系统独立性
- API 服务（8080）和管理系统（8081）是**解耦的**
- 不要假设它们共享数据库架构或代码
- 每个都有自己的 Spring Boot 应用入口点
- 它们可能有不同版本的相同库

---

## 修改前检查清单

### 检查清单
1. ✅ 搜索现有实现（Grep/Glob）
2. ✅ 阅读相关配置文件
3. ✅ 理解数据流（Entity ↔ DTO ↔ VO）
4. ✅ 验证 pom.xml 或 package.json 中是否存在依赖
5. ✅ 检查是否已存在类似代码
6. ✅ 识别所有受影响的文件
7. ✅ 规划实现方法

### 搜索命令
```bash
# 查找所有控制器
Glob: "**/*Controller.java"

# 查找所有服务
Glob: "**/*Service*.java"

# 查找所有实体
Glob: "**/domain/Im*.java"

# 搜索特定功能
Grep: "sendMessage"
Grep: "WebSocket"
```

---

## 常见工作流程

### 添加新功能（后端）

1. **创建实体**（如果需要新表）
   - 添加 `@TableName` 注解
   - 使用 `@TableField` 映射字段
   - 用 `@TableField(exist = false)` 标记非数据库字段

2. **创建 Mapper**
   - 继承 `BaseMapper<Entity>`
   - 使用 LambdaQueryWrapper 添加自定义查询

3. **创建 DTO**
   - 使用验证注解（`@NotNull`、`@NotBlank`、`@Size`）
   - 只包含用户提供的字段

4. **创建 VO**
   - 只包含返回给前端的字段
   - 可以包含计算/派生字段

5. **创建服务接口和实现**
   - 为修改数据的方法添加 `@Transactional`
   - 实现业务逻辑

6. **创建 Controller**
   - 使用 `@RestController` 和 `@RequestMapping`
   - 为 DTO 使用 `@Validated`
   - 返回 `Result<T>` 包装器

7. **创建 Mapper XML**（如果需要复杂 SQL）
   - 放置在 `src/main/resources/mapper/`

### 添加新功能（前端）

1. **创建 API 客户端**
   - 添加文件到 `src/api/im/`
   - 使用配置的 Axios 实例
   - 导出命名函数

2. **创建组件**
   - 使用 `<script setup>`
   - 定义 props 和 emits
   - 使用 Composition API 实现
   - 添加 scoped 样式

3. **添加路由**（如果是页面级组件）
   - 更新路由器配置

4. **与 Store 集成**（如果需要状态管理）
   - 向 Vuex 模块添加 actions/mutations

### 调试问题

**后端问题**：
- 检查日志：`tail -f logs/im-api.log`
- 验证数据库连接
- 检查 Redis 连接：`redis-cli -h 172.168.20.222 -p 6379 -a 123456 ping`
- 使用 Swagger UI：http://localhost:8080/swagger-ui.html

**前端问题**：
- 检查浏览器控制台是否有错误
- 验证 API 代理配置
- 在 DevTools 中检查网络选项卡
- 在 Network → WS 选项卡中验证 WebSocket 连接

---

## 重要文件位置

| 文件 | 用途 |
|------|------|
| `ruoyi-im-api/src/main/java/com/ruoyi/im/ImApplication.java` | API 服务入口点 |
| `ruoyi-im-admin/ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java` | 管理系统入口点 |
| `ruoyi-im-api/src/main/resources/application.yml` | API 配置 |
| `ruoyi-im-admin/ruoyi-admin/src/main/resources/application.yml` | 管理配置 |
| `ruoyi-im-web/vite.config.js` | 前端构建配置 |
| `ruoyi-im-web/src/api/request.js` | Axios 配置 |
| `ruoyi-im-web/src/composables/useImWebSocket.js` | WebSocket 逻辑 |
| `ruoyi-im-api/src/main/java/com/ruoyi/im/websocket/ImWebSocketEndpoint.java` | WebSocket 端点 |
| `.agent/rules/im.md` | 详细开发规则（务必阅读） |
| `CLAUDE.md` | 完整项目文档 |

---

## 环境特定说明

### 开发环境
- 所有服务本地运行
- 数据库：MySQL 在 172.168.20.222
- Redis：172.168.20.222
- API：http://localhost:8080
- 管理：http://localhost:8081
- 前端：http://localhost:3000

### 测试环境
- 与开发环境相同（内网部署）
- 无需外部访问
- 可通过 `app.security.enabled=false` 禁用安全

### 生产环境
- 需要正确的 SSL 证书
- 必须启用安全
- 必须更改数据库凭据和 JWT 密钥
- 文件上传路径应配置为持久存储
- 生产环境推荐 Redis 集群

---

## 版本信息

- **Java**：1.8
- **Spring Boot**：2.7.18（API），2.5.15（Admin）
- **MyBatis-Plus**：3.5.2
- **Vue**：3.3.11
- **Vite**：5.0.7
- **Element Plus**：2.4.4
- **MySQL**：8.0.33
- **Node.js**：16+（推荐 18+）

---

## 其他资源

- **项目文档**：查看 `CLAUDE.md` 获取完整文档
- **开发规则**：查看 `.agent/rules/im.md` 获取详细编码标准
- **API 文档**：运行时访问 http://localhost:8080/swagger-ui.html
- **管理面板**：http://localhost:8081（admin/123456）

---

## 总结

这是一个结构良好的企业 IM 系统，具有严格的关注点分离：
- 三层架构（Controller → Service → Mapper）
- 三层数据隔离（Entity ↔ DTO ↔ VO）
- JDK 1.8 兼容性要求
- 现有文件中有全面的文档
- API 和管理系统可独立部署

在此代码库中工作时：
1. 在添加新代码前始终阅读现有代码
2. 严格遵循分层架构
3. 使用适当的数据隔离模式
4. 遵守 JDK 1.8 约束
5. 更改后彻底测试
6. 参阅 `CLAUDE.md` 和 `.agent/rules/im.md` 获取详细指导
