<template>
  <div class="data-query">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>智能数据查询</span>
          <el-tag type="info">使用自然语言查询数据，AI自动生成SQL</el-tag>
        </div>
      </template>

      <!-- 查询输入区 -->
      <div class="query-input-area">
        <el-input
          v-model="queryText"
          type="textarea"
          :rows="3"
          placeholder="请输入您的查询需求，例如：&#10;• 本月新增用户数&#10;• 信誉分最高的10个用户&#10;• 各状态的书籍数量&#10;• 今日订单总金额"
          @keyup.ctrl.enter="handleQuery"
        />
        <div class="input-actions">
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="clearResult">清空</el-button>
          <span class="tip">提示：按 Ctrl+Enter 快速查询</span>
        </div>
      </div>

      <!-- 快捷查询 -->
      <div class="quick-queries">
        <span class="label">快捷查询：</span>
        <el-tag
          v-for="q in quickQueries"
          :key="q"
          @click="queryText = q; handleQuery()"
          class="quick-tag"
        >
          {{ q }}
        </el-tag>
      </div>

      <!-- 查询结果 -->
      <div v-if="result" class="query-result">
        <el-divider>
          <el-icon><Document /></el-icon>
          查询结果
        </el-divider>

        <!-- 生成的SQL -->
        <div class="sql-display">
          <div class="sql-header">
            <span>生成的SQL：</span>
            <el-button text size="small" @click="copySql">
              <el-icon><DocumentCopy /></el-icon>
              复制
            </el-button>
          </div>
          <el-input
            :value="result.sql"
            type="textarea"
            :rows="3"
            readonly
            class="sql-code"
          />
        </div>

        <!-- 结果消息 -->
        <el-alert
          :type="result.success ? 'success' : 'error'"
          :title="result.message"
          :closable="false"
          style="margin: 15px 0"
        />

        <!-- 数据表格 -->
        <el-table
          v-if="result.success && result.data && result.data.length > 0"
          :data="result.data"
          stripe
          border
          max-height="500"
        >
          <el-table-column
            v-for="(value, key) in result.data[0]"
            :key="key"
            :prop="key"
            :label="key"
            min-width="120"
          />
        </el-table>

        <!-- 空结果提示 -->
        <el-empty
          v-else-if="result.success && (!result.data || result.data.length === 0)"
          description="查询完成，但没有数据"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { dataQuery } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Search, Document, DocumentCopy } from '@element-plus/icons-vue'

const queryText = ref('')
const loading = ref(false)
const result = ref(null)

const quickQueries = [
  '本月新增用户数',
  '信誉分最高的10个用户',
  '各状态的书籍数量',
  '今日订单总金额',
  '最受欢迎的书籍分类',
  '平均订单金额',
  '最近一周的订单趋势'
]

const handleQuery = async () => {
  if (!queryText.value.trim()) {
    ElMessage.warning('请输入查询内容')
    return
  }

  loading.value = true
  try {
    const res = await dataQuery(queryText.value)
    if (res.code === 1) {
      result.value = res.data
      if (res.data.success) {
        ElMessage.success('查询成功')
      } else {
        ElMessage.warning(res.data.message)
      }
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

const clearResult = () => {
  result.value = null
  queryText.value = ''
}

const copySql = () => {
  if (result.value?.sql) {
    navigator.clipboard.writeText(result.value.sql).then(() => {
      ElMessage.success('SQL已复制到剪贴板')
    }).catch(() => {
      ElMessage.error('复制失败')
    })
  }
}
</script>

<style scoped>
.data-query {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-input-area {
  margin-bottom: 20px;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.tip {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.quick-queries {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.label {
  margin-right: 10px;
  color: #606266;
  font-weight: 500;
}

.quick-tag {
  margin-right: 10px;
  margin-bottom: 5px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-tag:hover {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.query-result {
  margin-top: 20px;
}

.sql-display {
  margin-bottom: 20px;
}

.sql-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: 500;
  color: #606266;
}

.sql-code {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.sql-code :deep(.el-textarea__inner) {
  background: #f5f7fa;
  color: #303133;
}
</style>

