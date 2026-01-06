<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="用户名称" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号码" prop="phonenumber">
        <el-input
          v-model="queryParams.phonenumber"
          placeholder="请输入手机号码"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="用户状态"
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

    <!-- 操作工具栏 -->
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

    <!-- 用户列表 -->
    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="用户编号" align="center" prop="userId" width="100" />
      <el-table-column
        label="用户名称"
        align="center"
        prop="userName"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column
        label="用户昵称"
        align="center"
        prop="nickName"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column label="手机号码" align="center" prop="phonenumber" width="130" />
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
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button link type="primary" :icon="Edit" @click="handleEdit(scope.row)"
            >编辑</el-button
          >
          <el-button link type="danger" :icon="Delete" @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 用户表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名称" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名称" :disabled="form.userId" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickName">
          <el-input v-model="form.nickName" placeholder="请输入用户昵称" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phonenumber">
          <el-input v-model="form.phonenumber" placeholder="请输入手机号码" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="!form.userId" label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  listUser,
  getUser,
  addUser,
  updateUser,
  delUser,
  changeUserStatus,
} from '@/api/system/user'

// 响应式状态
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  phonenumber: '',
  status: '',
})

const form = reactive({
  userId: null,
  userName: '',
  nickName: '',
  phonenumber: '',
  email: '',
  password: '',
  status: '0',
  remark: '',
})

const rules = {
  userName: [{ required: true, message: '用户名称不能为空', trigger: 'blur' }],
  nickName: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
  phonenumber: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }],
}

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const response = await listUser(queryParams)
    if (response.code === 200) {
      userList.value = response.rows || []
      total.value = response.total || 0
    } else {
      userList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.userName = ''
  queryParams.phonenumber = ''
  queryParams.status = ''
  handleQuery()
}

// 分页大小变化
const handleSizeChange = size => {
  queryParams.pageSize = size
  getList()
}

// 页码变化
const handleCurrentChange = page => {
  queryParams.pageNum = page
  getList()
}

// 多选框选中
const handleSelectionChange = selection => {
  selectedIds.value = selection.map(item => item.userId)
}

// 新增用户
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = async row => {
  resetForm()
  try {
    const response = await getUser(row.userId)
    if (response.code === 200 && response.data) {
      Object.assign(form, response.data)
    }
    dialogTitle.value = '编辑用户'
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 删除用户
const handleDelete = async row => {
  try {
    await ElMessageBox.confirm('是否确认删除该用户？', '警告', { type: 'warning' })
    await delUser(row.userId)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  try {
    await ElMessageBox.confirm('是否确认删除选中的用户？', '警告', { type: 'warning' })
    await delUser(selectedIds.value.join(','))
    ElMessage.success('批量删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 状态修改
const handleStatusChange = async row => {
  const text = row.status === '0' ? '启用' : '停用'
  try {
    await changeUserStatus(row.userId, row.status)
    ElMessage.success(`${text}成功`)
  } catch (error) {
    row.status = row.status === '0' ? '1' : '0'
    ElMessage.error(`${text}失败`)
  }
}

// 重置表单
const resetForm = () => {
  form.userId = null
  form.userName = ''
  form.nickName = ''
  form.phonenumber = ''
  form.email = ''
  form.password = ''
  form.status = '0'
  form.remark = ''
  formRef.value?.resetFields()
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()
    if (form.userId) {
      await updateUser(form)
      ElMessage.success('修改成功')
    } else {
      await addUser(form)
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
