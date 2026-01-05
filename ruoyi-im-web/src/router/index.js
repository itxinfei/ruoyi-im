import { createRouter, createWebHistory } from 'vue-router'

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
  // 开发阶段取消登录验证，允许直接访问所有页面
  next()
})

router.afterEach(to => {
  document.title = to.meta.title || 'RuoYi IM'
})

export default router
