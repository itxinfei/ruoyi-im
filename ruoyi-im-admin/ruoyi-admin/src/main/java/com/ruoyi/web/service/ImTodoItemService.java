package com.ruoyi.web.service;

import com.ruoyi.im.domain.ImTodoItem;
import java.util.List;

/**
 * IM待办事项Service接口（Admin模块专用）
 */
public interface ImTodoItemService {
    
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
}
