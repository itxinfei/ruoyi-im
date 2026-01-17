<template>
  <div 
    class="message-menu" 
    v-if="visible"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
    @click.stop
  >
    <div 
      v-for="item in menuItems" 
      :key="item.key"
      class="menu-item"
      :class="{ disabled: item.disabled }"
      @click="handleMenuClick(item)"
    >
      <el-icon class="menu-icon">
        <component :is="item.icon" />
      </el-icon>
      <span class="menu-text">{{ item.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { 
  Position, CopyDocument, Delete, RefreshRight, Share,
  Star, Collection, Flag, MoreFilled
} from '@element-plus/icons-vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  position: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'action'])

const visible = computed(() => {
  return props.message && props.position
})

const isOwnMessage = computed(() => {
  return props.message.senderId === getCurrentUserId()
})

const menuItems = computed(() => {
  const items = [
    {
      key: 'reply',
      label: '回复',
      icon: Reply,
      disabled: false
    },
    {
      key: 'copy',
      label: '复制',
      icon: CopyDocument,
      disabled: !props.message.content || props.message.type !== 'text'
    },
    {
      key: 'forward',
      label: '转发',
      icon: Share,
      disabled: false
    },
    {
      key: 'star',
      label: '收藏',
      icon: Star,
      disabled: false
    },
    {
      key: 'collect',
      label: '收藏到消息收藏',
      icon: Collection,
      disabled: false
    },
    {
      key: 'report',
      label: '举报',
      icon: Flag,
      disabled: isOwnMessage.value
    }
  ]
  
  // 自己的消息可以撤回和删除
  if (isOwnMessage.value) {
    // 检查是否在撤回时间限制内（比如2分钟）
    const canRecall = checkCanRecall()
    
    items.splice(4, 0, {
      key: 'recall',
      label: '撤回',
      icon: RefreshRight,
      disabled: !canRecall
    })
    
    items.splice(5, 0, {
      key: 'delete',
      label: '删除',
      icon: Delete,
      disabled: false
    })
  }
  
  return items
})

function handleMenuClick(item) {
  if (item.disabled) return
  
  emit('action', item.key)
  emit('close')
}

function getCurrentUserId() {
  // 这里应该从store或本地存储获取当前用户ID
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.userId || userInfo.id
}

function checkCanRecall() {
  if (!props.message) return false
  
  const messageTime = props.message.timestamp || props.message.time
  const now = Date.now()
  const timeDiff = now - messageTime
  
  // 2分钟内可以撤回
  return timeDiff <= 2 * 60 * 1000
}

// 点击外部关闭菜单
document.addEventListener('click', () => {
  emit('close')
}, { once: true })
</script>

<style scoped>
.message-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 4px 0;
  z-index: 1000;
  min-width: 160px;
  max-width: 240px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 14px;
  color: #262626;
}

.menu-item:hover:not(.disabled) {
  background: #f5f5f5;
}

.menu-item.disabled {
  color: #bfbfbf;
  cursor: not-allowed;
}

.menu-icon {
  font-size: 16px;
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .message-menu {
    background: #1f1f1f;
    border-color: #404040;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  }
  
  .menu-item {
    color: #e8e8e8;
  }
  
  .menu-item:hover:not(.disabled) {
    background: #333;
  }
  
  .menu-item.disabled {
    color: #666;
  }
}
</style>