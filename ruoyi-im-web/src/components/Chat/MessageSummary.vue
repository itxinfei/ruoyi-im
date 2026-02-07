<template>
  <teleport to="body">
    <transition name="modal-fade">
      <div
        v-if="visible"
        class="message-summary-modal"
        @click.self="handleClose"
      >
        <div
          class="summary-container"
          @click.stop
        >
          <!-- 头部 -->
          <div class="summary-header">
            <div class="header-left">
              <span class="material-icons-outlined header-icon">summarize</span>
              <h3 class="header-title">
                消息速读
              </h3>
              <span class="message-count">{{ messages.length }} 条消息</span>
            </div>
            <div class="header-actions">
              <button
                class="icon-btn"
                :class="{ loading }"
                @click="handleRefresh"
              >
                <span class="material-icons-outlined">refresh</span>
              </button>
              <button
                class="icon-btn"
                @click="handleClose"
              >
                <span class="material-icons-outlined">close</span>
              </button>
            </div>
          </div>

          <!-- 生成进度 -->
          <div
            v-if="loading"
            class="summary-loading"
          >
            <div class="loading-spinner" />
            <p class="loading-text">
              正在分析消息...
            </p>
          </div>

          <!-- 摘要内容 -->
          <div
            v-else
            class="summary-content"
          >
            <!-- 关键信息概览 -->
            <div
              v-if="summary"
              class="summary-overview"
            >
              <div class="overview-item">
                <span class="overview-icon topic-icon">
                  <span class="material-icons-outlined">topic</span>
                </span>
                <div class="overview-content">
                  <span class="overview-label">讨论主题</span>
                  <p class="overview-value">
                    {{ summary.topic || '暂无明确主题' }}
                  </p>
                </div>
              </div>
              <div class="overview-item">
                <span class="overview-icon time-icon">
                  <span class="material-icons-outlined">schedule</span>
                </span>
                <div class="overview-content">
                  <span class="overview-label">时间跨度</span>
                  <p class="overview-value">
                    {{ summary.timeRange }}
                  </p>
                </div>
              </div>
              <div class="overview-item">
                <span class="overview-icon participant-icon">
                  <span class="material-icons-outlined">people</span>
                </span>
                <div class="overview-content">
                  <span class="overview-label">参与人数</span>
                  <p class="overview-value">
                    {{ summary.participantCount }} 人
                  </p>
                </div>
              </div>
            </div>

            <!-- 关键要点 -->
            <div
              v-if="summary.keyPoints && summary.keyPoints.length > 0"
              class="summary-keypoints"
            >
              <div class="keypoints-header">
                <span class="material-icons-outlined">lightbulb</span>
                <span class="keypoints-title">关键要点</span>
              </div>
              <ul class="keypoints-list">
                <li
                  v-for="(point, index) in summary.keyPoints"
                  :key="index"
                  class="keypoint-item"
                >
                  <span class="keypoint-bullet">{{ index + 1 }}</span>
                  <span class="keypoint-text">{{ point }}</span>
                </li>
              </ul>
            </div>

            <!-- 消息时间线 -->
            <div class="summary-timeline">
              <div class="timeline-header">
                <span class="material-icons-outlined">timeline</span>
                <span class="timeline-title">消息时间线</span>
              </div>
              <div class="timeline-list">
                <div
                  v-for="(item, index) in timelineItems"
                  :key="index"
                  class="timeline-item"
                  @click="handleScrollToMessage(item.messageId)"
                >
                  <div class="timeline-avatar">
                    <img
                      v-if="item.avatar"
                      :src="item.avatar"
                      :alt="item.senderName"
                    >
                    <span
                      v-else
                      class="avatar-placeholder"
                    >{{ item.senderName?.charAt(0) || '?' }}</span>
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <span class="timeline-name">{{ item.senderName }}</span>
                      <span class="timeline-time">{{ formatTime(item.timestamp) }}</span>
                    </div>
                    <p class="timeline-text">
                      {{ item.summary }}
                    </p>
                    <span
                      v-if="item.attachment"
                      class="timeline-attachment"
                    >
                      <span class="material-icons-outlined">{{ getAttachmentIcon(item.type) }}</span>
                      {{ item.attachment }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 待办事项 -->
            <div
              v-if="summary.todos && summary.todos.length > 0"
              class="summary-todos"
            >
              <div class="todos-header">
                <span class="material-icons-outlined">check_circle</span>
                <span class="todos-title">待办事项</span>
              </div>
              <div class="todos-list">
                <div
                  v-for="(todo, index) in summary.todos"
                  :key="index"
                  class="todo-item"
                  @click="handleCreateTodo(todo)"
                >
                  <span class="todo-checkbox">
                    <span class="material-icons-outlined">check_box_outline_blank</span>
                  </span>
                  <span class="todo-text">{{ todo }}</span>
                  <span class="todo-action">
                    <span class="material-icons-outlined">add_circle</span>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部操作 -->
          <div class="summary-footer">
            <button
              class="footer-btn secondary"
              @click="handleViewFullHistory"
            >
              <span class="material-icons-outlined">history</span>
              查看完整记录
            </button>
            <button
              class="footer-btn primary"
              @click="handleClose"
            >
              <span class="material-icons-outlined">done</span>
              我知道了
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { formatRelativeTime } from '@/utils/format'

const props = defineProps({
  visible: { type: Boolean, default: false },
  conversationId: { type: [String, Number], default: null }
})

const emit = defineEmits(['close', 'scroll-to-message', 'create-todo'])

const loading = ref(false)
const messages = ref([])

// 模拟摘要数据
const summary = ref({
  topic: '',
  timeRange: '',
  participantCount: 0,
  keyPoints: [],
  todos: []
})

// 时间线项目
const timelineItems = ref([])

// 生成摘要
const generateSummary = async () => {
  loading.value = true

  // 模拟API调用延迟
  await new Promise(resolve => setTimeout(resolve, 800))

  // 这里应该调用后端API获取真实的AI摘要
  // 现在使用模拟数据演示UI
  summary.value = {
    topic: 'Q4产品规划讨论',
    timeRange: '今天 14:30 - 16:45',
    participantCount: 5,
    keyPoints: [
      '确定了Q4产品优先级，以用户体验改进为核心目标',
      '讨论了新功能上线时间表，预计11月中旬开始灰度测试',
      '确定了技术方案：采用微服务架构重构核心模块'
    ],
    todos: [
      '完成产品需求文档修订',
      '安排技术评审会议',
      '确认UI设计稿交付时间'
    ]
  }

  // 模拟时间线数据
  timelineItems.value = [
    {
      messageId: 'msg-1',
      senderName: '产品经理',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=PM',
      timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000),
      summary: '发起Q4产品规划讨论，重点关注用户体验改进',
      type: 'TEXT'
    },
    {
      messageId: 'msg-2',
      senderName: '技术负责人',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Tech',
      timestamp: new Date(Date.now() - 1.8 * 60 * 60 * 1000),
      summary: '建议采用微服务架构重构核心模块',
      type: 'TEXT'
    },
    {
      messageId: 'msg-3',
      senderName: 'UI设计师',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=UI',
      timestamp: new Date(Date.now() - 1.5 * 60 * 60 * 1000),
      summary: '设计稿预计下周初完成',
      type: 'TEXT'
    },
    {
      messageId: 'msg-4',
      senderName: '测试工程师',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=QA',
      timestamp: new Date(Date.now() - 1 * 60 * 60 * 1000),
      summary: '同意此计划，测试团队已做好准备',
      type: 'TEXT'
    }
  ]

  loading.value = false
}

// 刷新摘要
const handleRefresh = () => {
  generateSummary()
}

// 关闭弹窗
const handleClose = () => {
  emit('close')
}

// 滚动到指定消息
const handleScrollToMessage = messageId => {
  emit('scroll-to-message', messageId)
  handleClose()
}

// 创建待办
const handleCreateTodo = todoText => {
  emit('create-todo', todoText)
  ElMessage.success('已添加到待办')
}

// 查看完整历史
const handleViewFullHistory = () => {
  handleClose()
  ElMessage.info('滚动到消息顶部查看完整记录')
}

// 格式化时间
const formatTime = formatRelativeTime

// 获取附件图标
const getAttachmentIcon = type => {
  const icons = {
    IMAGE: 'image',
    FILE: 'insert_drive_file',
    VIDEO: 'videocam',
    AUDIO: 'mic',
    LINK: 'link'
  }
  return icons[type] || 'attach_file'
}

// 监听显示状态
watch(() => props.visible, newVal => {
  if (newVal) {
    generateSummary()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/z-index.scss' as *;

// ============================================================================
// 遮罩层
// ============================================================================
.message-summary-modal {
  position: fixed;
  inset: 0;
  background: var(--dt-screenshot-mask);
  backdrop-filter: blur(4px);
  z-index: $z-modal-top;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

// 模态弹出动画
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.3s var(--dt-ease-out);
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .summary-container,
.modal-fade-leave-to .summary-container {
  transform: scale(0.95) translateY(-20px);
}

// ============================================================================
// 容器
// ============================================================================
.summary-container {
  width: 100%;
  max-width: 560px;
  max-height: 80vh;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  box-shadow: 0 20px 60px var(--dt-screenshot-shadow-light);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s var(--dt-ease-out);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// ============================================================================
// 头部
// ============================================================================
.summary-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);
  background: linear-gradient(135deg, var(--dt-brand-bg) 0%, var(--dt-brand-lighter) 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 20px;
  color: var(--dt-brand-color);
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.message-count {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  background: var(--dt-bg-input);
  padding: 2px 8px;
  border-radius: var(--dt-radius-lg);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
  }

  &.loading {
    animation: spin 1s linear infinite;
    color: var(--dt-brand-color);
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

// ============================================================================
// 加载状态
// ============================================================================
.summary-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--dt-border-color);
  border-top-color: var(--dt-brand-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-text {
  font-size: 14px;
  color: var(--dt-text-secondary);
  margin: 0;
}

// ============================================================================
// 内容区
// ============================================================================
.summary-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
  }
}

// 概览卡片
.summary-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.overview-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  background: var(--dt-bg-input);
  border-radius: var(--dt-radius-lg);
  border: 1px solid var(--dt-border-light);
}

.overview-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-md);
  flex-shrink: 0;

  &.topic-icon { background: var(--dt-brand-light); color: var(--dt-brand-color); }
  &.time-icon { background: var(--dt-success-05); color: var(--dt-success-color); }
  &.participant-icon { background: var(--dt-warning-bg); color: var(--dt-warning-color); }

  .material-icons-outlined {
    font-size: 16px;
  }
}

.overview-content {
  flex: 1;
  min-width: 0;
}

.overview-label {
  font-size: 11px;
  color: var(--dt-text-quaternary);
  display: block;
  margin-bottom: 4px;
}

.overview-value {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin: 0;
  line-height: 1.4;
}

// 关键要点
.summary-keypoints {
  margin-bottom: 20px;
}

.keypoints-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  color: var(--dt-brand-color);
  font-size: 14px;
  font-weight: 600;

  .material-icons-outlined {
    font-size: 18px;
  }
}

.keypoints-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.keypoint-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  margin-bottom: 8px;
  background: var(--dt-bg-input);
  border-radius: var(--dt-radius-md);
  border-left: 3px solid var(--dt-brand-color);
}

.keypoint-bullet {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  border-radius: 50%;
  font-size: 11px;
  font-weight: 600;
}

.keypoint-text {
  flex: 1;
  font-size: 13px;
  color: var(--dt-text-primary);
  line-height: 1.5;
}

// 时间线
.summary-timeline {
  margin-bottom: 20px;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  color: var(--dt-text-secondary);
  font-size: 14px;
  font-weight: 600;

  .material-icons-outlined {
    font-size: 18px;
  }
}

.timeline-list {
  position: relative;
  padding-left: 20px;
}

.timeline-list::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 12px;
  bottom: 12px;
  width: 2px;
  background: var(--dt-border-color);
}

.timeline-item {
  position: relative;
  display: flex;
  gap: 12px;
  padding-bottom: 16px;
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:last-child {
    padding-bottom: 0;
  }

  &::before {
    content: '';
    position: absolute;
    left: -20px;
    top: 16px;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    border: 2px solid var(--dt-bg-card);
    background: var(--dt-brand-color);
  }

  &:hover {
    .timeline-content {
      background: var(--dt-bg-hover);
    }
  }
}

.timeline-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  background: var(--dt-bg-input);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--dt-brand-color);
    color: #fff;
    font-size: 14px;
    font-weight: 600;
  }
}

.timeline-content {
  flex: 1;
  padding: 10px 12px;
  background: var(--dt-bg-input);
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-base);
}

.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.timeline-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.timeline-time {
  font-size: 11px;
  color: var(--dt-text-quaternary);
}

.timeline-text {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 4px 0 0 0;
  line-height: 1.5;
}

.timeline-attachment {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  padding: 4px 8px;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  border-radius: var(--dt-radius-sm);
  font-size: 11px;

  .material-icons-outlined {
    font-size: 12px;
  }
}

// 待办事项
.summary-todos {
  margin-bottom: 20px;
}

.todos-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  color: var(--dt-warning-color);
  font-size: 14px;
  font-weight: 600;

  .material-icons-outlined {
    font-size: 18px;
  }
}

.todos-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
    background: var(--dt-warning-bg);
    border-color: var(--dt-warning-border);

    .todo-action {
      opacity: 1;
    }
  }
}

.todo-checkbox {
  flex-shrink: 0;
  color: var(--dt-text-quaternary);

  .material-icons-outlined {
    font-size: 20px;
  }
}

.todo-text {
  flex: 1;
  font-size: 13px;
  color: var(--dt-text-primary);
}

.todo-action {
  opacity: 0;
  color: var(--dt-warning-hover);
  transition: opacity var(--dt-transition-fast);

  .material-icons-outlined {
    font-size: 18px;
  }
}

// ============================================================================
// 底部操作
// ============================================================================
.summary-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);
}

.footer-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: var(--dt-radius-lg);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-base);

  .material-icons-outlined {
    font-size: 18px;
  }

  &.secondary {
    background: var(--dt-bg-input);
    color: var(--dt-text-secondary);

    &:hover {
      background: var(--dt-bg-hover);
    }
  }

  &.primary {
    background: var(--dt-brand-color);
    color: #fff;

    &:hover {
      background: var(--dt-brand-hover);
    }
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
:global(.dark) {
  .summary-container {
    background: var(--dt-bg-card-dark);
    box-shadow: 0 20px 60px var(--dt-screenshot-shadow-card);
  }

  .summary-header {
    border-color: var(--dt-border-dark);
    background: linear-gradient(135deg, var(--dt-brand-extra-light) 0%, var(--dt-brand-lighter) 100%);
  }

  .message-count {
    background: var(--dt-bg-input-dark);
  }

  .overview-item,
  .keypoint-item,
  .todo-item,
  .timeline-content {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);
  }

  .keypoint-item {
    border-left-color: var(--dt-brand-color);
  }

  .timeline-item::before {
    border-color: var(--dt-bg-card-dark);
  }

  .summary-footer {
    border-color: var(--dt-border-dark);
    background: var(--dt-bg-body-dark);
  }

  .footer-btn.secondary {
    background: var(--dt-bg-input-dark);
    color: var(--dt-text-secondary-dark);
  }
}
</style>
