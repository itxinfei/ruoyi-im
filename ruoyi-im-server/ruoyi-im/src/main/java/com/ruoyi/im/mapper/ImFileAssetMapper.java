package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImFileAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 文件资产Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImFileAssetMapper extends BaseMapper<ImFileAsset> {

    /**
     * 查询文件资产列表（包含上传者详细信息）
     * 
     * @param uploaderId 上传者用户ID
     * @return 文件资产列表
     */
    List<ImFileAsset> selectFileAssetsWithDetails(@Param("uploaderId") Long uploaderId);

    /**
     * 根据MD5值查询文件资产
     * 
     * @param md5 文件MD5值
     * @return 文件资产
     */
    ImFileAsset selectByMd5(@Param("md5") String md5);

    /**
     * 查询用户上传的文件列表
     * 
     * @param uploaderId 上传者用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 文件资产列表
     */
    List<ImFileAsset> selectUserFiles(@Param("uploaderId") Long uploaderId, 
                                     @Param("startTime") Date startTime, 
                                     @Param("endTime") Date endTime, 
                                     @Param("limit") Integer limit);

    /**
     * 搜索文件（根据文件名）
     * 
     * @param keyword 搜索关键词
     * @param uploaderId 上传者用户ID（可选）
     * @param limit 限制数量
     * @return 文件资产列表
     */
    List<ImFileAsset> searchFiles(@Param("keyword") String keyword, 
                                 @Param("uploaderId") Long uploaderId, 
                                 @Param("limit") Integer limit);

    /**
     * 统计用户上传文件数量
     * 
     * @param uploaderId 上传者用户ID
     * @return 文件数量
     */
    int countUserFiles(@Param("uploaderId") Long uploaderId);

    /**
     * 统计用户上传文件总大小
     * 
     * @param uploaderId 上传者用户ID
     * @return 文件总大小（字节）
     */
    Long sumUserFileSize(@Param("uploaderId") Long uploaderId);

    /**
     * 查询大文件列表（超过指定大小）
     * 
     * @param minSize 最小文件大小（字节）
     * @param limit 限制数量
     * @return 文件资产列表
     */
    List<ImFileAsset> selectLargeFiles(@Param("minSize") Long minSize, @Param("limit") Integer limit);

    /**
     * 查询指定类型的文件列表
     * 
     * @param mimeType MIME类型
     * @param limit 限制数量
     * @return 文件资产列表
     */
    List<ImFileAsset> selectFilesByMimeType(@Param("mimeType") String mimeType, @Param("limit") Integer limit);

    /**
     * 删除过期文件（超过指定天数的文件）
     * 
     * @param days 天数
     * @return 删除数量
     */
    int deleteExpiredFiles(@Param("days") int days);

    /**
     * 查询文件统计信息（按日期分组）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息列表
     */
    List<Object> selectFileStatistics(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 批量查询文件资产
     * 
     * @param fileIds 文件ID列表
     * @return 文件资产列表
     */
    List<ImFileAsset> selectBatchByIds(@Param("fileIds") List<Long> fileIds);

    /**
     * 删除用户上传的所有文件
     * 
     * @param uploaderId 上传者用户ID
     * @return 删除数量
     */
    int deleteByUploaderId(@Param("uploaderId") Long uploaderId);
}