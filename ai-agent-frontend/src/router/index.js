import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: 'RelaMind - AI 个人成长伙伴',
      description: 'RelaMind 是你的 AI 成长伙伴，帮助你记录成长、理解自己、成为更好的你'
    }
  },
  {
    path: '/relamind',
    name: 'RelaMind',
    component: () => import('../views/RelaMind.vue'),
    meta: {
      title: 'RelaMind - AI 个人成长伙伴',
      description: '与 RelaMind 对话，记录成长，理解自己'
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'AI超级智能体 - RelaMind',
      description: 'AI超级智能体是全能助手，能解答各类专业问题'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局导航守卫，设置文档标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router 