<template>
  <div class="app-center-container">
    <!-- 分类标签 -->
    <div class="category-tabs">
      <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="办公" name="OFFICE" />
        <el-tab-pane label="数据" name="DATA" />
        <el-tab-pane label="工具" name="TOOLS" />
        <el-tab-pane label="自定义" name="CUSTOM" />
      </el-tabs>
    </div>

    <!-- 应用列表 -->
    <div class="app-content">
      <div v-for="(apps, category) in groupedApps" :key="category" class="category-section">
        <div class="category-title">{{ getCategoryName(category) }}</div>
        <div class="app-grid">
          <div v-for="app in apps" :key="app.id" class="app-card" @click="handleAppClick(app)">
            <div class="app-icon" :style="{ backgroundColor: app.iconColor || '#1677ff' }">
              <img v-if="app.icon && app.icon.startsWith('/')" :src="app.icon" :alt="app.name" />
              <span v-else class="app-icon-text">{{ app.name?.charAt(0) || 'A' }}</span>
            </div>
            <div class="app-name">{{ app.name }}</div>
            <div v-if="app.description" class="app-desc">{{ app.description }}</div>
          </div>
        </div>
      </div>
      <el-empty
        v-if="Object.keys(groupedApps).length === 0"
        description="暂无应用"
        :image-size="100"
      />
    </div>

    <!-- 应用详情抽屉 -->
    <el-drawer v-model="drawerVisible" :title="currentApp?.name" size="400px">
      <div v-if="currentApp" class="app-detail">
        <div class="detail-icon" :style="{ backgroundColor: currentApp.iconColor || '#1677ff' }">
          <span class="app-icon-text">{{ currentApp.name?.charAt(0) || 'A' }}</span>
        </div>
        <div class="detail-info">
          <div class="info-row">
            <span class="info-label">应用名称</span>
            <span class="info-value">{{ currentApp.name }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">应用编码</span>
            <span class="info-value">{{ currentApp.code }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">应用分类</span>
            <span class="info-value">{{ getCategoryName(currentApp.category) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">应用类型</span>
            <span class="info-value">{{ getAppTypeText(currentApp.appType) }}</span>
          </div>
          <div v-if="currentApp.description" class="info-row">
            <span class="info-label">应用描述</span>
            <span class="info-value">{{ currentApp.description }}</span>
          </div>
        </div>
        <div class="detail-actions">
          <el-button type="primary" @click="handleOpenApp">打开应用</el-button>
          <el-button @click="drawerVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAppsByCategory } from '@/api/im/app'

const activeCategory = ref('all')
const allApps = ref([])
const drawerVisible = ref(false)
const currentApp = ref(null)

const groupedApps = computed(() => {
  if (activeCategory.value === 'all') {
    return allApps.value
  }
  const filtered = allApps.value[activeCategory.value] || []
  return { [activeCategory.value]: filtered }
})

const loadApps = async () => {
  try {
    const response = await getAppsByCategory()
    if (response.code === 200) {
      allApps.value = response.data || {}
    }
  } catch (error) {
    console.error('获取应用列表失败:', error)
    allApps.value = {}
  }
}

const handleCategoryChange = () => {
  // 分类切换时，数据由computed处理
}

const handleAppClick = app => {
  currentApp.value = app
  drawerVisible.value = true
}

const handleOpenApp = () => {
  if (!currentApp.value) return
  const { appType, appUrl } = currentApp.value

  if (appType === 'ROUTE' && appUrl) {
    // 内部路由跳转
    window.location.href = appUrl
  } else if (appType === 'IFRAME' && appUrl) {
    // 嵌入iframe
    ElMessage.info('即将在iframe中打开应用')
  } else if (appType === 'LINK' && appUrl) {
    // 外部链接
    window.open(appUrl, '_blank')
  } else {
    ElMessage.warning('应用地址未配置')
  }
}

const getCategoryName = category => {
  const map = {
    OFFICE: '办公',
    DATA: '数据',
    TOOLS: '工具',
    CUSTOM: '自定义',
  }
  return map[category] || category
}

const getAppTypeText = appType => {
  const map = {
    ROUTE: '路由',
    IFRAME: '嵌入',
    LINK: '外部链接',
  }
  return map[appType] || appType
}

onMounted(() => {
  loadApps()
})
</script>

<style lang="scss" scoped>
.app-center-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.category-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.app-content {
  min-height: 400px;
}

.category-section {
  margin-bottom: 32px;

  &:last-child {
    margin-bottom: 0;
  }
}

.category-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 3px solid #1677ff;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.app-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.app-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 12px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1677ff;

  img {
    width: 100%;
    height: 100%;
    border-radius: 16px;
    object-fit: cover;
  }
}

.app-icon-text {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}

.app-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-desc {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.detail-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  background: #1677ff;

  .app-icon-text {
    font-size: 32px;
    font-weight: 600;
    color: #fff;
  }
}

.detail-info {
  width: 100%;
  margin-bottom: 32px;
}

.info-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  width: 100px;
  color: #999;
  font-size: 14px;
}

.info-value {
  flex: 1;
  color: #333;
  font-size: 14px;
}

.detail-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-actions .el-button {
  width: 100%;
}

:deep(.el-tabs__header) {
  margin: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}
</style>
