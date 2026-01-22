<template>
  <div class="contacts-panel">
    <div class="contacts-left">
      <div class="left-header">
        <span class="title">通讯录</span>
        <div class="header-actions">
          <el-button circle :icon="UserFilled" @click="showRequests = true">
            <el-badge v-if="requestCount > 0" :value="requestCount" is-dot />
          </el-button>
          <el-button circle :icon="Plus" @click="showAddDialog = true" />
        </div>
      </div>
      <ContactList
        ref="contactListRef"
        :current-contact="selectedContact"
        @select="handleSelect"
        @nav="handleNav"
      />
    </div>
    
    <div class="contacts-right">
      <template v-if="view === 'detail'">
        <ContactDetail
          :contact="selectedContact"
          @update="refreshList"
        />
      </template>
      <template v-else-if="view === 'new-friends'">
        <div class="new-friends-view">
          <div class="view-header">新朋友</div>
          <FriendRequestsDialog :inline="true" @refresh="refreshList" />
        </div>
      </template>
    </div>

    <!-- Dialogs -->
    <AddContactDialog v-model:visible="showAddDialog" />
    <FriendRequestsDialog v-model="showRequests" @refresh="refreshList" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, UserFilled } from '@element-plus/icons-vue'
import ContactList from '@/components/Contacts/ContactList.vue'
import ContactDetail from '@/components/Contacts/ContactDetail.vue'
import AddContactDialog from '@/components/Contacts/AddContactDialog.vue'
import FriendRequestsDialog from '@/components/FriendRequestsDialog/index.vue'
import { getFriendRequests } from '@/api/im/contact'

const selectedContact = ref(null)
const view = ref('detail') // detail | new-friends
const showAddDialog = ref(false)
const showRequests = ref(false)
const requestCount = ref(0)
const contactListRef = ref(null)

const handleSelect = (contact) => {
  selectedContact.value = contact
  view.value = 'detail'
}

const handleNav = (target) => {
  view.value = target
  selectedContact.value = null
}

const refreshList = () => {
  contactListRef.value?.reload()
  selectedContact.value = null
  checkRequests()
}

const checkRequests = async () => {
  try {
    const res = await getFriendRequests()
    if (res.code === 200) {
      requestCount.value = res.data.filter(r => r.status === 'PENDING' && r.direction === 'RECEIVED').length
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  checkRequests()
})
</script>

<style scoped>
.contacts-panel {
  display: flex;
  height: 100%;
  background: white;
}

.contacts-left {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e6e6e6;
}

.left-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.contacts-right {
  flex: 1;
  overflow-y: auto;
  background: white;
}

.new-friends-view {
  height: 100%;
  display: flex;
  flex-direction: column;

  .view-header {
    padding: 24px 40px;
    font-size: 20px;
    font-weight: 600;
    border-bottom: 1px solid #f0f0f0;
  }

  :deep(.friend-requests-panel) {
    padding: 20px 40px;
    .el-dialog {
      margin: 0;
      width: 100% !important;
      box-shadow: none;
      border: none;
      .el-dialog__header { display: none; }
      .el-dialog__body { padding: 0; }
    }
  }
}
</style>
