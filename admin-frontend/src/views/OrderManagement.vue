<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div class="header-actions">
            <el-select v-model="status" placeholder="状态筛选" clearable style="width: 150px; margin-right: 10px" @change="handleSearch">
              <el-option label="待确认" value="待确认" />
              <el-option label="已完成" value="已完成" />
              <el-option label="已取消" value="已取消" />
            </el-select>
            <el-button @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="orderList" v-loading="loading" stripe>
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column prop="listingId" label="书籍ID" width="100" />
        <el-table-column prop="buyerId" label="买家ID" width="100" />
        <el-table-column prop="transactionPrice" label="交易价格" width="120">
          <template #default="{ row }">
            ¥{{ row.transactionPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.orderStatus)">{{ row.orderStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" width="180" />
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
import { getOrderList } from '@/api/admin'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const orderList = ref([])
const status = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getStatusType = (status) => {
  const map = {
    '待确认': 'warning',
    '已完成': 'success',
    '已取消': 'info'
  }
  return map[status] || ''
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrderList({
      page: page.value,
      pageSize: pageSize.value,
      status: status.value
    })
      if (res.code === 1) {
        orderList.value = res.data.items || []
        total.value = res.data.total || 0
      }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchOrders()
}

const handleSizeChange = () => {
  fetchOrders()
}

const handlePageChange = () => {
  fetchOrders()
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-management {
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

