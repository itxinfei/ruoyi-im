<template>
  <div class="group-detail-container">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="goBack" class="back-button">返回</el-button>
      <h2>群组详情</h2>
    </div>

    <div v-if="group" class="detail-content">
      <div class="profile-section">
        <el-avatar :size="100" :src="group.avatar || '/profile/avatar.png'">
          {{ group.name?.charAt(0) || '群' }}
        </el-avatar>
        <div class="profile-info">
          <h3>{{ group.name }}</h3>
          <p class="status">{{ group.memberCount }} 位成员</p>
        </div>
        <div class="profile-actions">
          <el-button type="primary" :icon="Comment" @click="startChat">进入群聊</el-button>
          <el-button :icon="Setting">群组设置</el-button>
        </div>
      </div>

      <div class="info-section">
        <h4>群组信息</h4>
        <div class="info-item">
          <span class="label">群组名称：</span>
          <span class="value">{{ group.name }}</span>
        </div>
        <div class="info-item">
          <span class="label">群组描述：</span>
          <span class="value">{{ group.description || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">创建时间：</span>
          <span class="value">{{ formatDate(group.createTime) }}</span>
        </div>
        <div class="info-item">
          <span class="label">群主：</span>
          <span class="value">{{ group.ownerName || '-' }}</span>
        </div>
      </div>

      <div class="members-section">
        <div class="section-header">
          <h4>群组成员</h4>
          <el-button type="primary" :icon="Plus" size="small" @click="addMember">
            添加成员
          </el-button>
        </div>
        <div class="members-list">
          <div v-for="member in members" :key="member.id" class="member-item">
            <el-avatar :size="40" :src="member.avatar">
              {{ member.name?.charAt(0) || '用' }}
            </el-avatar>
            <div class="member-info">
              <div class="member-name">{{ member.name }}</div>
              <div class="member-role">{{ member.role }}</div>
            </div>
            <el-dropdown trigger="click" @command="(cmd) => handleMemberCommand(cmd, member)">
              <el-button :icon="More" circle size="small" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="chat">发消息</el-dropdown-item>
                  <el-dropdown-item command="profile">查看资料</el-dropdown-item>
                  <el-dropdown-item command="remove" divided>移出群组</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="群组不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Comment,
  Setting,
  Plus,
  More,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const group = ref(null)
const members = ref([])

const goBack = () => {
  router.back()
}

const startChat = () => {
  router.push(`/im/chat/${group.value.id}`)
}

const addMember = () => {
  ElMessage.info('添加成员功能待实现')
}

const handleMemberCommand = (command, member) => {
  switch (command) {
    case 'chat':
      router.push(`/im/chat/${member.id}`)
      break
    case 'profile':
      router.push(`/im/contact/${member.id}`)
      break
    case 'remove':
      ElMessageBox.confirm(`确定要将 ${member.name} 移出群组吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          ElMessage.success('已移出群组')
        })
        .catch(() => {})
      break
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  const groupId = route.params.id
  group.value = {
    id: groupId,
    name: '群组' + groupId,
    description: '这是一个测试群组',
    avatar: '',
    memberCount: 5,
    ownerName: '群主',
    createTime: new Date().toISOString(),
  }

  members.value = [
    { id: 1, name: '张三', role: '群主', avatar: '' },
    { id: 2, name: '李四', role: '管理员', avatar: '' },
    { id: 3, name: '王五', role: '成员', avatar: '' },
    { id: 4, name: '赵六', role: '成员', avatar: '' },
    { id: 5, name: '钱七', role: '成员', avatar: '' },
  ]
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.group-detail-container {
  height: 100%;
  overflow-y: auto;
  background-color: $bg-white;

  .detail-header {
    display: flex;
    align-items: center;
    padding: $spacing-lg;
    border-bottom: 1px solid $border-light;
    gap: $spacing-md;

    .back-button {
      padding: $spacing-sm $spacing-md;
    }

    h2 {
      margin: 0;
      font-size: $font-size-xl;
      font-weight: $font-weight-semibold;
    }
  }

  .detail-content {
    padding: $spacing-xl;

    .profile-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: $spacing-2xl 0;
      border-bottom: 1px solid $border-light;
      margin-bottom: $spacing-xl;

      .profile-info {
        text-align: center;
        margin: $spacing-lg 0;

        h3 {
          margin: 0 0 $spacing-xs 0;
          font-size: $font-size-xl;
          font-weight: $font-weight-semibold;
        }

        .status {
          font-size: $font-size-sm;
          color: $text-tertiary;
        }
      }

      .profile-actions {
        display: flex;
        gap: $spacing-md;
      }
    }

    .info-section {
      margin-bottom: $spacing-xl;

      h4 {
        margin: 0 0 $spacing-lg 0;
        font-size: $font-size-lg;
        font-weight: $font-weight-semibold;
      }

      .info-item {
        display: flex;
        padding: $spacing-md 0;
        border-bottom: 1px solid $border-light;

        .label {
          width: 100px;
          color: $text-secondary;
        }

        .value {
          flex: 1;
          color: $text-primary;
        }
      }
    }

    .members-section {
      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: $spacing-lg;

        h4 {
          margin: 0;
          font-size: $font-size-lg;
          font-weight: $font-weight-semibold;
        }
      }

      .members-list {
        display: flex;
        flex-direction: column;
        gap: $spacing-md;

        .member-item {
          display: flex;
          align-items: center;
          padding: $spacing-md;
          border: 1px solid $border-light;
          border-radius: $border-radius-base;
          transition: all $transition-base $ease-base;

          &:hover {
            background-color: $bg-hover;
          }

          .member-info {
            flex: 1;
            margin-left: $spacing-md;

            .member-name {
              font-size: $font-size-base;
              color: $text-primary;
              margin-bottom: $spacing-xs;
            }

            .member-role {
              font-size: $font-size-sm;
              color: $text-tertiary;
            }
          }
        }
      }
    }
  }
}
</style>
