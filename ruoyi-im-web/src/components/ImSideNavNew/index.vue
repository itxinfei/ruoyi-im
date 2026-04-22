<template>
  <aside class="dt-premium-l1">
    <!-- 1. 顶部 Logo -->
    <div class="l1-header">
      <div class="logo-box">
        <el-icon><Promotion /></el-icon>
      </div>
    </div>

    <!-- 2. 中间主导航 (已对齐) -->
    <div class="l1-body custom-scrollbar">
      <div 
        v-for="item in topNavs" 
        :key="item.id" 
        class="nav-v4-item-wrap"
        :class="{ active: activeModule === item.id }"
        @click="$emit('switch-module', item.id)"
      >
        <div class="indicator"></div>
        <div class="nav-v4-content">
          <el-icon class="icon">
            <component :is="activeModule === item.id ? item.activeIcon : item.icon" />
          </el-icon>
          <span class="label">{{ item.name }}</span>
        </div>
        <div v-if="item.badge" class="v4-badge">{{ item.badge }}</div>
      </div>
    </div>

    <!-- 3. 🏁 重点纠偏：底部功能菜单 (对齐钉钉 8.2 Premium) -->
    <div class="l1-footer">
      <!-- 搜索：移至底部，带高亮态 -->
      <div class="footer-action" :class="{ active: activeModule === 'search' }" @click="$emit('switch-module', 'search')">
        <el-icon><Search /></el-icon>
      </div>
      
      <!-- 更多：定制暗色系弹出菜单 -->
      <el-dropdown trigger="click" placement="right-end" popper-class="dt-l1-dropdown-v4">
        <div class="footer-action">
          <el-icon><MoreFilled /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item :icon="Setting">系统设置</el-dropdown-item>
            <el-dropdown-item :icon="Connection">网络检测</el-dropdown-item>
            <el-dropdown-item :icon="InfoFilled" divided>关于 RuoYi-IM</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 个人头像：带呼吸感的状态环 -->
      <div class="v4-profile-trigger" @click="$emit('open-edit-profile')">
        <div class="avatar-container">
          <img src="/avatars/me.svg" class="main-avatar">
          <!-- 在线状态：带呼吸动效 -->
          <div class="status-ring online"></div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="js">
import { markRaw } from 'vue'
import {
  ChatDotRound, User, Bell, Menu, Tickets, Calendar,
  FolderOpened, FolderChecked, Phone, Search, MoreFilled, Setting,
  InfoFilled, Promotion, ChatLineRound,
  UserFilled, BellFilled, Grid, List,
  PhoneFilled, Connection
} from '@element-plus/icons-vue'

defineProps({ activeModule: String })
const emit = defineEmits(['switch-module', 'open-edit-profile'])

const topNavs = [
  { id: 'chat', name: '消息', icon: markRaw(ChatLineRound), activeIcon: markRaw(ChatDotRound), badge: 5 },
  { id: 'contacts', name: '通讯录', icon: markRaw(User), activeIcon: markRaw(UserFilled) },
  { id: 'workbench', name: '工作台', icon: markRaw(Menu), activeIcon: markRaw(Grid) },
  { id: 'todo', name: '待办', icon: markRaw(Tickets), activeIcon: markRaw(List), badge: 2 },
  { id: 'documents', name: '云盘', icon: markRaw(FolderOpened), activeIcon: markRaw(FolderChecked) }
]
</script>

<style scoped lang="scss">
.dt-premium-l1 {
  width: var(--dt-nav-sidebar-width, 64px); height: 100%; background: var(--dt-nav-sidebar-bg); display: flex; flex-direction: column;
  align-items: center; padding: var(--dt-spacing-md) 0; flex-shrink: 0; z-index: 2000;
  border-right: 1px solid var(--dt-border-subtle, rgba(255, 255, 255, 0.05));
}

.l1-header { margin-bottom: var(--dt-spacing-md); .logo-box { width: 34px; height: 34px; background: var(--dt-brand-gradient); border-radius: var(--dt-radius-lg); @include flex-center; color: #fff; font-size: 18px; } }

.l1-body { flex: 1; width: 100%; display: flex; flex-direction: column; gap: 2px; }

.nav-v4-item-wrap {
  position: relative; width: 100%; height: 48px; display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: var(--dt-transition-fast);
  .indicator { position: absolute; left: 0; width: 3px; height: 16px; background: var(--dt-brand-color); border-radius: 0 2px 2px 0; opacity: 0; transition: var(--dt-transition-fast); }
  .nav-v4-content { display: flex; flex-direction: column; align-items: center; gap: 2px; color: var(--dt-text-quaternary, rgba(255,255,255,0.6)); .icon { font-size: 20px; } .label { font-size: 10px; } }
  &:hover { background: var(--dt-nav-hover, rgba(255,255,255,0.05)); .nav-v4-content { color: var(--dt-text-primary, #fff); } }
  &.active { .indicator { opacity: 1; } .nav-v4-content { color: var(--dt-brand-color); font-weight: 600; } background: var(--dt-nav-active, rgba(255,255,255,0.08)); }
}

.v4-badge { position: absolute; top: 8px; right: 12px; background: var(--dt-error-color); color: var(--dt-text-white); font-size: 10px; height: 14px; min-width: 14px; border-radius: var(--dt-radius-sm); @include flex-center; padding: 0 4px; border: 1.5px solid var(--dt-nav-sidebar-bg); }

/* 侧栏底部功能区 */
.l1-footer {
  width: 100%; display: flex; flex-direction: column; align-items: center; gap: var(--dt-spacing-md); padding-bottom: var(--dt-spacing-sm);

  .footer-action {
    width: 40px; height: 40px; @include flex-center; color: var(--dt-text-quaternary, rgba(255,255,255,0.6));
    border-radius: var(--dt-radius-lg); cursor: pointer; font-size: 20px; transition: var(--dt-transition-fast);
    &:hover { background: var(--dt-nav-hover, rgba(255,255,255,0.05)); color: var(--dt-text-primary, #fff); }
    &.active { color: var(--dt-brand-color); background: var(--dt-nav-active, rgba(255,255,255,0.08)); }
  }
}

.v4-profile-trigger {
  position: relative; width: 36px; height: 36px; cursor: pointer;
  .avatar-container { position: relative; width: 100%; height: 100%;
    .main-avatar { width: 100%; height: 100%; border-radius: var(--dt-radius-lg); background: var(--dt-nav-sidebar-bg); object-fit: cover; }
    .status-ring { position: absolute; bottom: -2px; right: -2px; width: 10px; height: 10px; border-radius: 50%; border: 2px solid var(--dt-nav-sidebar-bg);
      &.online { background: var(--dt-success-color); }
    }
  }
}
</style>

<style lang="scss">
/* 🏁 全局纠偏：侧栏专用暗色下拉菜单 */
.dt-l1-dropdown-v4 {
  background: var(--dt-nav-sidebar-bg) !important; border: 1px solid rgba(255,255,255,0.08) !important;
  box-shadow: var(--dt-shadow-4) !important;
  .el-dropdown-menu__item {
    color: rgba(255,255,255,0.85) !important; font-size: 13px !important;
    &:hover { background: rgba(255,255,255,0.05) !important; color: var(--dt-brand-color) !important; }
    &.is-divided { border-top-color: rgba(255,255,255,0.05) !important; }
  }
  .el-popper__arrow::before { background: var(--dt-nav-sidebar-bg) !important; border-color: rgba(255,255,255,0.08) !important; }
}
</style>
