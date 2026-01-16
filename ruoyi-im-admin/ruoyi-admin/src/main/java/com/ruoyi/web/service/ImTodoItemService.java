package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImTodoItem;
import java.util.List;
import java.util.Map;

/**
 * IM待办事项Service接口（Admin模块专用）
 */
public interface ImTodoItemService {

    // ========== 原有方法保留 ==========
    int countTodos(Long userId, Boolean completed);

    int countRecentMessages(Long userId);

    int countPendingApprovals(Long userId);

    int countUnreadNotices(Long userId);

    List<ImTodoItem> getTodosForAdmin(Long userId, Boolean completed);

    Long createTodo(String title, String description, String type, Long relatedId, String relatedType, Long userId);

    void markAsCompleted(Long id, Long userId);

    void deleteTodo(Long id, Long userId);

    List<?> getRecentMessages(Long userId, Integer limit);

    List<?> getPendingApprovals(Long userId);

    List<?> getUnreadNotices(Long userId);

    // ========== 新增管理后台CRUD方法 ==========

    /**
     * 查询待办事项
     */
    ImTodoItem selectImTodoItemById(Long id);

    /**
     * 新增待办事项
     */
    int insertImTodoItem(ImTodoItem imTodoItem);

    /**
     * 查询待办事项列表
     */
    List<ImTodoItem> selectImTodoItemList(ImTodoItem imTodoItem);

    /**
     * 修改待办事项
     */
    int updateImTodoItem(ImTodoItem imTodoItem);

    /**
     * 批量删除待办事项
     */
    int deleteImTodoItemByIds(Long[] ids);

    /**
     * 更新待办状态
     */
    int updateItemStatus(Long id, String status);

    /**
     * 开始待办
     */
    int startItem(Long id);

    /**
     * 完成待办
     */
    int completeItem(Long id);

    /**
     * 取消待办
     */
    int cancelItem(Long id);

    /**
     * 获取待办统计数据
     */
    Map<String, Object> getTodoStatistics();

    /**
     * 查询逾期未完成的待办
     */
    List<ImTodoItem> selectOverdueItems();

    /**
     * 查询今日到期待办
     */
    List<ImTodoItem> selectTodayDueItems();

    /**
     * 清理已完成的待办
     */
    int cleanCompletedItems(int days);
}
