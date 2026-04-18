<template>
  <transition name="spotlight-fade">
    <div v-if="visible" class="dt-spotlight-overlay" @click.self="close">
      <div class="spotlight-container">
        <!-- 1. 搜索指令输入条 -->
        <header class="spotlight-header">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            ref="inputRef"
            v-model="keyword"
            class="spotlight-input"
            placeholder="搜索联系人、群聊、文件或功能..."
            @input="handleInput"
            @keydown.down.prevent="moveDown"
            @keydown.up.prevent="moveUp"
            @keydown.enter="handleConfirm"
            @keydown.esc="close"
          />
          <div class="shortcut-tip">ESC 退出</div>
        </header>

        <!-- 2. 动态结果区 -->
        <main v-if="keyword || hasHistory" class="spotlight-body custom-scrollbar">
          <div v-for="group in results" :key="group.type" class="res-group">
            <div class="group-title">{{ group.label }}</div>
            <div class="group-items">
              <div 
                v-for="(item, idx) in group.list" 
                :key="item.id" 
                class="res-item"
                :class="{ active: selectedId === item.id }"
                @mouseenter="selectedId = item.id"
                @click="handleConfirm(item)"
              >
                <div class="item-left">
                  <div class="icon-avatar" :class="group.type">
                    <el-icon v-if="group.type === 'file'"><Document /></el-icon>
                    <img v-else :src="item.avatar || '/avatars/default.png'" />
                  </div>
                  <div class="item-meta">
                    <div class="name" v-html="highlight(item.name)"></div>
                    <div class="sub">{{ item.sub }}</div>
                  </div>
                </div>
                <div v-if="selectedId === item.id" class="item-enter">
                  <span>打开</span>
                  <el-icon><Right /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </main>
        
        <!-- 3. 初始状态：功能导航 -->
        <footer v-else class="spotlight-footer">
          <div class="footer-hint">你可以搜索：</div>
          <div class="suggest-row">
            <span class="s-tag">@同事</span>
            <span class="s-tag">#群聊</span>
            <span class="s-tag">/功能</span>
          </div>
        </footer>
      </div>
    </div>
  </transition>
</template>

<script setup lang="js">
import { ref, watch, nextTick } from 'vue'
import { Search, Document, Right } from '@element-plus/icons-vue'

const props = defineProps({ visible: Boolean })
const emit = defineEmits(['update:visible', 'select'])

const keyword = ref('')
const inputRef = ref(null)
const selectedId = ref(null)
const results = ref([])

const close = () => emit('update:visible', false)

// 模拟大厂级即时反馈
const handleInput = () => {
  if (!keyword.value) { results.value = []; return }
  // 模拟搜索结果分组
  results.value = [
    { type: 'user', label: '联系人', list: [{ id: '1', name: '张小龙', sub: '产品部' }] },
    { type: 'group', label: '群聊', list: [{ id: 'g1', name: 'IM重构突击队', sub: '32人' }] }
  ]
  if (results.value.length > 0) selectedId.value = results.value[0].list[0].id
}

const highlight = (text) => text.replace(new RegExp(keyword.value, 'gi'), m => `<mark>${m}</mark>`)

watch(() => props.visible, (v) => {
  if (v) {
    keyword.value = ''
    nextTick(() => inputRef.value?.focus())
  }
})

const handleConfirm = (item) => {
  emit('select', item)
  close()
}
</script>

<style scoped lang="scss">
.dt-spotlight-overlay {
  position: fixed; inset: 0; background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(8px); z-index: 5000;
  display: flex; justify-content: center; padding-top: 15vh;
}

.spotlight-container {
  width: 640px; background: #fff; border-radius: 14px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  display: flex; flex-direction: column; overflow: hidden;
  border: 1px solid rgba(0,0,0,0.08);
}

.spotlight-header {
  height: 60px; display: flex; align-items: center; padding: 0 20px;
  border-bottom: 1px solid rgba(0,0,0,0.05); gap: 14px;
  
  .search-icon { font-size: 20px; color: var(--dt-brand-color); }
  .spotlight-input {
    flex: 1; border: none; background: transparent; outline: none;
    font-size: 18px; color: #1d1d1f;
    &::placeholder { color: #ccc; }
  }
  .shortcut-tip { font-size: 10px; color: #aaa; background: #f5f5f5; padding: 2px 6px; border-radius: 4px; }
}

.spotlight-body {
  max-height: 480px; overflow-y: auto; padding: 12px 8px;
}

.res-group {
  margin-bottom: 16px;
  .group-title { font-size: 11px; font-weight: 700; color: #999; padding: 0 12px 8px; text-transform: uppercase; }
}

.res-item {
  height: 52px; display: flex; align-items: center; justify-content: space-between;
  padding: 0 12px; border-radius: 8px; cursor: pointer; transition: 0.1s;
  
  &.active { background: var(--dt-brand-bg); }
  
  .item-left {
    display: flex; align-items: center; gap: 12px;
    .icon-avatar {
      width: 32px; height: 32px; border-radius: 6px; overflow: hidden; @include flex-center;
      img { width: 100%; height: 100%; }
      &.user { border-radius: 4px; }
      &.file { background: #fef0f0; color: #f56c6c; }
    }
    .item-meta {
      .name { font-size: 14px; color: #1d1d1f; :deep(mark) { background: transparent; color: var(--dt-brand-color); font-weight: 700; } }
      .sub { font-size: 11px; color: #86868b; margin-top: 1px; }
    }
  }
  
  .item-enter { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--dt-brand-color); font-weight: 600; }
}

.spotlight-footer {
  padding: 16px 20px; background: #f9f9f9; border-top: 1px solid rgba(0,0,0,0.03);
  .footer-hint { font-size: 11px; color: #999; margin-bottom: 8px; }
  .suggest-row { display: flex; gap: 8px; .s-tag { font-size: 11px; color: #666; background: #fff; border: 1px solid #eee; padding: 2px 8px; border-radius: 4px; } }
}

// 动画
.spotlight-fade-enter-active { transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); }
.spotlight-fade-enter-from { opacity: 0; transform: translateY(-20px) scale(0.98); }
</style>
