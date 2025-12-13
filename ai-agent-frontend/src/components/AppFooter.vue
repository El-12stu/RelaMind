<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-section">
        <div class="footer-logo">
          <h3>RelaMind</h3>
        </div>
        <div class="footer-links">
          <RouterLink to="/terms">《用户协议》</RouterLink>
          <RouterLink to="/privacy">《隐私政策》</RouterLink>
        </div>
      </div>
      
      <div class="footer-section">
        <h4>项目信息</h4>
        <div class="footer-links">
          <a href="https://github.com/El-12stu/RelaMind" target="_blank" rel="noopener noreferrer" class="github-link">
            GitHub 仓库
          </a>
          <a href="https://github.com/El-12stu/RelaMind/blob/main/README.md" target="_blank" rel="noopener noreferrer">
            项目文档
          </a>
        </div>
      </div>
      
      <div class="footer-section">
        <h4>开发者</h4>
        <div class="footer-links">
          <a href="https://github.com/El-12stu" target="_blank" rel="noopener noreferrer">
            <span class="github-icon">👤</span> El-12stu
          </a>
          <a href="https://github.com/El-12stu/RelaMind/issues" target="_blank" rel="noopener noreferrer">
            问题反馈
          </a>
        </div>
      </div>
    </div>
    
    <div class="footer-bottom">
      <p>© {{ currentYear }} RelaMind - 记录成长，理解自己，成为更好的你</p>
      <p class="author">
        Created by 
        <a href="https://github.com/El-12stu" target="_blank" rel="noopener noreferrer" class="author-link">El-12stu</a>
        | 
        <a href="https://github.com/El-12stu/RelaMind" target="_blank" rel="noopener noreferrer" class="author-link">
          View on GitHub
        </a>
      </p>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

// 计算当前年份
const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.app-footer {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.95) 100%);
  backdrop-filter: blur(20px);
  padding: 50px 0 25px;
  color: #666;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  width: 100%;
  margin-top: auto;
  position: relative;
  z-index: 2;
  overflow: hidden;
}

.app-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.3), transparent);
  animation: shimmer 3s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.6;
  }
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  padding: 0 20px;
}

.footer-section {
  flex: 1;
  min-width: 200px;
  margin-bottom: 30px;
  padding: 0 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.footer-section:first-child {
  align-items: flex-start;
  text-align: left;
}

.footer-section:last-child {
  align-items: flex-end;
  text-align: right;
}

.footer-logo h3 {
  font-size: 1.25rem;
  margin-bottom: 15px;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% 200%;
  animation: gradientFlow 5s ease infinite;
}

@keyframes gradientFlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.footer-section h4 {
  font-size: 1rem;
  margin-bottom: 15px;
  color: #333;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.footer-section h4::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.footer-links {
  display: flex;
  flex-direction: column;
}

.footer-links a {
  margin-bottom: 10px;
  color: #666;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.footer-links a::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

.footer-links a:hover {
  color: #667eea;
  transform: translateX(2px);
}

.footer-links a:hover::after {
  width: 100%;
}

.qrcode {
  display: flex;
  align-items: center;
}

.qrcode-container {
  text-align: center;
}

.qrcode-placeholder {
  width: 90px;
  height: 90px;
  background-color: #f5f5f5;
  margin: 0 auto 10px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
}

.qrcode-placeholder:after {
  content: '🤖';
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  margin-top: 20px;
  border-top: 1px solid #eee;
  color: #999;
  font-size: 0.9rem;
}

.author {
  margin-top: 8px;
  font-size: 0.85rem;
  color: #999;
}

.author-link {
  color: #667eea;
  text-decoration: none;
  transition: color 0.2s;
}

.author-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.github-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.github-link:hover {
  transform: translateY(-2px);
}

.github-icon {
  font-size: 1rem;
  transition: transform 0.3s ease;
}

.github-link:hover .github-icon {
  transform: scale(1.1) rotate(5deg);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
  }
  
  .footer-section {
    width: 100%;
    margin-bottom: 20px;
    padding: 0;
    align-items: center !important;
    text-align: center !important;
  }
}

@media (max-width: 480px) {
  .app-footer {
    padding: 30px 0 15px;
  }
  
  .footer-section h4 {
    font-size: 0.95rem;
  }
  
  .footer-links a {
    font-size: 0.9rem;
  }
  
  .qrcode-placeholder {
    width: 80px;
    height: 80px;
  }
}
</style> 