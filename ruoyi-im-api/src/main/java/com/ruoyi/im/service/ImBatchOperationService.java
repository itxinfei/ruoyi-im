package com.ruoyi.im.service;

import java.util.Map;

/**
 * 批量操作服务接口
 *
 * 用于执行批量好友请求、群组加入等操作
 *
 * @author ruoyi
 */
public interface ImBatchOperationService {

    /**
     * 为指定用户执行批量操作
     * 1) 向系统中除指定用户外的所有其他用户发送好友请求
     * 2) 加入系统中所有可加入的群组
     *
     * @param nicknames 用户昵称列表（如：["张三", "李四"]）
     * @return 操作结果统计
     */
    Map<String, Object> executeBatchOperationsForUsers(String[] nicknames);

    /**
     * 为单个用户执行批量操作
     *
     * @param nickname 用户昵称
     * @return 操作结果统计
     */
    Map<String, Object> executeBatchOperationsForUser(String nickname);
}
