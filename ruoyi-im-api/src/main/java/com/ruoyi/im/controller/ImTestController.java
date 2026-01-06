package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.ImTestRequest;
import com.ruoyi.im.service.ImTestService;
import com.ruoyi.im.vo.ImTestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * IM测试控制器（示例）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/test")
public class ImTestController {

    @Autowired
    private ImTestService imTestService;

    /**
     * 获取测试数据
     */
    @GetMapping("/list")
    public Result<List<ImTestVO>> getList() {
        List<ImTestVO> list = imTestService.getList();
        return Result.success(list);
    }

    /**
     * 获取详情
     */
    @GetMapping("/{id}")
    public Result<ImTestVO> getById(@PathVariable Long id) {
        ImTestVO vo = imTestService.getById(id);
        return Result.success(vo);
    }

    /**
     * 创建测试数据
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ImTestRequest request) {
        imTestService.create(request);
        return Result.success();
    }

    /**
     * 更新测试数据
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ImTestRequest request) {
        imTestService.update(id, request);
        return Result.success();
    }

    /**
     * 删除测试数据
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        imTestService.delete(id);
        return Result.success();
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("IM服务运行正常", "OK");
    }
}
