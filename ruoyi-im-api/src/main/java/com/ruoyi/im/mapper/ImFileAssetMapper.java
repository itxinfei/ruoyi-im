package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImFileAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 文件资产Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImFileAssetMapper extends BaseMapper<ImFileAsset> {

    /**
     * 统计文件总数
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 文件总数
     */
    Long countTotalFiles(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计今日上传数
     *
     * @param date 日期
     * @return 上传数
     */
    Long countTodayUploads(@Param("date") LocalDate date);

    /**
     * 统计总存储大小
     *
     * @return 存储大小（字节）
     */
    Long countTotalStorage();

    /**
     * 统计下载总数
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 下载总数
     */
    Long countTotalDownloads(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
