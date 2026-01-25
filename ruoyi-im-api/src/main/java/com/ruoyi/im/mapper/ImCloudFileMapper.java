package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImCloudFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 云盘文件Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImCloudFileMapper extends BaseMapper<ImCloudFile> {

    /**
     * 查询文件夹内的文件列表
     *
     * @param folderId 文件夹ID
     * @param userId   用户ID
     * @return 文件列表
     */
    List<ImCloudFile> selectByFolderId(@Param("folderId") Long folderId,
                                       @Param("userId") Long userId);

    /**
     * 搜索文件
     *
     * @param userId   用户ID
     * @param keyword  关键词
     * @param fileType 文件类型
     * @return 文件列表
     */
    List<ImCloudFile> searchFiles(@Param("userId") Long userId,
                                  @Param("keyword") String keyword,
                                  @Param("fileType") String fileType);

    /**
     * 统计用户文件总大小
     *
     * @param userId 用户ID
     * @return 总大小
     */
    Long sumUserFileSize(@Param("userId") Long userId);

    /**
     * 统计用户文件数量
     *
     * @param userId 用户ID
     * @return 文件数量
     */
    Integer countUserFiles(@Param("userId") Long userId);

    /**
     * 批量更新文件文件夹
     *
     * @param fileIds        文件ID列表
     * @param targetFolderId 目标文件夹ID
     * @return 更新行数
     */
    int batchUpdateFolder(@Param("fileIds") List<Long> fileIds,
                          @Param("targetFolderId") Long targetFolderId);

    /**
     * 查询最近上传的文件
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 文件列表
     */
    List<ImCloudFile> selectRecentFiles(@Param("userId") Long userId,
                                        @Param("limit") Integer limit);
}
