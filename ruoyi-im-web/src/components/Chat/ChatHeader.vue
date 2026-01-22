<template>
  <div class="chat-header">
    <el-avatar :size="40" :src="session?.avatar" class="header-avatar">
      {{ session?.name?.charAt(0) }}
    </el-avatar>
    <div class="header-info">
      <div class="header-name">{{ session?.name }}</div>
      <div class="header-status">
        <span class="status-dot online"></span> 在线
      </div>
    </div>
    <div class="header-actions">
      <el-tooltip content="语音通话">
        <el-button :icon="Phone" text circle size="small" />
      </el-tooltip>
      <el-tooltip content="视频通话">
        <el-button :icon="VideoCamera" text circle size="small" />
      </el-tooltip>
      <el-tooltip content="更多">
        <el-button :icon="MoreFilled" text circle size="small" />
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import { Phone, VideoCamera, MoreFilled } from '@element-plus/icons-vue'

defineProps({
  session: Object
})
</script>

<style scoped>
.chat-header {
  height: 56px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: #fff;
}

.header-avatar {
  background: #409eff;
  color: #fff;
}

.header-info {
  margin-left: 12px;
  flex: 1;
}

.header-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.header-status {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  margin-top: 2px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}

.status-dot.online {
  background-color: #52c41a;
}
</style>
