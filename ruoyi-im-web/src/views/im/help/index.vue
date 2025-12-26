<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 文档目录树 -->
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>文档目录</span>
              <el-button
                v-if="checkPermission(['im:help:add'])"
                style="float: right; padding: 3px 0"
                type="text"
                icon="el-icon-plus"
                @click="handleAddCategory"
                >新增</el-button
              >
            </div>
          </template>
          <el-tree
            :data="categoryTree"
            :props="defaultProps"
            :highlight-current="true"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ node.label }}</span>
                <span v-if="checkPermission(['im:help:edit', 'im:help:remove'])">
                  <el-button type="text" size="mini" @click.stop="handleEditCategory(data)"
                    >编辑</el-button
                  >
                  <el-button type="text" size="mini" @click.stop="handleDeleteCategory(data)"
                    >删除</el-button
                  >
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 文档内容 -->
      <el-col :span="18">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>{{ currentDoc.title || '文档内容' }}</span>
              <div v-if="currentDoc.id" style="float: right">
                <el-button
                  v-hasPermi="['im:help:edit']"
                  type="text"
                  icon="el-icon-edit"
                  @click="handleEdit"
                  >编辑</el-button
                >
                <el-button
                  v-hasPermi="['im:help:remove']"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleDelete"
                  >删除</el-button
                >
                <el-button
                  v-hasPermi="['im:help:add']"
                  type="text"
                  icon="el-icon-document-copy"
                  @click="handleCopy"
                  >复制</el-button
                >
              </div>
            </div>
          </template>
          <!-- 文档展示区 -->
          <div v-if="!isEditing" class="doc-content" v-html="currentDoc.content"></div>
          <!-- 文档编辑区 -->
          <div v-else>
            <el-form ref="form" :model="form" :rules="rules" label-width="80px">
              <el-form-item label="文档标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入文档标题" />
              </el-form-item>
              <el-form-item label="所属分类" prop="categoryId">
                <el-cascader
                  v-model="form.categoryId"
                  :options="categoryOptions"
                  :props="{ checkStrictly: true }"
                  placeholder="请选择所属分类"
                />
              </el-form-item>
              <el-form-item label="文档类型" prop="type">
                <el-select v-model="form.type" placeholder="请选择文档类型">
                  <el-option label="使用手册" value="manual" />
                  <el-option label="API文档" value="api" />
                  <el-option label="常见问题" value="faq" />
                  <el-option label="更新说明" value="changelog" />
                  <el-option label="开发指南" value="guide" />
                </el-select>
              </el-form-item>
              <el-form-item label="文档内容" prop="content">
                <editor v-model="form.content" :min-height="400" />
              </el-form-item>
              <el-form-item label="排序号" prop="orderNum">
                <el-input-number v-model="form.orderNum" :min="0" :max="999" />
              </el-form-item>
              <el-form-item label="状态">
                <el-radio-group v-model="form.status">
                  <el-radio label="0">正常</el-radio>
                  <el-radio label="1">隐藏</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitForm">保存</el-button>
                <el-button @click="cancelEdit">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类编辑对话框 -->
    <el-dialog v-model:visible="categoryOpen" :title="categoryTitle" width="500px" append-to-body>
      <el-form ref="categoryForm" :model="categoryForm" :rules="categoryRules" label-width="80px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类" prop="parentId">
          <el-cascader
            v-model="categoryForm.parentId"
            :options="categoryOptions"
            :props="{ checkStrictly: true }"
            placeholder="顶级分类"
          />
        </el-form-item>
        <el-form-item label="排序号" prop="orderNum">
          <el-input-number v-model="categoryForm.orderNum" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitCategoryForm">确 定</el-button>
          <el-button @click="cancelCategory">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  listCategory,
  delCategory,
  addCategory,
  updateCategory,
  getDoc,
  delDoc,
  addDoc,
  updateDoc,
} from '@/api/im/help'
import Editor from '@/components/Editor'

export default {
  name: 'ImHelp',
  components: {
    Editor,
  },
  data() {
    return {
      // 分类树数据
      categoryTree: [],
      // 分类选项
      categoryOptions: [],
      // 树形配置
      defaultProps: {
        children: 'children',
        label: 'categoryName',
      },
      // 当前文档
      currentDoc: {},
      // 是否处于编辑状态
      isEditing: false,
      // 分类弹窗标题
      categoryTitle: '',
      // 是否显示分类弹窗
      categoryOpen: false,
      // 表单参数
      form: {
        id: undefined,
        title: undefined,
        categoryId: undefined,
        type: undefined,
        content: undefined,
        orderNum: 0,
        status: '0',
      },
      // 分类表单参数
      categoryForm: {
        categoryId: undefined,
        categoryName: undefined,
        parentId: undefined,
        orderNum: 0,
      },
      // 表单校验
      rules: {
        title: [{ required: true, message: '文档标题不能为空', trigger: 'blur' }],
        categoryId: [{ required: true, message: '所属分类不能为空', trigger: 'change' }],
        type: [{ required: true, message: '文档类型不能为空', trigger: 'change' }],
        content: [{ required: true, message: '文档内容不能为空', trigger: 'blur' }],
      },
      // 分类表单校验
      categoryRules: {
        categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
      },
    }
  },
  created() {
    this.getCategories()
  },
  methods: {
    /** 获取分类列表 */
    getCategories() {
      listCategory().then(response => {
        this.categoryTree = this.handleTree(response.data, 'categoryId')
        this.categoryOptions = this.categoryTree
      })
    },
    /** 树节点点击事件 */
    handleNodeClick(data) {
      this.isEditing = false
      if (data.docId) {
        getDoc(data.docId).then(response => {
          this.currentDoc = response.data
        })
      } else {
        this.currentDoc = {}
      }
    },
    /** 新增分类 */
    handleAddCategory() {
      this.categoryTitle = '添加分类'
      this.categoryForm = {
        categoryId: undefined,
        categoryName: undefined,
        parentId: undefined,
        orderNum: 0,
      }
      this.categoryOpen = true
    },
    /** 编辑分类 */
    handleEditCategory(data) {
      this.categoryTitle = '修改分类'
      this.categoryForm = Object.assign({}, data)
      this.categoryOpen = true
    },
    /** 删除分类 */
    handleDeleteCategory(data) {
      this.$modal
        .confirm('是否确认删除名称为"' + data.categoryName + '"的分类？')
        .then(function () {
          return delCategory(data.categoryId)
        })
        .then(() => {
          this.getCategories()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    /** 提交分类 */
    submitCategoryForm() {
      this.$refs['categoryForm'].validate(valid => {
        if (valid) {
          if (this.categoryForm.categoryId != undefined) {
            updateCategory(this.categoryForm).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.categoryOpen = false
              this.getCategories()
            })
          } else {
            addCategory(this.categoryForm).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.categoryOpen = false
              this.getCategories()
            })
          }
        }
      })
    },
    /** 取消分类 */
    cancelCategory() {
      this.categoryOpen = false
      this.categoryForm = {
        categoryId: undefined,
        categoryName: undefined,
        parentId: undefined,
        orderNum: 0,
      }
      this.resetForm('categoryForm')
    },
    /** 编辑文档 */
    handleEdit() {
      this.form = Object.assign({}, this.currentDoc)
      this.isEditing = true
    },
    /** 删除文档 */
    handleDelete() {
      this.$modal
        .confirm('是否确认删除该文档？')
        .then(function () {
          return delDoc(this.currentDoc.id)
        })
        .then(() => {
          this.getCategories()
          this.currentDoc = {}
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    /** 复制文档 */
    handleCopy() {
      const doc = Object.assign({}, this.currentDoc)
      doc.id = undefined
      doc.title = doc.title + '（副本）'
      addDoc(doc).then(() => {
        this.getCategories()
        this.$modal.msgSuccess('复制成功')
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateDoc(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.isEditing = false
              this.currentDoc = this.form
            })
          } else {
            addDoc(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.isEditing = false
              this.currentDoc = this.form
              this.getCategories()
            })
          }
        }
      })
    },
    /** 取消编辑 */
    cancelEdit() {
      this.isEditing = false
      this.form = {
        id: undefined,
        title: undefined,
        categoryId: undefined,
        type: undefined,
        content: undefined,
        orderNum: 0,
        status: '0',
      }
    },
  },
}
</script>

<style scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
.doc-content {
  min-height: 400px;
  padding: 10px;
}
</style>
