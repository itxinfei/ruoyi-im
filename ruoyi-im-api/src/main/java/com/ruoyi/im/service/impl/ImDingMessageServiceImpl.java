package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImDingMessage;
import com.ruoyi.im.domain.ImDingReceipt;
import com.ruoyi.im.domain.ImDingTemplate;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.ding.DingSendRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImDingMessageMapper;
import com.ruoyi.im.mapper.ImDingReceiptMapper;
import com.ruoyi.im.mapper.ImDingTemplateMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImDingMessageService;
import com.ruoyi.im.vo.ding.DingDetailVO;
import com.ruoyi.im.vo.ding.DingReceiptVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DING消息服务实现
 */
@Service
public class ImDingMessageServiceImpl implements ImDingMessageService {

    @Autowired
    private ImDingMessageMapper dingMessageMapper;

    @Autowired
    private ImDingReceiptMapper dingReceiptMapper;

    @Autowired
    private ImDingTemplateMapper dingTemplateMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendDing(DingSendRequest request, Long userId) {
        // 验证接收者
        for (Long receiverId : request.getReceiverIds()) {
            ImUser user = userMapper.selectImUserById(receiverId);
            if (user == null) {
                throw new BusinessException("接收者不存在: " + receiverId);
            }
        }

        ImDingMessage ding = new ImDingMessage();
        ding.setSenderId(userId);
        ding.setContent(request.getContent());
        ding.setDingType(request.getDingType());
        ding.setIsUrgent(request.getIsUrgent());
        ding.setScheduleTime(request.getScheduleTime());
        ding.setReceiptRequired(request.getReceiptRequired() ? 1 : 0);
        ding.setAttachment(request.getAttachment());
        ding.setTotalCount(request.getReceiverIds().length);
        ding.setReadCount(0);
        ding.setConfirmedCount(0);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sendTime = request.getScheduleTime() != null ? request.getScheduleTime() : now;

        ding.setSendTime(sendTime);

        if (request.getScheduleTime() != null) {
            ding.setStatus("DRAFT"); // 定时发送，状态为草稿
        } else {
            ding.setStatus("SENT");
            ding.setCreateTime(now);
            ding.setUpdateTime(now);
        }

        dingMessageMapper.insert(ding);

        // 创建回执记录
        for (Long receiverId : request.getReceiverIds()) {
            ImDingReceipt receipt = new ImDingReceipt();
            receipt.setDingId(ding.getId());
            receipt.setReceiverId(receiverId);
            receipt.setConfirmed(0);
            receipt.setCreateTime(now);
            dingReceiptMapper.insert(receipt);

            // 如果立即发送，推送WebSocket通知（通过现有消息推送服务）
            // TODO: 实现DING消息的WebSocket推送
        }

        return ding.getId();
    }

    @Override
    public List<ImDingMessage> getReceivedDingList(Long userId) {
        return dingMessageMapper.selectUserDingList(userId);
    }

    @Override
    public List<ImDingMessage> getSentDingList(Long userId) {
        return dingMessageMapper.selectSentDingList(userId);
    }

    @Override
    public DingDetailVO getDingDetail(Long dingId, Long userId) {
        ImDingMessage ding = dingMessageMapper.selectById(dingId);
        if (ding == null) {
            throw new BusinessException("DING消息不存在");
        }

        // 检查权限（发送者或接收者可以查看）
        if (!ding.getSenderId().equals(userId)) {
            ImDingReceipt receipt = dingReceiptMapper.selectOne(
                    new LambdaQueryWrapper<ImDingReceipt>()
                            .eq(ImDingReceipt::getDingId, dingId)
                            .eq(ImDingReceipt::getReceiverId, userId)
            );
            if (receipt == null) {
                throw new BusinessException("无权限查看此DING");
            }
        }

        DingDetailVO vo = new DingDetailVO();
        BeanUtils.copyProperties(ding, vo);

        // 获取发送者信息
        ImUser sender = userMapper.selectImUserById(ding.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        // 获取回执列表
        List<DingReceiptVO> receipts = getDingReceipts(dingId, userId);
        vo.setReceipts(receipts);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void readDing(Long dingId, Long userId) {
        ImDingReceipt receipt = dingReceiptMapper.selectOne(
                new LambdaQueryWrapper<ImDingReceipt>()
                        .eq(ImDingReceipt::getDingId, dingId)
                        .eq(ImDingReceipt::getReceiverId, userId)
        );
        if (receipt == null) {
            throw new BusinessException("DING消息不存在");
        }

        if (receipt.getReadTime() == null) {
            receipt.setReadTime(LocalDateTime.now());
            dingReceiptMapper.updateById(receipt);

            // 更新已读计数
            ImDingMessage ding = dingMessageMapper.selectById(dingId);
            ding.setReadCount(ding.getReadCount() + 1);
            dingMessageMapper.updateById(ding);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmDing(Long dingId, Long userId, String remark) {
        ImDingReceipt receipt = dingReceiptMapper.selectOne(
                new LambdaQueryWrapper<ImDingReceipt>()
                        .eq(ImDingReceipt::getDingId, dingId)
                        .eq(ImDingReceipt::getReceiverId, userId)
        );
        if (receipt == null) {
            throw new BusinessException("DING消息不存在");
        }

        if (receipt.getConfirmed() == 0) {
            receipt.setConfirmed(1);
            receipt.setConfirmTime(LocalDateTime.now());
            receipt.setRemark(remark);
            dingReceiptMapper.updateById(receipt);

            // 更新已确认计数
            ImDingMessage ding = dingMessageMapper.selectById(dingId);
            ding.setConfirmedCount(ding.getConfirmedCount() + 1);
            dingMessageMapper.updateById(ding);
        }
    }

    @Override
    public List<DingReceiptVO> getDingReceipts(Long dingId, Long userId) {
        ImDingMessage ding = dingMessageMapper.selectById(dingId);
        if (ding == null || !ding.getSenderId().equals(userId)) {
            // 只有发送者可以查看所有回执
            throw new BusinessException("无权限查看回执");
        }

        List<ImDingReceipt> receipts = dingReceiptMapper.selectList(
                new LambdaQueryWrapper<ImDingReceipt>()
                        .eq(ImDingReceipt::getDingId, dingId)
        );

        List<DingReceiptVO> voList = new ArrayList<>();
        for (ImDingReceipt receipt : receipts) {
            DingReceiptVO vo = new DingReceiptVO();
            BeanUtils.copyProperties(receipt, vo);

            ImUser receiver = userMapper.selectImUserById(receipt.getReceiverId());
            if (receiver != null) {
                vo.setReceiverName(receiver.getNickname());
                vo.setReceiverAvatar(receiver.getAvatar());
            }

            voList.add(vo);
        }

        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelScheduledDing(Long dingId, Long userId) {
        ImDingMessage ding = dingMessageMapper.selectById(dingId);
        if (ding == null) {
            throw new BusinessException("DING消息不存在");
        }
        if (!ding.getSenderId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        if (!"DRAFT".equals(ding.getStatus())) {
            throw new BusinessException("只能取消定时的DING");
        }

        ding.setStatus("CANCELLED");
        dingMessageMapper.updateById(ding);
    }

    @Override
    public List<ImDingTemplate> getTemplateList(String category) {
        LambdaQueryWrapper<ImDingTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImDingTemplate::getStatus, "ACTIVE");
        if (category != null && !category.isEmpty()) {
            wrapper.eq(ImDingTemplate::getCategory, category);
        }
        wrapper.orderByAsc(ImDingTemplate::getSortOrder);
        return dingTemplateMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromTemplate(Long templateId, String params, Long userId) {
        ImDingTemplate template = dingTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        // 解析参数并替换模板内容中的占位符
        String content = template.getContent();
        if (params != null && !params.isEmpty()) {
            // 简单实现：假设params格式为 "key1:value1,key2:value2"
            String[] pairs = params.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    content = content.replace("{" + kv[0] + "}", kv[1]);
                }
            }
        }

        DingSendRequest request = new DingSendRequest();
        request.setContent(content);
        request.setDingType("APP");
        request.setIsUrgent(0);
        // 默认发送给所有人（这里需要根据业务调整）
        request.setReceiverIds(new Long[0]);
        request.setReceiptRequired(true);

        return sendDing(request, userId);
    }
}
