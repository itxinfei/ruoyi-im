package com.ruoyi.web.mapper;

import com.ruoyi.im.domain.ImTodoItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * IM待办事项Mapper接口（Admin模块专用）
 */
@Mapper
public interface ImTodoItemMapper {
    
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
}
