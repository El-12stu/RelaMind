<template>
  <div class="chat-container">
    <!-- 聊天记录区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
        <!-- AI消息 -->
        <div v-if="!msg.isUser" 
             class="message ai-message" 
             :class="[msg.type]">
          <div class="avatar ai-avatar">
            <AiAvatarFallback :type="aiType" />
          </div>
          <div class="message-bubble">
            <div class="message-content">
              <div class="message-text">{{ msg.content }}</div>
              <!-- 文件下载链接 -->
              <div v-if="msg.files && msg.files.length > 0" class="file-downloads">
                <div v-for="(file, fileIndex) in msg.files" :key="fileIndex" class="file-item">
                  <span class="file-icon">📁</span>
                  <span class="file-name">{{ file.fileName }}</span>
                  <a :href="file.downloadUrl" :download="file.fileName" class="download-link">
                    <span class="download-icon">⬇️</span>
                    下载
                  </a>
                </div>
              </div>
              <span v-if="connectionStatus === 'connecting' && index === messages.length - 1" class="typing-indicator">▋</span>
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>
        
        <!-- 用户消息 -->
        <div v-else class="message user-message" :class="[msg.type]">
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
          <div class="avatar user-avatar">
            <div class="avatar-placeholder">我</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-container">
      <div class="chat-input">
        <textarea 
          v-model="inputMessage" 
          @keydown.enter.prevent="sendMessage"
          placeholder="请输入消息..." 
          class="input-box"
          :disabled="connectionStatus === 'connecting'"
        ></textarea>
        <button 
          @click="sendMessage" 
          class="send-button"
          :disabled="connectionStatus === 'connecting' || !inputMessage.trim()"
        >发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch, computed } from 'vue'
import AiAvatarFallback from './AiAvatarFallback.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  },
  aiType: {
    type: String,
    default: 'default'  // 'relamind' 或 'super'
  }
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)

// 根据AI类型选择不同头像
const aiAvatar = computed(() => {
  if (props.aiType === 'relamind') {
    return '/ai-relamind-avatar.png'  // RelaMind 头像
  } else if (props.aiType === 'super') {
    return '/ai-super-avatar.png' // 超级智能体头像
  } else {
    return '/ai-default-avatar.png' // 默认头像
  }
})

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) return
  
  emit('send-message', inputMessage.value)
  inputMessage.value = ''
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 自动滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 监听消息变化与内容变化，自动滚动
watch(() => props.messages.length, () => {
  scrollToBottom()
})

watch(() => props.messages.map(m => m.content).join(''), () => {
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 180px);
  min-height: 600px;
  background: transparent;
  overflow: hidden;
  position: relative;
  padding: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  padding-bottom: 100px; /* 为输入框留出空间 */
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 1200px;
  bottom: 88px; /* 与输入框高度相匹配 */
  box-sizing: border-box;
}

/* 自定义滚动条样式 - 深色主题 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.4);
  border-radius: 3px;
  transition: background 0.3s;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.6);
}

.message-wrapper {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  width: 100%;
  animation: fadeInUp 0.4s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: 85%;
  margin-bottom: 8px;
}

.user-message {
  margin-left: auto; /* 用户消息靠右 */
  flex-direction: row; /* 正常顺序，先气泡后头像 */
}

.ai-message {
  margin-right: auto; /* AI消息靠左 */
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  margin-left: 8px; /* 用户头像在右侧，左边距 */
}

.ai-avatar {
  margin-right: 8px; /* AI头像在左侧，右边距 */
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 20px;
  position: relative;
  word-wrap: break-word;
  min-width: 100px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.9) 0%, rgba(118, 75, 162, 0.9) 100%);
  color: rgba(255, 255, 255, 0.95);
  border-bottom-right-radius: 6px;
  text-align: left;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.user-message .message-bubble:hover {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  transform: translateY(-1px);
}

.ai-message .message-bubble {
  background: rgba(37, 41, 54, 0.8);
  backdrop-filter: blur(20px);
  color: rgba(255, 255, 255, 0.9);
  border-bottom-left-radius: 6px;
  text-align: left;
  border: 1px solid rgba(102, 126, 234, 0.15);
}

.ai-message .message-bubble:hover {
  background: rgba(37, 41, 54, 0.9);
  border-color: rgba(102, 126, 234, 0.25);
}

.message-content {
  font-size: 15px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.user-message .message-content {
  color: rgba(255, 255, 255, 0.95);
}

.ai-message .message-content {
  color: rgba(255, 255, 255, 0.9);
}

.message-time {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 6px;
  text-align: right;
  color: rgba(255, 255, 255, 0.5);
}

.chat-input-container {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 1200px;
  background: rgba(26, 29, 41, 0.9);
  backdrop-filter: blur(20px) saturate(180%);
  border-top: 1px solid rgba(102, 126, 234, 0.2);
  z-index: 100;
  height: 88px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.3);
  padding: 0 24px;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.chat-input {
  display: flex;
  padding: 0;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  align-items: center;
  gap: 12px;
}

.input-box {
  flex-grow: 1;
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 24px;
  padding: 12px 20px;
  font-size: 15px;
  resize: none;
  min-height: 24px;
  max-height: 48px;
  outline: none;
  transition: all 0.3s ease;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  background: rgba(37, 41, 54, 0.6);
  backdrop-filter: blur(10px);
  color: rgba(255, 255, 255, 0.9);
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.2);
}

.input-box::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.input-box::-webkit-scrollbar {
  display: none;
}

.input-box:focus {
  border-color: rgba(102, 126, 234, 0.6);
  background: rgba(37, 41, 54, 0.8);
  box-shadow: 
    inset 0 2px 8px rgba(0, 0, 0, 0.2),
    0 0 0 3px rgba(102, 126, 234, 0.1);
}

.send-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: 200% 200%;
  color: white;
  border: none;
  border-radius: 24px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 48px;
  align-self: center;
  box-shadow: 
    0 4px 12px rgba(102, 126, 234, 0.3),
    0 2px 6px rgba(118, 75, 162, 0.2);
  animation: buttonGradient 4s ease infinite;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

@keyframes buttonGradient {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.send-button:hover:not(:disabled) {
  box-shadow: 
    0 6px 16px rgba(102, 126, 234, 0.4),
    0 4px 10px rgba(118, 75, 162, 0.3);
  transform: translateY(-1px);
}

.send-button:active:not(:disabled) {
  transform: translateY(0);
}

.typing-indicator {
  display: inline-block;
  animation: blink 0.7s infinite;
  margin-left: 4px;
  color: rgba(168, 181, 255, 0.8);
  font-weight: bold;
}

.message-text {
  margin-bottom: 8px;
}

.file-downloads {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin-bottom: 8px;
  background: rgba(16, 185, 129, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(16, 185, 129, 0.2);
  transition: all 0.3s ease;
}

.file-item:hover {
  background: rgba(16, 185, 129, 0.15);
  border-color: rgba(16, 185, 129, 0.3);
}

.file-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.file-name {
  flex: 1;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  word-break: break-all;
}

.download-link {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%);
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.download-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.download-icon {
  font-size: 14px;
}

@keyframes blink {
  0% { opacity: 0.3; }
  50% { opacity: 1; }
  100% { opacity: 0.3; }
}

.input-box:disabled, .send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message {
    max-width: 95%;
  }
  
  .message-content {
    font-size: 15px;
  }
  
  .chat-input {
    padding: 12px;
  }
  
  .input-box {
    padding: 8px 12px;
  }
  
  .send-button {
    padding: 0 15px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .avatar {
    width: 32px;
    height: 32px;
  }
  
  .message-bubble {
    padding: 10px;
  }
  
  .message-content {
    font-size: 14px;
  }
  
  .chat-input-container {
    height: 80px;
    padding: 0 16px;
  }
  
  .chat-messages {
    bottom: 80px;
    padding: 16px;
    padding-bottom: 90px;
  }
  
  .input-box {
    padding: 10px 16px;
    font-size: 14px;
  }
  
  .send-button {
    padding: 0 20px;
    height: 44px;
    font-size: 14px;
  }
}

/* 新增：不同类型消息的样式 */
.ai-answer {
  animation: fadeIn 0.3s ease-in-out;
}

.ai-final {
  /* 最终回答，可以有不同的样式，例如边框高亮等 */
}

.ai-error {
  opacity: 0.7;
}

.user-question {
  /* 用户提问的特殊样式 */
}

/* 连续消息气泡样式 */
.ai-message + .ai-message {
  margin-top: 4px;
}

.ai-message + .ai-message .avatar {
  visibility: hidden;
}

.ai-message + .ai-message .message-bubble {
  border-top-left-radius: 10px;
}
</style> 