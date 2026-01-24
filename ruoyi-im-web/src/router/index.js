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
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/UserManagement.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'groups',
        name: 'AdminGroups',
        component: () => import('../views/admin/GroupManagement.vue'),
        meta: { title: '群组管理' }
      },
      {
        path: 'messages',
        name: 'AdminMessages',
        component: () => import('../views/admin/MessageManagement.vue'),
        meta: { title: '消息管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态和角色权限
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('im_token')
  // 获取用户角色（从 localStorage 或登录信息中获取）
  const userRole = localStorage.getItem('im_user_role') || 'USER'

  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    if (!token) {
      // 未登录，跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 检查角色权限
    if (to.meta.roles && to.meta.roles.length > 0) {
      if (!to.meta.roles.includes(userRole)) {
        // 权限不足，跳转到首页
        console.warn('权限不足，需要角色:', to.meta.roles)
        next('/')
        return
      }
    }

    next()
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

