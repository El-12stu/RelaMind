package io.el12stu.RelaMind.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RelaMindAppDocumentLoaderTest {

    @Resource
    private RelaMindAppDocumentLoader relaMindAppDocumentLoader;

    @Test
    void loadMarkdowns() {
        relaMindAppDocumentLoader.loadMarkdowns();
    }
}