package com.ruoyi.im.service;

import com.ruoyi.im.dto.session.ImSessionUpdateRequest;
import com.ruoyi.im.vo.session.ImSessionVO;

import java.util.List;

/**
 * 会话服务接口
 *
 * @author ruoyi
 */
public interface ImSessionService {

    /**
     * 获取用户会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ImSessionVO> getSessionList(Long userId);

    /**
     * 根据ID获取会话信息
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    ImSessionVO getSessionById(Long sessionId);

    /**
     * 更新会话信息
     *
     * @param sessionId 会话ID
     * @param request 更新请求
     */
    void updateSession(Long sessionId, ImSessionUpdateRequest request);

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void deleteSession(Long sessionId, Long userId);

    /**
     * 清空未读消息数
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void clearUnread(Long sessionId, Long userId);

    /**
     * 置顶/取消置顶会话
     *
     * @param sessionId 会话ID
     * @param pinned 是否置顶
     */
    void togglePin(Long sessionId, Integer pinned);

    /**
     * 免打扰/取消免打扰会话
     *
     * @param sessionId 会话ID
     * @param muted 是否免打扰
     */
    void toggleMute(Long sessionId, Integer muted);
}
