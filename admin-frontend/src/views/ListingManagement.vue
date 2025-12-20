<template>
  <div class="listing-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>书籍管理</span>
          <div class="header-actions">
            <el-input
              v-model="keyword"
              placeholder="搜索书名、作者、ISBN"
              style="width: 250px; margin-right: 10px"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
            <el-select v-model="status" placeholder="状态筛选" clearable style="width: 150px; margin-right: 10px" @change="handleSearch">
              <el-option label="在售" value="在售" />
              <el-option label="已预定" value="已预定" />
              <el-option label="已售出" value="已售出" />
              <el-option label="已下架" value="已下架" />
            </el-select>
            <el-button @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="listingList" v-loading="loading" stripe>
        <el-table-column prop="listingId" label="ID" width="80" />
        <el-table-column prop="bookTitle" label="书名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="bookId" label="书籍ID" width="100" />
        <el-table-column prop="sellerId" label="卖家ID" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span v-if="row.price">¥{{ row.price }}</span>
            <span v-else style="color: #909399">赠送</span>
          </template>
        </el-table-column>
        <el-table-column prop="listingType" label="类型" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="postTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== '已下架'"
              type="danger"
              size="small"
              @click="handleTakeDown(row)"
            >
              下架
            </el-button>
            <span v-else style="color: #909399">已下架</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getListingList, takeDownListing } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const listingList = ref([])
const keyword = ref('')
const status = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getStatusType = (status) => {
  const map = {
    '在售': 'success',
    '已预定': 'warning',
    '已售出': 'info',
    '已下架': 'danger'
  }
  return map[status] || ''
}

const fetchListings = async () => {
  loading.value = true
  try {
    const res = await getListingList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      status: status.value
    })
      if (res.code === 1) {
        listingList.value = res.data.items || []
        total.value = res.data.total || 0
      }
  } catch (error) {
    ElMessage.error('获取书籍列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchListings()
}

const handleSizeChange = () => {
  fetchListings()
}

const handlePageChange = () => {
  fetchListings()
}

const handleTakeDown = (row) => {
  ElMessageBox.confirm('确定要下架该书籍吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await takeDownListing(row.listingId, '管理员下架')
      ElMessage.success('下架成功')
      fetchListings()
    } catch (error) {
      ElMessage.error('下架失败')
    }
  })
}

onMounted(() => {
  fetchListings()
})
</script>

<style scoped>
.listing-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}
</style>

