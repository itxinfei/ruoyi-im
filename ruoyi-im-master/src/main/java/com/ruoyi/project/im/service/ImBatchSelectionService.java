package com.ruoyi.web.service;

import com.ruoyi.web.domain.dto.ImBatchSelectionDTO;
import com.ruoyi.web.domain.vo.ImBatchSelectionStatusVO;

import java.util.List;

/**
 * 批量选择状态管理服务接口
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
public interface ImBatchSelectionService {

    /**
     * 处理批量选择操作
     * 支持选中、取消选中、清空、全选等操作
     *
     * @param selectionDTO 批量选择请求对象
     * @return 选择状态结果
     */
    ImBatchSelectionStatusVO handleBatchSelection(ImBatchSelectionDTO selectionDTO);

    /**
     * 获取当前选择状态
     * 返回指定页面的选择状态信息
     *
     * @param pageKey 页面标识
     * @param sessionId 会话ID
     * @return 选择状态对象
     */
    ImBatchSelectionStatusVO getSelectionStatus(String pageKey, String sessionId);

    /**
     * 清空指定页面的选择状态
     * 用于页面切换或过滤条件变化时清理状态
     *
     * @param pageKey 页面标识
     * @param sessionId 会话ID
     */
    void clearSelectionStatus(String pageKey, String sessionId);

    /**
     * 清空用户的所有选择状态
     * 用于用户登出时清理状态
     *
     * @param sessionId 会话ID
     */
    void clearAllSelectionStatus(String sessionId);

    /**
     * 根据过滤条件获取当前页的可选择用户ID列表
     * 用于全选操作时获取当前页的所有ID
     *
     * @param pageKey 页面标识
     * @param currentPage 当前页码
     * @param pageSize 每页大小
     * @param filterConditions 过滤条件
     * @return 用户ID列表
     */
    List<Long> getCurrentPageSelectableIds(String pageKey, Integer currentPage, Integer pageSize, 
                                       java.util.Map<String, Object> filterConditions);

    /**
     * 生成过滤条件指纹
     * 用于判断过滤条件是否发生变化
     *
     * @param filterConditions 过滤条件
     * @return 指纹字符串
     */
    String generateFilterFingerprint(java.util.Map<String, Object> filterConditions);

    /**
     * 清理过期的选择状态
     * 定期清理超时的选择状态数据
     */
    void cleanExpiredSelections();

    /**
     * 获取用户的选择统计信息
     * 包括总选择数量、页面选择详情等
     *
     * @param sessionId 会话ID
     * @return 统计信息Map
     */
    java.util.Map<String, Object> getSelectionStatistics(String sessionId);
}