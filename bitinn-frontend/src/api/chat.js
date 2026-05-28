/**
 * AI 聊天相关 API 请求封装
 * 包含 SSE 连接构建、消息发送、会话管理等功能的后端接口调用
 * @author aceFelix
 */
import request from '@/utils/request'
import { useTokenStore } from '@/stores/token'

const SSE_BASE = '/api/sse'

/**
 * 构建 SSE 连接 URL，携带 userId 和 token 用于服务端身份验证
 * @param {string} userId - 当前用户 ID
 * @returns {string} SSE 连接地址
 */
export function buildSSEUrl(userId) {
  const tokenStore = useTokenStore()
  return `${SSE_BASE}/connect?userId=${userId}&token=${encodeURIComponent(tokenStore.token || '')}`
}

/** 发送聊天消息（JSON 格式） */
export function sendMessage(conversationId, content, attachments, skill) {
  return request.post('/chat/send', { conversationId, content, attachments, skill })
}

/** 发送聊天消息（含文件，multipart/form-data 格式） */
export function sendMessageWithFiles(conversationId, formData) {
  return request.post('/chat/send', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 获取用户的会话列表 */
export function listConversations() {
  return request.get('/conversation/list')
}

/** 创建新会话 */
export function createConversation(title, model, mode) {
  return request.post('/conversation/create', { title, model, mode })
}

/** 删除指定会话 */
export function deleteConversation(id) {
  return request.delete(`/conversation/${id}`)
}

/** 获取会话中的消息列表 */
export function getMessages(conversationId) {
  return request.get(`/conversation/${conversationId}/messages`)
}

/** 获取单个会话详情 */
export function getConversation(conversationId) {
  return request.get(`/conversation/${conversationId}`)
}
