<!--
  位置选择器组件
  功能：用户可以选择位置并分享给聊天对象
  用途：支持发送位置消息（需求文档4.2.1中提到的消息类型之一）
-->
<template>
  <el-dialog
    v-model="visible"
    title="发送位置"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="location-picker">
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索地点"
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>

      <!-- 地图区域 -->
      <div class="map-container">
        <div class="map-placeholder">
          <el-icon :size="48" color="#999"><Location /></el-icon>
          <p>地图组件</p>
          <p class="tip">需要集成地图SDK（如高德地图、百度地图）</p>
        </div>
        <!-- 当前位置标记 -->
        <div v-if="currentLocation" class="location-marker">
          <el-icon :size="24" color="#0089FF"><Location /></el-icon>
        </div>
      </div>

      <!-- 位置信息 -->
      <div class="location-info">
        <div class="location-name">{{ selectedLocation.name || '请选择位置' }}</div>
        <div class="location-address">{{ selectedLocation.address || '点击地图选择位置' }}</div>
      </div>

      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <div
          v-for="item in searchResults"
          :key="item.id"
          class="result-item"
          @click="selectLocation(item)"
        >
          <div class="result-name">{{ item.name }}</div>
          <div class="result-address">{{ item.address }}</div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :disabled="!selectedLocation.name" @click="handleConfirm">
        发送
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Search, Location } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(false)
const searchKeyword = ref('')
const selectedLocation = ref({})
const searchResults = ref([])
const currentLocation = ref(null)

// 监听外部v-model变化
watch(
  () => props.modelValue,
  val => {
    visible.value = val
    if (val) {
      getCurrentLocation()
    }
  },
  { immediate: true }
)

// 监听内部visible变化
watch(visible, val => {
  emit('update:modelValue', val)
})

// 获取当前位置
function getCurrentLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      position => {
        currentLocation.value = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        }
      },
      error => {
        console.error('获取位置失败:', error)
      }
    )
  }
}

// 搜索地点
function handleSearch() {
  // TODO: 调用地图API搜索
  // 这里使用模拟数据
  if (searchKeyword.value) {
    searchResults.value = [
      { id: 1, name: `${searchKeyword.value}-地点1`, address: '详细地址1' },
      { id: 2, name: `${searchKeyword.value}-地点2`, address: '详细地址2' },
    ]
  } else {
    searchResults.value = []
  }
}

// 选择位置
function selectLocation(item) {
  selectedLocation.value = { ...item }
  searchResults.value = []
  searchKeyword.value = ''
}

// 确认发送
function handleConfirm() {
  if (selectedLocation.value.name) {
    emit('confirm', {
      type: 'location',
      location: {
        name: selectedLocation.value.name,
        address: selectedLocation.value.address,
        lat: selectedLocation.value.lat || currentLocation.value?.lat,
        lng: selectedLocation.value.lng || currentLocation.value?.lng,
      },
    })
    handleClose()
  }
}

// 关闭对话框
function handleClose() {
  visible.value = false
  selectedLocation.value = {}
  searchResults.value = []
  searchKeyword.value = ''
}
</script>

<style lang="scss" scoped>
.location-picker {
  .search-box {
    margin-bottom: 16px;
  }

  .map-container {
    position: relative;
    width: 100%;
    height: 300px;
    background: #f0f2f5;
    border-radius: 8px;
    overflow: hidden;

    .map-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #999;

      .tip {
        font-size: 12px;
        margin-top: 8px;
      }
    }

    .location-marker {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
  }

  .location-info {
    margin-top: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;

    .location-name {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 4px;
    }

    .location-address {
      font-size: 12px;
      color: #999;
    }
  }

  .search-results {
    margin-top: 12px;
    max-height: 200px;
    overflow-y: auto;

    .result-item {
      padding: 10px 12px;
      cursor: pointer;
      border-radius: 4px;
      transition: background 0.2s;

      &:hover {
        background: #f5f7fa;
      }

      .result-name {
        font-size: 14px;
        color: #333;
        margin-bottom: 4px;
      }

      .result-address {
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
