import router from './router'
import store from './store'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

// 临时取消权限验证 - 开发测试模式
const TEST_MODE = true

router.beforeEach(async (to, from, next) => {
  NProgress.start()

  // 测试模式：直接允许访问所有页面
  if (TEST_MODE) {
    // 自动设置模拟用户数据
    if (!localStorage.getItem('token')) {
      const mockUserInfo = {
        userId: '1',
        username: 'testuser',
        nickname: '测试用户',
        avatar: '',
        roles: ['admin'],
        permissions: ['*:*:*'],
      }

      localStorage.setItem('token', 'test-token')
      localStorage.setItem('userInfo', JSON.stringify(mockUserInfo))

      // 同步到Vuex store
      store.commit('user/SET_TOKEN', 'test-token')
      store.commit('user/SET_USER_INFO', mockUserInfo)
    }

    // 直接放行
    next()
    return
  }

  // 原有的权限验证逻辑（暂时禁用）
  const token = localStorage.getItem('token')
  const whiteList = ['/login', '/404', '/demo', '/test']

  if (token) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
