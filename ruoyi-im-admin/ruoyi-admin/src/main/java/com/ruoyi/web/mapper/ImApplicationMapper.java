package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM应用Mapper接口（Admin模块专用）
 */
@Mapper
public interface ImApplicationMapper {

    /**
     * 查询应用列表
     */
    List<ImApplication> selectImApplicationList(ImApplication imApplication);

    /**
     * 根据ID获取应用
     */
    ImApplication selectImApplicationById(Long id);

    /**
     * 新增应用
     */
    int insertImApplication(ImApplication imApplication);

    /**
     * 修改应用
     */
    int updateImApplication(ImApplication imApplication);

    /**
     * 删除应用
     */
    int deleteImApplicationByIds(Long[] ids);

    List<ImApplication> getApplications(@Param("category") String category);

    List<ImApplication> getApplicationsByCategory();

    void setVisibility(@Param("id") Long id, @Param("isVisible") Boolean isVisible);

    void batchSetVisibility(@Param("ids") Long[] ids, @Param("isVisible") Boolean isVisible);

    Map<String, Object> getApplicationStatistics();
}
