package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImFriend;
import com.ruoyi.web.service.ImFriendService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 好友管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/friend")
public class ImFriendController extends BaseController {

    private String prefix = "im/friend";

    @Autowired
    private ImFriendService imFriendService;

    @RequiresPermissions("im:friend:view")
    @GetMapping()
    public String friend() {
        return prefix + "/friend";
    }

    /**
     * 新增好友关系页面
     */
    @RequiresPermissions("im:friend:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改好友关系页面
     */
    @RequiresPermissions("im:friend:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("friend", imFriendService.selectImFriendById(id));
        return prefix + "/edit";
    }

    /**
     * 查询好友关系列表
     */
    @RequiresPermissions("im:friend:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImFriend imFriend) {
        startPage();
        List<ImFriend> list = imFriendService.selectImFriendList(imFriend);
        return getDataTable(list);
    }

    /**
     * 获取好友统计数据
     */
    @RequiresPermissions("im:friend:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imFriendService.getUserStatistics());
    }

    /**
     * 获取好友关系详细信息
     */
    @RequiresPermissions("im:friend:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imFriendService.selectImFriendById(id));
    }

    /**
     * 根据用户ID查询好友列表
     */
    @RequiresPermissions("im:friend:query")
    @GetMapping("/user/{userId}")
    @ResponseBody
    public AjaxResult getFriendsByUserId(@PathVariable("userId") Long userId) {
        return AjaxResult.success(imFriendService.selectFriendsByUserId(userId));
    }

    /**
     * 按分组查询好友
     */
    @RequiresPermissions("im:friend:query")
    @GetMapping("/user/{userId}/grouped")
    @ResponseBody
    public AjaxResult getFriendsGrouped(@PathVariable("userId") Long userId) {
        return AjaxResult.success(imFriendService.selectFriendsGrouped(userId));
    }

    /**
     * 获取好友分组列表
     */
    @RequiresPermissions("im:friend:query")
    @GetMapping("/groups/{userId}")
    @ResponseBody
    public AjaxResult getFriendGroups(@PathVariable("userId") Long userId) {
        return AjaxResult.success(imFriendService.selectFriendGroups(userId));
    }

    /**
     * 新增好友关系
     */
    @RequiresPermissions("im:friend:add")
    @Log(title = "好友管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImFriend imFriend) {
        return toAjax(imFriendService.insertImFriend(imFriend));
    }

    /**
     * 修改好友关系（API接口方式）
     */
    @RequiresPermissions("im:friend:edit")
    @Log(title = "好友管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ResponseBody
    public AjaxResult editApi(@RequestBody ImFriend imFriend) {
        return toAjax(imFriendService.updateImFriend(imFriend));
    }
    
    /**
     * 修改好友关系（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:friend:edit")
    @Log(title = "好友管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImFriend imFriend) {
        return toAjax(imFriendService.updateImFriend(imFriend));
    }

    /**
     * 删除好友关系
     */
    @RequiresPermissions("im:friend:remove")
    @Log(title = "好友管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imFriendService.deleteImFriendByIds(Convert.toLongArray(ids)));
    }

    /**
     * 删除指定好友关系
     */
    @RequiresPermissions("im:friend:remove")
    @Log(title = "好友管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/user/{userId}/friend/{friendId}")
    @ResponseBody
    public AjaxResult removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return toAjax(imFriendService.deleteFriendByUserAndFriend(userId, friendId));
    }

    /**
     * 导出好友关系列表
     */
    @RequiresPermissions("im:friend:export")
    @Log(title = "好友管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImFriend imFriend) {
        List<ImFriend> list = imFriendService.selectImFriendList(imFriend);
        ExcelUtil<ImFriend> util = new ExcelUtil<>(ImFriend.class);
        util.exportExcel(response, list, "好友关系数据");
    }
}
