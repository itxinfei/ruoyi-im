<!--
  网络质量指示器组件
  显示实时网络连接质量，包括信号格数、延迟、丢包率
  @author RuoYi-IM
-->
<template>
  <div class="network-indicator" :class="[`quality-${quality}`, { 'show-details': showDetails }]">
    <!-- 信号格数图标 -->
    <div v-if="qualityLevel > 0" class="signal-bars">
      <span
        v-for="i in 4"
        :key="i"
        class="signal-bar"
        :class="{ active: i <= qualityLevel }"
      ></span>
    </div>
    <span v-else class="disconnect-icon">
      <i class="el-icon-close"></i>
    </span>

    <!-- 延迟显示 -->
    <span v-if="showDelay && delay > 0" class="delay-text"> {{ delay }}ms </span>

    <!-- 详细信息（悬停显示） -->
    <div v-if="showDetails && showDetailInfo" class="detail-info">
      <div class="detail-row">
        <span class="label">延迟:</span>
        <span class="value" :class="{ poor: delay > 200 }">{{ delay }}ms</span>
      </div>
      <div class="detail-row">
        <span class="label">丢包:</span>
        <span class="value" :class="{ poor: packetLoss > 5 }">{{ packetLoss.toFixed(1) }}%</span>
      </div>
      <div class="detail-row">
        <span class="label">状态:</span>
        <span class="value" :style="{ color: qualityColor }">{{ delayStatus }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getQualityConfig } from '@/utils/webrtc-stats'

// Props
const props = defineProps({
  /** 网络质量等级: excellent | good | fair | poor | disconnected */
  quality: {
    type: String,
    default: 'disconnected',
  },
  /** 延迟（毫秒） */
  delay: {
    type: Number,
    default: 0,
  },
  /** 丢包率（百分比） */
  packetLoss: {
    type: Number,
    default: 0,
  },
  /** 是否显示延迟 */
  showDelay: {
    type: Boolean,
    default: true,
  },
  /** 是否显示详细信息 */
  showDetails: {
    type: Boolean,
    default: true,
  },
  /** 总是显示详细信息（而不是悬停） */
  alwaysShowDetail: {
    type: Boolean,
    default: false,
  },
})

// 计算属性
const qualityConfig = computed(() => getQualityConfig(props.quality))
const qualityLevel = computed(() => qualityConfig.value.level)
const qualityColor = computed(() => qualityConfig.value.color)

const delayStatus = computed(() => {
  if (props.delay < 50) return '优秀'
  if (props.delay < 100) return '良好'
  if (props.delay < 200) return '一般'
  if (props.delay < 500) return '较差'
  return '很差'
})

const showDetailInfo = computed(() => props.alwaysShowDetail)
</script>

<style lang="scss" scoped>
.network-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 12px;
  color: #fff;
  font-size: 12px;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(0, 0, 0, 0.6);
  }
}

// 信号格数
.signal-bars {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 16px;

  .signal-bar {
    width: 4px;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 1px;
    transition: all 0.3s ease;

    &:nth-child(1) {
      height: 4px;
    }
    &:nth-child(2) {
      height: 8px;
    }
    &:nth-child(3) {
      height: 12px;
    }
    &:nth-child(4) {
      height: 16px;
    }

    &.active {
      background: currentColor;
    }
  }
}

.disconnect-icon {
  font-size: 14px;
  opacity: 0.5;
}

.delay-text {
  font-weight: 500;
  font-feature-settings: 'tnum';
}

// 质量颜色
.network-indicator.quality-excellent {
  color: #52c41a;
}
.network-indicator.quality-good {
  color: #1890ff;
}
.network-indicator.quality-fair {
  color: #faad14;
}
.network-indicator.quality-poor {
  color: #ff4d4f;
}
.network-indicator.quality-disconnected {
  color: #8c8c8c;
}

// 详细信息
.detail-info {
  position: absolute;
  bottom: 100%;
  right: 0;
  margin-bottom: 8px;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transform: translateY(4px);
  transition: all 0.2s ease;

  .detail-row {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    font-size: 12px;
    line-height: 1.6;

    .label {
      color: rgba(255, 255, 255, 0.6);
    }

    .value {
      font-weight: 500;
      font-feature-settings: 'tnum';

      &.poor {
        color: #ff4d4f;
      }
    }
  }
}

.network-indicator:hover .detail-info,
.network-indicator.show-detail {
  .detail-info {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
