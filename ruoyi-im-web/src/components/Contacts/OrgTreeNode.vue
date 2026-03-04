<template>
  <div class="node-wrapper">
    <div 
      class="node-item" 
      :class="{ active: activeId === node.id }"
      :style="{ paddingLeft: level * 16 + 'px' }"
      @click="handleClick"
    >
      <span v-if="node.children && node.children.length" class="material-icons-outlined expand-icon" :class="{ rotated: isExpanded }">
        chevron_right
      </span>
      <span v-else class="expand-placeholder"></span>
      
      <span class="material-icons-outlined type-icon" :class="node.type">
        {{ node.type === 'COMPANY' ? 'corporate_fare' : (isExpanded ? 'folder_open' : 'folder') }}
      </span>
      
      <span class="node-name">{{ node.name }}</span>
      <span v-if="node.memberCount" class="node-count">{{ node.memberCount }}</span>
    </div>

    <!-- 递归展开 -->
    <div v-if="isExpanded && node.children" class="node-children">
      <OrgTreeNode 
        v-for="child in node.children" 
        :key="child.id" 
        :node="child" 
        :level="level + 1"
        :active-id="activeId"
        @select="$emit('select', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  node: Object,
  level: { type: Number, default: 0 },
  activeId: [String, Number]
})

const emit = defineEmits(['select'])
const isExpanded = ref(props.level === 0) // 默认展开第一级

const handleClick = () => {
  if (props.node.children?.length) {
    isExpanded.value = !isExpanded.value
  }
  emit('select', props.node)
}
</script>

<style scoped lang="scss">
.node-item {
  display: flex; align-items: center; height: 32px; padding: 0 8px; border-radius: 6px; cursor: pointer; transition: all 0.2s;
  &:hover { background: #f5f6f7; }
  &.active { background: #e8f4ff; .node-name { color: #1677ff; font-weight: 600; } .type-icon { color: #1677ff; } }
  
  .expand-icon { font-size: 16px; color: #8f959e; transition: transform 0.2s; margin-right: 4px; &.rotated { transform: rotate(90deg); } }
  .expand-placeholder { width: 20px; }
  
  .type-icon { font-size: 18px; margin-right: 8px; color: #8f959e;
    &.COMPANY { color: #1677ff; }
  }
  
  .node-name { font-size: 13px; color: #1f2329; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .node-count { font-size: 11px; color: #c9cdd4; margin-left: 8px; }
}
.node-children { display: flex; flex-direction: column; gap: 2px; margin-top: 2px; }
</style>
