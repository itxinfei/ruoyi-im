package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImUser;

import java.util.List;
import java.util.Map;

/**
 * IM用户Service接口（Admin模块专用）
 */
public interface ImUserService {

    List<ImUser> selectImUserList(ImUser imUser);

    ImUser selectImUserById(Long id);

    int insertImUser(ImUser imUser);

    int updateImUser(ImUser imUser);

    int deleteImUserById(Long id);

    int deleteImUserByIds(Long[] ids);

    int resetPassword(Long id, String password);

    int changeStatus(Long id, String status);

    int countOnlineUsers();

    boolean checkUsernameUnique(String username);

    List<ImUser> searchUsers(String keyword);

    /**
     * 获取用户统计数据
     */
    Map<String, Object> getUserStatistics();
}
