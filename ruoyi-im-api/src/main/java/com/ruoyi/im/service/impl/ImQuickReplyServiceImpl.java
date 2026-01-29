package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImQuickReply;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImQuickReplyMapper;
import com.ruoyi.im.service.ImQuickReplyService;
import com.ruoyi.im.vo.quickreply.ImQuickReplyVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 快捷回复服务实现
 *
 * @author ruoyi
 */
@Service
public class ImQuickReplyServiceImpl implements ImQuickReplyService {

    private static final Logger logger = LoggerFactory.getLogger(ImQuickReplyServiceImpl.class);

    private final ImQuickReplyMapper quickReplyMapper;

    public ImQuickReplyServiceImpl(ImQuickReplyMapper quickReplyMapper) {
        this.quickReplyMapper = quickReplyMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImQuickReplyVO createReply(com.ruoyi.im.dto.quickreply.ImQuickReplyCreateRequest request, Long userId) {
        ImQuickReply reply = new ImQuickReply();
        reply.setUserId(userId);
        reply.setContent(request.getContent());
        reply.setCategory(request.getCategory() != null ? request.getCategory() : "custom");
        reply.setTitle(request.getTitle());
        reply.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        reply.setUseCount(0);
        reply.setCreateTime(LocalDateTime.now());
        reply.setUpdateTime(LocalDateTime.now());

        quickReplyMapper.insert(reply);

        logger.info("创建快捷回复成功: userId={}, category={}", userId, reply.getCategory());
        return convertToVO(reply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImQuickReplyVO updateReply(com.ruoyi.im.dto.quickreply.ImQuickReplyUpdateRequest request, Long userId) {
        ImQuickReply reply = quickReplyMapper.selectById(request.getId());
        if (reply == null) {
            throw new BusinessException("快捷回复不存在");
        }

        if (!reply.getUserId().equals(userId) && reply.getUserId() != 0) {
            throw new BusinessException("只能修改自己的快捷回复");
        }

        if (request.getContent() != null) {
            reply.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            reply.setCategory(request.getCategory());
        }
        if (request.getTitle() != null) {
            reply.setTitle(request.getTitle());
        }
        if (request.getSortOrder() != null) {
            reply.setSortOrder(request.getSortOrder());
        }
        reply.setUpdateTime(LocalDateTime.now());

        quickReplyMapper.updateById(reply);

        logger.info("更新快捷回复成功: id={}, userId={}", request.getId(), userId);
        return convertToVO(reply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReply(Long id, Long userId) {
        ImQuickReply reply = quickReplyMapper.selectById(id);
        if (reply == null) {
            throw new BusinessException("快捷回复不存在");
        }

        // 系统默认回复(userId=0)不能删除，用户只能删除自己的
        if (reply.getUserId() == 0) {
            throw new BusinessException("系统默认回复不能删除");
        }

        if (!reply.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的快捷回复");
        }

        quickReplyMapper.deleteById(id);

        logger.info("删除快捷回复成功: id={}, userId={}", id, userId);
    }

    @Override
    public List<ImQuickReplyVO> getUserReplies(Long userId) {
        // 获取用户自己的回复 + 系统默认回复
        List<ImQuickReply> replies = quickReplyMapper.selectList(
                new LambdaQueryWrapper<ImQuickReply>()
                        .and(wrapper -> wrapper.eq(ImQuickReply::getUserId, userId)
                                .or().eq(ImQuickReply::getUserId, 0))
                        .orderByAsc(ImQuickReply::getCategory)
                        .orderByAsc(ImQuickReply::getSortOrder)
                        .orderByDesc(ImQuickReply::getUseCount)
        );

        return replies.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImQuickReplyVO> getRepliesByCategory(Long userId, String category) {
        // 获取指定分类的用户回复 + 系统默认回复
        List<ImQuickReply> replies = quickReplyMapper.selectList(
                new LambdaQueryWrapper<ImQuickReply>()
                        .eq(ImQuickReply::getCategory, category)
                        .and(wrapper -> wrapper.eq(ImQuickReply::getUserId, userId)
                                .or().eq(ImQuickReply::getUserId, 0))
                        .orderByAsc(ImQuickReply::getSortOrder)
                        .orderByDesc(ImQuickReply::getUseCount)
        );

        return replies.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useReply(Long id) {
        quickReplyMapper.incrementUseCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSortOrder(Long userId, List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return;
        }

        for (int i = 0; i < idList.size(); i++) {
            ImQuickReply reply = quickReplyMapper.selectById(idList.get(i));
            if (reply != null && reply.getUserId().equals(userId)) {
                reply.setSortOrder(i);
                reply.setUpdateTime(LocalDateTime.now());
                quickReplyMapper.updateById(reply);
            }
        }

        logger.info("更新排序成功: userId={}, count={}", userId, idList.size());
    }

    /**
     * 转换为VO
     *
     * @param reply 快捷回复实体
     * @return 快捷回复VO
     */
    private ImQuickReplyVO convertToVO(ImQuickReply reply) {
        ImQuickReplyVO vo = new ImQuickReplyVO();
        BeanUtils.copyProperties(reply, vo);
        return vo;
    }
}
