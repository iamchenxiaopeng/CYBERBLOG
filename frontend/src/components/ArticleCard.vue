<template>
  <article class="article-card cyber-card" @click="router.push(`/articles/${article.id}`)">
    <div v-if="article.coverImg" class="card-cover">
      <img :src="article.coverImg" :alt="article.title" />
    </div>
    <div class="card-body">
      <div class="card-meta">
        <img :src="article.avatarUrl" :alt="article.username" class="cyber-avatar" />
        <span class="meta-author">{{ article.username }}</span>
        <span class="meta-sep">//</span>
        <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
      </div>
      <h2 class="card-title">{{ article.title }}</h2>
      <p class="card-summary">{{ article.summary }}</p>
      <div class="card-footer">
        <div class="card-tags">
          <span v-for="tag in parseTags(article.tags)" :key="tag" class="cyber-tag">{{ tag }}</span>
        </div>
        <div class="card-stats">
          <span>👁 {{ article.viewCount }}</span>
          <span>♥ {{ article.likeCount }}</span>
          <span>💬 {{ article.commentCount }}</span>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Article } from '@/types'

const router = useRouter()
defineProps<{ article: Article }>()

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
  transition: transform 0.2s;
}
.article-card:hover { transform: translateY(-2px); }
.card-cover { height: 180px; overflow: hidden; }
.card-cover img { width: 100%; height: 100%; object-fit: cover; opacity: 0.7; transition: opacity 0.2s; }
.article-card:hover .card-cover img { opacity: 1; }
.card-body { padding: 20px; }
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
.card-title {
  font-family: var(--font-cyber);
  font-size: 16px;
  font-weight: 700;
  color: var(--cyber-text);
  margin-bottom: 10px;
  line-height: 1.4;
  transition: color 0.2s;
}
.article-card:hover .card-title { color: var(--cyber-primary); }
.card-summary {
  font-size: 13px;
  color: var(--cyber-text-muted);
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-stats {
  display: flex;
  gap: 12px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--cyber-text-muted);
}
</style>
