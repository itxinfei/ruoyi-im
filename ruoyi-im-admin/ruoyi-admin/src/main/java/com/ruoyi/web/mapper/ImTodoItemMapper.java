package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImTodoItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM待办事项Mapper接口（Admin模块专用）
 */
@Mapper
public interface ImTodoItemMapper {

    // ========== 原有方法保留 ==========
    int countTodos(@Param("userId") Long userId, @Param("completed") Boolean completed);

    int countRecentMessages(@Param("userId") Long userId);

    int countPendingApprovals(@Param("userId") Long userId);

    int countUnreadNotices(@Param("userId") Long userId);

    List<ImTodoItem> getTodosForAdmin(@Param("userId") Long userId, @Param("completed") Boolean completed);

    int insertTodo(ImTodoItem todo);

    int markAsCompleted(Long id);

    int deleteTodoById(Long id);

    List<?> getRecentMessages(@Param("userId") Long userId, @Param("limit") Integer limit);

    List<?> getPendingApprovals(@Param("userId") Long userId);

    List<?> getUnreadNotices(@Param("userId") Long userId);

    // ========== 新增管理后台CRUD方法 ==========

    /**
     * 查询待办事项
     */
    ImTodoItem selectImTodoItemById(Long id);

    /**
     * 查询待办事项列表（支持筛选）
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
    int updateItemStatus(@Param("id") Long id, @Param("status") String status);

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
     * 清理已完成的待办（指定天数前）
     */
    int cleanCompletedItems(@Param("days") int days);
}
