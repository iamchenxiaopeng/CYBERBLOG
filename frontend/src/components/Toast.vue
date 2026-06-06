<template>
  <Transition name="toast">
    <div v-if="toastState.visible" class="cyber-toast" :class="toastState.type">
      <span class="toast-prefix">{{ prefix }}</span> {{ toastState.message }}
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { toastState } from '@/utils/toast'

const prefix = computed(() => {
  const t = toastState.type
  return t === 'success' ? '[OK]' : t === 'error' ? '[ERR]' : '[INFO]'
})
</script>

<style scoped>
.cyber-toast {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 9999;
  padding: 12px 20px;
  background: var(--cyber-bg-card);
  border: 1px solid var(--cyber-border);
  border-left: 3px solid;
  font-family: var(--font-mono);
  font-size: 13px;
  letter-spacing: 1px;
}
.cyber-toast.success { border-left-color: var(--cyber-green); color: var(--cyber-green); }
.cyber-toast.error   { border-left-color: var(--cyber-red);   color: var(--cyber-red);   }
.cyber-toast.info    { border-left-color: var(--cyber-primary); color: var(--cyber-primary); }
.toast-prefix { font-weight: bold; margin-right: 6px; }
.toast-enter-active, .toast-leave-active { transition: all 0.3s; }
.toast-enter-from, .toast-leave-to { transform: translateX(100px); opacity: 0; }
</style>
