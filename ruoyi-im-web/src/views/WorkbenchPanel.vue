<template>
  <div class="workbench-premium-v3">
    <!-- 1. 左侧二级导航 (220px) -->
    <aside class="wb-sidebar-v3">
      <div class="sidebar-header">工作台</div>
      <nav class="sidebar-nav">
        <div 
          v-for="m in menus" :key="m.id" 
          class="nav-item" :class="{ active: activeMenu === m.id }"
          @click="activeMenu = m.id"
        >
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </div>
      </nav>
    </aside>

    <!-- 2. 主体区 (工业灰背景) -->
    <main class="wb-main-v3 custom-scrollbar">
      <div class="wb-scroll-content">
        <!-- 玻璃拟态 Banner -->
        <section class="premium-banner">
          <div class="banner-info">
            <h1 class="greeting">{{ greeting }}，{{ userName }}</h1>
            <p class="date-desc">{{ currentDate }}</p>
          </div>
          <div class="banner-stats">
            <div class="stat-box" @click="$emit('switch-module', 'todo')">
              <span class="num">{{ stats.todoCount }}</span>
              <span class="lab">待办事项</span>
            </div>
            <div class="v-divider"></div>
            <div class="stat-box" @click="$emit('switch-module', 'ding')">
              <span class="num">{{ stats.dingCount }}</span>
              <span class="lab">未读 DING</span>
            </div>
          </div>
        </section>

        <!-- 最近使用 (横向滚动) -->
        <section class="recent-apps">
          <div class="section-title">最近使用</div>
          <div class="recent-grid">
            <div v-for="app in recentApps" :key="app.id" class="recent-item" @click="handleAppClick(app)">
              <div class="app-icon-circle" :style="{ background: app.color }">
                <el-icon><component :is="app.icon" /></el-icon>
              </div>
              <span class="app-name">{{ app.name }}</span>
            </div>
          </div>
        </section>

        <!-- 全量应用 (8px 栅格) -->
        <section class="all-apps-section">
          <div class="group-header">
            <span class="g-title">基础协同</span>
          </div>
          <div class="app-grid-v3">
            <div v-for="app in baseApps" :key="app.id" class="app-card-v3" @click="handleAppClick(app)">
              <div class="card-icon" :style="{ color: app.color, background: app.bgColor }">
                <el-icon><component :is="app.icon" /></el-icon>
              </div>
              <div class="card-info">
                <div class="name">{{ app.name }}</div>
                <div class="desc">{{ app.desc }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import {
  Files, Finished, Clock, Notebook, Calendar,
  Promotion, Tickets, Management, FolderOpened
} from '@element-plus/icons-vue'

const store = useStore()
const activeMenu = ref('center')
const stats = ref({ todoCount: 8, dingCount: 2 })

// 用户名（响应式，从 store 读取）
const userName = computed(() => {
  return store.state.user?.userInfo?.nickName
    || store.state.user?.userInfo?.userName
    || store.state.user?.name
    || '同事'
})

// 问候语（按时段切换）
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 · ${weekDays[now.getDay()]}`
})

const menus = [
  { id: 'center', label: '应用中心', icon: Files },
  { id: 'my', label: '我的应用', icon: Finished },
  { id: 'manage', label: '应用管理', icon: Management }
]

const recentApps = [
  { id: 1, name: '审批', icon: Finished, color: 'var(--dt-brand-color)' },
  { id: 2, name: '考勤', icon: Clock, color: 'var(--dt-success-color)' },
  { id: 3, name: '日志', icon: Promotion, color: 'var(--dt-warning-color)' }
]

const baseApps = [
  { id: 10, name: '智能报销', desc: '快速提交差旅费', icon: Tickets, color: 'var(--dt-brand-color)', bgColor: 'var(--dt-brand-bg)' },
  { id: 11, name: '人事自助', desc: '请假、入职、证明', icon: Management, color: 'var(--dt-purple-color)', bgColor: 'var(--dt-purple-bg)' },
  { id: 12, name: '资产申领', desc: '办公用品领用', icon: FolderOpened, color: 'var(--dt-cyan-color)', bgColor: 'var(--dt-cyan-bg)' }
]

const handleAppClick = (app) => {}
</script>

<style scoped lang="scss">
.workbench-premium-v3 { display: flex; height: 100%; background: var(--dt-bg-card); overflow: hidden; }

.wb-sidebar-v3 {
  width: 240px; background: var(--dt-bg-body); border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
  .sidebar-header { height: 56px; padding: 0 20px; @include flex-center; justify-content: flex-start; font-size: 16px; font-weight: 700; }
  .sidebar-nav { padding: 10px 8px; .nav-item { height: 40px; padding: 0 12px; display: flex; align-items: center; gap: 12px; border-radius: var(--dt-radius-lg); cursor: pointer; color: var(--dt-text-secondary); transition: var(--dt-transition-fast); &.active { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: 600; } &:hover:not(.active) { background: var(--dt-bg-hover); } } }
}

.wb-main-v3 { flex: 1; overflow-y: auto; background: var(--dt-bg-body); }
.wb-scroll-content { padding: 24px 32px; max-width: 1100px; margin: 0 auto; }

.premium-banner {
  height: 160px; padding: 0 40px; margin-bottom: 32px; border-radius: var(--dt-radius-xl);
  background: var(--dt-brand-color);
  display: flex; align-items: center; justify-content: space-between; color: var(--dt-text-white);

  .greeting { font-size: 24px; font-weight: 700; margin-bottom: 8px; color: var(--dt-text-white); }
  .date-desc { font-size: 14px; opacity: 0.8; color: var(--dt-text-white); }

  .banner-stats {
    display: flex; align-items: center; gap: 32px; padding: 16px 28px;
    background: rgba(255, 255, 255, 0.15); border-radius: var(--dt-radius-lg); border: 1px solid rgba(255,255,255,0.2);
    .stat-box { text-align: center; cursor: pointer; .num { font-size: 22px; font-weight: 700; display: block; color: var(--dt-text-white); } .lab { font-size: 11px; opacity: 0.8; color: var(--dt-text-white); } }
    .v-divider { width: 1px; height: 30px; background: rgba(255,255,255,0.2); }
  }
}

.section-title { font-size: 15px; font-weight: 700; color: var(--dt-text-primary); margin-bottom: 16px; }

.recent-apps {
  margin-bottom: 32px;
  .recent-grid { display: flex; gap: 24px; }
  .recent-item {
    display: flex; flex-direction: column; align-items: center; gap: 10px; cursor: pointer;
    /* 钉钉规范：禁止超过 12px 圆角，改为 8px */
    .app-icon-circle { width: 48px; height: 48px; border-radius: var(--dt-radius-lg); @include flex-center; color: var(--dt-text-white); font-size: 24px; transition: var(--dt-transition-fast); }
    .app-name { font-size: 12px; color: var(--dt-text-secondary); }
  }
}

.app-grid-v3 {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px;
}
.app-card-v3 {
  background: var(--dt-bg-card); padding: 16px; border-radius: var(--dt-radius-lg); border: 1px solid transparent;
  display: flex; align-items: center; gap: 16px; cursor: pointer; transition: var(--dt-transition-fast);
  &:hover { border-color: var(--dt-brand-color); box-shadow: var(--dt-shadow-2); }

  /* 钉钉规范：禁止超过 12px 圆角，改为 8px */
  .card-icon { width: 44px; height: 44px; border-radius: var(--dt-radius-lg); @include flex-center; font-size: 20px; }
  .card-info { .name { font-size: 14px; font-weight: 600; color: var(--dt-text-primary); } .desc { font-size: 11px; color: var(--dt-text-quaternary); margin-top: 2px; } }
}
</style>
