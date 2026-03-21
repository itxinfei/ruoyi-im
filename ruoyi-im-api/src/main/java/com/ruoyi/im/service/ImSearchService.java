package com.ruoyi.im.service;

import com.ruoyi.im.dto.search.GlobalSearchRequest;
import com.ruoyi.im.vo.search.GlobalSearchResultVO;

/**
 * 全局搜索服务接口
 *
 * @author ruoyi
 */
public interface ImSearchService {

    /**
     * 全局搜索
     * 搜索联系人、群组、消息、文件等
     *
     * @param request 搜索请求参数
     * @param userId 当前用户ID
     * @return 搜索结果集合
     */
    GlobalSearchResultVO search(GlobalSearchRequest request, Long userId);
}
