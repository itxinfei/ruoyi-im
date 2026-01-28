<template>
  <div class="account-settings">
    <div class="setting-group">
      <h3 class="group-title">个人信息</h3>
      <div class="profile-card">
        <div class="profile-header">
          <el-avatar :size="80" :src="user.avatar" class="profile-avatar" />
          <div class="profile-primary">
            <h2 class="profile-name">{{ user.nickname || user.username }}</h2>
            <p class="profile-uid">UID: {{ user.id }}</p>
          </div>
          <el-button type="primary" plain round @click="$emit('edit-profile')">编辑资料</el-button>
        </div>
        
        <div class="profile-details">
          <div class="detail-item">
            <span class="detail-label">账号</span>
            <span class="detail-value">{{ user.username }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">邮箱</span>
            <span class="detail-value">{{ user.email || '未绑定' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">手机</span>
            <span class="detail-value">{{ user.phonenumber || '未绑定' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">部门</span>
            <span class="detail-value">{{ user.dept?.deptName || '无' }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="setting-group">
      <h3 class="group-title">账号安全</h3>
      <div class="setting-list">
        <div class="setting-item clickable" @click="$emit('change-password')">
          <div class="item-content">
            <div class="item-title">登录密码</div>
            <div class="item-desc">定期修改密码以保护账号安全</div>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">双重验证</div>
            <div class="item-desc">开启两步验证，提升账号安全性</div>
          </div>
          <el-switch v-model="twoFactorEnabled" disabled />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'

defineProps({
  user: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

defineEmits(['edit-profile', 'change-password'])

const twoFactorEnabled = ref(false)
</script>

<style scoped lang="scss">
.account-settings {
  padding-bottom: 20px;
}

.setting-group {
  margin-bottom: 32px;
}

.group-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  margin-bottom: 16px;
  padding-left: 4px;
}

.profile-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  overflow: hidden;
  
  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.profile-header {
  display: flex;
  align-items: center;
  padding: 24px;
  background: var(--dt-bg-hover); // Slightly different bg for header
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    background: rgba(255, 255, 255, 0.03);
    border-bottom-color: var(--dt-border-dark);
  }
}

.profile-avatar {
  border: 4px solid var(--dt-bg-card);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  .dark & {
    border-color: var(--dt-bg-card-dark);
  }
}

.profile-primary {
  flex: 1;
  margin-left: 20px;
}

.profile-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.profile-uid {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--dt-text-tertiary);
  font-family: monospace;
}

.profile-details {
  padding: 16px 24px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-label {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-bottom: 4px;
}

.detail-value {
  font-size: 14px;
  color: var(--dt-text-primary);
  font-weight: 500;
}

.setting-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  overflow: hidden;
  
  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);
  transition: background-color 0.2s;
  
  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }
  
  &.clickable {
    cursor: pointer;
    
    &:hover {
      background-color: var(--dt-bg-hover);
    }
  }
}

.item-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.item-desc {
  font-size: 13px;
  color: var(--dt-text-secondary);
}
</style>
