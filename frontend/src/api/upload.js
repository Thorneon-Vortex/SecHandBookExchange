import request from '@/utils/request'

/**
 * 上传图片到阿里云OSS
 * @param {File} file 图片文件
 * @returns {Promise} 返回图片URL
 */
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

