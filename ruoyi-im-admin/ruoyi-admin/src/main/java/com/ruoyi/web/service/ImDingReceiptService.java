package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImDingReceipt;

import java.util.List;
import java.util.Map;

/**
 * DING回执Service接口
 *
 * @author ruoyi
 * @date 2025-01-18
 */
public interface ImDingReceiptService {

    /**
     * 查询DING回执列表
     *
     * @param imDingReceipt DING回执
     * @return DING回执集合
     */
    List<ImDingReceipt> selectImDingReceiptList(ImDingReceipt imDingReceipt);

    /**
     * 根据ID查询DING回执
     *
     * @param id DING回执ID
     * @return DING回执
     */
    ImDingReceipt selectImDingReceiptById(Long id);

    /**
     * 根据DING ID查询回执列表
     *
     * @param dingId DING ID
     * @return DING回执集合
     */
    List<ImDingReceipt> selectReceiptsByDingId(Long dingId);

    /**
     * 获取DING回执统计数据
     *
     * @param dingId DING ID（可选，为空则统计全部）
     * @return 统计数据（已读数、未读数、已确认数、未确认数）
     */
    Map<String, Object> getReceiptStatistics(Long dingId);
}
