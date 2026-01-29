<template>
  <div class="data-backup">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h2>数据备份</h2>
        <p class="page-desc">管理系统数据备份和恢复</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" :icon="Plus" @click="backupDialogVisible = true">
          立即备份
        </el-button>
      </div>
    </div>

    <!-- 备份统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon stat-icon--blue">
              <el-icon><FolderOpened /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ backupStats.totalBackups || 0 }}</div>
              <div class="stat-label">备份总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon stat-icon--green">
              <el-icon><Coin /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatFileSize(backupStats.totalSize) }}</div>
              <div class="stat-label">占用空间</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon stat-icon--orange">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ backupStats.lastBackup || '-' }}</div>
              <div class="stat-label">上次备份</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon stat-icon--purple">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ backupStats.autoBackupCount || 0 }}</div>
              <div class="stat-label">自动备份</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 定时备份配置 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>定时备份配置</span>
          <el-switch
            v-model="autoBackupEnabled"
            active-text="已启用"
            inactive-text="已停用"
            @change="handleToggleAutoBackup"
          />
        </div>
      </template>
      <el-form :model="backupConfig" label-width="120px" inline>
        <el-form-item label="备份周期">
          <el-select
            v-model="backupConfig.cycle"
            :disabled="!autoBackupEnabled"
            style="width: 120px"
          >
            <el-option label="每天" value="daily" />
            <el-option label="每周" value="weekly" />
            <el-option label="每月" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item label="备份时间">
          <el-time-picker
            v-model="backupConfig.time"
            :disabled="!autoBackupEnabled"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择时间"
          />
        </el-form-item>
        <el-form-item label="保留份数">
          <el-input-number
            v-model="backupConfig.retainCount"
            :disabled="!autoBackupEnabled"
            :min="1"
            :max="30"
            controls-position="right"
          />
          <span class="form-unit">份</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!autoBackupEnabled" @click="handleSaveConfig">
            保存配置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 备份列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <span>备份列表</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索备份名称"
              :prefix-icon="Search"
              clearable
              style="width: 200px"
            />
          </div>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="filteredBackupList"
        stripe
      >
        <el-table-column prop="name" label="备份名称" min-width="200">
          <template #default="{ row }">
            <div class="backup-name">
              <el-icon v-if="row.type === 'auto'" class="auto-icon"><MagicStick /></el-icon>
              {{ row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'auto' ? 'info' : 'primary'" size="small">
              {{ row.type === 'auto' ? '自动' : '手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="文件大小" width="100" align="right">
          <template #default="{ row }">
            {{ formatFileSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileCount" label="文件数" width="80" align="center" />
        <el-table-column prop="createTime" label="备份时间" width="160" />
        <el-table-column prop="creator" label="创建者" width="100" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'success'" type="success" size="small">完成</el-tag>
            <el-tag v-else-if="row.status === 'processing'" type="warning" size="small">备份中</el-tag>
            <el-tag v-else type="danger" size="small">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :disabled="row.status === 'processing'"
              @click="handleRestore(row)"
            >
              恢复
            </el-button>
            <el-button
              type="primary"
              link
              :disabled="row.status === 'processing'"
              @click="handleDownload(row)"
            >
              下载
            </el-button>
            <el-button
              type="danger"
              link
              :disabled="row.status === 'processing'"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="backupList.length"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 新建备份对话框 -->
    <el-dialog
      v-model="backupDialogVisible"
      title="新建数据备份"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="backupForm" :rules="backupRules" ref="backupFormRef" label-width="100px">
        <el-form-item label="备份名称" prop="name">
          <el-input
            v-model="backupForm.name"
            placeholder="请输入备份名称（留空自动生成）"
          />
        </el-form-item>
        <el-form-item label="备份描述" prop="description">
          <el-input
            v-model="backupForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备份描述"
          />
        </el-form-item>
        <el-form-item label="备份内容">
          <el-checkbox-group v-model="backupForm.content">
            <el-checkbox label="users">用户数据</el-checkbox>
            <el-checkbox label="groups">群组数据</el-checkbox>
            <el-checkbox label="messages">消息记录</el-checkbox>
            <el-checkbox label="departments">部门数据</el-checkbox>
            <el-checkbox label="roles">角色权限</el-checkbox>
            <el-checkbox label="system">系统配置</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 16px"
        >
          备份过程可能需要较长时间，请耐心等待。备份期间请勿关闭页面。
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="backupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="backupLoading" @click="handleCreateBackup">
          开始备份
        </el-button>
      </template>
    </el-dialog>

    <!-- 恢复确认对话框 -->
    <el-dialog
      v-model="restoreDialogVisible"
      title="恢复数据备份"
      width="450px"
      :close-on-click-modal="false"
    >
      <div class="restore-confirm">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <div class="confirm-content">
          <h4>确定要恢复此备份吗？</h4>
          <p>恢复操作将覆盖当前数据，请谨慎操作。建议在恢复前先创建当前数据的备份。</p>
          <div v-if="selectedBackup" class="backup-info">
            <div class="info-row">
              <span class="info-label">备份名称：</span>
              <span>{{ selectedBackup.name }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">备份时间：</span>
              <span>{{ selectedBackup.createTime }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">数据大小：</span>
              <span>{{ formatFileSize(selectedBackup.size) }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="restoreDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="restoreLoading" @click="handleConfirmRestore">
          确定恢复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Download,
  Delete,
  FolderOpened,
  Coin,
  Clock,
  Timer,
  MagicStick,
  WarningFilled
} from '@element-plus/icons-vue'

const loading = ref(false)
const backupList = ref([])
const searchKeyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)

// 备份统计
const backupStats = ref({
  totalBackups: 0,
  totalSize: 0,
  lastBackup: '-',
  autoBackupCount: 0
})

// 定时备份配置
const autoBackupEnabled = ref(true)
const backupConfig = ref({
  cycle: 'daily',
  time: '02:00',
  retainCount: 7
})

// 对话框状态
const backupDialogVisible = ref(false)
const restoreDialogVisible = ref(false)
const backupLoading = ref(false)
const restoreLoading = ref(false)
const backupFormRef = ref(null)
const selectedBackup = ref(null)

// 备份表单
const backupForm = ref({
  name: '',
  description: '',
  content: ['users', 'groups', 'messages', 'departments', 'roles', 'system']
})

const backupRules = {
  content: [
    { type: 'array', required: true, message: '请选择备份内容', trigger: 'change' }
  ]
}

// 过滤后的备份列表
const filteredBackupList = computed(() => {
  if (!searchKeyword.value) return backupList.value
  return backupList.value.filter(item =>
    item.name?.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 加载备份列表
const loadBackups = async () => {
  loading.value = true
  try {
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 300))
    backupList.value = [
      {
        id: 1,
        name: '完整备份_20250129',
        type: 'auto',
        description: '系统自动备份',
        size: 1024 * 1024 * 156,
        fileCount: 5234,
        createTime: '2026-01-29 02:00:00',
        creator: '系统',
        status: 'success'
      },
      {
        id: 2,
        name: '手动备份_用户数据',
        type: 'manual',
        description: '用户信息手动备份',
        size: 1024 * 1024 * 32,
        fileCount: 120,
        createTime: '2026-01-28 15:30:00',
        creator: 'admin',
        status: 'success'
      },
      {
        id: 3,
        name: '消息记录备份',
        type: 'manual',
        description: '消息记录导出备份',
        size: 1024 * 1024 * 28,
        fileCount: 3500,
        createTime: '2026-01-27 10:15:00',
        creator: 'admin',
        status: 'success'
      },
      {
        id: 4,
        name: '完整备份_20250126',
        type: 'auto',
        description: '系统自动备份',
        size: 1024 * 1024 * 155,
        fileCount: 5201,
        createTime: '2026-01-26 02:00:00',
        creator: '系统',
        status: 'success'
      },
      {
        id: 5,
        name: '失败备份示例',
        type: 'manual',
        description: '备份失败示例',
        size: 0,
        fileCount: 0,
        createTime: '2026-01-25 18:00:00',
        creator: 'admin',
        status: 'failed'
      }
    ]
    backupStats.value = {
      totalBackups: backupList.value.length,
      totalSize: backupList.value.reduce((sum, item) => sum + item.size, 0),
      lastBackup: '2026-01-29 02:00:00',
      autoBackupCount: backupList.value.filter(item => item.type === 'auto').length
    }
  } catch (error) {
    ElMessage.error('加载备份列表失败')
  } finally {
    loading.value = false
  }
}

// 切换自动备份
const handleToggleAutoBackup = (val) => {
  ElMessage.success(val ? '已启用定时备份' : '已停用定时备份')
}

// 保存配置
const handleSaveConfig = () => {
  ElMessage.success('备份配置已保存')
}

// 创建备份
const handleCreateBackup = async () => {
  await backupFormRef.value?.validate()
  backupLoading.value = true
  try {
    // 模拟备份过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('数据备份完成')
    backupDialogVisible.value = false
    loadBackups()
  } catch (error) {
    ElMessage.error('备份失败')
  } finally {
    backupLoading.value = false
  }
}

// 恢复备份
const handleRestore = (row) => {
  selectedBackup.value = row
  restoreDialogVisible.value = true
}

const handleConfirmRestore = async () => {
  restoreLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('数据恢复完成')
    restoreDialogVisible.value = false
    loadBackups()
  } catch (error) {
    ElMessage.error('恢复失败')
  } finally {
    restoreLoading.value = false
  }
}

// 下载备份
const handleDownload = (row) => {
  ElMessage.info(`开始下载: ${row.name}`)
}

// 删除备份
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除备份"${row.name}"吗？此操作不可恢复。`, '删除确认', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
    loadBackups()
  } catch {
    // 取消
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

onMounted(() => {
  loadBackups()
})
</script>

<style scoped>
/* 引入主题变量 */
@import '@/styles/admin-theme.css';

/* ================================
   页面容器
   ================================ */
.data-backup {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--dt-space-md);
}

.page-title h2 {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0;
}

.page-actions {
  display: flex;
  gap: var(--dt-space-sm);
}

/* ================================
   统计卡片
   ================================ */
.stats-row {
  margin-bottom: var(--dt-space-md);
}

.stat-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon--blue { background: linear-gradient(135deg, #0089FF 0%, #0066CC 100%); }
.stat-icon--green { background: linear-gradient(135deg, #00C853 0%, #009624 100%); }
.stat-icon--orange { background: linear-gradient(135deg, #FFAB00 0%, #FF8F00 100%); }
.stat-icon--purple { background: linear-gradient(135deg, #7C4DFF 0%, #5E32CC 100%); }

.stat-value {
  font-size: 24px;
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-text-primary);
}

.stat-label {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-secondary);
}

/* ================================
   配置卡片
   ================================ */
.config-card {
  margin-bottom: var(--dt-space-md);
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.config-card :deep(.el-card__header) {
  padding: var(--dt-space-sm) var(--dt-space-md);
  border-bottom: 1px solid var(--dt-divider);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.config-card :deep(.el-card__body) {
  padding: var(--dt-space-md);
}

.config-card :deep(.el-form-item) {
  margin-right: var(--dt-space-lg);
  margin-bottom: var(--dt-space-xs);
}

.form-unit {
  margin-left: var(--dt-space-xs);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

/* ================================
   表格卡片
   ================================ */
.table-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.table-card :deep(.el-card__header) {
  padding: var(--dt-space-sm) var(--dt-space-md);
  border-bottom: 1px solid var(--dt-divider);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: var(--dt-space-sm);
}

.backup-name {
  display: flex;
  align-items: center;
  gap: var(--dt-space-xs);
}

.auto-icon {
  color: var(--dt-warning);
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: var(--dt-space-md);
  border-top: 1px solid var(--dt-divider);
}

/* ================================
   恢复确认对话框
   ================================ */
.restore-confirm {
  display: flex;
  gap: var(--dt-space-md);
  padding: var(--dt-space-md);
}

.warning-icon {
  width: 48px;
  height: 48px;
  color: var(--dt-warning);
  flex-shrink: 0;
}

.confirm-content h4 {
  font-size: var(--dt-font-size-md);
  color: var(--dt-text-primary);
  margin: 0 0 var(--dt-space-sm) 0;
}

.confirm-content p {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
  margin: 0 0 var(--dt-space-md) 0;
}

.backup-info {
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
  padding: var(--dt-space-sm);
}

.info-row {
  display: flex;
  margin-bottom: var(--dt-space-xs);
  font-size: var(--dt-font-size-sm);
}

.info-label {
  color: var(--dt-text-secondary);
  width: 80px;
  flex-shrink: 0;
}
</style>
