package com.ruoyi.im.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.im.domain.ImApproval;
import com.ruoyi.im.domain.ImApprovalFormData;
import com.ruoyi.im.domain.ImApprovalNode;
import com.ruoyi.im.domain.ImApprovalRecord;
import com.ruoyi.im.domain.ImApprovalTemplate;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImApprovalFormDataMapper;
import com.ruoyi.im.mapper.ImApprovalMapper;
import com.ruoyi.im.mapper.ImApprovalNodeMapper;
import com.ruoyi.im.mapper.ImApprovalRecordMapper;
import com.ruoyi.im.mapper.ImApprovalTemplateMapper;
import com.ruoyi.im.service.ImApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批服务实现
 *
 * @author ruoyi
 */
@Service
public class ImApprovalServiceImpl implements ImApprovalService {

    @Autowired
    private ImApprovalMapper approvalMapper;

    @Autowired
    private ImApprovalTemplateMapper templateMapper;

    @Autowired
    private ImApprovalNodeMapper nodeMapper;

    @Autowired
    private ImApprovalRecordMapper recordMapper;

    @Autowired
    private ImApprovalFormDataMapper formDataMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApproval(Long templateId, String title, Map<String, Object> formData, Long applicantId) {
        // 1. 获取模板信息
        ImApprovalTemplate template = templateMapper.selectImApprovalTemplateById(templateId);
        if (template == null) {
            throw new BusinessException("审批模板不存在");
        }
        if (!"ACTIVE".equals(template.getStatus())) {
            throw new BusinessException("审批模板已停用");
        }

        // 2. 创建审批实例
        ImApproval approval = new ImApproval();
        approval.setTemplateId(templateId);
        approval.setTitle(title);
        approval.setApplicantId(applicantId);
        approval.setStatus("PENDING");
        approval.setCreateTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.insertImApproval(approval);

        Long approvalId = approval.getId();

        // 3. 解析流程配置并创建审批节点
        String flowConfig = template.getFlowConfig();
        createApprovalNodesFromConfig(approvalId, flowConfig);

        // 4. 保存表单数据
        saveFormData(approvalId, formData);

        // 5. 更新当前节点
        ImApprovalNode firstNode = nodeMapper.selectFirstNodeByApprovalId(approvalId);
        if (firstNode != null) {
            approval.setCurrentNodeId(firstNode.getId());
            approvalMapper.updateImApproval(approval);
        }

        return approvalId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processApproval(Long approvalId, String action, String comment, Long approverId) {
        // 1. 查询审批实例
        ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
        if (approval == null) {
            throw new BusinessException("审批不存在");
        }

        if (!"PENDING".equals(approval.getStatus())) {
            throw new BusinessException("审批已处理");
        }

        // 2. 查询当前待处理节点
        List<ImApprovalNode> pendingNodes = nodeMapper.selectPendingNodesByApprovalId(approvalId);
        ImApprovalNode currentNode = findCurrentNode(pendingNodes, approverId);

        if (currentNode == null) {
            throw new BusinessException("无权限操作或审批已处理");
        }

        // 3. 记录审批操作
        ImApprovalRecord record = new ImApprovalRecord();
        record.setApprovalId(approvalId);
        record.setNodeId(currentNode.getId());
        record.setApproverId(approverId);
        record.setAction(action);
        record.setComment(comment);
        record.setActionTime(LocalDateTime.now());
        recordMapper.insertImApprovalRecord(record);

        // 4. 根据操作类型处理
        if ("APPROVE".equals(action)) {
            handleApprove(currentNode, approverId, approval);
        } else if ("REJECT".equals(action)) {
            handleReject(currentNode, approverId, approval);
        } else if ("TRANSFER".equals(action)) {
            handleTransfer(currentNode, approverId, approval);
        }
    }

    @Override
    public Map<String, Object> getApprovalDetail(Long approvalId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询审批实例
        ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
        result.put("approval", approval);

        // 2. 查询模板信息
        ImApprovalTemplate template = templateMapper.selectImApprovalTemplateById(approval.getTemplateId());
        result.put("template", template);

        // 3. 查询表单数据
        List<ImApprovalFormData> formDataList = formDataMapper.selectFormDataByApprovalId(approvalId);
        Map<String, Object> formDataMap = new HashMap<>();
        for (ImApprovalFormData data : formDataList) {
            formDataMap.put(data.getFieldName(), data.getFieldValue());
        }
        result.put("formData", formDataMap);

        // 4. 查询审批节点
        List<ImApprovalNode> nodes = nodeMapper.selectNodesByApprovalId(approvalId);
        result.put("nodes", nodes);

        // 5. 查询审批记录
        List<ImApprovalRecord> records = recordMapper.selectRecordsByApprovalId(approvalId);
        result.put("records", records);

        // 6. 构建流程进度信息
        result.put("progress", buildProgressInfo(nodes, records));

        return result;
    }

    @Override
    public List<ImApproval> getPendingApprovals(Long approverId) {
        return approvalMapper.selectPendingApprovalByApproverId(approverId);
    }

    @Override
    public List<ImApproval> getMyApprovals(Long applicantId) {
        return approvalMapper.selectApprovalByApplicantId(applicantId);
    }

    @Override
    public List<ImApproval> getProcessedApprovals(Long approverId) {
        return approvalMapper.selectProcessedApprovalByApproverId(approverId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApproval(Long approvalId, Long applicantId) {
        ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
        if (approval == null) {
            throw new BusinessException("审批不存在");
        }
        if (!approval.getApplicantId().equals(applicantId)) {
            throw new BusinessException("无权限操作");
        }
        if (!"PENDING".equals(approval.getStatus())) {
            throw new BusinessException("审批已处理，无法撤回");
        }

        approval.setStatus("CANCELLED");
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.updateImApproval(approval);

        // 标记后续节点为已跳过
        skipRemainingNodes(approvalId);
    }

    @Override
    public List<ImApprovalTemplate> getTemplates() {
        return templateMapper.selectImApprovalTemplateList(new ImApprovalTemplate());
    }

    @Override
    public List<ImApprovalTemplate> getActiveTemplates() {
        return templateMapper.selectActiveTemplates();
    }

    /**
     * 转交审批
     *
     * @param approvalId 审批ID
     * @param toUserId 转交给的用户ID
     * @param fromUserId 当前用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void transferApproval(Long approvalId, Long toUserId, Long fromUserId) {
        ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
        if (approval == null) {
            throw new BusinessException("审批不存在");
        }

        // 查询当前待处理节点
        List<ImApprovalNode> pendingNodes = nodeMapper.selectPendingNodesByApprovalId(approvalId);
        ImApprovalNode currentNode = findCurrentNode(pendingNodes, fromUserId);

        if (currentNode == null) {
            throw new BusinessException("无权限操作");
        }

        // 更新节点审批人（移除当前用户，添加目标用户）
        String approverIds = currentNode.getApproverIds();
        String[] approverIdArray = approverIds.split(",");
        List<String> newApproverIds = new ArrayList<>();
        for (String id : approverIdArray) {
            if (!id.equals(String.valueOf(fromUserId))) {
                newApproverIds.add(id);
            }
        }
        newApproverIds.add(String.valueOf(toUserId));
        currentNode.setApproverIds(String.join(",", newApproverIds));
        nodeMapper.updateImApprovalNode(currentNode);

        // 记录转交操作
        ImApprovalRecord record = new ImApprovalRecord();
        record.setApprovalId(approvalId);
        record.setNodeId(currentNode.getId());
        record.setApproverId(fromUserId);
        record.setAction("TRANSFER");
        record.setComment("转交给用户ID: " + toUserId);
        record.setActionTime(LocalDateTime.now());
        recordMapper.insertImApprovalRecord(record);
    }

    /**
     * 委托审批
     *
     * @param approvalId 审批ID
     * @param toUserId 委托给的用户ID
     * @param fromUserId 当前用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delegateApproval(Long approvalId, Long toUserId, Long fromUserId) {
        ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
        if (approval == null) {
            throw new BusinessException("审批不存在");
        }

        // 查询当前待处理节点
        List<ImApprovalNode> pendingNodes = nodeMapper.selectPendingNodesByApprovalId(approvalId);
        ImApprovalNode currentNode = findCurrentNode(pendingNodes, fromUserId);

        if (currentNode == null) {
            throw new BusinessException("无权限操作");
        }

        // 添加委托审批人
        String approverIds = currentNode.getApproverIds();
        if (!approverIds.contains(String.valueOf(toUserId))) {
            currentNode.setApproverIds(approverIds + "," + toUserId);
            nodeMapper.updateImApprovalNode(currentNode);
        }

        // 记录委托操作
        ImApprovalRecord record = new ImApprovalRecord();
        record.setApprovalId(approvalId);
        record.setNodeId(currentNode.getId());
        record.setApproverId(fromUserId);
        record.setAction("DELEGATE");
        record.setComment("委托给用户ID: " + toUserId);
        record.setActionTime(LocalDateTime.now());
        recordMapper.insertImApprovalRecord(record);
    }

    /**
     * 根据流程配置创建审批节点
     */
    private void createApprovalNodesFromConfig(Long approvalId, String flowConfig) {
        if (flowConfig == null || flowConfig.isEmpty()) {
            // 使用默认配置
            createDefaultNode(approvalId);
            return;
        }

        try {
            JSONArray nodes = JSON.parseArray(flowConfig);
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject nodeConfig = nodes.getJSONObject(i);
                ImApprovalNode node = new ImApprovalNode();
                node.setApprovalId(approvalId);
                node.setNodeName(nodeConfig.getString("nodeName"));
                node.setNodeType(nodeConfig.getString("nodeType"));

                JSONArray approvers = nodeConfig.getJSONArray("approvers");
                List<String> approverIds = new ArrayList<>();
                for (int j = 0; j < approvers.size(); j++) {
                    approverIds.add(approvers.getString(j));
                }
                node.setApproverIds(String.join(",", approverIds));

                node.setApproveType(nodeConfig.getString("approveType"));
                node.setSortOrder(i + 1);
                node.setStatus("PENDING");
                nodeMapper.insertImApprovalNode(node);
            }
        } catch (Exception e) {
            // JSON解析失败，使用默认配置
            createDefaultNode(approvalId);
        }
    }

    /**
     * 创建默认审批节点
     */
    private void createDefaultNode(Long approvalId) {
        ImApprovalNode node = new ImApprovalNode();
        node.setApprovalId(approvalId);
        node.setNodeName("审批");
        node.setNodeType("APPROVE");
        node.setApproverIds("1");
        node.setApproveType("ANY");
        node.setSortOrder(1);
        node.setStatus("PENDING");
        nodeMapper.insertImApprovalNode(node);
    }

    /**
     * 保存表单数据
     */
    private void saveFormData(Long approvalId, Map<String, Object> formData) {
        if (formData == null || formData.isEmpty()) {
            return;
        }

        List<ImApprovalFormData> dataList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            ImApprovalFormData data = new ImApprovalFormData();
            data.setApprovalId(approvalId);
            data.setFieldName(entry.getKey());
            data.setFieldValue(JSON.toJSONString(entry.getValue()));
            data.setFieldType("TEXT");
            data.setCreateTime(LocalDateTime.now());
            dataList.add(data);
        }
        if (!dataList.isEmpty()) {
            formDataMapper.batchInsertFormData(dataList);
        }
    }

    /**
     * 查找当前用户可处理的节点
     */
    private ImApprovalNode findCurrentNode(List<ImApprovalNode> pendingNodes, Long approverId) {
        for (ImApprovalNode node : pendingNodes) {
            String approverIds = node.getApproverIds();
            if (approverIds != null && approverIds.contains(String.valueOf(approverId))) {
                return node;
            }
        }
        return null;
    }

    /**
     * 处理通过操作
     */
    private void handleApprove(ImApprovalNode currentNode, Long approverId, ImApproval approval) {
        // 判断是否需要所有人审批
        if ("ALL".equals(currentNode.getApproveType())) {
            // 检查是否所有人都已审批
            boolean allApproved = checkAllApproved(currentNode);
            if (allApproved) {
                currentNode.setStatus("APPROVED");
                currentNode.setProcessTime(LocalDateTime.now());
                currentNode.setProcessorId(approverId);
                nodeMapper.updateImApprovalNode(currentNode);
                // 进入下一节点
                proceedToNextNode(approval.getId());
            }
        } else {
            // 任意一人通过即可
            currentNode.setStatus("APPROVED");
            currentNode.setProcessTime(LocalDateTime.now());
            currentNode.setProcessorId(approverId);
            nodeMapper.updateImApprovalNode(currentNode);
            // 进入下一节点
            proceedToNextNode(approval.getId());
        }
    }

    /**
     * 处理驳回操作
     */
    private void handleReject(ImApprovalNode currentNode, Long approverId, ImApproval approval) {
        // 驳回：直接结束流程
        approval.setStatus("REJECTED");
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.updateImApproval(approval);
        // 标记后续节点为已跳过
        skipRemainingNodes(approval.getId());
    }

    /**
     * 处理转交操作
     */
    private void handleTransfer(ImApprovalNode currentNode, Long approverId, ImApproval approval) {
        // 转交需要通过transferApproval方法单独处理
        currentNode.setProcessorId(approverId);
        nodeMapper.updateImApprovalNode(currentNode);
    }

    /**
     * 检查节点是否所有人都已审批
     */
    private boolean checkAllApproved(ImApprovalNode node) {
        String[] approverIds = node.getApproverIds().split(",");
        List<ImApprovalRecord> records = recordMapper.selectRecordsByApprovalId(node.getApprovalId());
        long approvedCount = records.stream()
                .filter(r -> r.getNodeId().equals(node.getId()) && "APPROVE".equals(r.getAction()))
                .count();
        return approvedCount >= approverIds.length;
    }

    /**
     * 进入下一节点
     */
    private void proceedToNextNode(Long approvalId) {
        List<ImApprovalNode> nodes = nodeMapper.selectNodesByApprovalId(approvalId);
        ImApprovalNode nextNode = null;
        boolean hasPendingNode = false;

        for (ImApprovalNode node : nodes) {
            if ("PENDING".equals(node.getStatus())) {
                hasPendingNode = true;
                nextNode = node;
                break;
            }
        }

        if (!hasPendingNode) {
            // 所有节点已审批完成，更新审批状态
            ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
            approval.setStatus("APPROVED");
            approval.setCompletedTime(LocalDateTime.now());
            approval.setUpdateTime(LocalDateTime.now());
            approvalMapper.updateImApproval(approval);
        } else if (nextNode != null) {
            // 更新当前节点
            ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
            approval.setCurrentNodeId(nextNode.getId());
            approval.setUpdateTime(LocalDateTime.now());
            approvalMapper.updateImApproval(approval);
        }
    }

    /**
     * 跳过剩余节点
     */
    private void skipRemainingNodes(Long approvalId) {
        List<ImApprovalNode> nodes = nodeMapper.selectNodesByApprovalId(approvalId);
        for (ImApprovalNode node : nodes) {
            if ("PENDING".equals(node.getStatus())) {
                node.setStatus("SKIPPED");
                nodeMapper.updateImApprovalNode(node);
            }
        }
    }

    /**
     * 构建流程进度信息
     */
    private Map<String, Object> buildProgressInfo(List<ImApprovalNode> nodes, List<ImApprovalRecord> records) {
        Map<String, Object> progress = new HashMap<>();
        int totalNodes = nodes.size();
        int completedNodes = (int) nodes.stream().filter(n -> !"PENDING".equals(n.getStatus())).count();
        progress.put("totalNodes", totalNodes);
        progress.put("completedNodes", completedNodes);
        progress.put("progressPercent", totalNodes > 0 ? (completedNodes * 100 / totalNodes) : 0);

        List<Map<String, Object>> nodeProgress = new ArrayList<>();
        for (ImApprovalNode node : nodes) {
            Map<String, Object> nodeInfo = new HashMap<>();
            nodeInfo.put("nodeId", node.getId());
            nodeInfo.put("nodeName", node.getNodeName());
            nodeInfo.put("nodeType", node.getNodeType());
            nodeInfo.put("status", node.getStatus());
            nodeInfo.put("sortOrder", node.getSortOrder());

            // 添加该节点的审批记录
            List<ImApprovalRecord> nodeRecords = new ArrayList<>();
            for (ImApprovalRecord record : records) {
                if (record.getNodeId().equals(node.getId())) {
                    nodeRecords.add(record);
                }
            }
            nodeInfo.put("records", nodeRecords);

            nodeProgress.add(nodeInfo);
        }
        progress.put("nodes", nodeProgress);

        return progress;
    }
}
