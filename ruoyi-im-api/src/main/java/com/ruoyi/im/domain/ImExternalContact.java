package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 外部联系人实体类
 */
@TableName("im_external_contact")
@Data
@Schema(description = "外部联系人")
public class ImExternalContact implements Serializable {

    @Schema(description = "联系人ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    @Schema(description = "所属用户ID")
    private Long userId;

    @TableField("group_id")
    @Schema(description = "分组ID")
    private Long groupId;

    @Schema(description = "联系人姓名")
    private String name;

    @Schema(description = "公司名称")
    private String company;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "微信号")
    private String wechat;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "头像")
    private String avatar;

    @TableField("is_starred")
    @Schema(description = "是否星标")
    private Integer isStarred;

    @Schema(description = "来源")
    private String source;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @Schema(description = "分组名称")
    private String groupName;

    @TableField(exist = false)
    @Schema(description = "标签列表")
    private String[] tagList;
}
