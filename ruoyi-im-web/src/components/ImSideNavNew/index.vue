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
  width: 68px; height: 100%; background: #1e1e1e; display: flex; flex-direction: column;
  align-items: center; padding: 12px 0; flex-shrink: 0; z-index: 2000;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.l1-header { margin-bottom: 16px; .logo-box { width: 34px; height: 34px; background: var(--dt-brand-gradient); border-radius: 8px; @include flex-center; color: #fff; font-size: 18px; } }

.l1-body { flex: 1; width: 100%; display: flex; flex-direction: column; gap: 2px; }

.nav-v4-item-wrap {
  position: relative; width: 100%; height: 54px; display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: 0.2s;
  .indicator { position: absolute; left: 0; width: 3px; height: 16px; background: var(--dt-brand-color); border-radius: 0 2px 2px 0; opacity: 0; transform: scaleY(0.5); transition: 0.2s; }
  .nav-v4-content { display: flex; flex-direction: column; align-items: center; gap: 2px; color: rgba(255,255,255,0.6); .icon { font-size: 22px; } .label { font-size: 10px; transform: scale(0.9); } }
  &:hover { background: rgba(255,255,255,0.05); .nav-v4-content { color: #fff; } }
  &.active { .indicator { opacity: 1; transform: scaleY(1); } .nav-v4-content { color: var(--dt-brand-color); font-weight: 600; } background: rgba(255,255,255,0.08); }
}

.v4-badge { position: absolute; top: 8px; right: 12px; background: #ff4d4f; color: #fff; font-size: 10px; height: 14px; min-width: 14px; border-radius: 7px; @include flex-center; padding: 0 4px; border: 1.5px solid #1e1e1e; transform: scale(0.8); }

/* 🏁 纠偏：侧栏底部功能区 */
.l1-footer {
  width: 100%; display: flex; flex-direction: column; align-items: center; gap: 14px; padding-bottom: 8px;
  
  .footer-action {
    width: 40px; height: 40px; @include flex-center; color: rgba(255,255,255,0.6);
    border-radius: 8px; cursor: pointer; font-size: 20px; transition: 0.2s;
    &:hover { background: rgba(255,255,255,0.05); color: #fff; }
    &.active { color: var(--dt-brand-color); background: rgba(255,255,255,0.08); }
  }
}

.v4-profile-trigger {
  position: relative; width: 36px; height: 36px; cursor: pointer;
  .avatar-container { position: relative; width: 100%; height: 100%; 
    .main-avatar { width: 100%; height: 100%; border-radius: 8px; background: #333; object-fit: cover; }
    .status-ring { position: absolute; bottom: -2px; right: -2px; width: 10px; height: 10px; border-radius: 50%; border: 2px solid #1e1e1e;
      &.online { background: #22ab5c; box-shadow: 0 0 0 rgba(34, 171, 92, 0.4); animation: status-pulse 2s infinite; }
    }
  }
}

@keyframes status-pulse { 0% { box-shadow: 0 0 0 0 rgba(34, 171, 92, 0.7); } 70% { box-shadow: 0 0 0 6px rgba(34, 171, 92, 0); } 100% { box-shadow: 0 0 0 0 rgba(34, 171, 92, 0); } }
</style>

<style lang="scss">
/* 🏁 全局纠偏：侧栏专用暗色下拉菜单 */
.dt-l1-dropdown-v4 {
  background: #2d2d2d !important; border: 1px solid rgba(255,255,255,0.08) !important;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3) !important;
  .el-dropdown-menu__item {
    color: rgba(255,255,255,0.85) !important; font-size: 13px !important;
    &:hover { background: rgba(255,255,255,0.05) !important; color: var(--dt-brand-color) !important; }
    &.is-divided { border-top-color: rgba(255,255,255,0.05) !important; }
  }
  .el-popper__arrow::before { background: #2d2d2d !important; border-color: rgba(255,255,255,0.08) !important; }
}
</style>
