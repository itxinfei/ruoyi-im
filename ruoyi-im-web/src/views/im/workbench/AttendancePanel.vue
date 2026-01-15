<template>
  <div class="attendance-panel">
    <!-- 占位内容 -->
    <el-empty description="考勤功能开发中...">
      <el-button type="primary" @click="handleMock">
        点击测试考勤功能
      </el-button>
    </el-empty>

    <!-- 开发中提示 -->
    <el-alert
      title="开发提示"
      type="info"
      :closable="false"
      show-icon
    >
      考勤模块正在开发中，包含以下功能：
      <ul>
        <li>上下班打卡</li>
        <li>位置记录</li>
        <li>工时统计</li>
        <li>异常考勤</li>
        <li>考勤报表导出</li>
      </ul>
    </el-alert>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const handleMock = () => {
  ElMessage.success('考勤打卡功能已记录（模拟）')
}
</script>

<style scoped lang="scss">
.attendance-panel {
  padding: 20px;

  .el-empty {
    margin: 40px 0;
  }
}
</style>