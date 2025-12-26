<template>
  <div class="call-container">
    <el-tabs v-model="activeTab" class="call-tabs">
      <el-tab-pane label="通话记录" name="history">
        <call-history />
      </el-tab-pane>
      <el-tab-pane label="通话统计" name="stats">
        <call-stats />
      </el-tab-pane>
      <el-tab-pane label="通话设置" name="settings">
        <call-settings />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import CallHistory from './CallHistory.vue'
import CallStats from './CallStats.vue'
import CallSettings from './CallSettings.vue'

export default {
  name: 'CallIndex',
  components: {
    CallHistory,
    CallStats,
    CallSettings,
  },
  data() {
    return {
      activeTab: 'history',
    }
  },
}
</script>

<style lang="scss" scoped>
.call-container {
  height: 100%;
  padding: 20px;
  background: #f5f7fa;
}

.call-tabs {
  height: 100%;

  :deep(.el-tabs__content) {
    height: calc(100% - 40px);
    overflow: hidden;
  }

  :deep(.el-tab-pane) {
    height: 100%;
    overflow: auto;
  }
}
</style>
