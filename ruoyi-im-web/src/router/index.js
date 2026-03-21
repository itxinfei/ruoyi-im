import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'

// Token 验证间隔（5分钟）
const TOKEN_VALIDATION_INTERVAL = 5 * 60 * 1000
let lastValidationTime = 0

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

  // 检查是否需要验证 token（距离上次验证超过5分钟才验证）
  const now = Date.now()
  const shouldValidate = now - lastValidationTime > TOKEN_VALIDATION_INTERVAL

  if (shouldValidate) {
    try {
      const res = await fetch(import.meta.env.VITE_API_BASE_URL + '/api/im/auth/validateToken', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${store.state.user.token}`
        },
        credentials: 'include'
      })
      if (!res.ok) {
        // Token 无效，清除用户信息并跳转到登录页
        await store.dispatch('user/logout')
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
        return
      }
      const data = await res.json()
      if (data.code !== 200 || !data.data) {
        // Token 无效，清除用户信息并跳转到登录页
        await store.dispatch('user/logout')
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
        return
      }
      // 验证成功，更新时间戳
      lastValidationTime = now
    } catch (error) {
      console.error('Token validation error:', error)
      // 网络错误时不强制登出，允许继续访问
      // 只在明确返回401时才登出
    }
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

// 导出重置验证时间的方法（登录成功后调用）
export function resetValidationTime() {
  lastValidationTime = Date.now()
}

export default router

