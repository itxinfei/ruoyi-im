<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.web.mapper.web.ImFriendMapper">

    <resultMap type="com.ruoyi.web.domain.ImFriend" id="ImFriendResult">
        <id property="id"/>
        <result property="userId"/>
        <result property="friendId"/>
        <result property="username"/>
        <result property="nickname"/>
        <result property="avatar"/>
        <result property="mobile"/>
        <result property="groupName"/>
        <result property="remark"/>
        <result property="isDeleted"/>
        <result property="deletedTime"/>
        <result property="createTime"/>
        <result property="updateTime"/>
    </resultMap>

    <!-- 查询分组后的好友列表 -->
    <sql id="selectFriendsGrouped">
        select f.user_id as userId, f.friend_id as friendId, u.username as username,
               u.nickname as nickname, u.avatar as avatar, f.group_name as groupName
        from im_friend f
        left join im_user u on f.user_id = u.id
        where f.is_deleted = 0
        and f.user_id = #{userId}
        and f.group_name != ''
        order by f.create_time desc
    </sql>

    <!-- 查询所有分组名称 -->
    <sql id="selectGroups">
        select distinct group_name from im_friend
        where user_id = #{userId}
          and group_name is not null
          and group_name != ''
        order by group_name asc
    </sql>

    <!-- 计算待处理请求数量 -->
    <sql id="countPendingRequests">
        select count(*) from im_friend_request
        where (status = 'PENDING'
        <if test="userId != null">and to_user_id = #{userId}</if>
    </sql>

    <!-- 计算分组数量 -->
    <sql id="countGroups">
        select count(distinct group_name) from im_friend
        where user_id = #{userId}
          and group_name is not null
          and group_name != ''
    </sql>

    <!-- 查询分组列表（带成员统计） -->
    <sql id="selectGroupList">
        select
            group_name as groupName,
            count(*) as memberCount
        from im_friend
        where user_id = #{userId}
          and is_deleted = 0
          and group_name is not null
          group_name != ''
        group by group_name
        order by memberCount desc
    </sql>

</mapper>
