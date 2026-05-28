package com.itniuma.bitinn.mapper.notification;

import com.itniuma.bitinn.pojo.entity.Notification;
import com.itniuma.bitinn.pojo.entity.NotificationCount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知 Mapper
 *
 * MyBatis 数据访问接口，SQL 定义在 NotificationMapper.xml 中。
 * 管理用户通知的存储、查询、已读标记和清理。
 *
 * 通知类型：like（点赞）、favorite（收藏）、comment（评论）、follow（关注）、repost（转发）
 *
 * @author aceFelix
 */
@Mapper
public interface NotificationMapper {

    /** 插入通知 */
    void insert(Notification notification);

    /**
     * 查询用户的通知列表（分页）
     *
     * @param userId 用户 ID
     * @param offset 偏移量
     * @param limit  每页条数
     */
    List<Notification> findByUserId(@Param("userId") Integer userId,
                                    @Param("offset") Integer offset,
                                    @Param("limit") Integer limit);

    /**
     * 查询用户某类型的通知（分页）
     */
    List<Notification> findByUserIdAndType(@Param("userId") Integer userId,
                                            @Param("type") String type,
                                            @Param("offset") Integer offset,
                                            @Param("limit") Integer limit);

    /** 将单条通知标记为已读 */
    void markAsRead(@Param("id") Integer id);

    /** 将用户所有通知标记为已读 */
    void markAllAsRead(@Param("userId") Integer userId);

    /** 将用户某类型通知标记为已读 */
    void markAsReadByType(@Param("userId") Integer userId, @Param("type") String type);

    /** 获取未读通知总数 */
    Integer getUnreadCount(@Param("userId") Integer userId);

    /**
     * 获取各类别未读数
     *
     * 返回 NotificationCount 对象，包含 likeCount、commentCount 等分类统计
     */
    NotificationCount getNotificationCount(@Param("userId") Integer userId);

    /**
     * 更新通知计数（增量 ±1）
     *
     * @param userId 用户 ID
     * @param type   通知类型
     * @param delta  增量值（+1 或 -1）
     */
    void updateNotificationCount(@Param("userId") Integer userId, @Param("type") String type, @Param("delta") Integer delta);

    /** 初始化用户通知计数（新用户首次收到通知时调用） */
    void initNotificationCount(@Param("userId") Integer userId);

    /**
     * 删除旧通知，保留最近 N 条
     *
     * @param userId    用户 ID
     * @param keepCount 保留数量
     */
    void deleteOldNotifications(@Param("userId") Integer userId, @Param("keepCount") Integer keepCount);
}
