<template>
  <article class="article-card cyber-card animate-card-3d" @click="router.push(`/articles/${article.id}`)">
    <div v-if="article.coverImg" class="card-cover">
      <div class="cover-overlay"></div>
      <img :src="article.coverImg" :alt="article.title" />
    </div>

    <!-- 编辑/删除按钮：右上角悬浮，hover 显示 -->
    <div v-if="canEdit || canDelete" class="card-admin-actions">
      <button v-if="canEdit" class="btn-admin btn-admin-edit" title="编辑" @click.stop.prevent="router.push(`/write/${article.id}`)">✎</button>
      <button v-if="canDelete" class="btn-admin btn-admin-del" title="删除" @click.stop.prevent="handleDelete">✕</button>
    </div>

    <div class="card-body">
      <!-- 元信息行：作者 + 日期 -->
      <div class="card-meta">
        <img :src="article.avatarUrl || '/avatar-default.svg'" :alt="article.username" class="cyber-avatar" @error="(e: Event) => (e.target as HTMLImageElement).src = '/avatar-default.svg'" />
        <span class="meta-author">{{ article.username }}</span>
        <span class="meta-sep">//</span>
        <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
      </div>

      <!-- 标题 -->
      <h2 class="card-title">{{ article.title }}</h2>

      <!-- 摘要 -->
      <p class="card-summary">{{ article.summary }}</p>

      <!-- 底部区域：标签 + 统计（同一行） -->
      <div class="card-footer">
        <div v-if="parseTags(article.tags).length" class="card-tags">
          <span v-for="tag in parseTags(article.tags)" :key="tag" class="cyber-tag">{{ tag }}</span>
        </div>
        <div class="card-stats">
          <span class="stat-item"><i class="stat-icon">👁</i> {{ article.viewCount }}</span>
          <span :class="['stat-item', 'like-btn']" :style="liked ? { color: 'var(--cyber-red)' } : {}" @click.stop="handleLike">
            <i class="stat-icon">{{ liked ? '♥' : '♡' }}</i> {{ likeCount }}
          </span>
          <span class="stat-item"><i class="stat-icon">💬</i> {{ article.commentCount }}</span>
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api/article'
import { likeApi } from '@/api/interact'
import { showToast } from '@/utils/toast'
import type { Article } from '@/types'

const emit = defineEmits<{ deleted: [id: number] }>()
const props = defineProps<{ article: Article }>()

const router = useRouter()
const userStore = useUserStore()
const liked = ref(props.article.liked)
const likeCount = ref(props.article.likeCount)

async function handleLike(e: MouseEvent) {
  e.stopPropagation()
  try {
    const res = await likeApi.toggle('article', props.article.id)
    if (res.code === 200) {
      liked.value = res.data.liked
      likeCount.value = Math.max(0, likeCount.value + (res.data.liked ? 1 : -1))
    }
  } catch (e: any) {
    showToast(e.message || '操作失败', 'error')
  }
}

// 是否显示删除按钮：已登录 且 (是作者 或 是管理员)
const canDelete = computed(() => {
  if (!userStore.user) return false
  return userStore.user.id === props.article.userId || userStore.user.username === 'admin'
})

// 是否显示编辑按钮：同删除权限
const canEdit = computed(() => {
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
/* ════════════════════════════════
   ArticleCard — 卡片组件样式
   ════════════════════════════════ */
.article-card {
  cursor: pointer;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
}

/* ── 管理按钮（右上角悬浮，hover 淡入） ── */
.card-admin-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 15;
  display: flex;
  gap: 4px;
  opacity: 0;
  transform: translateY(-3px);
  transition: all 0.2s ease;
}
.article-card:hover .card-admin-actions {
  opacity: 1;
  transform: translateY(0);
}

.btn-admin {
  width: 22px; height: 22px;
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 10px; border-radius: 3px;
  cursor: pointer; border: 1px solid transparent;
  padding: 0; transition: all 0.2s; line-height: 1;
}
.btn-admin-edit {
  color: var(--cyber-primary);
  background: rgba(0, 245, 255, 0.12);
  border-color: rgba(0, 245, 255, 0.3);
}
.btn-admin-edit:hover {
  background: var(--cyber-primary); color: var(--cyber-bg);
  box-shadow: 0 0 10px rgba(0, 245, 255, 0.4);
}
.btn-admin-del {
  color: var(--cyber-red);
  background: rgba(255, 50, 80, 0.12);
  border-color: rgba(255, 50, 80, 0.3);
}
.btn-admin-del:hover {
  background: var(--cyber-red); color: #fff;
  box-shadow: 0 0 10px rgba(255, 50, 80, 0.4);
}

/* ── 封面图 ── */
.card-cover {
  height: 150px;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
}
.card-cover img {
  width: 100%; height: 100%;
  object-fit: cover; opacity: 0.6;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.cover-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(180deg, transparent 0%, var(--cyber-bg-card) 100%);
  z-index: 1; transition: opacity 0.3s;
}
.article-card:hover .card-cover img { opacity: 1; transform: scale(1.08); }

/* ── 卡片主体（flex 纵向，等高对齐） ── */
.card-body {
  padding: 16px 18px 14px;
  position: relative; z-index: 2;
  display: flex; flex-direction: column; flex: 1;
}

/* ── 作者/日期元信息行 ── */
.card-meta {
  display: flex; align-items: center; gap: 7px;
  font-family: var(--font-mono); font-size: 11px;
  color: var(--cyber-text-muted); margin-bottom: 7px;
  flex-shrink: 0;
}
.meta-sep   { color: var(--cyber-primary); opacity: 0.5; }
.meta-author { color: var(--cyber-primary); }

/* ── 标题 ── */
.card-title {
  font-family: var(--font-cyber); font-size: 14.5px; font-weight: 700;
  color: var(--cyber-text); margin-bottom: 7px; line-height: 1.45;
  transition: all 0.3s; flex-shrink: 0;
}
.article-card:hover .card-title {
  color: var(--cyber-primary);
  text-shadow: 0 0 16px rgba(0, 245, 255, 0.4);
}

/* ── 摘要（弹性填充，等高） ── */
.card-summary {
  font-size: 11.5px; color: var(--cyber-text-muted); line-height: 1.65;
  margin-bottom: 10px; display: -webkit-box;
  -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;
  transition: color 0.3s; flex: 1; min-height: 3em;
}
.article-card:hover .card-summary { color: var(--cyber-text); opacity: 0.75; }

/* ── 底部栏：标签 | 统计（单行布局） ── */
.card-footer {
  flex-shrink: 0;
  display: flex; align-items: center; gap: 12px;
  padding-top: 6px;
  border-top: 1px solid rgba(0, 245, 255, 0.06);
}

/* 标签区 */
.card-tags {
  display: flex; flex-wrap: wrap; gap: 5px;
  flex-shrink: 0;
  max-width: 60%;
}

/* 统计数据区（靠右紧凑排列） */
.card-stats {
  display: flex; gap: 11px; margin-left: auto;
  font-family: var(--font-mono); font-size: 10.5px;
  color: var(--cyber-text-muted); opacity: 0.5;
  white-space: nowrap; transition: opacity 0.25s;
}
.article-card:hover .card-stats { opacity: 0.85; }

.stat-item { display: inline-flex; align-items: center; gap: 2.5px; transition: all 0.2s; }
.stat-icon { font-size: 9.5px; line-height: 1; opacity: 0.7; }
.article-card:hover .stat-item { color: var(--cyber-primary); }
.article-card:hover .stat-icon { opacity: 1; }

/* 点赞交互 */
.like-btn { cursor: pointer; }
.like-btn:hover { color: var(--cyber-red) !important; }

/* ── 边角装饰（hover 时显示） ── */
.card-corner {
  position: absolute; width: 18px; height: 18px;
  border-color: var(--cyber-primary); border-style: solid;
  opacity: 0; transition: all 0.3s; z-index: 10;
}
.card-corner-tl { top: 0; left: 0;  border-width: 2px 0 0 2px; }
.card-corner-tr { top: 0; right: 0; border-width: 2px 2px 0 0; }
.card-corner-bl { bottom: 0; left: 0; border-width: 0 0 2px 2px; }
.card-corner-br { bottom: 0; right: 0; border-width: 0 2px 2px 0; }

.article-card:hover .card-corner { opacity: 1; }
.article-card:hover .card-corner-tl,
.article-card:hover .card-corner-tr,
.article-card:hover .card-corner-bl,
.article-card:hover .card-corner-br { width: 26px; height: 26px; }

/* ── hover 发光边框 ── */
.article-card::after {
  content: ''; position: absolute; inset: 0;
  border: 2px solid transparent;
  background: linear-gradient(90deg, var(--cyber-primary), var(--cyber-secondary)) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor; mask-composite: exclude;
  opacity: 0; transition: opacity 0.3s; pointer-events: none;
}
.article-card:hover::after { opacity: 0.5; }
</style>
