<template>
  <div class="dt-bubble" :class="sent ? 'dt-bubble--sent' : 'dt-bubble--received'">
    <slot />
  </div>
</template>

<script setup>
/**
 * 全局统一消息气泡容器
 * 全站所有聊天气泡（IM 消息 / AI 助理回复等）统一使用此组件，
 * 样式唯一来源：@/styles/components/bubble.scss（全局引入，无需 scoped）
 *
 * 基准：prototype/dingtalk-h5.html + prototype/dingtalk-mockup.html
 *   - 内边距 9px 12px，圆角 12px，近头像顶角 4px
 *   - 接收方：面板底色 + 主文本色，无边框
 *   - 发送方：品牌蓝底 + 白字
 */
defineProps({
  /** 是否为自己发送的消息（右侧蓝色气泡） */
  sent: { type: Boolean, default: false }
})
</script>
