package com.ruoyi.im.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.domain.ImGroupFile;
import com.ruoyi.im.dto.group.ImGroupFileQueryRequest;
import com.ruoyi.im.dto.group.ImGroupFileUpdateRequest;
import com.ruoyi.im.dto.group.ImGroupFileUploadRequest;
import com.ruoyi.im.vo.group.ImGroupFileStatisticsVO;
import com.ruoyi.im.vo.group.ImGroupFileVO;

import java.util.List;

/**
 * 群组文件服务接口
 *
 * @author ruoyi
 */
public interface ImGroupFileService {

    /**
     * 上传群组文件
     *
     * @param request 上传请求
     * @param userId 用户ID
     * @return 文件ID
     */
    Long uploadFile(ImGroupFileUploadRequest request, Long userId);

    /**
     * 分页查询群组文件
     *
     * @param request 查询请求
     * @param userId 用户ID
     * @return 文件列表
     */
    IPage<ImGroupFileVO> getFileList(ImGroupFileQueryRequest request, Long userId);

    /**
     * 获取群组文件统计信息
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 统计信息
     */
    ImGroupFileStatisticsVO getStatistics(Long groupId, Long userId);

    /**
     * 获取群组文件分类列表
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 分类列表
     */
    List<String> getCategories(Long groupId, Long userId);

    /**
     * 更新群组文件信息
     *
     * @param groupFileId 群组文件ID
     * @param request 更新请求
     * @param userId 用户ID
     */
    void updateFile(Long groupFileId, ImGroupFileUpdateRequest request, Long userId);

    /**
     * 删除群组文件
     *
     * @param groupFileId 群组文件ID
     * @param userId 用户ID
     */
    void deleteFile(Long groupFileId, Long userId);

    /**
     * 记录文件下载
     *
     * @param groupFileId 群组文件ID
     * @param userId 用户ID
     * @return 文件URL
     */
    String downloadFile(Long groupFileId, Long userId);

    /**
     * 批量删除群组文件
     *
     * @param groupFileIds 群组文件ID列表
     * @param userId 用户ID
     */
    void batchDeleteFiles(List<Long> groupFileIds, Long userId);

    /**
     * 移动文件到其他分类
     *
     * @param groupFileId 群组文件ID
     * @param category 目标分类
     * @param userId 用户ID
     */
    void moveFile(Long groupFileId, String category, Long userId);
}
