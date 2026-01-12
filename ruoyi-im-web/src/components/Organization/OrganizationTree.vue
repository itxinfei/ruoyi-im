<template>
  <div class="organization-tree">
    <!-- 搜索框 -->
    <div v-if="showSearch" class="tree-search">
      <el-input
        v-model="searchText"
        :placeholder="searchPlaceholder"
        :prefix-icon="Search"
        clearable
        size="small"
        @input="handleSearch"
      />
    </div>

    <!-- 树形控件 -->
    <el-tree
      ref="treeRef"
      v-loading="loading"
      :data="treeData"
      :props="treeProps"
      :node-key="nodeKey"
      :default-expand-all="defaultExpandAll"
      :expand-on-click-node="expandOnClickNode"
      :highlight-current="highlightCurrent"
      :filter-node-method="filterNode"
      :current-node-key="currentNodeKey"
      :draggable="draggable"
      :allow-drop="allowDrop"
      :allow-drag="allowDrag"
      @node-click="handleNodeClick"
      @node-expand="handleNodeExpand"
      @node-collapse="handleNodeCollapse"
      @node-drag-start="handleDragStart"
      @node-drag-end="handleDragEnd"
      @node-drop="handleDrop"
      class="dept-tree"
    >
      <template #default="{ node, data }">
        <div class="tree-node">
          <!-- 节点图标 -->
          <span class="node-icon">
            <el-icon v-if="data.isDept"><OfficeBuilding /></el-icon>
            <el-avatar
              v-else
              :size="24"
              :src="data.avatar"
            >
              {{ data.name?.charAt(0) }}
            </el-avatar>
          </span>

          <!-- 节点内容 -->
          <span class="node-label" :title="node.label">
            {{ node.label }}
          </span>

          <!-- 部门统计 -->
          <span v-if="data.isDept && showMemberCount" class="node-count">
            ({{ data.memberCount || 0 }})
          </span>

          <!-- 在线状态 -->
          <span v-if="!data.isDept && showOnlineStatus" class="node-status">
            <span
              class="status-dot"
              :class="{
                online: data.onlineStatus === 'online',
                busy: data.onlineStatus === 'busy',
                away: data.onlineStatus === 'away'
              }"
            ></span>
          </span>

          <!-- 节点操作 -->
          <span v-if="showActions && !node.expanded" class="node-actions" @click.stop>
            <el-dropdown trigger="click" @command="handleAction($event, data)">
              <el-icon class="action-icon"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="data.isDept && canAddDept"
                    command="add-dept"
                  >
                    <el-icon><Plus /></el-icon>
                    添加子部门
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="data.isDept && canAddMember"
                    command="add-member"
                  >
                    <el-icon><UserFilled /></el-icon>
                    添加成员
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="!data.isDept"
                    command="send-message"
                  >
                    <el-icon><ChatDotRound /></el-icon>
                    发送消息
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="data.isDept && canEdit"
                    command="edit"
                  >
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="data.isDept && canDelete && !data.hasChildren"
                    command="delete"
                    divided
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </span>
        </div>
      </template>
    </el-tree>

    <!-- 空状态 -->
    <div v-if="!loading && treeData.length === 0" class="tree-empty">
      <el-icon :size="48"><Box /></el-icon>
      <p>暂无组织架构</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import {
  Search,
  OfficeBuilding,
  UserFilled,
  ChatDotRound,
  Edit,
  Delete,
  Plus,
  MoreFilled,
  Box,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDepartmentTree, getDepartmentMembers } from '@/api/im/organization'

const props = defineProps({
  // 是否显示搜索框
  showSearch: {
    type: Boolean,
    default: true,
  },
  // 搜索占位符
  searchPlaceholder: {
    type: String,
    default: '搜索部门或成员',
  },
  // 树节点配置
  treeProps: {
    type: Object,
    default: () => ({
      children: 'children',
      label: 'name',
    }),
  },
  // 节点key
  nodeKey: {
    type: String,
    default: 'id',
  },
  // 默认展开所有
  defaultExpandAll: {
    type: Boolean,
    default: false,
  },
  // 点击节点展开
  expandOnClickNode: {
    type: Boolean,
    default: false,
  },
  // 高亮当前节点
  highlightCurrent: {
    type: Boolean,
    default: true,
  },
  // 是否可拖拽
  draggable: {
    type: Boolean,
    default: false,
  },
  // 当前选中节点key
  currentNodeKey: {
    type: [String, Number],
    default: null,
  },
  // 是否显示成员数量
  showMemberCount: {
    type: Boolean,
    default: true,
  },
  // 是否显示在线状态
  showOnlineStatus: {
    type: Boolean,
    default: true,
  },
  // 是否显示操作按钮
  showActions: {
    type: Boolean,
    default: true,
  },
  // 权限控制
  canAddDept: {
    type: Boolean,
    default: false,
  },
  canAddMember: {
    type: Boolean,
    default: false,
  },
  canEdit: {
    type: Boolean,
    default: false,
  },
  canDelete: {
    type: Boolean,
    default: false,
  },
  // 是否加载成员
  loadMembers: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits([
  'node-click',
  'node-expand',
  'node-collapse',
  'add-dept',
  'add-member',
  'send-message',
  'edit',
  'delete',
  'drag-start',
  'drag-end',
  'drop',
  'load-complete',
])

// 状态
const treeRef = ref(null)
const loading = ref(false)
const searchText = ref('')
const treeData = ref([])
const expandedKeys = ref([])
const memberCache = ref(new Map())

// 加载部门树
async function loadTree() {
  loading.value = true
  try {
    const { data } = await getDepartmentTree()

    if (data.code === 200 && data.data) {
      const processedTree = processTreeData(data.data)
      treeData.value = processedTree

      // 默认展开第一层
      if (props.defaultExpandAll && processedTree.length > 0) {
        expandedKeys.value = processedTree.map(item => item[props.nodeKey])
      }

      emit('load-complete', processedTree)
    } else {
      treeData.value = []
    }
  } catch (error) {
    console.error('加载组织架构失败:', error)
    ElMessage.error('加载组织架构失败')
    treeData.value = []
  } finally {
    loading.value = false
  }
}

// 处理树数据 - 添加部门标识和成员
function processTreeData(nodes, parentId = null) {
  if (!nodes || !Array.isArray(nodes)) return []

  return nodes.map(node => {
    const isDept = !node.userId // 没有userId表示是部门

    const processedNode = {
      ...node,
      isDept,
      parentId,
    }

    // 递归处理子节点
    if (node.children && node.children.length > 0) {
      processedNode.children = processTreeData(node.children, node[props.nodeKey])
      processedNode.hasChildren = true
    }

    return processedNode
  })
}

// 加载部门成员
async function loadDepartmentMembers(deptId) {
  if (!props.loadMembers) return
  if (memberCache.value.has(deptId)) return memberCache.value.get(deptId)

  try {
    const { data } = await getDepartmentMembers(deptId)

    if (data.code === 200 && data.data) {
      const members = data.data.map(member => ({
        ...member,
        isDept: false,
        onlineStatus: member.onlineStatus || 'offline',
      }))

      memberCache.value.set(deptId, members)
      return members
    }
    return []
  } catch (error) {
    console.error('加载部门成员失败:', error)
    return []
  }
}

// 搜索过滤
function filterNode(value, data) {
  if (!value) return true

  const label = data[props.treeProps.label] || ''
  return label.toLowerCase().includes(value.toLowerCase())
}

function handleSearch(value) {
  treeRef.value?.filter(value)
}

// 节点点击
function handleNodeClick(data, node) {
  emit('node-click', data, node)

  // 如果是部门，加载成员
  if (data.isDept && props.loadMembers) {
    loadDepartmentMembers(data[props.nodeKey]).then(members => {
      // 将成员添加到子节点
      if (members && members.length > 0) {
        const existingChildren = data.children || []

        // 移除之前的成员节点
        const nonMemberChildren = existingChildren.filter(child => child.isDept)

        // 合并成员和子部门
        data.children = [...nonMemberChildren, ...members]
      }
    })
  }
}

// 节点展开
function handleNodeExpand(data, node) {
  expandedKeys.value.push(data[props.nodeKey])
  emit('node-expand', data, node)

  // 展开时加载成员
  if (data.isDept && props.loadMembers) {
    loadDepartmentMembers(data[props.nodeKey]).then(members => {
      if (members && members.length > 0) {
        const existingChildren = data.children || []
        const nonMemberChildren = existingChildren.filter(child => child.isDept)

        data.children = [...nonMemberChildren, ...members]
      }
    })
  }
}

// 节点折叠
function handleNodeCollapse(data, node) {
  const index = expandedKeys.value.indexOf(data[props.nodeKey])
  if (index > -1) {
    expandedKeys.value.splice(index, 1)
  }
  emit('node-collapse', data, node)
}

// 节点操作
function handleAction(command, data) {
  switch (command) {
    case 'add-dept':
      emit('add-dept', data)
      break
    case 'add-member':
      emit('add-member', data)
      break
    case 'send-message':
      emit('send-message', data)
      break
    case 'edit':
      emit('edit', data)
      break
    case 'delete':
      handleDelete(data)
      break
  }
}

// 删除确认
async function handleDelete(data) {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门"${data.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    emit('delete', data)
  } catch {
    // 用户取消
  }
}

// 拖拽相关
function allowDrop(draggingNode, dropNode, type) {
  // 只允许同级拖拽，或拖拽到部门中
  if (type === 'inner') {
    return dropNode.data.isDept
  }
  return true
}

function allowDrag(node) {
  // 只允许拖拽部门节点
  return node.data.isDept
}

function handleDragStart(node, event) {
  emit('drag-start', node, event)
}

function handleDragEnd(draggingNode, dropNode, dropType, event) {
  emit('drag-end', draggingNode, dropNode, dropType, event)
}

function handleDrop(draggingNode, dropNode, dropType, event) {
  emit('drop', draggingNode, dropNode, dropType, event)
}

// 公开方法
// 设置当前选中节点
function setCurrentKey(key) {
  treeRef.value?.setCurrentKey(key)
}

// 获取当前选中节点
function getCurrentNode() {
  return treeRef.value?.getCurrentNode()
}

// 设置展开的节点
function setExpandedKeys(keys) {
  expandedKeys.value = keys
}

// 获取展开的节点
function getExpandedKeys() {
  return expandedKeys.value
}

// 过滤节点
function filter(keyword) {
  treeRef.value?.filter(keyword)
}

// 刷新树
function refresh() {
  memberCache.value.clear()
  loadTree()
}

// 暴露方法
defineExpose({
  setCurrentKey,
  getCurrentNode,
  setExpandedKeys,
  getExpandedKeys,
  filter,
  refresh,
})

// 监听搜索文本
watch(searchText, (val) => {
  treeRef.value?.filter(val)
})

// 组件挂载
onMounted(() => {
  loadTree()
})
</script>

<style lang="scss" scoped>
@import '@/styles/dingtalk-theme.scss';

.organization-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-search {
  padding: 12px 16px;
  border-bottom: 1px solid $border-light;
}

.dept-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;

  :deep(.el-tree-node__content) {
    height: 40px;
    padding-right: 8px;

    &:hover {
      background-color: $bg-hover;
    }
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background-color: $primary-color-lighter;

    .node-label {
      color: $primary-color;
      font-weight: 500;
    }
  }
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 8px;
  overflow: hidden;

  .node-icon {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;

    .el-icon {
      font-size: 16px;
      color: $text-secondary;
    }

    .el-avatar {
      font-size: 12px;
    }
  }

  .node-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 14px;
    color: $text-primary;
  }

  .node-count {
    flex-shrink: 0;
    font-size: 12px;
    color: $text-tertiary;
  }

  .node-status {
    flex-shrink: 0;
    display: flex;
    align-items: center;

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background-color: $text-disabled;

      &.online {
        background-color: $success-color;
        box-shadow: 0 0 4px rgba($success-color, 0.5);
      }

      &.busy {
        background-color: $error-color;
        box-shadow: 0 0 4px rgba($error-color, 0.5);
      }

      &.away {
        background-color: $warning-color;
        box-shadow: 0 0 4px rgba($warning-color, 0.5);
      }
    }
  }

  .node-actions {
    flex-shrink: 0;
    opacity: 0;
    transition: opacity $transition-fast $ease-base;

    .action-icon {
      font-size: 16px;
      color: $text-secondary;
      cursor: pointer;

      &:hover {
        color: $primary-color;
      }
    }
  }

  &:hover .node-actions {
    opacity: 1;
  }
}

.tree-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: $text-tertiary;

  .el-icon {
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    margin: 0;
    font-size: 14px;
  }
}
</style>
