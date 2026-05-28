/**
 * 用户相关 API 请求封装
 * 包含注册、登录、信息修改、密码重置等功能的后端接口调用
 * @author aceFelix
 */
import request from '@/utils/request'

/**
 * 用户注册
 * @param {Object} registerData - 注册信息（username, password 等）
 * @returns {Promise} 注册结果
 */
export const userRegisterService = (registerData) => {
  const params = new URLSearchParams()
  for (const key in registerData) {
    params.append(key, registerData[key])
  }
  return request.post('/user/register', params)
}

/**
 * 用户登录
 * @param {Object} loginData - 登录凭证（username, password）
 * @returns {Promise} 登录结果，包含 token
 */
export const userLoginService = (loginData) => {
  const params = new URLSearchParams()
  for (const key in loginData) {
    params.append(key, loginData[key])
  }
  return request.post('/user/login', params)
}

/** 获取当前登录用户信息 */
export const getUserInfoService = () => request.get('/user/info')

/** 更新用户个人信息 */
export const userInfoUpdateService = (userInfo) => request.put('/user/update', userInfo)

/**
 * 更新用户头像
 * @param {string} avatarUrl - 头像 URL
 */
export const userAvatarUpdateService = (avatarUrl) => {
  const params = new URLSearchParams()
  params.append('avatarUrl', avatarUrl)
  return request.patch('/user/updateAvatar', params)
}

/** 重置用户密码 */
export const userResetPasswordService = (data) => request.patch('/user/updatePwd', data)
