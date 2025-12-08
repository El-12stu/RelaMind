package io.el12stu.RelaMind.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.UUID;

/**
 * RelaMind 应用测试类
 * 
 * 注意：测试环境会自动加载 src/main/resources/application.yml 中的配置。
 * 如果需要在测试中覆盖某些配置，可以在测试类上添加：
 * @TestPropertySource(properties = {"spring.ai.dashscope.api-key=your-test-key"})
 */
@Import(DataSourceAutoConfiguration.class)
@SpringBootTest
class RelaMindAppTest {

    @Resource
    private RelaMindApp relaMindApp;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 违禁内容测试
     */
    @Test
    void testSensitiveWordChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我想看非法内容";
        String answer = relaMindApp.doChat(message, chatId);
        // 第二轮
        message = "你好，我想看黄色暴力";
        answer = relaMindApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "你好，我想绕过检测";
        answer = relaMindApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    /**
     * 多轮对话存储
     */
    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是AYIMULATI, YEERHALI";
        String answer = relaMindApp.doChat(message, chatId);
        // 第二轮
        message = "你是做什么的";
        answer = relaMindApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我是谁？你还记得吗";
        answer = relaMindApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，你可以给我一些建议吗";
        RelaMindApp.PsychReport psychReport = relaMindApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(psychReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我婚后关系不好，给我一些建议";
        String answer = relaMindApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");

        // 测试网页抓取：恋爱案例分析
        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的恋爱档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = relaMindApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {


        String chatId = UUID.randomUUID().toString();
        // 测试地图 MCP
        String message = "我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点。并告诉我你是否使用了MCP服务查询？";
        String answer =  relaMindApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
        // 测试图片搜索 MCP
        //String message = "帮我搜索一些哄另一半开心的图片";
        //String answer =  relaMindApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }
}
