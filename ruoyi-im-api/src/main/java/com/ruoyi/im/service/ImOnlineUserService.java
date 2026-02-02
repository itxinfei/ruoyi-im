package com.ruoyi.im.service;

import com.ruoyi.im.vo.online.OnlineUserVO;

import java.util.List;

/**
 * 在线用户服务接口
 *
 * @author ruoyi
 */
public interface ImOnlineUserService {

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    List<OnlineUserVO> getOnlineUsers();

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    Integer getOnlineUserCount();

    /**
     * 踢出用户（强制下线）
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean kickUser(Long userId);

    /**
     * 根据会话ID踢出用户
     *
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean kickBySessionId(String sessionId);
}
