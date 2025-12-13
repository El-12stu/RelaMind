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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-button {
  font-size: 16px;
  cursor: pointer;
  color: #667eea;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  font-weight: 500;
  flex-shrink: 0;
}

.back-button:hover {
  color: #764ba2;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
  font-weight: 400;
}

.chat-id {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 24px;
}

.chat-area {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  min-height: calc(100vh - 200px);
}

.footer-container {
  margin-top: auto;
}

/* 响应式样式 */
@media (max-width: 768px) {
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
  
  .content-wrapper {
    padding: 16px;
  }
  
  .chat-area {
    min-height: calc(100vh - 220px);
  }
}

@media (max-width: 480px) {
  .header {
    padding: 12px 16px;
  }
  
  .back-button {
    font-size: 14px;
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
  
  .content-wrapper {
    padding: 12px;
  }
  
  .chat-area {
    min-height: calc(100vh - 180px);
    border-radius: 12px;
  }
}
</style>

