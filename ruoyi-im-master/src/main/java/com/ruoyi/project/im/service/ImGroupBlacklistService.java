package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImGroupBlacklist;

import java.util.List;
import java.util.Map;

/**
 * 群组黑名单Service接口（Admin模块专用）
 *
 * @author ruoyi
 * @date 2025-01-18
 */
public interface ImGroupBlacklistService {

    /**
     * 查询群组黑名单列表
     *
     * @param imGroupBlacklist 群组黑名单
     * @return 群组黑名单集合
     */
    List<ImGroupBlacklist> selectImGroupBlacklistList(ImGroupBlacklist imGroupBlacklist);

    /**
     * 根据ID查询群组黑名单
     *
     * @param id 主键ID
     * @return 群组黑名单
     */
    ImGroupBlacklist selectImGroupBlacklistById(Long id);

    /**
     * 根据群组ID查询黑名单列表
     *
     * @param groupId 群组ID
     * @return 黑名单集合
     */
    List<ImGroupBlacklist> selectImGroupBlacklistByGroupId(Long groupId);

    /**
     * 新增群组黑名单
     *
     * @param imGroupBlacklist 群组黑名单
     * @return 影响行数
     */
    int insertImGroupBlacklist(ImGroupBlacklist imGroupBlacklist);

    /**
     * 修改群组黑名单
     *
     * @param imGroupBlacklist 群组黑名单
     * @return 影响行数
     */
    int updateImGroupBlacklist(ImGroupBlacklist imGroupBlacklist);

    /**
     * 批量删除群组黑名单
     *
     * @param ids 主键ID数组
     * @return 影响行数
     */
    int deleteImGroupBlacklistByIds(Long[] ids);

    /**
     * 删除群组黑名单信息
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteImGroupBlacklistById(Long id);

    /**
     * 解除禁言/拉黑
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int releaseImGroupBlacklist(Long id);

    /**
     * 检查用户是否在群组黑名单中
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @param type    类型 MUTE/BLACKLIST
     * @return 是否在黑名单中
     */
    boolean checkUserInBlacklist(Long groupId, Long userId, String type);

    /**
     * 获取黑名单统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getBlacklistStatistics();
}
