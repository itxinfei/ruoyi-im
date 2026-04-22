<template>
  <div class="call-history-v2">
    <!-- 1. 顶部 Header -->
    <header class="view-header">
      <div class="header-left">
        <h2 class="view-title">通话记录</h2>
      </div>
    </header>

    <main class="view-body">
      <!-- 2. 通话记录列表 (对齐邮件列表风格) -->
      <div class="call-list-container custom-scrollbar">
        <div v-if="calls.length === 0" class="empty-view">暂无通话记录</div>
        <div
          v-for="call in calls"
          :key="call.id"
          class="call-row"
          :class="{ missed: call.status === 'MISSED' }"
          @click="selectedCall = call"
        >
          <div class="call-avatar">
            <DingtalkAvatar :src="call.peerAvatar" :name="call.peerName" :size="36" shape="square" />
          </div>
          <div class="call-main-info">
            <div class="name-row">
              <span class="name">{{ call.peerName }}</span>
              <span class="time">{{ formatTime(call.startTime) }}</span>
            </div>
            <div class="status-row">
              <el-icon :class="call.type"><component :is="call.type === 'video' ? VideoCamera : Phone" /></el-icon>
              <span class="status-text">{{ getStatusText(call) }}</span>
            </div>
          </div>
          <div class="call-ops">
            <el-icon class="call-back-btn" @click.stop="handleRedial(call)"><PhoneFilled /></el-icon>
          </div>
        </div>
      </div>

      <!-- 3. 右侧详情预览 -->
      <aside class="call-detail-pane">
        <div v-if="selectedCall" class="detail-container">
          <div class="detail-header">
            <DingtalkAvatar :src="selectedCall.peerAvatar" :name="selectedCall.peerName" :size="64" shape="square" />
            <h3>{{ selectedCall.peerName }}</h3>
            <div class="action-row">
              <el-button type="primary" :icon="PhoneFilled">语音通话</el-button>
              <el-button :icon="VideoCameraFilled">视频通话</el-button>
            </div>
          </div>
          <div class="detail-body">
            <div class="info-group">
              <label>通话时间</label>
              <span>{{ formatFullTime(selectedCall.startTime) }}</span>
            </div>
            <div class="info-group">
              <label>通话时长</label>
              <span>{{ selectedCall.duration || '--:--' }}</span>
            </div>
          </div>
        </div>
        <div v-else class="detail-empty">选择一条记录查看详情</div>
      </aside>
    </main>
  </div>
</template>

<script setup lang="js">
import { ref } from 'vue'
import { Phone, VideoCamera, PhoneFilled, VideoCameraFilled } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const calls = ref([
  { id: 1, peerName: '架构师小王', type: 'video', status: 'COMPLETED', startTime: Date.now() - 3600000, duration: '12:05' },
  { id: 2, peerName: '前端小李', type: 'voice', status: 'MISSED', startTime: Date.now() - 86400000, duration: 0 }
])
const selectedCall = ref(null)

const getStatusText = (c) => c.status === 'MISSED' ? '未接来电' : '已接通'
const formatTime = (t) => new Date(t).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
const formatFullTime = (t) => new Date(t).toLocaleString()

const handleRedial = (_call) => { /* TODO: 拨号逻辑 */ }
</script>

<style scoped lang="scss">
.call-history-v2 { display: flex; flex-direction: column; height: 100%; background: var(--dt-bg-card); }

.view-header { height: 56px; padding: 0 24px; border-bottom: 1px solid var(--dt-border-light); @include flex-center; .view-title { font-size: 16px; font-weight: 700; } }

.view-body { flex: 1; display: flex; overflow: hidden; }

.call-list-container { width: 320px; border-right: 1px solid var(--dt-border-light); overflow-y: auto;
  .call-row {
    height: 68px; display: flex; align-items: center; padding: 0 16px; gap: 12px; cursor: pointer; transition: var(--dt-transition-fast);
    &:hover { background: var(--dt-bg-hover); .call-ops { opacity: 1; } }
    &.missed { .name, .status-text { color: var(--dt-error-color); } }
    .call-main-info { flex: 1; min-width: 0; .name-row { display: flex; justify-content: space-between; .name { font-weight: 600; font-size: 14px; color: var(--dt-text-primary); } .time { font-size: 11px; color: var(--dt-text-tertiary); } } .status-row { display: flex; align-items: center; gap: 6px; font-size: 11px; color: var(--dt-text-tertiary); .video { color: var(--dt-brand-color); } } }
    .call-ops { opacity: 0; transition: var(--dt-transition-fast); .call-back-btn { font-size: 18px; color: var(--dt-brand-color); cursor: pointer; } }
  }
}

.call-detail-pane { flex: 1; background: var(--dt-bg-input); .detail-container { padding: 40px; text-align: center; h3 { margin: 16px 0 24px; font-size: 20px; } .action-row { display: flex; justify-content: center; gap: 12px; margin-bottom: 40px; } .info-group { display: flex; flex-direction: column; align-items: center; margin-bottom: 20px; label { font-size: 12px; color: var(--dt-text-tertiary); margin-bottom: 4px; } span { font-size: 15px; font-weight: 600; color: var(--dt-text-primary); } } } .detail-empty { height: 100%; @include flex-center; color: var(--dt-text-quaternary); } }
</style>
