package com.ruoyi.im.vo.search;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 全局搜索结果VO
 *
 * @author ruoyi
 */
@Data

public class GlobalSearchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    
    private String keyword;

    /**
     * 消息搜索结果
     */
    
    private List<MessageResult> messages;

    /**
     * 联系人搜索结果
     */
    
    private List<ContactResult> contacts;

    /**
     * 群组搜索结果
     */
    
    private List<GroupResult> groups;

    /**
     * 文件搜索结果
     */
    
    private List<FileResult> files;

    /**
     * 工作台内容搜索结果
     */
    
    private List<WorkbenchResult> workbenchItems;

    /**
     * 各类型结果数量统计
     */
    
    private Map<String, Integer> counts;

    /**
     * 消息搜索结果
     */
    @Data
    
    public static class MessageResult implements Serializable {
        
        private Long id;

        
        private Long conversationId;

        
        private String conversationName;

        
        private Long senderId;

        
        private String senderName;

        
        private String content;

        
        private String messageType;

        
        private String sendTime;
    }

    /**
     * 联系人搜索结果
     */
    @Data
    
    public static class ContactResult implements Serializable {
        
        private Long userId;

        
        private String userName;

        
        private String nickname;

        
        private String avatar;

        
        private String department;

        
        private String position;

        
        private String mobile;

        
        private Boolean isFriend;
    }

    /**
     * 群组搜索结果
     */
    @Data
    
    public static class GroupResult implements Serializable {
        
        private Long groupId;

        
        private String groupName;

        
        private String avatar;

        
        private Integer memberCount;

        
        private String description;
    }

    /**
     * 文件搜索结果
     */
    @Data
    
    public static class FileResult implements Serializable {
        
        private Long fileId;

        
        private String fileName;

        
        private String fileType;

        
        private Long fileSize;

        
        private Long uploaderId;

        
        private String uploaderName;

        
        private Long conversationId;

        
        private String conversationName;

        
        private String uploadTime;

        
        private String fileUrl;
    }

    /**
     * 工作台内容搜索结果
     */
    @Data
    
    public static class WorkbenchResult implements Serializable {
        
        private String contentType;

        
        private Long contentId;

        
        private String title;

        
        private String description;

        
        private String status;

        
        private String createTime;

        
        private String priority;

        
        private String dueTime;
    }
}

