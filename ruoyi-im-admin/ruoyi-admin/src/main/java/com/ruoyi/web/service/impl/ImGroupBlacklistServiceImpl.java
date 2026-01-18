package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImGroupBlacklist;
import com.ruoyi.web.mapper.ImGroupBlacklistMapper;
import com.ruoyi.web.service.ImGroupBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组黑名单Service实现（Admin模块专用）
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Service
public class ImGroupBlacklistServiceImpl implements ImGroupBlacklistService {

    @Autowired
    private ImGroupBlacklistMapper groupBlacklistMapper;

    /**
     * 查询群组黑名单列表
     *
     * @param imGroupBlacklist 群组黑名单
     * @return 群组黑名单
     */
    @Override
    public List<ImGroupBlacklist> selectImGroupBlacklistList(ImGroupBlacklist imGroupBlacklist) {
        return groupBlacklistMapper.selectImGroupBlacklistList(imGroupBlacklist);
    }

    /**
     * 根据ID查询群组黑名单
     *
     * @param id 主键ID
     * @return 群组黑名单
     */
    @Override
    public ImGroupBlacklist selectImGroupBlacklistById(Long id) {
        return groupBlacklistMapper.selectImGroupBlacklistById(id);
    }

    /**
     * 根据群组ID查询黑名单列表
     *
     * @param groupId 群组ID
     * @return 黑名单集合
     */
    @Override
    public List<ImGroupBlacklist> selectImGroupBlacklistByGroupId(Long groupId) {
        return groupBlacklistMapper.selectImGroupBlacklistByGroupId(groupId);
    }

    /**
     * 新增群组黑名单
     *
     * @param imGroupBlacklist 群组黑名单
     * @return 影响行数
     */
    @Override
    public int insertImGroupBlacklist(ImGroupBlacklist imGroupBlacklist) {
        imGroupBlacklist.setCreateTime(LocalDateTime.now());
        imGroupBlacklist.setUpdateTime(LocalDateTime.now());
        // 默认有效
        if (imGroupBlacklist.getIsActive() == null) {
            imGroupBlacklist.setIsActive(1);
        }
        return groupBlacklistMapper.insertImGroupBlacklist(imGroupBlacklist);
    }

    /**
     * 修改群组黑名单
     *
     * @param imGroupBlacklist 群组黑名单
     * @return 影响行数
     */
    @Override
    public int updateImGroupBlacklist(ImGroupBlacklist imGroupBlacklist) {
        imGroupBlacklist.setUpdateTime(LocalDateTime.now());
        return groupBlacklistMapper.updateImGroupBlacklist(imGroupBlacklist);
    }

    /**
     * 批量删除群组黑名单
     *
     * @param ids 需要删除的群组黑名单主键集合
     * @return 影响行数
     */
    @Override
    public int deleteImGroupBlacklistByIds(Long[] ids) {
        return groupBlacklistMapper.deleteImGroupBlacklistByIds(ids);
    }

    /**
     * 删除群组黑名单信息
     *
     * @param id 主键ID
     * @return 影响行数
     */
    @Override
    public int deleteImGroupBlacklistById(Long id) {
        return groupBlacklistMapper.deleteImGroupBlacklistById(id);
    }

    /**
     * 解除禁言/拉黑
     *
     * @param id 主键ID
     * @return 影响行数
     */
    @Override
    public int releaseImGroupBlacklist(Long id) {
        return groupBlacklistMapper.releaseImGroupBlacklist(id);
    }

    /**
     * 检查用户是否在群组黑名单中
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @param type    类型 MUTE/BLACKLIST
     * @return 是否在黑名单中
     */
    @Override
    public boolean checkUserInBlacklist(Long groupId, Long userId, String type) {
        int count = groupBlacklistMapper.checkUserInBlacklist(groupId, userId, type);
        return count > 0;
    }

    /**
     * 获取黑名单统计数据
     *
     * @return 统计数据，包含totalCount、mutedCount、blacklistedCount、todayCount
     */
    @Override
    public Map<String, Object> getBlacklistStatistics() {
        Map<String, Object> result = new HashMap<>();
        // 黑名单总数
        result.put("totalCount", groupBlacklistMapper.countTotalBlacklist());
        // 禁言用户数
        result.put("mutedCount", groupBlacklistMapper.countTotalMuted());
        // 拉黑用户数
        result.put("blacklistedCount", groupBlacklistMapper.countTotalBlacklisted());
        // 今日新增
        result.put("todayCount", groupBlacklistMapper.countTodayAdded());
        return result;
    }
}
