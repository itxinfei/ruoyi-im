<template>
  <div class="member-grid-container">
    <!-- 搜索栏（可选） -->
    <div
      v-if="showSearch"
      class="member-search"
    >
      <el-input
        v-model="searchKeyword"
        placeholder="搜索成员..."
        :prefix-icon="Search"
        clearable
        size="small"
      />
    </div>

    <!-- 成员网格 -->
    <div
      class="members-grid"
      :class="`grid-${columnsPerRow}`"
    >
      <!-- 邀请按钮（管理员可见） -->
      <div
        v-if="showAddButton && canAddMember"
        class="member-item add-member-item"
        @click="$emit('add-member')"
      >
        <div class="member-avatar-wrap add-avatar">
          <el-icon><Plus /></el-icon>
        </div>
        <span class="member-name">邀请</span>
      </div>

      <!-- 成员列表 -->
      <div
        v-for="member in filteredMembers"
        :key="member.id"
        class="member-item"
        :class="{ clickable: clickable }"
        @click="handleMemberClick(member)"
      >
        <div class="member-avatar-wrap">
          <DingtalkAvatar
            :src="member.avatar"
            :name="member.name"
            :size="avatarSize"
            shape="circle"
            custom-class="member-avatar"
          />
          <!-- 角色标识 -->
          <div
            v-if="showRole && member.role === 'OWNER'"
            class="role-badge owner"
          >
            <el-icon><Crown /></el-icon>
          </div>
          <div
            v-else-if="showRole && member.role === 'ADMIN'"
            class="role-badge admin"
          >
            <el-icon><UserFilled /></el-icon>
          </div>
          <!-- 在线状态 -->
          <div
            v-if="showOnlineStatus"
            class="online-status"
            :class="member.onlineStatus || 'offline'"
          />
        </div>
        <span
          class="member-name"
          :title="member.name"
        >{{ member.name }}</span>
        <span
          v-if="showPosition && member.position"
          class="member-position"
          :title="member.position"
        >
          {{ member.position }}
        </span>
      </div>
    </div>

    <!-- 加载更多 -->
    <div
      v-if="hasMore && !loadingMore"
      class="load-more"
    >
      <el-button
        text
        @click="$emit('load-more')"
      >
        <el-icon><More /></el-icon>
        加载更多
      </el-button>
    </div>

    <div
      v-if="loadingMore"
      class="loading-more"
    >
      <el-icon class="is-loading">
        <Loading />
      </el-icon>
      <span>加载中...</span>
    </div>

    <!-- 空状态 -->
    <div
      v-if="filteredMembers.length === 0 && !loading"
      class="empty-state"
    >
      <el-icon><UserFilled /></el-icon>
      <span>{{ emptyText }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, Plus, Crown, UserFilled, More, Loading } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  // 成员列表数据
  members: {
    type: Array,
    default: () => []
  },
  // 每行显示的成员数
  columnsPerRow: {
    type: Number,
    default: 6
  },
  // 头像大小
  avatarSize: {
    type: Number,
    default: 48
  },
  // 是否可点击
  clickable: {
    type: Boolean,
    default: true
  },
  // 是否显示角色标识
  showRole: {
    type: Boolean,
    default: true
  },
  // 是否显示在线状态
  showOnlineStatus: {
    type: Boolean,
    default: false
  },
  // 是否显示职位
  showPosition: {
    type: Boolean,
    default: false
  },
  // 是否显示邀请按钮
  showAddButton: {
    type: Boolean,
    default: false
  },
  // 是否可以添加成员（权限控制）
  canAddMember: {
    type: Boolean,
    default: true
  },
  // 是否显示搜索
  showSearch: {
    type: Boolean,
    default: false
  },
  // 是否有更多数据
  hasMore: {
    type: Boolean,
    default: false
  },
  // 是否正在加载
  loading: {
    type: Boolean,
    default: false
  },
  // 是否正在加载更多
  loadingMore: {
    type: Boolean,
    default: false
  },
  // 空状态文本
  emptyText: {
    type: String,
    default: '暂无成员'
  }
})

const emit = defineEmits(['member-click', 'add-member', 'load-more'])

const searchKeyword = ref('')

// 过滤后的成员列表
const filteredMembers = computed(() => {
  if (!searchKeyword.value) {
    return props.members
  }
  const keyword = searchKeyword.value.toLowerCase()
  return props.members.filter(member =>
    (member.name && member.name.toLowerCase().includes(keyword)) ||
    (member.position && member.position.toLowerCase().includes(keyword)) ||
    (member.remark && member.remark.toLowerCase().includes(keyword))
  )
})

// 成员点击处理
const handleMemberClick = member => {
  if (props.clickable) {
    emit('member-click', member)
  }
}
</script>

<style scoped lang="scss">
// 钉钉颜色变量
$dt-text-primary: #1F2329;
$dt-text-secondary: #646A73;
$dt-text-tertiary: #8F959E;
$dt-bg-hover: #F5F5F5;
$dt-border-color: #E5E6EB;

.member-grid-container {
  .member-search {
    margin-bottom: 16px;
  }

  .members-grid {
    display: grid;
    gap: 16px;

    // 每行4列
    &.grid-4 {
      grid-template-columns: repeat(4, 1fr);
    }

    // 每行5列
    &.grid-5 {
      grid-template-columns: repeat(5, 1fr);
    }

    // 每行6列（默认）
    &.grid-6 {
      grid-template-columns: repeat(6, 1fr);
    }

    // 每行8列
    &.grid-8 {
      grid-template-columns: repeat(8, 1fr);
    }
  }
}

.member-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 8px;
  border-radius: var(--dt-radius-md);
  transition: all 0.15s;

  &.clickable {
    cursor: pointer;

    &:hover {
      background: $dt-bg-hover;

      .member-avatar {
        transform: scale(1.05);
      }
    }
  }

  .member-avatar-wrap {
    position: relative;

    :deep(.member-avatar) {
      border-radius: 50%;
      transition: transform 0.15s;
    }

    // 角色徽章
    .role-badge {
      position: absolute;
      bottom: -2px;
      right: -2px;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      box-shadow: var(--dt-shadow-2);

      &.owner {
        background: #FF4D4F;
      }

      &.admin {
        background: #FAAD14;
      }

      .el-icon {
        font-size: 12px;
      }
    }

    // 在线状态
    .online-status {
      position: absolute;
      bottom: 2px;
      left: 2px;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      border: 2px solid #fff;

      &.online {
        background: #52C41A;
      }

      &.busy {
        background: #FAAD14;
      }

      &.offline {
        background: #D9D9D9;
      }
    }

    // 邀请按钮的头像
    &.add-avatar {
      width: 48px;
      height: 48px;
      border: 1px dashed $dt-border-color;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: $dt-text-tertiary;
      transition: all 0.15s;

      .el-icon {
        font-size: 20px;
      }
    }
  }

  &.add-member-item {
    cursor: pointer;

    &:hover .add-avatar {
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }
  }

  .member-name {
    font-size: 13px;
    color: $dt-text-primary;
    text-align: center;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .member-position {
    font-size: 11px;
    color: $dt-text-tertiary;
    text-align: center;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.load-more,
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  color: $dt-text-secondary;
  font-size: 14px;

  .el-icon {
    margin-right: 4px;
  }
}

.loading-more {
  color: $dt-text-tertiary;

  .is-loading {
    animation: rotating 2s linear infinite;
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: $dt-text-tertiary;

  .el-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }

  span {
    font-size: 14px;
  }
}

// 暗色模式适配
:global(.dark) {
  .member-item {
    .member-name {
      color: #94a3b8;
    }

    .member-position {
      color: #64748b;
    }

    &.clickable:hover {
      background: var(--dt-white-05);
    }
  }

  .add-avatar {
    border-color: #475569;
    color: #94a3b8;

    &:hover {
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }
  }

  .online-status {
    border-color: #1e293b;
  }
}
</style>
