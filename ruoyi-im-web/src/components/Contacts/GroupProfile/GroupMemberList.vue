<!--
  群组成员列表组件

  显示成员列表，支持搜索、点击查看、管理员操作、虚拟滚动
-->
<template>
  <div class="tab-content">
    <!-- 成员搜索 -->
    <div class="members-header">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索成员"
        clearable
        class="member-search"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-dropdown v-if="isOwnerOrAdmin" @command="handleBatchCommand">
        <el-button>
          批量管理<el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="invite">邀请成员</el-dropdown-item>
            <el-dropdown-item command="mute" divided>全员禁言</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 成员统计 -->
    <div class="members-count">
      共 {{ filteredMembers.length }} 位成员
    </div>

    <!-- 成员列表（支持虚拟滚动 + 角色分组） -->
    <div
      ref="listRef"
      class="members-list"
      :class="{ 'grouped-mode': enableGrouping && !searchKeyword }"
      @scroll="handleScroll"
    >
      <!-- 分组模式 -->
      <template v-if="enableGrouping && !searchKeyword">
        <div
          v-for="(group, role) in memberGroups"
          :key="role"
          class="role-group"
          :class="{ 'is-collapsed': !expandedGroups.includes(role) }"
        >
          <!-- 分组头部 -->
          <div
            class="role-group-header"
            :style="{ '--group-color': group.color }"
            @click="toggleGroup(role)"
          >
            <div class="role-group-info">
              <el-icon class="role-icon" :size="18">
                <Trophy v-if="role === 'OWNER'" />
                <UserFilled v-else-if="role === 'ADMIN'" />
                <User v-else />
              </el-icon>
              <span class="role-label">{{ group.label }}</span>
              <span class="role-count">{{ group.members.length }}</span>
            </div>
            <el-icon class="expand-icon" :size="16">
              <ArrowDown />
            </el-icon>
          </div>

          <!-- 分组成员列表 -->
          <div v-show="expandedGroups.includes(role)" class="role-group-members">
            <div
              v-for="member in group.members"
              :key="member.id || member.userId"
              class="member-item"
              @click="handleMemberClick(member)"
            >
              <div class="member-avatar-wrapper">
                <DingtalkAvatar
                  :src="member.avatar"
                  :name="member.nickname || member.username"
                  :size="44"
                  shape="circle"
                />
                <div v-if="isOnline(member)" class="online-indicator" title="在线"></div>
              </div>
              <div class="member-info">
                <div class="member-name">
                  {{ member.nickname || member.username }}
                  <span v-if="member.remark" class="member-remark">({{ member.remark }})</span>
                </div>
              </div>
              <div v-if="isOwnerOrAdmin && !isSelf(member)" class="member-actions">
                <el-dropdown trigger="click" @command="cmd => handleMemberCommand(cmd, member)">
                  <el-button link type="primary" @click.stop>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="isOwner && member.role !== 'ADMIN'" command="setAdmin">
                        设为管理员
                      </el-dropdown-item>
                      <el-dropdown-item v-if="isOwner && member.role === 'ADMIN'" command="cancelAdmin">
                        取消管理员
                      </el-dropdown-item>
                      <el-dropdown-item command="mute">禁言</el-dropdown-item>
                      <el-dropdown-item command="remove" divided>
                        <span style="color: #ff4d4f">移出群聊</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 列表模式（虚拟滚动） -->
      <template v-else>
        <!-- 顶部占位符 -->
        <div class="virtual-spacer-top" :style="{ height: topSpacerHeight + 'px' }"></div>

        <!-- 可见成员项 -->
        <div
          v-for="member in visibleMembers"
          :key="member.id || member.userId"
          :data-index="member.index"
          class="member-item"
          @click="handleMemberClick(member)"
        >
          <div class="member-avatar-wrapper">
            <DingtalkAvatar
              :src="member.avatar"
              :name="member.nickname || member.username"
              :size="44"
              shape="circle"
            />
            <div v-if="isOnline(member)" class="online-indicator" title="在线"></div>
          </div>
          <div class="member-info">
            <div class="member-name">
              {{ member.nickname || member.username }}
              <span v-if="member.remark" class="member-remark">({{ member.remark }})</span>
            </div>
            <div class="member-role">
              <el-tag
                v-if="member.role === 'OWNER'"
                size="small"
                type="danger"
                effect="light"
              >群主</el-tag>
              <el-tag
                v-else-if="member.role === 'ADMIN'"
                size="small"
                type="warning"
                effect="light"
              >管理员</el-tag>
              <span v-else class="role-text">成员</span>
            </div>
          </div>
          <div v-if="isOwnerOrAdmin && !isSelf(member)" class="member-actions">
            <el-dropdown trigger="click" @command="cmd => handleMemberCommand(cmd, member)">
              <el-button link type="primary" @click.stop>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="isOwner && member.role !== 'ADMIN'" command="setAdmin">
                    设为管理员
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isOwner && member.role === 'ADMIN'" command="cancelAdmin">
                    取消管理员
                  </el-dropdown-item>
                  <el-dropdown-item command="mute">禁言</el-dropdown-item>
                  <el-dropdown-item command="remove" divided>
                    <span style="color: #ff4d4f">移出群聊</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 底部占位符 -->
        <div class="virtual-spacer-bottom" :style="{ height: bottomSpacerHeight + 'px' }"></div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Search, ArrowDown, MoreFilled, Trophy, UserFilled, User } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  members: {
    type: Array,
    default: () => []
  },
  currentUser: {
    type: Object,
    default: () => ({})
  },
  isOwner: {
    type: Boolean,
    default: false
  },
  isAdmin: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'invite',
  'mute-all',
  'member-click',
  'set-admin',
  'cancel-admin',
  'mute-member',
  'remove-member'
])

const searchKeyword = ref('')
const listRef = ref(null)
const scrollTop = ref(0)
const clientHeight = ref(400)
const expandedGroups = ref(['all']) // 默认展开全部

// 成员角色分组
const memberGroups = computed(() => {
  const groups = {
    OWNER: { label: '群主', members: [], icon: 'crown', color: '#ff4d4f' },
    ADMIN: { label: '管理员', members: [], icon: 'admin_panel_settings', color: '#faad14' },
    MEMBER: { label: '成员', members: [], icon: 'person', color: '#1890ff' }
  }

  filteredIndexedMembers.value.forEach(member => {
    const role = member.role || 'MEMBER'
    if (groups[role]) {
      groups[role].members.push(member)
    }
  })

  return groups
})

// 是否启用分组模式
const enableGrouping = computed(() => props.members.length > 20)

// 虚拟滚动配置
const VIRTUAL_THRESHOLD = 50 // 成员数超过此值时启用虚拟滚动
const ITEM_HEIGHT = 60 // 单个成员项高度（像素）
const BUFFER_ITEMS = 10 // 缓冲区项数

// 是否启用虚拟滚动
const enableVirtual = computed(() => props.members.length > VIRTUAL_THRESHOLD)

// 带索引的成员列表
const indexedMembers = computed(() => {
  return props.members.map((member, index) => ({ ...member, index }))
})

// 过滤后的成员（带索引）
const filteredIndexedMembers = computed(() => {
  if (!searchKeyword.value) {
    return indexedMembers.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return indexedMembers.value.filter(m =>
    (m.nickname || m.username || '').toLowerCase().includes(keyword)
  )
})

// 计算可见范围
const visibleRange = computed(() => {
  if (!enableVirtual.value) {
    return { startIndex: 0, endIndex: filteredIndexedMembers.value.length }
  }

  const totalHeight = filteredIndexedMembers.value.length * ITEM_HEIGHT
  const viewportHeight = clientHeight.value

  // 计算可见区域的起始和结束索引
  const startRow = Math.floor(scrollTop.value / ITEM_HEIGHT)
  const endRow = Math.ceil((scrollTop.value + viewportHeight) / ITEM_HEIGHT)

  // 添加缓冲区
  const startIndex = Math.max(0, startRow - BUFFER_ITEMS)
  const endIndex = Math.min(filteredIndexedMembers.value.length, endRow + BUFFER_ITEMS)

  return { startIndex, endIndex }
})

// 可见成员列表
const visibleMembers = computed(() => {
  if (!enableVirtual.value) {
    return filteredIndexedMembers.value
  }

  const { startIndex, endIndex } = visibleRange.value
  return filteredIndexedMembers.value.slice(startIndex, endIndex)
})

// 顶部和底部占位符高度
const topSpacerHeight = computed(() => {
  if (!enableVirtual.value) return 0
  const { startIndex } = visibleRange.value
  return startIndex * ITEM_HEIGHT
})

const bottomSpacerHeight = computed(() => {
  if (!enableVirtual.value) return 0
  const { endIndex } = visibleRange.value
  return (filteredIndexedMembers.value.length - endIndex) * ITEM_HEIGHT
})

const isOwnerOrAdmin = computed(() => props.isOwner || props.isAdmin)

const isSelf = (member) => {
  return (member.userId || member.id) == props.currentUser?.id
}

const isOnline = (member) => {
  // 模拟在线状态
  return Math.random() > 0.5
}

const handleMemberClick = (member) => {
  emit('member-click', member)
}

const handleBatchCommand = (command) => {
  switch (command) {
    case 'invite':
      emit('invite')
      break
    case 'mute':
      emit('mute-all')
      break
  }
}

const handleScroll = (e) => {
  scrollTop.value = e.target.scrollTop
}

// 切换分组展开/收起
const toggleGroup = (role) => {
  const index = expandedGroups.value.indexOf(role)
  if (index > -1) {
    expandedGroups.value.splice(index, 1)
  } else {
    expandedGroups.value.push(role)
  }
}

const handleMemberCommand = async (command, member) => {
  const memberName = member.nickname || member.username
  const memberId = member.userId || member.id

  switch (command) {
    case 'setAdmin':
      emit('set-admin', { member, memberId })
      ElMessage.success(`已将 ${memberName} 设为管理员`)
      break
    case 'cancelAdmin':
      emit('cancel-admin', { member, memberId })
      ElMessage.success(`已取消 ${memberName} 的管理员权限`)
      break
    case 'mute':
      emit('mute-member', member)
      ElMessage.success(`已禁言 ${memberName}`)
      break
    case 'remove':
      ElMessageBox.confirm(
        `确定要将 ${memberName} 移出群聊吗？`,
        '移出群聊',
        { type: 'warning' }
      ).then(() => {
        emit('remove-member', { member, memberId })
      }).catch(() => {})
      break
  }
}

// 初始化
onMounted(() => {
  nextTick(() => {
    if (listRef.value) {
      clientHeight.value = listRef.value.clientHeight
    }
  })
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.members-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .member-search {
    flex: 1;
  }
}

.members-list {
  max-height: 400px;
  overflow-y: auto;
  position: relative;
}

// 虚拟滚动占位符
.virtual-spacer-top,
.virtual-spacer-bottom {
  width: 100%;
  pointer-events: none;
}

.virtual-spacer-top {
  flex-shrink: 0;
}

.virtual-spacer-bottom {
  flex-shrink: 0;
}

// 成员统计
.members-count {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-bottom: 12px;
  padding: 0 4px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-hover);
  }
}

.member-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #67c23a;
  border: 2px solid #fff;
  border-radius: 50%;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-remark {
  color: var(--dt-text-tertiary);
  font-size: 12px;
}

.member-role {
  .role-text {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}

.member-actions {
  flex-shrink: 0;
}

// 角色分组模式样式
.members-list.grouped-mode {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.role-group {
  background: var(--dt-bg-card);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--dt-border-light);
  transition: all var(--dt-transition-base);

  &.is-collapsed {
    .expand-icon {
      transform: rotate(-90deg);
    }
  }
}

.role-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
  user-select: none;
  background: linear-gradient(to right, var(--group-color) 2px, var(--dt-bg-card) 2px);
  transition: background var(--dt-transition-base);

  &:hover {
    background: linear-gradient(to right, var(--group-color) 3px, var(--dt-bg-hover) 3px);
  }
}

.role-group-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-icon {
  color: var(--group-color);
}

.role-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.role-count {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  background: var(--dt-bg-secondary);
  padding: 2px 8px;
  border-radius: 10px;
}

.expand-icon {
  color: var(--dt-text-placeholder);
  transition: transform 0.3s ease;
}

.role-group-members {
  padding: 4px 8px;

  .member-item {
    padding: 8px 10px;
    border-radius: 6px;

    &:hover {
      background: var(--dt-bg-secondary);
    }
  }
}
</style>
