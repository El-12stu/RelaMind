package io.el12stu.RelaMind.chatmemory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Cassandra 聊天消息实体类
 * 优化：在 Table 注解中显式指定 Clustering Order
 */
@Table("chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity {

    /**
     * 分区键：决定数据存储在哪个节点。
     * 对应 conversationId
     */
    @PrimaryKeyColumn(name = "key", type = PrimaryKeyType.PARTITIONED)
    private String key;

    /**
     * 聚类键：决定分区内数据的排序。
     * 对应 timestamp，设置为升序 (ASC)
     */
    @PrimaryKeyColumn(name = "timestamp", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private Long timestamp;

    private String type;

    // 建议：对于长文本，虽然 Cassandra 处理 Text 很好，但如果内容极大，
    // 生产环境有时会拆分为 blob 或外部存储，普通聊天记录用默认 Text 即可。
    private String context;
}