package com.itniuma.bitinn.controller.interaction;

import com.itniuma.bitinn.pojo.entity.Comment;
import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.interaction.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 互动控制器
 *
 * 管理文章的所有社交互动行为：点赞、收藏、关注、评论、分享。
 * 每次互动会触发计数变更，通过 RabbitMQ 异步同步到 Redis 和 Elasticsearch。
 *
 * 点赞/收藏/关注 均为 Toggle 模式（已点则取消，未点则添加），
 * 前端通过 status 接口查询当前用户的互动状态。
 *
 * @author aceFelix
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interaction")
public class InteractionController {

    private final InteractionService interactionService;

    /**
     * 点赞/取消点赞
     *
     * @param articleId 文章 ID
     * @return 操作结果（含最新点赞数）
     */
    @PostMapping("/like/{articleId}")
    public Result toggleLike(@PathVariable Integer articleId) {
        return interactionService.toggleLike(articleId);
    }

    /**
     * 查询当前用户是否已点赞该文章
     */
    @GetMapping("/like/status/{articleId}")
    public Result isLiked(@PathVariable Integer articleId) {
        return interactionService.isLiked(articleId);
    }

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/favorite/{articleId}")
    public Result toggleFavorite(@PathVariable Integer articleId) {
        return interactionService.toggleFavorite(articleId);
    }

    /**
     * 查询当前用户是否已收藏该文章
     */
    @GetMapping("/favorite/status/{articleId}")
    public Result isFavorited(@PathVariable Integer articleId) {
        return interactionService.isFavorited(articleId);
    }

    /**
     * 关注/取消关注用户
     */
    @PostMapping("/follow/{userId}")
    public Result toggleFollow(@PathVariable Integer userId) {
        return interactionService.toggleFollow(userId);
    }

    /**
     * 查询当前用户是否已关注目标用户
     */
    @GetMapping("/follow/status/{userId}")
    public Result isFollowed(@PathVariable Integer userId) {
        return interactionService.isFollowed(userId);
    }

    /**
     * 添加评论
     */
    @PostMapping("/comment")
    public Result addComment(@RequestBody Comment comment) {
        return interactionService.addComment(comment);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/{id}")
    public Result deleteComment(@PathVariable Integer id) {
        return interactionService.deleteComment(id);
    }

    /**
     * 获取文章评论列表
     */
    @GetMapping("/comment/{articleId}")
    public Result getComments(@PathVariable Integer articleId) {
        return interactionService.getComments(articleId);
    }

    /**
     * 获取当前用户点赞过的文章列表
     */
    @GetMapping("/liked-articles")
    public Result getLikedArticles() {
        return interactionService.getLikedArticles();
    }

    /**
     * 获取当前用户收藏过的文章列表
     */
    @GetMapping("/favorited-articles")
    public Result getFavoritedArticles() {
        return interactionService.getFavoritedArticles();
    }

    /**
     * 获取当前用户关注的用户列表
     */
    @GetMapping("/following-users")
    public Result getFollowingUsers() {
        return interactionService.getFollowingUsers();
    }

    /**
     * 获取当前用户对某篇文章的互动状态（点赞、收藏）
     */
    @GetMapping("/article-status/{articleId}")
    public Result getArticleInteractionStatus(@PathVariable Integer articleId) {
        return interactionService.getArticleInteractionStatus(articleId);
    }

    /**
     * 批量获取当前用户对多篇文章的互动状态
     *
     * 用于 Feed 流加载时一次性查询所有文章的点赞/收藏状态，
     * 避免逐条请求，减少网络开销。
     */
    @PostMapping("/batch-status")
    public Result getBatchArticleInteractionStatus(@RequestBody List<Integer> articleIds) {
        return interactionService.getBatchArticleInteractionStatus(articleIds);
    }

    /**
     * 分享文章（计数 +1）
     */
    @PostMapping("/share/{articleId}")
    public Result shareArticle(@PathVariable Integer articleId) {
        return interactionService.shareArticle(articleId);
    }
}
