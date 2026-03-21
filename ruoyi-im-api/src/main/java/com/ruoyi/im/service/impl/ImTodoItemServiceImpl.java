package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImTodoItemMapper;
import com.ruoyi.im.service.ImTodoItemService;
import com.ruoyi.im.util.BusinessExceptionHelper;
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
@RequiredArgsConstructor
public class ImTodoItemServiceImpl implements ImTodoItemService {

    private final ImTodoItemMapper todoItemMapper;
    private final ImWebSocketBroadcastService broadcastService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsCompleted(Long todoId, Long userId) {
        ImTodoItem todo = todoItemMapper.selectImTodoItemById(todoId);
        if (todo == null) {
            BusinessExceptionHelper.throwTodoItemNotFound();
        }
        if (!todo.getUserId().equals(userId)) {
            BusinessExceptionHelper.throwNoPermission();
        }
        
        todo.setStatus("DONE");
        todo.setUpdateTime(LocalDateTime.now());
        todoItemMapper.updateImTodoItem(todo);

        // 异步推送实时更新 (Doc-34 §3.2 同构)
        WsFrame frame = new WsFrame();
        frame.setType("NOTIFY");
        frame.setAction("TODO_UPDATE");
        frame.setData(todo);
        broadcastService.sendToUser(userId, frame);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTodo(String title, String description, String type, Long relatedId, Long userId) {
        ImTodoItem todo = new ImTodoItem();
        todo.setUserId(userId);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(2); // 默认中优先级
        todo.setStatus("PENDING");
        todo.setCreateTime(LocalDateTime.now());
        todoItemMapper.insertImTodoItem(todo);
        return todo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTodoWithPriority(String title, String description, Integer priority, Long userId) {
        ImTodoItem todo = new ImTodoItem();
        todo.setUserId(userId);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority != null ? priority : 2);
        todo.setStatus("PENDING");
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteTodo(Long todoId, Long userId) {
        ImTodoItem todo = todoItemMapper.selectImTodoItemById(todoId);
        if (todo == null) {
            BusinessExceptionHelper.throwTodoItemNotFound();
        }
        if (!todo.getUserId().equals(userId)) {
            BusinessExceptionHelper.throwNoPermission();
        }
        todoItemMapper.deleteImTodoItemById(todoId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTodo(Long todoId, String title, String description, Long userId) {
        ImTodoItem todo = todoItemMapper.selectImTodoItemById(todoId);
        if (todo == null) {
            BusinessExceptionHelper.throwTodoItemNotFound();
        }
        if (!todo.getUserId().equals(userId)) {
            BusinessExceptionHelper.throwNoPermission();
        }
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setUpdateTime(LocalDateTime.now());
        todoItemMapper.updateImTodoItem(todo);
    }

    @Override
    public int getUncompletedCountByType(Long userId, String type) {
        return todoItemMapper.countUncompletedByUserId(userId);
    }
}
