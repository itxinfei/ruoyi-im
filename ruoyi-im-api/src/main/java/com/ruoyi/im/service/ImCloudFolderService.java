package com.ruoyi.im.service;

import com.ruoyi.im.dto.cloud.ImCloudFolderCreateRequest;
import com.ruoyi.im.vo.cloud.ImCloudFolderVO;

import java.util.List;

/**
 * 云盘文件夹服务接口
 */
public interface ImCloudFolderService {

    Long createFolder(ImCloudFolderCreateRequest request, Long userId);

    void renameFolder(Long folderId, String newName, Long userId);

    void deleteFolder(Long folderId, Long userId);

    void permanentlyDeleteFolder(Long folderId, Long userId);

    void restoreFolder(Long folderId, Long userId);

    List<ImCloudFolderVO> getFolderList(Long parentId, String ownerType, Long userId);

    List<ImCloudFolderVO> getFolderPath(Long folderId);
}
