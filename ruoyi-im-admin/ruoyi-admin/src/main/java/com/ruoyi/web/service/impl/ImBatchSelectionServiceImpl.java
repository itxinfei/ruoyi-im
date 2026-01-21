package com.ruoyi.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.web.domain.ImUser;
import com.ruoyi.web.domain.dto.ImBatchSelectionDTO;
import com.ruoyi.web.domain.vo.ImBatchSelectionStatusVO;
import com.ruoyi.web.mapper.ImUserMapper;
import com.ruoyi.web.service.ImBatchSelectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 批量选择状态管理服务实现
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Service
public class ImBatchSelectionServiceImpl implements ImBatchSelectionService {

    private static final Logger logger = LoggerFactory.getLogger(ImBatchSelectionServiceImpl.class);

    @Autowired
    private ImUserMapper userMapper;

    // 使用内存存储替代 Redis
    private static final ConcurrentHashMap<String, SelectionData> SELECTION_STORE = new ConcurrentHashMap<>();

    private static final String SELECTION_PREFIX = "batch_selection:";
    private static final long DEFAULT_EXPIRE_TIME = 30 * 60 * 1000L; // 30分钟

    /**
     * 内部存储类
     */
    private static class SelectionData {
        String json;
        long expireTime;

        SelectionData(String json, long expireTime) {
            this.json = json;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    @Override
    public ImBatchSelectionStatusVO handleBatchSelection(ImBatchSelectionDTO selectionDTO) {
        try {
            String selectionKey = selectionDTO.getSelectionKey();
            ImBatchSelectionStatusVO currentStatus = getSelectionStatusFromRedis(selectionKey);

            if (currentStatus == null) {
                currentStatus = new ImBatchSelectionStatusVO();
                currentStatus.setPageKey(selectionDTO.getPageKey());
                // sessionId 不存储在 VO 中，仅用于构建 key
                currentStatus.setCurrentPage(selectionDTO.getCurrentPage());
                currentStatus.setPageSize(selectionDTO.getPageSize());
                currentStatus.setTotalCount(selectionDTO.getTotalCount());
            }

            switch (selectionDTO.getOperation()) {
                case "select":
                    handleSelectOperation(currentStatus, selectionDTO);
                    break;
                case "unselect":
                    handleUnselectOperation(currentStatus, selectionDTO);
                    break;
                case "clear":
                    handleClearOperation(currentStatus);
                    break;
                case "selectAll":
                    handleSelectAllOperation(currentStatus, selectionDTO);
                    break;
            }

            currentStatus.setFilterFingerprint(generateFilterFingerprint(selectionDTO.getFilterConditions()));
            currentStatus.setExpireTime(System.currentTimeMillis() + DEFAULT_EXPIRE_TIME);
            currentStatus.updateSelectedCount();

            saveSelectionStatusToRedis(selectionKey, currentStatus);
            return currentStatus;

        } catch (Exception e) {
            logger.error("处理批量选择失败", e);
            return createErrorStatus(selectionDTO.getPageKey(), selectionDTO.getSessionId());
        }
    }

    @Override
    public ImBatchSelectionStatusVO getSelectionStatus(String pageKey, String sessionId) {
        try {
            String selectionKey = SELECTION_PREFIX + sessionId + "_" + pageKey;
            SelectionData data = SELECTION_STORE.get(selectionKey);

            if (data != null && !data.isExpired()) {
                ImBatchSelectionStatusVO status = JSON.parseObject(data.json, ImBatchSelectionStatusVO.class);
                if (status.isSelectionValid()) {
                    return status;
                } else {
                    SELECTION_STORE.remove(selectionKey);
                }
            } else if (data != null && data.isExpired()) {
                SELECTION_STORE.remove(selectionKey);
            }
        } catch (Exception e) {
            logger.error("获取选择状态失败", e);
        }

        return createEmptyStatus(pageKey, sessionId);
    }

    @Override
    public void clearSelectionStatus(String pageKey, String sessionId) {
        try {
            String selectionKey = SELECTION_PREFIX + sessionId + "_" + pageKey;
            SELECTION_STORE.remove(selectionKey);
        } catch (Exception e) {
            logger.error("清空选择状态失败", e);
        }
    }

    @Override
    public void clearAllSelectionStatus(String sessionId) {
        try {
            String prefix = SELECTION_PREFIX + sessionId + "_";
            SELECTION_STORE.keySet().removeIf(key -> key.startsWith(prefix));
        } catch (Exception e) {
            logger.error("清空所有选择状态失败", e);
        }
    }

    @Override
    public List<Long> getCurrentPageSelectableIds(String pageKey, Integer currentPage, Integer pageSize,
                                             Map<String, Object> filterConditions) {
        try {
            ImUser searchCondition = new ImUser();
            // 根据过滤条件构建查询条件
            if (filterConditions != null) {
                buildSearchCondition(searchCondition, filterConditions);
            }

            // 使用现有的查询方法获取用户列表，然后提取 ID
            List<ImUser> users = userMapper.selectImUserList(searchCondition);
            if (users == null || users.isEmpty()) {
                return new ArrayList<>();
            }

            // 简化处理：返回所有符合条件的用户 ID
            // 实际分页应该在前端或通过其他方式处理
            List<Long> ids = new ArrayList<>();
            for (ImUser user : users) {
                ids.add(user.getId());
            }
            return ids;

        } catch (Exception e) {
            logger.error("获取当前页可选择用户ID失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public String generateFilterFingerprint(Map<String, Object> filterConditions) {
        try {
            if (filterConditions == null || filterConditions.isEmpty()) {
                return "";
            }
            
            JSONObject jsonObject = new JSONObject();
            filterConditions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> jsonObject.put(entry.getKey(), entry.getValue()));
            
            String jsonString = jsonObject.toJSONString();
            return md5(jsonString);
            
        } catch (Exception e) {
            logger.error("生成过滤条件指纹失败", e);
            return "";
        }
    }

    @Override
    public void cleanExpiredSelections() {
        try {
            long currentTime = System.currentTimeMillis();
            SELECTION_STORE.entrySet().removeIf(entry -> {
                try {
                    SelectionData data = entry.getValue();
                    if (data.isExpired()) {
                        return true;
                    }
                    if (data.json != null) {
                        ImBatchSelectionStatusVO status = JSON.parseObject(data.json, ImBatchSelectionStatusVO.class);
                        return status == null || !status.isSelectionValid();
                    }
                } catch (Exception e) {
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            logger.error("清理过期选择状态失败", e);
        }
    }

    @Override
    public Map<String, Object> getSelectionStatistics(String sessionId) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            String prefix = SELECTION_PREFIX + sessionId + "_";

            int totalSelectedCount = 0;
            int totalPages = 0;
            List<Map<String, Object>> pageDetails = new ArrayList<>();

            for (Map.Entry<String, SelectionData> entry : SELECTION_STORE.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    SelectionData data = entry.getValue();
                    if (data != null && !data.isExpired() && data.json != null) {
                        ImBatchSelectionStatusVO status = JSON.parseObject(data.json, ImBatchSelectionStatusVO.class);
                        if (status != null && status.isSelectionValid()) {
                            totalSelectedCount += status.getTotalSelectedCount();
                            totalPages++;

                            Map<String, Object> pageDetail = new HashMap<>();
                            pageDetail.put("pageKey", status.getPageKey());
                            pageDetail.put("selectedCount", status.getTotalSelectedCount());
                            pageDetail.put("isCrossPageSelectAll", status.getCrossPageSelectAll());
                            pageDetails.add(pageDetail);
                        }
                    }
                }
            }

            statistics.put("totalSelectedCount", totalSelectedCount);
            statistics.put("totalPages", totalPages);
            statistics.put("pageDetails", pageDetails);

        } catch (Exception e) {
            logger.error("获取选择统计信息失败", e);
            statistics.put("error", "获取统计信息失败");
        }

        return statistics;
    }

    private void handleSelectOperation(ImBatchSelectionStatusVO status, ImBatchSelectionDTO selectionDTO) {
        List<Long> targetIds = selectionDTO.getTargetIds();
        if (status.getSelectedIds() == null) {
            status.setSelectedIds(new ArrayList<>());
        }
        
        for (Long id : targetIds) {
            if (!status.getSelectedIds().contains(id)) {
                status.getSelectedIds().add(id);
            }
        }
    }

    private void handleUnselectOperation(ImBatchSelectionStatusVO status, ImBatchSelectionDTO selectionDTO) {
        List<Long> targetIds = selectionDTO.getTargetIds();
        if (status.getSelectedIds() != null) {
            status.getSelectedIds().removeAll(targetIds);
        }
        
        // 如果取消选中了当前页的某些项，需要更新跨页全选状态
        if (status.getCurrentPageSelectedIds() != null) {
            status.getCurrentPageSelectedIds().removeAll(targetIds);
        }
        
        if (status.getCrossPageSelectAll()) {
            status.setCrossPageSelectAll(false);
        }
    }

    private void handleClearOperation(ImBatchSelectionStatusVO status) {
        status.clearAllSelections();
    }

    private void handleSelectAllOperation(ImBatchSelectionStatusVO status, ImBatchSelectionDTO selectionDTO) {
        List<Long> currentPageIds = getCurrentPageSelectableIds(
            selectionDTO.getPageKey(),
            selectionDTO.getCurrentPage(),
            selectionDTO.getPageSize(),
            selectionDTO.getFilterConditions()
        );
        
        if (currentPageIds != null && !currentPageIds.isEmpty()) {
            status.setCurrentPageAllSelected(currentPageIds);
            // setCurrentPageAllSelected 已设置 currentPageSelectedIds
            
            if (status.getSelectedIds() == null) {
                status.setSelectedIds(new ArrayList<>());
            }
            
            // 如果是跨页全选，需要更新选中列表
            if (selectionDTO.getCrossPageSelectAll() != null && selectionDTO.getCrossPageSelectAll()) {
                status.setCrossPageSelectAll(true);
                status.setSelectedIds(new ArrayList<>());
                // 这里需要获取所有符合条件的数据ID，简化处理
            } else {
                status.getSelectedIds().removeAll(currentPageIds);
                status.getSelectedIds().addAll(currentPageIds);
            }
        }
    }

    private void buildSearchCondition(ImUser searchCondition, Map<String, Object> filterConditions) {
        if (filterConditions == null) return;
        
        for (Map.Entry<String, Object> entry : filterConditions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            switch (key) {
                case "username":
                    searchCondition.setUsername((String) value);
                    break;
                case "nickname":
                    searchCondition.setNickname((String) value);
                    break;
                case "status":
                    searchCondition.setStatus((Integer) value);
                    break;
                case "mobile":
                    searchCondition.setMobile((String) value);
                    break;
                case "email":
                    searchCondition.setEmail((String) value);
                    break;
                default:
                    // 忽略不支持的过滤条件
                    break;
            }
        }
    }

    private ImBatchSelectionStatusVO getSelectionStatusFromRedis(String selectionKey) {
        try {
            SelectionData data = SELECTION_STORE.get(SELECTION_PREFIX + selectionKey);
            if (data != null && !data.isExpired()) {
                return JSON.parseObject(data.json, ImBatchSelectionStatusVO.class);
            }
        } catch (Exception e) {
            logger.error("从内存获取选择状态失败", e);
        }
        return null;
    }

    private void saveSelectionStatusToRedis(String selectionKey, ImBatchSelectionStatusVO status) {
        try {
            String statusJson = JSON.toJSONString(status);
            long expireTime = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;
            SELECTION_STORE.put(SELECTION_PREFIX + selectionKey, new SelectionData(statusJson, expireTime));
        } catch (Exception e) {
            logger.error("保存选择状态到内存失败", e);
        }
    }

    private ImBatchSelectionStatusVO createEmptyStatus(String pageKey, String sessionId) {
        ImBatchSelectionStatusVO status = new ImBatchSelectionStatusVO();
        status.setPageKey(pageKey);
        // sessionId 不存储在 VO 中，仅用于构建 key
        status.setSelectedIds(new ArrayList<>());
        status.setCurrentPageSelectedIds(new ArrayList<>());
        status.setCrossPageSelectAll(false);
        status.setTotalSelectedCount(0);
        status.setCurrentPageCount(0);
        return status;
    }

    private ImBatchSelectionStatusVO createErrorStatus(String pageKey, String sessionId) {
        ImBatchSelectionStatusVO status = createEmptyStatus(pageKey, sessionId);
        status.setSelectable(false);
        status.setSelectionLimit("操作失败，请重试");
        return status;
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return input;
        }
    }
}