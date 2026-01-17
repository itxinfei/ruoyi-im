<template>
  <div class="workbench-workspace">
    <!-- 工作台头部 -->
    <div class="workbench-header">
      <div class="header-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="header-tab"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <el-icon>
            <component :is="tab.icon" />
          </el-icon>
          <span>{{ tab.label }}</span>
        </div>
      </div>
    </div>

    <!-- 动态内容区 -->
    <div class="workbench-content">
      <!-- 考勤面板 -->
      <AttendancePanel v-if="activeTab === 'attendance'" />

      <!-- 日程面板 -->
      <SchedulePanel v-else-if="activeTab === 'schedule'" />

      <!-- 报表面板 -->
      <ReportPanel v-else-if="activeTab === 'report'" />

      <!-- 审批面板 -->
      <ApprovalPanel v-else-if="activeTab === 'approval'" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Calendar, Document, Tickets, Odometer } from '@element-plus/icons-vue'
import AttendancePanel from '../workbench/Attendance.vue'
import SchedulePanel from '../workbench/Schedule.vue'
import ReportPanel from '../workbench/Report.vue'
import ApprovalPanel from '../workbench/Approval.vue'

const activeTab = ref('attendance')

const tabs = [
  { key: 'attendance', label: '考勤', icon: Odometer },
  { key: 'schedule', label: '日程', icon: Calendar },
  { key: 'report', label: '日报', icon: Document },
  { key: 'approval', label: '审批', icon: Tickets },
]
</script>

<style scoped lang="scss">
.workbench-workspace {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #fff;

  .workbench-header {
    border-bottom: 1px solid #e8e8e8;
    padding: 0 20px;

    .header-tabs {
      display: flex;
      gap: 8px;

      .header-tab {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 12px 16px;
        cursor: pointer;
        font-size: 14px;
        color: #595959;
        border-bottom: 2px solid transparent;
        transition: all 0.2s ease;

        &:hover {
          color: #1677ff;
        }

        &.active {
          color: #1677ff;
          border-bottom-color: #1677ff;
          font-weight: 500;
        }
      }
    }
  }

  .workbench-content {
    flex: 1;
    overflow: hidden;
  }
}
</style>
