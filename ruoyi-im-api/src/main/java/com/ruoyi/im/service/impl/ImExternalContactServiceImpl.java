package com.ruoyi.im.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImExternalContact;
import com.ruoyi.im.domain.ImExternalContactGroup;
import com.ruoyi.im.dto.contact.ExternalContactCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImExternalContactGroupMapper;
import com.ruoyi.im.mapper.ImExternalContactMapper;
import com.ruoyi.im.service.ImExternalContactService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 外部联系人服务实现
 */
@Service
public class ImExternalContactServiceImpl implements ImExternalContactService {

    @Autowired
    private ImExternalContactMapper contactMapper;

    @Autowired
    private ImExternalContactGroupMapper groupMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createContact(ExternalContactCreateRequest request, Long userId) {
        // 验证分组
        if (request.getGroupId() != null) {
            ImExternalContactGroup group = groupMapper.selectById(request.getGroupId());
            if (group == null || !group.getUserId().equals(userId)) {
                throw new BusinessException("分组不存在");
            }
        }

        ImExternalContact contact = new ImExternalContact();
        BeanUtils.copyProperties(request, contact);
        contact.setUserId(userId);
        contact.setIsStarred(Boolean.TRUE.equals(request.getIsStarred()) ? 1 : 0);
        contact.setSource("MANUAL");
        contactMapper.insert(contact);
        return contact.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContact(Long contactId, ExternalContactCreateRequest request, Long userId) {
        ImExternalContact contact = contactMapper.selectById(contactId);
        if (contact == null) {
            throw new BusinessException("联系人不存在");
        }
        if (!contact.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        // 验证分组
        if (request.getGroupId() != null) {
            ImExternalContactGroup group = groupMapper.selectById(request.getGroupId());
            if (group == null || !group.getUserId().equals(userId)) {
                throw new BusinessException("分组不存在");
            }
        }

        BeanUtils.copyProperties(request, contact);
        if (request.getIsStarred() != null) {
            contact.setIsStarred(Boolean.TRUE.equals(request.getIsStarred()) ? 1 : 0);
        }
        contactMapper.updateById(contact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContact(Long contactId, Long userId) {
        ImExternalContact contact = contactMapper.selectById(contactId);
        if (contact == null) {
            throw new BusinessException("联系人不存在");
        }
        if (!contact.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        contactMapper.deleteById(contactId);
    }

    @Override
    public ImExternalContact getContactDetail(Long contactId, Long userId) {
        ImExternalContact contact = contactMapper.selectById(contactId);
        if (contact == null) {
            throw new BusinessException("联系人不存在");
        }
        if (!contact.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }

        // 设置标签列表
        if (StrUtil.isNotBlank(contact.getTags())) {
            contact.setTagList(contact.getTags().split(","));
        }

        return contact;
    }

    @Override
    public List<ImExternalContact> getContactList(Long userId) {
        List<ImExternalContact> contacts = contactMapper.selectByUserId(userId);
        for (ImExternalContact contact : contacts) {
            if (StrUtil.isNotBlank(contact.getTags())) {
                contact.setTagList(contact.getTags().split(","));
            }
        }
        return contacts;
    }

    @Override
    public List<ImExternalContact> getContactsByGroup(Long groupId, Long userId) {
        // 验证分组
        ImExternalContactGroup group = groupMapper.selectById(groupId);
        if (group == null || !group.getUserId().equals(userId)) {
            throw new BusinessException("分组不存在");
        }

        List<ImExternalContact> contacts = contactMapper.selectByGroupId(userId, groupId);
        for (ImExternalContact contact : contacts) {
            if (StrUtil.isNotBlank(contact.getTags())) {
                contact.setTagList(contact.getTags().split(","));
            }
        }
        return contacts;
    }

    @Override
    public List<ImExternalContact> getStarredContacts(Long userId) {
        List<ImExternalContact> contacts = contactMapper.selectStarredByUserId(userId);
        for (ImExternalContact contact : contacts) {
            if (StrUtil.isNotBlank(contact.getTags())) {
                contact.setTagList(contact.getTags().split(","));
            }
        }
        return contacts;
    }

    @Override
    public List<ImExternalContact> searchContacts(String keyword, Long userId) {
        List<ImExternalContact> contacts = contactMapper.searchByKeyword(userId, keyword);
        for (ImExternalContact contact : contacts) {
            if (StrUtil.isNotBlank(contact.getTags())) {
                contact.setTagList(contact.getTags().split(","));
            }
        }
        return contacts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStarred(Long contactId, Long userId) {
        ImExternalContact contact = contactMapper.selectById(contactId);
        if (contact == null) {
            throw new BusinessException("联系人不存在");
        }
        if (!contact.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        contact.setIsStarred(contact.getIsStarred() == 0 ? 1 : 0);
        contactMapper.updateById(contact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(String name, Long userId) {
        if (StrUtil.isBlank(name)) {
            throw new BusinessException("分组名称不能为空");
        }

        // 检查名称是否重复
        Long count = groupMapper.selectCount(
                new LambdaQueryWrapper<ImExternalContactGroup>()
                        .eq(ImExternalContactGroup::getUserId, userId)
                        .eq(ImExternalContactGroup::getName, name)
        );
        if (count > 0) {
            throw new BusinessException("分组名称已存在");
        }

        ImExternalContactGroup group = new ImExternalContactGroup();
        group.setUserId(userId);
        group.setName(name);
        group.setSortOrder(0);
        groupMapper.insert(group);
        return group.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(Long groupId, String name, Long userId) {
        ImExternalContactGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("分组不存在");
        }
        if (!group.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        if (StrUtil.isNotBlank(name) && !name.equals(group.getName())) {
            // 检查名称是否重复
            Long count = groupMapper.selectCount(
                    new LambdaQueryWrapper<ImExternalContactGroup>()
                            .eq(ImExternalContactGroup::getUserId, userId)
                            .eq(ImExternalContactGroup::getName, name)
                            .ne(ImExternalContactGroup::getId, groupId)
            );
            if (count > 0) {
                throw new BusinessException("分组名称已存在");
            }
            group.setName(name);
        }

        groupMapper.updateById(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId, Long userId) {
        ImExternalContactGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("分组不存在");
        }
        if (!group.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        // 检查分组下是否有联系人
        Long count = contactMapper.selectCount(
                new LambdaQueryWrapper<ImExternalContact>()
                        .eq(ImExternalContact::getGroupId, groupId)
        );
        if (count > 0) {
            throw new BusinessException("分组下还有联系人，无法删除");
        }

        groupMapper.deleteById(groupId);
    }

    @Override
    public List<ImExternalContactGroup> getGroupList(Long userId) {
        return groupMapper.selectByUserId(userId);
    }
}
