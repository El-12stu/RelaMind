<template>
  <div class="privacy-container">
    <div class="content-wrapper">
      <div class="back-button" @click="goBack">
        <span class="back-icon">←</span>
        返回
      </div>
      
      <div class="document-content">
        <h1 class="document-title">隐私政策</h1>
        <div class="document-meta">
          <span class="update-date">最后更新：{{ currentDate }}</span>
        </div>
        
        <div class="document-body">
          <section class="section">
            <h2>1. 引言</h2>
            <p>RelaMind（以下简称"我们"）非常重视您的隐私保护。本隐私政策说明了我们如何收集、使用、存储和保护您的个人信息。</p>
            <p>在使用我们的服务之前，请仔细阅读本隐私政策。如果您不同意本政策的任何内容，请不要使用我们的服务。</p>
          </section>
          
          <section class="section">
            <h2>2. 信息收集</h2>
            <p>我们可能收集以下类型的信息：</p>
            <h3>2.1 您主动提供的信息</h3>
            <ul>
              <li>对话内容和消息记录</li>
              <li>个人成长记录和笔记</li>
              <li>其他您在使用服务时主动输入的信息</li>
            </ul>
            <h3>2.2 自动收集的信息</h3>
            <ul>
              <li>设备信息（如设备类型、操作系统）</li>
              <li>使用数据（如访问时间、功能使用情况）</li>
              <li>技术信息（如 IP 地址、浏览器类型）</li>
            </ul>
          </section>
          
          <section class="section">
            <h2>3. 信息使用</h2>
            <p>我们使用收集的信息用于以下目的：</p>
            <ul>
              <li>提供和改进我们的服务</li>
              <li>处理您的请求和提供客户支持</li>
              <li>分析使用情况以优化用户体验</li>
              <li>确保服务的安全性和稳定性</li>
              <li>遵守法律法规要求</li>
            </ul>
          </section>
          
          <section class="section">
            <h2>4. 信息存储</h2>
            <p>我们采用合理的技术手段和管理措施来保护您的个人信息安全：</p>
            <ul>
              <li>数据加密传输和存储</li>
              <li>访问控制和权限管理</li>
              <li>定期安全审计和更新</li>
            </ul>
            <p>我们会在必要的时间内保留您的信息，除非法律要求或您主动要求删除。</p>
          </section>
          
          <section class="section">
            <h2>5. 信息共享</h2>
            <p>我们承诺不会向第三方出售、交易或出租您的个人信息。在以下情况下，我们可能会共享您的信息：</p>
            <ul>
              <li>获得您的明确同意</li>
              <li>法律法规要求</li>
              <li>保护我们的权利和财产</li>
              <li>紧急情况下的安全需要</li>
            </ul>
          </section>
          
          <section class="section">
            <h2>6. 您的权利</h2>
            <p>您对自己的个人信息享有以下权利：</p>
            <ul>
              <li>访问权：查看我们持有的您的个人信息</li>
              <li>更正权：更正不准确的个人信息</li>
              <li>删除权：要求删除您的个人信息</li>
              <li>撤回同意：撤回您之前给予的同意</li>
            </ul>
            <p>如需行使上述权利，请通过 GitHub Issues 联系我们。</p>
          </section>
          
          <section class="section">
            <h2>7. Cookie 和类似技术</h2>
            <p>我们可能使用 Cookie 和类似技术来改善您的使用体验。您可以通过浏览器设置管理 Cookie 偏好。</p>
          </section>
          
          <section class="section">
            <h2>8. 儿童隐私</h2>
            <p>我们的服务不面向 13 岁以下的儿童。我们不会故意收集儿童的个人信息。如果您发现我们收集了儿童信息，请立即联系我们。</p>
          </section>
          
          <section class="section">
            <h2>9. 政策更新</h2>
            <p>我们可能会不时更新本隐私政策。更新后的政策将在本页面公布，并在显著位置标注更新日期。</p>
            <p>我们建议您定期查看本隐私政策，以了解我们如何保护您的信息。</p>
          </section>
          
          <section class="section">
            <h2>10. 联系我们</h2>
            <p>如果您对本隐私政策有任何疑问、意见或建议，请通过以下方式联系我们：</p>
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
  title: '隐私政策 - RelaMind',
  meta: [
    {
      name: 'description',
      content: 'RelaMind 隐私政策'
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
.privacy-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

.privacy-container::before {
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

.section h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #444;
  margin-top: 20px;
  margin-bottom: 10px;
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
  .privacy-container {
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

