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
    meta: { requiresAuth: true, adminOnly: true },
    redirect: '/admin/dashboard',
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
        path: 'departments',
        name: 'AdminDepartments',
        component: () => import('../views/admin/DepartmentManagement.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: 'roles',
        name: 'AdminRoles',
        component: () => import('../views/admin/RoleManagement.vue'),
        meta: { title: '角色权限' }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('../views/admin/SystemSettings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('../views/admin/OperationLog.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'backup',
        name: 'AdminBackup',
        component: () => import('../views/admin/DataBackup.vue'),
        meta: { title: '数据备份' }
      },
      {
        path: 'monitor',
        name: 'AdminMonitor',
        component: () => import('../views/admin/SystemMonitor.vue'),
        meta: { title: '系统监控' }
      }
    ]
  },
  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态和角色权限
router.beforeEach((to, from, next) => {
  const { getToken, getUserRole, getUserInfo: getStoredUserInfo } = require('@/utils/storage')
  const token = getToken()

  // 获取用户角色（优先从 im_user_role 读取，兼容 im_user_info）
  let userRole = getUserRole()

  // 如果没有单独存储的角色，尝试从用户信息中解析
  if (userRole === 'USER') {
    const userInfo = getStoredUserInfo()
    if (userInfo?.role) {
      userRole = userInfo.role
    }
  }

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

    // 检查是否是管理后台专属页面
    if (to.meta.adminOnly) {
      // 管理后台需要 ADMIN 或 SUPER_ADMIN 角色
      if (userRole !== 'ADMIN' && userRole !== 'SUPER_ADMIN') {
        console.warn('权限不足，需要管理员角色')
        // 权限不足，跳转到首页并提示
        next({
          path: '/',
          query: { error: 'no_admin_permission' }
        })
        return
      }
    }

    // 检查具体角色权限
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
      // 如果是管理员，跳转到管理后台
      if (userRole === 'ADMIN' || userRole === 'SUPER_ADMIN') {
        next('/admin/dashboard')
      } else {
        next('/')
      }
    } else {
      next()
    }
  }
})

export default router
