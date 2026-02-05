/**
 * ChatPanel - 消息多选 Composable
 *
 * 职责：
 * - 多选模式状态管理
 * - 消息选择/取消选择
 * - 全选/取消全选
 * - 批量操作（删除、转发、收藏等）
 */

import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export function useChatMultiSelect(messages) {
  // 多选模式是否激活
  const isMultiSelectMode = ref(false)

  // 已选择的消息ID集合
  const selectedMessageIds = ref(new Set())

  /**
   * 进入多选模式
   */
  const enterMultiSelectMode = () => {
    isMultiSelectMode.value = true
    selectedMessageIds.value.clear()
  }

  /**
   * 退出多选模式
   */
  const exitMultiSelectMode = () => {
    isMultiSelectMode.value = false
    selectedMessageIds.value.clear()
  }

  /**
   * 切换消息选中状态
   */
  const toggleMessageSelection = messageId => {
    if (selectedMessageIds.value.has(messageId)) {
      selectedMessageIds.value.delete(messageId)
    } else {
      selectedMessageIds.value.add(messageId)
    }
  }

  /**
   * 全选当前消息
   */
  const selectAll = () => {
    messages.value.forEach(msg => {
      // 只能选择可操作的消息（非系统消息、非撤回消息）
      if (msg.type !== 'SYSTEM' && msg.type !== 'RECALLED') {
        selectedMessageIds.value.add(msg.id)
      }
    })
  }

  /**
   * 取消全选
   */
  const deselectAll = () => {
    selectedMessageIds.value.clear()
  }

  /**
   * 判断消息是否被选中
   */
  const isMessageSelected = messageId => {
    return selectedMessageIds.value.has(messageId)
  }

  /**
   * 获取已选中的消息列表
   */
  const selectedMessages = computed(() => {
    return messages.value.filter(msg => selectedMessageIds.value.has(msg.id))
  })

  /**
   * 已选消息数量
   */
  const selectedCount = computed(() => {
    return selectedMessageIds.value.size
  })

  /**
   * 批量删除
   */
  const batchDelete = async deleteFn => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请先选择要删除的消息')
      return
    }

    try {
      for (const messageId of selectedMessageIds.value) {
        await deleteFn(messageId)
      }
      ElMessage.success(`已删除 ${selectedCount.value} 条消息`)
      exitMultiSelectMode()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('删除失败')
    }
  }

  /**
   * 批量转发
   */
  const batchForward = async (forwardFn, targetConversationIds) => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请先选择要转发的消息')
      return false
    }

    if (!targetConversationIds || targetConversationIds.length === 0) {
      ElMessage.warning('请选择转发目标')
      return false
    }

    try {
      await forwardFn(
        Array.from(selectedMessageIds.value),
        targetConversationIds
      )
      ElMessage.success(`已转发 ${selectedCount.value} 条消息`)
      exitMultiSelectMode()
      return true
    } catch (error) {
      console.error('批量转发失败:', error)
      ElMessage.error('转发失败')
      return false
    }
  }

  /**
   * 批量收藏
   */
  const batchFavorite = async favoriteFn => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请先选择要收藏的消息')
      return
    }

    try {
      for (const messageId of selectedMessageIds.value) {
        await favoriteFn(messageId)
      }
      ElMessage.success(`已收藏 ${selectedCount.value} 条消息`)
      exitMultiSelectMode()
    } catch (error) {
      console.error('批量收藏失败:', error)
      ElMessage.error('收藏失败')
    }
  }

  return {
    // 状态
    isMultiSelectMode,
    selectedMessageIds,
    selectedMessages,
    selectedCount,

    // 方法
    enterMultiSelectMode,
    exitMultiSelectMode,
    toggleMessageSelection,
    selectAll,
    deselectAll,
    isMessageSelected,
    batchDelete,
    batchForward,
    batchFavorite
  }
}
