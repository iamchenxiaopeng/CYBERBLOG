<template>
  <div class="detail-view">
    <div class="reading-bar" :style="{ width: readingProgress + '%' }"></div>

    <div v-if="loading" class="loading page-container">
      <span class="neon">LOADING NEURAL DATA...</span>
    </div>

    <template v-else-if="article">
      <!-- Cover -->
      <div v-if="article.coverImg" class="cover-img">
        <img :src="article.coverImg" :alt="article.title" />
        <div class="cover-overlay"></div>
      </div>

      <div class="page-container detail-container">
        <!-- Header -->
        <header class="article-header cyber-corners orbiting-border">
          <div class="article-meta">
            <img :src="article.avatarUrl || '/avatar-default.svg'" class="cyber-avatar" @error="(e: Event) => (e.target as HTMLImageElement).src = '/avatar-default.svg'" />
            <span class="meta-author neon">{{ article.username }}</span>
            <span class="meta-sep">//</span>
            <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
            <span class="meta-sep">//</span>
            <span class="meta-views">👁 {{ article.viewCount }}</span>
            <!-- 编辑按钮 -->
            <button
              v-if="canEdit"
              class="btn-detail-edit"
              @click="router.push(`/write/${article.id}`)"
            >✎ EDIT</button>
            <!-- 删除按钮 -->
            <button
              v-if="canDelete"
              class="btn-detail-delete"
              @click="handleDeleteArticle"
            >✕ DELETE</button>
          </div>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-tags">
            <span v-for="tag in parseTags(article.tags)" :key="tag" class="cyber-tag">{{ tag }}</span>
          </div>
        </header>

        <!-- Content + Sidebar -->
        <div class="detail-layout">
          <div class="article-content cyber-prose" v-html="renderedContent"></div>
          <aside class="sidebar">
            <div class="sidebar-card cyber-card cyber-corners">
              <h3 class="sidebar-title">互动数据</h3>
              <div class="sidebar-stats">
                <div class="stat-item">
                  <span class="stat-val neon">{{ article.viewCount }}</span>
                  <span class="stat-key">阅读</span>
                </div>
                <div class="stat-item">
                  <span class="stat-val neon-pink">{{ article.likeCount }}</span>
                  <span class="stat-key">点赞</span>
                </div>
                <div class="stat-item">
                  <span class="stat-val" style="color:var(--cyber-accent)">{{ article.commentCount }}</span>
                  <span class="stat-key">评论</span>
                </div>
              </div>
              <button
                class="like-btn"
                :class="{ liked: article.liked }"
                @click="toggleLike"
              >
                {{ article.liked ? '♥ 已点赞' : '♡ 点赞' }}
              </button>
            </div>
          </aside>
        </div>

        <hr class="cyber-divider" />

        <!-- Comments -->
        <section class="comments-section">
          <h2 class="section-title">
            <span class="neon">></span> 评论区 ({{ article.commentCount }})
          </h2>

          <!-- Add comment -->
          <div v-if="userStore.isLoggedIn" class="comment-form cyber-card">
            <label class="cyber-label">发表评论</label>
            <textarea
              v-model="commentContent"
              class="cyber-input"
              placeholder="在数字废墟中留下你的痕迹..."
              rows="4"
            ></textarea>
            <div class="form-actions">
              <button class="btn-cyber btn-cyber-sm" @click="submitComment" :disabled="submitting">
                {{ submitting ? 'SENDING...' : '提交' }}
              </button>
            </div>
          </div>
          <div v-else class="login-tip">
            <RouterLink to="/login" class="neon">登录</RouterLink>
            <span> 后才能发表评论</span>
          </div>

          <!-- Comment list -->
          <div class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item cyber-card">
              <div class="comment-header">
                <img :src="comment.avatarUrl || '/avatar-default.svg'" class="cyber-avatar" @error="(e: Event) => (e.target as HTMLImageElement).src = '/avatar-default.svg'" />
                <span class="comment-author neon">{{ comment.username }}</span>
                <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                <button
                  v-if="userStore.user?.id === comment.userId"
                  class="btn-del"
                  @click="deleteComment(comment.id)"
                >删除</button>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
              <!-- Replies -->
              <div v-if="comment.replies?.length" class="replies">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                  <img :src="reply.avatarUrl || '/avatar-default.svg'" class="cyber-avatar" style="width:26px;height:26px" @error="(e: Event) => (e.target as HTMLImageElement).src = '/avatar-default.svg'" />
                  <span class="comment-author neon" style="font-size:11px">{{ reply.username }}</span>
                  <span class="comment-content">{{ reply.content }}</span>
                </div>
              </div>
              <!-- Reply input -->
              <div v-if="replyTo === comment.id" class="reply-form">
                <input
                  v-model="replyContent"
                  class="cyber-input"
                  placeholder="回复..."
                  @keyup.enter="submitReply(comment.id)"
                />
                <button class="btn-cyber btn-cyber-sm" @click="submitReply(comment.id)">回复</button>
                <button class="btn-cyber btn-cyber-sm" style="color:var(--cyber-text-muted)" @click="replyTo=null">取消</button>
              </div>
              <button v-else-if="userStore.isLoggedIn" class="reply-btn" @click="replyTo=comment.id">回复</button>
            </div>
          </div>
        </section>
      </div>
    </template>

    <div v-else class="not-found page-container">
      <span class="neon-pink">// 404 ARTICLE NOT FOUND //</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import hljs from 'highlight.js'
import { articleApi } from '@/api/article'
import { commentApi, likeApi } from '@/api/interact'
import { useUserStore } from '@/stores/user'
import { showToast } from '@/utils/toast'
import type { Article, Comment } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const article = ref<Article | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(true)
const commentContent = ref('')
const replyContent = ref('')
const replyTo = ref<number | null>(null)
const submitting = ref(false)
const readingProgress = ref(0)

// 是否可删除：已登录 且 (是作者 或 是管理员)
const canDelete = computed(() => {
  if (!userStore.user || !article.value) return false
  return userStore.user.id === article.value.userId || userStore.user.username === 'admin'
})

// 是否可编辑：同删除权限（作者 或 管理员）
const canEdit = computed(() => {
  if (!userStore.user || !article.value) return false
  return userStore.user.id === article.value.userId || userStore.user.username === 'admin'
})

// Setup marked
marked.setOptions({
  highlight: (code: string, lang: string) => {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  }
} as any)

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return marked(article.value.content) as string
})

function handleScroll() {
  const el = document.documentElement
  const scrollTop = el.scrollTop || document.body.scrollTop
  const scrollHeight = el.scrollHeight - el.clientHeight
  readingProgress.value = scrollHeight > 0 ? Math.round((scrollTop / scrollHeight) * 100) : 0
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const [articleRes, commentRes] = await Promise.all([
      articleApi.get(id),
      commentApi.list(id)
    ])
    if (articleRes.code === 200) article.value = articleRes.data
    if (commentRes.code === 200) comments.value = commentRes.data
  } finally {
    loading.value = false
  }
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => window.removeEventListener('scroll', handleScroll))

async function toggleLike() {
  if (!article.value) return
  try {
    const res = await likeApi.toggle('article', article.value.id)
    if (res.code === 200) {
      article.value.liked = res.data.liked
      article.value.likeCount = Math.max(0, article.value.likeCount + (res.data.liked ? 1 : -1))
      showToast(res.data.liked ? '点赞成功' : '已取消点赞', 'success')
    }
  } catch (e: any) {
    showToast(e.message || '操作失败，请重试', 'error')
  }
}

async function submitComment() {
  if (!commentContent.value.trim()) return
  submitting.value = true
  try {
    const res = await commentApi.add(Number(route.params.id), commentContent.value)
    if (res.code === 200) {
      comments.value.unshift(res.data)
      if (article.value) article.value.commentCount++
      commentContent.value = ''
    }
  } finally {
    submitting.value = false
  }
}

async function submitReply(parentId: number) {
  if (!replyContent.value.trim()) return
  const res = await commentApi.add(Number(route.params.id), replyContent.value, parentId)
  if (res.code === 200) {
    const parent = comments.value.find(c => c.id === parentId)
    if (parent) parent.replies = parent.replies || []
    parent?.replies.push(res.data)
    replyContent.value = ''
    replyTo.value = null
  }
}

async function deleteComment(commentId: number) {
  const res = await commentApi.delete(Number(route.params.id), commentId)
  if (res.code === 200) {
    comments.value = comments.value.filter(c => c.id !== commentId)
    if (article.value) article.value.commentCount--
  }
}

async function handleDeleteArticle() {
  if (!article.value) return
  if (!confirm('确定删除这篇文章吗？此操作不可撤销。')) return
  try {
    const res = await articleApi.delete(article.value.id)
    if (res.code === 200) {
      showToast('文章已删除', 'success')
      router.push('/')
    }
  } catch (e: any) {
    showToast(e.message || '删除失败', 'error')
  }
}

function formatDate(s: string) {
  return new Date(s).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
function parseTags(tags: string) {
  return tags ? tags.split(',').filter(Boolean) : []
}
</script>

<style scoped>
.detail-view { min-height: 100vh; }
.loading, .not-found { padding: 80px 24px; text-align: center; font-family: var(--font-mono); font-size: 14px; letter-spacing: 3px; }

.cover-img { position: relative; height: 320px; overflow: hidden; }
.cover-img img { width: 100%; height: 100%; object-fit: cover; filter: brightness(0.5); }
.cover-overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, transparent, var(--cyber-bg)); }

.detail-container { padding: 40px 24px 80px; }
.article-header { padding: 32px; background: var(--cyber-bg-card); border: 1px solid var(--cyber-border); margin-bottom: 40px; }
.article-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--cyber-text-muted);
  margin-bottom: 16px;
}
.meta-sep { color: var(--cyber-primary); }
.article-title {
  font-family: var(--font-cyber);
  font-size: clamp(1.4rem, 3vw, 2.2rem);
  font-weight: 900;
  color: var(--cyber-text);
  margin-bottom: 16px;
  line-height: 1.3;
}
.article-tags { display: flex; flex-wrap: wrap; gap: 8px; }

.detail-layout { display: grid; grid-template-columns: 1fr 240px; gap: 40px; align-items: start; }
@media (max-width: 900px) { .detail-layout { grid-template-columns: 1fr; } }

.article-content { min-width: 0; }
.sidebar { position: sticky; top: 80px; }
.sidebar-card { padding: 20px; }
.sidebar-title { font-family: var(--font-cyber); font-size: 12px; letter-spacing: 2px; color: var(--cyber-primary); margin-bottom: 16px; }
.sidebar-stats { display: flex; justify-content: space-between; margin-bottom: 16px; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat-val { font-family: var(--font-cyber); font-size: 20px; font-weight: 700; }
.stat-key { font-family: var(--font-mono); font-size: 10px; color: var(--cyber-text-muted); letter-spacing: 1px; }
.like-btn { width: 100%; justify-content: center; }

.comments-section { margin-top: 40px; }
.section-title { font-family: var(--font-cyber); font-size: 18px; letter-spacing: 3px; margin-bottom: 24px; }
.comment-form { padding: 20px; margin-bottom: 24px; }
.form-actions { margin-top: 12px; }
.login-tip { font-family: var(--font-mono); font-size: 13px; color: var(--cyber-text-muted); margin-bottom: 24px; }
.comment-list { display: flex; flex-direction: column; gap: 16px; }
.comment-item { padding: 16px 20px; }
.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-mono);
  font-size: 12px;
  margin-bottom: 10px;
}
.comment-author { color: var(--cyber-primary); }
.comment-time { color: var(--cyber-text-muted); margin-left: auto; }
.comment-content { font-size: 14px; line-height: 1.6; color: var(--cyber-text); }
.btn-del { font-family: var(--font-mono); font-size: 10px; color: var(--cyber-red); background: none; border: none; cursor: pointer; }

/* 文章详情页编辑按钮 */
.btn-detail-edit {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--cyber-primary);
  background: rgba(0, 245, 255, 0.08);
  border: 1px solid rgba(0, 245, 255, 0.25);
  padding: 4px 14px;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-detail-edit:hover {
  background: var(--cyber-primary);
  color: var(--cyber-bg);
  box-shadow: 0 0 16px rgba(0, 245, 255, 0.5);
}

/* 文章详情页删除按钮 */
.btn-detail-delete {
  margin-left: auto;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--cyber-red);
  background: rgba(255, 50, 80, 0.08);
  border: 1px solid rgba(255, 50, 80, 0.25);
  padding: 4px 14px;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-detail-delete:hover {
  background: var(--cyber-red);
  color: #fff;
  box-shadow: 0 0 16px rgba(255, 50, 80, 0.5);
}
.replies { margin-top: 12px; padding-top: 12px; border-top: 1px solid rgba(0,245,255,0.08); display: flex; flex-direction: column; gap: 8px; }
.reply-item { display: flex; align-items: baseline; gap: 8px; font-size: 13px; }
.reply-form { display: flex; gap: 8px; margin-top: 12px; }
.reply-btn { background: none; border: none; color: var(--cyber-text-muted); font-family: var(--font-mono); font-size: 11px; cursor: pointer; margin-top: 8px; }
.reply-btn:hover { color: var(--cyber-primary); }
</style>
