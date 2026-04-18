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
            <h1 class="greeting">下午好，架构师</h1>
            <p class="date-desc">2026年4月18日 · 星期六</p>
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
import { 
  Files, Finished, Clock, Notebook, Calendar, 
  Promotion, Tickets, Management, FolderOpened 
} from '@element-plus/icons-vue'

const activeMenu = ref('center')
const stats = ref({ todoCount: 8, dingCount: 2 })

const menus = [
  { id: 'center', label: '应用中心', icon: Files },
  { id: 'my', label: '我的应用', icon: Finished },
  { id: 'manage', label: '应用管理', icon: Management }
]

const recentApps = [
  { id: 1, name: '审批', icon: Finished, color: '#277efb' },
  { id: 2, name: '考勤', icon: Clock, color: '#22ab5c' },
  { id: 3, name: '日志', icon: Promotion, color: '#ff9800' }
]

const baseApps = [
  { id: 10, name: '智能报销', desc: '快速提交差旅费', icon: Tickets, color: '#277efb', bgColor: '#eef5fe' },
  { id: 11, name: '人事自助', desc: '请假、入职、证明', icon: Management, color: '#722ed1', bgColor: '#f9f0ff' },
  { id: 12, name: '资产申领', desc: '办公用品领用', icon: FolderOpened, color: '#13c2c2', bgColor: '#e6fffb' }
]

const handleAppClick = (app) => {}
</script>

<style scoped lang="scss">
.workbench-premium-v3 { display: flex; height: 100%; background: #fff; overflow: hidden; }

.wb-sidebar-v3 {
  width: 220px; background: #f8fbff; border-right: 1px solid rgba(0,0,0,0.05);
  display: flex; flex-direction: column;
  .sidebar-header { height: 56px; padding: 0 20px; @include flex-center; justify-content: flex-start; font-size: 16px; font-weight: 700; }
  .sidebar-nav { padding: 10px 8px; .nav-item { height: 40px; padding: 0 12px; display: flex; align-items: center; gap: 12px; border-radius: 8px; cursor: pointer; color: #555; transition: 0.2s; &.active { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: 600; } &:hover:not(.active) { background: #eff4fc; } } }
}

.wb-main-v3 { flex: 1; overflow-y: auto; background: #f2f2f2; }
.wb-scroll-content { padding: 24px 32px; max-width: 1100px; margin: 0 auto; }

.premium-banner {
  height: 160px; padding: 0 40px; margin-bottom: 32px; border-radius: 16px;
  background: linear-gradient(135deg, #277efb 0%, #4facfe 100%);
  display: flex; align-items: center; justify-content: space-between; color: #fff;
  box-shadow: 0 10px 30px rgba(39, 126, 251, 0.2);
  
  .greeting { font-size: 24px; font-weight: 700; margin-bottom: 8px; }
  .date-desc { font-size: 14px; opacity: 0.8; }
  
  .banner-stats {
    display: flex; align-items: center; gap: 32px; padding: 16px 28px;
    background: rgba(255, 255, 255, 0.12); backdrop-filter: blur(10px);
    border-radius: 12px; border: 1px solid rgba(255,255,255,0.2);
    .stat-box { text-align: center; cursor: pointer; transition: 0.2s; &:hover { transform: translateY(-2px); } .num { font-size: 22px; font-weight: 700; display: block; } .lab { font-size: 11px; opacity: 0.8; } }
    .v-divider { width: 1px; height: 30px; background: rgba(255,255,255,0.2); }
  }
}

.section-title { font-size: 15px; font-weight: 700; color: #1d1d1f; margin-bottom: 16px; }

.recent-apps {
  margin-bottom: 32px;
  .recent-grid { display: flex; gap: 24px; }
  .recent-item {
    display: flex; flex-direction: column; align-items: center; gap: 10px; cursor: pointer;
    .app-icon-circle { width: 48px; height: 48px; border-radius: 14px; @include flex-center; color: #fff; font-size: 24px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); transition: 0.2s; }
    &:hover .app-icon-circle { transform: translateY(-3px) scale(1.05); }
    .app-name { font-size: 12px; color: #555; }
  }
}

.app-grid-v3 {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px;
}
.app-card-v3 {
  background: #fff; padding: 16px; border-radius: 12px; border: 1px solid transparent;
  display: flex; align-items: center; gap: 16px; cursor: pointer; transition: 0.2s;
  &:hover { border-color: var(--dt-brand-color); box-shadow: var(--dt-shadow-2); }
  
  .card-icon { width: 44px; height: 44px; border-radius: 10px; @include flex-center; font-size: 20px; }
  .card-info { .name { font-size: 14px; font-weight: 600; color: #1d1d1f; } .desc { font-size: 11px; color: #aaa; margin-top: 2px; } }
}
</style>
