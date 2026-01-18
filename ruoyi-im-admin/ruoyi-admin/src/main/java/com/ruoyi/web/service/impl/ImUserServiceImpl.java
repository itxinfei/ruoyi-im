package com.ruoyi.web.service.impl;

import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.web.domain.ImUser;
import com.ruoyi.web.mapper.ImUserMapper;
import com.ruoyi.web.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM用户Service实现（Admin模块专用）
 *
 * 使用 BCryptPasswordEncoder 加密密码，无需单独存储 salt
 */
@Service
public class ImUserServiceImpl implements ImUserService {

    @Autowired
    private ImUserMapper userMapper;

    /** BCrypt密码编码器 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public List<ImUser> selectImUserList(ImUser imUser) {
        return userMapper.selectImUserList(imUser);
    }

    @Override
    public ImUser selectImUserById(Long id) {
        return userMapper.selectImUserById(id);
    }

    @Override
    public int insertImUser(ImUser imUser) {
        // 对密码进行加密（BCrypt）
        if (imUser.getPassword() != null && !imUser.getPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(imUser.getPassword());
            imUser.setPassword(encryptedPassword);
        }
        return userMapper.insertImUser(imUser);
    }

    @Override
    public int updateImUser(ImUser imUser) {
        // 对密码进行加密（BCrypt）- 仅当密码被修改时
        if (imUser.getPassword() != null && !imUser.getPassword().isEmpty()) {
            // 检查密码是否已经是 BCrypt 格式（以 $2a$ 或 $2b$ 开头）
            if (!imUser.getPassword().startsWith("$2")) {
                String encryptedPassword = passwordEncoder.encode(imUser.getPassword());
                imUser.setPassword(encryptedPassword);
            }
        }
        return userMapper.updateImUser(imUser);
    }

    @Override
    public int deleteImUserById(Long id) {
        return userMapper.deleteImUserById(id);
    }

    @Override
    public int deleteImUserByIds(Long[] ids) {
        return userMapper.deleteImUserByIds(ids);
    }

    @Override
    public int resetPassword(Long id, String password, String adminPassword) {
        try {
            ImUser user = userMapper.selectImUserById(id);
            if (user == null) {
                return -2;
            }

            ImUser adminUser = userMapper.selectImUserById(ShiroUtils.getUserId());
            if (adminUser == null) {
                return -1;
            }

            if (!passwordEncoder.matches(adminPassword, adminUser.getPassword())) {
                return -1;
            }

            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            user.setUpdateTime(new java.util.Date());

            return userMapper.updateImUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("重置密码失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int changeStatus(Long id, String status) {
        return userMapper.changeStatus(id, status);
    }

    @Override
    public int countOnlineUsers() {
        return userMapper.countOnlineUsers();
    }

    @Override
    public boolean checkUsernameUnique(String username) {
        return userMapper.checkUsernameUnique(username);
    }

    @Override
    public List<ImUser> searchUsers(String keyword) {
        return userMapper.searchUsers(keyword);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> result = new HashMap<>();
        int total = userMapper.countTotalUsers();
        int online = userMapper.countOnlineUsers();
        result.put("totalCount", total);
        result.put("onlineCount", online);
        result.put("offlineCount", total - online);
        result.put("disabledCount", userMapper.countDisabledUsers());
        return result;
    }
}
