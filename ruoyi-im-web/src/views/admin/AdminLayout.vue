<template>
  <div class="admin-layout">
    <el-container class="layout-shell">
      <el-aside width="232px" class="admin-aside">
        <div class="admin-logo">
          <div class="logo-mark">IM</div>
          <div class="logo-text">
            <div class="title">管理后台</div>
            <div class="subtitle">Operations Console</div>
          </div>
        </div>

        <el-menu
          :default-active="activeMenu"
          router
          class="admin-menu"
          background-color="transparent"
          text-color="#cfd8ea"
          active-text-color="#ffffff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><Monitor /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/groups">
            <el-icon><ChatDotRound /></el-icon>
            <span>群组管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/messages">
            <el-icon><ChatLineSquare /></el-icon>
            <span>消息管理</span>
          </el-menu-item>
          <el-menu-item v-if="isSuperAdmin" index="/admin/system-config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item index="/admin/audit-log">
            <el-icon><Document /></el-icon>
            <span>审计日志</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <div class="title-row">
              <h1 class="page-title">{{ pageTitle }}</h1>
              <el-tag size="small" :type="roleTagType" effect="plain">{{ roleLabel }}</el-tag>
            </div>
            <p class="page-desc">统一管理用户、群组、消息与系统配置</p>
          </div>

          <div class="header-right">
            <span class="admin-user">{{ adminName }}</span>
            <el-button type="primary" plain @click="goToChat">返回聊天</el-button>
            <el-button type="danger" plain @click="logout">退出登录</el-button>
          </div>
        </el-header>

        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { Monitor, User, ChatDotRound, ChatLineSquare, Setting, Document } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const store = useStore()

const activeMenu = computed(() => route.path)

const pageTitleMap = {
  '/admin/dashboard': '数据概览',
  '/admin/users': '用户管理',
  '/admin/groups': '群组管理',
  '/admin/messages': '消息管理',
  '/admin/system-config': '系统配置',
  '/admin/audit-log': '审计日志'
}

const pageTitle = computed(() => pageTitleMap[route.path] || '管理后台')

const adminName = computed(() => {
  return store.getters['user/userName']
})

// 获取用户角色
const userRole = computed(() => {
  return store.getters['user/userRole']
})

// 是否是超级管理员
const isSuperAdmin = computed(() => userRole.value === 'SUPER_ADMIN')

// 角色标签显示
const roleTagType = computed(() => isSuperAdmin.value ? 'danger' : 'info')
const roleLabel = computed(() => isSuperAdmin.value ? '超级管理员' : '管理员')

const goToChat = () => {
  router.push('/')
}

const logout = () => {
  localStorage.removeItem('im_token')
  localStorage.removeItem('im_user_info')
  localStorage.removeItem('im_user_role')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background: var(--dt-bg-body);
}

.layout-shell {
  height: 100%;
}

.admin-aside {
  background: linear-gradient(180deg, var(--dt-bg-body-dark) 0%, var(--dt-bg-card-dark) 100%);
  border-right: 1px solid var(--dt-border-light);
}

.admin-logo {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-light) 100%);
  color: var(--dt-text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.logo-text .title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.1;
}

.logo-text .subtitle {
  color: #8fa8cf;
  font-size: 11px;
  margin-top: 2px;
}

.admin-menu {
  border-right: none;
  padding: 12px 8px;
}

:deep(.admin-menu .el-menu-item) {
  margin: 6px 8px;
  border-radius: 10px;
  height: 42px;
  line-height: 42px;
}

:deep(.admin-menu .el-menu-item:hover) {
  background: rgba(82, 130, 255, 0.16);
}

:deep(.admin-menu .el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(48, 112, 255, 0.88) 0%, rgba(63, 151, 255, 0.88) 100%);
}

.admin-header {
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 22px;
  height: 72px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: var(--dt-text-primary);
}

.page-desc {
  margin: 2px 0 0;
  color: var(--dt-text-secondary);
  font-size: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-user {
  color: var(--dt-text-secondary);
  font-weight: 500;
}

.admin-main {
  background: var(--dt-bg-body);
  padding: 16px;
}

@media (max-width: 1024px) {
  .admin-aside {
    width: 210px !important;
  }

  .header-right {
    gap: 6px;
  }

  .page-desc {
    display: none;
  }
}
</style>
