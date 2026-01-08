package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 消息搜索结果VO
 *
 * @author ruoyi
 */
@Schema(description = "消息搜索结果")
public class ImMessageSearchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总结果数量 */
    @Schema(description = "总结果数量")
    private Integer total;

    /** 当前页码 */
    @Schema(description = "当前页码")
    private Integer pageNum;

    /** 每页数量 */
    @Schema(description = "每页数量")
    private Integer pageSize;

    /** 总页数 */
    @Schema(description = "总页数")
    private Integer totalPages;

    /** 搜索结果列表 */
    @Schema(description = "搜索结果列表")
    private List<SearchResultItem> items;

    /** 搜索结果项 */
    public static class SearchResultItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 消息ID */
        @Schema(description = "消息ID")
        private Long id;

        /** 会话ID */
        @Schema(description = "会话ID")
        private Long conversationId;

        /** 发送者ID */
        @Schema(description = "发送者ID")
        private Long senderId;

        /** 发送者昵称 */
        @Schema(description = "发送者昵称")
        private String senderName;

        /** 发送者头像 */
        @Schema(description = "发送者头像")
        private String senderAvatar;

        /** 消息类型 */
        @Schema(description = "消息类型")
        private String type;

        /** 消息内容（解密后） */
        @Schema(description = "消息内容")
        private String content;

        /** 高亮内容片段（包含关键词） */
        @Schema(description = "高亮内容片段")
        private String highlightSnippet;

        /** 发送时间 */
        @Schema(description = "发送时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private java.time.LocalDateTime sendTime;

        /** 是否为当前用户发送的消息 */
        @Schema(description = "是否为当前用户发送")
        private Boolean isSelf;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getConversationId() {
            return conversationId;
        }

        public void setConversationId(Long conversationId) {
            this.conversationId = conversationId;
        }

        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getSenderAvatar() {
            return senderAvatar;
        }

        public void setSenderAvatar(String senderAvatar) {
            this.senderAvatar = senderAvatar;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getHighlightSnippet() {
            return highlightSnippet;
        }

        public void setHighlightSnippet(String highlightSnippet) {
            this.highlightSnippet = highlightSnippet;
        }

        public java.time.LocalDateTime getSendTime() {
            return sendTime;
        }

        public void setSendTime(java.time.LocalDateTime sendTime) {
            this.sendTime = sendTime;
        }

        public Boolean getIsSelf() {
            return isSelf;
        }

        public void setIsSelf(Boolean isSelf) {
            this.isSelf = isSelf;
        }
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<SearchResultItem> getItems() {
        return items;
    }

    public void setItems(List<SearchResultItem> items) {
        this.items = items;
    }
}
