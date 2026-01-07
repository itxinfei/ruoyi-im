package com.ruoyi.im.service.impl;

import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.BasePageRequest;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImUserService;

import com.ruoyi.im.utils.FileUtils;
import com.ruoyi.im.utils.ImRedisUtil;
import com.ruoyi.im.utils.JwtUtils;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ImRedisUtil imRedisUtil;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;



    @Override
    public ImLoginVO login(ImLoginRequest request) {
        ImUser user = imUserMapper.selectImUserByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("USER_NOT_EXIST", "用户不存在");
        }

        logger.info("用户登录 - 用户名: {}, 请求密码: {}, 数据库密码: {}", 
            request.getUsername(), request.getPassword(), user.getPassword());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.error("密码验证失败 - 用户名: {}, 请求密码长度: {}, 数据库密码长度: {}", 
                request.getUsername(), request.getPassword().length(), user.getPassword().length());
            throw new BusinessException("PASSWORD_ERROR", "密码错误");
        }

        logger.info("密码验证成功 - 用户名: {}", request.getUsername());

        String token = jwtUtils.generateToken(user.getUsername(), user.getId());

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
    public Long register(ImRegisterRequest request) {
        ImUser existingUser = imUserMapper.selectImUserByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException("USER_ALREADY_EXIST", "用户已存在");
        }

        ImUser user = new ImUser();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE"); // 使用字符串状态值
        user.setGender(0);
        user.setAvatar("/avatar/default.png");
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
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_EXIST", "用户不存在");
        }

        ImUserVO vo = new ImUserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setOnline(true);
        return vo;
    }

    @Override
    public void updateUser(Long userId, ImUserUpdateRequest request) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_EXIST", "用户不存在");
        }

        BeanUtils.copyProperties(request, user);
        user.setUpdateTime(LocalDateTime.now());
        imUserMapper.updateImUser(user);
    }

    @Override
    public void updateStatus(Long userId, String status) { // 修改参数类型为String
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_EXIST", "用户不存在");
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        imUserMapper.updateImUser(user);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_EXIST", "用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ImErrorCode.PASSWORD_ERROR, "密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        int result = imUserMapper.updateImUser(user);
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
    public Long createUser(ImRegisterRequest request) {
        ImUser existingUser = imUserMapper.selectImUserByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ImErrorCode.USER_ALREADY_EXIST, "用户名已存在");
        }

        ImUser user = new ImUser();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE"); // 使用字符串状态值
        user.setGender(0);
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("/avatar/default.png");
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
        for (Long userId : userIds) {
            imUserMapper.deleteImUserById(userId);
        }
    }

    @Override
    public void resetPassword(Long userId) {
        ImUser user = imUserMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        // 重置为默认密码 123456
        String defaultPassword = "123456";
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setUpdateTime(LocalDateTime.now());
        imUserMapper.updateImUser(user);
    }

    @Override
    public List<ImUserVO> getOnlineUsers() {
        // 通过ImRedisUtil获取在线用户列表
        Set<String> onlineUserIds = imRedisUtil.getOnlineUsers();
        List<ImUserVO> onlineUsers = new ArrayList<>();
        
        for (String userIdStr : onlineUserIds) {
            try {
                Long userId = Long.parseLong(userIdStr);
                ImUser user = imUserMapper.selectImUserById(userId);
                if (user != null) {
                    ImUserVO vo = new ImUserVO();
                    BeanUtils.copyProperties(user, vo);
                    vo.setOnline(true);
                    onlineUsers.add(vo);
                }
            } catch (NumberFormatException e) {
                // 忽略无效的用户ID
            }
        }
        
        return onlineUsers;
    }

    @Override
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

        logger.info("用户头像上传成功，userId={}, avatarUrl={}", userId, avatarUrl);

        return avatarUrl;
    }
}