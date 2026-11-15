# 09 Electron 桌面端架构设计

> **定位**：定义 Electron 桌面端的主进程/渲染进程架构、IPC 通信机制、原生能力集成、自动更新方案。
> **更新时间**：2026-10-30

---

## 一、整体架构

```
┌─────────────────────────────────────────────┐
│              Electron 32 + Vite 5           │
├──────────────────┬──────────────────────────┤
│   Main Process   │     Renderer Process     │
│                  │                          │
│  ┌────────────┐  │  ┌────────────────────┐  │
│  │ BrowserWindow│ │  │   Vue 3 + Vuex     │  │
│  │ TrayManager │  │  │   Element Plus     │  │
│  │ AutoUpdater │  │  │   Vue Router       │  │
│  │ IPC Handlers│  │  │   Vue Composables  │  │
│  │ GlobalShortcut│ │  └─────────┬──────────┘  │
│  │ PowerMonitor│  │            │             │
│  └──────┬───────┘  │     preload.js           │
│         │          │       (桥接)              │
│    ipcMain.on      │     ipcRenderer.invoke    │
└──────────────────┴──────────────────────────┘
```

---

## 二、项目结构

```
packages/
├── shell/                         ← Electron 壳 (主进程)
│   ├── src/
│   │   ├── main/
│   │   │   ├── index.ts           ← 主进程入口
│   │   │   ├── window-manager.ts  ← 窗口管理
│   │   │   ├── tray-manager.ts    ← 系统托盘
│   │   │   ├── auto-updater.ts    ← 自动更新
│   │   │   ├── global-shortcut.ts ← 全局快捷键
│   │   │   ├── ipc/
│   │   │   │   ├── index.ts       ← IPC 注册中心
│   │   │   │   ├── auth.ts        ← 认证操作
│   │   │   │   ├── window.ts      ← 窗口操作
│   │   │   │   ├── notification.ts← 通知操作
│   │   │   │   ├── clipboard.ts   ← 剪贴板操作
│   │   │   │   ├── shell.ts       ← 系统 Shell
│   │   │   │   └── store.ts       ← 磁盘持久化
│   │   └── preload/
│   │       └── index.ts           ← 渲染进程桥接
│   └── package.json
│
├── renderer/                      ← Vue 应用 (渲染进程)
│   ├── src/                       ← 与 ruoyi-im-web/ 共享组件
│   └── package.json
│
└── shared/                        ← 共享类型与工具
    ├── types/
    │   ├── ipc.d.ts              ← IPC 通道类型定义
    │   └── api.d.ts              ← 共享 API 类型
    └── package.json
```

---

## 三、主进程入口

```typescript
// shell/src/main/index.ts
import { app, BrowserWindow, Tray, Menu, ipcMain, nativeTheme, globalShortcut, powerMonitor } from 'electron'
import { WindowManager } from './window-manager'
import { TrayManager } from './tray-manager'
import { AutoUpdater } from './auto-updater'
import { registerIpcHandlers } from './ipc'

const isSingleInstance = app.requestSingleInstanceLock()
if (!isSingleInstance) { app.quit() }

class App {
  private windowManager: WindowManager
  private trayManager: TrayManager
  private autoUpdater: AutoUpdater

  constructor() {
    app.whenReady().then(() => {
      this.windowManager = new WindowManager()
      this.trayManager = new TrayManager(this.windowManager)
      this.autoUpdater = new AutoUpdater()
      registerIpcHandlers()
      this.registerShortcuts()
      this.monitorPower()
    })

    app.on('second-instance', () => this.windowManager.showMainWindow())
    app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit() })
    app.on('activate', () => this.windowManager.createMainWindow())
  }

  private registerShortcuts() {
    globalShortcut.register('CommandOrControl+Shift+M', () => {
      this.windowManager.toggleQuickMessage()
    })
  }

  private monitorPower() {
    powerMonitor.on('suspend', () => this.windowManager.notifySuspend())
    powerMonitor.on('resume', () => this.windowManager.notifyResume())
  }
}

new App()
```

---

## 四、窗口管理

```typescript
// shell/src/main/window-manager.ts
import { BrowserWindow, screen } from 'electron'
import path from 'path'

export class WindowManager {
  private mainWindow: BrowserWindow | null = null
  private quickMsgWindow: BrowserWindow | null = null

  createMainWindow(): BrowserWindow {
    const { width, height } = screen.getPrimaryDisplay().workAreaSize
    this.mainWindow = new BrowserWindow({
      width: Math.min(1200, width * 0.8),
      height: Math.min(800, height * 0.8),
      minWidth: 900,
      minHeight: 600,
      frame: false,  // 无边框 (自定义标题栏)
      titleBarStyle: 'hiddenInset',
      icon: path.join(__dirname, '../resources/icon.png'),
      webPreferences: {
        preload: path.join(__dirname, '../preload/index.js'),
        contextIsolation: true,
        nodeIntegration: false,
        sandbox: true,
        webSecurity: true
      }
    })

    // ⚠️ [P0 安全] 与实际代码差距警示 (27-辅助功能审查 §E1)
    // 当前 electron/main.js:24 实际值为 sandbox: false —— 渲染进程可绕过 contextBridge 直接访问 Node.js API
    // 风险: XSS 注入 (AI 助手渲染恶意内容 / 链接预览解析恶意页面) 可执行任意系统命令
    // 修复方向 (桌面端开发阶段): 改为 sandbox:true，所有需 Node 能力通过 preload.js 的 contextBridge 显式暴露
    // 本架构文档以 sandbox:true 为准，实际代码留待桌面端开发阶段对齐

    // 开发环境加载 Vite dev server, 生产环境加载打包产物
    if (process.env.VITE_DEV_SERVER_URL) {
      this.mainWindow.loadURL(process.env.VITE_DEV_SERVER_URL)
    } else {
      this.mainWindow.loadFile(path.join(__dirname, '../renderer/index.html'))
    }

    return this.mainWindow
  }

  toggleQuickMessage() {
    if (this.quickMsgWindow) {
      this.quickMsgWindow.destroy()
      this.quickMsgWindow = null
      return
    }
    this.quickMsgWindow = new BrowserWindow({
      width: 380, height: 520,
      frame: false,
      alwaysOnTop: true,
      resizable: false,
      show: false,
      webPreferences: { preload: path.join(__dirname, '../preload/index.js'), contextIsolation: true }
    })
    this.quickMsgWindow.on('blur', () => this.quickMsgWindow?.destroy())
    this.quickMsgWindow.show()
  }
}
```

---

## 五、Preload 桥接 (安全 IPC)

```typescript
// shell/src/preload/index.ts
import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制
  window: {
    minimize: () => ipcRenderer.invoke('window:minimize'),
    maximize: () => ipcRenderer.invoke('window:maximize'),
    close: () => ipcRenderer.invoke('window:close'),
    setTitle: (title: string) => ipcRenderer.send('window:setTitle', title)
  },

  // 通知
  notification: {
    show: (title: string, body: string, data?: unknown) =>
      ipcRenderer.invoke('notification:show', { title, body, data }),
    setBadge: (count: number) => ipcRenderer.invoke('notification:setBadge', count)
  },

  // 系统能力
  shell: {
    openExternal: (url: string) => ipcRenderer.invoke('shell:openExternal', url),
    openPath: (path: string) => ipcRenderer.invoke('shell:openPath', path),
    showItemInFolder: (path: string) => ipcRenderer.invoke('shell:showItemInFolder', path)
  },

  // 全局快捷键
  shortcut: {
    register: (accelerator: string, callbackName: string) =>
      ipcRenderer.invoke('shortcut:register', { accelerator, callbackName }),
    unregister: (accelerator: string) => ipcRenderer.invoke('shortcut:unregister', accelerator)
  },

  // 持久化存储 (本地状态)
  store: {
    get: (key: string) => ipcRenderer.invoke('store:get', key),
    set: (key: string, value: unknown) => ipcRenderer.invoke('store:set', { key, value }),
    delete: (key: string) => ipcRenderer.invoke('store:delete', key)
  },

  // 应用信息
  app: {
    getVersion: () => ipcRenderer.invoke('app:getVersion'),
    checkForUpdates: () => ipcRenderer.invoke('app:checkForUpdates'),
    quitAndInstall: () => ipcRenderer.invoke('app:quitAndInstall')
  },

  // 监听主进程推送
  onIpc: (channel: string, handler: Function) => {
    ipcRenderer.on(channel, (_, payload) => handler(payload))
  }
})
```

---

## 六、IPC 主进程处理器

```typescript
// shell/src/main/ipc/notification.ts
import { Notification, app, ipcMain, safeStorage } from 'electron'

export function registerNotificationIpc() {
  ipcMain.handle('notification:show', (_, { title, body, data }) => {
    if (Notification.isSupported()) {
      const n = new Notification({
        title,
        body,
        icon: app.getPath('userData') + '/resources/icon.png',
        silent: false
      })
      n.on('click', () => {
        // 触发 Vue 路由跳转事件
      })
      n.show()
    }
    // Windows 角标
    if (process.platform === 'win32') {
      if (app.getBadgeCount() > 0) app.setBadgeCount(0)
    }
  })

  ipcMain.handle('notification:setBadge', (_, count: number) => {
    app.setBadgeCount(count)
  })

  // 安全存储 Token
  ipcMain.handle('store:set', (_, { key, value }) => {
    const encrypted = safeStorage.encryptString(JSON.stringify(value))
    localStorage.setItem(key, encrypted.toString('base64'))
  })

  ipcMain.handle('store:get', (_, key: string) => {
    const data = localStorage.getItem(key)
    if (!data) return null
    return JSON.parse(safeStorage.decryptString(Buffer.from(data, 'base64')))
  })
}
```

---

## 七、系统托盘

```typescript
// shell/src/main/tray-manager.ts
import { Tray, Menu, nativeImage, BrowserWindow } from 'electron'
import path from 'path'

export class TrayManager {
  private tray: Tray | null = null

  constructor(private windowManager: WindowManager) {
    const icon = nativeImage.createFromPath(
      path.join(__dirname, '../resources/tray-icon.png')
    )
    this.tray = new Tray(icon.resize({ width: 16, height: 16 }))
    this.tray.setToolTip('RuoYi-IM')
    this.setContextMenu()

    this.tray.on('click', () => {
      const win = this.windowManager.getMainWindow()
      win?.isVisible() ? win.hide() : win?.show()
    })
  }

  setContextMenu() {
    const menu = Menu.buildFromTemplate([
      { label: '显示主窗口', click: () => this.windowManager.getMainWindow()?.show() },
      { type: 'separator' },
      { label: '退出', click: () => { app.isQuiting = true; app.quit() } }
    ])
    this.tray?.setContextMenu(menu)
  }
}
```

---

## 八、自动更新

```typescript
// shell/src/main/auto-updater.ts
import { autoUpdater } from 'electron-updater'
import { BrowserWindow } from 'electron'

export class AutoUpdater {
  constructor() {
    autoUpdater.autoDownload = true
    autoUpdater.autoInstallOnAppQuit = true

    autoUpdater.on('checking-for-update', () => this.sendStatus('checking'))
    autoUpdater.on('update-available', (info) => this.sendStatus('available', info))
    autoUpdater.on('update-not-available', () => this.sendStatus('none'))
    autoUpdater.on('download-progress', (p) => this.sendStatus('downloading', p))
    autoUpdater.on('update-downloaded', () => this.sendStatus('ready'))
    autoUpdater.on('error', (e) => this.sendStatus('error', e.message))
  }

  check() {
    autoUpdater.checkForUpdates()
  }

  quitAndInstall() {
    autoUpdater.quitAndInstall()
  }

  private sendStatus(status: string, payload?: unknown) {
    BrowserWindow.getAllWindows().forEach(win => {
      win.webContents.send('update:status', { status, payload })
    })
  }
}
```

---

## 九、构建与打包

### 9.1 package.json

```json
{
  "name": "ruoyi-im",
  "version": "1.0.0",
  "main": "out/main/index.js",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build && tsc -p shell",
    "electron:dev": "concurrently \"vite\" \"wait-on tcp:5173 && tsc -p shell && electron .\"",
    "electron:build": "npm run build && electron-builder"
  },
  "build": {
    "appId": "com.ruoyi.im",
    "productName": "若依IM",
    "directories": { "output": "dist-electron" },
    "files": ["out/**/*", "package.json"],
    "win": {
      "target": [{ "target": "nsis", "arch": ["x64"] }],
      "icon": "resources/icon.ico"
    },
    "mac": {
      "target": [{ "target": "dmg", "arch": ["x64", "arm64"] }],
      "icon": "resources/icon.icns"
    },
    "nsis": {
      "oneClick": false,
      "allowToChangeInstallationDirectory": true,
      "createDesktopShortcut": true
    },
    "publish": {
      "provider": "generic",
      "url": "https://update.ruoyi.com/releases/"
    }
  },
  "devDependencies": {
    "electron": "^32.0.0",
    "electron-builder": "^24.13.3",
    "concurrently": "^9.0.0",
    "wait-on": "^8.0.0"
  }
}
```

---

## 十、变更记录

| 日期 | 变更 |
|------|------|
| 2026-10-30 | 完成 Electron 桌面端架构定义:主进程/渲染进程分离、IPC 安全桥接、窗口管理、托盘、自动更新 |
| 2026-05-07 | §四 窗口管理代码新增 sandbox:true 与实际代码 (electron/main.js:24 sandbox:false) 的 P0 安全差距警示注释（对齐 27-审查 §E1） |
