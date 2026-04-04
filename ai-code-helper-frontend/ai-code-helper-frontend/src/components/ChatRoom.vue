<template>
  <div class="chat-container">
    <header class="chat-header">
      <div class="header-content">
        <div class="logo">
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <h1>编小能</h1>
        </div>
        <div class="room-info">
          <span class="room-label">会话 ID</span>
          <span class="room-id">{{ memoryId }}</span>
        </div>
      </div>
    </header>

    <main class="chat-main">
      <div class="messages-container" ref="messagesContainer">
        <div class="welcome-message" v-if="messages.length === 0">
          <div class="welcome-icon">👋</div>
          <h2>欢迎来到 编小能</h2>
          <p>我可以帮你解答编程学习问题和求职面试相关的问题</p>
          <div class="quick-actions">
            <button 
              v-for="(action, index) in quickActions" 
              :key="index"
              class="quick-action-btn"
              @click="sendQuickMessage(action)"
            >
              {{ action }}
            </button>
          </div>
        </div>

        <div 
          v-for="(message, index) in messages" 
          :key="index"
          :class="['message', message.type]"
        >
          <div class="message-avatar">
            <template v-if="message.type === 'user'">
              <div class="avatar user-avatar">我</div>
            </template>
            <template v-else>
              <div class="avatar ai-avatar">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor"/>
                  <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </template>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div class="message-text" v-html="formatMessage(message.content)"></div>
            </div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>

        <div v-if="isLoading" class="message ai loading">
          <div class="message-avatar">
            <div class="avatar ai-avatar">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor"/>
                <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <footer class="chat-footer">
      <div class="input-container">
        <div class="input-wrapper">
          <textarea
            v-model="inputMessage"
            @keydown.enter.prevent="handleEnter"
            @input="autoResize"
            ref="inputRef"
            placeholder="输入你的问题，我来帮你解答..."
            rows="1"
            :disabled="isLoading"
          ></textarea>
          <button 
            class="send-btn" 
            @click="sendMessage"
            :disabled="!inputMessage.trim() || isLoading"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M22 2L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M22 2L15 22L11 13L2 9L22 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'

interface Message {
  type: 'user' | 'ai'
  content: string
  time: string
}

const messages = ref<Message[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
const memoryId = ref<number>(0)
const messagesContainer = ref<HTMLDivElement>()
const inputRef = ref<HTMLTextAreaElement>()
let eventSource: EventSource | null = null

const quickActions = [
  '帮我准备 Java 面试题',
  '解释一下 Vue3 的响应式原理',
  '如何学习数据结构与算法？',
  '前端性能优化有哪些方法？'
]

const generateMemoryId = () => {
  return Math.floor(Math.random() * 1000000) + 1
}

const formatTime = () => {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
}

const formatMessage = (content: string) => {
  return content
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const autoResize = () => {
  const textarea = inputRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
  }
}

const handleEnter = (e: KeyboardEvent) => {
  if (!e.shiftKey) {
    sendMessage()
  } else {
    inputMessage.value += '\n'
    autoResize()
  }
}

const sendQuickMessage = (message: string) => {
  inputMessage.value = message
  sendMessage()
}

const sendMessage = () => {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value) return

  messages.value.push({
    type: 'user',
    content: message,
    time: formatTime()
  })

  inputMessage.value = ''
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }
  
  isLoading.value = true
  scrollToBottom()

  let aiResponse = ''
  const aiMessageIndex = messages.value.length
  
  messages.value.push({
    type: 'ai',
    content: '',
    time: formatTime()
  })

  const encodedMessage = encodeURIComponent(message)
  const url = `/api/ai/chat?memoryId=${memoryId.value}&message=${encodedMessage}`
  
  try {
    eventSource = new EventSource(url)
    
    eventSource.onmessage = (event) => {
      if (event.data) {
        const data = event.data
        if (data !== '[DONE]') {
          aiResponse += data
          messages.value[aiMessageIndex].content = aiResponse
          scrollToBottom()
        }
      }
    }
    
    eventSource.onerror = (error) => {
      console.error('SSE Error:', error)
      eventSource?.close()
      isLoading.value = false
      if (!aiResponse) {
        messages.value[aiMessageIndex].content = '抱歉，连接出现问题，请稍后重试。'
      }
    }
    
    eventSource.onopen = () => {
      console.log('SSE Connection opened')
    }
  } catch (error) {
    console.error('Failed to create EventSource:', error)
    isLoading.value = false
    messages.value[aiMessageIndex].content = '抱歉，无法建立连接，请确保后端服务已启动。'
  }
}

onMounted(() => {
  memoryId.value = generateMemoryId()
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 1200px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  color: white;
}

.logo h1 {
  color: white;
  font-size: 1.5rem;
  font-weight: 600;
}

.room-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
}

.room-label {
  font-size: 0.875rem;
}

.room-id {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-family: monospace;
}

.chat-main {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.messages-container {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  scroll-behavior: smooth;
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 3px;
}

.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: #4a5568;
}

.welcome-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.welcome-message h2 {
  font-size: 1.75rem;
  margin-bottom: 12px;
  color: #2d3748;
}

.welcome-message p {
  font-size: 1rem;
  margin-bottom: 32px;
  color: #718096;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  max-width: 600px;
  margin: 0 auto;
}

.quick-action-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(102, 126, 234, 0.3);
}

.quick-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(102, 126, 234, 0.4);
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.ai-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.ai-avatar svg {
  width: 20px;
  height: 20px;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message.user .message-content {
  align-items: flex-end;
}

.message-bubble {
  padding: 16px 20px;
  border-radius: 20px;
  font-size: 0.95rem;
  line-height: 1.6;
  word-wrap: break-word;
}

.message.ai .message-bubble {
  background: #f7fafc;
  color: #2d3748;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.05);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.message.user .message-text :deep(pre) {
  background: rgba(255, 255, 255, 0.2);
}

.message-text :deep(code) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.9em;
}

.message-text :deep(pre code) {
  background: transparent;
  padding: 0;
}

.message-text :deep(:not(pre) > code) {
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  color: #667eea;
}

.message.user .message-text :deep(:not(pre) > code) {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.message-time {
  font-size: 0.75rem;
  color: #a0aec0;
  margin-top: 4px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #a0aec0;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

.chat-footer {
  background: white;
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
}

.input-container {
  max-width: 900px;
  margin: 0 auto;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #f7fafc;
  border-radius: 24px;
  padding: 8px 8px 8px 20px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.input-wrapper textarea {
  flex: 1;
  border: none;
  background: transparent;
  resize: none;
  font-size: 0.95rem;
  line-height: 1.5;
  max-height: 120px;
  padding: 8px 0;
  outline: none;
  font-family: inherit;
}

.input-wrapper textarea::placeholder {
  color: #a0aec0;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn svg {
  width: 18px;
  height: 18px;
}

@media (max-width: 768px) {
  .chat-header {
    padding: 12px 16px;
  }
  
  .logo h1 {
    font-size: 1.25rem;
  }
  
  .room-info {
    display: none;
  }
  
  .messages-container {
    padding: 16px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .chat-footer {
    padding: 12px 16px;
  }
  
  .quick-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .quick-action-btn {
    width: 100%;
  }
}
</style>
