package io.el12stu.RelaMind.chatmemory.repository;

import io.el12stu.RelaMind.chatmemory.entity.ChatMessageEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends CassandraRepository<ChatMessageEntity, String> {

    /**
     * 根据 Key 查询所有消息。
     * 由于 Entity 中定义了 ordering = Ordering.ASC，
     * 这里不需要在方法名中加 OrderBy，返回的 List 默认就是按时间正序排列的。
     */
    List<ChatMessageEntity> findByKey(String key);

    /**
     * 范围查询优化：获取最新的 N 条消息。
     * 使用 CQL 的 ORDER BY DESC 和 LIMIT 来高效获取最新的消息，
     * 返回结果需要反转以保持时间正序。
     * 
     * @param key 会话ID
     * @param limit 需要获取的消息数量
     * @return 最新的 N 条消息（按时间升序排列）
     */
    @Query("SELECT * FROM chat_messages WHERE key = ?0 ORDER BY timestamp DESC LIMIT ?1")
    List<ChatMessageEntity> findLatestByKey(String key, int limit);


    /**
     * 删除整个会话（删除分区）
     * 这是一个高效的操作，比逐条删除要快得多。
     */
    @Query("DELETE FROM chat_messages WHERE key = ?0")
    void deleteByKey(String key);
}