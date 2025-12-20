import request from '@/utils/request'

/**
 * AI 智能客服对话
 * @param {string} message - 用户消息
 * @returns {Promise} - AI 响应
 */
export const chatWithAi = (message) => {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: { message }
  })
}


