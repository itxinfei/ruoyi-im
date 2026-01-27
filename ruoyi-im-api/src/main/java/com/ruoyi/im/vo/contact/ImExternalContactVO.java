package com.ruoyi.im.vo.contact;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 外部联系人视图对象
 * 用于返回给前端的外部联系人数据，与数据库实体 ImExternalContact 分离
 *
 * @author ruoyi
 */
@Data
public class ImExternalContactVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 联系人ID */
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 分组ID */
    private Long groupId;

    /** 分组名称（关联查询） */
    private String groupName;

    /** 联系人姓名 */
    private String name;

    /** 公司名称 */
    private String company;

    /** 职位 */
    private String position;

    /** 手机号 */
    private String mobile;

    /** 邮箱 */
    private String email;

    /** 微信号 */
    private String wechat;

    /** 地址 */
    private String address;

    /** 备注 */
    private String remark;

    /** 标签 */
    private String tags;

    /** 头像 */
    private String avatar;

    /** 是否星标：0=否, 1=是 */
    private Integer isStarred;

    /** 来源 */
    private String source;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
