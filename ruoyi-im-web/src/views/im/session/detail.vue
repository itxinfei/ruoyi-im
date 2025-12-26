<template>
  <div class="app-container">
    <el-card class="box-card">
      <!-- 会话基本信息 -->
      <template #header>
        <div class="clearfix">
          <span>会话信息</span>
          <el-button
            v-hasPermi="['im:session:update']"
            style="float: right; padding: 3px 0"
            type="text"
            @click="handleEdit"
            >编辑</el-button
          >
        </div>
      </template>
      <el-row :gutter="20" class="session-info">
        <el-col :span="4">
          <el-avatar :size="100" :src="session.sessionAvatar">
            {{ session.sessionName ? session.sessionName.charAt(0) : '?' }}
          </el-avatar>
        </el-col>
        <el-col :span="20">
          <el-descriptions :column="2">
            <el-descriptions-item label="会话名称">{{ session.sessionName }}</el-descriptions-item>
            <el-descriptions-item label="会话类型">
              <dict-tag :options="dict.type.im_session_type" :value="session.sessionType" />
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{
              parseTime(session.createTime)
            }}</el-descriptions-item>
            <el-descriptions-item label="创建者">{{ session.createBy }}</el-descriptions-item>
            <el-descriptions-item label="最后活动时间">{{
              parseTime(session.lastActiveTime)
            }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <dict-tag :options="dict.type.sys_normal_disable" :value="session.status" />
            </el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
    </el-card>

    <!-- 会话成员 -->
    <el-card class="box-card" style="margin-top: 20px">
      <template #header>
        <div class="clearfix">
          <span>会话成员</span>
          <el-button-group style="float: right">
            <el-button
              v-hasPermi="['im:session:member:add']"
              type="text"
              icon="el-icon-plus"
              @click="handleAddMember"
              >添加成员</el-button
            >
            <el-button
              v-hasPermi="['im:session:member:remove']"
              type="text"
              icon="el-icon-delete"
              @click="handleRemoveMember"
              >移除成员</el-button
            >
          </el-button-group>
        </div>
      </template>
      <el-table
        v-loading="memberLoading"
        :data="memberList"
        @selection-change="handleMemberSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="用户头像" align="center" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar">
              {{ scope.row.nickName ? scope.row.nickName.charAt(0) : '?' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="用户昵称" prop="nickName" />
        <el-table-column label="角色" align="center">
          <template #default="scope">
            <dict-tag :options="dict.type.im_member_role" :value="scope.row.role" />
          </template>
        </el-table-column>
        <el-table-column label="加入时间" align="center" prop="joinTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.joinTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center">
          <template #default="scope">
            <dict-tag :options="dict.type.im_member_status" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              v-hasPermi="['im:session:member:update']"
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdateMember(scope.row)"
              >修改</el-button
            >
            <el-button
              v-hasPermi="['im:session:member:remove']"
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleRemoveMember(scope.row)"
              >移除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 消息历史 -->
    <el-card class="box-card" style="margin-top: 20px">
      <template #header>
        <div class="clearfix">
          <span>消息历史</span>
          <el-button-group style="float: right">
            <el-button
              v-hasPermi="['im:message:export']"
              type="text"
              icon="el-icon-download"
              @click="handleExportMessages"
              >导出消息</el-button
            >
          </el-button-group>
        </div>
      </template>
      <el-table v-loading="messageLoading" :data="messageList">
        <el-table-column label="发送者" align="center" width="120">
          <template #default="scope">
            <el-avatar :size="30" :src="scope.row.senderAvatar">
              {{ scope.row.senderName ? scope.row.senderName.charAt(0) : '?' }}
            </el-avatar>
            <div>{{ scope.row.senderName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="消息内容" prop="content" :show-overflow-tooltip="true" />
        <el-table-column label="消息类型" align="center" width="100">
          <template #default="scope">
            <dict-tag :options="dict.type.im_message_type" :value="scope.row.messageType" />
          </template>
        </el-table-column>
        <el-table-column label="发送时间" align="center" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="messageTotal > 0"
        v-model:page="messageQuery.pageNum"
        v-model:limit="messageQuery.pageSize"
        :total="messageTotal"
        @pagination="getMessageList"
      />
    </el-card>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="memberOpen" :title="memberTitle" width="500px" append-to-body>
      <el-form ref="memberForm" :model="memberForm" :rules="memberRules" label-width="80px">
        <el-form-item label="选择用户" prop="userIds">
          <el-select
            v-model="memberForm.userIds"
            multiple
            filterable
            remote
            reserve-keyword
            placeholder="请输入用户名称搜索"
            :remote-method="remoteSearchUser"
            :loading="userLoading"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.userId"
              :label="item.nickName"
              :value="item.userId"
            >
              <el-avatar
                :size="24"
                :src="item.avatar"
                style="vertical-align: middle; margin-right: 8px"
              >
                {{ item.nickName.charAt(0) }}
              </el-avatar>
              <span>{{ item.nickName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="成员角色" prop="role">
          <el-radio-group v-model="memberForm.role">
            <el-radio :label="0">普通成员</el-radio>
            <el-radio :label="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitMemberForm">确 定</el-button>
          <el-button @click="cancelMember">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getSession } from '@/api/im/session'
import { listSessionMembers, addSessionMember, removeSessionMember } from '@/api/im/session'
import { listSessionMessages } from '@/api/im/message'
import { listUsers } from '@/api/system/user'

export default {
  name: 'SessionDetail',
  dicts: [
    'sys_normal_disable',
    'im_session_type',
    'im_member_role',
    'im_member_status',
    'im_message_type',
  ],
  data() {
    return {
      // 会话信息
      session: {},
      // 成员加载状态
      memberLoading: true,
      // 成员列表数据
      memberList: [],
      // 选中的成员
      selectedMembers: [],
      // 消息加载状态
      messageLoading: true,
      // 消息列表数据
      messageList: [],
      // 消息总数
      messageTotal: 0,
      // 消息查询参数
      messageQuery: {
        pageNum: 1,
        pageSize: 10,
        sessionId: null,
      },
      // 成员对话框标题
      memberTitle: '',
      // 成员对话框显示状态
      memberOpen: false,
      // 成员表单数据
      memberForm: {
        userIds: [],
        role: 0,
      },
      // 成员表单校验规则
      memberRules: {
        userIds: [{ required: true, message: '请选择用户', trigger: 'change' }],
      },
      // 用户选项加载状态
      userLoading: false,
      // 用户选项数据
      userOptions: [],
    }
  },
  created() {
    const sessionId = this.$route.query.sessionId
    this.messageQuery.sessionId = sessionId
    this.getSessionInfo(sessionId)
    this.getMemberList(sessionId)
    this.getMessageList()
  },
  methods: {
    /** 获取会话信息 */
    getSessionInfo(sessionId) {
      getSession(sessionId).then(response => {
        this.session = response.data
      })
    },
    /** 获取成员列表 */
    getMemberList(sessionId) {
      this.memberLoading = true
      listSessionMembers(sessionId).then(response => {
        this.memberList = response.data
        this.memberLoading = false
      })
    },
    /** 获取消息列表 */
    getMessageList() {
      this.messageLoading = true
      listSessionMessages(this.messageQuery).then(response => {
        this.messageList = response.rows
        this.messageTotal = response.total
        this.messageLoading = false
      })
    },
    /** 编辑会话信息 */
    handleEdit() {
      this.$router.push({ path: '/im/session', query: { sessionId: this.session.sessionId } })
    },
    /** 添加成员按钮操作 */
    handleAddMember() {
      this.memberTitle = '添加成员'
      this.memberOpen = true
    },
    /** 移除成员按钮操作 */
    handleRemoveMember(row) {
      const memberIds = row.userId || this.selectedMembers.map(item => item.userId)
      this.$modal
        .confirm('是否确认移除选中的成员？')
        .then(function () {
          return removeSessionMember(this.session.sessionId, memberIds)
        })
        .then(() => {
          this.getMemberList(this.session.sessionId)
          this.$modal.msgSuccess('移除成功')
        })
        .catch(() => {})
    },
    /** 导出消息按钮操作 */
    handleExportMessages() {
      this.$download.excel(
        '/im/message/export',
        {
          sessionId: this.session.sessionId,
        },
        `会话${this.session.sessionId}消息记录.xlsx`
      )
    },
    /** 成员多选框选中数据 */
    handleMemberSelectionChange(selection) {
      this.selectedMembers = selection
    },
    /** 远程搜索用户 */
    remoteSearchUser(query) {
      if (query !== '') {
        this.userLoading = true
        listUsers({
          pageSize: 20,
          nickName: query,
        }).then(response => {
          this.userOptions = response.rows
          this.userLoading = false
        })
      } else {
        this.userOptions = []
      }
    },
    /** 取消成员按钮操作 */
    cancelMember() {
      this.memberOpen = false
      this.resetMemberForm()
    },
    /** 重置成员表单 */
    resetMemberForm() {
      this.memberForm = {
        userIds: [],
        role: 0,
      }
      this.resetForm('memberForm')
    },
    /** 提交成员表单 */
    submitMemberForm() {
      this.$refs['memberForm'].validate(valid => {
        if (valid) {
          addSessionMember({
            sessionId: this.session.sessionId,
            userIds: this.memberForm.userIds,
            role: this.memberForm.role,
          }).then(() => {
            this.$modal.msgSuccess('添加成功')
            this.memberOpen = false
            this.getMemberList(this.session.sessionId)
          })
        }
      })
    },
  },
}
</script>

<style scoped>
.session-info {
  margin-bottom: 20px;
}
.box-card {
  width: 100%;
  margin-bottom: 20px;
}
</style>
