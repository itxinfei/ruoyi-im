package com.ruoyi.im.dto.contact;

import com.ruoyi.im.common.BasePageRequest;

/**
 * 鏌ヨ濂藉弸璇锋眰瀵硅薄
 * 
 * @author ruoyi
 */
public class ImFriendQueryRequest extends BasePageRequest
{
    /** 鍏抽敭璇?*/
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
