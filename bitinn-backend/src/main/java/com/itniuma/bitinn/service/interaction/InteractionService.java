package com.itniuma.bitinn.service.interaction;

import com.itniuma.bitinn.mapper.article.ArticleMapper;
import com.itniuma.bitinn.mapper.interaction.ArticleFavoriteMapper;
import com.itniuma.bitinn.mapper.interaction.ArticleLikeMapper;
import com.itniuma.bitinn.mapper.interaction.CommentMapper;
import com.itniuma.bitinn.mapper.interaction.UserFollowMapper;
import com.itniuma.bitinn.mapper.user.UserMapper;
import com.itniuma.bitinn.pojo.entity.*;
import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.cache.FeedCacheService;
import com.itniuma.bitinn.service.mq.DataSyncProducer;
import com.itniuma.bitinn.service.notification.NotificationService;
import com.itniuma.bitinn.service.search.ArticleDataSyncService;
import com.itniuma.bitinn.utils.RedisCacheHelper;
import com.itniuma.bitinn.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 交互服务
 * 负责点赞、收藏、关注、评论、转发等社交交互操作，含通知发送和计数同步。
 *
 * @author aceFelix
 */
@Slf4j
@Service
public class InteractionService {

    private final ArticleLikeMapper likeMapper;
    private final ArticleFavoriteMapper favoriteMapper;
    private final UserFollowMapper followMapper;
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final InteractionCounterService counterService;
    private final RedisCacheHelper redisCache;
    private final NotificationService notificationService;
    private final DataSyncProducer dataSyncProducer;
    private final FeedCacheService feedCacheService;

    @Autowired(required = false)
    private ArticleDataSyncService dataSyncService;

    public InteractionService(ArticleLikeMapper likeMapper,
                              ArticleFavoriteMapper favoriteMapper,
                              UserFollowMapper followMapper,
                              CommentMapper commentMapper,
                              ArticleMapper articleMapper,
                              UserMapper userMapper,
                              InteractionCounterService counterService,
                              RedisCacheHelper redisCache,
                              NotificationService notificationService,
                              DataSyncProducer dataSyncProducer,
                              FeedCacheService feedCacheService) {
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
        this.followMapper = followMapper;
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.counterService = counterService;
        this.redisCache = redisCache;
        this.notificationService = notificationService;
        this.dataSyncProducer = dataSyncProducer;
        this.feedCacheService = feedCacheService;
    }

    /**
     * 切换点赞状态，已点则取消，未点则点赞，并发送通知给文章作者。
     */
    @Transactional
    public Result toggleLike(Integer articleId) {
        Integer userId = getCurrentUserId();
        Article article = articleMapper.findById(articleId);
        if (article == null) {
            return Result.error("文章不存在");
        }

        ArticleLike existing = likeMapper.findByArticleAndUser(articleId, userId);
        boolean liked;
        if (existing != null) {
            likeMapper.delete(articleId, userId);
            counterService.decrementLikeCount(articleId);
            liked = false;
            log.info("用户{}取消点赞文章{}", userId, articleId);
        } else {
            likeMapper.insert(articleId, userId);
            counterService.incrementLikeCount(articleId);
            liked = true;
            log.info("用户{}点赞文章{}", userId, articleId);

            // 发送点赞通知
            String title = "收到新的点赞";
            String content = truncate(article.getTitle(), 50);
            notificationService.sendNotification(
                    article.getCreateUser(),  // 文章作者
                    "like",
                    title,
                    content,
                    userId,                  // 点赞者
                    articleId,               // 文章ID
                    "article"
            );
        }

        feedCacheService.incrementVersion();
        syncArticleCountsToEs(articleId);
        return Result.success(liked, liked ? "点赞成功" : "取消点赞成功");
    }

    /**
     * 检查当前用户是否已点赞该文章
     */
    public Result isLiked(Integer articleId) {
        Integer userId = getCurrentUserId();
        ArticleLike existing = likeMapper.findByArticleAndUser(articleId, userId);
        return Result.success(existing != null);
    }

    /**
     * 切换收藏状态，已收藏则取消，未收藏则收藏，并发送通知给文章作者。
     */
    @Transactional
    public Result toggleFavorite(Integer articleId) {
        Integer userId = getCurrentUserId();
        Article article = articleMapper.findById(articleId);
        if (article == null) {
            return Result.error("文章不存在");
        }

        ArticleFavorite existing = favoriteMapper.findByArticleAndUser(articleId, userId);
        boolean favorited;
        if (existing != null) {
            favoriteMapper.delete(articleId, userId);
            counterService.decrementFavoriteCount(articleId);
            favorited = false;
            log.info("用户{}取消收藏文章{}", userId, articleId);
        } else {
            favoriteMapper.insert(articleId, userId);
            counterService.incrementFavoriteCount(articleId);
            favorited = true;
            log.info("用户{}收藏文章{}", userId, articleId);

            // 发送收藏通知
            String title = "收到新的收藏";
            String content = truncate(article.getTitle(), 50);
            notificationService.sendNotification(
                    article.getCreateUser(),  // 文章作者
                    "favorite",
                    title,
                    content,
                    userId,                  // 收藏者
                    articleId,               // 文章ID
                    "article"
            );
        }

        feedCacheService.incrementVersion();
        syncArticleCountsToEs(articleId);
        return Result.success(favorited, favorited ? "收藏成功" : "取消收藏成功");
    }

    /**
     * 检查当前用户是否已收藏该文章
     */
    public Result isFavorited(Integer articleId) {
        Integer userId = getCurrentUserId();
        ArticleFavorite existing = favoriteMapper.findByArticleAndUser(articleId, userId);
        return Result.success(existing != null);
    }

    /**
     * 切换关注状态，已关注则取消，未关注则关注，并发送通知。
     */
    @Transactional
    public Result toggleFollow(Integer followingId) {
        Integer followerId = getCurrentUserId();
        if (followerId.equals(followingId)) {
            return Result.error("不能关注自己");
        }

        UserFollow existing = followMapper.findByFollowerAndFollowing(followerId, followingId);
        boolean followed;
        if (existing != null) {
            followMapper.delete(followerId, followingId);
            counterService.decrementFollowingCount(followerId);
            counterService.decrementFollowerCount(followingId);
            followed = false;
            log.info("用户{}取消关注用户{}", followerId, followingId);
        } else {
            followMapper.insert(followerId, followingId);
            counterService.incrementFollowingCount(followerId);
            counterService.incrementFollowerCount(followingId);
            followed = true;
            log.info("用户{}关注用户{}", followerId, followingId);

            // 发送关注通知
            User follower = userMapper.findById(followerId);
            String title = "收到新的关注";
            String content = follower != null ? follower.getNickname() + " 关注了你" : "有人关注了你";
            notificationService.sendNotification(
                    followingId,             // 被关注者
                    "follow",
                    title,
                    content,
                    followerId,              // 关注者
                    null,                    // 无关联资源
                    null
            );
        }

        return Result.success(followed, followed ? "关注成功" : "取消关注成功");
    }

    /**
     * 检查当前用户是否已关注目标用户
     */
    public Result isFollowed(Integer followingId) {
        Integer followerId = getCurrentUserId();
        UserFollow existing = followMapper.findByFollowerAndFollowing(followerId, followingId);
        return Result.success(existing != null);
    }

    /**
     * 发表评论，支持回复，对文章作者和被回复者分别发送通知。
     */
    @Transactional
    public Result addComment(Comment comment) {
        Integer userId = getCurrentUserId();
        comment.setUserId(userId);

        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        if (comment.getContent().length() > 1000) {
            return Result.error("评论内容不能超过1000字");
        }

        Article article = articleMapper.findById(comment.getArticleId());
        if (article == null) {
            return Result.error("文章不存在");
        }

        commentMapper.insert(comment);
        counterService.incrementCommentCount(comment.getArticleId());
        feedCacheService.incrementVersion();
        syncArticleCountsToEs(comment.getArticleId());

        log.info("用户{}评论文章{}", userId, comment.getArticleId());

        // 发送评论通知（通知文章作者，但不通知自己）
        if (!article.getCreateUser().equals(userId)) {
            String title = "收到新的评论";
            String content = truncate(comment.getContent(), 50);
            notificationService.sendNotification(
                    article.getCreateUser(),  // 文章作者
                    "comment",
                    title,
                    content,
                    userId,                  // 评论者
                    comment.getId(),         // 评论ID
                    "article"
            );
        }

        // 如果是回复评论，通知被回复者
        if (comment.getParentId() != null) {
            Comment parentComment = commentMapper.findById(comment.getParentId());
            if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                String title = "收到新的回复";
                String content = truncate(comment.getContent(), 50);
                notificationService.sendNotification(
                        parentComment.getUserId(), // 被回复者
                        "comment",
                        title,
                        content,
                        userId,                  // 回复者
                        comment.getId(),         // 回复ID
                        "comment"
                );
            }
        }

        return Result.success(null, "评论成功");
    }

    /**
     * 删除评论，仅允许评论作者本人操作。
     */
    @Transactional
    public Result deleteComment(Integer commentId) {
        Integer userId = getCurrentUserId();
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            return Result.error("无权删除此评论");
        }

        commentMapper.delete(commentId);
        counterService.decrementCommentCount(comment.getArticleId());
        feedCacheService.incrementVersion();
        syncArticleCountsToEs(comment.getArticleId());

        log.info("用户{}删除评论{}", userId, commentId);
        return Result.success(null, "删除成功");
    }

    /**
     * 获取文章的评论列表
     */
    public Result<List<Comment>> getComments(Integer articleId) {
        List<Comment> comments = commentMapper.findByArticleId(articleId);
        return Result.success(comments);
    }

    /**
     * 获取当前用户点赞过的文章列表，批量查询避免 N+1 问题。
     */
    public Result getLikedArticles() {
        Integer userId = getCurrentUserId();
        List<Integer> articleIds = likeMapper.findLikedArticleIdsByUserId(userId);
        if (articleIds.isEmpty()) {
            return Result.success(List.of());
        }
        // 批量查询文章，避免 N+1 问题
        List<Article> articles = articleMapper.findByIds(articleIds);
        articles.forEach(a -> {
            Long likeVal = counterService.getLikeCount(a.getId());
            a.setLikeCount(likeVal != null ? likeVal.intValue() : a.getLikeCount());
            Long favVal = counterService.getFavoriteCount(a.getId());
            a.setFavoriteCount(favVal != null ? favVal.intValue() : a.getFavoriteCount());
            Long comVal = counterService.getCommentCount(a.getId());
            a.setCommentCount(comVal != null ? comVal.intValue() : a.getCommentCount());
        });
        return Result.success(articles);
    }

    /**
     * 获取当前用户收藏的文章列表，批量查询避免 N+1 问题。
     */
    public Result getFavoritedArticles() {
        Integer userId = getCurrentUserId();
        List<Integer> articleIds = favoriteMapper.findFavoritedArticleIdsByUserId(userId);
        if (articleIds.isEmpty()) {
            return Result.success(List.of());
        }
        // 批量查询文章，避免 N+1 问题
        List<Article> articles = articleMapper.findByIds(articleIds);
        articles.forEach(a -> {
            Long likeVal = counterService.getLikeCount(a.getId());
            a.setLikeCount(likeVal != null ? likeVal.intValue() : a.getLikeCount());
            Long favVal = counterService.getFavoriteCount(a.getId());
            a.setFavoriteCount(favVal != null ? favVal.intValue() : a.getFavoriteCount());
            Long comVal = counterService.getCommentCount(a.getId());
            a.setCommentCount(comVal != null ? comVal.intValue() : a.getCommentCount());
        });
        return Result.success(articles);
    }

    /**
     * 获取当前用户关注的用户列表，优先从 Redis 缓存读取。
     */
    public Result getFollowingUsers() {
        Integer userId = getCurrentUserId();
        List<Integer> followingIds = followMapper.findFollowingIdsByFollowerId(userId);
        if (followingIds.isEmpty()) {
            return Result.success(List.of());
        }
        // 批量查询用户信息，避免 N+1 问题
        List<Map<String, Object>> users = followingIds.stream()
                .map(uid -> {
                    // 先尝试从缓存获取
                    String cacheKey = "user:info:id:" + uid;
                    User user = redisCache.get(cacheKey, User.class);
                    if (user == null) {
                        user = userMapper.findById(uid);
                        if (user != null) {
                            user.setPassword(null);
                            redisCache.set(cacheKey, user, 2, java.util.concurrent.TimeUnit.HOURS);
                        }
                    }
                    if (user == null) return null;
                    Map<String, Object> userInfo = new java.util.HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("nickname", user.getNickname());
                    userInfo.put("username", user.getUsername());
                    userInfo.put("avatar", user.getUserPic());
                    userInfo.put("followerCount", counterService.getFollowerCount(uid) != null ? counterService.getFollowerCount(uid) : 0);
                    return userInfo;
                })
                .filter(u -> u != null)
                .toList();
        return Result.success(users);
    }

    /**
     * 获取文章交互状态（是否已点赞/收藏）及各项计数
     */
    public Result getArticleInteractionStatus(Integer articleId) {
        Integer userId = getCurrentUserId();
        boolean liked = likeMapper.findByArticleAndUser(articleId, userId) != null;
        boolean favorited = favoriteMapper.findByArticleAndUser(articleId, userId) != null;

        // 使用 EXISTS 判断：null 表示缓存不存在，需从 MySQL 初始化
        Long likeCountVal = counterService.getLikeCount(articleId);
        long likeCount;
        if (likeCountVal != null) {
            likeCount = likeCountVal;
        } else {
            Article article = articleMapper.findById(articleId);
            likeCount = article != null && article.getLikeCount() != null ? article.getLikeCount() : 0;
            counterService.initArticleCounts(articleId, likeCount,
                    article != null && article.getFavoriteCount() != null ? article.getFavoriteCount() : 0,
                    article != null && article.getCommentCount() != null ? article.getCommentCount() : 0,
                    article != null && article.getShareCount() != null ? article.getShareCount() : 0);
        }

        Long favoriteCountVal = counterService.getFavoriteCount(articleId);
        long favoriteCount = favoriteCountVal != null ? favoriteCountVal : 0;

        Long commentCountVal = counterService.getCommentCount(articleId);
        long commentCount;
        if (commentCountVal != null) {
            commentCount = commentCountVal;
        } else {
            Long dbCount = commentMapper.countByArticleId(articleId);
            commentCount = dbCount != null ? dbCount : 0;
        }

        Long shareCountVal = counterService.getShareCount(articleId);
        long shareCount = shareCountVal != null ? shareCountVal : 0;

        Map<String, Object> status = Map.of(
                "liked", liked,
                "favorited", favorited,
                "likeCount", likeCount,
                "favoriteCount", favoriteCount,
                "commentCount", commentCount,
                "shareCount", shareCount
        );
        return Result.success(status);
    }

    /**
     * 批量获取多篇文章的交互状态（仅点赞/收藏状态）
     */
    public Result getBatchArticleInteractionStatus(List<Integer> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Result.success(Map.of());
        }
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (claims != null) ? (Integer) claims.get("id") : null;
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        if (userId == null) {
            for (Integer articleId : articleIds) {
                result.put(String.valueOf(articleId), Map.of("liked", false, "favorited", false));
            }
        } else {
            for (Integer articleId : articleIds) {
                boolean liked = likeMapper.findByArticleAndUser(articleId, userId) != null;
                boolean favorited = favoriteMapper.findByArticleAndUser(articleId, userId) != null;
                result.put(String.valueOf(articleId), Map.of("liked", liked, "favorited", favorited));
            }
        }
        return Result.success(result);
    }

    /**
     * 转发文章，递增分享计数。
     */
    @Transactional
    public Result shareArticle(Integer articleId) {
        Article article = articleMapper.findById(articleId);
        if (article == null) {
            return Result.error("文章不存在");
        }

        counterService.incrementShareCount(articleId);
        feedCacheService.incrementVersion();

        log.info("文章{}被转发", articleId);
        return Result.success(null, "转发成功");
    }

    private Integer getCurrentUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        return (Integer) claims.get("id");
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    private void syncArticleCountsToEs(Integer articleId) {
        if (articleId == null) return;
        try {
            // 通过 MQ 异步同步，保证跨存储最终一致性
            dataSyncProducer.sendArticleCountSync(articleId, "update");
        } catch (Exception e) {
            log.warn("[ES同步] 发送计数同步消息失败: articleId={}", articleId, e);
        }
    }
}
