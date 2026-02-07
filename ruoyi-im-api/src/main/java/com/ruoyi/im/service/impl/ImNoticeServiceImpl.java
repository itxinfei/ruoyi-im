package com.ruoyi.im.service.impl;

import com.ruoyi.im.service.ImNoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现
 *
 * @author ruoyi
 */
@Service
public class ImNoticeServiceImpl implements ImNoticeService {

    private static final Logger log = LoggerFactory.getLogger(ImNoticeServiceImpl.class);

    @Override
    public int getUnreadCount(Long userId) {
        log.debug("查询未读通知数: userId={}", userId);
        // TODO: 实现未读通知数查询逻辑
        return 0;
    }
}
