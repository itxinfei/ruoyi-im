package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImWorkReportComment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作日志评论Mapper
 */
public interface ImWorkReportCommentMapper extends BaseMapper<ImWorkReportComment> {

    /**
     * 查询日志的评论列表（带用户信息）
     */
    @Select("SELECT c.*, u.nickname as userName, u.avatar as userAvatar " +
            "FROM im_work_report_comment c " +
            "LEFT JOIN im_user u ON c.user_id = u.id " +
            "WHERE c.report_id = #{reportId} " +
            "ORDER BY c.create_time ASC")
    List<ImWorkReportComment> selectByReportId(@Param("reportId") Long reportId);

    /**
     * 统计日志的评论数
     */
    @Select("SELECT COUNT(*) FROM im_work_report_comment WHERE report_id = #{reportId}")
    Integer countByReportId(@Param("reportId") Long reportId);
}
