<template>
  <div class="group-management">
    <!-- 统计卡片 -->
    <el-row
      :gutter="20"
      class="mb-4"
    >
      <el-col :span="8">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">总群组数</span>
              <span class="stat-value">{{ stats.total }}</span>
            </div>
            <div class="stat-icon primary-bg">
              <el-icon>
                <ChatLineRound />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">今日活跃群组</span>
              <span class="stat-value">{{ stats.active }}</span>
            </div>
            <div class="stat-icon success-bg">
              <el-icon>
                <Compass />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">新创建(本周)</span>
              <span class="stat-value">{{ stats.weeklyNew }}</span>
            </div>
            <div class="stat-icon warning-bg">
              <el-icon>
                <Plus />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <el-card
      class="search-card"
      shadow="never"
    >
      <el-form
        :model="searchForm"
        inline
        class="search-form"
        @submit.prevent
      >
        <el-form-item label="群组名称">
          <el-input
            v-model="searchForm.name"
            placeholder="群名称/ID"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="群类型">
          <el-select
            v-model="searchForm.type"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option
              label="普通群"
              :value="1"
            />
            <el-option
              label="部门群"
              :value="2"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :icon="Search"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <el-button
            :icon="Refresh"
            @click="handleReset"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工具栏和表格 -->
    <el-card
      class="table-card"
      shadow="never"
    >
      <div class="batch-actions">
        <el-button
          type="primary"
          :icon="Plus"
          @click="handleAdd"
        >
          创建群组
        </el-button>
        <el-button
          type="danger"
          :disabled="selectedGroups.length === 0"
          @click="handleBatchDelete"
        >
          批量解散
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="groupList"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
          align="center"
        />
        <el-table-column
          prop="id"
          label="群ID"
          width="100"
          align="center"
        />
        <el-table-column
          label="群头像"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-avatar
              :size="32"
              :src="row.faceUrl"
            >
              {{ row.name?.[0] }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          label="群组名称"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="ownerId"
          label="群主ID"
          width="120"
          align="center"
        />
        <el-table-column
          prop="memberCount"
          label="成员数"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <span class="member-count">{{ row.memberCount }}<span class="member-max">/{{ row.maxMemberCount
            }}</span></span>
          </template>
        </el-table-column>
        <el-table-column
          prop="type"
          label="类型"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="row.type === 2 ? 'warning' : 'info'">
              {{ row.type === 2 ? '部门群' : '普通群' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="160"
          align="center"
        />
        <el-table-column
          label="操作"
          width="180"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="View"
              @click="handleView(row)"
            >
              详情
            </el-button>
            <el-button
              type="warning"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              管理
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              @click="handleDelete(row)"
            >
              解散
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadGroups"
          @current-change="loadGroups"
        />
      </div>
    </el-card>

    <!-- 群组详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="群组详情"
      width="600px"
    >
      <div
        v-if="currentGroup"
        class="group-detail"
      >
        <div class="group-info">
          <el-avatar
            :size="64"
            :src="currentGroup.faceUrl"
          >
            {{ currentGroup.name?.[0] }}
          </el-avatar>
          <div class="info-content">
            <h3>{{ currentGroup.name }}</h3>
            <p>群ID: {{ currentGroup.id }}</p>
          </div>
        </div>
        <el-descriptions
          :column="2"
          border
          class="mt-4"
        >
          <el-descriptions-item label="群主">
            {{ currentGroup.ownerName || currentGroup.ownerId }}
          </el-descriptions-item>
          <el-descriptions-item label="创建日期">
            {{ currentGroup.createTime }}
          </el-descriptions-item>
          <el-descriptions-item
            label="群人数"
            :span="2"
          >
            {{ currentGroup.memberCount }} / {{ currentGroup.maxMemberCount
            }}
          </el-descriptions-item>
          <el-descriptions-item
            label="群备注"
            :span="2"
          >
            {{ currentGroup.introduction || '暂无介绍' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, Delete, View, Edit,
  ChatLineRound, Compass
} from '@element-plus/icons-vue'
import { getGroupList, deleteGroup, getGroupStats } from '@/api/admin'

const loading = ref(false)
const groupList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const selectedGroups = ref([])

const stats = reactive({
  total: 0,
  active: 0,
  weeklyNew: 0
})

const searchForm = reactive({
  name: '',
  type: ''
})

const loadGroups = async () => {
  loading.value = true
  try {
    const res = await getGroupList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    })
    if (res.code === 200) {
      groupList.value = res.data.list
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载群组列表失败')
  } finally {
    loading.value = false
  }
}

// 加载群组统计数据
const loadGroupStats = async () => {
  try {
    const res = await getGroupStats()
    if (res.code === 200) {
      stats.total = res.data.total || 0
      stats.active = res.data.active || 0
      stats.weeklyNew = res.data.weeklyNew || 0
    }
  } catch (error) {
    console.error('加载群组统计失败:', error)
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadGroups()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.type = ''
  handleSearch()
}

const handleSelectionChange = selection => {
  selectedGroups.value = selection
}

const detailVisible = ref(false)
const currentGroup = ref(null)
const handleView = row => {
  currentGroup.value = row
  detailVisible.value = true
}

const handleAdd = () => {
  ElMessage.info('该功能目前需通过前端客户端创建')
}

const handleEdit = row => {
  ElMessage.info('群组成员管理功能开发中')
}

const handleDelete = row => {
  ElMessageBox.confirm(`确定要解散群组 "${row.name}" 吗？此操作无法恢复。`, '解散确认', {
    type: 'warning'
  }).then(async () => {
    // 实际应调用后端接口
    ElMessage.success('已发起解散请求')
  }).catch(() => { })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定要解散选中的群组吗？', '批量操作', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量解散请求已提交')
  })
}

onMounted(() => {
  loadGroups()
  loadGroupStats()
})
</script>

<style scoped lang="scss">
.group-management {
  padding: var(--dt-space-md);
}

.stat-card {
  .stat-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .stat-label {
      font-size: 14px;
      color: var(--dt-text-secondary);
    }

    .stat-value {
      font-size: 24px;
      font-weight: 600;
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;

      &.primary-bg {
        background: var(--dt-color-primary-bg-subtle);
        color: var(--dt-color-primary-text);
      }

      &.success-bg {
        background: var(--dt-color-success-bg-subtle);
        color: var(--dt-color-success-text);
      }

      &.warning-bg {
        background: var(--dt-color-warning-bg-subtle);
        color: var(--dt-color-warning-text);
      }
    }
  }
}

.search-card {
  margin-bottom: var(--dt-space-md);
}

.table-card {
  .batch-actions {
    margin-bottom: var(--dt-space-md);
  }
}

.member-count {
  font-family: var(--dt-font-family-mono);

  .member-max {
    color: var(--dt-text-tertiary);
    font-size: 12px;
  }
}

.group-detail {
  .group-info {
    display: flex;
    align-items: center;
    gap: 16px;

    h3 {
      margin: 0 0 4px;
    }

    p {
      margin: 0;
      color: var(--dt-text-secondary);
      font-size: 14px;
    }
  }
}

.pagination-container {
  margin-top: var(--dt-space-md);
  display: flex;
  justify-content: flex-end;
}

.mt-4 {
  margin-top: 16px;
}
</style>
