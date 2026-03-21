<template>
  <aside class="side-nav">
    <!-- 顶部功能区 -->
    <div class="nav-top">
      <div 
        v-for="item in topNavs" 
        :key="item.id"
        :class="['nav-item', { active: activeModule === item.id }]"
        @click="switchModule(item.id)"
      >
        <i :class="item.icon" :title="item.name"></i>
        <div v-if="item.badge > 0" class="nav-badge">{{ item.badge > 99 ? '99+' : item.badge }}</div>
      </div>
    </div>

    <!-- 底部功能区 -->
    <div class="nav-bottom">
      <div 
        class="nav-item admin-item" 
        v-if="isAdmin"
        :class="{ active: activeModule === 'admin' }"
        @click="switchModule('admin')"
      >
        <i class="el-icon-setting" title="管理后台"></i>
      </div>
      
      <div class="user-avatar-wrapper" @click="openProfile">
        <img :src="userAvatar" class="user-avatar" alt="me" />
        <div class="online-status-dot"></div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="js">
/**
 * ImSideNavNew (一级导航栏 - 8.2.0 视觉对齐版)
 * 强制引用全局变量，解决颜色不生效问题
 */
import { ref } from 'vue';

const props = defineProps({
  activeModule: { type: String, default: 'chat' }
});

const emit = defineEmits(['switch-module']);

// 状态
const isAdmin = ref(true);
const userAvatar = ref('/avatars/me.png');

const topNavs = [
  { id: 'chat', name: '消息', icon: 'el-icon-chat-dot-round', badge: 5 },
  { id: 'contacts', name: '通讯录', icon: 'el-icon-user', badge: 0 },
  { id: 'workbench', name: '工作台', icon: 'el-icon-menu', badge: 0 },
  { id: 'drive', name: '云盘', icon: 'el-icon-folder-opened', badge: 0 },
  { id: 'calendar', name: '日历', icon: 'el-icon-calendar', badge: 0 },
  { id: 'todo', name: '待办', icon: 'el-icon-tickets', badge: 2 },
  { id: 'approval', name: '审批', icon: 'el-icon-document-checked', badge: 0 }
];

const switchModule = (moduleId) => {
  emit('switch-module', moduleId);
};

const openProfile = () => {};
</script>

<style scoped>
.side-nav {
  width: 68px;
  height: 100%;
  /* 核心：引用 MainPage.vue 定义的侧边栏背景色 */
  background-color: var(--dt-bg-sidebar); 
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  flex-shrink: 0;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
}

.nav-top {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nav-item {
  position: relative;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  /* 核心：引用侧边栏专用图标色 */
  color: var(--dt-sb-icon); 
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-item i {
  font-size: 24px;
}

.nav-item:hover {
  background-color: var(--dt-sb-item-hover);
  color: #fff;
}

.nav-item.active {
  background-color: rgba(255, 255, 255, 0.15);
  /* 核心：选中态设为纯白，确保高对比 */
  color: var(--dt-sb-icon-active); 
}

/* 钉钉经典 3px 呼吸蓝条 */
.nav-item.active::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 12px;
  bottom: 12px;
  width: 3px;
  background-color: var(--dt-brand-color);
  border-radius: 0 4px 4px 0;
}

.nav-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background-color: #FF4D4F;
  color: #fff;
  font-size: 10px;
  height: 16px;
  min-width: 16px;
  padding: 0 4px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  box-shadow: 0 0 0 2px var(--dt-bg-sidebar);
}

.nav-bottom {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
}

.online-status-dot {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 10px;
  height: 10px;
  background-color: #52C41A;
  border: 2px solid var(--dt-bg-sidebar);
  border-radius: 50%;
}
</style>
