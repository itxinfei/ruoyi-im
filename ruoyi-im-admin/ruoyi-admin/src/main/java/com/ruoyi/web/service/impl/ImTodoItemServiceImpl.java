package com.ruoyi.web.service.impl;

import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.web.mapper.ImTodoItemMapper;
import com.ruoyi.web.service.ImTodoItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * IM待办事项Service实现（Admin模块专用）
 */
@Service
public class ImTodoItemServiceImpl implements ImTodoItemService {

    @Autowired
    private ImTodoItemMapper todoItemMapper;

    @Override
    public int countTodos(Long userId, Boolean completed) {
        return todoItemMapper.countTodos(userId, completed);
    }

    @Override
    public int countRecentMessages(Long userId) {
        return todoItemMapper.countRecentMessages(userId);
    }

    @Override
    public int countPendingApprovals(Long userId) {
        return todoItemMapper.countPendingApprovals(userId);
    }

    @Override
    public int countUnreadNotices(Long userId) {
        return todoItemMapper.countUnreadNotices(userId);
    }

    @Override
    public List<ImTodoItem> getTodosForAdmin(Long userId, Boolean completed) {
        return todoItemMapper.getTodosForAdmin(userId, completed);
    }

    @Override
    public Long createTodo(String title, String description, String type, Long relatedId, String relatedType, Long userId) {
        ImTodoItem todo = new ImTodoItem();
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setType(type);
        todo.setRelatedId(relatedId);
        todo.setRelatedType(relatedType);
        todo.setUserId(userId);
        todoItemMapper.insertTodo(todo);
        return todo.getId();
    }

    @Override
    public void markAsCompleted(Long id, Long userId) {
        todoItemMapper.markAsCompleted(id);
    }

    @Override
    public void deleteTodo(Long id, Long userId) {
        todoItemMapper.deleteTodoById(id);
    }

    @Override
    public List<?> getRecentMessages(Long userId, Integer limit) {
        return todoItemMapper.getRecentMessages(userId, limit);
    }

    @Override
    public List<?> getPendingApprovals(Long userId) {
        return todoItemMapper.getPendingApprovals(userId);
    }

    @Override
    public List<?> getUnreadNotices(Long userId) {
        return todoItemMapper.getUnreadNotices(userId);
    }
}
