package com.ruoyi.im.service.impl;

import com.ruoyi.im.config.FileUploadConfig;
import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.constant.UserRole;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.domain.ImChatBackground;
import com.ruoyi.im.dto.BasePageRequest;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.mapper.ImChatBackgroundMapper;
import com.ruoyi.im.service.ImUserService;

import com.ruoyi.im.util.FileUtils;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.util.JwtUtils;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 用户服务实现
 *
 * @author ruoyi
 */
@Service
public class ImUserServiceImpl implements ImUserService {

    private static final Logger logger = LoggerFactory.getLogger(ImUserServiceImpl.class);

    private final ImUserMapper imUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ImRedisUtil imRedisUtil;
    private final ImChatBackgroundMapper imChatBackgroundMapper;

    @Resource
    private FileUploadConfig fileUploadConfig;

    /**
     * 构造器注入依赖
     *
     * @param imUserMapper           用户Mapper
     * @param passwordEncoder        密码加密器
     * @param jwtUtils               JWT工具类
     * @param imRedisUtil            Redis工具类
     * @param imChatBackgroundMapper 聊天背景Mapper
     */
    public ImUserServiceImpl(ImUserMapper imUserMapper,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            ImRedisUtil imRedisUtil,
            ImChatBackgroundMapper imChatBackgroundMapper) {
        this.imUserMapper = imUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.imRedisUtil = imRedisUtil;
        this.imChatBackgroundMapper = imChatBackgroundMapper;
    }

    @Override
    public ImLoginVO login(ImLoginRequest request) {
        ImUser user = imUserMapper.selectImUserByUsername(request.getUsername());

        if (user == null) {
            logger.warn("登录失败 - 用户不存在: {}", request.getUsername());
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        // 防御性检查：确保密码不为 null
        String password = request.getPassword();
        String dbPassword = user.getPassword();

        logger.info("用户登录尝试 - 用户名: {}, 请求密码长度: {}, 数据库密码长度: {}",
                request.getUsername(),
                password != null ? password.length() : "null",
                dbPassword != null ? dbPassword.length() : "null");

        if (password == null || dbPassword == null || !passwordEncoder.matches(password, dbPassword)) {
            logger.error("密码验证失败 - 用户名: {}", request.getUsername());
            throw new BusinessException(ImErrorCode.PASSWORD_ERROR, "密码错误");
        }

        logger.info("密码验证成功 - 用户名: {}, 用户ID: {}", request.getUsername(), user.getId());

        String token = jwtUtils.generateToken(user.getUsername(), user.getId(),
                user.getRole() != null ? user.getRole() : "USER");

        user.setLastOnlineTime(LocalDateTime.now());
        imUserMapper.updateImUser(user);

        ImLoginVO vo = new ImLoginVO();
        vo.setToken(token);

        ImLoginVO.UserInfo userInfo = new ImLoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole() != null ? user.getRole() : "USER");
        vo.setUserInfo(userInfo);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(ImRegisterRequest request) {
        ImUser existingUser = imUserMapper.selectImUserByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException("USER_ALREADY_EXIST", "用户已存在");
        }

        ImUser user = new ImUser();
        BeanConvertUtil.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1); // 1=启用
        user.setGender(0);
        user.setRole(UserRole.USER); // 默认角色为普通用户
        user.setAvatar(SystemConstants.DEFAULT_USER_AVATAR);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        int result = imUserMapper.insertImUser(user);
        if (result <= 0) {
            throw new BusinessException("REGISTER_FAILED", "注册失败");
        }

        return user.getId();
    }

    @Override
    public ImUserVO getUserById(Long userId) {
        // 尝试从缓存获取用户信息
        Object cachedObj = imRedisUtil.getUserInfo(userId);

        if (cachedObj != null && cachedObj instanceof ImUserVO) {
            ImUserVO cachedVo = (ImUserVO) cachedObj;
            // 缓存命中，更新实时在线状态
            cachedVo.setOnline(imRedisUtil.isOnlineUser(userId));
            return cachedVo;
        }

        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        ImUserVO vo = new ImUserVO();
        BeanConvertUtil.copyProperties(user, vo);

        // 缓存用户信息（不包含实时在线状态）
        imRedisUtil.cacheUserInfo(userId, vo);

        // 设置实时在线状态
        vo.setOnline(imRedisUtil.isOnlineUser(userId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, ImUserUpdateRequest request) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        BeanConvertUtil.copyProperties(request, user);
        user.setUpdateTime(LocalDateTime.now());
        imUserMapper.updateImUser(user);

        // 清除缓存
        imRedisUtil.evictUserInfo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, Integer status) { // 0=禁用, 1=启用
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        imUserMapper.updateImUser(user);

        // 清除缓存
        imRedisUtil.evictUserInfo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ImErrorCode.PASSWORD_ERROR, "密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        int result = imUserMapper.updateImUser(user);

        // 清除缓存
        imRedisUtil.evictUserInfo(userId);
        return result > 0;
    }

    @Override
    public ImUser findByUsername(String username) {
        return imUserMapper.selectImUserByUsername(username);
    }

    @Override
    public Set<String> getUserPermissions(Long userId) {
        Set<String> permissions = new HashSet<>();
        try {
            ImUser user = imUserMapper.selectImUserById(userId);
            if (user != null) {
                permissions.add("im:user:view");
                permissions.add("im:message:send");
                permissions.add("im:message:view");
                permissions.add("im:group:view");
                permissions.add("im:contact:view");
            }
        } catch (Exception e) {
            logger.error("查询用户权限失败，userId={}", userId, e);
        }
        return permissions;
    }

    @Override
    public int getTotalUserCount() {
        return imUserMapper.countImUsers();
    }

    @Override
    public List<ImUserVO> getUserList(BasePageRequest request) {
        ImUser queryUser = new ImUser();
        List<ImUser> users = imUserMapper.selectImUserList(queryUser);
        List<ImUserVO> voList = new ArrayList<>();
        for (ImUser user : users) {
            ImUserVO vo = new ImUserVO();
            BeanConvertUtil.copyProperties(user, vo);
            vo.setOnline(true);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(ImRegisterRequest request) {
        ImUser existingUser = imUserMapper.selectImUserByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ImErrorCode.USER_ALREADY_EXIST, "用户名已存在");
        }

        ImUser user = new ImUser();
        BeanConvertUtil.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1); // 1=启用
        user.setGender(0);
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar(SystemConstants.DEFAULT_USER_AVATAR);
        }
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        int result = imUserMapper.insertImUser(user);
        if (result <= 0) {
            throw new BusinessException(ImErrorCode.REGISTER_FAILED, "创建用户失败");
        }
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }
        imUserMapper.deleteImUserById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        imUserMapper.deleteImUserByIds(userIds.toArray(new Long[0]));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        // 如果未提供新密码，使用系统默认密码
        String password = (newPassword == null || newPassword.trim().isEmpty())
                ? SystemConstants.DEFAULT_PASSWORD
                : newPassword;

        user.setPassword(passwordEncoder.encode(password));
        user.setUpdateTime(LocalDateTime.now());
        imUserMapper.updateImUser(user);

        // 清除缓存
        imRedisUtil.evictUserInfo(userId);
    }

    @Override
    public List<ImUserVO> getOnlineUsers() {
        // 通过ImRedisUtil获取在线用户列表
        Set<String> onlineUserIds = imRedisUtil.getOnlineUsers();
        if (onlineUserIds == null || onlineUserIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> userIds = new ArrayList<>();
        for (String userIdStr : onlineUserIds) {
            try {
                userIds.add(Long.parseLong(userIdStr));
            } catch (NumberFormatException e) {
                logger.warn("无效的用户ID格式: userIdStr={}", userIdStr);
            }
        }

        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ImUser> users = imUserMapper.selectImUserListByIds(userIds);
        List<ImUserVO> onlineUsers = new ArrayList<>();

        for (ImUser user : users) {
            ImUserVO vo = new ImUserVO();
            BeanConvertUtil.copyProperties(user, vo);
            vo.setOnline(true);
            onlineUsers.add(vo);
        }

        return onlineUsers;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("FILE_EMPTY", "头像文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = FileUtils.getFileExtension(originalFilename);

        // 验证文件类型
        if (!FileUtils.isImage(originalFilename)) {
            throw new BusinessException("FILE_TYPE_ERROR", "只支持图片格式的头像");
        }

        // 验证文件大小（最大2MB）
        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("FILE_SIZE_ERROR", "头像文件大小不能超过2MB");
        }

        // 生成文件名和路径
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = "avatar/" + datePath + "/" + fileName;
        String filePath = fileUploadConfig.getAbsoluteUploadPath() + relativePath;

        // 确保目录存在
        File targetFile = new File(filePath);
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 保存文件
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            logger.error("头像上传失败: {}", e.getMessage(), e);
            throw new BusinessException("UPLOAD_FAILED", "头像上传失败");
        }

        // 生成访问URL
        String avatarUrl = fileUploadConfig.buildFileUrl(relativePath);

        // 更新用户头像
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_EXIST", "用户不存在");
        }

        user.setAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        imUserMapper.updateImUser(user);

        // 清除缓存
        imRedisUtil.evictUserInfo(userId);

        logger.info("用户头像上传成功，userId={}, avatarUrl={}", userId, avatarUrl);

        return avatarUrl;
    }

    @Override
    public List<ImUserVO> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<ImUser> users = imUserMapper.selectImUserByKeyword(keyword.trim());
        List<ImUserVO> voList = new ArrayList<>();

        for (ImUser user : users) {
            ImUserVO vo = new ImUserVO();
            BeanConvertUtil.copyProperties(user, vo);
            vo.setOnline(imRedisUtil.isOnlineUser(user.getId()));
            voList.add(vo);
        }

        return voList;
    }

    @Override
    public List<ImUserVO> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ImUser> users = imUserMapper.selectImUserListByIds(userIds);
        List<ImUserVO> voList = new ArrayList<>();

        for (ImUser user : users) {
            ImUserVO vo = new ImUserVO();
            BeanConvertUtil.copyProperties(user, vo);
            vo.setOnline(imRedisUtil.isOnlineUser(user.getId()));
            voList.add(vo);
        }

        return voList;
    }

    @Override
    public List<ImUserVO> getUserListWithPagination(String keyword, String role, int offset, int limit) {
        ImUser query = new ImUser();
        // 传递关键词给 Mapper，由 Mapper 负责多字段匹配逻辑
        if (role != null && !role.isEmpty()) {
            query.setRole(role);
        }

        List<ImUser> users = imUserMapper.selectImUserListWithPagination(query, offset, limit, keyword);
        List<ImUserVO> voList = new ArrayList<>();
        for (ImUser user : users) {
            ImUserVO vo = new ImUserVO();
            BeanConvertUtil.copyProperties(user, vo);
            vo.setOnline(imRedisUtil.isOnlineUser(user.getId()));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public int countUsers(String keyword, String role) {
        ImUser query = new ImUser();
        if (role != null && !role.isEmpty()) {
            query.setRole(role);
        }
        return imUserMapper.selectImUserCount(query, keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long userId, String role) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }
        user.setRole(role);
        user.setUpdateTime(LocalDateTime.now());
        imUserMapper.updateImUser(user);
        imRedisUtil.evictUserInfo(userId);
    }

    @Override
    public java.util.Map<String, Long> getUserStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        int total = imUserMapper.countImUsers();
        long online = imRedisUtil.getOnlineUserCount();

        stats.put("total", (long) total);
        stats.put("online", online);
        stats.put("offline", Math.max(0, total - online));

        return stats;
    }

    @Override
    public Long adminCreateUser(java.util.Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        String nickname = (String) data.get("nickname");
        String mobile = (String) data.get("mobile");
        String email = (String) data.get("email");
        String role = (String) data.getOrDefault("role", "USER");

        // 验证必填字段
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        // 检查用户名是否已存在
        if (imUserMapper.selectImUserByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        ImUser user = new ImUser();
        user.setUsername(username);
        user.setPassword(password); // 这里应该加密，实际项目中需要使用 BCryptPasswordEncoder
        user.setNickname(nickname != null ? nickname : username);
        user.setMobile(mobile);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());

        imUserMapper.insertImUser(user);
        return user.getId();
    }

    @Override
    public void adminUpdateUser(Long userId, java.util.Map<String, Object> data) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新字段
        if (data.containsKey("nickname")) {
            user.setNickname((String) data.get("nickname"));
        }
        if (data.containsKey("mobile")) {
            user.setMobile((String) data.get("mobile"));
        }
        if (data.containsKey("email")) {
            user.setEmail((String) data.get("email"));
        }
        if (data.containsKey("role")) {
            user.setRole((String) data.get("role"));
        }
        if (data.containsKey("status")) {
            user.setStatus((Integer) data.get("status"));
        }
        if (data.containsKey("avatar")) {
            user.setAvatar((String) data.get("avatar"));
        }
        if (data.containsKey("signature")) {
            user.setSignature((String) data.get("signature"));
        }

        user.setUpdateTime(java.time.LocalDateTime.now());
        imUserMapper.updateImUser(user);
    }

    @Override
    public void batchUpdateUserStatus(List<Long> userIds, Integer status) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            updateStatus(userId, status);
        }
    }

    @Override
    public List<java.util.Map<String, Object>> getUserOptions(String keyword) {
        List<ImUserVO> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = searchUsers(keyword);
        } else {
            users = getUserListWithPagination(null, null, 0, 100);
        }

        List<java.util.Map<String, Object>> options = new java.util.ArrayList<>();
        for (ImUserVO user : users) {
            java.util.Map<String, Object> option = new java.util.HashMap<>();
            option.put("value", user.getId());
            option.put("label", user.getNickname() != null ? user.getNickname() : user.getUsername());
            option.put("avatar", user.getAvatar());
            option.put("role", user.getRole());
            options.add(option);
        }
        return options;
    }

    @Override
    public java.util.Map<String, Object> getChatBackground(Long userId, Long conversationId) {
        // 查询用户的聊天背景设置
        ImChatBackground background = imChatBackgroundMapper.selectByUserIdAndConversationId(userId, conversationId);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (background != null) {
            result.put("type", background.getBackgroundType());
            result.put("value", background.getBackgroundValue());
            result.put("conversationId", background.getConversationId());
        } else {
            // 返回默认背景
            result.put("type", "default");
            result.put("value", null);
            result.put("conversationId", conversationId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Object> importUsers(MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            // 设置表头映射
            reader.addHeaderAlias("用户名", "username");
            reader.addHeaderAlias("昵称", "nickname");
            reader.addHeaderAlias("邮箱", "email");
            reader.addHeaderAlias("手机号", "mobile");
            reader.addHeaderAlias("角色", "role");

            List<ImUser> users = reader.readAll(ImUser.class);
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            if (users == null || users.isEmpty()) {
                result.put("successCount", 0);
                result.put("failCount", 0);
                result.put("message", "未找到合适的导入数据");
                return result;
            }

            int successCount = 0;
            int failureCount = 0;
            List<String> errors = new java.util.ArrayList<>();

            for (ImUser user : users) {
                try {
                    // 补全默认信息
                    if (user.getPassword() == null || user.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(SystemConstants.DEFAULT_PASSWORD));
                    } else {
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                    }

                    if (user.getNickname() == null || user.getNickname().isEmpty()) {
                        user.setNickname(user.getUsername());
                    }

                    user.setStatus(1);
                    user.setCreateTime(LocalDateTime.now());
                    user.setUpdateTime(LocalDateTime.now());

                    if (imUserMapper.selectImUserByUsername(user.getUsername()) != null) {
                        failureCount++;
                        errors.add("用户 " + user.getUsername() + " 已存在");
                        continue;
                    }

                    imUserMapper.insertImUser(user);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    errors.add("用户 " + user.getUsername() + " 导入失败: " + e.getMessage());
                }
            }

            result.put("successCount", successCount);
            result.put("failCount", failureCount);
            result.put("errors", errors);
            return result;
        } catch (IOException e) {
            logger.error("导入用户失败", e);
            throw new BusinessException("导入失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        // 写入表头
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("nickname", "昵称");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("mobile", "手机号");
        writer.addHeaderAlias("role", "角色");

        // 写入示例数据
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("username", "zhangsan");
        row.put("nickname", "张三");
        row.put("email", "zhangsan@example.com");
        row.put("mobile", "13800138000");
        row.put("role", "USER");
        rows.add(row);

        writer.write(rows, true);

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        out.close();
    }

    @Override
    public void exportUsers(HttpServletResponse response, String keyword, String role) throws IOException {
        ImUser query = new ImUser();
        if (keyword != null && !keyword.isEmpty()) {
            query.setNickname(keyword);
        }
        if (role != null && !role.isEmpty()) {
            query.setRole(role);
        }

        // 导出全部（不分页）或者设置一个较大的限制
        List<ImUser> users = imUserMapper.selectImUserListWithPagination(query, 0, 10000, keyword);

        ExcelWriter writer = ExcelUtil.getWriter(true);
        // 设置映射
        writer.addHeaderAlias("id", "用户ID");
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("nickname", "昵称");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("mobile", "手机号");
        writer.addHeaderAlias("role", "角色");
        writer.addHeaderAlias("status", "状态");
        writer.addHeaderAlias("createTime", "创建时间");

        // 只导出指定字段
        writer.setOnlyAlias(true);
        writer.write(users, true);

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        out.close();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setChatBackground(Long userId, String type, String value, Long conversationId) {
        // 验证背景类型
        if (!"default".equals(type) && !"color".equals(type) && !"image".equals(type)) {
            throw new BusinessException("INVALID_BACKGROUND_TYPE", "无效的背景类型");
        }

        // 查询现有设置
        ImChatBackground background = imChatBackgroundMapper.selectByUserIdAndConversationId(userId, conversationId);

        if (background != null) {
            // 更新现有设置
            background.setBackgroundType(type);
            background.setBackgroundValue(value);
            background.setUpdateTime(LocalDateTime.now());
            imChatBackgroundMapper.updateById(background);
        } else {
            // 创建新设置
            background = new ImChatBackground();
            background.setUserId(userId);
            background.setConversationId(conversationId);
            background.setBackgroundType(type);
            background.setBackgroundValue(value);
            background.setCreateTime(LocalDateTime.now());
            background.setUpdateTime(LocalDateTime.now());
            imChatBackgroundMapper.insert(background);
        }

        logger.info("设置聊天背景成功，userId={}, type={}, conversationId={}", userId, type, conversationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearChatBackground(Long userId, Long conversationId) {
        // 删除用户的聊天背景设置
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ImChatBackground> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(ImChatBackground::getUserId, userId);
        if (conversationId == null) {
            wrapper.isNull(ImChatBackground::getConversationId);
        } else {
            wrapper.eq(ImChatBackground::getConversationId, conversationId);
        }

        imChatBackgroundMapper.delete(wrapper);
        logger.info("清除聊天背景成功，userId={}, conversationId={}", userId, conversationId);
    }

    @Override
    public java.util.Map<String, Object> scanQRCode(Long userId, String qrData) {
        logger.info("用户扫描二维码，userId={}, qrData={}", userId, qrData);

        java.util.Map<String, Object> result = new java.util.HashMap<>();

        // 判断二维码类型
        if (qrData == null || qrData.isEmpty()) {
            throw new BusinessException("INVALID_QR_CODE", "无效的二维码");
        }

        // 检查是否是用户二维码（格式：user:userId）
        if (qrData.startsWith("user:")) {
            try {
                Long targetUserId = Long.parseLong(qrData.substring(5));
                ImUserVO targetUser = getUserById(targetUserId);
                result.put("type", "user");
                result.put("data", targetUser);
            } catch (Exception e) {
                logger.error("解析用户二维码失败", e);
                throw new BusinessException("INVALID_USER_QR_CODE", "无效的用户二维码");
            }
        }
        // 检查是否是群组二维码（格式：group:groupId）
        else if (qrData.startsWith("group:")) {
            try {
                Long groupId = Long.parseLong(qrData.substring(6));
                result.put("type", "group");
                result.put("groupId", groupId);
            } catch (Exception e) {
                logger.error("解析群组二维码失败", e);
                throw new BusinessException("INVALID_GROUP_QR_CODE", "无效的群组二维码");
            }
        }
        // 检查是否是登录二维码（格式：login:token）
        else if (qrData.startsWith("login:")) {
            String token = qrData.substring(6);
            result.put("type", "login");
            result.put("token", token);
        }
        // 其他类型的URL
        else if (qrData.startsWith("http://") || qrData.startsWith("https://")) {
            result.put("type", "url");
            result.put("url", qrData);
        }
        // 纯文本
        else {
            result.put("type", "text");
            result.put("content", qrData);
        }

        return result;
    }

    // ==================== 供其他Service调用的内部方法 ====================

    @Override
    public ImUser getUserEntityById(Long userId) {
        if (userId == null) {
            return null;
        }
        return imUserMapper.selectImUserById(userId);
    }

    @Override
    public List<ImUser> getUserEntitiesByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return imUserMapper.selectImUserListByIds(userIds);
    }

    @Override
    public ImUser getUserEntityByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        return imUserMapper.selectImUserByUsername(username);
    }

    @Override
    public List<ImUser> searchUserEntities(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }
        return imUserMapper.selectImUserByKeyword(keyword.trim());
    }

    @Override
    public List<ImUser> getUserEntities(ImUser query) {
        if (query == null) {
            return new ArrayList<>();
        }
        return imUserMapper.selectImUserList(query);
    }
}