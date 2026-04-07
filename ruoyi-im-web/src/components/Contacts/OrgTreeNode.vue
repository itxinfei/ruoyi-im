<template>
  <div class="node-wrapper">
    <div
      class="node-item"
      :class="{ active: activeId === node.id }"
      :style="{ paddingLeft: level * 16 + 'px' }"
      @click="handleClick"
    >
      <el-icon v-if="node.children && node.children.length" class="expand-icon" :class="{ rotated: isExpanded }">
        <ArrowRight />
      </el-icon>
      <span v-else class="expand-placeholder" />

      <el-icon class="type-icon" :class="node.type">
        <component :is="getNodeIcon(node.type, isExpanded)" />
      </el-icon>

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
import { ArrowRight, OfficeBuilding, FolderOpened, Folder } from '@element-plus/icons-vue'

const props = defineProps({
  node: Object,
  level: { type: Number, default: 0 },
  activeId: [String, Number]
})

const emit = defineEmits(['select'])
const isExpanded = ref(props.level === 0) // 默认展开第一级

const getNodeIcon = (type, expanded) => {
  if (type === 'COMPANY') return OfficeBuilding
  return expanded ? FolderOpened : Folder
}

const handleClick = () => {
  if (props.node.children?.length) {
    isExpanded.value = !isExpanded.value
  }
  emit('select', props.node)
}
</script>

<style scoped lang="scss">
.node-item {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 var(--dt-spacing-sm);
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: var(--dt-brand-lighter);
    .node-name {
      color: var(--dt-brand-color);
      font-weight: 600;
    }
    .type-icon {
      color: var(--dt-brand-color);
    }
  }

  .expand-icon {
    font-size: 14px;
    color: var(--dt-text-tertiary);
    transition: transform 0.2s ease;
    margin-right: 6px;
    &.rotated {
      transform: rotate(90deg);
    }
  }
  .expand-placeholder {
    width: 20px;
  }

  .type-icon {
    font-size: 18px;
    margin-right: var(--dt-spacing-sm);
    color: var(--dt-text-secondary);
    &.COMPANY {
      color: var(--dt-brand-color);
    }
  }

  .node-name {
    font-size: 14px;
    color: var(--dt-text-primary);
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .node-count {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin-left: 8px;
    background: var(--dt-bg-hover);
    padding: 2px 8px;
    border-radius: var(--dt-radius-sm);
  }
}

.node-children {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 2px;
}
</style>
