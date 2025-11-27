import request from '@/utils/request'

// 获取所有分类
export const getCategories = () => {
  return request({
    url: '/categories',
    method: 'get'
  })
}


