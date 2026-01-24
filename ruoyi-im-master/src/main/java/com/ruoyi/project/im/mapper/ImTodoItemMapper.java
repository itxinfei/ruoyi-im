package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImTodoItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM待办事项数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM待办事项管理相关的数据库操作</p>
 * <p>主要功能包括：待办事项的增删改查、状态管理、统计信息、逾期提醒等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImTodoItemMapper {

    // ========== 原有方法保留 ==========

    /**
     * 统计待办事项数量
     *
     * <p>统计指定用户的待办事项数量，支持按完成状态筛选</p>
     *
     * @param userId 用户ID，不能为空
     * @param completed 完成状态：true-已完成，false-未完成，null-查询全部
     * @return 待办事项数量
     */
    int countTodos(@Param("userId") Long userId, @Param("completed") Boolean completed);

    /**
     * 统计最近消息数量
     *
     * <p>统计指定用户的最近消息数量</p>
     * <p>用于消息提醒和统计展示</p>
     *
     * @param userId 用户ID，不能为空
     * @return 最近消息数量
     */
    int countRecentMessages(@Param("userId") Long userId);

    /**
     * 统计待处理审批数量
     *
     * <p>统计指定用户的待处理审批数量</p>
     * <p>用于审批任务提醒</p>
     *
     * @param userId 用户ID，不能为空
     * @return 待处理审批数量
     */
    int countPendingApprovals(@Param("userId") Long userId);

    /**
     * 统计未读通知数量
     *
     * <p>统计指定用户的未读通知数量</p>
     * <p>用于通知提醒和徽章显示</p>
     *
     * @param userId 用户ID，不能为空
     * @return 未读通知数量
     */
    int countUnreadNotices(@Param("userId") Long userId);

    /**
     * 获取用户待办事项列表
     *
     * <p>查询指定用户的待办事项，支持按完成状态筛选</p>
     * <p>返回结果按创建时间倒序排列</p>
     *
     * @param userId 用户ID，不能为空
     * @param completed 完成状态：true-已完成，false-未完成，null-查询全部
     * @return 待办事项列表
     */
    List<ImTodoItem> getTodosForAdmin(@Param("userId") Long userId, @Param("completed") Boolean completed);

    /**
     * 新增待办事项
     *
     * <p>向数据库中插入新的待办事项记录</p>
     *
     * @param todo 待办事项对象，包含待办的基本信息
     * @return 影响行数，1表示成功，0表示失败
     */
    int insertTodo(ImTodoItem todo);

    /**
     * 标记待办为已完成
     *
     * <p>将指定待办事项的状态更新为已完成</p>
     *
     * @param id 待办事项ID，不能为空
     * @return 影响行数，1表示成功，0表示失败
     */
    int markAsCompleted(Long id);

    /**
     * 删除待办事项
     *
     * <p>根据ID删除指定的待办事项记录，删除操作为物理删除</p>
     *
     * @param id 待办事项ID，不能为空
     * @return 影响行数，1表示成功，0表示失败
     */
    int deleteTodoById(Long id);

    /**
     * 获取最近消息列表
     *
     * <p>查询指定用户的最近消息记录</p>
     * <p>用于消息中心展示</p>
     *
     * @param userId 用户ID，不能为空
     * @param limit 查询数量限制，为null表示默认限制
     * @return 最近消息列表
     */
    List<?> getRecentMessages(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 获取待处理审批列表
     *
     * <p>查询指定用户的待处理审批记录</p>
     * <p>用于审批中心展示</p>
     *
     * @param userId 用户ID，不能为空
     * @return 待处理审批列表
     */
    List<?> getPendingApprovals(@Param("userId") Long userId);

    /**
     * 获取未读通知列表
     *
     * <p>查询指定用户的未读通知记录</p>
     * <p>用于通知中心展示</p>
     *
     * @param userId 用户ID，不能为空
     * @return 未读通知列表
     */
    List<?> getUnreadNotices(@Param("userId") Long userId);

    // ========== 新增管理后台CRUD方法 ==========

    /**
     * 根据ID查询待办事项
     *
     * <p>通过待办事项ID查询详细信息</p>
     *
     * @param id 待办事项ID，不能为空
     * @return 待办事项对象，如果不存在则返回null
     */
    ImTodoItem selectImTodoItemById(Long id);

    /**
     * 查询待办事项列表
     *
     * <p>根据条件查询待办事项列表，支持按标题、状态、优先级、时间等条件筛选</p>
     *
     * @param imTodoItem 查询条件，包含title、status、priority、userId等字段
     * @return 待办事项列表，如果没有符合条件的记录则返回空列表
     */
    List<ImTodoItem> selectImTodoItemList(ImTodoItem imTodoItem);

    /**
     * 修改待办事项
     *
     * <p>更新待办事项的信息、状态、优先级等，待办事项ID不可修改</p>
     *
     * @param imTodoItem 待办事项对象，必须包含待办事项ID
     * @return 影响行数，1表示成功，0表示失败
     */
    int updateImTodoItem(ImTodoItem imTodoItem);

    /**
     * 批量删除待办事项
     *
     * <p>批量删除指定的待办事项记录，删除操作为物理删除</p>
     *
     * @param ids 待办事项ID数组，不能为空
     * @return 影响行数，表示成功删除的待办事项数量
     */
    int deleteImTodoItemByIds(Long[] ids);

    /**
     * 更新待办状态
     *
     * <p>更新指定待办事项的状态</p>
     * <p>支持的状态：PENDING-待处理、IN_PROGRESS-进行中、COMPLETED-已完成、CANCELLED-已取消</p>
     *
     * @param id 待办事项ID，不能为空
     * @param status 状态值，不能为空
     * @return 影响行数，1表示成功，0表示失败
     */
    int updateItemStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 获取待办统计数据
     *
     * <p>统计待办事项的各项数据</p>
     * <p>返回的Map包含以下统计项：</p>
     * <ul>
     *   <li>total_count: 待办总数</li>
     *   <li>completed_count: 已完成数量</li>
     *   <li>pending_count: 待处理数量</li>
     *   <li>overdue_count: 逾期数量</li>
     *   <li>today_due_count: 今日到期数量</li>
     * </ul>
     *
     * @return 统计数据Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getTodoStatistics();

    /**
     * 查询逾期未完成的待办
     *
     * <p>查询所有已逾期但未完成的待办事项</p>
     * <p>用于逾期提醒功能</p>
     *
     * @return 逾期待办事项列表
     */
    List<ImTodoItem> selectOverdueItems();

    /**
     * 查询今日到期待办
     *
     * <p>查询所有今日到期的待办事项</p>
     * <p>用于今日待办提醒</p>
     *
     * @return 今日到期待办事项列表
     */
    List<ImTodoItem> selectTodayDueItems();

    /**
     * 清理已完成的待办
     *
     * <p>删除指定天数前已完成的待办事项</p>
     * <p>用于定期清理历史数据</p>
     *
     * @param days 天数，表示删除多少天前的已完成待办
     * @return 影响行数，表示成功删除的待办事项数量
     */
    int cleanCompletedItems(@Param("days") int days);
}
