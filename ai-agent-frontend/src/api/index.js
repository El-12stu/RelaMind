import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const API_BASE_URL = process.env.NODE_ENV === 'production' 
 ? '/api' // 生产环境使用相对路径，适用于前后端部署在同一域名下
 : 'http://localhost:8123/api' // 开发环境指向本地后端服务

// 创建axios实例
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000
})

// 封装SSE连接
export const connectSSE = (url, params, onMessage, onError) => {
  // 构建带参数的URL
  const queryString = Object.keys(params)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  
  const fullUrl = `${API_BASE_URL}${url}?${queryString}`
  
  // 创建EventSource
  const eventSource = new EventSource(fullUrl)
  
  eventSource.onmessage = event => {
    let data = event.data
    
    // 检查是否是特殊标记
    if (data === '[DONE]') {
      if (onMessage) onMessage('[DONE]')
    } else {
      // 处理普通消息
      if (onMessage) onMessage(data)
    }
  }
  
  eventSource.onerror = error => {
    if (onError) onError(error)
    eventSource.close()
  }
  
  // 返回eventSource实例，以便后续可以关闭连接
  return eventSource
}

/**
 * RelaMind 主聊天接口（智能路由）
 * 
 * 功能：
 * - 自动识别意图（普通聊天 / 历史查询 / 工具调用）
 * - 自动选择最合适的处理方式
 * - 支持工具调用和历史记录检索
 * 
 * @param {string} message - 用户消息
 * @param {string} chatId - 会话ID
 * @returns {EventSource} SSE 连接
 */
export const chatWithRelaMind = (message, chatId) => {
  return connectSSE('/ai/chat', { message, chatId })
}

/**
 * 高级智能体接口（Manus）
 * 
 * 功能：
 * - 自主规划复杂任务
 * - 多步骤推理和执行
 * 
 * @param {string} message - 用户消息
 * @returns {EventSource} SSE 连接
 */
export const chatWithManus = (message) => {
  return connectSSE('/ai/manus', { message })
}

/**
 * 保存日记记录
 * 
 * @param {Object} diaryData - 日记数据
 * @param {string} diaryData.userId - 用户ID（可选）
 * @param {string} diaryData.content - 日记内容（必填）
 * @param {string} diaryData.mood - 心情（可选）
 * @param {Array<string>} diaryData.tags - 标签列表（可选）
 * @returns {Promise} 保存结果
 */
export const saveDiary = async (diaryData) => {
  try {
    const response = await request.post('/diary/save', diaryData)
    return response.data
  } catch (error) {
    console.error('保存日记失败:', error)
    throw error
  }
}

/**
 * 快速保存日记（只需要内容）
 * 
 * @param {string} content - 日记内容
 * @returns {Promise} 保存结果
 */
export const quickSaveDiary = async (content) => {
  try {
    const response = await request.post('/diary/quick-save', null, {
      params: { content }
    })
    return response.data
  } catch (error) {
    console.error('快速保存日记失败:', error)
    throw error
  }
}

export default {
  chatWithRelaMind,
  chatWithManus,
  saveDiary,
  quickSaveDiary
} 