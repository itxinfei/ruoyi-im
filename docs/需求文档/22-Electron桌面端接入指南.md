# 22 Electron 桌面端接入指南

> **定位**：现有 Web 工程如何零改造套上 Electron 壳。
> **前置**：技术锁定清单见 [21-前后端组件与技术锁定清单](21-前后端组件与技术锁定清单.md) §1.19
> **目标**：桌面端具备 tray / 全局快捷键 / 原生通知 / 离线存储 / 自动升级，且**不改动任何现有业务代码**。

---

## 一、工程结构（改造后）

```text
ruoyi-im-web/                    ← Web 工程（业务代码零改动）
├── src/
│   ├── main.js
│   ├── App.vue
│   └── ...
├── package.json                ← Web 依赖（不动）
├── vite.config.ts              ← 改 base: './' + outDir 指向 electron 产出
└── index.html

electron/                         ← 新增：桌面端主进程（与 ruoyi-im-web 同级，位于项目根目录）
├── main.js                     ← Electron 入口
├── preload.js                  ← 预加载脚本（安全暴露 API 给渲染层）
├── tray.js                     ← 系统托盘
├── notification.js             ← 原生通知封装
├── auto-updater.js             ← 自动升级（electron-updater）
└── ipc.js                      ← IPC 路由表

electron-builder.yml             ← 打包配置（项目根目录）
package.json                     ← 桌面端专用 package.json（含 electron 依赖，项目根目录）
```

> **原则**：业务代码零侵入。所有桌面能力只在 `electron/` 目录下，通过 `contextBridge` 暴露给渲染层。

---

## 二、依赖安装

```bash
# 进入 electron 目录（位于项目根目录，与 ruoyi-im-web 同级）
cd ../electron                   ← 从 ruoyi-im-web 出发回退到根目录再进 electron

# 或在根目录直接操作（推荐）
# 确保根目录有 package.json，如无则先 npm init -y
npm install --save-dev electron@^32 electron-builder@^25
npm install --save electron-updater@^6
```

`package.json` 新增：

```json
{
  "name": "ruoyi-im",
  "version": "1.0.0",
  "main": "electron/main.js",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "electron:dev": "vite build --mode electron && electron .",
    "electron:build": "vite build --mode electron && electron-builder"
  }
}
```

---

## 三、各文件实现要点

### 3.1 `electron/main.js` — 主进程入口

```javascript
const { app, BrowserWindow, ipcMain, Menu } = require('electron')
const path = require('path')
const { setupTray } = require('./tray')
const { setupIpc } = require('./ipc')
const { setupAutoUpdater } = require('./auto-updater')

let mainWindow

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 960,
    minHeight: 600,
    frame: false,              // 无边框，自定义标题栏
    titleBarStyle: 'hidden',
    icon: path.join(__dirname, '../public/icon.png'),
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,  // 安全隔离
      nodeIntegration: false,  // 禁 Node.js 集成（安全）
      sandbox: true            // 沙箱
    }
  })

  // 开发环境加载 Vite dev server；生产加载构建产物
  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:5173')
    mainWindow.webContents.openDevTools()
  } else {
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  }

  // 托盘
  setupTray(mainWindow)

  // IPC 路由
  setupIpc(mainWindow)

  // 自动升级
  setupAutoUpdater(mainWindow)
}

app.whenReady().then(createWindow)

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow()
})
```

### 3.2 `electron/preload.js` — 安全暴露桌面 API

```javascript
const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制
  minimize: () => ipcRenderer.send('window:minimize'),
  maximize: () => ipcRenderer.send('window:maximize'),
  close: () => ipcRenderer.send('window:close'),
  isMaximized: () => ipcRenderer.invoke('window:isMaximized'),

  // 通知
  showNotification: (title, body) => ipcRenderer.send('notify:show', title, body),

  // 托盘 badge（未读消息数）
  setBadgeCount: (count) => ipcRenderer.send('tray:badge', count),

  // 离线存储（localStorage 之外）
  setLocalData: (key, value) => ipcRenderer.invoke('data:set', key, value),
  getLocalData: (key) => ipcRenderer.invoke('data:get', key),

  // 监听主进程事件
  onNotificationClick: (callback) => ipcRenderer.on('notify:click', callback),
  onNewMessage: (callback) => ipcRenderer.on('ws:new-message', callback)
})
```

### 3.3 `electron/tray.js` — 系统托盘

```javascript
const { Tray, Menu, nativeImage } = require('electron')
const path = require('path')

let tray = null

function setupTray(mainWindow) {
  const icon = nativeImage.createFromPath(
    path.join(__dirname, '../public/tray-icon.png')
  )
  tray = new Tray(icon)
  tray.setToolTip('心飞IM')

  const contextMenu = Menu.buildFromTemplate([
    { label: '打开 心飞IM', click: () => mainWindow.show() },
    { type: 'separator' },
    { label: '退出', click: () => { mainWindow.destroy(); app.quit() } }
  ])
  tray.setContextMenu(contextMenu)

  tray.on('click', () => mainWindow.show())
}

function setBadge(count) {
  // macOS: tray title badge; Windows: overlay icon
}
```

### 3.4 `electron/notification.js` — 原生通知

```javascript
const { Notification } = require('electron')

function showNotification(title, body, onClick) {
  const n = new Notification({ title, body, silent: false })
  n.on('click', onClick)
  n.show()
}

module.exports = { showNotification }
```

### 3.5 `electron/ipc.js` — IPC 路由表

```javascript
const { ipcMain, BrowserWindow } = require('electron')

function setupIpc(mainWindow) {
  ipcMain.on('window:minimize', () => mainWindow.minimize())
  ipcMain.on('window:maximize', () => {
    if (mainWindow.isMaximized()) mainWindow.unmaximize()
    else mainWindow.maximize()
  })
  ipcMain.on('window:close', () => mainWindow.hide())  // 隐藏不退出
  ipcMain.handle('window:isMaximized', () => mainWindow.isMaximized())
  ipcMain.on('tray:badge', (_e, count) => { /* 更新 badge */ })
  ipcMain.on('notify:show', (_e, title, body) => { /* 触发原生通知 */ })
}

module.exports = { setupIpc }
```

### 3.6 `electron-builder.yml` — 打包配置

```yaml
appId: com.ruoyi.im
productName: XinFei IM
directories:
  output: dist-electron
files:
  - dist/**/*
  - electron/**/*
win:
  target:
    - target: nsis
      arch: [x64]
  icon: public/icon.ico
nsis:
  oneClick: false
  allowToChangeInstallationDirectory: true
mac:
  target: dmg
  icon: public/icon.icns
  category: public.app-category.productivity
linux:
  target: AppImage
  icon: public/icon.png
  category: Network
publish:
  provider: generic
  url: http://your-internal-update-server/updates/
```

---

## 四、Vite 配置改造（唯一对现有代码的改动）

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import ElementPlus from 'unplugin-element-plus/vite'

export default defineConfig(({ mode }) => ({
  plugins: [vue(), ElementPlus()],
  base: mode === 'electron' ? './' : '/',  // 关键：Electron 用相对路径
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    // 产物不要 hash，方便 electron-builder 寻址
    rollupOptions: {
      output: {
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  }
}))
```

> **为什么 `base: './'`**：Electron 用 `file://` 协议加载，绝对路径会变成 `file:///assets/xxx.js`（找不到），必须改为相对路径 `./assets/xxx.js`。

---

## 五、自定义标题栏（frame: false）

```vue
<!-- src/components/layout/AppTitleBar.vue -->
<template>
  <div class="app-titlebar" @mousedown="onMouseDown">
    <span class="title">心飞IM</span>
    <div class="window-controls">
      <button @click="electronAPI.minize">—</button>
      <button @click="electronAPI.maximize">□</button>
      <button class="close" @click="electronAPI.close">×</button>
    </div>
  </div>
</template>

<script setup lang="ts">
const electronAPI = (window as any).electronAPI

// 双击标题栏最大化
const onMouseDown = (e: MouseEvent) => {
  if (e.detail === 2) electronAPI.maximize()
  // 拖拽：electron 侧需在 IPC 里处理 drag 区域
}
</script>

<style scoped>
.app-titlebar {
  height: 32px; background: var(--dt-bg-body);
  display: flex; align-items: center; justify-content: space:-between;
  -webkit-app-region: drag;
}
.window-controls { -webkit-app-region: no-drag; }
</style>
```

---

## 六、自动升级（electron-updater）

```javascript
// electron/auto-updater.js
const { autoUpdater } = require('electron-updater')
const { Notification } = require('electron')

function setupAutoUpdater(mainWindow) {
  autoUpdater.checkForUpdatesAndNotify()

  autoUpdater.on('update-available', () => {
    new Notification({ title: '心飞IM', body: '发现新版本，正在下载...' }).show()
  })

  autoUpdater.on('update-downloaded', () => {
    new Notification({
      title: '心飞IM',
      body: '新版本已下载，重启后生效'
    }).show()
  })
}

module.exports = { setupAutoUpdater }
```

> 升级服务器：内网 Generic HTTP Server 即可（YAML 里 `publish.url` 指向内网地址）。

---

## 七、消息推送链路（WS消息 → 桌面通知）

```text
WebSocket Server ──JSON帧──▶ Spring Boot ──WS推──▶ Electron 渲染进程
                                         │
                                         ▼
                                  window.electronAPI.showNotification()
                                         │
                                         ▼
                              Electron Notification API
                                         │
                                         ▼
                            OS 原生通知弹窗 + 托盘 badge +1
```

关键在渲染进程封装 `useDesktopNotification` 组合式函数：

```javascript
// src/composables/useDesktopNotification.js
import { ref } from 'vue'

export function useDesktopNotification() {
  const unreadCount = ref(0)

  const showNotification = (title, body) => {
    if (window.electronAPI) {
      window.electronAPI.showNotification(title, body)
      unreadCount.value++
      window.electronAPI.setBadgeCount(unreadCount.value)
    } else {
      // 浏览器环境降级为 DOM Notification
      new Notification(title, { body })
    }
  }

  return { unreadCount, showNotification }
}
```

---

## 八、桌面端维度的研发红线

| 红线 | 说明 |
|------|------|
| `contextIsolation: true` | **强制**，禁 `nodeIntegration` |
| 禁止 `require` 在渲染进程用 | 渲染层只能通过 `window.electronAPI` 调用暴露的 API |
| 禁 `shell.openExternal` 直接打开外部 URL | 所有外网 URL 走链接卡片适配器（仅内网可达 URL） |
| 托盘 click 必须 `mainWindow.show()` | 不能无脑 `quit()`，Dock/macOS 行为要一致 |
| `window-all-closed` 在 macOS 不 quit | macOS 惯例是 Dock icon 常驻 |
| 通知权限必须检查 | `Notification.permission !== 'granted'` 时静默失败 |
| 离线存储禁 `localStorage` 存 JWT | JWT 存在 `electron-store`（加密 JSON），禁 localStorage（XSS 泄漏） |
| 自动升级包必须签名 | Windows 必须 EV 代码签名或内网自建 CA + 组策略信任 |
| 包体积监控 | 单包 > 150MB 必须告警；> 200MB 必须优化 |

---

## 九、构建与发布流水线

```bash
# 开发模式（热更新 + Electron 调试）
npm run electron:dev

# 生产构建（打包出安装包）
npm run electron:build

# 产出
dist-electron/
├── XinFei IM Setup 1.0.0.exe    ← Windows NSIS 安装包
├── XinFei IM-1.0.0.dmg          ← macOS
└── XinFei IM-1.0.0.AppImage     ← Linux

# 自动升级（Generic HTTP Server 托管）
# - latest.yml (electron-builder 自动生成)
# - *.exe / *.dmg / *.AppImage
```

---

## 十、与 Vue 现有代码的衔接

| 现有模块 | 是否需要改造 | 如何衔接 |
|---------|------------|---------|
| `vue-router` | ❌ 不改 | 路由逻辑不变，仅在 `main.js` 里挂载 `BrowserWindow` |
| `useImWebSocket` | ❌ 不改 | WS 连接在渲染层内部完成 |
| `main.js` | ⚠️ 加 1 行 | 加 `if (!window.electronAPI) { /* 浏览器环境 */ }` 的条件分支 |
| 用户设置停本地 | ⚠️ 改存储 | `useLocalStorage`（@vueuse/core）→ `electron-store`（仅在检测到时） |
| 登录流程 | ⚠️ 可改 | Electron 支持生物识别（macOS TouchID）等，一期不变 |

> **零代码改造** 是指业务组件、API、路由、Store 全部不变。只有主进程 + 预加载 + 构建配置是新增的。

---

## 十一、变更记录

| 日期 | 变更 |
|------|------|
| 2026-10-28 | 输出 Electron 接入指南：工程结构、预加载脚本、托盘、通知、自动升级、打包配置、与 Vue 衔接方式、桌面端研发红线 |

---

## 十二、代码签名与公证（Code Signing & Notarization）

### 12.1 Windows 代码签名

| 项 | 规范 |
|----|------|
| 证书类型 | EV Code Signing Certificate（扩展验证） |
| 颁发机构 | DigiCert / Sectigo / GlobalSign |
| 签名工具 | `signTool.exe` 或 electron-builder `certificateFile` 配置 |
| 时间戳 | 必须附加 RFC 3166 时间戳（防止签名过期后验证失败） |
| 签名位置 | electron-builder.yml → `win: certificateFile` + `certificatePassword` |

```yaml
# electron-builder.yml (Windows 签名配置)
win:
  certificateFile: certs/ev-cert.pfx
  certificatePassword: ${CERT_PASSWORD}
  signingHashAlgorithms: ['sha256']
  timeStampServer: http://timestamp.digicert.com
```

### 12.2 macOS 公证 (Notarization)

| 项 | 规范 |
|----|------|
| Apple Developer Account | Apple Developer Enterprise Program（内部发布）或 App Store |
| 公证工具 | `notarytool`（Xcode 13+）或 `altool` |
| electron-builder 配置 | `mac: notarize: true` + `APPLE_ID` + `APPLE_APP_SPECIFIC_PASSWORD` |
| 凭证存储 | keychain 或 CI/CD Secret 变量 |

```yaml
# electron-builder.yml (macOS 公证配置)
mac:
  notarize:
    teamId: XXXXXXXXXX
  entitlements: build/entitlements.mac.plist
  entitlementsInherit: build/entitlements.mac.plist
```

### 12.3 CI/CD 推荐流程

1. 代码提交触发 GitHub Actions / GitLab CI
2. 构建 `npm run build` 生成 dist/
3. `electron-builder publish always`（发布到内部更新服务器）
4. 下载构建产物，在隔离环境中执行签名/公证
5. 签名后产物上传到 `/updates/latest.yml` + 更新包
