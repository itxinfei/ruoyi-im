import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/LoginPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/MainPage.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('im_token')

  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    if (token) {
      next()
    } else {
      // 未登录，跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }
  } else {
    // 如果已登录，访问登录页时跳转到首页
    if (to.path === '/login' && token) {
      next('/')
    } else {
      next()
    }
  }
})

export default router

