# 快速启动集成指南

## 5 分钟快速启动

### 前置条件

- Node.js 18+
- Java 8+
- MySQL 8.0+
- Redis 6.0+

### 步骤 1: 启动后端 (2 分钟)

```bash
# 进入后端目录
cd ruoyi-im-api

# 编译项目
mvn clean package -DskipTests

# 运行应用
java -jar target/ruoyi-im-api-1.0.0.jar
```

**验证**: 访问 `http://localhost:8080/swagger-ui.html` 看到 Swagger 文档

### 步骤 2: 启动前端 (2 分钟)

```bash
# 进入前端目录
cd ruoyi-im-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

**验证**: 浏览器自动打开 `http://localhost:3000`

### 步骤 3: 测试集成 (1 分钟)

1. 打开浏览器开发者工具 (F12)
2. 进入 Console 标签
3. 查看是否有 WebSocket 连接成功的日志
4. 尝试发送消息

## 关键文件位置

| 功能 | 文件 | 说明 |
|------|------|------|
| WebSocket 管理 | `src/utils/socket/nativeSocket.js` | 原生 WebSocket 连接管理 |
| Socket Composable | `src/utils/socket/useImSocket.js` | Vue 组合式函数 |
| 主布局 | `src/views/im/ImChatLayoutOptimized.vue` | 优化后的主布局 |
| 聊天容器 | `src/views/im/chat/ChatContainerOptimized.vue` | 优化后的聊天容器 |
| 路由配置 | `src/router/modules/im.js` | IM 模块路由 |
| 主题样式 | `src/styles/dingtalk-theme.scss` | DingTalk 风格主题 |

## 常见问题快速解决

### Q: WebSocket 连接失败

**A**: 
```bash
# 1. 检查后端是否运行
curl http://localhost:8080/swagger-ui.html

# 2. 检查防火墙
# Windows: netstat -ano | findstr :8080
# Mac/Linux: lsof -i :8080

# 3. 查看浏览器控制台错误
# F12 -> Console -> 查看红色错误信息
```

### Q: 前端样式错误

**A**:
```bash
# 清除缓存并重新安装
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### Q: 数据库连接失败

**A**:
```bash
# 检查 MySQL 是否运行
mysql -h 172.168.20.222 -u im -p

# 检查 Redis 是否运行
redis-cli -h 172.168.20.222 ping
```

## 测试 WebSocket 连接

### 使用浏览器控制台测试

```javascript
// 1. 获取 Socket 实例
import { getNativeSocket } from '@/utils/socket/nativeSocket'
const socket = getNativeSocket({ debug: true })

// 2. 连接
await socket.connect()

// 3. 发送消息
socket.send('message', {
  sessionId: '123',
  type: 'text',
  content: 'Hello'
})

// 4. 监听消息
socket.on('message', (data) => {
  console.log('Received:', data)
})

// 5. 查看状态
console.log(socket.getStatus())
```

## 生产构建

```bash
# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 输出目录: dist/
```

## 下一步

1. ✅ 集成完成
2. 📝 查看 `INTEGRATION_GUIDE.md` 了解详细配置
3. 🧪 运行测试: `npm run test`
4. 📊 查看性能: `npm run build` 后查看 `dist/stats.html`
5. 🚀 部署到生产环境

---

**需要帮助?** 查看 `INTEGRATION_GUIDE.md` 或提交 Issue
