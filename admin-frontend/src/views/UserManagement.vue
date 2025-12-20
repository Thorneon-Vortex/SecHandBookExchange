<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-input
            v-model="keyword"
            placeholder="搜索学号或昵称"
            style="width: 300px"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="studentId" label="学号" width="150" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="creditScore" label="信誉分" width="100" />
        <el-table-column prop="registerTime" label="注册时间" width="180" />
        <el-table-column prop="contactInfo" label="联系方式" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              @click="handleDisable(row)"
              :disabled="!row.enabled"
            >
              禁用
            </el-button>
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
import { getUserList, updateUserStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value
    })
      if (res.code === 1) {
        userList.value = res.data.items || []
        total.value = res.data.total || 0
      }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchUsers()
}

const handleSizeChange = () => {
  fetchUsers()
}

const handlePageChange = () => {
  fetchUsers()
}

const handleDisable = (row) => {
  ElMessageBox.confirm('确定要禁用该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateUserStatus(row.userId, false)
      ElMessage.success('操作成功')
      fetchUsers()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

