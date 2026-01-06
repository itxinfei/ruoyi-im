package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImTodoItem;

import java.util.List;

/**
 * 待办事项Mapper接口
 *
 * @author ruoyi
 */
public interface ImTodoItemMapper {

    /**
     * 查询待办事项
     *
     * @param id 待办ID
     * @return 待办事项
     */
    ImTodoItem selectImTodoItemById(Long id);

    /**
     * 查询待办事项列表
     *
     * @param imTodoItem 待办事项
     * @return 待办事项集合
     */
    List<ImTodoItem> selectImTodoItemList(ImTodoItem imTodoItem);

    /**
     * 查询用户的待办事项列表
     *
     * @param userId 用户ID
     * @return 待办事项集合
     */
    List<ImTodoItem> selectTodoItemsByUserId(Long userId);

    /**
     * 查询用户的未完成待办数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    int countUncompletedByUserId(Long userId);

    /**
     * 新增待办事项
     *
     * @param imTodoItem 待办事项
     * @return 结果
     */
    int insertImTodoItem(ImTodoItem imTodoItem);

    /**
     * 修改待办事项
     *
     * @param imTodoItem 待办事项
     * @return 结果
     */
    int updateImTodoItem(ImTodoItem imTodoItem);

    /**
     * 标记待办为已完成
     *
     * @param id 待办ID
     * @return 结果
     */
    int markAsCompleted(Long id);

    /**
     * 删除待办事项
     *
     * @param id 待办ID
     * @return 结果
     */
    int deleteImTodoItemById(Long id);

    /**
     * 批量删除待办事项
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImTodoItemByIds(Long[] ids);
}
