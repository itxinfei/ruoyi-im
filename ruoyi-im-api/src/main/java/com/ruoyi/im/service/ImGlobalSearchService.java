package com.ruoyi.im.service;

import com.ruoyi.im.dto.search.GlobalSearchRequest;
import com.ruoyi.im.vo.search.GlobalSearchResultVO;

/**
 * 全局搜索服务接口
 * 提供跨模块的统一搜索功能
 *
 * @author ruoyi
 */
public interface ImGlobalSearchService {

    /**
     * 全局搜索
     * 根据关键词搜索消息、联系人、群组、文件、工作台内容
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    GlobalSearchResultVO globalSearch(GlobalSearchRequest request);

    /**
     * 搜索消息
     *
     * @param keyword 关键词
     * @param userId  用户ID
     * @return 搜索结果
     */
    GlobalSearchResultVO searchMessages(String keyword, Long userId);

    /**
     * 搜索联系人
     *
     * @param keyword 关键词
     * @param userId  用户ID
     * @return 搜索结果
     */
    GlobalSearchResultVO searchContacts(String keyword, Long userId);

    /**
     * 搜索群组
     *
     * @param keyword 关键词
     * @param userId  用户ID
     * @return 搜索结果
     */
    GlobalSearchResultVO searchGroups(String keyword, Long userId);

    /**
     * 搜索文件
     *
     * @param keyword 关键词
     * @param userId  用户ID
     * @return 搜索结果
     */
    GlobalSearchResultVO searchFiles(String keyword, Long userId);

    /**
     * 搜索工作台内容（任务、文档、日程等）
     *
     * @param keyword 关键词
     * @param userId  用户ID
     * @return 搜索结果
     */
    GlobalSearchResultVO searchWorkbench(String keyword, Long userId);
}
