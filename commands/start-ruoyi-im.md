---
name: start-ruoyi-im
description: 快速启动 RuoYi-IM 开发环境（后端 + 前端）
---

# RuoYi-IM 快速启动命令

启动完整的 RuoYi-IM 开发环境，包括后端 API 服务和前端 Web 应用。

## 执行步骤

1. **检查环境依赖**
   - 验证 Java 1.8+ 环境
   - 验证 MySQL 连接状态
   - 验证 Redis 连接状态
   - 验证 Node.js 16+ 环境

2. **启动后端服务**
   ```bash
   cd ruoyi-im-api
   mvn clean package -Dmaven.test.skip=true
   mv target/im-api.jar ./im-api.jar
   java -jar im-api.jar --spring.profiles.active=dev
   ```

3. **启动前端服务**
   ```bash
   cd ruoyi-im-web
   npm install
   npm run dev
   ```

4. **验证服务状态**
   - 后端 API: http://localhost:8888
   - 前端应用: http://localhost:5173
   - WebSocket: ws://localhost:8888/im/websocket

## 注意事项

- 确保数据库已初始化（执行 im.sql）
- 检查 application.yml 中的数据库和 Redis 配置
- 首次启动需要等待依赖下载和编译完成

## 常见问题

**端口冲突**: 修改 application.yml 中的端口配置
**数据库连接失败**: 检查 MySQL 服务状态和连接参数
**前端编译失败**: 删除 node_modules 重新安装依赖