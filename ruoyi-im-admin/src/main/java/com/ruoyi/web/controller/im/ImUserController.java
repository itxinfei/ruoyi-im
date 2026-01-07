package com.ruoyi.im.controller;

import com.ruoyi.common.config.annotation.RequiresPermissions;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM用户管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/admin/user")
public class ImUserController extends BaseController {

    @Autowired
    private ImUserMapper imUserMapper;
    @Autowired
    private ImGroupMapper imGroupMapper;
    @Autowired
    private ImFriendMapper imFriendMapper;
    @Autowired
    private ImMessageMapper imMessageMapper;

    /**
     * 获取IM用户列表
     */
    @RequiresPermissions("im:user:list")
    @GetMapping("/list")
    public TableDataInfo<ImUser> list(ImUser imUser) {
        startPage();
        List<ImUser> userList = imUserMapper.selectImUserList(imUser);
        return getDataTable(userList);
    }

    /**
     * 获取IM用户详情
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/query")
    public AjaxResult getInfo(Long userId) {
        return AjaxResult.success(imUserMapper.selectImUserById(userId));
    }

    /**
     * 新增IM用户
     */
    @RequiresPermissions("im:user:add")
    @PostMapping("/add")
    public AjaxResult add(@RequestBody ImUser imUser) {
        return toAjaxResult(imUserMapper.insertImUser(imUser));
    }

    /**
     * 修改IM用户
     */
    @RequiresPermissions("im:user:edit")
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody ImUser imUser) {
        return toAjaxResult(imUserMapper.updateImUser(imUser));
    }

    /**
     * 删除IM用户
     */
    @RequiresPermissions("im:user:remove")
    @DeleteMapping("/remove")
    public AjaxResult remove(Long userId) {
        return toAjaxResult(imUserMapper.deleteImUserById(userId));
    }

    /**
     * 玷取IM群组列表
     */
    @RequiresPermissions("im:group:list")
    @GetMapping("/group/list")
    public TableDataInfo<ImGroup> groupList(ImGroup imGroup) {
        startPage();
        List<ImGroup> groupList = imGroupMapper.selectImGroupList(imGroup);
        return getDataTable(groupList);
    }

    /**
     * 获取IM群组详情
     */
    @RequiresPermissions("im:group:query")
    @GetMapping("/group/query")
    public AjaxResult getGroupInfo(Long groupId) {
        return AjaxResult.success(imGroupMapper.selectImGroupById(groupId));
    }

    /**
     * 新增IM群组
     */
    @RequiresPermissions("im:group:add")
    @PostMapping("/group/add")
    public AjaxResult addGroup(@RequestBody ImGroup imGroup) {
        return toAjaxResult(imGroupMapper.insertImGroup(imGroup));
    }

    /**
     * 修改IM群组
     */
    @RequiresPermissions("im:group:edit")
    @PostMapping("/group/edit")
    public AjaxResult editGroup(@RequestBody ImGroup imGroup) {
        return toAjaxResult(imGroupMapper.updateImGroup(imGroup));
    }

    /**
     * 删除IM群组
     */
    @RequiresPermissions("im:group:remove")
    @DeleteMapping("/group/remove")
    public AjaxResult removeGroup(Long groupId) {
        return toAjaxResult(imGroupMapper.deleteImGroupById(groupId));
    }

    /**
     * 获取IM好友列表
     */
    @RequiresPermissions("im:friend:list")
    @GetMapping("/friend/list")
    public TableDataInfo<ImFriend> friendList(ImFriend imFriend) {
        startPage();
        List<ImFriend> friendList = imFriendMapper.selectImFriendList(imFriend);
        return getDataTable(friendList);
    }

    /**
     * 获取IM好友详情
     */
    @RequiresPermissions("im:friend:query")
    @GetMapping("/friend/query")
    public AjaxResult getFriendInfo(Long friendId) {
        return AjaxResult.success(imFriendMapper.selectImFriendById(friendId));
    }

    /**
     * 新增IM好友关系
     */
    @RequiresPermissions("im:friend:add")
    @PostMapping("/friend/add")
    public AjaxResult addFriend(@RequestBody ImFriend imFriend) {
        return toAjaxResult(imFriendMapper.insertImFriend(imFriend));
    }

    /**
     * 删除IM好友关系
     */
    @RequiresPermissions("im:friend:remove")
    @DeleteMapping("/friend/remove")
    public AjaxResult removeFriend(Long friendId) {
        return toAjaxResult(imFriendMapper.deleteImFriendById(friendId));
    }

    /**
     * 获取IM消息列表
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/message/list")
    public TableDataInfo<ImMessage> messageList(ImMessage imMessage) {
        startPage();
        List<ImMessage> messageList = imMessageMapper.selectImMessageList(imMessage);
        return getDataTable(messageList);
    }

    /**
     * 获取IM消息详情
     */
    @RequiresPermissions("im:message:query")
    @GetMapping("/message/query")
    public AjaxResult getMessageInfo(Long messageId) {
        return AjaxResult.success(imMessageMapper.selectImMessageById(messageId));
    }

    /**
     * 删除IM消息
     */
    @RequiresPermissions("im:message:remove")
    @DeleteMapping("/message/remove")
    public AjaxResult removeMessage(Long messageId) {
        return toAjaxResult(imMessageMapper.deleteImMessageById(messageId));
    }

    /**
     * 批量删除IM消息
     */
    @RequiresPermissions("im:message:batchRemove")
    @DeleteMapping("/message/batchRemove")
    public AjaxResult batchRemoveMessage(Long[] messageIds) {
        return toAjaxResult(imMessageMapper.deleteImMessageByIds(messageIds));
    }
}
