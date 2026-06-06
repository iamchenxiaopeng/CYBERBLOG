<template>
  <div id="app-root">
    <!-- 动态背景 -->
    <CyberBackground />
    
    <NavBar />
    <main class="main-content">
      <PageTransition>
        <RouterView />
      </PageTransition>
    </main>
    <footer class="cyber-footer">
      <div class="page-container">
        <span class="neon">CYBERBLOG</span>
        <span class="text-muted"> // NEURAL NET ONLINE // {{ year }}</span>
      </div>
    </footer>
    <Toast />
  </div>
</template>

<script setup lang="ts">
import NavBar from '@/components/NavBar.vue'
import Toast from '@/components/Toast.vue'
import CyberBackground from '@/components/CyberBackground.vue'
import PageTransition from '@/components/PageTransition.vue'

const year = new Date().getFullYear()
</script>

<style>
.main-content {
  min-height: calc(100vh - 120px);
  padding-top: 70px;
}
.cyber-footer {
  border-top: 1px solid var(--cyber-border);
  padding: 20px 0;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--cyber-text-muted);
  text-align: center;
  letter-spacing: 2px;
}
.text-muted { color: var(--cyber-text-muted); }
</style>
