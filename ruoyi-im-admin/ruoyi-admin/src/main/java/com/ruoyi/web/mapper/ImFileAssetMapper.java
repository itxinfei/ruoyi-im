package com.ruoyi.web.mapper;

import com.ruoyi.im.domain.ImFileAsset;
import org.apache.ibatis.annotations.Mapper;
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

    List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset);

    ImFileAsset selectImFileAssetById(Long id);

    int insertImFileAsset(ImFileAsset imFileAsset);

    int updateImFileAsset(ImFileAsset imFileAsset);

    int deleteImFileAssetById(Long id);

    int deleteImFileAssetByIds(Long[] ids);

    int countTotalFiles(ImFileAsset imFileAsset);

    int countTodayUploads(ImFileAsset imFileAsset);

    Long countTotalStorage();

    int cleanInvalidFiles();

    Map<String, Object> getFileStatistics();
}
