package com.ruoyi.im.dto.contact;

import com.ruoyi.im.common.BasePageRequest;

/**
 * 查询好友请求对象
 * 
 * @author ruoyi
 */
public class ImFriendQueryRequest extends BasePageRequest
{
    /** 关键词 */
    private String keyword;

    public void setKeyword(String keyword) 
    {
        this.keyword = keyword;
    }

    public String getKeyword() 
    {
        return keyword;
    }
}