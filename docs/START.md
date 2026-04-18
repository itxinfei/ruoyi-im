# START - 5分钟快速上手

> **目标**：让新开发者在 5 分钟内跑通项目
> **适用**：首次克隆项目 / 久未更新想快速回顾 / 想快速验证环境

---

## 如果你是 AI / 大模型执行者

不要直接开始写代码。先读：

1. [05-AI执行文档使用协议](05-AI执行文档使用协议.md)
2. [01-AI全栈开发综合指南](01-AI全栈开发综合指南.md)
3. [02-大模型研发规范](02-大模型研发规范.md)
4. [30-技术架构与约束](30-技术架构与约束.md)

如果任务涉及 PC 版钉钉 UI，再继续读：

1. [27-PC版钉钉UI设计蓝图](27-PC版钉钉UI设计蓝图.md)
2. [20-UI交互规范总纲](20-UI交互规范总纲.md)
3. [25-钉钉Windows复刻基线](25-钉钉Windows复刻基线.md)

---

## 环境检查清单

在你开始之前，确保以下工具已安装：

| 工具 | 最低版本 | 检查命令 |
|------|---------|---------|
| Node.js | 18+ | `node -v` |
| npm | 9+ | `npm -v` |
| Java | 1.8+ | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| MySQL | 5.7+ | `mysql --version` |
| Redis | 6.x+ | `redis-cli ping` |

> 如果某项缺失 → 跳到「常见问题」找安装指引

---

## 第一步：克隆项目

```bash
git clone <项目地址>
cd RuoYi-IM
```

---

## 第二步：初始化数据库

### 2.1 创建 MySQL 数据库

```sql
CREATE DATABASE IF NOT EXISTS im DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2.2 导入数据库脚本

```bash
# 脚本位置：ruoyi-im-api/sql/im_db.sql
mysql -u root -p im < ruoyi-im-api/sql/im_db.sql
```

**验证成功**：
```bash
mysql -u root -p im -e "SHOW TABLES;" | wc -l
# 输出应该 > 50（表示表已创建）
```

---

## 第三步：配置后端

### 3.1 修改数据库连接

编辑 `ruoyi-im-api/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/im?useUnicode=true&characterEncoding=UTF-8
    username: your_username    # 改这里
    password: your_password    # 改这里
  redis:
    host: localhost
    port: 6379
    password: your_redis_password  # 如果有的话
```

### 3.2 启动后端

```bash
cd ruoyi-im-api
mvn spring-boot:run
```

**验证成功**：
```bash
curl http://localhost:8080/api/user/info/1
# 应返回用户 JSON 数据
```

---

## 第四步：配置前端

### 4.1 安装依赖

```bash
cd ruoyi-im-web
npm install
```

### 4.2 配置 API 地址

编辑 `ruoyi-im-web/.env.development`：

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=ws://localhost:8080
```

### 4.3 启动前端

```bash
npm run dev
```

**验证成功**：
- 浏览器自动打开 `http://localhost:5173`
- 看到登录页面

---

## 第五步：登录验证

### 默认测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 测试用户 | user1 | 123456 |

### 验证完整链路

1. 使用 admin / 123456 登录
2. 左侧出现「消息」导航
3. 点击「消息」→「新建会话」
4. 搜索并添加 user1
5. 发送一条消息

**如果你完成了以上步骤 → 🎉 项目跑通了！**

---

## 常见问题

### Q1: `npm install` 报错 network

```bash
# 设置国内镜像
npm config set registry https://registry.npmmirror.com
npm install
```

### Q2: `mvn spring-boot:run` 报错 port already used

```bash
# 杀掉占用 8080 端口的进程
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :8080
kill -9 <PID>
```

### Q3: Redis 连接失败

确认 Redis 已启动：
```bash
redis-cli ping
# 返回 PONG 表示正常
```

### Q4: 前端一直 loading

1. 检查后端是否正常运行（curl 测试）
2. 检查 `.env.development` 配置是否正确
3. 浏览器按 F12 打开控制台，查看网络请求错误

---

## 下一步

项目跑通后，按你的角色选择阅读路径：

| 角色 | 推荐阅读 |
|------|---------|
| AI 执行者 | 05 → 01 → 02 → 30 → 13 |
| 前端开发 | 01 → 21 → 35(前端全局样式) → 33(API) |
| 后端开发 | 01 → 32 → 33 → 34(WebSocket) |
| AI大模型开发 | [60-AI大模型开发总览](60-AI大模型开发总览.md) → [68-端到端实战教程](68-端到端实战教程.md) |
| 产品/设计 | 10 → 15 → 27 → 20 → 25 → 21/26 |

完整文档索引 → [00-文档索引总纲](00-文档索引总纲.md)

---

## AI大模型开发快速入门

如果你需要开发或使用AI大模型功能，请参考以下快速路径：

### 环境准备

```bash
# 创建conda环境
conda create -n llm-training python=3.10 -y
conda activate llm-training

# 安装PyTorch
pip install torch==2.2.0 --index-url https://download.pytorch.org/whl/cu118

# 安装AI开发依赖
pip install transformers peft accelerate bitsandbytes datasets trl
```

### 快速验证

```python
# 验证环境
python -c "
import torch
print(f'PyTorch: {torch.__version__}')
print(f'CUDA: {torch.cuda.is_available()}')
"

# 运行端到端教程
# 详见 docs/68-端到端实战教程.md
```

---

*最后更新：2026-04-18*
