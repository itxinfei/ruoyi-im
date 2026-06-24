package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.*;
import com.ruoyi.im.dto.search.GlobalSearchRequest;
import com.ruoyi.im.mapper.*;
import com.ruoyi.im.service.ImSearchService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.search.GlobalSearchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局搜索服务实现
 *
 * @author ruoyi
 */
@Service
public class ImSearchServiceImpl implements ImSearchService {

    @Autowired
    private ImUserMapper userMapper;

    @Autowired
    private ImGroupMapper groupMapper;

    @Autowired
    private ImMessageMapper messageMapper;

    @Autowired
    private ImConversationMapper conversationMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 搜索全部类型 */
    private static final String SEARCH_TYPE_ALL = "all";
    /** 搜索联系人类型 */
    private static final String SEARCH_TYPE_CONTACT = "contact";
    /** 搜索群组类型 */
    private static final String SEARCH_TYPE_GROUP = "group";
    /** 搜索消息类型 */
    private static final String SEARCH_TYPE_MESSAGE = "message";
    /** 群组搜索分页大小 */
    private static final int GROUP_SEARCH_PAGE_SIZE = 20;
    /** 消息搜索分页大小 */
    private static final int MESSAGE_SEARCH_PAGE_SIZE = 50;

    @Override
    public GlobalSearchResultVO search(GlobalSearchRequest request, Long userId) {
        GlobalSearchResultVO result = new GlobalSearchResultVO();
        result.setKeyword(request.getKeyword());
        
        String type = request.getSearchType() == null ? SEARCH_TYPE_ALL : request.getSearchType();
        Map<String, Integer> counts = new HashMap<>();

        // 1. 搜索联系人
        if (SEARCH_TYPE_ALL.equals(type) || SEARCH_TYPE_CONTACT.equals(type)) {
            List<ImUser> users = userMapper.selectImUserByKeyword(request.getKeyword());
            List<GlobalSearchResultVO.ContactResult> contactResults = users.stream().map(user -> {
                GlobalSearchResultVO.ContactResult cr = new GlobalSearchResultVO.ContactResult();
                cr.setUserId(user.getId());
                cr.setUserName(user.getUsername());
                cr.setNickname(user.getNickname());
                cr.setAvatar(user.getAvatar());
                // 这里可以扩展是否为好友的逻辑
                return cr;
            }).collect(Collectors.toList());
            result.setContacts(contactResults);
            counts.put(SEARCH_TYPE_CONTACT, contactResults.size());
        }

        // 2. 搜索群组
        if (SEARCH_TYPE_ALL.equals(type) || SEARCH_TYPE_GROUP.equals(type)) {
            Page<ImGroup> page = new Page<>(1, GROUP_SEARCH_PAGE_SIZE);
            List<ImGroup> groups = groupMapper.selectGroupPage(page, request.getKeyword()).getRecords();
            List<GlobalSearchResultVO.GroupResult> groupResults = groups.stream().map(g -> {
                GlobalSearchResultVO.GroupResult gr = new GlobalSearchResultVO.GroupResult();
                gr.setGroupId(g.getId());
                gr.setGroupName(g.getName());
                gr.setAvatar(g.getAvatar());
                gr.setMemberCount(g.getMemberCount());
                gr.setDescription(g.getDescription());
                return gr;
            }).collect(Collectors.toList());
            result.setGroups(groupResults);
            counts.put("group", groupResults.size());
        }

        // 3. 搜索消息
        if (SEARCH_TYPE_ALL.equals(type) || SEARCH_TYPE_MESSAGE.equals(type)) {
            List<ImMessage> messages = messageMapper.searchMessages(
                    null,
                    request.getKeyword(),
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    0,
                    MESSAGE_SEARCH_PAGE_SIZE
            );
            
            List<GlobalSearchResultVO.MessageResult> messageResults = messages.stream().map(m -> {
                GlobalSearchResultVO.MessageResult mr = new GlobalSearchResultVO.MessageResult();
                mr.setId(m.getId());
                mr.setConversationId(m.getConversationId());
                mr.setContent(m.getContent());
                mr.setMessageType(m.getMessageType());
                mr.setSenderId(m.getSenderId());
                mr.setSendTime(m.getCreateTime() != null ? m.getCreateTime().format(TIME_FORMATTER) : "");
                
                // 补充会话名称和发送者名称（可以通过缓存或批量查询优化，此处简化处理）
                ImConversation conv = conversationMapper.selectById(m.getConversationId());
                if (conv != null) { mr.setConversationName(conv.getName()); }
                
                ImUser sender = userMapper.selectImUserById(m.getSenderId());
                if (sender != null) { mr.setSenderName(sender.getNickname()); }
                
                return mr;
            }).collect(Collectors.toList());
            result.setMessages(messageResults);
            counts.put("message", messageResults.size());
        }

        result.setCounts(counts);
        return result;
    }
}
