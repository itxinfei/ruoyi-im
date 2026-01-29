<template>
  <div class="workbench-panel">
    <!-- 顶部欢迎区 -->
    <header class="workbench-header">
      <div class="greeting-section">
        <h1 class="greeting-title">{{ greetingText }}，{{ displayName }}</h1>
        <p class="greeting-date">{{ currentDateText }}</p>
      </div>
      <div class="header-actions">
        <div class="search-box">
          <span class="material-icons-outlined search-icon">search</span>
          <input
            v-model="searchQuery"
            class="search-input"
            placeholder="搜索应用、文档、联系人..."
            type="text"
          />
        </div>
        <div class="header-buttons">
          <button class="icon-btn" @click="toggleEditMode" :class="{ active: isEditMode }">
            <span class="material-icons-outlined">{{ isEditMode ? 'lock' : 'lock_open' }}</span>
            {{ isEditMode ? '完成' : '编辑' }}
          </button>
          <button class="custom-btn">
            <span class="material-icons-outlined">add</span>
            添加卡片
          </button>
        </div>
      </div>
    </header>

    <!-- Bento Grid 主内容区 -->
    <div class="workbench-content">
      <!-- 快捷应用区域 -->
      <section class="quick-apps-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon title-icon-primary">
              <span class="material-icons-outlined">apps</span>
            </span>
            快捷应用
          </h3>
        </div>
        <div class="quick-apps-grid">
          <div
            v-for="app in quickApps"
            :key="app.id"
            class="quick-app-item"
            @click="handleAppClick(app)"
          >
            <div class="app-icon" :class="app.iconClass">
              <span class="material-icons-outlined">{{ app.icon }}</span>
            </div>
            <span class="app-label">{{ app.label }}</span>
          </div>
          <div class="quick-app-item add-app" @click="handleAddApp">
            <div class="app-icon add-icon">
              <span class="material-icons-outlined">add</span>
            </div>
            <span class="app-label">添加</span>
          </div>
        </div>
      </section>

      <!-- Bento Grid 布局 -->
      <div class="bento-grid" :class="{ 'edit-mode': isEditMode }">
        <div
          v-for="card in visibleCards"
          :key="card.id"
          class="bento-card"
          :class="[
            `card-${card.type}`,
            `card-size-${card.size || '1x1'}`,
            { 'dragging': draggingCard === card.id }
          ]"
          :style="getCardStyle(card)"
          draggable="isEditMode"
          @dragstart="handleDragStart($event, card)"
          @dragend="handleDragEnd"
          @dragover="handleDragOver($event, card)"
          @drop="handleDrop($event, card)"
          @click="handleCardClick(card)"
        >
          <!-- 卡片内容组件 -->
          <component :is="getCardComponent(card.type)" :card="card" :data="card.data" />

          <!-- 编辑模式下的操作按钮 -->
          <div v-if="isEditMode" class="card-actions">
            <button class="action-btn" @click.stop="handleEditCard(card)" title="编辑">
              <span class="material-icons-outlined">edit</span>
            </button>
            <button class="action-btn danger" @click.stop="handleRemoveCard(card.id)" title="移除">
              <span class="material-icons-outlined">close</span>
            </button>
          </div>
        </div>

        <!-- 空插槽提示 -->
        <div v-if="visibleCards.length === 0" class="empty-grid">
          <span class="material-icons-outlined empty-icon">dashboard_customize</span>
          <p class="empty-text">暂无卡片，点击"添加卡片"开始定制你的工作台</p>
        </div>
      </div>
    </div>

    <!-- 添加卡片弹窗 -->
    <el-dialog
      v-model="showAddCardDialog"
      title="添加卡片"
      width="600px"
      :close-on-click-modal="true"
    >
      <div class="add-card-dialog">
        <div class="card-type-tabs">
          <button
            v-for="type in cardTypes"
            :key="type.key"
            class="type-tab"
            :class="{ active: selectedCardType === type.key }"
            @click="selectedCardType = type.key"
          >
            <span class="material-icons-outlined">{{ type.icon }}</span>
            {{ type.label }}
          </button>
        </div>
        <div class="card-templates">
          <div
            v-for="template in getCardTemplates(selectedCardType)"
            :key="template.id"
            class="template-item"
            :class="{ selected: selectedTemplate === template.id }"
            @click="selectedTemplate = template.id"
          >
            <div class="template-preview" :class="`preview-${template.size || '1x1'}`">
              <span class="material-icons-outlined">{{ template.icon }}</span>
              <span class="template-label">{{ template.label }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAddCardDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddCard">确定添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const store = useStore()

// ============================================================================
// 状态管理
// ============================================================================
const searchQuery = ref('')
const isEditMode = ref(false)
const draggingCard = ref(null)
const dragOverCard = ref(null)
const showAddCardDialog = ref(false)
const selectedCardType = ref('data')
const selectedTemplate = ref('')

// ============================================================================
// 用户信息
// ============================================================================
const currentUser = computed(() => store.getters['user/currentUser'] || {})
const displayName = computed(() => currentUser.value.nickname || currentUser.value.username || '开发者')

// 问候语
const greetingText = computed(() => {
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
const currentDateText = computed(() => {
  const now = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const date = now.getDate()
  const weekday = weekdays[now.getDay()]
  return `${year}年${month}月${date}日 ${weekday}`
})

// ============================================================================
// 快捷应用
// ============================================================================
const quickApps = ref([
  { id: 'qa-1', label: '考勤', icon: 'access_time', iconClass: 'icon-orange', key: 'attendance' },
  { id: 'qa-2', label: '审批', icon: 'approval', iconClass: 'icon-blue', key: 'approval' },
  { id: 'qa-3', label: '汇报', icon: 'assignment', iconClass: 'icon-green', key: 'report' },
  { id: 'qa-4', label: '待办', icon: 'task_alt', iconClass: 'icon-purple', key: 'todo', badge: 3 },
  { id: 'qa-5', label: '公告', icon: 'campaign', iconClass: 'icon-pink', key: 'announcement' },
  { id: 'qa-6', label: '会议', icon: 'videocam', iconClass: 'icon-cyan', key: 'meeting' },
  { id: 'qa-7', label: '云盘', icon: 'folder_shared', iconClass: 'icon-indigo', key: 'cloud' },
  { id: 'qa-8', label: '日程', icon: 'calendar_today', iconClass: 'icon-teal', key: 'calendar' }
])

// ============================================================================
// Bento Grid 卡片配置
// ============================================================================

// 默认卡片布局
const defaultCards = [
  {
    id: 'card-todo',
    type: 'todo',
    size: '1x2',
    position: { row: 0, col: 0 },
    data: { title: '待办事项', limit: 5 }
  },
  {
    id: 'card-announcement',
    type: 'announcement',
    size: '1x2',
    position: { row: 0, col: 1 },
    data: { title: '公司公告', limit: 3 }
  },
  {
    id: 'card-schedule',
    type: 'schedule',
    size: '2x1',
    position: { row: 1, col: 0 },
    data: { title: '今日日程' }
  },
  {
    id: 'card-stats',
    type: 'stats',
    size: '2x2',
    position: { row: 2, col: 0 },
    data: { title: '工作概览' }
  },
  {
    id: 'card-approval',
    type: 'approval',
    size: '1x1',
    position: { row: 4, col: 0 },
    data: { title: '待审批' }
  },
  {
    id: 'card-mail',
    type: 'mail',
    size: '1x1',
    position: { row: 4, col: 1 },
    data: { title: '未读邮件' }
  }
]

// 卡片数据
const cards = ref([])

// 从 localStorage 加载卡片配置
const loadCardsFromStorage = () => {
  const saved = localStorage.getItem('workbench-cards')
  if (saved) {
    try {
      cards.value = JSON.parse(saved)
    } catch (e) {
      console.error('加载卡片配置失败', e)
      cards.value = [...defaultCards]
    }
  } else {
    cards.value = [...defaultCards]
  }
}

// 保存卡片配置到 localStorage
const saveCardsToStorage = () => {
  localStorage.setItem('workbench-cards', JSON.stringify(cards.value))
}

// 可见卡片（按位置排序）
const visibleCards = computed(() => {
  return [...cards.value].sort((a, b) => {
    const rowA = a.position?.row || 0
    const rowB = b.position?.row || 0
    const colA = a.position?.col || 0
    const colB = b.position?.col || 0
    if (rowA !== rowB) return rowA - rowB
    return colA - colB
  })
})

// ============================================================================
// 卡片类型配置
// ============================================================================
const cardTypes = [
  { key: 'data', label: '数据卡片', icon: 'analytics' },
  { key: 'app', label: '应用卡片', icon: 'apps' },
  { key: 'chart', label: '图表卡片', icon: 'bar_chart' },
  { key: 'list', label: '列表卡片', icon: 'view_list' }
]

// 卡片模板
const cardTemplates = {
  data: [
    { id: 'todo', label: '待办事项', icon: 'task_alt', size: '1x2' },
    { id: 'announcement', label: '公司公告', icon: 'campaign', size: '1x2' },
    { id: 'schedule', label: '日程安排', icon: 'calendar_today', size: '2x1' },
    { id: 'approval', label: '待审批', icon: 'approval', size: '1x1' },
    { id: 'mail', label: '未读邮件', icon: 'mail', size: '1x1' },
    { id: 'stats', label: '工作概览', icon: 'analytics', size: '2x2' }
  ],
  app: [
    { id: 'cloud-app', label: '云盘快捷', icon: 'folder_shared', size: '1x1' },
    { id: 'meeting-app', label: '会议快捷', icon: 'videocam', size: '1x1' },
    { id: 'doc-app', label: '文档协作', icon: 'description', size: '1x1' }
  ],
  chart: [
    { id: 'weekly-chart', label: '周报统计', icon: 'show_chart', size: '2x2' },
    { id: 'project-chart', label: '项目进度', icon: 'pie_chart', size: '2x1' }
  ],
  list: [
    { id: 'recent-files', label: '最近文件', icon: 'recent_actors', size: '1x2' },
    { id: 'team-activity', label: '团队动态', icon: 'groups', size: '1x2' }
  ]
}

// 获取指定类型的卡片模板
const getCardTemplates = (type) => {
  return cardTemplates[type] || []
}

// ============================================================================
// 卡片组件渲染
// ============================================================================

// 卡片样式
const getCardStyle = (card) => {
  return {
    '--card-color': card.color || 'var(--dt-brand-color)',
    '--card-bg': card.bgColor || 'var(--dt-brand-bg)'
  }
}

// 获取卡片组件
const getCardComponent = (type) => {
  const components = {
    todo: BentoTodoCard,
    announcement: BentoAnnouncementCard,
    schedule: BentoScheduleCard,
    stats: BentoStatsCard,
    approval: BentoApprovalCard,
    mail: BentoMailCard,
    'default': BentoDefaultCard
  }
  return components[type] || components.default
}

// ============================================================================
// 卡片内容组件
// ============================================================================

// 待办卡片
const BentoTodoCard = {
  props: ['card', 'data'],
  setup(props) {
    const todos = ref([
      { id: 1, title: '完成Q4季度产品规划文档', deadline: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(), priority: 'high' },
      { id: 2, title: '新员工入职培训会议', deadline: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), priority: 'medium' },
      { id: 3, title: '审核前端团队代码合并请求', deadline: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString(), priority: 'low' }
    ])

    const formatDate = (date) => {
      const d = new Date(date)
      const now = new Date()
      const diff = d - now
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      if (days === 0) return '今天'
      if (days === 1) return '明天'
      return `${d.getMonth() + 1}月${d.getDate()}日`
    }

    return () => h('div', { class: 'bento-card-content todo-card-content' }, [
      h('div', { class: 'card-header' }, [
        h('h4', { class: 'card-title' }, [
          h('span', { class: 'material-icons-outlined card-icon' }, 'task_alt'),
          props.data?.title || '待办事项'
        ]),
        h('a', {
          class: 'view-all-link',
          href: '#',
          onClick: (e) => {
            e.preventDefault()
            window.dispatchEvent(new CustomEvent('switch-tab', { detail: 'todo' }))
          }
        }, '查看全部')
      ]),
      h('div', { class: 'todo-list' }, todos.value.slice(0, props.data?.limit || 5).map(todo =>
        h('div', {
          class: ['todo-item', `priority-${todo.priority}`],
          key: todo.id,
          onClick: () => ElMessage.info(`查看待办: ${todo.title}`)
        }, [
          h('span', { class: 'todo-dot' }),
          h('div', { class: 'todo-content' }, [
            h('p', { class: 'todo-title' }, todo.title),
            h('span', { class: 'todo-deadline' }, [
              h('span', { class: 'material-icons-outlined' }, 'schedule'),
              formatDate(todo.deadline)
            ])
          ])
        ])
      ))
    ])
  }
}

// 公告卡片
const BentoAnnouncementCard = {
  props: ['card', 'data'],
  setup(props) {
    const announcements = ref([
      { id: 1, title: '关于国庆节放假安排的通知', department: '人事行政部', time: '2小时前', tag: '重要' },
      { id: 2, title: '2023年度员工体检预约开启', department: '人事行政部', time: '昨天', tag: '通知' },
      { id: 3, title: '系统升级维护通知', department: '技术部', time: '3天前', tag: '通知' }
    ])

    return () => h('div', { class: 'bento-card-content announcement-card-content' }, [
      h('div', { class: 'card-header' }, [
        h('h4', { class: 'card-title' }, [
          h('span', { class: 'material-icons-outlined card-icon' }, 'campaign'),
          props.data?.title || '公司公告'
        ]),
        h('a', {
          class: 'view-all-link',
          href: '#',
          onClick: (e) => {
            e.preventDefault()
            ElMessage.info('查看全部公告')
          }
        }, '更多')
      ]),
      h('div', { class: 'announcement-list' }, announcements.value.slice(0, props.data?.limit || 3).map(item =>
        h('div', {
          class: 'announcement-item',
          key: item.id,
          onClick: () => ElMessage.info(`查看公告: ${item.title}`)
        }, [
          h('span', { class: 'announcement-tag' }, item.tag),
          h('div', { class: 'announcement-content' }, [
            h('p', { class: 'announcement-title' }, item.title),
            h('span', { class: 'announcement-meta' }, [
              h('span', { class: 'material-icons-outlined' }, 'business'),
              item.department,
              ' · ',
              h('span', { class: 'material-icons-outlined' }, 'access_time'),
              item.time
            ])
          ])
        ])
      ))
    ])
  }
}

// 日程卡片
const BentoScheduleCard = {
  props: ['card', 'data'],
  setup(props) {
    const schedules = ref([
      { id: 1, title: '产品需求评审会', time: '10:00-11:30', location: '会议室A' },
      { id: 2, title: '与设计团队沟通', time: '14:00-15:00', location: '线上会议' },
      { id: 3, title: '代码评审', time: '16:00-17:00', location: '会议室B' }
    ])

    return () => h('div', { class: 'bento-card-content schedule-card-content' }, [
      h('div', { class: 'card-header' }, [
        h('h4', { class: 'card-title' }, [
          h('span', { class: 'material-icons-outlined card-icon' }, 'calendar_today'),
          props.data?.title || '今日日程'
        ])
      ]),
      h('div', { class: 'schedule-list' }, schedules.value.map(item =>
        h('div', {
          class: 'schedule-item',
          key: item.id,
          onClick: () => ElMessage.info(`查看日程: ${item.title}`)
        }, [
          h('div', { class: 'schedule-time' }, item.time),
          h('div', { class: 'schedule-info' }, [
            h('p', { class: 'schedule-title' }, item.title),
            h('span', { class: 'schedule-location' }, [
              h('span', { class: 'material-icons-outlined' }, 'place'),
              item.location
            ])
          ])
        ])
      ))
    ])
  }
}

// 统计卡片
const BentoStatsCard = {
  props: ['card', 'data'],
  setup(props) {
    const stats = ref([
      { label: '今日待办', value: 5, total: 12, unit: '项', color: '#1677ff' },
      { label: '待审批', value: 3, total: null, unit: '项', color: '#52c41a' },
      { label: '未读消息', value: 28, total: null, unit: '条', color: '#fa8c16' },
      { label: '本周会议', value: 8, total: 15, unit: '场', color: '#722ed1' }
    ])

    return () => h('div', { class: 'bento-card-content stats-card-content' }, [
      h('div', { class: 'card-header' }, [
        h('h4', { class: 'card-title' }, [
          h('span', { class: 'material-icons-outlined card-icon' }, 'analytics'),
          props.data?.title || '工作概览'
        ])
      ]),
      h('div', { class: 'stats-grid' }, stats.value.map(stat =>
        h('div', {
          class: 'stat-item',
          key: stat.label,
          style: { '--stat-color': stat.color }
        }, [
          h('div', { class: 'stat-value' }, [
            h('span', { class: 'stat-number' }, stat.value),
            h('span', { class: 'stat-unit' }, stat.unit),
            stat.total !== null ? h('span', { class: 'stat-total' }, `/ ${stat.total}`) : null
          ]),
          h('div', { class: 'stat-label' }, stat.label)
        ])
      ))
    ])
  }
}

// 审批卡片
const BentoApprovalCard = {
  props: ['card', 'data'],
  setup(props) {
    return () => h('div', { class: 'bento-card-content approval-card-content' }, [
      h('div', { class: 'card-header compact' }, [
        h('span', { class: 'material-icons-outlined card-icon' }, 'approval'),
        h('span', { class: 'card-title' }, props.data?.title || '待审批')
      ]),
      h('div', { class: 'card-value' }, '3'),
      h('div', { class: 'card-action' }, [
        h('span', { class: 'action-text' }, '立即处理'),
        h('span', { class: 'material-icons-outlined' }, 'arrow_forward')
      ])
    ])
  }
}

// 邮件卡片
const BentoMailCard = {
  props: ['card', 'data'],
  setup(props) {
    return () => h('div', { class: 'bento-card-content mail-card-content' }, [
      h('div', { class: 'card-header compact' }, [
        h('span', { class: 'material-icons-outlined card-icon' }, 'mail'),
        h('span', { class: 'card-title' }, props.data?.title || '未读邮件')
      ]),
      h('div', { class: 'card-value' }, '12'),
      h('div', { class: 'card-action' }, [
        h('span', { class: 'action-text' }, '查看邮箱'),
        h('span', { class: 'material-icons-outlined' }, 'arrow_forward')
      ])
    ])
  }
}

// 默认卡片
const BentoDefaultCard = {
  props: ['card', 'data'],
  setup(props) {
    return () => h('div', { class: 'bento-card-content default-card-content' }, [
      h('span', { class: 'material-icons-outlined card-icon-lg' }, 'dashboard_customize'),
      h('p', { class: 'card-label' }, props.data?.title || '未命名卡片'),
      h('p', { class: 'card-desc' }, '点击编辑配置卡片内容')
    ])
  }
}

// ============================================================================
// 拖拽相关
// ============================================================================
const handleDragStart = (e, card) => {
  draggingCard.value = card.id
  e.dataTransfer.effectAllowed = 'move'
  e.target.classList.add('dragging')
}

const handleDragEnd = (e) => {
  draggingCard.value = null
  dragOverCard.value = null
  e.target.classList.remove('dragging')
}

const handleDragOver = (e, card) => {
  e.preventDefault()
  if (draggingCard.value && draggingCard.value !== card.id) {
    dragOverCard.value = card.id
  }
}

const handleDrop = (e, targetCard) => {
  e.preventDefault()
  if (!draggingCard.value || draggingCard.value === targetCard.id) return

  // 交换卡片位置
  const draggingIndex = cards.value.findIndex(c => c.id === draggingCard.value)
  const targetIndex = cards.value.findIndex(c => c.id === targetCard.id)

  if (draggingIndex !== -1 && targetIndex !== -1) {
    const tempPos = { ...cards.value[draggingIndex].position }
    cards.value[draggingIndex].position = { ...cards.value[targetIndex].position }
    cards.value[targetIndex].position = tempPos
    saveCardsToStorage()
  }

  draggingCard.value = null
  dragOverCard.value = null
}

// ============================================================================
// 交互处理
// ============================================================================
const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
}

const handleCardClick = (card) => {
  if (isEditMode.value) return
  ElMessage.info(`点击卡片: ${card.data?.title || card.id}`)
}

const handleAppClick = (app) => {
  ElMessage.info(`打开应用: ${app.label}`)
}

const handleAddApp = () => {
  showAddCardDialog.value = true
  selectedCardType.value = 'app'
}

const handleAddCard = () => {
  if (!selectedTemplate.value) {
    ElMessage.warning('请选择卡片模板')
    return
  }

  const template = Object.values(cardTemplates).flat().find(t => t.id === selectedTemplate.value)
  if (!template) return

  // 计算新卡片位置
  const maxRow = Math.max(...cards.value.map(c => c.position?.row || 0), 0)
  const rowCards = cards.value.filter(c => c.position?.row === maxRow)
  const newCol = rowCards.length > 0 ? Math.max(...rowCards.map(c => c.position?.col || 0)) + 1 : 0

  const newCard = {
    id: `card-${Date.now()}`,
    type: template.id,
    size: template.size,
    position: { row: newCol > 1 ? maxRow + 1 : maxRow, col: newCol > 1 ? 0 : newCol },
    data: { title: template.label }
  }

  cards.value.push(newCard)
  saveCardsToStorage()
  showAddCardDialog.value = false
  selectedTemplate.value = ''
  ElMessage.success('卡片添加成功')
}

const handleEditCard = (card) => {
  ElMessage.info(`编辑卡片: ${card.data?.title || card.id}`)
}

const handleRemoveCard = (cardId) => {
  const index = cards.value.findIndex(c => c.id === cardId)
  if (index !== -1) {
    cards.value.splice(index, 1)
    saveCardsToStorage()
    ElMessage.success('卡片已移除')
  }
}

// ============================================================================
// 初始化
// ============================================================================
onMounted(() => {
  loadCardsFromStorage()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 工作台面板容器
// ============================================================================
.workbench-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
  overflow: hidden;
}

// ============================================================================
// 顶部欢迎区 - 钉钉风格优化
// ============================================================================
.workbench-header {
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 50%, #ffffff 100%);
  border-bottom: 1px solid var(--dt-border-light);
  padding: 20px 24px; // 钉钉标准内边距
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  min-height: 88px; // 钉钉标准头部高度

  &::before {
    content: '';
    position: absolute;
    right: -50px;
    top: -100px;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(22, 119, 255, 0.06) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
}

.greeting-section {
  position: relative;
  z-index: 1;
}

.greeting-title {
  font-size: 22px; // 钉钉标准标题大小
  font-weight: 600; // 钉钉标准字重
  color: var(--dt-text-primary);
  margin: 0;
  letter-spacing: -0.01em;
  line-height: 1.3;
}

.greeting-date {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 4px 0 0 0;
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 4px;

  .material-icons-outlined {
    font-size: 14px;
    color: var(--dt-brand-color);
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.header-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--dt-text-quaternary);
  font-size: 18px;
  pointer-events: none;
}

.search-input {
  width: 100%;
  max-width: 280px; // 钉钉搜索框宽度
  height: 36px;
  padding: 0 12px 0 38px; // 增加左侧 padding 适配图标
  background: #ffffff;
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md); // 钉钉使用 8px 圆角
  font-size: 14px;
  color: var(--dt-text-primary);
  outline: none;
  transition: all var(--dt-transition-base);

  &::placeholder {
    color: var(--dt-text-quaternary);
  }

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px var(--dt-brand-bg);
  }

  &:focus {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
  }

  .dark & {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);

    &:hover,
    &:focus {
      border-color: var(--dt-brand-color);
    }
  }
}

// ============================================================================
// 按钮样式 - 钉钉风格优化
// ============================================================================
.icon-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  height: 32px; // 钉钉标准按钮高度
  background: #ffffff;
  color: var(--dt-text-secondary);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  font-size: 14px;
  font-weight: 400;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }

  &.active {
    background: var(--dt-brand-color);
    color: #fff;
    border-color: var(--dt-brand-color);
    box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
  }

  .material-icons-outlined {
    font-size: 18px;
  }

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    color: var(--dt-text-secondary-dark);

    &:hover {
      border-color: var(--dt-brand-color);
    }
  }
}

.custom-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  height: 32px;
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-brand-hover);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.25);
  }

  &:active {
    transform: translateY(0);
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

// ============================================================================
// 主内容区
// ============================================================================
.workbench-content {
  flex: 1;
  padding: 20px 32px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;

    &:hover {
      background: var(--dt-text-quaternary);
    }
  }
}

// ============================================================================
// 快捷应用区域 - 钉钉风格优化
// ============================================================================
.quick-apps-section {
  position: relative;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px; // 钉钉标准间距
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px; // 钉钉标准标题大小
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0;
}

.title-icon {
  width: 28px;
  height: 28px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-icon-primary {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.title-icon .material-icons-outlined {
  font-size: 16px;
}

.quick-apps-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px; // 钉钉网格间距
}

.quick-app-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 8px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    border-color: var(--dt-brand-color);
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(22, 119, 255, 0.12);
  }

  &:active {
    transform: translateY(0);
  }

  &.add-app {
    background: transparent;
    border: 1px dashed var(--dt-border-color);

    .app-icon {
      background: var(--dt-bg-hover);
      color: var(--dt-text-quaternary);
    }

    .app-label {
      color: var(--dt-text-quaternary);
    }

    &:hover {
      border-color: var(--dt-brand-color);
      background: var(--dt-brand-bg);

      .app-icon {
        color: var(--dt-brand-color);
      }
    }
  }
}

.app-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dt-transition-base);

  .material-icons-outlined {
    font-size: 22px;
  }

  // 钉钉风格应用图标颜色
  &.icon-orange { background: rgba(250, 140, 22, 0.12); color: #fa8c16; }
  &.icon-blue { background: rgba(22, 119, 255, 0.12); color: #1677ff; }
  &.icon-green { background: rgba(82, 196, 26, 0.12); color: #52c41a; }
  &.icon-purple { background: rgba(114, 46, 209, 0.12); color: #722ed1; }
  &.icon-pink { background: rgba(235, 47, 150, 0.12); color: #eb2f96; }
  &.icon-cyan { background: rgba(13, 202, 240, 0.12); color: #13c2c2; }
  &.icon-indigo { background: rgba(89, 78, 236, 0.12); color: #594efc; }
  &.icon-teal { background: rgba(19, 180, 167, 0.12); color: #13c2c2; }

  .dark & {
    background: var(--dt-bg-input-dark);

    &.icon-orange { background: rgba(250, 140, 22, 0.2); color: #fdba74; }
    &.icon-blue { background: rgba(22, 119, 255, 0.2); color: #7dd3fc; }
    &.icon-green { background: rgba(82, 196, 26, 0.2); color: #86efac; }
    &.icon-purple { background: rgba(114, 46, 209, 0.2); color: #c084fc; }
    &.icon-pink { background: rgba(235, 47, 150, 0.2); color: #f472b6; }
    &.icon-cyan { background: rgba(13, 202, 240, 0.2); color: #22d3ee; }
    &.icon-indigo { background: rgba(89, 78, 236, 0.2); color: #818cf8; }
    &.icon-teal { background: rgba(19, 180, 167, 0.2); color: #2dd4bf; }
  }
}

.app-label {
  font-size: 12px;
  font-weight: 400;
  color: var(--dt-text-primary);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

// ============================================================================
// Bento Grid 布局（钉钉风格）
// ============================================================================
.bento-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); // 钉钉使用3列布局
  gap: 16px; // 钉钉间距16px
  align-items: start;

  &.edit-mode {
    .bento-card {
      cursor: move;

      &:hover {
        box-shadow: 0 0 0 2px var(--dt-brand-color);
      }

      &.dragging {
        opacity: 0.5;
        transform: scale(0.95);
      }
    }
  }
}

// ============================================================================
// Bento 卡片（钉钉风格）
// ============================================================================
.bento-card {
  position: relative;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px; // 钉钉使用8px圆角
  overflow: hidden;
  transition: all var(--dt-transition-base);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04); // 钉钉更轻的阴影

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); // 悬停时阴影加深
    transform: translateY(-1px);
  }

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  // 卡片尺寸
  &.card-size-1x1 {
    grid-column: span 1;
    grid-row: span 1;
  }

  &.card-size-1x2 {
    grid-column: span 1;
    grid-row: span 2;
  }

  &.card-size-2x1 {
    grid-column: span 2;
    grid-row: span 1;
  }

  &.card-size-2x2 {
    grid-column: span 2;
    grid-row: span 2;
  }

  &.card-size-2x3 {
    grid-column: span 2;
    grid-row: span 3;
  }
}

// 卡片内容
.bento-card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  &.compact {
    padding: 12px 16px;

    .card-title {
      font-size: 14px;
    }

    .card-icon {
      font-size: 18px;
    }
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.card-icon {
  font-size: 18px;
  color: var(--dt-brand-color);
}

.card-icon-lg {
  font-size: 48px;
  color: var(--dt-text-quaternary);
  margin-bottom: 8px;
}

.view-all-link {
  font-size: 12px;
  color: var(--dt-text-link);
  text-decoration: none;
  transition: color var(--dt-transition-fast);

  &:hover {
    color: var(--dt-text-link-hover);
  }
}

// 卡片操作按钮（编辑模式）
.card-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  z-index: 10;
}

.action-btn {
  width: 28px;
  height: 28px;
  padding: 0;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    border-color: var(--dt-brand-color);
  }

  &.danger:hover {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
    border-color: var(--dt-error-color);
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

// ============================================================================
// 待办卡片样式
// ============================================================================
.todo-card-content {
  min-height: 280px;
}

.todo-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-card-hover);
    border-color: var(--dt-brand-color);
    transform: translateX(2px);
  }

  &.priority-high {
    border-left: 3px solid #ff4d4f;
  }

  &.priority-medium {
    border-left: 3px solid #faad14;
  }

  &.priority-low {
    border-left: 3px solid var(--dt-brand-color);
  }
}

.todo-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;

  .todo-item.priority-high & { background: #ff4d4f; }
  .todo-item.priority-medium & { background: #faad14; }
  .todo-item.priority-low & { background: var(--dt-brand-color); }
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0 0 3px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-deadline {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--dt-text-quaternary);
  margin: 0;

  .material-icons-outlined {
    font-size: 12px;
  }
}

// ============================================================================
// 公告卡片样式
// ============================================================================
.announcement-card-content {
  min-height: 280px;
}

.announcement-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-card-hover);
    border-color: var(--dt-brand-color);
    transform: translateX(2px);
  }
}

.announcement-tag {
  padding: 2px 6px;
  font-size: 10px;
  font-weight: 600;
  border-radius: var(--dt-radius-sm);
  flex-shrink: 0;
  height: fit-content;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.announcement-content {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0 0 6px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--dt-text-quaternary);
  flex-wrap: wrap;

  .material-icons-outlined {
    font-size: 12px;
  }
}

// ============================================================================
// 日程卡片样式
// ============================================================================
.schedule-card-content {
  min-height: 180px;
}

.schedule-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-item {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-card-hover);
    border-color: var(--dt-brand-color);
  }
}

.schedule-time {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-brand-color);
  padding: 4px 8px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-sm);
}

.schedule-info {
  flex: 1;
  min-width: 0;
}

.schedule-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0 0 3px 0;
}

.schedule-location {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--dt-text-quaternary);

  .material-icons-outlined {
    font-size: 12px;
  }
}

// ============================================================================
// 统计卡片样式
// ============================================================================
.stats-card-content {
  min-height: 200px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: var(--dt-bg-input);
  border-radius: var(--dt-radius-lg);
  transition: all var(--dt-transition-base);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--dt-shadow-2);
  }

  --stat-color: var(--dt-brand-color);
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--stat-color);
  line-height: 1;
}

.stat-unit {
  font-size: 13px;
  color: var(--stat-color);
}

.stat-total {
  font-size: 12px;
  color: var(--dt-text-quaternary);
}

.stat-label {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

// ============================================================================
// 快捷卡片样式（审批、邮件）
// ============================================================================
.approval-card-content,
.mail-card-content {
  min-height: 140px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--dt-text-primary);
  text-align: center;
  padding: 20px 0;
}

.card-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-brand-color);
    color: #fff;
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

// ============================================================================
// 默认卡片样式
// ============================================================================
.default-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  padding: 24px;
  text-align: center;
}

.card-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 8px 0 4px 0;
}

.card-desc {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  margin: 0;
}

// ============================================================================
// 空网格状态
// ============================================================================
.empty-grid {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: var(--dt-bg-input);
  border: 2px dashed var(--dt-border-color);
  border-radius: var(--dt-radius-xl);
}

.empty-icon {
  font-size: 48px;
  color: var(--dt-text-quaternary);
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
  color: var(--dt-text-quaternary);
  margin: 0;
}

// ============================================================================
// 添加卡片弹窗
// ============================================================================
.add-card-dialog {
  padding: 8px 0;
}

.card-type-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--dt-border-light);
}

.type-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-md);
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

.card-templates {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  max-height: 320px;
  overflow-y: auto;
}

.template-item {
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    transform: translateY(-2px);
  }

  &.selected .template-preview {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px var(--dt-brand-bg);
  }
}

.template-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  background: var(--dt-bg-input);
  border: 2px solid var(--dt-border-light);
  border-radius: var(--dt-radius-lg);
  transition: all var(--dt-transition-base);

  &.preview-1x1 {
    aspect-ratio: 1;
  }

  &.preview-1x2 {
    aspect-ratio: 1/2;
  }

  &.preview-2x1 {
    aspect-ratio: 2/1;
  }

  &.preview-2x2 {
    aspect-ratio: 1;
  }

  .material-icons-outlined {
    font-size: 28px;
    color: var(--dt-brand-color);
  }
}

.template-label {
  font-size: 12px;
  color: var(--dt-text-secondary);
}

// ============================================================================
// 响应式
// ============================================================================
@media (max-width: 1200px) {
  .quick-apps-grid {
    grid-template-columns: repeat(6, 1fr);
  }
}

@media (max-width: 768px) {
  .workbench-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
  }

  .greeting-title {
    font-size: 20px;
  }

  .search-input {
    max-width: 200px;
  }

  .workbench-content {
    padding: 16px 20px;
  }

  .quick-apps-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .bento-grid {
    grid-template-columns: 1fr;
  }

  .bento-card {
    &.card-size-2x1,
    &.card-size-2x2,
    &.card-size-2x3 {
      grid-column: span 1;
    }

    &.card-size-1x2,
    &.card-size-2x2,
    &.card-size-2x3 {
      grid-row: span 1;
    }
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .workbench-header {
  background: linear-gradient(135deg, #0c4a6e 0%, #075985 50%, #0f172a 100%);
  border-color: var(--dt-border-dark);

  &::before {
    background: radial-gradient(circle, rgba(56, 189, 248, 0.1) 0%, transparent 70%);
  }
}

.dark .greeting-title {
  color: var(--dt-text-primary-dark);
}

.dark .greeting-date {
  color: var(--dt-text-secondary-dark);
}

.dark .search-input,
.dark .icon-btn,
.dark .custom-btn {
  background: var(--dt-bg-input-dark);
  border-color: var(--dt-border-dark);
  color: var(--dt-text-primary-dark);
}

.dark .icon-btn:hover,
.dark .icon-btn.active {
  background: var(--dt-brand-color);
  border-color: var(--dt-brand-color);
  color: #fff;
}

.dark .quick-apps-grid .quick-app-item {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .bento-card {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .todo-item,
.dark .announcement-item,
.dark .schedule-item {
  background: var(--dt-bg-input-dark);
  border-color: var(--dt-border-dark);
}

.dark .todo-item:hover,
.dark .announcement-item:hover,
.dark .schedule-item:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .stat-item {
  background: var(--dt-bg-input-dark);
}

.dark .app-icon {
  background: var(--dt-bg-input-dark);
}

.dark .app-icon.icon-orange { background: rgba(250, 140, 22, 0.15); color: #fdba74; }
.dark .app-icon.icon-blue { background: rgba(22, 119, 255, 0.15); color: #7dd3fc; }
.dark .app-icon.icon-green { background: rgba(82, 196, 26, 0.15); color: #86efac; }
.dark .app-icon.icon-purple { background: rgba(114, 46, 209, 0.15); color: #c084fc; }
.dark .app-icon.icon-pink { background: rgba(235, 47, 150, 0.15); color: #f472b6; }
.dark .app-icon.icon-cyan { background: rgba(13, 202, 240, 0.15); color: #22d3ee; }
.dark .app-icon.icon-indigo { background: rgba(89, 78, 236, 0.15); color: #818cf8; }
.dark .app-icon.icon-teal { background: rgba(19, 180, 167, 0.15); color: #2dd4bf; }
</style>
