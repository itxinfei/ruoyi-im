<template>
  <div class="group-management">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="page-title">
        <h2>群组管理</h2>
        <p class="page-desc">管理系统内所有群组，支持成员管理和禁言设置</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Plus" @click="handleCreate">创建群组</el-button>
        <el-button :icon="Download" @click="handleExport">导出数据</el-button>
      </div>
    </div>

    <!-- 搜索和筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索群名称/群主"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px" @change="handleSearch">
            <el-option label="正常" value="normal" />
            <el-option label="全员禁言" value="allMuted" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 群组列表卡片 -->
    <el-card class="table-card" shadow="never">
      <!-- 批量操作栏 -->
      <div v-if="selectedGroups.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedGroups.length }} 项</span>
        <el-button size="small" :icon="Bell" @click="handleBatchMute">批量禁言</el-button>
        <el-button size="small" :icon="MuteNotification" @click="handleBatchUnmute">取消禁言</el-button>
        <el-button size="small" :icon="Delete" type="danger" @click="handleBatchDelete">批量解散</el-button>
        <el-button size="small" text @click="handleClearSelection">取消选择</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="groupList"
        :header-cell-style="{ backgroundColor: 'var(--dt-table-header-bg)', color: 'var(--dt-table-header-text)' }"
        :row-style="{ height: '56px' }"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="群组ID" width="80" />
        <el-table-column label="群头像" width="70">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar">
              {{ row.name?.charAt(0) || '群' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="群名称" min-width="180" />
        <el-table-column prop="ownerName" label="群主" width="120" />
        <el-table-column prop="memberCount" label="成员数" width="90">
          <template #default="{ row }">
            <span class="member-count">
              {{ row.memberCount || 0 }}
              <span class="member-max">&nbsp;/&nbsp;{{ row.maxMembers || 500 }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.allMuted" type="danger" size="small">全员禁言</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" fixed="right" width="240">
          <template #default="{ row }">
            <el-button size="small" text @click="handleViewMembers(row)">
              <el-icon><User /></el-icon>
              成员
            </el-button>
            <el-button size="small" text @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              :type="row.allMuted ? 'success' : 'warning'"
              text
              @click="handleToggleMute(row)"
            >
              <el-icon><component :is="row.allMuted ? 'MuteNotification' : 'Bell'" /></el-icon>
              {{ row.allMuted ? '解除' : '禁言' }}
            </el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              解散
            </el-button>
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

    <!-- 成员管理抽屉 -->
    <el-drawer
      v-model="memberDrawerVisible"
      title="群组成员"
      size="600px"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="drawer-header">
          <span>群组成员</span>
          <div class="header-actions">
            <el-button size="small" :icon="Plus" @click="handleAddMember">添加成员</el-button>
          <el-button size="small" :icon="Upload" @click="handleBatchAddMembers">批量添加</el-button>
          </div>
        </div>
      </template>

      <div v-if="currentGroup" class="member-info">
        <div class="group-info">
          <el-avatar :size="48" :src="currentGroup.avatar">
            {{ currentGroup.name?.charAt(0) || '群' }}
          </el-avatar>
          <div class="info-content">
            <h3>{{ currentGroup.name }}</h3>
            <p>{{ currentGroup.description || '暂无描述' }}</p>
          </div>
        </div>
      </div>

      <!-- 成员列表 -->
      <el-table
        v-loading="memberLoading"
        :data="memberList"
        :header-cell-style="{ backgroundColor: 'var(--dt-table-header-bg)', color: 'var(--dt-table-header-text)' }"
        :row-style="{ height: '52px' }"
        stripe
        style="width: 100%; margin-top: var(--dt-space-md)"
      >
        <el-table-column prop="userId" label="ID" width="70" />
        <el-table-column label="头像" width="60">
          <template #default="{ row }">
            <el-avatar :size="32" :src="row.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="roleDisplay" label="角色" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'OWNER'" type="danger" size="small">群主</el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning" size="small">管理员</el-tag>
            <el-tag v-else type="info" size="small">成员</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="加入时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'OWNER'"
              size="small"
              text
              type="danger"
              @click="handleRemoveMember(row)"
            >
              移除
            </el-button>
            <el-dropdown v-else trigger="click" @command="handleOwnerCommand">
              <el-button size="small" text>
                管理
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-item command="transfer">转让群主</el-dropdown-item>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- 编辑群组对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editMode === 'create' ? '创建群组' : '编辑群组'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="90px">
        <el-form-item label="群名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入群名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="群描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入群描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="成员上限" prop="maxMembers">
          <el-input-number v-model="editForm.maxMembers" :min="3" :max="2000" />
        </el-form-item>
        <el-form-item label="全员禁言">
          <el-switch
            v-model="editForm.allMuted"
            :active-value="1"
            :inactive-value="0"
            active-text="已开启"
            inactive-text="已关闭"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="addMemberDialogVisible"
      title="添加成员"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="addMemberForm" label-width="80px">
        <el-form-item label="用户">
          <el-select
            v-model="addMemberForm.userId"
            filterable
            remote
            reserve-keyword
            placeholder="搜索用户添加"
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.nickname} (${user.username})`"
              :value="user.id"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="user.avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span>{{ user.nickname }}</span>
                <span class="user-option-username">{{ user.username }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="addMemberForm.role">
            <el-radio value="MEMBER">普通成员</el-radio>
            <el-radio value="ADMIN">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAddMemberSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量添加成员对话框 -->
    <el-dialog
      v-model="batchAddDialogVisible"
      title="批量添加成员"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-alert
        :title="`当前群组: ${currentGroup?.name || ''}`"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-form label-width="80px">
        <el-form-item label="选择成员">
          <el-select
            v-model="batchAddSelectedUsers"
            multiple
            filterable
            remote
            reserve-keyword
            placeholder="搜索并选择用户"
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.nickname} (${user.username})`"
              :value="user"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="user.avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span>{{ user.nickname }}</span>
                <span class="user-option-username">{{ user.username }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="已选择">
          <div v-if="batchAddSelectedUsers.length > 0" class="selected-users">
            <el-tag
              v-for="user in batchAddSelectedUsers"
              :key="user.id"
              closable
              @close="removeBatchUser(user)"
              style="margin: 4px"
            >
              {{ user.nickname }}
            </el-tag>
          </div>
          <span v-else style="color: var(--el-text-color-secondary)">未选择任何成员</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchAddDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="batchAddLoading"
          :disabled="batchAddSelectedUsers.length === 0"
          @click="handleConfirmBatchAdd"
        >
          确定添加 ({{ batchAddSelectedUsers.length }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, h } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Download,
  Delete,
  User,
  Edit,
  Bell,
  MuteNotification,
  ArrowDown,
  Upload
} from '@element-plus/icons-vue'
import {
  getGroupList,
  deleteGroup,
  updateGroup,
  batchDeleteGroups,
  getGroupMembers,
  removeGroupMember,
  addGroupMember,
  toggleGroupMute,
  batchMuteGroupMembers,
  batchUnmuteGroupMembers,
  getUserOptions,
  transferGroupOwner
} from '@/api/admin'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 列表数据
const loading = ref(false)
const groupList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const selectedGroups = ref([])
const tableRef = ref()

// 成员管理抽屉
const memberDrawerVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref([])
const currentGroup = ref(null)

// 编辑对话框
const editDialogVisible = ref(false)
const editMode = ref('create') // 'create' | 'edit'
const submitting = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: null,
  name: '',
  description: '',
  maxMembers: 500,
  allMuted: 0
})

// 表单验证规则
const editRules = {
  name: [
    { required: true, message: '请输入群名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  maxMembers: [
    { required: true, message: '请输入成员上限', trigger: 'blur' },
    { type: 'number', min: 3, max: 2000, message: '成员上限在 3-2000 之间', trigger: 'blur' }
  ]
}

// 添加成员对话框
const addMemberDialogVisible = ref(false)
const addMemberForm = reactive({
  userId: null,
  role: 'MEMBER'
})
const userSearchLoading = ref(false)
const userOptions = ref([])

// 批量添加成员对话框
const batchAddDialogVisible = ref(false)
const batchAddSelectedUsers = ref([])
const batchAddLoading = ref(false)

// 加载群组列表
const loadGroups = async () => {
  loading.value = true
  try {
    const res = await getGroupList({
      keyword: searchForm.keyword,
      status: searchForm.status,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      groupList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载群组列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadGroups()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  pageNum.value = 1
  loadGroups()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadGroups()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadGroups()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedGroups.value = selection
}

// 取消选择
const handleClearSelection = () => {
  tableRef.value.clearSelection()
}

// 查看成员
const handleViewMembers = async (row) => {
  currentGroup.value = row
  memberDrawerVisible.value = true
  memberLoading.value = true
  try {
    const res = await getGroupMembers(row.id)
    if (res.code === 200) {
      memberList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载成员列表失败')
  } finally {
    memberLoading.value = false
  }
}

// 编辑群组
const handleEdit = (row) => {
  editMode.value = 'edit'
  Object.assign(editForm, {
    id: row.id,
    name: row.name,
    description: row.description || '',
    maxMembers: row.maxMembers || 500,
    allMuted: row.allMuted || 0
  })
  editDialogVisible.value = true
}

// 创建群组
const handleCreate = () => {
  editMode.value = 'create'
  Object.assign(editForm, {
    id: null,
    name: '',
    description: '',
    maxMembers: 500,
    allMuted: 0
  })
  editDialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await updateGroup(editForm)
    if (res.code === 200) {
      ElMessage.success(editMode.value === 'create' ? '创建成功' : '保存成功')
      editDialogVisible.value = false
      loadGroups()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 切换禁言状态
const handleToggleMute = async (row) => {
  const action = row.allMuted ? '解除禁言' : '全员禁言'
  try {
    await ElMessageBox.confirm(`确定要${action}群组 ${row.name} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const newStatus = row.allMuted ? 0 : 1
    const res = await toggleGroupMute(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      row.allMuted = newStatus
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除群组
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解散群组 ${row.name} 吗？此操作不可恢复！`, '警告', {
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
    if (error !== 'cancel') {
      ElMessage.error('解散失败')
    }
  }
}

// 批量操作
const handleBatchMute = async () => {
  if (selectedGroups.value.length === 0) {
    ElMessage.warning('请先选择要禁言的群组')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要禁言选中的 ${selectedGroups.value.length} 个群组吗？`, '批量禁言', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 批量禁言所有选中的群组
    for (const group of selectedGroups.value) {
      await toggleGroupMute(group.id, true)
    }
    ElMessage.success('批量禁言成功')
    handleClearSelection()
    loadGroups()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量禁言失败')
    }
  }
}

const handleBatchUnmute = async () => {
  if (selectedGroups.value.length === 0) {
    ElMessage.warning('请先选择要解除禁言的群组')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要解除禁言选中的 ${selectedGroups.value.length} 个群组吗？`, '批量解除禁言', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 批量解除禁言所有选中的群组
    for (const group of selectedGroups.value) {
      await toggleGroupMute(group.id, false)
    }
    ElMessage.success('批量解除禁言成功')
    handleClearSelection()
    loadGroups()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量解除禁言失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedGroups.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定要解散选中的 ${selectedGroups.value.length} 个群组吗？此操作不可恢复！`, '批量解散', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedGroups.value.map(g => g.id)
    const res = await batchDeleteGroups(ids)
    if (res.code === 200) {
      ElMessage.success(`成功解散 ${res.data.successCount} 个群组`)
      handleClearSelection()
      loadGroups()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量解散失败')
    }
  }
}

// 移除成员
const handleRemoveMember = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员 ${row.nickname} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await removeGroupMember(currentGroup.value.id, row.userId)
    if (res.code === 200) {
      ElMessage.success('移除成功')
      // 重新加载成员列表和群组信息
      await handleViewMembers(currentGroup.value)
      loadGroups()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

// 群主操作
const handleOwnerCommand = async (command) => {
  if (command === 'transfer') {
    try {
      // 获取可能的群主候选人（管理员和普通成员）
      const candidates = memberList.value.filter(m => m.role !== 'OWNER')

      if (candidates.length === 0) {
        ElMessage.warning('没有可转让的成员')
        return
      }

      // 使用 ElMessageBox 和 ElSelect 结合实现选择
      const { value: selectedMemberId } = await ElMessageBox({
        title: '选择新群主',
        message: h('div', { class: 'transfer-owner-dialog' }, [
          h('p', { style: 'margin-bottom: 12px; color: var(--el-text-color-regular)' }, '请选择新群主：'),
          h('select', {
            id: 'new-owner-select',
            style: 'width: 100%; padding: 8px; border: 1px solid var(--el-border-color); border-radius: 4px;'
          }, candidates.map(m =>
            h('option', { value: m.userId, key: m.userId }, `${m.nickname} (${m.role === 'ADMIN' ? '管理员' : '成员'})`)
          ))
        ]),
        showCancelButton: true,
        confirmButtonText: '确定转让',
        cancelButtonText: '取消',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            const select = document.getElementById('new-owner-select')
            if (select) {
              instance.value = select.value
            }
          }
          done()
        }
      })

      if (!selectedMemberId) {
        ElMessage.warning('请选择新群主')
        return
      }

      const selectedMember = candidates.find(m => m.userId == selectedMemberId)
      if (!selectedMember) {
        ElMessage.error('选择的成员无效')
        return
      }

      await ElMessageBox.confirm(
        `确定将群主转让给 "${selectedMember.nickname}" 吗？转让后你将成为普通成员。`,
        '转让确认',
        { type: 'warning' }
      )

      const res = await transferGroupOwner(currentGroup.value.id, selectedMember.userId)
      if (res.code === 200) {
        ElMessage.success('群主转让成功')
        // 重新加载成员列表和群组信息
        await handleViewMembers(currentGroup.value)
        loadGroups()
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('转让失败')
      }
    }
  }
}

// 添加成员
const handleAddMember = () => {
  addMemberForm.userId = null
  addMemberForm.role = 'MEMBER'
  userOptions.value = []
  addMemberDialogVisible.value = true
}

// 批量添加成员
const handleBatchAddMembers = () => {
  if (!currentGroup.value) {
    ElMessage.warning('请先选择群组')
    return
  }
  batchAddSelectedUsers.value = []
  batchAddDialogVisible.value = true
}

// 批量添加成员确认
const handleConfirmBatchAdd = async () => {
  if (batchAddSelectedUsers.value.length === 0) {
    ElMessage.warning('请选择要添加的成员')
    return
  }

  // 检查是否已存在
  const existingUserIds = memberList.value.map(m => m.userId)
  const newUserIds = batchAddSelectedUsers.value
    .filter(u => !existingUserIds.includes(u.userId || u.id))
    .map(u => u.userId || u.id)

  if (newUserIds.length === 0) {
    ElMessage.warning('所选成员已在群组中')
    return
  }

  if (newUserIds.length < batchAddSelectedUsers.value.length) {
    ElMessage.warning(`${batchAddSelectedUsers.value.length - newUserIds.length} 个成员已存在，将跳过`)
  }

  batchAddLoading.value = true
  try {
    // 逐个添加成员（因为 API 支持单个添加）
    let successCount = 0
    for (const userId of newUserIds) {
      try {
        const res = await addGroupMember(currentGroup.value.id, {
          userId,
          role: 'MEMBER'
        })
        if (res.code === 200) {
          successCount++
        }
      } catch (error) {
        console.error(`添加成员 ${userId} 失败:`, error)
      }
    }

    if (successCount > 0) {
      ElMessage.success(`成功添加 ${successCount} 个成员`)
      batchAddDialogVisible.value = false
      // 重新加载成员列表
      await handleViewMembers(currentGroup.value)
      loadGroups()
    } else {
      ElMessage.error('添加失败')
    }
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    batchAddLoading.value = false
  }
}

// 移除批量添加中的用户
const removeBatchUser = (user) => {
  const index = batchAddSelectedUsers.value.findIndex(u => (u.id || u.userId) === (user.id || user.userId))
  if (index > -1) {
    batchAddSelectedUsers.value.splice(index, 1)
  }
}

// 搜索用户
const searchUsers = async (query) => {
  if (!query) return
  userSearchLoading.value = true
  try {
    const res = await getUserOptions(query)
    if (res.code === 200) {
      userOptions.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('搜索用户失败')
  } finally {
    userSearchLoading.value = false
  }
}

// 添加成员提交
const handleAddMemberSubmit = async () => {
  if (!addMemberForm.userId) {
    ElMessage.warning('请选择要添加的用户')
    return
  }

  submitting.value = true
  try {
    const res = await addGroupMember(currentGroup.value.id, {
      userId: addMemberForm.userId,
      role: addMemberForm.role
    })
    if (res.code === 200) {
      ElMessage.success('添加成功')
      addMemberDialogVisible.value = false
      await handleViewMembers(currentGroup.value)
      loadGroups()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    submitting.value = false
  }
}

// 导出数据
const handleExport = () => {
  try {
    // 导出当前群组列表为 CSV
    const headers = ['群组ID', '群组名称', '群主ID', '成员数量', '类型', '创建时间']
    const rows = groupList.value.map(g => [
      g.id,
      `"${g.name || ''}"`, // 名称加引号防止CSV格式问题
      g.ownerId,
      g.memberCount || 0,
      g.type || '',
      g.createTime || ''
    ])

    // 添加 BOM 以支持中文
    const BOM = '\uFEFF'
    const csvContent = BOM + headers.join(',') + '\n' + rows.map(r => r.join(',')).join('\n')

    // 创建 Blob 并下载
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `群组列表_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadGroups()
})
</script>

<style scoped>
/* 引入主题变量 */
@import '@/styles/admin-theme.css';

/* ================================
   页面容器
   ================================ */
.group-management {
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

/* 成员数字段 */
.member-count {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
}

.member-max {
  color: var(--dt-text-placeholder);
  font-size: var(--dt-font-size-xs);
  margin-left: 2px;
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
   抽屉头部
   ================================ */
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drawer-header .header-actions {
  display: flex;
  gap: var(--dt-space-xs);
}

.drawer-header h3 {
  margin: 0;
  font-size: var(--dt-font-size-md);
  color: var(--dt-text-primary);
}

/* ================================
   群组信息
   ================================ */
.group-info {
  display: flex;
  gap: var(--dt-space-md);
  padding: var(--dt-space-md);
  background-color: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  margin-bottom: var(--dt-space-md);
}

.info-content h3 {
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.info-content p {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0;
}

/* ================================
   用户选项样式
   ================================ */
.user-option {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

.user-option-username {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-placeholder);
  margin-left: 4px;
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
