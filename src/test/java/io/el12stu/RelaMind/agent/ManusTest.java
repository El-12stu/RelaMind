package io.el12stu.RelaMind.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ManusTest {

    @Resource
    private Manus manus;

    @Test
    public void run() {
        String userPrompt = """
                我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点，
                并结合一些网络图片，制定一份详细的约会计划，
                并以 PDF 格式输出""";
        String test = "你好";
        String test1 = "我想在上海静安区吃饭，给我推荐一些餐馆，用PDF格式输出";
        String answer = manus.run(test1);
        Assertions.assertNotNull(answer);
    }
}