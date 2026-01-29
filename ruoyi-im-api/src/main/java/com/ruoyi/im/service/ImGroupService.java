package com.ruoyi.im.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.dto.group.ImGroupCreateRequest;
import com.ruoyi.im.dto.group.ImGroupQueryRequest;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;

import java.util.List;

/**
 * 群组服务接口
 *
 * @author ruoyi
 */
public interface ImGroupService {

    /**
     * 创建群组
     *
     * @param request 创建请求
     * @param userId 当前用户ID
     * @return 群组ID
     */
    Long createGroup(ImGroupCreateRequest request, Long userId);

    /**
     * 更新群组信息
     *
     * @param groupId 群组ID
     * @param request 更新请求
     * @param userId 当前用户ID
     */
    void updateGroup(Long groupId, ImGroupUpdateRequest request, Long userId);

    /**
     * 解散群组
     *
     * @param groupId 群组ID
     * @param userId 当前用户ID
     */
    void dismissGroup(Long groupId, Long userId);

    /**
     * 获取群组详情
     *
     * @param groupId 群组ID
     * @param userId 当前用户ID
     * @return 群组信息
     */
    ImGroupVO getGroupById(Long groupId, Long userId);

    /**
     * 获取用户的群组列表
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<ImGroupVO> getUserGroups(Long userId);

    /**
     * 获取群组成员列表
     *
     * @param groupId 群组ID
     * @param userId 当前用户ID
     * @return 成员列表
     */
    List<ImGroupMemberVO> getGroupMembers(Long groupId, Long userId);

    /**
     * 添加群组成员
     *
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param operatorId 操作者ID
     */
    void addMembers(Long groupId, List<Long> userIds, Long operatorId);

    /**
     * 移除群组成员
     *
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param operatorId 操作者ID
     */
    void removeMembers(Long groupId, List<Long> userIds, Long operatorId);

    /**
     * 退出群组
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     */
    void quitGroup(Long groupId, Long userId);

    /**
     * 设置管理员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param isAdmin 是否为管理员
     * @param operatorId 操作者ID
     */
    void setAdmin(Long groupId, Long userId, Boolean isAdmin, Long operatorId);

    /**
     * 禁言/取消禁言成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param duration 禁言时长（分钟）
     * @param operatorId 操作者ID
     */
    void muteMember(Long groupId, Long userId, Long duration, Long operatorId);

    /**
     * 转让群主
     *
     * @param groupId 群组ID
     * @param newOwnerId 新群主用户ID
     * @param operatorId 操作者ID
     */
    void transferOwner(Long groupId, Long newOwnerId, Long operatorId);

    /**
     * 分页查询群组列表（管理员用）
     *
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 群组分页列表
     */
    IPage<ImGroup> getGroupPage(Page<ImGroup> page, String keyword);

    /**
     * 管理员获取群组详情（无权限检查）
     *
     * @param groupId 群组ID
     * @return 群组信息
     */
    ImGroupVO adminGetGroupById(Long groupId);

    /**
     * 管理员解散群组（无权限检查）
     *
     * @param groupId 群组ID
     */
    void adminDismissGroup(Long groupId);

    /**
     * 管理员批量解散群组
     *
     * @param groupIds 群组ID列表
     * @return 解散结果Map，包含successCount和failCount
     */
    java.util.Map<String, Integer> adminBatchDismissGroups(List<Long> groupIds);

    /**
     * 管理员更新群组信息（无权限检查）
     *
     * @param groupId 群组ID
     * @param request 更新请求
     */
    void adminUpdateGroup(Long groupId, ImGroupUpdateRequest request);

    /**
     * 管理员获取群组统计
     *
     * @return 统计数据Map
     */
    java.util.Map<String, Object> getGroupStats();

    /**
     * 管理员获取群组成员列表（无权限检查）
     *
     * @param groupId 群组ID
     * @return 成员列表Map
     */
    List<java.util.Map<String, Object>> adminGetGroupMembers(Long groupId);

    /**
     * 管理员移除群组成员（无权限检查）
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     */
    void adminRemoveMember(Long groupId, Long userId);

    /**
     * 获取两个用户共同加入的群组列表
     * 用于用户详情页展示共同群组
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 共同群组列表
     */
    List<ImGroupVO> getCommonGroups(Long userId1, Long userId2);
}
