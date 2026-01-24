<template>
  <div class="org-tree-container">
    <!-- 公司根节点 -->
    <div class="org-root">
      <div
        class="org-node root-node"
        :class="{ expanded: rootExpanded }"
        @click="toggleRoot"
      >
        <span class="material-icons-outlined expand-icon">
          {{ rootExpanded ? 'arrow_drop_down' : 'arrow_right' }}
        </span>
        <span class="node-label">RuoYi 科技</span>
      </div>

      <!-- 子部门列表 -->
      <div v-if="rootExpanded" class="org-children">
        <div
          v-for="dept in departments"
          :key="dept.id"
          class="dept-item"
        >
          <div
            class="org-node dept-node"
            :class="{
              active: selectedDept?.id === dept.id,
              clickable: dept.memberCount > 0
            }"
            @click="handleSelectDept(dept)"
          >
            <span class="node-label">{{ dept.name }}</span>
            <span v-if="dept.memberCount" class="member-badge">
              {{ dept.memberCount }}
            </span>
            <span v-if="dept.locked" class="material-icons-outlined lock-icon">lock</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrgTree } from '@/api/im/organization'

const props = defineProps({
  selectedDept: Object
})

const emit = defineEmits(['update:selectedDept', 'select'])

const rootExpanded = ref(true)
const departments = ref([
  { id: 'all', name: '全公司', memberCount: null },
  { id: 'ceo', name: '总经办', memberCount: null },
  { id: 'product', name: '产品部', memberCount: 12 },
  { id: 'tech', name: '技术部', memberCount: 28, locked: true },
  { id: 'marketing', name: '市场部', memberCount: 15 },
  { id: 'finance', name: '财务部', memberCount: 8 }
])

const toggleRoot = () => {
  rootExpanded.value = !rootExpanded.value
}

const handleSelectDept = (dept) => {
  if (dept.memberCount || dept.id === 'all') {
    emit('update:selectedDept', dept)
    emit('select', dept)
  }
}

const loadOrgTree = async () => {
  try {
    const res = await getOrgTree()
    if (res.code === 200 && res.data) {
      // 如果后端返回真实数据，使用真实数据
      // departments.value = res.data
    }
  } catch (e) {
    console.error('Failed to load org tree:', e)
  }
}

onMounted(() => {
  loadOrgTree()
})
</script>

<style scoped>
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
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: #595959;
  position: relative;
}

.root-node {
  font-weight: 500;
}

.root-node:hover,
.dept-node:hover {
  background: #f5f5f5;
}

.dept-node.active {
  background: #e6f7ff;
  color: #1677ff;
  font-weight: 500;
}

.dept-node.clickable {
  cursor: pointer;
}

.expand-icon {
  font-size: 20px;
  margin-right: 4px;
  color: #bfbfbf;
}

.node-label {
  font-size: 14px;
  flex: 1;
}

.member-badge {
  font-size: 10px;
  background: #e6f7ff;
  color: #1677ff;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 500;
}

.lock-icon {
  font-size: 12px;
  color: #d9d9d9;
  margin-left: 4px;
}

.org-children {
  padding-left: 16px;
  margin-left: 6px;
  border-left: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 2px;
}

/* 暗色模式 */
:deep(.dark) .org-node {
  color: #cbd5e1;
}

:deep(.dark) .org-node:hover {
  background: rgba(51, 65, 85, 0.5);
}

:deep(.dark) .dept-node.active {
  background: rgba(30, 58, 138, 0.3);
  color: #60a5fa;
}

:deep(.dark) .expand-icon {
  color: #64748b;
}

:deep(.dark) .member-badge {
  background: rgba(30, 58, 138, 0.3);
  color: #60a5fa;
}

:deep(.dark) .org-children {
  border-left-color: #334155;
}

:deep(.dark) .lock-icon {
  color: #475569;
}
</style>
