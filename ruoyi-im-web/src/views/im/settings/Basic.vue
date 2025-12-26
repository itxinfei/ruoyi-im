<template>
  <div class="basic-settings">
    <el-card class="profile-card">
      <div class="profile-header">
        <div class="avatar-container">
          <el-avatar
            :size="100"
            :src="userInfo.avatar || defaultAvatar"
            @click="handleClickAvatar"
          />
          <div class="avatar-actions">
            <el-upload
              ref="upload"
              :action="uploadUrl"
              :headers="headers"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
            >
              <el-button size="small" type="primary">
                {{ $t('settings.changeAvatar') }}
              </el-button>
            </el-upload>
          </div>
        </div>
        <div class="user-info">
          <h2>{{ userInfo.nickName }}</h2>
          <p>{{ userInfo.email }}</p>
        </div>
      </div>
    </el-card>

    <el-card class="info-card">
      <template #header>
        <div>
          <span>{{ $t('settings.basicInfo') }}</span>
        </div>
      </template>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('settings.nickName')" prop="nickName">
          <el-input v-model="form.nickName" />
        </el-form-item>

        <el-form-item :label="$t('settings.gender')" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="0">{{ $t('settings.male') }}</el-radio>
            <el-radio :label="1">{{ $t('settings.female') }}</el-radio>
            <el-radio :label="2">{{ $t('settings.other') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="$t('settings.birthday')" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            :placeholder="$t('settings.selectDate')"
          />
        </el-form-item>

        <el-form-item :label="$t('settings.phone')" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>

        <el-form-item :label="$t('settings.email')" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item :label="$t('settings.signature')" prop="signature">
          <el-input
            v-model="form.signature"
            type="textarea"
            :rows="3"
            :maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">
            {{ $t('common.save') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="security-card">
      <template #header>
        <div>
          <span>{{ $t('settings.security') }}</span>
        </div>
      </template>

      <div class="security-item">
        <div class="security-info">
          <h4>{{ $t('settings.password') }}</h4>
          <p>{{ $t('settings.passwordDesc') }}</p>
        </div>
        <el-button type="text" @click="handleChangePassword">
          {{ $t('settings.change') }}
        </el-button>
      </div>

      <div class="security-item">
        <div class="security-info">
          <h4>{{ $t('settings.phoneVerification') }}</h4>
          <p>{{ form.phone || $t('settings.notBound') }}</p>
        </div>
        <el-button type="text" @click="handleBindPhone">
          {{ form.phone ? $t('settings.change') : $t('settings.bind') }}
        </el-button>
      </div>

      <div class="security-item">
        <div class="security-info">
          <h4>{{ $t('settings.emailVerification') }}</h4>
          <p>{{ form.email || $t('settings.notBound') }}</p>
        </div>
        <el-button type="text" @click="handleBindEmail">
          {{ form.email ? $t('settings.change') : $t('settings.bind') }}
        </el-button>
      </div>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialog.visible"
      :title="$t('settings.changePassword')"
      width="400px"
    >
      <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item :label="$t('settings.oldPassword')" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>

        <el-form-item :label="$t('settings.newPassword')" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>

        <el-form-item :label="$t('settings.confirmPassword')" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <div>
          <el-button @click="passwordDialog.visible = false">
            {{ $t('common.cancel') }}
          </el-button>
          <el-button type="primary" @click="handlePasswordSubmit">
            {{ $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { updateUserProfile, updatePassword } from '@/api/user'
import defaultAvatar from '@/assets/images/profile.jpg'

export default {
  name: 'BasicSettings',
  data() {
    // 验证两次密码是否一致
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error(this.$t('settings.passwordNotMatch')))
      } else {
        callback()
      }
    }

    return {
      defaultAvatar,
      uploadUrl: process.env.VUE_APP_BASE_API + '/system/user/profile/avatar',
      form: {
        nickName: '',
        gender: 0,
        birthday: '',
        phone: '',
        email: '',
        signature: '',
      },
      rules: {
        nickName: [
          { required: true, message: this.$t('settings.nickNameRequired'), trigger: 'blur' },
        ],
        email: [{ type: 'email', message: this.$t('settings.emailInvalid'), trigger: 'blur' }],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: this.$t('settings.phoneInvalid'), trigger: 'blur' },
        ],
      },
      passwordDialog: {
        visible: false,
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: this.$t('settings.oldPasswordRequired'), trigger: 'blur' },
        ],
        newPassword: [
          { required: true, message: this.$t('settings.newPasswordRequired'), trigger: 'blur' },
          { min: 6, message: this.$t('settings.passwordLength'), trigger: 'blur' },
        ],
        confirmPassword: [
          { required: true, message: this.$t('settings.confirmPasswordRequired'), trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' },
        ],
      },
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    headers() {
      return {
        Authorization: 'Bearer ' + this.$store.getters.token,
      }
    },
  },
  created() {
    this.initForm()
  },
  methods: {
    initForm() {
      this.form = {
        ...this.form,
        ...this.userInfo,
      }
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        this.$message.error(this.$t('settings.avatarTypeError'))
      }
      if (!isLt2M) {
        this.$message.error(this.$t('settings.avatarSizeError'))
      }
      return isJPG && isLt2M
    },
    handleAvatarSuccess(res) {
      if (res.code === 200) {
        this.$store.dispatch('user/setAvatar', res.data)
        this.$message.success(this.$t('settings.avatarUpdateSuccess'))
      } else {
        this.$message.error(res.msg)
      }
    },
    handleAvatarError() {
      this.$message.error(this.$t('settings.avatarUpdateError'))
    },
    handleClickAvatar() {
      this.$refs.upload.$el.click()
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          updateUserProfile(this.form).then(res => {
            if (res.code === 200) {
              this.$store.dispatch('user/setUserInfo', res.data)
              this.$message.success(this.$t('settings.updateSuccess'))
            }
          })
        }
      })
    },
    handleChangePassword() {
      this.passwordDialog.visible = true
    },
    handlePasswordSubmit() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          updatePassword(this.passwordForm).then(res => {
            if (res.code === 200) {
              this.$message.success(this.$t('settings.passwordUpdateSuccess'))
              this.passwordDialog.visible = false
              this.passwordForm = {
                oldPassword: '',
                newPassword: '',
                confirmPassword: '',
              }
              // 密码修改成功后需要重新登录
              this.$store.dispatch('user/logout').then(() => {
                this.$router.push('/login')
              })
            }
          })
        }
      })
    },
    handleBindPhone() {
      this.$message.info(this.$t('settings.comingSoon'))
    },
    handleBindEmail() {
      this.$message.info(this.$t('settings.comingSoon'))
    },
  },
}
</script>

<style lang="scss" scoped>
.basic-settings {
  .profile-card {
    margin-bottom: 20px;

    .profile-header {
      display: flex;
      align-items: center;
      padding: 20px;

      .avatar-container {
        position: relative;
        margin-right: 30px;

        .avatar-actions {
          margin-top: 10px;
          text-align: center;
        }
      }

      .user-info {
        h2 {
          margin: 0 0 10px;
          color: var(--text-primary);
        }

        p {
          margin: 0;
          color: var(--text-secondary);
        }
      }
    }
  }

  .info-card {
    margin-bottom: 20px;
  }

  .security-card {
    .security-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 0;
      border-bottom: 1px solid var(--border-light);

      &:last-child {
        border-bottom: none;
      }

      .security-info {
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
    }
  }
}
</style>
