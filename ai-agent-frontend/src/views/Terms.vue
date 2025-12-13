<template>
  <div class="terms-container">
    <div class="content-wrapper">
      <div class="back-button" @click="goBack">
        <span class="back-icon">←</span>
        返回
      </div>
      
      <div class="document-content">
        <h1 class="document-title">用户协议</h1>
        <div class="document-meta">
          <span class="update-date">最后更新：{{ currentDate }}</span>
        </div>
        
        <div class="document-body">
          <section class="section">
            <h2>1. 服务条款</h2>
            <p>欢迎使用 RelaMind（以下简称"本服务"）。在使用本服务之前，请仔细阅读本用户协议（以下简称"本协议"）。</p>
            <p>通过访问或使用本服务，您表示同意遵守本协议的所有条款和条件。如果您不同意本协议的任何部分，请不要使用本服务。</p>
          </section>
          
          <section class="section">
            <h2>2. 服务描述</h2>
            <p>RelaMind 是一个 AI 驱动的个人成长伙伴平台，旨在帮助用户记录成长、理解自己、成为更好的自己。</p>
            <p>我们提供的服务包括但不限于：</p>
            <ul>
              <li>AI 成长伙伴对话服务</li>
              <li>AI 超级智能体服务</li>
              <li>个人成长记录与分析</li>
            </ul>
          </section>
          
          <section class="section">
            <h2>3. 用户责任</h2>
            <p>您在使用本服务时，应当：</p>
            <ul>
              <li>遵守相关法律法规和本协议的规定</li>
              <li>不得利用本服务从事任何违法或不当行为</li>
              <li>保护您的账户信息安全</li>
              <li>对您在本服务中的行为承担责任</li>
            </ul>
          </section>
          
          <section class="section">
            <h2>4. 隐私保护</h2>
            <p>我们重视您的隐私保护。关于我们如何收集、使用和保护您的个人信息，请参阅我们的《隐私政策》。</p>
          </section>
          
          <section class="section">
            <h2>5. 服务变更与终止</h2>
            <p>我们保留随时修改、暂停或终止本服务的权利，无需事先通知。我们不对因服务变更、暂停或终止而造成的任何损失承担责任。</p>
          </section>
          
          <section class="section">
            <h2>6. 免责声明</h2>
            <p>本服务按"现状"提供，我们不保证服务的准确性、完整性或可靠性。您使用本服务的风险由您自行承担。</p>
          </section>
          
          <section class="section">
            <h2>7. 协议修改</h2>
            <p>我们保留随时修改本协议的权利。修改后的协议将在本页面公布，修改后的协议自公布之日起生效。</p>
            <p>如果您继续使用本服务，即表示您接受修改后的协议。</p>
          </section>
          
          <section class="section">
            <h2>8. 联系我们</h2>
            <p>如果您对本协议有任何疑问，请通过以下方式联系我们：</p>
            <ul>
              <li>GitHub Issues: <a href="https://github.com/El-12stu/RelaMind/issues" target="_blank" rel="noopener noreferrer">https://github.com/El-12stu/RelaMind/issues</a></li>
            </ul>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'

useHead({
  title: '用户协议 - RelaMind',
  meta: [
    {
      name: 'description',
      content: 'RelaMind 用户协议'
    }
  ]
})

const router = useRouter()

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.terms-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

.terms-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(118, 75, 162, 0.1) 0%, transparent 50%);
  animation: gradientShift 15s ease infinite;
  pointer-events: none;
}

@keyframes gradientShift {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.8;
  }
}

.content-wrapper {
  max-width: 900px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: white;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  margin-bottom: 30px;
  padding: 8px 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(-4px);
}

.back-icon {
  font-size: 18px;
}

.document-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 50px 60px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.document-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.document-meta {
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.update-date {
  color: #999;
  font-size: 14px;
}

.document-body {
  line-height: 1.8;
  color: #444;
}

.section {
  margin-bottom: 40px;
}

.section h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.section p {
  margin-bottom: 15px;
  color: #555;
}

.section ul {
  margin: 15px 0;
  padding-left: 25px;
}

.section li {
  margin-bottom: 10px;
  color: #555;
}

.section a {
  color: #667eea;
  text-decoration: none;
  transition: color 0.2s;
}

.section a:hover {
  color: #764ba2;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .terms-container {
    padding: 20px 15px;
  }
  
  .document-content {
    padding: 30px 25px;
    border-radius: 16px;
  }
  
  .document-title {
    font-size: 2rem;
  }
  
  .section h2 {
    font-size: 1.3rem;
  }
}

@media (max-width: 480px) {
  .document-content {
    padding: 25px 20px;
  }
  
  .document-title {
    font-size: 1.75rem;
  }
  
  .back-button {
    font-size: 14px;
    padding: 6px 12px;
  }
}
</style>

