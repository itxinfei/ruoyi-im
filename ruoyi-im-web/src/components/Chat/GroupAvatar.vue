<template>
  <div class="group-avatar" @click="handleClick">
    <div
      v-for="(member, index) in members.slice(0, maxMembers)"
      :key="member.id"
      class="avatar-item"
      :style="{ zIndex: maxMembers - index, marginLeft: index > 0 ? '-8px' : '0' }"
    >
      <SmartAvatar
        :name="member.name"
        :avatar="member.avatar"
        :size="size"
      />
    </div>
    <div v-if="totalCount > maxMembers" class="more-members">
      +{{ totalCount - maxMembers }}
    </div>
  </div>
</template>

<script setup>
import SmartAvatar from '@/components/SmartAvatar/index.vue'

const props = defineProps({
  // 群组成员列表
  members: {
    type: Array,
    default: () => [],
    required: true
  },
  // 头像尺寸
  size: {
    type: Number,
    default: 28,
    validator: (value) => [24, 32, 36, 40, 48].includes(value)
  },
  // 最大显示数量
  maxMembers: {
    type: Number,
    default: 4
  }
})

const emit = defineEmits(['click'])

// 成员总数
const totalCount = computed(() => props.members.length)

// 处理点击
const handleClick = () => {
  emit('click')
}
</script>

<style scoped lang="scss">
.group-avatar {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px;
  border-radius: 12px;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }

  .avatar-item {
    border: 2px solid #fff;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    transition: all 0.2s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
    }
  }

  .more-members {
    width: 28px;
    height: 28px;
    background: #f0f0f0;
    border: 2px solid #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    font-weight: 500;
    color: #8c8c8c;
  }
}
</style>
