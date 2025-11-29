package io.el12stu.RelaMind.service;

/**
 * 敏感词检测服务接口
 */
public interface SensitiveWordService {

    /**
     * 检查输入是否包含敏感词
     * @param text 用户输入文本
     * @return 如果包含敏感词，返回 true
     */
    boolean containsSensitiveWords(String text);
}

