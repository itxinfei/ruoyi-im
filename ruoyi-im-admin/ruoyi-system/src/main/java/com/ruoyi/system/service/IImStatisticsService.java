package com.ruoyi.system.service;

import com.ruoyi.system.domain.ImSystemStatistics;
import com.ruoyi.system.dto.statistics.ImStatisticsQueryRequest;
import com.ruoyi.system.vo.file.ImFileStatisticsVO;
import com.ruoyi.system.vo.statistics.ImStatisticsVO;
import com.ruoyi.system.vo.statistics.ImUserStatisticsVO;

import java.util.List;

/**
 * 数据统计服务接口
 *
 * @author ruoyi
 */
public interface IImStatisticsService {

    /**
     * 获取系统统计数据
     *
     * @return 系统统计信息
     */
    ImSystemStatistics getSystemStatistics();

    /**
     * 获取统计列表
     *
     * @param request 查询请求
     * @return 统计信息列表
     */
    List<ImStatisticsVO> getStatistics(ImStatisticsQueryRequest request);

    /**
     * 获取用户统计数据
     *
     * @param userId 用户ID
     * @return 用户统计详情
     */
    ImUserStatisticsVO getUserDetailStatistics(Long userId);

    /**
     * 获取文件统计信息
     *
     * @return 文件统计信息
     */
    ImFileStatisticsVO getFileStatistics();
}