<template>
  <div class="toolbar-enhanced">
    <!-- 左侧工具组 -->
    <div class="toolbar-section left-section">
      <!-- 表情选择器 -->
      <div class="tool-item">
        <el-popover
          placement="top"
          :width="360"
          trigger="click"
          popper-class="emoji-popover"
          @show="handleEmojiShow"
          @hide="handleEmojiHide"
        >
          <template #reference>
            <button class="tool-btn emoji-btn" :class="{ active: showEmojiPanel }">
              <el-icon><ChatDotRound /></el-icon>
            </button>
          </template>
          
          <div class="emoji-panel">
            <!-- 表情分类标签 -->
            <div class="emoji-tabs">
              <div 
                v-for="tab in emojiTabs"
                :key="tab.key"
                :class="['emoji-tab', { active: activeEmojiTab === tab.key }]"
                @click="activeEmojiTab = tab.key"
              >
                {{ tab.label }}
              </div>
            </div>
            
            <!-- 表情网格 -->
            <div class="emoji-grid">
              <div
                v-for="emoji in currentEmojis"
                :key="emoji"
                class="emoji-item"
                @click="selectEmoji(emoji)"
              >
                {{ emoji }}
              </div>
            </div>
            
            <!-- 表情搜索 -->
            <div class="emoji-search">
              <el-input
                v-model="emojiSearch"
                placeholder="搜索表情..."
                size="small"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </div>
        </el-popover>
      </div>

      <!-- 文件传输 -->
      <div class="tool-item">
        <el-dropdown trigger="click" @command="handleFileCommand">
          <button class="tool-btn file-btn">
            <el-icon><FolderOpened /></el-icon>
          </button>
          <template #dropdown>
              <el-dropdown-menu class="action-menu">
                <el-dropdown-item command="screenshot">
                  <div class="menu-item-content">
                    <div class="menu-icon screenshot-icon">
                      <el-icon><ScissorOne /></el-icon>
                    </div>
                    <div class="menu-info">
                      <div class="menu-title">截图工具</div>
                      <div class="menu-desc">快速截图并分享</div>
                    </div>
                  </div>
                </el-dropdown-item>
              <el-dropdown-item command="format">
                  <div class="menu-item-content">
                    <div class="menu-icon format-icon">
                      <el-icon><Edit /></el-icon>
                    </div>
                    <div class="menu-info">
                      <div class="menu-title">格式化文本</div>
                      <div class="menu-desc">字体样式、颜色、大小等</div>
                    </div>
                  </div>
                </el-dropdown-item>
              <el-dropdown-item command="clock">
                  <div class="menu-item-content">
                    <div class="menu-icon">
                      <el-icon><Timer /></el-icon>
                    </div>
                    <div class="menu-info">
                      <div class="menu-title">发送时间</div>
                      <div class="menu-desc">设置消息定时发送</div>
                    </div>
                  </div>
                </el-dropdown-item>
              <el-dropdown-item command="priority">
                <div class="menu-item-content">
                  <div class="menu-icon priority-icon">
                    <el-icon><Flag /></el-icon>
                    </div>
                    <div class="menu-info">
                      <div class="menu-title">重要标记</div>
                      <div class="menu-desc">标记重要消息</div>
                    </div>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided command="separator">
                  <div class="menu-separator"></div>
                </el-dropdown-item>
                <el-dropdown-item command="archive">
                  <div class="menu-item-content">
                    <div class="menu-icon archive-icon">
                      <el-icon><FolderOpened /></el-icon>
                    </div>
                    <div class="menu-info">
                      <div class="menu-title">存档消息</div>
                      <div class="menu-desc">归档历史聊天</div>
                    </div>
                  </div>
                </el-dropdown-item>
                <div class="menu-item-content">
                  <div class="menu-icon">
                    <el-icon><Switch /></el-icon>
                  </div>
                  <div class="menu-info">
                    <div class="menu-title">实时翻译</div>
                    <div class="menu-desc">自动翻译消息</div>
                  </div>
                  <el-switch
                    v-model="translateEnabled"
                    size="small"
                    @click.stop
                  />
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 更多工具 -->
      <div class="tool-item">
        <el-popover
          placement="top"
          :width="400"
          trigger="click"
          popper-class="more-tools-popover"
        >
          <template #reference>
            <button class="tool-btn more-btn">
              <el-icon><More /></el-icon>
            </button>
          </template>
          
          <div class="more-tools-panel">
            <!-- 工具分类 -->
            <div class="tools-grid">
              <div
                v-for="tool in moreTools"
                :key="tool.key"
                class="tool-card"
                @click="handleToolAction(tool)"
              >
                <div class="tool-icon" :style="{ backgroundColor: tool.color }">
                  <el-icon><component :is="tool.icon" /></el-icon>
                </div>
                <div class="tool-info">
                  <div class="tool-name">{{ tool.name }}</div>
                  <div class="tool-desc">{{ tool.desc }}</div>
                </div>
                <div class="tool-status" v-if="tool.status">
                  <el-tag :type="tool.status.type" size="small">{{ tool.status.text }}</el-tag>
                </div>
              </div>
            </div>
            
            <!-- 最近使用 -->
            <div class="recent-tools" v-if="recentTools.length > 0">
              <div class="section-title">最近使用</div>
              <div class="recent-list">
                <div
                  v-for="tool in recentTools"
                  :key="tool.key"
                  class="recent-item"
                  @click="handleToolAction(tool)"
                >
                  <el-icon><component :is="tool.icon" /></el-icon>
                  <span>{{ tool.name }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-popover>
      </div>
    </div>

      <!-- 右侧功能组 -->
      <div class="toolbar-section right-section">
        <el-tooltip v-if="session?.type === 'GROUP'" content="@成员" placement="top">
          <button class="tool-btn" @click="handleAtMember">
            <el-icon><At /></el-icon>
          </button>
        </el-tooltip>



        <el-tooltip content="快捷回复" placement="top">
          <button class="tool-btn quick-reply-btn" @click="handleQuickReply">
            <el-icon><ChatLineSquare /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="收藏夹" placement="top">
          <button class="tool-btn collection-btn" @click="handleShowCollection">
            <el-icon><Star /></el-icon>
            <el-badge v-if="collectionCount > 0" :value="collectionCount" :max="99" class="collection-badge" />
          </button>
        </el-tooltip>

        <el-tooltip content="快捷操作" placement="top">
          <el-dropdown trigger="click" @command="handleQuickAction">
            <button class="tool-btn action-btn">
              <el-icon><Operation /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu class="action-menu">
                <el-dropdown-item command="forward">
                  <div class="menu-item-content">
                    <el-icon><Promotion /></el-icon>
                    <span>转发消息</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="multiselect">
                  <div class="menu-item-content">
                    <el-icon><Select /></el-icon>
                    <span>多选消息</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="search">
                  <div class="menu-item-content">
                    <el-icon><Search /></el-icon>
                    <span>搜索聊天记录</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="export">
                  <div class="menu-item-content">
                    <el-icon><Download /></el-icon>
                    <span>导出聊天记录</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided command="clear">
                  <div class="menu-item-content">
                    <el-icon><Delete /></el-icon>
                    <span>清空聊天记录</span>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-tooltip>

        <el-tooltip content="AI 助手" placement="top">
          <button class="tool-btn ai-btn" @click="handleShowSmartReply">
            <el-icon><MagicStick /></el-icon>
            <el-badge v-if="aiWaitingCount > 0" :value="aiWaitingCount" :max="99" class="ai-badge" />
          </button>
        </el-tooltip>
      </div>
      </div>

      <!-- 置顶/免打扰 -->
      <div class="tool-item">
        <button class="tool-btn pin-btn" @click="togglePin">
          <el-icon><Position /></el-icon>
        </button>
      </div>

      <!-- AI助手 -->
      <div class="tool-item">
        <el-popover placement="top" width="300" trigger="click" popper-class="ai-assistant-popover">
          <template #reference>
            <button class="tool-btn ai-btn">
              <el-icon><MagicStick /></el-icon>
              <span class="ai-badge" v-if="aiWaiting">3</span>
            </button>
          </template>
          
          <div class="ai-assistant-panel">
            <div class="ai-header">
              <el-icon><MagicStick /></el-icon>
              <span>AI助手</span>
              <el-switch v-model="aiEnabled" @change="handleAiToggle" />
            </div>
            
            <div class="ai-features">
              <div
                v-for="feature in aiFeatures"
                :key="feature.key"
                class="feature-item"
                @click="selectAiFeature(feature)"
              >
                <el-icon><component :is="feature.icon" /></el-icon>
                <div class="feature-info">
                  <div class="feature-name">{{ feature.name }}</div>
                  <div class="feature-desc">{{ feature.desc }}</div>
                </div>
                <div class="feature-hot" v-if="feature.hot">
                  <el-tag size="small" type="danger">HOT</el-tag>
                </div>
              </div>
            </div>
            
            <div class="ai-shortcuts">
              <div class="shortcut-item">
                <kbd>/</kbd>
                <span>快速命令</span>
              </div>
              <div class="shortcut-item">
                <kbd>Tab</kbd>
                <span>智能补全</span>
              </div>
            </div>
          </div>
        </el-popover>
      </div>

      <!-- 设置 -->
      <div class="tool-item">
        <el-dropdown trigger="click" @command="handleSettingCommand">
          <button class="tool-btn setting-btn">
            <el-icon><Setting /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu class="enhanced-menu">
              <el-dropdown-item command="theme">
                <div class="menu-item-content">
                  <div class="menu-icon">
                    <el-icon><Sunny /></el-icon>
                  </div>
                  <div class="menu-info">
                    <div class="menu-title">主题切换</div>
                    <div class="menu-desc">亮色/暗色模式</div>
                  </div>
                  <div class="menu-control">
                    <el-switch
                      v-model="isDarkMode"
                      @change="toggleTheme"
                      @click.stop
                    />
                  </div>
                </div>
              </el-dropdown-item>
              <el-dropdown-item command="notification">
                <div class="menu-item-content">
                  <div class="menu-icon">
                    <el-icon><Bell /></el-icon>
                  </div>
                  <div class="menu-info">
                    <div class="menu-title">通知设置</div>
                    <div class="menu-desc">消息提醒配置</div>
                  </div>
                </div>
              </el-dropdown-item>
              <el-dropdown-item command="privacy">
                <div class="menu-item-content">
                  <div class="menu-icon">
                    <el-icon><Lock /></el-icon>
                  </div>
                  <div class="menu-info">
                    <div class="menu-title">隐私设置</div>
                    <div class="menu-desc">端到端加密</div>
                  </div>
                </div>
              </el-dropdown-item>
              <el-dropdown-item divided command="help">
                <div class="menu-item-content">
                  <div class="menu-icon">
                    <el-icon><QuestionFilled /></el-icon>
                  </div>
                  <div class="menu-info">
                    <div class="menu-title">帮助中心</div>
                    <div class="menu-desc">使用指南</div>
                  </div>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleImageUpload"
    />
    <input
      ref="fileInputRef"
      type="file"
      style="display: none"
      @change="handleFileUpload"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import {
  ChatDotRound,
  FolderOpened,
  Document,
  Picture,
  Cloud,
  Bell,
  Star,
  Position,
  MagicStick,
  Setting,
  Sunny,
  Lock,
  QuestionFilled,
  More,
  Bolt,
  EditPen,
  Share,
  Calendar,
  Timer,
  DocumentChecked,
  ChatLineSquare,
  At
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

const props = defineProps({
  session: Object
})

const emit = defineEmits([
  'upload-image',
  'upload-file',
  'upload-video',
  'screenshot',
  'voice-mode',
  'ai-assistant',
  'tool-action',
  'favorite-toggle',
  'pin-toggle',
  'setting-change'
])

const store = useStore()
const { isDark, setThemeMode } = useTheme()

// 状态管理
const showEmojiPanel = ref(false)
const activeEmojiTab = ref('smileys')
const emojiSearch = ref('')
const aiEnabled = ref(false)
const isDarkMode = ref(isDark.value)
const isFavorite = ref(false)
const markMode = ref(false)
const collectionCount = ref(3)
const aiWaitingCount = ref(0)

// Refs
const imageInputRef = ref(null)
const fileInputRef = ref(null)

// 表情数据
const emojiTabs = computed(() => [
  { key: 'smileys', label: '表情' },
  { key: 'people', label: '人物' },
  { key: 'animals', label: '动物' },
  { key: 'food', label: '食物' },
  { key: 'activities', label: '活动' },
  { key: 'objects', label: '物品' }
])

const emojis = {
  smileys: ['😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊', '😇', '🥰', '😘', '😢', '😭', '😤', '😠', '😡', '🤯', '😎', '🤓', '🥸', '😴'],
  people: ['👶', '👮', '👱', '👲', '👳', '👴', '👵', '👶', '👷', '👸', '👹', '👺', '👻', '👼', '👽', '💏', '💐', '💑', '💒', '💔', '💖', '💗', '💘', '💙', '💚', '💛', '💜', '💝', '💞', '💟'],
  animals: ['🐶', '🐸', '🐹', '🐺', '🐻', '🐼', '🐽', '🐾', '🐿', '🦀', '🦁', '🦃', '🦄', '🦅', '🦆', '🦇', '🦈', '🦉', '🦊', '🦋', '🦌', '🦍', '🦎', '🦏'],
  food: ['🍎', '🍏', '🍐', '🍑', '🍒', '🍓', '🍔', '🍕', '🍖', '🍗', '🍘', '🍙', '🍚', '🍛', '🍜', '🍝', '🍞', '🍟', '🍠'],
  activities: ['⚽', '🏀', '🏈', '🎉', '🎊', '🎋', '🎌', '🎍', '🎎', '🏐', '🎮', '🕹', '🎵', '🎶', '🎤', '🎧', '🎨', '🎬', '🏆'],
  objects: ['🌟', '💡', '🔦', '🔧', '🔨', '📱', '💻', '⌨', '☎', '📞', '📷', '📹', '📺', '📻', '💾', '💿', '📀', '📁', '📂', '🗄️']
}

const currentEmojis = computed(() => {
  const tabEmojis = emojis[activeEmojiTab.value] || []
  if (!emojiSearch.value) return tabEmojis
  
  return tabEmojis.filter(emoji => 
    emoji.includes(emojiSearch.value)
  )
})

// 更多工具
const moreTools = computed(() => [
  {
    key: 'whiteboard',
    name: '协作白板',
    desc: '多人实时协作画板',
    icon: 'Monitor',
    color: '#722ed1',
    status: { type: 'success', text: '即将上线' }
  },
  {
    key: 'task',
    name: '任务管理',
    desc: '创建和分配任务',
    icon: 'DocumentChecked',
    color: '#1890ff'
  },
  {
    key: 'calendar',
    name: '日程安排',
    desc: '创建和管理日程',
    icon: 'Calendar',
    color: '#52c41a'
  },
  },
  {
    key: 'note',
    name: '笔记记录',
    desc: '快速记录要点',
    icon: 'EditPen',
    color: '#fa8c16'
  },
  },
  {
    key: 'poll',
    name: '投票决策',
    desc: '发起投票收集',
    icon: 'DataBoard',
    color: '#722ed1'
  }
  }
])

// AI功能
const aiFeatures = computed(() => [
  {
    key: 'smart-reply',
    name: '智能回复',
    desc: '智能推荐回复',
    icon: 'ChatLineSquare',
    hot: true
  },
  {
    key: 'content-create',
    name: '内容创作',
    desc: '创作文章和文案',
    icon: 'EditPen',
  },
  {
    key: 'translate',
    name: '实时翻译',
    desc: '多语言翻译',
    icon: 'Switch'
  },
  {
    key: 'summary',
    name: '对话总结',
    desc: '智能总结对话内容',
    icon: 'Document'
  },
  {
    key: 'analyze',
    name: '对话分析',
    desc: '分析对话模式和主题',
    icon: 'DataAnalysis'
  },
  {
    key: 'compose',
    name: '内容续写',
    desc: '续写已有内容',
    icon: 'Edit'
  }
]

const recentTools = ref([])

// 方法
const handleEmojiShow = () => {
  activeEmojiTab.value = 'smileys'
  emojiSearch.value = ''
}

const handleEmojiHide = () => {
  setTimeout(() => {
    showEmojiPanel.value = false
  }, 200)
}

const selectEmoji = (emoji) => {
  emit('tool-action', { type: 'emoji', data: emoji })
  showEmojiPanel.value = false
  
  // 添加到最近使用
  addToRecent('emoji', emoji)
}

const handleFileCommand = (command) => {
  switch (command) {
    case 'image':
      imageInputRef.value?.click()
      break
    case 'document':
      fileInputRef.value?.click()
      break
    case 'video':
      // 视频上传逻辑
      break
    case 'cloud':
      emit('tool-action', { type: 'cloud' })
      break
  }
}

const handleImageUpload = (e) => {
  const file = e.target.files[0]
  if (file) {
    emit('upload-image', file)
  }
}

const handleFileUpload = (e) => {
  const file = e.target.files[0]
  if (file) {
    emit('upload-file', file)
  }
}

const handleQuickCommand = (command) => {
  switch (command) {
    case 'screenshot':
      emit('screenshot')
      break
    case 'todo':
      handleShowTodo()
      break
    case 'calendar':
      handleShowCalendar()
      break
    case 'translate':
      translateEnabled.value = !translateEnabled.value
      break
    case 'quick-reply':
      handleQuickReply()
      break
  }
}

const handleToolAction = (tool) => {
  emit('tool-action', { type: 'tool', data: tool })
  addToRecent('tool', tool)
}

const selectAiFeature = (feature) => {
  emit('ai-assistant', feature)
  aiWaiting.value++
}

const handleAiToggle = (enabled) => {
  if (enabled) {
    aiWaiting.value = 0
  }
}

const handleShowMarkMenu = () => {
  emit('tool-action', { type: 'mark' })
}

const handleQuickReply = () => {
  emit('tool-action', { type: 'quick-reply' })
}

const handleSetReminder = () => {
  emit('tool-action', { type: 'reminder' })
}

const handleShowCollection = () => {
  emit('tool-action', { type: 'collection' })
}

const handleShowTodo = () => {
  emit('tool-action', { type: 'todo' })
}

const handleShowCalendar = () => {
  emit('tool-action', { type: 'calendar' })
}

const handleQuickReply = () => {
  emit('tool-action', { type: 'quick-reply' })
}

const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  emit('favorite-toggle', isFavorite.value)
}

const togglePin = () => {
  emit('pin-toggle')
}

const toggleTheme = () => {
  const newMode = isDarkMode.value ? 'light' : 'dark'
  setThemeMode(newMode)
  isDarkMode.value = !isDarkMode.value
}

const handleSettingCommand = (command) => {
  emit('setting-change', command)
}

const addToRecent = (type, item) => {
  // 添加到最近使用逻辑
  let key = ''
  if (type === 'emoji') {
    key = 'emoji'
  } else {
    key = 'tool'
  }
  
  // 这里简化处理，实际应该持久化到本地存储
  if (!recentTools.value.find(t => t.key === item.key || t === item)) {
    recentTools.value.unshift({ key: item.key || item, name: item.name || item, icon: item.icon })
    recentTools.value = recentTools.value.slice(0, 6)
  }
}

// 快捷键
const handleKeydown = (e) => {
  // Ctrl+/ 或 Cmd+/ 打开表情面板
  if ((e.ctrlKey || e.metaKey) && e.key === '/') {
    e.preventDefault()
    showEmojiPanel.value = !showEmojiPanel.value
  }
  
  // Esc 关闭所有面板
  if (e.key === 'Escape') {
    showEmojiPanel.value = false
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  
  // 从本地存储恢复状态
  const savedTheme = localStorage.getItem('im-theme-mode')
  if (savedTheme) {
    isDarkMode.value = savedTheme === 'dark'
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style lang="scss" scoped>
.toolbar-enhanced {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 8px 12px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  
  .toolbar-section {
    display: flex;
    align-items: center;
    gap: 8px;
    
    &.right-section {
      margin-left: auto;
    }
  }
  
  .tool-item {
    position: relative;
  }
  
  .tool-btn {
    width: 36px;
    height: 36px;
    border: none;
    background: transparent;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--el-text-color-regular);
    transition: all 0.2s ease;
    position: relative;
    
    &:hover {
      background: var(--el-fill-color-light);
      color: var(--el-color-primary);
      transform: translateY(-1px);
    }
    
    &.active {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }
    
    &.ai-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      
      &:hover {
        background: linear-gradient(135deg, #5a72d4 0%, #6a4190 100%);
        transform: scale(1.05);
      }
    }
    
    &.more-btn:hover {
      transform: rotate(180deg);
    }
    
    &.favorite-btn {
      &.is-favorite .el-icon {
        color: #F5B800;
      }
    }
    
    &.pin-btn {
      &.is-pinned .el-icon {
        color: #1890FF;
      }
    }
    
    &.mark-btn {
      &.is-marked .el-icon {
        color: #F5B800;
      }
    }
    
    &.reminder-btn {
      &.has-reminder .el-icon {
        color: #F5B800;
      }
    }
    
    &.quick-reply-btn {
      background: var(--el-fill-color-light);
      
      &:hover {
        background: var(--el-color-primary);
        color: white;
        transform: scale(1.02);
      }
    }
    
    &.action-btn {
      background: var(--el-fill-color-light);
      color: var(--el-text-color-regular);
      border: 1px solid var(--el-border-color-light);
      
      &:hover {
        background: var(--el-color-primary);
        color: white;
        border-color: var(--el-color-primary);
      }
      
      .el-icon {
        font-size: 16px;
      }
    }
    
    &.collection-btn {
      &.has-collection .el-icon {
        color: #F5B800;
      }
    }
  }
      
      .ai-badge {
        position: absolute;
        top: -4px;
        right: -4px;
        background: var(--el-color-danger);
        color: white;
        font-size: 10px;
        width: 16px;
        height: 16px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: bold;
      }
    }
    
    &.more-btn:hover {
      transform: rotate(180deg);
    }
    
    &.favorite-btn {
      &.is-favorite .el-icon {
        color: #F5B800;
      }
    }
  }
}

// 增强菜单样式
:deep(.enhanced-menu) {
  .el-dropdown-menu__item {
    padding: 0;
    min-height: 48px;
    
    &:hover {
      background: var(--el-fill-color-light);
    }
  }
  
  .menu-item-content {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    width: 100%;
    
    .menu-icon {
      width: 32px;
      height: 32px;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      
      &.image-icon {
        background: linear-gradient(135deg, #667eea, #764ba2);
      }
      
      &.doc-icon {
        background: linear-gradient(135deg, #3b82f6, #1e40af);
      }
      
      &.video-icon {
        background: linear-gradient(135deg, #ff6b6b, #ff4444);
      }
      
      &.cloud-icon {
        background: linear-gradient(135deg, #4CAF50, #2E7D32);
      }
      
      .el-icon {
        font-size: 16px;
      }
    }
    
    .menu-info {
      flex: 1;
      min-width: 0;
      
      .menu-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--el-text-color-primary);
        margin-bottom: 2px;
      }
      
      .menu-desc {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
    
    .menu-control {
      flex-shrink: 0;
    }
    
    .menu-shortcut {
      display: flex;
      gap: 2px;
      
      kbd {
        padding: 2px 4px;
        background: var(--el-fill-color-light);
        border: 1px solid var(--el-border-color-light);
        border-radius: 3px;
        font-size: 11px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

// 表情面板
:deep(.emoji-popover) {
  padding: 0;
  
  .emoji-panel {
    width: 360px;
    max-height: 320px;
    
    .emoji-tabs {
      display: flex;
      border-bottom: 1px solid var(--el-border-color-lighter);
      
      .emoji-tab {
        flex: 1;
        padding: 8px;
        text-align: center;
        cursor: pointer;
        font-size: 12px;
        color: var(--el-text-color-regular);
        transition: all 0.2s ease;
        
        &:hover {
          background: var(--el-fill-color-light);
        }
        
        &.active {
          color: var(--el-color-primary);
          border-bottom: 2px solid var(--el-color-primary);
        }
      }
    }
    
    .emoji-grid {
      padding: 8px;
      max-height: 200px;
      overflow-y: auto;
      display: grid;
      grid-template-columns: repeat(8, 1fr);
      gap: 4px;
      
      .emoji-item {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        border-radius: 4px;
        font-size: 20px;
        transition: all 0.2s ease;
        
        &:hover {
          background: var(--el-fill-color-light);
          transform: scale(1.2);
        }
      }
    }
    
    .emoji-search {
      padding: 8px;
      border-top: 1px solid var(--el-border-color-lighter);
    }
  }
}

// 更多工具面板
:deep(.more-tools-popover) {
  padding: 0;
  
  .more-tools-panel {
    width: 400px;
    max-height: 400px;
    
    .tools-grid {
      padding: 12px;
      max-height: 280px;
      overflow-y: auto;
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 8px;
      
      .tool-card {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 12px;
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s ease;
        position: relative;
        
        &:hover {
          border-color: var(--el-color-primary-light);
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .tool-icon {
          width: 36px;
          height: 36px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          flex-shrink: 0;
          
          .el-icon {
            font-size: 18px;
          }
        }
        
        .tool-info {
          flex: 1;
          min-width: 0;
          
          .tool-name {
            font-size: 13px;
            font-weight: 500;
            color: var(--el-text-color-primary);
            margin-bottom: 2px;
          }
          
          .tool-desc {
            font-size: 11px;
            color: var(--el-text-color-secondary);
            line-height: 1.3;
          }
        }
        
        .tool-status {
          position: absolute;
          top: 8px;
          right: 8px;
        }
      }
    }
    
    .recent-tools {
      border-top: 1px solid var(--el-border-color-lighter);
      
      .section-title {
        padding: 12px;
        font-size: 12px;
        color: var(--el-text-color-secondary);
        background: var(--el-fill-color-light);
      }
      
      .recent-list {
        display: flex;
        gap: 8px;
        padding: 8px;
        overflow-x: auto;
        
        .recent-item {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 6px 10px;
          background: var(--el-fill-color-light);
          border-radius: 6px;
          cursor: pointer;
          white-space: nowrap;
          
          &:hover {
            background: var(--el-color-primary-light-9);
            color: var(--el-color-primary);
          }
          
          .el-icon {
            font-size: 16px;
          }
          
          span {
            font-size: 12px;
          }
        }
      }
    }
  }
}

// AI助手面板
:deep(.ai-assistant-popover) {
  padding: 0;
  
  .ai-assistant-panel {
    width: 300px;
    
    .ai-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      
      span {
        font-weight: 500;
        margin: 0 8px;
      }
    }
    
    .ai-features {
      padding: 12px;
      
      .feature-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s ease;
        
        &:hover {
          background: var(--el-fill-color-light);
        }
        
        .feature-info {
          flex: 1;
          
          .feature-name {
            font-size: 13px;
            font-weight: 500;
            color: var(--el-text-color-primary);
            margin-bottom: 2px;
          }
          
          .feature-desc {
            font-size: 11px;
            color: var(--el-text-color-secondary);
          }
        }
        
        .feature-hot {
          flex-shrink: 0;
        }
      }
    }
    
    .ai-shortcuts {
      padding: 8px 12px;
      border-top: 1px solid var(--el-border-color-lighter);
      display: flex;
      flex-direction: column;
      gap: 6px;
      
      .shortcut-item {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        color: var(--el-text-color-secondary);
        
        kbd {
          padding: 2px 6px;
          background: var(--el-fill-color);
          border: 1px solid var(--el-border-color);
          border-radius: 4px;
          font-family: monospace;
        }
      }
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .toolbar-enhanced {
    gap: 8px;
    padding: 6px 8px 0;
    
    .tool-btn {
      width: 32px;
      height: 32px;
    }
  }
}

// 动画
.tool-btn {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>