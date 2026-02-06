package com.ruoyi.im.vo.group;

import lombok.Data;

/**
 * 群组权限VO
 *
 * @author ruoyi
 */
@Data
public class GroupPermissionVO {

    /**
     * 角色：OWNER-群主, ADMIN-管理员, MEMBER-普通成员
     */
    private String role;

    /**
     * 角色名称（中文）
     */
    private String roleName;

    /**
     * 邀请成员权限：0=禁止, 1=允许
     */
    private Integer canInvite;

    /**
     * 移除成员权限：0=禁止, 1=允许
     */
    private Integer canRemove;

    /**
     * 禁言成员权限：0=禁止, 1=允许
     */
    private Integer canMute;

    /**
     * 发布公告权限：0=禁止, 1=允许
     */
    private Integer canAnnounce;

    /**
     * 上传文件权限：0=禁止, 1=允许
     */
    private Integer canUpload;

    /**
     * 修改群信息权限：0=禁止, 1=允许
     */
    private Integer canEditGroup;

    /**
     * 踢人权限：0=禁止, 1=允许
     */
    private Integer canKick;

    /**
     * 设置管理员权限：0=禁止, 1=允许（仅群主）
     */
    private Integer canSetAdmin;

    /**
     * 解散群组权限：0=禁止, 1=允许（仅群主）
     */
    private Integer canDisband;

    /**
     * 获取角色中文名称
     */
    public static String getRoleName(String role) {
        if ("OWNER".equals(role)) {
            return "群主";
        } else if ("ADMIN".equals(role)) {
            return "管理员";
        } else if ("MEMBER".equals(role)) {
            return "普通成员";
        }
        return "未知";
    }
}
