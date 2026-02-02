<template>
  <div class="admin-layout">
    <el-container :class="{ 'sidebar-collapsed': isCollapsed }">
      <!-- 左侧主导航栏 -->
      <el-aside :width="sidebarWidth" class="admin-sidebar">
        <!-- Logo区域 -->
        <div class="sidebar-logo">
          <div v-if="!isCollapsed" class="logo-content">
            <span class="logo-text">RuoYi-IM</span>
            <span class="logo-subtitle">管理后台</span>
          </div>
          <el-icon v-else class="logo-icon" @click="toggleCollapse">
            <Monitor />
          </el-icon>
        </div>

        <!-- 导航菜单 -->
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>数据概览</template>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/groups">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>群组管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/messages">
            <el-icon><ChatLineSquare /></el-icon>
            <template #title>消息管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/departments">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>部门管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/roles">
            <el-icon><Lock /></el-icon>
            <template #title>角色权限</template>
          </el-menu-item>
          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Document /></el-icon>
            <template #title>操作日志</template>
          </el-menu-item>
          <el-menu-item index="/admin/backup">
            <el-icon><FolderOpened /></el-icon>
            <template #title>数据备份</template>
          </el-menu-item>
          <el-menu-item index="/admin/monitor">
            <el-icon><TrendCharts /></el-icon>
            <template #title>系统监控</template>
          </el-menu-item>
        </el-menu>

        <!-- 底部折叠按钮 -->
        <div class="sidebar-footer">
          <el-button
            :icon="isCollapsed ? DArrowRight : DArrowLeft"
            text
            @click="toggleCollapse"
            class="collapse-btn"
          >
            {{ isCollapsed ? '' : '收起' }}
          </el-button>
        </div>
      </el-aside>

      <!-- 主内容区容器 -->
      <el-container class="main-container">
        <!-- 顶部 Header -->
        <el-header class="admin-header">
          <!-- 左侧：折叠按钮 + 面包屑 -->
          <div class="header-left">
            <el-button
              :icon="isCollapsed ? DArrowRight : DArrowLeft"
              text
              class="collapse-trigger"
              @click="toggleCollapse"
            />
            <el-breadcrumb separator="/">
              <el-breadcrumb-item
                v-for="item in breadcrumbList"
                :key="item.path"
                :to="item.path"
              >
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <!-- 右侧：通知 + 主题切换 + 用户信息 + 退出 -->
          <div class="header-right">
            <!-- 通知图标 -->
            <el-badge :value="notificationCount" :hidden="notificationCount === 0" class="notification-badge">
              <el-button :icon="Bell" text class="header-icon-btn" />
            </el-badge>

            <!-- 主题切换按钮 -->
            <el-tooltip :content="themeTooltip" placement="bottom">
              <el-button
                :icon="themeIcon"
                text
                class="header-icon-btn theme-toggle-btn"
                @click="toggleDark"
              />
            </el-tooltip>

            <!-- 用户信息下拉 -->
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="user-info">
                <el-avatar :size="32" class="user-avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span v-if="!isMobile" class="user-name">{{ userName }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人信息
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    系统设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="back">
                    <el-icon><ChatDotRound /></el-icon>
                    返回聊天
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" style="color: var(--dt-error);">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="admin-main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>import { clearAuth } from '@/utils/storage'

import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import {
  Monitor,
  User,
  ChatDotRound,
  ChatLineSquare,
  DArrowLeft,
  DArrowRight,
  Bell,
  ArrowDown,
  Setting,
  SwitchButton,
  OfficeBuilding,
  Lock,
  Document,
  FolderOpened,
  TrendCharts,
  Sunny,
  Moon
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 侧边栏折叠状态
const isCollapsed = ref(false)
const sidebarWidth = computed(() => isCollapsed.value ? '64px' : '200px')

// 用户信息
const userName = ref('管理员')
const notificationCount = ref(0)

// 是否移动端
const isMobile = ref(window.innerWidth < 768)

// 主题管理
const { isDark, toggleDark } = useTheme()
const themeIcon = computed(() => isDark.value ? Moon : Sunny)
const themeTooltip = computed(() => isDark.value ? '切换到浅色模式' : '切换到深色模式')

// 监听窗口大小变化
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    isCollapsed.value = true
  }
}

watch(() => route.path, () => {
  // 移动端自动收起侧边栏
  if (isMobile.value) {
    isCollapsed.value = true
  }
})

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 页面标题映射
const pageTitleMap = {
  '/admin/dashboard': { title: '数据概览', parent: '' },
  '/admin/users': { title: '用户管理', parent: '' },
  '/admin/groups': { title: '群组管理', parent: '' },
  '/admin/messages': { title: '消息管理', parent: '' },
  '/admin/departments': { title: '部门管理', parent: '' },
  '/admin/roles': { title: '角色权限', parent: '' },
  '/admin/settings': { title: '系统设置', parent: '' },
  '/admin/logs': { title: '操作日志', parent: '' },
  '/admin/backup': { title: '数据备份', parent: '' },
  '/admin/monitor': { title: '系统监控', parent: '' }
}

// 面包屑列表
const breadcrumbList = computed(() => {
  const current = pageTitleMap[route.path]
  if (!current) return [{ title: '管理后台', path: '/admin/dashboard' }]
  return [
    { title: '首页', path: '/admin/dashboard' },
    { title: current.title, path: route.path }
  ]
})

// 切换侧边栏折叠
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 用户下拉菜单命令处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      // 打开个人信息
      break
    case 'settings':
      router.push('/admin/settings')
      break
    case 'back':
      router.push('/')
      break
    case 'logout':
      logout()
      break
  }
}

// 退出登录
const logout = () => {
  clearAuth()
  router.push('/login')
}

// 初始化
if (typeof window !== 'undefined') {
  window.addEventListener('resize', handleResize)
  handleResize()
}
</script>

<style scoped>
/* 引入主题变量 */
@import '@/styles/admin-theme.css';

/* ================================
   布局容器
   ================================ */
.admin-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.admin-layout :deep(.el-container) {
  height: 100%;
}

/* ================================
   左侧导航栏
   ================================ */
.admin-sidebar {
  background-color: var(--dt-sidebar-bg);
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  transition: width var(--dt-transition-base) var(--dt-ease-out);
  overflow: hidden;
}

/* Logo区域 */
.sidebar-logo {
  height: var(--dt-header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.logo-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-primary);
  line-height: 1.2;
}

.logo-subtitle {
  font-size: 12px;
  color: var(--dt-text-secondary);
  margin-top: 2px;
}

.logo-icon {
  font-size: 24px;
  color: var(--dt-primary);
  cursor: pointer;
}

/* 菜单样式 */
.sidebar-menu {
  flex: 1;
  border: none;
  background-color: transparent;
  padding: var(--dt-space-sm) 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

/* 菜单项 - 钉钉风格 */
.sidebar-menu :deep(.el-menu-item) {
  height: var(--dt-menu-item-height);
  line-height: var(--dt-menu-item-height);
  padding: 0 var(--dt-menu-item-padding);
  margin: var(--dt-space-xs) var(--dt-space-sm);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-sidebar-text);
  transition: all var(--dt-transition-base) var(--dt-ease-out);
  position: relative;
}

/* 选中状态 - 左侧蓝色指示条 */
.sidebar-menu :deep(.el-menu-item.is-active) {
  color: var(--dt-sidebar-text-active);
  background-color: var(--dt-sidebar-bg-active);
}

.sidebar-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: var(--dt-sidebar-indicator-width);
  height: 20px;
  background-color: var(--dt-primary);
  border-radius: 0 2px 2px 0;
}

/* 悬停效果 */
.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: var(--dt-bg-hover);
}

/* 折叠状态 */
.sidebar-menu.el-menu--collapse {
  width: 64px;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item) {
  padding: 0;
  justify-content: center;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item .el-icon) {
  margin: 0;
}

/* 底部折叠按钮 */
.sidebar-footer {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.collapse-btn {
  color: var(--dt-text-secondary);
  font-size: 12px;
}

.collapse-btn:hover {
  color: var(--dt-primary);
}

/* ================================
   主容器
   ================================ */
.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ================================
   顶部 Header
   ================================ */
.admin-header {
  height: var(--dt-header-height);
  background-color: var(--dt-header-bg);
  border-bottom: 1px solid var(--dt-header-border);
  box-shadow: var(--dt-shadow-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--dt-space-md);
  flex-shrink: 0;
}

/* 左侧区域 */
.header-left {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

.collapse-trigger {
  font-size: 18px;
  color: var(--dt-text-secondary);
  padding: 4px;
}

.collapse-trigger:hover {
  color: var(--dt-primary);
}

/* 面包屑 */
.header-left :deep(.el-breadcrumb) {
  font-size: 14px;
}

.header-left :deep(.el-breadcrumb__item) {
  color: var(--dt-text-secondary);
}

.header-left :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--dt-text-primary);
  font-weight: 500;
}

.header-left :deep(.el-breadcrumb__separator) {
  color: var(--dt-text-placeholder);
  margin: 0 8px;
}

/* 右侧区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

/* 通知图标 */
.notification-badge {
  margin-right: var(--dt-space-xs);
}

.header-icon-btn {
  font-size: 18px;
  color: var(--dt-text-secondary);
  padding: 8px;
}

.header-icon-btn:hover {
  color: var(--dt-primary);
}

/* 主题切换按钮 */
.theme-toggle-btn {
  font-size: 18px;
}

.theme-toggle-btn:hover {
  color: var(--dt-primary);
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  padding: 4px 8px;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  transition: background-color var(--dt-transition-base) var(--dt-ease-out);
}

.user-info:hover {
  background-color: var(--dt-bg-hover);
}

.user-avatar {
  background-color: var(--dt-primary);
  color: white;
}

.user-name {
  font-size: 14px;
  color: var(--dt-text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  color: var(--dt-text-secondary);
}

/* ================================
   主内容区
   ================================ */
.admin-main {
  flex: 1;
  background-color: var(--dt-bg-page);
  padding: var(--dt-space-md);
  overflow-y: auto;
  overflow-x: hidden;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--dt-transition-slow) var(--dt-ease-out),
              transform var(--dt-transition-slow) var(--dt-ease-out);
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ================================
   响应式适配
   ================================ */
@media (max-width: 768px) {
  .admin-header {
    padding: 0 var(--dt-space-sm);
  }

  .header-left :deep(.el-breadcrumb) {
    display: none;
  }

  .user-name {
    display: none;
  }

  .admin-main {
    padding: var(--dt-space-sm);
  }
}

/* ================================
   Element Plus 样式覆盖
   ================================ */

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  background-color: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  box-shadow: var(--dt-shadow-dropdown);
  padding: 4px 0;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 14px;
  color: var(--dt-text-primary);
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: var(--dt-bg-hover);
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}

/* 深色模式下拉菜单样式优化 */
[data-theme='dark'] :deep(.el-dropdown-menu) {
  background-color: var(--dt-card-bg);
  border-color: var(--dt-border-base);
}

[data-theme='dark'] :deep(.el-dropdown-menu__item) {
  color: var(--dt-text-regular);
}

[data-theme='dark'] :deep(.el-dropdown-menu__item:hover) {
  background-color: var(--dt-bg-hover);
  color: var(--dt-text-primary);
}

/* 徽标样式 */
:deep(.el-badge__content) {
  background-color: var(--dt-error);
  border: 1px solid var(--dt-header-bg);
}
</style>
