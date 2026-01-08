package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImWorkReport;
import com.ruoyi.im.dto.workreport.WorkReportQueryRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作日志Mapper
 */
public interface ImWorkReportMapper extends BaseMapper<ImWorkReport> {

    /**
     * 分页查询工作日志列表（带用户信息）
     */
    IPage<ImWorkReport> selectReportPage(Page<?> page, @Param("req") WorkReportQueryRequest request);

    /**
     * 查询用户提交的日志列表
     */
    @Select("SELECT * FROM im_work_report WHERE user_id = #{userId} ORDER BY report_date DESC, submit_time DESC")
    List<ImWorkReport> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据日期查询用户日志
     */
    @Select("SELECT * FROM im_work_report WHERE user_id = #{userId} AND report_date = #{reportDate}")
    ImWorkReport selectByUserAndDate(@Param("userId") Long userId, @Param("reportDate") String reportDate);

    /**
     * 统计用户提交的日志数量
     */
    @Select("SELECT COUNT(*) FROM im_work_report WHERE user_id = #{userId}")
    Integer countByUserId(@Param("userId") Long userId);
}
