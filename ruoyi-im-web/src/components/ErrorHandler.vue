<template>
  <el-alert
    v-if="error"
    :title="error.message"
    type="error"
    :description="error.details"
    show-icon
    @close="clearError"
  />
</template>

<script setup>
import { computed } from 'vue'
// TODO: 需要重新实现Vuex错误处理
const error = computed(() => null)

const clearError = () => {
  // TODO: 实现Vuex错误清除
}
</script>
