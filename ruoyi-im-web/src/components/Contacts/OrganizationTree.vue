<template>
  <div class="org-tree-premium custom-scrollbar">
    <!-- 递归组件渲染部门树 -->
    <div class="tree-root">
      <OrgTreeNode
        v-for="dept in treeData"
        :key="dept.id"
        :node="dept"
        :active-id="selectedDept?.id"
        @select="onSelect"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrgTree } from '@/api/im/organization'
import OrgTreeNode from './OrgTreeNode.vue' // 抽离递归子组件

defineProps({ selectedDept: Object })
const emit = defineEmits(['update:selectedDept', 'select'])

const treeData = ref([])

const onSelect = (dept) => {
  emit('update:selectedDept', dept)
  emit('select', dept)
}

const loadData = async () => {
  try {
    const res = await getOrgTree()
    if (res.code === 200) {
      // 钉钉风格：根节点通常是公司名
      treeData.value = [{
        id: 'root',
        name: 'RuoYi IM 研发中心',
        type: 'COMPANY',
        children: res.data || []
      }]
    }
  } catch (e) { console.error('加载组织架构失败', e) }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.org-tree-premium { height: 100%; overflow-y: auto; padding: 4px 8px; }
.tree-root { display: flex; flex-direction: column; gap: 2px; }
</style>
