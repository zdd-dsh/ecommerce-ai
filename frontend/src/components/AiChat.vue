<template>
  <div class="ai-chat">
    <el-button
      v-show="!open"
      class="ai-fab"
      type="primary"
      circle
      @click="open = true"
    >
      AI
    </el-button>

    <el-card v-show="open" class="ai-panel" shadow="hover">
      <template #header>
        <div class="ai-header">
          <div>
            <span class="eyebrow">AI Shopping Guide</span>
            <h3>AI 导购</h3>
          </div>
          <el-button text @click="open = false">收起</el-button>
        </div>
      </template>

      <div ref="listRef" class="ai-list">
        <p v-if="!messages.length && !streamingText" class="ai-hint">
          问我有什么在售商品，例如「有没有降噪耳机」。
        </p>
        <div
          v-for="item in messages"
          :key="item.id"
          class="ai-row"
          :class="item.role"
        >
          <div class="ai-bubble" :class="item.role">{{ item.content }}</div>
        </div>
        <div v-if="streamingText" class="ai-row assistant">
          <div class="ai-bubble assistant">{{ streamingText }}</div>
        </div>
        <p v-if="errorText" class="ai-error">{{ errorText }}</p>
      </div>

      <div class="ai-composer">
        <el-input
          v-model="draft"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4 }"
          placeholder="输入问题，Enter 发送，Shift+Enter 换行"
          :disabled="generating"
          @keydown="onKeydown"
        />
        <div class="ai-actions">
          <el-button
            v-if="generating"
            type="danger"
            @click="stopGenerating"
          >
            停止生成
          </el-button>
          <el-button
            v-else
            type="primary"
            :disabled="!draft.trim()"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, onUnmounted, ref } from 'vue'

const open = ref(false)
const draft = ref('')
const messages = ref([])
const streamingText = ref('')
const generating = ref(false)
const errorText = ref('')
const listRef = ref(null)

let abortController = null
let messageSeq = 0

onUnmounted(() => {
  abortController?.abort()
})

const onKeydown = (event) => {
  if (event.key !== 'Enter' || event.shiftKey) {
    return
  }
  event.preventDefault()
  if (!generating.value) {
    sendMessage()
  }
}

const sendMessage = async () => {
  const text = draft.value.trim()
  if (!text || generating.value) {
    return
  }

  messages.value.push({ id: ++messageSeq, role: 'user', content: text })
  draft.value = ''
  errorText.value = ''
  streamingText.value = ''
  generating.value = true
  abortController = new AbortController()
  await scrollToBottom()

  const decoder = new TextDecoder('utf-8', { stream: true })
  let lineBuffer = ''

  try {
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: text }),
      signal: abortController.signal
    })

    if (!response.ok) {
      throw new Error(await readErrorMessage(response))
    }
    if (!response.body) {
      throw new Error('浏览器不支持流式读取')
    }

    const reader = response.body.getReader()
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        lineBuffer += decoder.decode()
        consumeSseBuffer(lineBuffer, true)
        break
      }
      lineBuffer += decoder.decode(value, { stream: true })
      lineBuffer = consumeSseBuffer(lineBuffer, false)
    }
    commitStreamingMessage()
  } catch (error) {
    if (error?.name === 'AbortError') {
      commitStreamingMessage()
      errorText.value = '已停止生成'
    } else {
      console.error('AI 导购请求失败', error)
      errorText.value = error?.message || '对话失败，请稍后重试'
      commitStreamingMessage()
    }
  } finally {
    generating.value = false
    abortController = null
    await scrollToBottom()
  }
}

const stopGenerating = () => {
  abortController?.abort()
}

const consumeSseBuffer = (buffer, flush) => {
  const parts = buffer.split('\n')
  const rest = flush ? '' : parts.pop() ?? ''
  for (const rawLine of parts) {
    applySseLine(rawLine)
  }
  if (flush && rest) {
    applySseLine(rest)
  }
  return rest
}

const applySseLine = (rawLine) => {
  const line = rawLine.replace(/\r$/, '')
  if (!line) {
    return
  }
  if (!line.startsWith('data:')) {
    return
  }
  let payload = line.slice(5)
  if (payload.startsWith(' ')) {
    payload = payload.slice(1)
  }
  if (payload === '[DONE]') {
    commitStreamingMessage()
    return
  }
  streamingText.value += payload
  scrollToBottom()
}

const commitStreamingMessage = () => {
  const content = streamingText.value
  if (!content) {
    return
  }
  messages.value.push({ id: ++messageSeq, role: 'assistant', content })
  streamingText.value = ''
}

const readErrorMessage = async (response) => {
  const raw = await response.text()
  try {
    const body = JSON.parse(raw)
    return body.message || `请求失败（${response.status}）`
  } catch (error) {
    console.error('解析错误响应失败', error, raw)
    return raw || `请求失败（${response.status}）`
  }
}

const scrollToBottom = async () => {
  await nextTick()
  const list = listRef.value
  if (list) {
    list.scrollTop = list.scrollHeight
  }
}
</script>

<style scoped>
.ai-chat {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 200;
}

.ai-fab {
  width: 56px;
  height: 56px;
  font-weight: 700;
  box-shadow: 0 12px 28px rgba(64, 158, 255, 0.35);
}

.ai-panel {
  width: 360px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  overflow: hidden;
}

.ai-panel :deep(.el-card__header) {
  padding: 16px 18px 12px;
  border-bottom: 1px solid #eef2f7;
}

.ai-panel :deep(.el-card__body) {
  padding: 0;
}

.ai-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.eyebrow {
  display: block;
  color: #409eff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.ai-header h3 {
  margin-top: 4px;
  font-size: 18px;
  color: #303133;
}

.ai-list {
  height: 320px;
  overflow-y: auto;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: rgba(248, 251, 255, 0.92);
}

.ai-hint,
.ai-error {
  font-size: 13px;
  line-height: 1.6;
}

.ai-hint { color: #909399; }
.ai-error { color: #f56c6c; }

.ai-row {
  display: flex;
}

.ai-row.user {
  justify-content: flex-end;
}

.ai-bubble {
  max-width: 82%;
  padding: 10px 12px;
  border-radius: 16px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
}

.ai-bubble.user {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 6px;
}

.ai-bubble.assistant {
  background: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 6px;
}

.ai-composer {
  padding: 12px 18px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-top: 1px solid #eef2f7;
}

.ai-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .ai-panel { width: min(360px, calc(100vw - 32px)); }
}
</style>
