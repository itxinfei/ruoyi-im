package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImE2EKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 端到端加密密钥Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImE2EKeyMapper extends BaseMapper<ImE2EKey> {

    /**
     * 获取用户的激活公钥
     *
     * @param userId 用户ID
     * @return 公钥实体
     */
    @Select("SELECT * FROM im_e2e_key " +
            "WHERE user_id = #{userId} AND is_active = 1 " +
            "ORDER BY key_version DESC " +
            "LIMIT 1")
    ImE2EKey selectActiveKeyByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的所有公钥
     *
     * @param userId 用户ID
     * @return 公钥列表
     */
    @Select("SELECT * FROM im_e2e_key " +
            "WHERE user_id = #{userId} " +
            "ORDER BY key_version DESC")
    List<ImE2EKey> selectKeysByUserId(@Param("userId") Long userId);

    /**
     * 批量获取多个用户的公钥
     *
     * @param userIds 用户ID列表
     * @return 公钥列表
     */
    List<ImE2EKey> selectActiveKeysByUserIds(@Param("userIds") List<Long> userIds);
}
