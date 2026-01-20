<template>
  <div class="contacts-panel">
    <div class="contacts-left">
      <div class="contacts-header">
        <h2 class="contacts-title">通讯录</h2>
        <div class="search-box">
          <el-icon class="search-icon"><Search /></el-icon>
          <el-input
            v-model="searchQuery"
            placeholder="搜索联系人"
            class="search-input"
            clearable
          />
        </div>
      </div>

      <div class="contacts-body">
        <div v-if="!searchQuery" class="departments-section">
          <div class="section-header">
            <el-icon><OfficeBuilding /></el-icon>
            <span>组织架构</span>
          </div>
          <div class="departments-list">
            <div
              v-for="dept in departments"
              :key="dept.id"
              class="department-item"
              @click="handleDepartmentClick(dept)"
            >
              <div class="dept-info">
                <div class="dept-icon">{{ dept.icon }}</div>
                <span class="dept-name">{{ dept.name }}</span>
              </div>
              <div class="dept-meta">
                <span class="dept-count">{{ dept.count }}</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <div class="contacts-section">
          <div class="section-header">
            <el-icon><User /></el-icon>
            <span>全部联系人</span>
            <el-badge :value="filteredContacts.length" class="contact-badge" />
          </div>
          <div class="contacts-list">
            <div
              v-for="contact in filteredContacts"
              :key="contact.id"
              class="contact-item"
              :class="{ active: selectedContact?.id === contact.id }"
              @click="handleContactClick(contact)"
            >
              <div class="contact-avatar">{{ contact.avatar }}</div>
              <div class="contact-info">
                <div class="contact-name-row">
                  <span class="contact-name">{{ contact.name }}</span>
                  <el-icon v-if="contact.isFavorite" class="favorite-icon"><Star /></el-icon>
                </div>
                <p class="contact-position">{{ contact.position }} · {{ contact.department }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="contacts-right">
      <div v-if="selectedContact" class="contact-detail">
        <div class="detail-header">
          <div class="detail-avatar">{{ selectedContact.avatar }}</div>
          <div class="detail-info">
            <div class="detail-name-row">
              <h2 class="detail-name">{{ selectedContact.name }}</h2>
              <el-icon v-if="selectedContact.isFavorite" class="detail-favorite"><Star /></el-icon>
            </div>
            <p class="detail-position">{{ selectedContact.position }} · {{ selectedContact.department }}</p>
            <div class="detail-actions">
              <el-button type="primary" class="action-btn">
                <el-icon><ChatDotRound /></el-icon>
                发消息
              </el-button>
              <el-button class="action-btn action-green">
                <el-icon><Phone /></el-icon>
                语音
              </el-button>
              <el-button class="action-btn action-blue">
                <el-icon><VideoCamera /></el-icon>
                视频
              </el-button>
              <el-button class="action-btn">
                {{ selectedContact.isFavorite ? '取消收藏' : '添加收藏' }}
              </el-button>
            </div>
          </div>
        </div>

        <div class="detail-body">
          <div class="info-card">
            <h3 class="info-title">基本信息</h3>
            <div class="info-row">
              <span class="info-label">姓名</span>
              <span class="info-value">{{ selectedContact.name }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">职位</span>
              <span class="info-value">{{ selectedContact.position }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">部门</span>
              <span class="info-value">{{ selectedContact.department }}</span>
            </div>
          </div>

          <div class="info-card">
            <h3 class="info-title">联系方式</h3>
            <div class="info-row">
              <span class="info-label">手机</span>
              <span class="info-value info-link">{{ selectedContact.phone }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">邮箱</span>
              <span class="info-value info-link">{{ selectedContact.email }}</span>
            </div>
          </div>

          <div class="info-card">
            <h3 class="info-title">其他信息</h3>
            <div class="info-row">
              <span class="info-label">工号</span>
              <span class="info-value">2024{{ String(selectedContact.id).padStart(4, '0') }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">入职时间</span>
              <span class="info-value">2023-05-15</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-icon class="empty-icon"><User /></el-icon>
        <p class="empty-text">选择联系人查看详情</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Search,
  OfficeBuilding,
  User,
  ArrowRight,
  Star,
  ChatDotRound,
  Phone,
  VideoCamera
} from '@element-plus/icons-vue'

const searchQuery = ref('')
const selectedContact = ref(null)

const contacts = ref([
  {
    id: '1',
    name: '张伟',
    avatar: '张',
    position: '产品经理',
    department: '产品部',
    phone: '138-1234-5678',
    email: 'zhangwei@company.com',
    isFavorite: true
  },
  {
    id: '2',
    name: '李明',
    avatar: '李',
    position: 'UI设计师',
    department: '设计部',
    phone: '139-2345-6789',
    email: 'liming@company.com',
    isFavorite: true
  },
  {
    id: '3',
    name: '王芳',
    avatar: '王',
    position: '前端工程师',
    department: '研发部',
    phone: '136-3456-7890',
    email: 'wangfang@company.com',
    isFavorite: false
  },
  {
    id: '4',
    name: '刘洋',
    avatar: '刘',
    position: '后端工程师',
    department: '研发部',
    phone: '137-4567-8901',
    email: 'liuyang@company.com',
    isFavorite: false
  },
  {
    id: '5',
    name: '陈晨',
    avatar: '陈',
    position: '测试工程师',
    department: '测试部',
    phone: '135-5678-9012',
    email: 'chenchen@company.com',
    isFavorite: false
  },
  {
    id: '6',
    name: '赵敏',
    avatar: '赵',
    position: '人力资源',
    department: '人事部',
    phone: '134-6789-0123',
    email: 'zhaomin@company.com',
    isFavorite: false
  },
  {
    id: '7',
    name: '孙强',
    avatar: '孙',
    position: '销售经理',
    department: '销售部',
    phone: '133-7890-1234',
    email: 'sunqiang@company.com',
    isFavorite: false
  },
  {
    id: '8',
    name: '周杰',
    avatar: '周',
    position: '运营专员',
    department: '运营部',
    phone: '132-8901-2345',
    email: 'zhoujie@company.com',
    isFavorite: false
  }
])

const departments = ref([
  { id: '1', name: '产品部', count: 12, icon: '产' },
  { id: '2', name: '设计部', count: 8, icon: '设' },
  { id: '3', name: '研发部', count: 35, icon: '研' },
  { id: '4', name: '测试部', count: 15, icon: '测' },
  { id: '5', name: '销售部', count: 20, icon: '销' },
  { id: '6', name: '运营部', count: 10, icon: '运' },
  { id: '7', name: '人事部', count: 6, icon: '人' },
  { id: '8', name: '财务部', count: 5, icon: '财' }
])

const filteredContacts = computed(() => {
  if (!searchQuery.value) {
    return contacts.value
  }
  const query = searchQuery.value.toLowerCase()
  return contacts.value.filter(contact =>
    contact.name.toLowerCase().includes(query) ||
    contact.department.toLowerCase().includes(query) ||
    contact.position.toLowerCase().includes(query)
  )
})

const handleContactClick = (contact) => {
  selectedContact.value = contact
}

const handleDepartmentClick = (dept) => {
  console.log('Department clicked:', dept)
}
</script>

<style scoped>
.contacts-panel {
  flex: 1;
  display: flex;
  background: #fff;
  overflow: hidden;
}

.contacts-left {
  width: 320px;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.contacts-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;

  .contacts-title {
    font-size: 20px;
    font-weight: 600;
    color: #262626;
    margin: 0 0 12px 0;
  }

  .search-box {
    position: relative;

    .search-icon {
      position: absolute;
      left: 10px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 16px;
      color: #8c8c8c;
    }

    .search-input {
      width: 100%;

      :deep(.el-input__wrapper) {
        background-color: #f5f5f5;
        border: none;
        padding-left: 36px;
        box-shadow: none;
      }
    }
  }
}

.contacts-body {
  flex: 1;
  overflow-y: auto;
}

.departments-section {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #595959;
  margin-bottom: 12px;

  .el-icon {
    font-size: 16px;
  }

  .contact-badge {
    margin-left: auto;
  }
}

.departments-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.department-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #f5f5f5;
  }

  .dept-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .dept-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 500;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .dept-name {
    font-size: 14px;
    color: #262626;
  }

  .dept-meta {
    display: flex;
    align-items: center;
    gap: 8px;

    .dept-count {
      font-size: 12px;
      color: #8c8c8c;
    }

    .el-icon {
      font-size: 16px;
      color: #8c8c8c;
    }
  }
}

.contacts-section {
  padding: 16px;
}

.contacts-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #f5f5f5;
  }

  &.active {
    background-color: #e6f7ff;
  }

  .contact-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 500;
    flex-shrink: 0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .contact-info {
    flex: 1;
    min-width: 0;
  }

  .contact-name-row {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 2px;
  }

  .contact-name {
    font-size: 14px;
    font-weight: 500;
    color: #262626;
  }

  .favorite-icon {
    font-size: 12px;
    color: #faad14;
    flex-shrink: 0;
  }

  .contact-position {
    font-size: 12px;
    color: #8c8c8c;
    margin: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.contacts-right {
  flex: 1;
  background-color: #f7f8fa;
  overflow-y: auto;
}

.contact-detail {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.detail-header {
  background: #fff;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  gap: 16px;
}

.detail-avatar {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 600;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.detail-info {
  flex: 1;
}

.detail-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.detail-name {
  font-size: 24px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.detail-favorite {
  font-size: 20px;
  color: #faad14;
}

.detail-position {
  font-size: 14px;
  color: #595959;
  margin: 0 0 16px 0;
}

.detail-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;

  .action-btn {
    display: flex;
    align-items: center;
    gap: 4px;

    &.action-green {
      border-color: #d9f7be;
      color: #52c41a;

      &:hover {
        background-color: #f6ffed;
      }
    }

    &.action-blue {
      border-color: #bae7ff;
      color: #1890ff;

      &:hover {
        background-color: #e6f7ff;
      }
    }
  }
}

.detail-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.info-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.info-title {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin: 0 0 16px 0;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;

  .info-label {
    width: 96px;
    font-size: 14px;
    color: #8c8c8c;
    flex-shrink: 0;
  }

  .info-value {
    flex: 1;
    font-size: 14px;
    color: #262626;

    &.info-link {
      color: #1890ff;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;

  .empty-icon {
    font-size: 64px;
    color: #d9d9d9;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 14px;
    margin: 0;
  }
}
</style>
