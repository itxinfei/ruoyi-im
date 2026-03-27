<template>
  <el-dialog
    v-model="visible"
    width="720px"
    :show-close="false"
    class="spotlight-search-dialog"
    append-to-body
    @opened="onOpen"
  >
    <div class="spotlight-container">
      <!-- 搜索输入区 -->
      <div class="search-input-wrapper">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          ref="inputRef"
          v-model="query"
          type="text"
          placeholder="搜索联系人、群组、聊天记录..."
          @input="handleSearch"
          @keydown.down="moveNext"
          @keydown.up="movePrev"
          @keydown.enter="handleSelect"
        >
        <div class="search-kbd">
          <kbd>ESC</kbd>
        </div>
      </div>

      <!-- 搜索结果区 -->
      <div v-if="query" class="search-results custom-scrollbar">
        <!-- 分类：联系人 -->
        <div v-if="results.contacts.length" class="result-group">
          <div class="group-title">
            联系人
          </div>
          <div
            v-for="(item, idx) in results.contacts"
            :key="item.id"
            class="result-item"
            :class="{ active: currentIdx === idx }"
            @click="goToContact(item)"
          >
            <DingtalkAvatar
              :src="item.avatar"
              :name="item.nickname"
              :size="32"
              shape="square"
            />
            <div class="item-info">
              <span class="name" v-html="highlight(item.nickname)" />
              <span class="dept">{{ item.department || '研发部' }}</span>
            </div>
          </div>
        </div>

        <!-- 分类：群组 -->
        <div v-if="results.groups.length" class="result-group">
          <div class="group-title">
            群组
          </div>
          <div
            v-for="(item, idx) in results.groups"
            :key="item.id"
            class="result-item"
            :class="{ active: currentIdx === (results.contacts.length + idx) }"
            @click="goToGroup(item)"
          >
            <DingtalkAvatar
              :src="item.avatar"
              :name="item.name"
              :size="32"
              shape="square"
            />
            <div class="item-info">
              <span class="name" v-html="highlight(item.name)" />
              <span class="count">{{ item.memberCount }} 人</span>
            </div>
          </div>
        </div>

        <!-- 无结果 -->
        <div v-if="!hasResults" class="search-empty">
          <el-icon><Search /></el-icon>
          <p>未找到与 "{{ query }}" 相关的结果</p>
        </div>
      </div>

      <!-- 初始/引导状态 -->
      <div v-else class="search-guide">
        <div class="guide-tips">
          <div class="guide-item">
            <el-icon><Search /></el-icon>
            <span>找人</span>
          </div>
          <div class="guide-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>找群</span>
          </div>
          <div class="guide-item">
            <el-icon><Clock /></el-icon>
            <span>搜历史</span>
          </div>
        </div>
      </div>

      <!-- 底部快捷键提示 -->
      <div class="search-footer">
        <div class="shortcuts">
          <span><kbd>↑↓</kbd> 选择</span>
          <span><kbd>Enter</kbd> 跳转</span>
          <span><kbd>Esc</kbd> 关闭</span>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { highlightText } from '@/utils/htmlSanitizer'
import { Search, ChatDotRound, Clock } from '@element-plus/icons-vue'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const query = ref('')
const inputRef = ref(null)
const currentIdx = ref(0)
const results = ref({ contacts: [], groups: [], messages: [] })

const store = useStore()

const hasResults = computed(() => results.value.contacts.length || results.value.groups.length)

watch(() => props.modelValue, (val) => visible.value = val)
watch(visible, (val) => emit('update:modelValue', val))

const onOpen = () => {
  query.value = ''
  currentIdx.value = 0
  setTimeout(() => inputRef.value?.focus(), 50)
}

const handleSearch = () => {
  if (!query.value) return
  // 这里模拟搜索，实际应调用 store 或 API
  results.value.contacts = store.getters['im/contact/allContacts'].filter(c =>
    c.nickname.includes(query.value)
  ).slice(0, 5)
  results.value.groups = store.getters['im/session/allGroups']?.filter(g =>
    g.name.includes(query.value)
  ).slice(0, 5) || []
}

const highlight = (text) => {
  // 使用安全的高亮函数，防止 XSS 攻击
  return highlightText(text, query.value, 'mark', '')
}

const moveNext = () => currentIdx.value++
const movePrev = () => currentIdx.value = Math.max(0, currentIdx.value - 1)

const handleSelect = () => {
  // 执行跳转逻辑
}

const goToContact = (_c) => { visible.value = false /* 跳转 */ }
const goToGroup = (_g) => { visible.value = false /* 跳转 */ }
</script>

<style lang="scss">
.spotlight-search-dialog {
  .el-dialog__header { display: none; }
  .el-dialog__body { padding: 0 !important; background: transparent; }
  border-radius: var(--dt-radius-xl) !important; overflow: hidden;
}

.spotlight-container {
  background: var(--dt-bg-card); border-radius: var(--dt-radius-xl); box-shadow: var(--dt-shadow-float);
  display: flex; flex-direction: column; overflow: hidden;

  .search-input-wrapper {
    display: flex; align-items: center; padding: 16px 20px; border-bottom: 1px solid var(--dt-border-light);
    .search-icon { font-size: 24px; color: var(--dt-text-tertiary); margin-right: 12px; }
    input {
      flex: 1; border: none; outline: none; font-size: 18px; color: var(--dt-text-primary);
      &::placeholder { color: var(--dt-text-quaternary); }
    }
    .search-kbd { kbd { padding: 2px 6px; background: var(--dt-bg-hover); border-radius: var(--dt-radius-sm); color: var(--dt-text-secondary); font-size: 12px; } }
  }

  .search-results {
    max-height: 450px; overflow-y: auto; padding: 8px 0;

    .result-group {
      .group-title { padding: 12px 20px 4px; font-size: 12px; font-weight: 700; color: var(--dt-text-secondary); text-transform: uppercase; }
      .result-item {
        display: flex; align-items: center; gap: 12px; padding: 10px 20px; cursor: pointer;
        &:hover, &.active { background: var(--dt-brand-bg); }
        .item-info {
          display: flex; flex-direction: column;
          .name { font-size: 14px; font-weight: 600; color: var(--dt-text-primary); mark { background: #ffeb3b; padding: 0 2px; border-radius: var(--dt-radius-xs); } }
          .dept, .count { font-size: 12px; color: var(--dt-text-tertiary); }
        }
      }
    }
  }

  .search-guide {
    padding: 60px 0; text-align: center;
    .guide-tips {
      display: flex; justify-content: center; gap: 40px;
      .guide-item {
        display: flex; flex-direction: column; align-items: center; gap: 8px; color: var(--dt-text-secondary);
        .el-icon { font-size: 32px; }
      }
    }
  }

  .search-footer {
    padding: 12px 20px; background: var(--dt-bg-body); border-top: 1px solid var(--dt-border-light);
    .shortcuts {
      display: flex; gap: 16px; font-size: 12px; color: var(--dt-text-secondary);
      kbd { padding: 1px 4px; background: var(--dt-bg-card); border: 1px solid var(--dt-border-light); border-radius: var(--dt-radius-sm); font-family: inherit; }
    }
  }
}

.dark .spotlight-container {
  background: var(--dt-bg-card); border: 1px solid var(--dt-border-color);
  .search-input-wrapper { border-color: var(--dt-border-color); input { background: transparent; color: var(--dt-text-primary); } }
  .search-results .result-item { &:hover, &.active { background: var(--dt-bg-hover); } .item-info .name { color: var(--dt-text-primary); } }
  .search-footer { background: var(--dt-bg-body); border-color: var(--dt-border-color); }
}
</style>
