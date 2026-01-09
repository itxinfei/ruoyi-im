package com.ruoyi.im.vo.document;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档VO
 *
 * @author ruoyi
 */
@Data
public class ImDocumentVO {

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档类型
     */
    private String docType;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文档状态
     */
    private String status;

    /**
     * 所有者ID
     */
    private Long ownerId;

    /**
     * 所有者名称
     */
    private String ownerName;

    /**
     * 所有者头像
     */
    private String ownerAvatar;

    /**
     * 所属文件夹ID
     */
    private Long folderId;

    /**
     * 是否收藏
     */
    private Boolean isStarred;

    /**
     * 最后修改者ID
     */
    private Long lastModifiedBy;

    /**
     * 最后修改者名称
     */
    private String lastModifiedByName;

    /**
     * 最后修改时间
     */
    private LocalDateTime lastModifiedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 内容预览
     */
    private String preview;

    /**
     * 协作者列表
     */
    private List<CollaboratorInfo> collaborators;

    /**
     * 用户权限（查看共享文档时）
     */
    private String userPermission;

    @Data
    public static class CollaboratorInfo {
        private Long id;
        private String name;
        private String avatar;
        private String permission;
    }
}
