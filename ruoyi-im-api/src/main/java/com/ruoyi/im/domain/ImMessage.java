package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM消息实体
 *
 * 用于存储IM系统中的消息信息，包括文本、图片、文件、语音、视频等多种类型的消息
 * 支持消息状态跟踪（发送中、已发送、已送达、已读等）和消息撤回功能
 *
 * @author ruoyi
 */
@TableName("im_message")
@Data
public class ImMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID，关联到im_session表 */
    private Long sessionId;

    /** 发送者ID，关联到im_user表 */
    private Long senderId;

    /** 接收者ID，私聊时为接收用户ID，群聊时为群组ID */
    private Long receiverId;

    /** 消息类型: text=文本, image=图片, file=文件, voice=语音, video=视频, location=位置, system=系统 */
    private String type;

    /** 消息内容，根据类型不同存储不同的数据格式 */
    private String content;

    /** 消息状态: 0=发送中, 1=已发送, 2=已送达, 3=已读, 4=发送失败, 5=已撤回 */
    private Integer status;

    /** 是否撤回: 0=否, 1=是，撤回后的消息不再显示给接收者 */
    private Integer isRevoked;

    /** 撤回时间，记录消息被撤回的时间点 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokeTime;

    /** 发送时间，记录消息发送的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /** 创建时间，记录消息创建的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
