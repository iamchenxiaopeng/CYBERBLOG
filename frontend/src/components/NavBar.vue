<template>
  <nav class="navbar">
    <div class="page-container nav-inner">
      <RouterLink to="/" class="nav-logo">
        <span class="glitch neon" data-text="CYBERBLOG">CYBERBLOG</span>
      </RouterLink>
      <div class="nav-links">
        <RouterLink to="/" class="nav-link">主页</RouterLink>
        <RouterLink to="/articles" class="nav-link">文章</RouterLink>
        <RouterLink v-if="userStore.isLoggedIn" to="/write" class="nav-link">写作</RouterLink>
      </div>
      <div class="nav-actions">
        <template v-if="userStore.isLoggedIn">
          <span class="nav-username neon">{{ userStore.user?.username }}</span>
          <button class="btn-cyber btn-cyber-sm" @click="logout">登出</button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="btn-cyber btn-cyber-sm">登录</RouterLink>
          <RouterLink to="/register" class="btn-cyber btn-cyber-sm btn-cyber-pink">注册</RouterLink>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

function logout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0; left: 0; right: 0;
  height: 60px;
  background: rgba(5,5,8,0.92);
  border-bottom: 1px solid var(--cyber-border);
  backdrop-filter: blur(12px);
  z-index: 1000;
}
.nav-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 32px;
}
.nav-logo {
  font-family: var(--font-cyber);
  font-size: 18px;
  font-weight: 900;
  text-decoration: none;
  letter-spacing: 4px;
}
.nav-links {
  display: flex;
  gap: 24px;
  flex: 1;
}
.nav-link {
  font-family: var(--font-cyber);
  font-size: 11px;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: var(--cyber-text-muted);
  text-decoration: none;
  transition: color 0.2s;
}
.nav-link:hover, .nav-link.router-link-active {
  color: var(--cyber-primary);
  text-shadow: var(--cyber-glow);
}
.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.nav-username {
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 1px;
}
</style>
