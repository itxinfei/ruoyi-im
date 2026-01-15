package com.ruoyi.web.controller.im;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * IM管理页面控制器
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/im")
public class ImManageController
{
    private String prefix = "im";

    /**
     * 消息管理页面
     */
    @GetMapping("/message")
    public String message()
    {
        return prefix + "/message/message";
    }

    /**
     * 会话管理页面
     */
    @GetMapping("/session/page")
    public String session()
    {
        return prefix + "/session/session";
    }
}