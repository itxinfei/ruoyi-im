package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImExternalContactGroup;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 外部联系人分组Mapper
 */
public interface ImExternalContactGroupMapper extends BaseMapper<ImExternalContactGroup> {

    /**
     * 查询用户的分组列表（带联系人数量）
     */
    @Select("SELECT g.*, " +
            "(SELECT COUNT(*) FROM im_external_contact WHERE group_id = g.id) as contact_count " +
            "FROM im_external_contact_group g " +
            "WHERE g.user_id = #{userId} " +
            "ORDER BY g.sort_order ASC")
    List<ImExternalContactGroup> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的分组数量
     */
    @Select("SELECT COUNT(*) FROM im_external_contact_group WHERE user_id = #{userId}")
    Integer countByUserId(@Param("userId") Long userId);
}
