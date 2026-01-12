package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImEmail;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImEmailMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件服务实现
 *
 * @author ruoyi
 */
@Service
public class ImEmailServiceImpl implements ImEmailService {

    private static final Logger log = LoggerFactory.getLogger(ImEmailServiceImpl.class);

    @Autowired
    private ImEmailMapper emailMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Override
    public List<ImEmail> getEmailList(Long userId, String folder) {
        return emailMapper.selectEmailsByUserIdAndFolder(userId, folder);
    }

    @Override
    public ImEmail getEmailDetail(Long emailId, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        // 验证权限：只有发送者或接收者可以查看
        if (!email.getSenderId().equals(userId) && !email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限查看此邮件");
        }
        return email;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendEmail(List<Long> toIds, String subject, String content, Long senderId) {
        if (toIds == null || toIds.isEmpty()) {
            throw new BusinessException("接收者不能为空");
        }

        // 获取发送者信息
        String senderName = String.valueOf(senderId);
        String senderEmail = senderId + "@im.com";

        List<ImEmail> emails = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long toId : toIds) {
            ImEmail email = new ImEmail();
            email.setSenderId(senderId);
            email.setSenderName(senderName);
            email.setSenderEmail(senderEmail);
            email.setReceiverId(toId);
            email.setSubject(subject);
            email.setHtmlContent(content);
            email.setTextContent(stripHtml(content));
            email.setIsRead(false);
            email.setIsDeleted(false);
            email.setIsStarred(false);
            email.setFolder("INBOX");
            email.setAttachmentCount(0);
            email.setSendTime(now);
            email.setReceiveTime(now);
            emails.add(email);
        }

        // 为每个接收者创建邮件记录
        for (ImEmail email : emails) {
            emailMapper.insertEmail(email);
        }

        log.info("发送邮件: senderId={}, toIds={}, count={}", senderId, toIds, emails.size());

        return emails.isEmpty() ? null : emails.get(0).getId();
    }

    @Override
    public Long saveDraft(String subject, String content, Long senderId) {
        ImEmail email = new ImEmail();
        email.setSenderId(senderId);
        email.setReceiverId(senderId); // 草稿的接收者设为自己
        email.setSubject(subject);
        email.setHtmlContent(content);
        email.setTextContent(stripHtml(content));
        email.setIsRead(true);
        email.setIsDeleted(false);
        email.setIsStarred(false);
        email.setFolder("DRAFTS");
        email.setAttachmentCount(0);

        emailMapper.insertEmail(email);

        log.info("保存草稿: senderId={}, emailId={}", senderId, email.getId());

        return email.getId();
    }

    @Override
    public void markAsRead(Long emailId, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        if (!email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作此邮件");
        }

        email.setIsRead(true);
        emailMapper.updateEmail(email);

        log.info("标记已读: emailId={}, userId={}", emailId, userId);
    }

    @Override
    public void markAsStarred(Long emailId, Long userId, boolean starred) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        // 验证权限：只有发送者或接收者可以操作
        if (!email.getSenderId().equals(userId) && !email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作此邮件");
        }

        email.setIsStarred(starred);
        emailMapper.updateEmail(email);

        log.info("标记星标: emailId={}, userId={}, starred={}", emailId, userId, starred);
    }

    @Override
    public void moveToTrash(Long emailId, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        // 验证权限：只有发送者或接收者可以操作
        if (!email.getSenderId().equals(userId) && !email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作此邮件");
        }

        email.setIsDeleted(true);
        email.setFolder("TRASH");
        emailMapper.updateEmail(email);

        log.info("移至垃圾箱: emailId={}, userId={}", emailId, userId);
    }

    @Override
    public void permanentlyDelete(Long emailId, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        // 验证权限：只有发送者或接收者可以删除
        if (!email.getSenderId().equals(userId) && !email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限删除此邮件");
        }

        emailMapper.deleteEmailById(emailId);

        log.info("永久删除: emailId={}, userId={}", emailId, userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return emailMapper.countUnreadByUserId(userId);
    }

    /**
     * 去除HTML标签，获取纯文本
     */
    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").trim();
    }
}
