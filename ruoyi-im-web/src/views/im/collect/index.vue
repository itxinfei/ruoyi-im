<template>
  <div class="app-container">
    <!-- 顶部工具栏 -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-folder-add"
          size="mini"
          @click="handleAddFolder"
          >新建文件夹</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleBatchDelete"
          >批量删除</el-button
        >
      </el-col>
    </el-row>

    <!-- 文件夹和收藏列表 -->
    <el-row :gutter="20">
      <!-- 左侧文件夹树 -->
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div>
              <span>收藏夹</span>
            </div>
          </template>
          <el-tree
            :data="folderTree"
            :props="defaultProps"
            default-expand-all
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>
                  <i :class="data.type === 'folder' ? 'el-icon-folder' : 'el-icon-document'"></i>
                  {{ node.label }}
                </span>
                <span v-if="data.type === 'folder'">
                  <el-dropdown
                    trigger="click"
                    @command="command => handleFolderCommand(command, data)"
                  >
                    <span class="el-dropdown-link">
                      <i class="el-icon-more"></i>
                    </span>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="rename">重命名</el-dropdown-item>
                        <el-dropdown-item command="delete">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧收藏内容 -->
      <el-col :span="18">
        <el-card>
          <template #header>
            <div>
              <el-input
                v-model="searchText"
                placeholder="搜索收藏内容"
                prefix-icon="el-icon-search"
                clearable
                @clear="handleSearch"
                @input="handleSearch"
              ></el-input>
            </div>
          </template>

          <!-- 收藏列表 -->
          <el-table
            v-loading="loading"
            :data="collectList"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="类型" align="center" width="80">
              <template #default="scope">
                <el-tag :type="getTypeTag(scope.row.type)">{{
                  getTypeLabel(scope.row.type)
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="内容" align="left" :show-overflow-tooltip="true">
              <template #default="scope">
                <div class="collect-content" @click="handlePreview(scope.row)">
                  <!-- 文本类型 -->
                  <template v-if="scope.row.type === 'text'">
                    {{ scope.row.content }}
                  </template>
                  <!-- 图片类型 -->
                  <template v-else-if="scope.row.type === 'image'">
                    <el-image
                      style="width: 50px; height: 50px"
                      :src="scope.row.content"
                      :preview-src-list="[scope.row.content]"
                    ></el-image>
                  </template>
                  <!-- 文件类型 -->
                  <template v-else-if="scope.row.type === 'file'">
                    <i class="el-icon-document"></i>
                    {{ scope.row.fileName }}
                  </template>
                  <!-- 链接类型 -->
                  <template v-else-if="scope.row.type === 'link'">
                    <i class="el-icon-link"></i>
                    {{ scope.row.title }}
                  </template>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="收藏时间" align="center" width="180">
              <template #default="scope">
                {{ scope.row.createTime }}
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="150">
              <template #default="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-edit"
                  @click="handleMove(scope.row)"
                  >移动</el-button
                >
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleDelete(scope.row)"
                  >删除</el-button
                >
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <pagination
            v-show="total > 0"
            :page="queryParams.pageNum"
            :limit="queryParams.pageSize"
            :total="total"
            @pagination="getList"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 新建文件夹对话框 -->
    <el-dialog v-model="dialogVisibleComputed" :title="dialogTitle" width="30%">
      <el-form ref="folderForm" :model="folderForm" :rules="folderRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="folderForm.name" placeholder="请输入文件夹名称"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisibleComputed = false">取 消</el-button>
          <el-button type="primary" @click="submitFolder">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 移动收藏对话框 -->
    <el-dialog v-model="moveDialogVisible" title="移动到" width="30%">
      <el-tree
        :data="folderTree"
        :props="defaultProps"
        default-expand-all
        @node-click="handleMoveToFolder"
      ></el-tree>
    </el-dialog>
  </div>
</template>

<script>
import { modal } from '@/utils/message'
import Pagination from '@/components/Pagination'
import {
  listCollect,
  delCollect,
  moveCollect,
  addCollectFolder,
  updateCollectFolder,
  delCollectFolder,
} from '@/api/im/collect'

export default {
  name: 'Collect',
  components: {
    Pagination,
  },
  data() {
    return {
      // 加载状态
      loading: false,
      // 选中的项
      selectedItems: [],
      // 是否禁用批量操作
      multiple: true,
      // 搜索文本
      searchText: '',
      // 总条数
      total: 0,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        folderId: undefined,
      },
      // 收藏列表数据
      collectList: [],
      // 文件夹树数据
      folderTree: [
        {
          id: 'root',
          label: '全部收藏',
          type: 'folder',
          children: [
            {
              id: '1',
              label: '工作',
              type: 'folder',
            },
            {
              id: '2',
              label: '学习',
              type: 'folder',
            },
            {
              id: '3',
              label: '生活',
              type: 'folder',
            },
          ],
        },
      ],
      // 树形配置
      defaultProps: {
        children: 'children',
        label: 'label',
      },
      // 对话框显示状态
      dialogVisibleComputed: false,
      moveDialogVisible: false,
      // 对话框标题
      dialogTitle: '新建文件夹',
      // 文件夹表单
      folderForm: {
        name: '',
        parentId: 'root',
      },
      // 文件夹表单校验规则
      folderRules: {
        name: [
          { required: true, message: '请输入文件夹名称', trigger: 'blur' },
          { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' },
        ],
      },
      // 当前操作的收藏项
      currentItem: null,
    }
  },
  created() {
    this.getList()
    this.getFolderTree()
  },
  methods: {
    // 获取收藏列表
    getList() {
      this.loading = true
      listCollect(this.queryParams).then(response => {
        this.collectList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 处理文件夹点击
    handleNodeClick(data) {
      this.queryParams.folderId = data.id
      this.getList()
    },
    // 获取文件夹树
    getFolderTree() {
      getCollectFolderTree().then(response => {
        this.folderTree = response.data
      })
    },
    // 处理文件夹操作
    handleFolderCommand(command, data) {
      if (command === 'rename') {
        this.dialogTitle = '重命名文件夹'
        this.folderForm = { ...data, name: data.label }
        this.dialogVisible = true
      } else if (command === 'delete') {
        modal
          .confirm('确认删除该文件夹吗？')
          .then(() => {
            return delCollectFolder(data.id)
          })
          .then(() => {
            modal.success('删除成功')
            this.getFolderTree()
          })
      }
    },
    // 新建文件夹
    handleAddFolder() {
      this.dialogTitle = '新建文件夹'
      this.folderForm = {
        name: '',
        parentId: 'root',
      }
      this.dialogVisible = true
    },
    // 提交文件夹表单
    submitFolder() {
      this.$refs.folderForm.validate(valid => {
        if (valid) {
          const isUpdate = this.folderForm.id !== undefined
          const request = isUpdate
            ? updateCollectFolder(this.folderForm)
            : addCollectFolder(this.folderForm)

          request.then(() => {
            modal.success(this.dialogTitle + '成功')
            this.dialogVisible = false
            this.getFolderTree()
          })
        }
      })
    },
    // 处理搜索
    handleSearch() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    // 处理选择变化
    handleSelectionChange(selection) {
      this.selectedItems = selection
      this.multiple = !selection.length
    },
    // 批量删除
    handleBatchDelete() {
      const ids = this.selectedItems.map(item => item.id)
      modal
        .confirm('确认删除选中的收藏吗？')
        .then(() => {
          return delCollectBatch(ids)
        })
        .then(() => {
          modal.success('删除成功')
          this.getList()
        })
    },
    // 移动收藏
    handleMove(row) {
      this.currentItem = row
      this.moveDialogVisible = true
    },
    // 确认移动到文件夹
    handleMoveToFolder(data) {
      if (data.type === 'folder') {
        moveCollect({
          collectId: this.currentItem.id,
          folderId: data.id,
        }).then(() => {
          modal.success('移动成功')
          this.moveDialogVisible = false
          this.getList()
        })
      }
    },
    // 删除收藏
    handleDelete(row) {
      modal
        .confirm('确认删除该收藏吗？')
        .then(() => {
          return delCollect(row.id)
        })
        .then(() => {
          modal.success('删除成功')
          this.getList()
        })
    },
    // 预览收藏内容
    handlePreview(row) {
      if (row.type === 'link') {
        window.open(row.content, '_blank')
      }
    },
    // 获取类型标签样式
    getTypeTag(type) {
      const tags = {
        text: '',
        image: 'success',
        file: 'warning',
        link: 'info',
      }
      return tags[type] || ''
    },
    // 获取类型显示文本
    getTypeLabel(type) {
      const labels = {
        text: '文本',
        image: '图片',
        file: '文件',
        link: '链接',
      }
      return labels[type] || type
    },
  },
}
</script>

<style lang="scss" scoped>
.app-container {
  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
  }

  .collect-content {
    cursor: pointer;
    &:hover {
      color: #409eff;
    }
  }

  .el-dropdown-link {
    cursor: pointer;
    color: #409eff;
  }
}
</style>
