package com.ruoyi.web.service.impl;

import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.framework.shiro.service.SysPasswordService;
import com.ruoyi.web.domain.ImUser;
import com.ruoyi.web.mapper.ImUserMapper;
import com.ruoyi.web.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM用户Service实现（Admin模块专用）
 */
@Service
public class ImUserServiceImpl implements ImUserService {

    @Autowired
    private ImUserMapper userMapper;

    @Autowired
    private SysPasswordService passwordService;

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
        // 对密码进行加密
        if (imUser.getPassword() != null && !imUser.getPassword().isEmpty()) {
            String salt = ShiroUtils.randomSalt();
            String encryptedPassword = passwordService.encryptPassword(imUser.getUsername(), imUser.getPassword(), salt);
            imUser.setPassword(encryptedPassword);
        }
        return userMapper.insertImUser(imUser);
    }

    @Override
    public int updateImUser(ImUser imUser) {
        // 对密码进行加密
        if (imUser.getPassword() != null && !imUser.getPassword().isEmpty()) {
            String salt = ShiroUtils.randomSalt();
            String encryptedPassword = passwordService.encryptPassword(imUser.getUsername(), imUser.getPassword(), salt);
            imUser.setPassword(encryptedPassword);
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
    public int resetPassword(Long id, String password) {
        try {
            ImUser user = userMapper.selectImUserById(id);
            if (user != null) {
                // 对新密码进行加密
                String salt = ShiroUtils.randomSalt();
                String encryptedPassword = passwordService.encryptPassword(user.getUsername(), password, salt);
                // 更新密码
                user.setPassword(encryptedPassword);
                // 更新时间
                user.setUpdateTime(new java.util.Date());
                return userMapper.updateImUser(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 记录错误日志
            System.err.println("重置密码失败: " + e.getMessage());
            return 0;
        }
        return 0;
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
