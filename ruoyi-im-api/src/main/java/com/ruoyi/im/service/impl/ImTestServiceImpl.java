package com.ruoyi.im.service.impl;

import com.ruoyi.im.dto.ImTestRequest;
import com.ruoyi.im.service.ImTestService;
import com.ruoyi.im.vo.ImTestVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * IM测试服务实现（示例）
 *
 * @author ruoyi
 */
@Service
public class ImTestServiceImpl implements ImTestService {

    @Override
    public List<ImTestVO> getList() {
        // TODO: 实际业务逻辑
        List<ImTestVO> list = new ArrayList<>();
        ImTestVO vo = new ImTestVO();
        vo.setId(1L);
        vo.setName("测试数据");
        vo.setDescription("这是一个测试数据");
        list.add(vo);
        return list;
    }

    @Override
    public ImTestVO getById(Long id) {
        // TODO: 实际业务逻辑
        ImTestVO vo = new ImTestVO();
        vo.setId(id);
        vo.setName("测试数据");
        vo.setDescription("这是一个测试数据");
        return vo;
    }

    @Override
    public void create(ImTestRequest request) {
        // TODO: 实际业务逻辑
        System.out.println("创建测试数据：" + request.getName());
    }

    @Override
    public void update(Long id, ImTestRequest request) {
        // TODO: 实际业务逻辑
        System.out.println("更新测试数据：" + id + "，名称：" + request.getName());
    }

    @Override
    public void delete(Long id) {
        // TODO: 实际业务逻辑
        System.out.println("删除测试数据：" + id);
    }
}
