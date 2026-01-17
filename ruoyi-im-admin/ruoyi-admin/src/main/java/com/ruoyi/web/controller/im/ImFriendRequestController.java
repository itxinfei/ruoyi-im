package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImFriendRequest;
import com.ruoyi.web.service.ImFriendRequestService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 好友申请管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/friendRequest")
public class ImFriendRequestController extends BaseController {

    private String prefix = "im/friendRequest";

    @Autowired
    private ImFriendRequestService imFriendRequestService;

    @RequiresPermissions("im:friendRequest:view")
    @GetMapping()
    public String friendRequest() {
        return prefix + "/friendRequest";
    }

    /**
     * 查询好友申请列表
     */
    @RequiresPermissions("im:friendRequest:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImFriendRequest imFriendRequest) {
        startPage();
        List<ImFriendRequest> list = imFriendRequestService.selectImFriendRequestList(imFriendRequest);
        return getDataTable(list);
    }

    /**
     * 获取好友申请统计数据
     */
    @RequiresPermissions("im:friendRequest:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imFriendRequestService.getFriendRequestStatistics());
    }

    /**
     * 获取好友申请详细信息
     */
    @RequiresPermissions("im:friendRequest:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imFriendRequestService.selectImFriendRequestById(id));
    }

    /**
     * 新增好友申请
     */
    @RequiresPermissions("im:friendRequest:add")
    @Log(title = "好友申请管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(@RequestBody ImFriendRequest imFriendRequest) {
        return toAjax(imFriendRequestService.insertImFriendRequest(imFriendRequest));
    }

    /**
     * 修改好友申请（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:friendRequest:edit")
    @Log(title = "好友申请管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImFriendRequest imFriendRequest) {
        return toAjax(imFriendRequestService.updateImFriendRequest(imFriendRequest));
    }

    /**
     * 处理好友申请
     */
    @RequiresPermissions("im:friendRequest:handle")
    @Log(title = "好友申请管理", businessType = BusinessType.UPDATE)
    @PutMapping("/handle/{id}")
    @ResponseBody
    public AjaxResult handle(@PathVariable("id") Long id, @RequestParam boolean approved) {
        return toAjax(imFriendRequestService.handleFriendRequest(id, approved));
    }

    /**
     * 删除好友申请
     */
    @RequiresPermissions("im:friendRequest:remove")
    @Log(title = "好友申请管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imFriendRequestService.deleteImFriendRequestByIds(Convert.toLongArray(ids)));
    }

    /**
     * 导出好友申请列表
     */
    @RequiresPermissions("im:friendRequest:export")
    @Log(title = "好友申请管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImFriendRequest imFriendRequest) {
        List<ImFriendRequest> list = imFriendRequestService.selectImFriendRequestList(imFriendRequest);
        ExcelUtil<ImFriendRequest> util = new ExcelUtil<>(ImFriendRequest.class);
        util.exportExcel(response, list, "好友申请数据");
    }
}
