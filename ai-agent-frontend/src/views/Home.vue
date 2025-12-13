<template>
  <div class="home-container">
    <div class="main-content-wrapper">
      <div class="header">
        <div class="logo-wrapper">
          <h1 class="main-title">RelaMind</h1>
        </div>
        <div class="subtitle-wrapper">
          <div class="subtitle-decoration"></div>
          <p class="subtitle">
            <span class="subtitle-text">记录成长</span>
            <span class="subtitle-separator">·</span>
            <span class="subtitle-text">理解自己</span>
            <span class="subtitle-separator">·</span>
            <span class="subtitle-text">成为更好的你</span>
          </p>
          <div class="subtitle-decoration"></div>
        </div>
        <div class="divider-line"></div>
      </div>
      
      <div class="apps-container">
        <div class="app-card" @click="navigateTo('/relamind')">
          <div class="card-glow"></div>
          <div class="app-icon growth-icon">🌱</div>
          <div class="app-info">
            <div class="app-title">智能对话</div>
            <div class="app-desc">日常陪伴、历史回顾、成长分析，智能识别你的需求</div>
          </div>
          <div class="app-button">
            <span class="btn-text">开始对话</span>
            <span class="btn-icon">→</span>
          </div>
        </div>
        
        <div class="app-card" @click="navigateTo('/diary')">
          <div class="card-glow"></div>
          <div class="app-icon diary-icon">📝</div>
          <div class="app-info">
            <div class="app-title">成长日记</div>
            <div class="app-desc">记录感想和心情，构建你的个人成长知识库</div>
          </div>
          <div class="app-button">
            <span class="btn-text">开始记录</span>
            <span class="btn-icon">→</span>
          </div>
        </div>
        
        <div class="app-card" @click="navigateTo('/super-agent')">
          <div class="card-glow"></div>
          <div class="app-icon robot-icon">🤖</div>
          <div class="app-info">
            <div class="app-title">任务助手</div>
            <div class="app-desc">自主规划、多工具协作，完成复杂任务</div>
          </div>
          <div class="app-button">
            <span class="btn-text">立即体验</span>
            <span class="btn-icon">→</span>
          </div>
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

.home-container::before {
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
  animation: backgroundPulse 25s ease infinite;
  pointer-events: none;
  z-index: 0;
}

.home-container::after {
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

@keyframes gridPulse {
  0%, 100% {
    opacity: 0.2;
  }
  50% {
    opacity: 0.4;
  }
}

@keyframes backgroundPulse {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 1;
  }
  25% {
    transform: translate(40px, -40px) scale(1.08);
    opacity: 0.85;
  }
  50% {
    transform: translate(-30px, 30px) scale(0.92);
    opacity: 0.9;
  }
  75% {
    transform: translate(20px, 20px) scale(1.05);
    opacity: 0.88;
  }
}

.main-content-wrapper {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
  padding: 0 24px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.header {
  padding: 40px 0 50px;
  text-align: center;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

.logo-wrapper {
  margin-bottom: 24px;
}

.main-title {
  font-family: 'Inter', sans-serif;
  font-size: 4rem;
  font-weight: 700;
  color: white;
  margin: 0;
  text-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.4),
    0 0 40px rgba(102, 126, 234, 0.4);
  letter-spacing: -1px;
  position: relative;
  animation: titleGlow 3s ease-in-out infinite;
}

@keyframes titleGlow {
  0%, 100% {
    text-shadow: 
      0 4px 20px rgba(0, 0, 0, 0.4),
      0 0 40px rgba(102, 126, 234, 0.4);
  }
  50% {
    text-shadow: 
      0 4px 20px rgba(0, 0, 0, 0.4),
      0 0 50px rgba(118, 75, 162, 0.6),
      0 0 60px rgba(102, 126, 234, 0.5);
  }
}

.subtitle-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin: 0 auto 20px;
  max-width: 700px;
  position: relative;
}

.subtitle-decoration {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(102, 126, 234, 0.3) 50%, 
    transparent 100%);
  position: relative;
}

.subtitle-decoration::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 0 12px rgba(102, 126, 234, 0.6);
  animation: decorationPulse 2s ease-in-out infinite;
}

@keyframes decorationPulse {
  0%, 100% {
    opacity: 0.6;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.2);
  }
}

.subtitle {
  font-family: 'Inter', sans-serif;
  font-size: 1.4rem;
  margin: 0;
  font-weight: 300;
  letter-spacing: 2px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.subtitle-text {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%, 
    rgba(168, 181, 255, 0.9) 50%,
    rgba(192, 132, 252, 0.9) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: subtitleGradient 4s ease infinite;
  position: relative;
  transition: all 0.3s ease;
}

.subtitle-text:hover {
  transform: translateY(-2px);
  filter: drop-shadow(0 4px 8px rgba(102, 126, 234, 0.4));
}

@keyframes subtitleGradient {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.subtitle-separator {
  color: rgba(102, 126, 234, 0.6);
  font-size: 1.2rem;
  font-weight: 300;
  animation: separatorGlow 2s ease-in-out infinite;
  opacity: 0.7;
}

@keyframes separatorGlow {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 0.9;
    transform: scale(1.1);
  }
}

.divider-line {
  height: 2px;
  width: 80%;
  max-width: 400px;
  margin: 20px auto 0;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.5), transparent);
}

.apps-container {
  display: flex;
  flex-wrap: nowrap;
  justify-content: center;
  align-items: stretch;
  gap: 30px;
  width: 100%;
  max-width: 1400px;
  margin: 40px auto 60px;
  padding: 0 24px;
  flex: 1;
  position: relative;
  z-index: 2;
  box-sizing: border-box;
}

.app-card {
  flex: 1;
  min-width: 0;
  width: 100%;
  min-height: 420px;
  background: rgba(37, 41, 54, 0.8);
  backdrop-filter: blur(20px) saturate(180%);
  border-radius: 24px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.3),
    0 8px 25px rgba(102, 126, 234, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  padding: 40px 30px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(102, 126, 234, 0.2);
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
  box-shadow: 
    0 30px 80px rgba(102, 126, 234, 0.3),
    0 15px 40px rgba(118, 75, 162, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border-color: rgba(102, 126, 234, 0.4);
  background: rgba(37, 41, 54, 0.9);
}

.app-card:hover .card-glow {
  opacity: 1;
  animation: glowPulse 2s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
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

.diary-icon {
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
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.app-title {
  font-family: 'Inter', sans-serif;
  font-size: 1.8rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 12px;
}

.app-desc {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  min-height: 3.2em;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: 200% 200%;
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
  box-shadow: 
    0 4px 15px rgba(102, 126, 234, 0.3),
    0 2px 8px rgba(118, 75, 162, 0.2);
  animation: buttonGradient 4s ease infinite;
}

@keyframes buttonGradient {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
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
  box-shadow: 
    0 8px 25px rgba(102, 126, 234, 0.4),
    0 4px 12px rgba(118, 75, 162, 0.3);
  transform: scale(1.05) translateY(-2px);
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
  opacity: 0.08;
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
@media (max-width: 1200px) {
  .apps-container {
    gap: 24px;
    padding: 0 20px;
  }
  
  .app-card {
    padding: 32px 24px;
    min-height: 400px;
  }
  
  .app-title {
    font-size: 1.6rem;
  }
  
  .app-desc {
    font-size: 0.95rem;
    min-height: 3em;
  }
}

@media (max-width: 968px) {
  .apps-container {
    flex-wrap: wrap;
    gap: 24px;
    align-items: stretch;
  }
  
  .app-card {
    flex: 0 0 calc(50% - 12px);
    max-width: none;
    min-height: 380px;
  }
}

@media (max-width: 768px) {
  .main-content-wrapper {
    padding: 0 16px;
  }
  
  .header {
    padding: 30px 0 40px;
  }
  
  .logo-wrapper {
    margin-bottom: 20px;
  }
  
  .main-title {
    font-size: 3rem;
  }
  
  .subtitle-wrapper {
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .divider-line {
    margin: 16px auto 0;
  }
  
  .apps-container {
    margin-top: 30px;
  }
  
  .subtitle {
    font-size: 1.1rem;
    gap: 8px;
    letter-spacing: 1px;
  }
  
  .subtitle-separator {
    font-size: 1rem;
  }
  
  .subtitle-decoration {
    display: none;
  }
  
  .apps-container {
    flex-direction: column;
    gap: 24px;
    margin-top: 40px;
    margin-bottom: 40px;
    padding: 0 16px;
  }
  
  .app-card {
    width: 100%;
    max-width: 100%;
    flex: none;
    padding: 30px 25px;
    min-height: auto;
  }
  
  .app-desc {
    min-height: auto;
  }
  
  .app-icon {
    font-size: 4rem;
    width: 90px;
    height: 90px;
  }
}

@media (max-width: 480px) {
  .main-content-wrapper {
    padding: 0 12px;
  }
  
  .header {
    padding: 24px 0 32px;
  }
  
  .logo-wrapper {
    margin-bottom: 16px;
  }
  
  .main-title {
    font-size: 2.5rem;
  }
  
  .subtitle-wrapper {
    gap: 8px;
    margin-bottom: 12px;
  }
  
  .divider-line {
    margin: 12px auto 0;
  }
  
  .apps-container {
    margin-top: 24px;
  }
  
  .subtitle {
    font-size: 0.95rem;
    gap: 6px;
    letter-spacing: 0.5px;
    flex-direction: column;
  }
  
  .subtitle-separator {
    display: none;
  }
  
  .subtitle-decoration {
    display: none;
  }
  
  .apps-container {
    margin-top: 30px;
    margin-bottom: 30px;
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

/* Footer 深色主题适配 */
.home-container :deep(.app-footer) {
  background: rgba(26, 29, 41, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  color: rgba(255, 255, 255, 0.7);
  border-top: 1px solid rgba(102, 126, 234, 0.2);
}

.home-container :deep(.app-footer::before) {
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
}

.home-container :deep(.footer-logo h3) {
  background: linear-gradient(135deg, #a8b5ff 0%, #c084fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.home-container :deep(.footer-section h4) {
  color: rgba(255, 255, 255, 0.9);
}

.home-container :deep(.footer-links a) {
  color: rgba(255, 255, 255, 0.6);
}

.home-container :deep(.footer-links a:hover) {
  color: rgba(168, 181, 255, 1);
}

.home-container :deep(.footer-bottom) {
  color: rgba(255, 255, 255, 0.5);
  border-top: 1px solid rgba(102, 126, 234, 0.15);
}

.home-container :deep(.author) {
  color: rgba(255, 255, 255, 0.4);
}

.home-container :deep(.author-link) {
  color: rgba(168, 181, 255, 0.8);
}

.home-container :deep(.author-link:hover) {
  color: rgba(192, 132, 252, 1);
}
</style> 