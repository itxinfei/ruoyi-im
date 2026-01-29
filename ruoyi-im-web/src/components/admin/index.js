/**
 * 管理后台通用组件导出
 * Admin Common Components Export
 *
 * @example
 * // 全局注册
 * import AdminComponents from '@/components/admin'
 * app.use(AdminComponents)
 *
 * // 按需引入
 * import { AdminActionBar, AdminSearchBar } from '@/components/admin'
 */

import AdminActionBar from './ActionBar.vue'
import AdminSearchBar from './SearchBar.vue'
import AdminConfirmDialog from './ConfirmDialog.vue'

// 组件列表
const components = [
  AdminActionBar,
  AdminSearchBar,
  AdminConfirmDialog
]

// 安装函数
const install = (app) => {
  components.forEach(component => {
    app.component(component.name || component.__name, component)
  })
}

// 默认导出
export default {
  install
}

// 单独导出各组件
export {
  AdminActionBar,
  AdminSearchBar,
  AdminConfirmDialog
}
