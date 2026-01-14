<template>
  <div class="contacts-list">
    <div ref="contactsContainer" class="contacts-items">
      <transition-group name="contact-list" tag="div" class="contacts-list-inner">
        <div
          v-for="contact in filteredContacts"
          :key="contact.id"
          class="contact-item"
          :class="{ active: activeContactId === contact.id, collapsed: isCollapsed }"
          @click="onSelect(contact)"
          @mouseenter="hoveredContactId = contact.id"
          @mouseleave="hoveredContactId = null"
        >
          <div class="contact-avatar">
            <el-badge :is-dot="contact.online" class="status-badge">
              <el-avatar :src="contact.avatar" :size="isCollapsed ? 36 : 40">
                {{ contact.nickname?.charAt(0) || 'U' }}
              </el-avatar>
            </el-badge>
          </div>

          <div class="contact-info" :class="{ hidden: isCollapsed }">
            <div class="contact-name">{{ contact.nickname || contact.username }}</div>
            <div class="contact-status">
              <span class="status-dot" :class="contact.online ? 'online' : 'offline'"></span>
              <span class="status-text">{{ contact.online ? '在线' : '离线' }}</span>
            </div>
          </div>

          <div class="contact-actions" :class="{ hidden: isCollapsed }" @click.stop>
            <el-button
              :icon="Phone"
              circle
              size="small"
              text
              title="语音通话"
              @click="handleCall(contact)"
            />
            <el-button
              :icon="VideoCamera"
              circle
              size="small"
              text
              title="视频通话"
              @click="handleVideoCall(contact)"
            />
            <el-dropdown trigger="click" @command="cmd => handleContactAction(cmd, contact)">
              <el-button :icon="MoreFilled" circle size="small" text />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="info">
                    <el-icon><User /></el-icon>
                    查看资料
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除联系人
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </transition-group>

      <transition name="empty-state">
        <div v-if="filteredContacts.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="empty-text">暂无联系人</div>
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
import { Phone, VideoCamera, MoreFilled, User, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  contacts: { type: Array, default: () => [] },
  isCollapsed: { type: Boolean, default: false },
})

const emit = defineEmits(['select'])

const store = useStore()
const router = useRouter()

const hoveredContactId = ref(null)
const activeContactId = ref(null)
const contactsContainer = ref(null)

const filteredContacts = computed(() => {
  return Array.isArray(props.contacts) ? props.contacts : []
})

const onSelect = contact => {
  activeContactId.value = contact.id
  emit('select', contact)
  // 切换到与该联系人的私聊
  store.dispatch('im/switchToContact', contact).then(() => {
    router.push(`/im/chat/${contact.id}`)
  })
}

const handleCall = contact => {
  ElMessage.info(`正在呼叫 ${contact.nickname || contact.username}...`)
  // TODO: 实现语音通话逻辑
}

const handleVideoCall = contact => {
  ElMessage.info(`正在发起视频通话 ${contact.nickname || contact.username}...`)
  // TODO: 实现视频通话逻辑
}

const handleContactAction = (command, contact) => {
  switch (command) {
    case 'info':
      router.push(`/im/contact/${contact.id}`)
      break
    case 'delete':
      store.dispatch('im/deleteContact', contact.id)
      break
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.contacts-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: $bg-white;

  .contacts-items {
    flex: 1;
    overflow-y: auto;
    position: relative;
    @include custom-scrollbar(6px, $border-dark);

    .contacts-list-inner {
      min-height: 100%;
    }

    .contact-item {
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

        .contact-actions {
          opacity: 1;
          transform: translateX(0);
        }
      }

      &.active {
        background-color: $primary-color-light;
        border-left-color: $primary-color;

        .contact-name {
          color: $primary-color;
        }
      }

      &.collapsed {
        justify-content: center;
        padding: $spacing-md;

        .contact-avatar {
          margin-right: 0;
        }
      }

      .contact-avatar {
        position: relative;
        margin-right: $spacing-md;
        flex-shrink: 0;
        transition: margin-right $transition-base $ease-base;

        .status-badge {
          :deep(.el-badge__content) {
            border: 2px solid $bg-white;
            box-shadow: $shadow-sm;
          }
        }

        :deep(.el-avatar) {
          border: 2px solid transparent;
          transition: all $transition-base $ease-base;

          &:hover {
            border-color: $primary-color;
            transform: scale(1.05);
          }
        }
      }

      .contact-info {
        flex: 1;
        min-width: 0;
        transition: all $transition-base $ease-base;

        &.hidden {
          display: none;
        }

        .contact-name {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
          @include text-ellipsis;
          margin-bottom: 4px;
          transition: color $transition-base $ease-base;
        }

        .contact-status {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: $text-tertiary;

          .status-dot {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            flex-shrink: 0;

            &.online {
              background-color: $success-color;
            }

            &.offline {
              background-color: $text-placeholder;
            }
          }
        }
      }

      .contact-actions {
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
      }
    }
  }
}

.contact-list-enter-active,
.contact-list-leave-active {
  transition: all 0.3s ease;
}

.contact-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.contact-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.contact-list-move {
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
  .contacts-list {
    .contacts-items {
      .contact-item {
        padding: $spacing-md $spacing-sm;

        .contact-avatar {
          :deep(.el-avatar) {
            width: 36px !important;
            height: 36px !important;
          }
        }

        .contact-actions {
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
