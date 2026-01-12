package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImVideoCall;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 视频通话Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImVideoCallMapper {

    /**
     * 查询视频通话
     *
     * @param id 通话ID
     * @return 通话信息
     */
    ImVideoCall selectImVideoCallById(Long id);

    /**
     * 插入视频通话
     *
     * @param call 通话信息
     * @return 影响行数
     */
    int insertImVideoCall(ImVideoCall call);

    /**
     * 更新视频通话
     *
     * @param call 通话信息
     * @return 影响行数
     */
    int updateImVideoCall(ImVideoCall call);

    /**
     * 查询用户的通话记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 通话列表
     */
    java.util.List<ImVideoCall> selectCallsByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
}
