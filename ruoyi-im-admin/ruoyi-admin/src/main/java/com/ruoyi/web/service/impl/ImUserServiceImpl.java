package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImUser;
import com.ruoyi.web.mapper.ImUserMapper;
import com.ruoyi.web.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * IM用户Service实现（Admin模块专用）
 */
@Service
public class ImUserServiceImpl implements ImUserService {

    @Autowired
    private ImUserMapper userMapper;

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
        return userMapper.insertImUser(imUser);
    }

    @Override
    public int updateImUser(ImUser imUser) {
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
        return userMapper.resetPassword(id, password);
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
}
