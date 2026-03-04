package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组实体
 *
 * @author ruoyi
 */
@TableName("im_group")
@Data
public class ImGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 群组ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 群组名称 */
    private String name;

    /** 群头像 */
    private String avatar;

    /** 群主用户ID */
    private Long ownerId;

    /** 群组描述 */
    private String description;

    /** 成员数量限制 */
    @TableField("max_members")
    private Integer maxMembers;

    /** 是否删除：0=否 1=是 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 删除时间 */
    @TableField("deleted_time")
    private LocalDateTime deletedTime;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /** 群二维码URL */
    @TableField("qrcode_url")
    private String qrcodeUrl;

    /** 二维码过期时间 */
    @TableField("qrcode_expire_time")
    private LocalDateTime qrcodeExpireTime;

    /** 允许上传文件：0=否 1=是 */
    @TableField("allow_upload")
    private Integer allowUpload;

    /** 显示成员列表：0=否 1=是 */
    @TableField("show_member_list")
    private Integer showMemberList;

    // ==================== 非数据库字段 ====================

    /** 群公告 */
    @TableField(exist = false)
    private String notice;

    /** 状态 */
    @TableField(exist = false)
    private String status;

    /** 成员数量 */
    @TableField(exist = false)
    private Integer memberCount;

    /** 群组类型 */
    @TableField(exist = false)
    private String type;

    /** 全员禁言：0=否 1=是 */
    @TableField("all_muted")
    private Integer allMuted;

    /** 群主名称 */
    @TableField(exist = false)
    private String ownerName;
}
