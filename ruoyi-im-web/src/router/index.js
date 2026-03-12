import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'

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
      },
      {
        path: 'system-config',
        name: 'AdminSystemConfig',
        component: () => import('../views/admin/SystemConfig.vue'),
        meta: { title: '系统配置' }
      },
      {
        path: 'audit-log',
        name: 'AdminAuditLog',
        component: () => import('../views/admin/AuditLog.vue'),
        meta: { title: '审计日志' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态和角色权限
router.beforeEach(async (to, from, next) => {
  // 如果路由不需要认证，直接放行（登录页面除外）
  if (!to.meta.requiresAuth) {
    // 如果已登录，访问登录页时跳转到首页
    if (to.path === '/login' && store.getters['user/isLoggedIn']) {
      next('/')
    } else {
      next()
    }
    return
  }

  // 路由需要认证
  const token = store.getters['user/isLoggedIn']

  // 未登录，跳转到登录页
  if (!token) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 验证 token 有效性
  try {
    const isTokenValid = await store.dispatch('user/validateToken')
    if (!isTokenValid) {
      // Token 无效，清除用户信息并跳转到登录页
      await store.dispatch('user/logout')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  } catch (error) {
    console.error('Token validation error:', error)
    // Token 验证失败，清除用户信息并跳转到登录页
    await store.dispatch('user/logout')
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 检查角色权限（如果路由定义了需要的角色）
  if (to.meta.roles && to.meta.roles.length > 0) {
    try {
      // 对于敏感路由，强制从后端验证用户角色
      await store.dispatch('user/validateUserRole', to.meta.roles)
      next()
    } catch (error) {
      console.error('Role validation error:', error)
      // 权限不足，跳转到首页
      next('/')
      return
    }
  } else {
    next()
  }
})

export default router

