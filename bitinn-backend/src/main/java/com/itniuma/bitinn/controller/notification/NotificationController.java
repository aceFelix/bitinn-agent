package com.itniuma.bitinn.controller.notification;

import com.itniuma.bitinn.pojo.entity.Notification;
import com.itniuma.bitinn.pojo.entity.NotificationCount;
import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 *
 * 管理用户通知的查询和已读标记。
 * 通知类型包括：点赞、评论、收藏、关注、系统通知等。
 * 支持按类型筛选通知和批量已读操作。
 *
 * @author aceFelix
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取通知列表（分页）
     *
     * @param token    JWT Token，从 Authorization Header 提取
     * @param page     页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return 通知列表，按时间倒序
     */
    @GetMapping
    public Result<List<Notification>> getNotifications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Integer userId = extractUserId(token);
        List<Notification> notifications = notificationService.getNotifications(userId, page, pageSize);
        return Result.success(notifications);
    }

    /**
     * 获取某类型通知（如 like、comment、follow 等）
     */
    @GetMapping("/type/{type}")
    public Result<List<Notification>> getNotificationsByType(
            @RequestHeader("Authorization") String token,
            @PathVariable String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Integer userId = extractUserId(token);
        List<Notification> notifications = notificationService.getNotificationsByType(userId, type, page, pageSize);
        return Result.success(notifications);
    }

    /**
     * 获取未读数量统计（按类型分组）
     *
     * 前端用于显示通知红点数字
     */
    @GetMapping("/count")
    public Result<NotificationCount> getNotificationCount(@RequestHeader("Authorization") String token) {
        Integer userId = extractUserId(token);
        NotificationCount count = notificationService.getNotificationCount(userId);
        return Result.success(count);
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id) {
        Integer userId = extractUserId(token);
        notificationService.markAsRead(userId, id);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader("Authorization") String token) {
        Integer userId = extractUserId(token);
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 标记某类型所有通知为已读
     */
    @PutMapping("/type/{type}/read")
    public Result<Void> markAsReadByType(
            @RequestHeader("Authorization") String token,
            @PathVariable String type) {
        Integer userId = extractUserId(token);
        notificationService.markAsReadByType(userId, type);
        return Result.success();
    }

    /**
     * 从 Token 中提取用户 ID
     *
     * TODO: 当前为占位实现，实际应从 JWT 解析或通过 ThreadLocalUtil 获取
     */
    private Integer extractUserId(String token) {
        return null;
    }
}
