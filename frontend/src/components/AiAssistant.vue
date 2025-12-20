<template>
  <div class="ai-assistant">
    <!-- 悬浮按钮 -->
    <div class="ai-fab" @click="toggleDialog" :class="{ 'has-unread': hasUnread }">
      <el-icon :size="28"><Service /></el-icon>
      <span class="fab-label">小书</span>
    </div>

    <!-- 对话面板 -->
    <Transition name="slide-up">
      <div v-show="dialogVisible" class="chat-panel">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-info">
            <el-icon :size="24"><Service /></el-icon>
            <span>智能客服 · 小书</span>
          </div>
          <el-button text circle @click="dialogVisible = false">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>

        <!-- 消息列表 -->
        <div class="messages" ref="messagesRef">
          <div 
            v-for="(msg, index) in messages" 
            :key="index"
            :class="['message', msg.role]"
          >
            <div class="avatar">
              <el-icon v-if="msg.role === 'assistant'" :size="20"><Service /></el-icon>
              <el-icon v-else :size="20"><User /></el-icon>
            </div>
            <div class="bubble">
              <div class="content">{{ msg.content }}</div>
              
              <!-- 导航按钮 -->
              <el-button 
                v-if="msg.navigation && msg.navigation.page"
                type="primary"
                size="small"
                class="nav-btn"
                @click="handleNavigate(msg.navigation)"
              >
                <el-icon><Right /></el-icon>
                {{ getPageName(msg.navigation.page) }}
              </el-button>
            </div>
          </div>
          
          <!-- 加载中提示 -->
          <div v-if="loading" class="message assistant">
            <div class="avatar">
              <el-icon :size="20"><Service /></el-icon>
            </div>
            <div class="bubble">
              <div class="typing">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷问题 -->
        <div class="quick-questions" v-if="messages.length <= 1">
          <span class="tip">您可以问我：</span>
          <div class="tags">
            <el-tag 
              v-for="q in quickQuestions" 
              :key="q"
              @click="sendQuickQuestion(q)"
              class="quick-tag"
            >
              {{ q }}
            </el-tag>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <el-input 
            v-model="inputMessage"
            placeholder="有什么可以帮您？"
            @keyup.enter="sendMessage"
            :disabled="loading"
            maxlength="200"
            show-word-limit
          />
          <el-button 
            type="primary" 
            @click="sendMessage" 
            :loading="loading"
            :disabled="!inputMessage.trim()"
          >
            发送
          </el-button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { chatWithAi } from '@/api/ai'
import { ElMessage } from 'element-plus'

const router = useRouter()
const dialogVisible = ref(false)
const inputMessage = ref('')
const loading = ref(false)
const hasUnread = ref(false)
const messagesRef = ref(null)

const messages = ref([
  { 
    role: 'assistant', 
    content: '您好！我是智能客服小书 📚\n有什么可以帮您的吗？',
    navigation: null
  }
])

const quickQuestions = [
  '怎么发布书籍？',
  '怎么购买书籍？',
  '什么是信誉分？',
  '我想卖书',
  '搜索书籍'
]

const pageNames = {
  home: '去首页',
  listings: '去书籍市场',
  publish: '去发布书籍',
  orders: '去我的订单',
  profile: '去个人中心',
  login: '去登录',
  register: '去注册'
}

const getPageName = (page) => pageNames[page] || '跳转'

const toggleDialog = () => {
  dialogVisible.value = !dialogVisible.value
  hasUnread.value = false
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

const sendMessage = async () => {
  const msg = inputMessage.value.trim()
  if (!msg || loading.value) return
  
  // 添加用户消息
  messages.value.push({ role: 'user', content: msg, navigation: null })
  inputMessage.value = ''
  loading.value = true
  
  await scrollToBottom()
  
  try {
    const res = await chatWithAi(msg)
    
    if (res.code === 1 && res.data) {
      const data = res.data
      messages.value.push({
        role: 'assistant',
        content: data.message || '抱歉，我没理解您的意思',
        navigation: data.navigation || null
      })
    } else {
      messages.value.push({
        role: 'assistant',
        content: res.msg || '抱歉，出现了一些问题',
        navigation: null
      })
    }
  } catch (error) {
    console.error('AI chat error:', error)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，服务暂时不可用，请稍后再试 😅',
      navigation: null
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const handleNavigate = (navigation) => {
  dialogVisible.value = false
  
  const { page, params } = navigation
  const path = page === 'home' ? '/' : `/${page}`
  
  router.push({ path, query: params || {} })
  ElMessage.success('正在为您跳转...')
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

onMounted(() => {
  // 3秒后显示未读提示（引导用户使用）
  setTimeout(() => {
    if (!dialogVisible.value) {
      hasUnread.value = true
    }
  }, 3000)
})
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
}

/* 悬浮按钮 */
.ai-fab {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  color: white;
}

.ai-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.5);
}

.ai-fab.has-unread::after {
  content: '';
  position: absolute;
  top: 8px;
  right: 8px;
  width: 12px;
  height: 12px;
  background: #ff4757;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

.fab-label {
  font-size: 10px;
  margin-top: 2px;
}

/* 对话面板 */
.chat-panel {
  position: absolute;
  right: 0;
  bottom: 80px;
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 500;
}

.chat-header .el-button {
  color: white;
}

/* 消息区域 */
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.assistant .avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message.user .avatar {
  background: #409eff;
  color: white;
}

.bubble {
  max-width: 75%;
}

.content {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
}

.message.assistant .content {
  background: white;
  border-radius: 4px 16px 16px 16px;
}

.message.user .content {
  background: #409eff;
  color: white;
  border-radius: 16px 4px 16px 16px;
}

.nav-btn {
  margin-top: 10px;
}

/* 打字动画 */
.typing {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing span {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: typing 1.4s infinite both;
}

.typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  40% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

/* 快捷问题 */
.quick-questions {
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #ebeef5;
}

.tip {
  font-size: 12px;
  color: #909399;
}

.tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.quick-tag:hover {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

/* 输入区域 */
.input-area {
  padding: 16px;
  background: white;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
}

.input-area .el-input {
  flex: 1;
}

/* 动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>


