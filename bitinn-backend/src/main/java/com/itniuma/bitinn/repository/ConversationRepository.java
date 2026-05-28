package com.itniuma.bitinn.repository;

import com.itniuma.bitinn.pojo.mongo.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI 对话会话 Repository
 *
 * @author aceFelix
 */
@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    /**
     * 获取用户的会话列表（按更新时间倒序）
     */
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Integer userId, Pageable pageable);

    /**
     * 获取用户未删除的单个会话
     */
    Optional<Conversation> findByIdAndUserId(String id, Integer userId);

    /**
     * 统计用户的会话数量
     */
    long countByUserId(Integer userId);
}
