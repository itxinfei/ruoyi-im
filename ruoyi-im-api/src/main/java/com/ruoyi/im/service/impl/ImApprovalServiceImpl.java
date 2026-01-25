package com.ruoyi.im.service.impl;

import cn.hutool.json.JSONUtil;
import com.ruoyi.im.domain.ImApproval;
import com.ruoyi.im.domain.ImApprovalFormData;
import com.ruoyi.im.domain.ImApprovalNode;
import com.ruoyi.im.domain.ImApprovalRecord;
import com.ruoyi.im.domain.ImApprovalTemplate;
import com.ruoyi.im.dto.approval.ConditionBranch;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImApprovalFormDataMapper;
import com.ruoyi.im.mapper.ImApprovalMapper;
import com.ruoyi.im.mapper.ImApprovalNodeMapper;
import com.ruoyi.im.mapper.ImApprovalRecordMapper;
import com.ruoyi.im.mapper.ImApprovalTemplateMapper;
import com.ruoyi.im.service.ImApprovalService;
import com.ruoyi.im.util.ApprovalConditionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ImApprovalServiceImpl.class);

    private final ImApprovalMapper approvalMapper;
    private final ImApprovalTemplateMapper templateMapper;
    private final ImApprovalNodeMapper nodeMapper;
    private final ImApprovalRecordMapper recordMapper;
    private final ImApprovalFormDataMapper formDataMapper;
    private final ApprovalConditionEngine conditionEngine;

    /**
     * 构造器注入依赖
     */
    public ImApprovalServiceImpl(ImApprovalMapper approvalMapper,
                                 ImApprovalTemplateMapper templateMapper,
                                 ImApprovalNodeMapper nodeMapper,
                                 ImApprovalRecordMapper recordMapper,
                                 ImApprovalFormDataMapper formDataMapper,
                                 ApprovalConditionEngine conditionEngine) {
        this.approvalMapper = approvalMapper;
        this.templateMapper = templateMapper;
        this.nodeMapper = nodeMapper;
        this.recordMapper = recordMapper;
        this.formDataMapper = formDataMapper;
        this.conditionEngine = conditionEngine;
    }

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
            formDataMap.put(data.getFieldKey(), data.getFieldValue());
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
     * 支持条件分支配置，根据表单数据动态确定审批路径
     */
    private void createApprovalNodesFromConfig(Long approvalId, String flowConfig) {
        if (flowConfig == null || flowConfig.isEmpty()) {
            // 使用默认配置
            createDefaultNode(approvalId);
            return;
        }

        try {
            cn.hutool.json.JSONObject configObj = JSONUtil.parseObj(flowConfig);

            // 解析条件分支（如果有）
            if (configObj.containsKey("conditionBranches")) {
                createNodesWithConditions(approvalId, configObj);
            } else {
                // 原有逻辑：解析节点数组
                createNodesFromConfigArray(approvalId, configObj);
            }
        } catch (Exception e) {
            log.error("解析流程配置失败，使用默认配置", e);
            createDefaultNode(approvalId);
        }
    }

    /**
     * 根据节点数组创建审批节点（原有逻辑）
     */
    private void createNodesFromConfigArray(Long approvalId, cn.hutool.json.JSONObject configObj) {
        Object nodesObj = configObj.get("nodes");
        if (!(nodesObj instanceof List)) {
            createDefaultNode(approvalId);
            return;
        }

        List<cn.hutool.json.JSONObject> nodes = (List<cn.hutool.json.JSONObject>) nodesObj;
        for (int i = 0; i < nodes.size(); i++) {
            cn.hutool.json.JSONObject nodeConfig = nodes.get(i);
            ImApprovalNode node = new ImApprovalNode();
            node.setApprovalId(approvalId);
            node.setNodeName(nodeConfig.getStr("nodeName"));
            node.setNodeType(nodeConfig.getStr("nodeType"));

            // 获取审批人列表
            cn.hutool.json.JSONArray approversArray = nodeConfig.getJSONArray("approvers");
            if (approversArray != null && !approversArray.isEmpty()) {
                List<String> approvers = new ArrayList<>();
                for (int j = 0; j < approversArray.size(); j++) {
                    approvers.add(approversArray.getStr(j));
                }
                node.setApproverIds(String.join(",", approvers));
            }

            node.setApproveType(nodeConfig.getStr("approveType"));
            node.setSortOrder(i + 1);
            node.setStatus("PENDING");
            nodeMapper.insertImApprovalNode(node);
        }
    }

    /**
     * 根据条件分支创建审批节点
     */
    private void createNodesWithConditions(Long approvalId, cn.hutool.json.JSONObject configObj) {
        cn.hutool.json.JSONArray conditionBranches = configObj.getJSONArray("conditionBranches");
        if (conditionBranches == null || conditionBranches.isEmpty()) {
            createDefaultNode(approvalId);
            return;
        }

        for (int i = 0; i < conditionBranches.size(); i++) {
            cn.hutool.json.JSONObject branchConfig = conditionBranches.getJSONObject(i);
            cn.hutool.json.JSONObject nodeConfig = branchConfig.getJSONObject("node");
            if (nodeConfig == null) {
                continue;
            }

            ImApprovalNode node = new ImApprovalNode();
            node.setApprovalId(approvalId);
            node.setNodeName(nodeConfig.getStr("nodeName"));
            node.setNodeType(nodeConfig.getStr("nodeType"));

            // 获取审批人列表
            cn.hutool.json.JSONArray approversArray = nodeConfig.getJSONArray("approvers");
            if (approversArray != null && !approversArray.isEmpty()) {
                List<String> approvers = new ArrayList<>();
                for (int j = 0; j < approversArray.size(); j++) {
                    approvers.add(approversArray.getStr(j));
                }
                if (!approvers.isEmpty()) {
                    node.setApproverIds(String.join(",", approvers));
                }
            }

            node.setApproveType(nodeConfig.getStr("approveType"));
            node.setSortOrder(i + 1);
            node.setStatus("PENDING");

            // 保存节点（如果是条件分支节点，初始状态为PENDING，后续可能被跳过）
            nodeMapper.insertImApprovalNode(node);
        }

        // 存储条件分支到审批实例（用于后续评估）
        if (conditionBranches != null && !conditionBranches.isEmpty()) {
            // 可以将条件分支信息存储到审批记录或扩展表中
            log.debug("审批 {} 包含 {} 条件分支", approvalId, conditionBranches.size());
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
            data.setFieldKey(entry.getKey());
            data.setFieldValue(JSONUtil.toJsonStr(entry.getValue()));
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
     * 支持条件分支：根据表单数据动态确定审批路径
     */
    private void proceedToNextNode(Long approvalId) {
        List<ImApprovalNode> nodes = nodeMapper.selectNodesByApprovalId(approvalId);
        ImApproval approval = approvalMapper.selectImApprovalById(approvalId);

        // 获取模板配置中的条件分支
        List<ConditionBranch> conditionBranches = getConditionBranchesFromTemplate(approval.getTemplateId());

        // 获取表单数据用于条件评估
        Map<String, Object> formData = loadFormDataAsMap(approvalId);

        // 评估条件分支，跳过不满足条件的节点
        if (!conditionBranches.isEmpty()) {
            skipNodesByConditions(nodes, conditionBranches, formData, approvalId);
        }

        // 重新获取节点列表（可能已被跳过）
        nodes = nodeMapper.selectNodesByApprovalId(approvalId);
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
            approval.setStatus("APPROVED");
            approval.setFinishTime(LocalDateTime.now());
            approval.setUpdateTime(LocalDateTime.now());
            approvalMapper.updateImApproval(approval);
        } else if (nextNode != null) {
            // 更新当前节点
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
     * 从模板中获取条件分支配置
     *
     * @param templateId 模板ID
     * @return 条件分支列表
     */
    private List<ConditionBranch> getConditionBranchesFromTemplate(Long templateId) {
        List<ConditionBranch> conditionBranches = new ArrayList<>();
        try {
            ImApprovalTemplate template = templateMapper.selectImApprovalTemplateById(templateId);
            if (template == null || template.getFlowConfig() == null) {
                return conditionBranches;
            }

            cn.hutool.json.JSONObject configObj = JSONUtil.parseObj(template.getFlowConfig());
            Object branchesObj = configObj.get("conditionBranches");

            if (branchesObj instanceof List) {
                List<?> branches = (List<?>) branchesObj;
                for (Object branchObj : branches) {
                    if (branchObj instanceof Map) {
                        ConditionBranch branch = JSONUtil.toBean(JSONUtil.toJsonStr(branchObj), ConditionBranch.class);
                        if (branch.getEnabled() != null && branch.getEnabled()) {
                            conditionBranches.add(branch);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取条件分支配置失败: templateId={}", templateId, e);
        }
        return conditionBranches;
    }

    /**
     * 加载表单数据为Map格式
     *
     * @param approvalId 审批ID
     * @return 表单数据Map
     */
    private Map<String, Object> loadFormDataAsMap(Long approvalId) {
        Map<String, Object> formData = new HashMap<>();
        try {
            List<ImApprovalFormData> formDataList = formDataMapper.selectFormDataByApprovalId(approvalId);
            for (ImApprovalFormData data : formDataList) {
                String value = data.getFieldValue();
                // 尝试解析JSON值
                if (value != null && (value.startsWith("{") || value.startsWith("["))) {
                    try {
                        Object parsedValue = JSONUtil.parse(value);
                        formData.put(data.getFieldKey(), parsedValue);
                    } catch (Exception e) {
                        formData.put(data.getFieldKey(), value);
                    }
                } else {
                    formData.put(data.getFieldKey(), value);
                }
            }
        } catch (Exception e) {
            log.error("加载表单数据失败: approvalId={}", approvalId, e);
        }
        return formData;
    }

    /**
     * 根据条件分支跳过节点
     *
     * @param nodes             所有节点列表
     * @param conditionBranches 条件分支配置
     * @param formData          表单数据
     * @param approvalId        审批ID
     */
    private void skipNodesByConditions(List<ImApprovalNode> nodes,
                                       List<ConditionBranch> conditionBranches,
                                       Map<String, Object> formData,
                                       Long approvalId) {
        for (ConditionBranch branch : conditionBranches) {
            // 评估条件
            boolean conditionMet = conditionEngine.evaluateCondition(branch, formData);

            // 根据评估结果处理节点跳过
            String targetNodeId = branch.getTargetNodeId();
            if (targetNodeId != null) {
                if (conditionMet) {
                    // 条件满足，跳过其他分支的节点
                    skipOtherBranchNodes(nodes, targetNodeId, approvalId);
                } else {
                    // 条件不满足，跳过目标节点
                    skipTargetNode(nodes, targetNodeId, approvalId);
                }
            }
        }
    }

    /**
     * 跳过其他分支的节点
     *
     * @param nodes        节点列表
     * @param targetNodeId 目标节点ID
     * @param approvalId   审批ID
     */
    private void skipOtherBranchNodes(List<ImApprovalNode> nodes, String targetNodeId, Long approvalId) {
        try {
            Long targetId = Long.parseLong(targetNodeId);
            // 找到目标节点及其排序位置
            Integer targetSortOrder = null;
            for (ImApprovalNode node : nodes) {
                if (node.getId().equals(targetId)) {
                    targetSortOrder = node.getSortOrder();
                    break;
                }
            }

            if (targetSortOrder != null) {
                // 跳过同级其他节点
                for (ImApprovalNode node : nodes) {
                    if ("PENDING".equals(node.getStatus())
                            && node.getSortOrder().equals(targetSortOrder)
                            && !node.getId().equals(targetId)) {
                        node.setStatus("SKIPPED");
                        nodeMapper.updateImApprovalNode(node);
                        log.debug("跳过节点: nodeId={}, 原因: 条件分支路由", node.getId());
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.warn("目标节点ID格式错误: {}", targetNodeId);
        }
    }

    /**
     * 跳过指定节点
     *
     * @param nodes        节点列表
     * @param targetNodeId 目标节点ID
     * @param approvalId   审批ID
     */
    private void skipTargetNode(List<ImApprovalNode> nodes, String targetNodeId, Long approvalId) {
        try {
            Long targetId = Long.parseLong(targetNodeId);
            for (ImApprovalNode node : nodes) {
                if (node.getId().equals(targetId) && "PENDING".equals(node.getStatus())) {
                    node.setStatus("SKIPPED");
                    nodeMapper.updateImApprovalNode(node);
                    log.debug("跳过节点: nodeId={}, 原因: 条件不满足", node.getId());
                    break;
                }
            }
        } catch (NumberFormatException e) {
            log.warn("目标节点ID格式错误: {}", targetNodeId);
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
