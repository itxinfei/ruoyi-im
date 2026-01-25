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
     * @param userId  用户ID
     * @return 搜索结果
     */
    GlobalSearchResultVO globalSearch(GlobalSearchRequest request, Long userId);

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

    /**
     * 获取用户最近搜索的关键词
     *
     * @param userId 用户ID
     * @return 最近搜索的关键词列表
     */
    java.util.List<String> getHotKeywords(Long userId);

    /**
     * 保存用户搜索关键词
     *
     * @param keyword 关键词
     * @param userId  用户ID
     */
    void saveSearchKeyword(String keyword, Long userId);
}
