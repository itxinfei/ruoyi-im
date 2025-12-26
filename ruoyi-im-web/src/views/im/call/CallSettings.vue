<template>
  <div class="call-settings">
    <el-card>
      <template #header>
        <div>
          <span>通话设置</span>
        </div>
      </template>

      <el-form label-width="120px">
        <!-- 设备设置 -->
        <h3>设备设置</h3>
        <el-form-item label="麦克风">
          <el-select
            v-model="audioInput"
            placeholder="选择麦克风"
            @change="handleDeviceChange('audioInput', $event)"
          >
            <el-option
              v-for="device in audioInputDevices"
              :key="device.deviceId"
              :label="device.label"
              :value="device.deviceId"
            />
          </el-select>
          <div class="device-test">
            <el-progress v-if="isTestingMic" :percentage="micVolume" :color="micVolumeColor" />
            <el-button
              size="small"
              :type="isTestingMic ? 'danger' : 'primary'"
              @click="toggleMicTest"
            >
              {{ isTestingMic ? '停止测试' : '测试麦克风' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="扬声器">
          <el-select
            v-model="audioOutput"
            placeholder="选择扬声器"
            @change="handleDeviceChange('audioOutput', $event)"
          >
            <el-option
              v-for="device in audioOutputDevices"
              :key="device.deviceId"
              :label="device.label"
              :value="device.deviceId"
            />
          </el-select>
          <div class="device-test">
            <el-button size="small" type="primary" @click="testSpeaker"> 测试扬声器 </el-button>
          </div>
        </el-form-item>

        <el-form-item label="摄像头">
          <el-select
            v-model="videoInput"
            placeholder="选择摄像头"
            @change="handleDeviceChange('videoInput', $event)"
          >
            <el-option
              v-for="device in videoInputDevices"
              :key="device.deviceId"
              :label="device.label"
              :value="device.deviceId"
            />
          </el-select>
          <div class="device-test">
            <el-button
              size="small"
              :type="isTestingCamera ? 'danger' : 'primary'"
              @click="toggleCameraTest"
            >
              {{ isTestingCamera ? '停止测试' : '测试摄像头' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 视频预览 -->
        <el-dialog
          v-model:visible="showCameraPreview"
          title="摄像头预览"
          width="400px"
          @close="stopCameraTest"
        >
          <div class="camera-preview">
            <video ref="previewVideo" autoplay playsinline muted></video>
          </div>
        </el-dialog>

        <!-- 通话质量设置 -->
        <h3>通话质量</h3>
        <el-form-item label="视频质量">
          <el-select v-model="videoQuality" @change="handleQualityChange">
            <el-option label="自动" value="auto" />
            <el-option label="高清" value="high" />
            <el-option label="标清" value="medium" />
            <el-option label="流畅" value="low" />
          </el-select>
        </el-form-item>

        <el-form-item label="带宽限制">
          <el-select v-model="bandwidthLimit" @change="handleBandwidthChange">
            <el-option label="无限制" value="unlimited" />
            <el-option label="2Mbps" value="2000" />
            <el-option label="1Mbps" value="1000" />
            <el-option label="500Kbps" value="500" />
          </el-select>
        </el-form-item>

        <!-- 通话设置 -->
        <h3>通话设置</h3>
        <el-form-item label="自动接听">
          <el-switch v-model="autoAnswer" @change="handleAutoAnswerChange" />
          <span class="setting-hint">仅对联系人生效</span>
        </el-form-item>

        <el-form-item label="通话提醒">
          <el-switch v-model="callNotification" @change="handleNotificationChange" />
          <span class="setting-hint">接收来电通知</span>
        </el-form-item>

        <el-form-item label="噪音抑制">
          <el-switch v-model="noiseSuppression" @change="handleNoiseSuppressionChange" />
          <span class="setting-hint">减少背景噪音</span>
        </el-form-item>

        <el-form-item label="回音消除">
          <el-switch v-model="echoCancellation" @change="handleEchoCancellationChange" />
          <span class="setting-hint">防止声音重放</span>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="saveSettings"> 保存设置 </el-button>
          <el-button @click="resetSettings"> 重置设置 </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'CallSettings',
  data() {
    return {
      // 设备列表
      audioInputDevices: [],
      audioOutputDevices: [],
      videoInputDevices: [],

      // 设备选择
      audioInput: '',
      audioOutput: '',
      videoInput: '',

      // 设备测试状态
      isTestingMic: false,
      isTestingCamera: false,
      showCameraPreview: false,
      micVolume: 0,

      // 音频分析器
      audioContext: null,
      audioAnalyser: null,
      audioStream: null,

      // 视频流
      videoStream: null,

      // 测试音频
      testAudio: null,

      // 通话设置
      videoQuality: 'auto',
      bandwidthLimit: 'unlimited',
      autoAnswer: false,
      callNotification: true,
      noiseSuppression: true,
      echoCancellation: true,
    }
  },
  computed: {
    // 麦克风音量颜色
    micVolumeColor() {
      if (this.micVolume < 30) return '#67C23A'
      if (this.micVolume < 70) return '#E6A23C'
      return '#F56C6C'
    },
  },
  async created() {
    await this.loadDevices()
    this.loadSettings()
  },
  beforeUnmount() {
    this.stopMicTest()
    this.stopCameraTest()
  },
  methods: {
    // 加载设备列表
    async loadDevices() {
      try {
        // 请求权限
        await navigator.mediaDevices.getUserMedia({ audio: true, video: true })

        // 获取设备列表
        const devices = await navigator.mediaDevices.enumerateDevices()

        this.audioInputDevices = devices.filter(device => device.kind === 'audioinput')
        this.audioOutputDevices = devices.filter(device => device.kind === 'audiooutput')
        this.videoInputDevices = devices.filter(device => device.kind === 'videoinput')
      } catch (error) {
        console.error('获取设备列表失败:', error)
        this.$message.error('获取设备列表失败，请确保已授予相关权限')
      }
    },

    // 加载设置
    loadSettings() {
      const settings = this.$store.state.settings
      this.videoQuality = settings.videoQuality
      this.bandwidthLimit = settings.bandwidthLimit || 'unlimited'
      this.autoAnswer = settings.autoAnswer || false
      this.callNotification = settings.callNotification !== false
      this.noiseSuppression = settings.noiseSuppression !== false
      this.echoCancellation = settings.echoCancellation !== false

      // 设置默认设备
      this.audioInput = settings.defaultAudioInput || 'default'
      this.audioOutput = settings.defaultAudioOutput || 'default'
      this.videoInput = settings.defaultVideoInput || 'default'
    },

    // 处理设备变更
    handleDeviceChange(type, deviceId) {
      this.$store.dispatch('settings/updateSetting', {
        key: `default${type.charAt(0).toUpperCase() + type.slice(1)}`,
        value: deviceId,
      })
    },

    // 切换麦克风测试
    async toggleMicTest() {
      if (this.isTestingMic) {
        this.stopMicTest()
      } else {
        await this.startMicTest()
      }
    },

    // 开始麦克风测试
    async startMicTest() {
      try {
        this.audioStream = await navigator.mediaDevices.getUserMedia({
          audio: {
            deviceId: this.audioInput ? { exact: this.audioInput } : undefined,
          },
        })

        this.audioContext = new AudioContext()
        const source = this.audioContext.createMediaStreamSource(this.audioStream)
        this.audioAnalyser = this.audioContext.createAnalyser()
        source.connect(this.audioAnalyser)

        this.isTestingMic = true
        this.updateMicVolume()
      } catch (error) {
        console.error('启动麦克风测试失败:', error)
        this.$message.error('启动麦克风测试失败')
      }
    },

    // 停止麦克风测试
    stopMicTest() {
      if (this.audioStream) {
        this.audioStream.getTracks().forEach(track => track.stop())
        this.audioStream = null
      }
      if (this.audioContext) {
        this.audioContext.close()
        this.audioContext = null
      }
      this.audioAnalyser = null
      this.isTestingMic = false
      this.micVolume = 0
    },

    // 更新麦克风音量
    updateMicVolume() {
      if (!this.isTestingMic) return

      requestAnimationFrame(this.updateMicVolume)

      const dataArray = new Uint8Array(this.audioAnalyser.frequencyBinCount)
      this.audioAnalyser.getByteFrequencyData(dataArray)

      // 计算平均音量
      const average = dataArray.reduce((sum, value) => sum + value, 0) / dataArray.length
      this.micVolume = Math.min(100, Math.round((average * 100) / 255))
    },

    // 测试扬声器
    testSpeaker() {
      if (this.testAudio) {
        this.testAudio.pause()
        this.testAudio = null
        return
      }

      this.testAudio = new Audio('/static/media/test-sound.mp3')
      if (this.audioOutput !== 'default') {
        this.testAudio.setSinkId(this.audioOutput).catch(error => {
          console.error('设置音频输出设备失败:', error)
        })
      }

      this.testAudio.play().catch(error => {
        console.error('播放测试音频失败:', error)
        this.$message.error('播放测试音频失败')
      })

      this.testAudio.onended = () => {
        this.testAudio = null
      }
    },

    // 切换摄像头测试
    async toggleCameraTest() {
      if (this.isTestingCamera) {
        this.stopCameraTest()
      } else {
        await this.startCameraTest()
      }
    },

    // 开始摄像头测试
    async startCameraTest() {
      try {
        this.videoStream = await navigator.mediaDevices.getUserMedia({
          video: {
            deviceId: this.videoInput ? { exact: this.videoInput } : undefined,
          },
        })

        this.$refs.previewVideo.srcObject = this.videoStream
        this.isTestingCamera = true
        this.showCameraPreview = true
      } catch (error) {
        console.error('启动摄像头测试失败:', error)
        this.$message.error('启动摄像头测试失败')
      }
    },

    // 停止摄像头测试
    stopCameraTest() {
      if (this.videoStream) {
        this.videoStream.getTracks().forEach(track => track.stop())
        this.videoStream = null
      }
      this.isTestingCamera = false
      this.showCameraPreview = false
    },

    // 处理质量设置变更
    handleQualityChange(value) {
      this.$store.dispatch('settings/updateSetting', {
        key: 'videoQuality',
        value,
      })
    },

    // 处理带宽设置变更
    handleBandwidthChange(value) {
      this.$store.dispatch('settings/updateSetting', {
        key: 'bandwidthLimit',
        value,
      })
    },

    // 处理自动接听设置变更
    handleAutoAnswerChange(value) {
      this.$store.dispatch('settings/updateSetting', {
        key: 'autoAnswer',
        value,
      })
    },

    // 处理通知设置变更
    handleNotificationChange(value) {
      this.$store.dispatch('settings/updateSetting', {
        key: 'callNotification',
        value,
      })
    },

    // 处理噪音抑制设置变更
    handleNoiseSuppressionChange(value) {
      this.$store.dispatch('settings/updateSetting', {
        key: 'noiseSuppression',
        value,
      })
    },

    // 处理回音消除设置变更
    handleEchoCancellationChange(value) {
      this.$store.dispatch('settings/updateSetting', {
        key: 'echoCancellation',
        value,
      })
    },

    // 保存设置
    saveSettings() {
      this.$message.success('设置已保存')
    },

    // 重置设置
    resetSettings() {
      this.$confirm('确定要重置所有通话设置吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          this.$store.dispatch('settings/resetCallSettings')
          this.loadSettings()
          this.$message.success('设置已重置')
        })
        .catch(() => {})
    },
  },
}
</script>

<style lang="scss" scoped>
.call-settings {
  padding: 20px;

  h3 {
    margin: 24px 0 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
    color: #303133;

    &:first-child {
      margin-top: 0;
    }
  }

  .device-test {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 16px;

    .el-progress {
      width: 200px;
    }
  }

  .setting-hint {
    margin-left: 8px;
    color: #909399;
    font-size: 12px;
  }

  .camera-preview {
    width: 100%;
    height: 300px;
    background-color: #000;
    display: flex;
    align-items: center;
    justify-content: center;

    video {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }
}
</style>
