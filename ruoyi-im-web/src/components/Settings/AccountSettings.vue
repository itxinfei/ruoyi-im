<template>
  <div class="account-settings">
    <!-- 个人资料卡片 -->
    <section class="setting-section">
      <div class="section-header">
        <h3 class="section-title">个人资料</h3>
        <p class="section-desc">管理您的基本信息和头像</p>
      </div>

      <div class="setting-card">
        <!-- 头像设置 -->
        <div class="setting-item avatar-item">
          <div class="item-main">
            <span class="item-label">头像</span>
            <span class="item-desc">支持 JPG、PNG 格式,建议尺寸 200x200</span>
          </div>
          <div class="item-action">
            <div class="avatar-wrapper">
              <el-avatar :size="64" :src="user.avatar" class="user-avatar" />
              <div class="avatar-overlay" @click="$emit('edit-profile')">
                <span class="material-icons-outlined">edit</span>
              </div>
            </div>
          </div>
        </div>

        <el-divider class="item-divider" />

        <!-- 昵称 -->
        <div class="setting-item">
          <div class="item-main">
            <span class="item-label">昵称</span>
          </div>
          <div class="item-action">
            <span class="item-value">{{ user.nickname || user.username }}</span>
            <el-button link type="primary" @click="$emit('edit-profile')">
              <span class="material-icons-outlined" style="font-size: 18px;">edit</span>
            </el-button>
          </div>
        </div>

        <el-divider class="item-divider" />

        <!-- 账号ID -->
        <div class="setting-item">
          <div class="item-main">
            <span class="item-label">账号 ID</span>
          </div>
          <div class="item-action">
            <span class="item-value code-text">{{ user.username }}</span>
            <el-button link @click="copyToClipboard(user.username)">
              <span class="material-icons-outlined" style="font-size: 18px;">content_copy</span>
            </el-button>
          </div>
        </div>

        <el-divider class="item-divider" />

        <!-- UID -->
        <div class="setting-item">
          <div class="item-main">
            <span class="item-label">用户编号</span>
          </div>
          <div class="item-action">
            <span class="item-value code-text">{{ user.id }}</span>
            <el-button link @click="copyToClipboard(user.id)">
              <span class="material-icons-outlined" style="font-size: 18px;">content_copy</span>
            </el-button>
          </div>
        </div>

        <el-divider class="item-divider" />

        <!-- 所属部门 -->
        <div class="setting-item">
          <div class="item-main">
            <span class="item-label">所属部门</span>
          </div>
          <div class="item-action">
            <span class="item-value">{{ user.dept?.deptName || '暂无部门' }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 联系信息卡片 -->
    <section class="setting-section">
      <div class="section-header">
        <h3 class="section-title">联系方式</h3>
        <p class="section-desc">用于找回密码和接收通知</p>
      </div>

      <div class="setting-card">
        <div class="setting-item">
          <div class="item-main">
            <span class="item-label">邮箱地址</span>
          </div>
          <div class="item-action">
            <span class="item-value" :class="{ 'text-muted': !user.email }">
              {{ user.email || '未绑定' }}
            </span>
            <el-button v-if="!user.email" link type="primary" @click="$emit('edit-profile')">
              去绑定
            </el-button>
          </div>
        </div>

        <el-divider class="item-divider" />

        <div class="setting-item">
          <div class="item-main">
            <span class="item-label">手机号码</span>
          </div>
          <div class="item-action">
            <span class="item-value" :class="{ 'text-muted': !user.phonenumber }">
              {{ user.phonenumber || '未绑定' }}
            </span>
            <el-button v-if="!user.phonenumber" link type="primary" @click="$emit('edit-profile')">
              去绑定
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 账号安全卡片 -->
    <section class="setting-section">
      <div class="section-header">
        <h3 class="section-title">账号安全</h3>
        <p class="section-desc">保护您的账号安全</p>
      </div>

      <div class="setting-card">
        <div class="setting-item clickable" @click="$emit('change-password')">
          <div class="item-main">
            <span class="item-label">登录密码</span>
            <span class="item-desc">定期修改密码可以保护账号安全</span>
          </div>
          <div class="item-action">
            <span class="status-badge success">已设置</span>
            <span class="material-icons-outlined arrow-icon">chevron_right</span>
          </div>
        </div>

        <el-divider class="item-divider" />

        <div class="setting-item">
          <div class="item-main">
            <span class="item-label">双重验证</span>
            <span class="item-desc">启用后登录时需要进行二次验证(即将推出)</span>
          </div>
          <div class="item-action">
            <el-switch v-model="twoFactorEnabled" disabled />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

defineProps({
  user: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

defineEmits(['edit-profile', 'change-password'])

const twoFactorEnabled = ref(false)

const copyToClipboard = text => {
  navigator.clipboard.writeText(String(text)).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}
</script>

<style scoped lang="scss">
.account-settings {
  max-width: 720px;
}

// 区块样式
.setting-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

// 卡片样式
.setting-card {
  background: #ffffff;
  border: none;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.item-divider {
  margin: 0;
  border-color: #f3f4f6;
}

// 设置项样式
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  min-height: 56px;
  transition: all 0.2s ease;

  &.clickable {
    cursor: pointer;

    &:hover {
      background: #f9fafb;
    }
  }

  &.avatar-item {
    padding: 20px;
  }
}

.item-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.item-label {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.item-desc {
  font-size: 12px;
  color: #6b7280;
}

.item-action {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-value {
  font-size: 14px;
  color: var(--dt-text-primary);

  &.code-text {
    font-family: 'SF Mono', Monaco, monospace;
    background: var(--dt-bg-hover);
    padding: 4px 8px;
    border-radius: var(--dt-radius-sm);
    font-size: 13px;
  }

  &.text-muted {
    color: var(--dt-text-tertiary);
  }
}

// 头像样式
.avatar-wrapper {
  position: relative;
  cursor: pointer;

  .user-avatar {
    border: 2px solid var(--dt-border-light);
    transition: all 0.2s ease;
  }

  .avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: all 0.2s ease;

    span {
      color: white;
      font-size: 24px;
    }
  }

  &:hover {
    .avatar-overlay {
      opacity: 1;
    }

    .user-avatar {
      transform: scale(1.02);
    }
  }
}

// 状态徽章
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: var(--dt-font-weight-medium);

  &.success {
    background: var(--dt-success-bg);
    color: var(--dt-success);
  }
}

.arrow-icon {
  font-size: 20px;
  color: var(--dt-text-tertiary);
}

// 退出登录样式
.logout-item {
  &:hover {
    background: #fef2f2 !important;
  }
}

.logout-label {
  color: #dc2626 !important;
  font-weight: 500;
}

// 暗黑模式适配
.dark {
  .setting-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .item-divider {
    border-color: var(--dt-border-dark);
  }

  .setting-item.clickable:hover {
    background: var(--dt-bg-hover-dark);
  }

  .item-value.code-text {
    background: var(--dt-bg-hover-dark);
  }

  .avatar-wrapper .user-avatar {
    border-color: var(--dt-border-dark);
  }
}
</style>
