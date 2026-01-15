<template>
  <div class="report-panel">
    <!-- 占位内容 -->
    <el-empty description="日报功能开发中...">
      <el-button type="primary" @click="handleMock">
        点击测试日报功能
      </el-button>
    </el-empty>

    <!-- 开发中提示 -->
    <el-alert
      title="开发提示"
      type="info"
      :closable="false"
      show-icon
    >
      日报模块正在开发中，包含以下功能：
      <ul>
        <li>日报撰写</li>
        <li>日报模板</li>
        <li>日报统计</li>
        <li>日报导出</li>
        <li>日报分享</li>
      </ul>
    </el-alert>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const handleMock = () => {
  ElMessage.success('日报功能已记录（模拟）')
}
</script>

<style scoped lang="scss">
.report-panel {
  padding: 20px;

  .el-empty {
    margin: 40px 0;
  }

  .el-alert {
    margin: 20px 0;
  }
}
</style>