package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.config.FileUploadConfig;
import com.ruoyi.im.domain.ImCloudFile;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.dto.cloud.ImCloudFileMoveRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImCloudFileMapper;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.service.ImCloudFileService;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.util.FileUtils;
import com.ruoyi.im.vo.cloud.ImCloudFileVO;
import com.ruoyi.im.vo.cloud.ImCloudStorageQuotaVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 云盘文件服务实现
 */
@Service
public class ImCloudFileServiceImpl implements ImCloudFileService {

    private final ImCloudFileMapper cloudFileMapper;
    private final ImFileAssetMapper fileAssetMapper;
    private final FileUploadConfig fileUploadConfig;

    public ImCloudFileServiceImpl(ImCloudFileMapper cloudFileMapper, ImFileAssetMapper fileAssetMapper, FileUploadConfig fileUploadConfig) {
        this.cloudFileMapper = cloudFileMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.fileUploadConfig = fileUploadConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImCloudFileVO uploadFile(Long folderId, MultipartFile file, Long userId) {
        String name = file.getOriginalFilename();
        String ext = FileUtils.getFileExtension(name);
        String path = "cloud/" + UUID.randomUUID() + "." + ext;
        File target = new File(fileUploadConfig.getAbsoluteUploadPath() + path);
        if (!target.getParentFile().exists()) target.getParentFile().mkdirs();
        try { file.transferTo(target); } catch (IOException e) { throw new BusinessException("保存失败"); }

        ImFileAsset asset = new ImFileAsset();
        asset.setFileName(name); asset.setFileSize(file.getSize()); asset.setFilePath(path); asset.setFileType(ext);
        fileAssetMapper.insert(asset);

        ImCloudFile cloudFile = new ImCloudFile();
        cloudFile.setFolderId(folderId); 
        cloudFile.setFileAssetId(asset.getId()); 
        cloudFile.setFileName(name);
        cloudFile.setUploaderId(userId); 
        cloudFile.setIsDeleted(false); 
        cloudFile.setCreateTime(LocalDateTime.now());
        cloudFileMapper.insert(cloudFile);

        return BeanConvertUtil.convert(cloudFile, ImCloudFileVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFile(Long id, String name, Long uid) {
        ImCloudFile f = cloudFileMapper.selectById(id);
        if (f != null && f.getUploaderId().equals(uid)) { f.setFileName(name); cloudFileMapper.updateById(f); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long id, Long uid) {
        ImCloudFile f = cloudFileMapper.selectById(id);
        if (f != null && f.getUploaderId().equals(uid)) { f.setIsDeleted(true); cloudFileMapper.updateById(f); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteFile(Long id, Long uid) { cloudFileMapper.deleteById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFile(Long id, Long uid) {
        ImCloudFile f = cloudFileMapper.selectById(id);
        if (f != null && f.getUploaderId().equals(uid)) { f.setIsDeleted(false); cloudFileMapper.updateById(f); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFiles(ImCloudFileMoveRequest req, Long uid) {
        if (req.getFileIds() != null) req.getFileIds().forEach(id -> {
            ImCloudFile f = cloudFileMapper.selectById(id);
            if (f != null && f.getUploaderId().equals(uid)) { f.setFolderId(req.getTargetFolderId()); cloudFileMapper.updateById(f); }
        });
    }

    @Override
    public List<ImCloudFileVO> getFileList(Long fid, Long uid) {
        LambdaQueryWrapper<ImCloudFile> w = new LambdaQueryWrapper<>();
        w.eq(fid != null, ImCloudFile::getFolderId, fid).isNull(fid == null, ImCloudFile::getFolderId)
         .eq(ImCloudFile::getUploaderId, uid).eq(ImCloudFile::getIsDeleted, false);
        return BeanConvertUtil.convertList(cloudFileMapper.selectList(w), ImCloudFileVO.class);
    }

    @Override public List<ImCloudFileVO> searchFiles(String kw, String type, Long uid) { return Collections.emptyList(); }
    @Override public List<ImCloudFileVO> getRecentFiles(Long uid, Integer limit) { return Collections.emptyList(); }
    @Override public List<ImCloudFileVO> getRecycleBin(Long uid) { return Collections.emptyList(); }
    @Override public ImCloudStorageQuotaVO getStorageQuota(Long uid) { return new ImCloudStorageQuotaVO(); }
}
