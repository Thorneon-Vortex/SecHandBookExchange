<template>
  <div class="orders-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <h2>📋 我的订单</h2>
        </div>
      </template>

      <!-- 角色切换 -->
      <el-radio-group v-model="currentRole" @change="fetchOrders" class="role-tabs">
        <el-radio-button value="buyer">
          <el-icon><ShoppingCart /></el-icon>
          我买到的
        </el-radio-button>
        <el-radio-button value="seller">
          <el-icon><Sell /></el-icon>
          我卖出的
        </el-radio-button>
      </el-radio-group>

      <!-- 订单列表 -->
      <div v-loading="loading" class="orders-list">
        <div v-if="orders.length > 0" class="order-items">
          <el-card
            v-for="order in orders"
            :key="order.orderId"
            class="order-card"
            shadow="hover"
          >
            <div class="order-header">
              <div class="order-info">
                <span class="order-id">订单号：{{ order.orderId }}</span>
                <el-tag :type="getStatusType(order.orderStatus)" size="small">
                  {{ order.orderStatus }}
                </el-tag>
              </div>
              <div class="order-time">{{ formatDate(order.orderTime) }}</div>
            </div>

            <el-divider style="margin: 15px 0" />

            <div class="order-content">
              <div class="book-info">
                <h3 class="book-title">📖 {{ order.bookTitle }}</h3>
                <div class="order-detail">
                  <span class="detail-item">
                    <el-icon><User /></el-icon>
                    {{ currentRole === 'buyer' ? '卖家' : '买家' }}：{{ order.counterpartyNickname }}
                  </span>
                  <span class="detail-item price">
                    <el-icon><Money /></el-icon>
                    交易金额：<strong>¥{{ order.transactionPrice }}</strong>
                  </span>
                </div>
              </div>

              <div class="order-actions">
                <el-button
                  v-if="order.orderStatus === '待确认'"
                  type="success"
                  size="small"
                  @click="handleComplete(order.orderId)"
                >
                  <el-icon><Check /></el-icon>
                  确认完成
                </el-button>
                <el-button
                  v-if="order.orderStatus === '待确认'"
                  type="warning"
                  size="small"
                  @click="handleCancel(order.orderId)"
                >
                  <el-icon><Close /></el-icon>
                  取消订单
                </el-button>
                <el-tag v-if="order.orderStatus === '已完成'" type="success">
                  <el-icon><CircleCheck /></el-icon>
                  交易已完成
                </el-tag>
                <el-tag v-if="order.orderStatus === '已取消'" type="info">
                  <el-icon><CircleClose /></el-icon>
                  订单已取消
                </el-tag>
              </div>
            </div>
          </el-card>
        </div>

        <el-empty v-else description="暂无订单" />
      </div>
    </el-card>

    <!-- 订单说明 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <h3>📢 订单说明</h3>
      </template>
      <el-alert type="info" :closable="false">
        <ul style="padding-left: 20px; margin: 10px 0; line-height: 1.8">
          <li><strong>待确认：</strong>订单已创建，请线下联系对方完成交易</li>
          <li><strong>确认完成：</strong>买卖双方任意一方确认即可完成交易，双方各获得5积分</li>
          <li><strong>取消订单：</strong>如需取消，请点击取消按钮，书籍将恢复在售状态</li>
          <li><strong>联系方式：</strong>请通过个人资料中的联系方式与对方沟通</li>
        </ul>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders, completeOrder, cancelOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const currentRole = ref('buyer')
const orders = ref([])

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getMyOrders({ role: currentRole.value })
    orders.value = res.data || []
  } catch (error) {
    console.error('获取订单失败:', error)
  } finally {
    loading.value = false
  }
}

const handleComplete = async (orderId) => {
  try {
    await ElMessageBox.confirm(
      '确认线下交易已完成？确认后双方将各获得5积分。',
      '确认完成交易',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    await completeOrder(orderId)
    ElMessage.success('交易已完成，信誉分+5！')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成订单失败:', error)
    }
  }
}

const handleCancel = async (orderId) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？书籍将恢复在售状态。',
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '不取消',
        type: 'warning'
      }
    )

    await cancelOrder(orderId)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
}

const getStatusType = (status) => {
  const typeMap = {
    '待确认': 'warning',
    '已完成': 'success',
    '已取消': 'info'
  }
  return typeMap[status] || 'info'
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-page {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header h2 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.role-tabs {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.role-tabs :deep(.el-radio-button__inner) {
  padding: 12px 30px;
  font-size: 16px;
}

.orders-list {
  min-height: 400px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-card {
  transition: all 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.order-id {
  font-size: 14px;
  color: #606266;
}

.order-time {
  font-size: 13px;
  color: #909399;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.book-info {
  flex: 1;
  min-width: 300px;
}

.book-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 12px;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.detail-item.price {
  color: #f56c6c;
  font-size: 16px;
}

.detail-item strong {
  font-size: 20px;
}

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
}

@media (max-width: 768px) {
  .order-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-actions {
    width: 100%;
    flex-direction: row;
    justify-content: flex-start;
  }
}
</style>

