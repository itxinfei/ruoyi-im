package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImEmail;
import com.ruoyi.web.mapper.ImEmailMapper;
import com.ruoyi.web.service.ImEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 邮件Service实现
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Service
public class ImEmailServiceImpl implements ImEmailService {

    @Autowired
    private ImEmailMapper emailMapper;

    @Override
    public List<ImEmail> selectImEmailList(ImEmail imEmail) {
        return emailMapper.selectImEmailList(imEmail);
    }

    @Override
    public ImEmail selectImEmailById(Long id) {
        return emailMapper.selectImEmailById(id);
    }

    @Override
    public int insertImEmail(ImEmail imEmail) {
        return emailMapper.insertImEmail(imEmail);
    }

    @Override
    public int updateImEmail(ImEmail imEmail) {
        return emailMapper.updateImEmail(imEmail);
    }

    @Override
    public int deleteImEmailByIds(Long[] ids) {
        return emailMapper.deleteImEmailByIds(ids);
    }

    @Override
    public Map<String, Object> getEmailStatistics() {
        return emailMapper.getEmailStatistics();
    }

    @Override
    public int markAsRead(Long id) {
        ImEmail email = new ImEmail();
        email.setId(id);
        email.setIsRead(1);
        return emailMapper.updateImEmail(email);
    }

    @Override
    public int batchMarkAsRead(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return emailMapper.batchUpdateIsRead(ids, 1);
    }

    @Override
    public int markAsUnread(Long id) {
        ImEmail email = new ImEmail();
        email.setId(id);
        email.setIsRead(0);
        return emailMapper.updateImEmail(email);
    }

    @Override
    public int markAsStarred(Long id) {
        ImEmail email = new ImEmail();
        email.setId(id);
        email.setIsStarred(1);
        return emailMapper.updateImEmail(email);
    }

    @Override
    public int unmarkAsStarred(Long id) {
        ImEmail email = new ImEmail();
        email.setId(id);
        email.setIsStarred(0);
        return emailMapper.updateImEmail(email);
    }

    @Override
    public int moveToTrash(Long id) {
        ImEmail email = new ImEmail();
        email.setId(id);
        email.setFolder("TRASH");
        return emailMapper.updateImEmail(email);
    }

    @Override
    public int batchMoveToTrash(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return emailMapper.batchUpdateFolder(ids, "TRASH");
    }

    @Override
    public int restoreFromTrash(Long id) {
        ImEmail email = emailMapper.selectImEmailById(id);
        if (email != null) {
            email.setFolder("INBOX");
            return emailMapper.updateImEmail(email);
        }
        return 0;
    }

    @Override
    public int permanentDelete(Long id) {
        return emailMapper.deleteImEmailById(id);
    }

    @Override
    public int clearTrash() {
        return emailMapper.deleteEmailsByFolder("TRASH");
    }

    @Override
    public List<ImEmail> getEmailsByFolder(String folder) {
        ImEmail query = new ImEmail();
        query.setFolder(folder);
        return emailMapper.selectImEmailList(query);
    }

    @Override
    public int getUnreadCount() {
        ImEmail query = new ImEmail();
        query.setIsRead(0);
        query.setFolder("INBOX");
        return emailMapper.countEmails(query);
    }
}
