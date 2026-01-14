<template>
  <div class="groups-list">
    <div ref="groupsContainer" class="groups-items">
      <transition-group name="group-list" tag="div" class="groups-list-inner">
        <div
          v-for="group in filteredGroups"
          :key="group.id"
          class="group-item"
          :class="{ active: activeGroupId === group.id, collapsed: isCollapsed }"
          @click="onSelect(group)"
          @mouseenter="hoveredGroupId = group.id"
          @mouseleave="hoveredGroupId = null"
        >
          <div class="group-avatar">
            <el-avatar :src="group.avatar" :size="isCollapsed ? 36 : 40">
              {{ group.name?.charAt(0) || 'G' }}
            </el-avatar>
            <div class="member-count">{{ group.memberCount || 0 }}</div>
          </div>

          <div class="group-info" :class="{ hidden: isCollapsed }">
            <div class="group-name">{{ group.name }}</div>
            <div class="group-desc">{{ group.description || '暂无描述' }}</div>
          </div>

          <div class="group-actions" :class="{ hidden: isCollapsed }" @click.stop>
            <el-button
              :icon="Plus"
              circle
              size="small"
              text
              title="邀请成员"
              @click="handleInvite(group)"
            />
            <el-dropdown trigger="click" @command="cmd => handleGroupAction(cmd, group)">
              <el-button :icon="MoreFilled" circle size="small" text />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="info">
                    <el-icon><InfoFilled /></el-icon>
                    群组信息
                  </el-dropdown-item>
                  <el-dropdown-item command="members">
                    <el-icon><User /></el-icon>
                    成员管理
                  </el-dropdown-item>
                  <el-dropdown-item command="leave" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出群组
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </transition-group>

      <transition name="empty-state">
        <div v-if="filteredGroups.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="el-icon-user-solid"></i>
          </div>
          <div class="empty-text">暂无群组</div>
          <el-button type="primary" size="small" @click="handleCreateGroup"> 创建群组 </el-button>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, MoreFilled, InfoFilled, User, SwitchButton } from '@element-plus/icons-vue'

const props = defineProps({
  groups: { type: Array, default: () => [] },
  isCollapsed: { type: Boolean, default: false },
})

const emit = defineEmits(['select'])

const store = useStore()
const router = useRouter()

const hoveredGroupId = ref(null)
const activeGroupId = ref(null)
const groupsContainer = ref(null)

const filteredGroups = computed(() => {
  return Array.isArray(props.groups) ? props.groups : []
})

const onSelect = group => {
  activeGroupId.value = group.id
  emit('select', group)
  // 切换到群组会话
  store.dispatch('im/switchToGroup', group).then(() => {
    router.push(`/im/chat/${group.id}`)
  })
}

const handleInvite = group => {
  ElMessage.info(`邀请成员到 ${group.name}`)
  // TODO: 实现邀请成员逻辑
}

const handleCreateGroup = () => {
  router.push('/im/group/create')
}

const handleGroupAction = (command, group) => {
  switch (command) {
    case 'info':
      router.push(`/im/group/${group.id}`)
      break
    case 'members':
      router.push(`/im/group/${group.id}/members`)
      break
    case 'leave':
      store.dispatch('im/leaveGroup', group.id)
      break
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.groups-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: $bg-white;

  .groups-items {
    flex: 1;
    overflow-y: auto;
    position: relative;
    @include custom-scrollbar(6px, $border-dark);

    .groups-list-inner {
      min-height: 100%;
    }

    .group-item {
      display: flex;
      align-items: center;
      padding: $spacing-md;
      cursor: pointer;
      position: relative;
      transition: all $transition-base $ease-out;
      border-left: 3px solid transparent;

      &:hover {
        background-color: $bg-hover;
        transform: translateX(2px);

        .group-actions {
          opacity: 1;
          transform: translateX(0);
        }
      }

      &.active {
        background-color: $primary-color-light;
        border-left-color: $primary-color;

        .group-name {
          color: $primary-color;
        }
      }

      &.collapsed {
        justify-content: center;
        padding: $spacing-md;

        .group-avatar {
          margin-right: 0;
        }
      }

      .group-avatar {
        position: relative;
        margin-right: $spacing-md;
        flex-shrink: 0;
        transition: margin-right $transition-base $ease-base;

        :deep(.el-avatar) {
          border: 2px solid transparent;
          transition: all $transition-base $ease-base;
          background: linear-gradient(135deg, $primary-color 0%, $success-color 100%);

          &:hover {
            border-color: $primary-color;
            transform: scale(1.05);
          }
        }

        .member-count {
          position: absolute;
          bottom: -2px;
          right: -2px;
          background: $warning-color;
          color: #fff;
          border-radius: 50%;
          width: 20px;
          height: 20px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 10px;
          font-weight: 500;
          border: 2px solid $bg-white;
          box-shadow: $shadow-sm;
        }
      }

      .group-info {
        flex: 1;
        min-width: 0;
        transition: all $transition-base $ease-base;

        &.hidden {
          display: none;
        }

        .group-name {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
          @include text-ellipsis;
          margin-bottom: 4px;
          transition: color $transition-base $ease-base;
        }

        .group-desc {
          font-size: 12px;
          color: $text-tertiary;
          @include text-ellipsis;
        }
      }

      .group-actions {
        display: flex;
        gap: 4px;
        margin-left: $spacing-sm;
        opacity: 0;
        transform: translateX(10px);
        transition: all $transition-base $ease-base;

        &.hidden {
          display: none;
        }

        :deep(.el-button) {
          color: $text-tertiary;
          transition: all $transition-base $ease-base;

          &:hover {
            color: $primary-color;
            background-color: $bg-hover;
          }
        }
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px $spacing-xl;
      color: $text-tertiary;

      .empty-icon {
        font-size: 64px;
        color: $border-dark;
        margin-bottom: $spacing-lg;
      }

      .empty-text {
        font-size: 14px;
        color: $text-secondary;
        margin-bottom: $spacing-md;
      }

      :deep(.el-button) {
        margin-top: $spacing-md;
      }
    }
  }
}

.group-list-enter-active,
.group-list-leave-active {
  transition: all 0.3s ease;
}

.group-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.group-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.group-list-move {
  transition: transform 0.3s ease;
}

.empty-state-enter-active,
.empty-state-leave-active {
  transition: all 0.3s ease;
}

.empty-state-enter-from,
.empty-state-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

@media (max-width: $breakpoint-md) {
  .groups-list {
    .groups-items {
      .group-item {
        padding: $spacing-md $spacing-sm;

        .group-avatar {
          :deep(.el-avatar) {
            width: 36px !important;
            height: 36px !important;
          }

          .member-count {
            width: 18px;
            height: 18px;
            font-size: 9px;
          }
        }

        .group-actions {
          :deep(.el-button) {
            width: 24px;
            height: 24px;
          }
        }
      }
    }
  }
}
</style>
