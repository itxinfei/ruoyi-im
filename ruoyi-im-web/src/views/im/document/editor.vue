<template>
  <div class="app-container">
    <div class="document-editor">
      <!-- 工具栏 -->
      <div class="editor-toolbar">
        <el-button-group>
          <el-button size="mini" icon="el-icon-document" @click="handleSave">保存</el-button>
          <el-button size="mini" icon="el-icon-refresh-right" @click="handleReload">刷新</el-button>
          <el-button size="mini" icon="el-icon-time" @click="handleVersion">版本</el-button>
        </el-button-group>
        <el-button-group style="margin-left: 10px">
          <el-button size="mini" icon="el-icon-share" @click="handleShare">分享</el-button>
          <el-button size="mini" icon="el-icon-download" @click="handleDownload">下载</el-button>
        </el-button-group>
      </div>

      <!-- 编辑器容器 -->
      <div ref="editor" class="editor-container"></div>

      <!-- 版本历史对话框 -->
      <el-dialog v-model:visible="version.open" title="版本历史" width="600px" append-to-body>
        <el-table :data="version.list" height="400">
          <el-table-column label="版本号" prop="versionNum" width="100" />
          <el-table-column label="创建时间" prop="createTime" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建者" prop="creatorName" width="120" />
          <el-table-column label="说明" prop="remark" />
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button size="mini" type="text" @click="handleRestore(scope.row)">恢复</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <!-- 分享对话框 -->
      <el-dialog v-model:visible="share.open" title="分享文档" width="400px" append-to-body>
        <el-form :model="share.form" label-width="80px">
          <el-form-item label="过期时间">
            <el-date-picker
              v-model="share.form.expireTime"
              type="datetime"
              placeholder="选择过期时间"
            >
            </el-date-picker>
          </el-form-item>
          <el-form-item v-if="share.url" label="分享链接">
            <el-input v-model="share.url" readonly>
              <template #append>
                <el-button icon="el-icon-copy-document" @click="copyShareUrl">复制</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submitShare">生成链接</el-button>
            <el-button @click="share.open = false">取 消</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getEditConfig, restoreVersion, shareDocument } from '@/api/im/document'

export default {
  name: 'DocumentEditor',
  data() {
    return {
      // 文档ID
      documentId: undefined,
      // 编辑器实例
      editor: null,
      // 编辑器配置
      config: {},
      // 版本对话框
      version: {
        open: false,
        list: [],
      },
      // 分享对话框
      share: {
        open: false,
        url: '',
        form: {
          expireTime: undefined,
        },
      },
    }
  },
  created() {
    this.documentId = this.$route.params.documentId
    this.initEditor()
  },
  methods: {
    /** 初始化编辑器 */
    initEditor() {
      getEditConfig(this.documentId).then(response => {
        this.config = response.data
        this.createEditor()
      })
    },
    /** 创建编辑器实例 */
    createEditor() {
      // 创建OnlyOffice编辑器实例
      const docEditor = new DocsAPI.DocEditor(this.$refs.editor, {
        document: {
          fileType: this.config.documentType,
          key: this.documentId,
          title: this.config.title,
          url: this.config.url,
          permissions: {
            download: true,
            edit: this.config.mode === 'edit',
            review: true,
          },
        },
        documentType: this.config.documentType,
        editorConfig: {
          mode: this.config.mode,
          user: this.config.user,
          callbackUrl: this.config.callbackUrl,
          customization: {
            autosave: true,
            forcesave: true,
          },
        },
        events: {
          onDocumentStateChange: this.onDocumentStateChange,
          onError: this.onEditorError,
        },
      })
      this.editor = docEditor
    },
    /** 文档状态变化处理 */
    onDocumentStateChange() {
      // TODO: 处理文档状态变化
    },
    /** 编辑器错误处理 */
    onEditorError(event) {
      this.msgError('编辑器错误：' + event.message)
    },
    /** 保存按钮操作 */
    handleSave() {
      if (this.editor) {
        this.editor.downloadAs()
      }
    },
    /** 刷新按钮操作 */
    handleReload() {
      this.initEditor()
    },
    /** 版本按钮操作 */
    handleVersion() {
      // TODO: 获取版本历史
      this.version.open = true
    },
    /** 恢复版本操作 */
    handleRestore(row) {
      this.$confirm('是否确认恢复该版本?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          return restoreVersion(this.documentId, row.versionId)
        })
        .then(() => {
          this.msgSuccess('恢复成功')
          this.version.open = false
          this.initEditor()
        })
    },
    /** 分享按钮操作 */
    handleShare() {
      this.share.open = true
      this.share.url = ''
    },
    /** 提交分享 */
    submitShare() {
      shareDocument(this.documentId, this.share.form.expireTime).then(response => {
        this.share.url = response.data
        this.msgSuccess('生成分享链接成功')
      })
    },
    /** 复制分享链接 */
    copyShareUrl() {
      this.$copyText(this.share.url).then(() => {
        this.msgSuccess('复制成功')
      })
    },
    /** 下载按钮操作 */
    handleDownload() {
      if (this.editor) {
        this.editor.downloadAs()
      }
    },
  },
}
</script>

<style scoped>
.document-editor {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}
.editor-toolbar {
  padding: 10px;
  border-bottom: 1px solid #dcdfe6;
}
.editor-container {
  flex: 1;
  overflow: hidden;
}
</style>
