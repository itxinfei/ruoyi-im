package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImOfflineMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 离线消息 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImOfflineMessageMapper extends BaseMapper<ImOfflineMessage> {

    /**
     * 查询用户的离线消息
     *
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 离线消息列表
     */
    @Select("SELECT * FROM im_offline_message WHERE user_id = #{userId} AND status = 0 " +
            "ORDER BY create_time ASC LIMIT #{limit}")
    List<ImOfflineMessage> selectUnpulledByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询用户的离线消息数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM im_offline_message WHERE user_id = #{userId} AND status = 0")
    int countUnpulledByUserId(@Param("userId") Long userId);

    /**
     * 标记消息为已拉取
     *
     * @param ids 消息ID列表
     * @param pullTime 拉取时间
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE im_offline_message SET status = 1, pull_time = #{pullTime} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int markAsPulled(@Param("ids") List<Long> ids, @Param("pullTime") LocalDateTime pullTime);

    /**
     * 清理已拉取的旧消息（保留7天）
     *
     * @param beforeTime 时间点
     * @return 删除行数
     */
    @Select("DELETE FROM im_offline_message WHERE status = 1 AND pull_time &lt; #{beforeTime}")
    int cleanOldPulledMessages(@Param("beforeTime") LocalDateTime beforeTime);
}
