<template>
  <div class="workbench-viewport custom-scrollbar">
    <!-- 1. 顶部纯净欢迎区 (飞书呼吸感) -->
    <header class="wb-header">
      <div class="wb-header__content">
        <div class="greeting-box">
          <h1 class="greeting-box__title">{{ greetingText }}，{{ displayName }}</h1>
          <p class="greeting-box__date">{{ currentDateText }}</p>
        </div>
        <div class="wb-header__actions">
          <div class="wb-search">
            <el-icon class="wb-search__icon"><Search /></el-icon>
            <input v-model="searchQuery" class="wb-search__input" placeholder="搜索应用、审批、日程..." />
          </div>
          <el-button :icon="Plus" type="primary" class="wb-action-btn">添加应用</el-button>
        </div>
      </div>
    </header>

    <!-- 2. 主体网格布局 -->
    <main class="wb-main">
      <!-- 2.1 核心应用区块 -->
      <section class="wb-section">
        <div class="wb-section__header">
          <h3 class="wb-section__title">常用应用</h3>
          <el-button link type="primary">管理</el-button>
        </div>
        <div class="app-grid">
          <div v-for="app in commonApps" :key="app.key" class="app-item" @click="handleAppClick(app)">
            <div class="app-item__icon" :class="app.iconClass">
              <el-icon><component :is="app.elIcon" /></el-icon>
            </div>
            <span class="app-item__label">{{ app.label }}</span>
            <span v-if="app.badge" class="app-item__badge">{{ app.badge }}</span>
          </div>
        </div>
      </section>

      <!-- 2.2 动态资讯区块 (两栏布局) -->
      <div class="wb-data-row">
        <!-- 待办面板 -->
        <div class="wb-card wb-card--todo">
          <div class="wb-card__header">
            <span class="wb-card__title"><el-icon><Calendar /></el-icon> 待办事项</span>
            <el-button link>全部</el-button>
          </div>
          <div class="wb-card__body">
            <div v-if="todos.length > 0" class="todo-stack">
              <div v-for="todo in todos" :key="todo.id" class="todo-tile" @click="handleTodoClick(todo)">
                <div class="todo-tile__status" :class="todo.priorityClass"></div>
                <div class="todo-tile__info">
                  <p class="todo-tile__title">{{ todo.title }}</p>
                  <span class="todo-tile__meta">{{ formatDate(todo.deadline) }}</span>
                </div>
                <el-checkbox size="large" @click.stop="handleTodoComplete(todo)" />
              </div>
            </div>
            <el-empty v-else :image-size="60" description="没有待办" />
          </div>
        </div>

        <!-- 内部公告 -->
        <div class="wb-card wb-card--notice">
          <div class="wb-card__header">
            <span class="wb-card__title"><el-icon><Notification /></el-icon> 公司公告</span>
            <el-button link>更多</el-button>
          </div>
          <div class="wb-card__body">
            <div v-if="announcements.length > 0" class="notice-stack">
              <div v-for="item in announcements" :key="item.id" class="notice-tile" @click="handleAnnouncementClick(item)">
                <span class="notice-tile__tag" :class="item.tagClass">{{ item.tag }}</span>
                <div class="notice-tile__info">
                  <p class="notice-tile__title">{{ item.title }}</p>
                  <span class="notice-tile__meta">{{ item.department }} · {{ item.time }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else :image-size="60" description="暂无公告" />
          </div>
        </div>
      </div>

      <!-- 2.3 扩展应用区块 -->
      <section class="wb-section">
        <div class="wb-section__header">
          <h3 class="wb-section__title">全量中心</h3>
        </div>
        <div class="app-grid app-grid--sm">
          <div v-for="app in otherApps" :key="app.key" class="app-item app-item--sm" @click="handleAppClick(app)">
            <div class="app-item__icon app-item__icon--sm" :class="app.iconClass">
              <el-icon><component :is="app.elIcon" /></el-icon>
            </div>
            <span class="app-item__label">{{ app.label }}</span>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { 
  Search, Plus, Calendar, Notification, 
  Timer, Tickets, Management, Finished, Briefcase,
  Money, FolderOpened, Trophy, HelpFilled, ChatLineRound, VideoPlay
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const store = useStore()
const searchQuery = ref('')
const currentUser = computed(() => store.getters['user/currentUser'] || {})
const displayName = computed(() => currentUser.value.nickname || currentUser.value.username || '钉钉成员')

// 问候与日期逻辑 (保持原逻辑)
const greetingText = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return '早上好'; if (h < 18) return '下午好'; return '晚上好'
})
const currentDateText = computed(() => new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }))

// 100% 变量驱动应用定义
const commonApps = ref([
  { key: 'punch', label: '签到打卡', elIcon: Timer, iconClass: 'icon-orange' },
  { key: 'flow', label: '审批流', elIcon: Finished, iconClass: 'icon-blue' },
  { key: 'report', label: '周报/日报', elIcon: Tickets, iconClass: 'icon-green', badge: 1 },
  { key: 'task', label: '协作待办', elIcon: Management, iconClass: 'icon-purple' },
  { key: 'meeting', label: '视频会议', elIcon: VideoPlay, iconClass: 'icon-pink' }
])

const otherApps = ref([
  { key: 'finance', label: '财务报销', elIcon: Money, iconClass: 'icon-teal' },
  { key: 'disk', label: '企业网盘', elIcon: FolderOpened, iconClass: 'icon-indigo' },
  { key: 'culture', label: '荣誉激励', elIcon: Trophy, iconClass: 'icon-amber' },
  { key: 'support', label: 'IT工单', elIcon: HelpFilled, iconClass: 'icon-rose' },
  { key: 'assistant', label: 'AI助手', elIcon: ChatLineRound, iconClass: 'icon-cyan' }
])

const todos = ref([
  { id: 1, title: '审核 Q4 前端架构方案', deadline: '今天 18:00', priorityClass: 'high' },
  { id: 2, title: '部门周例会 - 302 会议室', deadline: '明天 10:00', priorityClass: 'medium' }
])

const announcements = ref([
  { id: 1, title: '2026年春节放假及调休安排', department: '行政部', time: '1小时前', tag: '重要', tagClass: 'tag-red' },
  { id: 2, title: '关于全员接入钉钉 8.2 UI 标准的通知', department: 'CTO 办公室', time: '昨天', tag: '规范', tagClass: 'tag-blue' }
])

const handleAppClick = (app) => ElMessage.success(`正在进入: ${app.label}`)
const handleTodoClick = (todo) => ElMessage.info(`待办详情: ${todo.title}`)
const handleTodoComplete = (todo) => { ElMessage.success('待办已完成'); todos.value = todos.value.filter(t => t.id !== todo.id) }
const handleAnnouncementClick = (a) => ElMessage.info(`正在读取: ${a.title}`)
const formatDate = (d) => d
</script>

<style scoped lang="scss">
// ============================================================================
// 工作台全局视口 - 100% 变量血统
// ============================================================================
.workbench-viewport {
  height: 100%; flex: 1; min-width: 0; background: var(--dt-bg-body); overflow-y: auto;
}

// ============================================================================
// 头部欢迎区 - 飞书呼吸感 (白底无边框)
// ============================================================================
.wb-header {
  background: #ffffff; padding: var(--dt-spacing-2xl) 40px; 
  border-bottom: 1px solid var(--dt-border-light);
  &__content { max-width: 1200px; margin: 0 auto; @include flex-between; }
  
  .greeting-box {
    &__title { font-size: 26px; font-weight: 700; color: var(--dt-text-primary); margin: 0; letter-spacing: -0.5px; }
    &__date { font-size: var(--dt-font-size-base); color: var(--dt-text-tertiary); margin-top: 8px; }
  }

  &__actions { display: flex; align-items: center; gap: var(--dt-spacing-md); }
}

.wb-search {
  position: relative; width: 320px;
  &__icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--dt-text-tertiary); font-size: 16px; }
  &__input {
    width: 100%; height: 36px; padding: 0 16px 0 36px; background: var(--dt-bg-body);
    border: 1px solid transparent; border-radius: var(--dt-radius-md); outline: none;
    font-size: 14px; transition: all var(--dt-transition-base);
    &:focus { background: #fff; border-color: var(--dt-brand-color); box-shadow: var(--dt-shadow-1); }
  }
}

// ============================================================================
// 主内容区 - 8pt 栅格
// ============================================================================
.wb-main {
  max-width: 1200px; margin: 0 auto; padding: var(--dt-spacing-xl) 40px var(--dt-spacing-2xl);
  display: flex; flex-direction: column; gap: var(--dt-spacing-2xl);
}

.wb-section {
  &__header { @include flex-between; margin-bottom: var(--dt-spacing-lg); }
  &__title { font-size: 16px; font-weight: 600; color: var(--dt-text-primary); margin: 0; }
}

// ============================================================================
// 应用网格 - 工业级质感
// ============================================================================
.app-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: var(--dt-spacing-lg);
  &--sm { grid-template-columns: repeat(auto-fill, minmax(80px, 1fr)); gap: var(--dt-spacing-md); }
}

.app-item {
  @include flex-center; flex-direction: column; gap: 10px; cursor: pointer; padding: var(--dt-spacing-md) 0;
  border-radius: var(--dt-radius-md); transition: all var(--dt-transition-base); position: relative;
  
  &:hover { 
    background: var(--dt-bg-session-hover); transform: translateY(-2px);
    .app-item__icon { box-shadow: var(--dt-shadow-3); transform: scale(1.05); }
  }

  &__icon {
    width: 52px; height: 52px; border-radius: 12px; @include flex-center; font-size: 24px;
    background: #fff; box-shadow: var(--dt-shadow-1); transition: all var(--dt-transition-base);
    &--sm { width: 44px; height: 44px; font-size: 20px; }
    
    &.icon-orange { background: #fff7e6; color: #fa8c16; }
    &.icon-blue { background: #e6f4ff; color: #1677ff; }
    &.icon-green { background: #f6ffed; color: #52c41a; }
    &.icon-purple { background: #f9f0ff; color: #722ed1; }
    &.icon-pink { background: #fff0f6; color: #eb2f96; }
    &.icon-teal { background: #e6fffa; color: #13c2c2; }
    &.icon-indigo { background: #eef0ff; color: #594efc; }
    &.icon-amber { background: #fffbe6; color: #faad14; }
    &.icon-rose { background: #fff1f0; color: #f74a5c; }
    &.icon-cyan { background: #e6fffe; color: #08bdb2; }
  }

  &__label { font-size: 13px; color: var(--dt-text-primary); font-weight: 500; }
  &__badge { 
    position: absolute; top: 10px; right: 20px; background: var(--dt-error-color);
    color: #fff; font-size: 10px; min-width: 16px; height: 16px; border-radius: 8px;
    @include flex-center; border: 1.5px solid #fff; font-weight: 600;
  }
}

// ============================================================================
// 数据卡片 - 10px 圆角
// ============================================================================
.wb-data-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--dt-spacing-xl); }

.wb-card {
  background: var(--dt-bg-card); border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md); box-shadow: var(--dt-shadow-card);
  display: flex; flex-direction: column; overflow: hidden;
  &:hover { box-shadow: var(--dt-shadow-card-hover); }

  &__header { padding: var(--dt-spacing-lg) var(--dt-spacing-xl); border-bottom: 1px solid var(--dt-border-lighter); @include flex-between; }
  &__title { font-size: 15px; font-weight: 600; color: var(--dt-text-primary); display: flex; align-items: center; gap: 8px; .el-icon { color: var(--dt-brand-color); } }
  &__body { padding: var(--dt-spacing-md) var(--dt-spacing-xl) var(--dt-spacing-xl); flex: 1; }
}

.todo-stack, .notice-stack { display: flex; flex-direction: column; gap: 8px; }

.todo-tile, .notice-tile {
  background: var(--dt-bg-body); padding: var(--dt-spacing-md); border-radius: 8px;
  cursor: pointer; transition: all var(--dt-transition-base); display: flex; align-items: center; gap: 12px;
  &:hover { background: var(--dt-bg-session-hover); transform: translateX(2px); }
  
  &__status { width: 4px; height: 32px; border-radius: 2px; &.high { background: var(--dt-error-color); } &.medium { background: var(--dt-warning-color); } &.low { background: var(--dt-brand-color); } }
  &__info { flex: 1; min-width: 0; }
  &__title { font-size: 14px; font-weight: 500; color: var(--dt-text-primary); @include text-ellipsis; }
  &__meta { font-size: 12px; color: var(--dt-text-tertiary); margin-top: 4px; display: block; }
}

.notice-tile {
  &__tag { padding: 2px 6px; font-size: 10px; border-radius: 4px; font-weight: 600;
    &.tag-red { background: var(--dt-error-bg); color: var(--dt-error-color); }
    &.tag-blue { background: var(--dt-brand-lighter); color: var(--dt-brand-color); }
  }
}
</style>
