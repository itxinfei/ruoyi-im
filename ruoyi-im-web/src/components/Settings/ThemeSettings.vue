<template>
  <div class="theme-settings">
    <h3 class="group-title">主题定制</h3>
    <div class="theme-body">
      <p class="desc">通过主题定制器可以修改主色调、字体和界面细节。</p>
      <el-button type="primary" @click="$emit('open-theme')">打开主题定制器</el-button>
    </div>
  </div>
</template>

<script setup>
// 简单占位组件，用于打开 ThemeCustomizer
const props = defineProps({})
const emit = defineEmits(['open-theme'])
</script>

<style scoped lang="scss">
.theme-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
}
.desc {
  color: var(--dt-text-secondary);
}
</style>
