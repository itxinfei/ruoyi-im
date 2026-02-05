<template>
  <div class="recommended-contacts">
    <!-- 头部 -->
    <div class="recommend-header">
      <h3 class="header-title">
        <el-icon><UserFilled /></el-icon>
        可能认识的人
      </h3>
      <el-button
        link
        type="primary"
        :loading="loading"
        @click="refreshRecommendations"
      >
        <el-icon><Refresh /></el-icon>
        换一批
      </el-button>
    </div>

    <!-- 推荐列表 -->
    <div
      v-if="recommendations.length > 0"
      class="recommend-list"
    >
      <div
        v-for="item in recommendations"
        :key="item.id"
        class="recommend-item"
      >
        <DingtalkAvatar
          :src="item.avatar"
          :name="item.name || item.nickname"
          :size="48"
        />
        <div class="recommend-info">
          <div class="recommend-name">
            {{ item.name || item.nickname }}
          </div>
          <div class="recommend-reason">
            <el-icon class="reason-icon">
              <Connection />
            </el-icon>
            {{ item.reason }}
          </div>
          <div
            v-if="item.dept"
            class="recommend-dept"
          >
            {{ item.dept }}
          </div>
        </div>
        <div class="recommend-actions">
          <el-button
            v-if="!item.requested"
            type="primary"
            size="small"
            round
            :loading="item.adding"
            @click="addFriend(item)"
          >
            添加
          </el-button>
          <el-button
            v-else
            size="small"
            round
            disabled
          >
            已发送
          </el-button>
          <el-button
            circle
            size="small"
            :icon="Close"
            class="dismiss-btn"
            @click="dismiss(item)"
          />
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div
      v-else-if="!loading"
      class="empty-recommendations"
    >
      <el-empty
        :image-size="100"
        description="暂无推荐"
      >
        <template #image>
          <div class="empty-icon">
            <el-icon><User /></el-icon>
          </div>
        </template>
      </el-empty>
    </div>

    <!-- 加载状态 -->
    <div
      v-if="loading && recommendations.length === 0"
      class="loading-state"
    >
      <el-skeleton
        :rows="3"
        animated
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { UserFilled, Refresh, Connection, Close, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getRecommendedContacts, sendFriendRequest } from '@/api/im/contact'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const emit = defineEmits(['update'])

const loading = ref(false)
const recommendations = ref([])

// 加载推荐列表
const loadRecommendations = async () => {
  loading.value = true
  try {
    const res = await getRecommendedContacts({ type: 'all', limit: 10 })
    if (res.code === 200) {
      recommendations.value = (res.data || []).map(item => ({
        ...item,
        requested: false,
        adding: false
      }))
    }
  } catch (error) {
    console.error('加载推荐失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新推荐
const refreshRecommendations = () => {
  loadRecommendations()
}

// 添加好友
const addFriend = async item => {
  item.adding = true
  try {
    await sendFriendRequest({
      userId: item.id,
      remark: `你好，我是${item.myName || ''}`
    })
    item.requested = true
    ElMessage.success('好友请求已发送')
    emit('update')
  } catch (error) {
    ElMessage.error(error.msg || '添加失败')
  } finally {
    item.adding = false
  }
}

// 忽略推荐
const dismiss = item => {
  const index = recommendations.value.findIndex(r => r.id === item.id)
  if (index > -1) {
    recommendations.value.splice(index, 1)
  }
}

onMounted(() => {
  loadRecommendations()
})

// 暴露刷新方法供父组件调用
defineExpose({
  refresh: loadRecommendations
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.recommended-contacts {
  padding: var(--dt-spacing-lg);
}

.recommend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--dt-spacing-lg);
  padding-bottom: var(--dt-spacing-md);
  border-bottom: 1px solid var(--dt-border-color-light);

  .header-title {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-md);
    font-size: 15px;
    font-weight: 600;
    color: var(--dt-color-text-primary);
    margin: 0;
  }
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
}

.recommend-item {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  padding: var(--dt-spacing-md);
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-card);
    transform: translateX(4px);
  }
}

.recommend-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recommend-name {
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  color: var(--dt-color-text-primary);
}

.recommend-reason {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--dt-font-size-xs);
  color: var(--dt-color-primary);

  .reason-icon {
    font-size: var(--dt-font-size-sm);
  }
}

.recommend-dept {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-color-text-secondary);
}

.recommend-actions {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);

  .dismiss-btn {
    color: var(--dt-color-text-placeholder);

    &:hover {
      color: var(--dt-color-danger);
    }
  }
}

.empty-recommendations {
  padding: var(--dt-spacing-2xl) 0;

  .empty-icon {
    width: 80px;
    height: 80px;
    margin: 0 auto var(--dt-spacing-lg);
    border-radius: var(--dt-radius-full);
    background: var(--dt-bg-hover);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    color: var(--dt-color-text-placeholder);
  }
}

.loading-state {
  padding: var(--dt-spacing-lg) 0;
}
</style>
