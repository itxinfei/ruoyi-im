<template>
  <transition name="mention-pop">
    <div v-if="visible" ref="selectorRef" class="mention-selector" :style="positionStyle">
      <div class="mention-header">
        <span class="mention-title">选择要@的成员</span>
        <el-input
          ref="searchInputRef"
          v-model="searchKeyword"
          placeholder="搜索成员..."
          :prefix-icon="Search"
          size="small"
          clearable
          @keydown.down.prevent="navigateDown"
          @keydown.up.prevent="navigateUp"
          @keydown.enter.prevent="selectCurrent"
          @keydown.esc.prevent="close"
        />
      </div>

      <div class="mention-list">
        <!-- @所有人选项 -->
        <div
          v-if="showAll && !searchKeyword"
          class="mention-item mention-all"
          :class="{ active: activeIndex === -1 }"
          @click="selectMember({ id: 'all', name: '所有人', avatar: null })"
          @mouseenter="activeIndex = -1"
        >
          <div class="member-avatar all">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="member-info">
            <span class="member-name">所有人</span>
            <span class="member-desc">通知群内所有成员</span>
          </div>
        </div>

        <!-- 成员列表 -->
        <template v-if="filteredMembers.length > 0">
          <div
            v-for="(member, index) in filteredMembers"
            :key="member.id"
            class="mention-item"
            :class="{ active: activeIndex === index }"
            @click="selectMember(member)"
            @mouseenter="activeIndex = index"
          >
            <el-avatar :src="member.avatar" :size="32" class="member-avatar">
              {{ member.name?.charAt(0) }}
            </el-avatar>
            <div class="member-info">
              <span class="member-name">{{ member.name }}</span>
              <span v-if="member.role" class="member-role">{{ member.role }}</span>
            </div>
            <div v-if="member.online" class="online-dot"></div>
          </div>
        </template>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-icon><Search /></el-icon>
          <span>未找到匹配的成员</span>
        </div>
      </div>

      <div class="mention-footer">
        <span class="tip">按 ↑↓ 键选择，Enter 确认，Esc 关闭</span>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { Search, UserFilled } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  members: {
    type: Array,
    default: () => [],
  },
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 }),
  },
  showAll: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['select', 'close'])

// 响应式数据
const searchKeyword = ref('')
const activeIndex = ref(0)
const selectorRef = ref(null)
const searchInputRef = ref(null)

// 计算属性
const filteredMembers = computed(() => {
  if (!searchKeyword.value) return props.members.slice(0, 50)

  const keyword = searchKeyword.value.toLowerCase()
  return props.members
    .filter(
      member =>
        member.name?.toLowerCase().includes(keyword) ||
        member.nickname?.toLowerCase().includes(keyword) ||
        member.userName?.toLowerCase().includes(keyword)
    )
    .slice(0, 50)
})

const positionStyle = computed(() => {
  const maxHeight = 360
  const windowHeight = window.innerHeight
  const windowWidth = window.innerWidth

  let top = props.position.y
  let left = props.position.x

  // 确保不超出屏幕底部
  if (top + maxHeight > windowHeight - 20) {
    top = windowHeight - maxHeight - 20
  }

  // 确保不超出屏幕右侧
  if (left + 280 > windowWidth - 20) {
    left = windowWidth - 280 - 20
  }

  // 确保不小于0
  top = Math.max(20, top)
  left = Math.max(20, left)

  return {
    top: `${top}px`,
    left: `${left}px`,
  }
})

// 方法
const navigateDown = () => {
  const maxIndex = filteredMembers.value.length - 1
  const startIndex = props.showAll && !searchKeyword.value ? -1 : 0
  if (activeIndex.value < maxIndex) {
    activeIndex.value++
  } else {
    activeIndex.value = startIndex
  }
  scrollToActive()
}

const navigateUp = () => {
  const startIndex = props.showAll && !searchKeyword.value ? -1 : 0
  const maxIndex = filteredMembers.value.length - 1
  if (activeIndex.value > startIndex) {
    activeIndex.value--
  } else {
    activeIndex.value = maxIndex
  }
  scrollToActive()
}

const scrollToActive = () => {
  nextTick(() => {
    const activeItem = selectorRef.value?.querySelector('.mention-item.active')
    if (activeItem) {
      activeItem.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
    }
  })
}

const selectCurrent = () => {
  if (activeIndex.value === -1 && props.showAll && !searchKeyword.value) {
    selectMember({ id: 'all', name: '所有人', avatar: null })
  } else if (filteredMembers.value[activeIndex.value]) {
    selectMember(filteredMembers.value[activeIndex.value])
  }
}

const selectMember = member => {
  emit('select', member)
  close()
}

const close = () => {
  searchKeyword.value = ''
  activeIndex.value = 0
  emit('close')
}

// 点击外部关闭
const handleClickOutside = event => {
  if (selectorRef.value && !selectorRef.value.contains(event.target)) {
    close()
  }
}

// 监听器
watch(
  () => props.visible,
  newVal => {
    if (newVal) {
      searchKeyword.value = ''
      activeIndex.value = props.showAll ? -1 : 0
      nextTick(() => {
        searchInputRef.value?.focus()
      })
      document.addEventListener('click', handleClickOutside)
    } else {
      document.removeEventListener('click', handleClickOutside)
    }
  }
)

watch(searchKeyword, () => {
  activeIndex.value = props.showAll && !searchKeyword.value ? -1 : 0
})

// 生命周期
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
.mention-selector {
  position: fixed;
  width: 280px;
  max-height: 360px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 30px rgba(0, 0, 0, 0.15);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.mention-header {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;

  .mention-title {
    display: block;
    font-size: 13px;
    font-weight: 500;
    color: #666;
    margin-bottom: 10px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: none;
    background: #fff;
    border: 1px solid #e8e8e8;

    &:focus-within {
      border-color: #1890ff;
    }
  }
}

.mention-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  max-height: 240px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 3px;
  }
}

.mention-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover,
  &.active {
    background: #f5f5f5;
  }

  &.active {
    background: #e6f7ff;

    .member-name {
      color: #1890ff;
    }
  }

  &.mention-all {
    .member-avatar.all {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 16px;
    }
  }

  .member-avatar {
    flex-shrink: 0;
  }

  .member-info {
    flex: 1;
    margin-left: 12px;
    min-width: 0;

    .member-name {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: #1a1a1a;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      transition: color 0.2s ease;
    }

    .member-role,
    .member-desc {
      display: block;
      font-size: 12px;
      color: #999;
      margin-top: 2px;
    }
  }

  .online-dot {
    width: 8px;
    height: 8px;
    background: #52c41a;
    border-radius: 50%;
    flex-shrink: 0;
    animation: pulse-online 2s infinite;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: #999;

  .el-icon {
    font-size: 32px;
    margin-bottom: 8px;
  }

  span {
    font-size: 13px;
  }
}

.mention-footer {
  padding: 10px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;

  .tip {
    font-size: 12px;
    color: #999;
  }
}

// 动画
.mention-pop-enter-active,
.mention-pop-leave-active {
  transition: all 0.2s ease;
}

.mention-pop-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}

.mention-pop-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

@keyframes pulse-online {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(82, 196, 26, 0);
  }
}

// 响应式
@media (max-width: 768px) {
  .mention-selector {
    width: 260px;
  }

  .mention-footer {
    display: none;
  }
}
</style>
