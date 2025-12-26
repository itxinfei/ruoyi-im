<template>
  <div class="privacy-settings">
    <el-card class="setting-card">
      <template #header>
        <div>
          <span>{{ $t('settings.privacySettings') }}</span>
        </div>
      </template>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.onlineStatus') }}</h4>
          <p>{{ $t('settings.onlineStatusDesc') }}</p>
        </div>
        <el-select v-model="settings.onlineStatusVisible" @change="handleSettingChange">
          <el-option
            v-for="option in visibilityOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.lastSeen') }}</h4>
          <p>{{ $t('settings.lastSeenDesc') }}</p>
        </div>
        <el-select v-model="settings.lastSeenVisible" @change="handleSettingChange">
          <el-option
            v-for="option in visibilityOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.profilePhoto') }}</h4>
          <p>{{ $t('settings.profilePhotoDesc') }}</p>
        </div>
        <el-select v-model="settings.profilePhotoVisible" @change="handleSettingChange">
          <el-option
            v-for="option in visibilityOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>
    </el-card>

    <el-card class="setting-card">
      <template #header>
        <div>
          <span>{{ $t('settings.messagePrivacy') }}</span>
        </div>
      </template>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.messagePermission') }}</h4>
          <p>{{ $t('settings.messagePermissionDesc') }}</p>
        </div>
        <el-select v-model="settings.messagePermission" @change="handleSettingChange">
          <el-option
            v-for="option in messagePermissionOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.readReceipts') }}</h4>
          <p>{{ $t('settings.readReceiptsDesc') }}</p>
        </div>
        <el-switch v-model="settings.enableReadReceipts" @change="handleSettingChange" />
      </div>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.typingIndicator') }}</h4>
          <p>{{ $t('settings.typingIndicatorDesc') }}</p>
        </div>
        <el-switch v-model="settings.enableTypingIndicator" @change="handleSettingChange" />
      </div>
    </el-card>

    <el-card class="setting-card">
      <template #header>
        <div>
          <div class="header-with-button">
            <span>{{ $t('settings.blockedUsers') }}</span>
            <el-button type="text" icon="el-icon-plus" @click="handleAddBlockedUser">
              {{ $t('settings.addBlockedUser') }}
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="blockedUsers.length === 0" class="empty-block">
        <i class="el-icon-user-solid"></i>
        <p>{{ $t('settings.noBlockedUsers') }}</p>
      </div>

      <div v-else class="blocked-list">
        <div v-for="user in blockedUsers" :key="user.userId" class="blocked-item">
          <div class="user-info">
            <el-avatar :size="40" :src="user.avatar" />
            <div class="user-details">
              <h4>{{ user.nickName }}</h4>
              <p>{{ formatDate(user.blockedTime) }}</p>
            </div>
          </div>
          <el-button type="text" @click="handleUnblockUser(user)">
            {{ $t('settings.unblock') }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 添加黑名单用户对话框 -->
    <el-dialog v-model="blockDialog.visible" :title="$t('settings.addBlockedUser')" width="500px">
      <el-form ref="blockForm" :model="blockForm" :rules="blockRules">
        <el-form-item :label="$t('settings.selectUser')" prop="userId">
          <el-select
            v-model="blockForm.userId"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="blockDialog.loading"
            :placeholder="$t('settings.searchUserPlaceholder')"
          >
            <el-option
              v-for="item in blockDialog.userOptions"
              :key="item.userId"
              :label="item.nickName"
              :value="item.userId"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="item.avatar" />
                <span>{{ item.nickName }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div>
          <el-button @click="blockDialog.visible = false">
            {{ $t('common.cancel') }}
          </el-button>
          <el-button type="primary" @click="handleBlockUser">
            {{ $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { updatePrivacySettings, getBlockedUsers, blockUser, unblockUser } from '@/api/im/config'
import { searchUserList } from '@/api/system/user'

export default {
  name: 'PrivacySettings',
  data() {
    return {
      settings: {
        onlineStatusVisible: 'all',
        lastSeenVisible: 'all',
        profilePhotoVisible: 'all',
        messagePermission: 'all',
        enableReadReceipts: true,
        enableTypingIndicator: true,
      },
      visibilityOptions: [
        { label: this.$t('settings.visibleToAll'), value: 'all' },
        { label: this.$t('settings.visibleToContacts'), value: 'contacts' },
        { label: this.$t('settings.visibleToNone'), value: 'none' },
      ],
      messagePermissionOptions: [
        { label: this.$t('settings.allowAll'), value: 'all' },
        { label: this.$t('settings.allowContacts'), value: 'contacts' },
        { label: this.$t('settings.allowNone'), value: 'none' },
      ],
      blockedUsers: [],
      blockDialog: {
        visible: false,
        loading: false,
        userOptions: [],
      },
      blockForm: {
        userId: null,
      },
      blockRules: {
        userId: [
          { required: true, message: this.$t('settings.selectUserRequired'), trigger: 'change' },
        ],
      },
    }
  },
  computed: {
    ...mapGetters(['userSettings']),
  },
  created() {
    this.initSettings()
    this.loadBlockedUsers()
  },
  methods: {
    formatDate(value) {
      return new Date(value).toLocaleDateString()
    },
    initSettings() {
      if (this.userSettings.privacy) {
        this.settings = {
          ...this.settings,
          ...this.userSettings.privacy,
        }
      }
    },
    async handleSettingChange() {
      try {
        await this.saveSettings()
        this.$message.success(this.$t('settings.saveSuccess'))
      } catch (error) {
        this.$message.error(this.$t('settings.saveFailed'))
      }
    },
    async saveSettings() {
      await updatePrivacySettings(this.settings)
      await this.$store.dispatch('user/updateSettings', {
        privacy: this.settings,
      })
    },
    async loadBlockedUsers() {
      try {
        const res = await getBlockedUsers()
        this.blockedUsers = res.data
      } catch (error) {
        this.$message.error(this.$t('settings.loadBlockedUsersFailed'))
      }
    },
    handleAddBlockedUser() {
      this.blockDialog.visible = true
      this.blockForm.userId = null
    },
    async searchUsers(query) {
      if (query) {
        this.blockDialog.loading = true
        try {
          const res = await searchUserList({ keywords: query })
          this.blockDialog.userOptions = res.rows
        } catch (error) {
          console.error('Search users failed:', error)
        } finally {
          this.blockDialog.loading = false
        }
      } else {
        this.blockDialog.userOptions = []
      }
    },
    async handleBlockUser() {
      this.$refs.blockForm.validate(async valid => {
        if (valid) {
          try {
            await blockUser(this.blockForm.userId)
            this.$message.success(this.$t('settings.blockSuccess'))
            this.blockDialog.visible = false
            await this.loadBlockedUsers()
          } catch (error) {
            this.$message.error(this.$t('settings.blockFailed'))
          }
        }
      })
    },
    async handleUnblockUser(user) {
      try {
        await this.$confirm(
          this.$t('settings.unblockConfirm', { name: user.nickName }),
          this.$t('common.warning'),
          {
            confirmButtonText: this.$t('common.confirm'),
            cancelButtonText: this.$t('common.cancel'),
            type: 'warning',
          }
        )

        await unblockUser(user.userId)
        this.$message.success(this.$t('settings.unblockSuccess'))
        await this.loadBlockedUsers()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(this.$t('settings.unblockFailed'))
        }
      }
    },
  },
}
</script>

<style lang="scss" scoped>
.privacy-settings {
  .setting-card {
    margin-bottom: 20px;

    .header-with-button {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .setting-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 0;
      border-bottom: 1px solid var(--border-light);

      &:last-child {
        border-bottom: none;
      }

      .setting-info {
        flex: 1;
        margin-right: 20px;

        h4 {
          margin: 0 0 5px;
          color: var(--text-primary);
        }

        p {
          margin: 0;
          color: var(--text-secondary);
          font-size: 14px;
        }
      }

      .el-select {
        width: 150px;
      }
    }

    .empty-block {
      text-align: center;
      padding: 30px 0;
      color: var(--text-secondary);

      i {
        font-size: 48px;
        margin-bottom: 10px;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }

    .blocked-list {
      .blocked-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid var(--border-light);

        &:last-child {
          border-bottom: none;
        }

        .user-info {
          display: flex;
          align-items: center;

          .user-details {
            margin-left: 10px;

            h4 {
              margin: 0 0 5px;
              color: var(--text-primary);
            }

            p {
              margin: 0;
              color: var(--text-secondary);
              font-size: 12px;
            }
          }
        }
      }
    }
  }
}

.user-option {
  display: flex;
  align-items: center;

  .el-avatar {
    margin-right: 8px;
  }
}
</style>
