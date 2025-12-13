<template>
  <div class="super-agent-container">
    <div class="header">
      <div class="back-button" @click="goBack">返回</div>
      <div class="title-section">
        <h1 class="title">AI超级智能体</h1>
        <div class="subtitle">全能型 AI 助手，解决各类专业问题</div>
      </div>
      <div class="header-actions">
        <div class="status-indicator">
          <span class="status-dot"></span>
          <span class="status-text">就绪</span>
        </div>
      </div>
    </div>
    
    <div class="main-content-wrapper">
      <div class="content-wrapper">
        <div class="sidebar">
          <div class="sidebar-header">
            <h3>智能工具</h3>
          </div>
          <div class="sidebar-content">
            <div class="tool-item">
              <div class="tool-icon">🔍</div>
              <div class="tool-info">
                <div class="tool-name">专业分析</div>
                <div class="tool-desc">深度解析问题</div>
              </div>
            </div>
            <div class="tool-item">
              <div class="tool-icon">💡</div>
              <div class="tool-info">
                <div class="tool-name">智能建议</div>
                <div class="tool-desc">提供精准方案</div>
              </div>
            </div>
            <div class="tool-item">
              <div class="tool-icon">⚡</div>
              <div class="tool-info">
                <div class="tool-name">快速响应</div>
                <div class="tool-desc">实时处理请求</div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="chat-area">
          <ChatRoom 
            :messages="messages" 
            :connection-status="connectionStatus"
            ai-type="super"
            @send-message="sendMessage"
          />
        </div>
      </div>
    </div>
    
    <div class="footer-container">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import { chatWithManus } from '../api'

// 设置页面标题和元数据
useHead({
  title: 'AI超级智能体 - RelaMind',
  meta: [
    {
      name: 'description',
      content: 'AI超级智能体是 RelaMind 平台的全能助手，能解答各类专业问题，提供精准建议和解决方案'
    },
    {
      name: 'keywords',
      content: 'AI超级智能体,智能助手,专业问答,AI问答,专业建议,RelaMind,AI智能体'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const connectionStatus = ref('disconnected')
let eventSource = null

  // 添加消息到列表
const addMessage = (content, isUser, type = '', files = null) => {
  messages.value.push({
    content,
    isUser,
    type,
    time: new Date().getTime(),
    files: files || null // 文件信息数组
  })
}

  // 格式化步骤数据为可读文本
  const formatStepData = (jsonData) => {
    try {
      const data = JSON.parse(jsonData);
      let formattedText = '';
      
      // 显示步骤信息
      if (data.step) {
        formattedText += `━━━ 步骤 ${data.step} ━━━\n\n`;
      }
      
      // 显示思考内容（如果存在）
      if (data.thought && data.thought.trim()) {
        formattedText += `💭 思考：${data.thought}\n\n`;
      }
      
      // 显示调用的工具
      if (data.action === '调用工具' && data.tools && Array.isArray(data.tools)) {
        formattedText += `🔧 调用的工具：${data.tools.join('、')}\n`;
      } else if (data.action === '任务完成' && data.tools && Array.isArray(data.tools)) {
        formattedText += `✅ 任务完成，调用的工具：${data.tools.join('、')}\n`;
        // 如果有最终答案，显示答案
        if (data.answer && data.answer.trim()) {
          formattedText += `\n📝 最终答案：\n${data.answer}\n`;
        }
      } else if (data.action && data.action !== '无需执行行动' && data.action !== '调用工具' && data.action !== '任务完成') {
        formattedText += `⚡ 行动：${data.action}\n`;
      }
      
      // 文件信息将在消息对象中单独存储，这里只添加提示文本
      if (data.files && Array.isArray(data.files) && data.files.length > 0) {
        formattedText += `\n📁 生成的文件：\n`;
      }
      
      // 如果只有步骤信息，没有其他内容，显示一个简单的提示
      if (!formattedText.includes('思考') && !formattedText.includes('工具') && !formattedText.includes('行动') && !formattedText.includes('答案') && !formattedText.includes('文件')) {
        formattedText += `⏳ 正在执行步骤 ${data.step}...\n`;
      }
      
      // 显示最终答案（如果有）
      if (data.answer) {
        formattedText += `\n📝 答案：\n${data.answer}`;
      }
      
      // 显示错误（如果有）
      if (data.error) {
        formattedText += `\n❌ 错误：${data.error}`;
      }
      
      return formattedText.trim() || jsonData; // 如果格式化失败，返回原始数据
    } catch (e) {
      // 如果不是 JSON，直接返回原始数据
      return jsonData;
    }
  };
  
  // 发送消息
  const sendMessage = (message) => {
    addMessage(message, true, 'user-question')
    
    // 连接SSE
    if (eventSource) {
      eventSource.close()
    }
  
    // 设置连接状态
    connectionStatus.value = 'connecting'
    
    // 临时存储
    let messageBuffer = ''; // 用于存储完整的 JSON 消息
    let lastBubbleTime = Date.now(); // 上一个气泡的创建时间
    let isFirstResponse = true; // 是否是第一次响应
    let isDone = false; // 标记是否已收到完成信号
    let hasReceivedContent = false; // 标记是否已收到内容
    
    const minBubbleInterval = 500; // 气泡最小间隔时间(毫秒)
    
    // 创建消息气泡的函数
    const createBubble = (content, type = 'ai-answer') => {
      if (!content.trim()) return;
      
      hasReceivedContent = true; // 标记已收到内容
      
      // 尝试解析 JSON 并格式化
      let formattedContent = formatStepData(content);
      
      // 提取文件信息
      let fileInfos = null;
      try {
        const data = JSON.parse(content);
        if (data.files && Array.isArray(data.files) && data.files.length > 0) {
          fileInfos = data.files.map(fileInfo => {
            try {
              const fileData = typeof fileInfo === 'string' ? JSON.parse(fileInfo) : fileInfo;
              if (fileData.success && fileData.fileName) {
                return {
                  fileName: fileData.fileName,
                  type: fileData.type || 'file',
                  downloadUrl: `/api/file/download?type=${fileData.type || 'file'}&fileName=${encodeURIComponent(fileData.fileName)}`
                };
              }
            } catch (e) {
              // 解析失败，返回 null
            }
            return null;
          }).filter(f => f !== null);
        }
      } catch (e) {
        // 不是 JSON，忽略
      }
      
      // 添加适当的延迟，使消息显示更自然
      const now = Date.now();
      const timeSinceLastBubble = now - lastBubbleTime;
      
      const addBubble = () => {
        addMessage(formattedContent, false, type, fileInfos);
      };
      
      if (isFirstResponse) {
        // 第一条消息立即显示
        addBubble();
        isFirstResponse = false;
      } else if (timeSinceLastBubble < minBubbleInterval) {
        // 如果与上一气泡间隔太短，添加一个延迟
        setTimeout(() => {
          addBubble();
        }, minBubbleInterval - timeSinceLastBubble);
      } else {
        // 正常添加消息
        addBubble();
      }
      
      lastBubbleTime = now;
      messageBuffer = ''; // 清空缓冲区
    };
    
    eventSource = chatWithManus(message)
    
    // 监听SSE消息
    eventSource.onmessage = (event) => {
      const data = event.data
      
      if (data && data !== '[DONE]') {
        // 累积消息到缓冲区
        messageBuffer += data;
        
        // 尝试解析缓冲区中的 JSON 对象（可能包含多个 JSON 对象）
        // 按换行符或 } 分割，尝试解析每个 JSON 对象
        let processed = false;
        while (true) {
          // 查找第一个完整的 JSON 对象
          const firstBrace = messageBuffer.indexOf('{');
          if (firstBrace === -1) {
            // 没有找到 JSON 开始，清空缓冲区
            messageBuffer = '';
            break;
          }
          
          // 从第一个 { 开始查找匹配的 }
          let braceCount = 0;
          let jsonEnd = -1;
          for (let i = firstBrace; i < messageBuffer.length; i++) {
            if (messageBuffer[i] === '{') {
              braceCount++;
            } else if (messageBuffer[i] === '}') {
              braceCount--;
              if (braceCount === 0) {
                jsonEnd = i;
                break;
              }
            }
          }
          
          if (jsonEnd === -1) {
            // 没有找到完整的 JSON，等待更多数据
            break;
          }
          
          // 提取完整的 JSON 对象
          const jsonStr = messageBuffer.substring(firstBrace, jsonEnd + 1);
          try {
            // 验证 JSON 是否有效
            JSON.parse(jsonStr);
            // 如果解析成功，创建气泡
            createBubble(jsonStr);
            processed = true;
            
            // 移除已处理的 JSON，继续处理剩余的
            messageBuffer = messageBuffer.substring(jsonEnd + 1).trim();
            
            // 如果缓冲区为空或不再包含 {，退出循环
            if (messageBuffer.length === 0 || !messageBuffer.includes('{')) {
              messageBuffer = '';
              break;
            }
          } catch (e) {
            // JSON 解析失败，可能是格式问题，尝试继续查找下一个
            messageBuffer = messageBuffer.substring(jsonEnd + 1).trim();
            if (messageBuffer.length === 0 || !messageBuffer.includes('{')) {
              messageBuffer = '';
              break;
            }
          }
        }
      }
      
      if (data === '[DONE]' || data.trim() === '[DONE]') {
        isDone = true; // 标记已完成
        // 如果还有未显示的内容，尝试解析并创建最后一个气泡
        if (messageBuffer.trim()) {
          // 尝试解析剩余的 JSON
          try {
            const trimmed = messageBuffer.trim();
            if (trimmed.startsWith('{')) {
              // 尝试找到最后一个完整的 JSON
              const lastBrace = trimmed.lastIndexOf('}');
              if (lastBrace !== -1) {
                const jsonStr = trimmed.substring(0, lastBrace + 1);
                JSON.parse(jsonStr); // 验证
                createBubble(jsonStr, 'ai-final');
              } else {
                createBubble(trimmed, 'ai-final');
              }
            } else {
              createBubble(trimmed, 'ai-final');
            }
          } catch (e) {
            // 如果解析失败，直接显示
            createBubble(messageBuffer.trim(), 'ai-final');
          }
        }
        
        // 完成后关闭连接
        connectionStatus.value = 'disconnected'
        eventSource.close()
      }
    }
    
    // 监听SSE错误
    eventSource.onerror = (error) => {
      console.error('SSE Error:', error)
      
      // 如果已经收到完成信号，或者已经收到内容，说明流已经正常工作，只是连接正常关闭
      if (isDone || hasReceivedContent) {
        // 正常关闭，不显示错误
        connectionStatus.value = 'disconnected'
        
        // 如果还有未显示的内容，创建最后一个气泡
        if (messageBuffer.trim()) {
          createBubble(messageBuffer.trim(), 'ai-final');
        }
      } else {
        // 真正的错误，显示错误状态
        connectionStatus.value = 'error'
        
        // 如果出错时有未显示的内容，也创建气泡
        if (messageBuffer.trim()) {
          createBubble(messageBuffer.trim(), 'ai-error');
        } else {
          // 如果没有收到任何内容，添加错误消息
          addMessage('抱歉，发生了错误，请稍后再试。', false, 'ai-error');
        }
      }
      
      eventSource.close()
    }
  }

// 返回主页
const goBack = () => {
  router.push('/')
}

// 页面加载时添加欢迎消息
onMounted(() => {
  // 添加欢迎消息
  addMessage('你好，我是AI超级智能体。我可以解答各类问题，提供专业建议，请问有什么可以帮助你的吗？', false)
})

// 组件销毁前关闭SSE连接
onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.super-agent-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  margin: 0;
  padding: 0;
  background: #1e2329;
  background-image: 
    radial-gradient(circle at 25% 30%, rgba(16, 185, 129, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 75% 70%, rgba(59, 130, 246, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(34, 211, 238, 0.05) 0%, transparent 60%),
    linear-gradient(135deg, #1e2329 0%, #2a2f37 50%, #252932 100%);
  background-attachment: fixed;
  position: relative;
  overflow-x: hidden;
}

.super-agent-container::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 25%, rgba(16, 185, 129, 0.12) 0%, transparent 45%),
    radial-gradient(circle at 80% 75%, rgba(59, 130, 246, 0.12) 0%, transparent 45%),
    radial-gradient(circle at 50% 50%, rgba(34, 211, 238, 0.08) 0%, transparent 55%);
  animation: backgroundFlow 25s ease infinite;
  pointer-events: none;
  z-index: 0;
}

.super-agent-container::after {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(16, 185, 129, 0.02) 2px, rgba(16, 185, 129, 0.02) 4px),
    repeating-linear-gradient(90deg, transparent, transparent 2px, rgba(59, 130, 246, 0.02) 2px, rgba(59, 130, 246, 0.02) 4px);
  animation: gridPulse 8s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
  opacity: 0.3;
}

@keyframes backgroundFlow {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 1;
  }
  33% {
    transform: translate(40px, -40px) scale(1.08);
    opacity: 0.85;
  }
  66% {
    transform: translate(-30px, 30px) scale(0.92);
    opacity: 0.9;
  }
}

@keyframes gridPulse {
  0%, 100% {
    opacity: 0.2;
  }
  50% {
    opacity: 0.4;
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: rgba(30, 35, 41, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid rgba(16, 185, 129, 0.15);
  transition: all 0.3s ease;
}

.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(16, 185, 129, 0.3), transparent);
  animation: shimmer 3s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% {
    opacity: 0.3;
    transform: translateX(-100%);
  }
  50% {
    opacity: 0.6;
    transform: translateX(100%);
  }
}

.back-button {
  font-size: 16px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  font-weight: 500;
  flex-shrink: 0;
  padding: 8px 16px;
  border-radius: 8px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.back-button:hover {
  color: rgba(255, 255, 255, 1);
  background: rgba(16, 185, 129, 0.2);
  border-color: rgba(16, 185, 129, 0.4);
  transform: translateX(-4px);
}

.back-button:before {
  content: '←';
  margin-right: 6px;
  font-size: 18px;
}

.title-section {
  flex: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
  animation: gradientFlow 5s ease infinite;
  position: relative;
  filter: drop-shadow(0 2px 8px rgba(16, 185, 129, 0.3));
}

@keyframes gradientFlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 4px;
  font-weight: 400;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(16, 185, 129, 0.1);
  border-radius: 6px;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

.status-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.main-content-wrapper {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
  padding: 0 24px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.content-wrapper {
  display: flex;
  flex: 1;
  gap: 24px;
  padding: 24px 0;
  width: 100%;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

.sidebar {
  width: 280px;
  flex-shrink: 0;
  background: rgba(37, 41, 54, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(16, 185, 129, 0.15);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  padding: 24px;
  height: fit-content;
  position: sticky;
  top: 100px;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
}

.sidebar-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(16, 185, 129, 0.15);
}

.sidebar-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(16, 185, 129, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(16, 185, 129, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.tool-item:hover {
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.25);
  transform: translateX(4px);
}

.tool-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(16, 185, 129, 0.15);
  border-radius: 10px;
  flex-shrink: 0;
}

.tool-info {
  flex: 1;
}

.tool-name {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 4px;
}

.tool-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.chat-area {
  flex: 1;
  min-width: 0;
  background: transparent;
  overflow: visible;
  min-height: calc(100vh - 180px);
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
}

.chat-area :deep(.chat-container) {
  width: 100%;
  max-width: 100%;
}

.chat-area :deep(.chat-messages) {
  max-width: 100%;
  padding-left: 24px;
  padding-right: 24px;
}

.footer-container {
  margin-top: auto;
  position: relative;
  z-index: 1;
}

.footer-container :deep(.app-footer) {
  background: rgba(30, 35, 41, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  color: rgba(255, 255, 255, 0.7);
  border-top: 1px solid rgba(16, 185, 129, 0.2);
}

.footer-container :deep(.app-footer::before) {
  background: linear-gradient(90deg, transparent, rgba(16, 185, 129, 0.2), transparent);
}

.footer-container :deep(.footer-logo h3) {
  background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-container :deep(.footer-section h4) {
  color: rgba(255, 255, 255, 0.9);
}

.footer-container :deep(.footer-links a) {
  color: rgba(255, 255, 255, 0.6);
}

.footer-container :deep(.footer-links a:hover) {
  color: rgba(16, 185, 129, 1);
}

.footer-container :deep(.footer-bottom) {
  color: rgba(255, 255, 255, 0.5);
  border-top: 1px solid rgba(16, 185, 129, 0.15);
}

.footer-container :deep(.author) {
  color: rgba(255, 255, 255, 0.4);
}

.footer-container :deep(.author-link) {
  color: rgba(16, 185, 129, 0.8);
}

.footer-container :deep(.author-link:hover) {
  color: rgba(59, 130, 246, 1);
}

/* 定制 ChatRoom 组件样式 - 青绿色主题 */
.chat-area :deep(.chat-messages::-webkit-scrollbar-thumb) {
  background: rgba(16, 185, 129, 0.4);
}

.chat-area :deep(.chat-messages::-webkit-scrollbar-thumb:hover) {
  background: rgba(16, 185, 129, 0.6);
}

.chat-area :deep(.user-message .message-bubble) {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.9) 0%, rgba(59, 130, 246, 0.9) 100%);
  border-color: rgba(255, 255, 255, 0.1);
}

.chat-area :deep(.ai-message .message-bubble) {
  background: rgba(37, 41, 54, 0.8);
  border-color: rgba(16, 185, 129, 0.15);
}

.chat-area :deep(.ai-message .message-bubble:hover) {
  background: rgba(37, 41, 54, 0.9);
  border-color: rgba(16, 185, 129, 0.25);
}

.chat-area :deep(.chat-input-container) {
  background: rgba(30, 35, 41, 0.9);
  border-top: 1px solid rgba(16, 185, 129, 0.2);
  max-width: 100%;
  padding-left: 24px;
  padding-right: 24px;
}

.chat-area :deep(.input-box) {
  background: rgba(37, 41, 54, 0.6);
  border-color: rgba(16, 185, 129, 0.3);
}

.chat-area :deep(.input-box:focus) {
  border-color: rgba(16, 185, 129, 0.6);
  box-shadow: 
    inset 0 2px 8px rgba(0, 0, 0, 0.2),
    0 0 0 3px rgba(16, 185, 129, 0.1);
}

.chat-area :deep(.send-button) {
  background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%);
  background-size: 200% 200%;
  box-shadow: 
    0 4px 12px rgba(16, 185, 129, 0.3),
    0 2px 6px rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-area :deep(.send-button:hover:not(:disabled)) {
  box-shadow: 
    0 6px 16px rgba(16, 185, 129, 0.4),
    0 4px 10px rgba(59, 130, 246, 0.3);
}

.chat-area :deep(.avatar-placeholder) {
  background: linear-gradient(135deg, #10b981, #3b82f6);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.chat-area :deep(.typing-indicator) {
  color: rgba(16, 185, 129, 0.8);
}

/* 响应式样式 */
@media (max-width: 1200px) {
  .sidebar {
    width: 240px;
  }
}

@media (max-width: 968px) {
  .main-content-wrapper {
    padding: 0 16px;
  }
  
  .content-wrapper {
    flex-direction: column;
    padding: 16px 0;
  }
  
  .sidebar {
    width: 100%;
    position: relative;
    top: 0;
    max-height: none;
  }
  
  .chat-area {
    min-height: calc(100vh - 280px);
  }
}

@media (max-width: 768px) {
  .main-content-wrapper {
    padding: 0 12px;
  }
  
  .header {
    padding: 16px 20px;
    flex-wrap: wrap;
  }
  
  .title {
    font-size: 24px;
  }
  
  .subtitle {
    font-size: 11px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: center;
    margin-top: 8px;
    order: 3;
  }
  
  .content-wrapper {
    padding: 12px 0;
    gap: 16px;
  }
  
  .chat-area {
    min-height: calc(100vh - 240px);
  }
}

@media (max-width: 480px) {
  .main-content-wrapper {
    padding: 0 12px;
  }
  
  .header {
    padding: 12px 16px;
  }
  
  .back-button {
    font-size: 14px;
    padding: 6px 12px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .subtitle {
    font-size: 10px;
  }
  
  .sidebar {
    padding: 16px;
  }
  
  .tool-item {
    padding: 12px;
  }
  
  .chat-area {
    min-height: calc(100vh - 220px);
  }
}
</style> 