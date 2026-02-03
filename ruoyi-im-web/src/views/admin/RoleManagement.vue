<template>
  <div class="role-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h2>角色权限管理</h2>
        <p class="page-desc">管理系统角色、分配权限和角色成员</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
      </div>
    </div>

    <!-- 内容区域：角色列表 + 权限配置 -->
    <div class="role-content">
      <!-- 左侧：角色列表 -->
      <el-card class="role-list-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>角色列表</span>
            <el-input
              v-model="roleSearch"
              placeholder="搜索角色"
              :prefix-icon="Search"
              size="small"
              clearable
              style="width: 150px"
            />
          </div>
        </template>
        <div class="role-list">
          <div
            v-for="role in filteredRoles"
            :key="role.id"
            class="role-item"
            :class="{ active: currentRole?.id === role.id }"
            @click="handleSelectRole(role)"
          >
            <div class="role-icon" :style="{ background: role.color || getRoleColor(role.roleCode) }">
              <el-icon><User /></el-icon>
            </div>
            <div class="role-info">
              <div class="role-name">{{ role.roleName }}</div>
              <div class="role-code">{{ role.roleCode }}</div>
            </div>
            <div class="role-badge">
              <el-tag v-if="role.builtin" size="small" type="info">系统</el-tag>
            </div>
          </div>

          <el-empty v-if="filteredRoles.length === 0" description="暂无角色" :image-size="60" />
        </div>
      </el-card>

      <!-- 右侧：角色详情 -->
      <el-card class="role-detail-card" shadow="never">
        <template v-if="currentRole">
          <!-- 角色头部 -->
          <div class="role-header">
            <div class="role-icon-large" :style="{ background: currentRole.color || getRoleColor(currentRole.roleCode) }">
              <el-icon :size="24"><User /></el-icon>
            </div>
            <div class="role-header-info">
              <h3 class="role-header-name">{{ currentRole.roleName }}</h3>
              <p class="role-header-code">{{ currentRole.roleCode }}</p>
              <p v-if="currentRole.description" class="role-header-desc">{{ currentRole.description }}</p>
            </div>
            <div class="role-header-actions">
              <el-button v-if="!currentRole.builtin" size="small" :icon="Edit" @click="handleEdit(currentRole)">
                编辑
              </el-button>
              <el-button size="small" :icon="User" @click="memberDrawerVisible = true">
                成员 ({{ currentRole.memberCount || 0 }})
              </el-button>
            </div>
          </div>

          <el-tabs v-model="activeTab" class="role-tabs">
            <!-- 权限配置 -->
            <el-tab-pane label="权限配置" name="permissions">
              <div class="permissions-content">
                <div class="permission-header">
                  <span class="permission-title">菜单权限</span>
                  <div class="permission-actions">
                    <el-button size="small" :icon="Check" @click="handleExpandAll">展开全部</el-button>
                    <el-button size="small" @click="handleCollapseAll">收起全部</el-button>
                    <el-button
                      size="small"
                      type="primary"
                      :icon="Select"
                      @click="handleSavePermissions"
                      :disabled="currentRole.builtin"
                    >
                      保存权限
                    </el-button>
                  </div>
                </div>

                <div class="permission-tree">
                  <el-tree
                    ref="permissionTreeRef"
                    :data="permissionTree"
                    :props="permissionProps"
                    :default-checked-keys="checkedPermissions"
                    node-key="id"
                    show-checkbox
                    default-expand-all
                  >
                    <template #default="{ node, data }">
                      <div class="permission-node">
                        <el-icon v-if="data.icon">
                          <component :is="data.icon" />
                        </el-icon>
                        <span class="permission-label">{{ node.label }}</span>
                        <el-tag v-if="data.type === 'button'" size="small" type="info">按钮</el-tag>
                      </div>
                    </template>
                  </el-tree>
                </div>

                <!-- 数据权限 -->
                <div class="data-permission-section">
                  <div class="section-title">数据权限范围</div>
                  <el-radio-group v-model="dataScope" :disabled="currentRole.builtin">
                    <el-radio label="all">全部数据</el-radio>
                    <el-radio label="dept">本部门数据</el-radio>
                    <el-radio label="deptAndSub">本部门及子部门数据</el-radio>
                    <el-radio label="self">仅本人数据</el-radio>
                  </el-radio-group>
                </div>
              </div>
            </el-tab-pane>

            <!-- 角色信息 -->
            <el-tab-pane label="基本信息" name="info">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="角色ID">{{ currentRole.id }}</el-descriptions-item>
                <el-descriptions-item label="角色编码">{{ currentRole.roleCode }}</el-descriptions-item>
                <el-descriptions-item label="角色名称">{{ currentRole.roleName }}</el-descriptions-item>
                <el-descriptions-item label="系统角色">
                  <el-tag :type="currentRole.builtin ? 'success' : 'info'" size="small">
                    {{ currentRole.builtin ? '是' : '否' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="成员数量">{{ currentRole.memberCount || 0 }} 人</el-descriptions-item>
                <el-descriptions-item label="数据范围">{{ currentRole.dataScopeDesc || '-' }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ currentRole.createTime }}</el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">
                  {{ currentRole.description || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
          </el-tabs>
        </template>

        <!-- 未选择角色时的空状态 -->
        <div v-else class="empty-role">
          <el-empty description="请选择一个角色查看详情" :image-size="100" />
        </div>
      </el-card>
    </div>

    <!-- 编辑/新增角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="roleForm.roleCode"
            placeholder="请输入角色编码，如：ROLE_EDITOR"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope" v-if="!isEdit">
          <el-select v-model="roleForm.dataScope" placeholder="请选择数据范围" style="width: 100%">
            <el-option label="全部数据" :value="1" />
            <el-option label="本部门数据" :value="2" />
            <el-option label="本部门及子部门数据" :value="3" />
            <el-option label="仅本人数据" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示颜色" prop="color">
          <el-color-picker v-model="roleForm.color" show-alpha />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="roleForm.sortOrder" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 成员抽屉 -->
    <el-drawer
      v-model="memberDrawerVisible"
      title="角色成员"
      size="400px"
      @open="loadRoleMembers"
    >
      <template v-if="currentRole">
        <div class="member-drawer-header">
          <div class="member-drawer-role">
            <div class="role-icon-small" :style="{ background: currentRole.color || getRoleColor(currentRole.roleCode) }">
              <el-icon><User /></el-icon>
            </div>
            <div>
              <h4>{{ currentRole.roleName }}</h4>
              <p>{{ roleMembers.length }} 位成员</p>
            </div>
          </div>
          <el-button type="primary" size="small" :icon="Plus" @click="addMemberDialogVisible = true">
            添加成员
          </el-button>
        </div>

        <div v-loading="membersLoading" class="member-list">
          <div v-for="member in roleMembers" :key="member.id" class="member-item">
            <el-avatar :size="40" :src="member.avatar">
              {{ member.nickname?.[0] || 'U' }}
            </el-avatar>
            <div class="member-info">
              <div class="member-name">{{ member.nickname || member.username }}</div>
              <div class="member-dept">{{ member.department || '未分配部门' }}</div>
            </div>
            <el-button
              size="small"
              text
              type="danger"
              :icon="Close"
              @click="handleRemoveMember(member)"
            >
              移除
            </el-button>
          </div>

          <el-empty v-if="roleMembers.length === 0" description="暂无成员" :image-size="60" />
        </div>
      </template>
    </el-drawer>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="addMemberDialogVisible"
      title="添加成员到角色"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="搜索用户">
          <el-select
            v-model="selectedUsersToAdd"
            multiple
            filterable
            remote
            :remote-method="searchUsers"
            placeholder="输入用户名搜索"
            :loading="searchUsersLoading"
            style="width: 100%"
          >
            <el-option
              v-for="user in userSearchResults"
              :key="user.id"
              :label="user.nickname || user.username"
              :value="user.id"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="user.avatar">{{ (user.nickname || user.username)?.[0] }}</el-avatar>
                <span>{{ user.nickname || user.username }}</span>
                <span class="user-dept">{{ user.department || '' }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="selectedUsersToAdd.length === 0" @click="handleAddMembers">
          添加 ({{ selectedUsersToAdd.length }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Edit,
  Delete,
  User,
  Check,
  Select,
  Close,
  // 菜单图标
  Monitor,
  ChatDotRound,
  ChatLineSquare,
  Setting,
  Document,
  Folder,
  Files
} from '@element-plus/icons-vue'
import {
  getRoleList,
  getRoleDetail,
  createRole,
  updateRole,
  deleteRole,
  getPermissionList,
  assignRolePermissions,
  getRoleMembers,
  addRoleMembers,
  removeRoleMember
} from '@/api/admin'
import { searchUsersApi, getUserInfo } from '@/api/im/user'

// 角色列表
const roles = ref([])
const roleSearch = ref('')
const currentRole = ref(null)
const activeTab = ref('permissions')

const filteredRoles = computed(() => {
  if (!roleSearch.value) return roles.value
  return roles.value.filter(r =>
    r.roleName?.toLowerCase().includes(roleSearch.value.toLowerCase()) ||
    r.roleCode?.toLowerCase().includes(roleSearch.value.toLowerCase())
  )
})

// 权限树
const permissionTree = ref([])
const permissionTreeRef = ref(null)
const checkedPermissions = ref([])
const dataScope = ref('all')
const permissionProps = {
  children: 'children',
  label: 'permissionName'
}

// 成员相关
const memberDrawerVisible = ref(false)
const addMemberDialogVisible = ref(false)
const membersLoading = ref(false)
const roleMembers = ref([])
const selectedUsersToAdd = ref([])
const userSearchResults = ref([])
const searchUsersLoading = ref(false)

// 编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const roleFormRef = ref(null)
const isEdit = ref(false)
const roleForm = ref({
  roleName: '',
  roleCode: '',
  color: '',
  sortOrder: 0,
  dataScope: 1,
  description: ''
})

const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

// 加载角色列表
const loadRoleList = async () => {
  const res = await getRoleList({ pageNum: 1, pageSize: 100 })
  if (res.code === 200) {
    roles.value = res.data?.list || []
    // 内置角色颜色
    roles.value.forEach(role => {
      if (!role.color) {
        role.color = getRoleColor(role.roleCode)
      }
    })
  }
}

// 加载权限树
const loadPermissionTree = async () => {
  const res = await getPermissionList()
  if (res.code === 200) {
    // 后端已经返回树形结构，直接使用
    permissionTree.value = res.data || []
  }
}

// 选择角色
const handleSelectRole = async (role) => {
  const res = await getRoleDetail(role.id)
  if (res.code === 200) {
    currentRole.value = res.data
    checkedPermissions.value = res.data.permissionIds || []
    // 数据范围值转换为前端枚举
    const scopeMap = { 1: 'all', 2: 'dept', 3: 'deptAndSub', 4: 'self' }
    dataScope.value = scopeMap[res.data.dataScope] || 'all'
    activeTab.value = 'permissions'
  }
}

// 新增角色
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  isEdit.value = false
  roleForm.value = {
    roleName: '',
    roleCode: '',
    color: '#' + Math.floor(Math.random()*16777215).toString(16),
    sortOrder: 0,
    dataScope: 1,
    description: ''
  }
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (role) => {
  dialogTitle.value = '编辑角色'
  isEdit.value = true
  roleForm.value = {
    id: role.id,
    roleName: role.roleName,
    roleCode: role.roleCode,
    color: role.color,
    sortOrder: role.sortOrder || 0,
    dataScope: role.dataScope || 1,
    description: role.description || ''
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  await roleFormRef.value?.validate()
  const data = { ...roleForm.value }

  if (isEdit.value) {
    const res = await updateRole(data.id, data)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      loadRoleList()
      // 更新当前选中角色
      if (currentRole.value?.id === data.id) {
        handleSelectRole(currentRole.value)
      }
    }
  } else {
    const res = await createRole(data)
    if (res.code === 200) {
      ElMessage.success('创建成功')
      dialogVisible.value = false
      loadRoleList()
    }
  }
}

// 展开/收起权限树
const handleExpandAll = () => {
  const nodes = permissionTreeRef.value?.store.nodesMap
  Object.values(nodes || {}).forEach(node => node.expand())
}

const handleCollapseAll = () => {
  const nodes = permissionTreeRef.value?.store.nodesMap
  Object.values(nodes || {}).forEach(node => node.collapse())
}

// 保存权限
const handleSavePermissions = async () => {
  const checkedKeys = permissionTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = permissionTreeRef.value?.getHalfCheckedKeys() || []
  const allPermissions = [...checkedKeys, ...halfCheckedKeys]

  const res = await assignRolePermissions(currentRole.value.id, allPermissions)
  if (res.code === 200) {
    ElMessage.success('权限保存成功')
    checkedPermissions.value = allPermissions
  }
}

// 加载角色成员
const loadRoleMembers = async () => {
  if (!currentRole.value) return
  membersLoading.value = true
  try {
    const res = await getRoleMembers(currentRole.value.id)
    if (res.code === 200) {
      const userIds = res.data || []
      // 根据用户ID列表获取完整用户信息 (使用 Promise.all 并发请求)
      if (userIds.length > 0) {
        const userPromises = userIds.map(id => getUserInfo(id))
        const results = await Promise.all(userPromises)
        roleMembers.value = results
          .filter(r => r.code === 200)
          .map(r => r.data)
      } else {
        roleMembers.value = []
      }
    }
  } finally {
    membersLoading.value = false
  }
}

// 搜索用户
const searchUsers = async (query) => {
  if (!query) return
  searchUsersLoading.value = true
  try {
    const res = await searchUsersApi(query)
    if (res.code === 200) {
      userSearchResults.value = res.data || []
    }
  } finally {
    searchUsersLoading.value = false
  }
}

// 添加成员
const handleAddMembers = async () => {
  if (selectedUsersToAdd.value.length === 0) {
    ElMessage.warning('请选择要添加的用户')
    return
  }
  try {
    const res = await addRoleMembers(currentRole.value.id, selectedUsersToAdd.value)
    if (res.code === 200) {
      ElMessage.success(`已添加 ${selectedUsersToAdd.value.length} 名成员`)
      addMemberDialogVisible.value = false
      selectedUsersToAdd.value = []
      loadRoleMembers()
    }
  } catch (error) {
    ElMessage.error('添加成员失败')
  }
}

// 移除成员
const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要将 ${member.nickname || member.username} 从该角色中移除吗？`, '确认', {
      type: 'warning'
    })
    const res = await removeRoleMember(currentRole.value.id, member.id)
    if (res.code === 200) {
      ElMessage.success('已移除')
      loadRoleMembers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

// 获取角色颜色
const getRoleColor = (code) => {
  const colors = {
    SUPER_ADMIN: '#FF3D00',
    ADMIN: '#0089FF',
    USER: '#00C853',
    EDITOR: '#9C27B0',
    AUDITOR: '#FFAB00'
  }
  return colors[code] || '#607D8B'
}

onMounted(() => {
  loadRoleList()
  loadPermissionTree()
})
</script>

<style scoped lang="scss">


/* ================================
   页面容器
   ================================ */
.role-management {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--dt-space-md);
}

.page-title h2 {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0;
}

.page-actions {
  display: flex;
  gap: var(--dt-space-sm);
}

/* ================================
   主内容区
   ================================ */
.role-content {
  display: flex;
  gap: var(--dt-space-md);
  height: calc(100vh - 180px);
}

/* 左侧角色列表 */
.role-list-card {
  width: 280px;
  flex-shrink: 0;
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
  display: flex;
  flex-direction: column;
}

.role-list-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: var(--dt-space-sm);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
}

.role-list {
  height: 100%;
  overflow-y: auto;
}

.role-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  padding: var(--dt-space-sm);
  margin-bottom: var(--dt-space-xs);
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  transition: background-color var(--dt-transition-base);
}

.role-item:hover {
  background: var(--dt-bg-hover);
}

.role-item.active {
  background: var(--dt-bg-active);
}

.role-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.role-info {
  flex: 1;
  min-width: 0;
}

.role-name {
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-code {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧详情 */
.role-detail-card {
  flex: 1;
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.role-detail-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 角色头部 */
.role-header {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
  padding: var(--dt-space-md);
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  margin-bottom: var(--dt-space-md);
}

.role-icon-large {
  width: 56px;
  height: 56px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.role-header-info {
  flex: 1;
}

.role-header-name {
  font-size: var(--dt-font-size-md);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.role-header-code {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0 0 4px 0;
}

.role-header-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
  margin: 0;
}

.role-header-actions {
  display: flex;
  gap: var(--dt-space-sm);
}

/* 标签页 */
.role-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.role-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
}

.role-tabs :deep(.el-tab-pane) {
  height: 100%;
}

/* 权限配置 */
.permissions-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.permission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--dt-space-md);
}

.permission-title {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.permission-actions {
  display: flex;
  gap: var(--dt-space-xs);
}

.permission-tree {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-space-sm);
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
}

.permission-node {
  display: flex;
  align-items: center;
  gap: var(--dt-space-xs);
  flex: 1;
}

.permission-label {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
}

/* 数据权限 */
.data-permission-section {
  margin-top: var(--dt-space-md);
  padding: var(--dt-space-md);
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
}

.section-title {
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin-bottom: var(--dt-space-sm);
}

/* 空状态 */
.empty-role {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 成员抽屉 */
.member-drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--dt-space-md);
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-sm);
  margin-bottom: var(--dt-space-md);
}

.member-drawer-role {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

.role-icon-small {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.member-drawer-role h4 {
  margin: 0;
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
}

.member-drawer-role p {
  margin: 0;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

.member-list {
  max-height: calc(100vh - 250px);
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  padding: var(--dt-space-sm);
  border-radius: var(--dt-radius-sm);
  margin-bottom: var(--dt-space-xs);
}

.member-item:hover {
  background: var(--dt-bg-hover);
}

.member-info {
  flex: 1;
}

.member-name {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-primary);
}

.member-dept {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}

/* 用户选项 */
.user-option {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

.user-dept {
  margin-left: auto;
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}

/* ================================
   响应式
   ================================ */
@media (max-width: 768px) {
  .role-content {
    flex-direction: column;
    height: auto;
  }

  .role-list-card {
    width: 100%;
    max-height: 300px;
  }
}
</style>
