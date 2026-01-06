package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/im/users")
public class ImUserController {
    @Autowired
    private ImUserService imUserService;

    @GetMapping
    public Result<List<ImUser>> getUsers() {
        return Result.success(imUserService.list());
    }

    @GetMapping("/{id}")
    public Result<ImUser> getUser(@PathVariable Long id) {
        return Result.success(imUserService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateUser(@PathVariable Long id, @RequestBody ImUser user) {
        user.setId(id);
        return Result.success(imUserService.updateById(user));
    }
}
