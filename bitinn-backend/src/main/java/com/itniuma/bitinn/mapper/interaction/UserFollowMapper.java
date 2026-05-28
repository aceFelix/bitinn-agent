package com.itniuma.bitinn.mapper.interaction;

import com.itniuma.bitinn.pojo.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户关注 Mapper
 *
 * MyBatis 数据访问接口，SQL 定义在 UserFollowMapper.xml 中。
 * 关注为 Toggle 模式：已关注则取消（delete），未关注则添加（insert）。
 *
 * @author aceFelix
 */
@Mapper
public interface UserFollowMapper {

    /** 关注用户 */
    void insert(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);

    /** 取消关注 */
    void delete(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);

    /** 查询是否已关注 */
    UserFollow findByFollowerAndFollowing(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);

    /** 查询用户关注的所有用户 ID 列表 */
    List<Integer> findFollowingIdsByFollowerId(@Param("followerId") Integer followerId);

    /** 统计粉丝数 */
    Integer countFollowers(@Param("userId") Integer userId);

    /** 统计关注数 */
    Integer countFollowing(@Param("userId") Integer userId);
}
