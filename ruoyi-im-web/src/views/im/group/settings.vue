<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="clearfix">
          <span>{{ groupInfo.groupName }} - 群组设置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基本设置 -->
        <el-tab-pane label="基本设置" name="basic">
          <el-form ref="basicForm" :model="basicForm" :rules="basicRules" label-width="100px">
            <el-form-item label="群组名称" prop="groupName">
              <el-input v-model="basicForm.groupName" placeholder="请输入群组名称" />
            </el-form-item>
            <el-form-item label="群组简介" prop="description">
              <el-input
                v-model="basicForm.description"
                type="textarea"
                placeholder="请输入群组简介"
              />
            </el-form-item>
            <el-form-item label="群组公告" prop="announcement">
              <el-input
                v-model="basicForm.announcement"
                type="textarea"
                placeholder="请输入群组公告"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitBasicForm">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 权限设置 -->
        <el-tab-pane label="权限设置" name="permission">
          <el-form ref="permissionForm" :model="permissionForm" label-width="100px">
            <el-form-item label="加群验证">
              <el-radio-group v-model="permissionForm.joinAuth">
                <el-radio label="0">无需验证</el-radio>
                <el-radio label="1">需要验证</el-radio>
                <el-radio label="2">禁止加群</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="发言权限">
              <el-radio-group v-model="permissionForm.speakAuth">
                <el-radio label="0">所有人可发言</el-radio>
                <el-radio label="1">仅管理员可发言</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="邀请权限">
              <el-radio-group v-model="permissionForm.inviteAuth">
                <el-radio label="0">所有人可邀请</el-radio>
                <el-radio label="1">仅管理员可邀请</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitPermissionForm">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <el-form ref="notificationForm" :model="notificationForm" label-width="100px">
            <el-form-item label="消息通知">
              <el-switch v-model="notificationForm.messageNotify" />
            </el-form-item>
            <el-form-item label="@我通知">
              <el-switch v-model="notificationForm.mentionNotify" />
            </el-form-item>
            <el-form-item label="群公告通知">
              <el-switch v-model="notificationForm.announcementNotify" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitNotificationForm">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { getGroup, updateGroup, updateGroupSettings } from '@/api/im/group'

export default {
  name: 'ImGroupSettings',
  data() {
    return {
      // 激活的标签页
      activeTab: 'basic',
      // 群组信息
      groupInfo: {},
      // 基本设置表单
      basicForm: {
        groupId: undefined,
        groupName: undefined,
        description: undefined,
        announcement: undefined,
      },
      // 权限设置表单
      permissionForm: {
        groupId: undefined,
        joinAuth: '1',
        speakAuth: '0',
        inviteAuth: '0',
      },
      // 通知设置表单
      notificationForm: {
        groupId: undefined,
        messageNotify: true,
        mentionNotify: true,
        announcementNotify: true,
      },
      // 表单校验规则
      basicRules: {
        groupName: [
          { required: true, message: '群组名称不能为空', trigger: 'blur' },
          { min: 2, max: 20, message: '群组名称长度必须介于 2 和 20 之间', trigger: 'blur' },
        ],
        description: [{ max: 200, message: '群组简介长度不能超过200个字符', trigger: 'blur' }],
        announcement: [{ max: 500, message: '群组公告长度不能超过500个字符', trigger: 'blur' }],
      },
    }
  },
  created() {
    const groupId = this.$route.query.groupId
    if (groupId) {
      this.basicForm.groupId = groupId
      this.permissionForm.groupId = groupId
      this.notificationForm.groupId = groupId
      this.getInfo()
    }
  },
  methods: {
    /** 获取群组详细信息 */
    getInfo() {
      getGroup(this.basicForm.groupId).then(response => {
        this.groupInfo = response.data
        this.basicForm = {
          ...this.basicForm,
          groupName: response.data.groupName,
          description: response.data.description,
          announcement: response.data.announcement,
        }
        this.permissionForm = {
          ...this.permissionForm,
          joinAuth: response.data.joinAuth,
          speakAuth: response.data.speakAuth,
          inviteAuth: response.data.inviteAuth,
        }
        // 通知设置可能需要从其他API获取
      })
    },
    /** 提交基本设置 */
    submitBasicForm() {
      this.$refs['basicForm'].validate(valid => {
        if (valid) {
          updateGroup(this.basicForm).then(() => {
            this.$modal.msgSuccess('保存成功')
            this.getInfo()
          })
        }
      })
    },
    submitPermissionForm() {
      updateGroupSettings({
        ...this.permissionForm,
        type: 'permission',
      }).then(() => {
        this.$modal.msgSuccess('保存成功')
        this.getInfo()
      })
    },
    /** 提交通知设置 */
    submitNotificationForm() {
      updateGroupSettings({
        ...this.notificationForm,
        type: 'notification',
      }).then(() => {
        this.$modal.msgSuccess('保存成功')
      })
    },
  },
}
</script>

<style scoped>
.box-card {
  margin-bottom: 20px;
}
.el-form {
  max-width: 600px;
}
</style>
