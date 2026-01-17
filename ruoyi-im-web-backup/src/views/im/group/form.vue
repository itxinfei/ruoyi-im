<template>
  <div class="app-container">
    <el-form ref="form" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="群组名称" prop="groupName">
        <el-input v-model="form.groupName" placeholder="请输入群组名称" />
      </el-form-item>
      <el-form-item label="群组简介" prop="description">
        <el-input v-model="form.description" type="textarea" placeholder="请输入群组简介" />
      </el-form-item>
      <el-form-item label="群组类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择群组类型">
          <el-option label="普通群组" value="0" />
          <el-option label="企业群组" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="最大成员数" prop="maxMemberCount">
        <el-input-number v-model="form.maxMemberCount" :min="2" :max="2000" />
      </el-form-item>
      <el-form-item label="加群验证" prop="joinAuth">
        <el-radio-group v-model="form.joinAuth">
          <el-radio label="0">无需验证</el-radio>
          <el-radio label="1">需要验证</el-radio>
          <el-radio label="2">禁止加群</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="群组状态">
        <el-radio-group v-model="form.status">
          <el-radio v-for="dict in statusOptions" :key="dict.dictValue" :label="dict.dictValue">{{
            dict.dictLabel
          }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="群组公告" prop="announcement">
        <el-input v-model="form.announcement" type="textarea" placeholder="请输入群组公告" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { getGroup, addGroup, updateGroup } from '@/api/im/group'

export default {
  name: 'ImGroupForm',
  data() {
    return {
      // 表单参数
      form: {
        groupId: undefined,
        groupName: undefined,
        description: undefined,
        type: '0',
        maxMemberCount: 200,
        joinAuth: '1',
        status: '0',
        announcement: undefined,
      },
      // 表单校验
      rules: {
        groupName: [
          { required: true, message: '群组名称不能为空', trigger: 'blur' },
          { min: 2, max: 20, message: '群组名称长度必须介于 2 和 20 之间', trigger: 'blur' },
        ],
        description: [{ max: 200, message: '群组简介长度不能超过200个字符', trigger: 'blur' }],
        announcement: [{ max: 500, message: '群组公告长度不能超过500个字符', trigger: 'blur' }],
      },
      // 状态数据字典
      statusOptions: [],
    }
  },
  created() {
    this.getDicts('sys_normal_disable').then(response => {
      this.statusOptions = response.data
    })
    const groupId = this.$route.query.groupId
    if (groupId) {
      this.form.groupId = groupId
      this.getInfo()
    }
  },
  methods: {
    /** 获取群组详细信息 */
    getInfo() {
      getGroup(this.form.groupId).then(response => {
        this.form = response.data
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.groupId) {
            updateGroup(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.cancel()
            })
          } else {
            addGroup(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.cancel()
            })
          }
        }
      })
    },
    /** 取消按钮 */
    cancel() {
      this.$router.push({ path: '/im/group' })
    },
  },
}
</script>
