<template>
  <div class="user-management">
    <!-- 页面头部状态卡片 -->
    <el-row
      :gutter="20"
      class="mb-4"
    >
      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">总用户数</span>
              <span class="stat-value">{{ userStats.total }}</span>
            </div>
            <div class="stat-icon primary-bg">
              <el-icon>
                <User />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">活跃用户</span>
              <span class="stat-value">{{ userStats.active }}</span>
            </div>
            <div class="stat-icon success-bg">
              <el-icon>
                <Connection />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">新增用户(今日)</span>
              <span class="stat-value">{{ userStats.todayNew }}</span>
            </div>
            <div class="stat-icon warning-bg">
              <el-icon>
                <Plus />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">禁用用户</span>
              <span class="stat-value">{{ userStats.disabled }}</span>
            </div>
            <div class="stat-icon info-bg">
              <el-icon>
                <Lock />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <el-card
      class="search-card"
      shadow="never"
    >
      <el-form
        :model="searchForm"
        inline
        class="search-form"
        @submit.prevent
      >
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/昵称/手机号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option
              label="普通用户"
              value="USER"
            />
            <el-option
              label="管理员"
              value="ADMIN"
            />
            <el-option
              label="超级管理员"
              value="SUPER_ADMIN"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option
              label="启用"
              :value="1"
            />
            <el-option
              label="禁用"
              :value="0"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :icon="Search"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <el-button
            :icon="Refresh"
            @click="handleReset"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工具栏和表格 -->
    <el-card
      class="table-card"
      shadow="never"
    >
      <div class="batch-actions">
        <el-button
          type="primary"
          :icon="Plus"
          @click="handleAdd"
        >
          新增用户
        </el-button>
        <el-button
          type="success"
          :icon="Download"
          @click="handleDownloadTemplate"
        >
          下载模板
        </el-button>
        <el-upload
          class="import-upload"
          :show-file-list="false"
          :http-request="handleImport"
          accept=".xlsx,.xls"
        >
          <el-button
            type="warning"
            :icon="Upload"
          >
            批量导入
          </el-button>
        </el-upload>
        <el-button
          type="info"
          :icon="Download"
          @click="handleExport"
        >
          导出数据
        </el-button>

        <div
          v-show="selectedUsers.length > 0"
          class="selection-tools"
        >
          <el-divider direction="vertical" />
          <span class="selected-count">已选 {{ selectedUsers.length }} 项</span>
          <el-button
            type="danger"
            link
            :icon="Delete"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
          <el-button
            type="warning"
            link
            :icon="Lock"
            @click="handleBatchDisable"
          >
            批量禁用
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        mode="responsive"
        :data="userList"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
          align="center"
        />
        <el-table-column
          prop="id"
          label="ID"
          width="80"
          align="center"
        />
        <el-table-column
          label="头像"
          width="70"
          align="center"
        >
          <template #default="{ row }">
            <el-avatar
              :size="32"
              :src="row.avatar"
            >
              {{ row.nickname?.[0] || row.username?.[0] }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column
          prop="username"
          label="用户名"
          width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="nickname"
          label="昵称"
          width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="role"
          label="角色"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getRoleTag(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="mobile"
          label="手机号"
          width="120"
          align="center"
        />
        <el-table-column
          prop="online"
          label="状态"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.online ? 'success' : 'info'"
              size="small"
            >
              {{ row.online ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="账户"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleChangeStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="注册时间"
          min-width="160"
          align="center"
        />
        <el-table-column
          label="操作"
          width="200"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="View"
              @click="handleView(row)"
            >
              详情
            </el-button>
            <el-button
              type="warning"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-dropdown
              trigger="click"
              class="ml-2"
              @command="(cmd) => handleCommand(cmd, row)"
            >
              <el-button
                type="info"
                link
                :icon="MoreFilled"
              />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="resetPwd">
                    重置密码
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="delete"
                    divided
                    class="text-danger"
                  >
                    删除用户
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 用户详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="用户详细信息"
      size="450px"
    >
      <div
        v-if="currentUser"
        class="user-detail"
      >
        <div class="detail-header">
          <el-avatar
            :size="80"
            :src="currentUser.avatar"
          >
            {{ currentUser.nickname?.[0] }}
          </el-avatar>
          <div class="user-info">
            <h3>{{ currentUser.nickname }}</h3>
            <p>@{{ currentUser.username }}</p>
            <el-tag :type="getRoleTag(currentUser.role)">
              {{ getRoleLabel(currentUser.role) }}
            </el-tag>
          </div>
        </div>
        <el-descriptions
          :column="1"
          border
          class="detail-descriptions"
        >
          <el-descriptions-item label="用户ID">
            {{ currentUser.id }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ currentUser.mobile || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ currentUser.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ getGenderLabel(currentUser.gender) }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ currentUser.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="最后在线">
            {{ currentUser.lastOnlineTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="个性签名">
            {{ currentUser.signature || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item
          label="用户名"
          prop="username"
        >
          <el-input
            v-model="userForm.username"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item
          label="昵称"
          prop="nickname"
        >
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item
          label="手机号"
          prop="mobile"
        >
          <el-input v-model="userForm.mobile" />
        </el-form-item>
        <el-form-item
          v-if="!isEdit"
          label="密码"
          prop="password"
        >
          <el-input
            v-model="userForm.password"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item
          label="角色"
          prop="role"
        >
          <el-select
            v-model="userForm.role"
            placeholder="选择角色"
            style="width: 100%"
          >
            <el-option
              label="普通用户"
              value="USER"
            />
            <el-option
              label="管理员"
              value="ADMIN"
            />
            <el-option
              label="超级管理员"
              value="SUPER_ADMIN"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, Connection, Plus, Lock, Search, Refresh, Delete,
  View, Edit, Upload, Download, MoreFilled
} from '@element-plus/icons-vue'
import {
  getUserList, updateUserStatus, resetUserPassword,
  batchDeleteUsers, batchUpdateUserStatus, createUser,
  updateUser, downloadUserTemplate, batchImportUsers, exportUsers,
  getUserStats
} from '@/api/admin'

// 数据定义
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const selectedUsers = ref([])

const userStats = reactive({
  total: 0,
  active: 0,
  todayNew: 0,
  disabled: 0
})

const searchForm = reactive({
  keyword: '',
  role: '',
  status: ''
})

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    })
    if (res.code === 200) {
      userList.value = res.data.list
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载用户统计数据
const loadUserStats = async () => {
  try {
    const res = await getUserStats()
    if (res.code === 200) {
      userStats.total = res.data.total || 0
      userStats.active = res.data.active || 0
      userStats.todayNew = res.data.todayNew || 0
      userStats.disabled = res.data.disabled || 0
    }
  } catch (error) {
    console.error('加载用户统计失败:', error)
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadUsers()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.status = ''
  handleSearch()
}

const handleSelectionChange = selection => {
  selectedUsers.value = selection
}

// 状态切换
const handleChangeStatus = async row => {
  try {
    const res = await updateUserStatus(row.id, row.status === 1 ? 'ENABLED' : 'DISABLED')
    if (res.code === 200) {
      ElMessage.success('操作成功')
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 详情
const drawerVisible = ref(false)
const currentUser = ref(null)
const handleView = row => {
  currentUser.value = row
  drawerVisible.value = true
}

// 新增/编辑
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const userForm = reactive({
  id: '',
  username: '',
  nickname: '',
  password: '',
  mobile: '',
  role: 'USER'
})

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  Object.assign(userForm, {
    id: '',
    username: '',
    nickname: '',
    password: '',
    mobile: '',
    role: 'USER'
  })
  dialogVisible.value = true
}

const handleEdit = row => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    const res = isEdit.value
      ? await updateUser(userForm.id, userForm)
      : await createUser(userForm)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadUsers()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

// 更多操作
const handleCommand = (cmd, row) => {
  if (cmd === 'resetPwd') {
    handleResetPassword(row)
  } else if (cmd === 'delete') {
    handleDelete(row)
  }
}

const handleResetPassword = async row => {
  ElMessageBox.prompt('请输入新密码', '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^.{6,}$/,
    inputErrorMessage: '密码长度至少6位'
  }).then(async ({ value }) => {
    const res = await resetUserPassword(row.id, value)
    if (res.code === 200) {
      ElMessage.success('密码重置成功')
    }
  }).catch(() => { })
}

const handleDelete = row => {
  ElMessageBox.confirm('确定要删除该用户吗？此操作不可逆。', '删除提醒', {
    type: 'warning'
  }).then(async () => {
    const res = await batchDeleteUsers([row.id])
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    }
  }).catch(() => { })
}

// 批量操作
const handleBatchDelete = () => {
  const ids = selectedUsers.value.map(u => u.id)
  ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 个用户吗？`, '批量删除', {
    type: 'warning'
  }).then(async () => {
    const res = await batchDeleteUsers(ids)
    if (res.code === 200) {
      ElMessage.success('批量删除成功')
      loadUsers()
    }
  }).catch(() => { })
}

const handleBatchDisable = () => {
  const ids = selectedUsers.value.map(u => u.id)
  ElMessageBox.confirm(`确定要禁用选中的 ${ids.length} 个用户吗？`, '批量禁用', {
    type: 'warning'
  }).then(async () => {
    const res = await batchUpdateUserStatus(ids, 'DISABLED')
    if (res.code === 200) {
      ElMessage.success('批量禁用成功')
      loadUsers()
    }
  }).catch(() => { })
}

// 导入导出
const handleDownloadTemplate = () => {
  downloadUserTemplate()
}

const handleImport = async ({ file }) => {
  try {
    const res = await batchImportUsers(file)
    if (res.code === 200) {
      const { successCount, failCount } = res.data
      ElMessage.success(`导入完成：成功 ${successCount} 个，失败 ${failCount} 个`)
      loadUsers()
    }
  } catch (error) {
    ElMessage.error('导入失败')
  }
}

const handleExport = () => {
  exportUsers(searchForm.keyword, searchForm.role)
}

// 辅助方法
const getRoleLabel = role => {
  const map = {
    USER: '普通用户',
    ADMIN: '管理员',
    SUPER_ADMIN: '超级管理员'
  }
  return map[role] || role
}

const getRoleTag = role => {
  const map = {
    USER: 'info',
    ADMIN: 'primary',
    SUPER_ADMIN: 'danger'
  }
  return map[role] || ''
}

const getGenderLabel = gender => {
  const map = { 0: '未知', 1: '男', 2: '女' }
  return map[gender] || '-'
}

onMounted(() => {
  loadUsers()
  loadUserStats()
})
</script>

<style scoped lang="scss">
.user-management {
  padding: var(--dt-space-md);
}

.search-card {
  margin-bottom: var(--dt-space-md);

  :deep(.el-card__body) {
    padding-bottom: 0px;
  }
}

.table-card {
  .batch-actions {
    margin-bottom: var(--dt-space-md);
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.stat-card {
  .stat-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .stat-label {
      font-size: 14px;
      color: var(--dt-text-secondary);
    }

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      font-family: var(--dt-font-family-mono);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;

      &.primary-bg {
        background: rgba(0, 137, 255, 0.1);
        color: #0089FF;
      }

      &.success-bg {
        background: rgba(43, 193, 123, 0.1);
        color: #2BC17B;
      }

      &.warning-bg {
        background: rgba(255, 172, 0, 0.1);
        color: #FFAC00;
      }

      &.info-bg {
        background: rgba(140, 140, 140, 0.1);
        color: #8C8C8C;
      }
    }
  }
}

.pagination-container {
  margin-top: var(--dt-space-md);
  display: flex;
  justify-content: flex-end;
}

.user-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--dt-border-color);

    h3 {
      margin: 0 0 4px;
    }

    p {
      margin: 0 0 8px;
      color: var(--dt-text-secondary);
    }
  }
}

.import-upload {
  display: inline-block;
}
</style>
