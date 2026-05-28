/**
 * 文件上传 API 请求封装
 * @author aceFelix
 */
import request from '@/utils/request'

/**
 * 上传文件到 OSS
 * @param {File} file - 要上传的文件对象
 * @returns {Promise} 上传结果
 */
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
