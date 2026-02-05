<template>
  <div class="basic-info">
    <div class="info-card">
      <!-- 昵称 -->
      <div class="info-row">
        <span class="row-label">
          <span class="material-icons-outlined item-icon">person</span>
          昵称
        </span>
        <span class="row-value">{{ user.nickname || '-' }}</span>
      </div>

      <!-- 个性签名 -->
      <div class="info-row">
        <span class="row-label">
          <span class="material-icons-outlined item-icon">format_quote</span>
          个性签名
        </span>
        <span class="row-value signature">{{ user.signature || '暂无签名' }}</span>
      </div>

      <!-- 性别 -->
      <div class="info-row">
        <span class="row-label">
          <span class="material-icons-outlined item-icon">wc</span>
          性别
        </span>
        <span class="row-value">{{ genderText }}</span>
      </div>

      <!-- 生日 -->
      <div class="info-row">
        <span class="row-label">
          <span class="material-icons-outlined item-icon">cake</span>
          生日
        </span>
        <span class="row-value">{{ user.birthday || '未设置' }}</span>
      </div>

      <!-- 邮箱 -->
      <div class="info-row">
        <span class="row-label">
          <span class="material-icons-outlined item-icon">email</span>
          邮箱
        </span>
        <span class="row-value">{{ user.email || '未绑定' }}</span>
      </div>
    </div>

    <div class="empty-state">
      <p class="hint">点击右上角"编辑"按钮修改您的基本信息</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  user: {
    type: Object,
    default: () => ({})
  }
})

const genderText = computed(() => {
  const gender = props.user?.gender
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '保密'
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.basic-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-color-light);

  &:last-child {
    border-bottom: none;
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

    &.signature {
      color: var(--dt-text-secondary);
      font-style: italic;
    }
  }
}

.empty-state {
  text-align: center;
  padding: 20px;

  .hint {
    font-size: 13px;
    color: var(--dt-text-tertiary);
    margin: 0;
  }
}
</style>
