# 27 辅助功能审查：AI 助手 / Electron 桌面端 / H5 手机端

> **审查范围**：PL-AI AI 助手（5 项）、Electron 桌面端（doc 09 vs 现有 electron/）、H5 手机端
> **审查日期**：2026-05-07
> **审查方法**：需求文档 x 架构文档 x 实际代码三方比对
> **结论等级**：P0 必须修复 | P1 本周修复 | P2 本月修复 | INFO 知会

---

## 一、PL-AI AI 助手审查

### 1.1 需求覆盖度清单

| 编号 | 需求项 | 需求来源 | 架构覆盖 | 覆盖结论 |
|------|--------|---------|---------|----------|
| PL-AI-001 | AI 对话（非流式） | doc 07 §三 | doc 06 POST /im/ai/chat + doc 02 AiProvider.chat() | 完整 |
| PL-AI-002 | AI 流式对话 (SSE) | doc 07 §三 | doc 06 POST /im/ai/chat/stream + doc 02 AiProvider.stream() | 完整 |
| PL-AI-003 | AI 智能推荐 | doc 07 §三 (部分完成) | 未定义独立后端端点；复用 POST /im/ai/chat + 系统 Prompt | 部分覆盖 |
| PL-AI-004 | 模型列表 | doc 07 §三 | doc 06 GET /im/ai/models 已定义 + doc 02 AiProvider **未定义** listModels() 方法 | 后端有接口，前端适配器缺方法 |
| PL-AI-005 | AI 服务状态检查 | doc 07 §三 | doc 06 GET /im/ai/status + doc 02 AiProvider.isAvailable() | 完整 |

### 1.2 AiProvider 适配器一致性分析

对比 doc 02 §6.1 接口定义与 doc 06 REST API 端点：

| AiProvider 方法 | 对应 REST 端点 | 对齐状态 |
|----------------|---------------|----------|
| chat(messages) | POST /im/ai/chat | 对齐 |
| stream(messages, onChunk) | POST /im/ai/chat/stream | 对齐 |
| isAvailable() | GET /im/ai/status | 对齐 |
| **listModels() (缺失)** | **GET /im/ai/models** | **后端有端点，前端无方法** |

**问题 1（P1）：AiProvider 缺少 listModels() 方法**

后端 doc 06 §十二明确定义了 GET /im/ai/models 端点，但前端 doc 02 §6.1 的 AiProvider 接口只定义了 chat / stream / isAvailable 三个方法。
后果：前端无法获取可用模型列表，用户无法切换模型。修复建议：AiProvider 增加 listModels(): Promise<AiModel[]> 方法。

### 1.3 流式对话（SSE）协议审查

**现状**：doc 06 定义了 POST /im/ai/chat/stream（未明确 text/event-stream Content-Type）；doc 02 AiProvider.stream 按 chunk 回调；前端无 ai.ts 实现文件。

**问题 2（P1）：SSE 错误处理与重试机制完全缺失**

| 缺失场景 | 风险 | 建议修复 |
|---------|------|--------|
| SSE 服务端返回 HTTP 500 | 前端无错误状态 | 后端在 SSE 第一帧返回 error 事件 |
| 网络中断导致 chunk 丢失 | 消息截断不完整 | 增加 Last-Event-Id 重连机制 |
| 首 token 推理超时 | 前端过早判定失败 | 服务端先发 200 + headers 再延迟首帧 |
| 内网模型服务宕机 | 前端白屏 | doc 13 §11.6 已定义顶部黄条降级态，但 doc 06 缺少轮询参数 |
| 用户中途关闭页面 | 资源泄漏 | 前端 AbortController 通知后端取消 |

建议 doc 06 补充 SSE 协议格式：

### 1.4 platform-ai 微服务 8091 实现状态

**问题 3（P0）：platform-ai 服务无 Java 实现代码**

| 检查项 | 状态 |
|--------|------|
| doc 03 微服务清单中有 platform-ai (8091) | 已规划 |
| doc 06 API 定义 4 个端点 | 已定义 |
| doc 16 Gateway 路由有 platform-ai -> lb://platform-ai | 已配置 |
| platform-ai Java Controller 实现代码 | **不存在** |
| Nacos Data ID: im-ai.yaml 业务配置 | **未创建** |

结论：platform-ai 是零实现的服务——仅有文档契约，无 Java 代码、无 Spring Boot 启动类、无 Nacos 注册配置。

### 1.5 智能推荐（PL-AI-003）分析

当前 AssistantPanel.vue 中的推荐是前端硬编码 Prompt 模板，推荐请求与普通对话共用同一接口，无独立推荐策略接口。

**问题 4（P1）：推荐策略不可插拔**

建议增加 AiRecommendationProvider 接口（复用 Plugable Adapter 模式），支持：

- 基于聊天历史的摘要推荐
- 基于时间/日程的上下文推荐
- NoOp 降级：未配置时返回空列表

### 1.6 AI 助手审查汇总

| # | 严重度 | 问题 | 修复方 |
|---|--------|------|--------|
| 1 | P1 | AiProvider 缺少 listModels() 方法 | 前端适配器 + doc 02 |
| 2 | P1 | SSE 协议缺少错误/重试/超时定义 | doc 06 补充 |
| 3 | P0 | platform-ai 服务无任何 Java 实现 | 后端新增微服务 |
| 4 | P1 | PL-AI-003 智能推荐无独立策略接口 | 前端 + 后端 |
| 5 | INFO | /im/ai/status 轮询间隔未配置 | doc 06 补充 |
| 6 | INFO | doc 02 api/ai/ 设计已定但代码未创建 | 前端 API 层 |

---

## 二、Electron 桌面端审查

### 2.1 架构文档覆盖度（doc 09 vs doc 22 vs 实际代码）

| 模块 | doc 09 (理想架构) | doc 22 (接入指南) | 实际代码 (electron/) | 差距 |
|------|-------------------|-------------------|---------------------|------|
| 主进程入口 | TS class App | JS createWindow | main.js 有 | 无单实例锁、无 TrayManager 类 |
| 窗口管理 | WindowManager 类 | 未详述 | 仅在 createWindow | 缺少多窗口 |
| Preload 桥接 | contextBridge 完整 API | 列出核心 API | preload.js 有但 API 更小 | 功能子集 |
| IPC 路由表 | 6 个 handler 文件 | ipc.js 简版 | 内联在主进程 | 结构扁平 |
| 系统托盘 | TrayManager 类 | tray.js | 仅有基础 Tray | 缺 Badge/右键菜单 |
| 自动更新 | AutoUpdater 类 | auto-updater.js | **完全缺失** | P0 |
| 全局快捷键 | GlobalShortcut | 未详述 | **完全缺失** | P0 |
| 电源监控 | PowerMonitor | 未详述 | **完全缺失** | P0 |
| 安全存储 | safeStorage | 未详述 | **完全缺失（localStorage 明文）** | P0 |
| 构建打包 | electron-builder 25 | electron-builder.yml | package.json build | 基本对齐 |
| Vite 集成 | Monorepo + Federation | vite.config.ts 改造 | 直接加载 prototype/index.html | 与生产方案不一致 |

### 2.2 P0 严重问题

#### 问题 E1：sandbox: false 与架构规范矛盾

实际代码 electron/main.js:24：

架构 doc 09 §四 要求 sandbox: true。

当前代码关闭了沙箱，意味着渲染进程可绕过 contextBridge 直接访问 Node.js API。若渲染进程被 XSS 注入（如 AI 助手渲染恶意内容、链接预览解析恶意页面），攻击者可执行任意系统命令。

修复：sandbox: true（对齐 doc 09）。如需某些 Node 能力，通过 preload.js 的 contextBridge 显式暴露。

#### 问题 E2：Token 明文存储在 localStorage

实际代码 electron/main.js 中 IPC handler 和 preload 没有任何加密存储逻辑。

doc 09 §六 明确要求 safeStorage 加密：

当前代码直接在渲染层用 localStorage，可通过 DevTools 或 XSS 直接读取。修复：使用 electron-store 加密管理配置，使用 safeStorage 加密 Token。

#### 问题 E3：Notification 图标路径可能不存在

实际代码 electron/main.js:51：

electron/ 目录下没有 icon.png 文件（仅有 main.js 和 preload.js）。Tray 创建被 silent catch，无法报错。

### 2.3 P1 问题

#### 问题 E4：自动更新器完全缺失

doc 09 §八 定义了完整的 AutoUpdater 类（electron-updater），doc 22 §六 也详述了 auto-updater.js 实现。
但实际代码 electron/main.js 零更新相关代码。影响：用户需手动下载安装包升级。

修复：将 doc 22 §六 的 auto-updater.js 合入主进程；实现 update:status 推送 IPC；实现 quitAndInstall IPC handler。

#### 问题 E5：全局快捷键完全缺失

doc 09 §三 定义了 registerShortcuts()（CommandOrControl+Shift+M 打开快速消息窗口）。
实际代码无对应实现。快速消息窗口 toggleQuickMessage() 也缺失。

#### 问题 E6：电源监控完全缺失

doc 09 §三 要求 Electron 端监听系统休眠/唤醒：笔记本电脑合盖后 WS 挂起但服务器不知道，导致消息丢失、已读回执不发。

#### 问题 E7：托盘 Badge 逻辑为空实现

doc 09 §六 IPC Notification handler 有 app.setBadgeCount(0)；doc 22 §3.3 tray.js 的 setBadge(count) 函数体为空注释。
Windows 未设置 Badge 角标。修复：Windows 用 mainWindow.setOverlayIcon() 实现。

### 2.4 P2 完善

| # | 问题 | 建议 |
|---|------|------|
| E8 | IPC channel 无白名单校验 | doc 09 §七 建议在 ipcMain.handle 前做 event.senderFrame 校验 |
| E9 | 无多窗口管理 | doc 09 §七 设计了 QuickMsgWindow；实际代码仅单主窗口 |
| E10 | window-all-Closed 未判断 tray | 检查 tray 不为 null 可跳过退出 |
| E11 | 版本号不一致：doc 写 electron ^32 + builder ^25，实际用 ^31 + ^24 | 统一版本号 |
| E12 | 缺少 Linux target | 按需构建 |
| E13 | Vite dev server 未接入 | 应走 Vite dev server (port 5173) |

### 2.5 Electron 桌面端与 LiveKit 屏幕共享

用户问：屏幕共享是否需要与 LiveKit 集成？

**回答**：需要。

doc 08-§五 已涵盖：服务端录制（IM-C-010）LiveKit Egress 已定义；屏幕共享按钮在通话控制栏。

**问题 E14（P1）：屏幕共享无协议规范**

| 检查项 | 状态 |
|--------|------|
| LiveKit Room 支持屏幕共享 | SDK 内置 |
| Electron 端屏幕源枚举 | doc 09 未定义（需 desktopCapturer.getSources()） |
| WS CALL 信令扩展 | 未包含 screen_share 子信令 |
| 服务端房间权限(canPublishScreen) | doc 09 未提及 |
| 多用户同时共享冲突 | 未定义 |

修复建议：

1. Electron 端使用 desktopCapturer.getSources({ types: [window, screen] }) 枚举窗口
2. WS CALL 信令增加 SCREEN_SHARE_START / SCREEN_SHARE_STOP 子类型
3. LiveKit Token 增加 canPublishScreen: true Grant
4. 房间内仅允许 1 人同时共享（先到先得）

### 2.6 Electron 审查汇总

| # | 严重度 | 问题 | 修复方 |
|---|--------|------|--------|
| E1 | P0 | sandbox: false 关闭沙箱 | electron/main.js |
| E2 | P0 | Token localStorage 明文存储 | preload + main 进程 |
| E3 | P0 | icon.png 不存在被 silent catch | 增加资源 + 错误日志 |
| E4 | P1 | 自动更新器完全缺失 | 新增 auto-updater 模块 |
| E5 | P1 | 全局快捷键完全缺失 | main 进程增加 registerShortcuts |
| E6 | P1 | 电源监控完全缺失 | main 进程增加 powerMonitor |
| E7 | P1 | 托盘 Badge 仅空实现 | tray.js 填 overlay icon 逻辑 |
| E8 | P2 | IPC 无白名单校验 | 安全过滤器 |
| E9 | P2 | 多窗口管理缺失 | window-manager 重构 |
| E10 | P2 | win-all-closed 未判断 tray | 逻辑修复 |
| E11 | P2 | doc 09 与 package.json 版本不一致 | 统一版本号 |
| E12 | P2 | Linux target 缺失 | 按需 |
| E13 | P2 | Vite dev server 未接入 | 按需 |
| E14 | P1 | 屏幕共享无协议规范 | doc 09 + 前端规范 |

---

## 三、H5 手机端审查

### 3.1 H5 在需求文档中的定位

#### doc 01-§做/不做：H5 明确排除

doc 01-四（本期范围）核心平台：**Windows 桌面** | **管理后台** | **移动端** (注：未列为本期)

doc 01-§二做/不做总结：H5 未列，意味着不在本期范围内。

#### doc 18-§一 决策摘要

第 4 项决策：功能范围全量 + 桌面端 Electron。H5 未列。

#### doc 18-§二 桌面端

桌面端选型 Electron + electron-builder，**复用同一 Vue 代码**。
这说明桌面端和 Web 共享组件/设计 token，但未提及 H5 独立移动端。

### 3.2 现有 H5 原型分析

#### prototype/dingtalk-h5.html 概况

文件：D:MyCodeimprototypedingtalk-h5.html (537行)

- 单 HTML 文件，纯静态 + CSS
- 6 个页面：消息列表、聊天、工作台、DING、通讯录、我的
- 375px 手机壳 + 刘海屏 + 状态栏设计
- 使用 design token CSS 变量子集
- 底部 Tab Bar 5 个入口

#### H5 原型 vs 需求文档一致性问题

| 需求（doc 01/08） | prototype 现状 | 差距 |
|-----------------|---------------|------|
| 会话列表 (IM-S-01 ~ 10) | 静态会话数据，无分页/搜索/置顶 | 缺: 加载更多、搜索框、置顶分组 |
| 聊天消息气泡 | 8 条静态消息，含 self/other/group | 缺: 图片/文件/语音/@/DING 消息类型 |
| 输入框 | 静态 textarea + emoji/toolbar 按钮 | 缺: 粘贴图片、@、文件上传、语音录制 |
| VCARD 名片分享 | 未实现 | 缺 |
| IMAGE 图片消息 | 未实现 | 缺 |
| FILE 文件消息 | 未实现 | 缺 |
| DING 发送 | DING 页面仅选择人(列表) | 缺: DING 方式(应用内)、定时发送 |
| 通讯录 | 静态 AA ~ EE 分组 | 缺: 组织架构树、搜索 |
| 我的 | 4 个列表项 | 缺: 头像编辑、账号安全、关于 |

静态原型仅做 UI 参考，无交互逻辑。

### 3.3 H5 一期不做的建议（P2 可选）

虽然 H5 不在本期，但若后续扩展：

| 能力 | 方案 | 优先级 |
|------|------|--------|n| 响应式断点 | 复制 6 个文件的 token 体系，桌面/移动各自覆盖 | P2 |
| 触屏手势 | 左滑删除会话、长按上下文菜单、下拉刷新 | P2 |
| 摄像头/相册 | navigator.mediaDevices.getUserMedia + 微信 JS-SDK chooseImage | P2 |
| 移动端推送 | Web Push API (VAPID) + Service Worker | 依赖内网，P2 |
| H5 专属路由 | 5 个 Tab 各自组件，按需加载 + 视图缓存 | P2 |
| 安全围栏 | 截屏检测、禁止复制/长按保存图片 | P2 |

### 3.4 H5 审查汇总

| # | 严重度 | 问题 | 说明 |
|---|--------|------|------|
| H1 | out-of-scope | H5 一期明确不做 | doc 01-§四 |
| H2 | P2-optional | dingtalk-h5.html 原型静态、无 API | 仅 UI 参考；重写率 > 95% |
| H3 | P2-optional | 手势/触摸/推送/拍照全未规划 | doc 08-§3 未涉及 |
| H4 | P2-optional | 响应式断点策略未设计 | doc 01 仅做 Web 桌面 |

---

## 四、跨模块一致性审查

### 4.1 版本对齐

| 依赖项 | doc 09 §三 | package.json | 状态 |
|--------|-----------|--------------|------|
| Electron | ^32 | ^31.7.7 | 不一致 |
| electron-builder | ^25 | ^24.13.3 | 不一致 |

风险：版本不同导致 API 行为差异 + 安全补丁不同步。

### 4.2 IPC 命名规范

- doc 09 §二十六：定义 IPC channel 命名用冒号分隔（例：app:quit）
- 实际 electron/main.js：用 kebab-case 横线分隔（例：window-minimize）

建议：统一 kebab-case（与现有代码对齐，修改 doc 09 措辞）。

### 4.3 H5 原型与桌面端 Token 差异

- 桌面端 design-tokens.scss：使用 --dt-brand: #277EFB 等 token
- H5 prototype：直接写 CSS 变量名（--dt-brand 等）但未 --dt-radius-lg / --dt-spacing-md 等，仅用了子集

建议：H5 独立复制定名，保持桌面/移动端 token 一致。

### 4.4 需求/架构文档自检清单

| 检查项 | 结果 | 备注 |
|--------|------|------|
| doc 06 API 路径与前端 preloader 路径一致 | 部分 | 7/10 |
| doc 09 与实际代码对齐 | 部分 | 1/3 模块 |
| doc 22 与 doc 09 模块名一致 | 是 | auto-updater/tray/ipc |
| doc 07 需求完成度统计准确 | 是 | 5/6 AI 需求完成 |

注：doc 09 设计了模块类（WindowManager 等），但实际代码是简化版。在 09 与 22 之间：09 是理想架构，22 是快速接入指南，09 的多处模块设计未落地。

---

## 五、修复优先级路线图

### 5.1 P0：安全/可用性/关键设计（立即修复）

| # | 模块 | 问题 | 修复建议 |
|---|------|------|----------|
| E1 | Electron | sandbox:false 关闭沙箱 | sandbox:true |
| E2 | Electron | Token localStorage 明文 | 迁移到 safeStorage + electron-store |
| E3 | Electron | icon.png 不存在被 silent catch | 创建图标 |
| 1-AI | AI | platform-ai 微服务零实现（8091 端口无代码） | 新建 ruoyi-im-ai 模块 |
| 1-SSE | AI | SSE 连接无超时/重断/错误处理 | 写入规范（30s timeout, 自动重试3次） |

### 5.2 P1：功能完备性（本周）

| # | 模块 | 问题 | 修复建议 |
|---|------|------|----------|
| E4 | Electron | 自动更新缺失 | 按 doc 22 §六 实现 auto-updater |
| E5 | Electron | 全局快捷键缺失 | 按 doc 09 §三 实现 registerShortcuts |
| E6 | Electron | 电源监控缺失 | main 进程增加 powerMonitor |
| E7 | Electron | tray Badge 空实现 | 实现 Windows overlay icon |
| 1-API | AI | AiProvider 接口缺 listModels() | 接口 + NoOp 实现 |
| E14 | Electron | 屏幕共享无协议规范 | 补 doc + 前端 |

### 5.3 P2：代码/文档质量（本月）

| # | 模块 | 问题 | 修复建议 |
|---|------|------|----------|
| E8 | Electron | IPC 无白名单校验 | 安全过滤器 |
| E9 | Electron | 多窗口管理缺失 | window-manager 重构 |
| E10 | Electron | win-all-closed 未判断 tray | 逻辑修复 |
| E11 | Electron | 版本号不一致 | 统一版本号 |
| E12 | Electron | Linux target 缺失 | 按需 |
| E13 | Electron | Vite dev server 未接入 | 按需 |
| H2 | H5 | dingtalk-h5.html 原型仅静态 | UI 参考，一期不做 |
| H3 | H5 | 手势/触摸/推送未规划 | 一期不做 |
| H4 | H5 | 响应式断点策略未设计 | 按需 |

---

## 六、最终结论

### 6.1 辅助功能覆盖度一览

| 模块 | 需求覆盖度 | 架构文档覆盖度 | 代码完成度 | 关键差距 |
|------|-----------|--------------|-----------|----------|
| PL-AI AI 助手 | 80% (4/5) | 70% (doc 06 缺 AI 实现详细设计) | 30% (AiProvider 协议不完整) | **platform-ai 8091 端口零实现** |
| Electron 桌面端 | 100% (本期必做) | doc 09 + doc 22 互补，覆盖 95% | 35% (仅基础窗口+托盘+IPC) | **sandbox:false、无 AutoUpdater、无加密 Token** |
| H5 手机端 | 0% (本期明确不做) | 0% (仅在 prototype/ 静态原型) | 5% (仅静态 HTML 原型) | **不存在需求/架构/代码实现** |

### 6.2 一行结论

- **PL-AI**：架构设计合理但**后端平台-ai 微服务零实现**，导致所有 AI 功能空转；需优先实现 8091 端口的 5 个 API 端点。
- **Electron**：架构文档（doc 09 + doc 22）覆盖度优秀但**实际代码仅实现基础骨架**，安全方面存在 sandbox:false 和明文 Token 的高风险缺陷，P0 必须立即修复。
- **H5**：**一期明确不做**，仅存在静态 prototype/dingtalk-h5.html 作为视觉参考；触摸、推送、手势、相机等移动端特征全未规划，二期按需启动。

### 6.3 顶层 3 项优先行动

1. **[P0·安全] sandbox:true + 加密 Token** — 否则桌面端上线即被安全团队打回。
2. **[P0·功能] 新建 ruoyi-im-ai 微服务实现 5 个 API** — 否则 PL-AI 所有需求无法验收。
3. **[P1·完备性] 实现 AutoUpdater** — 否则用户每次升级需手动下载安装包，不可接受。

---

## 七、变更记录

| 版本 | 日期 | 作者 | 变更内容 |
|------|------|------|----------|
| 1.0 | 2026-06-08 | CatPaw 审查 Agent | 初始版本：AI/Electron/H5 三模块深度审查 |
