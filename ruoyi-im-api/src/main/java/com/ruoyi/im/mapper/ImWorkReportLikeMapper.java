package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImWorkReportLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作日志点赞Mapper
 */
public interface ImWorkReportLikeMapper extends BaseMapper<ImWorkReportLike> {

    /**
     * 查询日志的点赞列表（带用户信息）
     */
    @Select("SELECT l.*, u.nickname as userName, u.avatar as userAvatar " +
            "FROM im_work_report_like l " +
            "LEFT JOIN im_user u ON l.user_id = u.id " +
            "WHERE l.report_id = #{reportId} " +
            "ORDER BY l.create_time DESC")
    List<ImWorkReportLike> selectByReportId(@Param("reportId") Long reportId);

    /**
     * 统计日志的点赞数
     */
    @Select("SELECT COUNT(*) FROM im_work_report_like WHERE report_id = #{reportId}")
    Integer countByReportId(@Param("reportId") Long reportId);

    /**
     * 检查用户是否已点赞
     */
    @Select("SELECT * FROM im_work_report_like WHERE report_id = #{reportId} AND user_id = #{userId}")
    ImWorkReportLike selectByReportAndUser(@Param("reportId") Long reportId, @Param("userId") Long userId);
}
