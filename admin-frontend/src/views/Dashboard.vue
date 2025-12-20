<template>
  <div class="dashboard">
    <!-- 数据统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6" v-for="stat in statistics" :key="stat.label">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-icon">
            <el-icon :size="40" :color="stat.color"><component :is="stat.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Text-to-SQL 查询区域 -->
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; gap: 10px">
          <el-icon><Search /></el-icon>
          <span>智能数据查询</span>
        </div>
      </template>

      <div class="text-to-sql-area">
        <el-input
          v-model="queryText"
          type="textarea"
          :rows="3"
          placeholder="用自然语言查询数据，例如：&#10;• 今天注册了多少用户&#10;• 最贵的书是什么&#10;• 信誉分最高的10个用户&#10;• 最近一周的订单数量"
          @keyup.ctrl.enter="handleQuery"
        />
        <div class="query-actions">
          <el-button type="primary" @click="handleQuery" :loading="queryLoading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="clearQuery">清空</el-button>
          <span class="tip">提示：Ctrl + Enter 快速查询</span>
        </div>

        <!-- 查询结果 -->
        <div v-if="queryResult" class="query-result">
          <el-divider>
            <el-icon><Document /></el-icon>
            查询结果
          </el-divider>

          <!-- 友好提示 -->
          <el-alert
            :title="queryResult.message"
            :type="queryResult.success ? 'success' : 'error'"
            :closable="false"
            style="margin-bottom: 15px"
          />

          <!-- SQL语句 -->
          <el-collapse v-if="queryResult.sql">
            <el-collapse-item title="查看生成的SQL语句" name="sql">
              <el-input
                :value="queryResult.sql"
                type="textarea"
                :rows="3"
                readonly
                style="font-family: monospace"
              />
            </el-collapse-item>
          </el-collapse>

          <!-- 数据表格 -->
          <el-table
            v-if="queryResult.data && queryResult.data.length > 0"
            :data="queryResult.data"
            stripe
            border
            style="margin-top: 15px"
            max-height="400"
          >
            <el-table-column
              v-for="(value, key) in queryResult.data[0]"
              :key="key"
              :prop="key"
              :label="key"
              min-width="120"
            />
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStatistics, textToSqlQuery } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { User, Reading, Tickets, Check, Search, Document } from '@element-plus/icons-vue'

const statistics = ref([
  { label: '用户总数', value: 0, icon: 'User', color: '#409eff' },
  { label: '书籍总数', value: 0, icon: 'Reading', color: '#67c23a' },
  { label: '在售书籍', value: 0, icon: 'Reading', color: '#e6a23c' },
  { label: '已完成订单', value: 0, icon: 'Check', color: '#f56c6c' }
])

const queryText = ref('')
const queryLoading = ref(false)
const queryResult = ref(null)

onMounted(async () => {
  try {
    const res = await getStatistics()
    if (res.code === 1) {
      const data = res.data
      statistics.value[0].value = data.userCount || 0
      statistics.value[1].value = data.listingCount || 0
      statistics.value[2].value = data.onSaleCount || 0
      statistics.value[3].value = data.completedOrderCount || 0
    }
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  }
})

const handleQuery = async () => {
  if (!queryText.value.trim()) {
    ElMessage.warning('请输入查询内容')
    return
  }

  queryLoading.value = true
  queryResult.value = null

  try {
    const res = await textToSqlQuery(queryText.value)
    if (res.code === 1) {
      queryResult.value = res.data
      if (res.data.success) {
        ElMessage.success('查询成功')
      } else {
        ElMessage.warning(res.data.message)
      }
    }
  } catch (error) {
    ElMessage.error('查询失败: ' + (error.message || '未知错误'))
    queryResult.value = {
      success: false,
      message: '查询失败',
      data: null,
      sql: null
    }
  } finally {
    queryLoading.value = false
  }
}

const clearQuery = () => {
  queryText.value = ''
  queryResult.value = null
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.stat-icon {
  opacity: 0.3;
}

.text-to-sql-area {
  padding: 10px 0;
}

.query-actions {
  margin-top: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.tip {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.query-result {
  margin-top: 20px;
}
</style>

