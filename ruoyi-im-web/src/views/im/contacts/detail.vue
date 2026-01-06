<template>
  <div class="contact-detail-container">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" class="back-button" @click="goBack">返回</el-button>
      <h2>联系人详情</h2>
    </div>

    <div v-if="contact" class="detail-content">
      <div class="profile-section">
        <el-avatar :size="100" :src="contact.avatar || '/profile/avatar.png'">
          {{ (contact.name || contact.nickname || contact.username)?.charAt(0) || '用' }}
        </el-avatar>
        <div class="profile-info">
          <h3>{{ contact.name || contact.nickname || contact.username }}</h3>
          <p class="status" :class="{ online: contact.online }">
            {{ contact.online ? '在线' : '离线' }}
          </p>
        </div>
        <div class="profile-actions">
          <el-button type="primary" :icon="Comment" @click="startChat">发消息</el-button>
          <el-button :icon="Phone">语音通话</el-button>
          <el-button :icon="VideoCamera">视频通话</el-button>
        </div>
      </div>

      <div class="info-section">
        <h4>详细信息</h4>
        <div class="info-item">
          <span class="label">用户名：</span>
          <span class="value">{{ contact.username }}</span>
        </div>
        <div class="info-item">
          <span class="label">昵称：</span>
          <span class="value">{{ contact.nickname || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">手机号：</span>
          <span class="value">{{ contact.phoneNumber || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">邮箱：</span>
          <span class="value">{{ contact.email || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">备注：</span>
          <span class="value">{{ contact.remark || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">添加时间：</span>
          <span class="value">{{ formatDate(contact.createTime) }}</span>
        </div>
      </div>

      <div class="actions-section">
        <h4>操作</h4>
        <el-button :icon="Edit" @click="editRemark">编辑备注</el-button>
        <el-button :icon="Star" @click="toggleStar">
          {{ contact.starred ? '取消星标' : '添加星标' }}
        </el-button>
        <el-button :icon="Delete" type="danger" @click="deleteContact">删除联系人</el-button>
      </div>
    </div>

    <el-empty v-else description="联系人不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Comment, Phone, VideoCamera, Edit, Star, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const contact = ref(null)

const goBack = () => {
  router.back()
}

const startChat = () => {
  router.push(`/im/chat/${contact.value.id}`)
}

const editRemark = () => {
  ElMessage.info('编辑备注功能待实现')
}

const toggleStar = () => {
  contact.value.starred = !contact.value.starred
  ElMessage.success(contact.value.starred ? '已添加星标' : '已取消星标')
}

const deleteContact = () => {
  ElMessageBox.confirm('确定要删除该联系人吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      ElMessage.success('删除成功')
      goBack()
    })
    .catch(() => {})
}

const formatDate = date => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  const contactId = route.params.id
  contact.value = {
    id: contactId,
    username: 'user_' + contactId,
    name: '联系人' + contactId,
    nickname: '昵称' + contactId,
    avatar: '',
    phoneNumber: '13800138000',
    email: 'user@example.com',
    remark: '',
    online: true,
    starred: false,
    createTime: new Date().toISOString(),
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.contact-detail-container {
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

          &.online {
            color: $success-color;
          }
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

    .actions-section {
      h4 {
        margin: 0 0 $spacing-lg 0;
        font-size: $font-size-lg;
        font-weight: $font-weight-semibold;
      }

      .el-button {
        margin-right: $spacing-md;
        margin-bottom: $spacing-md;
      }
    }
  }
}
</style>
