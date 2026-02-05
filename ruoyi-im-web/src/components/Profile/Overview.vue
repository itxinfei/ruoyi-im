<template>
  <div class="profile-overview">
    <!-- 头像卡片 -->
    <div class="avatar-card">
      <div class="avatar-wrapper">
        <DingtalkAvatar
          :src="user.avatar"
          :name="user.nickname || user.username"
          :size="120"
          shape="circle"
          class="user-avatar"
        />
        <div v-if="isOnline" class="status-badge online">
          <span class="status-dot" />
          在线
        </div>
        <div v-else class="status-badge offline">
          <span class="status-dot" />
          离线
        </div>
      </div>

      <div class="user-info">
        <h3 class="user-name">
          {{ user.nickname || user.username }}
          <span v-if="user.gender === 1" class="gender-icon male">
            <span class="material-icons-outlined">male</span>
          </span>
          <span v-else-if="user.gender === 2" class="gender-icon female">
            <span class="material-icons-outlined">female</span>
          </span>
        </h3>
        <p v-if="user.signature" class="user-signature">{{ user.signature }}</p>
        <p v-else class="user-signature empty">这个人很懒，什么都没留下</p>
      </div>
    </div>

    <!-- 基本信息卡片 -->
    <div class="info-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-primary">
          <span class="material-icons-outlined">badge</span>
        </div>
        <div class="section-title-group">
          <h4 class="section-title">基本信息</h4>
        </div>
      </div>

      <div class="info-card">
        <div class="info-row">
          <span class="row-label">用户编号</span>
          <span class="row-value code-text">{{ user.id }}</span>
        </div>
        <div class="info-row">
          <span class="row-label">所属部门</span>
          <span class="row-value">{{ user.dept?.deptName || user.department || '未分配' }}</span>
        </div>
        <div class="info-row">
          <span class="row-label">职位</span>
          <span class="row-value">{{ user.position || '暂无职位' }}</span>
        </div>
      </div>
    </div>

    <!-- 联系方式卡片 -->
    <div class="info-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-blue">
          <span class="material-icons-outlined">contact_phone</span>
        </div>
        <div class="section-title-group">
          <h4 class="section-title">联系方式</h4>
        </div>
      </div>

      <div class="info-card">
        <div class="info-row clickable" @click="copyText(user.mobile)">
          <span class="row-label">
            <span class="material-icons-outlined item-icon">phone</span>
            手机号码
          </span>
          <span class="row-value">{{ user.mobile || '未绑定' }}</span>
          <span v-if="user.mobile" class="row-copy">
            <span class="material-icons-outlined">content_copy</span>
          </span>
        </div>
        <div class="info-row clickable" @click="copyText(user.email)">
          <span class="row-label">
            <span class="material-icons-outlined item-icon">email</span>
            邮箱地址
          </span>
          <span class="row-value truncate">{{ user.email || '未绑定' }}</span>
          <span v-if="user.email" class="row-copy">
            <span class="material-icons-outlined">content_copy</span>
          </span>
        </div>
        <div class="info-row">
          <span class="row-label">
            <span class="material-icons-outlined item-icon">cake</span>
            生日
          </span>
          <span class="row-value">{{ user.birthday || '未设置' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  user: {
    type: Object,
    default: () => ({})
  }
})

const isOnline = computed(() => props.user?.online ?? false)

const copyText = (text) => {
  if (!text) return
  navigator.clipboard.writeText(String(text)).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.profile-overview {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

// 头像卡片
.avatar-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: 32px;
  text-align: center;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.user-avatar {
  border: 4px solid #fff;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.2);
}

.status-badge {
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: #fff;
  white-space: nowrap;

  &.online {
    background: linear-gradient(135deg, #00b09b, #96c93d);
    box-shadow: 0 2px 8px rgba(0, 176, 155, 0.3);
  }

  &.offline {
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(10px);
  }

  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: currentColor;
  }

  &.online .status-dot {
    animation: pulse-online 2s ease-in-out infinite;
  }
}

@keyframes pulse-online {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.user-info {
  .user-name {
    font-size: 24px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .gender-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border-radius: 50%;

    &.male {
      background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
      color: #4a90a2;
    }

    &.female {
      background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
      color: #d6336c;
    }

    .material-icons-outlined {
      font-size: 16px;
    }
  }

  .user-signature {
    font-size: 14px;
    color: var(--dt-text-secondary);
    margin: 0;

    &.empty {
      color: var(--dt-text-tertiary);
      font-style: italic;
    }
  }
}

// 信息分区
.info-section {
  margin-bottom: 0;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.section-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;

  span {
    font-size: 20px;
    color: white;
  }

  &.bg-gradient-primary {
    background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-hover) 100%);
  }

  &.bg-gradient-blue {
    background: linear-gradient(135deg, #448ef7 0%, #36cfc9 100%);
  }
}

.section-title-group {
  flex: 1;
}

.section-title {
  font-size: 16px;
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0;
}

// 信息卡片
.info-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid var(--dt-border-color-light);

  &:last-child {
    border-bottom: none;
  }

  &.clickable {
    cursor: pointer;
    transition: background 0.2s;
    border-radius: 8px;
    margin-left: 8px;
    margin-right: 8px;
    padding-left: 12px;
    padding-right: 12px;

    &:hover {
      background: var(--dt-bg-hover);
    }
  }

  .row-label {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 100px;
    font-size: 14px;
    color: var(--dt-text-secondary);

    .item-icon {
      font-size: 18px;
      opacity: 0.7;
    }
  }

  .row-value {
    flex: 1;
    font-size: 14px;
    color: var(--dt-text-primary);

    &.code-text {
      font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
      color: var(--dt-brand-color);
    }

    &.truncate {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .row-copy {
    font-size: 18px;
    color: var(--dt-text-tertiary);
    opacity: 0;
    transition: all 0.2s;

    .info-row.clickable:hover & {
      opacity: 1;
      color: var(--dt-brand-color);
    }
  }
}
</style>
