/**
 * 联系人批量操作 composable
 * 处理联系人列表的批量选择、移动、删除等操作
 */
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export function useContactBatch() {
  // 批量模式状态
  const batchMode = ref(false)
  const selectedContacts = ref(new Set())

  /**
   * 进入批量模式
   */
  const enterBatchMode = () => {
    batchMode.value = true
    selectedContacts.value.clear()
  }

  /**
   * 退出批量模式
   */
  const exitBatchMode = () => {
    batchMode.value = false
    selectedContacts.value.clear()
  }

  /**
   * 切换联系人选择状态
   * @param {number|string} contactId - 联系人ID
   * @param {boolean} selected - 是否选中
   */
  const toggleContactSelection = (contactId, selected) => {
    if (selected) {
      selectedContacts.value.add(contactId)
    } else {
      selectedContacts.value.delete(contactId)
    }
  }

  /**
   * 全选/取消全选
   * @param {Array} list - 当前列表数据
   * @param {boolean} selected - 是否全选
   */
  const handleSelectAll = (list, selected) => {
    if (selected) {
      list.forEach(item => selectedContacts.value.add(item.id))
    } else {
      selectedContacts.value.clear()
    }
  }

  /**
   * 批量移动到分组
   * @param {Function} callback - 执行移动的回调函数
   */
  const handleBatchMoveGroup = async callback => {
    const contactIds = Array.from(selectedContacts.value)
    if (contactIds.length === 0) {
      ElMessage.warning('请先选择联系人')
      return
    }

    try {
      await callback(contactIds)
      exitBatchMode()
      return true
    } catch (error) {
      ElMessage.error('移动失败')
      return false
    }
  }

  /**
   * 批量发送消息
   * @param {Function} callback - 执行发送的回调函数
   */
  const handleBatchSendMessage = async callback => {
    const contactIds = Array.from(selectedContacts.value)
    if (contactIds.length === 0) {
      ElMessage.warning('请先选择联系人')
      return
    }

    try {
      await callback(contactIds)
      exitBatchMode()
      return true
    } catch (error) {
      ElMessage.error('发送失败')
      return false
    }
  }

  /**
   * 批量删除联系人
   * @param {Function} callback - 执行删除的回调函数
   */
  const handleBatchDelete = async callback => {
    const contactIds = Array.from(selectedContacts.value)
    if (contactIds.length === 0) {
      ElMessage.warning('请先选择联系人')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${contactIds.length} 个联系人吗？`,
        '批量删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      await callback(contactIds)
      ElMessage.success('删除成功')
      exitBatchMode()
      return true
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
      return false
    }
  }

  /**
   * 批量添加好友
   * @param {Function} callback - 执行添加的回调函数
   */
  const handleBatchAddFriends = async callback => {
    const contactIds = Array.from(selectedContacts.value)
    if (contactIds.length === 0) {
      ElMessage.warning('请先选择联系人')
      return
    }

    try {
      const { value } = await ElMessageBox.prompt('请输入验证消息', '批量添加好友', {
        inputValue: '你好，我是...',
        confirmButtonText: '发送',
        cancelButtonText: '取消'
      })

      await callback(contactIds, value)
      ElMessage.success(`已向 ${contactIds.length} 个用户发送好友请求`)
      exitBatchMode()
      return true
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败')
      }
      return false
    }
  }

  // 计算属性
  const selectedCount = computed(() => selectedContacts.value.size)
  const hasSelection = computed(() => selectedCount.value > 0)
  const isAllSelected = computed(() => {
    return list => list.length > 0 && selectedContacts.value.size === list.length
  })

  return {
    // 状态
    batchMode,
    selectedContacts,
    selectedCount,
    hasSelection,
    isAllSelected,

    // 方法
    enterBatchMode,
    exitBatchMode,
    toggleContactSelection,
    handleSelectAll,
    handleBatchMoveGroup,
    handleBatchSendMessage,
    handleBatchDelete,
    handleBatchAddFriends
  }
}

export default useContactBatch
