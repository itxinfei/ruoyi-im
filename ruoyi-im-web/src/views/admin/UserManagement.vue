<template>
  <div class="user-management">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="page-title">
        <h2>用户管理</h2>
        <p class="page-desc">管理系统用户账号、角色和状态</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        <el-button :icon="Upload" @click="handleImport">批量导入</el-button>
        <el-button :icon="Download" @click="handleExport">导出数据</el-button>
      </div>
    </div>

    <!-- 搜索和筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名/昵称/手机号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部角色" clearable style="width: 140px" @change="handleSearch">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px" @change="handleSearch">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表卡片 -->
    <el-card class="table-card" shadow="never">
      <!-- 批量操作栏 -->
      <div v-if="selectedRows.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedRows.length }} 项</span>
        <el-button size="small" :icon="Delete" type="danger" @click="handleBatchDelete">批量删除</el-button>
        <el-button size="small" :icon="Lock" @click="handleBatchDisable">批量禁用</el-button>
        <el-button size="small" :icon="Unlock" @click="handleBatchEnable">批量启用</el-button>
        <el-button size="small" text @click="handleClearSelection">取消选择</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="userList"
        :header-cell-style="{ backgroundColor: 'var(--dt-table-header-bg)', color: 'var(--dt-table-header-text)' }"
        :row-style="{ height: '56px' }"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="avatar" label="头像" width="70">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'SUPER_ADMIN'" type="danger" size="small">超级管理员</el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning" size="small">管理员</el-tag>
            <el-tag v-else type="info" size="small">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button size="small" text @click="handleView(row)">查看</el-button>
            <el-button size="small" text @click="handleEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              text
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="用户详情"
      size="500px"
      :close-on-click-modal="false"
    >
      <div v-if="currentUser" class="user-detail">
        <div class="detail-header">
          <el-avatar :size="80" :src="currentUser.avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="user-info">
            <h3>{{ currentUser.nickname || currentUser.username }}</h3>
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentUser.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
        </div>
        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.mobile || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag v-if="currentUser.role === 'SUPER_ADMIN'" type="danger">超级管理员</el-tag>
            <el-tag v-else-if="currentUser.role === 'ADMIN'" type="warning">管理员</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentUser.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后登录">{{ currentUser.lastLoginTime || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-actions">
          <el-button type="primary" :icon="Edit" @click="handleEdit(currentUser)">编辑</el-button>
          <el-button :icon="RefreshRight" @click="handleResetPassword">重置密码</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editMode === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" :disabled="editMode === 'edit'" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="editForm.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="editMode === 'add'" label="密码" prop="password">
          <el-input v-model="editForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Upload,
  Download,
  Delete,
  Lock,
  Unlock,
  User,
  Edit,
  RefreshRight
} from '@element-plus/icons-vue'
import { getUserList, updateUserStatus, deleteUser, createUser, updateUser, batchDeleteUsers, batchUpdateUserStatus } from '@/api/admin'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: '',
  status: ''
})

// 列表数据
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

// 选中的行
const selectedRows = ref([])
const tableRef = ref()

// 详情抽屉
const detailDrawerVisible = ref(false)
const currentUser = ref(null)

// 编辑对话框
const editDialogVisible = ref(false)
const editMode = ref('add') // 'add' | 'edit'
const submitting = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: null,
  username: '',
  nickname: '',
  mobile: '',
  email: '',
  role: 'USER',
  status: 1,
  password: ''
})

// 表单验证规则
const editRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  mobile: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      keyword: searchForm.keyword,
      role: searchForm.role,
      status: searchForm.status,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      userList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadUsers()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.status = ''
  pageNum.value = 1
  loadUsers()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadUsers()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadUsers()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 取消选择
const handleClearSelection = () => {
  tableRef.value.clearSelection()
}

// 查看详情
const handleView = (row) => {
  currentUser.value = { ...row }
  detailDrawerVisible.value = true
}

// 新增用户
const handleAdd = () => {
  editMode.value = 'add'
  Object.assign(editForm, {
    id: null,
    username: '',
    nickname: '',
    mobile: '',
    email: '',
    role: 'USER',
    status: 1,
    password: ''
  })
  editDialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  editMode.value = 'edit'
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    mobile: row.mobile,
    email: row.email || '',
    role: row.role,
    status: row.status,
    password: ''
  })
  editDialogVisible.value = true
  detailDrawerVisible.value = false
}

// 提交表单
const handleSubmit = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    let res
    if (editMode.value === 'add') {
      res = await createUser(editForm)
    } else {
      res = await updateUser(editForm)
    }
    if (res.code === 200) {
      ElMessage.success(editMode.value === 'add' ? '新增成功' : '编辑成功')
      editDialogVisible.value = false
      loadUsers()
    }
  } catch (error) {
    ElMessage.error(editMode.value === 'add' ? '新增失败' : '编辑失败')
  } finally {
    submitting.value = false
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 ${row.nickname || row.username} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const newStatus = row.status === 1 ? 0 : 1
    const res = await updateUserStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      row.status = newStatus
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${row.nickname || row.username} 吗？此操作不可恢复！`, '警告', {
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
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个用户吗？此操作不可恢复！`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await batchDeleteUsers(ids)
    ElMessage.success('批量删除成功')
    handleClearSelection()
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 批量禁用
const handleBatchDisable = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要禁用的用户')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要禁用选中的 ${selectedRows.value.length} 个用户吗？`, '批量禁用', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await batchUpdateUserStatus(ids, 0)
    ElMessage.success('批量禁用成功')
    handleClearSelection()
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量禁用失败')
    }
  }
}

// 批量启用
const handleBatchEnable = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要启用的用户')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要启用选中的 ${selectedRows.value.length} 个用户吗？`, '批量启用', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await batchUpdateUserStatus(ids, 1)
    ElMessage.success('批量启用成功')
    handleClearSelection()
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量启用失败')
    }
  }
}

// 导入
const handleImport = () => {
  ElMessage.info('批量导入功能开发中')
}

// 导出
const handleExport = () => {
  try {
    const headers = ["用户ID", "用户名", "昵称", "邮箱", "手机号", "角色", "状态", "创建时间"]
    const roleMap = { USER: "普通用户", ADMIN: "管理员", SUPER_ADMIN: "超级管理员" }
    const statusMap = { 0: "禁用", 1: "启用" }

    const rows = userList.value.map(u => {
      return [
        u.id,
        JSON.stringify(String(u.username || "")),
        JSON.stringify(String(u.nickname || "")),
        JSON.stringify(String(u.email || "")),
        JSON.stringify(String(u.mobile || "")),
        roleMap[u.role] || u.role,
        statusMap[u.status] ?? u.status,
        u.createTime || ""
      ]
    })

    const BOM = "\uFEFF"
    const csvContent = BOM + headers.join(",") + "\n" + rows.map(r => r.join(",")).join("\n")

    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" })
    const url = URL.createObjectURL(blob)
    const link = document.createElement("a")
    link.href = url
    link.download = `用户列表_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    URL.revokeObjectURL(url)

    ElMessage.success("导出成功")
  } catch (error) {
    ElMessage.error("导出失败")
  }
}
const handleResetPassword = () => {
  ElMessage.info('重置密码功能开发中')
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
/* 引入主题变量 */
@import '@/styles/admin-theme.scss';

/* ================================
   页面容器
   ================================ */
.user-management {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* ================================
   页面头部
   ================================ */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--dt-space-md);
}

.page-title h2 {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin: 0;
}

.page-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 4px 0 0 0;
}

.page-actions {
  display: flex;
  gap: var(--dt-space-sm);
}

/* ================================
   搜索卡片
   ================================ */
.search-card {
  margin-bottom: var(--dt-space-md);
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.search-card :deep(.el-card__body) {
  padding: var(--dt-space-md);
}

.search-form {
  margin: 0;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: var(--dt-space-md);
}

.search-form :deep(.el-form-item__label) {
  color: var(--dt-text-regular);
  font-weight: var(--dt-font-weight-normal);
}

/* ================================
   表格卡片
   ================================ */
.table-card {
  flex: 1;
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
  display: flex;
  flex-direction: column;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.table-card :deep(.el-table) {
  flex: 1;
}

/* 批量操作栏 */
.batch-actions {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  padding: var(--dt-space-sm) var(--dt-space-md);
  background-color: var(--dt-bg-active);
  border-bottom: 1px solid var(--dt-divider);
}

.selected-count {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-primary);
  margin-right: var(--dt-space-sm);
}

/* ================================
   分页容器
   ================================ */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: var(--dt-space-md);
  border-top: 1px solid var(--dt-divider);
}

/* ================================
   用户详情
   ================================ */
.user-detail {
  padding: var(--dt-space-md);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
  padding-bottom: var(--dt-space-md);
  border-bottom: 1px solid var(--dt-divider);
  margin-bottom: var(--dt-space-md);
}

.user-info {
  flex: 1;
}

.user-info h3 {
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin: 0 0 8px 0;
}

.detail-descriptions {
  margin-bottom: var(--dt-space-lg);
}

.detail-descriptions :deep(.el-descriptions__label) {
  color: var(--dt-text-secondary);
  width: 90px;
}

.detail-descriptions :deep(.el-descriptions__content) {
  color: var(--dt-text-primary);
}

.detail-actions {
  display: flex;
  gap: var(--dt-space-sm);
  justify-content: flex-end;
  padding-top: var(--dt-space-md);
  border-top: 1px solid var(--dt-divider);
}

/* ================================
   对话框
   ================================ */
:deep(.el-dialog) {
  border-radius: var(--dt-radius-lg);
}

:deep(.el-dialog__header) {
  padding: var(--dt-space-md) var(--dt-space-lg);
  border-bottom: 1px solid var(--dt-divider);
}

:deep(.el-dialog__body) {
  padding: var(--dt-space-lg) var(--dt-space-lg);
}

:deep(.el-dialog__footer) {
  padding: var(--dt-space-sm) var(--dt-space-lg);
  border-top: 1px solid var(--dt-divider);
}

/* ================================
   抽屉
   ================================ */
:deep(.el-drawer) {
  border-radius: var(--dt-radius-lg) 0 0 0 0;
}

:deep(.el-drawer__header) {
  padding: var(--dt-space-md) var(--dt-space-lg);
  border-bottom: 1px solid var(--dt-divider);
  margin-bottom: 0;
}

:deep(.el-drawer__body) {
  padding: 0;
}

/* ================================
   响应式
   ================================ */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--dt-space-sm);
  }

  .page-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .search-form :deep(.el-form-item) {
    margin-right: 0;
    width: 100%;
  }

  .search-form :deep(.el-input),
  .search-form :deep(.el-select) {
    width: 100% !important;
  }
}
</style>
