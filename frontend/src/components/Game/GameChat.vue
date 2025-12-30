<template>
  <div class="chat-overlay" :class="{ 'chat-open': isOpen }">
    <div class="chat-header" @click="toggleChat">
      <span>💬 Chat</span>
      <span class="toggle-icon">{{ isOpen ? '▼' : '▲' }}</span>
    </div>
    <div class="chat-body" v-show="isOpen">
      <div class="chat-messages" ref="chatContainer">
        <div v-for="(msg, index) in messages" :key="index" class="chat-message">
          <span class="chat-sender">{{ msg.sender }}:</span>
          <span class="chat-content">{{ msg.content }}</span>
        </div>
      </div>
      <div class="chat-input-area">
        <input
            v-model="inputMessage"
            @keyup.enter="sendMessage"
            placeholder="Type..."
            class="chat-input"
        />
        <button @click="sendMessage" class="send-btn">➤</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const props = defineProps({
  chatId: {
    type: String,
    required: true
  },
  username: {
    type: String,
    required: true
  }
})

const isOpen = ref(false)
const messages = ref([])
const inputMessage = ref('')
const chatContainer = ref(null)
let stompClient = null

function toggleChat() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

function connect() {
  const sock = new SockJS('http://localhost:8080/ws')
  stompClient = new Client({
    webSocketFactory: () => sock,
    reconnectDelay: 5000,
    debug: () => {}
  })

  stompClient.onConnect = () => {
    // Subscribe to the specific chat topic
    stompClient.subscribe(`/topic/lobby/${props.chatId}/chat`, (msg) => {
      const chatMsg = JSON.parse(msg.body)
      messages.value.push(chatMsg)
      if (isOpen.value) {
        scrollToBottom()
      }
    })
  }

  stompClient.activate()
}

function sendMessage() {
  if (!inputMessage.value.trim()) return

  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: '/app/chat',
      body: JSON.stringify({
        sender: props.username,
        content: inputMessage.value,
        lobbyId: props.chatId
      })
    })
    inputMessage.value = ''
  }
}

onMounted(() => {
  connect()
})

onUnmounted(() => {
  if (stompClient) {
    stompClient.deactivate()
  }
})
</script>

<style scoped>
.chat-overlay {
  position: fixed;
  bottom: 0;
  right: 20px;
  width: 300px;
  background: white;
  border: 1px solid #ccc;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  transition: height 0.3s ease;
  height: 40px; /* Collapsed height */
}

.chat-overlay.chat-open {
  height: 400px;
}

.chat-header {
  background: #0077cc;
  color: white;
  padding: 10px;
  cursor: pointer;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.chat-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 10px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #eee;
  margin-bottom: 10px;
  padding: 5px;
  background: #f9f9f9;
  font-size: 0.9rem;
  text-align: left;
}

.chat-message {
  margin-bottom: 4px;
  word-wrap: break-word;
}

.chat-sender {
  font-weight: bold;
  margin-right: 4px;
  color: #0077cc;
}

.chat-input-area {
  display: flex;
  gap: 5px;
}

.chat-input {
  flex: 1;
  padding: 5px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.send-btn {
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0 10px;
  cursor: pointer;
}

.send-btn:hover {
  background: #218838;
}
</style>
