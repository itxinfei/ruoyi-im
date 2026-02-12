<template>
  <header class="workbench-header">
    <div class="header-overlay" />
    <div class="header-content">
      <div class="greeting-info">
        <h1 class="greeting-title">
          {{ greetingText }}，{{ displayName }}
        </h1>
        <div class="greeting-meta">
          <div class="meta-item">
            <span class="material-icons-outlined">event</span>
            {{ currentDateText }}
          </div>
          <div class="meta-divider" />
          <div class="meta-item status-online">
            <span class="online-dot" />
            系统运行正常
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-tooltip
          content="刷新数据"
          placement="bottom"
        >
          <button 
            class="action-icon-btn" 
            :class="{ rotating: refreshing }"
            @click="$emit('refresh')"
          >
            <span class="material-icons-outlined">refresh</span>
          </button>
        </el-tooltip>
      </div>
    </div>
    <!-- 装饰元素 -->
    <div class="header-decoration">
      <div class="deco-circle deco-circle-1" />
      <div class="deco-circle deco-circle-2" />
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  displayName: {
    type: String,
    default: '用户'
  },
  refreshing: {
    type: Boolean,
    default: false
  }
})

defineEmits(['refresh'])

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) {return '凌晨好'}
  if (hour < 9) {return '早上好'}
  if (hour < 12) {return '上午好'}
  if (hour < 14) {return '中午好'}
  if (hour < 18) {return '下午好'}
  if (hour < 22) {return '晚上好'}
  return '夜深了'
})

const currentDateText = computed(() => {
  const now = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${now.getMonth() + 1}月${now.getDate()}日 ${weekdays[now.getDay()]}`
})
</script>

<style scoped lang="scss">
.workbench-header {
  position: relative;
  height: 180px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 50%, #1e40af 100%);
  padding: 40px 60px;
  color: #fff;
  overflow: hidden;
  display: flex;
  align-items: center;
  border-radius: 0 0 40px 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.header-overlay {
  position: absolute;
  inset: 0;
  background: url('@/assets/images/login-bg.png') center/cover no-repeat;
  opacity: 0.15;
  mix-blend-mode: overlay;
}

.header-content {
  position: relative;
  z-index: 2;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.greeting-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.greeting-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  .material-icons-outlined { font-size: 16px; }
}

.meta-divider {
  width: 1px;
  height: 12px;
  background: rgba(255, 255, 255, 0.3);
}

.status-online {
  .online-dot {
    width: 6px;
    height: 6px;
    background: #4ade80;
    border-radius: 50%;
    box-shadow: 0 0 8px rgba(74, 222, 128, 0.6);
  }
}

.action-icon-btn {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(8px);

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: scale(1.05);
  }

  &.rotating {
    .material-icons-outlined { animation: spin 1s linear infinite; }
  }
}

.header-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  
  .deco-circle {
    position: absolute;
    border-radius: 50%;
    background: white;
    filter: blur(60px);
    opacity: 0.1;
  }
  
  .deco-circle-1 {
    width: 300px;
    height: 300px;
    top: -150px;
    right: -50px;
  }
  
  .deco-circle-2 {
    width: 200px;
    height: 200px;
    bottom: -100px;
    left: 20%;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

@media (max-width: 768px) {
  .workbench-header { padding: 40px 30px; height: 160px; }
  .greeting-title { font-size: 24px; }
}
</style>
