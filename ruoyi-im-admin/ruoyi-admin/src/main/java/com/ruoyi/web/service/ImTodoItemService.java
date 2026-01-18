package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImTodoItem;
import java.util.List;
import java.util.Map;

/**
 * IM待办事项Service接口（Admin模块专用）
 *
 * <p>提供待办事项管理的核心业务功能，包括待办的创建、查询、更新、删除等操作</p>
 * <p>支持待办状态管理、优先级设置、到期提醒等功能，并集成消息、审批、通知等关联数据</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
public interface ImTodoItemService {

    // ========== 原有方法保留 ==========

    /**
     * 统计用户待办事项数量
     *
     * <p>根据用户ID和完成状态统计待办事项总数</p>
     * <p>用于首页展示用户的待办数量，支持统计未完成和已完成的待办</p>
     *
     * @param userId 用户ID，不能为空
     * @param completed 完成状态，true表示已完成，false表示未完成，null表示统计全部
     * @return 待办事项数量
     */
    int countTodos(Long userId, Boolean completed);

    /**
     * 统计用户最近消息数量
     *
     * <p>统计用户在最近一段时间内的消息数量</p>
     * <p>用于首页动态展示用户的活跃度和消息量</p>
     *
     * @param userId 用户ID，不能为空
     * @return 最近消息数量
     */
    int countRecentMessages(Long userId);

    /**
     * 统计用户待审批数量
     *
     * <p>统计用户需要处理的待审批事项数量</p>
     * <p>用于首页提醒用户有待处理的审批任务</p>
     *
     * @param userId 用户ID，不能为空
     * @return 待审批事项数量
     */
    int countPendingApprovals(Long userId);

    /**
     * 统计用户未读通知数量
     *
     * <p>统计用户的未读系统通知数量</p>
     * <p>用于首页提醒用户有未读的重要通知</p>
     *
     * @param userId 用户ID，不能为空
     * @return 未读通知数量
     */
    int countUnreadNotices(Long userId);

    /**
     * 获取用户待办事项列表（用于管理后台）
     *
     * <p>根据用户ID和完成状态查询待办事项列表</p>
     * <p>包含待办的标题、描述、截止时间、优先级等详细信息</p>
     *
     * @param userId 用户ID，不能为空
     * @param completed 完成状态，true表示已完成，false表示未完成，null表示查询全部
     * @return 待办事项列表，如果没有数据则返回空列表
     */
    List<ImTodoItem> getTodosForAdmin(Long userId, Boolean completed);

    /**
     * 创建待办事项
     *
     * <p>创建新的待办事项，支持关联其他业务数据</p>
     * <p>可关联消息、审批、通知等不同类型的业务对象</p>
     *
     * @param title 待办标题，不能为空
     * @param description 待办详细描述
     * @param type 待办类型，如 MESSAGE（消息）、APPROVAL（审批）、NOTICE（通知）
     * @param relatedId 关联业务对象ID
     * @param relatedType 关联业务对象类型
     * @param userId 创建人用户ID，不能为空
     * @return 新创建的待办事项ID
     */
    Long createTodo(String title, String description, String type, Long relatedId, String relatedType, Long userId);

    /**
     * 标记待办事项为已完成
     *
     * <p>将指定待办事项的状态更新为已完成</p>
     * <p>更新完成时间，记录完成人信息</p>
     *
     * @param id 待办事项ID，不能为空
     * @param userId 操作人用户ID，不能为空
     */
    void markAsCompleted(Long id, Long userId);

    /**
     * 删除待办事项
     *
     * <p>根据ID删除指定的待办事项</p>
     * <p>只有待办的创建人或管理员才有权限删除</p>
     *
     * @param id 待办事项ID，不能为空
     * @param userId 操作人用户ID，用于权限校验
     */
    void deleteTodo(Long id, Long userId);

    /**
     * 获取用户最近消息列表
     *
     * <p>查询用户最近的聊天消息记录</p>
     * <p>用于首页展示用户最近的动态和消息</p>
     *
     * @param userId 用户ID，不能为空
     * @param limit 返回记录数量限制，不能为空
     * @return 最近消息列表
     */
    List<?> getRecentMessages(Long userId, Integer limit);

    /**
     * 获取用户待审批列表
     *
     * <p>查询用户需要处理的审批事项列表</p>
     * <p>包含审批类型、提交人、提交时间等信息</p>
     *
     * @param userId 用户ID，不能为空
     * @return 待审批事项列表
     */
    List<?> getPendingApprovals(Long userId);

    /**
     * 获取用户未读通知列表
     *
     * <p>查询用户的未读系统通知</p>
     * <p>包含通知标题、内容、发送时间等信息</p>
     *
     * @param userId 用户ID，不能为空
     * @return 未读通知列表
     */
    List<?> getUnreadNotices(Long userId);

    // ========== 新增管理后台CRUD方法 ==========

    /**
     * 根据ID查询待办事项
     *
     * <p>通过待办事项主键ID获取单个待办的详细信息</p>
     * <p>包含待办的标题、描述、状态、截止时间、优先级等完整信息</p>
     *
     * @param id 待办事项主键ID，不能为空
     * @return 待办事项对象，如果不存在则返回null
     */
    ImTodoItem selectImTodoItemById(Long id);

    /**
     * 新增待办事项
     *
     * <p>创建新的待办事项记录</p>
     * <p>自动设置创建时间、创建人等基础信息，并初始化待办状态</p>
     *
     * @param imTodoItem 待办事项对象，包含标题、描述、类型、优先级等必要信息
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int insertImTodoItem(ImTodoItem imTodoItem);

    /**
     * 查询待办事项列表
     *
     * <p>根据查询条件检索待办事项列表，支持多条件组合查询</p>
     * <p>可通过标题、状态、优先级、创建时间等条件进行筛选</p>
     *
     * @param imTodoItem 查询条件对象，包含状态、优先级、创建时间等筛选条件
     * @return 待办事项列表，如果没有符合条件的数据则返回空列表
     */
    List<ImTodoItem> selectImTodoItemList(ImTodoItem imTodoItem);

    /**
     * 修改待办事项
     *
     * <p>更新待办事项的信息，如标题、描述、优先级、截止时间等</p>
     * <p>不包含状态更新功能，状态更新请使用专门的状态管理方法</p>
     *
     * @param imTodoItem 待办事项对象，必须包含待办ID和要更新的字段
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int updateImTodoItem(ImTodoItem imTodoItem);

    /**
     * 批量删除待办事项
     *
     * <p>根据待办事项ID数组批量删除多个待办</p>
     * <p>适用于管理后台的批量删除操作，提高删除效率</p>
     *
     * @param ids 待办事项主键ID数组，不能为空或空数组
     * @return 影响的记录数，返回实际删除的待办数量
     */
    int deleteImTodoItemByIds(Long[] ids);

    /**
     * 更新待办状态
     *
     * <p>将待办事项更新为指定的状态</p>
     * <p>支持状态包括：PENDING（待处理）、IN_PROGRESS（进行中）、COMPLETED（已完成）、CANCELLED（已取消）</p>
     *
     * @param id 待办事项ID，不能为空
     * @param status 目标状态，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int updateItemStatus(Long id, String status);

    /**
     * 开始待办
     *
     * <p>将待办事项状态更新为"进行中"</p>
     * <p>记录开始时间，用于统计待办的处理时长</p>
     *
     * @param id 待办事项ID，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int startItem(Long id);

    /**
     * 完成待办
     *
     * <p>将待办事项状态更新为"已完成"</p>
     * <p>记录完成时间，标记待办已处理完毕</p>
     *
     * @param id 待办事项ID，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int completeItem(Long id);

    /**
     * 取消待办
     *
     * <p>将待办事项状态更新为"已取消"</p>
     * <p>记录取消时间，说明该待办不再需要处理</p>
     *
     * @param id 待办事项ID，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int cancelItem(Long id);

    /**
     * 获取待办统计数据
     *
     * <p>统计待办事项的总数、已完成数、未完成数、逾期数等关键指标</p>
     * <p>用于管理后台的数据看板展示和报表生成</p>
     *
     * @return 统计数据Map，包含 totalTodos（总数）、completedCount（已完成）、pendingCount（待处理）、overdueCount（逾期）等指标
     */
    Map<String, Object> getTodoStatistics();

    /**
     * 查询逾期未完成的待办
     *
     * <p>获取所有已超过截止时间但尚未完成的待办事项</p>
     * <p>用于提醒用户及时处理逾期的待办任务</p>
     *
     * @return 逾期待办事项列表，如果没有逾期数据则返回空列表
     */
    List<ImTodoItem> selectOverdueItems();

    /**
     * 查询今日到期的待办
     *
     * <p>获取截止日期为今天的所有待办事项</p>
     * <p>用于提醒用户今日需要完成的任务</p>
     *
     * @return 今日到期待办事项列表，如果没有今日到期数据则返回空列表
     */
    List<ImTodoItem> selectTodayDueItems();

    /**
     * 清理已完成的待办
     *
     * <p>批量删除指定天数之前已完成的所有待办事项</p>
     * <p>用于定期清理历史数据，保持系统性能</p>
     *
     * @param days 天数，删除该天数之前已完成的待办，如30表示删除30天前已完成的待办
     * @return 实际删除的待办数量
     */
    int cleanCompletedItems(int days);
}
