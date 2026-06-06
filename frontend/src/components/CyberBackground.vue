<template>
  <div class="cyber-background">
    <!-- 粒子星空 -->
    <canvas ref="starsCanvas" class="stars-layer"></canvas>
    
    <!-- 流动网格 -->
    <div class="grid-layer">
      <div class="grid-move"></div>
    </div>
    
    <!-- 霓虹光线 -->
    <div class="neon-lines">
      <div class="line line-1"></div>
      <div class="line line-2"></div>
      <div class="line line-3"></div>
    </div>
    
    <!-- 光晕效果 -->
    <div class="glow-effects">
      <div class="glow glow-1"></div>
      <div class="glow glow-2"></div>
      <div class="glow glow-3"></div>
    </div>
    
    <!-- 漂浮粒子 -->
    <div class="floating-particles">
      <div v-for="i in 20" :key="i" class="particle" :class="`particle-${i}`"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const starsCanvas = ref<HTMLCanvasElement | null>(null)
let animationId: number | null = null

interface Star {
  x: number
  y: number
  size: number
  speed: number
  opacity: number
  twinkleSpeed: number
  twinklePhase: number
}

const stars: Star[] = []
const STAR_COUNT = 150

function initStars() {
  if (!starsCanvas.value) return
  const canvas = starsCanvas.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  for (let i = 0; i < STAR_COUNT; i++) {
    stars.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      size: Math.random() * 2 + 0.5,
      speed: Math.random() * 0.3 + 0.1,
      opacity: Math.random() * 0.8 + 0.2,
      twinkleSpeed: Math.random() * 0.02 + 0.01,
      twinklePhase: Math.random() * Math.PI * 2
    })
  }
}

function animateStars() {
  if (!starsCanvas.value) return
  const canvas = starsCanvas.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  stars.forEach(star => {
    // 闪烁效果
    star.twinklePhase += star.twinkleSpeed
    const twinkle = Math.sin(star.twinklePhase) * 0.3 + 0.7
    const currentOpacity = star.opacity * twinkle

    // 绘制星星
    ctx.beginPath()
    ctx.arc(star.x, star.y, star.size, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(0, 245, 255, ${currentOpacity})`
    ctx.fill()

    // 绘制光晕
    const gradient = ctx.createRadialGradient(
      star.x, star.y, 0,
      star.x, star.y, star.size * 3
    )
    gradient.addColorStop(0, `rgba(0, 245, 255, ${currentOpacity * 0.3})`)
    gradient.addColorStop(1, 'rgba(0, 245, 255, 0)')
    ctx.beginPath()
    ctx.arc(star.x, star.y, star.size * 3, 0, Math.PI * 2)
    ctx.fillStyle = gradient
    ctx.fill()

    // 移动星星
    star.y += star.speed
    if (star.y > canvas.height) {
      star.y = 0
      star.x = Math.random() * canvas.width
    }
  })

  animationId = requestAnimationFrame(animateStars)
}

function handleResize() {
  if (!starsCanvas.value) return
  starsCanvas.value.width = window.innerWidth
  starsCanvas.value.height = window.innerHeight
}

onMounted(() => {
  initStars()
  animateStars()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.cyber-background {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  background: linear-gradient(135deg, #050508 0%, #0a0a14 50%, #050508 100%);
}

/* 粒子星空层 */
.stars-layer {
  position: absolute;
  inset: 0;
  z-index: 1;
}

/* 流动网格层 */
.grid-layer {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(0, 245, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 245, 255, 0.03) 1px, transparent 1px);
  background-size: 80px 80px;
  z-index: 2;
  overflow: hidden;
}

.grid-move {
  position: absolute;
  inset: 0;
  background: 
    linear-gradient(180deg, 
      transparent 0%,
      rgba(0, 245, 255, 0.02) 50%,
      transparent 100%
    );
  animation: gridFlow 8s linear infinite;
}

@keyframes gridFlow {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100%); }
}

/* 霓虹光线 */
.neon-lines {
  position: absolute;
  inset: 0;
  z-index: 3;
  overflow: hidden;
}

.line {
  position: absolute;
  background: linear-gradient(90deg, transparent, var(--cyber-primary), transparent);
  height: 1px;
  opacity: 0.3;
  animation: lineMove 10s linear infinite;
}

.line-1 {
  width: 40%;
  top: 20%;
  animation-delay: 0s;
}

.line-2 {
  width: 60%;
  top: 50%;
  background: linear-gradient(90deg, transparent, var(--cyber-secondary), transparent);
  animation-delay: -3s;
  animation-duration: 15s;
}

.line-3 {
  width: 30%;
  top: 80%;
  animation-delay: -6s;
  animation-duration: 12s;
}

@keyframes lineMove {
  0% { left: -100%; opacity: 0; }
  10% { opacity: 0.3; }
  90% { opacity: 0.3; }
  100% { left: 100%; opacity: 0; }
}

/* 光晕效果 */
.glow-effects {
  position: absolute;
  inset: 0;
  z-index: 4;
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.15;
  animation: glowPulse 8s ease-in-out infinite;
}

.glow-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, var(--cyber-primary), transparent);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.glow-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, var(--cyber-secondary), transparent);
  bottom: -50px;
  right: -50px;
  animation-delay: -2s;
}

.glow-3 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(0, 255, 65, 0.5), transparent);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -4s;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.1; transform: scale(1); }
  50% { opacity: 0.2; transform: scale(1.1); }
}

/* 漂浮粒子 */
.floating-particles {
  position: absolute;
  inset: 0;
  z-index: 5;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: var(--cyber-primary);
  border-radius: 50%;
  opacity: 0;
  animation: particleFloat 15s ease-in-out infinite;
}

.particle:nth-child(odd) {
  background: var(--cyber-secondary);
}

.particle:nth-child(3n) {
  background: var(--cyber-green);
  width: 3px;
  height: 3px;
}

.particle-1 { left: 10%; top: 20%; animation-delay: 0s; }
.particle-2 { left: 20%; top: 40%; animation-delay: -1s; }
.particle-3 { left: 30%; top: 60%; animation-delay: -2s; }
.particle-4 { left: 40%; top: 80%; animation-delay: -3s; }
.particle-5 { left: 50%; top: 30%; animation-delay: -4s; }
.particle-6 { left: 60%; top: 50%; animation-delay: -5s; }
.particle-7 { left: 70%; top: 70%; animation-delay: -6s; }
.particle-8 { left: 80%; top: 90%; animation-delay: -7s; }
.particle-9 { left: 90%; top: 10%; animation-delay: -8s; }
.particle-10 { left: 15%; top: 85%; animation-delay: -9s; }
.particle-11 { left: 25%; top: 15%; animation-delay: -10s; }
.particle-12 { left: 35%; top: 45%; animation-delay: -11s; }
.particle-13 { left: 45%; top: 75%; animation-delay: -12s; }
.particle-14 { left: 55%; top: 25%; animation-delay: -13s; }
.particle-15 { left: 65%; top: 55%; animation-delay: -14s; }
.particle-16 { left: 75%; top: 35%; animation-delay: -1.5s; }
.particle-17 { left: 85%; top: 65%; animation-delay: -2.5s; }
.particle-18 { left: 95%; top: 45%; animation-delay: -3.5s; }
.particle-19 { left: 5%; top: 55%; animation-delay: -4.5s; }
.particle-20 { left: 50%; top: 95%; animation-delay: -5.5s; }

@keyframes particleFloat {
  0% {
    opacity: 0;
    transform: translateY(100vh) scale(0);
  }
  10% {
    opacity: 0.8;
    transform: translateY(80vh) scale(1);
  }
  90% {
    opacity: 0.8;
    transform: translateY(-20vh) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-40vh) scale(0);
  }
}

/* 噪点效果 */
.cyber-background::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  opacity: 0.03;
  z-index: 20;
  pointer-events: none;
}
</style>
