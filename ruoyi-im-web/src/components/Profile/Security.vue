<template>
  <div class="security">
    <!-- 登录密码 -->
    <div class="info-card clickable" @click="handleOpenPassword">
      <div class="card-header">
        <div class="card-icon-wrapper bg-gradient-orange">
          <span class="material-icons-outlined">lock</span>
        </div>
        <div class="card-content">
          <h4 class="card-title">登录密码</h4>
          <p class="card-desc">定期修改密码可以保护账号安全</p>
        </div>
        <div class="card-action">
          <span class="status-badge success">
            <span class="material-icons-outlined" style="font-size: 14px;">check_circle</span>
            已设置
          </span>
          <span class="material-icons-outlined arrow-icon">chevron_right</span>
        </div>
      </div>
    </div>

    <!-- 登录设备 -->
    <div class="info-card">
      <div class="card-header">
        <div class="card-icon-wrapper bg-gradient-blue">
          <span class="material-icons-outlined">devices</span>
        </div>
        <div class="card-content">
          <h4 class="card-title">登录设备</h4>
          <p class="card-desc">查看当前登录的设备</p>
        </div>
      </div>
      <div class="card-body">
        <div class="device-list">
          <div class="device-item current">
            <span class="material-icons-outlined device-icon">computer</span>
            <div class="device-info">
              <span class="device-name">当前设备</span>
              <span class="device-time">刚刚活跃</span>
            </div>
            <span class="device-badge">当前</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  user: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['open-password'])

const handleOpenPassword = () => {
  emit('open-password')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.security {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  overflow: hidden;

  &.clickable {
    cursor: pointer;

    &:hover {
      border-color: var(--dt-brand-color);
    }
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
}

.card-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;

  span {
    font-size: 18px;
    color: white;
  }

  &.bg-gradient-orange {
    background: linear-gradient(135deg, #fa8c16, #ffc53d);
  }

  &.bg-gradient-blue {
    background: linear-gradient(135deg, #448ef7, #36cfc9);
  }
}

.card-content {
  flex: 1;

  .card-title {
    font-size: 15px;
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
    margin: 0;
  }

  .card-desc {
    font-size: 13px;
    color: var(--dt-text-secondary);
    margin: 4px 0 0;
  }
}

.card-action {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 100px;
  font-size: 13px;
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

.card-body {
  padding: 12px 20px 16px;
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-hover);

  .device-icon {
    font-size: 20px;
    color: var(--dt-text-secondary);
  }

  .device-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .device-name {
      font-size: 14px;
      color: var(--dt-text-primary);
    }

    .device-time {
      font-size: 12px;
      color: var(--dt-text-secondary);
    }
  }

  .device-badge {
    padding: 4px 10px;
    border-radius: 100px;
    font-size: 12px;
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }
}
</style>
