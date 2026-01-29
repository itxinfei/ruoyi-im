package com.ruoyi.im.service;

import com.ruoyi.im.dto.quickreply.ImQuickReplyCreateRequest;
import com.ruoyi.im.dto.quickreply.ImQuickReplyUpdateRequest;
import com.ruoyi.im.vo.quickreply.ImQuickReplyVO;

import java.util.List;

/**
 * 快捷回复服务接口
 *
 * @author ruoyi
 */
public interface ImQuickReplyService {

    /**
     * 创建快捷回复
     *
     * @param request 创建请求
     * @param userId 当前用户ID
     * @return 快捷回复VO
     */
    ImQuickReplyVO createReply(ImQuickReplyCreateRequest request, Long userId);

    /**
     * 更新快捷回复
     *
     * @param request 更新请求
     * @param userId 当前用户ID
     * @return 快捷回复VO
     */
    ImQuickReplyVO updateReply(ImQuickReplyUpdateRequest request, Long userId);

    /**
     * 删除快捷回复
     *
     * @param id 快捷回复ID
     * @param userId 当前用户ID
     */
    void deleteReply(Long id, Long userId);

    /**
     * 获取用户的快捷回复列表
     *
     * @param userId 用户ID
     * @return 快捷回复列表
     */
    List<ImQuickReplyVO> getUserReplies(Long userId);

    /**
     * 获取指定分类的快捷回复
     *
     * @param userId 用户ID
     * @param category 分类
     * @return 快捷回复列表
     */
    List<ImQuickReplyVO> getRepliesByCategory(Long userId, String category);

    /**
     * 使用快捷回复（增加使用次数）
     *
     * @param id 快捷回复ID
     */
    void useReply(Long id);

    /**
     * 批量更新排序
     *
     * @param userId 用户ID
     * @param idList 排序后的ID列表
     */
    void updateSortOrder(Long userId, List<Long> idList);
}
