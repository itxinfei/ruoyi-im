import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/global.scss'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import store from './store'

// 预加载 Material Icons 字体，避免闪烁
const preloadMaterialIcons = () => {
  const link = document.createElement('link')
  link.rel = 'preload'
  link.href = 'https://fonts.googleapis.com/icon?family=Material+Icons+Outlined'
  link.as = 'style'
  link.onload = function () {
    this.onload = null
    this.rel = 'stylesheet'

    // 字体加载完成后的处理
    setTimeout(() => {
      document.querySelectorAll('.material-icons-outlined').forEach(el => {
        el.classList.add('loaded')
      })
    }, 100)
  }
  document.head.appendChild(link)
}

// 立即执行预加载
preloadMaterialIcons()

const app = createApp(App)

app.use(router)
app.use(store)
app.use(ElementPlus)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
