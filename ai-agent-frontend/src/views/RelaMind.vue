<template>
  <div class="relamind-container">
    <div class="header">
      <div class="back-button" @click="goBack">返回</div>
      <div class="title-section">
        <h1 class="title">RelaMind</h1>
        <div class="subtitle">你的 AI 成长伙伴</div>
      </div>
      <div class="chat-id">会话ID: {{ chatId }}</div>
    </div>
    
    <div class="main-content-wrapper">
      <div class="content-wrapper">
        <div class="chat-area">
          <ChatRoom 
            :messages="messages" 
            :connection-status="connectionStatus"
            ai-type="relamind"
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
import { chatWithRelaMind } from '../api'

// 设置页面标题和元数据
useHead({
  title: 'RelaMind - AI 个人成长伙伴',
  meta: [
    {
      name: 'description',
      content: 'RelaMind 是你的 AI 成长伙伴，帮助你记录成长、理解自己、成为更好的你'
    },
    {
      name: 'keywords',
      content: 'RelaMind,AI成长伙伴,个人成长,成长记录,AI助手,成长分析'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
let eventSource = null

// 生成随机会话ID
const generateChatId = () => {
  return 'relamind_' + Math.random().toString(36).substring(2, 10) + '_' + Date.now()
}

// 添加消息到列表
const addMessage = (content, isUser) => {
  messages.value.push({
    content,
    isUser,
    time: new Date().getTime()
  })
}

// 发送消息
const sendMessage = (message) => {
  addMessage(message, true)
  
  // 连接SSE
  if (eventSource) {
    eventSource.close()
  }
  
  // 创建一个空的AI回复消息
  const aiMessageIndex = messages.value.length
  addMessage('', false)
  
  connectionStatus.value = 'connecting'
  let isDone = false // 标记是否已收到完成信号
  eventSource = chatWithRelaMind(message, chatId.value)
  
  // 监听SSE消息
  eventSource.onmessage = (event) => {
    const data = event.data
    if (data && data !== '[DONE]') {
      // 更新最新的AI消息内容
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content += data
      }
    }
    
    if (data === '[DONE]') {
      isDone = true // 标记已完成
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }
  
  // 监听SSE错误
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    
    // 如果已经收到完成信号，或者消息内容不为空，说明流已经正常工作，只是连接正常关闭
    const hasContent = aiMessageIndex < messages.value.length && 
                      messages.value[aiMessageIndex].content.trim().length > 0
    
    if (isDone || hasContent) {
      // 正常关闭，不显示错误
      connectionStatus.value = 'disconnected'
    } else {
      // 真正的错误，显示错误消息
      connectionStatus.value = 'error'
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content = '抱歉，发生了错误，请稍后再试。'
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
  // 生成聊天ID
  chatId.value = generateChatId()
  
  // 添加欢迎消息
  addMessage('你好！我是 RelaMind，你的 AI 成长伙伴。我可以倾听你的心声，帮你记录生活，回顾历史，分析成长模式。今天想聊些什么呢？', false)
})

// 组件销毁前关闭SSE连接
onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.relamind-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  margin: 0;
  padding: 0;
  background: #1a1d29;
  background-image: 
    radial-gradient(circle at 20% 30%, rgba(102, 126, 234, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(118, 75, 162, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(240, 147, 251, 0.05) 0%, transparent 60%),
    linear-gradient(135deg, #1a1d29 0%, #252936 50%, #1f2330 100%);
  background-attachment: fixed;
  position: relative;
  overflow-x: hidden;
}

.relamind-container::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 15% 25%, rgba(102, 126, 234, 0.12) 0%, transparent 45%),
    radial-gradient(circle at 85% 75%, rgba(118, 75, 162, 0.12) 0%, transparent 45%),
    radial-gradient(circle at 50% 50%, rgba(240, 147, 251, 0.08) 0%, transparent 55%);
  animation: backgroundFlow 25s ease infinite;
  pointer-events: none;
  z-index: 0;
}

.relamind-container::after {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(102, 126, 234, 0.02) 2px, rgba(102, 126, 234, 0.02) 4px),
    repeating-linear-gradient(90deg, transparent, transparent 2px, rgba(118, 75, 162, 0.02) 2px, rgba(118, 75, 162, 0.02) 4px);
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
  background: rgba(26, 29, 41, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid rgba(102, 126, 234, 0.15);
  transition: all 0.3s ease;
}

.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.3), transparent);
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
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.back-button:hover {
  color: rgba(255, 255, 255, 1);
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.4);
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
  background: linear-gradient(135deg, #a8b5ff 0%, #c084fc 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
  animation: gradientFlow 5s ease infinite;
  position: relative;
  filter: drop-shadow(0 2px 8px rgba(102, 126, 234, 0.3));
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

.chat-id {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 6px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.main-content-wrapper {
  width: 100%;
  max-width: 1200px;
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
  flex-direction: column;
  flex: 1;
  padding: 0;
  position: relative;
  z-index: 1;
  width: 100%;
  box-sizing: border-box;
}

.chat-area {
  flex: 1;
  width: 100%;
  padding: 0;
  background: transparent;
  overflow: visible;
  min-height: calc(100vh - 180px);
  position: relative;
  z-index: 1;
  box-sizing: border-box;
}

.footer-container {
  margin-top: auto;
  position: relative;
  z-index: 1;
}

.footer-container :deep(.app-footer) {
  background: rgba(26, 29, 41, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  color: rgba(255, 255, 255, 0.7);
  border-top: 1px solid rgba(102, 126, 234, 0.2);
}

.footer-container :deep(.app-footer::before) {
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
}

.footer-container :deep(.footer-logo h3) {
  background: linear-gradient(135deg, #a8b5ff 0%, #c084fc 100%);
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
  color: rgba(168, 181, 255, 1);
}

.footer-container :deep(.footer-bottom) {
  color: rgba(255, 255, 255, 0.5);
  border-top: 1px solid rgba(102, 126, 234, 0.15);
}

.footer-container :deep(.author) {
  color: rgba(255, 255, 255, 0.4);
}

.footer-container :deep(.author-link) {
  color: rgba(168, 181, 255, 0.8);
}

.footer-container :deep(.author-link:hover) {
  color: rgba(192, 132, 252, 1);
}

/* 响应式样式 */
@media (max-width: 768px) {
  .main-content-wrapper {
    padding: 0 16px;
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
  
  .chat-id {
    font-size: 11px;
    width: 100%;
    text-align: center;
    margin-top: 8px;
    order: 3;
  }
  
  .chat-area {
    min-height: calc(100vh - 220px);
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
  
  .chat-id {
    display: none;
  }
  
  .chat-area {
    min-height: calc(100vh - 180px);
  }
}
</style>

