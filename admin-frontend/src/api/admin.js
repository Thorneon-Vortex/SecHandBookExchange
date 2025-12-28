import request from '@/utils/request'

// 管理员登录
export const adminLogin = (data) => {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data
  })
}

// 获取数据统计
export const getStatistics = () => {
  return request({
    url: '/admin/dashboard/statistics',
    method: 'get'
  })
}

// 用户管理
export const getUserList = (params) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export const updateUserStatus = (userId, enabled) => {
  return request({
    url: `/admin/users/${userId}/status`,
    method: 'put',
    params: { enabled }
  })
}

// 书籍管理
export const getListingList = (params) => {
  return request({
    url: '/admin/listings',
    method: 'get',
    params
  })
}

export const takeDownListing = (listingId, reason) => {
  return request({
    url: `/admin/listings/${listingId}/take-down`,
    method: 'put',
    params: { reason }
  })
}

// 订单管理
export const getOrderList = (params) => {
  return request({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

// Text-to-SQL 查询
export const textToSqlQuery = (query) => {
  return request({
    url: '/admin/text-to-sql/query',
    method: 'post',
    data: { query }
  })
}

// 数据查询（Text-to-SQL的别名）
export const dataQuery = textToSqlQuery
