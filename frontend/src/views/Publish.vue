<template>
  <div class="publish-page">
    <el-card shadow="hover">
      <template #header>
        <h2>📝 发布书籍</h2>
      </template>

      <el-form
        ref="publishFormRef"
        :model="publishForm"
        :rules="rules"
        label-width="120px"
        size="large"
      >
        <el-divider content-position="left">书籍基本信息</el-divider>

        <el-form-item label="ISBN" prop="isbn">
          <el-input
            v-model="publishForm.isbn"
            placeholder="请输入ISBN（国际标准书号）"
            clearable
          >
            <template #prepend>ISBN</template>
          </el-input>
        </el-form-item>

        <el-form-item label="书名" prop="title">
          <el-input
            v-model="publishForm.title"
            placeholder="请输入书名"
            clearable
          />
        </el-form-item>

        <el-form-item label="作者" prop="author">
          <el-input
            v-model="publishForm.author"
            placeholder="请输入作者"
            clearable
          />
        </el-form-item>

        <el-form-item label="出版社" prop="publisher">
          <el-input
            v-model="publishForm.publisher"
            placeholder="请输入出版社"
            clearable
          />
        </el-form-item>

        <el-form-item label="出版年份" prop="publicationYear">
          <el-input
            v-model="publishForm.publicationYear"
            placeholder="请输入出版年份，如：2020"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="书籍分类" prop="categoryId">
          <el-select
            v-model="publishForm.categoryId"
            placeholder="请选择书籍分类"
            style="width: 200px"
            v-loading="categoriesLoading"
          >
            <el-option
              v-for="category in categories"
              :key="category.categoryId"
              :label="category.categoryName"
              :value="category.categoryId"
            />
          </el-select>
          <el-text type="danger" style="margin-left: 10px">* 必选</el-text>
        </el-form-item>

        <el-form-item label="封面图片" prop="coverImageUrl">
          <div class="cover-upload-container">
            <!-- 上传区域 -->
            <el-upload
              class="cover-uploader"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload"
              accept="image/*"
            >
              <div v-if="publishForm.coverImageUrl" class="cover-preview">
                <el-image
                  :src="publishForm.coverImageUrl"
                  fit="cover"
                  class="cover-image"
                />
                <div class="cover-mask">
                  <el-icon><Refresh /></el-icon>
                  <span>重新上传</span>
                </div>
              </div>
              <div v-else class="cover-placeholder" v-loading="uploading">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">点击上传封面</div>
                <div class="upload-tip">支持 JPG、PNG 格式</div>
              </div>
            </el-upload>
            
            <!-- 删除按钮 -->
            <el-button
              v-if="publishForm.coverImageUrl"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleRemoveImage"
              style="margin-top: 10px"
            >
              删除图片
            </el-button>
          </div>
        </el-form-item>

        <el-divider content-position="left">发布信息</el-divider>

        <el-form-item label="发布类型" prop="listingType">
          <el-radio-group v-model="publishForm.listingType">
            <el-radio value="出售">出售</el-radio>
            <el-radio value="赠送">赠送</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="价格" prop="price" v-if="publishForm.listingType === '出售'">
          <el-input-number
            v-model="publishForm.price"
            :min="0"
            :step="0.1"
            :precision="2"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #909399">元</span>
        </el-form-item>

        <el-form-item label="新旧程度" prop="conditionDesc">
          <el-select
            v-model="publishForm.conditionDesc"
            placeholder="请选择"
            style="width: 200px"
          >
            <el-option label="全新" value="全新" />
            <el-option label="九成新" value="九成新" />
            <el-option label="八成新" value="八成新" />
            <el-option label="七成新" value="七成新" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="publishForm.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述书籍的状况、内容简介等（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handlePublish">
            <el-icon><Upload /></el-icon>
            发布
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 发布提示 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <h3>📢 发布须知</h3>
      </template>
      <el-alert
        title="发布提示"
        type="info"
        :closable="false"
      >
        <ul style="padding-left: 20px; margin: 10px 0">
          <li>请确保提供准确的书籍信息，包括ISBN、书名、作者等</li>
          <li><strong>必须选择书籍分类</strong>，便于其他同学查找</li>
          <li>请如实描述书籍的新旧程度和状况</li>
          <li>价格请合理设置，参考市场价格</li>
          <li>发布后请及时关注订单信息，与买家联系完成交易</li>
          <li>交易完成后，您的信誉分将会增加</li>
        </ul>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createListing } from '@/api/listing'
import { getCategories } from '@/api/category'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'
import { Delete, Plus, Refresh } from '@element-plus/icons-vue'

const router = useRouter()

const publishFormRef = ref()
const loading = ref(false)
const uploading = ref(false)
const categoriesLoading = ref(false)
const categories = ref([])

const publishForm = reactive({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publicationYear: '',
  categoryId: undefined,
  coverImageUrl: '',
  listingType: '出售',
  price: 0,
  conditionDesc: '九成新',
  description: ''
})

// 获取分类列表
const fetchCategories = async () => {
  categoriesLoading.value = true
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    categoriesLoading.value = false
  }
}

// 监听发布类型变化
watch(() => publishForm.listingType, (newVal) => {
  if (newVal === '赠送') {
    publishForm.price = 0
  }
})

const rules = {
  isbn: [
    { required: true, message: '请输入ISBN', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择书籍分类', trigger: 'change' }
  ],
  listingType: [
    { required: true, message: '请选择发布类型', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ],
  conditionDesc: [
    { required: true, message: '请选择新旧程度', trigger: 'change' }
  ]
}

const handlePublish = async () => {
  if (!publishFormRef.value) return

  await publishFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 如果是赠送，价格设为0
        const data = {
          ...publishForm,
          price: publishForm.listingType === '赠送' ? 0 : publishForm.price,
          condition: publishForm.conditionDesc // 注意：后端接收的字段名
        }
        
        await createListing(data)
        ElMessage.success('发布成功！')
        router.push('/listings')
      } catch (error) {
        console.error('发布失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleReset = () => {
  publishFormRef.value?.resetFields()
  publishForm.coverImageUrl = ''
}

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

// 处理图片上传
const handleUpload = async ({ file }) => {
  uploading.value = true
  try {
    const res = await uploadImage(file)
    publishForm.coverImageUrl = res.data
    ElMessage.success('图片上传成功！')
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('图片上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

// 删除已上传的图片
const handleRemoveImage = () => {
  publishForm.coverImageUrl = ''
  ElMessage.info('图片已删除')
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.publish-page {
  max-width: 800px;
  margin: 0 auto;
}

.cover-upload-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.cover-uploader {
  width: 180px;
  height: 240px;
}

.cover-uploader :deep(.el-upload) {
  width: 180px;
  height: 240px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.cover-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.cover-preview {
  width: 100%;
  height: 100%;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview:hover .cover-mask {
  opacity: 1;
}

.cover-mask .el-icon {
  font-size: 30px;
  margin-bottom: 8px;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
}

.upload-icon {
  font-size: 40px;
  color: #8c939d;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}
</style>

