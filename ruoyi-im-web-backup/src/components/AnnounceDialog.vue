<template>
  <el-dialog
    v-model="visible"
    title="发布群公告"
    width="600px"
    @close="handleClose"
  >
    <div class="announce-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            placeholder="请输入公告内容"
            :rows="6"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="发布范围">
          <el-radio-group v-model="form.scope">
            <el-radio label="all">全员可见</el-radio>
            <el-radio label="admin">仅管理员可见</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="置顶">
          <el-switch v-model="form.isPinned" />
          <span class="form-tip">置顶后公告将在群聊顶部显示</span>
        </el-form-item>
        
        <el-form-item label="确认阅读">
          <el-switch v-model="form.requireConfirm" />
          <span class="form-tip">成员需要确认阅读该公告</span>
        </el-form-item>
      </el-form>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button 
        type="primary" 
        :loading="publishing"
        @click="handlePublish"
      >
        发布
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const store = useStore()
const formRef = ref(null)
const publishing = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  title: '',
  content: '',
  scope: 'all',
  isPinned: false,
  requireConfirm: false
})

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 5, max: 1000, message: '内容长度在 5 到 1000 个字符', trigger: 'blur' }
  ]
}

async function handlePublish() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  
  publishing.value = true
  
  try {
    // 调用发布公告的API
    // await store.dispatch('im/publishGroupAnnouncement', {
    //   groupId: props.groupId,
    //   ...form
    // })
    
    // 临时模拟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('公告发布成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('发布公告失败:', error)
    ElMessage.error('发布公告失败：' + error.message)
  } finally {
    publishing.value = false
  }
}

function handleClose() {
  formRef.value?.resetFields()
  visible.value = false
}
</script>

<style scoped>
.announce-dialog {
  padding: 8px 0;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #8c8c8c;
}
</style>