package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 文件分享对象 im_file_share
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImFileShare extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 分享ID */
    private Long id;

    /** 文件ID */
    @Excel(name = "文件ID")
    private Long fileId;

    /** 文件名（关联查询） */
    @Excel(name = "文件名")
    private String fileName;

    /** 文件大小（关联查询） */
    @Excel(name = "文件大小")
    private Long fileSize;

    /** 分享者ID */
    @Excel(name = "分享者ID")
    private Long sharerId;

    /** 分享者用户名（关联查询） */
    @Excel(name = "分享者")
    private String sharerName;

    /** 接收者ID，多个接收者用逗号分隔 */
    @Excel(name = "接收者")
    private String receiverIds;

    /** 接收者名称列表（关联查询） */
    private String receiverNames;

    /** 分享权限: 1=公开, 2=指定人可见, 3=密码保护 */
    @Excel(name = "分享权限")
    private Integer permission;

    /** 访问密码 */
    @Excel(name = "访问密码")
    private String accessPassword;

    /** 允许下载: 0=否, 1=是 */
    @Excel(name = "允许下载")
    private Boolean allowDownload;

    /** 允许预览: 0=否, 1=是 */
    @Excel(name = "允许预览")
    private Boolean allowPreview;

    /** 过期时间 */
    @Excel(name = "过期时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 访问次数 */
    @Excel(name = "访问次数")
    private Integer accessCount;

    /** 下载次数 */
    @Excel(name = "下载次数")
    private Integer downloadCount;

}
