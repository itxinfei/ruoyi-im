<template>
  <div class="schedule-panel">
    <!-- 占位内容 -->
    <el-empty description="日程功能开发中...">
      <el-button type="primary" @click="handleMock">
        点击测试日程功能
      </el-button>
    </el-empty>

    <!-- 开发中提示 -->
    <el-alert
      title="开发提示"
      type="info"
      :closable="false"
      show-icon
    >
      日程模块正在开发中，包含以下功能：
      <ul>
        <li>日程创建</li>
        <li>日程查看（日/周/月视图）</li>
        <li>日程提醒</li>
        <li>日程分享</li>
        <li>日历同步</li>
      </ul>
    </el-alert>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const handleMock = () => {
  ElMessage.success('日程功能已记录（模拟）')
}
</script>

<style scoped lang="scss">
.schedule-panel {
  padding: 20px;

  .el-empty {
    margin: 40px 0;
  }
}
</style>