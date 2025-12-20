<template>
  <div class="listing-detail-page">
    <el-card shadow="hover" v-loading="loading">
      <el-button
        type="text"
        @click="$router.back()"
        style="margin-bottom: 20px"
      >
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>

      <div v-if="detail" class="detail-content">
        <el-row :gutter="30">
          <!-- 左侧：图片 -->
          <el-col :xs="24" :md="10">
            <div class="book-image-section">
              <el-image
                :src="detail.book.coverImageUrl || '/placeholder-book.png'"
                fit="contain"
                class="book-image"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon :size="100"><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </el-col>

          <!-- 右侧：详情 -->
          <el-col :xs="24" :md="14">
            <div class="book-detail-section">
              <h1 class="book-title">{{ detail.book.title }}</h1>
              
              <div class="book-meta">
                <el-tag :type="detail.status === '在售' ? 'success' : 'info'" size="large">
                  {{ detail.status }}
                </el-tag>
                <el-tag v-if="detail.listingType === '赠送'" type="success" size="large">
                  免费赠送
                </el-tag>
              </div>

              <div class="price-section">
                <span class="price-label">价格：</span>
                <span class="price-value">
                  {{ detail.listingType === '赠送' ? '免费' : `¥${detail.price}` }}
                </span>
              </div>

              <el-descriptions :column="1" border class="book-info">
                <el-descriptions-item label="作者">
                  {{ detail.book.author }}
                </el-descriptions-item>
                <el-descriptions-item label="出版社">
                  {{ detail.book.publisher || '未提供' }}
                </el-descriptions-item>
                <el-descriptions-item label="出版年份">
                  {{ detail.book.publicationYear || '未提供' }}
                </el-descriptions-item>
                <el-descriptions-item label="ISBN">
                  {{ detail.book.isbn }}
                </el-descriptions-item>
                <el-descriptions-item label="新旧程度">
                  {{ detail.conditionDesc }}
                </el-descriptions-item>
                <el-descriptions-item label="发布时间">
                  {{ formatDate(detail.postTime) }}
                </el-descriptions-item>
              </el-descriptions>

              <el-divider />

              <!-- 卖家信息 -->
              <div class="seller-section">
                <h3>卖家信息</h3>
                <div class="seller-info">
                  <el-avatar :size="50">
                    <el-icon><User /></el-icon>
                  </el-avatar>
                  <div class="seller-details">
                    <div class="seller-name">{{ detail.seller.nickname }}</div>
                    <div class="seller-credit">
                      <el-icon color="#f56c6c"><Medal /></el-icon>
                      信誉分：{{ detail.seller.creditScore }}
                    </div>
                    <div class="seller-contact">
                      <el-icon><Phone /></el-icon>
                      {{ detail.seller.contactInfo }}
                    </div>
                  </div>
                </div>
              </div>

              <el-divider />

              <!-- 操作按钮 -->
              <div class="action-section">
                <el-button
                  v-if="detail.status === '在售' && !isOwner"
                  type="primary"
                  size="large"
                  :icon="ShoppingCart"
                  @click="handleBuy"
                  :disabled="!userStore.isLoggedIn"
                >
                  {{ userStore.isLoggedIn ? '立即购买' : '请先登录' }}
                </el-button>
                <el-button
                  v-if="isOwner"
                  type="danger"
                  size="large"
                  :icon="Delete"
                  @click="handleDelete"
                >
                  下架书籍
                </el-button>
                <el-alert
                  v-if="detail.status !== '在售'"
                  :title="`该书籍当前状态：${detail.status}，无法购买`"
                  type="warning"
                  :closable="false"
                  show-icon
                />
              </div>

              <!-- 书籍描述 -->
              <div v-if="detail.description" class="description-section">
                <h3>详细描述</h3>
                <p class="description-text">{{ detail.description }}</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getListingDetail, deleteListing } from '@/api/listing'
import { createOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref(null)

const isOwner = computed(() => {
  return userStore.userInfo?.userId === detail.value?.seller.userId
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getListingDetail(route.params.id)
    detail.value = res.data
  } catch (error) {
    console.error('获取书籍详情失败:', error)
    ElMessage.error('获取书籍详情失败')
  } finally {
    loading.value = false
  }
}

const handleBuy = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要购买《${detail.value.book.title}》吗？`,
      '确认购买',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await createOrder({ listingId: detail.value.listingId })
    ElMessage.success('下单成功！请联系卖家完成交易')
    router.push('/orders')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下单失败:', error)
    }
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要下架这本书籍吗？下架后将无法恢复。',
      '确认下架',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteListing(detail.value.listingId)
    ElMessage.success('下架成功')
    router.back()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.listing-detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-content {
  padding: 20px 0;
}

.book-image-section {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  min-height: 400px;
}

.book-image {
  max-width: 100%;
  max-height: 500px;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  min-height: 400px;
}

.book-detail-section {
  padding: 10px 0;
}

.book-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

.book-meta {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.price-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #fff5f5;
  border-radius: 8px;
}

.price-label {
  font-size: 18px;
  color: #606266;
  margin-right: 10px;
}

.price-value {
  font-size: 36px;
  color: #f56c6c;
  font-weight: bold;
}

.book-info {
  margin-bottom: 20px;
}

.seller-section h3,
.description-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #303133;
}

.seller-info {
  display: flex;
  gap: 15px;
  align-items: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.seller-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.seller-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.seller-credit,
.seller-contact {
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 5px;
}

.action-section {
  margin: 20px 0;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.description-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.description-text {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .book-title {
    font-size: 22px;
  }
  
  .price-value {
    font-size: 28px;
  }
}
</style>

