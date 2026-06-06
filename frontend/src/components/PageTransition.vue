<template>
  <Transition :name="transitionName" mode="out-in">
    <slot />
  </Transition>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const transitionName = ref('slide-left')

watch(() => route.path, (newPath, oldPath) => {
  const newDepth = newPath.split('/').length
  const oldDepth = oldPath?.split('/').length || 0
  
  transitionName.value = newDepth >= oldDepth ? 'slide-left' : 'slide-right'
})
</script>

<style>
/* 向左滑入（新页面从右进入） */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-left-enter-from {
  opacity: 0;
  transform: translateX(60px);
}

.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-60px);
}

/* 向右滑入（新页面从左进入） */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-right-enter-from {
  opacity: 0;
  transform: translateX(-60px);
}

.slide-right-leave-to {
  opacity: 0;
  transform: translateX(60px);
}

/* 淡入淡出 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 缩放淡入 */
.zoom-fade-enter-active,
.zoom-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.zoom-fade-enter-from {
  opacity: 0;
  transform: scale(0.95) translateY(20px);
}

.zoom-fade-leave-to {
  opacity: 0;
  transform: scale(1.05);
}
</style>
