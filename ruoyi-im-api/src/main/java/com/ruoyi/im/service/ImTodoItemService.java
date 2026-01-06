package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImTodoItem;

import java.util.List;

/**
 * 待办事项服务接口
 *
 * @author ruoyi
 */
public interface ImTodoItemService {

    /**
     * 创建待办事项
     *
     * @param title 标题
     * @param description 描述
     * @param type 类型
     * @param relatedId 关联ID
     * @param userId 用户ID
     * @return 待办ID
     */
    Long createTodo(String title, String description, String type, Long relatedId, Long userId);

    /**
     * 获取用户待办列表
     *
     * @param userId 用户ID
     * @return 待办列表
     */
    List<ImTodoItem> getUserTodos(Long userId);

    /**
     * 获取用户未完成待办数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    int getUncompletedCount(Long userId);

    /**
     * 标记待办为已完成
     *
     * @param todoId 待办ID
     * @param userId 用户ID
     */
    void markAsCompleted(Long todoId, Long userId);

    /**
     * 删除待办
     *
     * @param todoId 待办ID
     * @param userId 用户ID
     */
    void deleteTodo(Long todoId, Long userId);

    /**
     * 更新待办
     *
     * @param todoId 待办ID
     * @param title 标题
     * @param description 描述
     * @param userId 用户ID
     */
    void updateTodo(Long todoId, String title, String description, Long userId);
}
