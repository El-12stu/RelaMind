package io.el12stu.RelaMind.chatmemory.repository;

import io.el12stu.RelaMind.chatmemory.entity.UserConversationSummary;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户会话摘要 Repository
 * 支持根据 userId 查询该用户的所有会话摘要
 */
@Repository
public interface UserConversationRepository extends CassandraRepository<UserConversationSummary, String> {

    /**
     * 根据用户ID查询该用户的所有会话摘要。
     * 由于实体中定义了 latestTimestamp 为降序排列，
     * 返回的列表会自动按照最新消息时间降序排列（最新的在前）。
     * 
     * @param userId 用户ID
     * @return 该用户的所有会话摘要列表，按最新消息时间降序排列
     */
    List<UserConversationSummary> findByUserId(String userId);

    /**
     * 根据用户ID和会话ID查询会话摘要
     * 由于主键结构包含 (userId, latestTimestamp, conversationId)，
     * Spring Data Cassandra 可以高效地使用复合键进行查询。
     * 
     * @param userId 用户ID（分区键）
     * @param conversationId 会话ID（聚类键）
     * @return 匹配的会话摘要列表（理论上只有一条，因为主键唯一）
     */
    List<UserConversationSummary> findByUserIdAndConversationId(String userId, String conversationId);

    /**
     * 根据用户ID和会话ID删除会话摘要
     * 由于主键包含 userId 和 conversationId，这是一个高效的操作
     * 
     * @param userId 用户ID（分区键）
     * @param conversationId 会话ID（聚类键）
     */
    @Query("DELETE FROM user_conversation_summary WHERE user_id = ?0 AND conversation_id = ?1")
    // 这是最精确的行删除，需要知道完整的 PK
    void deleteByUserIdAndLatestTimestampAndConversationId(String userId, Long latestTimestamp, String conversationId);

    /**
     * 根据用户ID删除该用户的所有会话摘要
     * 这是一个高效的操作，因为 userId 是分区键
     * 
     * @param userId 用户ID
     */
    @Query("DELETE FROM user_conversation_summary WHERE user_id = ?0")
    void deleteByUserId(String userId);
}

