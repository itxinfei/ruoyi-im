package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.service.ImGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Group Controller
 */
@RestController
@RequestMapping("/api/im/groups")
public class ImGroupController {

    @Autowired
    private ImGroupService imGroupService;

    /**
     * Get group list
     */
    @GetMapping
    public Result<List<ImGroup>> getGroups() {
        return Result.success(imGroupService.list());
    }

    /**
     * Create group
     */
    @PostMapping
    public Result<ImGroup> createGroup(@RequestBody ImGroup group) {
        imGroupService.save(group);
        return Result.success(group);
    }

    /**
     * Update group
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateGroup(@PathVariable Long id, @RequestBody ImGroup group) {
        group.setId(id);
        return Result.success(imGroupService.updateById(group));
    }

    /**
     * Delete group
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteGroup(@PathVariable Long id) {
        return Result.success(imGroupService.removeById(id));
    }

    /**
     * Get group by id
     */
    @GetMapping("/{id}")
    public Result<ImGroup> getGroup(@PathVariable Long id) {
        return Result.success(imGroupService.getById(id));
    }

    /**
     * Add group member
     */
    @PostMapping("/{id}/members")
    public Result<Boolean> addMember(@PathVariable Long id, @RequestParam Long userId) {
        return Result.success(true);
    }

    /**
     * Remove group member
     */
    @DeleteMapping("/{id}/members/{userId}")
    public Result<Boolean> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        return Result.success(true);
    }

    /**
     * Get group members
     */
    @GetMapping("/{id}/members")
    public Result<List<?>> getMembers(@PathVariable Long id) {
        return Result.success(null);
    }
}
