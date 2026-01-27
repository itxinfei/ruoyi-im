/**
 * Vue 全局类型声明
 * 为 Vite 和 Vue 3 提供类型支持
 * @author ruoyi
 */

import { Router } from 'vue-router'
import { Store } from 'vuex'

// 扩展 ComponentCustomProperties
declare module '@vue/runtime-core' {
  export interface ComponentCustomProperties {
    $router: Router
    $store: Store<any>
    $api: any
  }
}

// 扩展窗体对象
declare global {
  interface Window {
    // 全局配置
    VUE_APP_BASE_API: string
    VUE_APP_WS_URL: string

    // 自定义全局属性
    imWebSocket?: WebSocket
  }
}

export {}
