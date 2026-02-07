<!--
  群组详情头部组件

  显示群组头像、名称、描述、标签等基本信息
-->
<template>
  <div class="profile-sidebar">
    <!-- 关闭按钮 -->
    <el-button
      class="close-btn"
      circle
      @click="$emit('close')"
    >
      <el-icon><Close /></el-icon>
    </el-button>

    <div class="sidebar-content">
      <div class="avatar-wrapper">
        <DingtalkAvatar
          :src="groupInfo.avatar"
          :name="groupInfo.name"
          :size="100"
          shape="square"
        />
      </div>

      <h2 class="group-name">
        {{ groupInfo.name }}
      </h2>
      <p class="group-meta">
        <span class="group-id">群号: {{ groupInfo.groupCode || groupInfo.id }}</span>
        <span class="member-count">{{ memberCount }} 人</span>
      </p>
      <p class="group-intro">
        {{ groupInfo.description || '暂无群简介' }}
      </p>

      <div class="group-tags">
        <el-tag
          v-if="groupInfo.groupType"
          size="small"
          type="primary"
          effect="light"
        >
          {{ groupInfo.groupType === 'INTERNAL' ? '内部群' : '外部群' }}
        </el-tag>
        <el-tag
          v-if="isOwner"
          size="small"
          type="danger"
          effect="light"
        >
          群主
        </el-tag>
        <el-tag
          v-else-if="isAdmin"
          size="small"
          type="warning"
          effect="light"
        >
          管理员
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  groupInfo: {
    type: Object,
    required: true
  },
  memberCount: {
    type: Number,
    default: 0
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

const emit = defineEmits(['close'])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.profile-sidebar {
  width: 240px;
  background: linear-gradient(180deg, var(--dt-brand-color) 0%, #0e5fd9 100%);
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  position: relative;
  flex-shrink: 0;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--dt-white-20);
  border: none;
  color: var(--dt-text-inverse);

  &:hover {
    background: var(--dt-white-30);
  }
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  flex: 1;
}

.avatar-wrapper {
  margin-bottom: 16px;
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
  box-shadow: var(--dt-shadow-dialog);
}

.group-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-inverse);
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.group-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--dt-white-80);
  margin: 0 0 12px 0;
}

.group-intro {
  font-size: 13px;
  color: var(--dt-white-90);
  line-height: 1.5;
  margin: 0 0 16px 0;
  max-width: 200px;
}

.group-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}
</style>
