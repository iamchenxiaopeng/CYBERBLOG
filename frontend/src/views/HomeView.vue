<template>
  <div class="home-view">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-grid"></div>
        <div class="hero-glow"></div>
      </div>
      <div class="page-container hero-content">
        <div class="hero-badge">
          <span class="neon">// SYSTEM ONLINE //</span>
        </div>
        <h1 class="hero-title">
          <span class="glitch" data-text="CYBERBLOG">CYBERBLOG</span>
        </h1>
        <p class="hero-subtitle cursor">在数字废墟中，记录人类的光与影</p>
        <div class="hero-actions">
          <RouterLink to="/articles" class="btn-cyber">浏览文章</RouterLink>
          <RouterLink v-if="!userStore.isLoggedIn" to="/register" class="btn-cyber btn-cyber-pink">
            接入神经网络
          </RouterLink>
          <RouterLink v-else to="/write" class="btn-cyber btn-cyber-pink">写作</RouterLink>
        </div>
        <div class="hero-stats">
          <div class="stat">
            <span class="stat-num neon">{{ stats.articles }}</span>
            <span class="stat-label">篇文章</span>
          </div>
          <div class="stat-sep">//</div>
          <div class="stat">
            <span class="stat-num neon-pink">{{ stats.users }}</span>
            <span class="stat-label">位作者</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Latest Articles -->
    <section class="latest-section page-container">
      <div class="section-header">
        <h2 class="section-title">
          <span class="neon">></span> 最新日志
        </h2>
        <RouterLink to="/articles" class="btn-cyber btn-cyber-sm">查看全部</RouterLink>
      </div>
      <div v-if="loading" class="loading">
        <span class="neon">LOADING...</span>
      </div>
      <div v-else class="articles-grid">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api/article'
import { useUserStore } from '@/stores/user'
import ArticleCard from '@/components/ArticleCard.vue'
import type { Article } from '@/types'

const userStore = useUserStore()
const articles = ref<Article[]>([])
const loading = ref(true)
const stats = ref({ articles: 0, users: 0 })

onMounted(async () => {
  try {
    const res = await articleApi.list(1, 6)
    if (res.code === 200) {
      articles.value = res.data.records
      stats.value.articles = res.data.total
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-view { overflow-x: hidden; }

/* Hero */
.hero {
  position: relative;
  min-height: 520px;
  display: flex;
  align-items: center;
  overflow: hidden;
}
.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,245,255,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,245,255,0.04) 1px, transparent 1px);
  background-size: 60px 60px;
}
.hero-glow {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  width: 600px; height: 400px;
  background: radial-gradient(ellipse, rgba(0,245,255,0.06) 0%, transparent 70%);
  pointer-events: none;
}
.hero-content {
  position: relative;
  padding: 80px 24px;
  z-index: 1;
}
.hero-badge {
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 3px;
  margin-bottom: 24px;
}
.hero-title {
  font-family: var(--font-cyber);
  font-size: clamp(3rem, 8vw, 6rem);
  font-weight: 900;
  letter-spacing: 8px;
  color: var(--cyber-primary);
  text-shadow: var(--cyber-glow);
  line-height: 1;
  margin-bottom: 20px;
}
.hero-subtitle {
  font-family: var(--font-mono);
  font-size: 16px;
  color: var(--cyber-text-muted);
  margin-bottom: 36px;
  letter-spacing: 2px;
}
.hero-actions { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 48px; }
.hero-stats { display: flex; align-items: center; gap: 20px; }
.stat { display: flex; flex-direction: column; }
.stat-num { font-family: var(--font-cyber); font-size: 28px; font-weight: 900; }
.stat-label { font-family: var(--font-mono); font-size: 11px; color: var(--cyber-text-muted); letter-spacing: 2px; }
.stat-sep { font-family: var(--font-mono); color: var(--cyber-text-muted); font-size: 18px; }

/* Latest */
.latest-section { padding: 60px 24px; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 32px; }
.section-title {
  font-family: var(--font-cyber);
  font-size: 20px;
  letter-spacing: 3px;
  color: var(--cyber-text);
}
.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}
.loading { text-align: center; padding: 60px; font-family: var(--font-mono); font-size: 14px; letter-spacing: 3px; }
</style>
