<template>
  <div class="mobile-stats-panel">
    <div class="stats-header">
      <div class="header-title">系统统计</div>
      <div class="header-actions">
        <el-icon @click="closeMobilePanel"><Close /></el-icon>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card" @click="goToUserStats">
        <div class="stat-icon user-icon">
          <el-icon :size="24"><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
          <div class="stat-label">总用户</div>
        </div>
      </div>

      <div class="stat-card" @click="goToMessageStats">
        <div class="stat-icon message-icon">
          <el-icon :size="24"><ChatLineSquare /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalMessages || 0 }}</div>
          <div class="stat-label">总消息</div>
        </div>
      </div>

      <div class="stat-card" @click="goToFileStats">
        <div class="stat-icon file-icon">
          <el-icon :size="24"><Folder /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ formatFileSize(statistics.totalStorageSize) }}</div>
          <div class="stat-label">文件存储</div>
        </div>
      </div>
    </div>

    <div class="stats-chart">
      <div class="chart-title">活跃趋势</div>
      <div class="chart-placeholder">
        <el-icon :size="32"><TrendCharts /></el-icon>
        <span>活跃用户趋势图</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

const router = useRouter()
const store = useStore()

const props = defineProps({
  visible: Boolean,
  statistics: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['close'])

const mobilePanelVisible = computed({
  get: () => props.visible,
  set: val => emit('close', val),
})

const closeMobilePanel = () => {
  mobilePanelVisible.value = false
}

const goToUserStats = () => {
  router.push('/admin/user')
}

const goToMessageStats = () => {
  router.push('/admin/message')
}

const goToFileStats = () => {
  router.push('/admin/file')
}

const formatFileSize = bytes => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const threshold = [1024, 1024 * 1024, 1024 * 1024 * 1024]
  let i = units.length - 1
  while (bytes >= threshold[i] && i >= 0) {
    i--
  }
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + units[i]
}
</script>

<style lang="scss" scoped>
.mobile-stats-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(10px);
  z-index: 9999;
  padding: 20px;
  border-radius: 16px;

  .stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-title {
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }

    .header-actions {
      .close-icon {
        cursor: pointer;
        padding: 8px;
        border-radius: 8px;
        transition: all 0.2s;

        &:hover {
          background: rgba(0, 0, 0, 0.05);
        }
      }
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    @media (max-width: 480px) {
      grid-template-columns: 1fr;
    }
  }

  .stat-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    &:active {
      transform: translateY(-4px);
      box-shadow: 0 4px 16px rgba(24, 144, 255, 0.15);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      margin: 0 auto 12px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
    }

    .stat-info {
      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #1677ff;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 12px;
        color: #666;
      }
    }
  }

  .stats-chart {
    grid-column: span 2;
    padding: 30px 20px;
    text-align: center;

    .chart-title {
      font-size: 14px;
      color: #666;
      margin-bottom: 16px;
    }

    .chart-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #999;
      padding: 40px 20px;
      background: #f5f5f5;
      border-radius: 8px;

      .el-icon {
        font-size: 32px;
        opacity: 0.3;
      }

      span {
        margin-top: 12px;
        font-size: 12px;
      }
    }
  }
}
</style>
