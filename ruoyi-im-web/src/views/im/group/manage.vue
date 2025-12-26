<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="clearfix">
          <span>{{ groupInfo.groupName }}</span>
          <el-button
            v-hasPermi="['im:group:member:add']"
            style="float: right; padding: 3px 0"
            type="text"
            @click="handleAddMember"
            >添加成员</el-button
          >
        </div>
      </template>

      <el-table v-loading="loading" :data="memberList">
        <el-table-column label="成员头像" align="center" prop="avatar" width="100">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar">
              {{ scope.row.nickName ? scope.row.nickName.charAt(0) : '?' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="成员昵称" align="center" prop="nickName" />
        <el-table-column label="角色" align="center" prop="role">
          <template #default="scope">
            <el-tag
              :type="scope.row.role === '1' ? 'danger' : scope.row.role === '2' ? 'warning' : ''"
            >
              {{ scope.row.role === '1' ? '群主' : scope.row.role === '2' ? '管理员' : '成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加入时间" align="center" prop="joinTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.joinTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              v-if="groupInfo.role === '1' && scope.row.role !== '1'"
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleSetRole(scope.row)"
              >设置角色</el-button
            >
            <el-button
              v-if="groupInfo.role === '1' || (groupInfo.role === '2' && scope.row.role === '0')"
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleRemove(scope.row)"
              >移出群组</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="open" :title="'添加成员'" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="选择成员">
          <el-select
            v-model="form.userIds"
            multiple
            filterable
            remote
            reserve-keyword
            placeholder="请输入用户名称搜索"
            :remote-method="remoteMethod"
            :loading="loading"
          >
            <el-option
              v-for="item in options"
              :key="item.userId"
              :label="item.nickName"
              :value="item.userId"
            >
              <span style="float: left">{{ item.nickName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.userName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 设置角色对话框 -->
    <el-dialog v-model:visible="roleOpen" :title="'设置角色'" width="500px" append-to-body>
      <el-form ref="roleForm" :model="roleForm" label-width="80px">
        <el-form-item label="成员角色">
          <el-radio-group v-model="roleForm.role">
            <el-radio label="0">普通成员</el-radio>
            <el-radio label="2">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitRoleForm">确 定</el-button>
          <el-button @click="cancelRole">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getGroup } from '@/api/im/group'
import {
  listGroupMember,
  addGroupMember,
  removeGroupMember,
  updateMemberRole,
} from '@/api/im/groupMember'
import { listUser } from '@/api/system/user'
import { modal } from '@/utils/message'
import Pagination from '@/components/Pagination'

export default {
  name: 'ImGroupManage',
  components: {
    Pagination,
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 成员列表
      memberList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 是否显示角色设置弹出层
      roleOpen: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        groupId: undefined,
      },
      // 群组信息
      groupInfo: {},
      // 表单参数
      form: {
        groupId: undefined,
        userIds: [],
      },
      // 角色表单参数
      roleForm: {
        groupId: undefined,
        userId: undefined,
        role: '0',
      },
      // 表单校验
      rules: {
        userIds: [{ required: true, message: '请选择要添加的成员', trigger: 'change' }],
      },
      // 用户选项
      options: [],
    }
  },
  created() {
    const groupId = this.$route.query.groupId
    if (groupId) {
      this.queryParams.groupId = groupId
      this.form.groupId = groupId
      this.roleForm.groupId = groupId
      this.getGroupInfo()
      this.getList()
    }
  },
  methods: {
    /** 查询群组信息 */
    getGroupInfo() {
      getGroup(this.queryParams.groupId).then(response => {
        this.groupInfo = response.data
      })
    },
    /** 查询成员列表 */
    getList() {
      this.loading = true
      listGroupMember(this.queryParams).then(response => {
        this.memberList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 搜索用户 */
    remoteMethod(query) {
      if (query !== '') {
        this.loading = true
        listUser({
          pageSize: 10,
          userName: query,
        }).then(response => {
          this.options = response.rows
          this.loading = false
        })
      } else {
        this.options = []
      }
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 取消角色设置按钮 */
    cancelRole() {
      this.roleOpen = false
      this.roleForm.role = '0'
      this.roleForm.userId = undefined
    },
    /** 表单重置 */
    reset() {
      this.form.userIds = []
    },
    /** 添加成员按钮操作 */
    handleAddMember() {
      this.reset()
      this.open = true
      this.title = '添加成员'
    },
    /** 移出成员按钮操作 */
    handleRemove(row) {
      modal
        .confirm('确认要将该成员移出群组吗？')
        .then(() => {
          return removeGroupMember({
            groupId: this.queryParams.groupId,
            userId: row.userId,
          })
        })
        .then(() => {
          this.getList()
          modal.success('移出成功')
        })
        .catch(() => {})
    },
    /** 设置角色按钮操作 */
    handleSetRole(row) {
      this.roleForm.userId = row.userId
      this.roleForm.role = row.role
      this.roleOpen = true
    },
    /** 提交添加成员按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          addGroupMember(this.form).then(() => {
            modal.success('添加成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    /** 提交角色设置按钮 */
    submitRoleForm() {
      updateMemberRole(this.roleForm).then(() => {
        this.$modal.msgSuccess('设置成功')
        this.roleOpen = false
        this.getList()
      })
    },
  },
}
</script>

<style lang="scss" scoped>
.app-container {
  .box-card {
    margin-bottom: 20px;
  }

  .el-table {
    margin-top: 15px;
  }

  .el-select {
    width: 100%;
  }

  .dialog-footer {
    text-align: right;
  }

  .clearfix:before,
  .clearfix:after {
    display: table;
    content: '';
  }
  .clearfix:after {
    clear: both;
  }
}
</style>
