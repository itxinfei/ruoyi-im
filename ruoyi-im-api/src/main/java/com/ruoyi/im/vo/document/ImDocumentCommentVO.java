package com.ruoyi.im.vo.document;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档评论VO
 *
 * @author ruoyi
 */
@Data
public class ImDocumentCommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 评论者ID
     */
    private Long userId;

    /**
     * 评论者名称
     */
    private String userName;

    /**
     * 评论者头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 选中的文本
     */
    private String selectedText;

    /**
     * 是否已解决
     */
    private Boolean isResolved;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否可以删除
     */
    private Boolean canDelete;

    /**
     * 回复列表
     */
    private List<ReplyVO> replies;

    @Data
    public static class ReplyVO {
        private Long id;
        private String userName;
        private String content;
        private LocalDateTime createTime;
    }
}
