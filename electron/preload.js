const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制
  minimize: () => ipcRenderer.invoke('window-minimize'),
  maximize: () => ipcRenderer.invoke('window-maximize'),
  close: () => ipcRenderer.invoke('window-close'),
  isMaximized: () => ipcRenderer.invoke('window-is-maximized'),
  
  // 文件操作
  openFiles: () => ipcRenderer.invoke('show-open-dialog'),
  saveFile: (opts) => ipcRenderer.invoke('show-save-dialog', opts),
  
  // 通知
  notify: (title, body) => ipcRenderer.invoke('show-notification', { title, body }),
  
  // 监听菜单事件
  onMenu: (callback) => {
    ipcRenderer.on('window-message', (e, msg) => callback(msg));
  }
});

// 标记 Electron 环境
window.isElectron = true;
