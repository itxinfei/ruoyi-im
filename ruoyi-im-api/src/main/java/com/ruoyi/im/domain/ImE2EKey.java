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
 * 端到端加密密钥实体
 *
 * 用于管理用户的公钥，支持端到端加密通信
 *
 * @author ruoyi
 */
@TableName("im_e2e_key")
@Data
public class ImE2EKey implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 公钥(PEM格式) */
    @TableField("public_key")
    private String publicKey;

    /** 密钥版本（用于密钥轮换） */
    @TableField("key_version")
    private Integer keyVersion;

    /** 密钥算法：RSA/ECC */
    @TableField("key_algorithm")
    private String keyAlgorithm;

    /** 密钥长度 */
    @TableField("key_size")
    private Integer keySize;

    /** 是否激活 */
    @TableField("is_active")
    private Boolean isActive;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;
}
