package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImExternalContact;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 外部联系人Mapper
 */
public interface ImExternalContactMapper extends BaseMapper<ImExternalContact> {

    /**
     * 查询用户的联系人列表（带分组信息）
     */
    @Select("SELECT c.*, g.name as group_name " +
            "FROM im_external_contact c " +
            "LEFT JOIN im_external_contact_group g ON c.group_id = g.id " +
            "WHERE c.user_id = #{userId} " +
            "ORDER BY c.is_starred DESC, c.create_time DESC")
    List<ImExternalContact> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询分组下的联系人列表
     */
    @Select("SELECT * FROM im_external_contact " +
            "WHERE user_id = #{userId} AND group_id = #{groupId} " +
            "ORDER BY is_starred DESC, create_time DESC")
    List<ImExternalContact> selectByGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

    /**
     * 查询星标联系人
     */
    @Select("SELECT * FROM im_external_contact " +
            "WHERE user_id = #{userId} AND is_starred = 1 " +
            "ORDER BY create_time DESC")
    List<ImExternalContact> selectStarredByUserId(@Param("userId") Long userId);

    /**
     * 搜索联系人
     */
    @Select("SELECT c.*, g.name as group_name " +
            "FROM im_external_contact c " +
            "LEFT JOIN im_external_contact_group g ON c.group_id = g.id " +
            "WHERE c.user_id = #{userId} " +
            "AND (c.name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR c.company LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR c.mobile LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY c.is_starred DESC, c.create_time DESC")
    List<ImExternalContact> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
}
