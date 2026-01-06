package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImGroupService;

/**
 * 缂囥倗绮峉ervice娑撴艾濮熺仦鍌氼槱閻?- 娴兼ê瀵查悧鍫熸拱
 * 娴兼ê瀵查崘鍛啇閿涙碍鍧婇崝鐘电处鐎涙ɑ婧€閸掕翰鈧椒绨ㄩ崝鈩冨付閸掕翰鈧焦鈧嗗厴閻╂垶甯堕妴渚€鏁婄拠顖氼槱閻? * 
 * @author ruoyi
 */
@Service
public class ImGroupServiceImpl extends EnhancedBaseServiceImpl<ImGroup, ImGroupMapper> implements ImGroupService {
    @Autowired
    private ImGroupMapper imGroupMapper;

    /**
     * 閺嶈宓侀悽銊﹀煕ID閺屻儴顕楃紘銈囩矋閸掓銆?
     * 
     * @param userId 閻劍鍩汭D
     * @return 缂囥倗绮嶉梿鍡楁値
     */
    @Override
    public List<ImGroup> selectImGroupListByUserId(Long userId) {
        // TODO: 鐎圭偟骞囬弽瑙勫祦閻劍鍩汭D閺屻儴顕楃紘銈囩矋閸掓銆冮惃鍕偓鏄忕帆
        // 鏉╂瑩鍣烽棁鈧憰浣稿彠閼辨梹鐓＄拠銏㈠參缂佸嫭鍨氶崨妯裤€冮弶銉ㄥ箯閸欐牜鏁ら幋閿嬪閸︺劎娈戠紘銈囩矋
        return imGroupMapper.selectImGroupList(new ImGroup());
    }
    
    /**
     * 閺嶈宓佺紘銈囩矋ID閺屻儴顕楃紘銈囩矋娣団剝浼?
     * 
     * @param id 缂囥倗绮岻D
     * @return 缂囥倗绮嶆穱鈩冧紖
     */
    @Override
    public ImGroup selectImGroupById(Long id) {
        return selectById(id);
    }
    
    /**
     * 閺囧瓨鏌婄紘銈囩矋娣団剝浼?
     * 
     * @param imGroup 缂囥倗绮嶆穱鈩冧紖
     * @return 缂佹挻鐏?
     */
    @Override
    public int updateImGroup(ImGroup imGroup) {
        return update(imGroup);
    }
    
    /**
     * 閺嶈宓両D閸掔娀娅庣紘銈囩矋娣団剝浼?
     * 
     * @param id 缂囥倗绮岻D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImGroupById(Long id) {
        return deleteById(id);
    }
}
