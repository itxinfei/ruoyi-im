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
 * 缇ょ粍Service涓氬姟灞傚鐞?- 浼樺寲鐗堟湰
 * 浼樺寲鍐呭锛氭坊鍔犵紦瀛樻満鍒躲€佷簨鍔℃帶鍒躲€佹€ц兘鐩戞帶銆侀敊璇鐞? * 
 * @author ruoyi
 */
@Service
public class ImGroupServiceImpl extends EnhancedBaseServiceImpl<ImGroup, ImGroupMapper> implements ImGroupService {
    @Autowired
    private ImGroupMapper imGroupMapper;

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ缇ょ粍鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 缇ょ粍闆嗗悎
     */
    @Override
    public List<ImGroup> selectImGroupListByUserId(Long userId) {
        // TODO: 瀹炵幇鏍规嵁鐢ㄦ埛ID鏌ヨ缇ょ粍鍒楄〃鐨勯€昏緫
        // 杩欓噷闇€瑕佸叧鑱旀煡璇㈢兢缁勬垚鍛樿〃鏉ヨ幏鍙栫敤鎴锋墍鍦ㄧ殑缇ょ粍
        return imGroupMapper.selectImGroupList(new ImGroup());
    }
    
    /**
     * 鏍规嵁缇ょ粍ID鏌ヨ缇ょ粍淇℃伅
     * 
     * @param id 缇ょ粍ID
     * @return 缇ょ粍淇℃伅
     */
    @Override
    public ImGroup selectImGroupById(Long id) {
        return selectById(id);
    }
    
    /**
     * 鏇存柊缇ょ粍淇℃伅
     * 
     * @param imGroup 缇ょ粍淇℃伅
     * @return 缁撴灉
     */
    @Override
    public int updateImGroup(ImGroup imGroup) {
        return update(imGroup);
    }
    
    /**
     * 鏍规嵁ID鍒犻櫎缇ょ粍淇℃伅
     * 
     * @param id 缇ょ粍ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImGroupById(Long id) {
        return deleteById(id);
    }
}
