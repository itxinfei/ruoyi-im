<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    class="call-dialog"
  >
    <div class="call-content">
      <div class="user-info">
        <!-- 群组使用图标，单聊使用钉钉风格头像 -->
        <div v-if="session?.type === 'GROUP'" class="call-avatar group-avatar">
          <span class="material-icons-outlined">groups</span>
        </div>
        <DingtalkAvatar
          v-else
          :src="session?.avatar"
          :name="session?.name"
          :user-id="session?.targetId || session?.targetUserId"
          :size="80"
          shape="circle"
          custom-class="call-avatar"
        />
        <h3 class="call-name">{{ session?.name }}</h3>
        <p class="call-status">{{ statusText }}</p>
      </div>

      <div class="call-actions">
        <!-- 呼叫中 / 被叫中 -->
        <template v-if="status === 'calling' || status === 'incoming'">
          <div v-if="status === 'incoming'" class="incoming-actions">
            <button class="action-btn accept" @click="handleAccept">
              <span class="material-icons-outlined">phone</span>
              <span>接听</span>
            </button>
            <button class="action-btn reject" @click="handleReject">
              <span class="material-icons-outlined">call_end</span>
              <span>挂断</span>
            </button>
          </div>
          <div v-else class="calling-actions">
            <button class="action-btn reject" @click="handleCancel">
              <span class="material-icons-outlined">call_end</span>
              <span>取消</span>
            </button>
          </div>
        </template>

        <!-- 通话中 -->
        <template v-else-if="status === 'talking'">
          <div class="talking-actions">
            <div class="duration">{{ formattedDuration }}</div>
            <button class="action-btn reject" @click="handleHangup">
              <span class="material-icons-outlined">call_end</span>
              <span>挂断</span>
            </button>
          </div>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  session: Object
})

const visible = ref(false)
const status = ref('calling') // calling, incoming, talking, hanging_up
const type = ref('voice') // voice, video
const duration = ref(0)
let timer = null

const title = computed(() => type.value === 'voice' ? '语音通话' : '视频通话')

const statusText = computed(() => {
  switch (status.value) {
    case 'calling': return '正在等待对方接受...'
    case 'incoming': return '邀请你进行通话...'
    case 'talking': return '正在通话中...'
    case 'hanging_up': return '通话已结束'
    default: return ''
  }
})

const formattedDuration = computed(() => {
  const m = Math.floor(duration.value / 60)
  const s = duration.value % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

const open = (callType, initialStatus = 'calling') => {
  type.value = callType
  status.value = initialStatus
  visible.value = true
  duration.value = 0
  if (status.value === 'talking') startTimer()
}

const handleAccept = () => {
  status.value = 'talking'
  startTimer()
}

const handleReject = () => {
  close()
}

const handleCancel = () => {
  close()
}

const handleHangup = () => {
  close()
}

const startTimer = () => {
  stopTimer()
  timer = setInterval(() => {
    duration.value++
  }, 1000)
}

const stopTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const close = () => {
  stopTimer()
  visible.value = false
}

onUnmounted(() => {
  stopTimer()
})

defineExpose({ open, close })
</script>

<style scoped lang="scss">
.call-dialog {
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 40px 0; }
}

.call-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
}

.user-info {
  text-align: center;

  .call-avatar {
    margin-bottom: 16px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }

  .group-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background-color: #1677ff;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36px;
    margin: 0 auto 16px;
  }

  .call-name {
    font-size: 20px;
    font-weight: 600;
    margin: 8px 0;
    color: #1f2329;
  }

  .call-status {
    font-size: 14px;
    color: #8f959e;
  }
}

.call-actions {
  width: 100%;
  padding: 0 40px;
}

.incoming-actions, .calling-actions, .talking-actions {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.talking-actions {
  flex-direction: column;
  gap: 16px;
  .duration { font-size: 18px; font-weight: 500; color: #1f2329; }
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  cursor: pointer;
  transition: transform 0.2s;

  &:hover { transform: scale(1.1); }
  
  .material-icons-outlined {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 28px;
  }
  
  span:not(.material-icons-outlined) {
    font-size: 13px;
    color: #646a73;
  }

  &.accept .material-icons-outlined { background-color: #52c41a; }
  &.reject .material-icons-outlined { background-color: #f54a45; }
}
</style>
