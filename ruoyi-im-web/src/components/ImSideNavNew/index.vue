<template>
  <aside class="dingtalk-l1-nav">
    <!-- 1. 顶部 Logo -->
    <div class="nav-header">
      <div class="logo-box">
        <el-icon><Promotion /></el-icon>
      </div>
    </div>

    <!-- 2. 主导航区 (带指示器) -->
    <div class="nav-body custom-scrollbar">
      <div 
        v-for="item in topNavs" 
        :key="item.id" 
        class="nav-item-wrapper"
        :class="{ active: activeModule === item.id }"
        @click="switchModule(item.id)"
      >
        <!-- 左侧 3px 指示条 -->
        <div class="active-indicator"></div>
        
        <div class="nav-item">
          <el-icon class="nav-icon">
            <component :is="activeModule === item.id ? item.activeIcon : item.icon" />
          </el-icon>
          <span class="nav-label">{{ item.name }}</span>
          <div v-if="item.badge > 0" class="nav-badge">{{ item.badge > 99 ? '9' : item.badge }}</div>
        </div>
      </div>
    </div>

    <!-- 3. 底部操作区 -->
    <div class="nav-footer">
      <!-- 搜索 -->
      <div class="footer-item" @click="switchModule('search')">
        <el-icon><Search /></el-icon>
      </div>
      
      <!-- 更多菜单 -->
      <el-dropdown trigger="click" placement="right-end" @command="handleMore">
        <div class="footer-item">
          <el-icon><MoreFilled /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="dt-more-dropdown">
            <el-dropdown-item command="settings" :icon="Setting">系统设置</el-dropdown-item>
            <el-dropdown-item command="theme" :icon="Sunny">切换主题</el-dropdown-item>
            <el-dropdown-item command="about" :icon="InfoFilled" divided>关于 IM</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 个人头像 -->
      <div class="profile-entry" @click="openEditProfile">
        <img :src="userAvatar" class="avatar-img" alt="me">
        <div class="status-dot online"></div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="js">
import { ref, markRaw } from 'vue'
import {
  ChatDotRound, User, Bell, Menu, Tickets, Calendar,
  FolderOpened, Phone, Search, MoreFilled, Setting,
  Sunny, InfoFilled, Promotion, ChatLineRound,
  UserFilled, BellFilled, Grid, List, CircleCheck,
  PhoneFilled
} from '@element-plus/icons-vue'

defineProps({
  activeModule: { type: String, default: 'chat' }
})

const emit = defineEmits(['switch-module', 'open-edit-profile', 'open-system-settings'])

const userAvatar = ref('/avatars/me.svg')

const topNavs = [
  { id: 'chat', name: '消息', icon: markRaw(ChatLineRound), activeIcon: markRaw(ChatDotRound), badge: 5 },
  { id: 'contacts', name: '通讯录', icon: markRaw(User), activeIcon: markRaw(UserFilled), badge: 0 },
  { id: 'ding', name: 'DING', icon: markRaw(Bell), activeIcon: markRaw(BellFilled), badge: 0 },
  { id: 'workbench', name: '工作台', icon: markRaw(Menu), activeIcon: markRaw(Grid), badge: 0 },
  { id: 'todo', name: '待办', icon: markRaw(Tickets), activeIcon: markRaw(List), badge: 2 },
  { id: 'calendar', name: '日历', icon: markRaw(Calendar), activeIcon: markRaw(CircleCheck), badge: 0 },
  { id: 'documents', name: '云盘', icon: markRaw(FolderOpened), activeIcon: markRaw(CircleCheck), badge: 0 },
  { id: 'call', name: '通话', icon: markRaw(Phone), activeIcon: markRaw(PhoneFilled), badge: 0 }
]

const switchModule = (id) => emit('switch-module', id)
const openEditProfile = () => emit('open-edit-profile')
const handleMore = (cmd) => {
  if (cmd === 'settings') emit('open-system-settings')
}
</script>

<style scoped lang="scss">
.dingtalk-l1-nav {
  width: 68px; // 钉钉标准宽度
  height: 100%;
  background: #1e1e1e; // 钉钉深色 L1 背景
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  flex-shrink: 0;
  z-index: 2000;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.nav-header {
  margin-bottom: 20px;
  .logo-box {
    width: 36px;
    height: 36px;
    background: var(--dt-brand-gradient);
    border-radius: 8px;
    @include flex-center;
    color: #fff;
    font-size: 20px;
    cursor: pointer;
    &:hover { filter: brightness(1.1); }
  }
}

.nav-body {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item-wrapper {
  position: relative;
  width: 100%;
  height: 56px; // 对齐钉钉纵向间距
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;

  .active-indicator {
    position: absolute;
    left: 0;
    width: 3px;
    height: 18px;
    background: var(--dt-brand-color);
    border-radius: 0 2px 2px 0;
    opacity: 0;
    transform: scaleY(0.5);
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: rgba(255, 255, 255, 0.65);
    transition: all 0.2s;
    
    .nav-icon { font-size: 22px; }
    .nav-label { font-size: 10px; font-weight: 500; transform: scale(0.9); }
  }

  &:hover {
    .nav-item { color: #fff; }
    background: rgba(255, 255, 255, 0.05);
  }

  &.active {
    .active-indicator { opacity: 1; transform: scaleY(1); }
    .nav-item { color: var(--dt-brand-color); font-weight: 600; }
    background: rgba(255, 255, 255, 0.08);
  }
}

.nav-badge {
  position: absolute;
  top: 8px;
  right: 14px;
  background: var(--dt-error-color);
  color: #fff;
  font-size: 10px;
  min-width: 14px;
  height: 14px;
  border-radius: 7px;
  @include flex-center;
  padding: 0 4px;
  border: 1.5px solid #1e1e1e;
  transform: scale(0.8);
}

.nav-footer {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding-bottom: 8px;

  .footer-item {
    width: 40px;
    height: 40px;
    @include flex-center;
    color: rgba(255, 255, 255, 0.65);
    border-radius: 8px;
    cursor: pointer;
    font-size: 20px;
    transition: all 0.2s;
    &:hover { background: rgba(255, 255, 255, 0.05); color: #fff; }
  }
}

.profile-entry {
  position: relative;
  width: 36px;
  height: 36px;
  cursor: pointer;
  
  .avatar-img {
    width: 100%;
    height: 100%;
    border-radius: 8px;
    background: #444;
    object-fit: cover;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .status-dot {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    border: 2px solid #1e1e1e;
    &.online { background: #22ab5c; }
  }

  &:hover .avatar-img { filter: brightness(1.1); }
}

// 更多菜单自定义样式
:deep(.dt-more-dropdown) {
  background: #2d2d2d;
  border: 1px solid rgba(255,255,255,0.1);
  .el-dropdown-menu__item {
    color: rgba(255,255,255,0.8);
    &:hover { background: rgba(255,255,255,0.05); color: var(--dt-brand-color); }
  }
}
</style>
