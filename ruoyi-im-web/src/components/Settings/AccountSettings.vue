<template>
  <div class="account-settings">
    <!-- 基本信息 -->
    <section class="setting-group">
      <h3 class="group-title">基本信息</h3>
      <div class="setting-list">
        <div class="setting-row">
          <span class="row-label">头像</span>
          <div class="row-value">
            <el-avatar :size="36" :src="user.avatar" />
            <el-button link type="primary" size="small" @click="$emit('edit-profile')">修改</el-button>
          </div>
        </div>
        <div class="setting-row">
          <span class="row-label">昵称</span>
          <span class="row-value">{{ user.nickname || user.username }}</span>
        </div>
        <div class="setting-row">
          <span class="row-label">账号</span>
          <span class="row-value">{{ user.username }}</span>
        </div>
        <div class="setting-row">
          <span class="row-label">UID</span>
          <span class="row-value text-mono">{{ user.id }}</span>
        </div>
        <div class="setting-row">
          <span class="row-label">部门</span>
          <span class="row-value">{{ user.dept?.deptName || '-' }}</span>
        </div>
        <div class="setting-row">
          <span class="row-label">邮箱</span>
          <span class="row-value">{{ user.email || '-' }}</span>
        </div>
        <div class="setting-row">
          <span class="row-label">手机</span>
          <span class="row-value">{{ user.phonenumber || '-' }}</span>
        </div>
      </div>
    </section>

    <!-- 账号安全 -->
    <section class="setting-group">
      <h3 class="group-title">账号安全</h3>
      <div class="setting-list">
        <div class="setting-row clickable" @click="$emit('change-password')">
          <span class="row-label">
            <el-icon class="row-icon"><Lock /></el-icon>
            登录密码
          </span>
          <div class="row-value">
            <span class="status-text">已设置</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Lock /></el-icon>
            双重验证
          </span>
          <el-switch v-model="twoFactorEnabled" disabled size="small" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowRight, Lock } from '@element-plus/icons-vue'

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
  padding: 0;
}

.setting-group {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.group-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.setting-list {
  background: #fafafa;
  border: 1px solid #e8e8e8;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  min-height: 40px;
  border-bottom: 1px solid #e8e8e8;

  &:last-child {
    border-bottom: none;
  }

  &.clickable {
    cursor: pointer;

    &:hover {
      background: #f0f0f0;
    }
  }
}

.row-label {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 6px;

  .row-icon {
    font-size: 14px;
    color: #999;
  }
}

.row-value {
  font-size: 13px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;

  &.text-mono {
    font-family: monospace;
    color: #666;
  }

  .status-text {
    color: #999;
    font-size: 12px;
  }

  .arrow-icon {
    font-size: 12px;
    color: #ccc;
  }
}

// 深色模式
.dark {
  .group-title {
    color: #ccc;
    border-color: #333;
  }

  .setting-list {
    background: #252525;
    border-color: #333;
  }

  .setting-row {
    border-color: #333;

    &.clickable:hover {
      background: #333;
    }
  }

  .row-label {
    color: #999;

    .row-icon {
      color: #666;
    }
  }

  .row-value {
    color: #ccc;

    &.text-mono {
      color: #999;
    }

    .status-text {
      color: #666;
    }

    .arrow-icon {
      color: #444;
    }
  }
}
</style>
