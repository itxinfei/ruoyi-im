package com.ruoyi.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Set;

/**
 * IM消息推送数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM消息推送相关的数据库操作和在线状态管理</p>
 * <p>主要功能包括：在线用户统计、用户在线状态查询、消息推送、广播消息等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImMessagePushMapper {

    /**
     * 获取在线用户数量
     *
     * <p>统计当前所有在线用户的总数</p>
     * <p>用于系统监控和在线状态展示</p>
     *
     * @return 在线用户数量
     */
    int getOnlineUserCount();

    /**
     * 获取在线用户ID集合
     *
     * <p>查询所有在线用户的ID集合</p>
     * <p>用于批量消息推送、用户筛选等场景</p>
     *
     * @return 在线用户ID集合
     */
    Set<Long> getOnlineUserIds();

    /**
     * 检查用户是否在线
     *
     * <p>判断指定用户当前是否在线</p>
     * <p>用于消息推送前的在线状态判断</p>
     *
     * @param userId 用户ID，不能为空
     * @return true-在线，false-离线
     */
    boolean isUserOnline(@Param("userId") Long userId);

    /**
     * 断开用户连接
     *
     * <p>强制断开指定用户的WebSocket连接</p>
     * <p>用于用户登出、强制下线等场景</p>
     *
     * @param userId 用户ID，不能为空
     */
    void disconnectUser(@Param("userId") Long userId);

    /**
     * 发送消息给指定用户
     *
     * <p>向指定用户发送消息</p>
     * <p>消息格式为JSON字符串，包含消息类型、内容等信息</p>
     *
     * @param userId 用户ID，不能为空
     * @param message 消息内容，JSON格式字符串
     */
    void sendMessageToUser(@Param("userId") Long userId, @Param("message") String message);

    /**
     * 广播消息给所有在线用户
     *
     * <p>向所有在线用户广播消息</p>
     * <p>用于系统公告、全局通知等场景</p>
     *
     * @param message 消息内容，JSON格式字符串
     */
    void broadcastMessage(@Param("message") String message);
}
