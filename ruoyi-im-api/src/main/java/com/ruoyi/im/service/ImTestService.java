package com.ruoyi.im.service;

import com.ruoyi.im.dto.ImTestRequest;
import com.ruoyi.im.vo.ImTestVO;

import java.util.List;

/**
 * IM测试服务接口（示例）
 *
 * @author ruoyi
 */
public interface ImTestService {

    /**
     * 获取测试数据列表
     */
    List<ImTestVO> getList();

    /**
     * 根据ID获取测试数据
     */
    ImTestVO getById(Long id);

    /**
     * 创建测试数据
     */
    void create(ImTestRequest request);

    /**
     * 更新测试数据
     */
    void update(Long id, ImTestRequest request);

    /**
     * 删除测试数据
     */
    void delete(Long id);
}
