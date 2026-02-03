<template>
  <div class="department-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h2>部门管理</h2>
        <p class="page-desc">管理组织架构部门、设置负责人和部门成员</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" :icon="Plus" @click="handleAddRoot">新增根部门</el-button>
        <el-button :icon="Sort" @click="expandAll">展开全部</el-button>
        <el-button :icon="DCaret" @click="collapseAll">收起全部</el-button>
      </div>
    </div>

    <!-- 主内容区：部门树 + 成员列表 -->
    <div class="department-content">
      <!-- 左侧：部门树 -->
      <el-card class="dept-tree-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>组织架构</span>
            <el-input
              v-model="filterText"
              placeholder="搜索部门"
              :prefix-icon="Search"
              size="small"
              clearable
              style="width: 150px"
            />
          </div>
        </template>
        <div v-loading="treeLoading" class="tree-container">
          <el-tree
            ref="treeRef"
            :data="departmentTree"
            :props="treeProps"
            :filter-node-method="filterNode"
            :expand-on-click-node="false"
            :highlight-current="true"
            node-key="id"
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <el-icon class="node-icon" :color="getIconColor(data.level)">
                  <OfficeBuilding />
                </el-icon>
                <span class="node-label">{{ node.label }}</span>
                <span class="node-count">({{ data.memberCount || 0 }})</span>
                <div class="node-actions" @click.stop>
                  <el-button
                    size="small"
                    text
                    :icon="Plus"
                    @click="handleAddChild(data)"
                  />
                  <el-button
                    size="small"
                    text
                    :icon="Edit"
                    @click="handleEdit(data)"
                  />
                  <el-button
                    size="small"
                    text
                    type="danger"
                    :icon="Delete"
                    @click="handleDelete(data)"
                  />
                </div>
              </div>
            </template>
          </el-tree>
        </div>
      </el-card>

      <!-- 右侧：部门详情和成员 -->
      <el-card class="dept-detail-card" shadow="never">
        <template v-if="currentDepartment">
          <!-- 部门信息头部 -->
          <div class="dept-header">
            <div class="dept-info">
              <el-icon class="dept-icon" :size="32" color="#0089FF">
                <OfficeBuilding />
              </el-icon>
              <div>
                <h3 class="dept-name">{{ currentDepartment.name }}</h3>
                <p class="dept-path">{{ currentDepartment.path }}</p>
              </div>
            </div>
            <div class="dept-actions">
              <el-button size="small" :icon="Edit" @click="handleEdit(currentDepartment)">
                编辑
              </el-button>
              <el-button size="small" :icon="User" @click="memberDialogVisible = true">
                添加成员
              </el-button>
            </div>
          </div>

          <!-- 负责人信息 -->
          <div v-if="currentDepartment.leader" class="leader-section">
            <div class="section-title">部门负责人</div>
            <div class="leader-card">
              <el-avatar :size="40" :src="currentDepartment.leaderAvatar">
                {{ currentDepartment.leaderName?.[0] || 'L' }}
              </el-avatar>
              <div class="leader-info">
                <div class="leader-name">{{ currentDepartment.leaderName }}</div>
                <div class="leader-id">ID: {{ currentDepartment.leaderId }}</div>
              </div>
              <el-button
                size="small"
                text
                type="primary"
                @click="handleChangeLeader"
              >
                更换负责人
              </el-button>
            </div>
          </div>

          <el-divider />

          <!-- 成员列表 -->
          <div class="members-section">
            <div class="section-header">
              <span class="section-title">部门成员 ({{ departmentMembers.length }})</span>
              <el-input
                v-model="memberSearch"
                placeholder="搜索成员"
                size="small"
                :prefix-icon="Search"
                clearable
                style="width: 150px"
              />
            </div>

            <div v-loading="membersLoading" class="members-list">
              <div
                v-for="member in filteredMembers"
                :key="member.id"
                class="member-item"
              >
                <el-avatar :size="36" :src="member.avatar">
                  {{ member.nickname?.[0] || 'U' }}
                </el-avatar>
                <div class="member-info">
                  <div class="member-name">{{ member.nickname }}</div>
                  <div class="member-position">{{ member.position || '员工' }}</div>
                </div>
                <el-tag v-if="member.id === currentDepartment.leaderId" size="small" type="warning">
                  负责人
                </el-tag>
                <el-dropdown trigger="click" @command="(cmd) => handleMemberAction(cmd, member)">
                  <el-button size="small" text :icon="MoreFilled" />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="setLeader">设为负责人</el-dropdown-item>
                      <el-dropdown-item command="remove" divided>移出部门</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <el-empty
                v-if="filteredMembers.length === 0"
                description="暂无成员"
                :image-size="60"
              />
            </div>
          </div>
        </template>

        <!-- 未选择部门时的空状态 -->
        <div v-else class="empty-dept">
          <el-empty description="请选择一个部门查看详情" :image-size="100" />
        </div>
      </el-card>
    </div>

    <!-- 编辑/新增部门对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="deptForm" :rules="deptRules" ref="deptFormRef" label-width="100px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="deptForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="deptForm.parentId"
            :data="departmentTreeForSelect"
            :props="{ label: 'name', value: 'id' }"
            :render-after-expand="false"
            check-strictly
            clearable
            placeholder="选择上级部门（不选则为根部门）"
          />
        </el-form-item>
        <el-form-item label="部门负责人" prop="leaderId">
          <el-select
            v-model="deptForm.leaderId"
            filterable
            remote
            :remote-method="searchUsers"
            placeholder="搜索用户选择负责人"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.nickname"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号" prop="sort">
          <el-input-number v-model="deptForm.sort" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="部门描述" prop="description">
          <el-input
            v-model="deptForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入部门描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="memberDialogVisible"
      title="添加部门成员"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="搜索用户">
          <el-select
            v-model="selectedMembersToAdd"
            multiple
            filterable
            remote
            :remote-method="searchUsersForAdd"
            placeholder="输入用户名搜索"
            :loading="searchUsersLoading"
            style="width: 100%"
          >
            <el-option
              v-for="user in userSearchResults"
              :key="user.id"
              :label="user.nickname"
              :value="user.id"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="user.avatar">{{ user.nickname?.[0] }}</el-avatar>
                <span>{{ user.nickname }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="selectedMembersToAdd.length === 0" @click="handleAddMembers">
          添加 ({{ selectedMembersToAdd.length }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Edit,
  Delete,
  OfficeBuilding,
  Sort,
  DCaret,
  User,
  MoreFilled
} from '@element-plus/icons-vue'
import {
  getDepartmentTree,
  getDepartmentDetail,
  getDepartmentMembers,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  setDepartmentLeader,
  moveDepartment,
  addDepartmentMembers,
  removeDepartmentMember
} from '@/api/admin'
import { searchUsersApi } from '@/api/im/user'

// 树形数据
const treeLoading = ref(false)
const departmentTree = ref([])
const treeRef = ref(null)
const filterText = ref('')

const treeProps = {
  children: 'children',
  label: 'name'
}

// 当前选中的部门
const currentDepartment = ref(null)

// 成员相关
const membersLoading = ref(false)
const departmentMembers = ref([])
const memberSearch = ref('')
const memberDialogVisible = ref(false)
const selectedMembersToAdd = ref([])

// 用户搜索
const userOptions = ref([])
const userSearchResults = ref([])
const searchUsersLoading = ref(false)

// 编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const deptFormRef = ref(null)
const deptForm = ref({
  name: '',
  parentId: null,
  leaderId: null,
  sort: 0,
  description: ''
})

const deptRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

// 计算属性
const departmentTreeForSelect = computed(() => {
  // 编辑时排除当前节点及其子节点，防止设置自己为父部门
  const excludeId = deptForm.value.id
  const excludeTree = (nodes, id) => {
    return nodes.filter(node => {
      if (node.id === id) return false
      if (node.children) {
        node.children = excludeTree(node.children, id)
      }
      return true
    })
  }
  return excludeId ? excludeTree(JSON.parse(JSON.stringify(departmentTree.value)), excludeId) : departmentTree.value
})

const filteredMembers = computed(() => {
  if (!memberSearch.value) return departmentMembers.value
  return departmentMembers.value.filter(m =>
    m.nickname?.toLowerCase().includes(memberSearch.value.toLowerCase())
  )
})

// 加载部门树
const loadDepartmentTree = async () => {
  treeLoading.value = true
  try {
    const res = await getDepartmentTree()
    if (res.code === 200) {
      departmentTree.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载部门树失败')
  } finally {
    treeLoading.value = false
  }
}

// 加载部门成员
const loadDepartmentMembers = async (deptId) => {
  membersLoading.value = true
  try {
    const res = await getDepartmentMembers(deptId)
    if (res.code === 200) {
      departmentMembers.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载部门成员失败')
  } finally {
    membersLoading.value = false
  }
}

// 节点点击
const handleNodeClick = async (data) => {
  const res = await getDepartmentDetail(data.id)
  if (res.code === 200) {
    currentDepartment.value = res.data
    loadDepartmentMembers(data.id)
  }
}

// 树形过滤
const filterNode = (value, data) => {
  if (!value) return true
  return data.name.includes(value)
}

watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

// 展开/收起
const expandAll = () => {
  const expandKeys = (nodes) => {
    const keys = []
    nodes.forEach(node => {
      keys.push(node.id)
      if (node.children) {
        keys.push(...expandKeys(node.children))
      }
    })
    return keys
  }
  const keys = expandKeys(departmentTree.value)
  keys.forEach(key => {
    treeRef.value?.store.nodesMap[key]?.expand()
  })
}

const collapseAll = () => {
  const allKeys = Object.keys(treeRef.value?.store.nodesMap || {})
  allKeys.forEach(key => {
    treeRef.value?.store.nodesMap[key]?.collapse()
  })
}

// 新增根部门
const handleAddRoot = () => {
  dialogTitle.value = '新增根部门'
  deptForm.value = {
    name: '',
    parentId: null,
    leaderId: null,
    sort: 0,
    description: ''
  }
  dialogVisible.value = true
}

// 新增子部门
const handleAddChild = (data) => {
  dialogTitle.value = '新增子部门'
  deptForm.value = {
    name: '',
    parentId: data.id,
    leaderId: null,
    sort: 0,
    description: ''
  }
  dialogVisible.value = true
}

// 编辑部门
const handleEdit = (data) => {
  dialogTitle.value = '编辑部门'
  deptForm.value = {
    id: data.id,
    name: data.name,
    parentId: data.parentId,
    leaderId: data.leaderId,
    sort: data.sort || 0,
    description: data.description || ''
  }
  // 加载负责人选项
  if (data.leaderId) {
    userOptions.value = [{ id: data.leaderId, nickname: data.leaderName }]
  }
  dialogVisible.value = true
}

// 删除部门
const handleDelete = async (data) => {
  const hasChildren = data.children && data.children.length > 0
  const hasMembers = data.memberCount > 0

  let message = '确定要删除该部门吗？'
  if (hasChildren) {
    message = '该部门包含子部门，删除后子部门将一并删除。'
  }
  if (hasMembers) {
    message += '该部门包含成员，删除后成员将转入根部门。'
  }

  try {
    await ElMessageBox.confirm(message, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteDepartment(data.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadDepartmentTree()
      if (currentDepartment.value?.id === data.id) {
        currentDepartment.value = null
        departmentMembers.value = []
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  await deptFormRef.value?.validate()
  const isEdit = !!deptForm.value.id
  const api = isEdit ? updateDepartment : createDepartment
  const data = { ...deptForm.value }
  delete data.id

  const res = await api(data.id || data, data)
  if (res.code === 200) {
    ElMessage.success(isEdit ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadDepartmentTree()
  }
}

// 更换负责人
const handleChangeLeader = async () => {
  ElMessage.info('请在编辑部门中更换负责人')
}

// 搜索用户
const searchUsers = async (query) => {
  if (!query) return
  try {
    const res = await searchUsersApi(query)
    if (res.code === 200) {
      userOptions.value = res.data || []
    }
  } catch (error) {
    console.error('搜索用户失败', error)
  }
}

const searchUsersForAdd = async (query) => {
  if (!query) return
  searchUsersLoading.value = true
  try {
    const res = await searchUsersApi(query)
    if (res.code === 200) {
      userSearchResults.value = res.data || []
    }
  } catch (error) {
    console.error('搜索用户失败', error)
  } finally {
    searchUsersLoading.value = false
  }
}

// 添加成员
const handleAddMembers = async () => {
  try {
    const userIds = selectedMembersToAdd.value.map(m => m.id)
    const res = await addDepartmentMembers(currentDepartment.value.id, userIds)
    if (res.code === 200) {
      ElMessage.success(`已添加 ${userIds.length} 名成员`)
      memberDialogVisible.value = false
      selectedMembersToAdd.value = []
      loadDepartmentMembers(currentDepartment.value.id)
    }
  } catch (error) {
    ElMessage.error('添加成员失败')
  }
}

// 成员操作
const handleMemberAction = async (command, member) => {
  switch (command) {
    case 'setLeader':
      await setDepartmentLeader(currentDepartment.value.id, member.id)
      ElMessage.success('已设置负责人')
      handleNodeClick(currentDepartment.value)
      break
    case 'remove':
      try {
        await ElMessageBox.confirm(`确定将 ${member.nickname} 移出该部门吗？`, '确认', {
          type: 'warning'
        })
        const res = await removeDepartmentMember(currentDepartment.value.id, member.id)
        if (res.code === 200) {
          ElMessage.success('已移除')
          loadDepartmentMembers(currentDepartment.value.id)
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('移除失败')
        }
      }
      break
  }
}

// 图标颜色
const getIconColor = (level) => {
  const colors = ['#0089FF', '#00C853', '#FFAB00', '#FF3D00', '#9C27B0']
  return colors[level % colors.length]
}

onMounted(() => {
  loadDepartmentTree()
})
</script>

<style scoped lang="scss">
/* 引入主题变量 */
@import '@/styles/admin-theme.scss';

/* ================================
   页面容器
   ================================ */
.department-management {
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
.department-content {
  display: flex;
  gap: var(--dt-space-md);
  height: calc(100vh - 180px);
}

/* 左侧部门树 */
.dept-tree-card {
  width: 320px;
  flex-shrink: 0;
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
  display: flex;
  flex-direction: column;
}

.dept-tree-card :deep(.el-card__body) {
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

.tree-container {
  height: 100%;
  overflow-y: auto;
}

/* 树节点 */
.tree-node {
  display: flex;
  align-items: center;
  gap: var(--dt-space-xs);
  flex: 1;
  padding-right: var(--dt-space-sm);
}

.node-icon {
  flex-shrink: 0;
}

.node-label {
  flex: 1;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-primary);
}

.node-count {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-placeholder);
}

.node-actions {
  display: none;
  gap: 2px;
}

.tree-node:hover .node-actions {
  display: flex;
}

/* 右侧详情 */
.dept-detail-card {
  flex: 1;
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.dept-detail-card :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
}

/* 部门头部 */
.dept-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--dt-space-md);
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  margin-bottom: var(--dt-space-md);
}

.dept-info {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
}

.dept-name {
  font-size: var(--dt-font-size-md);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.dept-path {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0;
}

.dept-actions {
  display: flex;
  gap: var(--dt-space-sm);
}

/* 负责人区域 */
.leader-section {
  margin-bottom: var(--dt-space-md);
}

.section-title {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin-bottom: var(--dt-space-sm);
}

.leader-card {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
  padding: var(--dt-space-md);
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
}

.leader-info {
  flex: 1;
}

.leader-name {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
}

.leader-id {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

/* 成员区域 */
.members-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--dt-space-sm);
}

.members-list {
  flex: 1;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  padding: var(--dt-space-sm) var(--dt-space-md);
  border-radius: var(--dt-radius-sm);
  transition: background-color var(--dt-transition-base);
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

.member-position {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
}

/* 空状态 */
.empty-dept {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 用户选项样式 */
.user-option {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

/* ================================
   响应式
   ================================ */
@media (max-width: 768px) {
  .department-content {
    flex-direction: column;
    height: auto;
  }

  .dept-tree-card {
    width: 100%;
    max-height: 300px;
  }
}
</style>
