<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="clearfix">
          <span>群组成员管理</span>
        </div>
      </template>

      <el-table :data="memberList">
        <el-table-column label="成员昵称" align="center" prop="nickName" />
        <el-table-column label="角色" align="center" prop="role">
          <template #default="scope">
            <el-tag :type="scope.row.role === '1' ? 'danger' : 'info'">
              {{ scope.row.role === '1' ? '群主' : '成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button size="small" type="text">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const memberList = ref([
  { id: 1, nickName: '用户1', role: '1' },
  { id: 2, nickName: '用户2', role: '2' },
])
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}
</style>
