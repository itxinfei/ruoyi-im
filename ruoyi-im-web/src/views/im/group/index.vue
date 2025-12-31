<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
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
      <el-form-item label="群主" prop="ownerName">
        <el-input
          v-model="queryParams.ownerName"
          placeholder="请输入群主名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="群组状态" clearable style="width: 150px">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
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
        <el-button type="primary" plain :icon="Plus" @click="handleAdd">新增群组</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >批量删除</el-button>
      </el-col>
    </el-row>

    <!-- 群组列表 -->
    <el-table v-loading="loading" :data="groupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="群组ID" align="center" prop="groupId" width="100" />
      <el-table-column label="群组头像" align="center" width="80">
        <template #default="scope">
          <el-avatar :size="40" :src="scope.row.avatar">
            {{ scope.row.groupName?.charAt(0) }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="群组名称" align="left" prop="groupName" min-width="150" show-overflow-tooltip />
      <el-table-column label="群主" align="center" prop="ownerName" width="120" />
      <el-table-column label="成员数量" align="center" prop="memberCount" width="100">
        <template #default="scope">
          <el-tag type="info">{{ scope.row.memberCount || 0 }}人</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' || scope.row.status === 0 ? 'success' : 'danger'">
            {{ scope.row.status === '0' || scope.row.status === 0 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="240" fixed="right">
        <template #default="scope">
          <el-button link type="primary" :icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button link type="primary" :icon="User" @click="handleMembers(scope.row)">成员</el-button>
          <el-button link type="primary" :icon="Setting" @click="handleSettings(scope.row)">设置</el-button>
          <el-button link type="danger" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 新增/编辑群组对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="群组名称" prop="groupName">
          <el-input v-model="formData.groupName" placeholder="请输入群组名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="群组头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-avatar v-if="formData.avatar" :size="80" :src="formData.avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="群组简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入群组简介"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 成员管理对话框 -->
    <el-dialog v-model="memberDialogVisible" title="群组成员" width="600px" destroy-on-close>
      <div class="member-dialog-content">
        <div class="member-header">
          <el-input
            v-model="memberSearchText"
            placeholder="搜索成员"
            style="width: 200px"
            clearable
          />
          <el-button type="primary" :icon="Plus" @click="handleAddMember">添加成员</el-button>
        </div>
        <el-table :data="filteredMembers" max-height="400">
          <el-table-column label="成员" min-width="150">
            <template #default="scope">
              <div class="member-info">
                <el-avatar :size="32" :src="scope.row.avatar">
                  {{ scope.row.nickname?.charAt(0) }}
                </el-avatar>
                <span class="member-name">{{ scope.row.nickname || scope.row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="角色" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getMemberRoleType(scope.row.role)">
                {{ getMemberRoleLabel(scope.row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="加入时间" width="160" align="center" prop="joinTime" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button
                v-if="scope.row.role !== 'owner'"
                link
                type="danger"
                @click="handleRemoveMember(scope.row)"
              >移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 群组设置对话框 -->
    <el-dialog v-model="settingsDialogVisible" title="群组设置" width="500px" destroy-on-close>
      <el-form :model="settingsForm" label-width="120px">
        <el-form-item label="入群验证">
          <el-switch v-model="settingsForm.joinApproval" />
          <span class="form-tip">开启后，新成员需要管理员审批</span>
        </el-form-item>
        <el-form-item label="全员禁言">
          <el-switch v-model="settingsForm.muteAll" />
          <span class="form-tip">开启后，只有管理员可以发言</span>
        </el-form-item>
        <el-form-item label="允许成员邀请">
          <el-switch v-model="settingsForm.allowMemberInvite" />
          <span class="form-tip">开启后，普通成员也可以邀请新成员</span>
        </el-form-item>
        <el-form-item label="群公告">
          <el-input
            v-model="settingsForm.announcement"
            type="textarea"
            :rows="4"
            placeholder="请输入群公告"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="settingsLoading" @click="handleSaveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * @file 群组管理页面
 * @description IM系统群组管理功能
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Delete,
  Edit,
  User,
  Setting,
} from '@element-plus/icons-vue'
import { listGroup, getGroup, addGroup, updateGroup, delGroup, getGroupSettings, updateGroupSettings } from '@/api/im/group'
import { getToken } from '@/utils/auth'

const router = useRouter()

// ==================== 响应式状态 ====================

/** 加载状态 */
const loading = ref(false)
const submitLoading = ref(false)
const settingsLoading = ref(false)

/** 表单引用 */
const queryFormRef = ref(null)
const formRef = ref(null)

/** 群组列表 */
const groupList = ref([])

/** 总数 */
const total = ref(0)

/** 选中的ID列表 */
const selectedIds = ref([])

/** 查询参数 */
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  groupName: '',
  ownerName: '',
  status: '',
})

/** 状态选项 */
const statusOptions = [
  { label: '正常', value: '0' },
  { label: '禁用', value: '1' },
]

/** 对话框控制 */
const dialogVisible = ref(false)
const dialogTitle = ref('')
const memberDialogVisible = ref(false)
const settingsDialogVisible = ref(false)

/** 表单数据 */
const formData = reactive({
  groupId: null,
  groupName: '',
  avatar: '',
  description: '',
  status: '0',
})

/** 表单校验规则 */
const formRules = {
  groupName: [
    { required: true, message: '请输入群组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
  ],
}

/** 成员相关 */
const currentGroup = ref(null)
const memberList = ref([])
const memberSearchText = ref('')

/** 设置表单 */
const settingsForm = reactive({
  groupId: null,
  joinApproval: false,
  muteAll: false,
  allowMemberInvite: true,
  announcement: '',
})

// ==================== 计算属性 ====================

/** 上传地址 */
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_BASE_API || ''
  return `${baseUrl}/api/im/file/upload`
})

/** 上传请求头 */
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken(),
}))

/** 过滤后的成员列表 */
const filteredMembers = computed(() => {
  if (!memberSearchText.value) return memberList.value
  const keyword = memberSearchText.value.toLowerCase()
  return memberList.value.filter(m => {
    const nickname = m.nickname || ''
    const username = m.username || ''
    return nickname.toLowerCase().includes(keyword) || username.toLowerCase().includes(keyword)
  })
})

// ==================== 方法定义 ====================

/**
 * 获取群组列表
 */
const getList = async () => {
  loading.value = true
  try {
    const response = await listGroup(queryParams)
    if (response.code === 200) {
      groupList.value = response.rows || response.data?.list || []
      total.value = response.total || response.data?.total || 0
    } else {
      // 模拟数据用于演示
      groupList.value = [
        { groupId: '1', groupName: '技术交流群', ownerName: '管理员', memberCount: 128, status: '0', createTime: '2024-01-15 10:30:00' },
        { groupId: '2', groupName: '产品讨论组', ownerName: '张三', memberCount: 45, status: '0', createTime: '2024-01-14 15:20:00' },
        { groupId: '3', groupName: '项目协作群', ownerName: '李四', memberCount: 32, status: '1', createTime: '2024-01-13 09:00:00' },
      ]
      total.value = 3
    }
  } catch (error) {
    console.error('获取群组列表失败:', error)
    // 使用模拟数据
    groupList.value = [
      { groupId: '1', groupName: '技术交流群', ownerName: '管理员', memberCount: 128, status: '0', createTime: '2024-01-15 10:30:00' },
      { groupId: '2', groupName: '产品讨论组', ownerName: '张三', memberCount: 45, status: '0', createTime: '2024-01-14 15:20:00' },
    ]
    total.value = 2
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

/**
 * 重置查询
 */
const resetQuery = () => {
  queryParams.groupName = ''
  queryParams.ownerName = ''
  queryParams.status = ''
  handleQuery()
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  getList()
}

/**
 * 页码变化
 */
const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  getList()
}

/**
 * 多选框选中
 */
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.groupId)
}

/**
 * 新增群组
 */
const handleAdd = () => {
  dialogTitle.value = '新增群组'
  formData.groupId = null
  formData.groupName = ''
  formData.avatar = ''
  formData.description = ''
  formData.status = '0'
  dialogVisible.value = true
}

/**
 * 编辑群组
 */
const handleUpdate = async (row) => {
  dialogTitle.value = '编辑群组'
  try {
    const response = await getGroup(row.groupId)
    if (response.code === 200 && response.data) {
      Object.assign(formData, response.data)
    } else {
      Object.assign(formData, row)
    }
  } catch {
    Object.assign(formData, row)
  }
  dialogVisible.value = true
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate()
  submitLoading.value = true

  try {
    if (formData.groupId) {
      await updateGroup(formData)
      ElMessage.success('修改成功')
    } else {
      await addGroup(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

/**
 * 删除群组
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除群组"${row.groupName}"吗？`, '警告', {
      type: 'warning',
    })
    await delGroup(row.groupId)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '网络错误'))
    }
  }
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的群组')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除选中的群组吗？', '警告', {
      type: 'warning',
    })
    for (const id of selectedIds.value) {
      await delGroup(id)
    }
    ElMessage.success('批量删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + (error.message || '网络错误'))
    }
  }
}

/**
 * 查看成员
 */
const handleMembers = (row) => {
  currentGroup.value = row
  // 模拟成员数据
  memberList.value = [
    { id: '1', nickname: '管理员', username: 'admin', role: 'owner', joinTime: '2024-01-01 10:00:00' },
    { id: '2', nickname: '张三', username: 'zhangsan', role: 'admin', joinTime: '2024-01-02 14:30:00' },
    { id: '3', nickname: '李四', username: 'lisi', role: 'member', joinTime: '2024-01-03 09:15:00' },
    { id: '4', nickname: '王五', username: 'wangwu', role: 'member', joinTime: '2024-01-04 16:45:00' },
  ]
  memberDialogVisible.value = true
}

/**
 * 添加成员
 */
const handleAddMember = () => {
  ElMessage.info('添加成员功能开发中...')
}

/**
 * 移除成员
 */
const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员"${member.nickname}"吗？`, '确认', {
      type: 'warning',
    })
    const index = memberList.value.findIndex(m => m.id === member.id)
    if (index > -1) {
      memberList.value.splice(index, 1)
      ElMessage.success('移除成功')
    }
  } catch {
    // 取消操作
  }
}

/**
 * 获取成员角色类型
 */
const getMemberRoleType = (role) => {
  const types = {
    owner: 'danger',
    admin: 'warning',
    member: 'info',
  }
  return types[role] || 'info'
}

/**
 * 获取成员角色标签
 */
const getMemberRoleLabel = (role) => {
  const labels = {
    owner: '群主',
    admin: '管理员',
    member: '成员',
  }
  return labels[role] || '成员'
}

/**
 * 群组设置
 */
const handleSettings = async (row) => {
  currentGroup.value = row
  settingsForm.groupId = row.groupId

  try {
    const response = await getGroupSettings(row.groupId)
    if (response.code === 200 && response.data) {
      settingsForm.joinApproval = response.data.joinApproval || false
      settingsForm.muteAll = response.data.muteAll || false
      settingsForm.allowMemberInvite = response.data.allowMemberInvite !== false
      settingsForm.announcement = response.data.announcement || ''
    }
  } catch {
    // 使用默认值
    settingsForm.joinApproval = false
    settingsForm.muteAll = false
    settingsForm.allowMemberInvite = true
    settingsForm.announcement = ''
  }

  settingsDialogVisible.value = true
}

/**
 * 保存设置
 */
const handleSaveSettings = async () => {
  settingsLoading.value = true
  try {
    await updateGroupSettings(settingsForm)
    ElMessage.success('设置保存成功')
    settingsDialogVisible.value = false
  } catch (error) {
    ElMessage.error('保存失败：' + (error.message || '网络错误'))
  } finally {
    settingsLoading.value = false
  }
}

/**
 * 头像上传成功
 */
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    formData.avatar = response.data?.url || response.url
  } else {
    ElMessage.error('头像上传失败')
  }
}

/**
 * 头像上传前校验
 */
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  return time
}

// ==================== 生命周期 ====================

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

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;
    width: 80px;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      border-color: #409eff;
    }
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
  }
}

.member-dialog-content {
  .member-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .member-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .member-name {
      font-size: 14px;
    }
  }
}

.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}
</style>
