<template>
  <div class="admin-page group-management">
    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <div>
            <h3>群组管理</h3>
            <p>统一处理群组治理、成员维护与批量操作</p>
          </div>
          <el-space>
            <el-button @click="handleReset">
              重置
            </el-button>
            <el-button type="primary" @click="loadGroups">
              刷新
            </el-button>
          </el-space>
        </div>
      </template>

      <el-row :gutter="12" class="toolbar-row">
        <el-col
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-input
            v-model="searchKeyword"
            placeholder="搜索群名称"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>
      </el-row>

      <!-- 群组统计卡片 -->
      <el-row :gutter="12" class="stats-row">
        <el-col :xs="24" :sm="8">
          <div class="stat-card">
            <div class="stat-icon groups">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalGroups || 0 }}</div>
              <div class="stat-label">群组总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card">
            <div class="stat-icon active">
              <el-icon><ChatLineSquare /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.activeGroups || 0 }}</div>
              <div class="stat-label">活跃群组(近7天)</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card">
            <div class="stat-icon new">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.newGroups || 0 }}</div>
              <div class="stat-label">新增群组(近7天)</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <div v-if="selectedGroups.length" class="batch-actions">
        <span>已选择 {{ selectedGroups.length }} 个群组</span>
        <el-space>
          <el-button size="small" type="danger" @click="handleBatchDelete">
            批量解散
          </el-button>
          <el-button v-if="failedItems.length" size="small" @click="failedDialogVisible = true">
            查看失败项
          </el-button>
        </el-space>
      </div>

      <el-table
        v-loading="loading"
        :data="groupList"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="群组ID" width="90" sortable />
        <el-table-column label="群头像" width="86">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="42">
              {{ row.name?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="群名称" min-width="180" sortable />
        <el-table-column prop="ownerName" label="群主" min-width="110" sortable />
        <el-table-column prop="memberCount" label="成员数" width="100" sortable>
          <template #default="{ row }">
            <el-tag type="info">
              {{ row.memberCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxMembers" label="成员上限" width="100" sortable />
        <el-table-column
          prop="description"
          label="群描述"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column prop="createTime" label="创建时间" width="180" sortable />
        <el-table-column label="操作" fixed="right" width="230">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewMembers(row)">
              成员
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              解散
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="memberDialogVisible" title="群组成员" width="640px">
      <el-table
        v-loading="memberLoading"
        :data="memberList"
        border
        max-height="420"
      >
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column label="头像" width="70">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="38">
              {{ row.nickname?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="130" />
        <el-table-column prop="roleDisplay" label="角色" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'OWNER'" type="danger">
              群主
            </el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning">
              管理员
            </el-tag>
            <el-tag v-else type="info">
              成员
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'OWNER'"
              size="small"
              type="danger"
              link
              @click="handleRemoveMember(row)"
            >
              移除
            </el-button>
            <span v-else class="muted">群主</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑群组" width="520px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="群名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="群描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="成员上限">
          <el-input-number v-model="editForm.maxMembers" :min="1" :max="2000" />
        </el-form-item>
        <el-form-item label="全员禁言">
          <el-switch v-model="editForm.allMuted" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="handleSaveEdit">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="failedDialogVisible" title="批量失败明细" width="520px">
      <el-table :data="failedItems" border>
        <el-table-column prop="id" label="群组ID" width="120" />
        <el-table-column prop="reason" label="失败原因" min-width="260" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getGroupList,
  getGroupActiveStats,
  deleteGroup,
  batchDeleteGroups,
  getGroupMembers,
  removeGroupMember,
  updateGroup
} from '@/api/admin'

const loading = ref(false)
const groupList = ref([])
const searchKeyword = ref('')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedGroups = ref([])
const stats = ref({ totalGroups: 0, activeGroups: 0, newGroups: 0 })

const memberDialogVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref([])
const currentGroupId = ref(null)

const editDialogVisible = ref(false)
const editForm = ref({ id: null, name: '', description: '', maxMembers: 500, allMuted: 0 })
const failedDialogVisible = ref(false)
const failedItems = ref([])

const loadGroups = async () => {
  loading.value = true
  try {
    const res = await getGroupList({ keyword: searchKeyword.value, pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) {
      groupList.value = res.data.list || []
      total.value = res.data.total || 0
      stats.value.totalGroups = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载群组列表失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await getGroupActiveStats(7)
    if (res.code === 200) {
      stats.value.activeGroups = res.data?.activeGroups || 0
      stats.value.newGroups = res.data?.newGroups || 0
    }
  } catch (error) {
    // 静默失败，不影响主流程
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadGroups()
}

const handleReset = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  loadGroups()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadGroups()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadGroups()
}

const handleSelectionChange = (selection) => {
  selectedGroups.value = selection
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解散群组 ${row.name} 吗？此操作不可恢复。`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteGroup(row.id)
    if (res.code === 200) {
      ElMessage.success('解散成功')
      loadGroups()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('解散失败')
  }
}

const handleBatchDelete = async () => {
  if (!selectedGroups.value.length) return
  try {
    await ElMessageBox.confirm(`确定要解散选中的 ${selectedGroups.value.length} 个群组吗？此操作不可恢复。`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    loading.value = true
    const ids = selectedGroups.value.map(g => g.id)
    const res = await batchDeleteGroups(ids)
    if (res.code === 200) {
      const successCount = res.data?.successCount ?? ids.length
      const failedCount = res.data?.failedCount ?? 0
      failedItems.value = (res.data?.failedItems || []).map(item => ({
        id: item.id || item.groupId || '未知',
        reason: item.reason || item.msg || '未知原因'
      }))
      ElMessage.success(`批量解散完成：成功 ${successCount}，失败 ${failedCount}`)
      if (failedCount > 0) failedDialogVisible.value = true
      selectedGroups.value = []
      loadGroups()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('批量解散失败')
  } finally {
    loading.value = false
  }
}

const handleViewMembers = async (row) => {
  currentGroupId.value = row.id
  memberDialogVisible.value = true
  memberLoading.value = true
  try {
    const res = await getGroupMembers(row.id)
    if (res.code === 200) memberList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载成员列表失败')
  } finally {
    memberLoading.value = false
  }
}

const handleRemoveMember = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员 ${row.nickname} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await removeGroupMember(currentGroupId.value, row.userId)
    if (res.code === 200) {
      ElMessage.success('移除成功')
      const memberRes = await getGroupMembers(currentGroupId.value)
      if (memberRes.code === 200) memberList.value = memberRes.data || []
      loadGroups()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('移除失败')
  }
}

const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    name: row.name,
    description: row.description || '',
    maxMembers: row.maxMembers || 500,
    allMuted: row.allMuted || 0
  }
  editDialogVisible.value = true
}

const handleSaveEdit = async () => {
  try {
    const res = await updateGroup(editForm.value.id, {
      name: editForm.value.name,
      description: editForm.value.description,
      maxMembers: editForm.value.maxMembers,
      allMuted: editForm.value.allMuted
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      loadGroups()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadGroups()
  loadStats()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel {
  border-radius: var(--dt-radius-xl);
  border: 1px solid var(--dt-border-light);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.panel-header p {
  margin: 4px 0 0;
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-sm);
}

.toolbar-row {
  margin-bottom: 12px;
}

.stats-row {
  margin-bottom: 12px;

  .stat-card {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-md);
    padding: 16px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    border-radius: var(--dt-radius-md);

    .stat-icon {
      width: 44px;
      height: 44px;
      border-radius: var(--dt-radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;

      &.groups { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
      &.active { background: var(--dt-info-bg); color: var(--dt-info-color); }
      &.new { background: var(--dt-success-bg); color: var(--dt-success-color); }
    }

    .stat-info {
      .stat-value {
        font-size: 22px;
        font-weight: 700;
        color: var(--dt-text-primary);
        line-height: 1.2;
      }
      .stat-label {
        font-size: 12px;
        color: var(--dt-text-secondary);
        margin-top: 2px;
      }
    }
  }
}

.batch-actions {
  margin-bottom: var(--dt-spacing-lg);
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.muted {
  color: var(--dt-text-quaternary);
}
</style>
