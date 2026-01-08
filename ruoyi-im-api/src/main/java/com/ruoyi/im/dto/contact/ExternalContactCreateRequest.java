package com.ruoyi.im.dto.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 创建外部联系人请求
 */
@Data
@Schema(description = "创建外部联系人请求")
public class ExternalContactCreateRequest implements Serializable {

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "联系人姓名")
    private String name;

    @Schema(description = "分组ID")
    private Long groupId;

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

    @Schema(description = "标签（逗号分隔）")
    private String tags;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "是否星标")
    private Boolean isStarred = false;
}
