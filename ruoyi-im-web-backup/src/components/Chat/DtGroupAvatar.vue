<template>
  <div
    class="dt-group-avatar"
    :class="[`dt-group-avatar-${size}`, { clickable: clickable }]"
    :style="{ width: finalSize + 'px', height: finalSize + 'px' }"
    @click="handleClick"
  >
    <!-- 单人模式 -->
    <template v-if="members.length === 0 || (members.length === 1 && members[0].avatar)">
      <img
        v-if="members.length === 1 && members[0].avatar"
        :src="members[0].avatar"
        class="single-avatar"
        @error="handleImageError"
      />
      <div v-else class="default-avatar">
        <span>{{ defaultText }}</span>
      </div>
    </template>

    <!-- 2-4人：2x2网格 -->
    <template v-else-if="members.length <= 4">
      <div class="avatar-grid grid-2x2">
        <div v-for="(member, index) in displayMembers" :key="index" class="grid-item">
          <img
            v-if="member.avatar"
            :src="member.avatar"
            class="member-avatar"
            @error="e => handleMemberError(e, index)"
          />
          <span v-else class="member-text">{{ getAvatarText(member.name) }}</span>
        </div>
      </div>
    </template>

    <!-- 5-9人：3x3网格 -->
    <template v-else-if="members.length <= 9">
      <div class="avatar-grid grid-3x3">
        <div v-for="(member, index) in displayMembers" :key="index" class="grid-item">
          <img
            v-if="member.avatar"
            :src="member.avatar"
            class="member-avatar"
            @error="e => handleMemberError(e, index)"
          />
          <span v-else class="member-text">{{ getAvatarText(member.name) }}</span>
        </div>
      </div>
    </template>

    <!-- 10+人：显示前9个 + "+N" -->
    <template v-else>
      <div class="avatar-grid grid-3x3">
        <div v-for="(member, index) in displayMembers" :key="index" class="grid-item">
          <template v-if="index < 8">
            <img
              v-if="member.avatar"
              :src="member.avatar"
              class="member-avatar"
              @error="e => handleMemberError(e, index)"
            />
            <span v-else class="member-text">{{ getAvatarText(member.name) }}</span>
          </template>
          <div v-else class="more-count">+{{ members.length - 8 }}</div>
        </div>
      </div>
    </template>

    <!-- 在线状态指示器 -->
    <span v-if="showStatus && status" class="avatar-status" :class="`status-${status}`"></span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  // 群组成员数组
  members: {
    type: Array,
    default: () => [],
  },
  // 头像尺寸：xs/sm/md/lg/xl 或自定义数字
  size: {
    type: [String, Number],
    default: 'md',
  },
  // 默认显示文字（当没有成员时）
  defaultText: {
    type: String,
    default: '群',
  },
  // 是否显示在线状态
  showStatus: Boolean,
  // 在线状态：online/busy/away/offline
  status: {
    type: String,
    default: 'offline',
  },
  // 是否可点击
  clickable: Boolean,
})

const emit = defineEmits(['click'])

// 图片加载错误记录
const imageErrors = ref({})

// 尺寸映射
const sizeMap = {
  xs: 24,
  sm: 32,
  md: 40,
  lg: 48,
  xl: 64,
}

// 最终尺寸
const finalSize = computed(() => {
  if (typeof props.size === 'number') return props.size
  return sizeMap[props.size] || sizeMap.md
})

// 显示的成员数量
const displayMembers = computed(() => {
  if (props.members.length <= 4) return props.members.slice(0, 4)
  if (props.members.length <= 9) return props.members.slice(0, 9)
  return props.members.slice(0, 9)
})

// 获取头像文字
const getAvatarText = name => {
  if (!name) return '?'
  // 中文名取最后一个字
  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.charAt(name.length - 1)
  }
  // 英文名取首字母
  return name.charAt(0).toUpperCase()
}

// 处理单人头像加载失败
const handleImageError = () => {
  // 单人头像加载失败时显示默认头像
}

// 处理成员头像加载失败
const handleMemberError = (event, index) => {
  imageErrors.value[index] = true
}

// 点击处理
const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style lang="scss" scoped>
.dt-group-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;

  &.clickable {
    cursor: pointer;

    &:hover {
      opacity: 0.85;
    }
  }

  // 单人头像
  .single-avatar {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  // 默认头像
  .default-avatar {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #0089ff 0%, #00a0ff 100%);
    color: #fff;
    font-weight: 500;

    span {
      font-size: 14px;
    }
  }
}

// 网格布局
.avatar-grid {
  width: 100%;
  height: 100%;
  display: grid;
  gap: 2px;
  padding: 2px;
  box-sizing: border-box;

  &.grid-2x2 {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr);
  }

  &.grid-3x3 {
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(3, 1fr);
  }

  .grid-item {
    position: relative;
    background: #f0f2f5;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;

    .member-avatar {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .member-text {
      font-size: 10px;
      color: #858b8f;
      font-weight: 500;
    }

    .more-count {
      font-size: 10px;
      color: #5f6468;
      font-weight: 600;
      background: #e5e8eb;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

// 在线状态指示器
.avatar-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);

  &.status-online {
    background-color: #52c41a;
    animation: online-pulse 2s infinite;
  }

  &.status-offline {
    background-color: #d9d9d9;
  }

  &.status-busy {
    background-color: #f5222d;
  }

  &.status-away {
    background-color: #faad14;
  }
}

@keyframes online-pulse {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(82, 196, 26, 0);
  }
}
</style>
