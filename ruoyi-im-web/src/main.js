import { createApp } from 'vue'
import 'viewerjs/dist/viewer.css'
import VueViewer from 'v-viewer'

import './styles/global.scss'
import './styles/a11y.scss'
import './styles/admin-theme.scss'
// 只注册 Element Plus 图标组件（其他组件自动按需导入）
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import store from './store'
import { initPerformanceMonitoring } from './utils/performance'
import { setupSkipLinks } from './utils/a11y'

// 初始化可访问性功能
setupSkipLinks()

// 初始化性能监控（仅生产环境）
if (import.meta.env.PROD) {
  initPerformanceMonitoring()
}

// Service Worker 注册（仅生产环境）
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  import('./registerSW.js').then(({ registerServiceWorker }) => {
    registerServiceWorker()
  })
}

const app = createApp(App)

app.use(router)
app.use(store)
app.use(VueViewer)

// 全局注册 Element Plus 图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
