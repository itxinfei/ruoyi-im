<template>
  <el-dialog
    v-model="dialogVisible"
    width="860px"
    class="group-profile-dialog"
    destroy-on-close
    :show-close="false"
    append-to-body
    modal-class="group-profile-modal"
  >
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="groupInfo" class="gp">
      <div class="gp-rail">
        <button class="gp-close" @click="dialogVisible = false">
          <el-icon><ArrowLeft /></el-icon>
        </button>

        <div class="gp-rail-top">
          <div class="gp-avatar-ring">
            <el-avatar
              :size="78"
              :src="groupInfo.avatar || ''"
              shape="square"
              class="gp-avatar"
            >
              {{ groupInfo.name?.charAt(0) || '群' }}
            </el-avatar>
          </div>

          <div class="gp-title">
            {{ groupInfo.name }}
          </div>
          <div class="gp-sub">
            群组 ID · {{ groupInfo.id }}
          </div>
        </div>

        <div class="gp-chips">
          <span class="gp-chip" :class="{ 'is-strong': groupInfo.isPublic }">
            {{ groupInfo.isPublic ? '公开群组' : '私有群组' }}
          </span>
          <span class="gp-chip">
            {{ groupMembers.length }} 人
          </span>
          <span v-if="groupMembers.length > 50" class="gp-chip">
            大型群组
          </span>
        </div>

        <div class="gp-actions">
          <button class="gp-btn gp-btn--primary" @click="copyGroupId">
            <span class="material-icons-outlined">content_copy</span>
            复制群号
          </button>
          <button class="gp-btn" @click="shareGroup">
            <span class="material-icons-outlined">ios_share</span>
            分享
          </button>
        </div>

        <div class="gp-rail-foot">
          <div class="gp-foot-card">
            <div class="gp-foot-title">小提示</div>
            <div class="gp-foot-text">点击成员头像可查看资料</div>
          </div>
        </div>
      </div>

      <div class="gp-main">
        <div class="gp-topbar">
          <div class="gp-tabs" role="tablist" aria-label="群组详情">
            <button
              class="gp-tab"
              :class="{ active: activeTab === 'members' }"
              role="tab"
              :aria-selected="activeTab === 'members'"
              @click="activeTab = 'members'"
            >
              成员
              <span class="gp-count">{{ groupMembers.length }}</span>
            </button>
            <button
              class="gp-tab"
              :class="{ active: activeTab === 'settings' }"
              role="tab"
              :aria-selected="activeTab === 'settings'"
              @click="activeTab = 'settings'"
            >
              群设置
            </button>
          </div>

          <div class="gp-top-actions">
            <el-tooltip content="复制群ID" placement="bottom">
              <button class="gp-icon-btn" @click="copyGroupId">
                <span class="material-icons-outlined">content_copy</span>
              </button>
            </el-tooltip>
            <el-tooltip content="关闭" placement="bottom">
              <button class="gp-icon-btn" @click="dialogVisible = false">
                <span class="material-icons-outlined">close</span>
              </button>
            </el-tooltip>
          </div>
        </div>

        <div class="gp-body">
          <div v-if="activeTab === 'members'" class="gp-pane">
            <div class="members-head">
              <div class="members-title">
                成员列表
              </div>
              <div class="members-tools">
                <el-input
                  v-model="memberKeyword"
                  placeholder="搜索成员"
                  clearable
                  class="member-search"
                />
                <button class="invite-btn" @click="handleAddMember">
                  <span class="material-icons-outlined">person_add</span>
                  邀请成员
                </button>
              </div>
            </div>

            <div class="members-grid" role="list">
              <button class="member-card member-card--add" @click="handleAddMember">
                <span class="material-icons-outlined">add</span>
                <span class="member-name">邀请</span>
              </button>

              <button
                v-for="member in filteredMembers"
                :key="member.userId"
                class="member-card"
                role="listitem"
                @click="viewMemberInfo(member.userId)"
              >
                <el-avatar :size="44" :src="member.avatar || ''" shape="square" class="member-avatar">
                  {{ member.userName?.charAt(0) || '用' }}
                </el-avatar>
                <span class="member-name">{{ member.userName }}</span>
              </button>
            </div>
          </div>

          <div v-else class="gp-pane">
            <div class="settings-card">
              <div class="settings-title">
                基础信息
              </div>
              <el-form :model="groupSettings" label-position="top" class="settings-form">
                <el-form-item label="群名称">
                  <el-input v-model="groupSettings.name" placeholder="请输入群组名称" />
                </el-form-item>
                <el-form-item label="群描述">
                  <el-input
                    v-model="groupSettings.description"
                    type="textarea"
                    placeholder="介绍一下这个群..."
                    :rows="4"
                  />
                </el-form-item>

                <div class="settings-row">
                  <div class="settings-row-left">
                    <div class="settings-row-title">公开群组</div>
                    <div class="settings-row-desc">允许任何人搜索并申请加入</div>
                  </div>
                  <el-switch v-model="groupSettings.isPublic" />
                </div>

                <div class="settings-actions">
                  <el-button type="primary" class="save-btn" @click="saveSettings">
                    保存修改
                  </el-button>
                </div>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getGroup, getGroupMembers, updateGroup } from '@/api/im/group'

const props = defineProps({
  visible: { type: Boolean, default: false },
  groupId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:visible', 'refresh-group', 'view-member'])

const dialogVisible = ref(props.visible)
const loading = ref(false)
const groupInfo = ref(null)
const groupMembers = ref([])
const activeTab = ref('members')
const groupSettings = ref({ name: '', description: '', isPublic: false })
const memberKeyword = ref('')

watch(() => props.visible, v => { dialogVisible.value = v })
watch(dialogVisible, v => {
  emit('update:visible', v)
  if (v && props.groupId) {
    activeTab.value = 'members'
    memberKeyword.value = ''
    loadGroupInfo()
  }
})
watch(() => props.groupId, gid => {
  if (gid && dialogVisible.value) {
    loadGroupInfo()
  }
})

const filteredMembers = computed(() => {
  const keyword = (memberKeyword.value || '').trim().toLowerCase()
  if (!keyword) {
    return groupMembers.value
  }
  return (groupMembers.value || []).filter(m => {
    const name = (m?.userName || '').toString().toLowerCase()
    return name.includes(keyword)
  })
})

const loadGroupInfo = async () => {
  if (!props.groupId) {
    return
  }
  loading.value = true
  try {
    const [gRes, mRes] = await Promise.all([getGroup(props.groupId), getGroupMembers(props.groupId)])
    if (gRes.code === 200) {
      groupInfo.value = gRes.data
      groupSettings.value = { name: gRes.data.name, description: gRes.data.description || '', isPublic: gRes.data.isPublic === 1 }
    }
    if (mRes.code === 200) {
      groupMembers.value = mRes.data
    }
  } catch (e) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const copyGroupId = () => {
  navigator.clipboard.writeText(groupInfo.value.id.toString())
  ElMessage.success('ID 已复制')
}

const saveSettings = async () => {
  try {
    const res = await updateGroup(props.groupId, { ...groupSettings.value, isPublic: groupSettings.value.isPublic ? 1 : 0 })
    if (res.code === 200) {
      ElMessage.success('设置已更新')
      loadGroupInfo()
      emit('refresh-group')
    }
  } catch (e) { ElMessage.error('更新失败') }
}

const viewMemberInfo = uid => { emit('view-member', uid) }
const handleAddMember = () => { ElMessage.info('邀请功能开发中') }
const shareGroup = () => { ElMessage.info('分享功能开发中') }
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:global(.group-profile-modal) {
  background-color: rgba(0, 0, 0, 0.55) !important;
  backdrop-filter: blur(2px);
}

.group-profile-dialog {
  :deep(.el-dialog) {
    border-radius: var(--dt-radius-xl);
    overflow: hidden;
    padding: 0;
    background: transparent;
    box-shadow: 0 30px 80px rgba(0, 0, 0, 0.35);
  }

  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.gp {
  height: 600px;
  display: flex;
  background:
    radial-gradient(1200px 600px at -10% -20%, rgba(71, 128, 255, 0.55), rgba(71, 128, 255, 0) 55%),
    radial-gradient(900px 600px at 80% 0%, rgba(255, 120, 120, 0.22), rgba(255, 120, 120, 0) 55%),
    radial-gradient(800px 520px at 55% 110%, rgba(120, 255, 214, 0.15), rgba(120, 255, 214, 0) 55%),
    #0b1020;
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
}

.gp-rail {
  width: 300px;
  padding: 20px 18px 18px;
  display: flex;
  flex-direction: column;
  position: relative;
  color: rgba(255, 255, 255, 0.92);
  border-right: 1px solid rgba(255, 255, 255, 0.12);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.02)),
    radial-gradient(700px 520px at 20% 20%, rgba(255, 255, 255, 0.10), rgba(255, 255, 255, 0) 60%);
}

.gp-close {
  position: absolute;
  top: 14px;
  left: 14px;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(0, 0, 0, 0.18);
  color: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.15s ease, background 0.15s ease, border-color 0.15s ease;

  &:hover {
    background: rgba(0, 0, 0, 0.30);
    border-color: rgba(255, 255, 255, 0.26);
    transform: translateY(-1px);
  }
}

.gp-rail-top {
  padding: 22px 6px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.gp-avatar-ring {
  padding: 8px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.gp-avatar {
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.40);
}

.gp-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.2px;
  text-align: center;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gp-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.66);
  text-align: center;
}

.gp-chips {
  margin-top: 16px;
  padding: 0 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.gp-chip {
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(6px);

  &.is-strong {
    border-color: rgba(120, 255, 214, 0.35);
    background: rgba(120, 255, 214, 0.12);
    color: rgba(235, 255, 250, 0.95);
  }
}

.gp-actions {
  margin-top: 18px;
  padding: 0 6px;
  display: grid;
  gap: 10px;
}

.gp-btn {
  height: 40px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.92);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.15s ease, background 0.15s ease, border-color 0.15s ease;

  .material-icons-outlined {
    font-size: 18px;
    opacity: 0.95;
  }

  &:hover {
    transform: translateY(-1px);
    background: rgba(255, 255, 255, 0.09);
    border-color: rgba(255, 255, 255, 0.26);
  }

  &.gp-btn--primary {
    background: linear-gradient(135deg, rgba(71, 128, 255, 0.95), rgba(120, 255, 214, 0.55));
    border-color: rgba(255, 255, 255, 0.12);
    color: rgba(12, 18, 34, 0.95);

    &:hover {
      filter: saturate(1.08);
    }
  }
}

.gp-rail-foot {
  margin-top: auto;
  padding: 12px 6px 0;
}

.gp-foot-card {
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(0, 0, 0, 0.16);
  padding: 12px 12px;
}

.gp-foot-title {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.82);
  margin-bottom: 4px;
}

.gp-foot-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.62);
}

.gp-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(1200px 700px at 70% 0%, rgba(255, 255, 255, 0.10), rgba(255, 255, 255, 0) 52%),
    rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
}

.gp-topbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.65);
}

.gp-tabs {
  display: inline-flex;
  align-items: center;
  background: rgba(15, 23, 42, 0.06);
  padding: 6px;
  border-radius: 16px;
}

.gp-tab {
  height: 40px;
  border-radius: 12px;
  border: 0;
  padding: 0 14px;
  background: transparent;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(15, 23, 42, 0.70);
  font-weight: 600;
  transition: background 0.15s ease, color 0.15s ease;

  &.active {
    background: rgba(255, 255, 255, 0.95);
    color: rgba(15, 23, 42, 0.95);
    box-shadow: 0 10px 24px rgba(15, 23, 42, 0.10);
  }
}

.gp-count {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.08);
  color: rgba(15, 23, 42, 0.72);
}

.gp-top-actions {
  display: flex;
  gap: 10px;
}

.gp-icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s ease, background 0.15s ease, border-color 0.15s ease;

  .material-icons-outlined {
    font-size: 18px;
    color: rgba(15, 23, 42, 0.72);
  }

  &:hover {
    transform: translateY(-1px);
    border-color: rgba(15, 23, 42, 0.18);
    background: rgba(255, 255, 255, 0.95);
  }
}

.gp-body {
  flex: 1;
  overflow: hidden;
}

.gp-pane {
  height: 100%;
  padding: 18px 18px 18px;
  overflow: auto;
}

.members-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.members-title {
  font-size: 14px;
  font-weight: 700;
  color: rgba(15, 23, 42, 0.92);
}

.members-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-search {
  width: 260px;

  :deep(.el-input__wrapper) {
    border-radius: 12px;
    box-shadow: none;
    border: 1px solid rgba(15, 23, 42, 0.10);
    background: rgba(255, 255, 255, 0.85);
  }
}

.invite-btn {
  height: 40px;
  padding: 0 14px;
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(15, 23, 42, 0.04);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.78);
  transition: transform 0.15s ease, background 0.15s ease, border-color 0.15s ease;

  .material-icons-outlined {
    font-size: 18px;
  }

  &:hover {
    transform: translateY(-1px);
    background: rgba(71, 128, 255, 0.10);
    border-color: rgba(71, 128, 255, 0.20);
    color: rgba(15, 23, 42, 0.92);
  }
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 12px;
}

.member-card {
  height: 96px;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 10px;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;

  &:hover {
    transform: translateY(-1px);
    border-color: rgba(71, 128, 255, 0.22);
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
  }

  &.member-card--add {
    background: rgba(71, 128, 255, 0.06);
    border-style: dashed;
    color: rgba(71, 128, 255, 0.92);

    .material-icons-outlined {
      font-size: 20px;
    }
  }
}

.member-avatar {
  border-radius: 14px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.10);
}

.member-name {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.84);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.settings-card {
  max-width: 520px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(255, 255, 255, 0.92);
  padding: 16px 16px;
  box-shadow: 0 22px 50px rgba(15, 23, 42, 0.08);
}

.settings-title {
  font-size: 14px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.92);
  margin-bottom: 10px;
}

.settings-form {
  :deep(.el-form-item__label) {
    color: rgba(15, 23, 42, 0.62);
    font-size: 12px;
    font-weight: 700;
    margin-bottom: 6px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 12px;
    box-shadow: none;
    border: 1px solid rgba(15, 23, 42, 0.10);
  }
}

.settings-row {
  margin-top: 8px;
  padding: 14px 12px;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(15, 23, 42, 0.03);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.settings-row-title {
  font-size: 13px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.90);
}

.settings-row-desc {
  margin-top: 2px;
  font-size: 12px;
  color: rgba(15, 23, 42, 0.62);
}

.settings-actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.save-btn {
  border-radius: 12px;
}
</style>
