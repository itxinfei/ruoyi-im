package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImGroupLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * IM群组管理日志数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM群组管理日志相关的数据库操作</p>
 * <p>主要功能包括：群组日志的增删改查、按群组/操作者查询、日志统计等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImGroupLogMapper {


    /**
     * 查询群组管理日志
     *
     * @param id 群组管理日志主键
     * @return 群组管理日志
     */
    ImGroupLog selectImGroupLogById(Long id);

    /**
     * 查询群组管理日志列表
     *
     * @param imGroupLog 群组管理日志
     * @return 群组管理日志集合
     */
    List<ImGroupLog> selectImGroupLogList(ImGroupLog imGroupLog);

    /**
     * 新增群组管理日志
     *
     * @param imGroupLog 群组管理日志
     * @return 结果
     */
    int insertImGroupLog(ImGroupLog imGroupLog);

    /**
     * 修改群组管理日志
     *
     * @param imGroupLog 群组管理日志
     * @return 结果
     */
    int updateImGroupLog(ImGroupLog imGroupLog);

    /**
     * 删除群组管理日志
     *
     * @param id 群组管理日志主键
     * @return 结果
     */
    int deleteImGroupLogById(Long id);

    /**
     * 批量删除群组管理日志
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImGroupLogByIds(Long[] ids);

    /**
     * 根据群组ID查询日志列表
     *
     * @param groupId 群组ID
     * @return 日志列表
     */
    List<ImGroupLog> selectLogsByGroupId(Long groupId);

    /**
     * 根据操作者ID查询日志列表
     *
     * @param operatorId 操作者ID
     * @return 日志列表
     */
    List<ImGroupLog> selectLogsByOperatorId(Long operatorId);

    /**
     * 统计群组日志数量
     *
     * @param groupId 群组ID
     * @return 数量
     */
    int countLogsByGroupId(Long groupId);

    /**
     * 获取群组日志统计数据
     *
     * @param groupId 群组ID
     * @return 统计数据
     */
    Map<String, Object> getLogStatistics(Long groupId);
}
