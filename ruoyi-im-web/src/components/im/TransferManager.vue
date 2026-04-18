<template>
  <transition name="slide-up">
    <div v-if="visible" class="transfer-manager">
      <!-- 头部：统计与状态 -->
      <div class="manager-header" @click="toggleExpand">
        <div class="header-left">
          <el-icon v-if="isAnyProcessing" class="is-loading"><Loading /></el-icon>
          <el-icon v-else><Check /></el-icon>
          <span class="status-text">{{ statusSummary }}</span>
        </div>
        <div class="header-right">
          <el-icon :class="{ 'is-expanded': expanded }"><ArrowUp /></el-icon>
          <el-icon class="close-icon" @click.stop="close"><Close /></el-icon>
        </div>
      </div>

      <!-- 内容区：传输列表 -->
      <div v-if="expanded" class="manager-body">
        <div v-if="tasks.length === 0" class="empty-list">暂无传输任务</div>
        <div v-for="task in tasks" :key="task.id" class="transfer-row">
          <div class="file-thumb" :class="getIconClass(task.fileType)">
            <el-icon><Document /></el-icon>
          </div>
          <div class="transfer-info">
            <div class="info-top">
              <span class="file-name">{{ task.fileName }}</span>
              <span class="file-status">{{ task.statusText }}</span>
            </div>
            <div class="info-bottom">
              <el-progress 
                :percentage="task.progress" 
                :status="getProgressStatus(task.status)" 
                :stroke-width="4" 
                :show-text="false" 
              />
              <div class="detail-row">
                <span>{{ task.formattedSize }}</span>
                <span v-if="task.status === 'processing'">{{ task.speed }}/s</span>
              </div>
            </div>
          </div>
          <div class="transfer-ops">
            <el-icon v-if="task.status === 'processing'" @click="pauseTask(task)"><VideoPause /></el-icon>
            <el-icon v-else-if="task.status === 'paused'" @click="resumeTask(task)"><VideoPlay /></el-icon>
            <el-icon v-if="task.status === 'failed'" @click="retryTask(task)"><Refresh /></el-icon>
            <el-icon class="del-icon" @click="removeTask(task)"><Delete /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Loading, Check, ArrowUp, Close, Document, VideoPause, VideoPlay, Refresh, Delete } from '@element-plus/icons-vue'

const visible = ref(false)
const expanded = ref(false)
const tasks = ref([]) // {id, fileName, fileType, progress, status, size, speed}

const isAnyProcessing = computed(() => tasks.value.some(t => t.status === 'processing'))

const statusSummary = computed(() => {
  const processing = tasks.value.filter(t => t.status === 'processing').length
  if (processing > 0) return `正在传输 ${processing} 个文件`
  return `传输已完成 (${tasks.value.length})`
})

const toggleExpand = () => expanded.value = !expanded.value
const close = () => { visible.value = false; expanded.value = false }

const getIconClass = (type) => {
  const map = { 'PDF': 'icon-pdf', 'EXCEL': 'icon-sheet', 'IMAGE': 'icon-image' }
  return map[type] || 'icon-doc'
}

const getProgressStatus = (s) => {
  if (s === 'failed') return 'exception'
  if (s === 'completed') return 'success'
  return ''
}

// 模拟添加任务（供父组件调用）
const addTask = (file) => {
  visible.value = true
  expanded.value = true
  tasks.value.unshift({
    id: Date.now(),
    fileName: file.name,
    fileType: file.type?.toUpperCase() || 'FILE',
    progress: 0,
    status: 'processing',
    statusText: '等待传输',
    formattedSize: '0B',
    speed: '0KB'
  })
}

const pauseTask = (t) => t.status = 'paused'
const resumeTask = (t) => t.status = 'processing'
const retryTask = (t) => { t.status = 'processing'; t.progress = 0 }
const removeTask = (t) => tasks.value = tasks.value.filter(x => x.id !== t.id)

defineExpose({ addTask, tasks })
</script>

<style scoped lang="scss">
.transfer-manager {
  position: fixed; right: 24px; bottom: 24px; width: 360px;
  background: #ffffff; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  border: 1px solid var(--dt-border-light); z-index: 2000; overflow: hidden;
}

.manager-header {
  height: 48px; padding: 0 16px; display: flex; align-items: center; justify-content: space-between;
  background: var(--dt-brand-bg); cursor: pointer;
  .header-left { display: flex; align-items: center; gap: 8px; font-size: 14px; color: var(--dt-brand-color); font-weight: 600; }
  .header-right { display: flex; align-items: center; gap: 12px; color: var(--dt-text-tertiary); 
    .is-expanded { transform: rotate(180deg); }
    .close-icon:hover { color: var(--dt-error-color); }
  }
}

.manager-body {
  max-height: 400px; overflow-y: auto; padding: 12px 0;
  &::-webkit-scrollbar { width: 4px; }
}

.transfer-row {
  display: flex; align-items: center; padding: 10px 16px; gap: 12px;
  &:hover { background: var(--dt-bg-hover); .transfer-ops { opacity: 1; } }
}

.file-thumb {
  width: 36px; height: 36px; border-radius: 6px; @include flex-center; font-size: 18px;
  &.icon-doc { background: #eef5fe; color: #2196f3; }
  &.icon-pdf { background: #fef0f0; color: #f56c6c; }
  &.icon-sheet { background: #f0f9eb; color: #67c23a; }
}

.transfer-info {
  flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px;
  .info-top { display: flex; justify-content: space-between; align-items: center;
    .file-name { font-size: 13px; font-weight: 500; @include text-ellipsis; }
    .file-status { font-size: 11px; color: var(--dt-text-tertiary); }
  }
  .detail-row { display: flex; justify-content: space-between; font-size: 11px; color: var(--dt-text-quaternary); margin-top: 2px; }
}

.transfer-ops {
  display: flex; gap: 8px; color: var(--dt-text-tertiary); opacity: 0; transition: 0.2s;
  .el-icon { cursor: pointer; &:hover { color: var(--dt-brand-color); } }
  .del-icon:hover { color: var(--dt-error-color); }
}

.empty-list { text-align: center; padding: 40px 0; color: var(--dt-text-quaternary); font-size: 13px; }

.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(100px); opacity: 0; }
</style>
