package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImFileAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * IM文件资源Mapper接口（Admin模块专用）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImFileAssetMapper {

    /**
     * 查询文件资源列表（支持条件查询和分页）
     *
     * @param imFileAsset 查询条件
     * @return 文件资源列表
     */
    List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset);

    /**
     * 根据ID查询文件资源详情（包含上传者名称）
     *
     * @param id 文件ID
     * @return 文件资源对象
     */
    ImFileAsset selectImFileAssetById(@Param("id") Long id);

    /**
     * 新增文件资源
     *
     * @param imFileAsset 文件资源对象
     * @return 影响行数
     */
    int insertImFileAsset(ImFileAsset imFileAsset);

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
    int deleteImFileAssetById(@Param("id") Long id);

    /**
     * 逻辑删除文件资源（批量）
     *
     * @param ids 文件ID数组
     * @return 影响行数
     */
    int deleteImFileAssetByIds(@Param("ids") Long[] ids);

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
     * @return 统计数据Map，包含：totalCount, imageCount, videoCount, docCount, audioCount, otherCount, totalStorage, todayUploads
     */
    Map<String, Object> getFileStatistics();

    /**
     * 增加文件下载次数
     *
     * @param id 文件ID
     * @return 影响行数
     */
    int incrementDownloadCount(@Param("id") Long id);
}
