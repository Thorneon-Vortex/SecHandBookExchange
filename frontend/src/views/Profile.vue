<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧：用户信息卡片 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="user-card">
          <div class="user-header">
            <el-avatar :size="100">
              <el-icon :size="50"><User /></el-icon>
            </el-avatar>
            <h2>{{ userInfo?.nickname || '用户' }}</h2>
            <p class="student-id">学号：{{ userInfo?.studentId }}</p>
          </div>

          <el-divider />

          <div class="user-stats">
            <div class="stat-item">
              <el-icon :size="30" color="#f56c6c"><Medal /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ userInfo?.creditScore || 100 }}</div>
                <div class="stat-label">信誉积分</div>
              </div>
            </div>
          </div>

          <el-divider />

          <div class="user-info-list">
            <div class="info-item">
              <span class="info-label">
                <el-icon><Phone /></el-icon>
                联系方式
              </span>
              <span class="info-value">{{ userInfo?.contactInfo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">
                <el-icon><Calendar /></el-icon>
                注册时间
              </span>
              <span class="info-value">{{ formatDate(userInfo?.registerTime) }}</span>
            </div>
          </div>

          <el-button type="primary" style="width: 100%; margin-top: 20px" @click="showEditDialog = true">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </el-card>
      </el-col>

      <!-- 右侧：快捷功能 -->
      <el-col :xs="24" :md="16">
        <el-card shadow="hover" class="actions-card">
          <template #header>
            <h3>快捷功能</h3>
          </template>

          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <div class="action-item" @click="$router.push('/publish')">
                <el-icon :size="50" color="#67c23a"><Upload /></el-icon>
                <h4>发布书籍</h4>
                <p>出售或赠送您的二手书籍</p>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12">
              <div class="action-item" @click="$router.push('/orders')">
                <el-icon :size="50" color="#409eff"><Tickets /></el-icon>
                <h4>我的订单</h4>
                <p>查看我的买入和卖出订单</p>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12">
              <div class="action-item" @click="$router.push('/listings')">
                <el-icon :size="50" color="#e6a23c"><Search /></el-icon>
                <h4>浏览书籍</h4>
                <p>查找您需要的二手书籍</p>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12">
              <div class="action-item" @click="$router.push('/')">
                <el-icon :size="50" color="#909399"><HomeFilled /></el-icon>
                <h4>返回首页</h4>
                <p>回到平台首页</p>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 信誉积分说明 -->
        <el-card shadow="hover" style="margin-top: 20px">
          <template #header>
            <h3>💡 信誉积分说明</h3>
          </template>
          <el-alert type="info" :closable="false">
            <ul style="padding-left: 20px; margin: 10px 0; line-height: 1.8">
              <li>新用户初始信誉积分为 100 分</li>
              <li>每完成一笔交易，买卖双方各获得 5 分</li>
              <li>信誉积分越高，表示交易记录越好</li>
              <li>高信誉用户将获得更多信任和交易机会</li>
            </ul>
          </el-alert>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑资料对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑个人资料"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="editForm.contactInfo" placeholder="请输入联系方式" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleUpdate">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateUserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const userInfo = ref(null)
const showEditDialog = ref(false)
const editLoading = ref(false)
const editFormRef = ref()

const editForm = reactive({
  nickname: '',
  contactInfo: ''
})

const editRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
}

const fetchUserInfo = async () => {
  await userStore.fetchUserInfo()
  userInfo.value = userStore.userInfo
  editForm.nickname = userInfo.value?.nickname || ''
  editForm.contactInfo = userInfo.value?.contactInfo || ''
}

const handleUpdate = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await updateUserInfo(editForm)
        ElMessage.success('更新成功')
        showEditDialog.value = false
        fetchUserInfo()
      } catch (error) {
        console.error('更新失败:', error)
      } finally {
        editLoading.value = false
      }
    }
  })
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}

.user-card {
  text-align: center;
}

.user-header {
  padding: 20px 0;
}

.user-header h2 {
  margin: 15px 0 5px;
  color: #303133;
}

.student-id {
  color: #909399;
  font-size: 14px;
}

.user-stats {
  padding: 20px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.stat-content {
  text-align: left;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #f56c6c;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.user-info-list {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f7fa;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.actions-card {
  min-height: 400px;
}

.action-item {
  text-align: center;
  padding: 30px 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.action-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  transform: translateY(-4px);
}

.action-item h4 {
  margin: 15px 0 10px;
  color: #303133;
  font-size: 18px;
}

.action-item p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}
</style>


