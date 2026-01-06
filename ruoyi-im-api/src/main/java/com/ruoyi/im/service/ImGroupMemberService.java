package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImGroupMember;

import java.util.List;

/**
 * Group member service interface
 */
public interface ImGroupMemberService {

    /**
     * Query group member by ID
     */
    ImGroupMember selectImGroupMemberById(Long id);

    /**
     * Query group member list
     */
    List<ImGroupMember> selectImGroupMemberList(ImGroupMember imGroupMember);

    /**
     * Insert group member
     */
    int insertImGroupMember(ImGroupMember imGroupMember);

    /**
     * Update group member
     */
    int updateImGroupMember(ImGroupMember imGroupMember);

    /**
     * Delete group member by ID
     */
    int deleteImGroupMemberById(Long id);

    /**
     * Query group member list by group ID
     */
    List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId);

    /**
     * Query group member by group ID and user ID
     */
    ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId);
}
