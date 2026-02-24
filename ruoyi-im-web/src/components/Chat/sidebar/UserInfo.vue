<template>
  <div class="user-info">
    <!-- 用户基本信息 -->
    <div class="user-header">
      <DingtalkAvatar
        :name="detail.nickname || detail.username"
        :src="detail.avatar"
        :size="80"
        shape="square"
      />
      <div class="user-meta">
        <div class="name-row">
          <h4 class="nickname">
            {{ detail.nickname || detail.username }}
          </h4>
          <span
            v-if="detail.gender === 1"
            class="gender-icon male"
          >♂</span>
          <span
            v-else-if="detail.gender === 2"
            class="gender-icon female"
          >♀</span>
        </div>
        <div class="username">
          @{{ detail.username }}
        </div>
        <div
          class="status-badge"
          :class="{ online: isOnline }"
        >
          <span class="status-dot" />
          {{ isOnline ? '在线' : '离线' }}
        </div>
      </div>
    </div>

    <el-divider />

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <button
        class="action-btn primary"
        @click="$emit('send-message')"
      >
        <span class="material-icons-outlined">chat</span>
        发消息
      </button>
      <button class="action-btn">
        <span class="material-icons-outlined">videocam</span>
        视频通话
      </button>
    </div>

    <el-divider />

    <!-- 详细信息 -->
    <div class="info-section">
      <div class="info-title">
        详细信息
      </div>
      
      <div class="info-list">
        <div class="info-item">
          <span class="info-label">
            <span class="material-icons-outlined">badge</span>
            职位
          </span>
          <span class="info-value">{{ detail.position || '未设置' }}</span>
        </div>
        
        <div class="info-item">
          <span class="info-label">
            <span class="material-icons-outlined">business</span>
            部门
          </span>
          <span class="info-value">{{ detail.departmentName || detail.department || '未分配' }}</span>
        </div>
        
        <div class="info-item">
          <span class="info-label">
            <span class="material-icons-outlined">phone</span>
            手机
          </span>
          <span class="info-value">{{ detail.mobile || '-' }}</span>
        </div>
        
        <div class="info-item">
          <span class="info-label">
            <span class="material-icons-outlined">email</span>
            邮箱
          </span>
          <span class="info-value">{{ detail.email || '-' }}</span>
        </div>
        
        <div class="info-item">
          <span class="info-label">
            <span class="material-icons-outlined">location_on</span>
            地区
          </span>
          <span class="info-value">{{ detail.location || '-' }}</span>
        </div>
      </div>
    </div>

    <el-divider />

    <!-- 个人签名 -->
    <div
      v-if="detail.signature"
      class="signature-section"
    >
      <div class="info-title">
        个性签名
      </div>
      <p class="signature-content">
        {{ detail.signature }}
      </p>
    </div>

    <!-- 设置 -->
    <div class="user-settings">
      <div class="setting-item">
        <span class="setting-label">消息免打扰</span>
        <el-switch
          v-model="isMuted"
          @change="handleMuteChange"
        />
      </div>
      <div class="setting-item">
        <span class="setting-label">置顶聊天</span>
        <el-switch
          v-model="isPinned"
          @change="handlePinChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  detail: Object,
  isOnline: Boolean
})

const emit = defineEmits(['send-message', 'mute-change', 'pin-change'])

const isMuted = ref(props.detail?.isMuted || false)
const isPinned = ref(props.detail?.isPinned || false)

watch(() => props.detail, newVal => {
  if (newVal) {
    isMuted.value = newVal.isMuted || false
    isPinned.value = newVal.isPinned || false
  }
}, { immediate: true })

const handleMuteChange = val => {
  emit('mute-change', val)
}

const handlePinChange = val => {
  emit('pin-change', val)
}
</script>

<style scoped>
.user-info {
  padding: 8px 0;
}

/* 用户头部 */
.user-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
}

.user-meta {
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.name-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.nickname {
  font-size: 20px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.gender-icon {
  font-size: 14px;
  font-weight: bold;
}

.gender-icon.male { color: #3b82f6; }
.gender-icon.female { color: #ec4899; }

.username {
  font-size: 13px;
  color: var(--dt-text-tertiary);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: var(--dt-bg-subtle);
  border-radius: 12px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.status-badge.online {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.1);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 12px;
  padding: 8px 0;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  color: var(--dt-text-primary);
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: var(--dt-brand-color);
  color: var(--dt-brand-color);
}

.action-btn.primary {
  background: var(--dt-brand-color);
  border-color: var(--dt-brand-color);
  color: #fff;
}

.action-btn.primary:hover {
  background: var(--dt-brand-hover);
}

.action-btn .material-icons-outlined {
  font-size: 20px;
}

/* 信息区 */
.info-section {
  padding: 8px 0;
}

.info-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 3px solid var(--dt-brand-color);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.info-item:hover {
  background: var(--dt-bg-hover);
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--dt-text-secondary);
}

.info-label .material-icons-outlined {
  font-size: 18px;
  color: var(--dt-text-tertiary);
}

.info-value {
  font-size: 14px;
  color: var(--dt-text-primary);
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: right;
}

/* 签名 */
.signature-section {
  padding: 8px 0;
}

.signature-content {
  padding: 12px;
  background: var(--dt-bg-subtle);
  border-radius: 8px;
  font-size: 13px;
  color: var(--dt-text-secondary);
  line-height: 1.6;
  margin: 0;
}

/* 设置 */
.user-settings {
  padding: 8px 0;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 8px;
}

.setting-label {
  font-size: 14px;
  color: var(--dt-text-primary);
}

/* 暗色模式 */
:global(.dark) .nickname,
:global(.dark) .info-title,
:global(.dark) .setting-label,
:global(.dark) .info-value {
  color: var(--dt-text-primary-dark);
}

:global(.dark) .signature-content {
  background: rgba(255, 255, 255, 0.05);
}
</style>
