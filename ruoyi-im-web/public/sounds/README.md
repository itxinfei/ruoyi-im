# 语音通话铃声资源

## 需要的音频文件

1. **ringtone.mp3** - 来电铃声（循环播放）
2. **connect.mp3** - 通话接通提示音

## 音频文件规格

- **格式**: MP3
- **采样率**: 44.1kHz
- **比特率**: 128kbps
- **时长**:
  - ringtone.mp3: 建议 3-5 秒（会循环播放）
  - connect.mp3: 1-2 秒

## 获取方式

### 方式一：使用免费音效网站
- [Freesound.org](https://freesound.org/)
- [Zapsplat.com](https://www.zapsplat.com/)
- [Mixkit.co](https://mixkit.co/free-sound-effects/)

搜索关键词：phone ring, call connect, notification

### 方式二：使用系统自带铃声
Windows 系统铃声位置：
```
C:\Windows\Media\ring01.wav
```

### 方式三：生成静默文件（用于开发测试）
如果只是测试功能，可以使用任意音频文件代替。

## 文件放置位置

将音频文件放置在此目录下：
```
public/sounds/ringtone.mp3
public/sounds/connect.mp3
```

## 注意事项

- 确保音频文件没有版权问题
- 音量适中，不要太大声
- 铃声应该是清晰、友好的提示音
