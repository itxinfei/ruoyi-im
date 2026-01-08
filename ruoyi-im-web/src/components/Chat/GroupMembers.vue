<template>
  <div class="dt-group-members">
    <!-- 搜索栏 -->
    <div class="dt-group-members__search">
      <el-icon class="search-icon"><Search /></el-icon>
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索群成员"
        @input="handleSearch"
      />
    </div>

    <!-- 成员统计 -->
    <div class="dt-group-members__stats">
      <span class="stat-item">
        <span class="stat-label">群成员</span>
        <span class="stat-value">{{ filteredMembers.length }}</span>
      </span>
      <span class="stat-divider"></span>
      <span class="stat-item">
        <span class="stat-label">在线</span>
        <span class="stat-value online">{{ onlineCount }}</span>
      </span>
    </div>

    <!-- 成员列表 -->
    <div class="dt-group-members__list">
      <!-- 群主 -->
      <div
        v-if="owner"
        :key="'owner-' + owner.id"
        class="dt-group-members__item"
        @click="handleMemberClick(owner)"
      >
        <div class="member-avatar">
          <el-avatar :size="44" :src="owner.avatar">
            {{ owner.nickname?.charAt(0) || '?' }}
          </el-avatar>
          <span class="online-status" :class="owner.onlineStatus || 'offline'"></span>
        </div>
        <div class="member-info">
          <div class="member-name">
            <span class="name">{{ owner.nickname || '未知' }}</span>
            <span class="role-badge owner">群主</span>
          </div>
          <div class="member-desc">{{ owner.signature || '这个人很懒，什么都没留下' }}</div>
        </div>
        <el-icon v-if="owner.id === currentUserId" class="more-icon" @click.stop="handleShowMenu(owner)">
          <MoreFilled />
        </el-icon>
      </div>

      <!-- 管理员 -->
      <div v-if="admins.length > 0" class="dt-group-members__section">
        <div class="section-header">
          <span class="section-title">管理员 ({{ admins.length }})</span>
        </div>
        <div
          v-for="admin in admins"
          :key="'admin-' + admin.id"
          class="dt-group-members__item"
          @click="handleMemberClick(admin)"
        >
          <div class="member-avatar">
            <el-avatar :size="44" :src="admin.avatar">
              {{ admin.nickname?.charAt(0) || '?' }}
            </el-avatar>
            <span class="online-status" :class="admin.onlineStatus || 'offline'"></span>
          </div>
          <div class="member-info">
            <div class="member-name">
              <span class="name">{{ admin.nickname || '未知' }}</span>
              <span class="role-badge admin">管理员</span>
            </div>
            <div class="member-desc">{{ admin.signature || '这个人很懒，什么都没留下' }}</div>
          </div>
          <el-icon v-if="canManage(admin.id)" class="more-icon" @click.stop="handleShowMenu(admin)">
            <MoreFilled />
          </el-icon>
        </div>
      </div>

      <!-- 普通成员 -->
      <div v-if="members.length > 0" class="dt-group-members__section">
        <div class="section-header">
          <span class="section-title">成员 ({{ members.length }})</span>
        </div>
        <div
          v-for="member in members"
          :key="'member-' + member.id"
          class="dt-group-members__item"
          @click="handleMemberClick(member)"
        >
          <div class="member-avatar">
            <el-avatar :size="44" :src="member.avatar">
              {{ member.nickname?.charAt(0) || '?' }}
            </el-avatar>
            <span class="online-status" :class="member.onlineStatus || 'offline'"></span>
          </div>
          <div class="member-info">
            <div class="member-name">
              <span class="name">{{ member.nickname || '未知' }}</span>
            </div>
            <div class="member-desc">{{ member.signature || '这个人很懒，什么都没留下' }}</div>
          </div>
          <el-icon v-if="canManage(member.id)" class="more-icon" @click.stop="handleShowMenu(member)">
            <MoreFilled />
          </el-icon>
        </div>
      </div>
    </div>

    <!-- 底部操作 -->
    <div class="dt-group-members__footer">
      <el-button type="primary" @click="handleAddMember">
        <el-icon><Plus /></el-icon>
        添加成员
      </el-button>
      <el-button @click="handleExitGroup">
        <el-icon><LogOut /></el-icon>
        退出群聊
      </el-button>
    </div>

    <!-- 成员操作菜单 -->
    <el-dropdown
      ref="contextMenuRef"
      :virtual-ref="contextMenuTarget"
      virtual-triggering
      trigger="click"
      @command="handleMenuCommand"
    >
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="profile">查看资料</el-dropdown-item>
          <el-dropdown-item command="chat">私聊</el-dropdown-item>
          <el-dropdown-item v-if="canManageSelected" command="setAdmin">设为管理员</el-dropdown-item>
          <el-dropdown-item v-if="canManageSelected" command="remove">移出群聊</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, MoreFilled, Plus, LogOut } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  session: {
    type: Object,
    default: () => ({})
  },
  currentUserId: {
    type: [String, Number],
    default: null
  },
  isOwner: {
    type: Boolean,
    default: false
  },
  isAdmin: {
    type: Boolean,
    default: false
  },
  members: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits([
  'member-click', 'add-member', 'exit-group', 'set-admin', 'remove-member'
])

// State
const searchKeyword = ref('')
const contextMenuTarget = ref(null)
const contextMenuRef = ref(null)
const selectedMember = ref(null)

// 模拟成员数据
const mockMembers = ref([
  { id: 1, nickname: '张三', avatar: '', role: 'owner', onlineStatus: 'online', signature: '努力工作' },
  { id: 2, nickname: '李四', avatar: '', role: 'admin', onlineStatus: 'online', signature: '加油加油' },
  { id: 3, nickname: '王五', avatar: '', role: 'member', onlineStatus: 'offline', signature: '' },
  { id: 4, nickname: '赵六', avatar: '', role: 'member', onlineStatus: 'away', signature: '忙碌中' },
  { id: 5, nickname: '钱七', avatar: '', role: 'member', onlineStatus: 'online', signature: '' },
])

// 计算属性
const allMembers = computed(() => {
  return props.members.length > 0 ? props.members : mockMembers.value
})

const filteredMembers = computed(() => {
  if (!searchKeyword.value) return allMembers.value
  const keyword = searchKeyword.value.toLowerCase()
  return allMembers.value.filter(m =>
    m.nickname?.toLowerCase().includes(keyword)
  )
})

const owner = computed(() => {
  return filteredMembers.value.find(m => m.role === 'owner')
})

const admins = computed(() => {
  return filteredMembers.value.filter(m => m.role === 'admin')
})

const members = computed(() => {
  return filteredMembers.value.filter(m => m.role === 'member')
})

const onlineCount = computed(() => {
  return allMembers.value.filter(m => m.onlineStatus === 'online').length
})

const canManageSelected = computed(() => {
  if (!selectedMember.value) return false
  return props.isOwner || (props.isAdmin && selectedMember.value.role === 'member')
})

// 方法
const handleSearch = () => {
  // 搜索已通过computed自动处理
}

const handleMemberClick = (member) => {
  emit('member-click', member)
}

const handleShowMenu = (member, event) => {
  selectedMember.value = member
  contextMenuTarget.value = event?.target
  contextMenuRef.value?.handleOpen()
}

const handleMenuCommand = (command) => {
  if (!selectedMember.value) return

  switch (command) {
    case 'profile':
      // 查看资料
      break
    case 'chat':
      // 私聊
      break
    case 'setAdmin':
      emit('set-admin', selectedMember.value)
      break
    case 'remove':
      emit('remove-member', selectedMember.value)
      break
  }
}

const canManage = (memberId) => {
  if (props.isOwner) return true
  if (props.isAdmin && memberId !== props.currentUserId) return true
  return memberId === props.currentUserId
}

const handleAddMember = () => {
  emit('add-member')
}

const handleExitGroup = () => {
  emit('exit-group')
}

onMounted(() => {
  // 初始化
})
</script>

<style lang="scss" scoped>
.dt-group-members {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--dt-bg-component);

  // 搜索栏 - 48px高
  &__search {
    display: flex;
    align-items: center;
    height: 48px;
    padding: 0 16px;
    border-bottom: 1px solid var(--dt-border-lighter);

    .search-icon {
      color: var(--dt-text-placeholder);
      font-size: 16px;
      margin-right: 8px;
    }

    input {
      flex: 1;
      border: none;
      background: transparent;
      outline: none;
      font-size: var(--dt-font-size-base);
      color: var(--dt-text-primary);

      &::placeholder {
        color: var(--dt-text-placeholder);
      }
    }
  }

  // 成员统计 - 40px高
  &__stats {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 40px;
    padding: 0 16px;
    background: var(--dt-bg-container);
    border-bottom: 1px solid var(--dt-border-lighter);

    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;

      .stat-label {
        font-size: var(--dt-font-size-sm);
        color: var(--dt-text-secondary);
      }

      .stat-value {
        font-size: var(--dt-font-size-md);
        font-weight: 500;
        color: var(--dt-text-primary);

        &.online {
          color: var(--dt-success);
        }
      }
    }

    .stat-divider {
      width: 1px;
      height: 12px;
      background: var(--dt-border-light);
      margin: 0 16px;
    }
  }

  // 成员列表
  &__list {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;
  }

  &__section {
    margin-bottom: 8px;
  }

  .section-header {
    padding: 8px 16px 4px;

    .section-title {
      font-size: var(--dt-font-size-xs);
      color: var(--dt-text-placeholder);
      font-weight: 500;
    }
  }

  // 成员项 - 64px高
  &__item {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    cursor: pointer;
    transition: background-color var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-container);
    }

    .member-avatar {
      position: relative;
      margin-right: 12px;
      flex-shrink: 0;

      // 在线状态点 - 12px
      .online-status {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 12px;
        height: 12px;
        border-radius: 50%;
        border: 2px solid var(--dt-bg-component);

        &.online {
          background: var(--dt-success);
        }

        &.offline {
          background: var(--dt-text-placeholder);
        }

        &.busy {
          background: var(--dt-error);
        }

        &.away {
          background: var(--dt-warning);
        }
      }
    }

    .member-info {
      flex: 1;
      min-width: 0;
    }

    .member-name {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 2px;

      .name {
        font-size: var(--dt-font-size-md);
        font-weight: 500;
        color: var(--dt-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .role-badge {
        padding: 0 6px;
        height: 16px;
        font-size: 11px;
        border-radius: 4px;
        flex-shrink: 0;

        &.owner {
          background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
          color: white;
        }

        &.admin {
          background: rgba(22, 119, 255, 0.1);
          color: var(--dt-primary);
        }
      }
    }

    .member-desc {
      font-size: var(--dt-font-size-xs);
      color: var(--dt-text-placeholder);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .more-icon {
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--dt-text-secondary);
      font-size: 16px;
      border-radius: var(--dt-radius-base);
      transition: all var(--dt-transition-fast);
      flex-shrink: 0;

      &:hover {
        background: var(--dt-bg-container);
        color: var(--dt-text-primary);
      }
    }
  }

  // 底部操作 - 64px高
  &__footer {
    display: flex;
    gap: 8px;
    padding: 12px 16px;
    border-top: 1px solid var(--dt-border-lighter);
    background: var(--dt-bg-component);

    .el-button {
      flex: 1;
      height: 40px;
      font-size: var(--dt-font-size-md);
      border-radius: var(--dt-radius-base);
    }
  }
}

// 空状态
.dt-group-members__list:empty::before {
  content: '暂无成员';
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-placeholder);
}
</style>
