package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImCloudFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 云盘文件夹Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImCloudFolderMapper extends BaseMapper<ImCloudFolder> {

    /**
     * 查询用户的文件夹列表
     *
     * @param userId    用户ID
     * @param parentId  父文件夹ID
     * @param ownerType 所有者类型
     * @return 文件夹列表
     */
    List<ImCloudFolder> selectUserFolders(@Param("userId") Long userId,
                                          @Param("parentId") Long parentId,
                                          @Param("ownerType") String ownerType);

    /**
     * 查询文件夹路径
     *
     * @param folderId 文件夹ID
     * @return 路径文件夹列表
     */
    List<ImCloudFolder> selectFolderPath(@Param("folderId") Long folderId);

    /**
     * 统计子文件夹数量
     *
     * @param parentId 父文件夹ID
     * @return 数量
     */
    Integer countSubFolders(@Param("parentId") Long parentId);

    /**
     * 统计文件夹内的文件数量
     *
     * @param folderId 文件夹ID
     * @return 数量
     */
    Integer countFilesInFolder(@Param("folderId") Long folderId);

    /**
     * 检查文件夹名称是否存在
     *
     * @param parentId  父文件夹ID
     * @param folderName 文件夹名称
     * @param ownerId   所有者ID
     * @return 文件夹
     */
    ImCloudFolder selectByNameAndOwner(@Param("parentId") Long parentId,
                                       @Param("folderName") String folderName,
                                       @Param("ownerId") Long ownerId);
}
