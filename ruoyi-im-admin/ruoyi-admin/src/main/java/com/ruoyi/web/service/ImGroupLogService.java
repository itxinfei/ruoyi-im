package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImGroupLog;
import java.util.List;

/**
 * 群组管理日志Service接口
 *
 * @author ruoyi
 */
public interface ImGroupLogService {

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
     * 批量删除群组管理日志
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImGroupLogByIds(Long[] ids);

    /**
     * 删除群组管理日志信息
     *
     * @param id 群组管理日志主键
     * @return 结果
     */
    int deleteImGroupLogById(Long id);

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
     * 记录群组操作日志
     *
     * @param groupId 群组ID
     * @param operatorId 操作者ID
     * @param operatorName 操作者名称
     * @param operationType 操作类型
     * @param operationDesc 操作描述
     * @return 结果
     */
    int logGroupOperation(Long groupId, Long operatorId, String operatorName, String operationType, String operationDesc);

    /**
     * 记录群组成员操作日志
     *
     * @param groupId 群组ID
     * @param operatorId 操作者ID
     * @param operatorName 操作者名称
     * @param operationType 操作类型
     * @param operationDesc 操作描述
     * @param targetUserId 目标用户ID
     * @param targetUserName 目标用户名称
     * @return 结果
     */
    int logMemberOperation(Long groupId, Long operatorId, String operatorName, String operationType, String operationDesc, Long targetUserId, String targetUserName);
}
