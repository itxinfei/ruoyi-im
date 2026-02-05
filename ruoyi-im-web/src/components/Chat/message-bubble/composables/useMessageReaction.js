/**
 * 消息表情回应管理
 * 处理表情添加、移除、聚合显示等
 */
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export function useMessageReaction(props, emit) {
  const store = useStore()
  const isReacting = ref(false)
  const showReactionDetail = ref(false)

  // ==================== 表情数据 ====================

  // 消息的表情回应（按表情分组）
  const messageReactions = computed(() => {
    if (!props.message?.reactions) {return []}

    const currentUser = store.getters['user/currentUser']
    const reactions = {}

    props.message.reactions.forEach(r => {
      if (!reactions[r.emoji]) {
        reactions[r.emoji] = {
          emoji: r.emoji,
          users: [],
          count: 0,
          hasOwnReaction: false
        }
      }
      reactions[r.emoji].users.push(r)
      reactions[r.emoji].count++
      reactions[r.emoji].hasOwnReaction = r.userId === currentUser?.id
    })

    return Object.values(reactions)
  })

  // 是否有表情回应
  const hasReactions = computed(() => messageReactions.value.length > 0)

  // 检查是否对某个表情回复过
  const hasReacted = emoji => {
    const currentUser = store.getters['user/currentUser']
    return props.message?.reactions?.some(
      r => r.emoji === emoji && r.userId === currentUser?.id
    )
  }

  // ==================== 表情操作 ====================

  // 切换表情回应
  const toggleReaction = async emoji => {
    if (isReacting.value) {return}

    const alreadyReacted = hasReacted(emoji)

    try {
      isReacting.value = true
      const { addReaction, removeReaction } = await import('@/api/im/message')

      if (alreadyReacted) {
        await removeReaction(props.message.id)
        // 更新本地状态
        if (props.message.reactions) {
          props.message.reactions = props.message.reactions.filter(
            r => !(r.emoji === emoji && r.userId === store.getters['user/currentUser']?.id)
          )
        }
      } else {
        await addReaction(props.message.id, { emoji })
        if (!props.message.reactions) {
          props.message.reactions = []
        }
        props.message.reactions.push({
          emoji,
          userId: store.getters['user/currentUser']?.id,
          userName: store.getters['user/currentUser']?.nickName || '我',
          userAvatar: store.getters['user/currentUser']?.avatar
        })
      }

      emit('toggle-reaction', props.message.id, emoji, !alreadyReacted)
    } catch (error) {
      console.error('表情回应失败:', error)
      ElMessage.error('操作失败，请重试')
    } finally {
      isReacting.value = false
    }
  }

  // 添加表情（从AI表情面板）
  const addReaction = async emoji => {
    if (hasReacted(emoji)) {
      ElMessage.warning('已经表达过这个表情了')
      return
    }
    await toggleReaction(emoji)
  }

  return {
    // State
    isReacting,
    showReactionDetail,

    // Computed
    messageReactions,
    hasReactions,

    // Methods
    hasReacted,
    toggleReaction,
    addReaction
  }
}
