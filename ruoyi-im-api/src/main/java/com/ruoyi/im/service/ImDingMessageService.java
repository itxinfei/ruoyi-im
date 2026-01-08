package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImDingMessage;
import com.ruoyi.im.domain.ImDingReceipt;
import com.ruoyi.im.domain.ImDingTemplate;
import com.ruoyi.im.dto.ding.DingSendRequest;
import com.ruoyi.im.vo.ding.DingDetailVO;
import com.ruoyi.im.vo.ding.DingReceiptVO;

import java.util.List;

/**
 * DING消息服务
 */
public interface ImDingMessageService {

    /**
     * 发送DING消息
     */
    Long sendDing(DingSendRequest request, Long userId);

    /**
     * 获取用户接收的DING列表
     */
    List<ImDingMessage> getReceivedDingList(Long userId);

    /**
     * 获取用户发送的DING列表
     */
    List<ImDingMessage> getSentDingList(Long userId);

    /**
     * 获取DING详情（含回执）
     */
    DingDetailVO getDingDetail(Long dingId, Long userId);

    /**
     * 阅读DING消息
     */
    void readDing(Long dingId, Long userId);

    /**
     * 确认DING消息
     */
    void confirmDing(Long dingId, Long userId, String remark);

    /**
     * 获取DING回执列表
     */
    List<DingReceiptVO> getDingReceipts(Long dingId, Long userId);

    /**
     * 取消定时DING
     */
    void cancelScheduledDing(Long dingId, Long userId);

    /**
     * 获取DING模板列表
     */
    List<ImDingTemplate> getTemplateList(String category);

    /**
     * 使用模板创建DING
     */
    Long createFromTemplate(Long templateId, String params, Long userId);
}
