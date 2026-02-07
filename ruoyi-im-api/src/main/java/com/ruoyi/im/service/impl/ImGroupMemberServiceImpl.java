package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.service.ImGroupMemberService;
import com.ruoyi.im.util.ImRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Group member service implementation
 */
@Service
public class ImGroupMemberServiceImpl implements ImGroupMemberService {

    private static final Logger log = LoggerFactory.getLogger(ImGroupMemberServiceImpl.class);

    private final ImGroupMemberMapper imGroupMemberMapper;
    private final ImRedisUtil imRedisUtil;

    /**
     * 构造器注入依赖
     *
     * @param imGroupMemberMapper 群组成员Mapper
     * @param imRedisUtil          Redis工具类
     */
    public ImGroupMemberServiceImpl(ImGroupMemberMapper imGroupMemberMapper,
                                     ImRedisUtil imRedisUtil) {
        this.imGroupMemberMapper = imGroupMemberMapper;
        this.imRedisUtil = imRedisUtil;
    }

    @Override
    public ImGroupMember selectImGroupMemberById(Long id) {
        return imGroupMemberMapper.selectImGroupMemberById(id);
    }

    @Override
    public List<ImGroupMember> selectImGroupMemberList(ImGroupMember imGroupMember) {
        return imGroupMemberMapper.selectImGroupMemberList(imGroupMember);
    }

    @Override
    public int insertImGroupMember(ImGroupMember imGroupMember) {
        int result = imGroupMemberMapper.insertImGroupMember(imGroupMember);
        // 清除群组成员缓存
        if (result > 0 && imGroupMember.getGroupId() != null) {
            imRedisUtil.evictGroupMembers(imGroupMember.getGroupId());
            log.debug("添加群组成员后清除缓存: groupId={}", imGroupMember.getGroupId());
        }
        return result;
    }

    @Override
    public int updateImGroupMember(ImGroupMember imGroupMember) {
        int result = imGroupMemberMapper.updateImGroupMember(imGroupMember);
        // 清除群组成员缓存
        if (result > 0 && imGroupMember.getGroupId() != null) {
            imRedisUtil.evictGroupMembers(imGroupMember.getGroupId());
            log.debug("更新群组成员后清除缓存: groupId={}", imGroupMember.getGroupId());
        }
        return result;
    }

    @Override
    public int deleteImGroupMemberById(Long id) {
        // 先查询获取groupId，用于清除缓存
        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberById(id);
        int result = imGroupMemberMapper.deleteImGroupMemberById(id);
        // 清除群组成员缓存
        if (result > 0 && member != null && member.getGroupId() != null) {
            imRedisUtil.evictGroupMembers(member.getGroupId());
            log.debug("删除群组成员后清除缓存: groupId={}", member.getGroupId());
        }
        return result;
    }

    @Override
    public List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId) {
        // 先尝试从缓存获取
        List<ImGroupMember> cached = imRedisUtil.getGroupMembers(groupId, ImGroupMember.class);
        if (cached != null) {
            log.debug("命中群组成员缓存: groupId={}, memberCount={}", groupId, cached.size());
            return cached;
        }

        // 从数据库查询
        List<ImGroupMember> members = imGroupMemberMapper.selectImGroupMemberListByGroupId(groupId);

        // 缓存查询结果
        if (members != null && !members.isEmpty()) {
            imRedisUtil.cacheGroupMembers(groupId, members);
        }

        return members;
    }

    @Override
    public ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId) {
        return imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
    }
}
