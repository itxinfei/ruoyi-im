<template>
  <div class="ding-premium-v2">
    <!-- 1. 顶部 Header (56px, 玻璃拟态) -->
    <header class="view-header">
      <div class="header-left">
        <h2 class="view-title">DING 消息中心</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" class="launch-btn" @click="showSendDialog = true">
          <el-icon><Promotion /></el-icon>
          <span>新建 DING</span>
        </el-button>
      </div>
    </header>

    <main class="view-body custom-scrollbar">
      <!-- 2. 状态统计卡片 -->
      <div class="stats-row">
        <div class="stat-card" v-for="s in statItems" :key="s.label">
          <div class="stat-icon" :class="s.type"><el-icon><component :is="s.icon" /></el-icon></div>
          <div class="stat-info">
            <span class="val">{{ s.value }}</span>
            <span class="lab">{{ s.label }}</span>
          </div>
        </div>
      </div>

      <!-- 3. 任务列表 -->
      <el-tabs v-model="activeTab" class="dt-tabs">
        <el-tab-pane label="收到的 DING" name="received" />
        <el-tab-pane label="我发出的" name="sent" />
      </el-tabs>

      <div class="ding-flow" v-loading="loading">
        <div v-for="ding in displayList" :key="ding.id" class="ding-card-v2" :class="{ urgent: ding.priority === 'URGENT' }">
          <div class="card-left">
            <DingtalkAvatar :src="ding.senderAvatar" :name="ding.senderName" :size="44" shape="square" />
          </div>
          <div class="card-main">
            <div class="row-top">
              <span class="sender">{{ ding.senderName }}</span>
              <span class="time">{{ formatTime(ding.sendTime) }}</span>
            </div>
            <div class="ding-body-text">{{ ding.content }}</div>
            <div class="card-footer">
              <div class="footer-left">
                <el-tag size="small" effect="plain" class="type-tag">{{ ding.dingType }}</el-tag>
                <span class="receipt-info"><el-icon><View /></el-icon> {{ ding.readCount }}/{{ ding.sendCount }} 已读</span>
              </div>
              <div class="footer-right">
                <el-button link type="primary" size="small">回复</el-button>
                <el-button v-if="activeTab === 'received'" type="success" size="small" plain @click="handleConfirm(ding)">确认收到</el-button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="displayList.length === 0" class="empty-view">
          <el-icon><Bell /></el-icon>
          <p>暂无 DING 消息</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
import { ref, computed, onMounted } from 'vue'
import { Promotion, Bell, View, Clock, Tickets } from '@element-plus/icons-vue'
import { getReceivedDings, getSentDings } from '@/api/im/ding'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const activeTab = ref('received')
const loading = ref(false)
const receivedList = ref([])
const sentList = ref([])

const statItems = computed(() => [
  { label: '待处理', value: receivedList.value.length, icon: Bell, type: 'orange' },
  { label: '今日发出', value: sentList.value.length, icon: Promotion, type: 'blue' },
  { label: '累计确认', value: 128, icon: Tickets, type: 'green' }
])

const displayList = computed(() => activeTab.value === 'received' ? receivedList.value : sentList.value)

const loadData = async () => {
  loading.value = true
  try {
    const [r, s] = await Promise.all([getReceivedDings(), getSentDings()])
    receivedList.value = r.data || []
    sentList.value = s.data || []
  } finally { loading.value = false }
}

const formatTime = (t) => t ? new Date(t).toLocaleDateString() : ''
onMounted(loadData)
</script>

<style scoped lang="scss">
.ding-premium-v2 { display: flex; flex-direction: column; height: 100%; background: var(--dt-bg-body); }

.view-header {
  height: 56px; padding: 0 24px; background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light); @include flex-between;
  .view-title { font-size: 16px; font-weight: 700; color: var(--dt-text-primary); }
}

.view-body { flex: 1; overflow-y: auto; padding: 20px 24px; }

.stats-row {
  display: flex; gap: 16px; margin-bottom: 24px;
  .stat-card {
    flex: 1; height: 84px; background: var(--dt-bg-card); border-radius: var(--dt-radius-lg); padding: 0 20px;
    display: flex; align-items: center; gap: 16px; border: 1px solid var(--dt-border-light);
    .stat-icon {
      width: 48px; height: 48px; border-radius: var(--dt-radius-lg); @include flex-center; font-size: 20px;
      &.orange { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
      &.blue { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
      &.green { background: var(--dt-success-bg); color: var(--dt-success-color); }
    }
    .stat-info { display: flex; flex-direction: column; .val { font-size: 22px; font-weight: 700; color: var(--dt-text-primary); } .lab { font-size: 12px; color: var(--dt-text-tertiary); } }
  }
}

.ding-flow {
  display: flex; flex-direction: column; gap: 12px;
}

.ding-card-v2 {
  background: var(--dt-bg-card); border-radius: var(--dt-radius-lg); padding: 20px; border: 1px solid var(--dt-border-light);
  display: flex; gap: 16px; transition: var(--dt-transition-fast);
  &:hover { border-color: var(--dt-brand-light); box-shadow: var(--dt-shadow-1); }
  &.urgent { border-left: 4px solid var(--dt-error-color); }

  .card-main { flex: 1; min-width: 0;
    .row-top { display: flex; justify-content: space-between; margin-bottom: 10px; .sender { font-weight: 600; font-size: 14px; color: var(--dt-text-primary); } .time { font-size: 11px; color: var(--dt-text-tertiary); } }
    .ding-body-text { font-size: 14px; color: var(--dt-text-primary); line-height: 1.6; margin-bottom: 16px; }
    .card-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--dt-border-light); padding-top: 12px;
      .footer-left { display: flex; align-items: center; gap: 12px; .receipt-info { font-size: 12px; color: var(--dt-text-tertiary); .el-icon { vertical-align: middle; } } }
    }
  }
}

.empty-view { padding-top: 100px; text-align: center; color: var(--dt-text-quaternary); .el-icon { font-size: 64px; opacity: 0.3; } }
</style>
