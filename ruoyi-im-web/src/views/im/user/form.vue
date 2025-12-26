<template>
  <div class="app-container">
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="用户名称" prop="userName">
        <el-input v-model="form.userName" placeholder="请输入用户名称" />
      </el-form-item>
      <el-form-item label="手机号码" prop="phonenumber">
        <el-input v-model="form.phonenumber" placeholder="请输入手机号码" maxlength="11" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
      </el-form-item>
      <el-form-item v-if="!form.userId" label="密码" prop="password">
        <el-input v-model="form.password" placeholder="请输入密码" type="password" />
      </el-form-item>
      <el-form-item label="用户状态">
        <el-radio-group v-model="form.status">
          <el-radio v-for="dict in statusOptions" :key="dict.dictValue" :label="dict.dictValue">{{
            dict.dictLabel
          }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { getUser, addUser, updateUser } from '@/api/im/user'

export default {
  name: 'ImUserForm',
  data() {
    return {
      // 表单参数
      form: {
        userId: undefined,
        userName: undefined,
        phonenumber: undefined,
        email: undefined,
        password: undefined,
        status: '0',
        remark: undefined,
      },
      // 表单校验
      rules: {
        userName: [{ required: true, message: '用户名称不能为空', trigger: 'blur' }],
        phonenumber: [
          { required: true, message: '手机号码不能为空', trigger: 'blur' },
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: '请输入正确的手机号码',
            trigger: 'blur',
          },
        ],
        email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
        password: [
          { required: true, message: '密码不能为空', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' },
        ],
      },
      // 状态数据字典
      statusOptions: [],
    }
  },
  created() {
    this.getDicts('sys_normal_disable').then(response => {
      this.statusOptions = response.data
    })
    const userId = this.$route.query.userId
    if (userId) {
      this.form.userId = userId
      this.getInfo()
    }
  },
  methods: {
    /** 获取用户详细信息 */
    getInfo() {
      getUser(this.form.userId).then(response => {
        this.form = response.data
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.userId) {
            updateUser(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.cancel()
            })
          } else {
            addUser(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.cancel()
            })
          }
        }
      })
    },
    /** 取消按钮 */
    cancel() {
      this.$router.push({ path: '/im/user' })
    },
  },
}
</script>
