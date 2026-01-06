<template>
  <div class="app-container">
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="群组名称" prop="groupName">
        <el-input
          v-model="queryParams.groupName"
          placeholder="请输入群组名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="群组类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="群组类型" clearable style="width: 120px">
          <el-option label="普通群组" value="0" />
          <el-option label="企业群组" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="群组状态"
          clearable
          style="width: 120px"
        >
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
          >批量删除</el-button
        >
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="groupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="群组编号" align="center" prop="groupId" width="100" />
      <el-table-column
        label="群组名称"
        align="center"
        prop="groupName"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column
        label="群组简介"
        align="center"
        prop="description"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column label="群组类型" align="center" prop="type" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.type === '1' ? 'success' : 'info'">
            {{ scope.row.type === '1' ? '企业群组' : '普通群组' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="成员数量" align="center" prop="memberCount" width="100" />
      <el-table-column label="最大成员数" align="center" prop="maxMemberCount" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" width="300" fixed="right">
        <template #default="scope">
          <el-button link type="primary" :icon="Edit" @click="handleEdit(scope.row)"
            >编辑</el-button
          >
          <el-button link type="primary" :icon="Setting" @click="handleManage(scope.row)"
            >成员管理</el-button
          >
          <el-button link type="primary" :icon="Setting" @click="handleSettings(scope.row)"
            >群组设置</el-button
          >
          <el-button link type="danger" :icon="Delete" @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="群组名称" prop="groupName">
          <el-input
            v-model="form.groupName"
            placeholder="请输入群组名称"
            :disabled="form.groupId"
          />
        </el-form-item>
        <el-form-item label="群组简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入群组简介"
          />
        </el-form-item>
        <el-form-item label="群组类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择群组类型" style="width: 100%">
            <el-option label="普通群组" value="0" />
            <el-option label="企业群组" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大成员数" prop="maxMemberCount">
          <el-input-number v-model="form.maxMemberCount" :min="2" :max="2000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="加群验证" prop="joinAuth">
          <el-radio-group v-model="form.joinAuth">
            <el-radio label="0">无需验证</el-radio>
            <el-radio label="1">需要验证</el-radio>
            <el-radio label="2">禁止加群</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="群组状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="群组公告" prop="announcement">
          <el-input
            v-model="form.announcement"
            type="textarea"
            :rows="4"
            placeholder="请输入群组公告"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Setting } from '@element-plus/icons-vue'
import {
  listGroup,
  getGroup,
  addGroup,
  updateGroup,
  delGroup,
  updateGroupSettings,
} from '@/api/im/group'

const router = useRouter()

const loading = ref(false)
const groupList = ref([])
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  groupName: '',
  type: '',
  status: '',
})

const form = reactive({
  groupId: null,
  groupName: '',
  description: '',
  type: '0',
  maxMemberCount: 200,
  joinAuth: '1',
  status: '0',
  announcement: '',
})

const rules = {
  groupName: [
    { required: true, message: '群组名称不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '群组名称长度必须介于 2 和 20 之间', trigger: 'blur' },
  ],
  description: [{ max: 200, message: '群组简介长度不能超过200个字符', trigger: 'blur' }],
  announcement: [{ max: 500, message: '群组公告长度不能超过500个字符', trigger: 'blur' }],
}

const getList = async () => {
  loading.value = true
  try {
    const response = await listGroup(queryParams)
    if (response.code === 200) {
      groupList.value = response.rows || []
      total.value = response.total || 0
    } else {
      groupList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取群组列表失败:', error)
    groupList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.groupName = ''
  queryParams.type = ''
  queryParams.status = ''
  handleQuery()
}

const handleSizeChange = size => {
  queryParams.pageSize = size
  getList()
}

const handleCurrentChange = page => {
  queryParams.pageNum = page
  getList()
}

const handleSelectionChange = selection => {
  selectedIds.value = selection.map(item => item.groupId)
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增群组'
  dialogVisible.value = true
}

const handleEdit = async row => {
  resetForm()
  try {
    const response = await getGroup(row.groupId)
    if (response.code === 200 && response.data) {
      Object.assign(form, response.data)
    }
    dialogTitle.value = '编辑群组'
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取群组信息失败')
  }
}

const handleManage = row => {
  router.push({ path: '/im/group/manage', query: { groupId: row.groupId } })
}

const handleSettings = row => {
  router.push({ path: '/im/group/settings', query: { groupId: row.groupId } })
}

const handleDelete = async row => {
  try {
    await ElMessageBox.confirm('是否确认删除该群组？', '警告', { type: 'warning' })
    await delGroup(row.groupId)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的群组')
    return
  }
  try {
    await ElMessageBox.confirm('是否确认删除选中的群组？', '警告', { type: 'warning' })
    await delGroup(selectedIds.value.join(','))
    ElMessage.success('批量删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleStatusChange = async row => {
  const text = row.status === '0' ? '启用' : '停用'
  try {
    await updateGroupSettings({
      groupId: row.groupId,
      status: row.status,
    })
    ElMessage.success(`${text}成功`)
  } catch (error) {
    row.status = row.status === '0' ? '1' : '0'
    ElMessage.error(`${text}失败`)
  }
}

const resetForm = () => {
  form.groupId = null
  form.groupName = ''
  form.description = ''
  form.type = '0'
  form.maxMemberCount = 200
  form.joinAuth = '1'
  form.status = '0'
  form.announcement = ''
  formRef.value?.resetFields()
}

const submitForm = async () => {
  try {
    await formRef.value.validate()
    if (form.groupId) {
      await updateGroup(form)
      ElMessage.success('修改成功')
    } else {
      await addGroup(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;

  .search-form {
    margin-bottom: 16px;
  }

  .mb8 {
    margin-bottom: 8px;
  }

  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
