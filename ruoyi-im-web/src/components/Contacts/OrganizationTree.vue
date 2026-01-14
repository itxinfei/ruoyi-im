<template>
  <div class="organization-tree">
    <!-- 树形结构 -->
    <el-tree
      ref="treeRef"
      :data="departmentTree"
      :props="treeProps"
      :highlight-current="true"
      node-key="id"
      :expand-on-click-node="false"
      :default-expand-all="false"
      :indent="16"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <div class="tree-node">
          <el-icon class="node-icon" :size="16">
            <component :is="data.children ? OfficeBuilding : User" />
          </el-icon>
          <span class="node-label">{{ node.label }}</span>
          <span v-if="data.memberCount !== undefined" class="member-count">
            ({{ data.memberCount }})
          </span>
        </div>
      </template>
    </el-tree>

    <!-- 部门成员列表 -->
    <div v-if="selectedDepartment && departmentMembers.length > 0" class="department-members">
      <div class="members-header">
        <h4>{{ selectedDepartment.name }}</h4>
        <span class="members-count">{{ departmentMembers.length }} 人</span>
      </div>
      <div class="members-list">
        <div
          v-for="member in departmentMembers"
          :key="member.id"
          class="member-item"
          @click="handleMemberClick(member)"
        >
          <el-avatar :size="36" :src="member.avatar">
            {{ (member.name || member.nickname || member.username)?.charAt(0) || '用' }}
          </el-avatar>
          <div class="member-info">
            <div class="member-name">{{ member.name || member.nickname || member.username }}</div>
            <div class="member-position">{{ member.position || '员工' }}</div>
          </div>
          <div v-if="member.online" class="online-status online"></div>
          <div v-else class="online-status offline"></div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!departmentTree || departmentTree.length === 0" description="暂无组织架构" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { OfficeBuilding, User } from '@element-plus/icons-vue'
import { getDepartmentTree, getDepartmentMembers } from '@/api/im/organization'

/**
 * 组织架构树组件
 * 用于展示企业部门层级结构和成员列表
 */

const emit = defineEmits(['select-member'])

// 树形数据
const departmentTree = ref([])
// 选中的部门
const selectedDepartment = ref(null)
// 部门成员列表
const departmentMembers = ref([])
// 树组件引用
const treeRef = ref(null)

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name',
}

/**
 * 加载部门树
 */
const loadDepartmentTree = async () => {
  try {
    const res = await getDepartmentTree()
    if (res.code === 200 && res.data) {
      departmentTree.value = res.data
    }
  } catch (error) {
    console.error('加载组织架构失败:', error)
  }
}

/**
 * 处理节点点击
 */
const handleNodeClick = async (data) => {
  selectedDepartment.value = data
  await loadDepartmentMembers(data.id)
}

/**
 * 加载部门成员
 */
const loadDepartmentMembers = async (departmentId) => {
  try {
    const res = await getDepartmentMembers(departmentId)
    if (res.code === 200 && res.data) {
      departmentMembers.value = res.data
    }
  } catch (error) {
    console.error('加载部门成员失败:', error)
    departmentMembers.value = []
  }
}

/**
 * 处理成员点击
 */
const handleMemberClick = (member) => {
  emit('select-member', member)
}

/**
 * 刷新数据
 */
const refresh = () => {
  loadDepartmentTree()
  if (selectedDepartment.value) {
    loadDepartmentMembers(selectedDepartment.value.id)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadDepartmentTree()
})

// 暴露方法供父组件调用
defineExpose({
  refresh,
})
</script>

<script>
export default {
  name: 'OrganizationTree'
}
</script>

<style lang="scss" scoped>
.organization-tree {
  height: 100%;
  overflow-y: auto;
  padding: 12px 0;

  // 滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 3px;

    &:hover {
      background: rgba(0, 0, 0, 0.2);
    }
  }

  :deep(.el-tree) {
    background: transparent;

    .el-tree-node {
      position: relative;

      .el-tree-node__content {
        height: 36px;
        padding-left: 10px !important;
        border-radius: 4px;
        transition: all 0.2s ease;

        &:hover {
          background: #f5f7fa;
        }
      }

      &.is-current > .el-tree-node__content {
        background: #e6f7ff;
        color: #1890ff;
      }
    }

    .el-tree-node__expand-icon {
      color: #c0c4cc;
    }
  }

  .tree-node {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;

    .node-icon {
      color: #67c23a;
      flex-shrink: 0;
    }

    .node-label {
      flex: 1;
      font-size: 14px;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .member-count {
      font-size: 12px;
      color: #999;
      flex-shrink: 0;
    }
  }

  .department-members {
    margin-top: 16px;
    padding: 0 8px;
    border-top: 1px solid #e8e8e8;

    .members-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 4px 8px;

      h4 {
        margin: 0;
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }

      .members-count {
        font-size: 12px;
        color: #999;
      }
    }

    .members-list {
      .member-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px 10px;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover {
          background: #f5f7fa;
        }

        .member-info {
          flex: 1;
          min-width: 0;

          .member-name {
            font-size: 14px;
            color: #333;
            overflow: hidden;
          }

          .member-position {
            font-size: 12px;
            color: #999;
            overflow: hidden;
          }
        }

        .online-status {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          flex-shrink: 0;

          &.online {
            background: #52c41a;
          }

          &.offline {
            background: #d9d9d9;
          }
        }
      }
    }
  }

  :deep(.el-empty) {
    padding: 40px 0;
  }
}
</style>
