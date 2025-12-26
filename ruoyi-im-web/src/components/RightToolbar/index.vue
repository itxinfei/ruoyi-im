<template>
  <div class="top-right-btn">
    <el-row>
      <el-tooltip
        class="item"
        effect="dark"
        :content="showSearch ? '隐藏搜索' : '显示搜索'"
        placement="top"
      >
        <el-button size="mini" circle icon="el-icon-search" @click="toggleSearch()" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" content="刷新" placement="top">
        <el-button size="mini" circle icon="el-icon-refresh" @click="refresh()" />
      </el-tooltip>
      <el-tooltip v-if="columns" class="item" effect="dark" content="显示/隐藏列" placement="top">
        <el-button size="mini" circle icon="el-icon-menu" @click="showColumn()" />
      </el-tooltip>
    </el-row>
    <el-dialog v-model="open" :title="title" append-to-body>
      <el-transfer
        v-model="value"
        :titles="['显示', '隐藏']"
        :data="columns"
        @change="dataChange"
      ></el-transfer>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  columns: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:showSearch', 'queryTable'])

// 显示搜索
const showSearch = ref(true)
// 弹出层标题
const title = ref('显示/隐藏列')
// 是否显示弹出层
const open = ref(false)
// 弹出层选中值
const value = ref([])

onMounted(() => {
  // 初始默认隐藏列
  if (props.columns) {
    value.value = props.columns.map(item => item.key)
  }
})

// 搜索
const toggleSearch = () => {
  showSearch.value = !showSearch.value
  emit('update:showSearch', showSearch.value)
}

// 刷新
const refresh = () => {
  emit('queryTable')
}

// 右侧列表元素变化
const dataChange = data => {
  for (let item of props.columns) {
    const index = data.indexOf(item.key)
    item.visible = index > -1
  }
}

// 打开显隐列dialog
const showColumn = () => {
  open.value = true
}
</script>

<style lang="scss" scoped>
.top-right-btn {
  position: absolute;
  right: 0;
  top: 0;

  .el-button {
    margin-left: 10px;
  }
}

:deep(.el-transfer__button) {
  border-radius: 50%;
  padding: 12px;
  display: block;
  margin-left: 0px;

  &:first-child {
    margin-bottom: 10px;
  }

  span {
    margin: 0;
  }
}
</style>
