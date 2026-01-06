package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.service.ImConversationMemberService;

/**
 * 娴兼俺鐦介幋鎰喅Service娑撴艾濮熺仦鍌氼槱閻? * 
 * @author ruoyi
 */
@Service
public class ImConversationMemberServiceImpl extends BaseServiceImpl<ImConversationMember, ImConversationMemberMapper> implements ImConversationMemberService {
    @Autowired
    private ImConversationMemberMapper imConversationMemberMapper;

    public ImConversationMember selectImConversationMemberById(Long id) {
        return selectById(id);
    }

    public List<ImConversationMember> selectImConversationMemberList(ImConversationMember imConversationMember) {
        return selectList(imConversationMember);
    }

    public int insertImConversationMember(ImConversationMember imConversationMember) {
        return insert(imConversationMember);
    }

    public int updateImConversationMember(ImConversationMember imConversationMember) {
        return update(imConversationMember);
    }

    public int deleteImConversationMemberByIds(Long[] ids) {
        return deleteByIds(ids);
    }

    public int deleteImConversationMemberById(Long id) {
        return deleteById(id);
    }


    
    /**
     * 閺嶈宓佹导姘崇樈ID閺屻儴顕楁导姘崇樈閹存劕鎲抽崚妤勩€?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @return 娴兼俺鐦介幋鎰喅闂嗗棗鎮?
     */
    @Override
    public List<ImConversationMember> selectImConversationMemberListByConversationId(Long conversationId) {
        return imConversationMemberMapper.selectImConversationMemberListByConversationId(conversationId);
    }
    
    /**
     * 閺嶈宓佹导姘崇樈ID閸滃瞼鏁ら幋绋〥閺屻儴顕楁导姘崇樈閹存劕鎲?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param userId 閻劍鍩汭D
     * @return 娴兼俺鐦介幋鎰喅
     */
    @Override
    public ImConversationMember selectImConversationMemberByConversationIdAndUserId(Long conversationId, Long userId) {
        return imConversationMemberMapper.selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕ID閺屻儴顕楁导姘崇樈閹存劕鎲抽崚妤勩€?
     * 
     * @param userId 閻劍鍩汭D
     * @return 娴兼俺鐦介幋鎰喅闂嗗棗鎮?
     */
    @Override
    public List<ImConversationMember> selectImConversationMemberListByUserId(Long userId) {
        return imConversationMemberMapper.selectImConversationMemberListByUserId(userId);
    }
    
    /**
     * 濞ｈ濮炴导姘崇樈閹存劕鎲?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param userIds 閻劍鍩汭D閸掓銆?
     * @return 缂佹挻鐏?
     */
    @Override
    public int addConversationMembers(Long conversationId, List<Long> userIds) {
        int result = 0;
        for (Long userId : userIds) {
            // 濡偓閺屻儳鏁ら幋閿嬫Ц閸氾箑鍑＄紒蹇旀Ц娴兼俺鐦介幋鎰喅
            ImConversationMember existingMember = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
            if (existingMember == null) {
                ImConversationMember member = new ImConversationMember();
                member.setConversationId(conversationId);
                member.setUserId(userId);
                member.setUnreadCount(0);
                
                result += insertImConversationMember(member);
            }
        }
        return result;
    }
    
    /**
     * 缁夊娅庢导姘崇樈閹存劕鎲?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param userId 閻劍鍩汭D
     * @return 缂佹挻鐏?
     */
    @Override
    public int removeConversationMember(Long conversationId, Long userId) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            return deleteImConversationMemberById(member.getId());
        }
        return 0;
    }
    
    /**
     * 閺囧瓨鏌婃导姘崇樈閹存劕鎲抽張顏囶嚢閺?     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param userId 閻劍鍩汭D
     * @param unreadCount 閺堫亣顕伴弫?     * @return 缂佹挻鐏?
     */
    @Override
    public int updateUnreadCount(Long conversationId, Long userId, Integer unreadCount) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setUnreadCount(unreadCount);
            return updateImConversationMember(member);
        }
        return 0;
    }
    
    /**
     * 閺嶅洩顔囨导姘崇樈閹存劕鎲冲☉鍫熶紖瀹歌尪顕?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param userId 閻劍鍩汭D
     * @param messageId 濞戝牊浼匢D
     * @return 缂佹挻鐏?
     */
    @Override
    public int markMessageAsRead(Long conversationId, Long userId, Long messageId) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setLastReadMessageId(messageId);
            // 鐏忓棙婀拠缁樻殶鐠佸墽鐤嗘稉?
            member.setUnreadCount(0);
            return updateImConversationMember(member);
        }
        return 0;
    }
    
    /**
     * 鐠佸墽鐤嗘导姘崇樈閹存劕鎲崇純顕€銆婇悩鑸碘偓?     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param userId 閻劍鍩汭D
     * @param isPinned 閺勵垰鎯佺純顕€銆?
     * @return 缂佹挻鐏?
     */
    @Override
    public int setConversationPinned(Long conversationId, Long userId, Boolean isPinned) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setIsPinned(isPinned);
            return updateImConversationMember(member);
        }
        return 0;
    }
    
    /**
     * 鐠佸墽鐤嗘导姘崇樈閹存劕鎲抽崗宥嗗ⅵ閹垫壆濮搁幀?     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param userId 閻劍鍩汭D
     * @param isMuted 閺勵垰鎯侀崗宥嗗ⅵ閹?     * @return 缂佹挻鐏?
     */
    @Override
    public int setConversationMuted(Long conversationId, Long userId, Boolean isMuted) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setIsMuted(isMuted);
            return updateImConversationMember(member);
        }
        return 0;
    }
}
