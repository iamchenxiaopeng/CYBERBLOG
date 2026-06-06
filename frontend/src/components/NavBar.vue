<template>
  <nav class="navbar">
    <div class="navbar-bg"></div>
    <div class="page-container nav-inner">
      <RouterLink to="/" class="nav-logo">
        <span class="glitch neon" data-text="CYBERBLOG">CYBERBLOG</span>
      </RouterLink>
      <div class="nav-links">
        <RouterLink to="/" class="nav-link">
          <span class="link-text">主页</span>
          <span class="link-line"></span>
        </RouterLink>
        <RouterLink to="/articles" class="nav-link">
          <span class="link-text">文章</span>
          <span class="link-line"></span>
        </RouterLink>
        <RouterLink v-if="userStore.isLoggedIn" to="/write" class="nav-link">
          <span class="link-text">写作</span>
          <span class="link-line"></span>
        </RouterLink>
      </div>
      <div class="nav-actions">
        <template v-if="userStore.isLoggedIn">
          <span class="nav-username neon animate-float">{{ userStore.user?.username }}</span>
          <button class="btn-cyber btn-cyber-sm btn-animate" @click="logout">登出</button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="btn-cyber btn-cyber-sm btn-animate">登录</RouterLink>
          <RouterLink to="/register" class="btn-cyber btn-cyber-sm btn-cyber-pink btn-animate">注册</RouterLink>
        </template>
      </div>
    </div>
    <!-- 底部发光线 -->
    <div class="nav-glow-line"></div>
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
  background: rgba(5,5,8,0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  z-index: 1000;
  overflow: hidden;
}

.navbar-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, 
    rgba(0, 245, 255, 0.05) 0%,
    transparent 100%
  );
  pointer-events: none;
}

.nav-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 32px;
  position: relative;
  z-index: 1;
}

.nav-logo {
  font-family: var(--font-cyber);
  font-size: 18px;
  font-weight: 900;
  text-decoration: none;
  letter-spacing: 4px;
  animation: slideDown 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  position: relative;
  padding: 4px 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.link-text {
  position: relative;
  z-index: 1;
  transition: all 0.3s;
}

.link-line {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, 
    var(--cyber-primary), 
    var(--cyber-secondary)
  );
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 10px var(--cyber-primary);
}

.nav-link:hover .link-line,
.nav-link.router-link-active .link-line {
  width: 100%;
}

.nav-link:hover .link-text,
.nav-link.router-link-active .link-text {
  color: var(--cyber-primary);
  text-shadow: var(--cyber-glow);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  animation: slideDown 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.1s backwards;
}

.nav-username {
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 1px;
}

.nav-glow-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent,
    rgba(0, 245, 255, 0.3),
    rgba(0, 245, 255, 0.5),
    rgba(0, 245, 255, 0.3),
    transparent
  );
  animation: glowPulse 3s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

/* 悬停效果 */
.btn-animate:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(0, 245, 255, 0.3);
}
</style>
