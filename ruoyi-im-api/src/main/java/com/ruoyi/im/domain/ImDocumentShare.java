package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档分享实体
 *
 * @author ruoyi
 */
@Data
@TableName("im_document_share")
public class ImDocumentShare implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分享ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 分享给的用户ID
     */
    private Long sharedToUserId;

    /**
     * 分享者ID
     */
    private Long sharedByUserId;

    /**
     * 权限（view=查看, edit=编辑, admin=管理）
     */
    private String permission;

    /**
     * 是否过期
     */
    private Boolean isExpired;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
