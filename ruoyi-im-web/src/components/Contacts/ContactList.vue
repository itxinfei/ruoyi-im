<template>
  <div class="contact-list-container">
    <div class="search-box">
      <el-input
        v-model="searchQuery"
        placeholder="搜索联系人"
        clearable
        :prefix-icon="Search"
      />
    </div>

    <div class="list-content" v-loading="loading">
      <!-- 导航列表 -->
      <div v-if="!searchQuery" class="nav-list">
        <div class="nav-item" @click="$emit('nav', 'new-friends')">
          <div class="nav-icon new-friends">
            <el-icon><User /></el-icon>
          </div>
          <span class="nav-label">新朋友</span>
        </div>
      </div>

      <el-collapse v-model="activeNames">
        <!-- 组织架构 -->
        <el-collapse-item v-if="!searchQuery" title="组织架构" name="org">
          <el-tree
            :data="orgTree"
            :props="{ children: 'children', label: 'name' }"
            @node-click="handleNodeClick"
          >
             <template #default="{ node, data }">
              <span class="custom-tree-node">
                <el-icon v-if="data.type === 'dept'"><OfficeBuilding /></el-icon>
                <el-icon v-else><User /></el-icon>
                <span class="node-label">{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
        </el-collapse-item>

        <!-- 我的群组 -->
        <el-collapse-item title="我的群组" name="groups">
          <div
            v-for="group in filteredGroups"
            :key="group.id"
            class="friend-item"
            :class="{ active: currentContact?.id === group.id && currentContact?.isGroup }"
            @click="handleGroupClick(group)"
          >
            <el-avatar :size="36" :src="addTokenToUrl(group.avatar)" class="avatar">
              {{ (group.name?.charAt(0) || '?').toUpperCase() }}
            </el-avatar>
            <div class="info">
              <div class="name">{{ group.name }}</div>
              <div class="desc">{{ group.memberCount || 0 }} 人</div>
            </div>
          </div>
        </el-collapse-item>

        <!-- 我的联系人 -->
        <el-collapse-item title="我的联系人" name="friends">
          <div
            v-for="contact in filteredContacts"
            :key="contact.id"
            class="friend-item"
            :class="{ active: currentContact?.id === contact.id && !currentContact?.isGroup }"
            @click="handleContactClick(contact)"
          >
            <div class="avatar">
              <img v-if="contact.friendAvatar" :src="contact.friendAvatar" />
              <div v-else class="avatar-text">
                {{ (contact.friendName || '?').charAt(0).toUpperCase() }}
              </div>
            </div>
            <div class="info">
              <div class="name">{{ contact.friendName }}</div>
              <div class="desc">{{ contact.position || '联系人' }}</div>
            </div>
          </div>
          <div v-if="filteredContacts.length === 0" class="empty-small">暂无联系人</div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, OfficeBuilding, User } from '@element-plus/icons-vue'
import { getContacts } from '@/api/im/contact'
import { getOrgTree } from '@/api/im/organization'
import { getGroups } from '@/api/im/group'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  currentContact: Object
})

const emit = defineEmits(['select'])

const searchQuery = ref('')
const contacts = ref([])
const groups = ref([])
const orgTree = ref([])
const loading = ref(false)
const activeNames = ref(['org', 'groups', 'friends'])

const loadData = async () => {
  loading.value = true
  try {
    const [cRes, oRes, gRes] = await Promise.all([getContacts(), getOrgTree(), getGroups()])
    if (cRes.code === 200) contacts.value = cRes.data
    if (oRes && oRes.code === 200) orgTree.value = oRes.data
    if (gRes && gRes.code === 200) groups.value = gRes.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const filteredContacts = computed(() => {
  if (!searchQuery.value) return contacts.value
  const q = searchQuery.value.toLowerCase()
  return contacts.value.filter(c => 
    (c.friendName && c.friendName.toLowerCase().includes(q)) ||
    (c.department && c.department.toLowerCase().includes(q))
  )
})

const filteredGroups = computed(() => {
  if (!searchQuery.value) return groups.value
  const q = searchQuery.value.toLowerCase()
  return groups.value.filter(g => g.name && g.name.toLowerCase().includes(q))
})

const handleNodeClick = (data) => {
  if (data.type === 'user') {
    emit('select', {
      id: data.id,
      friendId: data.id,
      friendName: data.name,
      friendAvatar: addTokenToUrl(data.avatar),
      isOrgNode: true
    })
  }
}

const handleGroupClick = (group) => {
  emit('select', { ...group, isGroup: true })
}

const handleContactClick = (contact) => {
  emit('select', { ...contact, isGroup: false })
}

onMounted(loadData)

defineExpose({ reload: loadData })
</script>

<style scoped>
.contact-list-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
  border-right: 1px solid #e6e6e6;
}

.search-box {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

.nav-list {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;

  .nav-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    cursor: pointer;
    gap: 12px;

    &:hover {
      background: #f5f7fa;
    }

    .nav-icon {
      width: 36px;
      height: 36px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;

      &.new-friends { background: #fab6b6; }
    }

    .nav-label {
      font-size: 14px;
      color: #333;
    }
  }
}

.friends-list {
  padding-top: 10px;
}

.list-header {
  padding: 0 12px 8px;
  font-size: 12px;
  color: #999;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.friend-item:hover, .friend-item.active {
  background-color: #f5f7fa;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  margin-right: 10px;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info {
  flex: 1;
  overflow: hidden;
}

.name {
  font-size: 14px;
  color: #333;
  margin-bottom: 2px;
}

.desc {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-small {
  text-align: center;
  color: #ccc;
  font-size: 12px;
  padding: 20px 0;
}
</style>
