<template>
  <div class="app-center">
    <!-- 头部区域 -->
    <div class="app-header">
      <div class="header-left">
        <h2 class="page-title">
          应用中心
        </h2>
        <p class="page-subtitle">
          发现和管理工作应用
        </p>
      </div>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索应用..."
          :prefix-icon="Search"
          clearable
          class="search-input"
          @clear="loadApplications"
          @keyup.enter="loadApplications"
        />
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs">
      <button
        v-for="category in categories"
        :key="category.key"
        class="category-tab"
        :class="{ active: activeCategory === category.key }"
        @click="switchCategory(category.key)"
      >
        <span class="tab-icon">{{ category.icon }}</span>
        <span class="tab-label">{{ category.label }}</span>
        <span
          v-if="category.count > 0"
          class="tab-count"
        >{{ category.count }}</span>
      </button>
    </div>

    <!-- 我的应用区域 -->
    <div
      v-if="myApps.length > 0"
      class="my-apps-section"
    >
      <div class="section-header">
        <h3 class="section-title">
          我的应用
        </h3>
        <div class="section-actions">
          <el-button
            size="small"
            text
            @click="editMyApps"
          >
            管理
          </el-button>
        </div>
      </div>
      <div class="my-apps-list">
        <div
          v-for="app in myApps"
          :key="app.id"
          class="my-app-item"
          @click="openApp(app)"
        >
          <div
            class="app-icon"
            :style="{ background: app.iconColor }"
          >
            <img
              v-if="app.iconUrl"
              :src="app.iconUrl"
              :alt="app.name"
            >
            <span v-else>{{ app.name.charAt(0) }}</span>
          </div>
          <span class="app-name">{{ app.name }}</span>
        </div>
      </div>
    </div>

    <!-- 应用市场区域 -->
    <div class="app-market-section">
      <div class="section-header">
        <h3 class="section-title">
          {{ categoryTitle }}
        </h3>
      </div>

      <!-- 加载状态 -->
      <div
        v-if="loading"
        class="loading-state"
      >
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <span>加载中...</span>
      </div>

      <!-- 空状态 -->
      <div
        v-else-if="appList.length === 0"
        class="empty-state"
      >
        <span class="material-icons-outlined empty-icon">apps</span>
        <p class="empty-text">
          {{ searchKeyword ? '未找到相关应用' : '暂无应用' }}
        </p>
      </div>

      <!-- 应用卡片列表 -->
      <div
        v-else
        class="app-grid"
      >
        <div
          v-for="app in appList"
          :key="app.id"
          class="app-card"
          :class="{ installed: isInstalled(app.id) }"
        >
          <div class="app-header">
            <div
              class="app-icon-wrapper"
              :style="{ background: app.iconColor }"
            >
              <img
                v-if="app.iconUrl"
                :src="app.iconUrl"
                :alt="app.name"
                class="app-icon-img"
              >
              <span
                v-else
                class="app-icon-text"
              >{{ app.name.charAt(0) }}</span>
              <div
                v-if="app.isHot"
                class="hot-badge"
              >
                HOT
              </div>
              <div
                v-if="app.isNew"
                class="new-badge"
              >
                NEW
              </div>
            </div>
            <div class="app-info">
              <h4 class="app-name">
                {{ app.name }}
              </h4>
              <p class="app-description">
                {{ app.description }}
              </p>
            </div>
          </div>

          <div class="app-details">
            <div class="app-meta">
              <span class="meta-item">
                <span class="material-icons-outlined">category</span>
                {{ app.categoryName }}
              </span>
              <span class="meta-item">
                <span class="material-icons-outlined">star</span>
                {{ app.rating || 5.0 }}
              </span>
              <span
                v-if="app.usageCount > 0"
                class="meta-item"
              >
                <span class="material-icons-outlined">download</span>
                {{ formatCount(app.usageCount) }}
              </span>
            </div>

            <div
              v-if="app.tags && app.tags.length > 0"
              class="app-tags"
            >
              <span
                v-for="tag in app.tags.slice(0, 3)"
                :key="tag"
                class="tag"
              >{{ tag }}</span>
            </div>
          </div>

          <div class="app-actions">
            <el-button
              v-if="isInstalled(app.id)"
              type="default"
              class="action-btn installed-btn"
              @click.stop="openApp(app)"
            >
              打开
            </el-button>
            <el-button
              v-else
              type="primary"
              class="action-btn install-btn"
              @click.stop="installApp(app)"
            >
              安装
            </el-button>

            <el-dropdown
              trigger="click"
              @command="(cmd) => handleAppAction(cmd, app)"
            >
              <el-button
                class="more-btn"
                :icon="MoreFilled"
                circle
              />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="detail">
                    <span class="material-icons-outlined">info</span>
                    应用详情
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="isInstalled(app.id)"
                    command="config"
                  >
                    <span class="material-icons-outlined">settings</span>
                    应用设置
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="isInstalled(app.id)"
                    command="uninstall"
                  >
                    <span class="material-icons-outlined">delete</span>
                    卸载应用
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="isInstalled(app.id)"
                    command="pin"
                  >
                    <span class="material-icons-outlined">
                      {{ isPinned(app.id) ? 'push_pin' : 'push_unpin' }}
                    </span>
                    {{ isPinned(app.id) ? '取消置顶' : '置顶' }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 应用详情对话框 -->
    <AppDetailDialog
      v-model="showDetailDialog"
      :app="selectedApp"
      @install="handleInstallFromDialog"
      @open="handleOpenFromDialog"
    />

    <!-- 应用配置对话框 -->
    <AppConfigDialog
      v-model="showConfigDialog"
      :app="selectedApp"
      @success="handleConfigSaved"
    />

    <!-- 应用管理对话框 -->
    <el-dialog
      v-model="showManageDialog"
      title="管理我的应用"
      width="500px"
      :close-on-click-modal="false"
      class="app-manage-dialog"
    >
      <div
        v-if="managingApps.length > 0"
        class="manage-apps-list"
      >
        <div
          v-for="(app, index) in managingApps"
          :key="app.id"
          class="manage-app-item"
        >
          <div
            class="app-icon"
            :style="{ background: app.iconColor }"
          >
            <img
              v-if="app.iconUrl"
              :src="app.iconUrl"
              :alt="app.name"
            >
            <span v-else>{{ app.name.charAt(0) }}</span>
          </div>
          <div class="app-info">
            <span class="app-name">{{ app.name }}</span>
            <el-tag
              v-if="app.pinned"
              size="small"
              type="warning"
            >
              置顶
            </el-tag>
          </div>
          <div class="app-actions">
            <el-button
              size="small"
              :disabled="index === 0"
              @click="handleAppTogglePin(app)"
            >
              <span class="material-icons-outlined">{{ app.pinned ? 'push_pin' : 'push_unpin' }}</span>
              {{ app.pinned ? '取消' : '置顶' }}
            </el-button>
            <el-button
              size="small"
              :disabled="index === 0"
              @click="handleAppMoveUp(index)"
            >
              <span class="material-icons-outlined">arrow_upward</span>
              上移
            </el-button>
            <el-button
              size="small"
              :disabled="index === managingApps.length - 1"
              @click="handleAppMoveDown(index)"
            >
              <span class="material-icons-outlined">arrow_downward</span>
              下移
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleAppUninstall(app)"
            >
              <span class="material-icons-outlined">delete</span>
              卸载
            </el-button>
          </div>
        </div>
      </div>
      <el-empty
        v-else
        description="暂无已安装应用"
      />

      <template #footer>
        <el-button @click="cancelAppManagement">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="saveAppManagement"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Loading, MoreFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getVisibleApplications,
  getApplicationsByCategory,
  getMyApplications,
  installApplication,
  uninstallApplication,
  pinApp,
  updateAppConfig,
  recordAppUsage,
  updateAppSort
} from '@/api/im/app'
import AppDetailDialog from '@/components/AppCenter/AppDetailDialog.vue'
import AppConfigDialog from '@/components/AppCenter/AppConfigDialog.vue'

// 定义 emit，用于与父组件通信
const emit = defineEmits(['switch-module', 'open-external-app'])

// 应用类型常量
const APP_TYPE = {
  ROUTE: 'ROUTE',     // 内部路由跳转
  IFRAME: 'IFRAME',   // 嵌入式iframe
  LINK: 'LINK'        // 外部链接
}

// 应用路由映射表
const APP_ROUTE_MAP = {
  // 工作台相关
  'workbench': 'workbench',
  'todo': 'todo',
  'approval': 'approval',
  'mail': 'mail',
  'assistant': 'assistant',
  'documents': 'drive',
  'calendar': 'calendar',
  // 通讯录
  'contacts': 'contacts',
  // 聊天
  'chat': 'chat'
}

// 状态
const loading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')
const showDetailDialog = ref(false)
const showConfigDialog = ref(false)
const showManageDialog = ref(false)
const selectedApp = ref(null)
const managingApps = ref([]) // 管理中的应用列表

// 数据
const appList = ref([])
const myApps = ref([])
const installedAppIds = ref(new Set())
const pinnedAppIds = ref(new Set())

// 分类配置
const categories = ref([
  { key: 'all', label: '全部', icon: 'apps', count: 0 },
  { key: 'office', label: '办公', icon: 'work', count: 0 },
  { key: 'collaboration', label: '协作', icon: 'group', count: 0 },
  { key: 'efficiency', label: '效率', icon: 'trending_up', count: 0 },
  { key: 'development', label: '开发', icon: 'code', count: 0 },
  { key: 'data', label: '数据', icon: 'bar_chart', count: 0 }
])

// 计算属性
const categoryTitle = computed(() => {
  const category = categories.value.find(c => c.key === activeCategory.value)
  return category ? category.label : '应用市场'
})

// 判断应用是否已安装
const isInstalled = appId => installedAppIds.value.has(appId)

// 判断应用是否置顶
const isPinned = appId => pinnedAppIds.value.has(appId)

// 格式化数量
const formatCount = count => {
  if (count >= 10000) { return (count / 10000).toFixed(1) + 'w' }
  if (count >= 1000) { return (count / 1000).toFixed(1) + 'k' }
  return count
}

// 切换分类
const switchCategory = categoryKey => {
  activeCategory.value = categoryKey
  loadApplications()
}

// 加载应用列表
const loadApplications = async () => {
  loading.value = true
  try {
    const res = activeCategory.value === 'all'
      ? await getVisibleApplications()
      : await getApplicationsByCategory()

    if (res.code === 200) {
      const apps = res.data || []
      appList.value = Array.isArray(apps) ? apps : (apps.list || apps.records || [])

      // 更新分类数量
      if (activeCategory.value === 'all' && res.data?.categories) {
        categories.value.forEach(cat => {
          const catData = res.data.categories.find(c => c.key === cat.key)
          cat.count = catData ? catData.count : 0
        })
      }
    } else {
      appList.value = []
    }
  } catch (error) {
    console.error('加载应用失败:', error)
    appList.value = []
  } finally {
    loading.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  try {
    const res = await getMyApplications()
    if (res.code === 200) {
      const apps = res.data || []
      myApps.value = Array.isArray(apps) ? apps : (apps.list || apps.records || [])

      // 更新已安装和置顶应用集合
      installedAppIds.value.clear()
      pinnedAppIds.value.clear()
      myApps.value.forEach(app => {
        installedAppIds.value.add(app.id)
        if (app.pinned) {
          pinnedAppIds.value.add(app.id)
        }
      })
    }
  } catch (error) {
    console.error('加载我的应用失败:', error)
  }
}

// 安装应用
const installApp = async app => {
  try {
    const res = await installApplication({
      appId: app.id,
      config: {},
      pinned: false,
      sortOrder: 0
    })

    if (res.code === 200) {
      installedAppIds.value.add(app.id)
      ElMessage.success(`"${app.name}" 安装成功`)
      await loadMyApps()
    } else {
      ElMessage.error(res.msg || '安装失败')
    }
  } catch (error) {
    console.error('安装应用失败:', error)
    ElMessage.error('安装失败，请稍后重试')
  }
}

// 卸载应用
const uninstallApp = async app => {
  try {
    await ElMessageBox.confirm(`确定要卸载"${app.name}"吗？`, '确认卸载', {
      type: 'warning'
    })

    const res = await uninstallApplication(app.id)
    if (res.code === 200) {
      installedAppIds.value.delete(app.id)
      pinnedAppIds.value.delete(app.id)
      ElMessage.success(`"${app.name}" 已卸载`)
      await loadMyApps()
    } else {
      ElMessage.error(res.msg || '卸载失败')
    }
  } catch {
    // 用户取消
  }
}

// 打开应用
const openApp = async app => {
  try {
    // 记录使用
    await recordAppUsage(app.id)

    const appType = (app.appType || 'ROUTE').toUpperCase()
    const appUrl = app.appUrl || ''

    switch (appType) {
      case APP_TYPE.ROUTE: {
        // 内部路由跳转
        const routeKey = appUrl.toLowerCase()
        const targetModule = APP_ROUTE_MAP[routeKey]

        if (targetModule) {
          // 切换到对应模块
          ElMessage.success(`正在打开 "${app.name}"...`)
          emit('switch-module', targetModule)
        } else {
          ElMessage.warning(`应用 "${app.name}" 的路由配置无效`)
        }
        break
      }

      case APP_TYPE.IFRAME:
        // 嵌入式应用 - 通过事件通知父组件打开iframe对话框
        emit('open-external-app', {
          ...app,
          openMode: 'iframe'
        })
        break

      case APP_TYPE.LINK:
        // 外部链接 - 在新窗口打开
        if (appUrl) {
          window.open(appUrl, '_blank')
          ElMessage.success(`已在新窗口打开 "${app.name}"`)
        } else {
          ElMessage.warning(`应用 "${app.name}" 未配置访问地址`)
        }
        break

      default:
        ElMessage.warning(`未知的应用类型: ${appType}`)
    }
  } catch (error) {
    console.error('打开应用失败:', error)
    ElMessage.error('打开应用失败，请稍后重试')
  }
}

// 置顶/取消置顶
const togglePin = async app => {
  try {
    const newPinnedState = !isPinned(app.id)
    const res = await pinApp(app.id, newPinnedState)

    if (res.code === 200) {
      if (newPinnedState) {
        pinnedAppIds.value.add(app.id)
      } else {
        pinnedAppIds.value.delete(app.id)
      }
      ElMessage.success(newPinnedState ? '已置顶' : '已取消置顶')
      await loadMyApps()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('置顶操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 应用操作处理
const handleAppAction = async (command, app) => {
  switch (command) {
    case 'detail':
      selectedApp.value = app
      showDetailDialog.value = true
      break
    case 'config':
      selectedApp.value = app
      showConfigDialog.value = true
      break
    case 'uninstall':
      await uninstallApp(app)
      break
    case 'pin':
      await togglePin(app)
      break
  }
}

// 从详情对话框安装
const handleInstallFromDialog = app => {
  showDetailDialog.value = false
  installApp(app)
}

// 从详情对话框打开
const handleOpenFromDialog = app => {
  showDetailDialog.value = false
  openApp(app)
}

// 配置保存成功
const handleConfigSaved = () => {
  showConfigDialog.value = false
  ElMessage.success('配置已保存')
}

// 编辑我的应用
const editMyApps = () => {
  // 复制我的应用列表到管理列表
  managingApps.value = myApps.value.map(app => ({ ...app }))
  showManageDialog.value = true
}

// 应用管理相关操作
const handleAppMoveUp = index => {
  if (index > 0) {
    const temp = managingApps.value[index]
    managingApps.value[index] = managingApps.value[index - 1]
    managingApps.value[index - 1] = temp
  }
}

const handleAppMoveDown = index => {
  if (index < managingApps.value.length - 1) {
    const temp = managingApps.value[index]
    managingApps.value[index] = managingApps.value[index + 1]
    managingApps.value[index + 1] = temp
  }
}

const handleAppUninstall = app => {
  const index = managingApps.value.findIndex(a => a.id === app.id)
  if (index > -1) {
    managingApps.value.splice(index, 1)
  }
}

const handleAppTogglePin = app => {
  const appItem = managingApps.value.find(a => a.id === app.id)
  if (appItem) {
    appItem.pinned = !appItem.pinned
  }
}

// 保存应用管理更改
const saveAppManagement = async () => {
  try {
    // 构建排序列表
    const sortList = managingApps.value.map((app, index) => ({
      appId: app.id,
      sortOrder: index,
      pinned: app.pinned || false
    }))

    const res = await updateAppSort({ sortList })
    if (res.code === 200) {
      ElMessage.success('应用排序已保存')
      showManageDialog.value = false
      await loadMyApps()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存应用管理失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

// 取消应用管理
const cancelAppManagement = () => {
  showManageDialog.value = false
  managingApps.value = []
}

// 组件挂载
onMounted(() => {
  loadApplications()
  loadMyApps()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.app-center {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--dt-bg-body);
}

// 头部
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);

  .header-left {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .page-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .page-subtitle {
    font-size: 13px;
    color: var(--dt-text-secondary);
    margin: 0;
  }

  .search-input {
    width: 280px;
  }
}

// 分类标签
.category-tabs {
  display: flex;
  padding: 12px 24px 0;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
  gap: 8px;
  overflow-x: auto;
  flex-shrink: 0;
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-2xl) var(--dt-radius-2xl) 0 0;
  font-size: 14px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  white-space: nowrap;

  .tab-icon {
    font-size: 16px;
  }

  .tab-label {
    font-weight: 500;
  }

  .tab-count {
    padding: 2px 8px;
    background: var(--dt-bg-hover);
    border-radius: var(--dt-radius-lg);
    font-size: 12px;
    font-weight: 600;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: var(--dt-brand-color);
    transform: scaleX(0);
    transition: transform 0.3s;
  }

  &:hover {
    color: var(--dt-text-primary);
    background: var(--dt-bg-hover);
  }

  &.active {
    color: var(--dt-brand-color);
    background: #fff;

    &::after {
      transform: scaleX(1);
    }

    .tab-count {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }
  }
}

// 我的应用区域
.my-apps-section {
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .section-actions {
    display: flex;
    gap: 8px;
  }
}

.my-apps-list {
  display: flex;
  gap: 12px;
  padding: 0 24px 16px;
  overflow-x: auto;
}

.my-app-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: var(--dt-brand-bg);
    transform: translateY(-2px);
  }

  .app-icon {
    width: 40px;
    height: 40px;
    border-radius: var(--dt-radius-lg);
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    flex-shrink: 0;
  }

  .app-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
  }
}

// 应用市场区域
.app-market-section {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: var(--dt-text-tertiary);

  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 14px;
    margin: 0;
  }
}

// 应用网格
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

// 应用卡片
.app-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-xl);
  padding: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  gap: 16px;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    transform: translateY(-4px);
    border-color: var(--dt-border-color);
  }

  &.installed {
    border-color: var(--dt-brand-color);
    background: var(--dt-brand-bg);

    .installed-btn {
      background: transparent;
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }
  }
}

.app-header {
  display: flex;
  gap: 12px;
}

.app-icon-wrapper {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: var(--dt-radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;

  .app-icon-img {
    width: 32px;
    height: 32px;
    object-fit: contain;
  }

  .app-icon-text {
    font-size: 20px;
    font-weight: 700;
    color: #fff;
  }

  .hot-badge,
  .new-badge {
    position: absolute;
    top: -4px;
    padding: 2px 6px;
    border-radius: var(--dt-radius-md);
    font-size: 10px;
    font-weight: 700;
    color: #fff;
    line-height: 1;
  }

  .hot-badge {
    right: -8px;
    background: linear-gradient(135deg, #f5222d 0%, #f5222d 100%);
  }

  .new-badge {
    left: -8px;
    background: linear-gradient(135deg, #52c41a 0%, #52c41a 100%);
  }
}

.app-info {
  flex: 1;
  min-width: 0;

  .app-name {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 6px 0;
  }

  .app-description {
    font-size: 13px;
    color: var(--dt-text-secondary);
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin: 0;
  }
}

.app-details {
  .app-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: var(--dt-text-tertiary);

      .material-icons-outlined {
        font-size: 14px;
      }
    }
  }

  .app-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: 8px;

    .tag {
      padding: 2px 8px;
      background: var(--dt-bg-hover);
      border-radius: var(--dt-radius-sm);
      font-size: 11px;
      color: var(--dt-text-secondary);
    }
  }
}

.app-actions {
  display: flex;
  align-items: center;
  gap: 8px;

  .action-btn {
    flex: 1;
    padding: 8px 16px;
    border-radius: var(--dt-radius-md);
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;

    &.install-btn {
      background: var(--dt-brand-color);
      color: #fff;
      border: none;

      &:hover {
        background: var(--dt-brand-hover);
      }
    }
  }

  .more-btn {
    flex-shrink: 0;
  }
}

/* 响应式 */
@media (max-width: 1023px) {
  .app-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;

    .search-input {
      width: 100%;
    }
  }

  .my-apps-list {
    flex-wrap: wrap;
  }

  .app-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

// 应用管理对话框
.app-manage-dialog {
  .manage-apps-list {
    max-height: 400px;
    overflow-y: auto;
  }

  .manage-app-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border: 1px solid var(--dt-border-color);
    border-radius: var(--dt-radius-md);
    margin-bottom: 8px;
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
    }

    .app-icon {
      width: 40px;
      height: 40px;
      border-radius: var(--dt-radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      font-size: 18px;
      font-weight: 600;
      color: #fff;

      img {
        width: 24px;
        height: 24px;
        object-fit: contain;
      }
    }

    .app-info {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 8px;

      .app-name {
        font-size: 14px;
        font-weight: 500;
        color: var(--dt-text-primary);
      }
    }

    .app-actions {
      display: flex;
      gap: 4px;
    }
  }
}
</style>
