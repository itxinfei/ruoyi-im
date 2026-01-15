<!--
  右键菜单组件
-->
<template>
  <teleport to="body">
    <ul
      v-if="visible"
      class="context-menu"
      :style="{ left: x + 'px', top: y + 'px' }"
      @click="handleMenuClick"
    >
      <li
        v-for="(item, index) in menuItems"
        :key="index"
        :class="['menu-item', { divider: item.divider, danger: item.danger }]"
        :data-command="item.command"
      >
        {{ item.label }}
      </li>
    </ul>
  </teleport>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  menuItems: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['select'])

const visible = ref(false)
const x = ref(0)
const y = ref(0)

const show = event => {
  x.value = event.clientX
  y.value = event.clientY
  visible.value = true

  // 添加全局点击监听
  setTimeout(() => {
    document.addEventListener('click', hide)
  }, 0)
}

const hide = () => {
  visible.value = false
  document.removeEventListener('click', hide)
}

const handleMenuClick = event => {
  const command = event.target.dataset.command
  if (command) {
    emit('select', command)
    hide()
  }
}

onUnmounted(() => {
  document.removeEventListener('click', hide)
})

defineExpose({
  show,
  hide,
})
</script>

<style lang="scss" scoped>
.context-menu {
  position: fixed;
  z-index: 9999;
  min-width: 160px;
  padding: 4px 0;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  list-style: none;
  margin: 0;

  .menu-item {
    padding: 8px 16px;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    user-select: none;

    &:hover {
      background: #f5f5f5;
    }

    &.divider {
      height: 1px;
      padding: 0;
      margin: 4px 0;
      background: #e8e8e8;
      cursor: default;

      &:hover {
        background: transparent;
      }
    }

    &.danger {
      color: #f56c6c;

      &:hover {
        background: #fee;
        color: #f56c6c;
      }
    }
  }
}
</style>
