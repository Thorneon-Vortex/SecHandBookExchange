import request from '@/utils/request'

// 创建订单
export const createOrder = (data) => {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

// 获取我的订单列表
export const getMyOrders = (params) => {
  return request({
    url: '/orders',
    method: 'get',
    params
  })
}

// 确认交易完成
export const completeOrder = (id) => {
  return request({
    url: `/orders/${id}/complete`,
    method: 'put'
  })
}

// 取消订单
export const cancelOrder = (id) => {
  return request({
    url: `/orders/${id}/cancel`,
    method: 'put'
  })
}

