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
  padding: 24px;
}

.recommend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--dt-border-lighter);

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
    
    .el-icon { color: var(--dt-brand-color); }
  }
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-lighter);
  border-radius: 12px;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
    border-color: var(--dt-brand-light);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
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
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.recommend-reason {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--dt-brand-color);
  font-weight: 500;

  .reason-icon {
    font-size: 14px;
  }
}

.recommend-dept {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.recommend-actions {
  display: flex;
  align-items: center;
  gap: 12px;

  .dismiss-btn {
    color: var(--dt-text-quaternary);
    border-color: transparent;
    background: transparent;

    &:hover {
      color: var(--dt-error-color);
      background: var(--dt-error-bg);
    }
  }
}

.empty-recommendations {
  padding: 60px 0;

  .empty-icon {
    width: 100px;
    height: 100px;
    margin: 0 auto 16px;
    border-radius: 50%;
    background: var(--dt-bg-body);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    color: var(--dt-text-quaternary);
  }
}

.loading-state {
  padding: var(--dt-spacing-lg) 0;
}
</style>
