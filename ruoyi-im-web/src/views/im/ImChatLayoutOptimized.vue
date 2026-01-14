<template>
  <div class="web-im-layout">
    <!-- 顶部导航栏（钉钉风格优化版） -->
    <header class="ding-header">
      <!-- 左侧：Logo和搜索 -->
      <div class="header-left">
        <div class="header-logo">
          <el-icon :size="24" color="#1677ff"><ChatDotRound /></el-icon>
          <span class="logo-text">钉钉</span>
        </div>
        <div class="header-divider"></div>
        <div class="header-search">
          <el-input
            v-model="globalSearchKeyword"
            placeholder="搜索联系人、群聊、消息..."
            :prefix-icon="Search"
            class="global-search-input"
            clearable
            @keyup.enter="handleGlobalSearch"
          >
            <template #suffix>
              <span class="search-shortcut">⌘K</span>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 右侧：操作区 -->
      <div class="header-right">
        <!-- 通知按钮 -->
        <el-badge :value="notificationCount" :hidden="notificationCount === 0" :max="99" class="header-badge">
          <el-tooltip content="通知" placement="bottom">
            <el-button :icon="Bell" text class="header-action-btn" @click="showNotifications" />
          </el-tooltip>
        </el-badge>

        <!-- 快捷操作 -->
        <el-tooltip content="发起聊天" placement="bottom">
          <el-button :icon="Plus" text class="header-action-btn" @click="showStartChatDialog" />
        </el-tooltip>

        <!-- 设置 -->
        <el-dropdown trigger="click" placement="bottom-end" @command="handleSettingsCommand">
          <div class="header-action-wrapper">
            <el-tooltip content="设置" placement="bottom">
              <el-button :icon="Setting" text class="header-action-btn" />
            </el-tooltip>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="settings-dropdown">
              <el-dropdown-item command="theme">
                <el-icon class="menu-icon theme"><Sunny /></el-icon>
                <span class="menu-text">主题切换</span>
                <span class="item-shortcut">⌘T</span>
              </el-dropdown-item>
              <el-dropdown-item command="notifications">
                <el-icon class="menu-icon"><Bell /></el-icon>
                <span class="menu-text">消息通知</span>
              </el-dropdown-item>
              <el-dropdown-item command="privacy">
                <el-icon class="menu-icon"><Lock /></el-icon>
                <span class="menu-text">隐私与安全</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="shortcuts">
                <el-icon class="menu-icon"><Key /></el-icon>
                <span class="menu-text">快捷键</span>
              </el-dropdown-item>
              <el-dropdown-item command="about">
                <el-icon class="menu-icon"><InfoFilled /></el-icon>
                <span class="menu-text">关于系统</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 帮助 -->
        <el-dropdown trigger="click" placement="bottom-end" @command="handleHelpCommand">
          <div class="header-action-wrapper">
            <el-tooltip content="帮助" placement="bottom">
              <el-button :icon="QuestionFilled" text class="header-action-btn" />
            </el-tooltip>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="help-dropdown">
              <el-dropdown-item command="help">
                <el-icon class="menu-icon"><QuestionFilled /></el-icon>
                <span class="menu-text">使用帮助</span>
              </el-dropdown-item>
              <el-dropdown-item command="shortcuts">
                <el-icon class="menu-icon"><Key /></el-icon>
                <span class="menu-text">快捷键指南</span>
              </el-dropdown-item>
              <el-dropdown-item command="feedback">
                <el-icon class="menu-icon"><Message /></el-icon>
                <span class="menu-text">反馈建议</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="update">
                <el-icon class="menu-icon"><Refresh /></el-icon>
                <span class="menu-text">检查更新</span>
                <el-tag size="small" type="success" class="version-tag">v1.0</el-tag>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 用户下拉 -->
        <el-dropdown trigger="click" placement="bottom-end" @command="handleUserCommand">
          <div class="header-user">
            <SmartAvatar
              :name="currentUser?.name"
              :avatar="currentUser?.avatar"
              :size="32"
              :show-border="true"
              :show-online="true"
              :online="currentOnlineStatus === 'online'"
              :online-status="currentOnlineStatus"
              class="user-avatar"
            />
            <span class="header-username">{{ currentUser?.name || '用户' }}</span>
            <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <div class="profile-item">
                  <el-avatar :size="36" :src="currentUser?.avatar" class="dropdown-avatar">
                    {{ currentUser?.name?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="user-info-item">
                    <span class="user-name">{{ currentUser?.name || '用户' }}</span>
                    <span class="user-id">ID: {{ currentUser?.userId }}</span>
                  </div>
                </div>
              </el-dropdown-item>
              <el-dropdown-item divided command="status">
                <div class="status-item-wrapper">
                  <div class="status-item-content">
                    <el-icon><CircleCheck /></el-icon>
                    <span>在线状态</span>
                  </div>
                  <div class="status-selector">
                    <el-tooltip content="在线" placement="top">
                      <span
                        class="status-btn"
                        :class="{ active: currentOnlineStatus === 'online' }"
                        @click.stop="setOnlineStatus('online')"
                      >
                        <span class="status-dot online"></span>
                      </span>
                    </el-tooltip>
                    <el-tooltip content="离开" placement="top">
                      <span
                        class="status-btn"
                        :class="{ active: currentOnlineStatus === 'away' }"
                        @click.stop="setOnlineStatus('away')"
                      >
                        <span class="status-dot away"></span>
                      </span>
                    </el-tooltip>
                    <el-tooltip content="忙碌" placement="top">
                      <span
                        class="status-btn"
                        :class="{ active: currentOnlineStatus === 'busy' }"
                        @click.stop="setOnlineStatus('busy')"
                      >
                        <span class="status-dot busy"></span>
                      </span>
                    </el-tooltip>
                    <el-tooltip content="离线" placement="top">
                      <span
                        class="status-btn"
                        :class="{ active: currentOnlineStatus === 'offline' }"
                        @click.stop="setOnlineStatus('offline')"
                      >
                        <span class="status-dot offline"></span>
                      </span>
                    </el-tooltip>
                  </div>
                  <div class="current-status-text">
                    {{ getOnlineStatusDisplay(currentOnlineStatus) }}
                  </div>
                </div>
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                个人设置
              </el-dropdown-item>
              <el-dropdown-item command="theme">
                <el-icon><Sunny /></el-icon>
                主题设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主体内容区 -->
    <div class="main-body">
      <!-- 左侧导航栏（钉钉68px图标式） -->
      <aside class="nav-sidebar">
        <nav class="nav-list">
          <el-tooltip
            v-for="item in navModules"
            :key="item.key"
            :content="item.label"
            placement="right"
            :show-after="500"
          >
            <div
              class="nav-item"
              :class="{ active: activeModule === item.key }"
              @click="switchModule(item.key)"
            >
              <el-icon class="nav-icon">
                <component :is="item.icon" />
              </el-icon>
              <!-- 未读红点 -->
              <span
                v-if="item.key === 'chat' && unreadCount > 0"
                class="nav-dot"
              />
            </div>
          </el-tooltip>
        </nav>
      </aside>

      <!-- 内容工作区 -->
      <main class="workspace">
        <!-- 消息模块 -->
        <div v-if="activeModule === 'chat'" class="chat-workspace">
          <!-- 会话列表 -->
          <div class="session-panel">
            <div class="session-list">
              <!-- 空状态提示 -->
              <div v-if="filteredSessions.length === 0" class="empty-sessions">
                <el-empty description="暂无会话">
                  <template #image>
                    <el-icon :size="60" color="#cccccc">
                      <ChatDotRound />
                    </el-icon>
                  </template>
                  <template #default>
                    <p class="empty-tip">开始聊天吧</p>
                    <el-button type="primary" @click="showStartChatDialog">发起聊天</el-button>
                  </template>
                </el-empty>
              </div>
              <!-- 会话列表 -->
              <div
                v-for="session in filteredSessions"
                :key="session.id"
                class="session-item"
                :class="{ active: currentSessionId === session.id }"
                @click="selectSession(session)"
              >
                <el-badge :value="session.unreadCount" :hidden="session.unreadCount === 0">
                  <SmartAvatar
                    :name="session.name"
                    :avatar="session.avatar"
                    :size="40"
                    :show-border="true"
                    :show-online="false"
                  />
                </el-badge>
                <div class="session-info">
                  <div class="session-top">
                    <span class="session-name">{{ session.name }}</span>
                    <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
                  </div>
                  <div class="session-preview">
                    {{ formatSessionPreview(session.lastMessage) || '暂无消息' }}
                  </div>
                </div>
              </div>
            </div>
          </div>

           <!-- 聊天内容 -->
           <div class="chat-panel">
             <div v-if="currentSessionId" class="chat-container">
               <!-- 聊天头部（优化版） -->
               <div class="chat-header">
                 <!-- 左侧：会话信息 -->
                 <div class="chat-info">
                   <div class="chat-avatar-group" @click="showChatProfile">
                     <el-badge :value="currentSession?.unreadCount || 0" :hidden="!(currentSession?.unreadCount > 0)" :max="99">
                       <SmartAvatar
                         :name="currentSession?.name"
                         :avatar="currentSession?.avatar"
                         :size="40"
                         :show-border="true"
                         :show-online="false"
                       />
                     </el-badge>
                     <el-icon v-if="currentSession?.type === 'GROUP'" class="group-badge"><User /></el-icon>
                   </div>
                  <div class="chat-title-info">
                    <div class="title-row">
                      <span class="title-name">{{ currentSession?.name }}</span>
                      <el-tag v-if="currentSession?.type === 'GROUP'" size="small" type="info" class="group-tag">
                        群聊
                      </el-tag>
                    </div>
                    <div class="title-subtitle">
                      <span class="status-dot" :class="{ online: isUserOnline }"></span>
                      <span class="status-text">{{ getOnlineStatusText() }}</span>
                      <span v-if="currentSession?.type === 'GROUP'" class="member-count">
                        · {{ currentSession?.memberCount || 0 }}人
                      </span>
                    </div>
                  </div>
                </div>

                <!-- 右侧：操作按钮 -->
                <div class="chat-actions">
                  <el-tooltip content="语音通话" placement="bottom">
                    <el-button :icon="Phone" class="action-btn" circle @click="startVoiceCall" />
                  </el-tooltip>
                  <el-tooltip content="视频通话" placement="bottom">
                    <el-button :icon="VideoCamera" class="action-btn" circle @click="startVideoCall" />
                  </el-tooltip>
                  <el-tooltip content="屏幕共享" placement="bottom">
                    <el-button :icon="View" class="action-btn" circle @click="startScreenShare" />
                  </el-tooltip>
                  <el-divider direction="vertical" />
                  <el-tooltip content="搜索聊天记录" placement="bottom">
                    <el-button :icon="Search" class="action-btn" circle @click="searchInChat" />
                  </el-tooltip>
                  <el-tooltip content="更多操作" placement="bottom">
                    <el-dropdown trigger="click" placement="bottom-end">
                      <el-button :icon="More" class="action-btn" circle />
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item @click="muteChat">
                            <el-icon><Bell /></el-icon>
                            {{ currentSession?.isMuted ? '取消免打扰' : '消息免打扰' }}
                          </el-dropdown-item>
                          <el-dropdown-item @click="pinChat">
                            <el-icon><Star /></el-icon>
                            {{ currentSession?.isPinned ? '取消置顶' : '置顶聊天' }}
                          </el-dropdown-item>
                          <el-dropdown-item divided @click="viewChatMembers" v-if="currentSession?.type === 'GROUP'">
                            <el-icon><User /></el-icon>
                            查看成员
                          </el-dropdown-item>
                          <el-dropdown-item divided @click="clearChatHistory">
                            <el-icon><Delete /></el-icon>
                            清空聊天记录
                          </el-dropdown-item>
                          <el-dropdown-item @click="exitGroup" v-if="currentSession?.type === 'GROUP'" class="danger-item">
                            <el-icon><DeleteFilled /></el-icon>
                            退出群聊
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </el-tooltip>
                </div>
              </div>

              <!-- 消息区 -->
              <div ref="messageAreaRef" class="message-area" @contextmenu.prevent>
                <!-- 连接状态提示 -->
                <div v-if="!isConnected" class="connection-status">
                  <el-icon><Warning /></el-icon>
                  <span>连接中...</span>
                </div>
                <div
                  v-for="msg in messages"
                  :key="msg.id || msg.clientMsgId"
                  class="message-item"
                  :class="{ isOwn: msg.isOwn || msg.senderId === currentUser?.userId }"
                >
                  <!-- 对方消息的左侧头像 -->
                  <SmartAvatar
                    v-if="!msg.isOwn && !(msg.senderId === currentUser?.userId)"
                    :name="msg.senderName || msg.sender?.name"
                    :avatar="msg.senderAvatar || msg.avatar"
                    :size="36"
                    :show-border="true"
                    :show-online="false"
                    class="message-avatar"
                  />
                  <div class="message-content" @click.right.prevent="showMessageMenu($event, msg)">
                    <div
                      v-if="!msg.isOwn && !(msg.senderId === currentUser?.userId)"
                      class="sender-name"
                    >
                      {{ msg.senderName || msg.sender?.name }}
                    </div>

                    <!-- 引用回复内容 -->
                    <div v-if="msg.replyTo" class="message-quote">
                      <div class="quote-title">{{ msg.replyTo.senderName }}:</div>
                      <div class="quote-content">{{ msg.replyTo.content }}</div>
                    </div>

                    <!-- 文本消息 -->
                    <div
                      v-if="msg.type === 'text' || !msg.type"
                      class="message-bubble"
                      :class="{
                        sending: msg.status === 'sending',
                        failed: msg.status === 'failed',
                      }"
                    >
                      <span v-html="formatMessageContent(msg.content)"></span>
                    </div>

                    <!-- 图片消息 -->
                    <div v-else-if="msg.type === 'image'" class="message-image">
                      <el-image
                        :src="getImageUrl(msg)"
                        :preview-src-list="[getImageUrl(msg)]"
                        fit="cover"
                        class="image-content"
                        :class="{ sending: msg.status === 'sending' }"
                      />
                    </div>

                    <!-- 文件消息 -->
                    <div
                      v-else-if="msg.type === 'file'"
                      class="message-file"
                      :class="{ sending: msg.status === 'sending' }"
                    >
                      <div class="file-icon">
                        <el-icon><Document /></el-icon>
                      </div>
                      <div class="file-info">
                        <div class="file-name">{{ getFileInfo(msg).name }}</div>
                        <div class="file-size">
                          {{ getFileInfo(msg).size ? formatFileSize(getFileInfo(msg).size) : '' }}
                        </div>
                      </div>
                      <el-button type="primary" size="small" @click.stop="downloadFile(msg)"
                        >下载</el-button
                      >
                    </div>

                    <!-- 语音消息 -->
                    <div
                      v-else-if="msg.type === 'voice'"
                      class="message-voice"
                      :class="{ sending: msg.status === 'sending' }"
                    >
                      <el-icon class="voice-icon"><Microphone /></el-icon>
                      <span class="voice-duration">{{ msg.duration || 0 }}''</span>
                    </div>

                    <!-- 其他类型 -->
                    <div
                      v-else
                      class="message-bubble"
                      :class="{ sending: msg.status === 'sending' }"
                    >
                      [{{ msg.type }}消息]
                    </div>

                    <div class="message-time">
                      {{ formatTime(msg.timestamp || msg.time) }}
                      <el-icon v-if="msg.status === 'sending'" class="status-icon sending"
                        ><Loading
                      /></el-icon>
                      <el-icon
                        v-else-if="msg.status === 'failed'"
                        class="status-icon failed"
                        @click="resendMessage(msg)"
                        ><WarningFilled
                      /></el-icon>
                      <el-icon
                        v-else-if="msg.isOwn || msg.senderId === currentUser?.userId"
                        class="status-icon sent"
                        ><SuccessFilled
                      /></el-icon>
                      <!-- 消息已读状态 -->
                      <span
                        v-if="(msg.isOwn || msg.senderId === currentUser?.userId) && msg.status !== 'sending' && msg.status !== 'failed'"
                        class="read-receipt"
                      >
                        <template v-if="currentSession?.type === 'PRIVATE'">
                          {{ msg.isRead ? '已读' : '未读' }}
                        </template>
                        <template v-else>
                          {{ msg.readCount || 0 }}{{ msg.memberCount ? `/${msg.memberCount}` : '' }}已读
                        </template>
                      </span>
                    </div>
                    <!-- 消息反应显示 -->
                    <div
                      v-if="getMessageReactions(msg.id).length > 0"
                      class="message-reactions"
                      @click.stop="showReactionPickerFor(msg)"
                    >
                      <div
                        v-for="(stat, key) in getReactionStats(msg.id)"
                        :key="key"
                        class="reaction-item"
                        :class="{ active: isReactedByUser(msg.id, key) }"
                      >
                        <span class="reaction-emoji">{{ getEmojiByKey(key) }}</span>
                        <span class="reaction-count">{{ stat }}</span>
                      </div>
                    </div>
                  </div>
                  <!-- 自己消息的右侧头像 -->
                  <SmartAvatar
                    v-if="msg.isOwn || msg.senderId === currentUser?.userId"
                    :name="currentUser?.nickName || currentUser?.userName || '我'"
                    :avatar="currentUser?.avatar || msg.senderAvatar || msg.avatar"
                    :size="36"
                    :show-border="true"
                    :show-online="false"
                    class="message-avatar own-avatar"
                  />
                </div>
              </div>

              <!-- 输入区 -->
              <div class="input-area">
                <!-- 隐藏的文件输入 -->
                <input
                  ref="fileInputRef"
                  type="file"
                  style="display: none"
                  @change="handleFileSelect"
                />
                <input
                  ref="imageInputRef"
                  type="file"
                  accept="image/*"
                  style="display: none"
                  @change="handleImageSelect"
                />

                <!-- 引用回复预览 -->
                <div v-if="replyingMessage" class="reply-preview">
                  <div class="reply-info">
                    <span class="reply-label">回复 {{ replyingMessage.senderName }}</span>
                    <span class="reply-content">{{ replyingMessage.content }}</span>
                  </div>
                  <el-button :icon="Close" text circle size="small" @click="cancelReply" />
                </div>

                <!-- 语音录制界面 -->
                <div v-if="isRecording" class="voice-recording-panel">
                  <div class="voice-info">
                    <span class="recording-duration">{{ formattedDuration }}</span>
                    <div class="volume-bars">
                      <div
                        v-for="(height, index) in getVolumeBars(5)"
                        :key="index"
                        class="volume-bar"
                        :style="{ height: height + '%' }"
                      ></div>
                    </div>
                    <span class="recording-tip">正在录音...</span>
                  </div>
                  <div class="voice-actions">
                    <el-button size="small" @click="cancelRecording">取消</el-button>
                    <el-button type="danger" size="small" @click="stopVoiceRecord">完成</el-button>
                  </div>
                </div>

                <!-- 表情选择器 -->
                <div v-if="showEmojiPicker" class="emoji-picker">
                  <div class="emoji-tabs">
                    <div
                      v-for="tab in emojiTabs"
                      :key="tab.key"
                      class="emoji-tab"
                      :class="{ active: activeEmojiTab === tab.key }"
                      @click="activeEmojiTab = tab.key"
                    >
                      {{ tab.icon }}
                    </div>
                  </div>
                  <div class="emoji-list">
                    <div
                      v-for="emoji in currentEmojis"
                      :key="emoji"
                      class="emoji-item"
                      @click="insertEmoji(emoji)"
                    >
                      {{ emoji }}
                    </div>
                  </div>
                </div>

                <!-- @提及建议 -->
                <div
                  v-if="showMentionSuggestions && mentionSuggestions.length > 0"
                  class="mention-suggestions"
                >
                  <div
                    v-for="user in mentionSuggestions"
                    :key="user.id"
                    class="mention-item"
                    :class="{ 'mention-all': user.isAll }"
                    @click="selectMention(user)"
                  >
                    <el-avatar v-if="!user.isAll" :size="28" :src="user.avatar">
                      {{ (user.name || user.nickname)?.charAt(0) || 'U' }}
                    </el-avatar>
                    <div v-else class="mention-all-avatar">
                      <el-icon><Promotion /></el-icon>
                    </div>
                    <span class="mention-name">{{ user.name || user.nickname }}</span>
                    <span v-if="user.isAll" class="mention-all-badge">全员</span>
                  </div>
                </div>

                <div class="input-toolbar">
                  <el-tooltip content="表情" placement="top">
                    <el-button
                      :icon="ChatDotRound"
                      text
                      :class="{ active: showEmojiPicker }"
                      @click="showEmojiPicker = !showEmojiPicker"
                    />
                  </el-tooltip>
                  <el-tooltip content="文件" placement="top">
                    <el-button
                      :icon="Folder"
                      text
                      :disabled="uploading"
                      @click="triggerFileSelect"
                    />
                  </el-tooltip>
                  <el-tooltip content="图片" placement="top">
                    <el-button
                      :icon="PictureFilled"
                      text
                      :disabled="uploading"
                      @click="selectImage"
                    />
                  </el-tooltip>
                  <el-tooltip content="语音" placement="top">
                    <el-button
                      :icon="Microphone"
                      text
                      :disabled="uploading"
                      :class="{ recording: isRecording }"
                      @click="isRecording ? stopVoiceRecord() : startVoiceRecord()"
                    />
                  </el-tooltip>
                </div>
                <el-input
                  ref="inputRef"
                  v-model="inputMessage"
                  type="textarea"
                  :rows="1"
                  :autosize="{ minRows: 1, maxRows: 7 }"
                  placeholder="输入消息... @提及、Enter发送、Ctrl+Enter换行"
                  class="chat-input"
                  @keydown="handleInputKeydown"
                  @input="handleInputChange"
                />
                <div class="input-footer">
                  <span class="input-tip">Enter 发送 · @提及</span>
                  <el-button type="primary" size="small" class="send-button" @click="sendMessage"
                    >发送</el-button
                  >
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <el-icon :size="64" color="#ddd"><ChatLineSquare /></el-icon>
              <p>选择一个会话开始聊天</p>
            </div>
          </div>
        </div>

        <!-- 通讯录模块 -->
        <div v-else-if="activeModule === 'contacts'" class="contacts-workspace">
          <!-- 通讯录分类 -->
          <div class="contacts-sidebar">
            <div class="section-title">通讯录</div>
            <div class="tree-list">
              <div
                v-for="item in contactCategories"
                :key="item.key"
                class="tree-item"
                :class="{ active: contactCategory === item.key }"
                @click="contactCategory = item.key"
              >
                <el-icon>
                  <component :is="item.icon" />
                </el-icon>
                <span class="tree-label">{{ item.label }}</span>
                <span v-if="item.count > 0" class="tree-count">{{ item.count }}</span>
              </div>
            </div>
          </div>

          <!-- 联系人列表 -->
          <div class="contacts-list-panel">
            <div class="list-header">
              <el-input
                v-model="contactSearch"
                placeholder="搜索..."
                :prefix-icon="Search"
                clearable
                class="search-input"
              />
            </div>

            <!-- 好友列表 / 群组列表（带A-Z索引） -->
            <div
              v-if="contactCategory === 'friends' || contactCategory === 'groups'"
              class="indexed-contacts"
            >
              <div
                v-for="group in searchedGroups"
                :id="`contact-section-${group.letter}`"
                :key="group.letter"
                class="contact-section"
              >
                <div class="section-header">{{ group.letter }}</div>
                <div
                  v-for="contact in group.contacts"
                  :key="contact.friendId || contact.id"
                  class="contact-item-row"
                  @click="
                    contactCategory === 'friends'
                      ? selectContact(contact)
                      : selectGroupContact(contact)
                  "
                >
                  <el-avatar :size="44" :src="contact.avatar">
                    {{ (contact.name || contact.groupName || contact.nickname)?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="contact-row-info">
                    <div class="contact-row-name">
                      {{ contact.name || contact.groupName || contact.nickname }}
                    </div>
                    <div v-if="contactCategory === 'friends'" class="contact-row-desc">
                      {{ contact.signature || contact.remark || '' }}
                    </div>
                    <div v-else class="contact-row-desc">{{ contact.memberCount || 0 }}人</div>
                  </div>
                </div>
              </div>
              <el-empty v-if="searchedGroups.length === 0" description="暂无数据" />
            </div>

            <!-- 组织架构 -->
            <div v-else-if="contactCategory === 'org'" class="org-tree">
              <el-tree
                :data="orgTree"
                :props="{ label: 'name', children: 'children' }"
                node-key="id"
                @node-click="handleOrgNodeClick"
              >
                <template #default="{ node, data }">
                  <div class="org-node">
                    <el-icon><Folder /></el-icon>
                    <span>{{ data.name }}</span>
                    <span v-if="data.userCount" class="user-count">({{ data.userCount }})</span>
                  </div>
                </template>
              </el-tree>

              <!-- 部门成员列表 -->
              <div v-if="orgMembers.length > 0" class="org-members">
                <div class="members-title">{{ currentDept?.name }} - 成员列表</div>
                <div
                  v-for="member in orgMembers"
                  :key="member.id"
                  class="contact-item"
                  @click="selectContact(member)"
                >
                  <el-avatar :size="36" :src="member.avatar">
                    {{ (member.name || member.nickname)?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="contact-info">
                    <div class="contact-name">{{ member.name || member.nickname }}</div>
                    <div class="contact-signature">
                      {{ member.position || member.signature || '-' }}
                    </div>
                  </div>
                  <el-button
                    v-if="!member.isFriend"
                    size="small"
                    @click.stop="sendFriendRequest(member)"
                    >添加</el-button
                  >
                  <div v-else-if="member.online" class="online-dot"></div>
                </div>
              </div>
            </div>

            <!-- 新朋友 -->
            <div v-else-if="contactCategory === 'new'" class="new-friends">
              <!-- 搜索新朋友 -->
              <div class="new-friends-search">
                <el-input
                  v-model="contactSearch"
                  placeholder="搜索用户名/手机号添加新朋友"
                  :prefix-icon="Search"
                  clearable
                  @input="handleNewFriendsSearch"
                  @clear="handleNewFriendsSearch('')"
                >
                  <template #append>
                    <el-button :icon="Plus" @click="showAddFriendDialog">添加</el-button>
                  </template>
                </el-input>
              </div>

              <!-- 搜索结果 -->
              <div v-if="contactSearch && newFriendsSearchResult.length > 0" class="search-results">
                <div
                  v-for="user in newFriendsSearchResult"
                  :key="user.id"
                  class="search-result-item"
                  @click="selectContact(user)"
                >
                  <el-avatar :size="40" :src="user.avatar">
                    {{ (user.name || user.nickname)?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="result-info">
                    <div class="result-name">{{ user.name || user.nickname }}</div>
                    <div class="result-detail">{{ user.deptName || user.position || '用户' }}</div>
                  </div>
                  <div class="result-action">
                    <el-button size="small" @click.stop="handleAddFriend(user)">添加</el-button>
                  </div>
                </div>
              </div>

              <!-- 好友请求列表 -->
              <div v-if="!contactSearch || newFriendsSearchResult.length === 0">
                <div v-if="friendRequests.length > 0" class="friend-requests-section">
                  <div class="section-title">好友请求</div>
                  <div v-for="request in friendRequests" :key="request.id" class="friend-request-item">
                    <el-avatar :size="48" :src="request.avatar">
                      {{ (request.name || request.nickname)?.charAt(0) || 'U' }}
                    </el-avatar>
                    <div class="request-info">
                      <div class="request-name">{{ request.name || request.nickname }}</div>
                      <div class="request-message">{{ request.message || '请求添加你为好友' }}</div>
                      <div class="request-time">{{ formatTime(request.timestamp) }}</div>
                    </div>
                    <div class="request-actions">
                      <el-button
                        v-if="request.status === 'pending'"
                        type="primary"
                        size="small"
                        @click="handleFriendRequest(request, 'accept')"
                      >
                        同意
                      </el-button>
                      <el-button
                        v-if="request.status === 'pending'"
                        size="small"
                        @click="handleFriendRequest(request, 'reject')"
                      >
                        拒绝
                      </el-button>
                      <span v-else class="request-status">
                        {{ request.status === 'accepted' ? '已添加' : '已拒绝' }}
                      </span>
                    </div>
                  </div>
                </div>
                <el-empty v-if="friendRequests.length === 0" description="暂无新朋友请求" />
              </div>
            </div>

            <!-- A-Z 索引侧边栏 -->
            <div
              v-if="contactCategory === 'friends' || contactCategory === 'groups'"
              class="index-sidebar"
            >
              <div
                v-for="group in searchedGroups"
                :key="group.letter"
                class="index-item"
                :class="{
                  active: activeLetter === group.letter,
                }"
                @click="scrollToLetter(group.letter)"
              >
                {{ group.letter }}
              </div>
            </div>
          </div>

          <!-- 联系人详情 -->
          <div class="contacts-detail-panel">
            <template v-if="selectedContact">
              <div class="detail-header">
                <el-avatar :size="80" :src="selectedContact.avatar">
                  {{ (selectedContact.name || selectedContact.nickname)?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="header-info">
                  <h2>{{ selectedContact.name || selectedContact.nickname }}</h2>
                  <p class="status" :class="{ online: selectedContact.online }">
                    {{ selectedContact.online ? '在线' : '离线' }}
                  </p>
                </div>
              </div>

              <div class="detail-actions">
                <el-button type="primary" :icon="ChatDotRound" @click="startChat(selectedContact)"
                  >发消息</el-button
                >
                <el-button :icon="VideoCamera" @click="startVideoCall(selectedContact)"
                  >视频通话</el-button
                >
              </div>

              <div class="detail-info">
                <div class="info-section">
                  <h4>个人信息</h4>
                  <div class="info-item">
                    <span class="label">昵称</span>
                    <span class="value">{{ selectedContact.nickname || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">部门</span>
                    <span class="value">{{ selectedContact.deptName || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">职位</span>
                    <span class="value">{{ selectedContact.position || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">邮箱</span>
                    <span class="value">{{ selectedContact.email || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">手机</span>
                    <span class="value">{{ selectedContact.phone || '-' }}</span>
                  </div>
                </div>
              </div>
            </template>
            <div v-else class="empty-detail">
              <el-icon :size="64" color="#ddd"><User /></el-icon>
              <p>选择一个联系人查看详情</p>
            </div>
          </div>
        </div>

        <!-- 工作台模块 -->
        <div v-else-if="activeModule === 'workbench'" class="workbench-workspace">
          <!-- 工作台侧边栏 -->
          <div class="workbench-sidebar">
            <div class="section-title">应用中心</div>
            <div class="app-category-list">
              <div
                v-for="category in workbenchCategories"
                :key="category.key"
                class="category-item"
                :class="{ active: workbenchCategory === category.key }"
                @click="workbenchCategory = category.key"
              >
                <el-icon>
                  <component :is="category.icon" />
                </el-icon>
                <span class="category-label">{{ category.label }}</span>
                <span v-if="category.count > 0" class="category-count">{{ category.count }}</span>
              </div>
            </div>
          </div>

          <!-- 工作台内容 -->
          <div class="workbench-content">
            <!-- 应用网格 -->
            <div v-if="workbenchCategory === 'all'" class="app-grid">
              <div
                v-for="app in workbenchApps"
                :key="app.key"
                class="app-card"
                @click="openApp(app.key)"
              >
                <div class="app-icon" :style="{ background: app.color }">
                  <el-icon :size="28" color="white">
                    <component :is="app.icon" />
                  </el-icon>
                </div>
                <span class="app-name">{{ app.name }}</span>
              </div>
            </div>

            <!-- 待办审批 -->
            <div v-else-if="workbenchCategory === 'approval'" class="approval-list">
              <div class="list-header">
                <h3>待办审批</h3>
                <el-tabs v-model="approvalTab" class="approval-tabs">
                  <el-tab-pane label="待我审批" name="pending"></el-tab-pane>
                  <el-tab-pane label="我发起的" name="my"></el-tab-pane>
                  <el-tab-pane label="已处理" name="done"></el-tab-pane>
                </el-tabs>
              </div>

              <div class="approval-items">
                <div v-for="item in mockApprovals" :key="item.id" class="approval-item">
                  <div class="approval-icon" :style="{ background: item.color }">
                    <el-icon :size="20" color="white">
                      <component :is="item.icon" />
                    </el-icon>
                  </div>
                  <div class="approval-info">
                    <div class="approval-title">{{ item.title }}</div>
                    <div class="approval-detail">
                      <span class="approval-type">{{ item.type }}</span>
                      <span class="approval-time">{{ formatTime(item.time) }}</span>
                    </div>
                    <div class="approval-applicant">申请人: {{ item.applicant }}</div>
                  </div>
                  <div class="approval-actions">
                    <el-button type="primary" size="small" @click="handleApproval(item, 'approve')"
                      >同意</el-button
                    >
                    <el-button size="small" @click="handleApproval(item, 'reject')">拒绝</el-button>
                  </div>
                </div>
                <el-empty v-if="mockApprovals.length === 0" description="暂无待办审批" />
              </div>
            </div>

            <!-- 考勤打卡 -->
            <div v-else-if="workbenchCategory === 'attendance'" class="attendance-panel">
              <div class="attendance-card">
                <div class="attendance-time">{{ currentTime }}</div>
                <div class="attendance-date">{{ currentDate }}</div>
                <div class="attendance-status" :class="{ checked: hasCheckedIn }">
                  {{ hasCheckedIn ? '今日已打卡' : '未打卡' }}
                </div>
                <el-button
                  v-if="!hasCheckedIn"
                  type="primary"
                  size="large"
                  :icon="Edit"
                  @click="handleCheckIn"
                >
                  立即打卡
                </el-button>
                <div v-else class="check-time">打卡时间: {{ checkInTime }}</div>
              </div>

              <div class="attendance-records">
                <h4>本周打卡记录</h4>
                <div class="record-list">
                  <div v-for="record in weeklyRecords" :key="record.date" class="record-item">
                    <div class="record-day">{{ record.day }}</div>
                    <div class="record-time">{{ record.checkInTime || '-' }}</div>
                    <div class="record-status" :class="record.status">
                      {{ record.statusText }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 日程管理 -->
            <div v-else-if="workbenchCategory === 'schedule'" class="schedule-panel">
              <div class="schedule-header">
                <h3>我的日程</h3>
                <el-button :icon="Plus" type="primary" size="small" @click="showAddSchedule"
                  >新建日程</el-button
                >
              </div>

              <div class="schedule-calendar">
                <div class="calendar-header">
                  <el-button :icon="ArrowLeft" circle size="small" @click="prevWeek" />
                  <span class="calendar-title">{{ currentWeekRange }}</span>
                  <el-button :icon="ArrowRight" circle size="small" @click="nextWeek" />
                </div>
                <div class="week-days">
                  <div
                    v-for="day in weekDays"
                    :key="day.date"
                    class="day-item"
                    :class="{ today: day.isToday, selected: selectedDate === day.date }"
                    @click="selectDate(day.date)"
                  >
                    <div class="day-name">{{ day.name }}</div>
                    <div class="day-date">{{ day.dayNum }}</div>
                  </div>
                </div>
              </div>

              <div class="schedule-list">
                <div
                  v-for="schedule in selectedDateSchedules"
                  :key="schedule.id"
                  class="schedule-item"
                  :style="{ borderLeftColor: schedule.color }"
                >
                  <div class="schedule-time">{{ schedule.time }}</div>
                  <div class="schedule-content">
                    <div class="schedule-title">{{ schedule.title }}</div>
                    <div v-if="schedule.location" class="schedule-location">
                      <el-icon><Location /></el-icon>
                      {{ schedule.location }}
                    </div>
                  </div>
                </div>
                <el-empty v-if="selectedDateSchedules.length === 0" description="暂无日程" />
              </div>
            </div>
          </div>
        </div>

        <!-- 钉盘模块 -->
        <div v-else-if="activeModule === 'drive'" class="drive-workspace">
          <!-- 钉盘侧边栏 -->
          <div class="drive-sidebar">
            <div class="section-title">文件分类</div>
            <div class="drive-nav-list">
              <div
                v-for="item in driveCategories"
                :key="item.key"
                class="drive-nav-item"
                :class="{ active: driveCategory === item.key }"
                @click="driveCategory = item.key"
              >
                <el-icon>
                  <component :is="item.icon" />
                </el-icon>
                <span class="nav-label">{{ item.label }}</span>
                <span v-if="item.count > 0" class="nav-count">{{ item.count }}</span>
              </div>
            </div>

            <div class="section-title" style="margin-top: 20px">文件夹</div>
            <div class="folder-tree">
              <el-tree
                :data="folderTreeData"
                :props="{ children: 'children', label: 'name' }"
                node-key="id"
                :highlight-current="true"
                :expand-on-click-node="false"
                @node-click="handleFolderClick"
              >
                <template #default="{ node, data }">
                  <div class="tree-node">
                    <el-icon><Folder /></el-icon>
                    <span class="tree-label">{{ node.label }}</span>
                    <span class="tree-count">({{ data.itemCount || 0 }})</span>
                  </div>
                </template>
              </el-tree>
            </div>

            <div class="section-title" style="margin-top: 20px">存储空间</div>
            <div class="storage-info">
              <el-progress :percentage="storagePercent" :stroke-width="6" />
              <div class="storage-text">已使用 {{ storageUsed }} / {{ storageTotal }}</div>
            </div>
          </div>

          <!-- 钉盘内容 -->
          <div class="drive-content">
            <div class="drive-toolbar">
              <div class="toolbar-left">
                <div class="breadcrumb">
                  <span class="breadcrumb-item" @click="navigateToRoot">根目录</span>
                  <span v-for="(crumb, index) in breadcrumbs" :key="index" class="breadcrumb-item">
                    / {{ crumb.name }}
                  </span>
                </div>
              </div>
              <div class="toolbar-right">
                <el-input
                  v-model="driveSearchKeyword"
                  placeholder="搜索文件..."
                  :prefix-icon="Search"
                  size="small"
                  style="width: 200px; margin-right: 10px"
                  clearable
                  @input="handleDriveSearch"
                />
                <el-upload
                  :show-file-list="false"
                  :before-upload="handleFileUpload"
                  :multiple="true"
                  :drag="true"
                  style="display: inline-block"
                >
                  <el-button :icon="Upload">上传文件</el-button>
                </el-upload>
                <el-button :icon="Folder" @click="createFolder">新建文件夹</el-button>
                <el-dropdown trigger="click" @command="handleBatchAction">
                  <el-button :disabled="selectedFiles.length === 0">
                    批量操作 <el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="download" :icon="Download">批量下载</el-dropdown-item>
                      <el-dropdown-item command="move" :icon="FolderOpened">移动到</el-dropdown-item>
                      <el-dropdown-item command="delete" :icon="Delete">批量删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <!-- 批量选择栏 -->
            <div v-if="selectedFiles.length > 0" class="batch-selection-bar">
              <span class="selection-info">已选择 {{ selectedFiles.length }} 项</span>
              <div class="selection-actions">
                <el-button size="small" @click="clearSelection">取消选择</el-button>
                <el-button size="small" :icon="Download" @click="batchDownload">下载</el-button>
                <el-button size="small" :icon="Delete" type="danger" @click="batchDelete">删除</el-button>
              </div>
            </div>

            <!-- 文件列表 -->
            <div
              class="file-list"
              @dragover.prevent
              @dragenter="handleDragEnter"
              @dragleave="handleDragLeave"
              @drop="handleFileDrop"
            >
              <!-- 文件夹 -->
              <div
                v-for="folder in currentFolders"
                :key="'folder-' + folder.id"
                class="file-item folder-item"
                :class="{
                  selected: isSelected(folder),
                  'batch-selected': isInBatchSelection(folder)
                }"
                @click="selectFile(folder, $event)"
                @dblclick="openFolder(folder)"
              >
                <el-checkbox
                  v-if="batchSelectMode || isInBatchSelection(folder)"
                  :model-value="isInBatchSelection(folder)"
                  class="file-checkbox"
                  @change="toggleFileSelection(folder, $event)"
                  @click.stop
                />
                <div class="file-icon folder-icon">
                  <el-icon :size="32"><Folder /></el-icon>
                </div>
                <div class="file-info">
                  <div class="file-name">{{ folder.name }}</div>
                  <div class="file-meta">{{ folder.itemCount || 0 }} 项</div>
                </div>
              </div>

              <!-- 文件 -->
              <div
                v-for="file in displayFiles"
                :key="'file-' + file.id"
                class="file-item"
                :class="{
                  selected: isSelected(file),
                  'batch-selected': isInBatchSelection(file)
                }"
                @click="selectFile(file, $event)"
                @dblclick="openFile(file)"
                @contextmenu.prevent="showFileContextMenu($event, file)"
              >
                <el-checkbox
                  v-if="batchSelectMode || isInBatchSelection(file)"
                  :model-value="isInBatchSelection(file)"
                  class="file-checkbox"
                  @change="toggleFileSelection(file, $event)"
                  @click.stop
                />
                <div class="file-icon" :class="`file-type-${file.type}`">
                  <el-icon :size="32">
                    <component :is="getFileIcon(file.type)" />
                  </el-icon>
                </div>
                <div class="file-info">
                  <div class="file-name">{{ file.name }}</div>
                  <div class="file-meta">
                    {{ formatFileSize(file.size) }} · {{ formatTime(file.updateTime) }}
                  </div>
                </div>
                <div class="file-actions">
                  <el-button :icon="View" size="small" circle @click.stop="previewFile(file)" />
                  <el-button :icon="Share" size="small" circle @click.stop="shareFile(file)" />
                  <el-button
                    :icon="Download"
                    size="small"
                    circle
                    @click.stop="downloadSingleFile(file)"
                  />
                </div>
              </div>

              <!-- 拖拽上传提示 -->
              <div v-if="isDraggingFile" class="drag-upload-overlay">
                <el-icon :size="60"><UploadFilled /></el-icon>
                <p>拖拽文件到这里上传</p>
              </div>

              <el-empty
                v-if="currentFolders.length === 0 && displayFiles.length === 0"
                :description="driveSearchKeyword ? '没有找到相关文件' : '暂无文件'"
              />
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 创建群组对话框 -->
    <el-dialog
      v-model="createGroupDialogVisible"
      title="创建群组"
      width="500px"
      @close="resetCreateGroupForm"
    >
      <el-form
        ref="createGroupFormRef"
        :model="createGroupForm"
        :rules="createGroupRules"
        label-width="80px"
      >
        <el-form-item label="群组名称" prop="name">
          <el-input
            v-model="createGroupForm.name"
            placeholder="请输入群组名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="群组头像" prop="avatar">
          <div class="avatar-upload">
            <el-upload
              :show-file-list="false"
              :on-success="handleGroupAvatarSuccess"
              :before-upload="beforeGroupAvatarUpload"
              :http-request="customGroupAvatarUpload"
              accept="image/*"
            >
              <el-avatar v-if="createGroupForm.avatar" :size="80" :src="createGroupForm.avatar" />
              <div v-else class="avatar-placeholder">
                <el-icon :size="32"><Plus /></el-icon>
                <span>上传头像</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="群组简介" prop="description">
          <el-input
            v-model="createGroupForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入群组简介"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="选择成员" prop="memberIds">
          <div class="member-selector">
            <div class="selected-members">
              <el-tag
                v-for="member in selectedGroupMembers"
                :key="member.id"
                closable
                @close="removeGroupMember(member)"
              >
                {{ member.name || member.nickname }}
              </el-tag>
              <el-button
                v-if="selectedGroupMembers.length < 1"
                text
                size="small"
                @click="showMemberSelector"
              >
                选择成员
              </el-button>
            </div>
            <el-button
              v-if="selectedGroupMembers.length > 0"
              text
              size="small"
              @click="showMemberSelector"
            >
              <el-icon><Plus /></el-icon> 添加成员
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="加群方式" prop="joinType">
          <el-radio-group v-model="createGroupForm.joinType">
            <el-radio label="open">自由加入</el-radio>
            <el-radio label="needApproval">需要验证</el-radio>
            <el-radio label="forbidden">禁止加群</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createGroupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creatingGroup" @click="submitCreateGroup"
          >创建</el-button
        >
      </template>
    </el-dialog>

    <!-- 选择成员对话框 -->
    <el-dialog v-model="memberSelectorVisible" title="选择成员" width="500px">
      <div class="member-selector-content">
        <el-input
          v-model="memberSearchKeyword"
          placeholder="搜索成员..."
          :prefix-icon="Search"
          clearable
          size="small"
          class="member-search"
        />
        <div class="member-list">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            class="member-list-item"
            @click="toggleGroupMember(user)"
          >
            <el-avatar :size="36" :src="user.avatar">
              {{ (user.name || user.nickname)?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="member-info">
              <span class="member-name">{{ user.name || user.nickname }}</span>
              <span class="member-dept">{{ user.deptName || user.position || '' }}</span>
            </div>
            <el-checkbox
              :model-value="isGroupMemberSelected(user)"
              @change="toggleGroupMember(user)"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="memberSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMemberSelection">确定</el-button>
      </template>
    </el-dialog>

    <!-- 群组管理对话框 -->
    <el-dialog v-model="groupManageDialogVisible" title="群组管理" width="600px">
      <div v-if="managingGroup" class="group-manage">
        <div class="group-info-header">
          <el-avatar :size="64" :src="managingGroup.avatar">
            {{ managingGroup.name?.charAt(0) || 'G' }}
          </el-avatar>
          <div class="group-info">
            <h3>{{ managingGroup.name }}</h3>
            <p>{{ managingGroup.memberCount || 0 }} 位成员</p>
          </div>
        </div>

        <el-tabs v-model="groupManageTab">
          <el-tab-pane label="成员" name="members">
            <div class="group-members">
              <div class="members-toolbar">
                <el-input
                  v-model="groupMemberSearch"
                  placeholder="搜索成员..."
                  size="small"
                  :prefix-icon="Search"
                  clearable
                  class="member-search-input"
                />
                <el-button
                  v-if="canAddMembers"
                  :icon="Plus"
                  size="small"
                  @click="showAddGroupMember"
                >
                  添加成员
                </el-button>
              </div>
              <div class="members-list">
                <div
                  v-for="member in filteredGroupMembers"
                  :key="member.id"
                  class="group-member-item"
                >
                  <el-avatar :size="40" :src="member.avatar">
                    {{ (member.name || member.nickname)?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="member-info">
                    <div class="member-name">
                      {{ member.name || member.nickname }}
                      <el-tag v-if="member.role === 'owner'" size="small" type="danger"
                        >群主</el-tag
                      >
                      <el-tag v-else-if="member.role === 'admin'" size="small">管理员</el-tag>
                    </div>
                    <div class="member-role">{{ member.roleText || '成员' }}</div>
                  </div>
                  <el-dropdown
                    v-if="canManageMember(member)"
                    @command="cmd => handleMemberCommand(cmd, member)"
                  >
                    <el-button :icon="More" circle size="small" text />
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item v-if="member.role !== 'admin'" command="setAdmin"
                          >设为管理员</el-dropdown-item
                        >
                        <el-dropdown-item v-if="member.role === 'admin'" command="removeAdmin"
                          >取消管理员</el-dropdown-item
                        >
                        <el-dropdown-item v-if="!member.isMuted" command="mute">
                          <el-icon><Mute /></el-icon>
                          禁言成员
                        </el-dropdown-item>
                        <el-dropdown-item v-if="member.isMuted" command="unmute">
                          <el-icon><Unlock /></el-icon>
                          取消禁言
                        </el-dropdown-item>
                        <el-dropdown-item command="remove" style="color: #f5222d"
                          >移出群组</el-dropdown-item
                        >
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="设置" name="settings">
            <el-form label-width="80px">
              <el-form-item label="群组名称">
                <el-input v-model="managingGroup.name" />
              </el-form-item>
              <el-form-item label="群组简介">
                <el-input v-model="managingGroup.description" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item label="群公告">
                <el-input
                  v-model="managingGroup.announcement"
                  type="textarea"
                  :rows="4"
                  placeholder="暂无公告"
                />
              </el-form-item>
              <el-form-item label="加群方式">
                <el-select v-model="managingGroup.joinType">
                  <el-option label="自由加入" value="open" />
                  <el-option label="需要验证" value="needApproval" />
                  <el-option label="禁止加群" value="forbidden" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveGroupSettings">保存设置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="群文件" name="files">
            <div class="group-files-panel">
              <div class="files-toolbar">
                <el-button :icon="Upload" size="small" @click="uploadGroupFile">上传文件</el-button>
                <el-button :icon="Folder" size="small">新建文件夹</el-button>
              </div>
              <div class="files-list">
                <div
                  v-for="file in groupFiles"
                  :key="file.id"
                  class="file-item"
                  @click="previewGroupFile(file)"
                >
                  <div class="file-icon">
                    <el-icon v-if="file.type === 'image'" :size="32"><Picture /></el-icon>
                    <el-icon v-else-if="file.type === 'video'" :size="32"><VideoCamera /></el-icon>
                    <el-icon v-else-if="file.type === 'folder'" :size="32"><Folder /></el-icon>
                    <el-icon v-else :size="32"><Document /></el-icon>
                  </div>
                  <div class="file-info">
                    <div class="file-name">{{ file.name }}</div>
                    <div class="file-meta">
                      <span>{{ file.size }}</span>
                      <span>{{ file.uploadTime }}</span>
                      <span>{{ file.uploader }}</span>
                    </div>
                  </div>
                  <el-dropdown @command="cmd => handleFileCommand(cmd, file)">
                    <el-button :icon="More" circle size="small" text />
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="download">下载</el-dropdown-item>
                        <el-dropdown-item command="rename">重命名</el-dropdown-item>
                        <el-dropdown-item command="delete" style="color: #f5222d"
                          >删除</el-dropdown-item
                        >
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
                <el-empty v-if="groupFiles.length === 0" description="暂无群文件" />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 消息右键菜单 -->
    <div
      v-if="messageMenuVisible"
      class="message-context-menu"
      :style="{ left: messageMenuPosition.x + 'px', top: messageMenuPosition.y + 'px' }"
    >
      <div class="menu-item" @click="replyMessage">
        <el-icon><Edit /></el-icon>
        <span>回复</span>
      </div>
      <div class="menu-item" @click="forwardMessage">
        <el-icon><Share /></el-icon>
        <span>转发</span>
      </div>
      <div class="menu-item" @click="copyMessage">
        <el-icon><DocumentCopy /></el-icon>
        <span>复制</span>
      </div>
      <!-- 添加表情反应 -->
      <div class="menu-item" @click="showReactionPicker">
        <el-icon><ChatDotRound /></el-icon>
        <span>添加表情反应</span>
      </div>
      <!-- 撤回：仅自己发送且在2分钟内的消息 -->
      <div v-if="canRecallMessage(selectedMessage)" class="menu-item" @click="recallMessage">
        <el-icon><RefreshLeft /></el-icon>
        <span>撤回</span>
      </div>
      <div
        v-if="selectedMessage?.isOwn || selectedMessage?.senderId === currentUser?.userId"
        class="menu-item"
        @click="deleteMessage"
      >
        <el-icon><Delete /></el-icon>
        <span>删除</span>
      </div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="selectMessage">
        <el-icon><Check /></el-icon>
        <span>多选</span>
      </div>
    </div>

    <!-- 表情反应选择器 -->
    <div
      v-if="reactionPickerVisible"
      class="reaction-picker"
      :style="{ left: reactionPickerPosition.x + 'px', top: reactionPickerPosition.y + 'px' }"
    >
      <div class="reaction-emoji-list">
        <div
          v-for="emoji in quickReactions"
          :key="emoji.key"
          class="reaction-emoji-item"
          :class="{ active: isReacted(emoji.key) }"
          @click="toggleReaction(emoji.key)"
        >
          {{ emoji.emoji }}
        </div>
      </div>
    </div>

    <!-- 转发消息对话框 -->
    <el-dialog v-model="forwardDialogVisible" title="转发消息" width="500px" :append-to-body="true">
      <div class="forward-content">
        <div class="forward-message">{{ selectedMessage?.content }}</div>
        <el-input
          v-model="forwardSearchKeyword"
          placeholder="搜索联系人或群组..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
        <div class="forward-targets">
          <div
            v-for="target in filteredForwardTargets"
            :key="target.id"
            class="target-item"
            @click="selectForwardTarget(target)"
          >
            <el-avatar :size="40" :src="target.avatar">
              {{ (target.name || target.groupName)?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="target-info">
              <div class="target-name">{{ target.name || target.groupName }}</div>
              <div class="target-desc">{{ target.type === 'group' ? '群组' : '好友' }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="forwardDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedForwardTarget" @click="confirmForward"
          >发送</el-button
        >
      </template>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="filePreviewDialogVisible"
      :title="previewingFile?.name"
      width="800px"
      :append-to-body="true"
      class="file-preview-dialog"
    >
      <div class="file-preview-content">
        <!-- 图片预览 -->
        <div v-if="isImageFile(previewingFile)" class="preview-image">
          <el-image :src="previewingFile?.url" fit="contain" />
        </div>
        <!-- PDF预览 -->
        <div v-else-if="isPdfFile(previewingFile)" class="preview-pdf">
          <iframe :src="previewingFile?.url" frameborder="0"></iframe>
        </div>
        <!-- 其他文件 -->
        <div v-else class="preview-file">
          <el-icon :size="80" color="#ddd"><Document /></el-icon>
          <p>{{ previewingFile?.name }}</p>
          <el-button type="primary" @click="downloadFile(previewingFile)">下载文件</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 通知面板 -->
    <notification-panel
      v-model:visible="notificationVisible"
      @close="notificationVisible = false"
    />

    <!-- 系统设置对话框 -->
    <system-settings v-model="systemSettingsVisible" @save="handleSettingsSave" />

    <!-- 主题设置对话框 -->
    <el-dialog v-model="themeSettingsVisible" title="系统设置" width="600px" :append-to-body="true">
      <el-tabs v-model="settingsTab" class="settings-tabs">
        <el-tab-pane label="界面设置" name="interface">
          <div class="settings-content">
            <div class="setting-item">
              <span class="setting-label">深色模式</span>
              <el-switch v-model="isDarkMode" @change="toggleDarkMode" />
            </div>
            <div class="setting-item">
              <span class="setting-label">消息提示音</span>
              <el-switch v-model="messageSoundEnabled" />
            </div>
            <div class="setting-item">
              <span class="setting-label">桌面通知</span>
              <el-switch v-model="desktopNotificationEnabled" @change="toggleDesktopNotification" />
            </div>
            <div class="setting-item">
              <span class="setting-label">字体大小</span>
              <el-select v-model="fontSize" placeholder="选择字体大小" style="width: 150px">
                <el-option label="小" value="small" />
                <el-option label="中" value="medium" />
                <el-option label="大" value="large" />
              </el-select>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="隐私设置" name="privacy">
          <div class="settings-content">
            <div class="setting-item">
              <span class="setting-label">显示在线状态</span>
              <el-switch v-model="showOnlineStatus" />
            </div>
            <div class="setting-item">
              <span class="setting-label">允许陌生人查找</span>
              <el-switch v-model="allowStrangerFind" />
            </div>
            <div class="setting-item">
              <span class="setting-label">允许陌生人发送消息</span>
              <el-switch v-model="allowStrangerMessage" />
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="账户安全" name="security">
          <div class="settings-content">
            <div class="setting-item">
              <span class="setting-label">两步验证</span>
              <el-switch v-model="twoFactorAuth" />
            </div>
            <div class="setting-item">
              <span class="setting-label">登录设备管理</span>
              <el-button link type="primary" @click="showDeviceManagement">查看设备</el-button>
            </div>
            <div class="setting-item">
              <span class="setting-label">修改密码</span>
              <el-button link type="primary" @click="showChangePassword">修改密码</el-button>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="消息设置" name="message">
          <div class="settings-content">
            <div class="setting-item">
              <span class="setting-label">消息保留时长</span>
              <el-select v-model="messageRetention" placeholder="选择保留时长" style="width: 150px">
                <el-option label="30天" :value="30" />
                <el-option label="90天" :value="90" />
                <el-option label="180天" :value="180" />
                <el-option label="永久" :value="0" />
              </el-select>
            </div>
            <div class="setting-item">
              <span class="setting-label">自动清理已读消息</span>
              <el-switch v-model="autoCleanReadMessages" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="themeSettingsVisible = false">关闭</el-button>
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
      </template>
    </el-dialog>

    <!-- 个人资料对话框 -->
    <el-dialog v-model="profileDialogVisible" title="个人资料" width="600px" :append-to-body="true">
      <div class="profile-dialog">
        <div class="profile-header">
          <el-avatar :size="80" :src="currentUser?.avatar">
            {{ currentUser?.name?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="profile-basic">
            <h3>{{ currentUser?.name || '用户' }}</h3>
            <p class="user-id">ID: {{ currentUser?.userId || '-' }}</p>
          </div>
        </div>
        <el-form :model="profileForm" label-width="100px">
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="个性签名">
            <el-input
              v-model="profileForm.signature"
              type="textarea"
              :rows="3"
              placeholder="请输入个性签名"
            />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 在线状态对话框 -->
    <el-dialog
      v-model="onlineStatusDialogVisible"
      title="在线状态"
      width="400px"
      :append-to-body="true"
    >
      <div class="online-status-dialog">
        <div
          v-for="status in onlineStatusOptions"
          :key="status.value"
          class="status-option"
          :class="{ active: currentOnlineStatus === status.value }"
          @click="selectOnlineStatus(status.value)"
        >
          <div class="status-icon" :class="status.iconClass">
            <el-icon><component :is="status.icon" /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-name">{{ status.label }}</div>
            <div class="status-desc">{{ status.desc }}</div>
          </div>
          <el-icon v-if="currentOnlineStatus === status.value" class="status-check">
            <CircleCheck />
          </el-icon>
        </div>
      </div>
      <template #footer>
        <el-button @click="onlineStatusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveOnlineStatus">确定</el-button>
      </template>
    </el-dialog>

    <!-- 主题切换对话框 -->
    <el-dialog v-model="themeDialogVisible" title="主题切换" width="500px" :append-to-body="true">
      <div class="theme-dialog">
        <div class="theme-options">
          <div
            v-for="theme in themeOptions"
            :key="theme.value"
            class="theme-option"
            :class="{ active: currentTheme === theme.value }"
            @click="selectTheme(theme.value)"
          >
            <div class="theme-preview" :class="theme.previewClass">
              <div class="preview-header"></div>
              <div class="preview-body">
                <div class="preview-sidebar"></div>
                <div class="preview-content"></div>
              </div>
            </div>
            <div class="theme-info">
              <div class="theme-name">{{ theme.label }}</div>
              <div class="theme-desc">{{ theme.desc }}</div>
            </div>
            <el-icon v-if="currentTheme === theme.value" class="theme-check">
              <CircleCheck />
            </el-icon>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="themeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTheme">确定</el-button>
      </template>
    </el-dialog>

    <!-- 快捷键提示对话框 -->
    <el-dialog v-model="shortcutsDialogVisible" title="快捷键指南" width="500px" :append-to-body="true">
      <div class="shortcuts-dialog">
        <div class="shortcut-section">
          <div class="section-title">消息操作</div>
          <div class="shortcut-item">
            <span class="keys"><kbd>Enter</kbd></span>
            <span class="desc">发送消息</span>
          </div>
          <div class="shortcut-item">
            <span class="keys"><kbd>Ctrl</kbd> + <kbd>Enter</kbd></span>
            <span class="desc">换行</span>
          </div>
          <div class="shortcut-item">
            <span class="keys"><kbd>Ctrl</kbd> + <kbd>K</kbd></span>
            <span class="desc">全局搜索</span>
          </div>
          <div class="shortcut-item">
            <span class="keys"><kbd>Ctrl</kbd> + <kbd>S</kbd></span>
            <span class="desc">保存草稿</span>
          </div>
        </div>
        <div class="shortcut-section">
          <div class="section-title">导航操作</div>
          <div class="shortcut-item">
            <span class="keys"><kbd>Ctrl</kbd> + <kbd>1</kbd> ~ <kbd>8</kbd></span>
            <span class="desc">切换模块</span>
          </div>
          <div class="shortcut-item">
            <span class="keys"><kbd>Ctrl</kbd> + <kbd>Tab</kbd></span>
            <span class="desc">下一个会话</span>
          </div>
          <div class="shortcut-item">
            <span class="keys"><kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>Tab</kbd></span>
            <span class="desc">上一个会话</span>
          </div>
        </div>
        <div class="shortcut-section">
          <div class="section-title">聊天操作</div>
          <div class="shortcut-item">
            <span class="keys"><kbd>@</kbd></span>
            <span class="desc">提及成员</span>
          </div>
          <div class="shortcut-item">
            <span class="keys"><kbd>:</kbd></span>
            <span class="desc">输入表情</span>
          </div>
          <div class="shortcut-item">
            <span class="keys"><kbd>Esc</kbd></span>
            <span class="desc">关闭弹窗</span>
          </div>
          <div class="shortcut-item">
            <span class="keys"><kbd>↑</kbd> / <kbd>↓</kbd></span>
            <span class="desc">选择建议</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="shortcutsDialogVisible = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 消息通知设置对话框 -->
    <el-dialog v-model="notificationSettingsVisible" title="消息通知设置" width="500px" :append-to-body="true">
      <div class="notification-settings">
        <div class="setting-group">
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-label">新消息声音</div>
              <div class="setting-desc">收到消息时播放提示音</div>
            </div>
            <el-switch v-model="notificationSettings.sound" />
          </div>
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-label">桌面通知</div>
              <div class="setting-desc">在桌面显示消息通知</div>
            </div>
            <el-switch v-model="notificationSettings.desktop" />
          </div>
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-label">消息已读回执</div>
              <div class="setting-desc">自动发送已读回执</div>
            </div>
            <el-switch v-model="notificationSettings.readReceipt" />
          </div>
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-label">群消息@提醒</div>
              <div class="setting-desc">仅在@我时通知</div>
            </div>
            <el-switch v-model="notificationSettings.mentionOnly" />
          </div>
        </div>
        <div class="setting-group">
          <div class="group-title">免打扰时段</div>
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-label">开启免打扰</div>
              <div class="setting-desc">在指定时段内不收到通知</div>
            </div>
            <el-switch v-model="notificationSettings.dndEnabled" />
          </div>
          <div v-if="notificationSettings.dndEnabled" class="dnd-time-settings">
            <el-time-picker
              v-model="notificationSettings.dndStart"
              placeholder="开始时间"
              format="HH:mm"
              value-format="HH:mm"
            />
            <span class="time-separator">至</span>
            <el-time-picker
              v-model="notificationSettings.dndEnd"
              placeholder="结束时间"
              format="HH:mm"
              value-format="HH:mm"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="notificationSettingsVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNotificationSettings">保存</el-button>
      </template>
    </el-dialog>

    <!-- 文件分享对话框 -->
    <el-dialog v-model="shareFileDialogVisible" title="分享文件" width="500px">
      <div v-if="sharingFile" class="share-file-dialog">
        <div class="share-file-info">
          <div class="file-icon-large">
            <el-icon :size="48">
              <component :is="getFileIcon(sharingFile.type)" />
            </el-icon>
          </div>
          <div class="file-details">
            <div class="file-name">{{ sharingFile.name }}</div>
            <div class="file-meta">{{ formatFileSize(sharingFile.size) }}</div>
          </div>
        </div>

        <el-tabs v-model="shareTab">
          <el-tab-pane label="复制链接" name="link">
            <div class="share-link-section">
              <el-input
                v-model="shareLink"
                readonly
                :suffix-icon="DocumentCopy"
                @click="copyShareLink"
              >
                <template #append>
                  <el-button @click="copyShareLink">复制</el-button>
                </template>
              </el-input>
              <div class="share-options">
                <el-checkbox v-model="shareOptions.password">设置密码</el-checkbox>
                <el-checkbox v-model="shareOptions.expire">设置过期时间</el-checkbox>
              </div>
              <div v-if="shareOptions.password" class="share-password-input">
                <el-input
                  v-model="shareOptions.passwordValue"
                  placeholder="请输入分享密码"
                  maxlength="6"
                />
              </div>
              <div v-if="shareOptions.expire" class="share-expire-input">
                <el-select v-model="shareOptions.expireDays" placeholder="选择有效期">
                  <el-option label="1天" :value="1" />
                  <el-option label="7天" :value="7" />
                  <el-option label="30天" :value="30" />
                </el-select>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="发送给好友" name="friend">
            <div class="share-friend-section">
              <el-input
                v-model="friendSearchKeyword"
                placeholder="搜索好友..."
                :prefix-icon="Search"
                clearable
              />
              <div class="friend-list">
                <div
                  v-for="friend in filteredFriendsForShare"
                  :key="friend.id"
                  class="friend-item"
                  @click="sendToFile(friend)"
                >
                  <el-avatar :size="36" :src="friend.avatar">
                    {{ (friend.name || friend.nickname)?.charAt(0) || 'U' }}
                  </el-avatar>
                  <span class="friend-name">{{ friend.name || friend.nickname }}</span>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 大文件上传进度对话框 -->
    <el-dialog
      v-model="uploadProgressDialogVisible"
      title="上传文件"
      width="450px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="upload-progress-dialog">
        <div v-for="upload in uploadProgressList" :key="upload.id" class="upload-item">
          <div class="upload-file-info">
            <el-icon class="file-icon"><Document /></el-icon>
            <div class="upload-file-details">
              <div class="upload-file-name">{{ upload.file.name }}</div>
              <div class="upload-file-size">{{ formatFileSize(upload.file.size) }}</div>
            </div>
          </div>
          <el-progress :percentage="upload.percent" :status="upload.status" />
          <div v-if="upload.status === 'success'" class="upload-success">上传成功</div>
          <div v-else-if="upload.status === 'exception'" class="upload-error">上传失败，请重试</div>
        </div>
      </div>
      <template #footer>
        <el-button :disabled="allUploadsComplete" @click="cancelAllUploads">取消全部</el-button>
        <el-button type="primary" :disabled="!allUploadsComplete" @click="closeUploadDialog"
          >关闭</el-button
        >
      </template>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      :title="previewingFile?.name || '文件预览'"
      width="800px"
      @close="previewingFile = null"
    >
      <div v-if="previewingFile" class="file-preview-dialog">
        <!-- 图片预览 -->
        <div v-if="previewingFile.type === 'image'" class="image-preview">
          <el-image
            :src="getLocalFilePreviewUrl(previewingFile)"
            fit="contain"
            style="width: 100%; max-height: 500px"
            :preview-src-list="[getLocalFilePreviewUrl(previewingFile)]"
          />
        </div>
        <!-- 其他文件类型显示 -->
        <div v-else class="file-preview-info">
          <div class="preview-icon">
            <el-icon :size="80">
              <component :is="getFileIcon(previewingFile.type)" />
            </el-icon>
          </div>
          <div class="preview-details">
            <h3>{{ previewingFile.name }}</h3>
            <p>大小: {{ formatFileSize(previewingFile.size) }}</p>
            <p>类型: {{ previewingFile.type }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" :icon="Download" @click="downloadSingleFile(previewingFile)">
          下载
        </el-button>
      </template>
    </el-dialog>

    <!-- 发起聊天对话框 -->
    <el-dialog v-model="startChatDialogVisible" title="发起聊天" width="500px">
      <div class="start-chat-dialog">
        <el-input
          v-model="memberSearchKeyword"
          placeholder="搜索好友..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
        <div class="friend-list">
          <div
            v-for="friend in filteredUsers"
            :key="friend.id"
            class="friend-item"
            @click="startPrivateChat(friend)"
          >
            <el-avatar :size="40" :src="friend.avatar">
              {{ (friend.name || friend.nickname || 'U').charAt(0) }}
            </el-avatar>
            <div class="friend-info">
              <div class="friend-name">{{ friend.name || friend.nickname }}</div>
              <div class="friend-dept">{{ friend.deptName || '' }}</div>
            </div>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Microphone,
  VideoCamera,
  ChatDotRound,
  Setting,
  Plus,
  Close,
  More,
  Paperclip,
  Picture,
  PictureFilled,
  Phone,
  VideoPlay,
  User,
  CircleCheck,
  Warning,
  CircleClose,
  Refresh,
  Share,
  Download,
  Upload,
  UploadFilled,
  View,
  Hide,
  Lock,
  Unlock,
  Bell,
  Mute,
  Grid,
  Document,
  Files,
  Notification,
  Odometer,
  Calendar,
  Edit,
  ChatLineSquare,
  ArrowDown,
  ArrowRight,
  ArrowLeft,
  SwitchButton,
  Folder,
  FolderOpened,
  Clock,
  Moon,
  QuestionFilled,
  InfoFilled,
  Sunny,
  Delete,
  Search,
  DocumentCopy,
  Promotion,
  Message,
  OfficeBuilding,
  Tickets,
  Star,
  DeleteFilled,
  Key,
  Minus,
} from '@element-plus/icons-vue'
import { formatTime as formatTimeUtil } from '@/utils/format/time'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { useVoiceRecorder } from '@/utils/audio/useVoiceRecorder'
import { getCurrentUserInfo } from '@/utils/im-user'
import { listSession } from '@/api/im/session'
import {
  uploadFile,
  uploadImage,
  downloadFile as apiDownloadFile,
  getFilePreviewUrl,
  getFilePreviewInfo
} from '@/api/im/file'
import {
  sendMessage as apiSendMessage,
  addMessageReaction,
  removeMessageReaction,
  getMessageReactions as apiGetMessageReactions,
  markMessageRead,
} from '@/api/im/message'
import {
  listContact,
  addContact,
  searchContacts,
  getReceivedFriendRequests,
  handleFriendRequest as apiHandleFriendRequest,
  searchUsers,
} from '@/api/im/contact'
import { getDepartmentTree, getDepartmentMembers } from '@/api/im/organization'
import { listGroup } from '@/api/im/group'
import NotificationPanel from '@/components/Notification/NotificationPanel.vue'
import SystemSettings from '@/views/settings/index.vue'

const router = useRouter()
const route = useRoute()
const store = useStore()

// WebSocket
const {
  isConnected,
  connect,
  disconnect,
  sendStatusChange: wsSendStatusChange,
} = useImWebSocket()

// 状态
const isNavCollapsed = ref(false)
const activeModule = ref('chat')
const sessionSearch = ref('')
const globalSearchKeyword = ref('')
const inputMessage = ref('')
const notificationCount = ref(0)

// 在线状态
const isUserOnline = ref(true)
const currentOnlineStatus = ref(localStorage.getItem('onlineStatus') || 'online') // online, busy, offline

// 消息区域引用
const messageAreaRef = ref(null)

// 文件上传
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const uploading = ref(false)

// 语音录制
const {
  isRecording,
  duration,
  volume,
  recordResult,
  error: recordError,
  isSupported: voiceSupported,
  formattedDuration,
  progress,
  canSend,
  startRecording,
  stopRecording,
  cancelRecording,
  clearResult,
  getVolumeBars,
} = useVoiceRecorder({
  maxDuration: 60,
  minDuration: 1,
  onRecordComplete: async result => {
    await sendVoiceMessage(result)
  },
})

// ==================== 联系人模块 ====================
const contactCategory = ref('friends')
const contactSearch = ref('')
const selectedContact = ref(null)
// 使用 Vuex store 中的联系人（已包含去重逻辑）
const friends = computed(() => store.state.im.contacts || [])
const groups = ref([]) // 群组列表
const groupSessions = ref([]) // 群组会话列表
const orgTree = ref([])
const orgMembers = ref([])
const currentDept = ref(null)
const friendRequests = ref([])
const newFriendsSearchResult = ref([]) // 新朋友搜索结果
const activeLetter = ref('') // 当前激活的字母索引

// A-Z 索引字母
const indexLetters = [
  'A',
  'B',
  'C',
  'D',
  'E',
  'F',
  'G',
  'H',
  'I',
  'J',
  'K',
  'L',
  'M',
  'N',
  'O',
  'P',
  'Q',
  'R',
  'S',
  'T',
  'U',
  'V',
  'W',
  'X',
  'Y',
  'Z',
  '#',
]

// 获取拼音首字母（简化版，实际项目建议使用pinyin库）
const getFirstLetter = str => {
  if (!str) return '#'
  const char = str.charAt(0).toUpperCase()

  // 检查是否是英文字母
  if (/[A-Z]/.test(char)) return char

  // 中文转拼音首字母（常用字映射）
  const pinyinMap = {
    阿: 'A',
    艾: 'A',
    安: 'A',
    八: 'B',
    白: 'B',
    包: 'B',
    贝: 'B',
    毕: 'B',
    博: 'B',
    布: 'B',
    蔡: 'C',
    曹: 'C',
    柴: 'C',
    陈: 'C',
    成: 'C',
    程: 'C',
    崔: 'C',
    达: 'D',
    戴: 'D',
    邓: 'D',
    狄: 'D',
    丁: 'D',
    董: 'D',
    杜: 'D',
    段: 'D',
    恩: 'E',
    发: 'F',
    范: 'F',
    方: 'F',
    费: 'F',
    冯: 'F',
    傅: 'F',
    付: 'F',
    高: 'G',
    葛: 'G',
    耿: 'G',
    龚: 'G',
    古: 'G',
    谷: 'G',
    关: 'G',
    郭: 'G',
    哈: 'H',
    海: 'H',
    韩: 'H',
    郝: 'H',
    何: 'H',
    贺: 'H',
    洪: 'H',
    侯: 'H',
    胡: 'H',
    华: 'H',
    黄: 'H',
    姬: 'J',
    季: 'J',
    简: 'J',
    江: 'J',
    姜: 'J',
    蒋: 'J',
    焦: 'J',
    金: 'J',
    晋: 'J',
    荆: 'J',
    康: 'K',
    柯: 'K',
    孔: 'K',
    库: 'K',
    匡: 'K',
    赖: 'L',
    兰: 'L',
    郎: 'L',
    乐: 'L',
    雷: 'L',
    黎: 'L',
    李: 'L',
    梁: 'L',
    廖: 'L',
    林: 'L',
    刘: 'L',
    柳: 'L',
    龙: 'L',
    卢: 'L',
    鲁: 'L',
    陆: 'L',
    吕: 'L',
    罗: 'L',
    马: 'M',
    麦: 'M',
    毛: 'M',
    梅: 'M',
    孟: 'M',
    莫: 'M',
    穆: 'M',
    那: 'N',
    南: 'N',
    倪: 'N',
    牛: 'N',
    钮: 'N',
    欧: 'O',
    潘: 'P',
    庞: 'P',
    裴: 'P',
    彭: 'P',
    皮: 'P',
    蒲: 'P',
    浦: 'P',
    戚: 'Q',
    祁: 'Q',
    齐: 'Q',
    钱: 'Q',
    乔: 'Q',
    秦: 'Q',
    邱: 'Q',
    屈: 'Q',
    瞿: 'Q',
    冉: 'R',
    饶: 'R',
    任: 'R',
    荣: 'R',
    容: 'R',
    茹: 'R',
    阮: 'R',
    桑: 'S',
    沙: 'S',
    商: 'S',
    尚: 'S',
    邵: 'S',
    施: 'S',
    石: 'S',
    时: 'S',
    史: 'S',
    舒: 'S',
    宋: 'S',
    苏: 'S',
    孙: 'S',
    谭: 'T',
    汤: 'T',
    唐: 'T',
    陶: 'T',
    滕: 'T',
    田: 'T',
    童: 'T',
    万: 'W',
    王: 'W',
    汪: 'W',
    魏: 'W',
    文: 'W',
    翁: 'W',
    沃: 'W',
    吴: 'W',
    武: 'W',
    西: 'X',
    夏: 'X',
    向: 'X',
    项: 'X',
    萧: 'X',
    谢: 'X',
    辛: 'X',
    邢: 'X',
    徐: 'X',
    许: 'X',
    薛: 'X',
    严: 'Y',
    颜: 'Y',
    晏: 'Y',
    燕: 'Y',
    杨: 'Y',
    姚: 'Y',
    叶: 'Y',
    易: 'Y',
    殷: 'Y',
    尹: 'Y',
    于: 'Y',
    余: 'Y',
    袁: 'Y',
    岳: 'Y',
    云: 'Y',
    詹: 'Z',
    张: 'Z',
    章: 'Z',
    赵: 'Z',
    甄: 'Z',
    郑: 'Z',
    钟: 'Z',
    周: 'Z',
    朱: 'Z',
    诸: 'Z',
    庄: 'Z',
    邹: 'Z',
    祖: 'Z',
    技: 'J',
    公: 'G',
  }

  return pinyinMap[char] || '#'
}

// 按字母分组的好友列表
const groupedFriends = computed(() => {
  const groups = {}

  // 先获取所有可用字母
  indexLetters.forEach(letter => {
    groups[letter] = []
  })

  // 对好友进行分组
  friends.value.forEach(friend => {
    const letter = getFirstLetter(friend.name || friend.nickname)
    if (!groups[letter]) groups[letter] = []
    groups[letter].push(friend)
  })

  // 过滤掉空分组并排序
  const result = []
  indexLetters.forEach(letter => {
    if (groups[letter].length > 0) {
      result.push({
        letter,
        contacts: groups[letter].sort((a, b) => {
          const nameA = a.name || a.nickname || ''
          const nameB = b.name || b.nickname || ''
          return nameA.localeCompare(nameB, 'zh-CN')
        }),
      })
    }
  })

  return result
})

// 可用的字母索引（有联系人的）
const availableLetters = computed(() => {
  return groupedFriends.value.map(g => g.letter)
})

// 按字母分组的群组列表
const groupedGroups = computed(() => {
  const groupMap = {}
  indexLetters.forEach(letter => {
    groupMap[letter] = []
  })

  groups.value.forEach(group => {
    const letter = getFirstLetter(group.name || group.groupName)
    if (!groupMap[letter]) groupMap[letter] = []
    groupMap[letter].push(group)
  })

  const result = []
  indexLetters.forEach(letter => {
    if (groupMap[letter].length > 0) {
      result.push({
        letter,
        contacts: groupMap[letter].sort((a, b) => {
          const nameA = a.name || a.groupName || ''
          const nameB = b.name || b.groupName || ''
          return nameA.localeCompare(nameB, 'zh-CN')
        }),
      })
    }
  })

  return result
})

// 联系人分类
const contactCategories = computed(() => [
  { key: 'friends', label: '我的好友', icon: User, count: friends.value.length },
  { key: 'groups', label: '我的群组', icon: ChatDotRound, count: groupSessions.value.length },
  { key: 'org', label: '组织架构', icon: Folder, count: 0 },
  {
    key: 'new',
    label: '新朋友',
    icon: Bell,
    count: friendRequests.value.filter(r => r.status === 'pending').length,
  },
])

// 搜索过滤后的分组联系人
const searchedGroups = computed(() => {
  if (!contactSearch.value) {
    return contactCategory.value === 'friends' ? groupedFriends.value : groupedGroups.value
  }

  const keyword = contactSearch.value.toLowerCase()
  const sourceList = contactCategory.value === 'friends' ? friends.value : groups.value

  const filtered = sourceList.filter(item => {
    const name = (item.name || item.groupName || item.nickname)?.toLowerCase() || ''
    return name.includes(keyword)
  })

  // 将搜索结果重新分组
  const groupMap = {}
  indexLetters.forEach(letter => {
    groupMap[letter] = []
  })

  filtered.forEach(item => {
    const letter = getFirstLetter(item.name || item.groupName || item.nickname)
    if (!groupMap[letter]) groupMap[letter] = []
    groupMap[letter].push(item)
  })

  const result = []
  indexLetters.forEach(letter => {
    if (groupMap[letter].length > 0) {
      result.push({
        letter,
        contacts: groupMap[letter],
      })
    }
  })

  return result
})

// 过滤好友列表（保留用于兼容）
const filteredFriends = computed(() => {
  return friends.value || []
})

// 加载好友列表 - 使用 Vuex store 统一管理
// 后端已在 getFriendList 中去重，Vuex store 也在 SET_CONTACTS 中去重
const loadFriends = async () => {
  try {
    await store.dispatch('im/loadContacts')
    console.log('[加载好友] 已通过 Vuex store 加载，去重已在后端和 store 中完成')
  } catch (error) {
    console.error('加载好友列表失败:', error)
  }
}

// 加载群组列表
const loadGroups = async () => {
  try {
    const res = await listGroup({ pageSize: 100 })
    const dataRows = res.rows || res.data || []
    groups.value = Array.isArray(dataRows)
      ? dataRows.map(g => ({
          id: g.id || g.groupId,
          name: g.name || g.groupName,
          avatar: g.avatar || g.groupAvatar,
          memberCount: g.memberCount || g.userCount || 0,
          description: g.description,
          ownerId: g.ownerId || g.creatorId,
        }))
      : []
    console.log(`[加载群组] 已加载 ${groups.value.length} 个群组`)
  } catch (error) {
    console.error('加载群组列表失败:', error)
    groups.value = []
  }
}

// 搜索新朋友（根据用户名或手机号搜索用户）
const searchNewFriends = async (keyword) => {
  if (!keyword || keyword.trim() === '') {
    newFriendsSearchResult.value = []
    return
  }
  try {
    const res = await searchUsers(keyword.trim())
    const dataRows = res.rows || res.data || []
    newFriendsSearchResult.value = Array.isArray(dataRows)
      ? dataRows.map(u => ({
          id: u.userId || u.id,
          name: u.username || u.name,
          nickname: u.nickname,
          avatar: u.avatar,
          mobile: u.mobile || u.phone,
          email: u.email,
          deptName: u.deptName || u.departmentName,
          position: u.position,
        }))
      : []
    console.log(`[搜索新朋友] 关键词"${keyword}"找到 ${newFriendsSearchResult.value.length} 个用户`)
  } catch (error) {
    console.error('搜索新朋友失败:', error)
    newFriendsSearchResult.value = []
  }
}

// 处理新朋友搜索输入事件
const handleNewFriendsSearch = (keyword) => {
  searchNewFriends(keyword)
}

// 处理添加好友操作
const handleAddFriend = async (user) => {
  try {
    await ElMessageBox.prompt('请输入验证消息', '添加好友', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      inputValue: '您好，我想添加您为好友',
      inputPlaceholder: '请输入验证消息',
    })
      .then(async ({ value }) => {
        const reason = value || '您好，我想添加您为好友'
        await addContact({
          userId: user.id,
          reason,
        })
        ElMessage.success('好友申请已发送，等待对方确认')
        // 清空搜索结果
        newFriendsSearchResult.value = []
        contactSearch.value = ''
      })
      .catch(() => {
        // 用户取消
      })
  } catch (error) {
    console.error('添加好友失败:', error)
    ElMessage.error('添加好友失败: ' + (error.message || '未知错误'))
  }
}

// 加载好友请求
const loadFriendRequests = async () => {
  try {
    const res = await getReceivedFriendRequests()
    const dataRows = res.rows || res.data?.rows || res.data || []
    friendRequests.value = Array.isArray(dataRows) ? dataRows : []
  } catch (error) {
    console.error('加载好友请求失败:', error)
    friendRequests.value = []
  }
}

// 初始化组织架构树
const initOrgTree = async () => {
  try {
    const res = await getDepartmentTree()
    const dataRows = res.data || res.rows || []
    // 兼容后端返回结构，确保每个节点有 name、id、children 字段
    const normalize = nodes => {
      if (!Array.isArray(nodes)) return []
      return nodes.map(n => {
        const child = normalize(n.children || n.childrenTree || [])
        return {
          id: n.id,
          name: n.name || n.deptName || '',
          userCount: n.userCount || n.count || 0,
          children: child
        }
      })
    }
    orgTree.value = normalize(dataRows)
  } catch (error) {
    console.error('加载组织架构失败:', error)
    orgTree.value = []
  }
}

// 组织架构节点点击
const handleOrgNodeClick = async data => {
  currentDept.value = data
  // 获取部门成员
  if (data.id) {
    try {
      const res = await getDepartmentMembers(data.id)
      const dataRows = res.data || res.rows || []
      orgMembers.value = Array.isArray(dataRows)
        ? dataRows.map(member => ({
            id: member.userId,
            name: member.username,
            nickname: member.nickname,
            avatar: member.avatar,
            position: member.position,
            deptName: data.name,
            online: member.online || false,
            isFriend: member.isFriend || false,
          }))
        : []
    } catch (error) {
      console.error('加载部门成员失败:', error)
      orgMembers.value = []
    }
  } else {
    orgMembers.value = []
  }
}

// 选择联系人
const selectContact = contact => {
  selectedContact.value = { ...contact }
}

// 滚动到指定字母
const scrollToLetter = letter => {
  activeLetter.value = letter
  const element = document.getElementById(`contact-section-${letter}`)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
  // 300ms后清除激活状态
  setTimeout(() => {
    activeLetter.value = ''
  }, 300)
}

// 选择群组（从联系人列表）
const selectGroupContact = group => {
  // 切换到聊天模块并打开该群组
  store.dispatch('im/switchSession', group.id)
  activeModule.value = 'chat'
  currentSessionId.value = group.id
  router.push(`/im/chat/${group.id}`)
}

// 开始聊天
const startChat = async contact => {
  await store.dispatch('im/switchToContact', contact)
  activeModule.value = 'chat'
  currentSessionId.value = store.state.im.currentSession?.id
  router.push('/im/chat')
}

// 视频通话
const startVideoCall = contact => {
  ElMessage.info(`正在发起视频通话: ${contact.name || contact.nickname}`)
}

// 发送好友请求
const sendFriendRequest = async user => {
  try {
    await ElMessageBox.prompt('请输入验证消息', '添加好友', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      inputValue: '我是' + currentUser.value?.name,
    })
    const { value } = await ElMessageBox.prompt('请输入验证消息', '添加好友', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      inputValue: '我是' + (currentUser.value?.name || '我'),
    })

    await addContact({
      friendId: user.id,
      message: value,
    })

    ElMessage.success('好友请求已发送')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 处理好友请求
const handleFriendRequest = async (request, action) => {
  try {
    await apiHandleFriendRequest({
      requestId: request.id,
      action: action, // accept or reject
    })

    request.status = action === 'accept' ? 'accepted' : 'rejected'

    if (action === 'accept') {
      // 重新加载好友列表
      await loadFriends()
    }

    ElMessage.success(action === 'accept' ? '已添加好友' : '已拒绝请求')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 显示添加好友对话框
const showAddFriendDialog = () => {
  ElMessageBox.prompt('请输入用户名或手机号', '添加好友', {
    confirmButtonText: '搜索',
    cancelButtonText: '取消',
  })
    .then(async ({ value }) => {
      if (!value) return
      // TODO: 调用搜索用户API
      ElMessage.info('搜索用户功能开发中...')
    })
    .catch(() => {})
}

// ==================== 工作台模块 ====================
const workbenchCategory = ref('all')
const approvalTab = ref('pending')

// 工作台分类
const workbenchCategories = computed(() => [
  { key: 'all', label: '全部应用', icon: Grid, count: 0 },
  { key: 'approval', label: '待办审批', icon: Document, count: 5 },
  { key: 'attendance', label: '考勤打卡', icon: Odometer, count: 0 },
  { key: 'schedule', label: '日程管理', icon: Calendar, count: 3 },
])

// 模拟审批数据
const mockApprovals = ref([
  {
    id: 1,
    title: '采购申请 - 办公用品',
    type: '采购审批',
    applicant: '张三',
    time: Date.now() - 3600000,
    icon: Document,
    color: '#1677ff',
  },
  {
    id: 2,
    title: '请假申请 - 年假',
    type: '请假审批',
    applicant: '李四',
    time: Date.now() - 7200000,
    icon: Edit,
    color: '#52c41a',
  },
  {
    id: 3,
    title: '报销申请 - 差旅费',
    type: '报销审批',
    applicant: '王五',
    time: Date.now() - 86400000,
    icon: Files,
    color: '#faad14',
  },
  {
    id: 4,
    title: '用印申请 - 合同盖章',
    type: '用印审批',
    applicant: '赵六',
    time: Date.now() - 172800000,
    icon: Notification,
    color: '#eb2f96',
  },
  {
    id: 5,
    title: '物品领用 - 笔记本电脑',
    type: '领用审批',
    applicant: '钱七',
    time: Date.now() - 259200000,
    icon: Odometer,
    color: '#13c2c2',
  },
])

// 考勤相关
const currentTime = ref('')
const currentDate = ref('')
const hasCheckedIn = ref(false)
const checkInTime = ref('')

// 本周打卡记录
const weeklyRecords = ref([
  { day: '周一', date: '2024-01-08', checkInTime: '08:55', status: 'normal', statusText: '正常' },
  { day: '周二', date: '2024-01-09', checkInTime: '09:02', status: 'late', statusText: '迟到' },
  { day: '周三', date: '2024-01-10', checkInTime: '08:58', status: 'normal', statusText: '正常' },
  { day: '周四', date: '2024-01-11', checkInTime: '08:45', status: 'normal', statusText: '正常' },
  { day: '周五', date: '2024-01-12', checkInTime: '-', status: 'pending', statusText: '未打卡' },
])

// 日程相关
const selectedDate = ref(new Date().toISOString().split('T')[0])
const currentWeekStart = ref(getWeekStart(new Date()))

const weekDays = ref(getWeekDays(new Date()))
const schedules = ref([
  {
    id: 1,
    title: '周会',
    date: getWeekDays(new Date())[0].date,
    time: '09:00',
    location: '会议室A',
    color: '#1677ff',
  },
  {
    id: 2,
    title: '产品评审',
    date: getWeekDays(new Date())[1].date,
    time: '14:00',
    location: '会议室B',
    color: '#52c41a',
  },
  {
    id: 3,
    title: '技术分享',
    date: getWeekDays(new Date())[2].date,
    time: '16:00',
    location: '线上',
    color: '#faad14',
  },
  {
    id: 4,
    title: '项目讨论',
    date: getWeekDays(new Date())[3].date,
    time: '10:00',
    location: '会议室C',
    color: '#722ed1',
  },
  {
    id: 5,
    title: '需求评审',
    date: getWeekDays(new Date())[4].date,
    time: '15:00',
    location: '会议室A',
    color: '#eb2f96',
  },
])

// 获取本周日期范围
function getWeekStart(date) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  return new Date(d.setDate(diff))
}

// 获取本周7天
function getWeekDays(date) {
  const weekStart = getWeekStart(date)
  const days = []
  const today = new Date().toISOString().split('T')[0]
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

  for (let i = 0; i < 7; i++) {
    const d = new Date(weekStart)
    d.setDate(d.getDate() + i)
    const dateStr = d.toISOString().split('T')[0]
    days.push({
      name: dayNames[i],
      date: dateStr,
      dayNum: d.getDate(),
      isToday: dateStr === today,
    })
  }
  return days
}

// 计算属性
const currentWeekRange = computed(() => {
  const days = weekDays.value
  if (days.length === 0) return ''
  const start = days[0].date
  const end = days[6].date
  return `${start.substring(5)} ~ ${end.substring(5)}`
})

const selectedDateSchedules = computed(() => {
  return schedules.value.filter(s => s.date === selectedDate.value)
})

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  })
}

// 审批操作
const handleApproval = async (item, action) => {
  try {
    const actionText = action === 'approve' ? '同意' : '拒绝'
    await ElMessageBox.confirm(`确定要${actionText}该申请吗？`, '确认', {
      type: 'warning',
    })

    // 从列表中移除
    const index = mockApprovals.value.findIndex(a => a.id === item.id)
    if (index > -1) {
      mockApprovals.value.splice(index, 1)
    }

    ElMessage.success(`已${actionText}该申请`)
  } catch {
    // 用户取消
  }
}

// 打卡操作
const handleCheckIn = () => {
  const now = new Date()
  hasCheckedIn.value = true
  checkInTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  ElMessage.success('打卡成功！')
}

// 日程操作
const prevWeek = () => {
  const newStart = new Date(currentWeekStart.value)
  newStart.setDate(newStart.getDate() - 7)
  currentWeekStart.value = newStart
  weekDays.value = getWeekDays(newStart)
}

const nextWeek = () => {
  const newStart = new Date(currentWeekStart.value)
  newStart.setDate(newStart.getDate() + 7)
  currentWeekStart.value = newStart
  weekDays.value = getWeekDays(newStart)
}

const selectDate = date => {
  selectedDate.value = date
}

const showAddSchedule = () => {
  ElMessage.info('新建日程功能开发中...')
}

// 启动时钟
let clockInterval = null

onMounted(() => {
  updateTime()
  clockInterval = setInterval(updateTime, 1000)

  // 全局快捷键支持
  const handleGlobalKeydown = (e) => {
    // Ctrl+K: 全局搜索
    if (e.ctrlKey && e.key === 'k') {
      e.preventDefault()
      const searchInput = document.querySelector('.global-search-input input')
      if (searchInput) {
        searchInput.focus()
        searchInput.select()
      }
      return
    }

    // Ctrl+N: 发起新聊天
    if (e.ctrlKey && e.key === 'n') {
      e.preventDefault()
      showStartChatDialog()
      return
    }

    // Escape: 关闭所有弹窗
    if (e.key === 'Escape') {
      if (showEmojiPicker.value) showEmojiPicker.value = false
      if (forwardDialogVisible.value) forwardDialogVisible.value = false
      if (profileDialogVisible.value) profileDialogVisible.value = false
      if (systemSettingsVisible.value) systemSettingsVisible.value = false
      return
    }
  }

  document.addEventListener('keydown', handleGlobalKeydown)

  // 保存清理函数
  onUnmounted(() => {
    document.removeEventListener('keydown', handleGlobalKeydown)
  })
})

onUnmounted(() => {
  if (clockInterval) {
    clearInterval(clockInterval)
  }
})

// ==================== 钉盘模块 ====================
const driveCategory = ref('personal')
const selectedFile = ref(null)
const breadcrumbs = ref([])
const currentFolderId = ref(null)

// 钉盘搜索和批量选择
const driveSearchKeyword = ref('')
const selectedFiles = ref([])
const batchSelectMode = ref(false)
const isDraggingFile = ref(false)

// 文件预览对话框
const previewDialogVisible = ref(false)
const previewingFile = ref(null)

// 钉盘分类
const driveCategories = computed(() => [
  { key: 'personal', label: '个人文件', icon: User, count: allFiles.value.length },
  { key: 'shared', label: '企业共享', icon: Grid, count: 0 },
  { key: 'department', label: '部门文件', icon: Grid, count: 0 },
  { key: 'favorite', label: '我的收藏', icon: Star, count: 0 },
])

// 存储空间
const storageUsed = ref('1.2 GB')
const storageTotal = ref('10 GB')
const storagePercent = computed(() => {
  const used = parseFloat(storageUsed.value)
  const total = parseFloat(storageTotal.value)
  return Math.round((used / total) * 100)
})

// 文件分享相关
const shareFileDialogVisible = ref(false)
const sharingFile = ref(null)
const shareTab = ref('link')
const shareLink = ref('https://im.example.com/share/')
const friendSearchKeyword = ref('')
const shareOptions = ref({
  password: false,
  passwordValue: '',
  expire: false,
  expireDays: 7,
})

// 过滤好友列表用于分享
const filteredFriendsForShare = computed(() => {
  if (!friendSearchKeyword.value) return friends.value
  return friends.value.filter(f =>
    (f.name || f.nickname)?.toLowerCase().includes(friendSearchKeyword.value.toLowerCase())
  )
})

// 大文件上传进度
const uploadProgressDialogVisible = ref(false)
const uploadProgressList = ref([])
const allUploadsComplete = computed(() => {
  return (
    uploadProgressList.value.length > 0 &&
    uploadProgressList.value.every(u => u.status === 'success' || u.status === 'exception')
  )
})

// 模拟文件和文件夹数据
const allFiles = ref([
  {
    id: 'f1',
    name: '项目文档.docx',
    type: 'word',
    size: 245760,
    updateTime: Date.now() - 86400000,
    folderId: null,
  },
  {
    id: 'f2',
    name: '产品需求.pdf',
    type: 'pdf',
    size: 524288,
    updateTime: Date.now() - 172800000,
    folderId: null,
  },
  {
    id: 'f3',
    name: '设计稿.png',
    type: 'image',
    size: 2097152,
    updateTime: Date.now() - 259200000,
    folderId: null,
  },
  {
    id: 'f4',
    name: '会议记录.xlsx',
    type: 'excel',
    size: 102400,
    updateTime: Date.now() - 345600000,
    folderId: null,
  },
  {
    id: 'f5',
    name: '演示文稿.pptx',
    type: 'ppt',
    size: 5242880,
    updateTime: Date.now() - 432000000,
    folderId: null,
  },
])

const allFolders = ref([
  { id: 'fol1', name: '工作文档', itemCount: 5, parentId: null },
  { id: 'fol2', name: '图片资料', itemCount: 12, parentId: null },
  { id: 'fol3', name: '项目文件', itemCount: 8, parentId: null },
  { id: 'fol4', name: '子文件夹', itemCount: 3, parentId: 'fol1' },
])

// 文件夹树形数据
const folderTreeData = computed(() => {
  const buildTree = (parentId = null) => {
    return allFolders.value
      .filter(f => f.parentId === parentId)
      .map(folder => ({
        ...folder,
        children: buildTree(folder.id)
      }))
  }
  return buildTree()
})

// 当前文件夹和文件
const currentFolders = computed(() => {
  return allFolders.value.filter(f => f.parentId === currentFolderId.value)
})

const currentFiles = computed(() => {
  return allFiles.value.filter(f => f.folderId === currentFolderId.value)
})

// 显示的文件列表（支持搜索）
const displayFiles = computed(() => {
  if (!driveSearchKeyword.value) {
    return currentFiles.value
  }
  const keyword = driveSearchKeyword.value.toLowerCase()
  return allFiles.value.filter(f =>
    f.name.toLowerCase().includes(keyword)
  )
})

// 获取文件图标
const getFileIcon = type => {
  const iconMap = {
    word: Document,
    excel: Document,
    ppt: Document,
    pdf: Document,
    image: PictureFilled,
    video: VideoCamera,
    audio: Microphone,
    archive: Folder,
    default: Document,
  }
  return iconMap[type] || iconMap.default
}

// 导航到根目录
const navigateToRoot = () => {
  currentFolderId.value = null
  breadcrumbs.value = []
  selectedFile.value = null
  selectedFiles.value = []
}

// 选择文件/文件夹
const selectFile = (file, event) => {
  // 如果按住Ctrl或Shift，进入批量选择模式
  if (event && (event.ctrlKey || event.shiftKey)) {
    batchSelectMode.value = true
    toggleFileSelection(file)
  } else {
    // 如果不是批量模式，清空之前的选中
    if (!batchSelectMode.value) {
      selectedFiles.value = []
    }
    selectedFile.value = file
  }
}

// 判断文件是否被选中
const isSelected = file => {
  return selectedFile.value?.id === file.id
}

// 判断文件是否在批量选择中
const isInBatchSelection = file => {
  return selectedFiles.value.some(f => f.id === file.id)
}

// 切换文件选择状态
const toggleFileSelection = file => {
  const index = selectedFiles.value.findIndex(f => f.id === file.id)
  if (index > -1) {
    selectedFiles.value.splice(index, 1)
  } else {
    selectedFiles.value.push(file)
  }
  if (selectedFiles.value.length === 0) {
    batchSelectMode.value = false
  }
}

// 清除选择
const clearSelection = () => {
  selectedFiles.value = []
  selectedFile.value = null
  batchSelectMode.value = false
}

// 搜索处理
const handleDriveSearch = () => {
  // 搜索结果通过 displayFiles 计算属性自动更新
}

// 文件夹树点击处理
const handleFolderClick = data => {
  currentFolderId.value = data.id
  // 构建面包屑路径
  buildBreadcrumbs(data.id)
  selectedFile.value = null
}

// 构建面包屑路径
const buildBreadcrumbs = folderId => {
  const path = []
  let current = allFolders.value.find(f => f.id === folderId)
  while (current) {
    path.unshift({ id: current.id, name: current.name })
    current = allFolders.value.find(f => f.id === current.parentId)
  }
  breadcrumbs.value = path
}

// 批量操作处理
const handleBatchAction = command => {
  switch (command) {
    case 'download':
      batchDownload()
      break
    case 'delete':
      batchDelete()
      break
    case 'move':
      // TODO: 实现移动功能
      ElMessage.info('移动功能开发中')
      break
  }
}

// 批量下载
const batchDownload = () => {
  ElMessage.info(`开始下载 ${selectedFiles.value.length} 个文件`)
  // TODO: 实现批量下载
}

// 批量删除
const batchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedFiles.value.length} 项吗？`, '确认删除', {
    type: 'warning',
  })
    .then(() => {
      selectedFiles.value.forEach(item => {
        if (item.id.startsWith('fol')) {
          const index = allFolders.value.findIndex(f => f.id === item.id)
          if (index > -1) allFolders.value.splice(index, 1)
        } else {
          const index = allFiles.value.findIndex(f => f.id === item.id)
          if (index > -1) allFiles.value.splice(index, 1)
        }
      })
      clearSelection()
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

// 文件预览
const previewFile = file => {
  previewingFile.value = file
  previewDialogVisible.value = true
}

// 文件右键菜单
const showFileContextMenu = (event, file) => {
  // TODO: 实现右键菜单
  event.preventDefault()
}

// 拖拽进入处理
const handleDragEnter = event => {
  event.preventDefault()
  isDraggingFile.value = true
}

// 拖拽离开处理
const handleDragLeave = event => {
  event.preventDefault()
  // 确保是从文件列表区域离开，而不是进入子元素
  if (event.target.classList.contains('file-list')) {
    isDraggingFile.value = false
  }
}

// 拖拽上传处理
const handleFileDrop = event => {
  isDraggingFile.value = false
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    Array.from(files).forEach(file => {
      handleFileUpload(file)
    })
  }
}

// 打开文件夹
const openFolder = folder => {
  currentFolderId.value = folder.id
  breadcrumbs.value.push({ id: folder.id, name: folder.name })
  selectedFile.value = null
  selectedFiles.value = []
}

// 打开文件
const openFile = async file => {
  if (!file) return

  // 如果是图片文件，直接打开预览对话框
  if (isImageFile(file)) {
    previewingFile.value = file
    filePreviewDialogVisible.value = true
    return
  }

  // 尝试获取文件预览信息
  try {
    const response = await getFilePreviewInfo(file.id)
    if (response.code === 200 && response.data) {
      const previewInfo = response.data
      // 设置预览文件信息
      previewingFile.value = {
        ...file,
        previewUrl: previewInfo.url,
        previewType: previewInfo.type
      }
      filePreviewDialogVisible.value = true
    } else {
      // 无法预览，提示下载
      ElMessageBox.confirm(
        `'${file.name}' 暂不支持在线预览，是否下载？`,
        '提示',
        {
          confirmButtonText: '下载',
          cancelButtonText: '取消',
          type: 'info',
        }
      ).then(() => {
        downloadSingleFile(file)
      }).catch(() => {})
    }
  } catch (error) {
    console.error('获取文件预览信息失败:', error)
    // API 调用失败，提示下载
    ElMessageBox.confirm(
      `'${file.name}' 暂不支持在线预览，是否下载？`,
      '提示',
      {
        confirmButtonText: '下载',
        cancelButtonText: '取消',
        type: 'info',
      }
    ).then(() => {
      downloadSingleFile(file)
    }).catch(() => {})
  }
}

// 上传文件
const handleFileUpload = async file => {
  // 大文件显示上传进度
  if (file.size > 10 * 1024 * 1024) {
    // 大于10MB
    return handleLargeFileUpload(file)
  }

  const newFile = {
    id: `f_${Date.now()}`,
    name: file.name,
    type: getFileType(file.name),
    size: file.size,
    updateTime: Date.now(),
    folderId: currentFolderId.value,
  }
  allFiles.value.push(newFile)
  ElMessage.success('文件上传成功')
  return false // 阻止自动上传
}

// 处理大文件上传
const handleLargeFileUpload = file => {
  const uploadId = `upload_${Date.now()}`
  const uploadItem = {
    id: uploadId,
    file: file,
    percent: 0,
    status: '',
  }

  uploadProgressList.value.push(uploadItem)
  uploadProgressDialogVisible.value = true

  // 模拟上传进度
  let progress = 0
  const interval = setInterval(() => {
    progress += Math.random() * 15
    if (progress >= 100) {
      progress = 100
      clearInterval(interval)
      uploadItem.percent = 100
      uploadItem.status = 'success'

      // 添加到文件列表
      const newFile = {
        id: `f_${Date.now()}`,
        name: file.name,
        type: getFileType(file.name),
        size: file.size,
        updateTime: Date.now(),
        folderId: currentFolderId.value,
      }
      allFiles.value.push(newFile)
      ElMessage.success('文件上传成功')
    } else {
      uploadItem.percent = Math.round(progress)
    }
  }, 300)

  return false
}

// 分享文件
const shareFile = file => {
  sharingFile.value = file
  shareLink.value = `https://im.example.com/share/${file.id}`
  shareFileDialogVisible.value = true
}

// 复制分享链接
const copyShareLink = () => {
  navigator.clipboard
    .writeText(shareLink.value)
    .then(() => {
      ElMessage.success('链接已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败，请手动复制')
    })
}

// 发送文件给好友
const sendToFile = friend => {
  ElMessage.success(`已将"${sharingFile.value?.name}"发送给${friend.name || friend.nickname}`)
  shareFileDialogVisible.value = false
}

// 取消所有上传
const cancelAllUploads = () => {
  uploadProgressList.value = []
  uploadProgressDialogVisible.value = false
}

// 关闭上传对话框
const closeUploadDialog = () => {
  uploadProgressList.value = []
  uploadProgressDialogVisible.value = false
}

// 获取文件类型
const getFileType = fileName => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  const typeMap = {
    doc: 'word',
    docx: 'word',
    xls: 'excel',
    xlsx: 'excel',
    ppt: 'ppt',
    pptx: 'ppt',
    pdf: 'pdf',
    jpg: 'image',
    jpeg: 'image',
    png: 'image',
    gif: 'image',
    mp4: 'video',
    avi: 'video',
    mp3: 'audio',
    wav: 'audio',
    zip: 'archive',
    rar: 'archive',
    '7z': 'archive',
  }
  return typeMap[ext] || 'default'
}

// 新建文件夹
const createFolder = () => {
  ElMessageBox.prompt('请输入文件夹名称', '新建文件夹', {
    confirmButtonText: '创建',
    cancelButtonText: '取消',
    inputValue: '新建文件夹',
  })
    .then(({ value }) => {
      if (!value) return
      const newFolder = {
        id: `fol_${Date.now()}`,
        name: value,
        itemCount: 0,
        parentId: currentFolderId.value,
      }
      allFolders.value.push(newFolder)
      ElMessage.success('文件夹创建成功')
    })
    .catch(() => {})
}

// 删除文件
const deleteFile = () => {
  if (!selectedFile.value) return

  ElMessageBox.confirm('确定要删除所选文件吗？', '确认删除', {
    type: 'warning',
  })
    .then(() => {
      if (selectedFile.value.id.startsWith('fol')) {
        // 删除文件夹
        const index = allFolders.value.findIndex(f => f.id === selectedFile.value.id)
        if (index > -1) allFolders.value.splice(index, 1)
      } else {
        // 删除文件
        const index = allFiles.value.findIndex(f => f.id === selectedFile.value.id)
        if (index > -1) allFiles.value.splice(index, 1)
      }
      selectedFile.value = null
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

// 下载单个文件
const downloadSingleFile = async file => {
  if (!file) return

  try {
    ElMessage.info(`开始下载: ${file.name}`)

    // 调用后端API下载文件
    const response = await apiDownloadFile(file.id)

    // 创建下载链接
    const blob = new Blob([response])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.name || `file_${file.id}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error(`下载失败: ${error.message || '未知错误'}`)

    // 如果API下载失败，尝试使用文件URL直接下载
    if (file.url) {
      const link = document.createElement('a')
      link.href = file.url
      link.download = file.name || `file_${file.id}`
      link.target = '_blank'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
  }
}

// 当前用户
const currentUser = computed(() => {
  return (
    getCurrentUserInfo() || {
      name: '用户',
      avatar: null,
      userId: null,
    }
  )
})

// 从 store 获取会话列表
const sessions = computed(() => store.state.im.sessions || [])

// 当前会话
const currentSessionId = computed(() => store.state.im.currentSession?.id)
const currentSession = computed(() => store.state.im.currentSession)

// 未读消息数
const unreadCount = computed(() => store.getters['im/totalUnreadCount'] || 0)

// 当前会话的消息列表
const messages = computed(() => {
  if (!currentSessionId.value) return []
  return store.getters['im/messagesBySession'](currentSessionId.value) || []
})

// 导航模块
const navModules = ref([
  { key: 'chat', label: '消息', icon: ChatLineSquare },
  { key: 'contacts', label: '联系人', icon: User },
  { key: 'workbench', label: '工作台', icon: Grid },
  { key: 'drive', label: '钉盘', icon: Folder },
  { key: 'approval', label: '审批', icon: Tickets },
  { key: 'ding', label: 'DING', icon: Promotion },
  { key: 'email', label: '邮箱', icon: Message },
  { key: 'app-center', label: '应用', icon: OfficeBuilding },
])

// 部门数据
const departments = ref([
  { id: 1, name: '技术部' },
  { id: 2, name: '产品部' },
  { id: 3, name: '市场部' },
  { id: 4, name: '人事部' },
])

// 工作台应用
const workbenchApps = ref([
  { key: 'approval', name: '审批', icon: Document, color: '#1677ff' },
  { key: 'attendance', name: '考勤打卡', icon: Odometer, color: '#52c41a' },
  { key: 'drive', name: '钉盘', icon: Files, color: '#faad14' },
  { key: 'calendar', name: '日程', icon: Calendar, color: '#722ed1' },
  { key: 'announcement', name: '公告', icon: Notification, color: '#eb2f96' },
  { key: 'meeting', name: '会议', icon: VideoCamera, color: '#13c2c2' },
  { key: 'mail', name: '邮件', icon: Edit, color: '#fa8c16' },
  { key: 'report', name: '汇报', icon: Document, color: '#1890ff' },
  { key: 'hr', name: '人事管理', icon: User, color: '#f5222d' },
  { key: 'finance', name: '财务', icon: Document, color: '#52c41a' },
])

// 方法
const getModuleTitle = key => {
  const titles = {
    chat: '消息',
    contacts: '联系人',
    workbench: '工作台',
    drive: '钉盘',
    approval: '审批',
    ding: 'DING',
    email: '邮箱',
    'app-center': '应用中心',
  }
  return titles[key] || ''
}

const toggleNavCollapse = () => {
  isNavCollapsed.value = !isNavCollapsed.value
  localStorage.setItem('navCollapsed', String(isNavCollapsed.value))
}

const switchModule = moduleKey => {
  activeModule.value = moduleKey
  // 根据模块类型决定是否进行路由跳转
  // 联系人、钉盘、审批、DING、邮箱、应用中心模块使用独立页面路由
  const routedModules = ['contacts', 'drive', 'approval', 'ding', 'email', 'app-center']
  if (routedModules.includes(moduleKey)) {
    // 钉盘模块跳转到文件管理页面
    if (moduleKey === 'drive') {
      router.push('/im/file')
    } else if (moduleKey === 'app-center') {
      router.push('/im/app-center')
    } else {
      router.push(`/im/${moduleKey}`)
    }
  } else {
    // 消息和工作台是内嵌模块，只更新URL状态
    window.history.replaceState(null, '', `/im/${moduleKey}`)
  }
}

const selectSession = async session => {
  // 切换会话
  await store.dispatch('im/switchSession', session)
  // 标记会话为已读（使用新接口）
  if (session.id) {
    // 简化：直接传递0，表示已读所有消息
    markMessageRead({
      conversationId: session.id,
      lastReadMessageId: 0
    }).catch(err => {
      console.warn('标记已读失败:', err)
    })
  }
  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 滚动到消息底部
const scrollToBottom = () => {
  if (messageAreaRef.value) {
    messageAreaRef.value.scrollTop = messageAreaRef.value.scrollHeight
  }
}

// 监听消息变化，自动滚动到底部
watch(
  messages,
  () => {
    nextTick(() => {
      scrollToBottom()
    })
  },
  { deep: true }
)

const handleEnter = e => {
  if (!e.shiftKey) {
    sendMessage()
  }
}

// 触发文件选择
const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

// 选择图片
const selectImage = () => {
  imageInputRef.value?.click()
}

// 处理文件选择
const handleFileSelect = async event => {
  const file = event.target.files?.[0]
  if (!file || !currentSessionId.value) return

  await uploadAndSendFile(file, 'file')
  event.target.value = ''
}

// 处理图片选择
const handleImageSelect = async event => {
  const file = event.target.files?.[0]
  if (!file || !currentSessionId.value) return

  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }

  await uploadAndSendFile(file, 'image')
  event.target.value = ''
}

// 上传并发送文件
const uploadAndSendFile = async (file, type) => {
  if (uploading.value) {
    ElMessage.warning('正在上传中，请稍候...')
    return
  }

  uploading.value = true

  try {
    // 调用上传接口
    const uploadApi = type === 'image' ? uploadImage : uploadFile
    const response = await uploadApi(file, progressEvent => {
      const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      // 可以在这里更新上传进度 UI
    })

    const fileUrl = response.data?.url || response.data?.fileUrl || response.url

    if (!fileUrl) {
      throw new Error('上传失败：未获取到文件地址')
    }

    // 发送文件消息
    const messageData = {
      sessionId: currentSessionId.value,
      type: type,
      content: fileUrl,
      fileName: file.name,
      fileSize: file.size,
    }

    // 统一通过 store 发送消息（内部使用 WebSocket）
    await store.dispatch('im/sendMessage', messageData)

    ElMessage.success(type === 'image' ? '图片发送成功' : '文件发送成功')
  } catch (error) {
    ElMessage.error('发送失败：' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

// 发送语音消息
const sendVoiceMessage = async result => {
  if (!currentSessionId.value || !result.blob) {
    ElMessage.error('发送语音失败')
    clearResult()
    return
  }

  uploading.value = true

  try {
    // 上传语音文件
    const formData = new FormData()
    formData.append('file', result.blob, `voice_${Date.now()}.wav`)

    const response = await uploadFile(result.blob)

    const voiceUrl = response.data?.url || response.data?.fileUrl || response.url

    if (!voiceUrl) {
      throw new Error('上传失败：未获取到语音文件地址')
    }

    // 发送语音消息
    const messageData = {
      sessionId: currentSessionId.value,
      type: 'voice',
      content: voiceUrl,
      duration: result.duration,
    }

    // 统一通过 store 发送消息（内部使用 WebSocket）
    await store.dispatch('im/sendMessage', messageData)

    ElMessage.success('语音发送成功')
  } catch (error) {
    ElMessage.error('发送失败：' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
    clearResult()
  }
}

// 开始录制语音
const startVoiceRecord = async () => {
  if (!voiceSupported.value) {
    ElMessage.warning('当前浏览器不支持语音录制')
    return
  }

  if (!currentSessionId.value) {
    ElMessage.warning('请先选择会话')
    return
  }

  await startRecording()
}

// 停止录制语音
const stopVoiceRecord = () => {
  stopRecording()
}

// 下载文件
const downloadFile = async msg => {
  if (!msg.content) return

  try {
    const link = document.createElement('a')
    link.href = msg.content
    link.download = msg.fileName || 'download'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 重发消息
const resendMessage = async msg => {
  if (msg.status !== 'failed') return

  try {
    await store.dispatch('im/resendMessage', msg)
  } catch (error) {
    ElMessage.error('重发失败')
  }
}

// 格式化文件大小
const formatFileSize = bytes => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

const formatTime = formatTimeUtil

// 获取本地文件预览URL
const getLocalFilePreviewUrl = file => {
  if (file.url) return file.url
  // 临时使用占位图，实际应该调用后端API获取预览URL
  return ''
}

// 判断是否为图片文件
const isImageFile = file => {
  return file.type === 'image' || ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(file.type)
}

// 打开应用
const openApp = appKey => {
  // 已实现面板的应用：切换到对应的工作台分类
  const panelApps = {
    approval: 'approval',
    attendance: 'attendance',
    calendar: 'schedule',
  }

  if (panelApps[appKey]) {
    workbenchCategory.value = panelApps[appKey]
  } else {
    // 其他应用暂未实现，显示提示
    ElMessage.info(`${workbenchApps.value.find(app => app.key === appKey)?.name || appKey}功能开发中`)
  }
}

const showSettings = () => {
  systemSettingsVisible.value = true
}

const toggleNotification = () => {
  notificationVisible.value = !notificationVisible.value
}

// ==================== 新增功能变量 ====================
// 表情选择器
const showEmojiPicker = ref(false)
const activeEmojiTab = ref('smile')
const emojiTabs = [
  { key: 'smile', icon: '😊' },
  { key: 'people', icon: '👋' },
  { key: 'animals', icon: '🐱' },
  { key: 'food', icon: '🍔' },
  { key: 'activities', icon: '⚽' },
  { key: 'travel', icon: '🚗' },
  { key: 'objects', icon: '💡' },
  { key: 'symbols', icon: '❤️' },
]
const emojis = {
  smile: [
    '😀',
    '😃',
    '😄',
    '😁',
    '😅',
    '😂',
    '🤣',
    '😊',
    '😇',
    '🙂',
    '😉',
    '😌',
    '😍',
    '🥰',
    '😘',
    '😗',
    '😙',
    '😚',
    '😋',
    '😛',
    '😜',
    '🤪',
    '😝',
    '🤗',
    '🤭',
    '🤔',
    '🤐',
    '🤨',
    '😐',
    '😑',
    '😶',
    '😏',
    '😒',
    '🙄',
    '😬',
    '🤥',
    '😌',
    '😔',
    '😪',
    '🤤',
    '😴',
    '😷',
    '🤒',
    '🤕',
    '🤢',
    '🤮',
    '🤧',
    '🥵',
    '🥶',
    '😶‍🌫️',
    '🥴',
    '😵',
    '🤯',
    '🤠',
    '🥳',
    '🥸',
    '😎',
    '🤓',
    '🧐',
  ],
  people: [
    '👋',
    '🤚',
    '🖐️',
    '✋',
    '🖖',
    '👌',
    '🤌',
    '🤏',
    '✌️',
    '🤞',
    '🤟',
    '🤘',
    '🤙',
    '👈',
    '👉',
    '👆',
    '👇',
    '☝️',
    '👍',
    '👎',
    '✊',
    '👊',
    '🤛',
    '🤜',
    '👏',
    '🙌',
    '👐',
    '🤲',
    '🤝',
    '🙏',
    '✍️',
    '💪',
    '🦾',
    '🦿',
    '🦵',
    '🦶',
    '👂',
    '🦻',
    '👃',
    '🧠',
    '🫀',
    '🫁',
    '🦷',
    '🦴',
    '👀',
    '👁️',
    '👅',
    '👄',
  ],
  animals: [
    '🐱',
    '🐶',
    '🐭',
    '🐹',
    '🐰',
    '🦊',
    '🐻',
    '🐼',
    '🐨',
    '🐯',
    '🦁',
    '🐮',
    '🐷',
    '🐸',
    '🐵',
    '🙈',
    '🙉',
    '🙊',
    '🐒',
    '🐔',
    '🐧',
    '🐦',
    '🐤',
    '🐣',
    '🐥',
    '🦆',
    '🦅',
    '🦉',
    '🦇',
    '🐺',
    '🐗',
    '🐴',
    '🦄',
    '🐝',
    '🐛',
    '🦋',
    '🐌',
    '🐞',
    '🐜',
    '🦟',
    '🦗',
    '🕷️',
    '🦂',
    '🐢',
    '🐍',
    '🦎',
    '🦖',
    '🦕',
    '🐙',
    '🦑',
    '🦐',
    '🦞',
    '🦀',
    '🐡',
    '🐠',
    '🐟',
    '🐬',
    '🐳',
    '🐋',
  ],
  food: [
    '🍔',
    '🍟',
    '🍕',
    '🌭',
    '🥪',
    '🌮',
    '🌯',
    '🥙',
    '🧆',
    '🥚',
    '🍳',
    '🥘',
    '🍲',
    '🥣',
    '🥗',
    '🍿',
    '🧈',
    '🧂',
    '🥫',
    '🍱',
    '🍘',
    '🍙',
    '🍚',
    '🍛',
    '🍜',
    '🍝',
    '🍠',
    '🍢',
    '🍣',
    '🍤',
    '🍥',
    '🥮',
    '🍡',
    '🥟',
    '🥠',
    '🥡',
    '🦀',
    '🦞',
    '🦐',
    '🦑',
    '🦪',
    '🍦',
    '🍧',
    '🍨',
    '🍩',
    '🍪',
    '🎂',
    '🍰',
    '🧁',
    '🥧',
    '🍫',
    '🍬',
    '🍭',
    '🍮',
    '🍯',
    '🍼',
    '🥛',
    '☕',
    '🍵',
    '🍶',
  ],
  activities: [
    '⚽',
    '🏀',
    '🏈',
    '⚾',
    '🥎',
    '🎾',
    '🏐',
    '🏉',
    '🥏',
    '🎱',
    '🪀',
    '🏓',
    '🏸',
    '🏒',
    '🏑',
    '🥍',
    '🏏',
    '🥅',
    '⛳',
    '🪁',
    '🏹',
    '🎣',
    '🤿',
    '🥊',
    '🥋',
    '🎽',
    '🛹',
    '🛼',
    '🛷',
    '⛸️',
    '🥌',
    '🎿',
    '⛷️',
    '🏂',
    '🪂',
    '🏋️',
    '🤼',
    '🤸',
    '🤾',
    '🏌️',
    '🏇',
    '🧘',
    '🏊',
    '🤽',
    '🚣',
    '🧗',
    '🚴',
    '🚵',
  ],
  travel: [
    '🚗',
    '🚕',
    '🚙',
    '🚌',
    '🚎',
    '🏎️',
    '🚓',
    '🚑',
    '🚒',
    '🚐',
    '🛻',
    '🚚',
    '🚛',
    '🚜',
    '🦯',
    '🦽',
    '🦼',
    '🛴',
    '🚲',
    '🛵',
    '🏍️',
    '🛺',
    '🚨',
    '🚔',
    '🚍',
    '🚘',
    '🚖',
    '🚡',
    '🚠',
    '🚟',
    '🚃',
    '🚋',
    '🚞',
    '🚝',
    '🚄',
    '🚅',
    '🚈',
    '🚂',
    '🚆',
    '🚇',
    '🚊',
    '🚉',
    '✈️',
    '🛫',
    '🛬',
    '🛩️',
    '💺',
    '🛰️',
    '🚀',
    '🛸',
    '🚁',
    '🛶',
    '⛵',
    '🚤',
    '🛥️',
    '🛳️',
    '⛴️',
    '⚓',
    '⛽',
    '🚧',
    '🚦',
    '🚥',
    '🚏',
  ],
  objects: [
    '💡',
    '🔦',
    '🏮',
    '📱',
    '💻',
    '🖥️',
    '🖨️',
    '⌨️',
    '🖱️',
    '🖲️',
    '💽',
    '💾',
    '💿',
    '📀',
    '🧮',
    '🎥',
    '📷',
    '📸',
    '📹',
    '📼',
    '🔍',
    '🔎',
    '🕯️',
    '💰',
    '💳',
    '💎',
    '⚖️',
    '🔧',
    '🔨',
    '⚒️',
    '🛠️',
    '⛏️',
    '🔩',
    '⚙️',
    '🧲',
    '🔫',
    '💣',
    '🔪',
    '🗡️',
    '⚔️',
    '🛡️',
    '🚬',
    '⚰️',
    '🏺',
    '🔮',
    '📿',
    '🧿',
    '💈',
    '⚗️',
    '🔭',
    '🔬',
    '🕳️',
    '🩹',
    '🩺',
    '💊',
    '💉',
    '🩸',
    '🧬',
    '🦠',
    '🧫',
    '🧪',
  ],
  symbols: [
    '❤️',
    '🧡',
    '💛',
    '💚',
    '💙',
    '💜',
    '🖤',
    '🤍',
    '🤎',
    '💔',
    '❣️',
    '💕',
    '💞',
    '💓',
    '💗',
    '💖',
    '💘',
    '💝',
    '💟',
    '☮️',
    '✝️',
    '☪️',
    '🕉️',
    '☸️',
    '✡️',
    '🔯',
    '🕎',
    '☯️',
    '☦️',
    '🛐',
    '⛎',
    '♈',
    '♉',
    '♊',
    '♋',
    '♌',
    '♍',
    '♎',
    '♏',
    '♐',
    '♑',
    '♒',
    '♓',
    '🆔',
    '⚛️',
    '🉑',
    '☢️',
    '☣️',
    '📴',
    '📳',
    '🈶',
    '🈚',
    '🈸',
    '🈺',
    '🈷️',
    '✴️',
    '🆚',
    '💮',
    '🉐',
    '㊙️',
    '㊗️',
    '🈴',
    '🈵',
    '🈲',
    '🅰️',
    '🅱️',
  ],
}
const currentEmojis = computed(() => emojis[activeEmojiTab.value] || emojis.smile)

// @提及
const showMentionSuggestions = ref(false)
const mentionSuggestions = ref([])
const mentionStartIndex = ref(-1)

// 引用回复
const replyingMessage = ref(null)

// 消息右键菜单
const messageMenuVisible = ref(false)
const messageMenuPosition = ref({ x: 0, y: 0 })
const selectedMessage = ref(null)

// 转发消息
const forwardDialogVisible = ref(false)
const forwardSearchKeyword = ref('')
const selectedForwardTarget = ref(null)
const forwardTargets = ref([])

// 表情反应
const reactionPickerVisible = ref(false)
const reactionPickerPosition = ref({ x: 0, y: 0 })
const quickReactions = [
  { key: 'like', emoji: '👍', name: '赞' },
  { key: 'heart', emoji: '❤️', name: '喜欢' },
  { key: 'laugh', emoji: '😄', name: '哈哈' },
  { key: 'surprise', emoji: '😮', name: '惊讶' },
  { key: 'cry', emoji: '😭', name: '哭泣' },
  { key: 'angry', emoji: '😡', name: '生气' },
]
const messageReactions = ref(new Map()) // 存储每条消息的反应

// 文件预览
const filePreviewDialogVisible = ref(false)
const previewingFileContent = ref(null)

const systemSettingsVisible = ref(false)

// 主题设置
const themeSettingsVisible = ref(false)
const settingsTab = ref('interface')
const isDarkMode = ref(localStorage.getItem('darkMode') === 'true')
const messageSoundEnabled = ref(localStorage.getItem('messageSoundEnabled') !== 'false')
const desktopNotificationEnabled = ref(
  localStorage.getItem('desktopNotificationEnabled') !== 'false'
)
const fontSize = ref(localStorage.getItem('fontSize') || 'medium')
const showOnlineStatus = ref(localStorage.getItem('showOnlineStatus') !== 'false')
const allowStrangerFind = ref(localStorage.getItem('allowStrangerFind') !== 'false')
const allowStrangerMessage = ref(localStorage.getItem('allowStrangerMessage') !== 'false')
const twoFactorAuth = ref(localStorage.getItem('twoFactorAuth') === 'true')
const messageRetention = ref(parseInt(localStorage.getItem('messageRetention') || '30'))
const autoCleanReadMessages = ref(localStorage.getItem('autoCleanReadMessages') === 'true')

// 保存设置
const saveSettings = () => {
  localStorage.setItem('darkMode', String(isDarkMode.value))
  localStorage.setItem('messageSoundEnabled', String(messageSoundEnabled.value))
  localStorage.setItem('desktopNotificationEnabled', String(desktopNotificationEnabled.value))
  localStorage.setItem('fontSize', fontSize.value)
  localStorage.setItem('showOnlineStatus', String(showOnlineStatus.value))
  localStorage.setItem('allowStrangerFind', String(allowStrangerFind.value))
  localStorage.setItem('allowStrangerMessage', String(allowStrangerMessage.value))
  localStorage.setItem('twoFactorAuth', String(twoFactorAuth.value))
  localStorage.setItem('messageRetention', String(messageRetention.value))
  localStorage.setItem('autoCleanReadMessages', String(autoCleanReadMessages.value))
  ElMessage.success('设置保存成功')
  themeSettingsVisible.value = false
}

const handleSettingsSave = () => {
  ElMessage.success('系统设置已保存')
}

// 显示设备管理
const showDeviceManagement = () => {
  ElMessage.info('设备管理功能开发中')
}

// 显示修改密码
const showChangePassword = () => {
  ElMessage.info('修改密码功能开发中')
}

// 通知面板
const notificationVisible = ref(false)

// 个人资料对话框
const profileDialogVisible = ref(false)

// 个人资料表单
const profileForm = ref({
  nickname: '',
  signature: '',
  phone: '',
  email: '',
})

// 在线状态对话框
const onlineStatusDialogVisible = ref(false)

// 在线状态选项
const onlineStatusOptions = [
  {
    value: 'online',
    label: '在线',
    desc: '我可以接收消息',
    icon: CircleCheck,
    iconClass: 'status-online',
  },
  {
    value: 'busy',
    label: '忙碌',
    desc: '暂时无法回复',
    icon: Clock,
    iconClass: 'status-busy',
  },
  {
    value: 'away',
    label: '离开',
    desc: '暂时离开',
    icon: Moon,
    iconClass: 'status-away',
  },
  {
    value: 'offline',
    label: '隐身',
    desc: '不显示在线状态',
    icon: Hide,
    iconClass: 'status-offline',
  },
]

// 显示在线状态对话框
const showOnlineStatusDialog = () => {
  onlineStatusDialogVisible.value = true
}

// 选择在线状态
const selectOnlineStatus = status => {
  currentOnlineStatus.value = status
}

// 保存在线状态
const saveOnlineStatus = () => {
  localStorage.setItem('onlineStatus', currentOnlineStatus.value)

  // 通过WebSocket广播状态变更
  if (isConnected.value) {
    wsSendStatusChange(currentOnlineStatus.value)
  }

  ElMessage.success('在线状态已更新')
  onlineStatusDialogVisible.value = false
}

// 主题切换对话框
const themeDialogVisible = ref(false)
const currentTheme = ref(localStorage.getItem('theme') || 'light')

// 主题选项
const themeOptions = [
  {
    value: 'light',
    label: '浅色主题',
    desc: '清爽明亮的界面风格',
    previewClass: 'theme-light',
  },
  {
    value: 'dark',
    label: '深色主题',
    desc: '护眼的深色界面风格',
    previewClass: 'theme-dark',
  },
  {
    value: 'auto',
    label: '自动切换',
    desc: '根据系统设置自动切换',
    previewClass: 'theme-auto',
  },
]

// 显示主题对话框
const showThemeDialog = () => {
  themeDialogVisible.value = true
}

// 选择主题
const selectTheme = theme => {
  currentTheme.value = theme
}

// 保存主题
const saveTheme = () => {
  localStorage.setItem('theme', currentTheme.value)

  if (currentTheme.value === 'dark') {
    isDarkMode.value = true
    document.documentElement.classList.add('dark')
  } else if (currentTheme.value === 'light') {
    isDarkMode.value = false
    document.documentElement.classList.remove('dark')
  } else {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    isDarkMode.value = prefersDark
    document.documentElement.classList.toggle('dark', prefersDark)
  }

  ElMessage.success('主题已更新')
  themeDialogVisible.value = false
}

// 保存个人资料
const saveProfile = () => {
  ElMessage.success('个人资料保存成功')
  profileDialogVisible.value = false
  // 更新本地存储的用户信息
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  userInfo.nickName = profileForm.value.nickname
  localStorage.setItem('userInfo', JSON.stringify(userInfo))
}

// 输入框引用
const inputRef = ref(null)

// ==================== 新增功能函数 ====================

// 表情相关
const insertEmoji = emoji => {
  const input = inputRef.value?.$el?.querySelector('textarea')
  if (!input) return

  const start = input.selectionStart || input.value.length
  const end = input.selectionEnd || input.value.length
  const text = input.value

  input.value = text.substring(0, start) + emoji + text.substring(end)
  inputMessage.value = input.value

  // 设置光标位置
  nextTick(() => {
    input.selectionStart = input.selectionEnd = start + emoji.length
    input.focus()
  })
}

// 格式化消息内容（处理文本、表情、换行）
// 注意：消息内容的解析和标准化已在store中完成，这里只负责显示格式化
const formatMessageContent = content => {
  if (!content) return ''

  // 转换为字符串并转义HTML（防止XSS）
  let formatted = String(content)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 处理@提及 - 匹配 @用户名 格式
  formatted = formatted.replace(/@([a-zA-Z0-9\u4e00-\u9fa5_]+)/g, '<span class="mention">@$1</span>')

  // 处理换行
  formatted = formatted.replace(/\n/g, '<br>')

  return formatted
}

// 格式化会话预览文本（用于会话列表）
const formatSessionPreview = (content) => {
  if (!content) return ''

  let actualContent = content
  let messageType = 'text'

  // 检查是否为JSON字符串并尝试解析
  if (typeof content === 'string') {
    try {
      const parsed = JSON.parse(content)
      if (typeof parsed === 'object' && parsed !== null) {
        actualContent = parsed.text || parsed.content || parsed.body || ''
        messageType = parsed.type || parsed.messageType || 'text'
      }
    } catch (e) {
      // 不是JSON，使用原始内容
      actualContent = content
    }
  } else if (typeof content === 'object') {
    actualContent = content.text || content.content || content.body || ''
    messageType = content.type || content.messageType || 'text'
  }

  // 根据消息类型返回预览文本
  switch (messageType) {
    case 'image':
      return '[图片]'
    case 'video':
      return '[视频]'
    case 'voice':
    case 'audio':
      return '[语音]'
    case 'file':
      return '[文件]'
    case 'location':
      return '[位置]'
    default:
      // 对于文本消息，截取前30个字符
      const text = String(actualContent).trim()
      if (text.length > 30) {
        return text.substring(0, 30) + '...'
      }
      return text || ''
  }
}

// 获取图片URL（处理解析后的消息内容）
const getImageUrl = (message) => {
  // 优先使用解析后的URL
  if (message._parsedContent) {
    if (message._parsedContent.url) return message._parsedContent.url
    if (message._parsedContent.src) return message._parsedContent.src
  }

  // 尝试解析content字段（如果content是JSON字符串）
  if (message.content && typeof message.content === 'string') {
    const trimmed = message.content.trim()
    if (trimmed.startsWith('{')) {
      try {
        const parsed = JSON.parse(message.content)
        if (parsed.url) return parsed.url
        if (parsed.src) return parsed.src
      } catch (e) {
        // 解析失败，继续
      }
    }
    // 如果content看起来像URL，直接返回
    if (trimmed.startsWith('http://') || trimmed.startsWith('https://') || trimmed.startsWith('/')) {
      return trimmed
    }
  }

  // 使用消息本身的fileUrl字段
  if (message.fileUrl) return message.fileUrl

  // 最后尝试使用content
  return message.content || ''
}

// 获取文件信息（处理解析后的消息内容）
const getFileInfo = (message) => {
  // 优先使用解析后的文件信息
  if (message._parsedContent && !message._parsedContent.isPlainText) {
    return {
      name: message._parsedContent.name || message._parsedContent.fileName || '文件',
      size: message._parsedContent.size || message._parsedContent.fileSize || 0,
      url: message._parsedContent.url || message._parsedContent.fileUrl || '',
    }
  }

  // 尝试解析content字段（如果content是JSON字符串）
  if (message.content && typeof message.content === 'string') {
    const trimmed = message.content.trim()
    if (trimmed.startsWith('{')) {
      try {
        const parsed = JSON.parse(message.content)
        return {
          name: parsed.name || parsed.fileName || '文件',
          size: parsed.size || parsed.fileSize || 0,
          url: parsed.url || parsed.fileUrl || '',
        }
      } catch (e) {
        // 解析失败，继续
      }
    }
  }

  // 使用消息本身的字段
  return {
    name: message.fileName || message.name || '文件',
    size: message.fileSize || message.size || 0,
    url: message.fileUrl || message.content || '',
  }
}

// 处理输入变化（检测@符号）
const handleInputChange = value => {
  const textarea = inputRef.value?.$el?.querySelector('textarea')
  if (!textarea) return

  const cursorPosition = textarea.selectionStart
  const textBeforeCursor = inputMessage.value.substring(0, cursorPosition)

  // 检查是否输入了@
  const lastAtIndex = textBeforeCursor.lastIndexOf('@')
  if (lastAtIndex !== -1) {
    const textAfterAt = textBeforeCursor.substring(lastAtIndex + 1)
    // 如果@后面没有空格且在文本末尾附近
    if (!textAfterAt.includes(' ') && textAfterAt.length < 20) {
      mentionStartIndex.value = lastAtIndex
      // 获取可@的成员列表
      const currentSession = store.state.im.currentSession
      if (currentSession?.type === 'group') {
        // 先添加@所有人选项（如果是群组且用户有权限）
        const suggestions = []
        // 只有群主和管理员可以@所有人
        const isOwnerOrAdmin =
          currentSession.creatorId === currentUser.value?.userId || currentSession.isAdmin === true
        if (isOwnerOrAdmin) {
          suggestions.push({ id: 'all', name: '所有人', nickname: '所有人', isAll: true })
        }
        // 添加匹配的成员
        const matchedMembers = groupMembers.value
          .filter(m => m.name?.toLowerCase().includes(textAfterAt.toLowerCase()))
          .slice(0, 5)
        suggestions.push(...matchedMembers)
        mentionSuggestions.value = suggestions.slice(0, 6)
      } else {
        mentionSuggestions.value = friends.value
          .filter(f => f.name?.toLowerCase().includes(textAfterAt.toLowerCase()))
          .slice(0, 5)
      }
      showMentionSuggestions.value = mentionSuggestions.value.length > 0
      return
    }
  }

  showMentionSuggestions.value = false
  mentionSuggestions.value = []
}

// 选择@提及
const selectMention = user => {
  const textarea = inputRef.value?.$el?.querySelector('textarea')
  if (!textarea) return

  const textBeforeMention = inputMessage.value.substring(0, mentionStartIndex.value)
  const textAfterMention = inputMessage.value.substring(textarea.selectionStart)

  // 如果是@所有人，使用特殊格式
  if (user.isAll) {
    inputMessage.value = textBeforeMention + '@所有人 ' + textAfterMention
  } else {
    inputMessage.value =
      textBeforeMention + '@' + (user.name || user.nickname) + ' ' + textAfterMention
  }

  showMentionSuggestions.value = false
  mentionSuggestions.value = []

  nextTick(() => {
    textarea.focus()
  })
}

// 处理键盘事件
const handleInputKeydown = e => {
  // Ctrl+Enter 发送
  if (e.ctrlKey && e.key === 'Enter') {
    e.preventDefault()
    sendMessage()
    return
  }

  // Enter 发送（Shift+Enter 换行）
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
    return
  }

  // ESC 关闭表情选择器
  if (e.key === 'Escape') {
    showEmojiPicker.value = false
    return
  }

  // 处理@提及上下选择
  if (showMentionSuggestions.value) {
    if (e.key === 'ArrowDown' || e.key === 'ArrowUp' || e.key === 'Enter') {
      e.preventDefault()
      // 这里可以实现键盘导航，暂时简化
    }
  }
}

// 显示消息右键菜单
const showMessageMenu = (event, message) => {
  selectedMessage.value = message
  messageMenuPosition.value = { x: event.clientX, y: event.clientY }
  messageMenuVisible.value = true

  // 点击其他地方关闭菜单
  const closeMenu = () => {
    messageMenuVisible.value = false
    document.removeEventListener('click', closeMenu)
  }
  setTimeout(() => {
    document.addEventListener('click', closeMenu)
  }, 0)
}

// 回复消息
const replyMessage = () => {
  replyingMessage.value = selectedMessage.value
  messageMenuVisible.value = false
  nextTick(() => {
    inputRef.value?.focus()
  })
}

// 取消回复
const cancelReply = () => {
  replyingMessage.value = null
}

// 转发消息
const forwardMessage = () => {
  // 收集可转发的目标（好友和群组）
  forwardTargets.value = [
    ...friends.value.map(f => ({ ...f, type: 'friend' })),
    ...groupSessions.value.map(g => ({ ...g, type: 'group' })),
  ]
  forwardDialogVisible.value = true
  messageMenuVisible.value = false
}

// 选择转发目标
const selectForwardTarget = target => {
  selectedForwardTarget.value = target
}

// 过滤转发目标
const filteredForwardTargets = computed(() => {
  if (!forwardSearchKeyword.value) return forwardTargets.value
  const keyword = forwardSearchKeyword.value.toLowerCase()
  return forwardTargets.value.filter(t => {
    const name = (t.name || t.groupName || '').toLowerCase()
    return name.includes(keyword)
  })
})

// 确认转发
const confirmForward = async () => {
  if (!selectedForwardTarget.value || !selectedMessage.value) return

  try {
    const target = selectedForwardTarget.value
    const toSessionId = target.type === 'group' ? target.id : target.sessionId
    const toUserId = target.type === 'friend' ? target.id : null

    await store.dispatch('im/forwardMessage', {
      messageId: selectedMessage.value.id,
      toSessionId,
      toUserId,
    })

    ElMessage.success('转发成功')
    forwardDialogVisible.value = false
    selectedForwardTarget.value = null
  } catch (error) {
    ElMessage.error('转发失败')
  }
}

// 复制消息
const copyMessage = () => {
  if (selectedMessage.value?.content) {
    navigator.clipboard.writeText(selectedMessage.value.content)
    ElMessage.success('已复制')
  }
  messageMenuVisible.value = false
}

// 删除消息
const deleteMessage = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      type: 'warning',
    })
    await store.dispatch('im/deleteMessage', selectedMessage.value.id)
    ElMessage.success('已删除')
  } catch {
    // 取消
  }
  messageMenuVisible.value = false
}

// 判断是否可以撤回消息
const canRecallMessage = message => {
  if (!message) return false
  // 只能撤回自己发送的消息
  const isOwnMessage = message.isOwn || message.senderId === currentUser.value?.userId
  if (!isOwnMessage) return false
  // 只能撤回2分钟内发送的消息
  const sendTime = new Date(message.sendTime || message.timestamp)
  const now = new Date()
  const twoMinutesInMs = 2 * 60 * 1000
  return now - sendTime < twoMinutesInMs
}

// 撤回消息
const recallMessage = async () => {
  if (!selectedMessage.value) return

  try {
    await ElMessageBox.confirm('确定要撤回这条消息吗？', '提示', {
      type: 'warning',
    })
    await store.dispatch('im/recallMessage', selectedMessage.value.id)
    ElMessage.success('消息已撤回')
    messageMenuVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '撤回失败')
    }
  }
}

// 显示表情反应选择器
const showReactionPicker = () => {
  if (!selectedMessage.value) return
  // 计算位置：显示在消息下方
  const rect = event?.target?.getBoundingClientRect()
  if (rect) {
    reactionPickerPosition.value = {
      x: rect.left,
      y: rect.bottom + 5,
    }
  }
  reactionPickerVisible.value = true
  messageMenuVisible.value = false

  // 点击其他地方关闭
  const closePicker = () => {
    reactionPickerVisible.value = false
    document.removeEventListener('click', closePicker)
  }
  setTimeout(() => {
    document.addEventListener('click', closePicker)
  }, 100)
}

// 判断当前用户是否已对消息反应
const isReacted = (reactionKey) => {
  if (!selectedMessage.value) return false
  const reactions = messageReactions.value.get(selectedMessage.value.id) || []
  return reactions.some(r => r.key === reactionKey && r.userId === currentUser.value?.userId)
}

// 切换表情反应
const toggleReaction = async (reactionKey) => {
  if (!selectedMessage.value) return

  const messageId = selectedMessage.value.id
  const reactions = messageReactions.value.get(messageId) || []
  const existingReaction = reactions.find(
    r => r.key === reactionKey && r.userId === currentUser.value?.userId
  )

  try {
    if (existingReaction) {
      // 取消反应
      await removeMessageReaction({ messageId, reactionKey })
      const updated = reactions.filter(r => !(r.key === reactionKey && r.userId === currentUser.value?.userId))
      messageReactions.value.set(messageId, updated)
    } else {
      // 添加反应
      await addMessageReaction({
        messageId,
        reactionType: reactionKey.toUpperCase(),
      })
      reactions.push({
        key: reactionKey,
        userId: currentUser.value?.userId,
        userName: currentUser.value?.nickname || currentUser.value?.username,
      })
      messageReactions.value.set(messageId, reactions)
    }
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
  }

  reactionPickerVisible.value = false
}

// 获取消息的反应列表
const getMessageReactions = (messageId) => {
  return messageReactions.value.get(messageId) || []
}

// 获取反应统计
const getReactionStats = (messageId) => {
  const reactions = getMessageReactions(messageId)
  const stats = {}
  reactions.forEach(r => {
    stats[r.key] = (stats[r.key] || 0) + 1
  })
  return stats
}

// 为指定消息显示表情选择器
const showReactionPickerFor = (message) => {
  selectedMessage.value = message
  // 计算位置：显示在消息反应下方
  const rect = event?.target?.getBoundingClientRect()
  if (rect) {
    reactionPickerPosition.value = {
      x: rect.left,
      y: rect.bottom + 5,
    }
  }
  reactionPickerVisible.value = true

  // 点击其他地方关闭
  const closePicker = () => {
    reactionPickerVisible.value = false
    document.removeEventListener('click', closePicker)
  }
  setTimeout(() => {
    document.addEventListener('click', closePicker)
  }, 100)
}

// 判断当前用户是否对消息进行了某个反应
const isReactedByUser = (messageId, reactionKey) => {
  const reactions = getMessageReactions(messageId)
  return reactions.some(
    r => r.key === reactionKey && r.userId === currentUser.value?.userId
  )
}

// 根据key获取emoji
const getEmojiByKey = (key) => {
  const emoji = quickReactions.find(e => e.key === key)
  return emoji ? emoji.emoji : ''
}

// 文件预览相关
const isPdfFile = file => {
  if (!file) return false
  const ext = (file.name || '').split('.').pop()?.toLowerCase()
  return ext === 'pdf'
}

// 切换深色模式
const toggleDarkMode = () => {
  localStorage.setItem('darkMode', String(isDarkMode.value))
  document.documentElement.classList.toggle('dark', isDarkMode.value)
}

// 切换桌面通知
const toggleDesktopNotification = async () => {
  if (desktopNotificationEnabled.value) {
    if ('Notification' in window) {
      const permission = await Notification.requestPermission()
      if (permission !== 'granted') {
        ElMessage.warning('浏览器通知权限被拒绝')
        desktopNotificationEnabled.value = false
      }
    }
  }
  localStorage.setItem('desktopNotificationEnabled', String(desktopNotificationEnabled.value))
}

// 显示桌面通知
const showDesktopNotification = (title, body) => {
  if (
    desktopNotificationEnabled.value &&
    'Notification' in window &&
    Notification.permission === 'granted'
  ) {
    new Notification(title, {
      body: body,
      icon: '/logo.png',
      badge: '/logo.png',
    })
  }
}

// 播放消息提示音
const playMessageSound = () => {
  if (messageSoundEnabled.value) {
    const audio = new Audio('/sounds/message.mp3')
    audio.volume = 0.3
    audio.play().catch(() => {})
  }
}

// 发送文本消息
// 注意：store.dispatch('im/sendMessage') 已经通过 WebSocket 发送，不需要额外调用 wsSendMessage
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSessionId.value) return

  const messageData = {
    sessionId: currentSessionId.value,
    type: 'text',
    content: inputMessage.value.trim(),
  }

  // 添加引用回复
  if (replyingMessage.value) {
    messageData.replyTo = replyingMessage.value.id
    replyingMessage.value = null
  }

  // 统一通过 store 发送消息（内部使用 WebSocket）
  await store.dispatch('im/sendMessage', messageData)

  inputMessage.value = ''
  showEmojiPicker.value = false
}

// 获取群组成员（用于@提及）
const groupMembers = computed(() => {
  const currentSession = store.state.im.currentSession
  if (currentSession?.type === 'group' && currentSession.members) {
    return currentSession.members
  }
  return []
})

// 初始化深色模式
if (isDarkMode.value) {
  document.documentElement.classList.add('dark')
}

// 监听设置页面的主题变更
const handleThemeChange = event => {
  const { isDark } = event.detail
  isDarkMode.value = isDark
}

// 监听系统主题偏好变化
const handleSystemThemeChange = e => {
  const theme = localStorage.getItem('theme')
  if (theme === 'auto') {
    isDarkMode.value = e.matches
    document.documentElement.classList.toggle('dark', e.matches)
  }
}

// 添加事件监听
onMounted(() => {
  window.addEventListener('themeChange', handleThemeChange)
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addEventListener('change', handleSystemThemeChange)
})

// 清理事件监听
onUnmounted(() => {
  window.removeEventListener('themeChange', handleThemeChange)
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.removeEventListener('change', handleSystemThemeChange)
})

// 监听消息播放提示音
watch(
  () => store.state.im.messages,
  (newMessages, oldMessages) => {
    if (newMessages.length > (oldMessages?.length || 0)) {
      const latestMessage = newMessages[newMessages.length - 1]
      if (!latestMessage.isOwn) {
        playMessageSound()
        showDesktopNotification('新消息', `${latestMessage.senderName}: ${latestMessage.content}`)
      }
    }
  },
  { deep: true }
)

const handleUserCommand = async command => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      // 用户取消
    }
  } else if (command === 'profile') {
    // 初始化个人资料表单 - 使用getCurrentUserInfo获取完整用户数据
    const userInfo = getCurrentUserInfo()
    if (userInfo) {
      profileForm.value = {
        nickname: userInfo.nickname || userInfo.nickName || userInfo.userName || '',
        signature: userInfo.signature || '',
        phone: userInfo.phonenumber || userInfo.phone || '',
        email: userInfo.email || '',
      }
      currentUser.value = { ...currentUser.value, ...userInfo }
    }
    profileDialogVisible.value = true
  } else if (command === 'settings') {
    // 打开个人设置对话框
    systemSettingsVisible.value = true
  } else if (command === 'status') {
    // 显示在线状态选择 - 现在在下拉菜单中直接选择
  } else if (command === 'theme') {
    // 显示主题切换对话框
    showThemeDialog()
  }
}

// 设置下拉菜单处理
const handleSettingsCommand = command => {
  if (command === 'theme') {
    showThemeDialog()
  } else if (command === 'notifications') {
    notificationSettingsVisible.value = true
  } else if (command === 'privacy') {
    ElMessage.info('隐私与安全设置功能开发中...')
  } else if (command === 'shortcuts') {
    showShortcutsDialog()
  } else if (command === 'about') {
    ElMessageBox.alert(
      '企业级即时通讯与协同办公系统 v1.0\n\n基于若依框架构建\n支持私有化部署',
      '关于',
      {
        confirmButtonText: '确定',
        type: 'info',
      }
    )
  }
}

// 帮助下拉菜单处理
const handleHelpCommand = command => {
  if (command === 'help') {
    ElMessageBox.alert(
      '1. 发送消息：在输入框中输入内容，按Enter发送\n2. 快捷键：支持Ctrl+Enter换行，Ctrl+K搜索\n3. 表情：点击表情图标选择表情\n4. 文件：点击文件图标发送文件\n5. 截图：支持粘贴截图发送',
      '使用帮助',
      {
        confirmButtonText: '我知道了',
        type: 'info',
      }
    )
  } else if (command === 'shortcuts') {
    showShortcutsDialog()
  } else if (command === 'feedback') {
    ElMessage.info('反馈建议功能开发中...')
  } else if (command === 'update') {
    ElMessage.info('当前已是最新版本')
  }
}

// 设置在线状态
const setOnlineStatus = status => {
  currentOnlineStatus.value = status
  isUserOnline.value = status === 'online'
  localStorage.setItem('onlineStatus', status)
  // 同步到服务器
  wsSendStatusChange?.(status)
  ElMessage.success(`已切换为：${getOnlineStatusDisplay(status)}`)
}

// 获取在线状态显示文本
const getOnlineStatusDisplay = status => {
  const statusMap = {
    online: '在线',
    away: '离开',
    busy: '忙碌',
    offline: '离线',
    dnd: '免打扰',
  }
  return statusMap[status] || '在线'
}

// 快捷键对话框
const shortcutsDialogVisible = ref(false)
const showShortcutsDialog = () => {
  shortcutsDialogVisible.value = true
}

// 通知设置对话框
const notificationSettingsVisible = ref(false)

// 通知设置状态
const notificationSettings = ref({
  sound: true,
  desktop: true,
  readReceipt: true,
  mentionOnly: false,
  dndEnabled: false,
  dndStart: '22:00',
  dndEnd: '08:00',
})

// 保存通知设置
const saveNotificationSettings = () => {
  localStorage.setItem('notificationSettings', JSON.stringify(notificationSettings.value))
  ElMessage.success('通知设置已保存')
  notificationSettingsVisible.value = false
}

// 初始化通知设置
onMounted(() => {
  const saved = localStorage.getItem('notificationSettings')
  if (saved) {
    try {
      notificationSettings.value = JSON.parse(saved)
    } catch {
      // 解析失败使用默认值
    }
  }
})


// 顶部导航栏方法
const showNotifications = () => {
  notificationVisible.value = true
}

const handleGlobalSearch = () => {
  if (!globalSearchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  ElMessage.info('搜索功能开发中...')
  // TODO: 实现全局搜索
}

// 聊天头部方法
const getOnlineStatusText = () => {
  if (currentSession.value?.type === 'GROUP') {
    return '群聊'
  }
  return isUserOnline.value ? '在线' : '离线'
}

const showChatProfile = () => {
  // TODO: 显示会话详情对话框
  ElMessage.info('会话详情功能开发中...')
}

const startScreenShare = () => {
  ElMessage.info('屏幕共享功能开发中...')
}

const searchInChat = () => {
  ElMessage.info('搜索聊天记录功能开发中...')
}

const muteChat = () => {
  if (currentSession.value) {
    currentSession.value.isMuted = !currentSession.value.isMuted
    ElMessage.success(currentSession.value.isMuted ? '已开启消息免打扰' : '已取消消息免打扰')
  }
}

const pinChat = () => {
  if (currentSession.value) {
    currentSession.value.isPinned = !currentSession.value.isPinned
    ElMessage.success(currentSession.value.isPinned ? '已置顶聊天' : '已取消置顶')
  }
}

const viewChatMembers = () => {
  ElMessage.info('查看成员功能开发中...')
}

const clearChatHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空聊天记录吗？此操作不可恢复。', '清空聊天记录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    // TODO: 调用API清空聊天记录
    ElMessage.success('聊天记录已清空')
  } catch {
    // 用户取消
  }
}

const exitGroup = async () => {
  try {
    await ElMessageBox.confirm('确定要退出群聊吗？', '退出群聊', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    // TODO: 调用API退出群聊
    ElMessage.success('已退出群聊')
    activeModule.value = 'chat'
  } catch {
    // 用户取消
  }
}

// 初始化 - 优化为渐进式加载，提升登录体验
const init = async () => {
  console.log('[IM初始化] 开始初始化，采用渐进式加载策略')

  // 读取折叠状态
  const savedCollapsed = localStorage.getItem('navCollapsed')
  if (savedCollapsed !== null) {
    isNavCollapsed.value = savedCollapsed === 'true'
  }

  // 根据路由或URL设置当前模块
  const pathSegments = route.path.split('/')
  const moduleFromPath = pathSegments[2]

  // 支持所有模块类型（包括内嵌的工作台和钉盘）
  const validModules = ['chat', 'contacts', 'workbench', 'drive']
  if (moduleFromPath && validModules.includes(moduleFromPath)) {
    activeModule.value = moduleFromPath
  }

  // ========== 第一阶段：立即加载（核心功能） ==========
  // 目标：快速建立连接，展示基本界面
  console.time('[IM初始化] 第一阶段耗时')

  // 连接 WebSocket（不等待，异步执行）
  connect()

  // 加载最近会话列表（限制20条，快速展示）
  loadRecentSessionsOnly().catch(error => {
    console.error('加载最近会话失败:', error)
  })

  console.timeEnd('[IM初始化] 第一阶段耗时')

  // ========== 第二阶段：延迟加载（重要功能） ==========
  // 目标：页面渲染完成后，加载完整数据
  setTimeout(() => {
    console.time('[IM初始化] 第二阶段耗时')

    // 加载完整会话列表
    loadSessions().catch(error => {
      console.error('加载完整会话列表失败:', error)
    })

    // 加载好友列表
    loadFriends().catch(error => {
      console.error('加载好友列表失败:', error)
    })

    // 加载群组列表
    loadGroups().catch(error => {
      console.error('加载群组列表失败:', error)
    })

    console.timeEnd('[IM初始化] 第二阶段耗时')
  }, 100) // 延迟100ms，让UI先渲染

  // ========== 第三阶段：后台加载（辅助功能） ==========
  // 目标：用户不敏感的数据，后台异步加载
  setTimeout(() => {
    console.time('[IM初始化] 第三阶段耗时')

    // 加载好友请求
    loadFriendRequests().catch(error => {
      console.error('加载好友请求失败:', error)
    })

    // 初始化组织架构树（仅在联系人模块需要）
    if (activeModule.value === 'contacts') {
      initOrgTree().catch(error => {
        console.error('加载组织架构失败:', error)
      })
    }

    // 重发离线消息
    store.dispatch('im/resendOfflineMessages').catch(error => {
      console.error('重发离线消息失败:', error)
    })

    console.timeEnd('[IM初始化] 第三阶段耗时')
  }, 500) // 延迟500ms，避免阻塞主要功能

  console.log('[IM初始化] 渐进式加载策略已启动')
}

// 加载最近会话列表（仅加载前20条，用于快速展示）
const loadRecentSessionsOnly = async () => {
  try {
    const response = await listSession({ pageSize: 20, pageNum: 1 })
    const sessionList = response.rows || response.data || []

    // 转换会话数据格式
    const formattedSessions = sessionList.map(s => ({
      id: s.id || s.conversationId,
      name: s.name || s.title,
      avatar: s.avatar,
      type: s.type || 'private',
      peerId: s.peerId || s.targetId,
      groupId: s.groupId || s.targetId,
      unreadCount: s.unreadCount || 0,
      lastMessage: s.lastMessage?.content || s.lastMessage,
      lastMessageTime: s.lastMessage?.sendTime || s.lastMessageTime || s.updatedAt || s.updateTime,
      pinned: s.isPinned || s.pinned || false,
      muted: s.isMuted || s.muted || false,
    }))

    // 去重逻辑
    const uniqueSessionsMap = new Map()
    formattedSessions.forEach(session => {
      let key
      if (session.type === 'private' || session.type === 'PRIVATE') {
        key = `private_${session.peerId}`
      } else if (session.type === 'group' || session.type === 'GROUP') {
        key = `group_${session.groupId}`
      } else {
        key = `session_${session.id}`
      }

      if (uniqueSessionsMap.has(key)) {
        const existing = uniqueSessionsMap.get(key)
        const existingTime = new Date(existing.lastMessageTime || 0).getTime()
        const newTime = new Date(session.lastMessageTime || 0).getTime()
        if (newTime > existingTime) {
          uniqueSessionsMap.set(key, session)
        }
      } else {
        uniqueSessionsMap.set(key, session)
      }
    })

    store.commit('im/SET_SESSIONS', Array.from(uniqueSessionsMap.values()))
    console.log(`[快速加载] 已加载 ${uniqueSessionsMap.size} 个最近会话`)
  } catch (error) {
    console.error('加载最近会话失败:', error)
    throw error
  }
}

// 加载会话列表
const loadSessions = async () => {
  try {
    const response = await listSession()
    const sessionList = response.rows || response.data || []

    // 转换会话数据格式
    const formattedSessions = sessionList.map(s => ({
      id: s.id || s.conversationId,
      name: s.name || s.title,
      avatar: s.avatar,
      type: s.type || 'private', // private, group
      peerId: s.peerId || s.targetId,
      groupId: s.groupId || s.targetId,
      unreadCount: s.unreadCount || 0,
      // 后端返回的lastMessage是ImMessageVO对象，包含content和sendTime字段
      lastMessage: s.lastMessage?.content || s.lastMessage,
      // 优先使用lastMessage的sendTime，其次使用lastMessageTime，最后使用updateTime
      lastMessageTime: s.lastMessage?.sendTime || s.lastMessageTime || s.updatedAt || s.updateTime,
      pinned: s.isPinned || s.pinned || false,
      muted: s.isMuted || s.muted || false,
    }))

    // 去重逻辑：私聊会话按peerId去重，群聊会话按groupId去重
    const uniqueSessionsMap = new Map()
    formattedSessions.forEach(session => {
      // 生成去重key：私聊使用peerId，群聊使用groupId
      let key
      if (session.type === 'private' || session.type === 'PRIVATE') {
        key = `private_${session.peerId}`
      } else if (session.type === 'group' || session.type === 'GROUP') {
        key = `group_${session.groupId}`
      } else {
        key = `session_${session.id}`
      }

      // 如果key已存在，保留更新时间更晚的会话
      if (uniqueSessionsMap.has(key)) {
        const existing = uniqueSessionsMap.get(key)
        const existingTime = new Date(existing.lastMessageTime || 0).getTime()
        const newTime = new Date(session.lastMessageTime || 0).getTime()
        if (newTime > existingTime) {
          uniqueSessionsMap.set(key, session)
        }
      } else {
        uniqueSessionsMap.set(key, session)
      }
    })

    store.commit('im/SET_SESSIONS', Array.from(uniqueSessionsMap.values()))

    // 同步群组会话到本地 groupSessions，便于"我的群组"列表显示
    groupSessions.value = Array.from(uniqueSessionsMap.values())
      .filter(s => s.type === 'group')
      .map(s => ({ id: s.id, name: s.name, avatar: s.avatar, groupId: s.groupId }))
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// ==================== 群组功能 ====================
// 创建群组相关
const createGroupDialogVisible = ref(false)
const createGroupFormRef = ref(null)
const creatingGroup = ref(false)

// 发起聊天对话框
const startChatDialogVisible = ref(false)
const selectedFriendForChat = ref(null)

// 显示发起聊天对话框
const showStartChatDialog = () => {
  if (friends.value.length === 0) {
    ElMessage.warning('请先添加好友')
    return
  }
  startChatDialogVisible.value = true
}

// 发起私聊
const startPrivateChat = friend => {
  selectedFriendForChat.value = friend
  // 通过发送消息来触发会话创建（如果不存在）
  startChatDialogVisible.value = false
  // TODO: 跳转到与该好友的聊天界面
  ElMessage.success(`开始与 ${friend.name || friend.nickname} 聊天`)
}

const createGroupForm = ref({
  name: '',
  avatar: '',
  description: '',
  memberIds: [],
  joinType: 'needApproval',
})

const createGroupRules = {
  name: [
    { required: true, message: '请输入群组名称', trigger: 'blur' },
    { min: 2, max: 20, message: '群组名称长度在2-20个字符', trigger: 'blur' },
  ],
  memberIds: [{ required: true, message: '请至少选择一个成员', trigger: 'change' }],
}

// 成员选择器相关
const memberSelectorVisible = ref(false)
const memberSearchKeyword = ref('')
const selectedGroupMembers = ref([])

// 可选用户列表（模拟数据，实际应从好友列表获取）
const availableUsers = computed(() => {
  return friends.value.map(f => ({
    id: f.id || f.friendId || f.userId,
    name: f.name,
    nickname: f.nickname,
    avatar: f.avatar,
    deptName: f.deptName,
    position: f.position,
  }))
})

const filteredUsers = computed(() => {
  if (!memberSearchKeyword.value) return availableUsers.value
  const keyword = memberSearchKeyword.value.toLowerCase()
  return availableUsers.value.filter(u => {
    const name = (u.name || u.nickname || '').toLowerCase()
    return name.includes(keyword)
  })
})

// 群组管理相关
const groupManageDialogVisible = ref(false)
const groupManageTab = ref('members')
const managingGroup = ref(null)
const groupMemberSearch = ref('')
const managedGroupMembers = ref([])
const groupFiles = ref([
  {
    id: '1',
    name: '项目计划.docx',
    type: 'document',
    size: '2.3 MB',
    uploadTime: '2024-01-10',
    uploader: '张三',
  },
  {
    id: '2',
    name: '会议记录.pdf',
    type: 'pdf',
    size: '1.1 MB',
    uploadTime: '2024-01-09',
    uploader: '李四',
  },
  {
    id: '3',
    name: '产品截图',
    type: 'image',
    size: '5.6 MB',
    uploadTime: '2024-01-08',
    uploader: '王五',
  },
])

// 过滤后的会话列表
const filteredSessions = computed(() => {
  return sessions.value
})

// 过滤后的群组成员
const filteredGroupMembers = computed(() => {
  if (!groupMemberSearch.value) return managedGroupMembers.value
  const keyword = groupMemberSearch.value.toLowerCase()
  return managedGroupMembers.value.filter(m => {
    const name = (m.name || m.nickname || '').toLowerCase()
    return name.includes(keyword)
  })
})

// 是否可以添加成员
const canAddMembers = computed(() => {
  return (
    managingGroup.value &&
    (managingGroup.value.role === 'owner' || managingGroup.value.role === 'admin')
  )
})

// 显示创建群组对话框
const showCreateGroupDialog = () => {
  createGroupDialogVisible.value = true
}

// 重置创建群组表单
const resetCreateGroupForm = () => {
  createGroupForm.value = {
    name: '',
    avatar: '',
    description: '',
    memberIds: [],
    joinType: 'needApproval',
  }
  selectedGroupMembers.value = []
  createGroupFormRef.value?.resetFields()
}

// 群组头像上传成功
const handleGroupAvatarSuccess = response => {
  createGroupForm.value.avatar = response.url || response.data?.url
}

// 群组头像上传前检查
const beforeGroupAvatarUpload = file => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  return true
}

// 自定义群组头像上传
const customGroupAvatarUpload = async options => {
  try {
    const response = await uploadImage(options.file)
    options.onSuccess(response)
  } catch (error) {
    options.onError(error)
    ElMessage.error('头像上传失败')
  }
}

// 显示成员选择器
const showMemberSelector = () => {
  memberSelectorVisible.value = true
}

// 判断成员是否已选中
const isGroupMemberSelected = user => {
  return selectedGroupMembers.value.some(m => m.id === user.id)
}

// 切换成员选中状态
const toggleGroupMember = user => {
  const index = selectedGroupMembers.value.findIndex(m => m.id === user.id)
  if (index > -1) {
    selectedGroupMembers.value.splice(index, 1)
  } else {
    selectedGroupMembers.value.push(user)
  }
}

// 移除成员
const removeGroupMember = member => {
  const index = selectedGroupMembers.value.findIndex(m => m.id === member.id)
  if (index > -1) {
    selectedGroupMembers.value.splice(index, 1)
  }
}

// 确认成员选择
const confirmMemberSelection = () => {
  memberSelectorVisible.value = false
}

// 提交创建群组
const submitCreateGroup = async () => {
  try {
    await createGroupFormRef.value.validate()

    if (selectedGroupMembers.value.length === 0) {
      ElMessage.warning('请至少选择一个成员')
      return
    }

    creatingGroup.value = true

    // 调用创建群组API
    const groupData = {
      name: createGroupForm.value.name,
      avatar: createGroupForm.value.avatar,
      description: createGroupForm.value.description,
      memberIds: selectedGroupMembers.value.map(m => m.id),
      joinType: createGroupForm.value.joinType,
    }

    // TODO: 调用实际的创建群组API
    // const response = await addGroup(groupData)

    // 模拟创建成功
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('群组创建成功')
    createGroupDialogVisible.value = false
    resetCreateGroupForm()

    // 刷新会话列表
    await loadSessions()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('创建失败：' + (error.message || '未知错误'))
    }
  } finally {
    creatingGroup.value = false
  }
}

// 打开群组管理
const openGroupManage = group => {
  managingGroup.value = { ...group }
  managedGroupMembers.value = [
    {
      id: currentUser.value?.userId,
      name: currentUser.value?.name || '我',
      role: 'owner',
      roleText: '群主',
    },
    ...selectedGroupMembers.value.map(m => ({ ...m, role: 'member', roleText: '成员' })),
  ]
  groupManageDialogVisible.value = true
}

// 是否可以管理成员
const canManageMember = member => {
  if (!managingGroup.value) return false
  const currentRole = 'owner' // 当前用户角色
  if (currentRole === 'owner') return member.role !== 'owner'
  if (currentRole === 'admin') return member.role === 'member'
  return false
}

// 处理成员命令
const handleMemberCommand = async (command, member) => {
  try {
    switch (command) {
      case 'setAdmin':
        member.role = 'admin'
        member.roleText = '管理员'
        ElMessage.success('已设为管理员')
        break
      case 'removeAdmin':
        member.role = 'member'
        member.roleText = '成员'
        ElMessage.success('已取消管理员')
        break
      case 'mute':
        await ElMessageBox.confirm(`确定要禁言${member.name || member.nickname}吗？`, '确认禁言')
        member.isMuted = true
        ElMessage.success('已禁言该成员')
        break
      case 'unmute':
        member.isMuted = false
        ElMessage.success('已取消禁言')
        break
      case 'remove':
        await ElMessageBox.confirm(`确定要将${member.name || member.nickname}移出群组吗？`, '确认')
        const index = managedGroupMembers.value.findIndex(m => m.id === member.id)
        if (index > -1) managedGroupMembers.value.splice(index, 1)
        ElMessage.success('已移出群组')
        break
    }
  } catch {
    // 用户取消
  }
}

// 显示添加群成员
const showAddGroupMember = () => {
  ElMessage.info('添加群成员功能开发中...')
}

// 保存群组设置
const saveGroupSettings = () => {
  ElMessage.success('群组设置已保存')
}

// 上传群文件
const uploadGroupFile = () => {
  ElMessage.info('群文件上传功能开发中...')
}

// 预览群文件
const previewGroupFile = file => {
  previewingFile.value = file
  filePreviewDialogVisible.value = true
}

// 处理文件命令
const handleFileCommand = async (command, file) => {
  switch (command) {
    case 'download':
      ElMessage.success(`正在下载: ${file.name}`)
      break
    case 'rename':
      ElMessage.info('重命名功能开发中...')
      break
    case 'delete':
      try {
        await ElMessageBox.confirm(`确定要删除文件"${file.name}"吗？`, '确认删除')
        const index = groupFiles.value.findIndex(f => f.id === file.id)
        if (index > -1) groupFiles.value.splice(index, 1)
        ElMessage.success('文件已删除')
      } catch {
        // 用户取消
      }
      break
  }
}

// 组件挂载
onMounted(() => {
  init()

  // 开发环境：添加全局调试函数
  if (import.meta.env.DEV) {
    window.__debug_friends__ = {
      getFriends: () => friends.value,
      getFriendCount: () => friends.value.length,
      getGroupedFriends: () => groupedFriends.value,
      showDuplicates: () => {
        const idMap = new Map()
        const duplicates = []
        friends.value.forEach(f => {
          const key = String(f.friendId)
          if (!idMap.has(key)) {
            idMap.set(key, [])
          }
          idMap.get(key).push(f)
        })
        idMap.forEach((list, key) => {
          if (list.length > 1) {
            duplicates.push({ friendId: key, count: list.length, items: list })
          }
        })
        return duplicates
      },
      showByName: (name) => {
        return friends.value.filter(f => (f.name || f.nickname).includes(name))
      }
    }
    console.log('🔍 调试工具已安装: window.__debug_friends__')
    console.log('  - getFriends(): 获取好友列表')
    console.log('  - getFriendCount(): 获取好友数量')
    console.log('  - showDuplicates(): 检查重复')
    console.log('  - showByName("名字"): 搜索好友')
  }
})

// 组件卸载
onUnmounted(() => {
  // 这里不主动断开 WebSocket，因为其他组件可能也在使用
})
</script>

<style lang="scss" scoped>
@use 'sass:color';

// ==================== 钉钉PC客户端 设计规范变量 ====================
// 布局尺寸（严格按照钉钉PC客户端实际测量）
$nav-width-narrow: 68px;     // 左侧导航栏宽度（窄模式-仅图标）- 钉钉规范68px
$nav-width-wide: 180px;      // 左侧导航栏宽度（宽模式-图标+文字）
$nav-width: $nav-width-narrow; // 当前使用窄模式
$header-height: 48px;        // 顶部导航栏高度
$session-panel-width: 320px; // 会话列表宽度（固定）- 钉钉规范320px
$chat-panel-min-width: 400px; // 聊天区域最小宽度
$nav-item-size: 48px;         // 导航项尺寸 - 钉钉规范48×48px
$chat-header-height: 48px;    // 聊天头部高度
$input-bar-height: 56px;      // 输入栏高度
$search-bar-height: 36px;     // 搜索栏高度
$session-item-height: 64px;   // 会话项高度 - 钉钉规范64px

// 品牌色系（钉钉标准色）
$primary-color: #1890FF;     // 钉钉蓝（主色）
$primary-color-hover: #40A9FF;
$primary-color-active: #096DD9;
$primary-color-light: #E6F7FF; // 浅蓝（选中背景）
$primary-disabled: #D9D9D9;

// 中性色系（钉钉规范）
$text-primary: #262626;      // 主要文字 - 钉钉规范标题色
$text-secondary: #333333;     // 正文内容 - 钉钉规范正文色
$text-regular: #666666;       // 副标题、次要信息 - 钉钉规范次要文字
$text-tertiary: #999999;      // 时间戳、提示信息 - 钉钉规范辅助文字
$text-disabled: #CCCCCC;      // 禁用状态
$text-placeholder: #999999;   // 占位符 - 钉钉规范辅助文字

$bg-white: #FFFFFF;
$bg-gray: #F5F7FA;           // 页面整体背景（钉钉浅灰）
$bg-light: #FAFAFA;          // 卡片、面板背景
$bg-hover: #F5F7FA;          // 悬停背景（钉钉浅灰）
$bg-nav-narrow: #F5F7FA;     // 导航栏窄模式背景
$bg-nav-wide: #FFFFFF;       // 导航栏宽模式背景

$border-color: #E8E8E8;      // 分割线、边框 - 钉钉规范边框色
$border-hover: #D9D9D9;      // 边框悬停

// 功能色
$success-color: #52C41A;
$warning-color: #FAAD14;
$danger-color: #FF4D4F;
$info-color: #1890FF;

// 导航栏颜色
$nav-bg: #FFFFFF;
$nav-item-hover: #F5F5F5;
$nav-item-active: #E6F7FF;
$nav-item-icon: #666666;
$nav-item-icon-active: #1890FF;

// 消息气泡颜色（钉钉规范）
$message-sent-bg: #1890FF;         // 发送方：钉钉蓝
$message-sent-text: #FFFFFF;       // 发送方文字：白色
$message-received-bg: #FFFFFF;    // 接收方：白色
$message-received-text: #333333;  // 接收方文字：钉钉正文色
$message-received-border: #E8E8E8; // 接收方边框：钉钉规范边框色

// 阴影系统
$shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.03);
$shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
$shadow-base: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
$shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
$shadow-message: 0 1px 2px rgba(0, 0, 0, 0.08);

// 圆角
$radius-xs: 2px;
$radius-sm: 4px;
$radius-base: 6px;
$radius-lg: 8px;
$radius-xl: 12px;
$radius-round: 50%;

// 间距
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 12px;
$spacing-lg: 16px;
$spacing-xl: 24px;

// 动画时长（钉钉规范）
$transition-instant: 0.1s;   // 按钮点击 - 极快
$transition-fast: 0.2s;       // 快速切换 - 颜色/阴影变化
$transition-base: 0.3s;       // 标准过渡 - 展开/收起
$transition-slow: 0.5s;       // 较慢 - 复杂动画

// 缓动函数（钉钉规范）
$ease-linear: linear;                           // 线性 - 进度条
$ease-base: cubic-bezier(0.4, 0, 0.2, 1);       // 标准缓动
$ease-in: cubic-bezier(0.4, 0, 1, 1);           // 入场缓动
$ease-out: cubic-bezier(0, 0, 0.2, 1);          // 出场缓动
$ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);     // 双向缓动
$ease-bounce: cubic-bezier(0.34, 1.56, 0.64, 1); // 弹性缓动 - 消息发送

// 头像尺寸
$avatar-xs: 24px;
$avatar-sm: 32px;
$avatar-base: 40px;
$avatar-lg: 48px;
$avatar-xl: 64px;

// 用户下拉菜单样式
:deep(.el-dropdown-menu) {
  padding: 8px 0;
  min-width: 200px;
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.18);
  border: 1px solid #e5e6eb;
  background-color: #fff;
}

:deep(.el-dropdown-menu__item) {
  padding: 12px 18px;
  font-size: 14px;
  color: #1d2129;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
  border-radius: 0;

  &:hover {
    background: linear-gradient(90deg, #f0f7ff 0%, #ffffff 100%);
    color: #165dff;
  }

  .el-icon {
    font-size: 18px;
    color: #4e5969;
    transition: color 0.3s ease;
  }

  &:hover .el-icon {
    color: #165dff;
    transform: scale(1.1);
  }

  &.is-disabled {
    color: #c9cdd4;
    cursor: not-allowed;

    .el-icon {
      color: #c9cdd4;
    }

    &:hover {
      background: transparent;
      color: #c9cdd4;
    }
  }

  // 用户信息项特殊样式
  &.user-info-item {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
    background: linear-gradient(90deg, #f7f8fa 0%, #ffffff 100%);

    .dropdown-avatar {
      margin-bottom: 8px;
      border: 2px solid #165dff;
      box-shadow: 0 2px 8px rgba(22, 93, 255, 0.2);
    }

    .user-info-item {
      .user-name {
        font-size: 16px;
        font-weight: 600;
        color: #1d2129;
      }

      .user-id {
        font-size: 12px;
        color: #86909c;
        margin-top: 2px;
      }
    }
  }

  // 下拉菜单中用户信息行的样式
  .profile-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px;
    margin: -8px;
    border-radius: 8px;
    transition: background-color 0.2s;

    &:hover {
      background: #f7f8fa;
    }
  }

  // 状态项包装器
  .status-item-wrapper {
    padding: 4px 0;
  }

  // 状态项内容
  .status-item-content {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    font-weight: 500;
  }

  // 状态选择器样式
  .status-selector {
    display: flex;
    gap: 12px;
    margin: 12px 0;
    padding: 8px 12px;
    background: #f7f8fa;
    border-radius: 8px;
  }

  // 状态按钮
  .status-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    cursor: pointer;
    transition: all 0.2s ease;
    background: #fff;
    border: 2px solid transparent;

    &:hover {
      transform: scale(1.1);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    &.active {
      border-color: #1677ff;
      box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.15);
    }
  }

  // 状态点
  .status-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    border: 2px solid #fff;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);

    &.online {
      background: #52c41a;
    }

    &.away {
      background: #faad14;
    }

    &.busy {
      background: #ff4d4f;
    }

    &.offline {
      background: #d9d9d9;
    }
  }

  // 当前状态文本
  .current-status-text {
    font-size: 12px;
    color: #86909c;
    text-align: center;
    padding-bottom: 4px;
  }

  // 头部操作按钮包装器
  .header-action-wrapper {
    display: flex;
    align-items: center;
    padding: 4px;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background: #f2f3f5;
    }
  }

  // 设置和帮助下拉菜单样式
  .settings-dropdown,
  .help-dropdown {
    .menu-icon {
      margin-right: 8px;
      font-size: 16px;
      color: #606266;

      &.theme {
        color: #faad14;
      }
    }

    .menu-text {
      flex: 1;
    }

    .version-tag {
      margin-left: 8px;
    }
  }

  // 快捷键标签
  .item-shortcut {
    margin-left: auto;
    font-size: 12px;
    color: #86909c;
    background: #f2f3f5;
    padding: 2px 6px;
    border-radius: 4px;
  }
}

// 滚动条样式
@mixin web-scrollbar {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 3px;
    &:hover {
      background: #bfbfbf;
    }
  }
}

// 文本省略样式
@mixin text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// 多行文本省略
@mixin text-ellipsis-multiline($lines: 2) {
  display: -webkit-box;
  -webkit-line-clamp: $lines;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.web-im-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;
  overflow: hidden;

  // 顶部导航栏（钉钉风格优化版）
  .ding-header {
    height: $header-height;
    min-height: $header-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    background: #fff;
    border-bottom: 1px solid $border-color;
    z-index: 100;

    // 左侧区域
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      flex: 1;

      .header-logo {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;

        .logo-text {
          font-size: 18px;
          font-weight: 600;
          color: $primary-color;
        }
      }

      .header-divider {
        width: 1px;
        height: 24px;
        background: $border-color;
      }

      .header-search {
        flex: 1;
        max-width: 400px;

        .global-search-input {
          :deep(.el-input__wrapper) {
            border-radius: 8px;
            background: $bg-gray;
            border: 1px solid transparent;
            box-shadow: none;
            transition: all 0.2s;

            &:hover {
              background: #fafafa;
              border-color: #d9d9d9;
            }

            &.is-focus {
              background: #fff;
              border-color: $primary-color;
              box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
            }
          }

          :deep(.el-input__inner) {
            font-size: 13px;
          }

          :deep(.el-input__prefix) {
            color: $text-tertiary;
          }

          .search-shortcut {
            font-size: 11px;
            color: $text-tertiary;
            background: rgba(0, 0, 0, 0.04);
            padding: 2px 6px;
            border-radius: 4px;
          }
        }
      }
    }

    // 右侧区域
    .header-right {
      display: flex;
      align-items: center;
      gap: 4px;

      .header-badge {
        :deep(.el-badge__content) {
          background: $danger-color;
          border: 2px solid #fff;
        }
      }

      .header-action-btn {
        padding: 8px;
        border-radius: 6px;
        color: $text-secondary;
        transition: all 0.2s;

        &:hover {
          color: $primary-color;
          background: $nav-item-hover;
        }

        &:active {
          background: color.adjust($nav-item-hover, $lightness: -5%);
        }
      }
      }

      .header-user {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 4px 12px 4px 4px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s;
        margin-left: 8px;
        position: relative;

        &:hover {
          background: $nav-item-hover;
        }

        .header-username {
          font-size: 13px;
          color: $text-secondary;
          max-width: 80px;
          @include text-ellipsis;
        }

        .dropdown-arrow {
          font-size: 12px;
          color: $text-tertiary;
          transition: transform 0.2s;
        }

        &:hover .dropdown-arrow {
          transform: rotate(180deg);
        }
      }
    }
  }


  // 主体区域
  .main-body {
    flex: 1;
    display: flex;
    overflow: hidden;

    // 左侧导航栏（钉钉68px窄模式）
    .nav-sidebar {
      width: $nav-width-narrow;
      background: $bg-nav-narrow;
      border-right: 1px solid $border-color;
      display: flex;
      flex-direction: column;
      position: relative;
      z-index: 50;

      .nav-list {
        flex: 1;
        padding: 12px 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        overflow-y: auto;
        overflow-x: hidden;

        .nav-item {
          width: 48px;    // 钉钉规范：导航项尺寸48×48px
          height: 48px;   // 钉钉规范：导航项尺寸48×48px
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          color: $nav-item-icon;
          border-radius: 6px;
          transition: all 0.2s ease;
          position: relative;

          .nav-icon {
            font-size: 24px;  // 钉钉规范：图标24×24px
          }

          // 未读红点
          .nav-dot {
            position: absolute;
            top: 6px;
            right: 6px;
            width: 8px;
            height: 8px;
            background: $danger-color;
            border-radius: 50%;
            border: 1px solid $bg-nav-narrow;
          }

          &:hover {
            background: rgba(0, 0, 0, 0.06);
            color: $primary-color;
          }

          &.active {
            background: $primary-color-light;
            color: $nav-item-icon-active;

            &::after {
              content: '';
              position: absolute;
              left: 0;
              top: 50%;
              transform: translateY(-50%);
              width: 3px;
              height: 20px;
              background: $primary-color;
              border-radius: 0 2px 2px 0;
            }
          }
        }
      }

      // 底部用户头像
      .nav-user {
        padding: 12px 0;
        display: flex;
        justify-content: center;
        border-top: 1px solid $border-color;
      }
    }

    // 工作区
    .workspace {
      flex: 1;
      display: flex;
      overflow: hidden;
      background: #fafafa;

      // 聊天工作区
      .chat-workspace {
        display: flex;
        width: 100%;

        .session-panel {
          width: $session-panel-width;
          background: #fff;
          border-right: 1px solid $border-color;
          display: flex;
          flex-direction: column;

          .session-list {
            flex: 1;
            height: 0;
            overflow-y: auto;
            @include web-scrollbar;

            .session-item {
              display: flex;
              align-items: center;
              height: $session-item-height;
              padding: 8px 12px;
              margin: 0 8px;
              cursor: pointer;
              border-radius: 6px;
              transition: all $transition-fast $ease-base;
              position: relative;

              // 新会话滑入动画
              &.new-session {
                animation: listSlideIn 0.3s $ease-base;
              }

              &:hover {
                background: $bg-hover;
              }

              &.active {
                background: $primary-color-light;

                &::before {
                  content: '';
                  position: absolute;
                  left: 0;
                  top: 50%;
                  transform: translateY(-50%);
                  width: 3px;
                  height: 24px;
                  background: $primary-color;
                  border-radius: 0 2px 2px 0;
                }
              }

              .el-badge {
                margin-right: 10px;
                flex-shrink: 0;

                :deep(.el-badge__content) {
                  background: $danger-color;
                  border: none;
                  font-size: 11px;
                  height: 16px;
                  line-height: 16px;
                  padding: 0 5px;
                }
              }

              .session-info {
                flex: 1;
                min-width: 0;

                .session-top {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 2px;

                  .session-name {
                    font-size: 14px;
                    font-weight: 500;
                    color: $text-secondary;
                    @include text-ellipsis;
                  }

                  .session-time {
                    font-size: 11px;
                    color: $text-tertiary;
                    flex-shrink: 0;
                    margin-left: 8px;
                  }
                }

                .session-preview {
                  font-size: 12px;
                  color: $text-tertiary;
                  @include text-ellipsis;
                }
              }
            }
          }
        }

        .chat-panel {
          flex: 1;
          display: flex;
          flex-direction: column;
          background: #fff;

          .chat-container {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: hidden;

            .chat-header {
              height: 60px;
              padding: 0 16px;
              display: flex;
              align-items: center;
              justify-content: space-between;
              border-bottom: 1px solid $border-color;
              background: #fff;
              box-shadow: $shadow-sm;
              z-index: 10;

              // 左侧：会话信息
              .chat-info {
                display: flex;
                align-items: center;
                gap: 12px;

                .chat-avatar-group {
                  position: relative;
                  cursor: pointer;

                  .group-badge {
                    position: absolute;
                    bottom: -2px;
                    right: -2px;
                    width: 16px;
                    height: 16px;
                    background: $primary-color;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: #fff;
                    font-size: 10px;
                    border: 2px solid #fff;
                  }
                }

                .chat-title-info {
                  display: flex;
                  flex-direction: column;
                  gap: 2px;

                  .title-row {
                    display: flex;
                    align-items: center;
                    gap: 6px;

                    .title-name {
                      font-size: 15px;
                      font-weight: 600;
                      color: $text-primary;
                    }

                    .group-tag {
                      height: 18px;
                      padding: 0 6px;
                      font-size: 11px;
                      background: $nav-item-hover;
                      border: none;
                    }
                  }

                  .title-subtitle {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    font-size: 12px;
                    color: $text-tertiary;

                    .status-dot {
                      width: 6px;
                      height: 6px;
                      border-radius: 50%;
                      background: $text-tertiary;

                      &.online {
                        background: $success-color;
                      }
                    }

                    .status-text {
                      line-height: 1;
                    }

                    .member-count {
                      color: $text-tertiary;
                    }
                  }
                }
              }

              // 右侧：操作按钮
              .chat-actions {
                display: flex;
                align-items: center;
                gap: 6px;

                .action-btn {
                  --el-button-border-color: transparent;
                  --el-button-bg-color: transparent;
                  --el-button-text-color: $text-regular;
                  width: 34px;
                  height: 34px;
                  padding: 6px;
                  border-radius: 8px;

                  &:hover {
                    --el-button-text-color: $primary-color;
                    --el-button-hover-bg-color: $nav-item-hover;
                  }
                }

                .el-divider--vertical {
                  height: 20px;
                  margin: 0 4px;
                }

                :deep(.el-dropdown-menu__item.danger-item) {
                  color: $danger-color;

                  &:hover {
                    background: rgba(245, 34, 45, 0.1);
                    color: $danger-color;
                  }

                  .el-icon {
                    color: $danger-color;
                  }
                }
              }
            }

            .message-area {
              flex: 1;
              display: flex;
              flex-direction: column;
              padding: 16px 20px;
              overflow-y: auto;
              background: $bg-gray;
              @include web-scrollbar;

              .connection-status {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
                padding: 10px 16px;
                margin-bottom: 12px;
                background: #fff7e6;
                border: 1px solid #ffd591;
                border-radius: $radius-base;
                color: $warning-color;
                font-size: 13px;

                .el-icon {
                  animation: pulse 1.5s infinite;
                }
              }

              .message-item {
                display: flex;
                margin-bottom: 16px;
                gap: 12px;
                padding: 0 16px;

                // 新消息弹出动画
                &.new-message {
                  animation: messagePop 0.3s $ease-bounce;
                }

                /* 发送方消息 - 气泡在左边，头像在右边（整个靠右对齐） */
                &.isOwn {
                  align-self: flex-end;
                  flex-direction: row;

                  .message-avatar {
                    margin-left: 12px;
                    margin-right: 0;
                  }

                  .message-content {
                    display: flex;
                    flex-direction: column;
                    align-items: flex-end;
                  }

                  .message-bubble {
                    // 钉钉发送方消息气泡
                    background: $message-sent-bg;
                    color: $message-sent-text;
                    border-radius: 8px;
                    border-bottom-right-radius: 2px;
                    border: none;
                    box-shadow: $shadow-message;
                    padding: 10px 14px;
                    font-size: 14px;
                    line-height: 1.6;

                    &:hover {
                      background: $primary-color-hover;
                    }

                    // 链接样式
                    a {
                      color: rgba(255, 255, 255, 0.9);
                      text-decoration: underline;

                      &:hover {
                        color: #fff;
                      }
                    }
                  }
                }

                /* 接收方消息 - 头像在左边，消息在右边 */
                &:not(.isOwn) {
                  align-self: flex-start;

                  .message-avatar {
                    margin-right: 12px;
                  }

                  .message-content {
                    align-items: flex-start;
                  }
                }

                .message-content {
                  max-width: 60%;  // 钉钉规范：气泡最大宽度60%

                  .sender-name {
                    font-size: 12px;
                    color: $text-tertiary;
                    margin-bottom: 4px;
                    margin-left: 4px;
                  }

                  /* 接收方消息气泡 - 白色 */
                  .message-bubble {
                    position: relative;
                    padding: 10px 14px;
                    border-radius: 8px;
                    border-bottom-left-radius: 2px;
                    background: $message-received-bg;
                    color: $message-received-text;
                    font-size: 14px;
                    line-height: 1.6;
                    word-break: break-word;
                    border: 1px solid $message-received-border;
                    box-shadow: $shadow-message;
                    transition: all 0.2s ease;

                    &:hover {
                      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
                    }

                    &.sending {
                      opacity: 0.7;
                    }

                    &.failed {
                      background: #fff1f0;
                      color: $danger-color;
                      border: 1px solid #ffccc7;
                      box-shadow: 0 1px 3px rgba(255, 77, 79, 0.15);
                    }
                  }

                  // 图片消息
                  .message-image {
                    max-width: 200px;

                    .image-content {
                      width: 200px;
                      height: 200px;
                      border-radius: var(--dt-radius-lg);
                      overflow: hidden;
                      background: #f0f0f0;
                      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
                      transition: all var(--dt-duration-base) var(--dt-ease-out);
                      cursor: pointer;

                      &:hover {
                        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
                        transform: scale(1.02);
                      }

                      &.sending {
                        opacity: 0.7;
                      }
                    }
                  }

                  // 文件消息
                  .message-file {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    padding: 12px 16px;
                    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
                    border-radius: var(--dt-radius-lg);
                    border: 1px solid rgba(0, 0, 0, 0.04);
                    min-width: 200px;
                    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
                    transition: all var(--dt-duration-base) var(--dt-ease-out);
                    cursor: pointer;

                    &:hover {
                      background: linear-gradient(135deg, #e8ecf1 0%, #dbe0e6 100%);
                      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                      transform: translateY(-1px);
                    }

                    &.sending {
                      opacity: 0.7;
                    }

                    .file-icon {
                      width: 40px;
                      height: 40px;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      background: #e6f7ff;
                      border-radius: 8px;
                      color: $primary-color;
                      font-size: 20px;
                    }

                    .file-info {
                      flex: 1;
                      min-width: 0;

                      .file-name {
                        font-size: 14px;
                        font-weight: 500;
                        color: $text-primary;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                      }

                      .file-size {
                        font-size: 12px;
                        color: $text-tertiary;
                        margin-top: 2px;
                      }
                    }
                  }

                  // 语音消息
                  .message-voice {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    padding: 10px 16px;
                    background: $bg-gray;
                    border-radius: 20px;
                    min-width: 80px;

                    &.sending {
                      opacity: 0.7;
                    }

                    .voice-icon {
                      font-size: 18px;
                      color: $text-secondary;
                    }

                    .voice-duration {
                      font-size: 13px;
                      color: $text-secondary;
                    }
                  }

                  .message-time {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    font-size: 12px;
                    color: $text-tertiary;
                    margin-top: 6px;

                    .status-icon {
                      font-size: 13px;

                      &.sending {
                        color: $warning-color;
                        animation: spin 1s linear infinite;
                      }

                      &.sent {
                        color: $success-color;
                      }

                      &.failed {
                        color: $danger-color;
                        cursor: pointer;

                        &:hover {
                          color: #d9363e;
                        }
                      }
                    }
                  }
                }
              }

              /* 发送方消息时间戳右对齐 */
              .message-item.isOwn {
                .message-time {
                  align-self: flex-end;
                  flex-direction: row-reverse;
                }
              }
            }

            .input-area {
              padding: 12px 16px;
              border-top: 1px solid $border-color;
              background: $bg-light;

              .voice-recording-panel {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 12px 16px;
                margin-bottom: 12px;
                background: #fff7e6;
                border: 1px solid #ffd591;
                border-radius: $radius-base;

                .voice-info {
                  display: flex;
                  align-items: center;
                  gap: 12px;

                  .recording-duration {
                    font-size: 16px;
                    font-weight: 500;
                    color: $primary-color;
                    min-width: 50px;
                  }

                  .volume-bars {
                    display: flex;
                    gap: 3px;
                    height: 20px;

                    .volume-bar {
                      width: 4px;
                      min-height: 4px;
                      background: linear-gradient(to top, $primary-color, $primary-color-hover);
                      border-radius: 2px;
                      transition: height 0.1s ease;
                    }
                  }

                  .recording-tip {
                    font-size: 13px;
                    color: $warning-color;
                  }
                }

                .voice-actions {
                  display: flex;
                  gap: 8px;
                }
              }

              .input-toolbar {
                display: flex;
                gap: 4px;
                margin-bottom: 8px;
                padding: 0 4px;

                .el-button {
                  --el-button-border-color: transparent;
                  --el-button-bg-color: transparent;
                  --el-button-text-color: $text-regular;
                  padding: 6px;
                  border-radius: $radius-sm;
                  width: 32px;
                  height: 32px;

                  &:hover {
                    --el-button-text-color: $primary-color;
                    --el-button-hover-bg-color: $nav-item-hover;
                    --el-button-hover-border-color: transparent;
                  }

                  &.recording {
                    --el-button-text-color: $danger-color;
                    animation: pulse 1.5s infinite;
                  }
                }
              }

              .chat-input {
                border-radius: $radius-base;
                border: 1px solid $border-color;
                background: #fff;
                box-shadow: $shadow-sm;

                :deep(.el-textarea__inner) {
                  border: none;
                  padding: 10px 12px;
                  resize: none;
                  font-size: 14px;
                  line-height: 1.5;
                  background: transparent;
                  min-height: 36px; // 钉钉规范：最小高度36px
                  max-height: 150px; // 钉钉规范：最大高度150px
                }

                &:focus-within {
                  border-color: $primary-color;
                  box-shadow: 0 0 0 2px $primary-color-light;
                }
              }

              .input-footer {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-top: 8px;
                padding: 0 4px;

                .input-tip {
                  font-size: 12px;
                  color: $text-tertiary;
                }

                .send-button {
                  min-width: 80px;
                  height: 32px;
                  border-radius: $radius-base;
                  font-size: 14px;
                  padding: 6px 16px;
                  transition: all $transition-instant $ease-base;

                  &:active {
                    animation: buttonClick 0.1s $ease-base;
                  }

                  &:disabled {
                    --el-button-text-color: #fff;
                    --el-button-bg-color: $primary-disabled;
                    --el-button-border-color: transparent;

                    &:active {
                      animation: none;
                    }
                  }
                }
              }
            }
          }

          .empty-state {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: $text-tertiary;

            p {
              margin-top: 16px;
              font-size: 14px;
            }
          }
        }
      }

      // 通讯录工作区
      .contacts-workspace {
        display: flex;
        width: 100%;
        height: 100%;

        .contacts-sidebar {
          width: 240px;
          background: #fff;
          border-right: 1px solid $border-color;
          display: flex;
          flex-direction: column;
          height: 100%;
          min-height: 0;
          box-shadow: $shadow-sm;

          .section-title {
            padding: 16px 20px 8px;
            font-size: 12px;
            font-weight: 600;
            color: $text-tertiary;
            letter-spacing: 0.5px;
            text-transform: uppercase;
          }

          .tree-list {
            flex: 1;
            overflow-y: auto;
            @include web-scrollbar;
          }

          .tree-item {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 10px 16px;
            margin: 0 8px;
            cursor: pointer;
            font-size: 14px;
            color: $text-secondary;
            transition: all 0.2s ease;
            border-radius: $radius-sm;

            .tree-label {
              flex: 1;
            }

            .tree-count {
              font-size: 12px;
              color: $primary-color;
              background: $primary-color-light;
              padding: 2px 8px;
              border-radius: 10px;
              font-weight: 500;
            }

            &:hover {
              background: $nav-item-hover;
            }

            &.active {
              background: $primary-color;
              color: #fff;

              .tree-count {
                background: #fff;
                color: $primary-color;
              }
            }
          }
        }

        .contacts-list-panel {
          width: 320px;
          background: #fff;
          border-right: 1px solid $border-color;
          display: flex;
          flex-direction: column;
          height: 100%;
          min-height: 0;

          .list-header {
            padding: 12px 16px;
            border-bottom: 1px solid $border-color;

            .search-input {
              border-radius: $radius-base;
              border: 1px solid $border-color;
              transition: all 0.2s ease;
              box-shadow: none;

              &:hover {
                border-color: $primary-color;
                box-shadow: 0 0 0 2px $primary-color-light;
              }

              &:focus-within {
                border-color: $primary-color;
                box-shadow: 0 0 0 2px $primary-color-light;
              }
            }
          }

          .contact-items,
          .org-members {
            flex: 1;
            overflow-y: auto;
            @include web-scrollbar;

            .contact-item {
              display: flex;
              align-items: center;
              padding: 10px 12px;
              margin: 4px 8px;
              cursor: pointer;
              border-radius: $radius-sm;
              transition: all 0.2s ease;
              position: relative;

              &:hover {
                background: $bg-hover;
              }

              &:active {
                transform: scale(0.98);
              }

              .contact-info {
                flex: 1;
                margin-left: 10px;
                min-width: 0;

                .contact-name {
                  font-size: 14px;
                  font-weight: 500;
                  color: $text-secondary;
                  @include text-ellipsis;
                  margin-bottom: 2px;
                }

                .contact-signature {
                  font-size: 12px;
                  color: $text-tertiary;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                }
              }

              .online-dot {
                width: 10px;
                height: 10px;
                background: $success-color;
                border-radius: 50%;
                flex-shrink: 0;
                border: 2px solid #fff;
                box-shadow: 0 0 0 1px rgba(0, 191, 165, 0.2);
              }
            }
          }

          .org-tree {
            flex: 1;
            overflow-y: auto;
            @include web-scrollbar;
            padding: 8px 0;

            :deep(.el-tree) {
              .el-tree-node__content {
                height: 40px;
                padding: 0 16px;
                transition: all 0.2s ease;

                &:hover {
                  background: $primary-color-light;
                  border-radius: 4px;
                }
              }

              .org-node {
                display: flex;
                align-items: center;
                gap: 8px;
                font-size: 14px;

                .user-count {
                  font-size: 12px;
                  color: $text-tertiary;
                  background: $bg-gray;
                  padding: 2px 6px;
                  border-radius: 10px;
                  margin-left: 4px;
                }
              }
            }
          }

          .org-members {
            border-top: 1px solid $border-color;
            max-height: 300px;

            .members-title {
              padding: 12px 16px 8px;
              font-size: 13px;
              font-weight: 500;
              color: $text-secondary;
            }
          }

          .new-friends {
            flex: 1;
            overflow-y: auto;
            @include web-scrollbar;

            .new-friends-search {
              padding: 12px 16px;
              background: #fff;
              border-bottom: 1px solid #f0f0f0;
            }

            .friend-request-item {
              display: flex;
              padding: 16px;
              border-bottom: 1px solid #f0f0f0;

              .request-info {
                flex: 1;
                margin: 0 12px;

                .request-name {
                  font-size: 14px;
                  font-weight: 500;
                  color: $text-primary;
                }

                .request-message {
                  font-size: 13px;
                  color: $text-secondary;
                  margin-top: 4px;
                }

                .request-time {
                  font-size: 12px;
                  color: $text-tertiary;
                  margin-top: 4px;
                }
              }

              .request-actions {
                display: flex;
                flex-direction: column;
                gap: 6px;

                .request-status {
                  font-size: 13px;
                  color: $text-tertiary;
                }
              }
            }
          }

          // 索引联系人列表
          .indexed-contacts {
            flex: 1;
            overflow-y: auto;
            @include web-scrollbar;
            position: relative;

            .contact-section {
              .section-header {
                padding: 8px 16px;
                background: #f5f5f5;
                font-size: 12px;
                font-weight: 500;
                color: $text-secondary;
                position: sticky;
                top: 0;
                z-index: 1;
              }

              .contact-item-row {
                display: flex;
                align-items: center;
                padding: 12px 16px;
                cursor: pointer;
                transition: background 0.2s ease;
                border-radius: 4px;
                margin: 0 8px;
                margin-bottom: 4px;

                &:hover {
                  background: #f5f7fa;
                }

                &.active {
                  background: $primary-color-light;
                  color: $primary-color;
                }

                .contact-row-info {
                  flex: 1;
                  margin-left: 12px;
                  min-width: 0;

                  .contact-row-name {
                    font-size: 15px;
                    font-weight: 400;
                    color: $text-primary;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  }

                  .contact-row-desc {
                    font-size: 12px;
                    color: $text-tertiary;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    margin-top: 2px;
                  }
                }
              }
            }
          }

          // A-Z 索引侧边栏
          .index-sidebar {
            width: 36px;
            background: #fff;
            border-left: 1px solid #f0f0f0;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 8px 0;
            overflow-y: auto;
            @include web-scrollbar;

            .index-item {
              width: 28px;
              height: 18px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 11px;
              color: $text-secondary;
              cursor: pointer;
              border-radius: 4px;
              margin: 1px 0;
              transition: all 0.15s;

              &:hover:not(.disabled) {
                background: $bg-hover;
                color: $primary-color;
              }

              &.active {
                background: $primary-color;
                color: #fff;
              }

              &.disabled {
                color: #e0e0e0;
                cursor: default;
              }
            }
          }
        }

        .contacts-detail-panel {
          flex: 1;
          background: #fff;
          display: flex;
          flex-direction: column;
          overflow: hidden;
          min-height: 0; // 允许flex子元素正确收缩

          .detail-header {
            padding: 24px;
            display: flex;
            align-items: center;
            gap: 16px;
            border-bottom: 1px solid $border-color;

            .header-info {
              flex: 1;

              h2 {
                margin: 0 0 4px;
                font-size: 20px;
                font-weight: 500;
                color: $text-primary;
              }

              .status {
                margin: 0;
                font-size: 14px;
                color: $text-secondary;

                &.online {
                  color: $success-color;
                }
              }
            }
          }

          .detail-actions {
            padding: 16px 24px;
            border-bottom: 1px solid $border-color;
            display: flex;
            gap: 12px;
          }

          .detail-info {
            flex: 1;
            padding: 24px;
            overflow-y: auto;
            @include web-scrollbar;

            .info-section {
              h4 {
                margin: 0 0 16px;
                font-size: 14px;
                font-weight: 500;
                color: $text-secondary;
              }

              .info-item {
                display: flex;
                justify-content: space-between;
                padding: 10px 0;
                border-bottom: 1px solid #f5f5f5;

                &:last-child {
                  border-bottom: none;
                }

                .label {
                  font-size: 14px;
                  color: $text-secondary;
                }

                .value {
                  font-size: 14px;
                  color: $text-primary;
                }
              }
            }
          }

          .empty-detail {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: $text-tertiary;

            p {
              margin-top: 16px;
              font-size: 14px;
            }
          }
        }
      }

      // 工作台工作区
      .workbench-workspace {
        display: flex;
        width: 100%;

        .workbench-sidebar {
          width: 200px;
          background: #fff;
          border-right: 1px solid $border-color;
          display: flex;
          flex-direction: column;

          .section-title {
            padding: 16px 16px 8px;
            font-size: 13px;
            font-weight: 500;
            color: $text-secondary;
          }

          .app-category-list {
            flex: 1;
            overflow-y: auto;
            @include web-scrollbar;

            .category-item {
              display: flex;
              align-items: center;
              gap: 10px;
              padding: 10px 16px;
              margin: 0 8px;
              cursor: pointer;
              font-size: 14px;
              color: $text-secondary;
              transition: all 0.2s;
              border-radius: $radius-sm;

              .category-label {
                flex: 1;
              }

              .category-count {
                font-size: 12px;
                color: $text-tertiary;
                background: $bg-gray;
                padding: 2px 8px;
                border-radius: 10px;
              }

              &:hover {
                background: $nav-item-hover;
              }

              &.active {
                background: $primary-color-light;
                color: $primary-color;
                font-weight: 500;
              }

              .el-icon {
                font-size: 16px;
              }
            }
          }
        }

        .workbench-content {
          flex: 1;
          background: $bg-light;
          overflow-y: auto;
          @include web-scrollbar;

          .app-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
            gap: 16px;
            padding: 20px;

            .app-card {
              display: flex;
              flex-direction: column;
              align-items: center;
              padding: 16px;
              background: #fff;
              border-radius: $radius-lg;
              cursor: pointer;
              transition: all 0.2s;
              box-shadow: $shadow-sm;
              border: 1px solid $border-color;

              &:hover {
                transform: translateY(-2px);
                box-shadow: $shadow-md;
                border-color: $primary-color;
              }

              .app-icon {
                width: 56px;
                height: 56px;
                border-radius: $radius-lg;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 12px;
              }

              .app-name {
                font-size: 14px;
                color: $text-primary;
                text-align: center;
              }
            }
          }

          // 审批列表
          .approval-list {
            padding: 24px;

            .list-header {
              background: #fff;
              padding: 16px 20px;
              border-radius: 8px 8px 0 0;
              border-bottom: 1px solid $border-color;

              h3 {
                margin: 0 0 12px;
                font-size: 16px;
                font-weight: 500;
              }

              .approval-tabs {
                :deep(.el-tabs__header) {
                  margin: 0;
                }

                :deep(.el-tabs__nav-wrap::after) {
                  display: none;
                }
              }
            }

            .approval-items {
              background: #fff;
              border-radius: 0 0 8px 8px;

              .approval-item {
                display: flex;
                align-items: center;
                padding: 16px 20px;
                border-bottom: 1px solid #f0f0f0;

                &:last-child {
                  border-bottom: none;
                }

                .approval-icon {
                  width: 40px;
                  height: 40px;
                  border-radius: 8px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  margin-right: 16px;
                  flex-shrink: 0;
                }

                .approval-info {
                  flex: 1;

                  .approval-title {
                    font-size: 14px;
                    font-weight: 500;
                    color: $text-primary;
                    margin-bottom: 4px;
                  }

                  .approval-detail {
                    display: flex;
                    gap: 12px;
                    font-size: 13px;
                    color: $text-secondary;
                    margin-bottom: 4px;
                  }

                  .approval-applicant {
                    font-size: 12px;
                    color: $text-tertiary;
                  }
                }

                .approval-actions {
                  display: flex;
                  gap: 8px;
                }
              }
            }
          }

          // 考勤打卡
          .attendance-panel {
            padding: 24px;

            .attendance-card {
              background: linear-gradient(135deg, #1677ff 0%, #69b1ff 100%);
              border-radius: 12px;
              padding: 32px;
              text-align: center;
              color: #fff;
              margin-bottom: 24px;

              .attendance-time {
                font-size: 48px;
                font-weight: 300;
                margin-bottom: 8px;
              }

              .attendance-date {
                font-size: 16px;
                opacity: 0.9;
                margin-bottom: 16px;
              }

              .attendance-status {
                display: inline-block;
                padding: 6px 16px;
                background: rgba(255, 255, 255, 0.2);
                border-radius: 20px;
                font-size: 14px;
                margin-bottom: 20px;

                &.checked {
                  background: rgba(82, 196, 26, 0.3);
                }
              }

              .check-time {
                font-size: 18px;
                font-weight: 500;
              }
            }

            .attendance-records {
              background: #fff;
              border-radius: 12px;
              padding: 20px;

              h4 {
                margin: 0 0 16px;
                font-size: 15px;
                font-weight: 500;
              }

              .record-list {
                .record-item {
                  display: flex;
                  align-items: center;
                  padding: 12px 0;
                  border-bottom: 1px solid #f5f5f5;

                  &:last-child {
                    border-bottom: none;
                  }

                  .record-day {
                    width: 60px;
                    font-size: 14px;
                    color: $text-secondary;
                  }

                  .record-time {
                    flex: 1;
                    font-size: 14px;
                    color: $text-primary;
                  }

                  .record-status {
                    padding: 2px 10px;
                    border-radius: 4px;
                    font-size: 12px;

                    &.normal {
                      background: #f6ffed;
                      color: #52c41a;
                    }

                    &.late {
                      background: #fff7e6;
                      color: #fa8c16;
                    }

                    &.pending {
                      background: #f5f5f5;
                      color: $text-tertiary;
                    }
                  }
                }
              }
            }
          }

          // 日程管理
          .schedule-panel {
            padding: 24px;

            .schedule-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 20px;

              h3 {
                margin: 0;
                font-size: 18px;
                font-weight: 500;
              }
            }

            .schedule-calendar {
              background: #fff;
              border-radius: 12px;
              padding: 20px;
              margin-bottom: 20px;

              .calendar-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 16px;

                .calendar-title {
                  font-size: 15px;
                  font-weight: 500;
                }
              }

              .week-days {
                display: flex;
                gap: 8px;

                .day-item {
                  flex: 1;
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  padding: 12px 8px;
                  border-radius: 8px;
                  cursor: pointer;
                  transition: all 0.2s;

                  .day-name {
                    font-size: 12px;
                    color: $text-secondary;
                    margin-bottom: 4px;
                  }

                  .day-date {
                    font-size: 16px;
                    font-weight: 500;
                    color: $text-primary;
                  }

                  &:hover {
                    background: $bg-hover;
                  }

                  &.selected {
                    background: $primary-color;
                    color: #fff;

                    .day-name,
                    .day-date {
                      color: #fff;
                    }
                  }

                  &.today {
                    border: 1px solid $primary-color;
                  }
                }
              }
            }

            .schedule-list {
              background: #fff;
              border-radius: 12px;
              padding: 16px 20px;
              min-height: 200px;

              .schedule-item {
                display: flex;
                gap: 16px;
                padding: 14px 0;
                border-left: 3px solid $primary-color;
                padding-left: 16px;
                margin-bottom: 12px;

                &:last-child {
                  margin-bottom: 0;
                }

                .schedule-time {
                  font-size: 13px;
                  color: $text-secondary;
                  min-width: 50px;
                }

                .schedule-content {
                  flex: 1;

                  .schedule-title {
                    font-size: 14px;
                    font-weight: 500;
                    color: $text-primary;
                    margin-bottom: 4px;
                  }

                  .schedule-location {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    font-size: 12px;
                    color: $text-tertiary;
                  }
                }
              }
            }
          }
        }
      }

      // 钉盘工作区
      .drive-workspace {
        display: flex;
        width: 100%;

        .drive-sidebar {
          width: 200px;
          background: #fff;
          border-right: 1px solid $border-color;
          display: flex;
          flex-direction: column;

          .section-title {
            padding: 16px 16px 8px;
            font-size: 13px;
            font-weight: 500;
            color: $text-secondary;
          }

          .drive-nav-list {
            flex: 1;
            overflow-y: auto;
            @include web-scrollbar;

            .drive-nav-item {
              display: flex;
              align-items: center;
              gap: 10px;
              padding: 10px 16px;
              cursor: pointer;
              font-size: 14px;
              color: $text-primary;
              transition: background 0.2s;

              .nav-label {
                flex: 1;
              }

              .nav-count {
                font-size: 12px;
                color: $text-tertiary;
                background: $bg-gray;
                padding: 2px 8px;
                border-radius: 10px;
              }

              &:hover {
                background: $bg-hover;
              }

              &.active {
                background: $bg-hover;
                color: $primary-color;
                font-weight: 500;
              }

              .el-icon {
                font-size: 16px;
              }
            }
          }

          .storage-info {
            padding: 16px;
            border-top: 1px solid $border-color;

            .storage-text {
              font-size: 12px;
              color: $text-tertiary;
              text-align: center;
              margin-top: 8px;
            }
          }
        }

        .drive-content {
          flex: 1;
          background: #fff;
          display: flex;
          flex-direction: column;

          .drive-toolbar {
            padding: 16px 20px;
            border-bottom: 1px solid $border-color;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;

            .breadcrumb {
              display: flex;
              align-items: center;
              font-size: 14px;
              color: $text-secondary;

              .breadcrumb-item {
                cursor: pointer;

                &:hover {
                  color: $primary-color;
                }
              }
            }

            .toolbar-actions {
              display: flex;
              gap: 8px;
            }
          }

          .file-list {
            flex: 1;
            padding: 20px;
            overflow-y: auto;
            @include web-scrollbar;
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
            gap: 16px;
            align-content: start;

            .file-item {
              display: flex;
              flex-direction: column;
              align-items: center;
              padding: 16px 12px;
              border-radius: 8px;
              cursor: pointer;
              transition: all 0.2s;
              position: relative;

              &:hover {
                background: $bg-hover;
              }

              &.selected {
                background: #e6f7ff;
                border: 1px solid $primary-color;
              }

              .file-icon {
                margin-bottom: 8px;
                color: $text-secondary;

                &.folder-icon {
                  color: #faad14;
                }

                &.file-type-image {
                  color: #722ed1;
                }

                &.file-type-video {
                  color: #eb2f96;
                }

                &.file-type-audio {
                  color: #13c2c2;
                }

                &.file-type-word {
                  color: #1677ff;
                }

                &.file-type-excel {
                  color: #52c41a;
                }

                &.file-type-ppt {
                  color: #fa8c16;
                }

                &.file-type-pdf {
                  color: #f5222d;
                }
              }

              .file-info {
                text-align: center;
                width: 100%;

                .file-name {
                  font-size: 13px;
                  color: $text-primary;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  width: 100%;
                }

                .file-meta {
                  font-size: 11px;
                  color: $text-tertiary;
                  margin-top: 4px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  width: 100%;
                }
              }

              .file-actions {
                position: absolute;
                top: 8px;
                right: 8px;
                opacity: 0;
                transition: opacity 0.2s;
              }

              &:hover .file-actions {
                opacity: 1;
              }

              &.batch-selected {
                background: #fff7e6;
                border: 1px solid #ffd591;
              }
            }

            // 文件复选框
            .file-checkbox {
              position: absolute;
              top: 8px;
              left: 8px;
              z-index: 2;
            }

            // 批量选择栏
            .batch-selection-bar {
              position: sticky;
              top: 0;
              z-index: 10;
              display: flex;
              align-items: center;
              justify-content: space-between;
              padding: 12px 16px;
              background: #e6f7ff;
              border: 1px solid #91d5ff;
              border-radius: 8px;
              margin-bottom: 16px;

              .selection-info {
                font-size: 14px;
                color: #1677ff;
                font-weight: 500;
              }

              .selection-actions {
                display: flex;
                gap: 8px;
              }
            }

            // 拖拽上传提示
            .drag-upload-overlay {
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              background: rgba(22, 119, 255, 0.1);
              border: 2px dashed #1677ff;
              border-radius: 8px;
              color: #1677ff;
              font-size: 16px;
              z-index: 100;

              p {
                margin-top: 12px;
              }
            }
          }

          // 文件夹树
          .folder-tree {
            flex: 1;
            overflow-y: auto;
            padding: 0 8px;

            .el-tree {
              background: transparent;

              .tree-node {
                display: flex;
                align-items: center;
                gap: 6px;
                font-size: 14px;
                color: $text-primary;

                .tree-label {
                  flex: 1;
                }

                .tree-count {
                  font-size: 12px;
                  color: $text-tertiary;
                }
              }
            }
          }
        }
      }
    }
  }


// 响应式适配
@media (max-width: 1024px) {
  .web-im-layout {
    .header {
      .header-brand {
        min-width: auto;

        .brand-name {
          display: none;
        }
      }
    }

    .main-body {
      .workspace {
        .chat-workspace {
          .session-panel {
            width: 240px;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .web-im-layout {
    .header {
      padding: 0 12px;

      .header-center {
        .search-input {
          max-width: 200px;
        }
      }

      .header-right {
        .user-dropdown {
          .user-name {
            display: none;
          }
        }
      }
    }

    .main-body {
      .nav-sidebar {
        &.collapsed {
          width: 48px;
        }
      }

      .workspace {
        .chat-workspace {
          flex-direction: column;

          .session-panel {
            width: 100%;
            height: 40%;
            border-right: none;
            border-bottom: 1px solid $border-color;
          }

          .chat-panel {
            height: 60%;
          }
        }

        .contacts-workspace {
          .contacts-sidebar {
            width: 160px;
          }
        }

        .workbench-workspace {
          .workbench-sidebar {
            width: 140px;
          }

          .app-grid {
            grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
            gap: 12px;
            padding: 16px;

            .app-card {
              padding: 16px;

              .app-icon {
                width: 48px;
                height: 48px;
              }

              .app-name {
                font-size: 12px;
              }
            }
          }
        }
      }
    }
  }
}

// ==================== 群组功能样式 ====================
.avatar-upload {
  .avatar-placeholder {
    width: 80px;
    height: 80px;
    border: 2px dashed $border-color;
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: $text-tertiary;
    transition: all 0.2s;

    &:hover {
      border-color: $primary-color;
      color: $primary-color;
    }

    span {
      font-size: 12px;
      margin-top: 4px;
    }
  }
}

.member-selector {
  .selected-members {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    min-height: 32px;
  }
}

.member-selector-content {
  .member-search {
    margin-bottom: 16px;
  }

  .member-list {
    max-height: 300px;
    overflow-y: auto;
    @include web-scrollbar;

    .member-list-item {
      display: flex;
      align-items: center;
      padding: 10px;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.2s;

      &:hover {
        background: $bg-hover;
      }

      .member-info {
        flex: 1;
        margin-left: 12px;
        display: flex;
        flex-direction: column;

        .member-name {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
        }

        .member-dept {
          font-size: 12px;
          color: $text-tertiary;
        }
      }
    }
  }
}

.group-manage {
  .group-info-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding-bottom: 20px;
    border-bottom: 1px solid $border-color;
    margin-bottom: 20px;

    .group-info {
      flex: 1;

      h3 {
        margin: 0 0 4px;
        font-size: 18px;
        font-weight: 500;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: $text-secondary;
      }
    }
  }

  .group-members {
    .members-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .member-search-input {
        width: 200px;
      }
    }

    .members-list {
      max-height: 350px;
      overflow-y: auto;
      @include web-scrollbar;

      .group-member-item {
        display: flex;
        align-items: center;
        padding: 12px;
        border-bottom: 1px solid #f5f5f5;

        &:last-child {
          border-bottom: none;
        }

        .member-info {
          flex: 1;
          margin-left: 12px;

          .member-name {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            font-weight: 500;
            color: $text-primary;
          }

          .member-role {
            font-size: 12px;
            color: $text-tertiary;
            margin-top: 4px;
          }
        }
      }
    }
  }
}

// ==================== 新增功能样式 ====================

// 消息引用样式
.message-quote {
  background: rgba(0, 0, 0, 0.05);
  border-left: 3px solid $primary-color;
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: 4px;

  .quote-title {
    font-size: 12px;
    color: $text-secondary;
    margin-bottom: 4px;
  }

  .quote-content {
    font-size: 13px;
    color: $text-tertiary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 200px;
  }
}

// 消息内容中的@提及样式
:deep(.message-bubble) {
  .mention {
    color: $primary-color;
    font-weight: 500;
  }
}

// 回复预览
.reply-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #f0f7ff;
  border-top: 1px solid $border-color;

  .reply-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .reply-label {
      font-size: 12px;
      color: $text-secondary;
    }

    .reply-content {
      font-size: 13px;
      color: $text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 300px;
    }
  }
}

// 表情选择器
.emoji-picker {
  position: absolute;
  bottom: 100%;
  left: 0;
  background: #fff;
  border: 1px solid $border-color;
  border-radius: 8px;
  box-shadow: $shadow-md;
  width: 320px;
  max-height: 280px;
  z-index: 100;

  .emoji-tabs {
    display: flex;
    border-bottom: 1px solid $border-color;
    overflow-x: auto;

    .emoji-tab {
      flex-shrink: 0;
      padding: 8px 12px;
      font-size: 18px;
      cursor: pointer;
      transition: background 0.2s;

      &:hover {
        background: $bg-hover;
      }

      &.active {
        background: $primary-color-light;
        color: $primary-color;
      }
    }
  }

  .emoji-list {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 4px;
    padding: 8px;
    max-height: 200px;
    overflow-y: auto;
    @include web-scrollbar;

    .emoji-item {
      font-size: 20px;
      text-align: center;
      padding: 4px;
      cursor: pointer;
      border-radius: 4px;
      transition: background 0.15s;

      &:hover {
        background: $bg-hover;
      }
    }
  }
}

// @提及建议
.mention-suggestions {
  position: absolute;
  bottom: 100%;
  left: 0;
  background: #fff;
  border: 1px solid $border-color;
  border-radius: 8px;
  box-shadow: $shadow-md;
  width: 240px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;

  .mention-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: $bg-hover;
    }

    &.mention-all {
      background: $primary-color-light;

      &:hover {
        background: color.scale($primary-color-light, $lightness: -5%);
      }

      .mention-all-avatar {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: $primary-color;
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .mention-all-badge {
        margin-left: auto;
        padding: 2px 6px;
        background: $primary-color;
        color: #fff;
        font-size: 11px;
        border-radius: 4px;
      }
    }

    .mention-name {
      flex: 1;
      font-size: 14px;
    }
  }
}

// 群文件面板
.group-files-panel {
  .files-toolbar {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid $border-color;
  }

  .files-list {
    max-height: 400px;
    overflow-y: auto;
  }

  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: $bg-hover;
    }

    .file-icon {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: $text-secondary;
    }

    .file-info {
      flex: 1;
      min-width: 0;

      .file-name {
        font-size: 14px;
        font-weight: 500;
        color: $text-primary;
        @include text-ellipsis;
      }

      .file-meta {
        display: flex;
        gap: 12px;
        margin-top: 4px;
        font-size: 12px;
        color: $text-tertiary;
      }
    }
  }
}

// 消息右键菜单
.message-context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid $border-color;
  border-radius: 8px;
  box-shadow: $shadow-md;
  padding: 4px 0;
  z-index: 1000;
  min-width: 140px;
  animation: dropdownSlide 0.2s $ease-base;

  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    font-size: 14px;
    color: $text-primary;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: $bg-hover;
    }

    .el-icon {
      font-size: 16px;
    }
  }

  .menu-divider {
    height: 1px;
    background: $border-color;
    margin: 4px 0;
  }
}

// 表情反应选择器
.reaction-picker {
  position: fixed;
  background: #fff;
  border: 1px solid $border-color;
  border-radius: 12px;
  box-shadow: $shadow-md;
  padding: 8px;
  z-index: 1001;
  animation: dropdownSlide 0.2s $ease-base;

  .reaction-emoji-list {
    display: flex;
    gap: 4px;

    .reaction-emoji-item {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      border-radius: 8px;
      cursor: pointer;
      transition: all $transition-fast $ease-base;

      &:hover {
        background: $bg-hover;
        transform: scale(1.1);
      }

      &.active {
        background: $primary-color-light;
        border: 1px solid $primary-color;
      }
    }
  }
}

// 消息反应显示
.message-reactions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;

  .reaction-item {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    background: $bg-light;
    border: 1px solid $border-color;
    border-radius: 12px;
    font-size: 12px;
    cursor: pointer;
    transition: all $transition-fast $ease-base;

    &:hover {
      background: $bg-hover;
      border-color: $primary-color;
    }

    &.active {
      background: $primary-color-light;
      border-color: $primary-color;

      .reaction-emoji {
        transform: scale(1.1);
      }
    }

    .reaction-emoji {
      font-size: 14px;
      transition: transform $transition-instant $ease-bounce;
    }

    .reaction-count {
      font-size: 12px;
      color: $text-secondary;
      font-weight: 500;
    }
  }
}

// 消息搜索结果
.search-results {
  max-height: 400px;
  overflow-y: auto;
  @include web-scrollbar;
  margin-top: 16px;

  .search-result-item {
    padding: 12px;
    border-bottom: 1px solid $border-color;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: $bg-hover;
    }

    .result-time {
      font-size: 11px;
      color: $text-tertiary;
      margin-bottom: 4px;
    }

    .result-sender {
      font-size: 13px;
      color: $text-secondary;
      margin-bottom: 4px;
    }

    .result-content {
      font-size: 14px;
      color: $text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 转发对话框
.forward-content {
  .forward-message {
    padding: 12px;
    background: $bg-light;
    border-radius: 8px;
    margin-bottom: 16px;
    font-size: 14px;
    color: $text-primary;
  }

  .search-input {
    margin-bottom: 16px;
  }

  .forward-targets {
    max-height: 300px;
    overflow-y: auto;
    @include web-scrollbar;

    .target-item {
      display: flex;
      align-items: center;
      padding: 10px;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.15s;

      &:hover {
        background: $bg-hover;
      }

      &.selected {
        background: $primary-color-light;
      }

      .target-info {
        margin-left: 12px;

        .target-name {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
        }

        .target-desc {
          font-size: 12px;
          color: $text-tertiary;
        }
      }
    }
  }
}

// 文件预览
.file-preview-content {
  min-height: 300px;

  .preview-image {
    text-align: center;

    :deep(.el-image) {
      max-height: 500px;
    }
  }

  .preview-pdf {
    iframe {
      width: 100%;
      height: 500px;
      border: none;
    }
  }

  .preview-file {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 300px;

    p {
      margin: 16px 0;
      font-size: 16px;
      color: $text-secondary;
    }
  }
}

// 主题设置
.theme-settings {
  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid $border-color;

    &:last-child {
      border-bottom: none;
    }

    .setting-label {
      font-size: 14px;
      color: $text-primary;
    }
  }
}

// 个人资料对话框
.profile-dialog {
  .profile-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 0;
    border-bottom: 1px solid $border-color;
    margin-bottom: 20px;

    .profile-basic {
      h3 {
        margin: 0 0 8px 0;
        font-size: 18px;
        color: $text-primary;
      }

      .user-id {
        margin: 0;
        font-size: 14px;
        color: $text-secondary;
      }
    }
  }
}

// 深色模式
:deep(.dark) {
  background: #1a1a1a;
  color: #e0e0e0;
}

// 消息高亮动画
:deep(.message-item) {
  &.highlight {
    animation: highlightPulse 1s ease-in-out;
  }
}

@keyframes highlightPulse {
  0%,
  100% {
    background: transparent;
  }
  50% {
    background: rgba(22, 119, 255, 0.2);
  }
}

// 按钮激活状态
:deep(.el-button.active) {
  color: $primary-color !important;
  background: rgba(22, 119, 255, 0.1) !important;
}

// 消息已读回执样式
.message-read-receipt {
  font-size: 11px;
  color: $text-tertiary;
  margin-top: 4px;

  &.read {
    color: $success-color;
  }
}

// 消息时间中的已读回执
.read-receipt {
  font-size: 11px;
  color: $text-tertiary;
  margin-left: 8px;

  &.read {
    color: $success-color;
  }
}

// 空会话列表样式
.empty-sessions {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 40px 20px;

  .empty-tip {
    margin: 12px 0 20px;
    color: #999;
    font-size: 14px;
  }
}

// 发起聊天对话框样式
.start-chat-dialog {
  .search-input {
    margin-bottom: 16px;
  }

  .friend-list {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
  }

  .friend-item {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f7fa;
    }

    .friend-info {
      flex: 1;
      margin-left: 12px;

      .friend-name {
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }

      .friend-dept {
        font-size: 12px;
        color: #999;
        margin-top: 4px;
      }
    }

    .arrow-icon {
      color: #ccc;
    }
  }
}

// 在线状态对话框样式
.online-status-dialog {
  .status-option {
    display: flex;
    align-items: center;
    padding: 16px;
    margin-bottom: 12px;
    border: 2px solid $border-color;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: $primary-color;
      background: rgba(22, 119, 255, 0.05);
    }

    &.active {
      border-color: $primary-color;
      background: rgba(22, 119, 255, 0.1);
    }

    .status-icon {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      margin-right: 12px;

      &.status-online {
        background: #52c41a;
        color: white;
      }

      &.status-busy {
        background: #faad14;
        color: white;
      }

      &.status-away {
        background: #1890ff;
        color: white;
      }

      &.status-offline {
        background: #8c8c8c;
        color: white;
      }
    }

    .status-info {
      flex: 1;

      .status-name {
        font-size: 16px;
        font-weight: 500;
        color: $text-primary;
        margin-bottom: 4px;
      }

      .status-desc {
        font-size: 13px;
        color: $text-secondary;
      }
    }

    .status-check {
      color: $primary-color;
      font-size: 20px;
    }
  }
}

// 主题切换对话框样式
.theme-dialog {
  .theme-options {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .theme-option {
      display: flex;
      align-items: center;
      padding: 16px;
      border: 2px solid $border-color;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: $primary-color;
        background: rgba(22, 119, 255, 0.05);
      }

      &.active {
        border-color: $primary-color;
        background: rgba(22, 119, 255, 0.1);
      }

      .theme-preview {
        width: 80px;
        height: 60px;
        border-radius: 6px;
        margin-right: 16px;
        overflow: hidden;
        border: 1px solid $border-color;

        .preview-header {
          height: 12px;
          background: $primary-color;
        }

        .preview-body {
          display: flex;
          height: calc(100% - 12px);

          .preview-sidebar {
            width: 20px;
            background: #f0f0f0;
          }

          .preview-content {
            flex: 1;
            background: #fafafa;
          }
        }

        &.theme-dark {
          .preview-header {
            background: #1677ff;
          }

          .preview-body {
            .preview-sidebar {
              background: #2a2a2a;
            }

            .preview-content {
              background: #1a1a1a;
            }
          }
        }

        &.theme-auto {
          .preview-header {
            background: linear-gradient(90deg, #1677ff 50%, #1a1a1a 50%);
          }

          .preview-body {
            .preview-sidebar {
              background: linear-gradient(180deg, #f0f0f0 50%, #2a2a2a 50%);
            }

            .preview-content {
              background: linear-gradient(180deg, #fafafa 50%, #1a1a1a 50%);
            }
          }
        }
      }

      .theme-info {
        flex: 1;

        .theme-name {
          font-size: 16px;
          font-weight: 500;
          color: $text-primary;
          margin-bottom: 4px;
        }

        .theme-desc {
          font-size: 13px;
          color: $text-secondary;
        }
      }

      .theme-check {
        color: $primary-color;
        font-size: 20px;
      }
    }
  }
}

// 快捷键提示对话框样式
.shortcuts-dialog {
  .shortcut-section {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px solid $border-color;
    }

    .shortcut-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 0;

      .keys {
        display: flex;
        gap: 4px;
        align-items: center;

        kbd {
          display: inline-block;
          padding: 4px 8px;
          font-size: 12px;
          font-family: inherit;
          color: $text-primary;
          background: $bg-light;
          border: 1px solid $border-color;
          border-radius: 4px;
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
        }
      }

      .desc {
        font-size: 13px;
        color: $text-secondary;
      }
    }
  }
}

// 消息通知设置对话框样式
.notification-settings {
  .setting-group {
    margin-bottom: 24px;
    padding-bottom: 24px;
    border-bottom: 1px solid $border-color;

    &:last-child {
      margin-bottom: 0;
      padding-bottom: 0;
      border-bottom: none;
    }

    .group-title {
      font-size: 14px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 16px;
    }

    .setting-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 0;

      .setting-info {
        .setting-label {
          font-size: 14px;
          color: $text-primary;
          margin-bottom: 4px;
        }

        .setting-desc {
          font-size: 12px;
          color: $text-secondary;
        }
      }
    }

    .dnd-time-settings {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 0;

      .time-separator {
        color: $text-secondary;
      }

      :deep(.el-time-picker) {
        width: 140px;
      }
    }
  }
}
</style>
