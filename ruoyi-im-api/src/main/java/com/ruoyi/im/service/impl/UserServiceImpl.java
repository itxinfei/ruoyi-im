package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.IUserService;
import com.ruoyi.im.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 閻劍鍩涢張宥呭鐎圭偟骞囬敍鍫濈唨娴滃孩鏆熼幑顔肩氨閿? * 
 * @author ruoyi
 */
@Service
public class UserServiceImpl implements IUserService {
    
    @Autowired
    private ImUserMapper imUserMapper;

    @Override
    public ImUser findByUsername(String username) {
        return imUserMapper.selectImUserByUsername(username);
    }

    @Override
    public List<ImUser> findAll() {
        return imUserMapper.selectImUserList(new ImUser());
    }

    @Override
    public ImUser findById(Long id) {
        return imUserMapper.selectImUserById(id);
    }

    @Override
    public int insert(ImUser user) {
        return imUserMapper.insertImUser(user);
    }

    @Override
    public int updateById(ImUser user) {
        return imUserMapper.updateImUser(user);
    }

    @Override
    public int deleteById(Long id) {
        return imUserMapper.deleteImUserById(id);
    }
}
