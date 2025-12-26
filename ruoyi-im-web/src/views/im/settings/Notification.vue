<template>
  <div class="notification-settings">
    <el-card class="setting-card">
      <template #header>
        <div>
          <span>{{ $t('settings.messageNotification') }}</span>
        </div>
      </template>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.enableNotification') }}</h4>
          <p>{{ $t('settings.enableNotificationDesc') }}</p>
        </div>
        <el-switch v-model="settings.enableNotification" @change="handleNotificationChange" />
      </div>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.desktopNotification') }}</h4>
          <p>{{ $t('settings.desktopNotificationDesc') }}</p>
        </div>
        <el-switch
          v-model="settings.desktopNotification"
          :disabled="!settings.enableNotification"
          @change="handleDesktopNotificationChange"
        />
      </div>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.soundNotification') }}</h4>
          <p>{{ $t('settings.soundNotificationDesc') }}</p>
        </div>
        <el-switch
          v-model="settings.soundNotification"
          :disabled="!settings.enableNotification"
          @change="handleSoundChange"
        />
      </div>
    </el-card>

    <el-card class="setting-card">
      <template #header>
        <div>
          <span>{{ $t('settings.notificationSound') }}</span>
        </div>
      </template>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.messageSound') }}</h4>
          <p>{{ $t('settings.messageSoundDesc') }}</p>
        </div>
        <div class="sound-selector">
          <el-select
            v-model="settings.messageSound"
            :disabled="!settings.soundNotification"
            @change="handleSoundTypeChange"
          >
            <el-option
              v-for="sound in soundOptions"
              :key="sound.value"
              :label="sound.label"
              :value="sound.value"
            />
          </el-select>
          <el-button
            type="text"
            icon="el-icon-video-play"
            :disabled="!settings.soundNotification"
            @click="playSound(settings.messageSound)"
          >
            {{ $t('settings.preview') }}
          </el-button>
        </div>
      </div>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.callSound') }}</h4>
          <p>{{ $t('settings.callSoundDesc') }}</p>
        </div>
        <div class="sound-selector">
          <el-select
            v-model="settings.callSound"
            :disabled="!settings.soundNotification"
            @change="handleCallSoundChange"
          >
            <el-option
              v-for="sound in callSoundOptions"
              :key="sound.value"
              :label="sound.label"
              :value="sound.value"
            />
          </el-select>
          <el-button
            type="text"
            icon="el-icon-video-play"
            :disabled="!settings.soundNotification"
            @click="playSound(settings.callSound)"
          >
            {{ $t('settings.preview') }}
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="setting-card">
      <template #header>
        <div>
          <span>{{ $t('settings.notificationFilter') }}</span>
        </div>
      </template>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.notifyWhenMentioned') }}</h4>
          <p>{{ $t('settings.notifyWhenMentionedDesc') }}</p>
        </div>
        <el-switch
          v-model="settings.notifyWhenMentioned"
          :disabled="!settings.enableNotification"
          @change="handleSettingChange"
        />
      </div>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.muteGroupNotification') }}</h4>
          <p>{{ $t('settings.muteGroupNotificationDesc') }}</p>
        </div>
        <el-switch
          v-model="settings.muteGroupNotification"
          :disabled="!settings.enableNotification"
          @change="handleSettingChange"
        />
      </div>

      <div class="setting-item">
        <div class="setting-info">
          <h4>{{ $t('settings.quietHours') }}</h4>
          <p>{{ $t('settings.quietHoursDesc') }}</p>
        </div>
        <div class="quiet-hours">
          <el-switch
            v-model="settings.enableQuietHours"
            :disabled="!settings.enableNotification"
            @change="handleQuietHoursChange"
          />
          <el-time-picker
            v-if="settings.enableQuietHours"
            v-model="settings.quietHoursStart"
            format="HH:mm"
            :placeholder="$t('settings.startTime')"
            :disabled="!settings.enableNotification"
            @change="handleSettingChange"
          />
          <el-time-picker
            v-if="settings.enableQuietHours"
            v-model="settings.quietHoursEnd"
            format="HH:mm"
            :placeholder="$t('settings.endTime')"
            :disabled="!settings.enableNotification"
            @change="handleSettingChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { updateNotificationSettings } from '@/api/im/config'

export default {
  name: 'NotificationSettings',
  data() {
    return {
      settings: {
        enableNotification: true,
        desktopNotification: true,
        soundNotification: true,
        messageSound: 'default',
        callSound: 'ringtone',
        notifyWhenMentioned: true,
        muteGroupNotification: false,
        enableQuietHours: false,
        quietHoursStart: null,
        quietHoursEnd: null,
      },
      soundOptions: [
        { label: this.$t('settings.defaultSound'), value: 'default' },
        { label: this.$t('settings.bell'), value: 'bell' },
        { label: this.$t('settings.chime'), value: 'chime' },
        { label: this.$t('settings.drop'), value: 'drop' },
        { label: this.$t('settings.none'), value: 'none' },
      ],
      callSoundOptions: [
        { label: this.$t('settings.defaultRingtone'), value: 'ringtone' },
        { label: this.$t('settings.classic'), value: 'classic' },
        { label: this.$t('settings.electronic'), value: 'electronic' },
        { label: this.$t('settings.none'), value: 'none' },
      ],
    }
  },
  computed: {
    ...mapGetters(['userSettings']),
  },
  created() {
    this.initSettings()
  },
  methods: {
    initSettings() {
      if (this.userSettings.notification) {
        this.settings = {
          ...this.settings,
          ...this.userSettings.notification,
        }
      }
    },
    async handleSettingChange() {
      try {
        await this.saveSettings()
        this.$message.success(this.$t('settings.saveSuccess'))
      } catch (error) {
        this.$message.error(this.$t('settings.saveFailed'))
      }
    },
    async handleNotificationChange(value) {
      if (value && !this.checkNotificationPermission()) {
        await this.requestNotificationPermission()
      }
      this.handleSettingChange()
    },
    async handleDesktopNotificationChange(value) {
      if (value && !this.checkNotificationPermission()) {
        const granted = await this.requestNotificationPermission()
        if (!granted) {
          this.settings.desktopNotification = false
          return
        }
      }
      this.handleSettingChange()
    },
    handleSoundChange() {
      this.handleSettingChange()
    },
    handleSoundTypeChange() {
      this.handleSettingChange()
      this.playSound(this.settings.messageSound)
    },
    handleCallSoundChange() {
      this.handleSettingChange()
      this.playSound(this.settings.callSound)
    },
    handleQuietHoursChange(value) {
      if (!value) {
        this.settings.quietHoursStart = null
        this.settings.quietHoursEnd = null
      }
      this.handleSettingChange()
    },
    checkNotificationPermission() {
      if (!('Notification' in window)) {
        this.$message.warning(this.$t('settings.notificationNotSupported'))
        return false
      }
      return Notification.permission === 'granted'
    },
    async requestNotificationPermission() {
      try {
        const permission = await Notification.requestPermission()
        if (permission !== 'granted') {
          this.$message.warning(this.$t('settings.notificationDenied'))
          return false
        }
        return true
      } catch (error) {
        this.$message.error(this.$t('settings.notificationError'))
        return false
      }
    },
    playSound(soundType) {
      if (soundType === 'none') return

      const audio = new Audio(`/static/sounds/${soundType}.mp3`)
      audio.play().catch(error => {
        console.error('Failed to play sound:', error)
      })
    },
    async saveSettings() {
      await updateNotificationSettings(this.settings)
      await this.$store.dispatch('user/updateSettings', {
        notification: this.settings,
      })
    },
  },
}
</script>

<style lang="scss" scoped>
.notification-settings {
  .setting-card {
    margin-bottom: 20px;

    .setting-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 0;
      border-bottom: 1px solid var(--border-light);

      &:last-child {
        border-bottom: none;
      }

      .setting-info {
        flex: 1;
        margin-right: 20px;

        h4 {
          margin: 0 0 5px;
          color: var(--text-primary);
        }

        p {
          margin: 0;
          color: var(--text-secondary);
          font-size: 14px;
        }
      }

      .sound-selector {
        display: flex;
        align-items: center;

        .el-select {
          width: 150px;
          margin-right: 10px;
        }
      }

      .quiet-hours {
        display: flex;
        align-items: center;
        gap: 10px;

        .el-time-picker {
          width: 120px;
        }
      }
    }
  }
}
</style>
