package io.el12stu.RelaMind.controller;

import io.el12stu.RelaMind.service.DiaryService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日记记录控制器
 * 提供日记记录的API接口
 */
@RestController
@RequestMapping("/diary")
public class DiaryController {

    @Resource
    private DiaryService diaryService;

    /**
     * 保存日记请求体
     */
    @Data
    public static class SaveDiaryRequest {
        /**
         * 用户ID（可选）
         */
        private String userId;
        
        /**
         * 日记内容（必填）
         */
        private String content;
        
        /**
         * 心情（可选）
         */
        private String mood;
        
        /**
         * 标签列表（可选）
         */
        private List<String> tags;
    }

    /**
     * 保存日记响应
     */
    @Data
    public static class SaveDiaryResponse {
        private boolean success;
        private String message;
        private String documentId;
    }

    /**
     * 保存日记记录
     * 
     * @param request 日记记录请求
     * @return 保存结果
     */
    @PostMapping("/save")
    public SaveDiaryResponse saveDiary(@RequestBody SaveDiaryRequest request) {
        SaveDiaryResponse response = new SaveDiaryResponse();
        
        if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            response.setSuccess(false);
            response.setMessage("日记内容不能为空");
            return response;
        }

        boolean success = diaryService.saveDiary(
            request.getUserId(),
            request.getContent(),
            request.getMood(),
            request.getTags()
        );

        if (success) {
            response.setSuccess(true);
            response.setMessage("日记保存成功，已存储到知识库中");
        } else {
            response.setSuccess(false);
            response.setMessage("日记保存失败，请稍后重试");
        }

        return response;
    }

    /**
     * 快速保存日记（简化接口，只需要内容）
     * 
     * @param content 日记内容
     * @return 保存结果
     */
    @PostMapping("/quick-save")
    public SaveDiaryResponse quickSaveDiary(@RequestParam String content) {
        SaveDiaryResponse response = new SaveDiaryResponse();
        
        if (content == null || content.trim().isEmpty()) {
            response.setSuccess(false);
            response.setMessage("日记内容不能为空");
            return response;
        }

        boolean success = diaryService.saveDiary(content);

        if (success) {
            response.setSuccess(true);
            response.setMessage("日记保存成功");
        } else {
            response.setSuccess(false);
            response.setMessage("日记保存失败，请稍后重试");
        }

        return response;
    }
}

