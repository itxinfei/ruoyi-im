package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.ImSystemStatistics;
import com.ruoyi.system.dto.statistics.ImStatisticsQueryRequest;
import com.ruoyi.system.mapper.*;
import com.ruoyi.system.service.IImStatisticsService;
import com.ruoyi.system.vo.file.ImFileStatisticsVO;
import com.ruoyi.system.vo.statistics.ImStatisticsVO;
import com.ruoyi.system.vo.statistics.ImUserStatisticsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据统计服务实现
 *
 * @author ruoyi
 */
@Service
public class ImStatisticsServiceImpl implements IImStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(ImStatisticsServiceImpl.class);

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private ImMessageMapper imMessageMapper;

    @Autowired
    private ImGroupMapper imGroupMapper;

    @Autowired
    private ImFileAssetMapper imFileAssetMapper;

    @Override
    public ImSystemStatistics getSystemStatistics() {
        LocalDate today = LocalDate.now();

        Integer totalUsers = imUserMapper.countImUsers();
        Integer activeUsers = imUserMapper.countActiveUsers(today.minusDays(7));

        Integer newUsers = imUserMapper.countNewUsers(today);

        Integer todayLogins = imUserMapper.countTodayLogins(today);

        Long totalMessages = imMessageMapper.countTotalMessages();
        Long todayMessages = imMessageMapper.countTodayMessages(today);

        Integer totalGroups = imGroupMapper.countTotalGroups();
        Integer activeGroups = imGroupMapper.countActiveGroups(today.minusDays(7));

        Long totalFiles = imFileAssetMapper.countTotalFiles();
        Long todayUploads = imFileAssetMapper.countTodayUploads(today);
        Long totalStorage = imFileAssetMapper.countTotalStorage();

        ImSystemStatistics statistics = new ImSystemStatistics();
        statistics.setStatisticsDate(today.toString());
        statistics.setTotalUsers(totalUsers);
        statistics.setActiveUsers(activeUsers);
        statistics.setNewUsers(newUsers);
        statistics.setTodayLoginUsers(todayLogins);
        statistics.setTotalMessages(totalMessages.intValue());
        statistics.setTodayMessages(todayMessages.intValue());
        statistics.setTotalGroups(totalGroups);
        statistics.setActiveGroups(activeGroups);
        statistics.setTotalFiles(totalFiles.intValue());
        statistics.setTodayUploads(todayUploads.intValue());
        statistics.setTotalStorageSize(totalStorage);

        return statistics;
    }

    @Override
    public List<ImStatisticsVO> getStatistics(ImStatisticsQueryRequest request) {
        List<ImStatisticsVO> result = new ArrayList<>();

        LocalDate startDate = request.getStartDate() != null
            ? LocalDate.parse(request.getStartDate())
            : LocalDate.now().minusDays(30);

        LocalDate endDate = request.getEndDate() != null
            ? LocalDate.parse(request.getEndDate())
            : LocalDate.now();

        switch (request.getType()) {
            case "user":
                result.add(getUserStatistics(request.getType(), startDate, endDate));
                break;
            case "message":
                result.add(getMessageStatistics(startDate, endDate));
                break;
            case "group":
                result.add(getGroupStatistics(startDate, endDate));
                break;
            case "file":
                result.add(getFileStatistics(startDate, endDate));
                break;
            case "system":
                ImSystemStatistics systemStats = getSystemStatistics();
                result.add(convertToVO(systemStats));
                break;
            default:
                logger.warn("未知的统计类型: {}", request.getType());
        }

        return result;
    }

    private ImStatisticsVO getUserStatistics(String userId, LocalDate startDate, LocalDate endDate) {
        ImStatisticsVO vo = new ImStatisticsVO();

        Long totalMessages = imMessageMapper.countTotalMessages(startDate, endDate);
        Long totalGroups = imGroupMapper.countTotalGroups(startDate, endDate);
        Long totalFiles = imFileAssetMapper.countTotalFiles(startDate, endDate);

        vo.setTotalUsers(Integer.parseInt(userId)); // 简化处理
        vo.setTodayMessages(totalMessages.intValue());
        vo.setTotalGroups(totalGroups.intValue());
        vo.setTotalFiles(totalFiles.intValue());

        return vo;
    }

    private ImStatisticsVO getMessageStatistics(LocalDate startDate, LocalDate endDate) {
        ImStatisticsVO vo = new ImStatisticsVO();

        Long totalMessages = imMessageMapper.countTotalMessages(startDate, endDate);
        Long todayMessages = imMessageMapper.countTodayMessages(endDate);

        Double growthRate = 0.0;
        if (todayMessages != null && todayMessages > 0) {
            Long periodMessages = imMessageMapper.countPeriodMessages(startDate.minusDays(7), endDate);
            if (periodMessages != null && periodMessages > 0) {
                growthRate = (double) (todayMessages - periodMessages) / periodMessages * 100;
            }
        }

        vo.setTotalMessages(totalMessages.intValue());
        vo.setTodayMessages(todayMessages.intValue());
        vo.setTotalUsers(periodMessages.intValue()); // 使用临时变量
        vo.setTotalGroups(periodMessages.intValue());
        vo.setTotalFiles(periodMessages.intValue());
        vo.setGrowthRate(growthRate);

        return vo;
    }

    private ImStatisticsVO getGroupStatistics(LocalDate startDate, LocalDate endDate) {
        ImStatisticsVO vo = new ImStatisticsVO();

        Long totalGroups = imGroupMapper.countTotalGroups(startDate, endDate);
        Long newGroups = imGroupMapper.countNewGroups(startDate, endDate);
        Integer activeGroups = imGroupMapper.countActiveGroups(startDate, endDate);

        Double groupGrowth = 0.0;
        if (newGroups != null && newGroups > 0) {
            Long periodNewGroups = imGroupMapper.countNewGroups(startDate.minusDays(7), endDate);
            if (periodNewGroups != null && periodNewGroups > 0) {
                groupGrowth = (double) (newGroups - periodNewGroups) / periodNewGroups * 100;
            }
        }

        vo.setTotalGroups(totalGroups.intValue());
        vo.setTodayMessages(newGroups.intValue()); // 临时赋值
        vo.setTotalUsers(activeGroups);
        vo.setTotalFiles(periodNewGroups.intValue()); // 临时赋值
        vo.setGrowthRate(groupGrowth);

        return vo;
    }

    private ImStatisticsVO getFileStatistics(LocalDate startDate, LocalDate endDate) {
        ImStatisticsVO vo = new ImStatisticsVO();

        Long totalFiles = imFileAssetMapper.countTotalFiles(startDate, endDate);
        Long todayUploads = imFileAssetMapper.countTodayUploads(endDate);
        Long totalStorage = imFileAssetMapper.countTotalStorage();
        Long totalDownloads = imFileAssetMapper.countTotalDownloads(startDate, endDate);

        vo.setTotalFiles(totalFiles.intValue());
        vo.setTodayUploads(todayUploads.intValue());
        vo.setTotalStorageSize(totalStorage);
        vo.setTotalDownloads(totalDownloads.intValue());
        vo.setTotalUsers(totalDownloads.intValue()); // 临时赋值
        vo.setTotalGroups(totalDownloads.intValue()); // 临时赋值

        return vo;
    }

    private ImStatisticsVO convertToVO(ImSystemStatistics stats) {
        ImStatisticsVO vo = new ImStatisticsVO();
        vo.setTotalUsers(stats.getTotalUsers());
        vo.setActiveUsers(stats.getActiveUsers());
        vo.setNewUsers(stats.getNewUsers());
        vo.setTodayLogins(stats.getTodayLoginUsers());
        vo.setTotalMessages(stats.getTotalMessages());
        vo.setTodayMessages(stats.getTodayMessages());
        vo.setTotalGroups(stats.getTotalGroups());
        vo.setActiveGroups(stats.getActiveGroups());
        vo.setTotalFiles(stats.getTotalFiles());
        vo.setTodayUploads(stats.getTodayUploads());
        vo.setTotalStorageSize(stats.getTotalStorageSize());
        return vo;
    }

    @Override
    public ImUserStatisticsVO getUserDetailStatistics(Long userId) {
        ImUserStatisticsVO vo = new ImUserStatisticsVO();
        vo.setUserId(userId);
        // 这里需要根据实际的用户信息进行填充
        return vo;
    }

    @Override
    public ImFileStatisticsVO getFileStatistics() {
        ImFileStatisticsVO vo = new ImFileStatisticsVO();
        // 这里需要根据实际的文件统计信息进行填充
        return vo;
    }
}