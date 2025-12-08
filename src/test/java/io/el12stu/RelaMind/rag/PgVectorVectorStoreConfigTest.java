package io.el12stu.RelaMind.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

/**
 * PgVector 向量存储配置测试类
 * 
 * 注意：由于主应用类 RelaMindApplication 排除了 DataSourceAutoConfiguration，
 * 测试中需要重新导入以启用 DataSource 和 JdbcTemplate 的自动配置。
 * 同时需要激活 local profile 以加载数据库连接配置。
 */
@SpringBootTest
@ActiveProfiles("local")
@Import(DataSourceAutoConfiguration.class)
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("鱼皮的编程导航有什么用？学编程啊，做项目啊", Map.of("meta1", "meta1")),
                new Document("程序员鱼皮的原创项目教程 codefather.cn"),
                new Document("鱼皮这小伙子比较帅气", Map.of("meta2", "meta2")));
        // 添加文档
        pgVectorVectorStore.add(documents);
        // 相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("怎么学编程啊").topK(3).build());
        Assertions.assertNotNull(results);
    }
}
