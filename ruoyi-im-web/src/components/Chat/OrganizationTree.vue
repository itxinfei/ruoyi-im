<template>
  <div class="organization-tree-container">
    <!-- 搜索栏 -->
    <div
      v-if="showSearch"
      class="tree-search"
    >
      <el-input
        v-model="searchKeyword"
        placeholder="搜索部门或成员..."
        :prefix-icon="Search"
        clearable
        size="small"
        @input="handleSearch"
      />
    </div>

    <!-- 部门树 -->
    <el-tree
      ref="treeRef"
      v-loading="loading"
      :data="treeData"
      :props="treeProps"
      :expand-on-click-node="false"
      :highlight-current="true"
      :default-expand-all="false"
      :filter-node-method="filterNode"
      node-key="id"
      class="org-tree"
      @node-click="handleNodeClick"
      @node-expand="handleNodeExpand"
    >
      <template #default="{ node, data }">
        <div class="tree-node">
          <!-- 部门节点 -->
          <div
            v-if="data.type === 'department'"
            class="department-node"
          >
            <el-icon class="node-icon dept-icon">
              <Folder />
            </el-icon>
            <span class="node-label">{{ data.name }}</span>
            <span
              v-if="data.memberCount !== undefined"
              class="member-count"
            >
              ({{ data.memberCount }})
            </span>
          </div>

          <!-- 成员节点 -->
          <div
            v-else
            class="member-node"
          >
            <DingtalkAvatar
              :src="data.avatar"
              :name="data.name"
              :size="28"
              shape="circle"
              custom-class="tree-member-avatar"
            />
            <span class="node-label">{{ data.name }}</span>
            <span
              v-if="data.position"
              class="member-position"
            >{{ data.position }}</span>
          </div>
        </div>
      </template>
    </el-tree>

    <!-- 空状态 -->
    <div
      v-if="!loading && treeData.length === 0"
      class="empty-state"
    >
      <el-icon><FolderOpened /></el-icon>
      <span>{{ emptyText }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Search, Folder, FolderOpened } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getDepartmentTree, getDepartmentMembers } from '@/api/organization'

const props = defineProps({
  // 是否显示搜索
  showSearch: {
    type: Boolean,
    default: true
  },
  // 是否显示成员
  showMembers: {
    type: Boolean,
    default: true
  },
  // 是否默认展开所有节点
  defaultExpandAll: {
    type: Boolean,
    default: false
  },
  // 空状态文本
  emptyText: {
    type: String,
    default: '暂无组织架构'
  }
})

const emit = defineEmits(['department-click', 'member-click', 'loaded'])

const treeRef = ref(null)
const loading = ref(false)
const treeData = ref([])
const searchKeyword = ref('')
const memberCache = ref(new Map()) // 缓存部门成员

// 树节点配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 加载组织架构树
const loadTreeData = async () => {
  loading.value = true
  try {
    const res = await getDepartmentTree()
    if (res.data && res.data.length > 0) {
      treeData.value = buildTreeData(res.data)
    } else {
      treeData.value = []
    }
    emit('loaded', treeData.value)
  } catch (e) {
    ElMessage.error('加载组织架构失败')
    treeData.value = []
  } finally {
    loading.value = false
  }
}

// 构建树数据结构
const buildTreeData = departments => {
  return departments.map(dept => {
    const node = {
      id: dept.id,
      name: dept.name,
      type: 'department',
      memberCount: dept.memberCount || 0,
      children: dept.children ? buildTreeData(dept.children) : []
    }
    return node
  })
}

// 节点点击处理
const handleNodeClick = async (data, node) => {
  if (data.type === 'department') {
    emit('department-click', data)
    // 展开/收起时加载成员
    if (props.showMembers && data.memberCount > 0) {
      await loadDepartmentMembers(data, node)
    }
  } else if (data.type === 'member') {
    emit('member-click', data)
  }
}

// 节点展开处理
const handleNodeExpand = async (data, node) => {
  if (data.type === 'department' && props.showMembers) {
    await loadDepartmentMembers(data, node)
  }
}

// 加载部门成员
const loadDepartmentMembers = async (deptData, node) => {
  // 检查缓存
  if (memberCache.value.has(deptData.id)) {
    const members = memberCache.value.get(deptData.id)
    if (members.length > 0) {
      updateNodeChildren(deptData, node, members)
      return
    }
  }

  try {
    const res = await getDepartmentMembers(deptData.id)
    if (res.data && res.data.length > 0) {
      const members = res.data.map(m => ({
        id: `member-${m.userId || m.id}`,
        userId: m.userId || m.id,
        name: m.userName || m.name || '未知',
        avatar: m.avatar || '',
        position: m.position || '',
        type: 'member'
      }))
      // 缓存成员
      memberCache.value.set(deptData.id, members)
      // 更新节点子元素
      updateNodeChildren(deptData, node, members)
    }
  } catch (e) {
    // 静默失败
  }
}

// 更新节点子元素
const updateNodeChildren = (deptData, node, members) => {
  // 合并已有的子部门和成员
  const existingChildren = deptData.children || []
  const memberChildren = members.map(m => ({
    ...m,
    parent: deptData.id
  }))

  // 去重：如果成员已存在则不重复添加
  const existingMemberIds = new Set(
    existingChildren
      .filter(c => c.type === 'member')
      .map(c => c.userId)
  )
  const newMembers = memberChildren.filter(m => !existingMemberIds.has(m.userId))

  deptData.children = [...existingChildren, ...newMembers]

  // 更新树的数据
  if (treeRef.value) {
    treeRef.value.updateKeyChildren(deptData.id, deptData.children)
  }
}

// 搜索过滤
const filterNode = (value, data) => {
  if (!value) {return true}
  const keyword = value.toLowerCase()

  if (data.type === 'department') {
    // 部门名称匹配或成员名称匹配
    if (data.name.toLowerCase().includes(keyword)) {
      return true
    }
    // 检查子节点
    if (data.children && data.children.length > 0) {
      return data.children.some(child => filterNode(keyword, child))
    }
    return false
  } else if (data.type === 'member') {
    return data.name.toLowerCase().includes(keyword) ||
           (data.position && data.position.toLowerCase().includes(keyword))
  }
  return true
}

// 搜索处理
const handleSearch = value => {
  if (treeRef.value) {
    treeRef.value.filter(value)
  }
}

// 暴露方法：刷新树数据
const refresh = () => {
  memberCache.value.clear()
  loadTreeData()
}

// 暴露方法：展开指定节点
const expandNode = nodeKey => {
  if (treeRef.value) {
    treeRef.value.store.nodesMap[nodeKey]?.expand()
  }
}

// 暴露方法：收起指定节点
const collapseNode = nodeKey => {
  if (treeRef.value) {
    treeRef.value.store.nodesMap[nodeKey]?.collapse()
  }
}

// 暴露方法：展开所有节点
const expandAll = () => {
  if (treeRef.value) {
    const allKeys = treeRef.value.store.getAllData()
    const deptKeys = allKeys.filter(n => n.type === 'department').map(n => n.id)
    deptKeys.forEach(key => expandNode(key))
  }
}

// 暴露方法：收起所有节点
const collapseAll = () => {
  const allKeys = treeRef.value.store.getAllData()
  const deptKeys = allKeys.filter(n => n.type === 'department').map(n => n.id)
  deptKeys.forEach(key => collapseNode(key))
}

// 暴露方法给父组件
defineExpose({
  refresh,
  expandNode,
  collapseNode,
  expandAll,
  collapseAll
})

// 监听搜索关键字
watch(searchKeyword, val => {
  handleSearch(val)
})

// 初始化加载
loadTreeData()
</script>

<style scoped lang="scss">
// 钉钉颜色变量
$dt-blue: #3296FA;
$dt-text-primary: #1F2329;
$dt-text-secondary: #646A73;
$dt-text-tertiary: #8F959E;
$dt-bg-hover: #F5F5F5;
$dt-border-color: #E5E6EB;

.organization-tree-container {
  .tree-search {
    margin-bottom: 12px;
  }

  .org-tree {
    background: transparent;

    :deep(.el-tree-node) {
      position: relative;
    }

    :deep(.el-tree-node__content) {
      height: 36px;
      border-radius: var(--dt-radius-md);
      padding-right: 8px;
      transition: background 0.15s;

      &:hover {
        background: $dt-bg-hover;
      }
    }

    :deep(.el-tree-node__expand-icon) {
      color: $dt-text-secondary;
    }

    :deep(.el-tree-node.is-current > .el-tree-node__content) {
      background: var(--dt-brand-light);

      .tree-node {
        color: $dt-blue;
      }
    }
  }

  .tree-node {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    min-width: 0;

    .node-icon {
      font-size: 18px;
      color: $dt-text-tertiary;
      flex-shrink: 0;
    }

    .node-label {
      font-size: 14px;
      color: $dt-text-primary;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .member-count {
      font-size: 12px;
      color: $dt-text-tertiary;
      flex-shrink: 0;
    }

    // 部门节点
    .department-node {
      display: flex;
      align-items: center;
      gap: 8px;

      .dept-icon {
        color: #FAAD14;
      }
    }

    // 成员节点
    .member-node {
      display: flex;
      align-items: center;
      gap: 8px;

      :deep(.tree-member-avatar) {
        flex-shrink: 0;
      }

      .member-position {
        font-size: 12px;
        color: $dt-text-tertiary;
        flex-shrink: 0;
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: $dt-text-tertiary;

    .el-icon {
      font-size: 48px;
      margin-bottom: 12px;
    }

    span {
      font-size: 14px;
    }
  }
}

// 暗色模式适配
:global(.dark) {
  .org-tree {
    :deep(.el-tree-node__content) {
      &:hover {
        background: var(--dt-bg-hover-dark);
      }
    }

    :deep(.el-tree-node.is-current > .el-tree-node__content) {
      background: var(--dt-mention-bg-dark);
    }
  }

  .tree-node {
    .node-label {
      color: #e2e8f0;
    }

    .member-count,
    .member-position {
      color: #64748b;
    }
  }

  .empty-state {
    color: #64748b;
  }
}
</style>
