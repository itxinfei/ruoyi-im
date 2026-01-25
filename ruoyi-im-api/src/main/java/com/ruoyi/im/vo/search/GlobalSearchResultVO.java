package com.ruoyi.im.vo.search;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "全局搜索结果")
public class GlobalSearchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词")
    private String keyword;

    /**
     * 消息搜索结果
     */
    @Schema(description = "消息搜索结果")
    private List<MessageResult> messages;

    /**
     * 联系人搜索结果
     */
    @Schema(description = "联系人搜索结果")
    private List<ContactResult> contacts;

    /**
     * 群组搜索结果
     */
    @Schema(description = "群组搜索结果")
    private List<GroupResult> groups;

    /**
     * 文件搜索结果
     */
    @Schema(description = "文件搜索结果")
    private List<FileResult> files;

    /**
     * 工作台内容搜索结果
     */
    @Schema(description = "工作台内容搜索结果")
    private List<WorkbenchResult> workbenchItems;

    /**
     * 各类型结果数量统计
     */
    @Schema(description = "各类型结果数量统计")
    private Map<String, Integer> counts;

    /**
     * 消息搜索结果
     */
    @Data
    @Schema(description = "消息搜索结果")
    public static class MessageResult implements Serializable {
        @Schema(description = "消息ID")
        private Long id;

        @Schema(description = "会话ID")
        private Long conversationId;

        @Schema(description = "会话名称")
        private String conversationName;

        @Schema(description = "发送者ID")
        private Long senderId;

        @Schema(description = "发送者名称")
        private String senderName;

        @Schema(description = "消息内容（高亮）")
        private String content;

        @Schema(description = "消息类型")
        private String messageType;

        @Schema(description = "发送时间")
        private String sendTime;
    }

    /**
     * 联系人搜索结果
     */
    @Data
    @Schema(description = "联系人搜索结果")
    public static class ContactResult implements Serializable {
        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名称")
        private String userName;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "部门")
        private String department;

        @Schema(description = "职位")
        private String position;

        @Schema(description = "手机号")
        private String mobile;

        @Schema(description = "是否为好友")
        private Boolean isFriend;
    }

    /**
     * 群组搜索结果
     */
    @Data
    @Schema(description = "群组搜索结果")
    public static class GroupResult implements Serializable {
        @Schema(description = "群组ID")
        private Long groupId;

        @Schema(description = "群组名称")
        private String groupName;

        @Schema(description = "群组头像")
        private String avatar;

        @Schema(description = "成员数量")
        private Integer memberCount;

        @Schema(description = "群组简介")
        private String description;
    }

    /**
     * 文件搜索结果
     */
    @Data
    @Schema(description = "文件搜索结果")
    public static class FileResult implements Serializable {
        @Schema(description = "文件ID")
        private Long fileId;

        @Schema(description = "文件名")
        private String fileName;

        @Schema(description = "文件类型")
        private String fileType;

        @Schema(description = "文件大小")
        private Long fileSize;

        @Schema(description = "上传者ID")
        private Long uploaderId;

        @Schema(description = "上传者名称")
        private String uploaderName;

        @Schema(description = "所属会话ID")
        private Long conversationId;

        @Schema(description = "所属会话名称")
        private String conversationName;

        @Schema(description = "上传时间")
        private String uploadTime;

        @Schema(description = "文件URL")
        private String fileUrl;
    }

    /**
     * 工作台内容搜索结果
     */
    @Data
    @Schema(description = "工作台内容搜索结果")
    public static class WorkbenchResult implements Serializable {
        @Schema(description = "内容类型：task/todo/document/schedule")
        private String contentType;

        @Schema(description = "内容ID")
        private Long contentId;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "内容描述")
        private String description;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "创建时间")
        private String createTime;

        @Schema(description = "优先级")
        private String priority;

        @Schema(description = "截止时间")
        private String dueTime;
    }
}
