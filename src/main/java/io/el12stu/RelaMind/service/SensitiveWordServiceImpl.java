package io.el12stu.RelaMind.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词检测服务实现类
 * 基于 Trie（前缀树）实现高效的敏感词检测
 */
@Slf4j
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    /**
     * Trie 树根节点
     */
    private final TrieNode root = new TrieNode();

    /**
     * Trie 树节点内部类
     */
    private static class TrieNode {
        /**
         * 子节点映射：key 为字符，value 为对应的子节点
         */
        private final Map<Character, TrieNode> children = new HashMap<>();

        /**
         * 是否为词尾节点（表示从根到当前节点构成一个完整的敏感词）
         */
        private boolean isEndOfWord = false;

        /**
         * 获取或创建子节点
         */
        public TrieNode getOrCreateChild(char ch) {
            return children.computeIfAbsent(ch, k -> new TrieNode());
        }

        /**
         * 获取子节点（不创建）
         */
        public TrieNode getChild(char ch) {
            return children.get(ch);
        }

        /**
         * 标记为词尾节点
         */
        public void setEndOfWord(boolean isEndOfWord) {
            this.isEndOfWord = isEndOfWord;
        }

        /**
         * 判断是否为词尾节点
         */
        public boolean isEndOfWord() {
            return isEndOfWord;
        }
    }

    /**
     * 应用启动时加载敏感词库
     */
    @PostConstruct
    public void init() {
        try {
            log.info("开始加载敏感词库...");
            ClassPathResource resource = new ClassPathResource("sensitive_words.txt");
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String word;
                int count = 0;
                while ((word = reader.readLine()) != null) {
                    word = word.trim();
                    if (!word.isEmpty()) {
                        addWord(word);
                        count++;
                    }
                }
                log.info("敏感词库加载完成，共加载 {} 个敏感词", count);
            }
        } catch (Exception e) {
            log.error("加载敏感词库失败", e);
            throw new RuntimeException("加载敏感词库失败", e);
        }
    }

    /**
     * 向 Trie 树中添加敏感词
     * 
     * @param word 敏感词
     */
    private void addWord(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        
        TrieNode currentNode = root;
        for (char ch : word.toCharArray()) {
            currentNode = currentNode.getOrCreateChild(ch);
        }
        // 标记为词尾节点
        currentNode.setEndOfWord(true);
    }

    /**
     * 检测文本中是否包含敏感词
     * 使用 Trie 树进行高效的多模式匹配
     * 
     * @param text 待检测的文本
     * @return 如果包含敏感词，返回 true；否则返回 false
     */
    @Override
    public boolean containsSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        int length = text.length();
        // 遍历文本的每个字符作为可能的匹配起点
        for (int i = 0; i < length; i++) {
            TrieNode currentNode = root;
            // 从当前位置开始尝试匹配敏感词
            for (int j = i; j < length; j++) {
                char ch = text.charAt(j);
                TrieNode childNode = currentNode.getChild(ch);
                
                if (childNode == null) {
                    // 当前字符不在 Trie 树中，从下一个位置重新开始匹配
                    break;
                }
                
                // 检查是否匹配到完整的敏感词
                if (childNode.isEndOfWord()) {
                    return true;
                }
                
                currentNode = childNode;
            }
        }
        
        return false;
    }
}

