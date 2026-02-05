<template>
  <div class="contact-info">
    <!-- 手机号码 -->
    <div class="info-card">
      <div class="card-header">
        <div class="card-icon-wrapper bg-gradient-green">
          <span class="material-icons-outlined">phone</span>
        </div>
        <h4 class="card-title">手机号码</h4>
      </div>
      <div class="card-body">
        <div class="info-value-row">
          <span class="value">{{ user.mobile || '未绑定' }}</span>
          <el-button
            v-if="user.mobile"
            text
            @click="copyText(user.mobile)"
          >
            <span class="material-icons-outlined">content_copy</span>
            复制
          </el-button>
        </div>
      </div>
    </div>

    <!-- 邮箱地址 -->
    <div class="info-card">
      <div class="card-header">
        <div class="card-icon-wrapper bg-gradient-orange">
          <span class="material-icons-outlined">email</span>
        </div>
        <h4 class="card-title">邮箱地址</h4>
      </div>
      <div class="card-body">
        <div class="info-value-row">
          <span class="value truncate">{{ user.email || '未绑定' }}</span>
          <el-button
            v-if="user.email"
            text
            @click="copyText(user.email)"
          >
            <span class="material-icons-outlined">content_copy</span>
            复制
          </el-button>
        </div>
      </div>
    </div>

    <!-- 所属部门 -->
    <div class="info-card">
      <div class="card-header">
        <div class="card-icon-wrapper bg-gradient-blue">
          <span class="material-icons-outlined">business</span>
        </div>
        <h4 class="card-title">所属部门</h4>
      </div>
      <div class="card-body">
        <div class="info-value-row">
          <span class="value">{{ user.dept?.deptName || user.department || '未分配' }}</span>
        </div>
      </div>
    </div>

    <!-- 职位 -->
    <div class="info-card">
      <div class="card-header">
        <div class="card-icon-wrapper bg-gradient-purple">
          <span class="material-icons-outlined">work</span>
        </div>
        <h4 class="card-title">职位</h4>
      </div>
      <div class="card-body">
        <div class="info-value-row">
          <span class="value">{{ user.position || '暂无职位' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const props = defineProps({
  user: {
    type: Object,
    default: () => ({})
  }
})

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

.contact-info {
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

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-color-light);
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

  &.bg-gradient-green {
    background: linear-gradient(135deg, #00b09b, #96c93d);
  }

  &.bg-gradient-orange {
    background: linear-gradient(135deg, #fa8c16, #ffc53d);
  }

  &.bg-gradient-blue {
    background: linear-gradient(135deg, #448ef7, #36cfc9);
  }

  &.bg-gradient-purple {
    background: linear-gradient(135deg, #667eea, #764ba2);
  }
}

.card-title {
  flex: 1;
  font-size: 15px;
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0;
}

.card-body {
  padding: 12px 20px;
}

.info-value-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  .value {
    font-size: 14px;
    color: var(--dt-text-primary);

    &.truncate {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
</style>
