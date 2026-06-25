package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDingReceipt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DING回执Mapper
 */
public interface ImDingReceiptMapper extends BaseMapper<ImDingReceipt> {

    /**
     * 批量插入回执记录
     *
     * @param receipts 回执列表
     * @return 插入行数
     */
    int batchInsert(@Param("list") List<ImDingReceipt> receipts);
}
