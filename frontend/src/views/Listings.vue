<template>
  <div class="listings-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <h2>📖 书籍市场</h2>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索书名、作者、ISBN"
              clearable
              style="width: 250px"
            />
          </el-form-item>
          
          <el-form-item label="分类">
            <el-select
              v-model="searchForm.categoryId"
              placeholder="全部分类"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="category in categories"
                :key="category.categoryId"
                :label="category.categoryName"
                :value="category.categoryId"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="排序">
            <el-select v-model="searchForm.sortBy" style="width: 150px">
              <el-option label="最新发布" value="time_desc" />
              <el-option label="价格升序" value="price_asc" />
              <el-option label="价格降序" value="price_desc" />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 书籍列表 -->
      <div v-loading="loading" class="listings-content">
        <el-row :gutter="20">
          <el-col
            v-for="listing in listings"
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
                <el-tag v-if="listing.listingType === '赠送'" class="free-tag" type="success">
                  免费赠送
                </el-tag>
              </div>
              <div class="book-info">
                <h3 class="book-title" :title="listing.title">{{ listing.title }}</h3>
                <p class="book-author">{{ listing.author }}</p>
                <div class="book-footer">
                  <span class="book-price">
                    {{ listing.listingType === '赠送' ? '免费' : `¥${listing.price}` }}
                  </span>
                  <span class="book-seller">
                    <el-icon><User /></el-icon>
                    {{ listing.sellerNickname }}
                  </span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-empty v-if="!listings.length && !loading" description="没有找到相关书籍" />

        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="searchForm.page"
            v-model:page-size="searchForm.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="fetchListings"
            @size-change="fetchListings"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getListings } from '@/api/listing'
import { getCategories } from '@/api/category'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const listings = ref([])
const total = ref(0)
const categories = ref([])

const searchForm = reactive({
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  sortBy: 'time_desc',
  page: 1,
  pageSize: 20
})

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchListings = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      categoryId: searchForm.categoryId || undefined
    }
    const res = await getListings(params)
    listings.value = res.data.items || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取书籍列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.page = 1
  fetchListings()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = undefined
  searchForm.sortBy = 'time_desc'
  searchForm.page = 1
  fetchListings()
}

const handleBookClick = (listingId) => {
  router.push(`/listings/${listingId}`)
}

// 监听路由变化
watch(() => route.query, (newQuery) => {
  if (newQuery.keyword !== undefined) {
    searchForm.keyword = newQuery.keyword
  }
  if (newQuery.categoryId) {
    searchForm.categoryId = Number(newQuery.categoryId)
  }
  fetchListings()
}, { immediate: false })

onMounted(() => {
  fetchCategories()
  fetchListings()
})
</script>

<style scoped>
.listings-page {
  min-height: calc(100vh - 180px);
}

.page-header h2 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.listings-content {
  min-height: 400px;
}

.book-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
  height: 100%;
}

.book-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.book-cover {
  position: relative;
  width: 100%;
  height: 250px;
  overflow: hidden;
  background: #f5f7fa;
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
  font-size: 60px;
  color: #c0c4cc;
}

.book-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 5px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: white;
  backdrop-filter: blur(5px);
}

.book-status.available {
  background: rgba(103, 194, 58, 0.9);
}

.book-status.unavailable {
  background: rgba(144, 147, 153, 0.9);
}

.free-tag {
  position: absolute;
  top: 10px;
  left: 10px;
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
  color: #303133;
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
  gap: 10px;
}

.book-price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.book-seller {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>


