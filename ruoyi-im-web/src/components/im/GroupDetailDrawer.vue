<template>
  <el-drawer
    v-model="visible"
    title="群详情"
    size="320px"
    :with-header="false"
    class="group-detail-drawer"
  >
    <!-- Header区 (Doc-21 §10.1) -->
    <header class="drawer-header">
      <span class="title">群详情</span>
      <i class="el-icon-close close-icon" @click="visible = false"></i>
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
          <span class="view-all" @click="processViewAllMembers">查看全部 <i class="el-icon-arrow-right"></i></span>
        </div>
        <div class="member-grid">
          <div v-for="member in displayMembers" :key="member.userId" class="member-item">
            <img :src="member.avatar" class="member-avatar" alt="avatar" />
            <span class="member-name">{{ member.nickname }}</span>
          </div>
          <div class="member-item add-btn" @click="processAddMember">
            <div class="add-icon">+</div>
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
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
        <div class="func-item">
          <span>群公告</span>
          <div class="func-right">
            <span class="desc-text text-ellipsis">{{ groupData.announcement || '未设置' }}</span>
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
      </section>

      <div class="section-divider"></div>

      <!-- Section 3: 设置区 -->
      <section class="section setting-section">
        <div class="setting-item">
          <span>消息免打扰</span>
          <el-switch v-model="settings.isMuted" size="small" />
        </div>
        <div class="setting-item">
          <span>置顶聊天</span>
          <el-switch v-model="settings.isPinned" size="small" />
        </div>
      </section>

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
 * GroupDetailDrawer.vue (修正版)
 * 1. 彻底肃清标识符重复声明
 * 2. 严格对齐 Doc-21 业务抽屉规范
 */
import { ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
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

// 模拟数据 (应对齐 Doc-31 实体)
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

const isOwner = computed(() => groupData.value.ownerId === 1); // 模拟当前用户ID为1
const displayMembers = computed(() => members.value.slice(0, 8));

// 业务动词命名 (禁止 handle 前缀)
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
  height: 60px; padding: 0 20px; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-color);
}
.drawer-header .title { font-size: 16px; font-weight: 600; }
.close-icon { cursor: pointer; color: var(--dt-text-desc); }

.drawer-content { height: calc(100% - 60px); overflow-y: auto; background-color: var(--dt-bg-body); }
.section { padding: 24px 20px; }
.section-divider { height: 8px; background-color: var(--dt-bg-hover); }

.group-info { display: flex; flex-direction: column; align-items: center; margin-bottom: 24px; }
.group-avatar { width: 64px; height: 64px; border-radius: 8px; margin-bottom: 12px; background: #eee; }
.group-name { font-size: 18px; font-weight: 600; margin-bottom: 4px; }
.group-desc { font-size: 12px; color: var(--dt-text-desc); }

.member-grid-header { display: flex; justify-content: space-between; font-size: 12px; color: var(--dt-text-desc); margin-bottom: 12px; }
.member-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px 8px; }
.member-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.member-avatar, .add-icon { width: 40px; height: 40px; border-radius: 4px; }
.add-icon { display: flex; align-items: center; justify-content: center; border: 1px dashed #ccc; color: #999; cursor: pointer; }
.member-name { font-size: 10px; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; width: 100%; }

.func-item, .setting-item { display: flex; justify-content: space-between; align-items: center; height: 44px; font-size: 14px; cursor: pointer; }
.func-right { display: flex; align-items: center; gap: 8px; color: var(--dt-text-desc); }
.text-ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 120px; font-size: 12px; }

.danger-btn { width: 100%; height: 40px; border: none; background: transparent; color: #FF4D4F; font-size: 14px; cursor: pointer; border-radius: 4px; }
.danger-btn:hover { background-color: #FFF1F0; }
</style>
