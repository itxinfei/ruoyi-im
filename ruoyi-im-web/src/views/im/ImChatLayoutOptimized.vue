<template>
  <div class="web-im-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <!-- Logo和品牌 -->
      <div class="header-brand">
        <div class="logo">
          <el-icon :size="24" color="#1677ff">
            <ChatLineSquare />
          </el-icon>
        </div>
        <span class="brand-name">企业IM</span>
      </div>

      <!-- 中间：搜索框或模块标题 -->
      <div class="header-center">
        <el-input
          v-if="activeModule === 'chat'"
          v-model="sessionSearch"
          placeholder="搜索会话、联系人..."
          :prefix-icon="Search"
          class="search-input"
          clearable
        />
        <div v-else class="module-title">
          {{ getModuleTitle(activeModule) }}
        </div>
      </div>

      <!-- 右侧：用户操作 -->
      <div class="header-right">
        <!-- 快捷操作 -->
        <div class="quick-actions">
          <el-tooltip content="通知" placement="bottom">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge-item">
              <el-button :icon="Bell" text />
            </el-badge>
          </el-tooltip>
          <el-tooltip content="设置" placement="bottom">
            <el-button :icon="Setting" text @click="showSettings" />
          </el-tooltip>
        </div>

        <!-- 用户信息 -->
        <el-dropdown trigger="click" placement="bottom-end" @command="handleUserCommand">
          <div class="user-dropdown">
            <el-avatar :size="32" :src="currentUser?.avatar">
              {{ currentUser?.name?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="user-name">{{ currentUser?.name || '用户' }}</span>
            <el-icon :size="12"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人资料
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                系统设置
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
      <!-- 左侧导航栏 -->
      <aside class="nav-sidebar" :class="{ collapsed: isNavCollapsed }">
        <nav class="nav-list">
          <div
            v-for="item in navModules"
            :key="item.key"
            class="nav-item"
            :class="{ active: activeModule === item.key }"
            @click="switchModule(item.key)"
          >
            <el-icon class="nav-icon">
              <component :is="item.icon" />
            </el-icon>
            <span v-if="!isNavCollapsed" class="nav-label">{{ item.label }}</span>
            <el-badge
              v-if="item.key === 'chat' && unreadCount > 0 && !isNavCollapsed"
              :value="unreadCount"
              class="nav-badge"
            />
          </div>
        </nav>

        <!-- 折叠按钮 -->
        <div class="collapse-trigger" @click="toggleNavCollapse">
          <el-icon>
            <component :is="isNavCollapsed ? ArrowRight : ArrowLeft" />
          </el-icon>
        </div>
      </aside>

      <!-- 内容工作区 -->
      <main class="workspace">
        <!-- 消息模块 -->
        <div v-if="activeModule === 'chat'" class="chat-workspace">
          <!-- 会话列表 -->
          <div class="session-panel">
            <div class="session-header">
              <el-input
                v-model="sessionSearch"
                placeholder="搜索会话..."
                :prefix-icon="Search"
                size="small"
                clearable
                class="session-search"
              />
              <el-button :icon="Plus" size="small" circle @click="showCreateGroupDialog" />
            </div>
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
                  <el-avatar :size="40" :src="session.avatar">
                    {{ session.name?.charAt(0) || 'U' }}
                  </el-avatar>
                </el-badge>
                <div class="session-info">
                  <div class="session-top">
                    <span class="session-name">{{ session.name }}</span>
                    <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
                  </div>
                  <div class="session-preview">{{ session.lastMessage || '暂无消息' }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 聊天内容 -->
          <div class="chat-panel">
            <div v-if="currentSessionId" class="chat-container">
              <!-- 聊天头部 -->
              <div class="chat-header">
                <div class="chat-title">
                  <el-avatar :size="36" :src="currentSession?.avatar">
                    {{ currentSession?.name?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div>
                    <div class="title-name">{{ currentSession?.name }}</div>
                    <div class="title-status">在线</div>
                  </div>
                </div>
                <div class="chat-actions">
                  <el-button :icon="Phone" circle size="small" />
                  <el-button :icon="VideoCamera" circle size="small" />
                  <el-button :icon="More" circle size="small" />
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
                  <el-avatar
                    v-if="!msg.isOwn && !(msg.senderId === currentUser?.userId)"
                    :size="36"
                    :src="msg.senderAvatar || msg.avatar"
                  >
                    {{ (msg.senderName || msg.sender?.name)?.charAt(0) || 'U' }}
                  </el-avatar>
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
                        :src="msg.content"
                        :preview-src-list="[msg.content]"
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
                        <div class="file-name">{{ msg.fileName || '文件' }}</div>
                        <div class="file-size">
                          {{ msg.fileSize ? formatFileSize(msg.fileSize) : '' }}
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
                    </div>
                  </div>
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
                  <el-tooltip content="历史记录 (Ctrl+H)" placement="top">
                    <el-button :icon="Search" text @click="showMessageSearch" />
                  </el-tooltip>
                </div>
                <el-input
                  ref="inputRef"
                  v-model="inputMessage"
                  type="textarea"
                  :rows="2"
                  :autosize="{ minRows: 2, maxRows: 6 }"
                  placeholder="输入消息... @提及、Enter发送、Ctrl+Enter换行"
                  @keydown="handleInputKeydown"
                  @input="handleInputChange"
                />
                <div class="input-footer">
                  <span class="input-tip">Enter 发送 · @提及 · Ctrl+H 搜索</span>
                  <el-button type="primary" size="small" @click="sendMessage">发送</el-button>
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
                  :key="contact.id"
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
              <el-empty v-if="friendRequests.length === 0" description="暂无新朋友请求" />
            </div>

            <!-- A-Z 索引侧边栏 -->
            <div
              v-if="contactCategory === 'friends' || contactCategory === 'groups'"
              class="index-sidebar"
            >
              <div
                v-for="letter in indexLetters"
                :key="letter"
                class="index-item"
                :class="{
                  active: activeLetter === letter,
                  disabled: !searchedGroups.find(g => g.letter === letter),
                }"
                @click="scrollToLetter(letter)"
              >
                {{ letter }}
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

            <div class="section-title" style="margin-top: 20px">存储空间</div>
            <div class="storage-info">
              <el-progress :percentage="storagePercent" :stroke-width="6" />
              <div class="storage-text">已使用 {{ storageUsed }} / {{ storageTotal }}</div>
            </div>
          </div>

          <!-- 钉盘内容 -->
          <div class="drive-content">
            <div class="drive-toolbar">
              <div class="breadcrumb">
                <span class="breadcrumb-item" @click="navigateToRoot">根目录</span>
                <span v-for="(crumb, index) in breadcrumbs" :key="index" class="breadcrumb-item">
                  / {{ crumb.name }}
                </span>
              </div>
              <div class="toolbar-actions">
                <el-upload
                  :show-file-list="false"
                  :before-upload="handleFileUpload"
                  :multiple="true"
                >
                  <el-button :icon="Upload">上传文件</el-button>
                </el-upload>
                <el-button :icon="Folder" @click="createFolder">新建文件夹</el-button>
                <el-button :icon="Download" :disabled="!selectedFile">下载</el-button>
                <el-button :icon="Delete" :disabled="!selectedFile" @click="deleteFile"
                  >删除</el-button
                >
              </div>
            </div>

            <!-- 文件列表 -->
            <div class="file-list">
              <!-- 文件夹 -->
              <div
                v-for="folder in currentFolders"
                :key="'folder-' + folder.id"
                class="file-item folder-item"
                :class="{ selected: selectedFile?.id === folder.id }"
                @click="selectFile(folder)"
                @dblclick="openFolder(folder)"
              >
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
                v-for="file in currentFiles"
                :key="'file-' + file.id"
                class="file-item"
                :class="{ selected: selectedFile?.id === file.id }"
                @click="selectFile(file)"
                @dblclick="openFile(file)"
              >
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
                  <el-button :icon="Share" size="small" circle @click.stop="shareFile(file)" />
                  <el-button
                    :icon="Download"
                    size="small"
                    circle
                    @click.stop="downloadSingleFile(file)"
                  />
                </div>
              </div>

              <el-empty
                v-if="currentFolders.length === 0 && currentFiles.length === 0"
                description="暂无文件"
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

    <!-- 消息搜索对话框 -->
    <el-dialog
      v-model="messageSearchDialogVisible"
      title="搜索聊天记录"
      width="600px"
      :append-to-body="true"
    >
      <el-input
        v-model="messageSearchKeyword"
        placeholder="搜索消息内容..."
        :prefix-icon="Search"
        clearable
        class="search-input"
        @input="searchMessages"
      />
      <div class="search-results">
        <div
          v-for="result in searchResults"
          :key="result.id"
          class="search-result-item"
          @click="jumpToMessage(result)"
        >
          <div class="result-time">{{ formatTime(result.timestamp) }}</div>
          <div class="result-sender">{{ result.senderName }}</div>
          <div class="result-content">{{ highlightKeyword(result.content) }}</div>
        </div>
        <el-empty
          v-if="searchResults.length === 0 && messageSearchKeyword"
          description="没有找到相关消息"
        />
      </div>
    </el-dialog>

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

    <!-- 主题设置对话框 -->
    <el-dialog v-model="themeSettingsVisible" title="主题设置" width="400px" :append-to-body="true">
      <div class="theme-settings">
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
      </div>
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
  Search,
  More,
  Paperclip,
  Picture,
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
} from '@element-plus/icons-vue'
import { formatTime as formatTimeUtil } from '@/utils/format/time'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { useVoiceRecorder } from '@/utils/audio/useVoiceRecorder'
import { listSession } from '@/api/im/session'
import { uploadFile, uploadImage } from '@/api/im/file'
import { sendMessage as apiSendMessage } from '@/api/im/message'
import {
  listContact,
  addContact,
  searchContacts,
  getReceivedFriendRequests,
  handleFriendRequest as apiHandleFriendRequest,
} from '@/api/im/contact'

const router = useRouter()
const route = useRoute()
const store = useStore()

// WebSocket
const { isConnected, connect, disconnect, sendMessage: wsSendMessage } = useImWebSocket()

// 状态
const isNavCollapsed = ref(false)
const activeModule = ref('chat')
const sessionSearch = ref('')
const inputMessage = ref('')

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
const friends = ref([])
const groupSessions = ref([]) // 群组会话列表
const orgTree = ref([])
const orgMembers = ref([])
const currentDept = ref(null)
const friendRequests = ref([])
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
  const groups = {}
  indexLetters.forEach(letter => {
    groups[letter] = []
  })

  groupSessions.value.forEach(group => {
    const letter = getFirstLetter(group.name || group.groupName)
    if (!groups[letter]) groups[letter] = []
    groups[letter].push(group)
  })

  const result = []
  indexLetters.forEach(letter => {
    if (groups[letter].length > 0) {
      result.push({
        letter,
        contacts: groups[letter].sort((a, b) => {
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

// 搜索过滤后的分组好友
const searchedGroups = computed(() => {
  if (!contactSearch.value) {
    return contactCategory.value === 'friends' ? groupedFriends.value : groupedGroups.value
  }

  const keyword = contactSearch.value.toLowerCase()
  const sourceList = contactCategory.value === 'friends' ? friends.value : groupSessions.value

  const filtered = sourceList.filter(item => {
    const name = (item.name || item.groupName || item.nickname)?.toLowerCase() || ''
    return name.includes(keyword)
  })

  // 将搜索结果重新分组
  const groups = {}
  indexLetters.forEach(letter => {
    groups[letter] = []
  })

  filtered.forEach(item => {
    const letter = getFirstLetter(item.name || item.groupName || item.nickname)
    if (!groups[letter]) groups[letter] = []
    groups[letter].push(item)
  })

  const result = []
  indexLetters.forEach(letter => {
    if (groups[letter].length > 0) {
      result.push({
        letter,
        contacts: groups[letter],
      })
    }
  })

  return result
})

// 过滤好友列表（保留用于兼容）
const filteredFriends = computed(() => {
  return friends.value
})

// 加载好友列表
const loadFriends = async () => {
  try {
    const res = await listContact()
    const dataRows = res.rows || res.data?.rows || res.data || []
    friends.value = Array.isArray(dataRows)
      ? dataRows.map(f => ({
          ...f,
          online: f.status === 'ACTIVE',
          name: f.remark || f.friendName || f.name,
        }))
      : []
  } catch (error) {
    console.error('加载好友列表失败:', error)
    friends.value = []
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
const initOrgTree = () => {
  orgTree.value = [
    {
      id: 1,
      name: '总公司',
      userCount: 100,
      children: [
        {
          id: 11,
          name: '技术部',
          userCount: 35,
          children: [
            { id: 111, name: '前端组', userCount: 12 },
            { id: 112, name: '后端组', userCount: 15 },
            { id: 113, name: '测试组', userCount: 8 },
          ],
        },
        {
          id: 12,
          name: '产品部',
          userCount: 20,
          children: [
            { id: 121, name: '产品设计', userCount: 8 },
            { id: 122, name: '用户研究', userCount: 12 },
          ],
        },
        {
          id: 13,
          name: '市场部',
          userCount: 25,
          children: [
            { id: 131, name: '销售组', userCount: 15 },
            { id: 132, name: '推广组', userCount: 10 },
          ],
        },
        {
          id: 14,
          name: '人事部',
          userCount: 10,
        },
        {
          id: 15,
          name: '财务部',
          userCount: 10,
        },
      ],
    },
  ]
}

// 组织架构节点点击
const handleOrgNodeClick = async data => {
  currentDept.value = data
  // 模拟获取部门成员
  if (data.userCount) {
    orgMembers.value = [
      {
        id: `u_${Date.now()}_1`,
        name: '张三',
        nickname: '张三',
        avatar: null,
        position: '工程师',
        deptName: data.name,
        online: true,
        isFriend: false,
      },
      {
        id: `u_${Date.now()}_2`,
        name: '李四',
        nickname: '李四',
        avatar: null,
        position: '设计师',
        deptName: data.name,
        online: false,
        isFriend: false,
      },
      {
        id: `u_${Date.now()}_3`,
        name: '王五',
        nickname: '王五',
        avatar: null,
        position: '产品经理',
        deptName: data.name,
        online: true,
        isFriend: true,
      },
    ]
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
])

// 当前文件夹和文件
const currentFolders = computed(() => {
  return allFolders.value.filter(f => f.parentId === currentFolderId.value)
})

const currentFiles = computed(() => {
  return allFiles.value.filter(f => f.folderId === currentFolderId.value)
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
}

// 选择文件/文件夹
const selectFile = file => {
  selectedFile.value = file
}

// 打开文件夹
const openFolder = folder => {
  currentFolderId.value = folder.id
  breadcrumbs.value.push({ id: folder.id, name: folder.name })
  selectedFile.value = null
}

// 打开文件
const openFile = file => {
  ElMessage.info(`打开文件: ${file.name}`)
  // TODO: 实现文件预览
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
const downloadSingleFile = file => {
  ElMessage.info(`下载文件: ${file.name}`)
  // TODO: 实现文件下载
}

// 当前用户
const currentUser = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return {
    name: userInfo.nickName || userInfo.userName || '用户',
    avatar: userInfo.avatar || null,
    userId: userInfo.userId,
  }
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
  }
  return titles[key] || ''
}

const toggleNavCollapse = () => {
  isNavCollapsed.value = !isNavCollapsed.value
  localStorage.setItem('navCollapsed', String(isNavCollapsed.value))
}

const switchModule = moduleKey => {
  activeModule.value = moduleKey
  // 只更新URL用于状态保持，不触发路由导航
  // 使用replace避免产生历史记录
  const validRoutes = ['chat', 'contacts', 'drive']
  if (validRoutes.includes(moduleKey)) {
    // 钉盘模块跳转到文件管理页面
    if (moduleKey === 'drive') {
      router.push('/im/file')
    } else {
      router.push(`/im/${moduleKey}`)
    }
  } else {
    // 工作台是内嵌模块，不更新路由
    // 只更新浏览器URL状态（可选）
    window.history.replaceState(null, '', `/im/${moduleKey}`)
  }
}

const selectSession = async session => {
  // 切换会话
  await store.dispatch('im/switchSession', session)
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

    await store.dispatch('im/sendMessage', messageData)

    // 通过 WebSocket 发送
    if (isConnected.value) {
      wsSendMessage(messageData)
    }

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

    await store.dispatch('im/sendMessage', messageData)

    // 通过 WebSocket 发送
    if (isConnected.value) {
      wsSendMessage(messageData)
    }

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

const openApp = appKey => {
  ElMessage.info(`打开应用：${appKey}`)
}

const showSettings = () => {
  themeSettingsVisible.value = true
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

// 消息搜索
const messageSearchDialogVisible = ref(false)
const messageSearchKeyword = ref('')
const searchResults = ref([])

// 转发消息
const forwardDialogVisible = ref(false)
const forwardSearchKeyword = ref('')
const selectedForwardTarget = ref(null)
const forwardTargets = ref([])

// 文件预览
const filePreviewDialogVisible = ref(false)
const previewingFile = ref(null)

// 主题设置
const themeSettingsVisible = ref(false)
const isDarkMode = ref(localStorage.getItem('darkMode') === 'true')
const messageSoundEnabled = ref(localStorage.getItem('messageSoundEnabled') !== 'false')
const desktopNotificationEnabled = ref(
  localStorage.getItem('desktopNotificationEnabled') !== 'false'
)

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

// 格式化消息内容（处理@提及和换行）
const formatMessageContent = content => {
  if (!content) return ''
  // 转义HTML
  let formatted = content.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  // 处理@提及
  formatted = formatted.replace(/@([^\s]+)/g, '<span class="mention">@$1</span>')
  // 处理换行
  formatted = formatted.replace(/\n/g, '<br>')
  return formatted
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

  // Ctrl+H 搜索消息
  if (e.ctrlKey && e.key === 'h') {
    e.preventDefault()
    showMessageSearch()
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

// 显示消息搜索
const showMessageSearch = () => {
  messageSearchDialogVisible.value = true
  messageSearchKeyword.value = ''
  searchResults.value = []
}

// 搜索消息
const searchMessages = () => {
  if (!messageSearchKeyword.value.trim()) {
    searchResults.value = []
    return
  }

  const keyword = messageSearchKeyword.value.toLowerCase()
  const allMessages = store.state.im.messages || []

  searchResults.value = allMessages
    .filter(msg => msg.content?.toLowerCase().includes(keyword))
    .map(msg => ({
      ...msg,
      sessionId: currentSessionId.value,
    }))
    .slice(0, 50) // 限制结果数量
}

// 高亮关键词
const highlightKeyword = content => {
  if (!messageSearchKeyword.value) return content
  const keyword = messageSearchKeyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${keyword})`, 'gi')
  return content.replace(regex, '<span style="background: #ffeb3b;">$1</span>')
}

// 跳转到消息
const jumpToMessage = message => {
  messageSearchDialogVisible.value = false
  // 滚动到指定消息
  nextTick(() => {
    const element = document.querySelector(`[data-message-id="${message.id}"]`)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' })
      element.classList.add('highlight')
      setTimeout(() => element.classList.remove('highlight'), 2000)
    }
  })
}

// 文件预览相关
const isImageFile = file => {
  if (!file) return false
  const ext = (file.name || '').split('.').pop()?.toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)
}

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

// 修改发送消息函数以支持引用回复
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSessionId.value) return

  const messageData = {
    sessionId: currentSessionId.value,
    type: 'text',
    content: inputMessage.value.trim(),
  }

  // 添加引用回复
  if (replyingMessage.value) {
    messageData.replyTo = {
      messageId: replyingMessage.value.id,
      senderName: replyingMessage.value.senderName,
      content: replyingMessage.value.content,
    }
    replyingMessage.value = null
  }

  // 发送消息
  await store.dispatch('im/sendMessage', messageData)

  // 通过 WebSocket 发送
  if (isConnected.value) {
    wsSendMessage(messageData)
  }

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
    ElMessage.info('个人资料')
  } else if (command === 'settings') {
    showSettings()
  }
}

// 初始化
const init = async () => {
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

  // 连接 WebSocket
  connect()

  // 加载会话列表
  try {
    await loadSessions()
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }

  // 加载联系人数据
  try {
    await loadFriends()
    await loadFriendRequests()
    initOrgTree()
  } catch (error) {
    console.error('加载联系人数据失败:', error)
  }

  // 尝试重发离线消息
  try {
    await store.dispatch('im/resendOfflineMessages')
  } catch (error) {
    console.error('重发离线消息失败:', error)
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
      peerId: s.peerId,
      groupId: s.groupId,
      unreadCount: s.unreadCount || 0,
      lastMessage: s.lastMessage?.content || s.lastMessage,
      lastMessageTime: s.lastMessage?.timestamp || s.updatedAt,
      pinned: s.pinned || false,
      muted: s.muted || false,
    }))

    store.commit('im/SET_SESSIONS', formattedSessions)
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
  if (!sessionSearch.value) return sessions.value
  const keyword = sessionSearch.value.toLowerCase()
  return sessions.value.filter(s => {
    const name = (s.name || '').toLowerCase()
    return name.includes(keyword)
  })
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
})

// 组件卸载
onUnmounted(() => {
  // 这里不主动断开 WebSocket，因为其他组件可能也在使用
})
</script>

<style lang="scss" scoped>
@use 'sass:color';

// Web IM 布局变量
$nav-width: 180px;
$nav-width-collapsed: 56px;
$header-height: 56px;
$session-panel-width: 280px;
$primary-color: #1677ff;
$primary-color-light: #e8f3ff;
$success-color: #52c41a;
$warning-color: #faad14;
$danger-color: #ff4d4f;
$bg-gray: #f5f5f5;
$bg-base: #f0f2f5;
$bg-light: #fafafa;
$bg-white: #ffffff;
$bg-hover: #e6f7ff;
$border-color: #e8e8e8;
$border-base: #d9d9d9;
$text-primary: #262626;
$text-secondary: #595959;
$text-tertiary: #8c8c8c;
$text-light: #bfbfbf;
$shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
$shadow-md:
  0 4px 6px -1px rgba(0, 0, 0, 0.1),
  0 2px 4px -1px rgba(0, 0, 0, 0.06);
$shadow-lg:
  0 10px 15px -3px rgba(0, 0, 0, 0.1),
  0 4px 6px -2px rgba(0, 0, 0, 0.05);

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

  // 顶部导航栏
  .header {
    height: $header-height;
    min-height: $header-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    background: #fff;
    border-bottom: 1px solid $border-color;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    z-index: 100;

    .header-brand {
      display: flex;
      align-items: center;
      gap: 10px;
      min-width: 140px;

      .logo {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        background: linear-gradient(135deg, #1677ff 0%, #69b1ff 100%);
        border-radius: 8px;
      }

      .brand-name {
        font-size: 18px;
        font-weight: 600;
        color: $text-primary;
        letter-spacing: 0.5px;
      }
    }

    .header-center {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      max-width: 500px;

      .search-input {
        width: 100%;
        max-width: 400px;

        :deep(.el-input__wrapper) {
          border-radius: 20px;
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
      }

      .module-title {
        font-size: 16px;
        font-weight: 500;
        color: $text-primary;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 8px;

      .quick-actions {
        display: flex;
        align-items: center;
        gap: 4px;

        .el-button {
          color: $text-secondary;
          padding: 8px;

          &:hover {
            color: $primary-color;
            background: $bg-hover;
          }
        }

        .badge-item {
          :deep(.el-badge__content) {
            transform: translateY(-50%) translateX(50%);
          }
        }
      }

      .user-dropdown {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 12px;
        border-radius: 20px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: $bg-gray;
        }

        .el-avatar {
          background: linear-gradient(135deg, #1677ff 0%, #69b1ff 100%);
        }

        .user-name {
          font-size: 14px;
          color: $text-primary;
          max-width: 80px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  // 主体区域
  .main-body {
    flex: 1;
    display: flex;
    overflow: hidden;

    // 左侧导航栏
    .nav-sidebar {
      width: $nav-width;
      background: #fff;
      border-right: 1px solid $border-color;
      display: flex;
      flex-direction: column;
      transition: width 0.2s ease;
      position: relative;

      &.collapsed {
        width: $nav-width-collapsed;

        .nav-label,
        .nav-badge {
          display: none;
        }

        .nav-item {
          justify-content: center;
          padding: 12px 0;
        }
      }

      .nav-list {
        flex: 1;
        padding: 12px 8px;

        .nav-item {
          display: flex;
          align-items: center;
          padding: 10px 12px;
          margin-bottom: 4px;
          cursor: pointer;
          color: $text-secondary;
          border-radius: 6px;
          transition: all 0.2s;
          position: relative;

          .nav-icon {
            font-size: 20px;
            margin-right: 10px;
          }

          .nav-label {
            flex: 1;
            font-size: 14px;
          }

          .nav-badge {
            :deep(.el-badge__content) {
              transform: translateY(-50%) translateX(0);
            }
          }

          &:hover {
            background: $bg-gray;
            color: $text-primary;
          }

          &.active {
            background: $bg-hover;
            color: $primary-color;
            font-weight: 500;
          }
        }
      }

      .collapse-trigger {
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: $text-tertiary;
        border-top: 1px solid $border-color;
        transition: all 0.2s;

        &:hover {
          background: $bg-gray;
          color: $text-primary;
        }
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

          .session-header {
            padding: 12px 16px;
            border-bottom: 1px solid $border-color;
            display: flex;
            gap: 8px;
            align-items: center;

            .session-search {
              flex: 1;
            }
          }

          .session-list {
            flex: 1;
            height: 0;
            overflow-y: auto;
            @include web-scrollbar;

            .session-item {
              display: flex;
              align-items: center;
              padding: 12px 16px;
              cursor: pointer;
              border-bottom: 1px solid #f0f0f0;
              transition: all 0.15s;

              &:hover {
                background: $bg-gray;
              }

              &.active {
                background: $bg-hover;
              }

              .el-badge {
                margin-right: 12px;
                flex-shrink: 0;
              }

              .session-info {
                flex: 1;
                min-width: 0;

                .session-top {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 4px;

                  .session-name {
                    font-size: 14px;
                    font-weight: 500;
                    color: $text-primary;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  }

                  .session-time {
                    font-size: 12px;
                    color: $text-tertiary;
                    flex-shrink: 0;
                  }
                }

                .session-preview {
                  font-size: 12px;
                  color: $text-tertiary;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
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
              padding: 0 20px;
              display: flex;
              align-items: center;
              justify-content: space-between;
              border-bottom: 1px solid $border-color;

              .chat-title {
                display: flex;
                align-items: center;
                gap: 12px;

                .title-name {
                  font-size: 16px;
                  font-weight: 500;
                  color: $text-primary;
                }

                .title-status {
                  font-size: 12px;
                  color: $success-color;
                }
              }

              .chat-actions {
                display: flex;
                gap: 8px;

                .el-button {
                  --el-button-border-color: transparent;
                  --el-button-bg-color: transparent;
                  --el-button-text-color: $text-secondary;
                  --el-button-hover-text-color: $primary-color;
                  --el-button-hover-bg-color: $bg-hover;
                }
              }
            }

            .message-area {
              flex: 1;
              padding: 20px;
              overflow-y: auto;
              @include web-scrollbar;

              .connection-status {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
                padding: 12px;
                margin-bottom: 16px;
                background: #fff7e6;
                border: 1px solid #ffd591;
                border-radius: 8px;
                color: #fa8c16;
                font-size: 13px;

                .el-icon {
                  animation: pulse 1.5s infinite;
                }
              }

              .message-item {
                display: flex;
                margin-bottom: 20px;

                &.isOwn {
                  flex-direction: row-reverse;

                  .message-content {
                    align-items: flex-end;

                    .message-bubble {
                      background: $primary-color;
                      color: #fff;
                    }

                    .message-time {
                      text-align: right;
                    }
                  }
                }

                .message-content {
                  max-width: 60%;

                  .sender-name {
                    font-size: 12px;
                    color: $text-tertiary;
                    margin-bottom: 6px;
                  }

                  .message-bubble {
                    padding: 10px 14px;
                    border-radius: 8px;
                    background: $bg-gray;
                    color: $text-primary;
                    font-size: 14px;
                    line-height: 1.5;
                    word-break: break-word;

                    &.sending {
                      opacity: 0.7;
                    }

                    &.failed {
                      background: #fff1f0;
                      color: $danger-color;
                    }
                  }

                  // 图片消息
                  .message-image {
                    max-width: 200px;

                    .image-content {
                      width: 200px;
                      height: 200px;
                      border-radius: 8px;
                      overflow: hidden;
                      background: #f0f0f0;

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
                    background: $bg-gray;
                    border-radius: 8px;
                    min-width: 200px;

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

                      &.failed {
                        color: $danger-color;
                        cursor: pointer;

                        &:hover {
                          color: #d9363e;
                        }
                      }

                      &.sent {
                        color: $success-color;
                      }
                    }
                  }
                }
              }
            }

            .input-area {
              padding: 16px 20px;
              border-top: 1px solid $border-color;

              .voice-recording-panel {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 12px 16px;
                margin-bottom: 12px;
                background: #fff7e6;
                border: 1px solid #ffd591;
                border-radius: 8px;

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
                      background: linear-gradient(to top, $primary-color, #69b1ff);
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
                margin-bottom: 12px;

                .el-button {
                  --el-button-border-color: transparent;
                  --el-button-bg-color: transparent;
                  --el-button-text-color: $text-secondary;
                  padding: 8px;

                  &:hover {
                    --el-button-text-color: $primary-color;
                    --el-button-hover-bg-color: $bg-hover;
                  }

                  &.recording {
                    --el-button-text-color: $danger-color;
                    animation: pulse 1.5s infinite;
                  }
                }
              }

              .input-footer {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-top: 8px;

                .input-tip {
                  font-size: 12px;
                  color: $text-tertiary;
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
            color: $text-light;

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

        .contacts-sidebar {
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

          .tree-list {
            flex: 1;
            overflow-y: auto;
            @include web-scrollbar;

            .tree-item {
              display: flex;
              align-items: center;
              gap: 10px;
              padding: 10px 16px;
              cursor: pointer;
              font-size: 14px;
              color: $text-primary;
              transition: background 0.2s;

              .tree-label {
                flex: 1;
              }

              .tree-count {
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
        }

        .contacts-list-panel {
          width: 320px;
          background: #fff;
          border-right: 1px solid $border-color;
          display: flex;
          flex-direction: column;

          .list-header {
            padding: 12px 16px;
            border-bottom: 1px solid $border-color;
            display: flex;
            gap: 8px;
            align-items: center;

            .search-input {
              flex: 1;
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
              padding: 12px 16px;
              cursor: pointer;
              border-bottom: 1px solid #f0f0f0;
              transition: background 0.2s;
              position: relative;

              &:hover {
                background: $bg-hover;
              }

              .contact-info {
                flex: 1;
                margin-left: 12px;
                min-width: 0;

                .contact-name {
                  font-size: 14px;
                  font-weight: 500;
                  color: $text-primary;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                }

                .contact-signature {
                  font-size: 12px;
                  color: $text-tertiary;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  margin-top: 2px;
                }
              }

              .online-dot {
                width: 8px;
                height: 8px;
                background: $success-color;
                border-radius: 50%;
                flex-shrink: 0;
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
                height: 36px;

                &:hover {
                  background: $bg-hover;
                }
              }

              .org-node {
                display: flex;
                align-items: center;
                gap: 6px;
                font-size: 14px;

                .user-count {
                  font-size: 12px;
                  color: $text-tertiary;
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
                padding: 10px 16px;
                cursor: pointer;
                border-bottom: 1px solid #f0f0f0;
                transition: background 0.15s;

                &:hover {
                  background: $bg-hover;
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
            color: $text-light;

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
          width: 180px;
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
              cursor: pointer;
              font-size: 14px;
              color: $text-primary;
              transition: background 0.2s;

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
        }

        .workbench-content {
          flex: 1;
          background: $bg-base;
          overflow-y: auto;
          @include web-scrollbar;

          .app-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
            gap: 20px;
            padding: 24px;

            .app-card {
              display: flex;
              flex-direction: column;
              align-items: center;
              padding: 20px;
              background: #fff;
              border-radius: 8px;
              cursor: pointer;
              transition: all 0.2s;
              box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

              &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
              }

              .app-icon {
                width: 56px;
                height: 56px;
                border-radius: 12px;
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
    border: 2px dashed $border-base;
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
  border-top: 1px solid $border-base;

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
  border: 1px solid $border-base;
  border-radius: 8px;
  box-shadow: $shadow-lg;
  width: 320px;
  max-height: 280px;
  z-index: 100;

  .emoji-tabs {
    display: flex;
    border-bottom: 1px solid $border-base;
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
  border: 1px solid $border-base;
  border-radius: 8px;
  box-shadow: $shadow-lg;
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
    border-bottom: 1px solid $border-base;
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
  border: 1px solid $border-base;
  border-radius: 8px;
  box-shadow: $shadow-lg;
  padding: 4px 0;
  z-index: 1000;
  min-width: 140px;

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
    background: $border-base;
    margin: 4px 0;
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
    border-bottom: 1px solid $border-base;
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
    border-bottom: 1px solid $border-base;

    &:last-child {
      border-bottom: none;
    }

    .setting-label {
      font-size: 14px;
      color: $text-primary;
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
</style>
