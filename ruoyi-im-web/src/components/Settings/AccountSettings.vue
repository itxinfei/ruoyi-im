<template>
  <div class="account-settings">
    <!-- 基本信息 -->
    <section class="setting-section">
      <div class="section-header">
        <h3 class="section-title">基本信息</h3>
        <p class="section-desc">管理您的个人资料和展示信息</p>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-content">
            <span class="item-label">头像</span>
            <span class="item-desc">支持 jpg, png, gif 格式，最大 2MB</span>
          </div>
          <div class="item-action">
            <el-avatar :size="48" :src="user.avatar" class="user-avatar" />
            <el-button link type="primary" @click="$emit('edit-profile')">更换头像</el-button>
          </div>
        </div>
        
        <el-divider class="card-divider" />
        
        <div class="setting-item">
          <div class="item-content">
            <span class="item-label">昵称</span>
          </div>
          <div class="item-action">
            <span class="item-value">{{ user.nickname || user.username }}</span>
            <el-button link type="primary" icon="Edit" @click="$emit('edit-profile')"></el-button>
          </div>
        </div>

        <el-divider class="card-divider" />
        
        <div class="setting-item">
          <div class="item-content">
            <span class="item-label">账号 ID</span>
          </div>
          <div class="item-action">
            <span class="item-value text-mono">{{ user.username }}</span>
          </div>
        </div>

        <el-divider class="card-divider" />
        
        <div class="setting-item">
          <div class="item-content">
            <span class="item-label">UID</span>
          </div>
          <div class="item-action">
            <span class="item-value text-mono">{{ user.id }}</span>
          </div>
        </div>
        
        <el-divider class="card-divider" />
        
        <div class="setting-item">
          <div class="item-content">
            <span class="item-label">所属部门</span>
          </div>
          <div class="item-action">
            <span class="item-value">{{ user.dept?.deptName || '暂无部门' }}</span>
          </div>
        </div>
        
        <el-divider class="card-divider" />
        
        <div class="setting-item">
          <div class="item-content">
            <span class="item-label">联系方式</span>
          </div>
          <div class="item-action vertical">
            <div class="contact-row">
              <el-icon><Message /></el-icon>
              <span>{{ user.email || '未绑定邮箱' }}</span>
            </div>
            <div class="contact-row">
              <el-icon><Iphone /></el-icon>
              <span>{{ user.phonenumber || '未绑定手机' }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 账号安全 -->
    <section class="setting-section">
      <div class="section-header">
        <h3 class="section-title">账号安全</h3>
        <p class="section-desc">保护您的账号安全和隐私</p>
      </div>
      
      <div class="setting-card">
        <div class="setting-item clickable" @click="$emit('change-password')">
          <div class="item-content">
            <div class="label-with-icon">
              <el-icon class="item-icon"><Lock /></el-icon>
              <span class="item-label">登录密码</span>
            </div>
            <span class="item-desc">定期修改密码可以保护账号安全</span>
          </div>
          <div class="item-action">
            <span class="status-text success">已设置</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <el-divider class="card-divider" />
        
        <div class="setting-item">
          <div class="item-content">
            <div class="label-with-icon">
              <el-icon class="item-icon"><Key /></el-icon>
              <span class="item-label">双重验证</span>
            </div>
            <span class="item-desc">启用后，登录时需要进行二次验证</span>
          </div>
          <div class="item-action">
            <el-switch v-model="twoFactorEnabled" disabled />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowRight, Lock, Key, Message, Iphone, Edit } from '@element-plus/icons-vue'

defineProps({
  user: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

defineEmits(['edit-profile', 'change-password'])

const twoFactorEnabled = ref(false)
</script>

<style scoped lang="scss">
.account-settings {
  max-width: 800px;
  margin: 0 auto;
}

.setting-section {
  margin-bottom: 32px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  margin-bottom: 16px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color-primary);
    margin: 0 0 4px 0;
  }
  
  .section-desc {
    font-size: 13px;
    color: var(--text-color-secondary);
    margin: 0;
  }
}

.setting-card {
  background: var(--bg-color-overlay);
  border: 1px solid var(--border-color-light);
  border-radius: var(--dt-radius-md);
  overflow: hidden;
}

.card-divider {
  margin: 0;
  border-color: var(--border-color-lighter);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  min-height: 24px;
  transition: background 0.2s;
  
  &.clickable {
    cursor: pointer;
    
    &:hover {
      background: var(--bg-color-hover);
    }
  }

  .item-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 4px;
    
    .item-label {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-color-primary);
    }
    
    .item-desc {
      font-size: 12px;
      color: var(--text-color-secondary);
    }
    
    .label-with-icon {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .item-icon {
        font-size: 16px;
        color: var(--text-color-secondary);
      }
    }
  }

  .item-action {
    display: flex;
    align-items: center;
    gap: 12px;
    
    &.vertical {
      flex-direction: column;
      align-items: flex-end;
      gap: 4px;
    }
    
    .item-value {
      font-size: 14px;
      color: var(--text-color-regular);
      
      &.text-mono {
        font-family: monospace;
        background: var(--bg-color-hover);
        padding: 2px 6px;
        border-radius: var(--dt-radius-sm);
        font-size: 13px;
      }
    }
    
    .status-text {
      font-size: 13px;
      
      &.success {
        color: var(--el-color-success);
      }
    }
    
    .arrow-icon {
      font-size: 16px;
      color: var(--text-color-placeholder);
    }
    
    .contact-row {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: var(--text-color-regular);
      
      .el-icon {
        color: var(--text-color-secondary);
      }
    }
  }
}

// 深色模式适配
.dark {
  .setting-card {
    background: #252525;
    border-color: #333;
  }
  
  .card-divider {
    border-color: #333;
  }
  
  .setting-item {
    &.clickable:hover {
      background: #333;
    }
    
    .item-action .item-value.text-mono {
      background: #333;
    }
  }
}
</style>