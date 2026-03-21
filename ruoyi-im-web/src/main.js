import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/global.scss'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import store from './store'

/**
 * RuoYi-IM 启动入口 (加固版)
 * 1. 移除了阻塞渲染的外部 CDN 字体加载逻辑
 * 2. 优化了插件挂载顺序
 */
const app = createApp(App)

app.use(store)
app.use(router)
app.use(ElementPlus)

// 注册全量 Element Icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
