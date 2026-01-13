package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DING消息服务实现
 * 提供DING消息的发送、接收、确认等功能
 *
 * @author ruoyi
 */
@Service
public class ImDingMessageServiceImpl implements ImDingMessageService {

    private static final Logger log = LoggerFactory.getLogger(ImDingMessageServiceImpl.class);

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
        ding.setRemindInterval(request.getRemindInterval());
        ding.setMaxRemindCount(request.getMaxRemindCount());
        ding.setRemindedCount(0);
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

        // 创建回执记录并推送
        for (Long receiverId : request.getReceiverIds()) {
            ImDingReceipt receipt = new ImDingReceipt();
            receipt.setDingId(ding.getId());
            receipt.setReceiverId(receiverId);
            receipt.setConfirmed(0);
            receipt.setCreateTime(now);
            dingReceiptMapper.insert(receipt);

            // 如果立即发送，推送WebSocket通知
            if ("SENT".equals(ding.getStatus())) {
                sendDingNotification(receiverId, ding, userId);
            }
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

    /**
     * 发送DING消息的WebSocket通知
     *
     * @param receiverId 接收者ID
     * @param ding DING消息
     * @param senderId 发送者ID
     */
    private void sendDingNotification(Long receiverId, ImDingMessage ding, Long senderId) {
        try {
            // 获取发送者信息
            ImUser sender = userMapper.selectImUserById(senderId);
            String senderName = sender != null ? (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : "未知用户";
            String senderAvatar = sender != null ? sender.getAvatar() : "";

            // 构建WebSocket消息
            Map<String, Object> dingMessage = new HashMap<>();
            dingMessage.put("type", "ding");
            dingMessage.put("action", "new_ding");

            Map<String, Object> payload = new HashMap<>();
            payload.put("dingId", ding.getId());
            payload.put("content", ding.getContent());
            payload.put("dingType", ding.getDingType());
            payload.put("isUrgent", ding.getIsUrgent());
            payload.put("receiptRequired", ding.getReceiptRequired());
            payload.put("sendTime", ding.getSendTime() != null ? ding.getSendTime().toString() : LocalDateTime.now().toString());
            payload.put("senderId", senderId);
            payload.put("senderName", senderName);
            payload.put("senderAvatar", senderAvatar);

            dingMessage.put("payload", payload);
            dingMessage.put("timestamp", System.currentTimeMillis());

            // 转换为JSON
            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(dingMessage);

            // 发送给接收者
            Map<Long, Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            Session targetSession = onlineUsers.get(receiverId);

            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getBasicRemote().sendText(messageJson);
                log.info("DING消息已推送给用户: receiverId={}, dingId={}", receiverId, ding.getId());
            } else {
                log.debug("接收者不在线，跳过WebSocket推送: receiverId={}", receiverId);
            }

        } catch (Exception e) {
            log.error("发送DING消息WebSocket通知失败: receiverId={}, dingId={}", receiverId, ding.getId(), e);
        }
    }

    /**
     * 广播DING消息已读状态更新
     *
     * @param dingId DING消息ID
     * @param userId 已读用户ID
     */
    private void broadcastDingReadStatus(Long dingId, Long userId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ding");
            message.put("action", "ding_read");

            Map<String, Object> payload = new HashMap<>();
            payload.put("dingId", dingId);
            payload.put("userId", userId);

            message.put("payload", payload);
            message.put("timestamp", System.currentTimeMillis());

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(message);

            // 广播给所有在线用户
            ImWebSocketEndpoint.broadcastToAllOnline(messageJson);

        } catch (Exception e) {
            log.error("广播DING已读状态失败: dingId={}, userId={}", dingId, userId, e);
        }
    }

    /**
     * 广播DING消息确认状态更新
     *
     * @param dingId DING消息ID
     * @param userId 确认用户ID
     */
    private void broadcastDingConfirmStatus(Long dingId, Long userId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ding");
            message.put("action", "ding_confirmed");

            Map<String, Object> payload = new HashMap<>();
            payload.put("dingId", dingId);
            payload.put("userId", userId);

            message.put("payload", payload);
            message.put("timestamp", System.currentTimeMillis());

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(message);

            // 广播给所有在线用户
            ImWebSocketEndpoint.broadcastToAllOnline(messageJson);

        } catch (Exception e) {
            log.error("广播DING确认状态失败: dingId={}, userId={}", dingId, userId, e);
        }
    }
}
