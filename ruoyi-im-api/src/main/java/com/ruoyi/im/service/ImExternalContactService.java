package com.ruoyi.im.service;

import com.ruoyi.im.dto.contact.ExternalContactCreateRequest;
import com.ruoyi.im.domain.ImExternalContact;
import com.ruoyi.im.domain.ImExternalContactGroup;

import java.util.List;

/**
 * 外部联系人服务
 */
public interface ImExternalContactService {

    /**
     * 创建联系人
     *
     * @param request 创建请求
     * @param userId  用户ID
     * @return 联系人ID
     */
    Long createContact(ExternalContactCreateRequest request, Long userId);

    /**
     * 更新联系人
     *
     * @param contactId 联系人ID
     * @param request   更新请求
     * @param userId    用户ID
     */
    void updateContact(Long contactId, ExternalContactCreateRequest request, Long userId);

    /**
     * 删除联系人
     *
     * @param contactId 联系人ID
     * @param userId    用户ID
     */
    void deleteContact(Long contactId, Long userId);

    /**
     * 获取联系人详情
     *
     * @param contactId 联系人ID
     * @param userId    用户ID
     * @return 联系人详情
     */
    ImExternalContact getContactDetail(Long contactId, Long userId);

    /**
     * 获取用户的联系人列表
     *
     * @param userId 用户ID
     * @return 联系人列表
     */
    List<ImExternalContact> getContactList(Long userId);

    /**
     * 获取分组下的联系人列表
     *
     * @param groupId 分组ID
     * @param userId  用户ID
     * @return 联系人列表
     */
    List<ImExternalContact> getContactsByGroup(Long groupId, Long userId);

    /**
     * 获取星标联系人列表
     *
     * @param userId 用户ID
     * @return 星标联系人列表
     */
    List<ImExternalContact> getStarredContacts(Long userId);

    /**
     * 搜索联系人
     *
     * @param keyword 关键词
     * @param userId  用户ID
     * @return 搜索结果
     */
    List<ImExternalContact> searchContacts(String keyword, Long userId);

    /**
     * 切换星标状态
     *
     * @param contactId 联系人ID
     * @param userId    用户ID
     */
    void toggleStarred(Long contactId, Long userId);

    // ==================== 分组管理 ====================

    /**
     * 创建分组
     *
     * @param name   分组名称
     * @param userId 用户ID
     * @return 分组ID
     */
    Long createGroup(String name, Long userId);

    /**
     * 更新分组
     *
     * @param groupId 分组ID
     * @param name    新名称
     * @param userId  用户ID
     */
    void updateGroup(Long groupId, String name, Long userId);

    /**
     * 删除分组
     *
     * @param groupId 分组ID
     * @param userId  用户ID
     */
    void deleteGroup(Long groupId, Long userId);

    /**
     * 获取用户的分组列表
     *
     * @param userId 用户ID
     * @return 分组列表
     */
    List<ImExternalContactGroup> getGroupList(Long userId);
}
