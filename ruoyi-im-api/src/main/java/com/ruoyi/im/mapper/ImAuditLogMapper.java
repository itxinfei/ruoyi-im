package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作审计日志Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImAuditLogMapper {

    /**
     * 新增审计日志
     *
     * @param auditLog 审计日志
     * @return 结果
     */
    int insertImAuditLog(ImAuditLog auditLog);

    /**
     * 查询审计日志列表
     *
     * @param auditLog 审计日志
     * @return 日志集合
     */
    List<ImAuditLog> selectImAuditLogList(ImAuditLog auditLog);

    /**
     * 查询用户操作日志
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<ImAuditLog> selectUserLogs(@Param("userId") Long userId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期日志
     *
     * @param beforeDate 删除此日期之前的日志
     * @return 删除数量
     */
    int deleteExpiredLogs(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 查询日志详情
     *
     * @param id 日志ID
     * @return 日志
     */
    ImAuditLog selectImAuditLogById(Long id);
}
