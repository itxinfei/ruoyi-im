<template>
  <div class="meeting-list-panel">
    <!-- 头部 -->
    <div class="panel-header">
      <div class="header-left">
        <span class="material-icons-outlined header-icon">videocam</span>
        <span class="header-title">视频会议</span>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleCreateMeeting">
          发起会议
        </el-button>
        <el-button :icon="Refresh" @click="loadMeetings" :loading="loading" />
      </div>
    </div>

    <!-- 分类标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="meeting-tabs">
      <el-tab-pane name="upcoming">
        <template #label>
          <span class="tab-item">
            <span>即将开始</span>
            <span v-if="upcomingCount > 0" class="count-badge">{{ upcomingCount }}</span>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="in-progress">
        <template #label>
          <span class="tab-item">
            <span class="live-dot"></span>
            <span>进行中</span>
            <span v-if="inProgressCount > 0" class="count-badge live">{{ inProgressCount }}</span>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="completed">
        <template #label>
          <span class="tab-item">
            <span>已结束</span>
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 会议列表 -->
    <div class="meeting-list-container" v-loading="loading">
      <!-- 空状态 -->
      <div v-if="!loading && filteredMeetings.length === 0" class="empty-state">
        <span class="material-icons-outlined empty-icon">videocam_off</span>
        <span class="empty-text">{{ emptyText }}</span>
        <el-button v-if="activeTab === 'upcoming'" type="primary" plain @click="handleCreateMeeting">
          发起会议
        </el-button>
      </div>

      <!-- 会议卡片列表 -->
      <div v-else class="meeting-list">
        <div
          v-for="meeting in filteredMeetings"
          :key="meeting.id"
          class="meeting-card"
          :class="{ 'is-live': meeting.status === 'IN_PROGRESS' }"
        >
          <!-- 会议封面/头像 -->
          <div class="meeting-cover">
            <div v-if="meeting.status === 'IN_PROGRESS'" class="live-badge">
              <span class="live-dot"></span>
              进行中
            </div>
            <span v-else class="meeting-icon">
              <span class="material-icons-outlined">groups</span>
            </span>
          </div>

          <!-- 会议信息 -->
          <div class="meeting-info">
            <div class="meeting-title">{{ meeting.title }}</div>
            <div class="meeting-desc">{{ meeting.description || '无描述' }}</div>
            <div class="meeting-meta">
              <span class="meta-item">
                <span class="material-icons-outlined">person</span>
                {{ meeting.hostName }}
              </span>
              <span class="meta-item">
                <span class="material-icons-outlined">schedule</span>
                {{ formatTime(meeting.scheduledStartTime) }}
              </span>
              <span class="meta-item">
                <span class="material-icons-outlined">group</span>
                {{ meeting.participantCount || 0 }}人
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="meeting-actions">
            <template v-if="meeting.status === 'SCHEDULED'">
              <!-- 预定状态 -->
              <el-button
                v-if="meeting.isHost"
                type="primary"
                :icon="PlayArrow"
                @click="handleStartMeeting(meeting)"
              >
                开始会议
              </el-button>
              <el-button
                v-else
                type="success"
                :icon="Login"
                @click="handleJoinMeeting(meeting)"
              >
                加入会议
              </el-button>
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, meeting)">
                <el-button :icon="MoreFilled" circle />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="detail">
                      <span class="material-icons-outlined">info</span>
                      查看详情
                    </el-dropdown-item>
                    <el-dropdown-item v-if="meeting.isHost" command="edit">
                      <span class="material-icons-outlined">edit</span>
                      编辑会议
                    </el-dropdown-item>
                    <el-dropdown-item v-if="meeting.isHost" command="cancel" class="danger-item">
                      <span class="material-icons-outlined">cancel</span>
                      取消会议
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>

            <template v-else-if="meeting.status === 'IN_PROGRESS'">
              <!-- 进行中状态 -->
              <el-button
                type="success"
                :icon="Login"
                @click="handleJoinMeeting(meeting)"
              >
                加入会议
              </el-button>
              <el-button
                v-if="meeting.isHost"
                type="danger"
                :icon="Stop"
                @click="handleEndMeeting(meeting)"
              >
                结束会议
              </el-button>
            </template>

            <template v-else>
              <!-- 已结束状态 -->
              <el-button
                plain
                :icon="History"
                @click="handleViewDetail(meeting)"
              >
                查看回放
              </el-button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建会议对话框 -->
    <CreateMeetingDialog
      v-model="showCreateDialog"
      @success="handleCreateSuccess"
    />

    <!-- 会议详情对话框 -->
    <MeetingDetailDialog
      v-model="showDetailDialog"
      :meeting="selectedMeeting"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Refresh,
  PlayArrow,
  Login,
  Stop,
  MoreFilled,
  History
} from '@element-plus/icons-vue'
import { getMeetingList, startMeeting, endMeeting, cancelMeeting, joinMeeting } from '@/api/im/meeting'
import CreateMeetingDialog from './CreateMeetingDialog.vue'
import MeetingDetailDialog from './MeetingDetailDialog.vue'

const props = defineProps({
  currentUser: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['join-meeting', 'start-meeting'])

// 状态
const loading = ref(false)
const activeTab = ref('upcoming')
const meetings = ref([])
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const selectedMeeting = ref(null)

// 计算属性
const filteredMeetings = computed(() => {
  if (activeTab.value === 'upcoming') {
    return meetings.value.filter(m => m.status === 'SCHEDULED')
  } else if (activeTab.value === 'in-progress') {
    return meetings.value.filter(m => m.status === 'IN_PROGRESS')
  } else {
    return meetings.value.filter(m => m.status === 'COMPLETED')
  }
})

const upcomingCount = computed(() => {
  return meetings.value.filter(m => m.status === 'SCHEDULED').length
})

const inProgressCount = computed(() => {
  return meetings.value.filter(m => m.status === 'IN_PROGRESS').length
})

const emptyText = computed(() => {
  if (activeTab.value === 'upcoming') return '暂无即将开始的会议'
  if (activeTab.value === 'in-progress') return '暂无进行中的会议'
  return '暂无历史会议'
})

// 加载会议列表
const loadMeetings = async () => {
  loading.value = true
  try {
    const res = await getMeetingList()
    if (res.code === 200) {
      // 添加是否为主持人的标记
      const currentUserId = props.currentUser?.id
      meetings.value = (res.data || []).map(m => ({
        ...m,
        isHost: m.hostId === currentUserId
      }))
    }
  } catch (error) {
    console.error('加载会议列表失败:', error)
    ElMessage.error('加载会议列表失败')
  } finally {
    loading.value = false
  }
}

// 处理创建会议
const handleCreateMeeting = () => {
  showCreateDialog.value = true
}

// 处理创建成功
const handleCreateSuccess = (meeting) => {
  loadMeetings()
  emit('start-meeting', meeting)
}

// 处理开始会议
const handleStartMeeting = async (meeting) => {
  try {
    const res = await startMeeting(meeting.id)
    if (res.code === 200) {
      ElMessage.success('会议已开始')
      emit('start-meeting', { ...meeting, status: 'IN_PROGRESS' })
      loadMeetings()
    }
  } catch (error) {
    ElMessage.error('开始会议失败')
  }
}

// 处理加入会议
const handleJoinMeeting = async (meeting) => {
  try {
    const res = await joinMeeting(meeting.id)
    if (res.code === 200) {
      emit('join-meeting', res.data)
    }
  } catch (error) {
    ElMessage.error('加入会议失败')
  }
}

// 处理结束会议
const handleEndMeeting = async (meeting) => {
  try {
    await ElMessageBox.confirm('确定要结束会议吗？', '结束会议', {
      confirmButtonText: '结束',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await endMeeting(meeting.id)
    if (res.code === 200) {
      ElMessage.success('会议已结束')
      loadMeetings()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('结束会议失败')
    }
  }
}

// 处理菜单命令
const handleCommand = async (command, meeting) => {
  switch (command) {
    case 'detail':
      handleViewDetail(meeting)
      break
    case 'edit':
      // TODO: 打开编辑对话框
      ElMessage.info('编辑功能开发中')
      break
    case 'cancel':
      await handleCancelMeeting(meeting)
      break
  }
}

// 处理取消会议
const handleCancelMeeting = async (meeting) => {
  try {
    await ElMessageBox.confirm(`确定要取消会议"${meeting.title}"吗？`, '取消会议', {
      confirmButtonText: '取消会议',
      cancelButtonText: '保留',
      type: 'warning'
    })

    const res = await cancelMeeting(meeting.id)
    if (res.code === 200) {
      ElMessage.success('会议已取消')
      loadMeetings()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消会议失败')
    }
  }
}

// 查看详情
const handleViewDetail = (meeting) => {
  selectedMeeting.value = meeting
  showDetailDialog.value = true
}

// 标签切换
const handleTabChange = () => {
  // 可以在这里添加特殊的加载逻辑
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '待定'
  const date = new Date(time)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const meetingDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  const timeStr = date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })

  const diffDays = Math.floor((today - meetingDate) / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return `今天 ${timeStr}`
  if (diffDays === 1) return `明天 ${timeStr}`

  return `${date.getMonth() + 1}月${date.getDate()}日 ${timeStr}`
}

// 组件挂载时加载数据
onMounted(() => {
  loadMeetings()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.meeting-list-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--dt-bg-card);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-lighter);

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .header-icon {
      font-size: 20px;
      color: var(--dt-brand-color);
    }

    .header-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--dt-text-primary);
    }
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.meeting-tabs {
  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 20px;
    border-bottom: 1px solid var(--dt-border-lighter);
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    padding: 12px 16px;
    font-size: 14px;
    color: var(--dt-text-secondary);

    &.is-active {
      color: var(--dt-brand-color);
      font-weight: 500;
    }
  }

  .tab-item {
    display: flex;
    align-items: center;
    gap: 6px;

    .live-dot {
      width: 6px;
      height: 6px;
      background: var(--dt-error-color);
      border-radius: 50%;
      animation: pulse 1.5s infinite;
    }

    .count-badge {
      font-size: 11px;
      padding: 2px 6px;
      border-radius: 10px;
      background: var(--dt-bg-hover);
      color: var(--dt-text-secondary);

      &.live {
        background: rgba(var(--dt-error-rgb, 220, 38, 38), 0.1);
        color: var(--dt-error-color);
      }
    }
  }

  @keyframes pulse {
    0%, 100% {
      opacity: 1;
    }
    50% {
      opacity: 0.5;
    }
  }
}

.meeting-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border);
    border-radius: 3px;

    &:hover {
      background: var(--dt-border-dark);
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  color: var(--dt-text-tertiary);
  text-align: center;

  .empty-icon {
    font-size: 48px;
    opacity: 0.3;
    margin-bottom: 12px;
  }

  .empty-text {
    font-size: 14px;
    color: var(--dt-text-secondary);
    margin-bottom: 16px;
  }
}

.meeting-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meeting-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-lighter);
  border-radius: 12px;
  transition: all 0.2s;

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 2px 8px rgba(22, 119, 255, 0.1);
  }

  &.is-live {
    background: linear-gradient(135deg, rgba(22, 119, 255, 0.05) 0%, rgba(22, 119, 255, 0.02) 100%);
    border-color: rgba(22, 119, 255, 0.2);
  }

  .meeting-cover {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--dt-bg-hover);
    border-radius: 8px;
    flex-shrink: 0;
    position: relative;

    .live-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 2px 6px;
      background: var(--dt-error-color);
      color: white;
      font-size: 10px;
      border-radius: 10px;
      white-space: nowrap;

      .live-dot {
        width: 4px;
        height: 4px;
        background: white;
        border-radius: 50%;
        animation: pulse 1.5s infinite;
      }
    }

    .meeting-icon {
      font-size: 24px;
      color: var(--dt-brand-color);

      .material-icons-outlined {
        font-size: 24px;
      }
    }
  }

  .meeting-info {
    flex: 1;
    min-width: 0;

    .meeting-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary);
      margin-bottom: 4px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .meeting-desc {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      margin-bottom: 6px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .meeting-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: var(--dt-text-secondary);

        .material-icons-outlined {
          font-size: 14px;
        }
      }
    }
  }

  .meeting-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;

    .danger-item {
      color: var(--dt-error-color);
    }
  }
}

// 暗色模式适配
.dark .meeting-card {
  &:hover {
    box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
  }
}
</style>
