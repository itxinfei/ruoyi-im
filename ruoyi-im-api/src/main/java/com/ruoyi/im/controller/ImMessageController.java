package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/im/messages")
public class ImMessageController {
    @Autowired
    private ImMessageService imMessageService;

    @GetMapping
    public Result<List<ImMessage>> getMessages() {
        return Result.success(imMessageService.list());
    }

    @PostMapping
    public Result<ImMessage> sendMessage(@RequestBody ImMessage message) {
        imMessageService.save(message);
        return Result.success(message);
    }

    @GetMapping("/{id}")
    public Result<ImMessage> getMessage(@PathVariable Long id) {
        return Result.success(imMessageService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMessage(@PathVariable Long id) {
        return Result.success(imMessageService.removeById(id));
    }
}
