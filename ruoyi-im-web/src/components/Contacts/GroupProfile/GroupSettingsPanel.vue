<!--
  群组设置面板组件

  显示群组设置选项，包括群信息、权限、个人设置和危险操作
-->
<template>
  <div class="tab-content">
    <!-- 群信息设置 -->
    <div class="settings-section">
      <div class="section-title">
        群信息
      </div>
      <div class="settings-list">
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-name">群名称</span>
            <span class="setting-value">{{ groupInfo.name }}</span>
          </div>
          <el-button
            v-if="isOwnerOrAdmin"
            link
            type="primary"
            @click="$emit('edit-name')"
          >
            <el-icon><Edit /></el-icon>修改
          </el-button>
        </div>
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-name">群简介</span>
            <span class="setting-value">{{ groupInfo.description || '暂无简介' }}</span>
          </div>
          <el-button
            v-if="isOwnerOrAdmin"
            link
            type="primary"
            @click="$emit('edit-desc')"
          >
            <el-icon><Edit /></el-icon>修改
          </el-button>
        </div>
      </div>
    </div>

    <!-- 群权限设置 -->
    <div
      v-if="isOwnerOrAdmin"
      class="settings-section"
    >
      <div class="section-title">
        群权限
      </div>
      <div class="settings-list">
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-name">入群验证</span>
            <span class="setting-desc">新成员加入需要管理员审核</span>
          </div>
          <el-switch
            :model-value="settings.needVerify"
            @change="$emit('setting-change', 'needVerify', $event)"
          />
        </div>
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-name">允许成员邀请</span>
            <span class="setting-desc">群成员可以邀请其他人加入</span>
          </div>
          <el-switch
            :model-value="settings.allowInvite"
            @change="$emit('setting-change', 'allowInvite', $event)"
          />
        </div>
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-name">全员禁言</span>
            <span class="setting-desc">只有群主和管理员可以发言</span>
          </div>
          <el-switch
            :model-value="settings.allMuted"
            @change="$emit('setting-change', 'allMuted', $event)"
          />
        </div>
      </div>
    </div>

    <!-- 我的设置 -->
    <div class="settings-section">
      <div class="section-title">
        我的设置
      </div>
      <div class="settings-list">
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-name">消息免打扰</span>
            <span class="setting-desc">不再接收该群的消息通知</span>
          </div>
          <el-switch
            :model-value="isMuted"
            @change="$emit('mute-change', $event)"
          />
        </div>
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-name">置顶会话</span>
            <span class="setting-desc">将该群置顶在会话列表</span>
          </div>
          <el-switch
            :model-value="isTop"
            @change="$emit('top-change', $event)"
          />
        </div>
      </div>
    </div>

    <!-- 危险操作 -->
    <div class="settings-section danger-section">
      <div class="section-title">
        危险操作
      </div>
      <div class="action-list">
        <div
          v-if="isOwner"
          class="action-item"
          @click="$emit('transfer')"
        >
          <div class="action-icon">
            <el-icon><SwitchIcon /></el-icon>
          </div>
          <div class="action-content">
            <span class="action-name">转让群主</span>
            <span class="action-desc">将群主身份转让给其他成员</span>
          </div>
          <el-icon class="action-arrow">
            <ArrowRight />
          </el-icon>
        </div>
        <div
          v-if="isOwner"
          class="action-item danger"
          @click="$emit('dismiss')"
        >
          <div class="action-icon">
            <el-icon><Delete /></el-icon>
          </div>
          <div class="action-content">
            <span class="action-name">解散群聊</span>
            <span class="action-desc">解散后将删除所有聊天记录</span>
          </div>
          <el-icon class="action-arrow">
            <ArrowRight />
          </el-icon>
        </div>
        <div
          v-else
          class="action-item danger"
          @click="$emit('exit')"
        >
          <div class="action-icon">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="action-content">
            <span class="action-name">退出群聊</span>
            <span class="action-desc">退出后将不再接收群消息</span>
          </div>
          <el-icon class="action-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Edit, Switch as SwitchIcon, Delete, CircleClose, ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  groupInfo: {
    type: Object,
    required: true
  },
  settings: {
    type: Object,
    default: () => ({})
  },
  isMuted: {
    type: Boolean,
    default: false
  },
  isTop: {
    type: Boolean,
    default: false
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

const isOwnerOrAdmin = computed(() => props.isOwner || props.isAdmin)

const emit = defineEmits([
  'edit-name',
  'edit-desc',
  'setting-change',
  'mute-change',
  'top-change',
  'transfer',
  'dismiss',
  'exit'
])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.settings-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }

  &.danger-section {
    .section-title {
      color: #f56c6c;
    }
  }
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  margin-bottom: 12px;
}

.settings-list {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  overflow: hidden;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--dt-border-light);

  &:last-child {
    border-bottom: none;
  }
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.setting-name {
  font-size: 14px;
  color: var(--dt-text-primary);
}

.setting-value {
  font-size: 13px;
  color: var(--dt-text-tertiary);
}

.setting-desc {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.action-list {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  overflow: hidden;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--dt-border-light);
  cursor: pointer;
  transition: background-color var(--dt-transition-base);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.danger {
    .action-icon {
      color: #f56c6c;
      background: rgba(245, 108, 108, 0.1);
    }

    .action-name {
      color: #f56c6c;
    }
  }
}

.action-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-secondary);
  color: var(--dt-text-secondary);
}

.action-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.action-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
}

.action-desc {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.action-arrow {
  color: var(--dt-text-placeholder);
}
</style>
