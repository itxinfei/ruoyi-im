package com.ruoyi.im.service;

import com.ruoyi.im.vo.contact.ImContactGroupVO;

import java.util.List;

/**
 * 好友分组与标签服务接口
 */
public interface ImFriendGroupService {

    /**
     * 获取分组好友列表
     */
    List<ImContactGroupVO> getGroupedFriendList(Long userId);

    /**
     * 获取分组名称列表
     */
    List<String> getGroupNames(Long userId);

    /**
     * 重命名分组
     */
    void renameGroup(Long userId, String oldName, String newName);

    /**
     * 删除分组（好友移至默认分组）
     */
    void deleteGroup(Long userId, String groupName);

    /**
     * 批量移动好友到分组
     */
    void moveFriendsToGroup(Long userId, List<Long> friendIds, String groupName);

    /**
     * 获取用户所有标签
     */
    List<String> getUserTags(Long userId);

    /**
     * 更新好友标签
     */
    void updateFriendTags(Long userId, Long friendId, List<String> tags);

    /**
     * 按标签获取好友
     */
    List<Long> getFriendIdsByTag(Long userId, String tag);
}
