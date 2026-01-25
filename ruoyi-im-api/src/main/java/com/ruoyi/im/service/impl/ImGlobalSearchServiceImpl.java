package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.dto.search.GlobalSearchRequest;
import com.ruoyi.im.domain.*;
import com.ruoyi.im.mapper.*;
import com.ruoyi.im.service.ImGlobalSearchService;
import com.ruoyi.im.util.MessageEncryptionUtil;
import com.ruoyi.im.vo.search.GlobalSearchResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局搜索服务实现
 * 整合各个模块的搜索功能，提供统一搜索入口
 *
 * @author ruoyi
 */
@Service
public class ImGlobalSearchServiceImpl implements ImGlobalSearchService {

    private static final Logger log = LoggerFactory.getLogger(ImGlobalSearchServiceImpl.class);

    private static final int MAX_RESULTS_PER_TYPE = 20;

    @Autowired
    private ImMessageMapper messageMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Autowired
    private ImFriendMapper friendMapper;

    @Autowired
    private ImGroupMapper groupMapper;

    @Autowired
    private ImGroupMemberMapper groupMemberMapper;

    @Autowired
    private ImFileAssetMapper fileAssetMapper;

    @Autowired
    private ImTodoItemMapper todoItemMapper;

    @Autowired
    private ImTaskMapper taskMapper;

    @Autowired
    private ImDocumentMapper documentMapper;

    @Autowired
    private ImScheduleEventMapper scheduleEventMapper;

    @Autowired
    private ImConversationMapper conversationMapper;

    @Autowired
    private MessageEncryptionUtil encryptionUtil;

    @Override
    public GlobalSearchResultVO globalSearch(GlobalSearchRequest request) {
        if (!StringUtils.hasText(request.getKeyword())) {
            return new GlobalSearchResultVO();
        }

        String keyword = request.getKeyword().trim();
        GlobalSearchResultVO result = new GlobalSearchResultVO();
        result.setKeyword(keyword);

        // 根据搜索类型决定搜索哪些内容
        String searchType = request.getSearchType();
        if ("all".equals(searchType) || searchType == null) {
            // 搜索所有类型
            result.setMessages(searchMessagesList(keyword, null));
            result.setContacts(searchContactsList(keyword, null));
            result.setGroups(searchGroupsList(keyword, null));
            result.setFiles(searchFilesList(keyword, null));
            result.setWorkbenchItems(searchWorkbenchList(keyword, null));
        } else {
            switch (searchType) {
                case "message":
                    result.setMessages(searchMessagesList(keyword, null));
                    break;
                case "contact":
                    result.setContacts(searchContactsList(keyword, null));
                    break;
                case "group":
                    result.setGroups(searchGroupsList(keyword, null));
                    break;
                case "file":
                    result.setFiles(searchFilesList(keyword, null));
                    break;
                case "workbench":
                    result.setWorkbenchItems(searchWorkbenchList(keyword, null));
                    break;
                default:
                    result.setMessages(searchMessagesList(keyword, null));
                    break;
            }
        }

        // 统计各类型结果数量
        Map<String, Integer> counts = new HashMap<>();
        counts.put("messages", result.getMessages() != null ? result.getMessages().size() : 0);
        counts.put("contacts", result.getContacts() != null ? result.getContacts().size() : 0);
        counts.put("groups", result.getGroups() != null ? result.getGroups().size() : 0);
        counts.put("files", result.getFiles() != null ? result.getFiles().size() : 0);
        counts.put("workbench", result.getWorkbenchItems() != null ? result.getWorkbenchItems().size() : 0);
        result.setCounts(counts);

        return result;
    }

    @Override
    public GlobalSearchResultVO searchMessages(String keyword, Long userId) {
        GlobalSearchResultVO result = new GlobalSearchResultVO();
        result.setKeyword(keyword);
        result.setMessages(searchMessagesList(keyword, userId));
        return result;
    }

    @Override
    public GlobalSearchResultVO searchContacts(String keyword, Long userId) {
        GlobalSearchResultVO result = new GlobalSearchResultVO();
        result.setKeyword(keyword);
        result.setContacts(searchContactsList(keyword, userId));
        return result;
    }

    @Override
    public GlobalSearchResultVO searchGroups(String keyword, Long userId) {
        GlobalSearchResultVO result = new GlobalSearchResultVO();
        result.setKeyword(keyword);
        result.setGroups(searchGroupsList(keyword, userId));
        return result;
    }

    @Override
    public GlobalSearchResultVO searchFiles(String keyword, Long userId) {
        GlobalSearchResultVO result = new GlobalSearchResultVO();
        result.setKeyword(keyword);
        result.setFiles(searchFilesList(keyword, userId));
        return result;
    }

    @Override
    public GlobalSearchResultVO searchWorkbench(String keyword, Long userId) {
        GlobalSearchResultVO result = new GlobalSearchResultVO();
        result.setKeyword(keyword);
        result.setWorkbenchItems(searchWorkbenchList(keyword, userId));
        return result;
    }

    /**
     * 搜索消息列表
     */
    private List<GlobalSearchResultVO.MessageResult> searchMessagesList(String keyword, Long userId) {
        try {
            // 搜索消息内容（使用模糊查询）
            LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(ImMessage::getContent, "%" + keyword + "%")
                .eq(ImMessage::getIsRevoked, 0)
                .orderByDesc(ImMessage::getCreateTime)
                .last("LIMIT " + MAX_RESULTS_PER_TYPE);

            List<ImMessage> messages = messageMapper.selectList(queryWrapper);

            // 获取会话信息用于显示
            Map<Long, ImConversation> conversationMap = new HashMap<>();
            List<Long> conversationIds = messages.stream()
                .map(ImMessage::getConversationId)
                .distinct()
                .collect(Collectors.toList());
            if (!conversationIds.isEmpty()) {
                List<ImConversation> conversations = conversationMapper.selectBatchIds(conversationIds);
                conversationMap.putAll(conversations.stream()
                    .collect(Collectors.toMap(ImConversation::getId, c -> c)));
            }

            // 获取用户信息
            Set<Long> userIds = new HashSet<>();
            userIds.addAll(messages.stream().map(ImMessage::getSenderId).collect(Collectors.toList()));
            userIds.addAll(conversationMap.values().stream()
                .map(ImConversation::getTargetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

            Map<Long, ImUser> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<ImUser> users = userMapper.selectImUserListByIds(new ArrayList<>(userIds));
                userMap.putAll(users.stream()
                    .collect(Collectors.toMap(ImUser::getId, u -> u)));
            }

            return messages.stream()
                .limit(MAX_RESULTS_PER_TYPE)
                .map(msg -> {
                    GlobalSearchResultVO.MessageResult result = new GlobalSearchResultVO.MessageResult();
                    result.setId(msg.getId());
                    result.setConversationId(msg.getConversationId());

                    // 获取会话名称
                    ImConversation conv = conversationMap.get(msg.getConversationId());
                    if (conv != null) {
                        result.setConversationName(conv.getType().equals("GROUP") ?
                            conv.getName() : getPrivateConversationName(conv, userMap));
                    }

                    result.setSenderId(msg.getSenderId());
                    ImUser sender = userMap.get(msg.getSenderId());
                    result.setSenderName(sender != null ?
                        (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : "未知");

                    // 解密并高亮消息内容
                    String content = decryptContent(msg.getContent());
                    result.setContent(highlightKeyword(content, keyword));
                    result.setMessageType(msg.getMessageType());
                    result.setSendTime(formatTime(msg.getCreateTime()));
                    return result;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索消息失败: keyword={}", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * 搜索联系人列表
     */
    private List<GlobalSearchResultVO.ContactResult> searchContactsList(String keyword, Long userId) {
        try {
            // 搜索用户（按用户名、昵称、手机号）
            LambdaQueryWrapper<ImUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.and(wrapper -> wrapper
                .like(ImUser::getUsername, keyword)
                .or()
                .like(ImUser::getNickname, keyword)
                .or()
                .like(ImUser::getMobile, keyword)
            )
            .orderByDesc(ImUser::getCreateTime)
            .last("LIMIT " + MAX_RESULTS_PER_TYPE);

            List<ImUser> users = userMapper.selectImUserByKeyword(keyword);

            // 获取好友关系
            Set<Long> friendIds = new HashSet<>();
            if (userId != null) {
                ImFriend friendQuery = new ImFriend();
                friendQuery.setUserId(userId);
                friendQuery.setIsDeleted(0);
                List<ImFriend> friends = friendMapper.selectImFriendList(friendQuery);
                friendIds = new HashSet<>();
                for (ImFriend friend : friends) {
                    friendIds.add(friend.getFriendId());
                }
            }

            Set<Long> finalFriendIds = friendIds;
            return users.stream()
                .limit(MAX_RESULTS_PER_TYPE)
                .map(user -> {
                    GlobalSearchResultVO.ContactResult result = new GlobalSearchResultVO.ContactResult();
                    result.setUserId(user.getId());
                    result.setUserName(user.getUsername());
                    result.setNickname(user.getNickname());
                    result.setAvatar(user.getAvatar());
                    result.setDepartment(user.getDepartment());
                    result.setPosition(user.getPosition());
                    result.setMobile(user.getMobile());
                    result.setIsFriend(finalFriendIds.contains(user.getId()));
                    return result;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索联系人失败: keyword={}", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * 搜索群组列表
     */
    private List<GlobalSearchResultVO.GroupResult> searchGroupsList(String keyword, Long userId) {
        try {
            // 搜索群组（按群组名称）
            ImGroup queryWrapper = new ImGroup();
            queryWrapper.setName(keyword);
            List<ImGroup> groups = groupMapper.selectImGroupList(queryWrapper);

            return groups.stream()
                .limit(MAX_RESULTS_PER_TYPE)
                .map(group -> {
                    GlobalSearchResultVO.GroupResult result = new GlobalSearchResultVO.GroupResult();
                    result.setGroupId(group.getId());
                    result.setGroupName(group.getName());
                    result.setAvatar(group.getAvatar());
                    result.setMemberCount(group.getMemberCount());
                    result.setDescription(group.getDescription());
                    return result;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索群组失败: keyword={}", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * 搜索文件列表
     */
    private List<GlobalSearchResultVO.FileResult> searchFilesList(String keyword, Long userId) {
        try {
            // 搜索文件资产
            LambdaQueryWrapper<ImFileAsset> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(ImFileAsset::getFileName, keyword)
                .orderByDesc(ImFileAsset::getCreateTime)
                .last("LIMIT " + MAX_RESULTS_PER_TYPE);

            List<ImFileAsset> files = fileAssetMapper.selectList(queryWrapper);

            // 获取用户信息
            Set<Long> uploaderIds = files.stream()
                .map(ImFileAsset::getUploaderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            Map<Long, ImUser> userMap = new HashMap<>();
            if (!uploaderIds.isEmpty()) {
                List<ImUser> users = userMapper.selectImUserListByIds(new ArrayList<>(uploaderIds));
                userMap.putAll(users.stream()
                    .collect(Collectors.toMap(ImUser::getId, u -> u)));
            }

            return files.stream()
                .limit(MAX_RESULTS_PER_TYPE)
                .map(file -> {
                    GlobalSearchResultVO.FileResult result = new GlobalSearchResultVO.FileResult();
                    result.setFileId(file.getId());
                    result.setFileName(file.getFileName());
                    result.setFileType(file.getFileType());
                    result.setFileSize(file.getFileSize());
                    result.setUploaderId(file.getUploaderId());
                    ImUser uploader = userMap.get(file.getUploaderId());
                    result.setUploaderName(uploader != null ?
                        (uploader.getNickname() != null ? uploader.getNickname() : uploader.getUsername()) : "未知");
                    result.setUploadTime(formatTime(file.getCreateTime()));
                    return result;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索文件失败: keyword={}", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * 搜索工作台内容
     */
    private List<GlobalSearchResultVO.WorkbenchResult> searchWorkbenchList(String keyword, Long userId) {
        List<GlobalSearchResultVO.WorkbenchResult> results = new ArrayList<>();

        try {
            // 搜索待办事项
            ImTodoItem todoQuery = new ImTodoItem();
            todoQuery.setTitle(keyword);
            List<ImTodoItem> todos = todoItemMapper.selectImTodoItemList(todoQuery);

            todos.forEach(todo -> {
                GlobalSearchResultVO.WorkbenchResult result = new GlobalSearchResultVO.WorkbenchResult();
                result.setContentType("todo");
                result.setContentId(todo.getId());
                result.setTitle(highlightKeyword(todo.getTitle(), keyword));
                result.setDescription(todo.getDescription());
                result.setStatus(todo.getStatus());
                result.setCreateTime(formatTime(todo.getCreateTime()));
                result.setDueTime(formatTime(todo.getDueDate()));
                results.add(result);
            });
        } catch (Exception e) {
            log.error("搜索待办事项失败: keyword={}", keyword, e);
        }

        try {
            // 搜索任务
            ImTask taskQuery = new ImTask();
            taskQuery.setTitle(keyword);
            List<ImTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<ImTask>()
                .like(ImTask::getTitle, keyword)
                .or().like(ImTask::getDescription, keyword)
                .last("LIMIT 5"));

            tasks.forEach(task -> {
                GlobalSearchResultVO.WorkbenchResult result = new GlobalSearchResultVO.WorkbenchResult();
                result.setContentType("task");
                result.setContentId(task.getId());
                result.setTitle(highlightKeyword(task.getTitle(), keyword));
                result.setDescription(task.getDescription());
                result.setStatus(task.getStatus());
                result.setPriority(task.getPriority() != null ? task.getPriority().toString() : "");
                result.setCreateTime(formatTime(task.getCreateTime()));
                result.setDueTime(formatTime(task.getDueDate()));
                results.add(result);
            });
        } catch (Exception e) {
            log.error("搜索任务失败: keyword={}", keyword, e);
        }

        try {
            // 搜索文档
            LambdaQueryWrapper<ImDocument> docQuery = new LambdaQueryWrapper<>();
            docQuery.like(ImDocument::getTitle, keyword)
                .or().like(ImDocument::getContent, keyword)
                .last("LIMIT 5");
            List<ImDocument> docs = documentMapper.selectList(docQuery);

            docs.forEach(doc -> {
                GlobalSearchResultVO.WorkbenchResult result = new GlobalSearchResultVO.WorkbenchResult();
                result.setContentType("document");
                result.setContentId(doc.getId());
                result.setTitle(highlightKeyword(doc.getTitle(), keyword));
                result.setDescription(truncateText(doc.getContent(), 100));
                result.setStatus(doc.getStatus());
                result.setCreateTime(formatTime(doc.getCreateTime()));
                results.add(result);
            });
        } catch (Exception e) {
            log.error("搜索文档失败: keyword={}", keyword, e);
        }

        try {
            // 搜索日程
            LambdaQueryWrapper<ImScheduleEvent> scheduleQuery = new LambdaQueryWrapper<>();
            scheduleQuery.like(ImScheduleEvent::getTitle, keyword)
                .or().like(ImScheduleEvent::getDescription, keyword)
                .last("LIMIT 5");
            List<ImScheduleEvent> schedules = scheduleEventMapper.selectList(scheduleQuery);

            schedules.forEach(schedule -> {
                GlobalSearchResultVO.WorkbenchResult result = new GlobalSearchResultVO.WorkbenchResult();
                result.setContentType("schedule");
                result.setContentId(schedule.getId());
                result.setTitle(highlightKeyword(schedule.getTitle(), keyword));
                result.setDescription(schedule.getDescription());
                result.setCreateTime(formatTime(schedule.getCreateTime()));
                results.add(result);
            });
        } catch (Exception e) {
            log.error("搜索日程失败: keyword={}", keyword, e);
        }

        return results.stream()
            .limit(MAX_RESULTS_PER_TYPE)
            .collect(Collectors.toList());
    }

    /**
     * 解密消息内容
     */
    private String decryptContent(String content) {
        if (content == null) {
            return "";
        }
        try {
            return encryptionUtil.decryptMessage(content);
        } catch (Exception e) {
            return content;
        }
    }

    /**
     * 高亮关键词
     */
    private String highlightKeyword(String text, String keyword) {
        if (text == null || !StringUtils.hasText(keyword)) {
            return text;
        }
        return text.replace(keyword, "<mark>" + keyword + "</mark>");
    }

    /**
     * 截断文本
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 获取私聊会话名称
     */
    private String getPrivateConversationName(ImConversation conv, Map<Long, ImUser> userMap) {
        if ("PRIVATE".equals(conv.getType())) {
            ImUser targetUser = userMap.get(conv.getTargetId());
            if (targetUser != null) {
                return targetUser.getNickname() != null ? targetUser.getNickname() : targetUser.getUsername();
            }
        }
        return conv.getName();
    }

    /**
     * 格式化时间
     */
    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 格式化日期
     */
    private String formatTime(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
