<template>
  <div class="home-view">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-grid"></div>
        <div class="hero-glow"></div>
      </div>
      <div class="page-container hero-content">
        <div ref="badgeRef" class="hero-badge" :style="badgeStyle">
          <span class="neon typing-effect">// SYSTEM ONLINE //</span>
        </div>
        <h1 ref="titleRef" class="hero-title" :style="titleStyle">
          <span class="glitch" data-text="CYBERBLOG">CYBERBLOG</span>
        </h1>
        <p ref="subtitleRef" class="hero-subtitle cursor" :style="subtitleStyle">在数字废墟中，记录人类的光与影</p>
        <div ref="actionsRef" class="hero-actions" :style="actionsStyle">
          <RouterLink to="/articles" class="btn-cyber btn-animate">浏览文章</RouterLink>
          <RouterLink v-if="!userStore.isLoggedIn" to="/register" class="btn-cyber btn-cyber-pink btn-animate">
            接入神经网络
          </RouterLink>
          <RouterLink v-else to="/write" class="btn-cyber btn-cyber-pink btn-animate">写作</RouterLink>
        </div>
        <div ref="statsRef" class="hero-stats" :style="statsStyle">
          <div class="stat animate-item">
            <span class="stat-num neon">{{ stats.articles }}</span>
            <span class="stat-label">篇文章</span>
          </div>
          <div class="stat-sep">//</div>
          <div class="stat animate-item">
            <span class="stat-num neon-pink">{{ stats.users }}</span>
            <span class="stat-label">位作者</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Latest Articles -->
    <section class="latest-section page-container">
      <div ref="sectionHeaderRef" class="section-header" :style="sectionHeaderStyle">
        <h2 class="section-title">
          <span class="neon">></span> 最新日志
        </h2>
        <RouterLink to="/articles" class="btn-cyber btn-cyber-sm btn-animate">查看全部</RouterLink>
      </div>
      <div v-if="loading" class="loading">
        <div class="loader"></div>
        <span class="neon">LOADING...</span>
      </div>
      <div v-else ref="articlesGridRef" class="articles-grid">
        <ArticleCard 
          v-for="(article, index) in articles" 
          :key="article.id" 
          :article="article"
          :style="getArticleStyle(index)"
          class="article-card-animate"
        />
      </div>
    </section>

    <!-- 特效展示区域 -->
    <section class="effects-section page-container">
      <h2 class="section-title animate-slide-up">
        <span class="neon">></span> 视觉特效展示
      </h2>
      <div class="effects-grid">
        <div class="effect-card cyber-card gradient-border">
          <h3 class="effect-title neon animate-neon-flicker">霓虹闪烁</h3>
          <p class="effect-desc">动态光晕效果</p>
        </div>
        <div class="effect-card cyber-card">
          <h3 class="effect-title neon-pink animate-float">浮动动画</h3>
          <p class="effect-desc">3D悬浮效果</p>
        </div>
        <div class="effect-card cyber-card">
          <h3 class="effect-title animate-pulse-glow" style="color: var(--cyber-accent);">脉冲光晕</h3>
          <p class="effect-desc">呼吸灯效果</p>
        </div>
        <div class="effect-card cyber-card">
          <h3 class="effect-title animate-border-flow" style="font-size: 24px;">渐变边框</h3>
          <p class="effect-desc">流动边框</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { articleApi } from '@/api/article'
import { useUserStore } from '@/stores/user'
import ArticleCard from '@/components/ArticleCard.vue'
import type { Article } from '@/types'

const userStore = useUserStore()
const articles = ref<Article[]>([])
const loading = ref(true)
const stats = ref({ articles: 0, users: 0 })

// 动画状态
const isLoaded = ref(false)

// 动画元素引用
const badgeRef = ref<HTMLElement | null>(null)
const titleRef = ref<HTMLElement | null>(null)
const subtitleRef = ref<HTMLElement | null>(null)
const actionsRef = ref<HTMLElement | null>(null)
const statsRef = ref<HTMLElement | null>(null)
const sectionHeaderRef = ref<HTMLElement | null>(null)
const articlesGridRef = ref<HTMLElement | null>(null)

// 动画样式
const badgeStyle = computed(() => ({
  opacity: isLoaded.value ? 1 : 0,
  transform: isLoaded.value ? 'translateY(0)' : 'translateY(20px)',
  transition: 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
  transitionDelay: '0.2s'
}))

const titleStyle = computed(() => ({
  opacity: isLoaded.value ? 1 : 0,
  transform: isLoaded.value ? 'translateY(0) scale(1)' : 'translateY(30px) scale(0.9)',
  transition: 'all 0.8s cubic-bezier(0.4, 0, 0.2, 1)',
  transitionDelay: '0.4s'
}))

const subtitleStyle = computed(() => ({
  opacity: isLoaded.value ? 1 : 0,
  transform: isLoaded.value ? 'translateY(0)' : 'translateY(20px)',
  transition: 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
  transitionDelay: '0.6s'
}))

const actionsStyle = computed(() => ({
  opacity: isLoaded.value ? 1 : 0,
  transform: isLoaded.value ? 'translateY(0)' : 'translateY(20px)',
  transition: 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
  transitionDelay: '0.8s'
}))

const statsStyle = computed(() => ({
  opacity: isLoaded.value ? 1 : 0,
  transform: isLoaded.value ? 'translateY(0)' : 'translateY(20px)',
  transition: 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
  transitionDelay: '1s'
}))

const sectionHeaderStyle = computed(() => ({
  opacity: isLoaded.value ? 1 : 0,
  transform: isLoaded.value ? 'translateY(0)' : 'translateY(30px)',
  transition: 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
  transitionDelay: '0.2s'
}))

function getArticleStyle(index: number) {
  return {
    opacity: isLoaded.value ? 1 : 0,
    transform: isLoaded.value ? 'translateY(0) scale(1)' : `translateY(${40 + index * 10}px) scale(0.95)`,
    transition: `all 0.6s cubic-bezier(0.4, 0, 0.2, 1)`,
    transitionDelay: `${0.3 + index * 0.1}s`
  }
}

onMounted(async () => {
  try {
    const res = await articleApi.list(1, 6)
    if (res.code === 200) {
      articles.value = res.data.records
      stats.value.articles = res.data.total
    }
  } finally {
    loading.value = false
    // 触发入场动画
    setTimeout(() => {
      isLoaded.value = true
    }, 100)
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
.loading { 
  text-align: center; 
  padding: 60px; 
  font-family: var(--font-mono); 
  font-size: 14px; 
  letter-spacing: 3px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* 特效展示 */
.effects-section { padding: 60px 24px; }
.effects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
  margin-top: 32px;
}
.effect-card {
  padding: 32px;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.effect-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 245, 255, 0.1);
}
.effect-title {
  font-family: var(--font-cyber);
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
}
.effect-desc {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--cyber-text-muted);
  letter-spacing: 1px;
}
</style>
