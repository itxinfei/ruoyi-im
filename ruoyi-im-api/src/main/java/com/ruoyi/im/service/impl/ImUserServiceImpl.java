package com.ruoyi.im.service.impl;

import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.BasePageRequest;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImUserService;

import com.ruoyi.im.util.FileUtils;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.util.JwtUtils;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    /**
     * 构造器注入依赖
     *
     * @param imUserMapper 用户Mapper
     * @param passwordEncoder 密码加密器
     * @param jwtUtils JWT工具类
     * @param imRedisUtil Redis工具类
     */
    public ImUserServiceImpl(ImUserMapper imUserMapper,
                              PasswordEncoder passwordEncoder,
                              JwtUtils jwtUtils,
                              ImRedisUtil imRedisUtil) {
        this.imUserMapper = imUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.imRedisUtil = imRedisUtil;
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
            throw new BusinessException("PASSWORD_ERROR", "密码错误");
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
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1); // 1=启用
        user.setGender(0);
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
        BeanUtils.copyProperties(user, vo);
        
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

        BeanUtils.copyProperties(request, user);
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
            BeanUtils.copyProperties(user, vo);
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
        BeanUtils.copyProperties(request, user);
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
    public void deleteUser(Long userId) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }
        imUserMapper.deleteImUserById(userId);
    }

    @Override
    public void batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        imUserMapper.deleteImUserByIds(userIds.toArray(new Long[0]));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        // 重置为默认密码
        user.setPassword(passwordEncoder.encode(SystemConstants.DEFAULT_PASSWORD));
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
                // 忽略无效的用户ID
            }
        }

        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ImUser> users = imUserMapper.selectImUserListByIds(userIds);
        List<ImUserVO> onlineUsers = new ArrayList<>();

        for (ImUser user : users) {
            ImUserVO vo = new ImUserVO();
            BeanUtils.copyProperties(user, vo);
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
        String filePath = uploadPath + relativePath;

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
        String avatarUrl = urlPrefix + relativePath;

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
            BeanUtils.copyProperties(user, vo);
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
            BeanUtils.copyProperties(user, vo);
            vo.setOnline(imRedisUtil.isOnlineUser(user.getId()));
            voList.add(vo);
        }

        return voList;
    }
}