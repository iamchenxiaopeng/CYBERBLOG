<template>
  <div class="list-view page-container">
    <div class="list-header">
      <h1 class="neon">// 文章数据库</h1>
      <div class="search-bar">
        <input
          v-model="keyword"
          class="cyber-input"
          placeholder="搜索文章标题或摘要..."
          @keyup.enter="search"
        />
        <button class="btn-cyber" @click="search">检索</button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <span class="neon">FETCHING DATA...</span>
    </div>
    <template v-else>
      <div class="articles-grid">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
      <div v-if="articles.length === 0" class="empty">
        <span class="neon-pink">// NO DATA FOUND //</span>
      </div>
      <!-- Pagination -->
      <div v-if="total > 0" class="pagination">
        <button class="btn-cyber btn-cyber-sm" :disabled="page <= 1" @click="changePage(page - 1)">
          &lt; PREV
        </button>
        <span class="page-info">{{ page }} / {{ totalPages }}</span>
        <button class="btn-cyber btn-cyber-sm" :disabled="page >= totalPages" @click="changePage(page + 1)">
          NEXT &gt;
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import type { Article } from '@/types'

const articles = ref<Article[]>([])
const loading = ref(true)
const keyword = ref('')
const page = ref(1)
const total = ref(0)
const totalPages = ref(1)

async function fetchArticles() {
  loading.value = true
  try {
    const res = await articleApi.list(page.value, 12, keyword.value || undefined)
    if (res.code === 200) {
      articles.value = res.data.records
      total.value = res.data.total
      totalPages.value = res.data.pages
    }
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  fetchArticles()
}

function changePage(p: number) {
  page.value = p
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(fetchArticles)
</script>

<style scoped>
.list-view { padding: 40px 24px; }
.list-header { margin-bottom: 40px; }
.list-header h1 {
  font-family: var(--font-cyber);
  font-size: 24px;
  letter-spacing: 4px;
  margin-bottom: 20px;
}
.search-bar { display: flex; gap: 12px; max-width: 500px; }
.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}
.empty { text-align: center; padding: 80px; font-family: var(--font-mono); font-size: 14px; letter-spacing: 3px; }
.loading { text-align: center; padding: 80px; font-family: var(--font-mono); font-size: 14px; letter-spacing: 3px; }
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 20px 0;
}
.page-info { font-family: var(--font-mono); font-size: 12px; color: var(--cyber-primary); letter-spacing: 2px; }
button:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
