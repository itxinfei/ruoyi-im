<template>
  <el-drawer
    v-model="visible"
    size="320px"
    :with-header="false"
    class="group-detail-drawer"
  >
    <!-- Header区 -->
    <header class="drawer-header">
      <span class="title">群详情</span>
      <el-icon class="close-icon" @click="visible = false"><Close /></el-icon>
    </header>

    <div class="drawer-content">
      <!-- Section 1: 基础/成员九宫格 -->
      <section class="section base-section">
        <div class="group-info">
          <img :src="groupData.avatar || '/avatars/group.png'" class="group-avatar" alt="avatar" />
          <h3 class="group-name">{{ groupData.name }}</h3>
          <p class="group-desc">{{ groupData.description || '暂无简介' }}</p>
        </div>

        <div class="member-grid-header">
          <span>群成员 ({{ members.length }})</span>
          <span class="view-all" @click="processViewAllMembers">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </span>
        </div>
        <div class="member-grid">
          <div v-for="member in displayMembers" :key="member.userId" class="member-item">
            <img :src="member.avatar || '/avatars/default.png'" class="member-avatar" alt="avatar" />
            <span class="member-name">{{ member.nickname }}</span>
          </div>
          <!-- 添加按钮 -->
          <div class="member-item add-btn" @click="processAddMember">
            <div class="add-icon"><el-icon><Plus /></el-icon></div>
            <span class="member-name">添加</span>
          </div>
        </div>
      </section>

      <div class="section-divider"></div>

      <!-- Section 2: 功能矩阵 -->
      <section class="section func-section">
        <div class="func-item" @click="filesDrawerVisible = true">
          <span>群文件</span>
          <div class="func-right">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="func-item">
          <span>群公告</span>
          <div class="func-right">
            <span class="desc-text text-ellipsis">{{ groupData.announcement || '未设置' }}</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </section>

      <div class="section-divider"></div>

      <!-- Section 3: 设置区 -->
      <section class="section setting-section">
        <div class="setting-item">
          <span>消息免打扰</span>
          <el-switch v-model="settings.isMuted" size="small" style="--el-switch-on-color: var(--dt-brand-color);" />
        </div>
        <div class="setting-item">
          <span>置顶聊天</span>
          <el-switch v-model="settings.isPinned" size="small" style="--el-switch-on-color: var(--dt-brand-color);" />
        </div>
      </section>

      <div class="section-divider"></div>

      <!-- 危险操作 -->
      <section class="section danger-section">
        <button v-if="isOwner" class="danger-btn" @click="processDismissGroup">解散群聊</button>
        <button v-else class="danger-btn" @click="processQuitGroup">退出群聊</button>
      </section>
    </div>

    <!-- 内嵌嵌套抽屉: 群文件 -->
    <GroupFilesDrawer v-model="filesDrawerVisible" :group-id="groupId" />
  </el-drawer>
</template>

<script setup lang="js">
/**
 * GroupDetailDrawer.vue (对齐钉钉 UI)
 */
import { ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, ArrowRight, Plus } from '@element-plus/icons-vue';
import GroupFilesDrawer from './GroupFilesDrawer.vue';

const props = defineProps({
  modelValue: Boolean,
  groupId: [Number, String]
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const filesDrawerVisible = ref(false);

const groupData = ref({
  name: '项目研发核心群',
  description: '钉钉复刻版核心团队',
  avatar: '',
  ownerId: 1
});

const members = ref([
  { userId: 1, nickname: '张三', avatar: '/avatars/user1.png' },
  { userId: 2, nickname: '李四', avatar: '/avatars/user2.png' }
]);

const settings = ref({
  isMuted: false,
  isPinned: true
});

const isOwner = computed(() => groupData.value.ownerId === 1);
const displayMembers = computed(() => members.value.slice(0, 7)); // 最多展示7个 + 1个添加按钮 = 8个

const processAddMember = () => ElMessage.info('添加成员功能开发中');
const processViewAllMembers = () => ElMessage.info('查看全部成员');

const processQuitGroup = () => {
  ElMessageBox.confirm('确定退出群聊？', '提示', { type: 'warning' }).then(() => {
    ElMessage.success('已退出');
    visible.value = false;
  });
};

const processDismissGroup = () => {
  ElMessageBox.confirm('确定解散群聊？', '警告', { type: 'error' }).then(() => {
    ElMessage.success('已解散');
    visible.value = false;
  });
};
</script>

<style scoped>
.drawer-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-light);
  background-color: var(--dt-bg-card);
}

.drawer-header .title {
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
}

.close-icon {
  cursor: pointer;
  color: var(--dt-text-tertiary);
  font-size: 18px;
  transition: color var(--dt-transition-fast);
}

.close-icon:hover {
  color: var(--dt-text-primary);
}

.drawer-content {
  height: calc(100% - 60px);
  overflow-y: auto;
  background-color: var(--dt-bg-card);
}

.section {
  padding: 20px;
}

.section-divider {
  height: 8px;
  background-color: var(--dt-bg-body);
}

.group-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.group-avatar {
  width: 56px;
  height: 56px;
  border-radius: var(--dt-radius-sm);
  margin-bottom: 12px;
  background: var(--dt-border-lighter);
  object-fit: cover;
}

.group-name {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.group-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.member-grid-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin-bottom: 16px;
}

.view-all {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color var(--dt-transition-fast);
}

.view-all:hover {
  color: var(--dt-brand-color);
}

/* 九宫格布局：4列 */
.member-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 8px;
}

.member-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.member-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-sm);
  object-fit: cover;
}

.add-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-sm);
  border: 1px dashed var(--dt-border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-tertiary);
  background-color: var(--dt-bg-body);
  transition: all var(--dt-transition-fast);
}

.member-item:hover .add-icon {
  border-color: var(--dt-brand-color);
  color: var(--dt-brand-color);
}

.member-name {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.func-item, .setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  cursor: pointer;
}

.func-item:hover {
  background-color: var(--dt-bg-hover);
  margin: 0 -20px;
  padding: 0 20px;
}

.func-right {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--dt-text-tertiary);
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 120px;
  font-size: var(--dt-font-size-sm);
}

.danger-btn {
  width: 100%;
  height: 40px;
  border: none;
  background: transparent;
  color: var(--dt-error-color);
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  cursor: pointer;
  border-radius: var(--dt-radius-sm);
  transition: background-color var(--dt-transition-fast);
}

.danger-btn:hover {
  background-color: var(--dt-error-bg);
}

.drawer-content::-webkit-scrollbar { width: 4px; }
.drawer-content::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.05); border-radius: 2px; }
</style>
