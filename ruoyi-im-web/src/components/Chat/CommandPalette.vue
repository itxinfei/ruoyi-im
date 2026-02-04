<template>
  <teleport to="body">
    <transition name="fade">
      <div
        v-if="show"
        class="command-palette-overlay"
        @click="handleClose"
      >
        <div
          class="command-palette-container"
          :style="positionStyle"
          @click.stop
        >
          <!-- 搜索框 -->
          <div class="command-search">
            <span class="command-prefix">/</span>
            <input
              ref="searchInputRef"
              v-model="searchQuery"
              class="search-input"
              placeholder="搜索命令..."
              type="text"
              @keydown="handleKeydown"
            />
            <span v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">
              <span class="material-icons-outlined">close</span>
            </span>
          </div>

          <!-- 命令列表 -->
          <div class="command-list">
            <div
              v-for="(cmd, index) in filteredCommands"
              :key="cmd.id"
              class="command-item"
              :class="{
                active: selectedIndex === index,
                category: cmd.category
              }"
              @click="handleSelectCommand(cmd)"
              @mouseenter="selectedIndex = index"
            >
              <div class="command-icon" :class="cmd.iconClass">
                <span v-if="cmd.materialIcon" class="material-icons-outlined">{{ cmd.materialIcon }}</span>
                <el-icon v-else><component :is="cmd.icon" /></el-icon>
              </div>
              <div class="command-info">
                <div class="command-name">{{ cmd.name }}</div>
                <div class="command-desc">{{ cmd.description }}</div>
              </div>
              <div class="command-shortcut" v-if="cmd.shortcut">
                {{ cmd.shortcut }}
              </div>
            </div>

            <!-- 无结果提示 -->
            <div v-if="filteredCommands.length === 0" class="no-results">
              <span class="material-icons-outlined">search_off</span>
              <p>没有找到匹配的命令</p>
            </div>
          </div>

          <!-- 底部提示 -->
          <div class="command-footer">
            <span class="footer-hint">
              <span class="key">↑↓</span> 选择
              <span class="key">Enter</span> 执行
              <span class="key">Esc</span> 关闭
            </span>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import {
  Document as DocIcon,
  Calendar as CalendarIcon,
  VideoCamera as VideoIcon,
  CircleCheck as TodoIcon,
  Folder as ApprovalIcon,
  ChatDotRound as VoteIcon,
  Bell as AnnouncementIcon,
  Location as CheckinIcon
} from '@element-plus/icons-vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  }
})

const emit = defineEmits(['close', 'select'])

const searchQuery = ref('')
const selectedIndex = ref(0)
const searchInputRef = ref(null)

// 命令列表配置
const commands = [
  {
    id: 'document',
    name: '发文档',
    keyword: 'document doc 文档',
    description: '发送文档到当前会话',
    icon: DocIcon,
    materialIcon: null,
    iconClass: 'icon-blue',
    category: 'file',
    shortcut: 'Alt+D'
  },
  {
    id: 'schedule',
    name: '发日程',
    keyword: 'schedule calendar 日程 日历',
    description: '创建并发送日程安排',
    icon: CalendarIcon,
    materialIcon: null,
    iconClass: 'icon-green',
    category: 'schedule',
    shortcut: 'Alt+C'
  },
  {
    id: 'meeting',
    name: '发起会议',
    keyword: 'meeting video 会议 视频',
    description: '发起视频会议',
    icon: VideoIcon,
    materialIcon: null,
    iconClass: 'icon-purple',
    category: 'meeting',
    shortcut: 'Alt+V'
  },
  {
    id: 'todo',
    name: '待办',
    keyword: 'todo task 待办 任务',
    description: '创建待办事项',
    icon: TodoIcon,
    materialIcon: null,
    iconClass: 'icon-orange',
    category: 'task',
    shortcut: 'Alt+T'
  },
  {
    id: 'approval',
    name: '审批',
    keyword: 'approval 审批',
    description: '发起审批流程',
    icon: ApprovalIcon,
    materialIcon: null,
    iconClass: 'icon-red',
    category: 'workflow',
    shortcut: 'Alt+A'
  },
  {
    id: 'vote',
    name: '投票',
    keyword: 'vote poll 投票',
    description: '发起投票',
    icon: VoteIcon,
    materialIcon: null,
    iconClass: 'icon-cyan',
    category: 'interactive',
    shortcut: 'Alt+P'
  },
  {
    id: 'announcement',
    name: '公告',
    keyword: 'announcement 公告',
    description: '发布公告',
    icon: AnnouncementIcon,
    materialIcon: null,
    iconClass: 'icon-yellow',
    category: 'notify',
    shortcut: 'Alt+N'
  },
  {
    id: 'checkin',
    name: '签到',
    keyword: 'checkin 签到 打卡',
    description: '发起签到',
    icon: CheckinIcon,
    materialIcon: null,
    iconClass: 'icon-pink',
    category: 'location',
    shortcut: 'Alt+L'
  }
]

// 过滤后的命令列表
const filteredCommands = computed(() => {
  if (!searchQuery.value) {
    return commands
  }

  const query = searchQuery.value.toLowerCase().trim()
  return commands.filter(cmd => {
    const nameMatch = cmd.name.toLowerCase().includes(query)
    const keywordMatch = cmd.keyword.toLowerCase().includes(query)
    const descMatch = cmd.description.toLowerCase().includes(query)
    return nameMatch || keywordMatch || descMatch
  })
})

// 容器位置样式
const positionStyle = computed(() => {
  // 计算合适的显示位置，避免超出视口
  const x = Math.min(props.position.x, window.innerWidth - 420)
  const y = Math.min(props.position.y, window.innerHeight - 400)
  return {
    left: `${x}px`,
    top: `${y}px`
  }
})

// 处理键盘事件
const handleKeydown = (e) => {
  const count = filteredCommands.value.length

  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      selectedIndex.value = (selectedIndex.value + 1) % count
      break
    case 'ArrowUp':
      e.preventDefault()
      selectedIndex.value = (selectedIndex.value - 1 + count) % count
      break
    case 'Enter':
      e.preventDefault()
      if (filteredCommands.value[selectedIndex.value]) {
        handleSelectCommand(filteredCommands.value[selectedIndex.value])
      }
      break
    case 'Escape':
      handleClose()
      break
  }
}

// 选择命令
const handleSelectCommand = (command) => {
  emit('select', command.id)
  handleClose()
}

// 关闭面板
const handleClose = () => {
  searchQuery.value = ''
  selectedIndex.value = 0
  emit('close')
}

// 监听显示状态，自动聚焦搜索框
watch(() => props.show, (show) => {
  if (show) {
    nextTick(() => {
      searchInputRef.value?.focus()
    })
  }
})

// 组件挂载时监听全局快捷键
const handleGlobalKeydown = (e) => {
  // Alt+/ 打开命令面板
  if (e.altKey && e.key === '/') {
    e.preventDefault()
    emit('open')
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleGlobalKeydown)
})

// 暴露方法供父组件调用
defineExpose({
  focus: () => searchInputRef.value?.focus()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.command-palette-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 3000;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding-top: 80px;
  padding-left: 300px;
}

.command-palette-container {
  position: absolute;
  width: 400px;
  max-width: calc(100vw - 32px);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-3xl);
  overflow: hidden;
  animation: scaleIn 0.2s var(--dt-ease-out);
}

// 搜索框
.command-search {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);
}

.command-prefix {
  font-size: 24px;
  font-weight: 600;
  color: var(--dt-brand-color);
  margin-right: 8px;
  line-height: 1;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 16px;
  color: var(--dt-text-primary);
  font-family: var(--dt-font-family);

  &::placeholder {
    color: var(--dt-text-quaternary);
  }
}

.clear-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: var(--dt-radius-full);
  color: var(--dt-text-quaternary);
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-border-color);
    color: var(--dt-text-secondary);
  }
}

// 命令列表
.command-list {
  max-height: 320px;
  overflow-y: auto;
  padding: 8px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
  }
}

.command-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: rgba(0, 137, 255, 0.1);

    .command-name {
      color: var(--dt-brand-color);
    }
  }
}

.command-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-lg);
  font-size: 20px;
  flex-shrink: 0;

  &.icon-blue { background: rgba(0, 137, 255, 0.1); color: #0089FF; }
  &.icon-green { background: rgba(82, 196, 26, 0.1); color: #52c41a; }
  &.icon-purple { background: rgba(114, 46, 209, 0.1); color: #722ed1; }
  &.icon-orange { background: rgba(250, 140, 22, 0.1); color: #fa8c16; }
  &.icon-red { background: rgba(255, 77, 79, 0.1); color: #ff4d4f; }
  &.icon-cyan { background: rgba(13, 202, 240, 0.1); color: #13c2c2; }
  &.icon-yellow { background: rgba(250, 219, 20, 0.1); color: #fadb14; }
  &.icon-pink { background: rgba(235, 47, 150, 0.1); color: #eb2f96; }
}

.command-info {
  flex: 1;
  min-width: 0;
}

.command-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 2px;
}

.command-desc {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.command-shortcut {
  font-size: 11px;
  color: var(--dt-text-quaternary);
  background: var(--dt-bg-body);
  padding: 2px 6px;
  border-radius: var(--dt-radius-sm);
  font-family: monospace;
  flex-shrink: 0;
}

// 无结果
.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--dt-text-quaternary);

  .material-icons-outlined {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

// 底部提示
.command-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);
}

.footer-hint {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--dt-text-quaternary);
}

.key {
  display: inline-flex;
  align-items: center;
  padding: 2px 6px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-sm);
  font-family: monospace;
  font-size: 11px;
}

// 动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s var(--dt-ease-out);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// 暗色模式
.dark .command-palette-overlay {
  background: rgba(0, 0, 0, 0.6);
}

.dark .command-palette-container {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .command-search {
  background: var(--dt-bg-input-dark);
  border-color: var(--dt-border-dark);
}

.dark .search-input {
  color: var(--dt-text-primary-dark);
}
</style>
