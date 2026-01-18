package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImMessage;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * IM消息数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM消息管理相关的数据库操作</p>
 * <p>主要功能包括：消息的增删改查、会话消息查询、敏感内容管理、消息撤回、统计信息等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImMessageMapper {

    /**
     * 查询消息列表
     *
     * <p>根据条件查询IM消息列表，支持按会话、发送者、消息类型、时间等条件筛选</p>
     *
     * @param imMessage 消息查询条件，包含conversationId、senderId、messageType等字段
     * @return 消息列表，如果没有符合条件的消息则返回空列表
     */
    List<ImMessage> selectImMessageList(ImMessage imMessage);

    /**
     * 根据ID获取消息信息
     *
     * <p>通过消息ID查询消息的详细信息，包括消息内容、发送者、接收者、状态等</p>
     *
     * @param id 消息ID，不能为空
     * @return 消息对象，如果消息不存在则返回null
     */
    ImMessage selectImMessageById(Long id);

    /**
     * 新增消息
     *
     * <p>向数据库中插入新的消息记录，消息发送后默认为未读状态</p>
     *
     * @param imMessage 消息对象，包含消息的基本信息和内容
     * @return 影响行数，1表示成功，0表示失败
     */
    int insertImMessage(ImMessage imMessage);

    /**
     * 修改消息
     *
     * <p>更新消息的信息、状态、内容等，消息ID不可修改</p>
     *
     * @param imMessage 消息对象，必须包含消息ID
     * @return 影响行数，1表示成功，0表示失败
     */
    int updateImMessage(ImMessage imMessage);

    /**
     * 删除消息
     *
     * <p>根据ID删除指定的消息记录，删除操作为物理删除</p>
     *
     * @param id 消息ID，不能为空
     * @return 影响行数，1表示成功，0表示失败
     */
    int deleteImMessageById(Long id);

    /**
     * 批量删除消息
     *
     * <p>批量删除指定的消息记录，删除操作为物理删除</p>
     *
     * @param ids 消息ID数组，不能为空
     * @return 影响行数，表示成功删除的消息数量
     */
    int deleteImMessageByIds(Long[] ids);

    /**
     * 根据会话ID获取消息列表
     *
     * <p>查询指定会话的所有消息记录，按发送时间升序排列</p>
     * <p>用于聊天窗口加载历史消息</p>
     *
     * @param conversationId 会话ID，不能为空
     * @return 消息列表，按发送时间升序排列
     */
    List<ImMessage> selectImMessageListByConversationId(Long conversationId);

    /**
     * 根据时间范围获取消息列表
     *
     * <p>查询指定会话在特定时间范围内的消息记录</p>
     * <p>用于按时间筛选历史消息、数据统计等功能</p>
     *
     * @param conversationId 会话ID，不能为空
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 消息列表，按发送时间升序排列
     */
    List<ImMessage> selectImMessageListByTimeRange(Long conversationId, String startTime, String endTime);

    /**
     * 统计消息数量
     *
     * <p>根据条件统计消息的数量，支持按会话、发送者、消息类型等条件统计</p>
     *
     * @param imMessage 统计条件，包含conversationId、senderId、messageType等字段
     * @return 消息数量
     */
    int countMessages(ImMessage imMessage);

    /**
     * 统计敏感消息数量
     *
     * <p>统计系统中标记为敏感的消息总数</p>
     * <p>用于敏感内容监控和统计</p>
     *
     * @return 敏感消息数量
     */
    int countSensitiveMessages();

    /**
     * 撤回消息
     *
     * <p>将指定的消息标记为已撤回状态</p>
     * <p>撤回后消息内容将不再显示，但保留消息记录</p>
     *
     * @param messageId 消息ID，不能为空
     * @return 影响行数，1表示成功，0表示失败
     */
    int revokeMessage(Long messageId);

    /**
     * 获取消息统计数据
     *
     * <p>统计消息的各项数据，包括总数、各类型数量、发送量、接收量等</p>
     * <p>返回的Map包含以下统计项：</p>
     * <ul>
     *   <li>total_count: 消息总数</li>
     *   <li>text_count: 文本消息数量</li>
     *   <li>image_count: 图片消息数量</li>
     *   <li>file_count: 文件消息数量</li>
     *   <li>voice_count: 语音消息数量</li>
     *   <li>video_count: 视频消息数量</li>
     *   <li>today_count: 今日消息数量</li>
     * </ul>
     *
     * @return 统计数据Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getMessageStatistics();

    /**
     * 批量更新消息敏感级别
     *
     * <p>批量更新指定消息的敏感级别标记</p>
     * <p>用于敏感内容审核后的批量处理</p>
     *
     * @param messageIds 消息ID列表，不能为空
     * @param sensitiveLevel 敏感级别：NONE-无、LOW-低、MEDIUM-中、HIGH-高
     * @return 影响行数，表示成功更新的消息数量
     */
    int batchUpdateSensitiveLevel(List<Long> messageIds, String sensitiveLevel);
}
