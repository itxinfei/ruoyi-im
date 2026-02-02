/// <reference types="vite/client" />

/**
 * Vite 环境变量类型声明
 * 定义所有 import.meta.env 的类型
 * @author ruoyi
 */

declare module '*.vue' {
  import { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_BASE_API: string
  readonly VITE_APP_WS_URL: string
  readonly BASE_URL: string
  readonly MODE: string
  readonly DEV: boolean
  readonly PROD: boolean
  readonly SSR: boolean
}

// 扩展 ImportMeta 以支持 import.meta.env
interface ImportMeta {
  readonly env: ImportMetaEnv
}
