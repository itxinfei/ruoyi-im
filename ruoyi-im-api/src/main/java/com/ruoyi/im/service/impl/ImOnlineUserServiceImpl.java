package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.service.ImOnlineUserService;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.vo.online.OnlineUserVO;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线用户服务实现
 *
 * @author ruoyi
 */
@Service
public class ImOnlineUserServiceImpl implements ImOnlineUserService {

    private static final Logger log = LoggerFactory.getLogger(ImOnlineUserServiceImpl.class);

    private final ImUserService imUserService;
    private final ImRedisUtil imRedisUtil;

    public ImOnlineUserServiceImpl(ImUserService imUserService, ImRedisUtil imRedisUtil) {
        this.imUserService = imUserService;
        this.imRedisUtil = imRedisUtil;
    }

    @Override
    public List<OnlineUserVO> getOnlineUsers() {
        List<OnlineUserVO> result = new ArrayList<>();

        // 从内存中的在线用户获取（支持多设备）
        Map<Long, List<Session>> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();

        for (Map.Entry<Long, List<Session>> entry : onlineUsers.entrySet()) {
            Long userId = entry.getKey();
            List<Session> sessions = entry.getValue();

            if (sessions == null || sessions.isEmpty()) {
                continue;
            }

            // 只显示用户的第一个会话
            Session session = sessions.get(0);
            if (session == null || !session.isOpen()) {
                continue;
            }

            try {
                // 获取用户信息
                ImUser user = imUserService.getUserEntityById(userId);
                if (user == null) {
                    continue;
                }

                OnlineUserVO vo = new OnlineUserVO();
                vo.setUserId(userId);
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
                vo.setSessionId(session.getId());
                vo.setLoginTime(LocalDateTime.now()); // 简化处理，实际应记录登录时间
                vo.setLastActiveTime(LocalDateTime.now());

                // 尝试从 Redis 获取更多会话信息
                if (imRedisUtil != null) {
                    try {
                        Map<String, Object> sessionInfo = imRedisUtil.getSessionInfo(userId);
                        if (sessionInfo != null && !sessionInfo.isEmpty()) {
                            // 可以解析 sessionInfo 获取更多信息
                            vo.setClientInfo(sessionInfo.toString());
                        }
                    } catch (Exception e) {
                        // 忽略 Redis 获取失败
                    }
                }

                result.add(vo);
            } catch (Exception e) {
                log.error("获取在线用户信息失败: userId={}", userId, e);
            }
        }

        // 按 userId 排序
        result.sort(Comparator.comparing(OnlineUserVO::getUserId));
        return result;
    }

    @Override
    public Integer getOnlineUserCount() {
        return ImWebSocketEndpoint.getOnlineUserCount();
    }

    @Override
    public boolean kickUser(Long userId) {
        try {
            Map<Long, List<Session>> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            List<Session> sessions = onlineUsers.get(userId);

            if (sessions != null && !sessions.isEmpty()) {
                // 关闭所有会话
                for (Session session : sessions) {
                    if (session != null && session.isOpen()) {
                        session.close();
                    }
                }

                // 从 Redis 移除在线状态
                if (imRedisUtil != null) {
                    imRedisUtil.removeOnlineUser(userId);
                    imRedisUtil.removeSessionInfo(userId);
                }

                log.info("踢出用户成功: userId={}", userId);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("踢出用户失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public boolean kickBySessionId(String sessionId) {
        try {
            Map<Long, List<Session>> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();

            // 查找对应会话的用户
            for (Map.Entry<Long, List<Session>> entry : onlineUsers.entrySet()) {
                List<Session> sessions = entry.getValue();
                for (Session session : sessions) {
                    if (session != null && session.getId().equals(sessionId)) {
                        Long userId = entry.getKey();
                        return kickUser(userId);
                    }
                }
            }

            return false;
        } catch (Exception e) {
            log.error("踢出会话失败: sessionId={}", sessionId, e);
            return false;
        }
    }
}
