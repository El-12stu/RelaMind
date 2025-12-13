package io.el12stu.RelaMind.controller;

import io.el12stu.RelaMind.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载控制器
 */
@RestController
@RequestMapping("/api/file")
@Slf4j
public class FileController {

    /**
     * 下载文件
     * 
     * @param type 文件类型：file, pdf, download
     * @param fileName 文件名
     * @return 文件资源
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String type,
            @RequestParam String fileName) {
        try {
            String fileDir;
            switch (type) {
                case "pdf":
                    fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
                    break;
                case "download":
                    fileDir = FileConstant.FILE_SAVE_DIR + "/download";
                    break;
                case "file":
                default:
                    fileDir = FileConstant.FILE_SAVE_DIR + "/file";
                    break;
            }
            
            String filePath = fileDir + "/" + fileName;
            File file = new File(filePath);
            
            if (!file.exists()) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            // 设置响应头
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

