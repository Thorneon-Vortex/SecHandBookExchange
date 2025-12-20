import request from '@/utils/request'

// 用户注册
export const register = (data) => {
  return request({
    url: '/users/register',
    method: 'post',
    data
  })
}

// 用户登录
export const login = (data) => {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export const getUserInfo = () => {
  return request({
    url: '/users/me',
    method: 'get'
  })
}

// 更新用户信息
export const updateUserInfo = (data) => {
  return request({
    url: '/users/me',
    method: 'put',
    data
  })
}

