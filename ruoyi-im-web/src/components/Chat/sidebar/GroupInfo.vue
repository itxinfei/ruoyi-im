<template>
  <div class="group-info-dingtalk">
    <!-- 1. 成员九宫格区域 -->
    <div class="members-section">
      <div class="section-header">
        <span class="title">群成员 ({{ detail.memberCount || members.length }})</span>
        <span
          class="more"
          @click="$emit('view-members')"
        >
          <span class="material-icons-outlined">chevron_right</span>
        </span>
      </div>
      <div class="members-grid">
        <!-- 成员头像 -->
        <div 
          v-for="member in displayMembers" 
          :key="member.id" 
          class="member-box"
          @click="$emit('show-user', member.id)"
        >
          <DingtalkAvatar
            :name="member.nickname || member.username"
            :src="member.avatar"
            :size="42"
            shape="square"
          />
          <span class="name">{{ member.nickname || member.username }}</span>
        </div>
        <!-- 邀请按钮 -->
        <div
          class="member-box action"
          @click="$emit('add-member')"
        >
          <div class="action-btn-inner">
            <span class="material-icons-outlined">add</span>
          </div>
          <span class="name">邀请</span>
        </div>
        <!-- 移除按钮 (仅管理员/群主) -->
        <div
          v-if="isAdmin"
          class="member-box action"
          @click="$emit('remove-member')"
        >
          <div class="action-btn-inner">
            <span class="material-icons-outlined">remove</span>
          </div>
          <span class="name">移除</span>
        </div>
      </div>
    </div>

    <div class="divider-space" />

    <!-- 2. 群基础设置列表 -->
    <div class="settings-list">
      <div
        class="list-item"
        @click="$emit('edit-name')"
      >
        <span class="label">群聊名称</span>
        <div class="value-container">
          <span class="value">{{ detail.name }}</span>
          <span class="material-icons-outlined arrow">chevron_right</span>
        </div>
      </div>
      <div
        class="list-item"
        @click="$emit('view-qr')"
      >
        <span class="label">群二维码</span>
        <div class="value-container">
          <span class="material-icons-outlined qr-icon">qr_code_2</span>
          <span class="material-icons-outlined arrow">chevron_right</span>
        </div>
      </div>
      <div
        class="list-item"
        @click="$emit('edit-announcement')"
      >
        <span class="label">群公告</span>
        <span class="material-icons-outlined arrow">chevron_right</span>
      </div>
      <div
        v-if="detail.announcement"
        class="announcement-preview"
      >
        {{ detail.announcement }}
      </div>
    </div>

    <div class="divider-space" />

    <!-- 3. 交互设置 -->
    <div class="settings-list">
      <div class="list-item no-click">
        <span class="label">消息免打扰</span>
        <el-switch
          v-model="localMuted"
          @change="$emit('mute-change', $event)"
        />
      </div>
      <div class="list-item no-click">
        <span class="label">置顶聊天</span>
        <el-switch
          v-model="localPinned"
          @change="$emit('pin-change', $event)"
        />
      </div>
      <div
        class="list-item"
        @click="$emit('view-files')"
      >
        <span class="label">查找聊天内容</span>
        <span class="material-icons-outlined arrow">chevron_right</span>
      </div>
    </div>

    <div class="divider-space" />

    <!-- 4. 危险操作区 -->
    <div class="settings-list danger-zone">
      <div
        class="list-item text-center"
        @click="$emit('clear-history')"
      >
        <span class="label danger">清空聊天记录</span>
      </div>
      <div
        v-if="isOwner"
        class="list-item text-center"
        @click="$emit('dismiss-group')"
      >
        <span class="label danger">解散群聊</span>
      </div>
      <div
        v-else
        class="list-item text-center"
        @click="$emit('exit-group')"
      >
        <span class="label danger">退出群聊</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  detail: { type: Object, required: true },
  members: { type: Array, default: () => [] },
  currentUserRole: { type: String, default: 'MEMBER' }
})

const emit = defineEmits([
  'view-members', 'view-files', 'edit-announcement', 'mute-change', 'pin-change', 
  'show-user', 'add-member', 'remove-member', 'edit-name', 'view-qr', 'exit-group', 'dismiss-group', 'clear-history'
])

const displayMembers = computed(() => props.members.slice(0, 10))
const isAdmin = computed(() => ['ADMIN', 'OWNER'].includes(props.detail.memberRole || props.currentUserRole))
const isOwner = computed(() => (props.detail.memberRole || props.currentUserRole) === 'OWNER')

const localMuted = ref(false)
const localPinned = ref(false)

watch(() => props.detail, val => {
  localMuted.value = !!val.isMuted
  localPinned.value = !!val.isPinned
}, { immediate: true })
</script>

<style scoped lang="scss">
.group-info-dingtalk {
  background: #f5f7fa;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: #171a1d;
}

.divider-space {
  height: 12px;
  background: #f5f7fa;
  flex-shrink: 0;
}

/* 成员九宫格 */
.members-section {
  background: #fff;
  padding: 16px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    .title { font-size: 15px; font-weight: 600; }
    .more { color: #8c8c8c; cursor: pointer; display: flex; align-items: center; }
  }
  
  .members-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 12px 8px;
  }
  
  .member-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    
    .name {
      font-size: 11px;
      color: #8c8c8c;
      width: 100%;
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    &.action {
      .action-btn-inner {
        width: 42px;
        height: 42px;
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #8c8c8c;
        transition: all 0.2s;
        &:hover { border-color: #3296fa; color: #3296fa; }
      }
    }
  }
}

/* 列表样式 */
.settings-list {
  background: #fff;
  
  .list-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    min-height: 52px;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child { border-bottom: none; }
    &:active { background: #f9f9f9; }
    &.no-click { cursor: default; &:active { background: #fff; } }
    &.text-center { justify-content: center; }
    
    .label { font-size: 15px; color: #171a1d; }
    .label.danger { color: #ff4d4f; }
    
    .value-container {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #8c8c8c;
      font-size: 14px;
      max-width: 60%;
      
      .value { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .qr-icon { font-size: 18px; }
      .arrow { font-size: 18px; }
    }
  }
  
  .announcement-preview {
    padding: 0 16px 12px;
    font-size: 13px;
    color: #8c8c8c;
    line-height: 1.5;
  }
}

/* 暗色模式适配 */
:global(.dark) {
  .group-info-dingtalk, .divider-space { background: #121212; }
  .members-section, .settings-list { background: #1e1e1e; }
  .members-section .section-header .title, .list-item .label { color: #e8e8e8; }
  .list-item { border-bottom-color: #2d2d2d; }
  .list-item:active { background: #252525; }
}
</style>
