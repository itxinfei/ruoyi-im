<template>
  <div class="approval-panel">
    <!-- 占位内容 -->
    <el-empty description="审批功能开发中...">
      <el-button type="primary" @click="handleMock">
        点击测试审批功能
      </el-button>
    </el-empty>

    <!-- 开发中提示 -->
    <el-alert
      title="开发提示"
      type="info"
      :closable="false"
      show-icon
    >
      审批模块正在开发中，包含以下功能：
      <ul>
        <li>我的申请</li>
        <li>待我审批</li>
        <li>申请记录</li>
        <li>审批统计</li>
        <li>审批流配置</li>
      </ul>
    </el-alert>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const handleMock = () => {
  ElMessage.success('审批功能已记录（模拟）')
}
</script>

<style scoped lang="scss">
.approval-panel {
  padding: 20px;

  .el-empty {
    margin: 40px 0;
  }

  .el-alert {
    margin: 20px 0;
  }
}
</style>