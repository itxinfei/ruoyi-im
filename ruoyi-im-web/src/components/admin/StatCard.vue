<template>
  <el-card
    :class="['stat-card', { 'stat-card--clickable': clickable }]"
    :shadow="shadow"
    @click="handleClick"
  >
    <div class="stat-card__content">
      <!-- 图标 -->
      <div v-if="icon || $slots.icon" class="stat-card__icon" :style="{ backgroundColor: iconColor }">
        <slot name="icon">
          <el-icon :size="iconSize">
            <component :is="icon" />
          </el-icon>
        </slot>
      </div>

      <!-- 内容 -->
      <div class="stat-card__info">
        <div class="stat-card__title">{{ title }}</div>
        <div class="stat-card__value">
          <span v-if="loading" class="stat-card__skeleton">--</span>
          <template v-else>
            <span class="stat-card__number">{{ formattedValue }}</span>
            <span v-if="unit" class="stat-card__unit">{{ unit }}</span>
          </template>
        </div>
        <!-- 趋势 -->
        <div v-if="trend !== undefined && !loading" class="stat-card__trend" :class="trendClass">
          <el-icon>
            <ArrowUp v-if="trend > 0" />
            <ArrowDown v-else-if="trend < 0" />
            <Minus v-else />
          </el-icon>
          <span>{{ Math.abs(trend) }}%</span>
        </div>
      </div>

      <!-- 额外信息 -->
      <div v-if="$slots.extra" class="stat-card__extra">
        <slot name="extra" />
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'

/**
 * 统计卡片组件
 * 用于展示数据概览，支持图标、趋势、加载状态
 */
const props = defineProps({
  // 标题
  title: {
    type: String,
    required: true
  },
  // 数值
  value: {
    type: [Number, String],
    default: 0
  },
  // 单位
  unit: {
    type: String,
    default: ''
  },
  // 图标组件
  icon: {
    type: [String, Object],
    default: null
  },
  // 图标背景色
  iconColor: {
    type: String,
    default: '#409eff'
  },
  // 图标大小
  iconSize: {
    type: Number,
    default: 24
  },
  // 趋势百分比（正数上升，负数下降，0无变化）
  trend: {
    type: Number,
    default: undefined
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 是否可点击
  clickable: {
    type: Boolean,
    default: false
  },
  // 卡片阴影
  shadow: {
    type: String,
    default: 'hover'
  },
  // 格式化函数
  formatter: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['click'])

// 格式化数值
const formattedValue = computed(() => {
  if (props.loading) return '--'
  if (props.formatter) return props.formatter(props.value)
  return props.value
})

// 趋势样式
const trendClass = computed(() => {
  if (props.trend > 0) return 'stat-card__trend--up'
  if (props.trend < 0) return 'stat-card__trend--down'
  return 'stat-card__trend--flat'
})

// 点击处理
const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style scoped lang="scss">
.stat-card {
  transition: all 0.3s ease;

  &--clickable {
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
    }
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  &__content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    border-radius: var(--dt-radius-lg);
    color: #fff;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin-bottom: 8px;
  }

  &__value {
    display: flex;
    align-items: baseline;
    gap: 4px;
    margin-bottom: 4px;
  }

  &__number {
    font-size: 28px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    line-height: 1;
  }

  &__unit {
    font-size: 14px;
    color: var(--el-text-color-regular);
  }

  &__skeleton {
    font-size: 28px;
    color: var(--el-text-color-placeholder);
  }

  &__trend {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 500;

    &--up {
      color: #67c23a;
    }

    &--down {
      color: #f56c6c;
    }

    &--flat {
      color: var(--el-text-color-secondary);
    }

    .el-icon {
      font-size: 14px;
    }
  }

  &__extra {
    flex-shrink: 0;
  }
}
</style>
