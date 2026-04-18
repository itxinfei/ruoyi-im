<template>
  <el-dialog
    v-model="visible"
    width="440px"
    :show-close="false"
    class="premium-profile-modal"
    append-to-body
    destroy-on-close
  >
    <div v-loading="loading" class="v4-profile-shell">
      <div v-if="userDetail" class="v4-profile-content">
        
        <!-- 1. 沉浸式头部 (对齐钉钉 8.2 Premium) -->
        <header class="v4-header-banner">
          <div class="banner-bg"></div>
          <el-icon class="close-btn" @click="visible = false"><Close /></el-icon>
          <div class="avatar-stack">
            <DingtalkAvatar :src="userDetail.avatar" :name="userDetail.nickname" :size="72" shape="square" class="v4-main-avatar" />
            <div class="status-indicator online"></div>
          </div>
        </header>

        <!-- 2. 身份信息区 -->
        <main class="v4-info-body custom-scrollbar">
          <div class="primary-info">
            <h2 class="u-name">{{ userDetail.nickname || userDetail.username }}</h2>
            <div class="u-org">
              <span class="dept">{{ userDetail.departmentName || '总公司' }}</span>
              <span class="sep">·</span>
              <span class="pos">{{ userDetail.position || '成员' }}</span>
            </div>
          </div>

          <div class="info-sections">
            <div class="section-block">
              <label>工号</label>
              <div class="val">{{ userDetail.username }}</div>
            </div>
            <div class="section-block">
              <label>手机</label>
              <div class="val">{{ userDetail.mobile || '保密' }}</div>
            </div>
            <div class="section-block">
              <label>邮箱</label>
              <div class="val">{{ userDetail.email || '未设置' }}</div>
            </div>
            <div v-if="userDetail.signature" class="section-block">
              <label>个性签名</label>
              <p class="val signature">{{ userDetail.signature }}</p>
            </div>
          </div>
        </main>

        <!-- 3. 底部浮动操作中心 (Pills) -->
        <footer class="v4-action-footer">
          <div class="action-pills">
            <button class="pill-btn chat" @click="handleStartChat">
              <el-icon><ChatDotRound /></el-icon>
              <span>发消息</span>
            </button>
            <div class="pill-divider"></div>
            <button class="pill-btn icon-only" title="语音通话" @click="handleStartCall('voice')">
              <el-icon><Phone /></el-icon>
            </button>
            <button class="pill-btn icon-only" title="视频通话" @click="handleStartCall('video')">
              <el-icon><VideoCamera /></el-icon>
            </button>
            <el-dropdown trigger="click" @command="handleMore">
              <button class="pill-btn icon-only more"><el-icon><MoreFilled /></el-icon></button>
              <template #dropdown>
                <el-dropdown-menu class="v4-profile-more">
                  <el-dropdown-item command="star">设为星标</el-dropdown-item>
                  <el-dropdown-item command="remark">设置备注</el-dropdown-item>
                  <el-dropdown-item command="block" divided>加入黑名单</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </footer>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="js">
import { ref, watch } from 'vue'
import { Close, ChatDotRound, Phone, VideoCamera, MoreFilled } from '@element-plus/icons-vue'
import { getUserInfo } from '@/api/im/user'
import { createConversation } from '@/api/im/conversation'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({ modelValue: Boolean, userId: [String, Number] })
const emit = defineEmits(['update:modelValue'])

const store = useStore()
const visible = ref(false)
const loading = ref(false)
const userDetail = ref(null)

const loadData = async () => {
  if (!props.userId) return
  loading.value = true
  try {
    const res = await getUserInfo(props.userId)
    if (res.code === 200) userDetail.value = res.data
  } finally { loading.value = false }
}

const handleStartChat = async () => {
  const res = await createConversation({ type: 'PRIVATE', targetId: props.userId })
  if (res.code === 200) {
    store.commit('im/session/SET_CURRENT_SESSION', res.data)
    visible.value = false
  }
}

watch(() => props.modelValue, (v) => { visible.value = v; if (v) loadData() })
watch(visible, (v) => emit('update:modelValue', v))
</script>

<style scoped lang="scss">
.premium-profile-modal {
  :deep(.el-dialog) { border-radius: 16px; overflow: hidden; padding: 0; box-shadow: var(--dt-shadow-3); background: #fff; }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 0; }
}

.v4-profile-shell { height: 560px; display: flex; flex-direction: column; }

// 1. Banner
.v4-header-banner {
  height: 140px; position: relative;
  .banner-bg { height: 100%; background: linear-gradient(135deg, #277efb 0%, #4facfe 100%); opacity: 0.9; }
  .close-btn { position: absolute; top: 16px; right: 16px; color: #fff; cursor: pointer; font-size: 20px; z-index: 10; &:hover { opacity: 0.8; } }
  
  .avatar-stack {
    position: absolute; left: 24px; bottom: -30px; z-index: 5;
    .v4-main-avatar { border: 4px solid #fff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
    .status-indicator { position: absolute; bottom: 2px; right: 2px; width: 14px; height: 14px; border: 2.5px solid #fff; border-radius: 50%; &.online { background: #22ab5c; } }
  }
}

// 2. Body
.v4-info-body {
  flex: 1; padding: 40px 24px 20px; overflow-y: auto;
  
  .primary-info { margin-bottom: 24px; 
    .u-name { font-size: 22px; font-weight: 700; color: #1d1d1f; margin-bottom: 4px; }
    .u-org { font-size: 13px; color: #86868b; .sep { margin: 0 6px; } }
  }
  
  .info-sections {
    .section-block { margin-bottom: 18px; 
      label { display: block; font-size: 11px; color: #aaa; margin-bottom: 4px; text-transform: uppercase; font-weight: 600; }
      .val { font-size: 14px; color: #1d1d1f; &.signature { line-height: 1.5; color: #5f6368; } }
    }
  }
}

// 3. Footer Actions (Pills)
.v4-action-footer {
  padding: 0 24px 32px; display: flex; justify-content: center;
  
  .action-pills {
    height: 44px; background: #fff; border: 1px solid rgba(0,0,0,0.08); border-radius: 22px;
    padding: 4px; display: flex; align-items: center; box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    
    .pill-btn {
      height: 36px; border: none; background: transparent; cursor: pointer; border-radius: 18px;
      display: flex; align-items: center; gap: 8px; padding: 0 16px;
      transition: all 0.2s; color: #1d1d1f; font-weight: 600;
      
      &:hover { background: #f5f5f7; }
      &.chat { background: var(--dt-brand-color); color: #fff; &:hover { background: var(--dt-brand-hover); } }
      &.icon-only { width: 36px; padding: 0; @include flex-center; font-size: 18px; color: #555; }
    }
    
    .pill-divider { width: 1px; height: 16px; background: rgba(0,0,0,0.08); margin: 0 4px; }
  }
}
</style>
