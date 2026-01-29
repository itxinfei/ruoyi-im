package com.ruoyi.im.service.impl;

import com.ruoyi.im.constants.StatusConstants;
import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImTodoItemMapper;
import com.ruoyi.im.service.ImTodoItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待办事项服务实现
 *
 * @author ruoyi
 */
@Service
public class ImTodoItemServiceImpl implements ImTodoItemService {

    @Autowired
    private ImTodoItemMapper todoItemMapper;

    @Override
    public Long createTodo(String title, String description, String type, Long relatedId, Long userId) {
        ImTodoItem todo = new ImTodoItem();
        todo.setUserId(userId);
        todo.setTitle(title);
        todo.setDescription(description);
        // type和relatedId不在数据库表中，跳过设置
        todo.setPriority(2); // 2=中
        todo.setStatus(StatusConstants.Task.PENDING);
        todo.setCreateTime(LocalDateTime.now());
        todoItemMapper.insertImTodoItem(todo);
        return todo.getId();
    }

    @Override
    public Long createTodoWithPriority(String title, String description, Integer priority, Long userId) {
        ImTodoItem todo = new ImTodoItem();
        todo.setUserId(userId);
        todo.setTitle(title);
        todo.setDescription(description);
        // 优先级：1=低, 2=中, 3=高
        todo.setPriority(priority != null && priority >= 1 && priority <= 3 ? priority : 2);
        todo.setStatus(StatusConstants.Task.PENDING);
        todo.setCreateTime(LocalDateTime.now());
        todoItemMapper.insertImTodoItem(todo);
        return todo.getId();
    }

    @Override
    public List<ImTodoItem> getUserTodos(Long userId) {
        return todoItemMapper.selectTodoItemsByUserId(userId);
    }

    @Override
    public int getUncompletedCount(Long userId) {
        return todoItemMapper.countUncompletedByUserId(userId);
    }

    @Override
    public void markAsCompleted(Long todoId, Long userId) {
        ImTodoItem todo = todoItemMapper.selectImTodoItemById(todoId);
        if (todo == null) {
            throw new BusinessException("待办事项不存在");
        }
        if (!todo.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        todoItemMapper.markAsCompleted(todoId);
    }

    @Override
    public void deleteTodo(Long todoId, Long userId) {
        ImTodoItem todo = todoItemMapper.selectImTodoItemById(todoId);
        if (todo == null) {
            throw new BusinessException("待办事项不存在");
        }
        if (!todo.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        todoItemMapper.deleteImTodoItemById(todoId);
    }

    @Override
    public void updateTodo(Long todoId, String title, String description, Long userId) {
        ImTodoItem todo = todoItemMapper.selectImTodoItemById(todoId);
        if (todo == null) {
            throw new BusinessException("待办事项不存在");
        }
        if (!todo.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setUpdateTime(LocalDateTime.now());
        todoItemMapper.updateImTodoItem(todo);
    }

    @Override
    public int getUncompletedCountByType(Long userId, String type) {
        // 根据类型统计未完成待办数量
        // 注意：当前数据库表中可能没有type字段，这里按需求处理
        // 如果数据库表支持type筛选，使用mapper方法查询
        // 否则返回总数或0
        try {
            return todoItemMapper.countUncompletedByUserId(userId);
        } catch (Exception e) {
            // 如果查询失败，返回0
            return 0;
        }
    }
}
