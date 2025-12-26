import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import App from './App.vue'
import router from './router'
import store from './store'
import '@/assets/styles/index.scss'
import '@/styles/im-theme.scss' // IM主题样式
import './permission' // 路由守卫

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(store)

app.mount('#app')
