package com.ruoyi.web.service.impl;

import com.ruoyi.common.utils.CacheUtils;
import com.ruoyi.web.domain.ImGroup;
import com.ruoyi.web.domain.ImGroupMember;
import com.ruoyi.web.mapper.ImGroupMapper;
import com.ruoyi.web.service.ImGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM群组Service实现（Admin模块专用）
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {

    private static final Logger logger = LoggerFactory.getLogger(ImGroupServiceImpl.class);

    @Autowired
    private ImGroupMapper groupMapper;

    @Override
    public List<ImGroup> selectImGroupList(ImGroup imGroup) {
        return groupMapper.selectImGroupList(imGroup);
    }

    @Override
    public ImGroup selectImGroupById(Long id) {
        return groupMapper.selectImGroupById(id);
    }

    @Override
    public int insertImGroup(ImGroup imGroup) {
        return groupMapper.insertImGroup(imGroup);
    }

    @Override
    public int updateImGroup(ImGroup imGroup) {
        return groupMapper.updateImGroup(imGroup);
    }

    @Override
    public int deleteImGroupById(Long id) {
        return groupMapper.deleteImGroupById(id);
    }

    @Override
    public int deleteImGroupByIds(Long[] ids) {
        return groupMapper.deleteImGroupByIds(ids);
    }

    @Override
    public List<ImGroupMember> selectGroupMembersByGroupId(Long groupId) {
        return groupMapper.selectGroupMembersByGroupId(groupId);
    }

    @Override
    public int addGroupMember(Long groupId, Long userId, String role, Long inviterId) {
        return groupMapper.addGroupMember(groupId, userId, role, inviterId);
    }

    @Override
    public int dismissGroup(Long groupId) {
        return groupMapper.dismissGroup(groupId);
    }

    /**
     * 获取群组统计数据
     *
     * @return 统计数据，包含totalCount、activeCount、mutedCount、largeCount
     */
    @Override
    public Map<String, Object> getGroupStatistics() {
        String cacheKey = "im:group:stats";
        String cacheName = "im-stats";

        // 1. 尝试从缓存获取
        Map<String, Object> stats = (Map<String, Object>) CacheUtils.get(cacheName, cacheKey);
        if (stats != null) {
            return stats;
        }

        // 2. 聚合统计查询
        stats = groupMapper.selectGroupStatistics();

        // 3. 结果处理（防止Null并确保类型正确）
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("totalCount", 0);
            stats.put("activeCount", 0);
            stats.put("mutedCount", 0);
            stats.put("largeCount", 0);
        }

        // 4. 存入缓存
        CacheUtils.put(cacheName, cacheKey, stats);

        return stats;
    }

    @Override
    @Transactional
    public Map<String, Object> batchImportGroups(List<ImGroup> groups, boolean updateSupported) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failList = new ArrayList<>();

        if (groups == null || groups.isEmpty()) {
            result.put("totalRows", 0);
            result.put("successCount", 0);
            result.put("failCount", 0);
            result.put("failList", failList);
            return result;
        }

        for (int i = 0; i < groups.size(); i++) {
            ImGroup group = groups.get(i);
            int rowNum = i + 2; // Excel 行号从2开始（第1行是表头）

            try {
                // 数据校验
                String errorMsg = validateGroup(group);
                if (errorMsg != null) {
                    failCount++;
                    Map<String, String> failInfo = new HashMap<>();
                    failInfo.put("row", String.valueOf(rowNum));
                    failInfo.put("groupName", group.getName() != null ? group.getName() : "");
                    failInfo.put("reason", errorMsg);
                    failList.add(failInfo);
                    logger.warn("导入群组失败（第{}行）: {}", rowNum, errorMsg);
                    continue;
                }

                // 检查群组名称是否已存在
                List<ImGroup> existList = groupMapper.selectImGroupList(
                    new ImGroup() {{ setName(group.getName()); }}
                );
                ImGroup existGroup = null;
                if (!existList.isEmpty()) {
                    existGroup = existList.get(0);
                }

                if (existGroup != null) {
                    if (updateSupported) {
                        // 更新已存在的群组
                        group.setId(existGroup.getId());
                        updateImGroup(group);
                        successCount++;
                        logger.info("更新群组成功（第{}行）: {}", rowNum, group.getName());
                    } else {
                        failCount++;
                        Map<String, String> failInfo = new HashMap<>();
                        failInfo.put("row", String.valueOf(rowNum));
                        failInfo.put("groupName", group.getName());
                        failInfo.put("reason", "群组名称已存在");
                        failList.add(failInfo);
                        logger.warn("导入群组失败（第{}行）: 群组名称已存在 - {}", rowNum, group.getName());
                    }
                } else {
                    // 新增群组
                    // 设置默认值
                    if (group.getAllMuted() == null) {
                        group.setAllMuted(0); // 默认不禁言
                    }
                    if (group.getMaxMembers() == null || group.getMaxMembers() <= 0) {
                        group.setMaxMembers(500); // 默认最大成员数
                    }
                    if (group.getIsDeleted() == null) {
                        group.setIsDeleted(0);
                    }
                    insertImGroup(group);
                    successCount++;
                    logger.info("导入群组成功（第{}行）: {}", rowNum, group.getName());
                }

            } catch (Exception e) {
                failCount++;
                Map<String, String> failInfo = new HashMap<>();
                failInfo.put("row", String.valueOf(rowNum));
                failInfo.put("groupName", group.getName() != null ? group.getName() : "");
                failInfo.put("reason", "系统异常: " + e.getMessage());
                failList.add(failInfo);
                logger.error("导入群组失败（第{}行）: {}", rowNum, e.getMessage(), e);
            }
        }

        result.put("totalRows", groups.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failList", failList);
        logger.info("批量导入群组完成 - 总行数: {}, 成功: {}, 失败: {}",
            groups.size(), successCount, failCount);

        return result;
    }

    /**
     * 校验群组数据
     *
     * @param group 群组对象
     * @return 错误信息，null 表示校验通过
     */
    private String validateGroup(ImGroup group) {
        if (group.getName() == null || group.getName().trim().isEmpty()) {
            return "群组名称不能为空";
        }
        if (group.getName().length() > 50) {
            return "群组名称长度不能超过50个字符";
        }

        // 群主ID必须有效
        if (group.getOwnerId() == null || group.getOwnerId() <= 0) {
            return "群主ID不能为空";
        }

        // 描述长度限制
        if (group.getDescription() != null && group.getDescription().length() > 200) {
            return "群描述长度不能超过200个字符";
        }

        // 最大成员数范围检查
        if (group.getMaxMembers() != null && group.getMaxMembers() > 0) {
            if (group.getMaxMembers() < 2) {
                return "最大成员数不能小于2";
            }
            if (group.getMaxMembers() > 5000) {
                return "最大成员数不能超过5000";
            }
        }

        return null;
    }
}
