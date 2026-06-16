<template>
  <article class="article-card cyber-card animate-card-3d" @click="router.push(`/articles/${article.id}`)">
    <div v-if="article.coverImg" class="card-cover">
      <div class="cover-overlay"></div>
      <img :src="article.coverImg" :alt="article.title" />
    </div>
    <div class="card-body">
      <div class="card-meta">
        <img :src="article.avatarUrl" :alt="article.username" class="cyber-avatar" />
        <span class="meta-author">{{ article.username }}</span>
        <span class="meta-sep">//</span>
        <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
        <!-- 删除按钮：仅作者和管理员可见 -->
        <button
          v-if="canDelete"
          class="btn-card-delete"
          title="删除文章"
          @click.stop.prevent="handleDelete"
        >✕ DELETE</button>
      </div>
      <h2 class="card-title">{{ article.title }}</h2>
      <p class="card-summary">{{ article.summary }}</p>
      <div class="card-footer">
        <div class="card-tags">
          <span v-for="tag in parseTags(article.tags)" :key="tag" class="cyber-tag">{{ tag }}</span>
        </div>
        <div class="card-stats">
          <span class="stat-item">👁 {{ article.viewCount }}</span>
          <span class="stat-item">♥ {{ article.likeCount }}</span>
          <span class="stat-item">💬 {{ article.commentCount }}</span>
        </div>
      </div>
    </div>
    <!-- 装饰性边角 -->
    <div class="card-corner card-corner-tl"></div>
    <div class="card-corner card-corner-tr"></div>
    <div class="card-corner card-corner-bl"></div>
    <div class="card-corner card-corner-br"></div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api/article'
import { showToast } from '@/utils/toast'
import type { Article } from '@/types'

const emit = defineEmits<{ deleted: [id: number] }>()
const props = defineProps<{ article: Article }>()

const router = useRouter()
const userStore = useUserStore()

// 是否显示删除按钮：已登录 且 (是作者 或 是管理员)
const canDelete = computed(() => {
  if (!userStore.user) return false
  return userStore.user.id === props.article.userId || userStore.user.username === 'admin'
})

async function handleDelete() {
  if (!confirm(`确定删除文章「${props.article.title}」吗？此操作不可撤销。`)) return
  try {
    const res = await articleApi.delete(props.article.id)
    if (res.code === 200) {
      showToast('文章已删除', 'success')
      emit('deleted', props.article.id)
    }
  } catch (e: any) {
    showToast(e.message || '删除失败', 'error')
  }
}

function formatDate(s: string) {
  return new Date(s).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
function parseTags(tags: string) {
  return tags ? tags.split(',').filter(Boolean).slice(0, 3) : []
}
</script>

<style scoped>
.article-card {
  cursor: pointer;
  overflow: hidden;
  position: relative;
}

.card-cover {
  height: 180px;
  overflow: hidden;
  position: relative;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.6;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 0%, var(--cyber-bg-card) 100%);
  z-index: 1;
  transition: opacity 0.3s;
}

.article-card:hover .card-cover img {
  opacity: 1;
  transform: scale(1.1);
}

.card-body {
  padding: 20px;
  position: relative;
  z-index: 2;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--cyber-text-muted);
  margin-bottom: 12px;
}

.meta-sep { color: var(--cyber-primary); }
.meta-author { color: var(--cyber-primary); }

/* 卡片内删除按钮 */
.btn-card-delete {
  margin-left: auto;
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 1px;
  color: var(--cyber-red);
  background: rgba(255, 50, 80, 0.08);
  border: 1px solid rgba(255, 50, 80, 0.25);
  padding: 2px 10px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.3s;
}
.article-card:hover .btn-card-delete { opacity: 1; }
.btn-card-delete:hover {
  background: var(--cyber-red);
  color: #fff;
  box-shadow: 0 0 12px rgba(255, 50, 80, 0.5);
}

.card-title {
  font-family: var(--font-cyber);
  font-size: 16px;
  font-weight: 700;
  color: var(--cyber-text);
  margin-bottom: 10px;
  line-height: 1.4;
  transition: all 0.3s;
}

.article-card:hover .card-title {
  color: var(--cyber-primary);
  text-shadow: 0 0 20px rgba(0, 245, 255, 0.5);
}

.card-summary {
  font-size: 13px;
  color: var(--cyber-text-muted);
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s;
}

.article-card:hover .card-summary {
  color: var(--cyber-text);
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.card-stats {
  display: flex;
  gap: 12px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--cyber-text-muted);
}

.stat-item {
  transition: all 0.3s;
}

.article-card:hover .stat-item {
  color: var(--cyber-primary);
  text-shadow: 0 0 10px rgba(0, 245, 255, 0.3);
}

/* 边角装饰 */
.card-corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border-color: var(--cyber-primary);
  border-style: solid;
  opacity: 0;
  transition: all 0.3s;
  z-index: 10;
}

.card-corner-tl {
  top: 0;
  left: 0;
  border-width: 2px 0 0 2px;
}

.card-corner-tr {
  top: 0;
  right: 0;
  border-width: 2px 2px 0 0;
}

.card-corner-bl {
  bottom: 0;
  left: 0;
  border-width: 0 0 2px 2px;
}

.card-corner-br {
  bottom: 0;
  right: 0;
  border-width: 0 2px 2px 0;
}

.article-card:hover .card-corner {
  opacity: 1;
}

.article-card:hover .card-corner-tl,
.article-card:hover .card-corner-tr {
  width: 30px;
  height: 30px;
}

.article-card:hover .card-corner-bl,
.article-card:hover .card-corner-br {
  width: 30px;
  height: 30px;
}

/* 发光边框效果 */
.article-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border: 2px solid transparent;
  background: linear-gradient(90deg, var(--cyber-primary), var(--cyber-secondary)) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

.article-card:hover::after {
  opacity: 0.5;
}
</style>
