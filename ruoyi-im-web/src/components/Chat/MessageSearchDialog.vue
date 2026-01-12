<!--
  消息搜索对话框组件
  功能：搜索聊天记录
  用途：支持消息搜索功能（需求文档4.2.1中提到的功能之一）
-->
<template>
  <el-dialog
    v-model="visible"
    title="搜索聊天记录"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="search-dialog">
      <!-- 搜索输入 -->
      <div class="search-input">
        <el-input
          v-model="keyword"
          placeholder="搜索消息内容"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 搜索条件 -->
      <div class="search-filters">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="small"
          clearable
        />
        <el-select v-model="searchType" placeholder="消息类型" size="small" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="文字" value="text" />
          <el-option label="图片" value="image" />
          <el-option label="文件" value="file" />
        </el-select>
      </div>

      <!-- 搜索结果 -->
      <div class="search-results">
        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>搜索中...</span>
        </div>
        <div v-else-if="results.length === 0 && searched" class="empty">
          <el-empty description="未找到相关消息" />
        </div>
        <div v-else class="result-list">
          <div
            v-for="item in results"
            :key="item.id"
            class="result-item"
            @click="handleClick(item)"
          >
            <div class="result-header">
              <el-avatar :size="32" :src="item.senderAvatar">
                {{ item.senderName?.charAt(0) }}
              </el-avatar>
              <div class="result-info">
                <div class="sender-name">{{ item.senderName }}</div>
                <div class="send-time">{{ formatTime(item.timestamp) }}</div>
              </div>
            </div>
            <div class="result-content">
              <div v-if="item.type === 'text'" class="text-content" v-html="highlightKeyword(item.content)"></div>
              <div v-else-if="item.type === 'image'" class="image-content">
                <el-image :src="item.content" fit="cover" style="width: 80px; height: 80px" />
              </div>
              <div v-else class="other-content">[{{ item.type }}消息]</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Search, Loading } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: Boolean,
  sessionId: [String, Number],
})

const emit = defineEmits(['update:modelValue', 'select'])

const store = useStore()
const visible = ref(false)
const keyword = ref('')
const dateRange = ref(null)
const searchType = ref('')
const results = ref([])
const loading = ref(false)
const searched = ref(false)

watch(() => props.modelValue, val => { visible.value = val })
watch(visible, val => { emit('update:modelValue', val) })

async function handleSearch() {
  if (!keyword.value.trim()) return
  
  loading.value = true
  searched.value = true
  
  try {
    // TODO: 调用搜索API
    // const res = await store.dispatch('im/searchMessages', {
    //   sessionId: props.sessionId,
    //   keyword: keyword.value,
    //   startDate: dateRange.value?.[0],
    //   endDate: dateRange.value?.[1],
    //   type: searchType.value,
    // })
    // results.value = res
    
    // 模拟数据
    results.value = []
  } finally {
    loading.value = false
  }
}

function handleClick(item) {
  emit('select', item)
  handleClose()
}

function highlightKeyword(text) {
  if (!keyword.value) return text
  const regex = new RegExp(`(${keyword.value})`, 'gi')
  return text.replace(regex, '<span style="color: #1890FF; background: #e6f7ff;">$1</span>')
}

function formatTime(timestamp) {
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm')
}

function handleClose() {
  visible.value = false
  keyword.value = ''
  results.value = []
  searched.value = false
}
</script>

<style lang="scss" scoped>
.search-dialog {
  .search-input { margin-bottom: 16px; }
  
  .search-filters {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .search-results {
    max-height: 400px;
    overflow-y: auto;
    
    .loading {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 40px 0;
      color: #999;
    }
    
    .result-list {
      .result-item {
        padding: 12px;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: background 0.2s;
        
        &:hover { background: #f5f7fa; }
        
        .result-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          
          .result-info {
            .sender-name { font-size: 14px; color: #333; }
            .send-time { font-size: 12px; color: #999; }
          }
        }
        
        .result-content {
          padding-left: 40px;
          
          .text-content { font-size: 14px; color: #666; }
          .other-content { font-size: 12px; color: #999; }
        }
      }
    }
  }
}
</style>
