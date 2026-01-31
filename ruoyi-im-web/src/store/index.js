import { createStore } from 'vuex'
import im from './modules/im'
import user from './modules/user'
import { startCleanupTimer } from './modules/im-message'

const store = createStore({
  modules: {
    im,
    user
  }
})

// ========== 启动定期清理任务 ==========
// 在应用启动时启动定期清理任务（每 5 分钟清理一次）
if (typeof window !== 'undefined') {
  startCleanupTimer(store)
}

export default store

