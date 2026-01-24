# 统一架构实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 移除 ruoyi-im-admin（若依后台管理系统），将管理功能集成到 ruoyi-im-api 中，前端复用 ruoyi-im-web，根据用户角色权限动态展示界面。

**Architecture:**
- 单一后端服务 `ruoyi-im-api` 提供用户API (`/api/im/*`) 和管理API (`/api/admin/*`)
- 前端 `ruoyi-im-web` 根据用户角色（USER/ADMIN/SUPER_ADMIN）动态展示用户端或管理端界面
- 使用 `@PreAuthorize` 注解实现权限控制

**Tech Stack:**
- Spring Boot 2.7.18, Spring Security, MyBatis-Plus
- Vue 3 + Element Plus + Vuex
- MySQL 8.0, Redis

---

## Task 1: 数据库变更 - 添加用户角色字段

**Files:**
- Create: `sql/migrations/20250124_add_user_role.sql`
- Modify: `ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImUser.java`

**Step 1: 创建数据库迁移脚本**

创建文件 `sql/migrations/20250124_add_user_role.sql`:

```sql
-- 添加用户角色字段
-- 执行日期: 2025-01-24
-- 说明: 为 im_user 表添加 role 字段，用于区分普通用户和管理员

-- 添加 role 字段
ALTER TABLE im_user ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '用户角色: USER=普通用户, ADMIN=管理员, SUPER_ADMIN=超级管理员' AFTER status;

-- 为现有用户设置默认角色（id=1 的设为 SUPER_ADMIN，其他设为 USER）
UPDATE im_user SET role = 'SUPER_ADMIN' WHERE id = 1;
UPDATE im_user SET role = 'USER' WHERE id != 1 OR id IS NULL;

-- 添加索引以提升查询性能
CREATE INDEX idx_user_role ON im_user(role);

-- 添加角色字段注释
ALTER TABLE im_user MODIFY COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '用户角色: USER=普通用户, ADMIN=管理员, SUPER_ADMIN=超级管理员';
```

**Step 2: 执行数据库迁移脚本**

```bash
mysql -h 172.168.20.222 -P 3306 -u im -p123456 im < sql/migrations/20250124_add_user_role.sql
```

Expected: `Query OK, 0 rows affected` 表示字段已存在，或执行成功无报错

**Step 3: 更新 ImUser 实体类**

修改文件 `ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImUser.java`，在 `status` 字段后添加：

```java
/** 状态: 0=禁用, 1=启用 */
private Integer status;

/** 用户角色: USER=普通用户, ADMIN=管理员, SUPER_ADMIN=超级管理员 */
private String role;

/** 签名,用户个人简介或个性签名 */
private String signature;
```

**Step 4: 编译验证**

```bash
cd ruoyi-im-api && mvn clean compile
```

Expected: `BUILD SUCCESS`

**Step 5: 提交**

```bash
git add sql/migrations/20250124_add_user_role.sql ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImUser.java
git commit -m "feat(db): 添加用户角色字段支持权限管理

- im_user 表新增 role 字段 (USER/ADMIN/SUPER_ADMIN)
- 更新 ImUser 实体类
- 默认用户为 USER，id=1 用户设为 SUPER_ADMIN
"
```

---

## Task 2: 创建角色常量和枚举

**Files:**
- Create: `ruoyi-im-api/src/main/java/com/ruoyi/im/constant/UserRole.java`
- Create: `ruoyi-im-api/src/main/java/com/ruoyi/im/enums/UserRoleEnum.java`

**Step 1: 创建角色常量类**

创建文件 `ruoyi-im-api/src/main/java/com/ruoyi/im/constant/UserRole.java`:

```java
package com.ruoyi.im.constant;

/**
 * 用户角色常量
 *
 * @author ruoyi
 */
public class UserRole {

    /** 普通用户 */
    public static final String USER = "USER";

    /** 管理员 */
    public static final String ADMIN = "ADMIN";

    /** 超级管理员 */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    /** 角色前缀，Spring Security 需要 */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * 获取带前缀的角色名称，用于 Spring Security
     *
     * @param role 角色名称
     * @return 带前缀的角色名称，如 "ROLE_ADMIN"
     */
    public static String withPrefix(String role) {
        if (role == null) {
            return null;
        }
        return role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
    }

    /**
     * 判断是否为管理员角色
     *
     * @param role 角色名称
     * @return true 表示是管理员
     */
    public static boolean isAdmin(String role) {
        return ADMIN.equals(role) || SUPER_ADMIN.equals(role);
    }

    /**
     * 判断是否为超级管理员
     *
     * @param role 角色名称
     * @return true 表示是超级管理员
     */
    public static boolean isSuperAdmin(String role) {
        return SUPER_ADMIN.equals(role);
    }
}
```

**Step 2: 编译验证**

```bash
cd ruoyi-im-api && mvn clean compile
```

Expected: `BUILD SUCCESS`

**Step 3: 提交**

```bash
git add ruoyi-im-api/src/main/java/com/ruoyi/im/constant/UserRole.java
git commit -m "feat: 添加用户角色常量类

- 定义 USER/ADMIN/SUPER_ADMIN 三个角色常量
- 提供 withPrefix 方法适配 Spring Security
- 提供 isAdmin/isSuperAdmin 判断方法
"
```

---

## Task 3: 更新 Spring Security 配置

**Files:**
- Modify: `ruoyi-im-api/src/main/java/com/ruoyi/im/config/SecurityConfig.java`

**Step 1: 更新 SecurityConfig 添加管理员路径权限控制**

修改文件 `ruoyi-im-api/src/main/java/com/ruoyi/im/config/SecurityConfig.java`，在权限配置部分添加：

找到这段代码：
```java
// 配置权限访问
.authorizeRequests()
    // 对于登录接口允许匿名访问
    .antMatchers("/", "/health", "/auth/login", "/auth/register", "/api/auth/login", "/api/auth/register",
            "/api/im/auth/login", "/api/im/auth/register", "/ws/**", "/websocket/**", "/public/**",
            "/error", "/test/**")
    .permitAll()
```

替换为：
```java
// 配置权限访问
.authorizeRequests()
    // 对于登录接口允许匿名访问
    .antMatchers("/", "/health", "/auth/login", "/auth/register", "/api/auth/login", "/api/auth/register",
            "/api/im/auth/login", "/api/im/auth/register", "/ws/**", "/websocket/**", "/public/**",
            "/error", "/test/**")
    .permitAll()
    // 管理员接口需要 ADMIN 或 SUPER_ADMIN 角色
    .antMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
    // 用户接口需要认证后的任意角色
    .antMatchers("/api/im/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
```

**Step 2: 编译验证**

```bash
cd ruoyi-im-api && mvn clean compile
```

Expected: `BUILD SUCCESS`

**Step 3: 提交**

```bash
git add ruoyi-im-api/src/main/java/com/ruoyi/im/config/SecurityConfig.java
git commit -m "feat(security): 添加管理员API路径权限控制

- /api/admin/** 需要 ADMIN 或 SUPER_ADMIN 角色
- /api/im/** 需要任意认证用户
- 支持方法级 @PreAuthorize 注解
"
```

---

## Task 4: 创建管理员 Controller - 用户管理

**Files:**
- Create: `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/admin/ImUserAdminController.java`

**Step 1: 创建 admin 包目录和用户管理控制器**

创建文件 `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/admin/ImUserAdminController.java`:

```java
package com.ruoyi.im.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.common.BasePageRequest;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.constant.UserRole;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-用户管理控制器
 * 提供用户管理、状态修改、角色管理等管理员功能
 *
 * @author ruoyi
 */
@Tag(name = "管理员-用户管理", description = "管理员用户管理接口")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ImUserAdminController {

    @Autowired
    private ImUserService imUserService;

    /**
     * 获取用户列表（分页）
     *
     * @param request 分页请求参数
     * @return 用户列表
     */
    @Operation(summary = "获取用户列表", description = "管理员获取用户列表，支持分页和关键词搜索")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        // 构建查询条件
        LambdaQueryWrapper<ImUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(ImUser::getUsername, keyword)
                    .or().like(ImUser::getNickname, keyword)
                    .or().like(ImUser::getMobile, keyword));
        }
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(ImUser::getRole, role);
        }
        wrapper.orderByDesc(ImUser::getCreateTime);

        // 分页查询
        Page<ImUser> page = new Page<>(pageNum, pageSize);
        Page<ImUser> result = imUserService.page(page, wrapper);

        // 转换为 VO
        List<ImUserVO> voList = result.getRecords().stream()
                .map(user -> {
                    ImUserVO vo = new ImUserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());

        // 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("list", voList);
        data.put("total", result.getTotal());
        data.put("pageNum", result.getCurrent());
        data.put("pageSize", result.getSize());
        data.put("pages", result.getPages());

        return Result.success(data);
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Operation(summary = "获取用户详情", description = "管理员获取指定用户的详细信息")
    @GetMapping("/{id}")
    public Result<ImUserVO> getById(@PathVariable Long id) {
        ImUser user = imUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        ImUserVO vo = new ImUserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    /**
     * 修改用户状态
     *
     * @param id     用户ID
     * @param status 状态：0=禁用，1=启用
     * @return 操作结果
     */
    @Operation(summary = "修改用户状态", description = "管理员启用或禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ImUser user = imUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setStatus(status);
        imUserService.updateById(user);
        return Result.success("状态修改成功");
    }

    /**
     * 修改用户角色
     *
     * @param id   用户ID
     * @param role 角色：USER/ADMIN/SUPER_ADMIN
     * @return 操作结果
     */
    @Operation(summary = "修改用户角色", description = "超级管理员修改用户角色")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestParam String role) {
        if (!UserRole.USER.equals(role) && !UserRole.ADMIN.equals(role) && !UserRole.SUPER_ADMIN.equals(role)) {
            return Result.fail("无效的角色");
        }
        ImUser user = imUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setRole(role);
        imUserService.updateById(user);
        return Result.success("角色修改成功");
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户", description = "管理员删除指定用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ImUser user = imUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        imUserService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 获取用户统计
     *
     * @return 统计数据
     */
    @Operation(summary = "获取用户统计", description = "获取用户总数、在线人数等统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        long total = imUserService.count();
        long online = imUserService.count(new LambdaQueryWrapper<ImUser>()
                .eq(ImUser::getStatus, 1));

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("online", online);
        stats.put("offline", total - online);

        return Result.success(stats);
    }
}
```

**Step 2: 编译验证**

```bash
cd ruoyi-im-api && mvn clean compile
```

Expected: `BUILD SUCCESS`

**Step 3: 提交**

```bash
git add ruoyi-im-api/src/main/java/com/ruoyi/im/controller/admin/
git commit -m "feat(admin): 添加用户管理接口

- 新增 ImUserAdminController 提供用户列表、详情、状态修改、角色管理
- 支持分页查询和关键词搜索
- 使用 @PreAuthorize 注解控制管理员权限
"
```

---

## Task 5: 创建管理员 Controller - 数据统计

**Files:**
- Create: `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/admin/ImStatsController.java`

**Step 1: 创建数据统计控制器**

创建文件 `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/admin/ImStatsController.java`:

```java
package com.ruoyi.im.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-数据统计控制器
 * 提供系统数据统计、活跃度分析等功能
 *
 * @author ruoyi
 */
@Tag(name = "管理员-数据统计", description = "管理员数据统计接口")
@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ImStatsController {

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private ImGroupMapper imGroupMapper;

    @Autowired
    private ImMessageMapper imMessageMapper;

    /**
     * 获取系统概览数据
     *
     * @return 概览统计数据
     */
    @Operation(summary = "获取系统概览", description = "获取用户数、群组数、消息数等概览数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 用户统计
        Long totalUsers = imUserMapper.selectCount(null);
        overview.put("totalUsers", totalUsers);

        // 群组统计
        Long totalGroups = imGroupMapper.selectCount(null);
        overview.put("totalGroups", totalGroups);

        // 消息统计
        Long totalMessages = imMessageMapper.selectCount(null);
        overview.put("totalMessages", totalMessages);

        // 今日消息数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayMessages = imMessageMapper.selectCount(new LambdaQueryWrapper<ImMessage>()
                .ge(ImMessage::getCreateTime, todayStart));
        overview.put("todayMessages", todayMessages);

        // 本周活跃用户（简化实现：返回今日登录用户数）
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        Long activeUsers = imUserMapper.selectCount(new LambdaQueryWrapper<com.ruoyi.im.domain.ImUser>()
                .ge(com.ruoyi.im.domain.ImUser::getLastOnlineTime, weekStart));
        overview.put("activeUsers", activeUsers);

        return Result.success(overview);
    }

    /**
     * 获取用户活跃度统计
     *
     * @param days 统计天数，默认7天
     * @return 活跃度数据
     */
    @Operation(summary = "获取用户活跃度", description = "获取指定天数内的用户活跃度统计")
    @GetMapping("/users/active")
    public Result<Map<String, Object>> getUserActiveStats(
            @RequestParam(required = false, defaultValue = "7") Integer days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        // 活跃用户数（有登录记录）
        Long activeUsers = imUserMapper.selectCount(new LambdaQueryWrapper<com.ruoyi.im.domain.ImUser>()
                .ge(com.ruoyi.im.domain.ImUser::getLastOnlineTime, since));

        // 新增用户数
        Long newUsers = imUserMapper.selectCount(new LambdaQueryWrapper<com.ruoyi.im.domain.ImUser>()
                .ge(com.ruoyi.im.domain.ImUser::getCreateTime, since));

        Map<String, Object> stats = new HashMap<>();
        stats.put("activeUsers", activeUsers);
        stats.put("newUsers", newUsers);
        stats.put("days", days);

        return Result.success(stats);
    }

    /**
     * 获取群组活跃度统计
     *
     * @param days 统计天数，默认7天
     * @return 活跃度数据
     */
    @Operation(summary = "获取群组活跃度", description = "获取指定天数内的群组活跃度统计")
    @GetMapping("/groups/active")
    public Result<Map<String, Object>> getGroupActiveStats(
            @RequestParam(required = false, defaultValue = "7") Integer days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        // 活跃群组数（有新消息的群组）
        Long activeGroups = imMessageMapper.selectCount(new LambdaQueryWrapper<ImMessage>()
                .ge(ImMessage::getCreateTime, since)
                .isNotNull(ImMessage::getGroupId));

        // 新建群组数
        Long newGroups = imGroupMapper.selectCount(new LambdaQueryWrapper<ImGroup>()
                .ge(ImGroup::getCreateTime, since));

        Map<String, Object> stats = new HashMap<>();
        stats.put("activeGroups", activeGroups);
        stats.put("newGroups", newGroups);
        stats.put("days", days);

        return Result.success(stats);
    }

    /**
     * 获取消息统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 消息统计数据
     */
    @Operation(summary = "获取消息统计", description = "获取指定日期范围内的消息统计")
    @GetMapping("/messages")
    public Result<Map<String, Object>> getMessageStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);

        // 日期范围内的消息总数
        Long messageCount = imMessageMapper.selectCount(new LambdaQueryWrapper<ImMessage>()
                .between(ImMessage::getCreateTime, start, end));

        Map<String, Object> stats = new HashMap<>();
        stats.put("messageCount", messageCount);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("days", java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);

        return Result.success(stats);
    }
}
```

**Step 2: 编译验证**

```bash
cd ruoyi-im-api && mvn clean compile
```

Expected: `BUILD SUCCESS`

**Step 3: 提交**

```bash
git add ruoyi-im-api/src/main/java/com/ruoyi/im/controller/admin/ImStatsController.java
git commit -m "feat(admin): 添加数据统计接口

- 新增 ImStatsController 提供系统概览统计
- 支持用户活跃度、群组活跃度、消息量统计
- 支持自定义日期范围查询
"
```

---

## Task 6: 更新登录接口返回角色信息

**Files:**
- Modify: `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImAuthController.java`
- Modify: `ruoyi-im-api/src/main/java/com/ruoyi/im/vo/user/ImUserVO.java`
- Modify: `ruoyi-im-api/src/main/java/com/ruoyi/im/security/JwtAuthenticationTokenFilter.java`

**Step 1: 更新 ImUserVO 添加角色字段**

修改文件 `ruoyi-im-api/src/main/java/com/ruoyi/im/vo/user/ImUserVO.java`，添加 role 字段：

```java
/** 状态 */
private Integer status;

/** 用户角色 */
private String role;

/** 签名 */
private String signature;
```

**Step 2: 更新 JWT Token 包含角色信息**

修改 `ruoyi-im-api/src/main/java/com/ruoyi/im/security/JwtAuthenticationTokenFilter.java`，确保角色信息被加载到认证对象中。

找到创建 `UsernamePasswordAuthenticationToken` 的代码，确保用户的 role 被添加到 authorities 中。

**Step 3: 编译验证**

```bash
cd ruoyi-im-api && mvn clean compile
```

Expected: `BUILD SUCCESS`

**Step 4: 提交**

```bash
git add ruoyi-im-api/src/main/java/com/ruoyi/im/vo/user/ImUserVO.java
git add ruoyi-im-api/src/main/java/com/ruoyi/im/security/JwtAuthenticationTokenFilter.java
git commit -m "feat(auth): 登录接口返回用户角色信息

- ImUserVO 新增 role 字段
- JWT Token 包含用户角色信息
- 支持 Spring Security 角色验证
"
```

---

## Task 7: 前端 - 创建管理 API 封装

**Files:**
- Create: `ruoyi-im-web/src/api/admin.js`

**Step 1: 创建管理员 API 封装文件**

创建文件 `ruoyi-im-web/src/api/admin.js`:

```javascript
import request from '@/utils/request'

/**
 * 管理员 API 接口封装
 */

// ==================== 用户管理 ====================

/**
 * 获取用户列表（分页）
 * @param {Object} params - 查询参数 { keyword, role, pageNum, pageSize }
 * @returns {Promise}
 */
export function getUserList(params) {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 * @param {Number} id - 用户ID
 * @returns {Promise}
 */
export function getUserDetail(id) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'get'
  })
}

/**
 * 修改用户状态
 * @param {Number} id - 用户ID
 * @param {Number} status - 状态 0=禁用 1=启用
 * @returns {Promise}
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/api/admin/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 修改用户角色
 * @param {Number} id - 用户ID
 * @param {String} role - 角色 USER/ADMIN/SUPER_ADMIN
 * @returns {Promise}
 */
export function updateUserRole(id, role) {
  return request({
    url: `/api/admin/users/${id}/role`,
    method: 'put',
    params: { role }
  })
}

/**
 * 删除用户
 * @param {Number} id - 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'delete'
  })
}

/**
 * 获取用户统计
 * @returns {Promise}
 */
export function getUserStats() {
  return request({
    url: '/api/admin/users/stats',
    method: 'get'
  })
}

// ==================== 数据统计 ====================

/**
 * 获取系统概览数据
 * @returns {Promise}
 */
export function getOverview() {
  return request({
    url: '/api/admin/stats/overview',
    method: 'get'
  })
}

/**
 * 获取用户活跃度统计
 * @param {Number} days - 统计天数，默认7天
 * @returns {Promise}
 */
export function getUserActiveStats(days = 7) {
  return request({
    url: '/api/admin/stats/users/active',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取群组活跃度统计
 * @param {Number} days - 统计天数，默认7天
 * @returns {Promise}
 */
export function getGroupActiveStats(days = 7) {
  return request({
    url: '/api/admin/stats/groups/active',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取消息统计
 * @param {Object} params - { startDate, endDate }
 * @returns {Promise}
 */
export function getMessageStats(params) {
  return request({
    url: '/api/admin/stats/messages',
    method: 'get',
    params
  })
}
```

**Step 2: 提交**

```bash
git add ruoyi-im-web/src/api/admin.js
git commit -m "feat(web): 添加管理员 API 接口封装

- 新增 admin.js 封装所有管理员 API
- 包含用户管理、数据统计相关接口
"
```

---

## Task 8: 前端 - 创建管理后台布局组件

**Files:**
- Create: `ruoyi-im-web/src/views/admin/AdminLayout.vue`
- Create: `ruoyi-im-web/src/views/admin/Dashboard.vue`

**Step 1: 创建管理后台布局组件**

创建文件 `ruoyi-im-web/src/views/admin/AdminLayout.vue`:

```vue
<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="admin-aside">
        <div class="admin-logo">
          <span>IM 管理后台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#001529"
          text-color="#fff"
          active-text-color="#1890ff"
        >
          <el-menu-item index="/admin/dashboard">
            <i class="el-icon-s-platform"></i>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <i class="el-icon-user"></i>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/groups">
            <i class="el-icon-chat-dot-round"></i>
            <span>群组管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/messages">
            <i class="el-icon-chat-line-square"></i>
            <span>消息管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部栏 -->
        <el-header class="admin-header">
          <div class="header-left">
            <span class="page-title">{{ pageTitle }}</span>
          </div>
          <div class="header-right">
            <span class="admin-user">管理员</span>
            <el-button size="small" @click="goToChat">返回聊天</el-button>
            <el-button size="small" type="danger" @click="logout">退出</el-button>
          </div>
        </el-header>

        <!-- 内容区 -->
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

const pageTitleMap = {
  '/admin/dashboard': '数据概览',
  '/admin/users': '用户管理',
  '/admin/groups': '群组管理',
  '/admin/messages': '消息管理'
}

const pageTitle = computed(() => pageTitleMap[route.path] || '管理后台')

const goToChat = () => {
  router.push('/')
}

const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-aside {
  background-color: #001529;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #002140;
}

.admin-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left .page-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-user {
  margin-right: 10px;
  color: #666;
}

.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
```

**Step 2: 创建数据概览页面**

创建文件 `ruoyi-im-web/src/views/admin/Dashboard.vue`:

```vue
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon active-icon">
              <i class="el-icon-star-off"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon group-icon">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalGroups || 0 }}</div>
              <div class="stat-label">群组数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon message-icon">
              <i class="el-icon-chat-line-square"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.todayMessages || 0 }}</div>
              <div class="stat-label">今日消息</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOverview } from '@/api/admin'

const loading = ref(false)
const overview = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalGroups: 0,
  totalMessages: 0,
  todayMessages: 0
})

const loadOverview = async () => {
  loading.value = true
  try {
    const res = await getOverview()
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (error) {
    console.error('加载概览数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOverview()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.active-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.group-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.message-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  margin-left: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.loading-container {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}
</style>
```

**Step 3: 提交**

```bash
git add ruoyi-im-web/src/views/admin/
git commit -m "feat(web): 添加管理后台布局和数据概览页面

- 新增 AdminLayout.vue 管理后台布局
- 新增 Dashboard.vue 数据概览页面
- 支持侧边栏导航和顶部栏
"
```

---

## Task 9: 前端 - 创建用户管理页面

**Files:**
- Create: `ruoyi-im-web/src/views/admin/UserManagement.vue`

**Step 1: 创建用户管理页面**

创建文件 `ruoyi-im-web/src/views/admin/UserManagement.vue`:

```vue
<template>
  <div class="user-management">
    <el-card>
      <!-- 搜索栏 -->
      <el-row :gutter="20" class="search-bar">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名/昵称/手机号"
            clearable
            @clear="handleSearch"
          >
            <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchRole" placeholder="选择角色" clearable @change="handleSearch">
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="超级管理员" value="SUPER_ADMIN"></el-option>
          </el-select>
        </el-col>
      </el-row>

      <!-- 用户列表 -->
      <el-table :data="userList" v-loading="loading" border style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
        <el-table-column prop="mobile" label="手机号" width="130"></el-table-column>
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'SUPER_ADMIN'" type="danger">超级管理员</el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning">管理员</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: right"
      >
      </el-pagination>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus, deleteUser } from '@/api/admin'

const loading = ref(false)
const userList = ref([])
const searchKeyword = ref('')
const searchRole = ref('')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      keyword: searchKeyword.value,
      role: searchRole.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      userList.value = res.data.list
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadUsers()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadUsers()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadUsers()
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 ${row.nickname} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const newStatus = row.status === 1 ? 0 : 1
    const res = await updateUserStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      row.status = newStatus
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${row.nickname} 吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}
</style>
```

**Step 2: 提交**

```bash
git add ruoyi-im-web/src/views/admin/UserManagement.vue
git commit -m "feat(web): 添加用户管理页面

- 支持用户列表查询、搜索、分页
- 支持启用/禁用用户
- 支持删除用户
- 角色标签显示
"
```

---

## Task 10: 前端 - 配置路由和权限守卫

**Files:**
- Modify: `ruoyi-im-web/src/router/index.js`

**Step 1: 更新路由配置**

修改文件 `ruoyi-im-web/src/router/index.js`，添加管理后台路由：

```javascript
// 在路由配置中添加
{
  path: '/admin',
  component: () => import('@/views/admin/AdminLayout.vue'),
  meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] },
  children: [
    {
      path: 'dashboard',
      name: 'AdminDashboard',
      component: () => import('@/views/admin/Dashboard.vue'),
      meta: { title: '数据概览' }
    },
    {
      path: 'users',
      name: 'AdminUsers',
      component: () => import('@/views/admin/UserManagement.vue'),
      meta: { title: '用户管理' }
    },
    {
      path: 'groups',
      name: 'AdminGroups',
      component: () => import('@/views/admin/GroupManagement.vue'),
      meta: { title: '群组管理' }
    },
    {
      path: 'messages',
      name: 'AdminMessages',
      component: () => import('@/views/admin/MessageManagement.vue'),
      meta: { title: '消息管理' }
    }
  ]
}
```

**Step 2: 添加路由守卫检查角色**

在路由文件中添加全局前置守卫：

```javascript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole') // 假设登录时保存了角色

  // 需要认证的路由
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 需要特定角色的路由
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(userRole)) {
      // 权限不足，跳转到首页
      next('/')
      return
    }
  }

  next()
})
```

**Step 3: 提交**

```bash
git add ruoyi-im-web/src/router/index.js
git commit -m "feat(web): 添加管理后台路由和权限守卫

- 新增 /admin 路由组
- 添加角色权限检查
- 支持子路由嵌套
"
```

---

## Task 11: 更新文档

**Files:**
- Modify: `CLAUDE.md`
- Modify: `README.md`

**Step 1: 更新 CLAUDE.md**

在 CLAUDE.md 中找到项目架构部分，更新为：

```markdown
# 项目概述

RuoYi-IM 是一个**内网环境部署**的企业级即时通讯系统，采用 Java 后端和 Vue 3 前端架构。

### 部署环境
- **内网部署**: 不对外开放端口，仅内部使用
- **数据安全**: 要求高
- **网络安全**: 要求相对较低

### 架构分工

| 模块 | 端口 | 定位 | 说明 |
|------|------|------|------|
| **ruoyi-im-api** | 8080 | 核心通讯服务 | 提供 IM 核心功能和管理后台 API |
| **ruoyi-im-web** | 5173 | 用户界面 | 聊天界面 + 管理后台（根据角色动态展示） |
```

删除关于 ruoyi-im-admin 的所有描述。

**Step 2: 更新 README.md**

更新部署说明，简化为两个服务的部署。

**Step 3: 提交**

```bash
git add CLAUDE.md README.md
git commit -m "docs: 更新文档移除 ruoyi-im-admin

- 移除若依后台管理系统相关描述
- 更新架构为 api + web 双服务模式
- 简化部署流程说明
"
```

---

## Task 12: 清理 ruoyi-im-admin 目录

**Files:**
- Delete: `ruoyi-im-admin/`

**Step 1: 删除 ruoyi-im-admin 目录**

```bash
git rm -r ruoyi-im-admin
```

**Step 2: 提交**

```bash
git commit -m "refactor: 移除 ruoyi-im-admin 后台管理系统

- 删除若依后台管理系统代码
- 管理功能已迁移到 ruoyi-im-api
- 前端管理页面已集成到 ruoyi-im-web
"
```

---

## Task 13: 最终测试验证

**Step 1: 启动后端服务**

```bash
cd ruoyi-im-api
mvn clean package
# 运行 ImApplication 的 main 方法
```

**Step 2: 启动前端服务**

```bash
cd ruoyi-im-web
npm install
npm run dev
```

**Step 3: 功能验证清单**

- [ ] 用户登录成功，Token 包含角色信息
- [ ] 普通用户无法访问 `/api/admin/*` 接口（返回 403）
- [ ] 管理员可以访问 `/api/admin/*` 接口
- [ ] 前端根据角色显示不同的导航菜单
- [ ] 管理后台可以正常访问 `/admin/dashboard`
- [ ] 用户管理页面可以正常加载数据
- [ ] 用户状态修改功能正常

**Step 4: 提交最终版本**

```bash
git add .
git commit -m "feat: 完成统一架构迁移

- 移除 ruoyi-im-admin 后台管理系统
- 管理功能集成到 ruoyi-im-api
- 前端管理页面集成到 ruoyi-im-web
- 基于 Spring Security 实现角色权限控制

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
"
```

---

## 实施完成后的架构

```
┌─────────────────────────────────────────────────────────┐
│                    ruoyi-im-web                          │
│         (Vue 3 - 用户界面 + 管理界面)                     │
│                 根据权限显示不同模块                       │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│                   ruoyi-im-api                           │
│           (Spring Boot - 统一API服务)                    │
│       ┌─────────────────┬──────────────────┐            │
│       │  用户API        │   管理API         │            │
│       │  /api/im/*      │   /api/admin/*   │            │
│       └─────────────────┴──────────────────┘            │
└─────────────────────────────────────────────────────────┘
```

---

**计划完成！** 保存到 `docs/plans/2025-01-24-unified-architecture.md`
