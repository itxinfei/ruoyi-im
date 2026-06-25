import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/global.scss'
import App from './App.vue'
import router from './router'
import store from './store'

/**
 * RuoYi-IM 启动入口
 * 移除了全量 ElementPlus 图标注册（~200KB），各组件按需导入所需图标
 */
const app = createApp(App)

app.use(store)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
