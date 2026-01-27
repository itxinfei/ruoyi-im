# 系统设置增强功能实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标:** 增强系统设置对话框，实现编辑资料功能、新增设置分类、UI/UX优化和后端持久化

**架构:** 前端 Vue 3 + Vuex 管理设置状态，后端 Spring Boot 提供设置 API，数据存储在 im_user_setting 表

**技术栈:** Vue 3 Composition API、Vuex、Element Plus、MyBatis-Plus、Spring Boot 2.7

---

## 阶段一：后端基础（用户生日字段）

### Task 1: 添加生日字段到 ImUser 实体

**文件:**
- 修改: `ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImUser.java:24-75`

**Step 1: 添加 birthday 字段到 ImUser.java**

在 `ImUser.java` 的 `position` 字段后添加 `birthday` 字段：

```java
/** 职位 */
private String position;

/** 生日 */
@JsonFormat(pattern = "yyyy-MM-dd")
private LocalDate birthday;

/** 最后在线时间,记录用户最近一次在线的时间 */
```

添加导入：
```java
import java.time.LocalDate;
```

**Step 2: 创建数据库迁移脚本**

创建文件: `ruoyi-im-api/src/main/resources/db/migration/V2025.01.27__AddBirthdayToUser.sql`

```sql
-- 为 im_user 表添加生日字段
ALTER TABLE im_user ADD COLUMN birthday DATE DEFAULT NULL COMMENT '生日' AFTER position;
```

**Step 3: 更新 ImUserUpdateRequest DTO**

**文件:**
- 修改: `ruoyi-im-api/src/main/java/com/ruoyi/im/dto/user/ImUserUpdateRequest.java:53-57`

在 `position` 字段后添加：

```java
/** 职位 */
@Size(max = 100, message = "职位长度不能超过100")
private String position;

/** 生日 */
@JsonFormat(pattern = "yyyy-MM-dd")
private LocalDate birthday;

/** 用户角色: USER/ADMIN/SUPER_ADMIN */
private String role;
```

添加导入：
```java
import java.time.LocalDate;
```

**Step 4: 编译验证**

```bash
cd ruoyi-im-api
mvn clean compile
```

预期: BUILD SUCCESS

**Step 5: 提交**

```bash
git add ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImUser.java
git add ruoyi-im-api/src/main/java/com/ruoyi/im/dto/user/ImUserUpdateRequest.java
git add ruoyi-im-api/src/main/resources/db/migration/V2025.01.27__AddBirthdayToUser.sql
git commit -m "feat(user): 添加生日字段到用户实体

- ImUser 实体新增 birthday 字段（LocalDate 类型）
- ImUserUpdateRequest DTO 支持生日更新
- 添加数据库迁移脚本

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

---

## 阶段二：后端用户设置 API

### Task 2: 创建用户设置 DTO 和 VO

**Step 1: 创建 UserSettingVO**

**文件:**
- 创建: `ruoyi-im-api/src/main/java/com/ruoyi/im/vo/setting/UserSettingVO.java`

```java
package com.ruoyi.im.vo.setting;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设置返回VO
 *
 * @author ruoyi
 */
@Data
public class UserSettingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 设置ID */
    private Long id;

    /** 设置键名 */
    private String settingKey;

    /** 设置值 */
    private String settingValue;

    /** 设置类型 */
    private String settingType;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
```

**Step 2: 创建 UserSettingUpdateRequest**

**文件:**
- 创建: `ruoyi-im-api/src/main/java/com/ruoyi/im/dto/setting/UserSettingUpdateRequest.java`

```java
package com.ruoyi.im.dto.setting;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户设置更新请求
 *
 * @author ruoyi
 */
@Data
public class UserSettingUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置键名，格式：category.key
     * 例如：chat.fontSize, notifications.enabled
     */
    @NotBlank(message = "设置键名不能为空")
    @Size(max = 100, message = "设置键名长度不能超过100")
    private String settingKey;

    /**
     * 设置值，支持字符串、数字、布尔值
     */
    @NotBlank(message = "设置值不能为空")
    @Size(max = 1000, message = "设置值长度不能超过1000")
    private String settingValue;

    /**
     * 设置类型：NOTIFICATION, PRIVACY, CHAT, FILE, DATA, GENERAL
     */
    @Size(max = 50, message = "设置类型长度不能超过50")
    private String settingType;
}
```

**Step 3: 创建 UserSettingsBatchUpdateRequest**

**文件:**
- 创建: `ruoyi-im-api/src/main/java/com/ruoyi/im/dto/setting/UserSettingsBatchUpdateRequest.java`

```java
package com.ruoyi.im.dto.setting;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 用户设置批量更新请求
 *
 * @author ruoyi
 */
@Data
public class UserSettingsBatchUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置键值对映射
     * 键格式：category.key（如 chat.fontSize）
     * 值：字符串形式的设置值
     */
    private Map<String, String> settings;
}
```

**Step 4: 编译验证**

```bash
cd ruoyi-im-api
mvn clean compile
```

预期: BUILD SUCCESS

**Step 5: 提交**

```bash
git add ruoyi-im-api/src/main/java/com/ruoyi/im/vo/setting/
git add ruoyi-im-api/src/main/java/com/ruoyi/im/dto/setting/
git commit -m "feat(setting): 添加用户设置DTO和VO

- 新增 UserSettingVO 用于返回设置数据
- 新增 UserSettingUpdateRequest 用于单个设置更新
- 新增 UserSettingsBatchUpdateRequest 用于批量设置更新

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

---

### Task 3: 创建用户设置服务

**Step 1: 创建 IImUserSettingService 接口**

**文件:**
- 创建: `ruoyi-im-api/src/main/java/com/ruoyi/im/service/IImUserSettingService.java`

```java
package com.ruoyi.im.service;

import com.ruoyi.im.dto.setting.UserSettingUpdateRequest;
import com.ruoyi.im.dto.setting.UserSettingsBatchUpdateRequest;
import com.ruoyi.im.vo.setting.UserSettingVO;

import java.util.List;
import java.util.Map;

/**
 * 用户设置服务接口
 *
 * @author ruoyi
 */
public interface IImUserSettingService {

    /**
     * 获取用户所有设置
     *
     * @param userId 用户ID
     * @return 设置列表
     */
    List<UserSettingVO> getUserSettings(Long userId);

    /**
     * 获取用户设置（按类型）
     *
     * @param userId      用户ID
     * @param settingType 设置类型
     * @return 设置列表
     */
    List<UserSettingVO> getUserSettingsByType(Long userId, String settingType);

    /**
     * 获取用户单个设置值
     *
     * @param userId     用户ID
     * @param settingKey 设置键名
     * @return 设置值，不存在返回null
     */
    String getSettingValue(Long userId, String settingKey);

    /**
     * 获取用户设置映射（键值对）
     *
     * @param userId 用户ID
     * @return 设置键值对映射
     */
    Map<String, String> getUserSettingsMap(Long userId);

    /**
     * 更新用户单个设置
     *
     * @param userId  用户ID
     * @param request 更新请求
     */
    void updateSetting(Long userId, UserSettingUpdateRequest request);

    /**
     * 批量更新用户设置
     *
     * @param userId  用户ID
     * @param request 批量更新请求
     */
    void batchUpdateSettings(Long userId, UserSettingsBatchUpdateRequest request);

    /**
     * 删除用户单个设置
     *
     * @param userId     用户ID
     * @param settingKey 设置键名
     */
    void deleteSetting(Long userId, String settingKey);
}
```

**Step 2: 创建 ImUserSettingServiceImpl 实现**

**文件:**
- 创建: `ruoyi-im-api/src/main/java/com/ruoyi/im/service/impl/ImUserSettingServiceImpl.java`

```java
package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImUserSetting;
import com.ruoyi.im.dto.setting.UserSettingUpdateRequest;
import com.ruoyi.im.dto.setting.UserSettingsBatchUpdateRequest;
import com.ruoyi.im.mapper.ImUserSettingMapper;
import com.ruoyi.im.service.IImUserSettingService;
import com.ruoyi.im.vo.setting.UserSettingVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户设置服务实现
 *
 * @author ruoyi
 */
@Service
public class ImUserSettingServiceImpl implements IImUserSettingService {

    private static final Logger logger = LoggerFactory.getLogger(ImUserSettingServiceImpl.class);

    private final ImUserSettingMapper userSettingMapper;

    public ImUserSettingServiceImpl(ImUserSettingMapper userSettingMapper) {
        this.userSettingMapper = userSettingMapper;
    }

    @Override
    public List<UserSettingVO> getUserSettings(Long userId) {
        List<ImUserSetting> settings = userSettingMapper.selectByUserId(userId);
        return settings.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSettingVO> getUserSettingsByType(Long userId, String settingType) {
        List<ImUserSetting> settings = userSettingMapper.selectByUserIdAndType(userId, settingType);
        return settings.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public String getSettingValue(Long userId, String settingKey) {
        ImUserSetting setting = userSettingMapper.selectByUserIdAndKey(userId, settingKey);
        return setting != null ? setting.getSettingValue() : null;
    }

    @Override
    public Map<String, String> getUserSettingsMap(Long userId) {
        List<ImUserSetting> settings = userSettingMapper.selectByUserId(userId);
        return settings.stream()
                .collect(Collectors.toMap(
                        ImUserSetting::getSettingKey,
                        ImUserSetting::getSettingValue,
                        (v1, v2) -> v1
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetting(Long userId, UserSettingUpdateRequest request) {
        ImUserSetting existing = userSettingMapper.selectByUserIdAndKey(userId, request.getSettingKey());

        if (existing != null) {
            // 更新已存在的设置
            existing.setSettingValue(request.getSettingValue());
            if (request.getSettingType() != null) {
                existing.setSettingType(request.getSettingType());
            }
            existing.setUpdateTime(LocalDateTime.now());
            userSettingMapper.updateImUserSetting(existing);
            logger.info("更新用户设置: userId={}, key={}, value={}", userId, request.getSettingKey(), request.getSettingValue());
        } else {
            // 新增设置
            ImUserSetting newSetting = new ImUserSetting();
            newSetting.setUserId(userId);
            newSetting.setSettingKey(request.getSettingKey());
            newSetting.setSettingValue(request.getSettingValue());
            newSetting.setSettingType(request.getSettingType() != null ? request.getSettingType() : inferTypeFromKey(request.getSettingKey()));
            newSetting.setCreateTime(LocalDateTime.now());
            newSetting.setUpdateTime(LocalDateTime.now());
            userSettingMapper.insertImUserSetting(newSetting);
            logger.info("新增用户设置: userId={}, key={}, value={}", userId, request.getSettingKey(), request.getSettingValue());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSettings(Long userId, UserSettingsBatchUpdateRequest request) {
        if (request.getSettings() == null || request.getSettings().isEmpty()) {
            return;
        }

        List<ImUserSetting> toInsert = new ArrayList<>();
        List<ImUserSetting> toUpdate = new ArrayList<>();

        for (Map.Entry<String, String> entry : request.getSettings().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            ImUserSetting existing = userSettingMapper.selectByUserIdAndKey(userId, key);
            if (existing != null) {
                existing.setSettingValue(value);
                existing.setUpdateTime(LocalDateTime.now());
                toUpdate.add(existing);
            } else {
                ImUserSetting newSetting = new ImUserSetting();
                newSetting.setUserId(userId);
                newSetting.setSettingKey(key);
                newSetting.setSettingValue(value);
                newSetting.setSettingType(inferTypeFromKey(key));
                newSetting.setCreateTime(LocalDateTime.now());
                newSetting.setUpdateTime(LocalDateTime.now());
                toInsert.add(newSetting);
            }
        }

        for (ImUserSetting setting : toUpdate) {
            userSettingMapper.updateImUserSetting(setting);
        }

        if (!toInsert.isEmpty()) {
            userSettingMapper.batchInsertImUserSetting(toInsert);
        }

        logger.info("批量更新用户设置: userId={}, 更新{}条, 新增{}条", userId, toUpdate.size(), toInsert.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSetting(Long userId, String settingKey) {
        ImUserSetting existing = userSettingMapper.selectByUserIdAndKey(userId, settingKey);
        if (existing != null) {
            userSettingMapper.deleteImUserSettingById(existing.getId());
            logger.info("删除用户设置: userId={}, key={}", userId, settingKey);
        }
    }

    /**
     * 根据设置键名推断设置类型
     */
    private String inferTypeFromKey(String key) {
        if (key.startsWith("chat.")) {
            return "CHAT";
        } else if (key.startsWith("notifications.") || key.startsWith("notification.")) {
            return "NOTIFICATION";
        } else if (key.startsWith("privacy.")) {
            return "PRIVACY";
        } else if (key.startsWith("file.")) {
            return "FILE";
        } else if (key.startsWith("data.")) {
            return "DATA";
        } else {
            return "GENERAL";
        }
    }

    private UserSettingVO convertToVO(ImUserSetting setting) {
        UserSettingVO vo = new UserSettingVO();
        BeanUtils.copyProperties(setting, vo);
        return vo;
    }
}
```

**Step 3: 编译验证**

```bash
cd ruoyi-im-api
mvn clean compile
```

预期: BUILD SUCCESS

**Step 4: 提交**

```bash
git add ruoyi-im-api/src/main/java/com/ruoyi/im/service/IImUserSettingService.java
git add ruoyi-im-api/src/main/java/com/ruoyi/im/service/impl/ImUserSettingServiceImpl.java
git commit -m "feat(setting): 创建用户设置服务

- 新增 IImUserSettingService 接口
- 新增 ImUserSettingServiceImpl 实现
- 支持单个设置、批量设置增删改查
- 根据键名自动推断设置类型

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

---

### Task 4: 创建用户设置控制器

**Step 1: 创建 ImUserSettingController**

**文件:**
- 创建: `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImUserSettingController.java`

```java
package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.setting.UserSettingUpdateRequest;
import com.ruoyi.im.dto.setting.UserSettingsBatchUpdateRequest;
import com.ruoyi.im.service.IImUserSettingService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.setting.UserSettingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户设置控制器
 * 提供用户设置查询、更新等接口
 *
 * @author ruoyi
 */
@Tag(name = "用户设置", description = "用户偏好设置管理接口")
@RestController
@RequestMapping("/api/im/user/settings")
@Validated
public class ImUserSettingController {

    private final IImUserSettingService userSettingService;

    public ImUserSettingController(IImUserSettingService userSettingService) {
        this.userSettingService = userSettingService;
    }

    /**
     * 获取当前用户所有设置
     *
     * @return 用户设置列表
     */
    @Operation(summary = "获取用户设置", description = "获取当前登录用户的所有偏好设置")
    @GetMapping
    public Result<List<UserSettingVO>> getUserSettings() {
        Long userId = SecurityUtils.getLoginUserId();
        List<UserSettingVO> settings = userSettingService.getUserSettings(userId);
        return Result.success(settings);
    }

    /**
     * 获取当前用户设置（按类型）
     *
     * @param settingType 设置类型（CHAT, NOTIFICATION, PRIVACY, FILE, DATA, GENERAL）
     * @return 用户设置列表
     */
    @Operation(summary = "按类型获取用户设置", description = "根据设置类型获取用户偏好设置")
    @GetMapping("/type/{settingType}")
    public Result<List<UserSettingVO>> getUserSettingsByType(
            @PathVariable String settingType) {
        Long userId = SecurityUtils.getLoginUserId();
        List<UserSettingVO> settings = userSettingService.getUserSettingsByType(userId, settingType);
        return Result.success(settings);
    }

    /**
     * 获取当前用户设置的键值对映射
     *
     * @return 用户设置键值对映射
     */
    @Operation(summary = "获取用户设置映射", description = "获取当前用户设置的键值对形式，便于前端使用")
    @GetMapping("/map")
    public Result<Map<String, String>> getUserSettingsMap() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, String> settingsMap = userSettingService.getUserSettingsMap(userId);
        return Result.success(settingsMap);
    }

    /**
     * 更新用户单个设置
     *
     * @param request 更新请求
     * @return 更新结果
     */
    @Operation(summary = "更新单个设置", description = "更新用户的单个偏好设置")
    @PutMapping
    public Result<Void> updateSetting(@Valid @RequestBody UserSettingUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        userSettingService.updateSetting(userId, request);
        return Result.success("设置更新成功");
    }

    /**
     * 批量更新用户设置
     *
     * @param request 批量更新请求
     * @return 更新结果
     */
    @Operation(summary = "批量更新设置", description = "批量更新用户的多个偏好设置")
    @PutMapping("/batch")
    public Result<Void> batchUpdateSettings(@Valid @RequestBody UserSettingsBatchUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        userSettingService.batchUpdateSettings(userId, request);
        return Result.success("设置批量更新成功");
    }

    /**
     * 删除用户单个设置
     *
     * @param settingKey 设置键名
     * @return 删除结果
     */
    @Operation(summary = "删除单个设置", description = "删除用户的单个偏好设置，恢复默认值")
    @DeleteMapping("/{settingKey}")
    public Result<Void> deleteSetting(
            @Parameter(description = "设置键名，如 chat.fontSize")
            @PathVariable String settingKey) {
        Long userId = SecurityUtils.getLoginUserId();
        userSettingService.deleteSetting(userId, settingKey);
        return Result.success("设置删除成功");
    }
}
```

**Step 2: 编译验证**

```bash
cd ruoyi-im-api
mvn clean compile
```

预期: BUILD SUCCESS

**Step 3: 启动应用验证 API 可用性**

```bash
cd ruoyi-im-api
mvn spring-boot:run
```

在另一个终端测试：
```bash
curl -X GET http://localhost:8080/api/im/user/settings/map -H "Authorization: Bearer YOUR_TOKEN"
```

预期: 返回空对象 {} 或用户设置映射

**Step 4: 停止应用并提交**

```bash
git add ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImUserSettingController.java
git commit -m "feat(setting): 创建用户设置控制器

- 新增 ImUserSettingController
- GET /api/im/user/settings - 获取所有设置
- GET /api/im/user/settings/type/{type} - 按类型获取
- GET /api/im/user/settings/map - 获取键值对映射
- PUT /api/im/user/settings - 更新单个设置
- PUT /api/im/user/settings/batch - 批量更新
- DELETE /api/im/user/settings/{key} - 删除设置

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

---

## 阶段三：前端 API 封装

### Task 5: 创建用户设置前端 API

**Step 1: 创建 user_setting.js**

**文件:**
- 创建: `ruoyi-im-web/src/api/im/user_setting.js`

```javascript
import request from '../request'

/**
 * 获取用户所有设置
 * @returns {Promise}
 */
export function getUserSettings() {
  return request({
    url: '/api/im/user/settings',
    method: 'get'
  })
}

/**
 * 按类型获取用户设置
 * @param {string} settingType - 设置类型（CHAT, NOTIFICATION, PRIVACY, FILE, DATA, GENERAL）
 * @returns {Promise}
 */
export function getUserSettingsByType(settingType) {
  return request({
    url: `/api/im/user/settings/type/${settingType}`,
    method: 'get'
  })
}

/**
 * 获取用户设置键值对映射
 * @returns {Promise}
 */
export function getUserSettingsMap() {
  return request({
    url: '/api/im/user/settings/map',
    method: 'get'
  })
}

/**
 * 更新用户单个设置
 * @param {Object} data - { settingKey: string, settingValue: string, settingType?: string }
 * @returns {Promise}
 */
export function updateUserSetting(data) {
  return request({
    url: '/api/im/user/settings',
    method: 'put',
    data
  })
}

/**
 * 批量更新用户设置
 * @param {Object} settings - 键值对形式的设置对象
 * @returns {Promise}
 */
export function batchUpdateUserSettings(settings) {
  return request({
    url: '/api/im/user/settings/batch',
    method: 'put',
    data: { settings }
  })
}

/**
 * 删除用户单个设置
 * @param {string} settingKey - 设置键名
 * @returns {Promise}
 */
export function deleteUserSetting(settingKey) {
  return request({
    url: `/api/im/user/settings/${settingKey}`,
    method: 'delete'
  })
}
```

**Step 2: 提交**

```bash
git add ruoyi-im-web/src/api/im/user_setting.js
git commit -m "feat(api): 新增用户设置前端API

- getUserSettings - 获取所有设置
- getUserSettingsByType - 按类型获取
- getUserSettingsMap - 获取键值对映射
- updateUserSetting - 更新单个设置
- batchUpdateUserSettings - 批量更新
- deleteUserSetting - 删除设置

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

---

### Task 6: 扩展 Vuex Store 支持用户设置

**Step 1: 更新 im.js store**

**文件:**
- 修改: `ruoyi-im-web/src/store/modules/im.js:19-48`

扩展 state.settings 结构：

```javascript
state: () => ({
  // 当前用户
  currentUser: {
    id: null,
    name: '',
    avatar: '',
    email: ''
  },

  // WebSocket 连接状态
  wsConnected: false,

  // 系统实用设置
  settings: {
    notifications: {
      enabled: true,
      sound: false
    },
    privacy: {
      showStatus: true,
      readReceipt: true
    },
    general: {
      language: 'zh-CN',
      theme: 'auto' // 'light' | 'dark' | 'auto'
    },
    shortcuts: {
      send: 'enter' // 'enter' | 'ctrl-enter'
    },
    // 新增：聊天设置
    chat: {
      fontSize: 'medium', // 'small' | 'medium' | 'large' | 'xlarge'
      background: 'default', // 'default' | 'custom' | 'solid'
      bubbleStyle: 'default', // 'default' | 'compact' | 'loose'
      sendShortcut: 'enter' // 'enter' | 'ctrl-enter'
    },
    // 新增：文件管理
    file: {
      autoDownloadImage: true,
      autoDownloadFile: false,
      sizeWarning: true
    },
    // 新增：数据保留
    data: {
      keepOnLogout: true
    }
  }
}),
```

**Step 2: 添加设置同步相关的 mutations 和 actions**

**文件:**
- 修改: `ruoyi-im-web/src/store/modules/im.js:71-110`

在 mutations 中添加：

```javascript
mutations: {
  // 设置当前用户
  SET_CURRENT_USER(state, user) {
    state.currentUser = user
  },

  // 设置 WebSocket 连接状态
  SET_WS_CONNECTED(state, connected) {
    state.wsConnected = connected
  },

  // 更新系统设置
  UPDATE_SETTINGS(state, settings) {
    state.settings = { ...state.settings, ...settings }
    localStorage.setItem('im-system-settings', JSON.stringify(state.settings))
  },

  // 从服务器加载设置并合并到本地
  MERGE_SERVER_SETTINGS(state, serverSettings) {
    // serverSettings 是键值对形式，如 { 'chat.fontSize': 'large' }
    const merged = { ...state.settings }

    for (const [key, value] of Object.entries(serverSettings)) {
      const parts = key.split('.')
      let current = merged

      for (let i = 0; i < parts.length - 1; i++) {
        if (!current[parts[i]]) {
          current[parts[i]] = {}
        }
        current = current[parts[i]]
      }

      current[parts[parts.length - 1]] = value
    }

    state.settings = merged
    localStorage.setItem('im-system-settings', JSON.stringify(merged))
  },

  // 加载本地设置
  LOAD_SETTINGS(state) {
    try {
      const local = localStorage.getItem('im-system-settings')
      if (local) {
        state.settings = { ...state.settings, ...JSON.parse(local) }
      }
    } catch (e) {
      console.warn('加载设置失败', e)
    }
  },

  // 清空所有状态
  CLEAR_ALL_STATE(state) {
    state.currentUser = {
      id: null,
      name: '',
      avatar: '',
      email: ''
    }
    state.wsConnected = false
  }
},
```

**Step 3: 添加设置同步的 actions**

**文件:**
- 修改: `ruoyi-im-web/src/store/modules/im.js:112-157`

在 actions 中添加：

```javascript
actions: {
  // 初始化设置
  async initSettings({ commit, dispatch }) {
    commit('LOAD_SETTINGS')
    // 从服务器加载设置
    try {
      await dispatch('syncServerSettings')
    } catch (e) {
      console.warn('从服务器同步设置失败', e)
    }
  },

  // 从服务器同步设置
  async syncServerSettings({ commit }) {
    const { data } = await getUserSettingsMap()
    if (data && typeof data === 'object') {
      commit('MERGE_SERVER_SETTINGS', data)
    }
  },

  // 更新设置到服务器
  async updateServerSetting({ dispatch }, { key, value, type }) {
    try {
      await updateUserSetting({
        settingKey: key,
        settingValue: String(value),
        settingType: type
      })
    } catch (e) {
      console.error('更新设置到服务器失败', e)
      throw e
    }
  },

  // 批量更新设置到服务器
  async batchUpdateServerSettings({ dispatch }, settings) {
    try {
      await batchUpdateUserSettings(settings)
    } catch (e) {
      console.error('批量更新设置到服务器失败', e)
      throw e
    }
  },

  // 更新通知设置
  updateNotificationSettings({ commit, dispatch }, settings) {
    commit('UPDATE_SETTINGS', { notifications: { ...settings } })
    // 同步到服务器
    dispatch('batchUpdateServerSettings', {
      'notifications.enabled': String(settings.enabled),
      'notifications.sound': String(settings.sound)
    })
  },

  // 更新隐私设置
  updatePrivacySettings({ commit, dispatch }, settings) {
    commit('UPDATE_SETTINGS', { privacy: { ...settings } })
    dispatch('batchUpdateServerSettings', {
      'privacy.showStatus': String(settings.showStatus),
      'privacy.readReceipt': String(settings.readReceipt)
    })
  },

  // 更新通用设置
  updateGeneralSettings({ commit, dispatch }, settings) {
    commit('UPDATE_SETTINGS', { general: { ...settings } })
    dispatch('batchUpdateServerSettings', {
      'general.language': settings.language,
      'general.theme': settings.theme
    })
  },

  // 更新快捷键设置
  updateShortcutSettings({ commit, dispatch }, settings) {
    commit('UPDATE_SETTINGS', { shortcuts: { ...settings } })
    dispatch('batchUpdateServerSettings', {
      'shortcuts.send': settings.send
    })
  },

  // 新增：更新聊天设置
  updateChatSettings({ commit, dispatch }, settings) {
    commit('UPDATE_SETTINGS', { chat: { ...settings } })
    dispatch('batchUpdateServerSettings', {
      'chat.fontSize': settings.fontSize,
      'chat.background': settings.background,
      'chat.bubbleStyle': settings.bubbleStyle,
      'chat.sendShortcut': settings.sendShortcut
    })
  },

  // 新增：更新文件管理设置
  updateFileSettings({ commit, dispatch }, settings) {
    commit('UPDATE_SETTINGS', { file: { ...settings } })
    dispatch('batchUpdateServerSettings', {
      'file.autoDownloadImage': String(settings.autoDownloadImage),
      'file.autoDownloadFile': String(settings.autoDownloadFile),
      'file.sizeWarning': String(settings.sizeWarning)
    })
  },

  // 新增：更新数据保留设置
  updateDataSettings({ commit, dispatch }, settings) {
    commit('UPDATE_SETTINGS', { data: { ...settings } })
    dispatch('batchUpdateServerSettings', {
      'data.keepOnLogout': String(settings.keepOnLogout)
    })
  },

  // 设置当前用户
  setCurrentUser({ commit }, user) {
    commit('SET_CURRENT_USER', user)
  },

  // 设置 WebSocket 连接状态
  setWsConnected({ commit }, connected) {
    commit('SET_WS_CONNECTED', connected)
  },

  // 登出 - 清空所有状态
  logout({ commit, state }) {
    // 根据设置决定是否清除本地数据
    if (!state.settings.data?.keepOnLogout) {
      localStorage.removeItem('im-system-settings')
    }
    commit('CLEAR_ALL_STATE')
    commit('session/CLEAR_STATE', null, { root: true })
    commit('message/CLEAR_STATE', null, { root: true })
    commit('contact/CLEAR_STATE', null, { root: true })
  }
}
```

**Step 4: 添加导入语句**

**文件:**
- 修改: `ruoyi-im-web/src/store/modules/im.js:1-8`

在文件顶部添加 API 导入：

```javascript
/**
 * IM 模块主 Store
 * 管理即时通讯的核心状态和子模块
 */
import session from './im-session'
import message from './im-message'
import contact from './im-contact'
import { getUserSettingsMap, updateUserSetting, batchUpdateUserSettings } from '@/api/im/user_setting'
```

**Step 5: 提交**

```bash
git add ruoyi-im-web/src/store/modules/im.js
git commit -m "feat(store): 扩展Vuex store支持用户设置同步

- 扩展 settings.state 支持聊天、文件、数据设置
- 新增 MERGE_SERVER_SETTINGS mutation 合并服务器设置
- 新增 syncServerSettings action 从服务器同步设置
- 新增 updateServerSetting/batchUpdateServerSettings 同步到服务器
- 新增 updateChatSettings/updateFileSettings/updateDataSettings actions
- 登出时根据 keepOnLogout 设置决定是否清除本地数据

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

---

## 阶段四：前端组件开发

### Task 7: 创建编辑资料对话框

**Step 1: 创建 EditProfileDialog 组件**

**文件:**
- 创建: `ruoyi-im-web/src/components/Common/EditProfileDialog.vue`

```vue
<template>
  <el-dialog
    v-model="visible"
    title="编辑资料"
    width="500px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <!-- 头像上传 -->
      <el-form-item label="头像">
        <div class="avatar-upload">
          <el-avatar :size="80" :src="form.avatar || ''">
            {{ form.nickname?.charAt(0) || '?' }}
          </el-avatar>
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :on-success="handleAvatarSuccess"
            name="avatarfile"
          >
            <el-button size="small" type="primary">更换头像</el-button>
          </el-upload>
        </div>
      </el-form-item>

      <!-- 昵称 -->
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="50" show-word-limit />
      </el-form-item>

      <!-- 个性签名 -->
      <el-form-item label="个性签名" prop="signature">
        <el-input
          v-model="form.signature"
          type="textarea"
          :rows="3"
          placeholder="介绍一下自己..."
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 性别 -->
      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio :label="0">保密</el-radio>
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 生日 -->
      <el-form-item label="生日" prop="birthday">
        <el-date-picker
          v-model="form.birthday"
          type="date"
          placeholder="选择生日"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledDate"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 邮箱 -->
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { updateUser } from '@/api/im/user'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'success'])

const store = useStore()
const formRef = ref(null)
const saving = ref(false)
const visible = ref(false)

// 上传配置
const uploadUrl = computed(() => {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${baseURL}/im/user/avatar`
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token') || ''
  return { Authorization: `Bearer ${token}` }
})

// 表单数据
const form = reactive({
  nickname: '',
  signature: '',
  gender: 0,
  birthday: '',
  email: '',
  avatar: ''
})

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 50, message: '昵称长度在2-50个字符之间', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 禁用未来日期
const disabledDate = (time) => {
  return time.getTime() > Date.now()
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.warning('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.warning('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    form.avatar = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.msg || '头像上传失败')
  }
}

// 初始化表单
const initForm = () => {
  const currentUser = store.getters['user/currentUser'] || store.state.im.currentUser
  form.nickname = currentUser.nickname || ''
  form.signature = currentUser.signature || ''
  form.gender = currentUser.gender ?? 0
  form.birthday = currentUser.birthday || ''
  form.email = currentUser.email || ''
  form.avatar = currentUser.avatar || ''
}

// 保存
const handleSave = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const userId = store.getters['im/currentUserId'] || store.state.im.currentUser.id
    await updateUser(userId, {
      nickname: form.nickname,
      signature: form.signature,
      gender: form.gender,
      birthday: form.birthday || null,
      email: form.email || null
    })

    // 更新本地用户信息
    store.commit('im/SET_CURRENT_USER', {
      ...store.state.im.currentUser,
      ...form
    })

    ElMessage.success('保存成功')
    emit('success')
    visible.value = false
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 对话框关闭后重置
const handleClosed = () => {
  formRef.value?.resetFields()
}

// 监听显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    initForm()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-uploader {
  :deep(.el-upload) {
    display: block;
  }
}
</style>
```

**Step 2: 更新 SystemSettingsDialog 引入 EditProfileDialog**

**文件:**
- 修改: `ruoyi-im-web/src/components/Common/SystemSettingsDialog.vue:252-254`

将原有的编辑资料按钮点击事件改为打开对话框：

找到：
```vue
<el-button class="action-btn" @click="handleEditProfile">
```

改为：
```vue
<el-button class="action-btn" @click="showEditProfile = true">
```

在 script 中添加导入和状态：
```javascript
import EditProfileDialog from '@/components/Common/EditProfileDialog.vue'

const showEditProfile = ref(false)
```

在 template 底部添加组件：
```vue
<EditProfileDialog v-model="showEditProfile" @success="handleProfileUpdate" />
```

添加处理方法：
```javascript
const handleProfileUpdate = () => {
  // 刷新用户信息
  const currentUser = computed(() => store.getters['user/currentUser'] || { status: 'online' })
}
```

**Step 3: 提交**

```bash
git add ruoyi-im-web/src/components/Common/EditProfileDialog.vue
git add ruoyi-im-web/src/components/Common/SystemSettingsDialog.vue
git commit -m "feat(dialog): 创建编辑资料对话框

- 新增 EditProfileDialog.vue 组件
- 支持修改昵称、签名、性别、生日、邮箱
- 支持头像上传（2MB限制）
- 集成到 SystemSettingsDialog
- 保存后更新本地用户状态

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

---

### Task 8: 扩展 SystemSettingsDialog 添加新设置分类

**Step 1: 扩展菜单项**

**文件:**
- 修改: `ruoyi-im-web/src/components/Common/SystemSettingsDialog.vue:276-282`

修改 menuItems 添加新分类：

```javascript
const menuItems = [
  { id: 'account', label: '账号安全', icon: 'manage_accounts' },
  { id: 'notification', label: '通知设置', icon: 'notifications' },
  { id: 'privacy', label: '隐私安全', icon: 'security' },
  { id: 'general', label: '通用设置', icon: 'settings' },
  { id: 'chat', label: '聊天设置', icon: 'chat' },      // 新增
  { id: 'file', label: '文件管理', icon: 'folder' },    // 新增
  { id: 'storage', label: '存储与数据', icon: 'storage' }, // 新增
  { id: 'about', label: '关于应用', icon: 'info' }
]
```

**Step 2: 添加新分类的模板内容**

**文件:**
- 修改: `ruoyi-im-web/src/components/Common/SystemSettingsDialog.vue:250`（在 about 模板之前添加）

添加聊天设置模板：
```vue
<template v-else-if="activeMenu === 'chat'">
  <div class="chat-section">
    <h2 class="section-title">聊天设置</h2>
    <div class="settings-grid">
      <div class="setting-card">
        <div class="setting-header">
          <div class="icon-wrapper font-bg">
            <span class="font-icon">A</span>
          </div>
          <div class="setting-info">
            <h4>字体大小</h4>
            <p>调整聊天消息的字体大小</p>
          </div>
        </div>
        <el-select v-model="localSettings.chat.fontSize" size="large" style="width: 140px" @change="handleChatSettingChange">
          <el-option label="小" value="small" />
          <el-option label="中" value="medium" />
          <el-option label="大" value="large" />
          <el-option label="特大" value="xlarge" />
        </el-select>
      </div>
      <div class="setting-card">
        <div class="setting-header">
          <div class="icon-wrapper bubble-bg">
            <span class="bubble-icon">💬</span>
          </div>
          <div class="setting-info">
            <h4>气泡样式</h4>
            <p>调整消息气泡的显示样式</p>
          </div>
        </div>
        <el-select v-model="localSettings.chat.bubbleStyle" size="large" style="width: 140px" @change="handleChatSettingChange">
          <el-option label="默认" value="default" />
          <el-option label="紧凑" value="compact" />
          <el-option label="宽松" value="loose" />
        </el-select>
      </div>
    </div>
  </div>
</template>

<template v-else-if="activeMenu === 'file'">
  <div class="file-section">
    <h2 class="section-title">文件管理</h2>
    <div class="settings-grid">
      <div class="setting-card">
        <div class="setting-header">
          <div class="icon-wrapper image-bg">
            <span class="file-icon">🖼️</span>
          </div>
          <div class="setting-info">
            <h4>自动下载图片</h4>
            <p>接收图片时自动下载到本地</p>
          </div>
        </div>
        <el-switch v-model="localSettings.file.autoDownloadImage" size="large" @change="handleFileSettingChange" />
      </div>
      <div class="setting-card">
        <div class="setting-header">
          <div class="icon-wrapper file-bg">
            <span class="file-icon">📁</span>
          </div>
          <div class="setting-info">
            <h4>自动下载文件</h4>
            <p>接收文件时自动下载到本地</p>
          </div>
        </div>
        <el-switch v-model="localSettings.file.autoDownloadFile" size="large" @change="handleFileSettingChange" />
      </div>
      <div class="setting-card">
        <div class="setting-header">
          <div class="icon-wrapper warning-bg">
            <span class="file-icon">⚠️</span>
          </div>
          <div class="setting-info">
            <h4>文件大小警告</h4>
            <p>下载大文件前显示确认提示</p>
          </div>
        </div>
        <el-switch v-model="localSettings.file.sizeWarning" size="large" @change="handleFileSettingChange" />
      </div>
    </div>
  </div>
</template>

<template v-else-if="activeMenu === 'storage'">
  <div class="storage-section">
    <h2 class="section-title">存储与数据</h2>
    <div class="settings-grid">
      <div class="setting-card storage-card">
        <div class="setting-header">
          <div class="icon-wrapper cache-bg">
            <span class="storage-icon">💾</span>
          </div>
          <div class="setting-info">
            <h4>缓存大小</h4>
            <p>当前缓存占用约 {{ cacheSize }}</p>
          </div>
        </div>
        <el-button type="danger" plain @click="handleClearCache">
          <el-icon><Delete /></el-icon>
          清理缓存
        </el-button>
      </div>
      <div class="setting-card">
        <div class="setting-header">
          <div class="icon-wrapper export-bg">
            <span class="storage-icon">📤</span>
          </div>
          <div class="setting-info">
            <h4>导出聊天记录</h4>
            <p>将聊天记录导出为 JSON 文件</p>
          </div>
        </div>
        <el-button type="primary" plain @click="handleExportChat">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
      </div>
    </div>

    <h2 class="section-title mt-6">数据保留</h2>
    <div class="settings-grid">
      <div class="setting-card">
        <div class="setting-header">
          <div class="icon-wrapper logout-bg">
            <span class="data-icon">🔒</span>
          </div>
          <div class="setting-info">
            <h4>退出保留数据</h4>
            <p>退出登录后保留本地聊天记录</p>
          </div>
        </div>
        <el-switch v-model="localSettings.data.keepOnLogout" size="large" @change="handleDataSettingChange" />
      </div>
    </div>
  </div>
</template>
```

**Step 3: 添加新的图标导入**

**文件:**
- 修改: `ruoyi-im-web/src/components/Common/SystemSettingsDialog.vue:261`

添加 Delete 和 Download 图标：
```javascript
import { VideoPlay, Lock, Edit, Bell, Camera, Position, View, Document, Sunny, Moon, Monitor, Refresh, Delete, Download } from '@element-plus/icons-vue'
```

**Step 4: 添加新的计算属性和方法**

**文件:**
- 修改: `ruoyi-im-web/src/components/Common/SystemSettingsDialog.vue:284-329`

在 script 中添加：
```javascript
// 缓存大小估算
const cacheSize = ref('0 MB')

const calculateCacheSize = () => {
  let total = 0
  for (let key in localStorage) {
    if (localStorage.hasOwnProperty(key)) {
      total += localStorage[key].length + key.length
    }
  }
  // 估算 IndexedDB 大小（简化版）
  const sizeInMB = (total / 1024 / 1024).toFixed(2)
  cacheSize.value = `${sizeInMB} MB`
}

// 聊天设置变更处理
const handleChatSettingChange = () => {
  store.dispatch('im/updateChatSettings', localSettings.chat)
}

// 文件设置变更处理
const handleFileSettingChange = () => {
  store.dispatch('im/updateFileSettings', localSettings.file)
}

// 数据设置变更处理
const handleDataSettingChange = () => {
  store.dispatch('im/updateDataSettings', localSettings.data)
}

// 清理缓存
const handleClearCache = () => {
  ElMessageBox.confirm(
    '清理缓存后将清除所有本地缓存的图片和文件，聊天记录不受影响。是否继续？',
    '清理缓存',
    {
      confirmButtonText: '确认清理',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 清除 localStorage 中的非必要数据
    const keysToKeep = ['im-system-settings', 'token', 'user-info']
    const keysToRemove = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (!keysToKeep.includes(key)) {
        keysToRemove.push(key)
      }
    }
    keysToRemove.forEach(key => localStorage.removeItem(key))

    // 清除图片缓存（需要根据实际实现调整）
    calculateCacheSize()
    ElMessage.success('缓存清理完成')
  }).catch(() => {})
}

// 导出聊天记录
const handleExportChat = () => {
  try {
    const messages = store.state.im.message.messages || []
    const dataStr = JSON.stringify(messages, null, 2)
    const blob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `chat-export-${new Date().toISOString().slice(0, 10)}.json`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('聊天记录导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 组件挂载时计算缓存大小
calculateCacheSize()
```

还需要导入 ElMessageBox：
```javascript
import { ElMessage, ElMessageBox } from 'element-plus'
```

**Step 5: 添加新的样式**

**文件:**
- 修改: `ruoyi-im-web/src/components/Common/SystemSettingsDialog.vue:593`（在现有样式后添加）

```scss
// 新增设置项图标背景色
&.font-bg { background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%); }
&.bubble-bg { background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%); }
&.image-bg { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }
&.file-bg { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
&.warning-bg { background: linear-gradient(135deg, #ef4444 0%, #f87171 100%); }
&.cache-bg { background: linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%); }
&.export-bg { background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%); }
&.logout-bg { background: linear-gradient(135deg, #f97316 0%, #fb923c 100%); }

.font-icon, .bubble-icon, .file-icon, .storage-icon, .data-icon {
  font-size: 24px;
}

.storage-card {
  grid-column: span 1;
}
```

**Step 6: 提交**

```bash
git add ruoyi-im-web/src/components/Common/SystemSettingsDialog.vue
git commit -m "feat(settings): 扩展系统设置对话框新增分类

- 新增聊天设置：字体大小、气泡样式
- 新增文件管理：自动下载图片/文件、文件大小警告
- 新增存储与数据：缓存清理、聊天记录导出
- 新增数据保留：退出保留数据开关
- 添加设置变更处理和同步到 Vuex store
- 添加缓存大小计算和清理功能

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

---

## 阶段五：集成测试与文档

### Task 9: 端到端测试验证

**Step 1: 启动后端服务**

```bash
cd ruoyi-im-api
mvn spring-boot:run
```

验证服务启动：访问 http://localhost:8080/api/im/user/settings/map

**Step 2: 启动前端服务**

```bash
cd ruoyi-im-web
npm run dev
```

验证前端启动：访问 http://localhost:5173

**Step 3: 功能测试清单**

1. **编辑资料功能**
   - [ ] 打开系统设置 → 点击编辑资料按钮
   - [ ] 修改昵称并保存
   - [ ] 修改个性签名并保存
   - [ ] 修改性别并保存
   - [ ] 选择生日并保存
   - [ ] 修改邮箱并保存
   - [ ] 上传头像并验证显示

2. **聊天设置**
   - [ ] 切换到聊天设置标签
   - [ ] 修改字体大小，验证生效
   - [ ] 修改气泡样式，验证生效
   - [ ] 刷新页面，验证设置保留

3. **文件管理**
   - [ ] 切换到文件管理标签
   - [ ] 开关自动下载图片
   - [ ] 开关自动下载文件
   - [ ] 开关文件大小警告

4. **存储与数据**
   - [ ] 切换到存储与数据标签
   - [ ] 查看缓存大小显示
   - [ ] 点击清理缓存，验证成功
   - [ ] 点击导出聊天记录，验证文件下载

5. **设置同步**
   - [ ] 修改任意设置
   - [ ] 检查 LocalStorage 中的 im-system-settings
   - [ ] 检查后端 im_user_setting 表数据
   - [ ] 退出登录后重新登录，验证设置保留

**Step 4: 测试问题记录**

记录发现的问题并修复。

**Step 5: 提交测试修复**

```bash
git add .
git commit -m "fix(settings): 修复测试发现的问题

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```

---

## 实施完成检查清单

### 后端
- [x] ImUser 实体添加 birthday 字段
- [x] ImUserUpdateRequest 支持 birthday
- [x] UserSettingVO 创建
- [x] UserSettingUpdateRequest 创建
- [x] UserSettingsBatchUpdateRequest 创建
- [x] IImUserSettingService 接口
- [x] ImUserSettingServiceImpl 实现
- [x] ImUserSettingController 控制器
- [x] 数据库迁移脚本

### 前端
- [x] user_setting.js API 封装
- [x] Vuex store 扩展支持设置同步
- [x] EditProfileDialog.vue 组件
- [x] SystemSettingsDialog.vue 扩展新分类
- [x] 设置变更处理和同步

### 测试
- [ ] 编辑资料功能测试
- [ ] 聊天设置功能测试
- [ ] 文件管理功能测试
- [ ] 存储与数据功能测试
- [ ] 设置同步测试

---

## 预期成果

1. 用户可以编辑个人资料（昵称、签名、性别、生日、邮箱、头像）
2. 用户可以自定义聊天设置（字体大小、气泡样式）
3. 用户可以管理文件下载设置
4. 用户可以清理缓存和导出聊天记录
5. 设置在服务器端持久化，跨设备同步
6. 退出登录时根据设置决定是否保留本地数据
