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
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button type="success" @click="importDialogVisible = true">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <el-button @click="handleReset">
              重置
            </el-button>
            <el-button type="primary" @click="loadUsers">
              刷新
            </el-button>
          </el-space>
        </div>
      </template>

      <el-row :gutter="12" class="toolbar-row">
        <el-col
          :xs="24"
          :sm="10"
          :md="8"
          :lg="6"
        >
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名/昵称/手机号"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>
        <el-col
          :xs="24"
          :sm="8"
          :md="6"
          :lg="4"
        >
          <el-select
            v-model="searchRole"
            placeholder="选择角色"
            clearable
            style="width: 100%"
            @change="handleSearch"
          >
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-col>
      </el-row>

      <div v-if="selectedUsers.length" class="batch-actions">
        <span>已选择 {{ selectedUsers.length }} 个用户</span>
        <el-space>
          <el-button size="small" @click="handleBatchStatus(1)">
            批量启用
          </el-button>
          <el-button size="small" type="warning" @click="handleBatchStatus(0)">
            批量禁用
          </el-button>
          <el-button size="small" type="danger" @click="handleBatchDelete">
            批量删除
          </el-button>
          <el-button v-if="failedItems.length" size="small" @click="failedDialogVisible = true">
            查看失败项
          </el-button>
        </el-space>
      </div>

      <el-table
        v-loading="loading"
        :data="userList"
        border
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column
          prop="id"
          label="ID"
          width="80"
          sortable
        />
        <el-table-column
          prop="username"
          label="用户名"
          min-width="120"
          sortable
        />
        <el-table-column
          prop="nickname"
          label="昵称"
          min-width="120"
          sortable
        />
        <el-table-column
          prop="mobile"
          label="手机号"
          min-width="130"
          sortable
        />
        <el-table-column
          prop="role"
          label="角色"
          width="120"
          sortable
        >
          <template #default="{ row }">
            <el-tag v-if="row.role === 'SUPER_ADMIN'" type="danger">
              超级管理员
            </el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning">
              管理员
            </el-tag>
            <el-tag v-else type="info">
              普通用户
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button v-if="isSuperAdmin" size="small" @click="handleToggleRole(row)">
              角色
            </el-button>
            <el-button size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              删除
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

    <el-dialog v-model="failedDialogVisible" title="批量失败明细" width="520px">
      <el-table :data="failedItems" border>
        <el-table-column prop="id" label="用户 ID" width="120" />
        <el-table-column prop="reason" label="失败原因" min-width="260" />
      </el-table>
    </el-dialog>

    <!-- 导入用户对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入用户"
      width="500px"
      append-to-body
    >
      <div class="import-tip">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            请上传 CSV 格式文件，编码 UTF-8
          </template>
        </el-alert>
      </div>
      <div class="import-template">
        <p>文件格式要求：</p>
        <ul>
          <li>第一行为表头：用户名,昵称,手机号,密码,角色</li>
          <li>角色可选值：USER（普通用户）, ADMIN（管理员）</li>
          <li>密码留空则使用默认密码 123456</li>
        </ul>
        <el-button
          size="small"
          type="primary"
          link
          @click="downloadTemplate"
        >
          <el-icon><Download /></el-icon>
          下载模板
        </el-button>
      </div>
      <el-upload
        ref="uploadRef"
        class="import-upload"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".csv"
        :on-change="handleFileChange"
      >
        <el-icon class="el-icon--upload">
          <Upload />
        </el-icon>
        <div class="el-upload__text">
          拖拽文件到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 CSV 文件
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="importing" @click="confirmImport">
          确认导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 角色变更对话框（仅 SUPER_ADMIN 可用） -->
    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="420px">
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
        <template #title>
          仅超级管理员可修改用户角色
        </template>
      </el-alert>
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="当前角色">
          <el-tag :type="getRoleTagType(currentRoleData.role)">
            {{ getRoleLabel(currentRoleData.role) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="目标角色">
          <el-select v-model="roleForm.targetRole" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="roleChanging" @click="confirmRoleChange">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 用户详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="用户详情" size="520px">
      <template #default>
        <div v-if="currentDetail" class="detail-content">
          <el-descriptions title="基本信息" :column="1" border>
            <el-descriptions-item label="用户 ID">
              {{ currentDetail.id }}
            </el-descriptions-item>
            <el-descriptions-item label="用户名">
              {{ currentDetail.username }}
            </el-descriptions-item>
            <el-descriptions-item label="昵称">
              {{ currentDetail.nickname || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ currentDetail.mobile || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ currentDetail.email || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="getRoleTagType(currentDetail.role)">
                {{ getRoleLabel(currentDetail.role) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentDetail.status === 1 ? 'success' : 'danger'">
                {{ currentDetail.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <el-descriptions
            title="扩展信息"
            :column="1"
            border
            style="margin-top: 16px"
          >
            <el-descriptions-item label="头像">
              <el-avatar v-if="currentDetail.avatar" :size="64" :src="currentDetail.avatar" />
              <span v-else class="text-secondary">未设置</span>
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ getGenderLabel(currentDetail.gender) }}
            </el-descriptions-item>
            <el-descriptions-item label="生日">
              {{ currentDetail.birthday || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="签名">
              {{ currentDetail.signature || '-' }}
            </el-descriptions-item>
          </el-descriptions>

          <el-descriptions
            title="最近活动"
            :column="1"
            border
            style="margin-top: 16px"
          >
            <el-descriptions-item label="会话数">
              {{ currentDetail.sessionCount ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="好友数">
              {{ currentDetail.friendCount ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="群组数">
              {{ currentDetail.groupCount ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="消息数">
              {{ currentDetail.messageCount ?? '-' }}
            </el-descriptions-item>
          </el-descriptions>

          <el-descriptions
            title="时间信息"
            :column="1"
            border
            style="margin-top: 16px"
          >
            <el-descriptions-item label="创建时间">
              {{ currentDetail.createTime }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ currentDetail.updateTime }}
            </el-descriptions-item>
            <el-descriptions-item label="最后登录时间">
              {{ currentDetail.lastLoginTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="登录IP">
              {{ currentDetail.lastLoginIp || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, Upload } from '@element-plus/icons-vue'
import { getUserList, getUserDetail, updateUserStatus, updateUserRole, deleteUser, batchDeleteUsers, batchImportUsers } from '@/api/admin'
import tokenManager from '@/utils/tokenManager'

const loading = ref(false)
const userList = ref([])
const selectedUsers = ref([])
const failedDialogVisible = ref(false)
const failedItems = ref([])
const importDialogVisible = ref(false)
const importing = ref(false)
const uploadRef = ref(null)
const templateFile = ref(null)
const searchKeyword = ref('')
const searchRole = ref('')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 角色变更相关
const roleDialogVisible = ref(false)
const roleChanging = ref(false)
const currentRoleData = ref({ role: '' })
const roleForm = ref({ targetRole: '' })
const roleTargetRow = ref(null)

// 详情抽屉相关
const detailDrawerVisible = ref(false)
const currentDetail = ref(null)

// 当前用户是否是超级管理员
const isSuperAdmin = ref(false)

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

// 导出用户列表
const handleExport = () => {
  if (userList.value.length === 0) {
    ElMessage.warning('暂无用户数据可导出')
    return
  }
  const headers = ['ID', '用户名', '昵称', '手机号', '角色', '状态', '创建时间']
  const fields = ['id', 'username', 'nickname', 'mobile', 'role', 'status', 'createTime']
  const data = userList.value.map(user => fields.map(f => {
    if (f === 'role') return getRoleLabel(user[f])
    if (f === 'status') return user[f] === 1 ? '正常' : '禁用'
    return user[f] || ''
  }))
  const csvContent = [headers, ...data].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n')
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `用户列表_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 下载导入模板
const downloadTemplate = () => {
  const headers = ['username', 'nickname', 'mobile', 'password', 'role']
  const example = ['zhangsan', '张三', '13800138000', '', 'USER']
  const csvContent = [headers, example].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n')
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '用户导入模板.csv'
  link.click()
  URL.revokeObjectURL(url)
}

// 处理文件选择
const handleFileChange = (file) => {
  templateFile.value = file.raw
}

// 确认导入
const confirmImport = async () => {
  if (!templateFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }
  importing.value = true
  try {
    const text = await templateFile.value.text()
    const lines = text.split('\n').filter(line => line.trim())
    if (lines.length < 2) {
      ElMessage.warning('文件内容为空或格式不正确')
      importing.value = false
      return
    }
    // 跳过表头，解析数据
    const users = []
    for (let i = 1; i < lines.length; i++) {
      const cols = lines[i].split(',').map(c => c.replace(/^"|"$/g, '').trim())
      if (cols.length >= 1 && cols[0]) {
        users.push({
          username: cols[0] || '',
          nickname: cols[1] || cols[0],
          mobile: cols[2] || '',
          password: cols[3] || '123456',
          role: cols[4] || 'USER'
        })
      }
    }
    if (users.length === 0) {
      ElMessage.warning('未解析到有效用户数据')
      importing.value = false
      return
    }
    const res = await batchImportUsers(users)
    if (res.code === 200) {
      const result = res.data
      if (result.failCount > 0) {
        ElMessage.warning(`成功导入 ${result.successCount} 个用户，${result.failCount} 个失败`)
        if (result.failedItems && result.failedItems.length > 0) {
          failedItems.value = result.failedItems.map(item => ({
            username: item.id || '未知',
            reason: item.reason
          }))
          failedDialogVisible.value = true
        }
      } else {
        ElMessage.success(`成功导入 ${result.successCount} 个用户`)
      }
      importDialogVisible.value = false
      templateFile.value = null
      loadUsers()
    } else {
      throw new Error(res.message || '导入失败')
    }
  } catch (e) {
    ElMessage.error('导入失败：' + e.message)
  } finally {
    importing.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadUsers()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadUsers()
}

/**
 * 获取角色标签文字
 */
const getRoleLabel = (role) => {
  if (role === 'SUPER_ADMIN') return '超级管理员'
  if (role === 'ADMIN') return '管理员'
  return '普通用户'
}

/**
 * 获取角色标签类型
 */
const getRoleTagType = (role) => {
  if (role === 'SUPER_ADMIN') return 'danger'
  if (role === 'ADMIN') return 'warning'
  return 'info'
}

/**
 * 获取性别标签
 */
const getGenderLabel = (gender) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

/**
 * 查看详情
 */
const handleViewDetail = async (row) => {
  try {
    const res = await getUserDetail(row.id)
    if (res.code === 200) {
      currentDetail.value = res.data
      detailDrawerVisible.value = true
    } else {
      ElMessage.error('获取用户详情失败')
    }
  } catch (error) {
    ElMessage.error('获取用户详情失败')
  }
}

/**
 * 打开角色变更对话框（仅 SUPER_ADMIN 可用）
 */
const handleToggleRole = (row) => {
  if (!isSuperAdmin.value) {
    ElMessage.warning('仅超级管理员可修改用户角色')
    return
  }
  currentRoleData.value = { role: row.role }
  roleForm.value.targetRole = row.role
  roleTargetRow.value = row
  roleDialogVisible.value = true
}

/**
 * 确认角色变更
 */
const confirmRoleChange = async () => {
  const newRole = roleForm.value.targetRole
  if (!newRole || newRole === currentRoleData.value.role) {
    ElMessage.warning('请选择不同的角色')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要将用户 ${roleTargetRow.value.nickname || roleTargetRow.value.username} 的角色变更为 ${getRoleLabel(newRole)} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    roleChanging.value = true
    const res = await updateUserRole(roleTargetRow.value.id, newRole)
    if (res.code === 200) {
      ElMessage.success('角色修改成功')
      roleTargetRow.value.role = newRole
      roleDialogVisible.value = false
      loadUsers()
    } else {
      ElMessage.error(res.msg || '角色修改失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('角色修改失败')
  } finally {
    roleChanging.value = false
  }
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

    loading.value = true
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
  } finally {
    loading.value = false
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

    loading.value = true
    const ids = selectedUsers.value.map(u => u.id)
    const res = await batchDeleteUsers(ids)

    if (res.code === 200 && res.data) {
      const { successCount, failCount, failedItems: items } = res.data
      ElMessage.success(`批量删除完成：成功 ${successCount}，失败 ${failCount}`)
      if (failCount > 0) {
        failedItems.value = items || []
        failedDialogVisible.value = true
      }
    } else {
      ElMessage.error(res.msg || '批量删除失败')
    }

    selectedUsers.value = []
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('批量删除失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 检查当前用户是否是超级管理员
  try {
    const userRole = tokenManager.getRole()
    isSuperAdmin.value = userRole === 'SUPER_ADMIN'
  } catch (e) {
    isSuperAdmin.value = false
  }
  loadUsers()
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
  font-size: var(--dt-font-size-lg);
  color: var(--dt-text-primary);
}

.panel-header p {
  margin: var(--dt-spacing-xs) 0 0;
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
}

.toolbar-row {
  margin-bottom: var(--dt-spacing-md);
}

.batch-actions {
  margin-bottom: var(--dt-spacing-md);
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  padding: var(--dt-spacing-sm) var(--dt-spacing-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager-wrap {
  margin-top: var(--dt-spacing-md);
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-lg);
}

.text-secondary {
  color: var(--dt-text-tertiary);
}

.import-tip {
  margin-bottom: var(--dt-spacing-lg);
}

.import-template {
  margin-bottom: var(--dt-spacing-lg);
  padding: var(--dt-spacing-md);
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);

  p {
    margin: 0 0 var(--dt-spacing-sm);
    font-weight: 600;
    color: var(--dt-text-primary);
  }

  ul {
    margin: 0 0 var(--dt-spacing-sm);
    padding-left: var(--dt-spacing-lg);
    color: var(--dt-text-secondary);
    font-size: var(--dt-font-size-sm);

    li {
      margin-bottom: 4px;
    }
  }
}

.import-upload {
  text-align: center;

  .el-icon--upload {
    font-size: 48px;
    color: var(--dt-text-quaternary);
    margin-bottom: var(--dt-spacing-sm);
  }

  .el-upload__text {
    color: var(--dt-text-secondary);
    font-size: var(--dt-font-size-sm);

    em {
      color: var(--dt-brand-color);
      font-style: normal;
    }
  }
}
</style>
