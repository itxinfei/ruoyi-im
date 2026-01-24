package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImFileAsset;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * IM文件资源Service接口（Admin模块专用）
 *
 * @author ruoyi
 * @date 2025-01-17
 */
public interface ImFileAssetService {

    /**
     * 查询文件资源列表
     *
     * @param imFileAsset 查询条件
     * @return 文件资源列表
     */
    List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset);

    /**
     * 根据ID查询文件资源详情
     *
     * @param id 文件ID
     * @return 文件资源对象
     */
    ImFileAsset selectImFileAssetById(Long id);

    /**
     * 新增文件资源
     *
     * @param imFileAsset 文件资源对象
     * @return 影响行数
     */
    int insertImFileAsset(ImFileAsset imFileAsset);

    /**
     * 上传文件并保存记录
     *
     * @param file 上传的文件
     * @param uploaderId 上传者ID
     * @return 文件资源对象
     * @throws Exception 上传异常
     */
    ImFileAsset uploadFile(MultipartFile file, Long uploaderId) throws Exception;

    /**
     * 修改文件资源
     *
     * @param imFileAsset 文件资源对象
     * @return 影响行数
     */
    int updateImFileAsset(ImFileAsset imFileAsset);

    /**
     * 逻辑删除文件资源（单个）
     *
     * @param id 文件ID
     * @return 影响行数
     */
    int deleteImFileAssetById(Long id);

    /**
     * 逻辑删除文件资源（批量）
     *
     * @param ids 文件ID数组
     * @return 影响行数
     */
    int deleteImFileAssetByIds(Long[] ids);

    /**
     * 统计文件总数
     *
     * @param imFileAsset 查询条件（可按上传者筛选）
     * @return 文件总数
     */
    int countTotalFiles(ImFileAsset imFileAsset);

    /**
     * 统计今日上传数量
     *
     * @param imFileAsset 查询条件（可按上传者筛选）
     * @return 今日上传数量
     */
    int countTodayUploads(ImFileAsset imFileAsset);

    /**
     * 统计总存储空间（字节）
     *
     * @return 总存储空间
     */
    Long countTotalStorage();

    /**
     * 清理无效文件（文件路径为空的记录）
     *
     * @return 影响行数
     */
    int cleanInvalidFiles();

    /**
     * 获取文件统计数据
     *
     * @return 统计数据Map
     */
    Map<String, Object> getFileStatistics();

    /**
     * 增加文件下载次数
     *
     * @param id 文件ID
     * @return 影响行数
     */
    int incrementDownloadCount(Long id);

    /**
     * 根据文件扩展名获取文件类型
     *
     * @param extension 文件扩展名
     * @return 文件类型（image/video/document/audio/other）
     */
    String getFileTypeByExtension(String extension);
}
