package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImCloudFolder;
import com.ruoyi.im.dto.cloud.ImCloudFolderCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImCloudFolderMapper;
import com.ruoyi.im.service.ImCloudFolderService;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.vo.cloud.ImCloudFolderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 云盘文件夹服务实现
 */
@Service
public class ImCloudFolderServiceImpl implements ImCloudFolderService {

    private final ImCloudFolderMapper cloudFolderMapper;

    public ImCloudFolderServiceImpl(ImCloudFolderMapper cloudFolderMapper) {
        this.cloudFolderMapper = cloudFolderMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(ImCloudFolderCreateRequest request, Long userId) {
        ImCloudFolder folder = new ImCloudFolder();
        folder.setParentId(request.getParentId());
        folder.setFolderName(request.getFolderName());
        folder.setOwnerType(request.getOwnerType());
        folder.setOwnerId(userId);
        folder.setIsDeleted(false);
        folder.setCreateTime(LocalDateTime.now());
        cloudFolderMapper.insert(folder);
        return folder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFolder(Long id, String name, Long uid) {
        ImCloudFolder f = cloudFolderMapper.selectById(id);
        if (f != null && f.getOwnerId().equals(uid)) { f.setFolderName(name); cloudFolderMapper.updateById(f); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long id, Long uid) {
        ImCloudFolder f = cloudFolderMapper.selectById(id);
        if (f != null && f.getOwnerId().equals(uid)) { f.setIsDeleted(true); f.setDeleteTime(LocalDateTime.now()); cloudFolderMapper.updateById(f); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteFolder(Long id, Long uid) { cloudFolderMapper.deleteById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFolder(Long id, Long uid) {
        ImCloudFolder f = cloudFolderMapper.selectById(id);
        if (f != null && f.getOwnerId().equals(uid)) { f.setIsDeleted(false); f.setDeleteTime(null); cloudFolderMapper.updateById(f); }
    }

    @Override
    public List<ImCloudFolderVO> getFolderList(Long pid, String type, Long uid) {
        LambdaQueryWrapper<ImCloudFolder> w = new LambdaQueryWrapper<>();
        w.eq(ImCloudFolder::getParentId, pid).eq(ImCloudFolder::getOwnerType, type).eq(ImCloudFolder::getOwnerId, uid).eq(ImCloudFolder::getIsDeleted, false);
        return BeanConvertUtil.convertList(cloudFolderMapper.selectList(w), ImCloudFolderVO.class);
    }

    @Override
    public List<ImCloudFolderVO> getFolderPath(Long id) {
        List<ImCloudFolderVO> path = new ArrayList<>();
        ImCloudFolder curr = cloudFolderMapper.selectById(id);
        while (curr != null) {
            path.add(BeanConvertUtil.convert(curr, ImCloudFolderVO.class));
            if (curr.getParentId() == 0) break;
            curr = cloudFolderMapper.selectById(curr.getParentId());
        }
        Collections.reverse(path);
        return path;
    }
}
