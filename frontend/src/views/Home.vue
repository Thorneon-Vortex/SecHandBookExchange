<template>
  <div class="home">
    <!-- Hero Section -->
    <div class="hero-section">
      <el-card shadow="never" class="hero-card">
        <h1>📚 让知识循环起来</h1>
        <p>校园二手书交易平台 - 买书、卖书、分享知识</p>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索书名、作者、ISBN..."
            size="large"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </template>
          </el-input>
        </div>
      </el-card>
    </div>

    <!-- 分类快捷入口 -->
    <el-card class="categories-section" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon><Grid /></el-icon>
          <span>热门分类</span>
        </div>
      </template>
      <div class="categories-grid" v-loading="categoriesLoading">
        <div
          v-for="category in categories"
          :key="category.categoryId"
          class="category-item"
          @click="handleCategoryClick(category.categoryId)"
        >
          <el-icon :size="30" color="#409eff"><Reading /></el-icon>
          <span>{{ category.categoryName }}</span>
        </div>
      </div>
    </el-card>

    <!-- 最新书籍 -->
    <el-card class="latest-section" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon><Clock /></el-icon>
          <span>最新发布</span>
          <el-button type="text" style="margin-left: auto" @click="$router.push('/listings')">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      <div v-loading="listingsLoading">
        <el-row :gutter="20">
          <el-col
            v-for="listing in latestListings"
            :key="listing.listingId"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <div class="book-card" @click="handleBookClick(listing.listingId)">
              <div class="book-cover">
                <el-image
                  :src="listing.coverImageUrl || '/placeholder-book.png'"
                  fit="cover"
                  lazy
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="book-status" :class="listing.status === '在售' ? 'available' : 'unavailable'">
                  {{ listing.status }}
                </div>
              </div>
              <div class="book-info">
                <h3 class="book-title">{{ listing.title }}</h3>
                <p class="book-author">{{ listing.author }}</p>
                <div class="book-footer">
                  <span class="book-price">
                    {{ listing.listingType === '赠送' ? '免费' : `¥${listing.price}` }}
                  </span>
                  <span class="book-seller">{{ listing.sellerNickname }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="!latestListings.length && !listingsLoading" description="暂无书籍" />
      </div>
    </el-card>

    <!-- 功能介绍 -->
    <el-card class="features-section" shadow="never">
      <el-row :gutter="40">
        <el-col :xs="24" :sm="8">
          <div class="feature-item">
            <el-icon :size="50" color="#67c23a"><ShoppingCart /></el-icon>
            <h3>便捷交易</h3>
            <p>快速发布，轻松购买，校园内安全交易</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="feature-item">
            <el-icon :size="50" color="#e6a23c"><PriceTag /></el-icon>
            <h3>价格实惠</h3>
            <p>二手书籍价格优惠，让知识更实惠</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="feature-item">
            <el-icon :size="50" color="#409eff"><Trophy /></el-icon>
            <h3>信誉保障</h3>
            <p>信誉积分系统，保障交易安全可靠</p>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories } from '@/api/category'
import { getListings } from '@/api/listing'

const router = useRouter()

const searchKeyword = ref('')
const categories = ref([])
const categoriesLoading = ref(false)
const latestListings = ref([])
const listingsLoading = ref(false)

const fetchCategories = async () => {
  categoriesLoading.value = true
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
  } finally {
    categoriesLoading.value = false
  }
}

const fetchLatestListings = async () => {
  listingsLoading.value = true
  try {
    const res = await getListings({
      sortBy: 'time_desc',
      page: 1,
      pageSize: 8
    })
    latestListings.value = res.data.items || []
  } catch (error) {
    console.error('获取最新书籍失败:', error)
  } finally {
    listingsLoading.value = false
  }
}

const handleSearch = () => {
  router.push({
    path: '/listings',
    query: { keyword: searchKeyword.value }
  })
}

const handleCategoryClick = (categoryId) => {
  router.push({
    path: '/listings',
    query: { categoryId }
  })
}

const handleBookClick = (listingId) => {
  router.push(`/listings/${listingId}`)
}

onMounted(() => {
  fetchCategories()
  fetchLatestListings()
})
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-section {
  margin-bottom: 20px;
}

.hero-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
  padding: 60px 20px;
  border: none;
}

.hero-card :deep(.el-card__body) {
  padding: 0;
}

.hero-card h1 {
  font-size: 42px;
  margin-bottom: 15px;
  font-weight: bold;
}

.hero-card p {
  font-size: 18px;
  margin-bottom: 30px;
  opacity: 0.9;
}

.search-box {
  max-width: 600px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.category-item span {
  font-size: 14px;
  color: #606266;
}

.book-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.book-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.book-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.book-cover .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  font-size: 50px;
  color: #c0c4cc;
}

.book-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.book-status.available {
  background: #67c23a;
}

.book-status.unavailable {
  background: #909399;
}

.book-info {
  padding: 15px;
}

.book-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.book-seller {
  font-size: 12px;
  color: #909399;
}

.features-section {
  background: transparent;
  border: none;
}

.features-section :deep(.el-card__body) {
  padding: 40px 20px;
}

.feature-item {
  text-align: center;
  padding: 20px;
}

.feature-item h3 {
  margin: 20px 0 10px;
  font-size: 20px;
  color: #303133;
}

.feature-item p {
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
}
</style>

