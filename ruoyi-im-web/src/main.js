import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import App from './App.vue'
import router from './router'
import store from './store'
import '@/assets/styles/index.scss'
import '@/assets/styles/variables.scss'
import '@/assets/styles/components/_button.scss'
import '@/assets/styles/components/_card.scss'
import '@/assets/styles/animations/_index.scss'
import clickFeedback from '@/directives/clickFeedback'
import hoverScale from '@/directives/hoverScale'
import './permission'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(store)

app.directive('click-feedback', clickFeedback)
app.directive('hover-scale', hoverScale)

app.mount('#app')
