package com.ruoyi.im.service.impl;

import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.utils.JwtUtils;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    @Override
    public ImLoginVO login(ImLoginRequest request) {
        // 1. 验证用户名密码
        ImUser user = imUserMapper.selectImUserByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("USER_NOT_EXIST", "用户不存在");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("PASSWORD_ERROR", "密码错误");
        }

        // 2. 生成Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId());

        // 3. 记录登录日志
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp("127.0.0.1"); // 后续需要获取真实IP
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
        // 1. 检查用户名是否存在
        ImUser existingUser = imUserMapper.selectImUserByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException("USER_ALREADY_EXIST", "用户已存在");
        }

        // 2. 密码加密存储
        ImUser user = new ImUser();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(0); // 0=正常
        user.setGender(0); // 0=未知
        user.setAvatar("/avatar/default.png");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 3. 保存用户信息
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
        vo.setOnline(true); // 后续需要根据WebSocket连接状态判断
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
    public void updateStatus(Long userId, Integer status) {
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
        // TODO: 实现用户权限查询逻辑
        // 1. 查询用户角色
        // 2. 查询角色权限
        // 3. 合并权限集合
        Set<String> permissions = new HashSet<>();
        permissions.add("im:user:view");
        permissions.add("im:user:edit");
        permissions.add("im:message:send");
        return permissions;
    }
}
