<template>
  <div class="write-view page-container">
    <div class="write-header">
      <h1 class="neon">// 创作控制台</h1>
      <div class="mode-tabs">
        <button
          v-for="m in modes"
          :key="m.key"
          class="btn-cyber btn-cyber-sm"
          :class="{ active: mode === m.key }"
          @click="mode = m.key"
        >{{ m.label }}</button>
      </div>
    </div>

    <!-- Upload MD Mode -->
    <div v-if="mode === 'upload'" class="upload-zone cyber-card cyber-corners" @dragover.prevent @drop.prevent="handleDrop">
      <div class="upload-inner">
        <div class="upload-icon neon">⬆</div>
        <p class="upload-text">拖拽 .md 文件到此处</p>
        <p class="upload-sub">或</p>
        <label class="btn-cyber" style="cursor:pointer">
          选择文件
          <input type="file" accept=".md" @change="handleFileSelect" style="display:none" />
        </label>
        <div v-if="selectedFile" class="file-info">
          <span class="neon-yellow">// {{ selectedFile.name }}</span>
        </div>
      </div>

      <div v-if="selectedFile" class="upload-meta">
        <hr class="cyber-divider" />
        <div class="form-group">
          <label class="cyber-label">文章标题（留空则使用文件名）</label>
          <input v-model="uploadForm.title" class="cyber-input" placeholder="可选：自定义标题" />
        </div>
        <div class="form-group">
          <label class="cyber-label">标签（逗号分隔）</label>
          <input v-model="uploadForm.tags" class="cyber-input" placeholder="例: 科技,赛博朋克,AI" />
        </div>
        <button class="btn-cyber" @click="submitUpload" :disabled="uploading">
          {{ uploading ? 'UPLOADING...' : '上传发布' }}
        </button>
      </div>
    </div>

    <!-- Write Mode -->
    <div v-else class="write-editor cyber-card">
      <div class="editor-toolbar">
        <input
          v-model="writeForm.title"
          class="cyber-input editor-title"
          placeholder="文章标题..."
          required
        />
      </div>
      <div class="editor-meta">
        <input v-model="writeForm.tags" class="cyber-input" placeholder="标签（逗号分隔）" />
        <select v-model="writeForm.status" class="cyber-input">
          <option value="published">发布</option>
          <option value="draft">草稿</option>
        </select>
      </div>

      <!-- Editor / Preview tabs -->
      <div class="editor-tabs">
        <button :class="['tab', { active: editorTab === 'edit' }]" @click="editorTab='edit'">编辑</button>
        <button :class="['tab', { active: editorTab === 'preview' }]" @click="editorTab='preview'">预览</button>
      </div>
      <div v-if="editorTab === 'edit'" class="editor-area">
        <textarea
          v-model="writeForm.content"
          class="cyber-input md-editor"
          placeholder="支持 Markdown 语法..."
        ></textarea>
      </div>
      <div v-else class="preview-area cyber-prose" v-html="previewHtml"></div>

      <div class="editor-actions">
        <button class="btn-cyber" @click="submitWrite" :disabled="writing">
          {{ writing ? 'PUBLISHING...' : '发布文章' }}
        </button>
      </div>
    </div>

    <div v-if="successMsg" class="success-msg neon-yellow">
      {{ successMsg }}
      <RouterLink :to="`/articles/${publishedId}`" class="neon">查看文章</RouterLink>
    </div>
    <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import hljs from 'highlight.js'
import { articleApi } from '@/api/article'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 进入页面时检查登录状态（token 可能已过期）
onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.replace('/login?redirect=/write')
  }
})

const modes: { key: 'write' | 'upload'; label: string }[] = [
  { key: 'write', label: '手写创作' },
  { key: 'upload', label: '上传 MD' }
]
const mode = ref<'write' | 'upload'>('write')
const editorTab = ref<'edit' | 'preview'>('edit')

const selectedFile = ref<File | null>(null)
const uploadForm = ref({ title: '', tags: '' })
const uploading = ref(false)

const writeForm = ref({ title: '', content: '', tags: '', status: 'published' })
const writing = ref(false)

const successMsg = ref('')
const errorMsg = ref('')
const publishedId = ref<number>(0)

marked.setOptions({
  highlight: (code: string, lang: string) => {
    if (lang && hljs.getLanguage(lang)) return hljs.highlight(code, { language: lang }).value
    return hljs.highlightAuto(code).value
  }
} as any)

const previewHtml = computed(() => {
  return writeForm.value.content ? marked(writeForm.value.content) as string : '<p style="color:var(--cyber-text-muted)">// 暂无内容 //</p>'
})

function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.[0]) selectedFile.value = input.files[0]
}
function handleDrop(e: DragEvent) {
  const file = e.dataTransfer?.files[0]
  if (file?.name.endsWith('.md')) selectedFile.value = file
}

async function submitUpload() {
  if (!userStore.isLoggedIn) {
    errorMsg.value = '登录已过期，请重新登录'
    setTimeout(() => router.push('/login?redirect=/write'), 1500)
    return
  }
  if (!selectedFile.value) return
  uploading.value = true
  errorMsg.value = ''
  try {
    const fd = new FormData()
    fd.append('file', selectedFile.value)
    if (uploadForm.value.title) fd.append('title', uploadForm.value.title)
    if (uploadForm.value.tags) fd.append('tags', uploadForm.value.tags)
    const res = await articleApi.upload(fd)
    if (res.code === 200) {
      publishedId.value = res.data.id
      successMsg.value = '// 文章已上传发布 //'
    } else {
      errorMsg.value = res.message
    }
  } catch (e: any) {
    errorMsg.value = e.message
  } finally {
    uploading.value = false
  }
}

async function submitWrite() {
  if (!userStore.isLoggedIn) {
    errorMsg.value = '登录已过期，请重新登录'
    setTimeout(() => router.push('/login?redirect=/write'), 1500)
    return
  }
  if (!writeForm.value.title.trim()) {
    errorMsg.value = '请填写文章标题'
    return
  }
  writing.value = true
  errorMsg.value = ''
  try {
    const res = await articleApi.create({
      title: writeForm.value.title,
      content: writeForm.value.content,
      tags: writeForm.value.tags,
      status: writeForm.value.status
    })
    if (res.code === 200) {
      publishedId.value = res.data.id
      successMsg.value = '// 文章发布成功 //'
    } else {
      errorMsg.value = res.message
    }
  } catch (e: any) {
    errorMsg.value = e.message
  } finally {
    writing.value = false
  }
}
</script>

<style scoped>
.write-view { padding: 40px 24px; }
.write-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 32px; flex-wrap: wrap; gap: 16px; }
.write-header h1 { font-family: var(--font-cyber); font-size: 22px; letter-spacing: 4px; }
.mode-tabs { display: flex; gap: 8px; }
.btn-cyber.active { background: var(--cyber-primary); color: var(--cyber-bg); }

/* Upload Zone */
.upload-zone { padding: 40px; text-align: center; border: 2px dashed var(--cyber-border); transition: border-color 0.2s; }
.upload-zone:hover { border-color: var(--cyber-primary); }
.upload-inner { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.upload-icon { font-size: 48px; line-height: 1; }
.upload-text { font-family: var(--font-cyber); font-size: 14px; letter-spacing: 2px; }
.upload-sub { color: var(--cyber-text-muted); font-family: var(--font-mono); font-size: 12px; }
.file-info { font-family: var(--font-mono); font-size: 12px; margin-top: 8px; }
.upload-meta { text-align: left; display: flex; flex-direction: column; gap: 16px; }

/* Editor */
.write-editor { padding: 24px; }
.editor-toolbar { margin-bottom: 16px; }
.editor-title { font-size: 18px; font-family: var(--font-cyber); letter-spacing: 2px; }
.editor-meta { display: grid; grid-template-columns: 1fr auto; gap: 12px; margin-bottom: 16px; }
.editor-tabs { display: flex; gap: 0; margin-bottom: 0; border-bottom: 1px solid var(--cyber-border); }
.tab {
  font-family: var(--font-cyber);
  font-size: 11px;
  letter-spacing: 2px;
  padding: 8px 20px;
  background: none;
  border: none;
  color: var(--cyber-text-muted);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}
.tab.active { color: var(--cyber-primary); border-bottom-color: var(--cyber-primary); }
.editor-area { margin-top: 1px; }
.md-editor { min-height: 400px; border-radius: 0; border-top: none; font-family: var(--font-mono); font-size: 14px; line-height: 1.7; }
.preview-area { min-height: 400px; padding: 16px; background: var(--cyber-bg-input); border: 1px solid var(--cyber-border); border-top: none; }
.editor-actions { margin-top: 16px; }

.form-group { display: flex; flex-direction: column; gap: 6px; }
.success-msg { margin-top: 20px; font-family: var(--font-mono); font-size: 13px; letter-spacing: 2px; }
.error-msg { margin-top: 20px; color: var(--cyber-red); font-family: var(--font-mono); font-size: 12px; }
</style>
