import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/global.scss'
import App from './App.vue'
import router from './router'
import store from './store'
import AppBubble from './components/Common/AppBubble.vue'

/**
 * RuoYi-IM 启动入口
 * 移除了全量 ElementPlus 图标注册（~200KB），各组件按需导入所需图标
 */
const app = createApp(App)

app.use(store)
app.use(router)
app.use(ElementPlus)

// 全局注册：消息气泡（全站唯一气泡样式来源，任何页面可直接 <AppBubble> 调用）
app.component('AppBubble', AppBubble)

app.mount('#app')
