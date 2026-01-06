package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.service.ImConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/im/sessions")
public class ImSessionController {
    @Autowired
    private ImConversationService imConversationService;

    @GetMapping
    public Result<List<ImConversation>> getSessions() {
        return Result.success(imConversationService.list());
    }

    @PostMapping
    public Result<ImConversation> createSession(@RequestBody ImConversation conversation) {
        imConversationService.save(conversation);
        return Result.success(conversation);
    }

    @GetMapping("/{id}")
    public Result<ImConversation> getSession(@PathVariable Long id) {
        return Result.success(imConversationService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSession(@PathVariable Long id) {
        return Result.success(imConversationService.removeById(id));
    }
}
