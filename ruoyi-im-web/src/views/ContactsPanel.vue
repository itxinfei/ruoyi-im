<template>
  <div class="contacts-panel-v3">
    <!-- 1. 中栏：导航与组织树 (对齐钉钉 280px) -->
    <aside class="contacts-sidebar">
      <header class="sidebar-header">
        <div class="search-box">
          <el-icon><Search /></el-icon>
          <input v-model="sidebarSearch" placeholder="搜索联系人、部门" />
        </div>
      </header>

      <div class="sidebar-body custom-scrollbar">
        <!-- 核心导航 (极简彩色线性风格) -->
        <nav class="nav-group">
          <div 
            v-for="item in navItems" 
            :key="item.id" 
            class="nav-item"
            :class="{ active: view === item.id }"
            @click="switchView(item.id)"
          >
            <div class="icon-wrapper" :style="{ color: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <span class="label">{{ item.label }}</span>
          </div>
        </nav>

        <div class="section-divider">组织架构</div>

        <!-- 组织树区 -->
        <div class="org-tree-section">
          <OrganizationTree @select="handleDeptSelect" />
        </div>
      </div>
    </aside>

    <!-- 2. 右侧主视图 (对齐钉钉：工业灰背景 + 纯白卡片) -->
    <main class="contacts-main">
      <!-- 统一头部面包屑 (56px) -->
      <header v-if="view === 'dept' || breadcrumbs.length" class="main-header">
        <div class="breadcrumb-bar">
          <span class="root-node" @click="handleBreadcrumbClick(null)">公司通讯录</span>
          <template v-for="node in breadcrumbs" :key="node.id">
            <el-icon class="sep"><ArrowRight /></el-icon>
            <span class="node" @click="handleBreadcrumbClick(node)">{{ node.name }}</span>
          </template>
        </div>
        <el-button type="primary" size="small" plain :icon="Plus">邀请成员</el-button>
      </header>

      <div class="content-scroller custom-scrollbar">
        <template v-if="view === 'dept'">
          <!-- 部门网格 (纯白文件夹卡片) -->
          <div v-if="subDepartments.length" class="dept-grid">
            <div v-for="dept in subDepartments" :key="dept.id" class="dept-card" @click="handleDeptSelect(dept)">
              <div class="folder-icon-box"><el-icon><FolderOpened /></el-icon></div>
              <div class="folder-info">
                <div class="name">{{ dept.name }}</div>
                <div class="count">{{ dept.memberCount || 0 }} 人</div>
              </div>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>

          <!-- 成员网格 (纯白成员卡片) -->
          <div v-if="deptMembers.length" class="member-grid-header">直属成员 ({{ deptMembers.length }})</div>
          <div class="members-grid">
            <div v-for="m in deptMembers" :key="m.id" class="member-card-v3" @click="handleMemberClick(m)">
              <DingtalkAvatar :src="m.avatar" :name="m.name" :size="40" shape="square" />
              <div class="member-info">
                <div class="m-name">{{ m.name || m.userName }}</div>
                <div class="m-post">{{ m.position || '成员' }}</div>
              </div>
              <button class="chat-btn" @click.stop="handleChat(m)"><el-icon><ChatDotRound /></el-icon></button>
            </div>
          </div>
        </template>
        
        <!-- 其他子模块视图 -->
        <component :is="activeSubView" v-else @select-member="handleMemberClick" />
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import { Search, Plus, UserFilled, Menu, StarFilled, Link, Clock, FolderOpened, ArrowRight, ChatDotRound } from '@element-plus/icons-vue'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const view = ref('dept')
const sidebarSearch = ref('')
const breadcrumbs = ref([])
const subDepartments = ref([])
const deptMembers = ref([])

const navItems = [
  { id: 'new', label: '新的联系人', icon: UserFilled, color: '#ff9800' },
  { id: 'groups', label: '我的群组', icon: Menu, color: '#277efb' },
  { id: 'friends', label: '我的好友', icon: StarFilled, color: '#22ab5c' },
  { id: 'external', label: '外部联系人', icon: Link, color: '#722ed1' },
  { id: 'frequent', label: '常用联系人', icon: Clock, color: '#13c2c2' }
]

const switchView = (v) => view.value = v
const handleDeptSelect = (dept) => { /* 加载逻辑 */ }
const handleBreadcrumbClick = (n) => { /* 跳转逻辑 */ }
const handleMemberClick = (m) => { /* 详情逻辑 */ }
</script>

<style scoped lang="scss">
.contacts-panel-v3 {
  display: flex; height: 100%; background: #fff; overflow: hidden;
}

// 1. 中栏 (280px)
.contacts-sidebar {
  width: 280px; background: #fff; border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column; flex-shrink: 0;
  
  .sidebar-header {
    height: 56px; padding: 0 12px; @include flex-center; border-bottom: 1px solid rgba(0,0,0,0.05);
    .search-box {
      width: 100%; height: 32px; background: var(--dt-bg-hover); border-radius: 6px;
      display: flex; align-items: center; padding: 0 10px; gap: 8px;
      input { border: none; background: transparent; outline: none; font-size: 13px; flex: 1; }
      .el-icon { color: var(--dt-text-tertiary); font-size: 16px; }
    }
  }
}

.sidebar-body { flex: 1; overflow-y: auto; padding: 12px 8px; }

.nav-item {
  height: 44px; display: flex; align-items: center; padding: 0 12px; gap: 12px;
  border-radius: 8px; cursor: pointer; transition: 0.1s; margin-bottom: 2px;
  &:hover { background: var(--dt-bg-hover); }
  &.active { background: var(--dt-brand-bg); .label { color: var(--dt-brand-color); font-weight: 600; } }
  
  .icon-wrapper { font-size: 18px; @include flex-center; width: 28px; }
  .label { font-size: 14px; color: var(--dt-text-primary); }
}

.section-divider {
  font-size: 11px; color: var(--dt-text-tertiary); font-weight: 600;
  padding: 24px 12px 12px; text-transform: uppercase;
}

// 2. 右侧主区 (核心：工业灰背景)
.contacts-main {
  flex: 1; display: flex; flex-direction: column; background: #f2f2f2; min-width: 0;
}

.main-header {
  height: 56px; padding: 0 24px; background: #fff; border-bottom: 1px solid var(--dt-border-light);
  @include flex-between;
  .breadcrumb-bar {
    display: flex; align-items: center; font-size: 14px; color: var(--dt-text-secondary);
    .root-node, .node { cursor: pointer; &:hover { color: var(--dt-brand-color); } }
    .root-node { color: var(--dt-text-tertiary); }
    .sep { margin: 0 8px; font-size: 12px; color: var(--dt-text-quaternary); }
  }
}

.content-scroller { flex: 1; padding: 24px; overflow-y: auto; }

// 纯白文件夹卡片
.dept-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; margin-bottom: 32px;
}
.dept-card {
  background: #fff; padding: 16px 20px; border-radius: 12px; border: 1px solid var(--dt-border-light);
  display: flex; align-items: center; gap: 16px; cursor: pointer; transition: 0.2s;
  &:hover { border-color: var(--dt-brand-light); box-shadow: var(--dt-shadow-1); .arrow-icon { opacity: 1; } }
  
  .folder-icon-box { width: 44px; height: 44px; background: #eef5fe; color: var(--dt-brand-color); border-radius: 10px; @include flex-center; font-size: 20px; }
  .folder-info { flex: 1; .name { font-size: 15px; font-weight: 600; color: var(--dt-text-primary); } .count { font-size: 12px; color: var(--dt-text-tertiary); margin-top: 2px; } }
  .arrow-icon { font-size: 14px; color: var(--dt-text-quaternary); opacity: 0; }
}

.member-grid-header { font-size: 14px; font-weight: 700; color: var(--dt-text-secondary); margin-bottom: 16px; }

.members-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px;
}

// 纯白成员卡片
.member-card-v3 {
  background: #fff; padding: 14px 16px; border-radius: 10px; border: 1px solid var(--dt-border-light);
  display: flex; align-items: center; gap: 12px; cursor: pointer; transition: 0.2s;
  &:hover { border-color: var(--dt-brand-color); .chat-btn { opacity: 1; } }
  
  .member-info { flex: 1; min-width: 0; .m-name { font-size: 14px; font-weight: 600; color: var(--dt-text-primary); } .m-post { font-size: 12px; color: var(--dt-text-tertiary); margin-top: 2px; } }
  .chat-btn { width: 32px; height: 32px; background: var(--dt-brand-bg); color: var(--dt-brand-color); border: none; border-radius: 50%; opacity: 0; transition: 0.2s; cursor: pointer; @include flex-center; &:hover { background: var(--dt-brand-color); color: #fff; } }
}
</style>
