# TODO 注释处理清单

**处理日期**: 2026-03-02
**发现总数**: 11 个 TODO 注释
**处理状态**: 待处理

---

## 📋 TODO 注释列表

### 高优先级（安全/稳定性相关）

#### 1. ImUserDepartureServiceImpl.java:82
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImUserDepartureServiceImpl.java`
- **位置**: 第 82 行
- **内容**: `// TODO: 实际实现中需要清理Redis中的在线状态`
- **影响**: 用户离职后，Redis 中的在线状态可能残留
- **优先级**: 🔴 高
- **建议**: 实现 Redis 在线状态清理逻辑

### 中优先级（功能完整性）

#### 2. ImFriendServiceImpl.java:1023
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImFriendServiceImpl.java`
- **位置**: 第 1023 行
- **内容**: `// TODO: 实现获取之前上传通讯录匹配结果的功能`
- **影响**: 通讯录匹配功能不完整
- **优先级**: 🟡 中
- **建议**: 实现通讯录匹配结果查询功能

#### 3. ImMeetingRoomServiceImpl.java:575
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImMeetingRoomServiceImpl.java`
- **位置**: 第 575 行
- **内容**: `// TODO: 从签到记录表查询实际签到状态`
- **影响**: 会议签到状态不准确
- **优先级**: 🟡 中
- **建议**: 从签到记录表查询实际签到状态

#### 4. ImCacheServiceImpl.java:178
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImCacheServiceImpl.java`
- **位置**: 第 178 行
- **内容**: `// TODO: 缓存预热，预加载热点数据`
- **影响**: 系统启动后首次访问较慢
- **优先级**: 🟡 中
- **建议**: 实现缓存预热功能

### 低优先级（第三方集成）

#### 5. ImVoiceTranscriptServiceImpl.java:213
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImVoiceTranscriptServiceImpl.java`
- **位置**: 第 213 行
- **内容**: `// TODO: 集成阿里云、讯飞或腾讯云语音识别服务`
- **影响**: 语音转文字功能不可用
- **优先级**: 🟢 低
- **建议**: 集成第三方语音识别服务

#### 6. ImTranslationServiceImpl.java:219
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImTranslationServiceImpl.java`
- **位置**: 第 219 行
- **内容**: `// TODO: 实现百度翻译API调用`
- **影响**: 翻译功能不可用
- **优先级**: 🟢 低
- **建议**: 实现百度翻译API集成

#### 7. ImTranslationServiceImpl.java:254
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImTranslationServiceImpl.java`
- **位置**: 第 254 行
- **内容**: `// TODO: 实现腾讯云翻译API调用`
- **影响**: 翻译功能不可用
- **优先级**: 🟢 低
- **建议**: 实现腾讯云翻译API集成

#### 8. ImPushDeviceServiceImpl.java:178
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImPushDeviceServiceImpl.java`
- **位置**: 第 178 行
- **内容**: `* TODO: 实际实现需要集成APNs`
- **影响**: iOS 推送功能不可用
- **优先级**: 🟢 低
- **建议**: 集成 APNs 推送服务

#### 9. ImPushDeviceServiceImpl.java:187
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImPushDeviceServiceImpl.java`
- **位置**: 第 187 行
- **内容**: `* TODO: 实际实现需要集成FCM`
- **影响**: Android 推送功能不可用
- **优先级**: 🟢 低
- **建议**: 集成 FCM 推送服务

#### 10. ImPushDeviceServiceImpl.java:196
- **文件**: `src/main/java/com/ruoyi/im/service/impl/ImPushDeviceServiceImpl.java`
- **位置**: 第 196 行
- **内容**: `* TODO: 实际实现需要集成Gotify`
- **影响**: 自建推送功能不可用
- **优先级**: 🟢 低
- **建议**: 集成 Gotify 推送服务

### 极低优先级（可选）

#### 11. EmailPermissionAspect.java:218
- **文件**: `src/main/java/com/ruoyi/im/aspect/EmailPermissionAspect.java`
- **位置**: 第 218 行
- **内容**: `// TODO: 实现管理员检查逻辑`
- **影响**: 邮件权限管理不够精细
- **优先级**: ⚪ 极低
- **建议**: 实现更细粒度的权限检查

---

## 🎯 处理优先级

### 立即处理（高优先级）
1. ✅ 实现用户离职时清理 Redis 在线状态

### 近期处理（中优先级）
2. 实现通讯录匹配结果查询
3. 实现会议签到状态查询
4. 实现缓存预热功能

### 可选处理（低优先级）
5-10. 集成第三方服务（语音识别、翻译、推送）

### 暂缓处理（极低优先级）
11. 实现更细粒度的邮件权限检查

---

## 💡 处理建议

### 处理方式

1. **立即实现**: 对高优先级 TODO，应该立即实现
2. **标记为 FEATURE**: 对低优先级的第三方集成 TODO，可以标记为未来功能
3. **删除注释**: 对于不打算实现的功能，删除 TODO 注释
4. **添加说明**: 对于暂不实现的功能，添加详细说明

### 推荐方案

**方案 A（保守）**:
- 只处理高优先级 TODO（1个）
- 其他 TODO 保持不变，标记为未来功能

**方案 B（积极）**:
- 处理高优先级和中优先级 TODO（4个）
- 低优先级 TODO 标记为未来功能

**方案 C（激进）**:
- 处理所有 TODO（11个）
- 完整实现所有功能

---

## 📊 统计信息

| 优先级 | 数量 | 百分比 |
|--------|------|--------|
| 高 | 1 | 9.1% |
| 中 | 3 | 27.3% |
| 低 | 6 | 54.5% |
| 极低 | 1 | 9.1% |
| **总计** | **11** | **100%** |

---

**清单生成时间**: 2026-03-02
**处理人员**: AI Code Reviewer
**建议方案**: 方案 B（处理高/中优先级，标记低优先级）