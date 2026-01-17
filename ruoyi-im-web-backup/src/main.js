import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import store from './store'

// 钉钉6.5.x样式系统 - 必须在其他样式之前引入
import '@/styles/dingtalk-index.scss'

import '@/assets/styles/index.scss'
import '@/assets/styles/variables.scss'
import '@/assets/styles/components/_button.scss'
import '@/assets/styles/components/_card.scss'
import '@/assets/styles/animations/_index.scss'
import clickFeedback from '@/directives/clickFeedback'
import hoverScale from '@/directives/hoverScale'
import './permission'
import { initTheme as initAppTheme } from '@/utils/theme'
import globalErrorHandler from '@/utils/globalErrorHandler'
import SmartAvatar from '@/components/SmartAvatar/index.vue'

// 初始化主题（确保默认使用亮色主题）
initAppTheme()

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(store)
app.use(globalErrorHandler)

app.directive('click-feedback', clickFeedback)
app.directive('hover-scale', hoverScale)

// 全局注册智能头像组件
app.component('SmartAvatar', SmartAvatar)

app.mount('#app')
