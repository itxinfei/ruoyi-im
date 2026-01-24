<template>
  <div class="group-management">
    <el-card>
      <!-- 搜索栏 -->
      <el-row :gutter="20" class="search-bar">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索群名称"
            clearable
            @clear="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>
        <el-col :span="4" :offset="14">
          <el-button type="primary" @click="handleRefresh">刷新</el-button>
        </el-col>
      </el-row>

      <!-- 群组列表 -->
      <el-table
        :data="groupList"
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="群组ID" width="80" />
        <el-table-column label="群头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="50">
              {{ row.name?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="群名称" width="200" />
        <el-table-column prop="ownerName" label="群主" width="120" />
        <el-table-column prop="memberCount" label="成员数" width="100">
          <template #default="{ row }">
            <el-tag type="info">{{ row.memberCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxMembers" label="成员上限" width="100" />
        <el-table-column prop="description" label="群描述" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="260">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewMembers(row)">成员</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">解散</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectedGroups.length > 0" class="batch-actions">
        <span class="selected-info">已选择 {{ selectedGroups.length }} 个群组</span>
        <el-button type="danger" size="small" @click="handleBatchDelete">批量解散</el-button>
      </div>

      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 成员列表对话框 -->
    <el-dialog v-model="memberDialogVisible" title="群组成员" width="600px">
      <el-table :data="memberList" v-loading="memberLoading" border max-height="400">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="头像" width="60">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="40">
              {{ row.nickname?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="roleDisplay" label="角色" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'OWNER'" type="danger">群主</el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning">管理员</el-tag>
            <el-tag v-else type="info">成员</el-tag>
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
            <span v-else style="color: #ccc">群主</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 编辑群组对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑群组" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="群名称">
          <el-input v-model="editForm.name" placeholder="请输入群名称" />
        </el-form-item>
        <el-form-item label="群描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入群描述"
          />
        </el-form-item>
        <el-form-item label="成员上限">
          <el-input-number v-model="editForm.maxMembers" :min="1" :max="2000" />
        </el-form-item>
        <el-form-item label="全员禁言">
          <el-switch v-model="editForm.allMuted" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getGroupList,
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

// 成员对话框
const memberDialogVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref([])
const currentGroupId = ref(null)

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  name: '',
  description: '',
  maxMembers: 500,
  allMuted: 0
})

const loadGroups = async () => {
  loading.value = true
  try {
    const res = await getGroupList({
      keyword: searchKeyword.value,
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

const handleSearch = () => {
  pageNum.value = 1
  loadGroups()
}

const handleRefresh = () => {
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

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要解散选中的 ${selectedGroups.value.length} 个群组吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedGroups.value.map(g => g.id)
    const res = await batchDeleteGroups(ids)
    if (res.code === 200) {
      ElMessage.success(`成功解散 ${res.data.successCount} 个群组`)
      selectedGroups.value = []
      loadGroups()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量解散失败')
    }
  }
}

const handleViewMembers = async (row) => {
  currentGroupId.value = row.id
  memberDialogVisible.value = true
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
      // 重新加载成员列表
      const memberRes = await getGroupMembers(currentGroupId.value)
      if (memberRes.code === 200) {
        memberList.value = memberRes.data || []
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
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
})
</script>

<style scoped>
.group-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.selected-info {
  color: #606266;
  font-size: 14px;
}
</style>
