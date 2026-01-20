<template>
  <div class="user-profile-panel">
    <div class="profile-content">
      <div class="cover-image"></div>

      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-section">
            <div class="avatar">
              <span class="avatar-text">我</span>
            </div>
            <div class="online-indicator"></div>
          </div>

          <div class="profile-info">
            <div class="info-top">
              <div class="name-section">
                <h1 class="user-name">王小明</h1>
                <el-badge class="status-badge" type="success">在线</el-badge>
              </div>
              <p class="user-position">产品设计师 · 产品部</p>
            </div>

            <div class="action-buttons">
              <el-button type="primary" class="message-btn">
                <el-icon><ChatDotRound /></el-icon>
                发送消息
              </el-button>
              <el-button class="qr-btn">
                <el-icon><GridIcon /></el-icon>
                我的二维码
              </el-button>
              <el-button text class="icon-btn">
                <el-icon><Share /></el-icon>
              </el-button>
              <el-button text class="icon-btn">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
            </div>
          </div>

          <el-button class="edit-btn">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </div>

        <el-divider class="profile-divider" />

        <div class="contact-section">
          <h2 class="section-title">联系方式</h2>
          <div class="contact-grid">
            <div class="contact-item">
              <div class="contact-icon icon-blue">
                <el-icon><Phone /></el-icon>
              </div>
              <div class="contact-details">
                <p class="contact-label">手机号码</p>
                <p class="contact-value">138-1234-5678</p>
              </div>
            </div>

            <div class="contact-item">
              <div class="contact-icon icon-purple">
                <el-icon><Message /></el-icon>
              </div>
              <div class="contact-details">
                <p class="contact-label">邮箱地址</p>
                <p class="contact-value">wangxiaoming@company.com</p>
              </div>
            </div>
          </div>
        </div>

        <el-divider class="profile-divider" />

        <div class="work-section">
          <h2 class="section-title">工作信息</h2>
          <div class="work-grid">
            <div class="work-item">
              <div class="work-icon icon-green">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <div class="work-details">
                <p class="work-label">所属部门</p>
                <p class="work-value">产品部 / 设计组</p>
              </div>
            </div>

            <div class="work-item">
              <div class="work-icon icon-orange">
                <el-icon><Briefcase /></el-icon>
              </div>
              <div class="work-details">
                <p class="work-label">职位</p>
                <p class="work-value">高级产品设计师</p>
              </div>
            </div>

            <div class="work-item">
              <div class="work-icon icon-red">
                <el-icon><Location /></el-icon>
              </div>
              <div class="work-details">
                <p class="work-label">办公地点</p>
                <p class="work-value">北京市朝阳区望京SOHO</p>
              </div>
            </div>

            <div class="work-item">
              <div class="work-icon icon-cyan">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="work-details">
                <p class="work-label">入职时间</p>
                <p class="work-value">2022年6月15日</p>
              </div>
            </div>
          </div>
        </div>

        <el-divider class="profile-divider" />

        <div class="other-section">
          <h2 class="section-title">其他信息</h2>
          <div class="other-list">
            <div class="other-item">
              <span class="other-label">工号</span>
              <span class="other-value">20220615001</span>
            </div>
            <div class="other-item">
              <span class="other-label">直属上级</span>
              <span class="other-value link">张伟 (产品总监)</span>
            </div>
            <div class="other-item">
              <span class="other-label">汇报对象</span>
              <span class="other-value link">李明 (设计总监)</span>
            </div>
          </div>
        </div>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">本月考勤</span>
            <el-badge class="stat-badge" type="success">正常</el-badge>
          </div>
          <p class="stat-value">22天</p>
          <p class="stat-subtitle">迟到0次 · 早退0次</p>
        </div>

        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">待办任务</span>
            <el-badge class="stat-badge" type="warning">进行中</el-badge>
          </div>
          <p class="stat-value">8个</p>
          <p class="stat-subtitle">已完成15个</p>
        </div>

        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">团队协作</span>
            <el-badge class="stat-badge" type="primary">活跃</el-badge>
          </div>
          <p class="stat-value">156次</p>
          <p class="stat-subtitle">本月沟通次数</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  ChatDotRound,
  Grid as GridIcon,
  Share,
  MoreFilled,
  Edit,
  Phone,
  Message,
  OfficeBuilding,
  Briefcase,
  Location,
  Calendar
} from '@element-plus/icons-vue'
</script>

<style scoped>
.user-profile-panel {
  flex: 1;
  background-color: #f7f8fa;
  overflow-y: auto;
}

.profile-content {
  max-width: 896px;
  margin: 0 auto;
}

.cover-image {
  height: 192px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6, #2563eb);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.1);
  }
}

.profile-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  margin: -64px 24px 24px;
  position: relative;
  z-index: 10;
  padding: 24px;
}

.profile-header {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 24px;
}

.avatar-section {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 96px;
  height: 96px;
  border-radius: 16px;
  background: linear-gradient(135deg, #60a5fa, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 4px #fff;
}

.avatar-text {
  font-size: 36px;
  font-weight: 600;
}

.online-indicator {
  position: absolute;
  bottom: -4px;
  right: -4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #52c41a;
  border: 2px solid #fff;
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-top {
  .name-section {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .user-name {
    font-size: 24px;
    font-weight: 500;
    color: #262626;
    margin: 0;
  }

  .status-badge {
    :deep(.el-badge__content) {
      background-color: #e6f7ff;
      color: #1890ff;
      border: none;
    }
  }

  .user-position {
    font-size: 14px;
    color: #595959;
    margin: 0;
  }
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.message-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.qr-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.icon-btn {
  .el-icon {
    font-size: 16px;
  }
}

.edit-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.profile-divider {
  margin: 24px 0;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin: 0 0 16px 0;
}

.contact-grid,
.work-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.contact-item,
.work-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.contact-icon,
.work-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .el-icon {
    font-size: 20px;
  }

  &.icon-blue {
    background-color: #e6f7ff;
    color: #1890ff;
  }

  &.icon-purple {
    background-color: #f9f0ff;
    color: #722ed1;
  }

  &.icon-green {
    background-color: #f6ffed;
    color: #52c41a;
  }

  &.icon-orange {
    background-color: #fff7e6;
    color: #fa8c16;
  }

  &.icon-red {
    background-color: #fff1f0;
    color: #f5222d;
  }

  &.icon-cyan {
    background-color: #e6fffb;
    color: #13c2c2;
  }
}

.contact-details,
.work-details {
  flex: 1;
}

.contact-label,
.work-label {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0 0 4px 0;
}

.contact-value,
.work-value {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.other-section {
  .other-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.other-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;

  .other-label {
    font-size: 14px;
    color: #595959;
  }

  .other-value {
    font-size: 14px;
    font-weight: 500;
    color: #262626;

    &.link {
      color: #1890ff;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.stat-title {
  font-size: 14px;
  color: #595959;
}

.stat-badge {
  :deep(.el-badge__content) {
    background-color: #f0f0f0;
    color: #595959;
    border: none;
  }
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 4px 0;
}

.stat-subtitle {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0;
}
</style>
