/**
 * Service Worker 注册和更新管理
 */
import { registerSW as _registerSW } from 'virtual:pwa-register'

/**
 * 注册 Service Worker
 */
export function registerServiceWorker() {
  const updateSW = _registerSW({
    onNeedRefresh() {
      // 有新版本可用时，显示更新提示
      showUpdatePrompt()
    },
    onOfflineReady() {
      // 离线内容已准备好
      console.log('[SW] 离线内容已准备就绪')
    },
    onRegistered(registration) {
      // Service Worker 注册成功
      console.log('[SW] Service Worker 已注册')

      // 定时检查更新（每小时）
      if (registration) {
        setInterval(() => {
          registration.update()
        }, 60 * 60 * 1000)
      }
    },
    onRegisterError(error) {
      console.error('[SW] Service Worker 注册失败:', error)
    }
  })

  return updateSW
}

/**
 * 显示更新提示
 */
function showUpdatePrompt() {
  // 防止重复提示
  if (document.querySelector('.sw-update-prompt')) {
    return
  }

  // 创建提示容器
  const prompt = document.createElement('div')
  prompt.className = 'sw-update-prompt'
  prompt.innerHTML = `
    <div class="sw-update-content">
      <div class="sw-update-icon">🔄</div>
      <div class="sw-update-message">
        <div class="sw-update-title">发现新版本</div>
        <div class="sw-update-desc">点击刷新获取最新内容</div>
      </div>
      <div class="sw-update-actions">
        <button class="sw-update-btn sw-update-btn--refresh" id="sw-refresh">立即刷新</button>
        <button class="sw-update-btn sw-update-btn--close" id="sw-close">稍后</button>
      </div>
    </div>
  `

  // 添加样式
  const style = document.createElement('style')
  style.textContent = `
    .sw-update-prompt {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      animation: swSlideIn 0.3s ease-out;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    @keyframes swSlideIn {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }
    
    .sw-update-content {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: white;
      border-radius: 8px;
      min-width: 320px;
      border: 1px solid #e5e7eb;
    }
    
    .sw-update-icon {
      font-size: 24px;
      flex-shrink: 0;
    }
    
    .sw-update-message {
      flex: 1;
    }
    
    .sw-update-title {
      font-size: 14px;
      font-weight: 600;
      color: #111827;
      margin-bottom: 2px;
    }
    
    .sw-update-desc {
      font-size: 12px;
      color: #6b7280;
    }
    
    .sw-update-actions {
      display: flex;
      gap: 8px;
    }
    
    .sw-update-btn {
      padding: 6px 12px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;
      cursor: pointer;
      border: none;
      transition: all 0.2s;
    }
    
    .sw-update-btn--refresh {
      background: var(--dt-brand-color);
      color: white;
    }
    
    .sw-update-btn--refresh:hover {
      background: var(--dt-brand-hover);
    }
    
    .sw-update-btn--close {
      background: #f3f4f6;
      color: #374151;
    }
    
    .sw-update-btn--close:hover {
      background: #e5e7eb;
    }
  `

  document.head.appendChild(style)
  document.body.appendChild(prompt)

  // 绑定事件
  const refreshBtn = document.getElementById('sw-refresh')
  const closeBtn = document.getElementById('sw-close')

  refreshBtn?.addEventListener('click', () => {
    // 刷新页面以应用新版本
    window.location.reload()
  })

  closeBtn?.addEventListener('click', () => {
    // 关闭提示（用户选择稍后更新）
    prompt.remove()
  })
}

export default registerServiceWorker
