<template>
  <div class="org-tree-container">
    <!-- 公司根节点 -->
    <div class="org-root">
      <div
        class="org-node root-node"
        :class="{ expanded: rootExpanded }"
        @click="toggleRoot"
      >
        <svg class="expand-icon" viewBox="0 0 24 24" fill="none">
          <path d="M10 17l5-5-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="node-label">组织架构</span>
        <span v-if="loading" class="loading-icon"></span>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="org-children loading-state">
        <span class="loading-text">加载中...</span>
      </div>

      <!-- 空状态 -->
      <div v-else-if="departments.length === 0" class="org-children empty-state">
        <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="empty-text">暂无部门数据</span>
      </div>

      <!-- 子部门列表 -->
      <div v-else-if="rootExpanded" class="org-children">
        <div
          v-for="dept in departments"
          :key="dept.id"
          class="dept-item"
          :class="{ 'is-child': dept.isChild }"
        >
          <div
            class="org-node dept-node"
            :class="{
              active: selectedDept?.id === dept.id
            }"
            @click="handleSelectDept(dept)"
          >
            <span v-if="dept.isChild" class="child-indicator"></span>
            <span class="node-label">{{ dept.name }}</span>
            <span v-if="dept.memberCount !== null && dept.memberCount !== undefined" class="member-badge">
              {{ dept.memberCount }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getOrgTree } from '@/api/im/organization'

const props = defineProps({
  selectedDept: Object
})

const emit = defineEmits(['update:selectedDept', 'select'])

const rootExpanded = ref(true)
const loading = ref(false)
const orgTreeData = ref([])

// 展开第一级部门作为子部门列表
const departments = computed(() => {
  if (orgTreeData.value.length === 0) {
    // 默认空列表
    return []
  }
  // 扁平化第一级部门
  const result = []
  orgTreeData.value.forEach(dept => {
    result.push(dept)
    // 如果有子部门，也加入列表（可展开）
    if (dept.children && dept.children.length > 0) {
      dept.children.forEach(child => {
        result.push({
          ...child,
          isChild: true,
          parentId: dept.id
        })
      })
    }
  })
  return result
})

const toggleRoot = () => {
  rootExpanded.value = !rootExpanded.value
}

const handleSelectDept = (dept) => {
  // 允许选择任何部门（memberCount可能为0但仍可查看）
  emit('update:selectedDept', dept)
  emit('select', dept)
}

const loadOrgTree = async () => {
  loading.value = true
  try {
    const res = await getOrgTree()
    if (res.code === 200 && res.data) {
      orgTreeData.value = res.data || []
    }
  } catch (e) {
    console.error('加载组织架构失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrgTree()
})

// 暴露刷新方法
defineExpose({
  refresh: loadOrgTree
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.org-tree-container {
  display: flex;
  flex-direction: column;
}

.org-root {
  display: flex;
  flex-direction: column;
}

.org-node {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s var(--dt-ease-out);
  color: var(--dt-text-secondary);
  position: relative;
  font-size: 13px;

  .dark & {
    color: var(--dt-text-secondary-dark);
  }
}

.root-node {
  font-weight: 600;
  font-size: 14px;
  color: var(--dt-text-primary);

  .dark & {
    color: var(--dt-text-primary-dark);
  }
}

.root-node:hover,
.dept-node:hover {
  background: var(--dt-bg-hover);

  .dark & {
    background: var(--dt-bg-hover-dark);
  }
}

.dept-node.active {
  background: var(--dt-brand-lighter);
  color: var(--dt-brand-color);
  font-weight: 600;

  .dark & {
    background: rgba(22, 119, 255, 0.15);
    color: #60a5fa;
  }
}

.expand-icon {
  width: 16px;
  height: 16px;
  margin-right: 4px;
  color: var(--dt-text-quaternary);
  transition: transform 0.2s;
  transform: rotate(-90deg);

  .dark & {
    color: var(--dt-text-quaternary-dark);
  }
}

.root-node.expanded .expand-icon {
  transform: rotate(0deg);
}

.loading-icon {
  width: 14px;
  height: 14px;
  margin-left: auto;
  border: 2px solid var(--dt-border-color);
  border-top-color: var(--dt-brand-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.node-label {
  font-size: 13px;
  flex: 1;
}

.child-indicator {
  width: 4px;
  height: 4px;
  background: var(--dt-text-quaternary);
  border-radius: 50%;
  margin-right: 8px;

  .dark & {
    background: var(--dt-text-quaternary-dark);
  }
}

.dept-item.is-child {
  margin-left: 20px;
}

.member-badge {
  font-size: 10px;
  background: var(--dt-brand-lighter);
  color: var(--dt-brand-color);
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 600;
  margin-left: 6px;

  .dark & {
    background: rgba(22, 119, 255, 0.15);
    color: #60a5fa;
  }
}

.org-children {
  padding-left: 12px;
  margin-left: 10px;
  border-left: 1px dashed var(--dt-border-light);
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 4px;

  .dark & {
    border-left-color: var(--dt-border-dark);
  }
}

.org-children.loading-state,
.org-children.empty-state {
  padding: 16px 12px;
  text-align: center;
  border-left: none;
}

.loading-text {
  font-size: 12px;
  color: var(--dt-text-quaternary);

  .dark & {
    color: var(--dt-text-quaternary-dark);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .empty-icon {
    width: 40px;
    height: 40px;
    color: var(--dt-text-quaternary);
    opacity: 0.3;

    .dark & {
      color: var(--dt-text-quaternary-dark);
    }
  }

  .empty-text {
    font-size: 12px;
    color: var(--dt-text-tertiary);

    .dark & {
      color: var(--dt-text-tertiary-dark);
    }
  }
}
</style>
