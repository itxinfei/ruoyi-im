<template>
  <div class="account-settings">
    <!-- 个人信息卡片 -->
    <section class="setting-section">
      <h3 class="section-title">个人信息</h3>
      <div class="profile-card">
        <div class="profile-main">
          <div class="avatar-wrapper">
            <el-avatar :size="72" :src="user.avatar" class="profile-avatar" />
            <div class="avatar-overlay" @click="$emit('edit-profile')">
              <el-icon><Camera /></el-icon>
            </div>
          </div>
          <div class="profile-info">
            <h2 class="profile-name">{{ user.nickname || user.username }}</h2>
            <p class="profile-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ user.username }}
              </span>
              <span class="meta-divider">·</span>
              <span class="meta-item">UID: {{ user.id }}</span>
            </p>
          </div>
          <el-button type="primary" @click="$emit('edit-profile')">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </div>

        <div class="profile-details">
          <div class="detail-row">
            <div class="detail-item">
              <span class="detail-label">
                <el-icon><Message /></el-icon>
                邮箱
              </span>
              <span class="detail-value">{{ user.email || '未绑定' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">
                <el-icon><Phone /></el-icon>
                手机
              </span>
              <span class="detail-value">{{ user.phonenumber || '未绑定' }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="detail-label">
                <el-icon><OfficeBuilding /></el-icon>
                部门
              </span>
              <span class="detail-value">{{ user.dept?.deptName || '无' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">
                <el-icon><Postcard /></el-icon>
                职位
              </span>
              <span class="detail-value">{{ user.position || '未设置' }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 账号安全 -->
    <section class="setting-section">
      <h3 class="section-title">账号安全</h3>
      <div class="setting-list">
        <div class="setting-item clickable" @click="$emit('change-password')">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-blue">
              <el-icon><Lock /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">登录密码</div>
            <div class="item-desc">定期修改密码以保护账号安全</div>
          </div>
          <div class="item-action">
            <span class="action-text">修改</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-green">
              <el-icon><Shield /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">双重验证</div>
            <div class="item-desc">开启两步验证，提升账号安全性</div>
          </div>
          <el-switch v-model="twoFactorEnabled" disabled />
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-orange">
              <el-icon><Monitor /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">登录设备管理</div>
            <div class="item-desc">查看和管理已登录的设备</div>
          </div>
          <div class="item-action">
            <span class="action-text">管理</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- 账号绑定 -->
    <section class="setting-section">
      <h3 class="section-title">第三方账号</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-wechat">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">微信</div>
            <div class="item-desc">未绑定</div>
          </div>
          <el-button type="primary" link>绑定</el-button>
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-dingtalk">
              <el-icon><Connection /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">钉钉</div>
            <div class="item-desc">未绑定</div>
          </div>
          <el-button type="primary" link>绑定</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  ArrowRight,
  Camera,
  User,
  Edit,
  Message,
  Phone,
  OfficeBuilding,
  Postcard,
  Lock,
  Shield,
  Monitor,
  ChatDotRound,
  Connection
} from '@element-plus/icons-vue'

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
  max-width: 680px;
}

// 分区标题
.setting-section {
  margin-bottom: 32px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 16px;
  padding-left: 4px;
}

// 个人信息卡片
.profile-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.profile-main {
  display: flex;
  align-items: center;
  padding: 24px;
  gap: 20px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;

  &:hover .avatar-overlay {
    opacity: 1;
  }
}

.profile-avatar {
  border: 3px solid var(--dt-bg-body);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .dark & {
    border-color: var(--dt-bg-body-dark);
  }
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;

  .el-icon {
    color: #fff;
    font-size: 20px;
  }
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-name {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.profile-meta {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--dt-text-secondary);

  .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;

    .el-icon {
      font-size: 14px;
    }
  }

  .meta-divider {
    color: var(--dt-text-tertiary);
  }
}

// 详细信息
.profile-details {
  padding: 20px 24px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);

  .dark & {
    border-top-color: var(--dt-border-dark);
    background: rgba(255, 255, 255, 0.02);
  }
}

.detail-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  &:not(:last-child) {
    margin-bottom: 16px;
  }
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--dt-text-tertiary);

  .el-icon {
    font-size: 14px;
  }
}

.detail-value {
  font-size: 14px;
  color: var(--dt-text-primary);
  font-weight: 500;
}

// 设置列表
.setting-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.setting-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  gap: 16px;
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

// 图标样式
.item-icon-wrapper {
  flex-shrink: 0;
}

.item-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;

  &.bg-blue {
    background: rgba(22, 119, 255, 0.1);
    color: #1677ff;
  }

  &.bg-green {
    background: rgba(82, 196, 26, 0.1);
    color: #52c41a;
  }

  &.bg-orange {
    background: rgba(250, 140, 22, 0.1);
    color: #fa8c16;
  }

  &.bg-wechat {
    background: rgba(7, 193, 96, 0.1);
    color: #07c160;
  }

  &.bg-dingtalk {
    background: rgba(64, 158, 255, 0.1);
    color: #409eff;
  }
}

// 内容区域
.item-content {
  flex: 1;
  min-width: 0;
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

// 操作区域
.item-action {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--dt-text-tertiary);
  font-size: 14px;

  .action-text {
    font-size: 14px;
  }

  .el-icon {
    font-size: 16px;
  }
}

// 响应式适配
@media (max-width: 640px) {
  .profile-main {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .profile-meta {
    justify-content: center;
  }

  .detail-row {
    grid-template-columns: 1fr;
  }

  .setting-item {
    padding: 14px 16px;
  }
}
</style>
