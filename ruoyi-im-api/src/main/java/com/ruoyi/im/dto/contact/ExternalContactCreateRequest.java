package com.ruoyi.im.dto.contact;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 创建外部联系人请求
 */
@Data

public class ExternalContactCreateRequest implements Serializable {

    @NotBlank(message = "姓名不能为空")
    
    private String name;

    
    private Long groupId;

    
    private String company;

    
    private String position;

    
    private String mobile;

    
    private String email;

    
    private String wechat;

    
    private String address;

    
    private String remark;

    
    private String tags;

    
    private String avatar;

    
    private Boolean isStarred = false;
}

