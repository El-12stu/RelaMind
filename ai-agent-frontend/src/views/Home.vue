<template>
  <div class="home-container">
    <div class="header">
      <div class="logo-wrapper">
        <h1 class="main-title">RelaMind</h1>
      </div>
      <p class="subtitle">记录成长，理解自己，成为更好的你</p>
      <div class="divider-line"></div>
    </div>
    
    <div class="apps-container">
      <div class="app-card" @click="navigateTo('/relamind')">
        <div class="card-glow"></div>
        <div class="app-icon growth-icon">🌱</div>
        <div class="app-info">
          <div class="app-title">AI 成长伙伴</div>
          <div class="app-desc">倾听你的心声，记录生活点滴，分析成长模式</div>
        </div>
        <div class="app-button">
          <span class="btn-text">开始对话</span>
          <span class="btn-icon">→</span>
        </div>
      </div>
      
      <div class="app-card" @click="navigateTo('/super-agent')">
        <div class="card-glow"></div>
        <div class="app-icon robot-icon">🤖</div>
        <div class="app-info">
          <div class="app-title">AI超级智能体</div>
          <div class="app-desc">全能型AI助手，解决各类专业问题</div>
        </div>
        <div class="app-button">
          <span class="btn-text">立即体验</span>
          <span class="btn-icon">→</span>
        </div>
      </div>
    </div>
    
    <div class="feature-circles">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
    
    <AppFooter />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppFooter from '../components/AppFooter.vue'

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

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  position: relative;
  overflow: hidden;
}

.header {
  padding: 80px 20px 60px;
  text-align: center;
  position: relative;
  z-index: 2;
}

.logo-wrapper {
  margin-bottom: 20px;
}

.main-title {
  font-family: 'Inter', sans-serif;
  font-size: 4rem;
  font-weight: 700;
  color: white;
  margin: 0;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  letter-spacing: -1px;
}

.subtitle {
  font-family: 'Inter', sans-serif;
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 auto 30px;
  max-width: 600px;
  font-weight: 400;
}

.divider-line {
  height: 2px;
  width: 80%;
  max-width: 400px;
  margin: 0 auto;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
}

.apps-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 50px;
  max-width: 1200px;
  margin: 60px auto;
  padding: 0 20px;
  flex: 1;
  position: relative;
  z-index: 2;
}

.app-card {
  width: 360px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  padding: 40px 30px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle at center,
    rgba(102, 126, 234, 0.1) 0%,
    transparent 70%
  );
  opacity: 0;
  transition: opacity 0.5s;
  pointer-events: none;
}

.app-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.3);
}

.app-card:hover .card-glow {
  opacity: 1;
}

.app-icon {
  font-size: 5rem;
  margin-bottom: 30px;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  position: relative;
  z-index: 1;
}

.growth-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.4);
}

.robot-icon {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  box-shadow: 0 8px 30px rgba(245, 87, 108, 0.4);
}

.app-info {
  text-align: center;
  margin-bottom: 30px;
  width: 100%;
}

.app-title {
  font-family: 'Inter', sans-serif;
  font-size: 1.8rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.app-desc {
  font-size: 1rem;
  color: #666;
  line-height: 1.6;
}

.app-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 14px 32px;
  border-radius: 30px;
  font-weight: 500;
  transition: all 0.3s;
  margin-top: auto;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.app-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.7s;
}

.app-button:hover {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  transform: scale(1.05);
}

.app-button:hover::before {
  left: 100%;
}

.btn-text {
  margin-right: 8px;
  letter-spacing: 0.5px;
}

.btn-icon {
  font-size: 1.2rem;
  transition: transform 0.3s;
}

.app-button:hover .btn-icon {
  transform: translateX(4px);
}

/* 背景圆圈动画 */
.feature-circles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -150px;
  right: -150px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  animation: float 20s infinite alternate;
}

.circle-2 {
  width: 600px;
  height: 600px;
  bottom: -250px;
  left: -250px;
  background: linear-gradient(135deg, #764ba2, #f093fb);
  animation: float 25s infinite alternate-reverse;
}

.circle-3 {
  width: 300px;
  height: 300px;
  top: 50%;
  right: 10%;
  background: linear-gradient(135deg, #f093fb, #667eea);
  animation: float 15s infinite alternate;
}

/* 动画效果 */
@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  100% {
    transform: translate(50px, 50px) rotate(10deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 3rem;
  }
  
  .subtitle {
    font-size: 1.1rem;
  }
  
  .apps-container {
    gap: 30px;
    margin: 40px auto;
  }
  
  .app-card {
    width: 100%;
    max-width: 420px;
    padding: 30px 25px;
  }
  
  .app-icon {
    font-size: 4rem;
    width: 90px;
    height: 90px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 60px 15px 40px;
  }
  
  .main-title {
    font-size: 2.5rem;
  }
  
  .subtitle {
    font-size: 1rem;
  }
  
  .apps-container {
    margin: 30px auto;
    padding: 0 15px;
  }
  
  .app-card {
    padding: 25px 20px;
  }
  
  .app-icon {
    font-size: 3.5rem;
    margin-bottom: 20px;
    width: 80px;
    height: 80px;
  }
  
  .app-title {
    font-size: 1.5rem;
  }
  
  .app-desc {
    font-size: 0.9rem;
  }
}
</style> 