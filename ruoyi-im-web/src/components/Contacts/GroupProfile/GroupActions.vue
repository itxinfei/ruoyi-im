<!--
  群组操作按钮组件

  显示发消息、邀请、管理等快捷操作按钮
-->
<template>
  <div class="quick-actions">
    <el-button type="primary" size="large" @click="$emit('send-message')">
      <el-icon><ChatDotRound /></el-icon>发消息
    </el-button>
    <div class="secondary-actions">
      <el-button size="large" @click="$emit('invite')">
        <el-icon><Plus /></el-icon>邀请
      </el-button>
      <el-button v-if="isOwnerOrAdmin" size="large" @click="$emit('manage')">
        <el-icon><Setting /></el-icon>管理
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ChatDotRound, Plus, Setting } from '@element-plus/icons-vue'

const props = defineProps({
  isOwnerOrAdmin: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send-message', 'invite', 'manage'])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  margin-top: 24px;

  .el-button {
    width: 100%;
  }
}

.secondary-actions {
  display: flex;
  gap: 8px;

  .el-button {
    flex: 1;
  }
}
</style>
