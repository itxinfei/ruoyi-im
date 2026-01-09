<template>
  <div class="test-local-storage">
    <h3>本地存储测试</h3>
    <div class="storage-info">
      <h4>用户信息:</h4>
      <pre>{{ userInfo }}</pre>
    </div>
    <div class="storage-info">
      <h4>用户头像:</h4>
      <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="用户头像" class="test-avatar" />
      <span v-else>头像不存在</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TestLocalStorage',
  data() {
    return {
      userInfo: {},
    }
  },
  mounted() {
    this.getUserInfoFromStorage()
  },
  methods: {
    getUserInfoFromStorage() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        this.userInfo = JSON.parse(userInfoStr)
      }
    },
  },
}
</script>

<style scoped>
.test-local-storage {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  max-width: 500px;
  margin: 20px;
}

.storage-info {
  margin-bottom: 20px;
}

pre {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}

.test-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}
</style>