package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImMessageReactionMapper;
import com.ruoyi.im.domain.ImMessageReaction;
import com.ruoyi.im.service.ImMessageReactionService;

/**
 * 濞戝牊浼呮禍鎺戝ЗService娑撴艾濮熺仦鍌氼槱閻?
 * 
 * @author ruoyi
 */
@Service
public class ImMessageReactionServiceImpl implements ImMessageReactionService {
    @Autowired
    private ImMessageReactionMapper imMessageReactionMapper;

    /**
     * 閺屻儴顕楀☉鍫熶紖娴滄帒濮?
     * 
     * @param id 濞戝牊浼呮禍鎺戝ЗID
     * @return 濞戝牊浼呮禍鎺戝З
     */
    @Override
    public ImMessageReaction selectImMessageReactionById(Long id) {
        return imMessageReactionMapper.selectImMessageReactionById(id);
    }

    /**
     * 閺屻儴顕楀☉鍫熶紖娴滄帒濮╅崚妤勩€?
     * 
     * @param imMessageReaction 濞戝牊浼呮禍鎺戝З
     * @return 濞戝牊浼呮禍鎺戝З
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionList(ImMessageReaction imMessageReaction) {
        return imMessageReactionMapper.selectImMessageReactionList(imMessageReaction);
    }

    /**
     * 閺傛澘顤冨☉鍫熶紖娴滄帒濮?
     * 
     * @param imMessageReaction 濞戝牊浼呮禍鎺戝З
     * @return 缂佹挻鐏?
     */
    @Override
    public int insertImMessageReaction(ImMessageReaction imMessageReaction) {
        return imMessageReactionMapper.insertImMessageReaction(imMessageReaction);
    }

    /**
     * 娣囶喗鏁煎☉鍫熶紖娴滄帒濮?
     * 
     * @param imMessageReaction 濞戝牊浼呮禍鎺戝З
     * @return 缂佹挻鐏?
     */
    @Override
    public int updateImMessageReaction(ImMessageReaction imMessageReaction) {
        return imMessageReactionMapper.updateImMessageReaction(imMessageReaction);
    }

    /**
     * 閹靛綊鍣洪崚鐘绘珟濞戝牊浼呮禍鎺戝З
     * 
     * @param ids 闂団偓鐟曚礁鍨归梽銈囨畱濞戝牊浼呮禍鎺戝ЗID
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImMessageReactionByIds(Long[] ids) {
        return imMessageReactionMapper.deleteImMessageReactionByIds(ids);
    }

    /**
     * 閸掔娀娅庡☉鍫熶紖娴滄帒濮╂穱鈩冧紖
     * 
     * @param id 濞戝牊浼呮禍鎺戝ЗID
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImMessageReactionById(Long id) {
        return imMessageReactionMapper.deleteImMessageReactionById(id);
    }
    
    /**
     * 閺嶈宓佸☉鍫熶紖ID閺屻儴顕楁禍鎺戝З閸掓銆?
     * 
     * @param messageId 濞戝牊浼匢D
     * @return 濞戝牊浼呮禍鎺戝З闂嗗棗鎮?
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionByMessageId(Long messageId) {
        return imMessageReactionMapper.selectImMessageReactionByMessageId(messageId);
    }
    
    /**
     * 閺嶈宓佸☉鍫熶紖ID閺屻儴顕楁禍鎺戝З閸掓銆冮敍鍫濈敨閸掑棝銆夐敍?
     * 
     * @param messageId 濞戝牊浼匢D
     * @return 濞戝牊浼呮禍鎺戝З闂嗗棗鎮?
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionListByMessageId(Long messageId) {
        return imMessageReactionMapper.selectImMessageReactionListByMessageId(messageId);
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕ID閺屻儴顕楁禍鎺戝З閸掓銆?
     * 
     * @param userId 閻劍鍩汭D
     * @return 濞戝牊浼呮禍鎺戝З闂嗗棗鎮?
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionByUserId(Long userId) {
        return imMessageReactionMapper.selectImMessageReactionByUserId(userId);
    }
    
    /**
     * 濞ｈ濮炲☉鍫熶紖娴滄帒濮?
     * 
     * @param messageId 濞戝牊浼匢D
     * @param userId 閻劍鍩汭D
     * @param reactionType 娴滄帒濮╃猾璇茬€?
     * @param emoji 鐞涖劍鍎?
     * @return 缂佹挻鐏?
     */
    @Override
    public int addReaction(Long messageId, Long userId, String reactionType, String emoji) {
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        reaction.setEmoji(emoji);
        return insertImMessageReaction(reaction);
    }
    
    /**
     * 閸欐牗绉峰☉鍫熶紖娴滄帒濮?
     * 
     * @param messageId 濞戝牊浼匢D
     * @param userId 閻劍鍩汭D
     * @param reactionType 娴滄帒濮╃猾璇茬€?
     * @return 缂佹挻鐏?
     */
    @Override
    public int removeReaction(Long messageId, Long userId, String reactionType) {
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        List<ImMessageReaction> reactions = selectImMessageReactionList(reaction);
        if (reactions != null && !reactions.isEmpty()) {
            return deleteImMessageReactionById(reactions.get(0).getId());
        }
        return 0;
    }
    
    /**
     * 閸掔娀娅庡☉鍫熶紖娴滄帒濮?
     * 
     * @param messageId 濞戝牊浼匢D
     * @param userId 閻劍鍩汭D
     * @param reactionType 娴滄帒濮╃猾璇茬€?
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImMessageReaction(Long messageId, Long userId, String reactionType) {
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        List<ImMessageReaction> reactions = selectImMessageReactionList(reaction);
        if (reactions != null && !reactions.isEmpty()) {
            return deleteImMessageReactionById(reactions.get(0).getId());
        }
        return 0;
    }
    
    /**
     * 閸掔娀娅庡☉鍫熶紖閻ㄥ嫭澧嶉張澶夌鞍閸?
     * 
     * @param messageId 濞戝牊浼匢D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImMessageReactionByMessageId(Long messageId) {
        return imMessageReactionMapper.deleteImMessageReactionByMessageId(messageId);
    }
}
