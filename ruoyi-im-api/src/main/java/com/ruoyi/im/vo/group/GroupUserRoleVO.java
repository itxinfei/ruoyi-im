package com.ruoyi.im.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 群组用户角色信息VO
 *
 * @author ruoyi
 */
@Data
@Builder
@Schema(description = "群组用户角色信息")
public class GroupUserRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否是群主")
    private Boolean isOwner;

    @Schema(description = "是否是管理员或群主")
    private Boolean isAdminOrOwner;
}
