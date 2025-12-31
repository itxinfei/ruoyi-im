package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImConversationMember;
import java.util.List;

/**
 * 会话成员Mapper接口
 * 
 * @author ruoyi
 */
public interface ImConversationMemberMapper {
    /**
     * 查询会话成员
     * 
     * @param id 会话成员ID
     * @return 会话成员
     */
    public ImConversationMember selectImConversationMemberById(Long id);

    /**
     * 查询会话成员列表
     * 
     * @param imConversationMember 会话成员
     * @return 会话成员集合
     */
    public List<ImConversationMember> selectImConversationMemberList(ImConversationMember imConversationMember);

    /**
     * 新增会话成员
     * 
     * @param imConversationMember 会话成员
     * @return 结果
     */
    public int insertImConversationMember(ImConversationMember imConversationMember);

    /**
     * 修改会话成员
     * 
     * @param imConversationMember 会话成员
     * @return 结果
     */
    public int updateImConversationMember(ImConversationMember imConversationMember);

    /**
     * 删除会话成员
     * 
     * @param id 会话成员ID
     * @return 结果
     */
    public int deleteImConversationMemberById(Long id);

    /**
     * 批量删除会话成员
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteImConversationMemberByIds(Long[] ids);
    
    /**
     * 根据会话ID查询会话成员列表
     * 
     * @param conversationId 会话ID
     * @return 会话成员集合
     */
    public List<ImConversationMember> selectImConversationMemberListByConversationId(Long conversationId);
    
    /**
     * 根据会话ID和用户ID查询会话成员
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话成员
     */
    public ImConversationMember selectImConversationMemberByConversationIdAndUserId(Long conversationId, Long userId);
    
    /**
     * 根据用户ID查询会话成员列表
     * 
     * @param userId 用户ID
     * @return 会话成员集合
     */
    public List<ImConversationMember> selectImConversationMemberListByUserId(Long userId);
    
    /**
     * 批量删除会话成员（根据会话ID）
     * 
     * @param conversationIds 会话ID数组
     * @return 结果
     */
    public int deleteImConversationMemberByConversationIds(Long[] conversationIds);
}