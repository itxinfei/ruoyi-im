<template>
  <div class="chat-header">
    <!-- 左侧：会话信息 -->
    <div class="chat-info">
      <!-- 群组成员头像墙（群聊时显示） -->
      <div 
        v-if="session.type === 'GROUP'" 
        class="group-avatar-wall"
        @click="handleProfileClick"
      >
        <DtAvatar
          v-for="(member, index) in groupMembers.slice(0, 4)"
          :key="member.id"
          :name="member.name"
          :avatar="member.avatar"
          :size="28"
          :style="{ zIndex: 4 - index, marginLeft: index > 0 ? '-8px' : '0' }"
          class="member-avatar"
        />
        <div v-if="session.memberCount > 4" class="more-members">
          +{{ session.memberCount - 4 }}
        </div>
      </div>
      
      <!-- 私聊头像 -->
      <div 
        v-else 
        class="private-chat-avatar"
        @click="handleProfileClick"
      >
        <DtAvatar
          :name="session.name"
          :avatar="session.avatar"
          :size="36"
        />
        <!-- 在线状态指示器 -->
        <div 
          class="online-indicator"
          :class="getOnlineStatusClass()"
        ></div>
      </div>
      
      <div class="chat-title-info">
        <div class="title-row">
          <span class="title-name">{{ session.name }}</span>
          <!-- 置顶标识 -->
          <el-icon v-if="session.isPinned" class="pinned-icon" color="#fadb14">
            <Star />
          </el-icon>
          <!-- 免打扰标识 -->
          <el-icon v-if="session.isMuted" class="muted-icon" color="#8c8c8c">
            <Bell />
          </el-icon>
        </div>
        <div class="title-subtitle">
          <!-- 在线状态 -->
          <span class="status-text">{{ getOnlineStatusText() }}</span>
          <span v-if="session.type === 'GROUP'" class="member-count">
            · {{ session.memberCount || 0 }}人
          </span>
          <!-- 群组在线人数 -->
          <span v-if="session.type === 'GROUP'" class="online-count">
            · {{ onlineMemberCount }}人在线
          </span>
        </div>
      </div>
    </div>

    <!-- 右侧：快捷操作按钮 -->
    <div class="chat-actions">
      <!-- 搜索聊天记录 -->
      <el-tooltip content="搜索聊天记录" placement="bottom">
        <el-button :icon="Search" class="action-btn search-btn" @click="handleSearch" />
      </el-tooltip>
      
      <!-- 语音通话 -->
      <el-tooltip content="语音通话" placement="bottom">
        <el-button :icon="Phone" class="action-btn voice-btn" @click="handleVoiceCall" />
      </el-tooltip>
      
      <!-- 视频通话 -->
      <el-tooltip content="视频通话" placement="bottom">
        <el-button :icon="VideoCamera" class="action-btn video-btn" @click="handleVideoCall" />
      </el-tooltip>
      
      <!-- 更多操作 -->
      <el-dropdown trigger="click" placement="bottom-end">
        <el-button :icon="More" class="action-btn more-btn" />
        <template #dropdown>
          <el-dropdown-menu>
            <!-- 私聊菜单 -->
            <template v-if="session.type !== 'GROUP'">
              <el-dropdown-item @click="handleViewProfile">
                <el-icon><User /></el-icon>
                查看资料
              </el-dropdown-item>
              <el-dropdown-item @click="handleSetRemark">
                <el-icon><Edit /></el-icon>
                设置备注
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleMute">
                <el-icon>
                  <Bell v-if="!session.isMuted" />
                  <Mute v-else />
                </el-icon>
                {{ session.isMuted ? '取消免打扰' : '消息免打扰' }}
              </el-dropdown-item>
              <el-dropdown-item @click="handlePin">
                <el-icon><Star /></el-icon>
                {{ session.isPinned ? '取消置顶' : '置顶聊天' }}
              </el-dropdown-item>
            </template>
            
            <!-- 群聊菜单 -->
            <template v-else>
              <el-dropdown-item @click="handleViewMembers">
                <el-icon><User /></el-icon>
                查看成员
              </el-dropdown-item>
              <el-dropdown-item @click="handleShowAnnouncement">
                <el-icon><Notification /></el-icon>
                群公告
              </el-dropdown-item>
              <el-dropdown-item @click="handleShowFiles">
                <el-icon><Folder /></el-icon>
                群文件
              </el-dropdown-item>
              <el-dropdown-item @click="handleGroupSettings">
                <el-icon><Setting /></el-icon>
                群设置
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleMute">
                <el-icon>
                  <Bell v-if="!session.isMuted" />
                  <Mute v-else />
                </el-icon>
                {{ session.isMuted ? '取消免打扰' : '消息免打扰' }}
              </el-dropdown-item>
              <el-dropdown-item @click="handlePin">
                <el-icon><Star /></el-icon>
                {{ session.isPinned ? '取消置顶' : '置顶聊天' }}
              </el-dropdown-item>
            </template>
            
            <!-- 通用菜单 -->
            <el-dropdown-item divided @click="handleClearHistory" class="danger-item">
              <el-icon><Delete /></el-icon>
              清空聊天记录
            </el-dropdown-item>
            <el-dropdown-item 
              v-if="session.type === 'GROUP'" 
              class="danger-item"
              @click="handleExitGroup"
            >
              <el-icon><DeleteFilled /></el-icon>
              退出群聊
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, Phone, VideoCamera, More, Star, Bell, 
  Edit, User, Folder, Notification, Setting, 
  Delete, DeleteFilled, Mute
} from '@element-plus/icons-vue'
import SmartAvatar from '@/components/SmartAvatar/index.vue'

const props = defineProps({
  session: {
    type: Object,
    required: true
  }
})

const emit = defineEmits([
  'search-in-chat', 
  'voice-call', 
  'video-call', 
  'show-profile',
  'mute',
  'pin',
  'clear-history'
])

// 模拟群组成员
const groupMembers = ref([
  { id: 1, name: '张三', avatar: '' },
  { id: 2, name: '李四', avatar: '' },
  { id: 3, name: '王五', avatar: '' },
  { id: 4, name: '赵六', avatar: '' },
])

// 在线成员数量
const onlineMemberCount = ref(3)

// 获取在线状态样式类
const getOnlineStatusClass = () => {
  if (props.session.type === 'GROUP') return ''
  return 'online' // 默认在线，实际应从API获取
}

// 获取在线状态文字
const getOnlineStatusText = () => {
  if (props.session.type === 'GROUP') return ''
  return '在线' // 默认在线，实际应从API获取
}

// 头像点击
const handleProfileClick = () => {
  emit('show-profile')
}

// 搜索聊天
const handleSearch = () => {
  emit('search-in-chat')
}

// 语音通话
const handleVoiceCall = () => {
  emit('voice-call')
}

// 视频通话
const handleVideoCall = () => {
  emit('video-call')
}

// 查看资料
const handleViewProfile = () => {
  ElMessage.info('查看资料功能开发中...')
}

// 设置备注
const handleSetRemark = () => {
  ElMessage.info('设置备注功能开发中...')
}

// 免打扰
const handleMute = () => {
  emit('mute')
}

// 置顶
const handlePin = () => {
  emit('pin')
}

// 查看成员
const handleViewMembers = () => {
  ElMessage.info('查看成员功能开发中...')
}

// 群公告
const handleShowAnnouncement = () => {
  ElMessage.info('群公告功能开发中...')
}

// 群文件
const handleShowFiles = () => {
  ElMessage.info('群文件功能开发中...')
}

// 群设置
const handleGroupSettings = () => {
  ElMessage.info('群设置功能开发中...')
}

// 清空记录
const handleClearHistory = () => {
  ElMessage.info('清空记录功能开发中...')
}

// 退出群聊
const handleExitGroup = () => {
  ElMessage.info('退出群聊功能开发中...')
}
</script>

<style scoped lang="scss">
.chat-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  background: #ffffff;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  // 左侧：会话信息
  .chat-info {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;

    // 群组成员头像墙
    .group-avatar-wall {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 4px;
      border-radius: 12px;
      transition: all 0.2s ease;

      &:hover {
        background: rgba(0, 0, 0, 0.04);
      }

      .member-avatar {
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
        color: #8c8c8c;
        font-weight: 500;
      }
    }

    // 私聊头像
    .private-chat-avatar {
      position: relative;
      cursor: pointer;
      padding: 4px;
      border-radius: 12px;
      transition: all 0.2s ease;

      &:hover {
        background: rgba(0, 0, 0, 0.04);
      }

      .online-indicator {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 12px;
        height: 12px;
        border: 2px solid #fff;
        border-radius: 50%;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);

        &.online {
          background: #52c41a;
          animation: online-pulse 2s ease-in-out infinite;
        }

        &.offline {
          background: #d9d9d9;
        }
      }
    }

    .chat-title-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
      min-width: 0;

      .title-row {
        display: flex;
        align-items: center;
        gap: 6px;

        .title-name {
          font-size: 15px;
          font-weight: 600;
          color: #262626;
          line-height: 1.2;
        }

        .pinned-icon {
          font-size: 12px;
          animation: rotate 20s linear infinite;
        }

        .muted-icon {
          font-size: 12px;
          opacity: 0.7;
        }
      }

      .title-subtitle {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #8c8c8c;
        line-height: 1.2;

        .member-count,
        .online-count {
          font-weight: 500;
          color: #595959;
        }

        .online-count {
          color: #52c41a;
        }
      }
    }
  }

  // 右侧：操作按钮
  .chat-actions {
    display: flex;
    align-items: center;
    gap: 2px;

    .action-btn {
      --el-button-border-color: transparent;
      --el-button-bg-color: transparent;
      --el-button-text-color: #8c8c8c;
      width: 36px;
      height: 36px;
      padding: 0;
      border-radius: 8px;
      font-size: 16px;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        --el-button-text-color: #1677ff;
        --el-button-hover-bg-color: rgba(22, 119, 255, 0.06);
        transform: scale(1.05);
      }

      &:active {
        transform: scale(0.95);
      }

      &.search-btn:hover {
        --el-button-text-color: #52c41a;
        --el-button-hover-bg-color: rgba(82, 196, 26, 0.06);
      }

      &.voice-btn:hover {
        --el-button-text-color: #722ed1;
        --el-button-hover-bg-color: rgba(114, 46, 209, 0.06);
      }

      &.video-btn:hover {
        --el-button-text-color: #eb2f96;
        --el-button-hover-bg-color: rgba(235, 47, 150, 0.06);
      }

      &.more-btn:hover {
        --el-button-text-color: #595959;
        --el-button-hover-bg-color: rgba(0, 0, 0, 0.06);
      }
    }

    :deep(.el-dropdown-menu__item) {
      padding: 10px 16px;
      font-size: 13px;
      transition: all 0.2s ease;

      &:hover {
        background: rgba(22, 119, 255, 0.06);
        color: #1677ff;
      }

      .el-icon {
        margin-right: 8px;
        font-size: 14px;
      }

      &.danger-item {
        color: #ff4d4f;

        &:hover {
          background: rgba(255, 77, 79, 0.06);
          color: #ff4d4f;
        }

        .el-icon {
          color: #ff4d4f;
        }
      }
    }
  }

  // 动画定义
  @keyframes rotate {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }

  @keyframes online-pulse {
    0%, 100% { 
      opacity: 1;
      box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.7);
    }
    50% { 
      opacity: 0.8;
      box-shadow: 0 0 0 6px rgba(82, 196, 26, 0);
    }
  }
}
</style>
