package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档协作者实体
 * 记录文档的协作关系和权限
 *
 * @author ruoyi
 */
@TableName("im_document_collaborator")
@Data

public class ImDocumentCollaborator implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 协作者ID
     */
    @TableId(type = IdType.AUTO)
    
    private Long id;

    /**
     * 文档ID
     */
    
    @TableField("document_id")
    private Long documentId;

    /**
     * 用户ID
     */
    
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名称（冗余）
     */
    
    @TableField("user_name")
    private String userName;

    /**
     * 用户头像（冗余）
     */
    
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 协作权限：EDIT编辑, COMMENT评论, VIEW查看
     */
    
    @TableField("permission")
    private String permission;

    /**
     * 邀请者ID
     */
    
    @TableField("inviter_id")
    private Long inviterId;

    /**
     * 加入时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("join_time")
    private LocalDateTime joinTime;

    /**
     * 最后活跃时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_active_time")
    private LocalDateTime lastActiveTime;

    /**
     * 在线状态：ONLINE在线, OFFLINE离线
     */
    
    @TableField("online_status")
    private String onlineStatus;

    /**
     * 光标位置（JSON格式）
     */
    
    @TableField("cursor_position")
    private String cursorPosition;

    /**
     * 当前选择范围（JSON格式）
     */
    
    @TableField("selection_range")
    private String selectionRange;

    /**
     * 创建时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}

