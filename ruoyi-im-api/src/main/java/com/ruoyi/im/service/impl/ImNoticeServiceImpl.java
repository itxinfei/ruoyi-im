package com.ruoyi.im.service.impl;

import com.ruoyi.im.service.ImNoticeService;
import org.springframework.stereotype.Service;

@Service
public class ImNoticeServiceImpl implements ImNoticeService {
    @Override
    public int getUnreadCount(Long userId) {
        return 0;
    }
}
