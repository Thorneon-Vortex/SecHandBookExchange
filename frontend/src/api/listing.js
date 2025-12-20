import request from '@/utils/request'

// 搜索/浏览书籍列表
export const getListings = (params) => {
  return request({
    url: '/listings',
    method: 'get',
    params
  })
}

// 获取书籍详情
export const getListingDetail = (id) => {
  return request({
    url: `/listings/${id}`,
    method: 'get'
  })
}

// 发布书籍
export const createListing = (data) => {
  return request({
    url: '/listings',
    method: 'post',
    data
  })
}

// 下架书籍
export const deleteListing = (id) => {
  return request({
    url: `/listings/${id}`,
    method: 'delete'
  })
}

