package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImMessageReadReceiptMapper;
import com.ruoyi.im.domain.ImMessageReadReceipt;
import com.ruoyi.im.service.ImMessageReadReceiptService;

/**
 * 濞戝牊浼呭鑼额嚢閸ョ偞澧絊ervice娑撴艾濮熺仦鍌氼槱閻?
 * 
 * @author ruoyi
 */
@Service
public class ImMessageReadReceiptServiceImpl implements ImMessageReadReceiptService {
    @Autowired
    private ImMessageReadReceiptMapper imMessageReadReceiptMapper;

    /**
     * 閺屻儴顕楀☉鍫熶紖瀹歌尪顕伴崶鐐村⒔
     * 
     * @param id 濞戝牊浼呭鑼额嚢閸ョ偞澧絀D
     * @return 濞戝牊浼呭鑼额嚢閸ョ偞澧?
     */
    @Override
    public ImMessageReadReceipt selectImMessageReadReceiptById(Long id) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptById(id);
    }

    /**
     * 閺屻儴顕楀☉鍫熶紖瀹歌尪顕伴崶鐐村⒔閸掓銆?
     * 
     * @param imMessageReadReceipt 濞戝牊浼呭鑼额嚢閸ョ偞澧?
     * @return 濞戝牊浼呭鑼额嚢閸ョ偞澧?
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptList(ImMessageReadReceipt imMessageReadReceipt) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptList(imMessageReadReceipt);
    }

    /**
     * 閺傛澘顤冨☉鍫熶紖瀹歌尪顕伴崶鐐村⒔
     * 
     * @param imMessageReadReceipt 濞戝牊浼呭鑼额嚢閸ョ偞澧?
     * @return 缂佹挻鐏?
     */
    @Override
    public int insertImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt) {
        return imMessageReadReceiptMapper.insertImMessageReadReceipt(imMessageReadReceipt);
    }

    /**
     * 娣囶喗鏁煎☉鍫熶紖瀹歌尪顕伴崶鐐村⒔
     * 
     * @param imMessageReadReceipt 濞戝牊浼呭鑼额嚢閸ョ偞澧?
     * @return 缂佹挻鐏?
     */
    @Override
    public int updateImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt) {
        return imMessageReadReceiptMapper.updateImMessageReadReceipt(imMessageReadReceipt);
    }

    /**
     * 閹靛綊鍣洪崚鐘绘珟濞戝牊浼呭鑼额嚢閸ョ偞澧?
     * 
     * @param ids 闂団偓鐟曚礁鍨归梽銈囨畱濞戝牊浼呭鑼额嚢閸ョ偞澧絀D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImMessageReadReceiptByIds(Long[] ids) {
        return imMessageReadReceiptMapper.deleteImMessageReadReceiptByIds(ids);
    }

    /**
     * 閸掔娀娅庡☉鍫熶紖瀹歌尪顕伴崶鐐村⒔娣団剝浼?
     * 
     * @param id 濞戝牊浼呭鑼额嚢閸ョ偞澧絀D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImMessageReadReceiptById(Long id) {
        return imMessageReadReceiptMapper.deleteImMessageReadReceiptById(id);
    }
    
    /**
     * 閺嶈宓佸☉鍫熶紖ID閺屻儴顕楀鑼额嚢閸ョ偞澧介崚妤勩€?
     * 
     * @param messageId 濞戝牊浼匢D
     * @return 濞戝牊浼呭鑼额嚢閸ョ偞澧介梿鍡楁値
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByMessageId(Long messageId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptByMessageId(messageId);
    }
    
    /**
     * 閺嶈宓佸☉鍫熶紖ID閺屻儴顕楀鑼额嚢閸ョ偞澧介崚妤勩€冮敍鍫濈敨閸掑棝銆夐敍?
     * 
     * @param messageId 濞戝牊浼匢D
     * @return 濞戝牊浼呭鑼额嚢閸ョ偞澧介梿鍡楁値
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByMessageId(Long messageId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptListByMessageId(messageId);
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕ID閺屻儴顕楀鑼额嚢閸ョ偞澧介崚妤勩€?
     * 
     * @param userId 閻劍鍩汭D
     * @return 濞戝牊浼呭鑼额嚢閸ョ偞澧介梿鍡楁値
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByUserId(Long userId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptByUserId(userId);
    }
    
    /**
     * 閺嶈宓佹导姘崇樈ID閺屻儴顕楀鑼额嚢閸ョ偞澧介崚妤勩€?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @return 濞戝牊浼呭鑼额嚢閸ョ偞澧介梿鍡楁値
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByConversationId(Long conversationId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptByConversationId(conversationId);
    }
    
    /**
     * 閺嶈宓佹导姘崇樈ID閺屻儴顕楀鑼额嚢閸ョ偞澧介崚妤勩€冮敍鍫濈敨閸掑棝銆夐敍?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @return 濞戝牊浼呭鑼额嚢閸ョ偞澧介梿鍡楁値
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByConversationId(Long conversationId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptListByConversationId(conversationId);
    }
    
    /**
     * 閺嶅洩顔囧☉鍫熶紖瀹歌尪顕?
     * 
     * @param messageId 濞戝牊浼匢D
     * @param userId 閻劍鍩汭D
     * @param conversationId 娴兼俺鐦絀D
     * @param deviceType 鐠佹儳顦猾璇茬€?
     * @return 缂佹挻鐏?
     */
    @Override
    public int markMessageAsRead(Long messageId, Long userId, Long conversationId, String deviceType) {
        ImMessageReadReceipt receipt = new ImMessageReadReceipt();
        receipt.setMessageId(messageId);
        receipt.setUserId(userId);
        receipt.setConversationId(conversationId);
        receipt.setDeviceType(deviceType);
        return insertImMessageReadReceipt(receipt);
    }
    
    /**
     * 閹靛綊鍣洪弽鍥唶濞戝牊浼呭鑼额嚢
     * 
     * @param messageIds 濞戝牊浼匢D閸掓銆?
     * @param userId 閻劍鍩汭D
     * @param conversationId 娴兼俺鐦絀D
     * @param deviceType 鐠佹儳顦猾璇茬€?
     * @return 缂佹挻鐏?
     */
    @Override
    public int batchMarkMessagesAsRead(List<Long> messageIds, Long userId, Long conversationId, String deviceType) {
        int count = 0;
        for (Long messageId : messageIds) {
            count += markMessageAsRead(messageId, userId, conversationId, deviceType);
        }
        return count;
    }
    
    /**
     * 閸掔娀娅庡☉鍫熶紖閻ㄥ嫭澧嶉張澶婂嚒鐠囪娲栭幍?
     * 
     * @param messageId 濞戝牊浼匢D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImMessageReadReceiptByMessageId(Long messageId) {
        return imMessageReadReceiptMapper.deleteImMessageReadReceiptByMessageId(messageId);
    }
}
