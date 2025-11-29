package io.el12stu.RelaMind.chatmemory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * 用户会话摘要实体类
 * 用于支持根据 userId 查询该用户拥有的所有会话及其最近一条消息的摘要
 * 
 * 主键设计：
 * - 分区键：userId（决定数据存储在哪个节点）
 * - 聚类键1：latestTimestamp（降序，以便快速获取用户最新的会话）
 * - 聚类键2：conversationId（保证主键唯一性，避免并发环境下相同时间戳导致的数据覆盖）
 */
@Table("user_conversation_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConversationSummary {

    /**
     * 分区键：用户ID，决定数据存储在哪个节点
     */
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    private String userId;

    /**
     * 聚类键1：会话的最新消息时间戳，设置为降序 (DESC)
     * 这样查询时，最新的会话会自动排在前面
     */
    @PrimaryKeyColumn(name = "latest_timestamp", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING, ordinal = 0)
    private Long latestTimestamp;

    /**
     * 聚类键2：会话ID，对应 ChatMessageEntity 的 key
     * 作为第二个聚类键，保证主键唯一性，避免并发环境下相同时间戳导致的数据覆盖
     */
    @PrimaryKeyColumn(name = "conversation_id", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING, ordinal = 1)
    private String conversationId;

    /**
     * 会话标题或最新消息摘要
     * 可用于显示在会话列表中
     */
    private String title;

    /**
     * 最新消息的片段（可选，用于显示预览）
     * 如果 title 已经足够，这个字段可以为空
     */
    private String latestMessageSnippet;
}

