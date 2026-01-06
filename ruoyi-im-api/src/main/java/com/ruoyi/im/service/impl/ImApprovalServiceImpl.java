package com.ruoyi.im.service.impl;

import com.alibaba.fastjson.JSON;
import com.ruoyi.im.domain.ImApproval;
import com.ruoyi.im.domain.ImApprovalFormData;
import com.ruoyi.im.domain.ImApprovalNode;
import com.ruoyi.im.domain.ImApprovalRecord;
import com.ruoyi.im.domain.ImApprovalTemplate;
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
            throw new RuntimeException("审批模板不存在");
        }

        // 2. 创建审批实例
        ImApproval approval = new ImApproval();
        approval.setTemplateId(templateId);
        approval.setTitle(title);
        approval.setApplicantId(applicantId);
        approval.setStatus("PENDING");
        approval.setCreateTime(LocalDateTime.now());
        approvalMapper.insertImApproval(approval);

        Long approvalId = approval.getId();

        // 3. 解析流程配置并创建审批节点
        String flowConfig = template.getFlowConfig();
        createApprovalNodes(approvalId, flowConfig);

        // 4. 保存表单数据
        saveFormData(approvalId, formData);

        return approvalId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processApproval(Long approvalId, String action, String comment, Long approverId) {
        // 1. 查询审批实例
        ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
        if (approval == null) {
            throw new RuntimeException("审批不存在");
        }

        if (!"PENDING".equals(approval.getStatus())) {
            throw new RuntimeException("审批已处理");
        }

        // 2. 查询当前待处理节点
        List<ImApprovalNode> pendingNodes = nodeMapper.selectNodesByApprovalId(approvalId);
        ImApprovalNode currentNode = null;
        for (ImApprovalNode node : pendingNodes) {
            if ("PENDING".equals(node.getStatus()) && node.getApproverIds().contains(approverId.toString())) {
                currentNode = node;
                break;
            }
        }

        if (currentNode == null) {
            throw new RuntimeException("无权限操作或审批已处理");
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

        // 4. 更新节点状态
        if ("APPROVE".equals(action)) {
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
                    proceedToNextNode(approvalId);
                }
            } else {
                // 任意一人通过即可
                currentNode.setStatus("APPROVED");
                currentNode.setProcessTime(LocalDateTime.now());
                currentNode.setProcessorId(approverId);
                nodeMapper.updateImApprovalNode(currentNode);
                // 进入下一节点
                proceedToNextNode(approvalId);
            }
        } else if ("REJECT".equals(action)) {
            // 驳回：直接结束流程
            approval.setStatus("REJECTED");
            approvalMapper.updateImApproval(approval);
            // 标记后续节点为已跳过
            skipRemainingNodes(approvalId);
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
        result.put("formData", formDataList);

        // 4. 查询审批节点
        List<ImApprovalNode> nodes = nodeMapper.selectNodesByApprovalId(approvalId);
        result.put("nodes", nodes);

        // 5. 查询审批记录
        List<ImApprovalRecord> records = recordMapper.selectRecordsByApprovalId(approvalId);
        result.put("records", records);

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
        // TODO: 实现已审批列表查询
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApproval(Long approvalId, Long applicantId) {
        ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
        if (approval == null) {
            throw new RuntimeException("审批不存在");
        }
        if (!approval.getApplicantId().equals(applicantId)) {
            throw new RuntimeException("无权限操作");
        }
        if (!"PENDING".equals(approval.getStatus())) {
            throw new RuntimeException("审批已处理，无法撤回");
        }

        approval.setStatus("CANCELLED");
        approvalMapper.updateImApproval(approval);
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
     * 创建审批节点
     */
    private void createApprovalNodes(Long approvalId, String flowConfig) {
        // TODO: 解析flowConfig JSON，创建审批节点
        // 这里简化处理，创建默认节点
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
        boolean hasPendingNode = false;

        for (ImApprovalNode node : nodes) {
            if ("PENDING".equals(node.getStatus())) {
                hasPendingNode = true;
                break;
            }
        }

        if (!hasPendingNode) {
            // 所有节点已审批完成，更新审批状态
            ImApproval approval = approvalMapper.selectImApprovalById(approvalId);
            approval.setStatus("APPROVED");
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
}
