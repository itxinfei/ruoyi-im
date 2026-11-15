const { app, BrowserWindow, Menu, ipcMain, dialog, Notification, Tray } = require('electron');
const path = require('path');

let mainWindow;
let tray = null;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 900,
    minHeight: 600,
    frame: false,
    titleBarStyle: 'hidden',
    titleBarOverlay: {
      color: '#ffffff',
      symbolColor: '#86909c',
      height: 56
    },
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false
    }
  });

  // 加载 prototype 主页面
  mainWindow.loadFile(path.join(__dirname, '..', 'prototype', 'index.html'));

  // 最小化到托盘
  mainWindow.on('close', (event) => {
    if (!app.isQuiting) {
      event.preventDefault();
      mainWindow.hide();
    }
  });

  // 开发模式打开 DevTools
  // mainWindow.webContents.openDevTools();
}

app.whenReady().then(() => {
  createWindow();

  // 创建托盘
  const iconPath = path.join(__dirname, 'icon.png');
  try { tray = new Tray(iconPath); } catch(e) { /* 无图标跳过 */ }
  if (tray) {
    tray.setToolTip('XinFei IM');
    tray.on('click', () => mainWindow?.show());
  }

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
    else mainWindow?.show();
  });

  // 菜单栏
  const template = [
    {
      label: '文件',
      submenu: [
        { label: '新建会话', accelerator: 'CmdOrCtrl+N', click: () => mainWindow?.webContents.send('menu-new-chat') },
        { type: 'separator' },
        { label: '退出', accelerator: 'CmdOrCtrl+Q', click: () => { app.isQuiting = true; app.quit(); } }
      ]
    },
    {
      label: '编辑',
      submenu: [
        { label: '搜索', accelerator: 'CmdOrCtrl+K', click: () => mainWindow?.webContents.send('menu-search') },
        { type: 'separator' },
        { label: '刷新', accelerator: 'F5', role: 'reload' }
      ]
    },
    {
      label: '视图',
      submenu: [
        { label: '全屏', accelerator: 'F11', role: 'togglefullscreen' },
        { type: 'separator' },
        { label: '开发者工具', accelerator: 'F12', role: 'toggleDevTools' }
      ]
    }
  ];
  Menu.setApplicationMenu(Menu.buildFromTemplate(template));
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

// IPC
ipcMain.handle('window-minimize', () => mainWindow?.minimize());
ipcMain.handle('window-maximize', () => {
  if (!mainWindow) return;
  if (mainWindow.isMaximized()) mainWindow.unmaximize();
  else mainWindow.maximize();
});
ipcMain.handle('window-close', () => { app.isQuiting = true; mainWindow?.close(); });
ipcMain.handle('window-is-maximized', () => mainWindow?.isMaximized() || false);
ipcMain.handle('show-notification', (e, { title, body }) => {
  new Notification({ title, body }).show();
});
ipcMain.handle('show-open-dialog', async () => {
  const result = await dialog.showOpenDialog(mainWindow, {
    properties: ['openFile', 'multiSelections'],
    filters: [
      { name: '文档', extensions: ['doc', 'docx', 'xlsx', 'pptx', 'pdf'] },
      { name: '图片', extensions: ['jpg', 'jpeg', 'png', 'gif', 'webp'] }
    ]
  });
  return result.filePaths;
});
ipcMain.handle('show-save-dialog', async (e, { defaultPath, filters }) => {
  const result = await dialog.showSaveDialog(mainWindow, { defaultPath, filters });
  return result.filePath;
});
ipcMain.on('window-message', (e, message) => {
  mainWindow?.webContents.send('window-message', message);
});
