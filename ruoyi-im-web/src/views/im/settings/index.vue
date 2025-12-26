<template>
  <div class="settings-container">
    <el-tabs v-model="activeTab" tab-position="left" class="settings-tabs">
      <el-tab-pane :label="$t('settings.basic')" name="basic">
        <basic-settings />
      </el-tab-pane>

      <el-tab-pane :label="$t('settings.theme')" name="theme">
        <theme-switch />
      </el-tab-pane>

      <el-tab-pane :label="$t('settings.notification')" name="notification">
        <notification-settings />
      </el-tab-pane>

      <el-tab-pane :label="$t('settings.privacy')" name="privacy">
        <privacy-settings />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import BasicSettings from './Basic'
import ThemeSwitch from '@/components/ThemeSwitch'
import NotificationSettings from './Notification'
import PrivacySettings from './Privacy'

export default {
  name: 'Settings',
  components: {
    BasicSettings,
    ThemeSwitch,
    NotificationSettings,
    PrivacySettings,
  },
  data() {
    return {
      activeTab: 'basic',
    }
  },
  watch: {
    activeTab(val) {
      // 更新路由参数
      this.$router.push({
        query: { ...this.$route.query, tab: val },
      })
    },
  },
  created() {
    // 从路由参数中获取要激活的标签
    const { tab } = this.$route.query
    if (tab) {
      this.activeTab = tab
    }
  },
}
</script>

<style lang="scss" scoped>
.settings-container {
  padding: 20px;
  height: 100%;
  background-color: var(--background-color);

  .settings-tabs {
    height: 100%;
    background-color: var(--header-background);
    border-radius: 8px;
    box-shadow: var(--box-shadow);

    :deep(.el-tabs__header) {
      margin-right: 0;
      border-right: 1px solid var(--border-color);

      .el-tabs__item {
        height: 50px;
        line-height: 50px;
        color: var(--text-regular);

        &.is-active {
          color: var(--primary-color);
          background-color: var(--primary-light);
        }

        &:hover {
          color: var(--primary-color);
        }
      }
    }

    :deep(.el-tabs__content) {
      padding: 20px;
      height: 100%;
      overflow-y: auto;
    }
  }
}
</style>
