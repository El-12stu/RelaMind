package io.el12stu.RelaMind.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "下载pdf文件.pdf";
        String content = "你好，我是el-12stu";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}