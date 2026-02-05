<template>
  <el-dialog
    v-model="visible"
    title="添加好友"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="add-friend-content">
      <!-- 搜索用户 -->
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="输入手机号/用户名/邮箱搜索"
          :prefix-icon="Search"
          clearable
          size="large"
          @keyup.enter="handleSearch"
          @input="handleSearchInput"
        >
          <template #append>
            <el-button
              :icon="Search"
              :loading="searching"
              @click="handleSearch"
            >
              搜索
            </el-button>
          </template>
        </el-input>

        <!-- 搜索提示 -->
        <div class="search-tips">
          <el-icon><InfoFilled /></el-icon>
          <span>可以通过手机号、用户名或邮箱查找好友</span>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div
        v-if="searchResults.length > 0"
        class="search-results"
      >
        <div
          v-for="user in searchResults"
          :key="user.id"
          class="user-result-item"
          :class="{ selected: selectedUser?.id === user.id }"
          @click="selectUser(user)"
        >
          <DingtalkAvatar
            :src="user.avatar"
            :name="user.name || user.nickname"
            :size="48"
          />
          <div class="user-info">
            <div class="user-name">
              {{ user.name || user.nickname }}
            </div>
            <div class="user-desc">
              {{ user.dept || user.position || '这个人很懒~' }}
            </div>
          </div>
          <el-button
            v-if="selectedUser?.id === user.id"
            type="primary"
            :icon="Check"
            circle
            size="small"
          />
        </div>
      </div>

      <!-- 无结果状态 -->
      <div
        v-else-if="searched && !searching"
        class="no-results"
      >
        <el-empty
          :description="searchQuery ? '未找到用户' : '请输入搜索内容'"
          :image-size="80"
        />
      </div>

      <!-- 验证消息 -->
      <div
        v-if="selectedUser"
        class="verify-section"
      >
        <div class="section-title">
          <span>发送给</span>
          <span class="target-name">{{ selectedUser.name || selectedUser.nickname }}</span>
        </div>
        <el-input
          v-model="verifyMessage"
          type="textarea"
          :rows="3"
          placeholder="请输入验证消息（选填）"
          maxlength="100"
          show-word-limit
          resize="none"
        />
      </div>

      <!-- 最近联系 / 推荐 -->
      <div
        v-if="!searched && !selectedUser"
        class="recommend-section"
      >
        <div class="section-title">
          可能认识的人
        </div>
        <div
          v-if="recommendations.length > 0"
          class="recommend-list"
        >
          <div
            v-for="user in recommendations.slice(0, 3)"
            :key="user.id"
            class="recommend-item"
            @click="selectUser(user)"
          >
            <DingtalkAvatar
              :src="user.avatar"
              :name="user.name"
              :size="40"
            />
            <div class="recommend-info">
              <span class="recommend-name">{{ user.name }}</span>
              <span class="recommend-reason">{{ user.reason }}</span>
            </div>
            <el-button
              size="small"
              round
              @click.stop="selectUser(user)"
            >
              添加
            </el-button>
          </div>
        </div>
        <el-button
          v-else
          link
          type="primary"
          @click="showAllRecommended"
        >
          查看更多推荐
        </el-button>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">
          取消
        </el-button>
        <el-button
          type="primary"
          :disabled="!selectedUser"
          :loading="sending"
          @click="handleSendRequest"
        >
          发送好友请求
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search, InfoFilled, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { sendFriendRequest, getRecommendedContacts } from '@/api/im/contact'
import { searchUsers } from '@/api/im/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const searchQuery = ref('')
const searchResults = ref([])
const selectedUser = ref(null)
const verifyMessage = ref('')
const searching = ref(false)
const sending = ref(false)
const searched = ref(false)
const recommendations = ref([])

// 搜索防抖定时器
let searchTimer = null

// 同步v-model
watch(() => props.modelValue, val => {
  visible.value = val
  if (val) {
    loadRecommendations()
  }
})

watch(visible, val => {
  emit('update:modelValue', val)
})

// 加载推荐用户
const loadRecommendations = async () => {
  try {
    const res = await getRecommendedContacts({ type: 'department', limit: 5 })
    if (res.code === 200) {
      recommendations.value = res.data || []
    }
  } catch (error) {
    console.error('加载推荐失败:', error)
  }
}

// 处理搜索输入
const handleSearchInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    if (searchQuery.value.trim().length >= 2) {
      handleSearch()
    }
  }, 300)
}

// 执行搜索
const handleSearch = async () => {
  const query = searchQuery.value.trim()
  if (!query) {
    ElMessage.warning('请输入搜索内容')
    return
  }

  searching.value = true
  searched.value = true

  try {
    const res = await searchUsers(query)
    if (res.code === 200) {
      searchResults.value = res.data || []
      if (searchResults.value.length === 0) {
        ElMessage.info('未找到匹配的用户')
      }
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

// 选择用户
const selectUser = user => {
  selectedUser.value = user
  verifyMessage.value = `你好，我是${getCurrentUserName()}，想加你为好友`
}

// 获取当前用户名称（应该从store中获取）
const getCurrentUserName = () => {
  return localStorage.getItem('nickname') || '我'
}

// 发送好友请求
const handleSendRequest = async () => {
  if (!selectedUser.value) {
    ElMessage.warning('请先选择要添加的用户')
    return
  }

  sending.value = true
  try {
    await sendFriendRequest({
      userId: selectedUser.value.id,
      remark: verifyMessage.value
    })
    ElMessage.success('好友请求已发送')
    emit('success')
    handleClose()
  } catch (error) {
    ElMessage.error(error.msg || '发送失败')
  } finally {
    sending.value = false
  }
}

// 显示全部推荐
const showAllRecommended = () => {
  emit('update:modelValue', false)
  // 触发父组件切换到推荐页面
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  searchQuery.value = ''
  searchResults.value = []
  selectedUser.value = null
  verifyMessage.value = ''
  searched.value = false
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.add-friend-content {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-2xl);
}

.search-section {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
}

.search-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--dt-font-size-xs);
  color: var(--dt-color-text-secondary);

  .el-icon {
    font-size: var(--dt-font-size-sm);
  }
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
  max-height: 200px;
  overflow-y: auto;
}

.user-result-item {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  padding: var(--dt-spacing-md);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-color-primary);
    background: var(--dt-color-primary-light);
  }

  &.selected {
    border-color: var(--dt-color-primary);
    background: var(--dt-color-primary-light);
  }
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  color: var(--dt-color-text-primary);
}

.user-desc {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-color-text-secondary);
}

.no-results {
  padding: var(--dt-spacing-xl) 0;
}

.verify-section {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
  padding: var(--dt-spacing-lg);
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
}

.section-title {
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  color: var(--dt-color-text-primary);

  .target-name {
    color: var(--dt-color-primary);
    margin-left: 4px;
  }
}

.recommend-section {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
}

.recommend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: var(--dt-spacing-md);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }
}

.recommend-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.recommend-name {
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  color: var(--dt-color-text-primary);
}

.recommend-reason {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-color-text-secondary);
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
