<template>
  <div class="settings-container">
    <el-tabs v-model="activeTab" tab-position="left" class="settings-tabs">
      <el-tab-pane label="基本设置" name="basic">
        <div class="setting-section">
          <el-card>
            <template #header>
              <span>个人资料</span>
            </template>
            <div class="profile-header">
              <div class="avatar-container">
                <AvatarUpload
                  :avatar="userInfo.avatar"
                  :default-text="userInfo.nickname?.charAt(0) || 'U'"
                  :size="80"
                  @success="handleAvatarSuccess"
                  @error="handleAvatarError"
                />
              </div>
              <div class="user-basic">
                <h3>{{ userInfo.nickname || '用户' }}</h3>
                <p class="user-id">ID: {{ userInfo.userId || '-' }}</p>
              </div>
            </div>

            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="20" />
              </el-form-item>
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio :value="0">男</el-radio>
                  <el-radio :value="1">女</el-radio>
                  <el-radio :value="2">保密</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="手机号" prop="mobile">
                <el-input v-model="profileForm.mobile" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="个性签名" prop="signature">
                <el-input
                  v-model="profileForm.signature"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入个性签名"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="profileLoading" @click="handleSaveProfile">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <span>账号安全</span>
            </template>
            <div class="security-item">
              <div class="security-info">
                <h4>登录密码</h4>
                <p>定期更换密码可以提高账号安全性</p>
              </div>
              <el-button type="primary" link @click="showPasswordDialog = true">修改密码</el-button>
            </div>
            <div class="security-item">
              <div class="security-info">
                <h4>登录设备</h4>
                <p>管理当前登录的设备，移除不信任设备</p>
              </div>
              <el-button type="primary" link @click="showSecurityDialog = true">管理设备</el-button>
            </div>
            <div class="security-item">
              <div class="security-info">
                <h4>登录日志</h4>
                <p>查看最近登录记录，发现异常登录</p>
              </div>
              <el-button type="primary" link @click="showSecurityDialog = true">查看日志</el-button>
            </div>
            <div class="security-item">
              <div class="security-info">
                <h4>两步验证</h4>
                <p>开启两步验证，提高账号安全性</p>
              </div>
              <el-switch v-model="twoFactorEnabled" @change="handleTwoFactorChange" />
            </div>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <div class="card-header-with-action">
                <span>联系方式</span>
                <el-button type="primary" link @click="showContactDialog = true">添加</el-button>
              </div>
            </template>
            <div v-if="contactList.length === 0" class="empty-state">
              <el-empty description="暂无联系方式" />
            </div>
            <div v-else class="contact-list">
              <div v-for="contact in contactList" :key="contact.id" class="contact-item">
                <div class="contact-info">
                  <div class="contact-type">
                    <el-tag :type="getContactTypeColor(contact.type)" size="small">
                      {{ getContactTypeName(contact.type) }}
                    </el-tag>
                  </div>
                  <span class="contact-value">{{ contact.value }}</span>
                </div>
                <div class="contact-actions">
                  <el-tag v-if="contact.verified" type="success" size="small">已验证</el-tag>
                  <el-tag v-else type="warning" size="small">未验证</el-tag>
                  <el-button type="danger" link size="small" @click="handleDeleteContact(contact)"
                    >删除</el-button
                  >
                </div>
              </div>
            </div>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <span>身份认证</span>
            </template>
            <div class="auth-section">
              <div class="auth-item">
                <div class="auth-info">
                  <h4>实名认证</h4>
                  <p>完成实名认证，提高账号安全性</p>
                </div>
                <el-button
                  v-if="!authInfo.realNameVerified"
                  type="primary"
                  link
                  @click="showRealNameDialog = true"
                >
                  立即认证
                </el-button>
                <el-tag v-else type="success">已认证</el-tag>
              </div>
              <div class="auth-item">
                <div class="auth-info">
                  <h4>企业认证</h4>
                  <p>完成企业认证，享受企业专属服务</p>
                </div>
                <el-button
                  v-if="!authInfo.enterpriseVerified"
                  type="primary"
                  link
                  @click="showEnterpriseDialog = true"
                >
                  立即认证
                </el-button>
                <el-tag v-else type="success">已认证</el-tag>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="主题设置" name="theme">
        <div class="setting-section">
          <el-card>
            <template #header>
              <span>外观设置</span>
            </template>
            <div class="theme-setting">
              <div class="setting-row">
                <span class="setting-label">主题模式</span>
                <el-radio-group v-model="themeForm.mode" @change="handleThemeChange">
                  <el-radio-button value="light">浅色</el-radio-button>
                  <el-radio-button value="dark">深色</el-radio-button>
                  <el-radio-button value="auto">跟随系统</el-radio-button>
                </el-radio-group>
              </div>
              <div class="setting-row">
                <span class="setting-label">主题颜色</span>
                <div class="color-options">
                  <div
                    v-for="color in themeColors"
                    :key="color.value"
                    class="color-item"
                    :class="{ active: themeForm.primaryColor === color.value }"
                    :style="{ backgroundColor: color.value }"
                    @click="handleColorChange(color.value)"
                  >
                    <el-icon v-if="themeForm.primaryColor === color.value"><Check /></el-icon>
                  </div>
                </div>
              </div>
              <div class="setting-row">
                <span class="setting-label">字体大小</span>
                <el-slider
                  v-model="themeForm.fontSize"
                  :min="12"
                  :max="18"
                  :step="1"
                  :marks="fontSizeMarks"
                  style="width: 300px"
                  @change="handleFontSizeChange"
                />
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="通知设置" name="notification">
        <div class="setting-section">
          <el-card>
            <template #header>
              <span>消息通知</span>
            </template>
            <div class="setting-item">
              <div class="setting-info">
                <h4>启用通知</h4>
                <p>接收新消息的通知提醒</p>
              </div>
              <el-switch v-model="notificationForm.enabled" @change="handleNotificationChange" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>桌面通知</h4>
                <p>在桌面显示通知弹窗</p>
              </div>
              <el-switch
                v-model="notificationForm.desktop"
                :disabled="!notificationForm.enabled"
                @change="handleNotificationChange"
              />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>声音提醒</h4>
                <p>收到新消息时播放提示音</p>
              </div>
              <el-switch
                v-model="notificationForm.sound"
                :disabled="!notificationForm.enabled"
                @change="handleNotificationChange"
              />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>消息预览</h4>
                <p>在通知中显示消息内容预览</p>
              </div>
              <el-switch
                v-model="notificationForm.preview"
                :disabled="!notificationForm.enabled"
                @change="handleNotificationChange"
              />
            </div>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <span>免打扰设置</span>
            </template>
            <div class="setting-item">
              <div class="setting-info">
                <h4>免打扰模式</h4>
                <p>开启后将不会收到任何通知</p>
              </div>
              <el-switch
                v-model="notificationForm.doNotDisturb"
                @change="handleNotificationChange"
              />
            </div>
            <div v-if="notificationForm.doNotDisturb" class="setting-item">
              <div class="setting-info">
                <h4>免打扰时段</h4>
                <p>设置免打扰的时间段</p>
              </div>
              <div class="time-range">
                <el-time-picker
                  v-model="notificationForm.dndStart"
                  placeholder="开始时间"
                  format="HH:mm"
                />
                <span style="margin: 0 8px">至</span>
                <el-time-picker
                  v-model="notificationForm.dndEnd"
                  placeholder="结束时间"
                  format="HH:mm"
                />
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="隐私设置" name="privacy">
        <div class="setting-section">
          <el-card>
            <template #header>
              <span>隐私设置</span>
            </template>
            <div class="setting-item">
              <div class="setting-info">
                <h4>在线状态</h4>
                <p>谁可以看到我的在线状态</p>
              </div>
              <el-select v-model="privacyForm.onlineStatus" @change="handlePrivacyChange">
                <el-option label="所有人" value="all" />
                <el-option label="仅好友" value="friends" />
                <el-option label="不公开" value="none" />
              </el-select>
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>添加好友验证</h4>
                <p>别人添加我为好友时需要验证</p>
              </div>
              <el-switch v-model="privacyForm.friendVerification" @change="handlePrivacyChange" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>已读回执</h4>
                <p>对方可以看到我是否已读消息</p>
              </div>
              <el-switch v-model="privacyForm.readReceipt" @change="handlePrivacyChange" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>正在输入</h4>
                <p>对方可以看到我正在输入</p>
              </div>
              <el-switch v-model="privacyForm.typingStatus" @change="handlePrivacyChange" />
            </div>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <div class="card-header-with-action">
                <span>黑名单</span>
                <el-button type="primary" link @click="showBlockDialog = true">添加</el-button>
              </div>
            </template>
            <div v-if="blockedUsers.length === 0" class="empty-state">
              <el-empty description="暂无黑名单用户" />
            </div>
            <div v-else class="blocked-list">
              <div v-for="user in blockedUsers" :key="user.id" class="blocked-item">
                <div class="user-info">
                  <el-avatar :size="40" :src="user.avatar">{{ user.name?.charAt(0) }}</el-avatar>
                  <span class="user-name">{{ user.name }}</span>
                </div>
                <el-button type="danger" link @click="handleUnblock(user)">移除</el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="系统设置" name="system">
        <div class="setting-section">
          <el-card>
            <template #header>
              <span>消息管理</span>
            </template>
            <div class="setting-item">
              <div class="setting-info">
                <h4>消息保留时长</h4>
                <p>设置聊天消息的保留时间</p>
              </div>
              <el-select v-model="systemForm.messageRetention" @change="handleSystemChange">
                <el-option label="7天" value="7" />
                <el-option label="30天" value="30" />
                <el-option label="90天" value="90" />
                <el-option label="180天" value="180" />
                <el-option label="永久" value="permanent" />
              </el-select>
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>自动清理</h4>
                <p>自动清理过期的消息记录</p>
              </div>
              <el-switch v-model="systemForm.autoClean" @change="handleSystemChange" />
            </div>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <span>文件存储</span>
            </template>
            <div class="setting-item">
              <div class="setting-info">
                <h4>文件存储路径</h4>
                <p>设置文件下载后的保存位置</p>
              </div>
              <el-button type="primary" link @click="handleSelectPath">选择路径</el-button>
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>自动下载</h4>
                <p>自动下载接收的文件</p>
              </div>
              <el-switch v-model="systemForm.autoDownload" @change="handleSystemChange" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>最大文件大小</h4>
                <p>设置允许发送的最大文件大小</p>
              </div>
              <el-select v-model="systemForm.maxFileSize" @change="handleSystemChange">
                <el-option label="100MB" value="100" />
                <el-option label="200MB" value="200" />
                <el-option label="500MB" value="500" />
                <el-option label="1GB" value="1024" />
              </el-select>
            </div>
          </el-card>

          <el-card style="margin-top: 20px">
            <template #header>
              <span>清理策略</span>
            </template>
            <div class="setting-item">
              <div class="setting-info">
                <h4>清理缓存</h4>
                <p>清理应用缓存数据，释放存储空间</p>
              </div>
              <el-button type="primary" link @click="handleClearCache">立即清理</el-button>
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>清理临时文件</h4>
                <p>清理临时文件和下载记录</p>
              </div>
              <el-button type="primary" link @click="handleClearTempFiles">立即清理</el-button>
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>存储空间</h4>
                <p>当前已使用 {{ systemForm.usedSpace }} / {{ systemForm.totalSpace }}</p>
              </div>
              <el-progress :percentage="systemForm.spacePercentage" :color="getSpaceColor()" />
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px" destroy-on-close>
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入当前密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请确认新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword"
          >确定</el-button
        >
      </template>
    </el-dialog>

    <!-- 账户安全对话框 -->
    <AccountSecurityDialog v-model:visible="showSecurityDialog" />

    <!-- 添加联系方式对话框 -->
    <el-dialog v-model="showContactDialog" title="添加联系方式" width="500px" destroy-on-close>
      <el-form ref="contactFormRef" :model="contactForm" :rules="contactRules" label-width="100px">
        <el-form-item label="联系方式" prop="type">
          <el-select v-model="contactForm.type" placeholder="请选择联系方式类型">
            <el-option label="手机号" value="mobile" />
            <el-option label="邮箱" value="email" />
            <el-option label="QQ" value="qq" />
            <el-option label="微信" value="wechat" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系方式" prop="value">
          <el-input v-model="contactForm.value" placeholder="请输入联系方式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showContactDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddContact">确定</el-button>
      </template>
    </el-dialog>

    <!-- 实名认证对话框 -->
    <el-dialog v-model="showRealNameDialog" title="实名认证" width="500px" destroy-on-close>
      <el-form
        ref="realNameFormRef"
        :model="realNameForm"
        :rules="realNameRules"
        label-width="100px"
      >
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="realNameForm.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="realNameForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="realNameForm.mobile" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRealNameDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRealName">提交认证</el-button>
      </template>
    </el-dialog>

    <!-- 企业认证对话框 -->
    <el-dialog v-model="showEnterpriseDialog" title="企业认证" width="500px" destroy-on-close>
      <el-form
        ref="enterpriseFormRef"
        :model="enterpriseForm"
        :rules="enterpriseRules"
        label-width="100px"
      >
        <el-form-item label="企业名称" prop="name">
          <el-input v-model="enterpriseForm.name" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="enterpriseForm.creditCode" placeholder="请输入统一社会信用代码" />
        </el-form-item>
        <el-form-item label="法人姓名" prop="legalPerson">
          <el-input v-model="enterpriseForm.legalPerson" placeholder="请输入法人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="enterpriseForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEnterpriseDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEnterprise">提交认证</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * @file 设置页面
 * @description IM系统用户设置功能
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import AvatarUpload from '@/components/AvatarUpload/index.vue'
import AccountSecurityDialog from '@/components/Security/AccountSecurityDialog.vue'
import { getCurrentUserInfo, updateProfile, changePassword } from '@/api/im/user'

const router = useRouter()
const store = useStore()

// ==================== 响应式状态 ====================

/** 当前激活的标签 */
const activeTab = ref('basic')

/** 加载状态 */
const profileLoading = ref(false)
const passwordLoading = ref(false)

/** 表单引用 */
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const contactFormRef = ref(null)
const realNameFormRef = ref(null)
const enterpriseFormRef = ref(null)

/** 对话框显示控制 */
const showPasswordDialog = ref(false)
const showBlockDialog = ref(false)
const showSecurityDialog = ref(false)
const showContactDialog = ref(false)
const showRealNameDialog = ref(false)
const showEnterpriseDialog = ref(false)

/** 两步验证状态 */
const twoFactorEnabled = ref(false)

/** 联系方式列表 */
const contactList = ref([
  { id: 1, type: 'mobile', value: '138****8888', verified: true },
  { id: 2, type: 'email', value: 'test@example.com', verified: true },
])

/** 身份认证信息 */
const authInfo = ref({
  realNameVerified: false,
  enterpriseVerified: false,
})

/** 用户信息 */
const userInfo = ref({
  userId: '10001',
  nickname: '测试用户',
  avatar: '',
  mobile: '138****8888',
  email: 'test@example.com',
  gender: 0,
  signature: '',
})

/** 个人资料表单 */
const profileForm = reactive({
  nickname: '',
  gender: 0,
  mobile: '',
  email: '',
  signature: '',
})

/** 个人资料验证规则 */
const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  email: [
    {
      pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      message: '请输入正确的邮箱地址',
      trigger: 'blur',
    },
  ],
  mobile: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur',
    },
  ],
  signature: [{ max: 100, message: '个性签名不能超过100个字符', trigger: 'blur' }],
}

/** 密码表单 */
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

/** 密码验证规则 */
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

/** 主题设置表单 */
const themeForm = reactive({
  mode: 'light',
  primaryColor: '#409eff',
  fontSize: 14,
})

/** 主题颜色选项 */
const themeColors = [
  { label: '默认蓝', value: '#409eff' },
  { label: '清新绿', value: '#67c23a' },
  { label: '活力橙', value: '#e6a23c' },
  { label: '热情红', value: '#f56c6c' },
  { label: '典雅紫', value: '#9b59b6' },
  { label: '稳重灰', value: '#606266' },
]

/** 字体大小标记 */
const fontSizeMarks = {
  12: '小',
  14: '中',
  16: '大',
  18: '特大',
}

/** 通知设置表单 */
const notificationForm = reactive({
  enabled: true,
  desktop: true,
  sound: true,
  preview: true,
  doNotDisturb: false,
  dndStart: null,
  dndEnd: null,
})

/** 隐私设置表单 */
const privacyForm = reactive({
  onlineStatus: 'all',
  friendVerification: true,
  readReceipt: true,
  typingStatus: true,
})

/** 系统设置表单 */
const systemForm = reactive({
  messageRetention: '30',
  autoClean: true,
  filePath: '',
  autoDownload: false,
  maxFileSize: '200',
  usedSpace: '256 MB',
  totalSpace: '1 GB',
  spacePercentage: 25.6,
})

/** 联系方式表单 */
const contactForm = reactive({
  type: '',
  value: '',
})

/** 联系方式验证规则 */
const contactRules = {
  type: [{ required: true, message: '请选择联系方式类型', trigger: 'change' }],
  value: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
}

/** 实名认证表单 */
const realNameForm = reactive({
  name: '',
  idCard: '',
  mobile: '',
})

/** 实名认证验证规则 */
const realNameRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      pattern:
        /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/,
      message: '请输入正确的身份证号',
      trigger: 'blur',
    },
  ],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur',
    },
  ],
}

/** 企业认证表单 */
const enterpriseForm = reactive({
  name: '',
  creditCode: '',
  legalPerson: '',
  phone: '',
})

/** 企业认证验证规则 */
const enterpriseRules = {
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    {
      pattern: /^[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}$/,
      message: '请输入正确的统一社会信用代码',
      trigger: 'blur',
    },
  ],
  legalPerson: [{ required: true, message: '请输入法人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur',
    },
  ],
}

/** 黑名单用户列表 */
const blockedUsers = ref([])

// ==================== 方法定义 ====================

/**
 * 初始化表单数据
 */
const initFormData = () => {
  Object.assign(profileForm, {
    nickname: userInfo.value.nickname,
    gender: userInfo.value.gender,
    mobile: userInfo.value.mobile,
    email: userInfo.value.email,
    signature: userInfo.value.signature,
  })
}

/**
 * 头像上传成功
 */
const handleAvatarSuccess = avatarUrl => {
  userInfo.value.avatar = avatarUrl
  store.commit('user/SET_AVATAR', avatarUrl)
  ElMessage.success('头像更新成功')
}

/**
 * 头像上传失败
 */
const handleAvatarError = error => {
  ElMessage.error(error || '头像上传失败')
}

/**
 * 保存个人资料
 */
const handleSaveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
  } catch (error) {
    return
  }

  profileLoading.value = true
  try {
    const userId = userInfo.value.userId
    await updateProfile(userId, profileForm)
    Object.assign(userInfo.value, profileForm)

    store.commit('user/SET_NAME', profileForm.nickname)
    store.commit('user/SET_AVATAR', userInfo.value.avatar)

    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    profileLoading.value = false
  }
}

/**
 * 修改密码
 */
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate()
  passwordLoading.value = true

  try {
    const userId = userInfo.value.userId
    await changePassword(userId, passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    showPasswordDialog.value = false
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    // 清除Token并跳转登录页
    localStorage.removeItem('token')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

/**
 * 主题模式变化
 */
const handleThemeChange = mode => {
  // 应用主题
  if (mode === 'dark') {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
  localStorage.setItem('theme-mode', mode)
  ElMessage.success('主题已切换')
}

/**
 * 主题颜色变化
 */
const handleColorChange = color => {
  themeForm.primaryColor = color
  document.documentElement.style.setProperty('--el-color-primary', color)
  localStorage.setItem('theme-color', color)
}

/**
 * 字体大小变化
 */
const handleFontSizeChange = size => {
  document.documentElement.style.setProperty('--base-font-size', `${size}px`)
  localStorage.setItem('font-size', size)
}

/**
 * 系统设置变化
 */
const handleSystemChange = () => {
  localStorage.setItem('system-settings', JSON.stringify(systemForm))
  ElMessage.success('设置已保存')
}

/**
 * 选择文件存储路径
 */
const handleSelectPath = () => {
  ElMessage.info('请在系统设置中配置文件存储路径')
}

/**
 * 清理缓存
 */
const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清理应用缓存吗？此操作不可恢复。', '确认清理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    ElMessage.loading('正在清理缓存...')
    setTimeout(() => {
      systemForm.usedSpace = '128 MB'
      systemForm.spacePercentage = 12.8
      ElMessage.success('缓存清理成功')
    }, 1500)
  } catch {
    // 取消操作
  }
}

/**
 * 清理临时文件
 */
const handleClearTempFiles = async () => {
  try {
    await ElMessageBox.confirm('确定要清理临时文件吗？此操作不可恢复。', '确认清理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    ElMessage.loading('正在清理临时文件...')
    setTimeout(() => {
      systemForm.usedSpace = '64 MB'
      systemForm.spacePercentage = 6.4
      ElMessage.success('临时文件清理成功')
    }, 1500)
  } catch {
    // 取消操作
  }
}

/**
 * 获取存储空间颜色
 */
const getSpaceColor = () => {
  const percentage = systemForm.spacePercentage
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

/**
 * 通知设置变化
 */
const handleNotificationChange = () => {
  // 如果启用桌面通知，请求权限
  if (notificationForm.desktop && 'Notification' in window) {
    if (Notification.permission !== 'granted') {
      Notification.requestPermission()
    }
  }
  localStorage.setItem('notification-settings', JSON.stringify(notificationForm))
  ElMessage.success('设置已保存')
}

/**
 * 隐私设置变化
 */
const handlePrivacyChange = () => {
  localStorage.setItem('privacy-settings', JSON.stringify(privacyForm))
  ElMessage.success('设置已保存')
}

/**
 * 两步验证开关变化
 */
const handleTwoFactorChange = value => {
  if (value) {
    showSecurityDialog.value = true
  } else {
    ElMessage.warning('请在账户安全对话框中关闭两步验证')
    twoFactorEnabled.value = true
  }
}

/**
 * 获取联系方式类型名称
 */
const getContactTypeName = type => {
  const typeMap = {
    mobile: '手机号',
    email: '邮箱',
    qq: 'QQ',
    wechat: '微信',
    other: '其他',
  }
  return typeMap[type] || '其他'
}

/**
 * 获取联系方式类型颜色
 */
const getContactTypeColor = type => {
  const colorMap = {
    mobile: 'primary',
    email: 'success',
    qq: 'warning',
    wechat: 'success',
    other: 'info',
  }
  return colorMap[type] || 'info'
}

/**
 * 添加联系方式
 */
const handleAddContact = () => {
  contactFormRef.value.validate(valid => {
    if (valid) {
      const newContact = {
        id: Date.now(),
        type: contactForm.type,
        value: contactForm.value,
        verified: false,
      }
      contactList.value.push(newContact)
      ElMessage.success('联系方式添加成功')
      showContactDialog.value = false
      contactForm.type = ''
      contactForm.value = ''
    }
  })
}

/**
 * 删除联系方式
 */
const handleDeleteContact = async contact => {
  try {
    await ElMessageBox.confirm(`确定要删除该联系方式吗？`, '确认删除', {
      type: 'warning',
    })
    const index = contactList.value.findIndex(c => c.id === contact.id)
    if (index > -1) {
      contactList.value.splice(index, 1)
      ElMessage.success('联系方式删除成功')
    }
  } catch {
    // 取消操作
  }
}

/**
 * 提交实名认证
 */
const handleSubmitRealName = () => {
  realNameFormRef.value.validate(valid => {
    if (valid) {
      ElMessage.loading('正在提交认证信息...')
      setTimeout(() => {
        authInfo.value.realNameVerified = true
        ElMessage.success('实名认证提交成功，请等待审核')
        showRealNameDialog.value = false
        realNameForm.name = ''
        realNameForm.idCard = ''
        realNameForm.mobile = ''
      }, 1500)
    }
  })
}

/**
 * 提交企业认证
 */
const handleSubmitEnterprise = () => {
  enterpriseFormRef.value.validate(valid => {
    if (valid) {
      ElMessage.loading('正在提交认证信息...')
      setTimeout(() => {
        authInfo.value.enterpriseVerified = true
        ElMessage.success('企业认证提交成功，请等待审核')
        showEnterpriseDialog.value = false
        enterpriseForm.name = ''
        enterpriseForm.creditCode = ''
        enterpriseForm.legalPerson = ''
        enterpriseForm.phone = ''
      }, 1500)
    }
  })
}

/**
 * 解除黑名单
 */
const handleUnblock = async user => {
  try {
    await ElMessageBox.confirm(`确定要将"${user.name}"移出黑名单吗？`, '确认', {
      type: 'warning',
    })
    const index = blockedUsers.value.findIndex(u => u.id === user.id)
    if (index > -1) {
      blockedUsers.value.splice(index, 1)
      ElMessage.success('已移出黑名单')
    }
  } catch {
    // 取消操作
  }
}

/**
 * 加载本地设置
 */
const loadLocalSettings = () => {
  // 加载主题设置
  const themeMode = localStorage.getItem('theme-mode')
  if (themeMode) {
    themeForm.mode = themeMode
    if (themeMode === 'dark') {
      document.documentElement.classList.add('dark')
    }
  }

  const themeColor = localStorage.getItem('theme-color')
  if (themeColor) {
    themeForm.primaryColor = themeColor
    document.documentElement.style.setProperty('--el-color-primary', themeColor)
  }

  const fontSize = localStorage.getItem('font-size')
  if (fontSize) {
    themeForm.fontSize = parseInt(fontSize)
  }

  // 加载通知设置
  const notificationSettings = localStorage.getItem('notification-settings')
  if (notificationSettings) {
    Object.assign(notificationForm, JSON.parse(notificationSettings))
  }

  // 加载隐私设置
  const privacySettings = localStorage.getItem('privacy-settings')
  if (privacySettings) {
    Object.assign(privacyForm, JSON.parse(privacySettings))
  }
}

/**
 * 加载当前用户信息
 */
const loadUserInfo = async () => {
  try {
    const response = await getCurrentUserInfo()
    if (response.code === 200 && response.data) {
      userInfo.value = {
        userId: response.data.id,
        nickname: response.data.nickname,
        avatar: response.data.avatar,
        mobile: response.data.mobile,
        email: response.data.email,
        gender: response.data.gender || 0,
        signature: response.data.signature || '',
      }
      initFormData()
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadUserInfo()
  loadLocalSettings()
})
</script>

<style lang="scss" scoped>
.settings-container {
  height: 100%;
  padding: 20px;
  background-color: #f5f5f5;

  .settings-tabs {
    height: 100%;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    :deep(.el-tabs__header) {
      margin-right: 0;
      border-right: 1px solid #e4e7ed;
      width: 150px;

      .el-tabs__item {
        height: 50px;
        line-height: 50px;
        text-align: left;
        padding-left: 20px;

        &.is-active {
          background-color: #ecf5ff;
        }
      }
    }

    :deep(.el-tabs__content) {
      padding: 20px;
      height: 100%;
      overflow-y: auto;
    }
  }
}

.setting-section {
  max-width: 800px;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;

  .avatar-container {
    margin-right: 24px;
    text-align: center;

    .avatar-upload {
      margin-top: 12px;
    }
  }

  .user-basic {
    h3 {
      margin: 0 0 8px;
      font-size: 20px;
    }

    .user-id {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }
}

.profile-form {
  max-width: 500px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;

  .security-info {
    h4 {
      margin: 0 0 4px;
      font-size: 14px;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 12px;
    }
  }
}

.theme-setting {
  .setting-row {
    display: flex;
    align-items: center;
    margin-bottom: 24px;

    .setting-label {
      width: 100px;
      font-size: 14px;
      color: #606266;
    }

    .color-options {
      display: flex;
      gap: 12px;

      .color-item {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: transform 0.2s;

        &:hover {
          transform: scale(1.1);
        }

        &.active {
          box-shadow:
            0 0 0 2px #fff,
            0 0 0 4px currentColor;
        }

        .el-icon {
          color: #fff;
        }
      }
    }
  }
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;

  &:last-child {
    border-bottom: none;
  }

  .setting-info {
    flex: 1;
    margin-right: 16px;

    h4 {
      margin: 0 0 4px;
      font-size: 14px;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 12px;
    }
  }

  .time-range {
    display: flex;
    align-items: center;
  }
}

.card-header-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  padding: 40px 0;
}

.contact-list {
  .contact-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .contact-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .contact-value {
        font-size: 14px;
        color: #606266;
      }
    }

    .contact-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

.auth-section {
  .auth-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .auth-info {
      h4 {
        margin: 0 0 4px;
        font-size: 14px;
        font-weight: 500;
      }

      p {
        margin: 0;
        color: #909399;
        font-size: 12px;
      }
    }
  }
}

.blocked-list {
  .blocked-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .user-name {
        font-size: 14px;
      }
    }
  }
}
</style>
