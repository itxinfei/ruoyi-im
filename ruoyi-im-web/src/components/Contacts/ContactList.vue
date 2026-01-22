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
      <!-- 组织架构 -->
      <el-collapse v-if="!searchQuery" v-model="activeNames">
        <el-collapse-item title="组织架构" name="org">
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
      </el-collapse>

      <!-- 联系人列表 -->
      <div v-if="filteredContacts.length > 0" class="friends-list">
        <div class="list-header">联系人</div>
        <div
          v-for="contact in filteredContacts"
          :key="contact.id"
          class="friend-item"
          :class="{ active: currentContact?.id === contact.id }"
          @click="$emit('select', contact)"
        >
          <div class="avatar">
            <img v-if="contact.friendAvatar" :src="contact.friendAvatar" />
            <div v-else class="avatar-text">
              {{ (contact.friendName || '?').charAt(0) }}
            </div>
          </div>
          <div class="info">
            <div class="name">{{ contact.friendName }}</div>
            <div class="desc">{{ contact.position }} · {{ contact.department }}</div>
          </div>
        </div>
      </div>
      <div v-else class="empty">无联系人</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, OfficeBuilding, User } from '@element-plus/icons-vue'
import { getContacts } from '@/api/im/contact'
import { getOrgTree } from '@/api/im/organization'

const props = defineProps({
  currentContact: Object
})

const emit = defineEmits(['select'])

const searchQuery = ref('')
const contacts = ref([])
const orgTree = ref([])
const loading = ref(false)
const activeNames = ref(['org'])

const loadData = async () => {
  loading.value = true
  try {
    const [cRes, oRes] = await Promise.all([getContacts(), getOrgTree()])
    if (cRes.code === 200) contacts.value = cRes.data
    if (oRes && oRes.code === 200) orgTree.value = oRes.data
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

const handleNodeClick = (data) => {
  if (data.type === 'user') {
    // Convert org user to contact format if possible, or just emit info
    // For now assuming org tree nodes have minimal info.
    // Ideally org tree users should match contact structure or fetch details
    // Here we might just emit a "preview" object
    emit('select', {
      id: data.id,
      friendName: data.name,
      // ... info usually incomplete in tree, might need fetch
      isOrgNode: true
    })
  }
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

.empty {
  text-align: center;
  color: #ccc;
  margin-top: 20px;
}
</style>
