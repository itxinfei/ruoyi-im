<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="admin-aside">
        <div class="admin-logo">
          <span>IM 管理后台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#001529"
          text-color="#fff"
          active-text-color="#1890ff"
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
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部栏 -->
        <el-header class="admin-header">
          <div class="header-left">
            <span class="page-title">{{ pageTitle }}</span>
          </div>
          <div class="header-right">
            <span class="admin-user">管理员</span>
            <el-button size="small" @click="goToChat">返回聊天</el-button>
            <el-button size="small" type="danger" @click="logout">退出</el-button>
          </div>
        </el-header>

        <!-- 内容区 -->
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
import { Monitor, User, ChatDotRound, ChatLineSquare } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

const pageTitleMap = {
  '/admin/dashboard': '数据概览',
  '/admin/users': '用户管理',
  '/admin/groups': '群组管理',
  '/admin/messages': '消息管理'
}

const pageTitle = computed(() => pageTitleMap[route.path] || '管理后台')

const goToChat = () => {
  router.push('/')
}

const logout = () => {
  localStorage.removeItem('im_token')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-aside {
  background-color: #001529;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #002140;
}

/* 紧凑菜单样式 */
:deep(.el-menu) {
  --el-menu-item-height: 40px;
}

:deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 4px;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-menu-item.is-active) {
  background-color: #1890ff;
}

.admin-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left .page-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-user {
  margin-right: 10px;
  color: #666;
}

.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  :deep(.el-menu) {
    --el-menu-item-height: 36px;
  }
  
  :deep(.el-menu-item) {
    margin: 2px 4px;
  }
}
</style>
