<template>
  <div class="diary-container">
    <div class="header">
      <div class="back-button" @click="goBack">返回</div>
      <div class="title-section">
        <h1 class="title">📝 记录心情</h1>
        <div class="subtitle">记录你的感想和心情，让 RelaMind 更好地了解你</div>
      </div>
      <div class="date-display">{{ currentDate }}</div>
    </div>
    
    <div class="main-content-wrapper">
      <div class="diary-form">
        <!-- 心情选择 -->
        <div class="form-section">
          <label class="form-label">今天的心情</label>
          <div class="mood-selector">
            <div 
              v-for="mood in moods" 
              :key="mood.value"
              class="mood-item"
              :class="{ active: selectedMood === mood.value }"
              @click="selectedMood = mood.value"
            >
              <span class="mood-emoji">{{ mood.emoji }}</span>
              <span class="mood-text">{{ mood.label }}</span>
            </div>
          </div>
        </div>

        <!-- 标签选择 -->
        <div class="form-section">
          <label class="form-label">标签（可选）</label>
          <div class="tags-input">
            <div class="tag-list">
              <span 
                v-for="(tag, index) in selectedTags" 
                :key="index"
                class="tag-item"
              >
                {{ tag }}
                <span class="tag-remove" @click="removeTag(index)">×</span>
              </span>
            </div>
            <input 
              v-model="tagInput"
              type="text"
              class="tag-input"
              placeholder="输入标签后按回车添加"
              @keyup.enter="addTag"
            />
          </div>
          <div class="common-tags">
            <span 
              v-for="tag in commonTags" 
              :key="tag"
              class="common-tag"
              @click="addCommonTag(tag)"
            >
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- 日记内容 -->
        <div class="form-section">
          <label class="form-label">记录你的感想</label>
          <textarea
            v-model="diaryContent"
            class="diary-textarea"
            placeholder="今天发生了什么？你有什么感想？记录下来，让 RelaMind 帮你回顾和分析..."
            rows="12"
          ></textarea>
          <div class="char-count">{{ diaryContent.length }} 字</div>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <button 
            class="btn btn-secondary" 
            @click="clearForm"
            :disabled="isSaving"
          >
            清空
          </button>
          <button 
            class="btn btn-primary" 
            @click="saveDiary"
            :disabled="isSaving || !diaryContent.trim()"
          >
            <span v-if="isSaving">保存中...</span>
            <span v-else>💾 保存日记</span>
          </button>
        </div>

        <!-- 保存提示 -->
        <div v-if="saveMessage" :class="['save-message', saveSuccess ? 'success' : 'error']">
          {{ saveMessage }}
        </div>
      </div>
    </div>
    
    <div class="footer-container">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppFooter from '../components/AppFooter.vue'
import { saveDiary as saveDiaryAPI } from '../api'

// 设置页面标题和元数据
useHead({
  title: '记录心情 - RelaMind',
  meta: [
    {
      name: 'description',
      content: '记录你的感想和心情，让 RelaMind 更好地了解你'
    }
  ]
})

const router = useRouter()

// 数据
const diaryContent = ref('')
const selectedMood = ref('')
const selectedTags = ref([])
const tagInput = ref('')
const isSaving = ref(false)
const saveMessage = ref('')
const saveSuccess = ref(false)

// 心情选项
const moods = [
  { value: 'happy', label: '开心', emoji: '😊' },
  { value: 'excited', label: '兴奋', emoji: '🤩' },
  { value: 'calm', label: '平静', emoji: '😌' },
  { value: 'tired', label: '疲惫', emoji: '😴' },
  { value: 'sad', label: '难过', emoji: '😢' },
  { value: 'anxious', label: '焦虑', emoji: '😰' },
  { value: 'angry', label: '生气', emoji: '😠' },
  { value: 'confused', label: '困惑', emoji: '😕' }
]

// 常用标签
const commonTags = ['工作', '学习', '生活', '情感', '健康', '旅行', '美食', '运动', '思考', '成长']

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${now.getFullYear()}年${String(now.getMonth() + 1).padStart(2, '0')}月${String(now.getDate()).padStart(2, '0')}日 ${weekdays[now.getDay()]}`
})

// 添加标签
const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && !selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
    tagInput.value = ''
  }
}

// 添加常用标签
const addCommonTag = (tag) => {
  if (!selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
  }
}

// 移除标签
const removeTag = (index) => {
  selectedTags.value.splice(index, 1)
}

// 清空表单
const clearForm = () => {
  diaryContent.value = ''
  selectedMood.value = ''
  selectedTags.value = []
  tagInput.value = ''
  saveMessage.value = ''
}

// 保存日记
const saveDiary = async () => {
  if (!diaryContent.value.trim()) {
    saveMessage.value = '请输入日记内容'
    saveSuccess.value = false
    return
  }

  isSaving.value = true
  saveMessage.value = ''

  try {
    const result = await saveDiaryAPI({
      content: diaryContent.value,
      mood: selectedMood.value || null,
      tags: selectedTags.value.length > 0 ? selectedTags.value : null
    })

    if (result.success) {
      saveSuccess.value = true
      saveMessage.value = result.message || '日记保存成功！已存储到知识库中，你可以在聊天时询问相关记录。'
      
      // 3秒后清空表单
      setTimeout(() => {
        clearForm()
      }, 3000)
    } else {
      saveSuccess.value = false
      saveMessage.value = result.message || '保存失败，请稍后重试'
    }
  } catch (error) {
    saveSuccess.value = false
    saveMessage.value = '保存失败：' + (error.response?.data?.message || error.message || '网络错误')
  } finally {
    isSaving.value = false
  }
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 页面加载时的提示
onMounted(() => {
  // 可以添加一些引导提示
})
</script>

<style scoped>
.diary-container {
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

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: rgba(26, 29, 41, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.3);
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid rgba(102, 126, 234, 0.15);
}

.back-button {
  font-size: 16px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  padding: 8px 16px;
  border-radius: 8px;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.back-button:hover {
  color: rgba(255, 255, 255, 1);
  background: rgba(102, 126, 234, 0.2);
  transform: translateX(-4px);
}

.back-button:before {
  content: '←';
  margin-right: 6px;
}

.title-section {
  flex: 1;
  text-align: center;
}

.title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #a8b5ff 0%, #c084fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 4px;
}

.date-display {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 6px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.main-content-wrapper {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;
  flex: 1;
}

.diary-form {
  background: rgba(37, 41, 54, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 32px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.form-section {
  margin-bottom: 32px;
}

.form-label {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16px;
}

.mood-selector {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.mood-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: rgba(102, 126, 234, 0.05);
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mood-item:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.4);
  transform: translateY(-2px);
}

.mood-item.active {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.6);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.mood-emoji {
  font-size: 32px;
  margin-bottom: 8px;
}

.mood-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  background: rgba(37, 41, 54, 0.6);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 12px;
  min-height: 50px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.2);
  border: 1px solid rgba(102, 126, 234, 0.4);
  border-radius: 20px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

.tag-remove {
  margin-left: 6px;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  opacity: 0.7;
}

.tag-remove:hover {
  opacity: 1;
}

.tag-input {
  flex: 1;
  min-width: 120px;
  background: transparent;
  border: none;
  outline: none;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.tag-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.common-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.common-tag {
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 16px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.2s ease;
}

.common-tag:hover {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.4);
  color: rgba(255, 255, 255, 0.9);
}

.diary-textarea {
  width: 100%;
  padding: 16px;
  background: rgba(37, 41, 54, 0.6);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  line-height: 1.6;
  resize: vertical;
  outline: none;
  transition: all 0.3s ease;
  font-family: inherit;
}

.diary-textarea:focus {
  border-color: rgba(102, 126, 234, 0.6);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.diary-textarea::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 8px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover:not(:disabled) {
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-2px);
}

.save-message {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
}

.save-message.success {
  background: rgba(16, 185, 129, 0.2);
  border: 1px solid rgba(16, 185, 129, 0.4);
  color: rgba(16, 185, 129, 1);
}

.save-message.error {
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.4);
  color: rgba(239, 68, 68, 1);
}

.footer-container {
  margin-top: auto;
}

.footer-container :deep(.app-footer) {
  background: rgba(26, 29, 41, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  color: rgba(255, 255, 255, 0.7);
  border-top: 1px solid rgba(102, 126, 234, 0.2);
}

/* 响应式 */
@media (max-width: 768px) {
  .mood-selector {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }
  
  .mood-item {
    padding: 12px;
  }
  
  .mood-emoji {
    font-size: 24px;
  }
  
  .diary-form {
    padding: 24px;
  }
  
  .header {
    flex-wrap: wrap;
  }
  
  .date-display {
    width: 100%;
    text-align: center;
    margin-top: 8px;
    order: 3;
  }
}
</style>

