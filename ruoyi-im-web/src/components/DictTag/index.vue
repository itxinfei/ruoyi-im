<template>
  <div>
    <template v-if="type === ''">
      <span :class="tagClass">{{ label }}</span>
    </template>
    <template v-else>
      <el-tag :type="type" :effect="effect">
        {{ label }}
      </el-tag>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: [Number, String, Boolean],
    required: true,
  },
  type: {
    type: String,
    default: '',
  },
  effect: {
    type: String,
    default: 'light',
  },
})

const tagClass = computed(() => {
  const classMap = {
    0: 'success-text',
    1: 'danger-text',
  }
  return classMap[props.value] || ''
})

const label = computed(() => {
  const value = props.value
  const actions = {
    0: '正常',
    1: '停用',
  }
  return actions[value] || value
})
</script>

<style scoped>
.success-text {
  color: #67c23a;
}
.danger-text {
  color: #f56c6c;
}
</style>
