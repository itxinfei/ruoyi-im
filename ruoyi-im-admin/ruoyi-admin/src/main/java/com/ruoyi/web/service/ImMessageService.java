package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImMessage;
import java.util.List;
import java.util.Map;

/**
 * IM消息Service接口（Admin模块专用）
 *
 * <p>提供消息管理的核心业务功能，包括消息的查询、统计、删除、撤回等操作</p>
 * <p>支持消息敏感级别管理、时间范围查询、会话消息检索等功能</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
public interface ImMessageService {

    /**
     * 查询IM消息列表
     *
     * <p>根据查询条件检索消息列表，支持多条件组合查询</p>
     * <p>可通过会话ID、发送者、消息类型、发送时间等条件进行筛选</p>
     *
     * @param imMessage 查询条件对象，包含会话ID、发送者ID、消息类型等筛选条件
     * @return 消息列表，如果没有符合条件的数据则返回空列表
     */
    List<ImMessage> selectImMessageList(ImMessage imMessage);

    /**
     * 根据会话ID查询消息列表
     *
     * <p>获取指定会话的所有消息记录</p>
     * <p>按发送时间倒序排列，支持分页查询</p>
     *
     * @param conversationId 会话ID，不能为空
     * @return 消息列表，如果会话不存在或没有消息则返回空列表
     */
    List<ImMessage> selectImMessageListByConversationId(Long conversationId);

    /**
     * 根据时间范围查询消息列表
     *
     * <p>查询指定会话在特定时间范围内的消息记录</p>
     * <p>用于审计、数据导出、历史消息查询等场景</p>
     *
     * @param conversationId 会话ID，不能为空
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 消息列表，如果没有符合条件的数据则返回空列表
     */
    List<ImMessage> selectImMessageListByTimeRange(Long conversationId, String startTime, String endTime);

    /**
     * 根据消息ID查询消息详情
     *
     * <p>通过消息主键ID获取单条消息的详细信息</p>
     * <p>包含消息内容、发送者、发送时间、会话信息等完整数据</p>
     *
     * @param id 消息主键ID，不能为空
     * @return 消息对象，如果不存在则返回null
     */
    ImMessage selectImMessageById(Long id);

    /**
     * 新增IM消息
     *
     * <p>创建新的消息记录</p>
     * <p>自动设置发送时间、消息状态等基础信息</p>
     * <p>支持文本、图片、文件、语音、视频等多种消息类型</p>
     *
     * @param imMessage 消息对象，包含会话ID、发送者ID、消息类型、内容等必要信息
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int insertImMessage(ImMessage imMessage);

    /**
     * 修改IM消息
     *
     * <p>更新消息的信息，如内容、敏感级别等</p>
     * <p>不支持修改消息的发送者、会话等核心字段</p>
     *
     * @param imMessage 消息对象，必须包含消息ID和要更新的字段
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int updateImMessage(ImMessage imMessage);

    /**
     * 统计消息数量
     *
     * <p>根据查询条件统计消息总数</p>
     * <p>支持按会话、发送者、消息类型、时间范围等维度统计</p>
     *
     * @param imMessage 查询条件对象，包含统计所需的筛选条件
     * @return 消息总数
     */
    int countMessages(ImMessage imMessage);

    /**
     * 统计敏感消息数量
     *
     * <p>统计系统中被标记为敏感的消息总数</p>
     * <p>用于内容安全管理和数据统计</p>
     *
     * @return 敏感消息数量
     */
    int countSensitiveMessages();

    /**
     * 删除单条消息
     *
     * <p>根据消息ID删除消息记录</p>
     * <p>注意：此操作是物理删除，数据不可恢复，请谨慎使用</p>
     *
     * @param id 消息主键ID，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int deleteImMessageById(Long id);

    /**
     * 批量删除消息
     *
     * <p>根据消息ID数组批量删除多条消息</p>
     * <p>适用于管理后台的批量删除操作，提高删除效率</p>
     *
     * @param ids 消息主键ID数组，不能为空或空数组
     * @return 影响的记录数，返回实际删除的消息数量
     */
    int deleteImMessageByIds(Long[] ids);

    /**
     * 撤回消息
     *
     * <p>将指定消息标记为已撤回状态</p>
     * <p>撤回后消息内容会被清空或替换为"消息已撤回"提示</p>
     * <p>只能撤回一定时间内发送的消息（如2分钟内）</p>
     *
     * @param messageId 消息ID，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int revokeMessage(Long messageId);

    /**
     * 获取消息统计数据
     *
     * <p>统计消息的总数、今日消息数、敏感消息数等关键指标</p>
     * <p>用于管理后台的数据看板展示和报表生成</p>
     *
     * @return 统计数据Map，包含 totalMessages（消息总数）、todayMessages（今日消息）、sensitiveMessages（敏感消息）等指标
     */
    Map<String, Object> getMessageStatistics();

    /**
     * 批量更新消息敏感级别
     *
     * <p>将指定的多条消息批量标记为敏感级别</p>
     * <p>用于内容审核后的批量标记操作</p>
     *
     * @param messageIds 消息ID列表，不能为空或空列表
     * @param sensitiveLevel 敏感级别，如 HIGH（高）、MEDIUM（中）、LOW（低）、NONE（无）
     * @return 更新的记录数，返回实际更新的消息数量
     */
    int batchUpdateSensitiveLevel(List<Long> messageIds, String sensitiveLevel);

    /**
     * 批量撤回消息
     *
     * <p>将指定的多条消息批量标记为已撤回状态</p>
     * <p>撤回后消息内容会被清空或替换为"消息已撤回"提示</p>
     * <p>用于管理员批量撤回违规消息</p>
     *
     * @param messageIds 消息ID数组，不能为空或空数组
     * @return 撤回的记录数，返回实际撤回的消息数量
     */
    int batchRevokeMessages(Long[] messageIds);
}
