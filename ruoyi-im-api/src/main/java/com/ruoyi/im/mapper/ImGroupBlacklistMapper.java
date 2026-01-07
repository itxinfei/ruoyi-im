package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImGroupBlacklist;

import java.util.List;

/**
 * 群组黑名单/禁言Mapper接口
 *
 * @author ruoyi
 */
public interface ImGroupBlacklistMapper {

    /**
     * 查询群组黑名单
     *
     * @param id 黑名单ID
     * @return 群组黑名单
     */
    ImGroupBlacklist selectImGroupBlacklistById(Long id);

    /**
     * 根据群组ID和用户ID查询黑名单记录
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 群组黑名单
     */
    ImGroupBlacklist selectByGroupIdAndUserId(Long groupId, Long userId);

    /**
     * 查询群组黑名单列表
     *
     * @param imGroupBlacklist 群组黑名单
     * @return 群组黑名单集合
     */
    List<ImGroupBlacklist> selectImGroupBlacklistList(ImGroupBlacklist imGroupBlacklist);

    /**
     * 根据群组ID查询所有黑名单记录
     *
     * @param groupId 群组ID
     * @return 群组黑名单集合
     */
    List<ImGroupBlacklist> selectByGroupId(Long groupId);

    /**
     * 根据群组ID查询生效中的黑名单记录
     *
     * @param groupId 群组ID
     * @return 群组黑名单集合
     */
    List<ImGroupBlacklist> selectActiveByGroupId(Long groupId);

    /**
     * 根据用户ID查询被禁言/拉黑的群组列表
     *
     * @param userId 用户ID
     * @return 群组黑名单集合
     */
    List<ImGroupBlacklist> selectByUserId(Long userId);

    /**
     * 查询用户在指定群组中是否被禁言
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 群组黑名单
     */
    ImGroupBlacklist selectMutedUser(Long groupId, Long userId);

    /**
     * 新增群组黑名单
     *
     * @param imGroupBlacklist 群组黑名单
     * @return 结果
     */
    int insertImGroupBlacklist(ImGroupBlacklist imGroupBlacklist);

    /**
     * 修改群组黑名单
     *
     * @param imGroupBlacklist 群组黑名单
     * @return 结果
     */
    int updateImGroupBlacklist(ImGroupBlacklist imGroupBlacklist);

    /**
     * 删除群组黑名单
     *
     * @param id 黑名单ID
     * @return 结果
     */
    int deleteImGroupBlacklistById(Long id);

    /**
     * 根据群组ID删除所有黑名单记录
     *
     * @param groupId 群组ID
     * @return 结果
     */
    int deleteImGroupBlacklistByGroupId(Long groupId);

    /**
     * 解除用户在群组中的禁言/黑名单
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @param type    类型（MUTE/BLACKLIST）
     * @return 结果
     */
    int removeUserFromBlacklist(Long groupId, Long userId, String type);

    /**
     * 清理过期记录
     *
     * @return 清理的记录数
     */
    int cleanupExpiredRecords();
}
