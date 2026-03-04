<template>
  <div class="admin-page user-management">
    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <div>
            <h3>用户管理</h3>
            <p>筛选、启停用与批量治理</p>
          </div>
          <el-space>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="loadUsers">刷新</el-button>
          </el-space>
        </div>
      </template>

      <el-row :gutter="12" class="toolbar-row">
        <el-col :xs="24" :sm="10" :md="8" :lg="6">
          <el-input v-model="searchKeyword" placeholder="搜索用户名/昵称/手机号" clearable @keyup.enter="handleSearch" @clear="handleSearch">
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="8" :md="6" :lg="4">
          <el-select v-model="searchRole" placeholder="选择角色" clearable style="width: 100%" @change="handleSearch">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-col>
      </el-row>

      <div v-if="selectedUsers.length" class="batch-actions">
        <span>已选择 {{ selectedUsers.length }} 个用户</span>
        <el-space>
          <el-button size="small" @click="handleBatchStatus(1)">批量启用</el-button>
          <el-button size="small" type="warning" @click="handleBatchStatus(0)">批量禁用</el-button>
          <el-button size="small" type="danger" @click="handleBatchDelete">批量删除</el-button>
          <el-button v-if="failedItems.length" size="small" @click="failedDialogVisible = true">查看失败项</el-button>
        </el-space>
      </div>

      <el-table :data="userList" v-loading="loading" border @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="mobile" label="手机号" min-width="130" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'SUPER_ADMIN'" type="danger">超级管理员</el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning">管理员</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="170">
          <template #default="{ row }">
            <el-button size="small" @click="handleToggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <el-dialog v-model="failedDialogVisible" title="批量失败明细" width="520px">
      <el-table :data="failedItems" border>
        <el-table-column prop="id" label="用户ID" width="120" />
        <el-table-column prop="reason" label="失败原因" min-width="260" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUserList, updateUserStatus, deleteUser } from '@/api/admin'

const loading = ref(false)
const userList = ref([])
const selectedUsers = ref([])
const failedDialogVisible = ref(false)
const failedItems = ref([])
const searchKeyword = ref('')
const searchRole = ref('')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      keyword: searchKeyword.value,
      role: searchRole.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      userList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const onSelectionChange = (rows) => {
  selectedUsers.value = rows
}

const handleSearch = () => {
  pageNum.value = 1
  loadUsers()
}

const handleReset = () => {
  searchKeyword.value = ''
  searchRole.value = ''
  pageNum.value = 1
  loadUsers()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadUsers()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadUsers()
}

const handleToggleStatus = async (row) => {
  const targetStatus = row.status === 1 ? 0 : 1
  const action = targetStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 ${row.nickname || row.username} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await updateUserStatus(row.id, targetStatus)
    if (res.code === 200) {
      row.status = targetStatus
      ElMessage.success(`${action}成功`)
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

const handleBatchStatus = async (status) => {
  const action = status === 1 ? '启用' : '禁用'
  if (!selectedUsers.value.length) return
  try {
    await ElMessageBox.confirm(`确定要批量${action} ${selectedUsers.value.length} 个用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    let success = 0
    let failed = 0
    failedItems.value = []
    for (const user of selectedUsers.value) {
      try {
        const res = await updateUserStatus(user.id, status)
        if (res.code === 200) {
          success++
        } else {
          failed++
          failedItems.value.push({ id: user.id, reason: res.msg || '失败' })
        }
      } catch (e) {
        failed++
        failedItems.value.push({ id: user.id, reason: '请求失败' })
      }
    }
    ElMessage.success(`批量${action}完成：成功 ${success}，失败 ${failed}`)
    if (failed > 0) failedDialogVisible.value = true
    selectedUsers.value = []
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(`批量${action}失败`)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${row.nickname || row.username} 吗？此操作不可恢复。`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  if (!selectedUsers.value.length) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可恢复。`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    let success = 0
    let failed = 0
    failedItems.value = []
    for (const user of selectedUsers.value) {
      try {
        const res = await deleteUser(user.id)
        if (res.code === 200) {
          success++
        } else {
          failed++
          failedItems.value.push({ id: user.id, reason: res.msg || '失败' })
        }
      } catch (e) {
        failed++
        failedItems.value.push({ id: user.id, reason: '请求失败' })
      }
    }
    ElMessage.success(`批量删除完成：成功 ${success}，失败 ${failed}`)
    if (failed > 0) failedDialogVisible.value = true
    selectedUsers.value = []
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('批量删除失败')
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel {
  border-radius: 12px;
  border: 1px solid #e6ebf3;
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
  color: #64748b;
  font-size: 12px;
}

.toolbar-row {
  margin-bottom: 12px;
}

.batch-actions {
  margin-bottom: 12px;
  background: #f8fafc;
  border: 1px solid #e6ebf3;
  border-radius: 8px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .batch-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
