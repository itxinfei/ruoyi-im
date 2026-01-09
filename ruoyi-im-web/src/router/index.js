import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '@/utils/im-user'

import imRoutes from './modules/im'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true },
  },
  {
    path: '/',
    redirect: '/im/chat',
  },
  ...imRoutes,
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

router.beforeEach((to, from, next) => {
  // 如果是登录页面，允许直接访问
  if (to.path === '/login') {
    next()
    return
  }
  
  // 检查用户是否已登录
  if (isLoggedIn()) {
    next()
  } else {
    // 未登录，跳转到登录页面
    next('/login')
  }
})

router.afterEach(to => {
  document.title = to.meta.title || 'RuoYi IM'
})

export default router
