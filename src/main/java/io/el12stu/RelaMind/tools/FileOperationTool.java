package io.el12stu.RelaMind.tools;

import cn.hutool.core.io.FileUtil;
import io.el12stu.RelaMind.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类（提供文件读写功能）
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content
    ) {
        String filePath = FILE_DIR + "/" + fileName;

        try {
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            // 返回 JSON 格式的文件信息，便于前端显示下载链接
            return String.format("{\"success\":true,\"type\":\"file\",\"fileName\":\"%s\",\"filePath\":\"%s\",\"message\":\"文件已成功保存\"}", 
                    fileName.replace("\\", "\\\\").replace("\"", "\\\""), 
                    filePath.replace("\\", "\\\\").replace("\"", "\\\""));
        } catch (Exception e) {
            return String.format("{\"success\":false,\"error\":\"%s\"}", 
                    e.getMessage().replace("\\", "\\\\").replace("\"", "\\\""));
        }
    }
}
