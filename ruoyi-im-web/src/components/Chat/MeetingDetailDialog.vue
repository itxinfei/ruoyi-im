<template>
  <el-dialog
    v-model="visible"
    :title="meeting?.title || '会议详情'"
    width="500px"
    @close="handleClose"
  >
    <div
      v-if="meeting"
      class="meeting-detail"
    >
      <!-- 状态标签 -->
      <div
        class="status-badge"
        :class="statusClass"
      >
        {{ statusText }}
      </div>

      <!-- 会议信息 -->
      <div class="detail-section">
        <div class="section-title">
          会议信息
        </div>
        <div class="info-list">
          <div class="info-item">
            <span class="material-icons-outlined info-icon">person</span>
            <div class="info-content">
              <span class="info-label">主持人</span>
              <span class="info-value">{{ meeting.hostName }}</span>
            </div>
          </div>
          <div class="info-item">
            <span class="material-icons-outlined info-icon">schedule</span>
            <div class="info-content">
              <span class="info-label">开始时间</span>
              <span class="info-value">{{ formatFullTime(meeting.scheduledStartTime) }}</span>
            </div>
          </div>
          <div
            v-if="meeting.scheduledEndTime"
            class="info-item"
          >
            <span class="material-icons-outlined info-icon">access_time</span>
            <div class="info-content">
              <span class="info-label">结束时间</span>
              <span class="info-value">{{ formatFullTime(meeting.scheduledEndTime) }}</span>
            </div>
          </div>
          <div class="info-item">
            <span class="material-icons-outlined info-icon">meeting_room</span>
            <div class="info-content">
              <span class="info-label">会议号</span>
              <span class="info-value">{{ meeting.roomId || '-' }}</span>
            </div>
          </div>
          <div
            v-if="meeting.password"
            class="info-item"
          >
            <span class="material-icons-outlined info-icon">lock</span>
            <div class="info-content">
              <span class="info-label">会议密码</span>
              <span class="info-value">{{ meeting.password }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 会议描述 -->
      <div
        v-if="meeting.description"
        class="detail-section"
      >
        <div class="section-title">
          会议描述
        </div>
        <div class="description">
          {{ meeting.description }}
        </div>
      </div>

      <!-- 参会人员 -->
      <div class="detail-section">
        <div class="section-title">
          参会人员
          <span class="participant-count">({{ participantCount }}人)</span>
        </div>
        <div class="participant-list">
          <div
            v-for="participant in participants"
            :key="participant.id"
            class="participant-item"
          >
            <DingtalkAvatar
              :src="participant.avatar"
              :name="participant.name"
              :user-id="participant.id"
              :size="32"
              shape="square"
            />
            <span class="participant-name">{{ participant.name }}</span>
            <el-tag
              v-if="participant.role === 'HOST'"
              size="small"
              type="warning"
            >
              主持人
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">
        关闭
      </el-button>
      <el-button
        v-if="meeting?.status === 'IN_PROGRESS'"
        type="primary"
        @click="handleJoin"
      >
        加入会议
      </el-button>
      <el-button
        v-else-if="meeting?.status === 'SCHEDULED' && meeting?.isHost"
        type="primary"
        @click="handleStart"
      >
        开始会议
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  modelValue: Boolean,
  meeting: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'join', 'start'])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const participants = ref([])

const statusClass = computed(() => {
  switch (props.meeting?.status) {
    case 'SCHEDULED': return 'status-scheduled'
    case 'IN_PROGRESS': return 'status-progress'
    case 'COMPLETED': return 'status-completed'
    case 'CANCELLED': return 'status-cancelled'
    default: return ''
  }
})

const statusText = computed(() => {
  switch (props.meeting?.status) {
    case 'SCHEDULED': return '已预约'
    case 'IN_PROGRESS': return '进行中'
    case 'COMPLETED': return '已结束'
    case 'CANCELLED': return '已取消'
    default: return ''
  }
})

const participantCount = computed(() => {
  return props.meeting?.participantCount || participants.value.length || 0
})

const formatFullTime = time => {
  if (!time) {return '-'}
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleClose = () => {
  visible.value = false
}

const handleJoin = () => {
  emit('join', props.meeting)
  visible.value = false
}

const handleStart = () => {
  emit('start', props.meeting)
  visible.value = false
}

watch(() => props.meeting, newMeeting => {
  if (newMeeting?.participants) {
    participants.value = newMeeting.participants
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.meeting-detail {
  position: relative;

  .status-badge {
    position: absolute;
    top: 0;
    right: 0;
    padding: 4px 12px;
    border-radius: var(--dt-radius-lg);
    font-size: 12px;
    font-weight: 500;

    &.status-scheduled {
      background: rgba(0, 137, 255, 0.1);
      color: var(--dt-brand-color);
    }

    &.status-progress {
      background: rgba(5, 150, 105, 0.1);
      color: #059669;
    }

    &.status-completed {
      background: rgba(107, 114, 128, 0.1);
      color: #6b7280;
    }

    &.status-cancelled {
      background: rgba(220, 38, 38, 0.1);
      color: #dc2626;
    }
  }

  .detail-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary);
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 4px;

      .participant-count {
        font-size: 12px;
        color: var(--dt-text-tertiary);
        font-weight: normal;
      }
    }

    .description {
      font-size: 14px;
      color: var(--dt-text-secondary);
      line-height: 1.6;
      padding: 12px;
      background: var(--dt-bg-body);
      border-radius: var(--dt-radius-md);
    }
  }

  .info-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .info-item {
      display: flex;
      align-items: center;
      gap: 12px;

      .info-icon {
        font-size: 18px;
        color: var(--dt-text-tertiary);
      }

      .info-content {
        display: flex;
        flex-direction: column;
        gap: 2px;

        .info-label {
          font-size: 12px;
          color: var(--dt-text-tertiary);
        }

        .info-value {
          font-size: 14px;
          color: var(--dt-text-primary);
        }
      }
    }
  }

  .participant-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .participant-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 10px;
      background: var(--dt-bg-body);
      border-radius: var(--dt-radius-2xl);
      font-size: 13px;
      color: var(--dt-text-secondary);

      .participant-name {
        max-width: 80px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

// 暗色模式适配
.dark .meeting-detail {
  .description {
    background: var(--dt-bg-hover);
  }

  .participant-list .participant-item {
    background: var(--dt-bg-hover);
  }
}
</style>
